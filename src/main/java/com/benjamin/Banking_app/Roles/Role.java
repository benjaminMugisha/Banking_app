package com.benjamin.Banking_app.Roles;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@RequiredArgsConstructor
public enum Role {
    USER, ADMIN;

    public List<SimpleGrantedAuthority> getAuthorities() {
       return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
