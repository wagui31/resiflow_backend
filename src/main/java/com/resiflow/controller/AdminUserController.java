package com.resiflow.controller;

import com.resiflow.dto.AdminUserActionRequest;
import com.resiflow.dto.UpdateResidenceEntryDateRequest;
import com.resiflow.dto.UpdateUserRoleRequest;
import com.resiflow.dto.UserResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getPendingUsers(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getPendingUsers(authenticatedUser).stream()
                .map(UserResponse::fromUser)
                .toList());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> approveUser(
            @PathVariable final Long id,
            @RequestBody(required = false) final AdminUserActionRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(userService.approveUser(id, authenticatedUser, request)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> rejectUser(
            @PathVariable final Long id,
            @RequestBody(required = false) final AdminUserActionRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(userService.rejectUser(id, authenticatedUser, request)));
    }

    @PutMapping("/{id}/date-entree-residence")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateResidenceEntryDate(
            @PathVariable final Long id,
            @RequestBody final UpdateResidenceEntryDateRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(
                userService.updateResidenceEntryDate(id, authenticatedUser, request.getDateEntreeResidence())
        ));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable final Long id,
            @RequestBody final UpdateUserRoleRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(
                userService.updateUserRole(id, authenticatedUser, request.getRole())
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        userService.deleteUser(id, authenticatedUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
