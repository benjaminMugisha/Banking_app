package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Exception.LoanAlreadyPaidException;
import com.benjamin.Banking_app.Transactions.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionServiceImpl transactionService;
    private final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    @Override
    public LoanResponse applyForLoan(LoanRequest request) {
        logger.info("loan application initiated");
        long accountId = request.getAccountId();
        double income = request.getIncome(); //yearly income
        double principal = request.getPrincipal();
        int monthsToRepay = request.getMonthsToRepay();
        double monthlyPayment = calculateMonthlyInstallment(principal, monthsToRepay);
        double yearlyPayment = monthlyPayment * 12;

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("account with id: " + accountId + " not found"));

        // Check if the loan is affordable by checking if Debt-to-Income (DTI) ratio exceeds 40%
        if (!isLoanAffordable(income, yearlyPayment, accountId)) {
            return new LoanResponse("Loan denied due to high debt-to-income ratio", null);
        }

        LocalDateTime startDate = LocalDateTime.now();

        // Create and save loan
        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setPrincipal(principal);
        loan.setRemainingBalance(principal);
        loan.setStartDate(startDate);
        loan.setAmountToPayEachMonth(monthlyPayment);

        loanRepository.save(loan);

        //save the loan transaction
        transactionService.recordTransaction(account,
                "LOAN_APPLICATION",
                loan.getRemainingBalance(),
                "loan created. the initial amount is: " + principal, null, null);

        scheduleFirstRepayment(loan);
        return new LoanResponse("loan accepted: ", loan);
    }
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public void scheduleFirstRepayment(Loan loan) {
        long initialDelay = ChronoUnit.MILLIS.between(LocalDateTime.now(), loan.getStartDate().plusDays(30));
        long period = TimeUnit.DAYS.toMillis(30); // Run every 30 days

        scheduler.scheduleAtFixedRate(() -> processMonthlyRepayment(loan.getLoanId(), loan.getAmountToPayEachMonth()),
                initialDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public LoanPageResponse getAllLoans(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Loan> loans = loanRepository.findAll(pageable);
        List<Loan> content = loans.getContent();
        LoanPageResponse response = new LoanPageResponse();
        response.setContent(content);
        response.setPageNo(loans.getNumber());
        response.setPageSize(loans.getSize());
        response.setTotalElements(loans.getTotalElements());
        response.setTotalPages(loans.getTotalPages());
        response.setLast(loans.isLast());

        return response;
    }

    @Override
    public Loan getLoanByLoanId(long loanId) {
        logger.info("searching for loan: {}", loanId);
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));
    }

    //all loans including both active and fully repaid loans.
    @Override
    public List<Loan> getLoansByAccountId(long accountId) {
        return loanRepository.findByAccountId(accountId);
    }

    //repay the whole loan early. comes with a penalty
    @Override
    public LoanResponse repayLoanEarly(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));
        Account account = loan.getAccount();
        double accountBalance = account.getBalance();
        if (loan.getRemainingBalance() > accountBalance) {
            return new LoanResponse("insufficient funds to clear the loan.", null);
        }

        double totalPayment = loan.getRemainingBalance() * 0.02; //2% penalty
        if (totalPayment > accountBalance) {
            return new LoanResponse("insufficient funds to clear the loan with the 2% penalty included.", null);
        }

        //loan fully repaid:
        loan.setRemainingBalance(0);
        loanRepository.save(loan);

        account.setBalance(accountBalance - totalPayment); //updating our user's balance.

        return new LoanResponse("Loan of fully repaid early successfully, 2% penalty applied");
    }

    @Override
    public double calculateMonthlyInstallment(double principal, int monthsToRepay) {
        // annualInterestRate is 5 hence 0.05
        double monthlyInterestRate = 0.005/12;
        double timeInYears =  (monthsToRepay/12.0);

        double totalLoan = principal + (principal * monthlyInterestRate * timeInYears );

        // Return the ceiling to ensure no underpayment
        return Math.ceil(totalLoan/monthsToRepay);
    }

    @Override
    public void deleteLoan(long loanId) {
        loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId + " not found"));

        loanRepository.deleteById(loanId);
    }

    @Override
    public boolean isLoanAffordable(double yearlyIncome, double estimatedYearlyPayment, long accountId) {
        // Get all active loans for the account
        List<Loan> activeLoans = loanRepository.findByAccountIdAndRemainingBalanceGreaterThan(accountId, 0.0);

        // Calculate yearly payment for all existing loans
        double yearlyPaymentOfExistingLoans = activeLoans.stream()
                .mapToDouble(loan -> loan.getAmountToPayEachMonth() * 12)
                .sum();

        // Total yearly loan payments (existing + new loan)
        double totalYearlyPayment = yearlyPaymentOfExistingLoans + estimatedYearlyPayment;

        // Debt-to-Income (DTI) Ratio Calculation
        double dti = (totalYearlyPayment / yearlyIncome) * 100;

        // Loan is affordable if DTI <= 40%
        return dti <= 40;
    }

    @Override
    public LoanResponse processMonthlyRepayment(Long loanId, double amount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan with id: " + loanId + " not found"));

        Account account = loan.getAccount();

        // Check if the loan is already fully paid
        if (loan.getRemainingBalance() <= 0) {
            throw new LoanAlreadyPaidException("loan fully paid", HttpStatus.NOT_FOUND);
        }
        // using default if the amount <=0
        double paymentAmount = (amount <= 0) ? loan.getAmountToPayEachMonth() : amount;

        if (paymentAmount > loan.getRemainingBalance()){  //test  for this scenario only
            paymentAmount = loan.getRemainingBalance();
        }

        // Check if the account has sufficient funds
        if (account.getBalance() < loan.getAmountToPayEachMonth()) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // update:
        loan.setRemainingBalance(loan.getRemainingBalance() - paymentAmount);
        account.setBalance(account.getBalance() - paymentAmount);
//        loan.setAmountPaid(loan.getAmountPaid() + paymentAmount);

        // Save the updated loan and account
        loanRepository.save(loan);
        accountRepository.save(account);

        //recording the transaction.
        transactionService.recordTransaction(account,
                "LOAN_REPAYMENT",
                paymentAmount,
                "this month's loan repaid. the amount is: " + loan.getAmountToPayEachMonth()
                , null, null);
        logger.info(" loan: {} repaid for this month successfully", loan.getLoanId());

//        return new LoanResponse("this month's Loan repaid successfully ");
        return new LoanResponse("Loan repayment of " + paymentAmount + " processed successfully. Remaining balance is: "
                + loan.getRemainingBalance());
    }
}