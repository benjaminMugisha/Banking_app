package com.benjamin.Banking_app.Admin;

import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.DirectDebit.DirectDebitRepo;
import com.benjamin.Banking_app.Loans.LoanRepository;
import com.benjamin.Banking_app.Security.UserRepository;
import com.benjamin.Banking_app.Transactions.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepo;
    private final AccountRepository accRepo;
    private final LoanRepository loanRepo;
    private final DirectDebitRepo ddRepo;
    private final TransactionRepository trepo;

    @Override
    public AdminStats getStats(){
        return new AdminStats(
                userRepo.count(), accRepo.count(),
                loanRepo.count(),ddRepo.count(),trepo.count()
        );
    }
}
