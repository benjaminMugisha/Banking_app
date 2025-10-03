package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Accounts.TransferRequest;
import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.EntityNotFoundException;
import com.benjamin.Banking_app.UserUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectDebitServiceImpl implements DirectDebitService {

    private final DirectDebitRepo directDebitRepo;
    private final AccountRepository accountRepository;
    private final AccountServiceImpl accountService;
    private final UserUtils userUtils;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DirectDebitServiceImpl.class);


    @Override
    @Transactional
    public DirectDebitDto createDirectDebit(String toIban, BigDecimal amount) {
        Account fromAccount = userUtils.getCurrentUserAccount();
        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new EntityNotFoundException(
                        "account named: " + toIban +" not found"));

        //saving the direct debit info and update nextPaymentDate for processDueDebits().
        DirectDebit directDebit = DirectDebit.builder()
                .fromAccount(fromAccount).toAccount(toAccount).active(true)
                .amount(amount).nextPaymentDate(LocalDate.now().plusDays(28))
                .build();
        directDebitRepo.save(directDebit);


        //perform the first payment immediately.
        TransferRequest request = new TransferRequest(toIban, amount);
        accountService.transfer(request);
        logger.info("direct debit paid from: {} to: {} of €{}"
        , fromAccount.getAccountUsername(), toIban, amount);

        return DirectDebitMapper.mapToDirectDebitDto(directDebit);
    }

    @Scheduled(cron = "0 0 0 * * *") //to run every day at midnight.
    @Transactional
    public void processDueDebits() {
        LocalDate today = LocalDate.now();
        List<DirectDebit> dueDebits =
                directDebitRepo.findByActiveTrueAndNextPaymentDate(today);

        for (DirectDebit debit : dueDebits) {
            try {
                TransferRequest request = new TransferRequest(
                        debit.getToAccount().getAccountUsername(),
                        debit.getAmount()
                );

                accountService.transfer(request);

                //update next payment date
                debit.setNextPaymentDate(today.plusDays(28));
                directDebitRepo.save(debit);
            } catch (Exception e) {
                logger.error("Failed to process debit ID: {} : {}", debit.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void cancelDirectDebit(Long directDebitId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Account currentUserAccount = userUtils.getCurrentUserAccount();

        DirectDebit debit = directDebitRepo.findById(directDebitId)
                .orElseThrow(() -> new EntityNotFoundException("Direct debit not found"));

        if (!isAdmin && !debit.getFromAccount().getId().equals(currentUserAccount.getId())) {
            throw new AccessDeniedException("You are not allowed to cancel this direct debit");
        }

        debit.setActive(false);
        directDebitRepo.save(debit);
    }

    @Override
    public DirectDebitDto getById(long id) {
        DirectDebit dd = directDebitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Direct debit with id: " +  id + " not found"));
       return DirectDebitMapper.mapToDirectDebitDto(dd);
    }

    @Override
    public List<DirectDebit> all() {
        return directDebitRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DirectDebitResponse getDirectDebits(
            int pageNo, int pageSize, String accountUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Account accountToQuery;

        if(isAdmin && accountUsername != null){
            //Admin fetching someone else's direct debits.
            accountToQuery = accountRepository.findByAccountUsername(accountUsername)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountUsername));
        } else {
            //normal user OR admin fetching their own direct debits.
            accountToQuery = userUtils.getCurrentUserAccount();
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<DirectDebit> debitsPage =
                directDebitRepo.findByFromAccountAndActiveTrue(accountToQuery, pageable);

        List<DirectDebitDto> content = debitsPage.stream()
                .map(DirectDebitMapper::mapToDirectDebitDto)
                .toList();

        return DirectDebitResponse.builder()
                .content(content)
                .pageNo(debitsPage.getNumber())
                .pageSize(debitsPage.getSize())
                .totalElements(debitsPage.getTotalElements())
                .totalPages(debitsPage.getTotalPages())
                .last(debitsPage.isLast())
                .build();
    }
}
