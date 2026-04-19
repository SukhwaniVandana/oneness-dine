package com.oneness.dine.platform.entity;

import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "platform_config")
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PlatformType platform;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "store_id", length = 100)
    private String storeId;

    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    @Column(name = "auto_accept_orders")
    private Boolean autoAcceptOrders = false;

    @Column(name = "menu_sync_enabled")
    private Boolean menuSyncEnabled = false;

    @Column(name = "last_menu_sync_at")
    private LocalDateTime lastMenuSyncAt;

    @Column(name = "webhook_secret", length = 200)
    private String webhookSecret;
}
