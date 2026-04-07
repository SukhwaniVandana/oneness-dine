package com.oneness.dine.auth.entity;

import com.oneness.dine.core.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @Column(name = "terminal_id", nullable = false, length = 50)
    private String terminalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType = DeviceType.DESKTOP;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "screen_number")
    private Integer screenNumber = 1;

    @Column(name = "token_hash", length = 255)
    private String tokenHash;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}