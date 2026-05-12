package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken,Long> {

    // Used by AuthService.verifyOtp()
    List<OtpToken> findByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, String purpose);

    // Used by AuthService to get latest valid token
    Optional<OtpToken> findTopByEmailAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
        String email, String purpose, LocalDateTime now);

    // Used by AuthService.requestOtp() to invalidate old tokens
    @Modifying @Transactional
    @Query("UPDATE OtpToken o SET o.used=TRUE WHERE o.email=:email AND o.purpose=:purpose AND o.used=FALSE")
    void invalidateAll(@Param("email") String email, @Param("purpose") String purpose);

    @Modifying @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
