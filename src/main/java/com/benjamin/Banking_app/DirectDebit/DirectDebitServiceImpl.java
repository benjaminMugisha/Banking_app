package com.benjamin.Banking_app.DirectDebit;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Accounts.AccountRepository;
import com.benjamin.Banking_app.Accounts.AccountServiceImpl;
import com.benjamin.Banking_app.Accounts.TransferRequest;
import com.benjamin.Banking_app.Exception.AccessDeniedException;
import com.benjamin.Banking_app.Exception.BadRequestException;
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
import java.util.stream.Collectors;

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
    public DirectDebitResponse createDirectDebit(String toIban, BigDecimal amount) {
        Account fromAccount = userUtils.getCurrentUserAccount();
        Account toAccount = accountRepository.findByIban(toIban)
                .orElseThrow(() -> new EntityNotFoundException(
                        "account with iban: " + toIban +" not found"));

        if(fromAccount.getId().equals(toAccount.getId())) {
            throw new BadRequestException("this is your iban. not allowed");
        }
        //find the existing dd. null if it doesn't exist.
        DirectDebit dd = directDebitRepo.findByFromAccountAndToAccount(fromAccount, toAccount);

        if(dd == null) { // if the dd doesn't exist, create and  pay it.
            DirectDebit directDebit = DirectDebit.builder()
                    .active(true).nextPaymentDate(LocalDate.now().plusDays(28))
                    .fromAccount(fromAccount).toAccount(toAccount)
                    .amount(amount)
                    .build();
            directDebitRepo.save(directDebit);
            setToZero(directDebit);
            TransferRequest transfer = new TransferRequest(toIban, amount);
            accountService.transfer(transfer);

            logger.info("Direct debit of €{} created and paid from user: {} to user: {} ",
                    amount, fromAccount.getUser().getEmail(), toAccount.getUser().getEmail());
            return new DirectDebitResponse(DirectDebitMapper.mapToDirectDebitDto(directDebit)
                    , DDStatusMessage.CREATED_AND_PAID);
        }
        else if (!dd.isActive()) { //if we had this dd and deactivated it, reactivate it and update the amount.
            dd.setActive(true);
            return updateDirectDebit(dd.getId(), amount);
        } else { //if it does exist, update the amount only.
           return updateDirectDebit(dd.getId(), amount);
        }
    }

    @Override
    public DirectDebitResponse updateDirectDebit(Long directDebitId, BigDecimal amount) {
        DirectDebit dd = directDebitRepo.findById(directDebitId)
                .orElseThrow(() -> new EntityNotFoundException("Direct debit not found"));
        if(amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("amount can't be less than zero");
        if (dd.getAmount().compareTo(amount) == 0) {
            return new DirectDebitResponse(DirectDebitMapper.mapToDirectDebitDto(dd), DDStatusMessage.UNCHANGED);
        }
        if(amount.compareTo(BigDecimal.ZERO) == 0) {
            dd.setActive(false);
            dd.setAmount(BigDecimal.ZERO);
            directDebitRepo.save(dd);

            return new DirectDebitResponse(
                    DirectDebitMapper.mapToDirectDebitDto(dd),
                    DDStatusMessage.CANCELLED
            );
        }
        logger.info("updating DD from user: {} to user: {} from the old amount of {} the new amount of {}",
                dd.getFromAccount().getUser().getEmail(),
                dd.getToAccount().getUser().getEmail(), dd.getAmount(), amount);
        dd.setAmount(amount);
        dd.setActive(true);
        directDebitRepo.save(dd);
        setToZero(dd);

        return new DirectDebitResponse(DirectDebitMapper.mapToDirectDebitDto(dd),
                DDStatusMessage.UPDATED);
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
                        debit.getToAccount().getUser().getEmail(),
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
    public DirectDebitResponse cancelDirectDebit(Long directDebitId) {
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
        return new DirectDebitResponse(DirectDebitMapper.mapToDirectDebitDto(debit), DDStatusMessage.CANCELLED);
    }

    @Override
    public DirectDebitDto getById(long id) {
        DirectDebit dd = directDebitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Direct debit with id: " +  id + " not found"));
       return DirectDebitMapper.mapToDirectDebitDto(dd);
    }

    //Direct debits belonging to an account.
    @Override
    @Transactional(readOnly = true)
    public DirectDebitPageResponse getDirectDebits(
            int pageNo, int pageSize, String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Account accountToQuery;

        if(isAdmin && email != null){
            //Admin fetching someone else's direct debits.
            accountToQuery = accountRepository.findByUserEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + email));
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
        int totalPages = debitsPage.getTotalPages() == 0 ? 1 : debitsPage.getTotalPages();

        return DirectDebitPageResponse.builder()
                .content(content)
                .pageNo(debitsPage.getNumber()).pageSize(debitsPage.getSize())
                .totalElements(debitsPage.getTotalElements())
                .totalPages(totalPages).last(debitsPage.isLast())
                .build();
    }

    @Override
    public DirectDebitPageResponse getAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<DirectDebit> dds = directDebitRepo.findAll(pageable);

        List<DirectDebitDto> content = dds.stream()
                .map(DirectDebitMapper::mapToDirectDebitDto)
                .toList();
        int totalPages = dds.getTotalPages() == 0 ? 1 : dds.getTotalPages();
        return DirectDebitPageResponse.builder()
                .content(content).pageNo(dds.getNumber()).pageSize(dds.getSize())
                .totalElements(dds.getTotalElements()).totalPages(totalPages)
                .last(dds.isLast())
                .build();
    }

    public DirectDebitPageResponse getActiveDds(int pageNo, int pageSize ) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
         Page<DirectDebit> dds = directDebitRepo.findByActiveTrue(pageable);

        List<DirectDebitDto> content = dds.stream()
                .map(DirectDebitMapper::mapToDirectDebitDto)
                .toList();
        int totalPages = dds.getTotalPages() == 0 ? 1 : dds.getTotalPages();
        return DirectDebitPageResponse.builder()
                .content(content).pageNo(dds.getNumber()).pageSize(dds.getSize())
                .totalElements(dds.getTotalElements()).totalPages(totalPages)
                .last(dds.isLast())
                .build();
    }


    private void setToZero(DirectDebit dd){
        if(dd.getAmount().compareTo(BigDecimal.ZERO) == 0 && dd.isActive()){
            dd.setActive(false);
            directDebitRepo.save(dd);
        }
    }

    @Override
    public String deleteById(Long id){
        DirectDebit debit = directDebitRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Direct debit not found"));

        directDebitRepo.deleteById(id);
        return "DELETED";
    }

}
