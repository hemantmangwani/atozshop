package com.atozshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity - System users (admin, staff, customers)
 * Supports role-based access control
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "email"}),
    @UniqueConstraint(columnNames = {"tenantId", "phone"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(length = 100)
    private String username;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean phoneVerified = false;

    private LocalDateTime lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Helper method to get full name
    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return (firstName != null ? firstName : "") +
               (lastName != null ? " " + lastName : "");
    }
}