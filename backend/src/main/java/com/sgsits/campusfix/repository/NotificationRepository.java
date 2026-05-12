package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.read=TRUE WHERE n.userId=:uid")
    void markAllRead(@Param("uid") Long userId);
}
