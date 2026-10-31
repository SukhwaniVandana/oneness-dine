package com.oneness.dine.auth.service;

import com.oneness.dine.auth.dto.LoginRequest;
import com.oneness.dine.auth.dto.LoginResponse;
import com.oneness.dine.auth.dto.UserDto;
import com.oneness.dine.auth.entity.User;
import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.auth.repository.UserSessionRepository;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder       passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate username & password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Fetch user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", request.getUsername()));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessException(
                    "Your account is deactivated. Contact admin.",
                    "ACCOUNT_INACTIVE");
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().getRoleName(),
                user.getBranch().getId()
        );

        // Save session
        UserSession session = new UserSession();
        session.setUser(user);
        session.setBranch(user.getBranch());
        session.setTerminalId(request.getTerminalId());
        session.setDeviceType(request.getDeviceType());
        session.setDeviceInfo(request.getDeviceInfo());
        session.setScreenNumber(request.getScreenNumber());
        session.setTokenHash(jwtService.hashToken(token));
        session.setLoginAt(LocalDateTime.now());
        session.setIsActive(true);
        userSessionRepository.save(session);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User '{}' logged in from terminal '{}'",
                user.getUsername(), request.getTerminalId());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .branchId(user.getBranch().getId())
                .branchName(user.getBranch().getBranchName())
                .deviceType(request.getDeviceType())
                .terminalId(request.getTerminalId())
                .build();
    }

    @Transactional
    public void logout(String token) {
        userSessionRepository.findByTokenHashAndIsActiveTrue(token)
                .ifPresent(session -> {
                    session.setIsActive(false);
                    session.setLogoutAt(LocalDateTime.now());
                    userSessionRepository.save(session);
                    log.info("User '{}' logged out from terminal '{}'",
                            session.getUser().getUsername(),
                            session.getTerminalId());
                });
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .roleName(user.getRole().getRoleName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .branchId(user.getBranch().getId())
                .branchName(user.getBranch().getBranchName())
                .build();
    }

    @Transactional
    public LoginResponse pinLogin(String pinCode, String terminalId) {
        User user = userRepository.findByPinCode(pinCode)
                .orElseThrow(() -> new BusinessException(
                        "Invalid PIN code", "INVALID_PIN"));

        if (!user.getIsActive()) {
            throw new BusinessException(
                    "Your account is deactivated.", "ACCOUNT_INACTIVE");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().getRoleName(),
                user.getBranch().getId()
        );

        UserSession session = new UserSession();
        session.setUser(user);
        session.setBranch(user.getBranch());
        session.setTerminalId(terminalId);
        session.setDeviceType(com.oneness.dine.core.enums.DeviceType.DESKTOP);
        session.setTokenHash(token);
        session.setLoginAt(LocalDateTime.now());
        session.setIsActive(true);
        userSessionRepository.save(session);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .branchId(user.getBranch().getId())
                .branchName(user.getBranch().getBranchName())
                .build();
    }
}