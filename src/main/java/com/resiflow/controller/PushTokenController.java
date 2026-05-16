package com.resiflow.controller;

import com.resiflow.dto.PushTokenDeactivateRequest;
import com.resiflow.dto.PushTokenResponse;
import com.resiflow.dto.PushTokenUpsertRequest;
import com.resiflow.entity.UserPushToken;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.PushTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push-tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    public PushTokenController(final PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PushTokenResponse> registerPushToken(
            @RequestBody final PushTokenUpsertRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        UserPushToken token = pushTokenService.registerCurrentUserToken(authenticatedUser, request);
        return ResponseEntity.ok(PushTokenResponse.fromEntity(token));
    }

    @PutMapping("/current/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutPushToken(
            @RequestBody final PushTokenDeactivateRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        pushTokenService.logoutCurrentUserToken(authenticatedUser, request);
        return ResponseEntity.noContent().build();
    }
}
