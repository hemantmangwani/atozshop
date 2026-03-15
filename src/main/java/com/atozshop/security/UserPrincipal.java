package com.atozshop.security;

import com.atozshop.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation for Spring Security
 * Wraps our User entity with Spring Security's UserDetails interface
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private Long tenantId;
    private Long storeId;
    private Long customerId;
    private String email;
    private String username;
    private String phone;
    private String fullName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isActive;

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());

        return new UserPrincipal(
            user.getId(),
            user.getTenantId(),
            user.getStoreId(), // Added storeId
            user.getCustomerId(), // Added customerId
            user.getEmail(),
            user.getUsername(),
            user.getPhone(), // Added phone
            user.getFullName(), // Added fullName
            user.getPasswordHash(),
            authorities,
            user.getIsActive()
        );
    }

    /**
     * Check if user has ADMIN role
     */
    public boolean isAdmin() {
        return authorities != null && authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if user has CUSTOMER role
     */
    public boolean isCustomer() {
        return authorities != null && authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER"));
    }

    /**
     * Get store ID with fallback to default (1)
     */
    public Long getStoreIdOrDefault() {
        return storeId != null ? storeId : 1L;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email != null ? email : username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}