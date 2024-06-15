package com.benjamin.Banking_app.Controller;

import com.benjamin.Banking_app.Entity.MyUser;
import com.benjamin.Banking_app.Repository.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RegistrationController {

    @Autowired
    private MyUserRepository myUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register/users")
    public MyUser createUser(@RequestBody MyUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return myUserRepository.save(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/users")
    public List<MyUser> getMyUsers(){
        return myUserRepository.findAll();
    }
}
