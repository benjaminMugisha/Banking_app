package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Exception.BadRequestException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final AccountServiceImpl accountService;
    private final UserUtils userUtils;

    private final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    @Transactional
    public LoanResponse applyForLoan(LoanRequest request) {

        Account account = userUtils.getCurrentUserAccount();
        logger.info("{} is applying for loan", account.getUser().getEmail());

        List<Loan> activeLoans = loanRepository.findByAccountIdAndActiveTrue(account.getId());
        if(activeLoans.size() >= 3) {
            throw new BadRequestException("You have reached the maximum of 3 active loans");
        }
        Loan recentLoan = loanRepository.findTopByAccountIdOrderByStartDateDesc(account.getId());
        if(recentLoan != null) {
            LocalDateTime nextTimeLoan = recentLoan.getStartDate().plusHours(24);
            if(LocalDateTime.now().isBefore(nextTimeLoan))
                throw new BadRequestException("you must wait 24 hours before next loan application");
        }

        BigDecimal monthlyPayment = calculateMonthlyInstallment(request.getPrincipal(), request.getMonthsToRepay());
        BigDecimal yearlyPayment = monthlyPayment.multiply(BigDecimal.valueOf(12));
        BigDecimal fullLoanBalance = monthlyPayment.multiply(BigDecimal.valueOf(request.getMonthsToRepay()));

        LoanEligibilityResult eligibility = checkEligibility(request.getIncome(), yearlyPayment, account.getId());
        if (!eligibility.isAffordable()) {
            logger.warn("Loan denied of account: {} because their DTI is: {}%",
                    account.getUser().getEmail(), eligibility.dti());
            throw new InsufficientFundsException("Your Debt-to-income ratio of " + eligibility.dti() +
                    "% is too high maximum dti allowed is 40%");
        }

        Loan loan = Loan.builder()
                .account(account).principal(request.principal)
                .amountToPayEachMonth(monthlyPayment)
                .remainingBalance(fullLoanBalance).startDate(LocalDateTime.now())
                .build();
        loan.setNextPaymentDate(LocalDate.now().plusDays(30));

        loanRepository.save(loan);
        accountService.deposit(request.principal);
        recordLoanTransaction(account,
                loan.getRemainingBalance(), TransactionType.LOAN_APPLICATION, account.getBalance());

        return new LoanResponse(
                "Loan accepted: ", LoanMapper.mapToDto(loan)
        );
    }

    LoanEligibilityResult checkEligibility(
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
                                loan.getLoanId(), account.getUser().getEmail());
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

                recordLoanTransaction(account, loan.getAmountToPayEachMonth(),
                        TransactionType.LOAN_REPAYMENT, account.getBalance());
            } catch (Exception e) {
                logger.error("Failed to process repayment for Loan ID: {}",
                        loan.getLoanId(), e);
            }
        }
    }

    @Override
    public LoanPageResponse getLoansOfAnAccount(int pageNo, int pageSize, String email) {
//        Account account = accountRepository.findByUserEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + email));
        Account account;
        if (isAdmin() && email != null ) {
            account = accountRepository.findByUserEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + email));
        } else {
             account = userUtils.getCurrentUserAccount();
        }
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Loan> loans = loanRepository.findByAccountIdAndActiveTrue(account.getId(), pageable);

        List<LoanDto> loanDtos = loans.getContent().stream()
                .map(LoanMapper::mapToDto)
                .collect(Collectors.toList());
        logger.info("returning all loans of user: {}", account.getUser().getEmail());
        int totalPages = loans.getTotalPages() == 0 ? 1 : loans.getTotalPages();

        return LoanPageResponse.builder()
                .content(loanDtos).pageNo(loans.getNumber())
                .pageSize(loans.getSize()).totalElements(loans.getTotalElements())
                .totalPages(totalPages)
                .last(loans.isLast())
                .build();
    }

    @Override
    @Transactional
    public LoanResponse repayLoanEarly(Long loanId) {
        Account currentUser = userUtils.getCurrentUserAccount();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan not found"));

        if (!loan.getAccount().getId().equals(currentUser.getId())) {
            return new LoanResponse("You can not repay someone else's loan.");
        }

//        if(loan.getRemainingBalance().compareTo(BigDecimal.ZERO)) {}
        if(loan.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
            return new LoanResponse("loan already fully paid.");
        }
        Account account = loan.getAccount();
        BigDecimal accountBalance = account.getBalance();
        BigDecimal loanBalance = loan.getRemainingBalance();

        if (loanBalance.compareTo(accountBalance) > 0) {
            return new LoanResponse("insufficient funds to clear the loan.");
        }

        account.setBalance(accountBalance.subtract(loanBalance));
        loan.setRemainingBalance(BigDecimal.valueOf(0));
        loan.setActive(false);

        loanRepository.save(loan);
        accountRepository.save(account);
        recordLoanTransaction(account, loanBalance, TransactionType.FULL_LOAN_REPAYMENT, account.getBalance());

        return new LoanResponse(
                "Loan of €" + loanBalance +
                        " fully repaid. your remaining balance is: €" + account.getBalance());
    }

    @Override
    @Transactional
    public LoanResponse repayCustomAmount (Long loanId, BigDecimal amount) {
        Account currentUser = userUtils.getCurrentUserAccount();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan not found"));

        if (!loan.getAccount().getId().equals(currentUser.getId())) {
            return new LoanResponse("You can not repay someone else's loan.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return new LoanResponse("Repayment amount must be greater than 0");

        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
            return new LoanResponse("loan already fully paid.");
        }

        Account account = loan.getAccount();
        BigDecimal accountBalance = account.getBalance();
        BigDecimal loanBalance = loan.getRemainingBalance();

        if (amount.compareTo(loanBalance) > 0) {
//            BigDecimal newAmount = loanBalance;
            account.setBalance(accountBalance.subtract(loanBalance));
            loan.setRemainingBalance(BigDecimal.valueOf(0));
            loan.setActive(false);
            return new LoanResponse("loan fully repaid with " + loanBalance +
                    ". your new balance is " + account.getBalance());
        }

        if (amount.compareTo(accountBalance) > 0) {
            return new LoanResponse("insufficient funds to clear the loan.");
        }

        account.setBalance(accountBalance.subtract(amount));
        loan.setRemainingBalance(loanBalance.subtract(amount));

        loanRepository.save(loan);
        accountRepository.save(account);

        recordLoanTransaction(account, amount, TransactionType.LOAN_REPAYMENT, account.getBalance());

        logger.info("user: {}, amount of: {}, loan balance: {}", currentUser.getUser().getEmail(),
                amount, loan.getRemainingBalance());

        return new LoanResponse(
                "amount of €" + amount +
                        " repaid. your remaining loan balance is: €" + loan.getRemainingBalance() +
        " and your remaining account balance is €" + accountBalance);
    }

    @Override
    public LoanDto getLoanByLoanId(long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));
        logger.info("returning a loan with id: {} that belongs to: {}"
                , loanId, loan.getAccount().getUser().getEmail());

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

    private void recordLoanTransaction(Account account, BigDecimal amount, TransactionType type, BigDecimal balance) {
        transactionService.recordTransaction(
                account, type,
                amount, null, balance
        );
    }

    private boolean isAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public LoanPageResponse getAllLoans(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Loan> loans = loanRepository.findAll(pageable);

        List<LoanDto> content = loans.stream()
                .map(LoanMapper::mapToDto)
                .toList();
        int totalPages = loans.getTotalPages() == 0 ? 1 : loans.getTotalPages();
        return LoanPageResponse.builder()
                .content(content).pageNo(loans.getNumber())
                .pageSize(loans.getSize()).totalElements(loans.getTotalElements())
                .totalPages(totalPages)
                .build();
    }
}
