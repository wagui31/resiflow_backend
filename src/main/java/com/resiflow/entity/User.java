package com.resiflow.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residence_id")
    private Residence residence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logement_id")
    private Logement logement;

    @Column(name = "date_entree_residence", nullable = false)
    private java.time.LocalDate dateEntreeResidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Residence getResidence() {
        return residence;
    }

    public void setResidence(final Residence residence) {
        this.residence = residence;
    }

    public Long getResidenceId() {
        return residence == null ? null : residence.getId();
    }

    public void setResidenceId(final Long residenceId) {
        if (residenceId == null) {
            this.residence = null;
            return;
        }

        Residence residenceReference = new Residence();
        residenceReference.setId(residenceId);
        this.residence = residenceReference;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(final UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(final UserStatus status) {
        this.status = status;
    }

    public Logement getLogement() {
        return logement;
    }

    public void setLogement(final Logement logement) {
        this.logement = logement;
    }

    public Long getLogementId() {
        return logement == null ? null : logement.getId();
    }

    public void setLogementId(final Long logementId) {
        if (logementId == null) {
            this.logement = null;
            return;
        }
        Logement logementReference = new Logement();
        logementReference.setId(logementId);
        this.logement = logementReference;
    }

    public java.time.LocalDate getDateEntreeResidence() {
        return dateEntreeResidence;
    }

    public void setDateEntreeResidence(final java.time.LocalDate dateEntreeResidence) {
        this.dateEntreeResidence = dateEntreeResidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        validateRoleAssignments();
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (dateEntreeResidence == null) {
            dateEntreeResidence = now.toLocalDate();
        }
    }

    @PreUpdate
    public void preUpdate() {
        validateRoleAssignments();
        updatedAt = LocalDateTime.now();
    }

    private void validateRoleAssignments() {
        if (role == null) {
            return;
        }
        if (role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (residence == null) {
            throw new IllegalStateException("Residence is required for non-super-admin users");
        }
        if (logement == null) {
            throw new IllegalStateException("Logement is required for non-super-admin users");
        }
    }
}
