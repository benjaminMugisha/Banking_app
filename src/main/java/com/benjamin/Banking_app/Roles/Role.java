package com.benjamin.Banking_app.Roles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {
    USER(Set.of("READ_DATA",
            "CREATE_DATA",
            "WRITE_DATA",
            "DELETE_DATA")),
    ADMIN(Set.of(
            "READ_DATA",
            "CREATE_DATA",
            "WRITE_DATA",
            "DELETE_DATA"
    ));

    @Getter
    private final Set<String> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
       var authorities = getPermissions().stream()
               .map(permission -> new SimpleGrantedAuthority(permission))
               .collect(Collectors.toList());
       authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
       return authorities;
    }
}
