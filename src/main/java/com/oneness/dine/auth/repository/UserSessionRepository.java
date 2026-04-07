package com.oneness.dine.auth.repository;

import com.oneness.dine.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByTokenHashAndIsActiveTrue(String tokenHash);
    void deleteByUserIdAndIsActiveTrue(Long userId);
}