package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.UserUtils;
import com.benjamin.Banking_app.Transactions.TransactionService;
import com.benjamin.Banking_app.Transactions.TransactionType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final UserUtils userUtils;

    private final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    @Transactional
    public LoanResponse applyForLoan(LoanRequest request) {
        logger.info("Loan application initiated for principal: {}", request.getPrincipal());

        Account account = userUtils.getCurrentUserAccount();

        BigDecimal monthlyPayment = calculateMonthlyInstallment(request.getPrincipal(), request.getMonthsToRepay());
        BigDecimal yearlyPayment = monthlyPayment.multiply(BigDecimal.valueOf(12));
        BigDecimal fullLoanBalance = monthlyPayment.multiply(BigDecimal.valueOf(request.getMonthsToRepay()));

        LoanEligibilityResult eligibility = checkEligibility(request.getIncome(), yearlyPayment, account.getId());
        if (!eligibility.isAffordable()) {
            logger.warn("Loan denied of account: {} because their DTI is: {}%",
                    account.getAccountUsername(), eligibility.dti());
            return new LoanResponse("Loan denied. your Debt-to-income ratio is too high:" +
                    "  (" + eligibility.dti() + "%). maximum is 40% of your yearly income )");
        }

        Loan loan = Loan.builder()
                .account(account).principal(request.principal)
                .amountToPayEachMonth(monthlyPayment)
                .remainingBalance(fullLoanBalance).startDate(LocalDate.now())
                .build();
        loan.setNextPaymentDate(LocalDate.now().plusDays(30));

        loanRepository.save(loan);
        recordLoanTransaction(account,
                loan.getRemainingBalance(), TransactionType.LOAN_APPLICATION);

        return new LoanResponse(
                "Loan accepted: ", LoanMapper.mapToDto(loan)
        );
    }

    private LoanEligibilityResult checkEligibility(
            BigDecimal yearlyIncome, BigDecimal estimatedYearlyPayment, long accountId) {
        List<Loan> activeLoans = loanRepository
                .findByAccountIdAndRemainingBalanceGreaterThan(accountId, 0.0);

        BigDecimal yearlyPaymentOfExistingLoans = activeLoans.stream()
                .map(loan ->
                        loan.getAmountToPayEachMonth().multiply(BigDecimal.valueOf(12)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalYearlyPayment = yearlyPaymentOfExistingLoans.add(estimatedYearlyPayment);
        BigDecimal dti = totalYearlyPayment.divide(yearlyIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new LoanEligibilityResult(dti.compareTo(BigDecimal.valueOf(40)) <= 0, dti);
    }


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processDueLoanRepayments() {
        LocalDate today = LocalDate.now();
        List<Loan> dueLoans =
                loanRepository.findByRemainingBalanceGreaterThanAndNextPaymentDate(0.0, today);

        for (Loan loan : dueLoans) {
            try {
                Account account = loan.getAccount();

                if (account.getBalance().compareTo(loan.getAmountToPayEachMonth()) < 0) {
                        logger.warn("Insufficient funds for loan repayment. Loan ID: {}, Account: {}",
                                loan.getLoanId(), account.getAccountUsername());
                        continue;
                }
                account.setBalance(account.getBalance().subtract(loan.getAmountToPayEachMonth()));
                loan.setRemainingBalance(loan.getRemainingBalance().subtract(loan.getAmountToPayEachMonth()));
                if(loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0){
                    loan.setRemainingBalance(BigDecimal.ZERO);
                    loan.setActive(false);
                } else {
                    loan.setNextPaymentDate(today.plusDays(30));
                }

                accountRepository.save(account);
                loanRepository.save(loan);

                recordLoanTransaction(account, loan.getAmountToPayEachMonth(), TransactionType.LOAN_REPAYMENT);
            } catch (Exception e) {
                logger.error("Failed to process repayment for Loan ID: {}",
                        loan.getLoanId(), e);
            }
        }
    }

    @Override
    public LoanPageResponse getLoansOfAnAccount(int pageNo, int pageSize, String accountUsername) {
        Account account;
        if (isAdmin() && accountUsername != null ) {
            account = accountRepository.findByAccountUsername(accountUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountUsername));
        } else {
             account = userUtils.getCurrentUserAccount();
        }
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Loan> loans = loanRepository.findByAccountIdAndActiveTrue(account.getId(), pageable);

        List<LoanDto> loanDtos = loans.getContent().stream()
                .map(LoanMapper::mapToDto)
                .collect(Collectors.toList());

        return LoanPageResponse.builder()
                .content(loanDtos).pageNo(loans.getNumber())
                .pageSize(loans.getSize()).totalElements(loans.getTotalElements())
                .last(loans.isLast())
                .build();
    }

    @Override
    @Transactional
    public LoanResponse repayLoanEarly(Long loanId) {
        Account currentUser = userUtils.getCurrentUserAccount();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));

        if (!loan.getAccount().getId().equals(currentUser.getId())) {
            return new LoanResponse("You can not repay someone else's loan.");
        }

        Account account = loan.getAccount();
        BigDecimal accountBalance = account.getBalance();
        BigDecimal loanBalance = loan.getRemainingBalance();

        if(loanBalance.compareTo(accountBalance) > 0) {
            return new LoanResponse("insufficient funds to clear the loan.");
        }

        account.setBalance(accountBalance.subtract(loanBalance));
        loan.setRemainingBalance(BigDecimal.valueOf(0));
        loan.setActive(false);

        recordLoanTransaction(account, loanBalance, TransactionType.LOAN_REPAYMENT);

        loanRepository.save(loan);
        accountRepository.save(account);

        return new LoanResponse(
                "Loan of €" + loanBalance +
                        " fully repaid. your remaining balance is: €" + account.getBalance());
    }

    @Override
    public LoanDto getLoanByLoanId(long loanId) {
        logger.info("searching for loan with id: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));
        return LoanMapper.mapToDto(loan);
    }

    //✅ private helper methods:
    private BigDecimal calculateMonthlyInstallment(BigDecimal principal, int monthsToRepay) {
        BigDecimal annualInterestRate = BigDecimal.valueOf(0.05); // 5% annual interest rate.
        BigDecimal timeInYears = BigDecimal.valueOf(monthsToRepay)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal totalInterest = principal.multiply(annualInterestRate).multiply(timeInYears);
        BigDecimal totalLoan = principal.add(totalInterest);

        return totalLoan.divide(BigDecimal.valueOf(monthsToRepay), 2, RoundingMode.HALF_UP);
    }

    private void recordLoanTransaction(Account account, BigDecimal amount, TransactionType type) {
        transactionService.recordTransaction(
                account,
                type,
                amount,
                null
        );
    }

    private boolean isAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
