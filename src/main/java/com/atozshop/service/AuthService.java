package com.atozshop.service;

import com.atozshop.dto.request.LoginRequest;
import com.atozshop.dto.request.RegisterRequest;
import com.atozshop.dto.response.JwtResponse;
import com.atozshop.dto.response.MessageResponse;
import com.atozshop.entity.Role;
import com.atozshop.entity.User;
import com.atozshop.exception.BadRequestException;
import com.atozshop.repository.RoleRepository;
import com.atozshop.repository.UserRepository;
import com.atozshop.security.JwtTokenProvider;
import com.atozshop.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles user login and registration
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT token
     */
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
            .map(item -> item.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toList());

        // Update last login
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new BadRequestException("User not found"));
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return JwtResponse.builder()
            .token(jwt)
            .type("Bearer")
            .id(userPrincipal.getId())
            .email(userPrincipal.getEmail())
            .username(userPrincipal.getUsername())
            .fullName(user.getFullName())
            .tenantId(userPrincipal.getTenantId())
            .roles(roles)
            .build();
    }

    /**
     * Register a new user
     */
    @Transactional
    public MessageResponse register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmailAndTenantId(
                registerRequest.getEmail(), registerRequest.getTenantId())) {
            throw new BadRequestException("Email is already taken");
        }

        // Check if phone already exists (if provided)
        if (registerRequest.getPhone() != null &&
            userRepository.existsByPhoneAndTenantId(
                registerRequest.getPhone(), registerRequest.getTenantId())) {
            throw new BadRequestException("Phone number is already taken");
        }

        // Create new user
        User user = User.builder()
            .tenantId(registerRequest.getTenantId())
            .email(registerRequest.getEmail())
            .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .phone(registerRequest.getPhone())
            .username(registerRequest.getUsername())
            .storeId(registerRequest.getStoreId())
            .isActive(true)
            .emailVerified(false)
            .phoneVerified(false)
            .build();

        // Assign default role (USER or create if needed)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByNameAndTenantId("USER", registerRequest.getTenantId())
            .orElseGet(() -> {
                Role newRole = Role.builder()
                    .tenantId(registerRequest.getTenantId())
                    .name("USER")
                    .description("Default user role")
                    .isSystem(true)
                    .build();
                return roleRepository.save(newRole);
            });
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return new MessageResponse("User registered successfully");
    }
}
