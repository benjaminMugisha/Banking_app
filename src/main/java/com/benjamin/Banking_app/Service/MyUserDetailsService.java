package com.benjamin.Banking_app.Service;

import com.benjamin.Banking_app.Entity.Users;
import com.benjamin.Banking_app.Entity.UserPrincipal;
import com.benjamin.Banking_app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = repo.findByUsername(username);

        if(user == null) {
            System.out.println("User not found");
            //to let the system know user not found you can throw the UsernameNotFoundException from this method
            throw new UsernameNotFoundException("user not found");
        }

        // if user exists return an object of UserDetails which is an interface so we have to return a class that implements it:
        return new UserPrincipal(user);
    }
}
