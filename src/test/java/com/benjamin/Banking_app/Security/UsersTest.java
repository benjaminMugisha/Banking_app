package com.benjamin.Banking_app.Security;

import com.benjamin.Banking_app.Accounts.Account;
import com.benjamin.Banking_app.Roles.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    @Test
    void testGetAuthorities_AdminRole_ReturnsAdminRole() {
        Users user = Users.builder()
                .role(Role.ADMIN)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void testGetAuthorities_UserRole_ReturnsUserRole() {
        Users user = Users.builder()
                .role(Role.USER)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void testIsAccountNonExpired_ReturnsTrue() {
        Users user = Users.builder().build();

        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_ReturnsTrue() {
        Users user = Users.builder().build();

        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired_ReturnsTrue() {
        Users user = Users.builder().build();

        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_ReturnsTrue() {
        Users user = Users.builder().build();

        assertTrue(user.isEnabled());
    }

    @Test
    void testUserBuilder_ReturnsUser() {
        Users user = Users.builder()
                .id(1)
                .firstname("John").lastname("Doe").email("john.doe@example.com")
                .password("securePassword").role(Role.USER)
                .build();

        assertEquals(1, user.getId());
        assertEquals("John", user.getFirstname());
        assertEquals("Doe", user.getLastname());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("securePassword", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals("john.doe@example.com", user.getUsername());
    }

    @Test
    void testAccountRelationship() {
        Users user = Users.builder()
                .email("test@example.com")
                .build();

        Account account = new Account();
        account.setUser(user);
        user.setAccount(account);

        assertNotNull(user.getAccount());
        assertEquals(account, user.getAccount());
        assertEquals(user, account.getUser());
    }

    @Test
    void testUserDetailsImplementation() {
        Users user = Users.builder()
                .email("user@example.com").password("testPassword").role(Role.USER)
                .build();

        assertEquals("user@example.com", user.getUsername());
        assertEquals("testPassword", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }
}
