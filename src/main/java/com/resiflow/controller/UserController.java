package com.resiflow.controller;

import com.resiflow.dto.CreateAdminRequest;
import com.resiflow.dto.UpdateCurrentUserRequest;
import com.resiflow.dto.UserPaiementHistoryResponse;
import com.resiflow.dto.UserResponse;
import com.resiflow.entity.User;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PaiementService;
import com.resiflow.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/users", "/api/users"})
public class UserController {

    private final UserService userService;
    private final PaiementService paiementService;

    public UserController(final UserService userService, final PaiementService paiementService) {
        this.userService = userService;
        this.paiementService = paiementService;
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createAdmin(@RequestBody final CreateAdminRequest request) {
        User createdUser = userService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromUser(createdUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        List<UserResponse> responses = userService.getUsers(authenticatedUser).stream()
                .map(UserResponse::fromUser)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/residence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getResidenceUsers(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        List<UserResponse> responses = userService.getResidenceUsers(authenticatedUser).stream()
                .map(UserResponse::fromUser)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(final Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(userService.getCurrentUser(authenticatedUser)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @RequestBody final UpdateCurrentUserRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(UserResponse.fromUser(userService.updateCurrentUser(authenticatedUser, request)));
    }

    @GetMapping("/{id}/paiements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserPaiementHistoryResponse>> getPaiementHistory(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(paiementService.getPaiementHistoryByUtilisateur(id, authenticatedUser));
    }
}
