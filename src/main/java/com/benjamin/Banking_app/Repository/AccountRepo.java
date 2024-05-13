package com.benjamin.Banking_app.Repository;

import com.benjamin.Banking_app.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account, Long> {

}
