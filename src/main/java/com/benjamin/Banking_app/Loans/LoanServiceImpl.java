package com.benjamin.Banking_app.Loans;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.Exception.InsufficientFundsException;
import com.benjamin.Banking_app.Transactions.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService{

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
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
                .orElseThrow(() -> new EntityNotFoundException( "account with id: " + accountId + " not found"));

        // Check if the loan is affordable by checking if Debt-to-Income (DTI) ratio exceeds 40%
        if (!isLoanAffordable(income, yearlyPayment, accountId)) {
            logger.info("loan denied due to high dti");
            return new LoanResponse("Loan denied due to high debt-to-income ratio", null);
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(monthsToRepay);

        // Create and save loan
        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setPrincipal(principal);
        loan.setLoanAmount(principal);
        loan.setInterestRate(5); // Fixed interest rate of 5%
        loan.setRemainingBalance(principal);
        loan.setAmountPaid(0);
        loan.setStartDate(startDate);
        loan.setEndDate(endDate);
        loan.setAmountToPayEachMonth(monthlyPayment);

        loanRepository.save(loan);

        //save the loan transaction
        transactionService.recordTransaction(account,
                "LOAN ",
                loan.getRemainingBalance(),
                "loan created. the initial amount is: " + principal);
        logger.info(" loan: {} created successfully",loan.getLoanId());
        return new LoanResponse("loan accepted: " , loan);
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Loan getLoanByLoanId(long loanId){
        logger.info("searching for loan: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId +  " not found"));
        return loan;
    }

    //all loans including both active and fully repaid loans.
    @Override
    public List<Loan> getLoansByAccountId(long accountId) {
        return loanRepository.findByAccountId(accountId);
    }

    //repay the whole loan early
    @Override
    public LoanResponse repayLoanEarly(Long loanId, double paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId +  " not found"));

        double remainingBalance = loan.getRemainingBalance();
        if (paymentAmount < remainingBalance) {
            return new LoanResponse("Payment amount is insufficient to clear the loan.", null);
        }

        double penalty = remainingBalance * 0.02; // 2% prepayment penalty
        double totalPayment = remainingBalance + penalty;

        if (paymentAmount < totalPayment) {
            return new LoanResponse("Payment amount is insufficient to clear the loan with penalty.", null);
        }

        loan.setRemainingBalance(0);
        loan.setAmountPaid(loan.getAmountPaid() + remainingBalance);
        loan.setEndDate(LocalDateTime.now()); // Update the end date to mark it as paid
        loanRepository.save(loan);

        transactionService.recordTransaction(
                loan.getAccount(),
                "LOAN EARLY REPAYMENT",
                0,
                "Loan repaid early with a penalty of " + penalty
        );
        logger.info("loan: {} is fully repaid", loan.getLoanId());
        return new LoanResponse("Loan repaid early successfully, penalty applied: " + penalty, loan);
    }

    @Override
    public void processMonthlyRepayments() {
        List<Loan> activeLoans = loanRepository.findByRemainingBalanceGreaterThan(0.0); // get all active loans

        for (Loan loan : activeLoans) {
            double monthlyRepayment = loan.getAmountToPayEachMonth();
            double remainingBalance = loan.getRemainingBalance();
            //double principal = loan.getPrincipal();
            double amountPaid = loan.getAmountPaid();
            Account account = loan.getAccount();

            if (account.getBalance() >= monthlyRepayment) {
                // Deduct the payment from the account balance
                account.setBalance(account.getBalance() - monthlyRepayment);
                accountRepository.save(account);

                // Update the loan balance
                loan.setRemainingBalance(remainingBalance - monthlyRepayment);
                loan.setAmountPaid(amountPaid + monthlyRepayment);
                if (remainingBalance < 0) {
                    loan.setRemainingBalance(0); // Avoid negative balances
                }
                loanRepository.save(loan);

                // Record the repayment as a transaction
                transactionService.recordTransaction(account,
                        "LOAN REPAYMENT",
                        -monthlyRepayment,
                        "Monthly repayment for loan ID: " + loan.getLoanId());
            } else {
                // Optional: Handle insufficient funds (e.g., send a warning)
                throw new InsufficientFundsException("insufficient funds to repay this month");
            }
        }
    }

    @Override
    public double calculateMonthlyInstallment(double principal, int monthsToRepay) {
        double annualInterestRate = 0.05 ; // 5% annual interest rate. 5/100
        double monthlyInterestRate = annualInterestRate / 12;
        int m = monthsToRepay; // months to repay the loan

        // equated monthly instalment formula: EMI = [P x R x (1+R)^N]/[(1+R)^N-1].
        double emi = (principal * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, m))
                / (Math.pow(1 + monthlyInterestRate, m) - 1);

        // Return the ceiling of EMI to ensure no underpayment
        return Math.ceil(emi);
    }

    @Override
    public void deleteLoan(long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("loan with id: " + loanId +  " not found"));

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

}