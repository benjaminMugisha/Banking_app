package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Entity.MyUser;
import com.benjamin.Banking_app.Repository.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private MyUserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<MyUser> user = repo.findByUsername(username);
        if(user.isPresent()){
            var userObj = user.get();
            return User.builder()
                    .roles(myRoles(userObj))
                    .password(userObj.getPassword())
                    .username(userObj.getUsername())
                    .build();
        } else {
            throw new UsernameNotFoundException(" user not found ");
        }
    }
    private String[] myRoles(MyUser myUser){
        if(myUser.getRole()==null){
            return new String[]{"USER"};
        }
        return myUser.getRole().split(",");
    }
}
