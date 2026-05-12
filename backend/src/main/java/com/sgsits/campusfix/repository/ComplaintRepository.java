package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    List<Complaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    @Query(value="""
        SELECT * FROM complaints c
        WHERE (:cat IS NULL OR c.category=:cat)
          AND (:stat IS NULL OR c.status=:stat)
          AND (:pri IS NULL OR c.priority=:pri)
        ORDER BY c.importance_score DESC, c.created_at DESC
        """, nativeQuery=true)
    List<Complaint> findFiltered(@Param("cat") String cat, @Param("stat") String stat, @Param("pri") String pri);

    @Query(value="""
        SELECT * FROM complaints
        WHERE category=:cat AND location_id=:locId
          AND created_at > :since AND status NOT IN('closed','rejected')
        ORDER BY created_at DESC LIMIT 1
        """, nativeQuery=true)
    Optional<Complaint> findDupByLocation(@Param("cat") String cat, @Param("locId") Long locId, @Param("since") LocalDateTime since);

    @Query(value="""
        SELECT * FROM complaints
        WHERE category=:cat
          AND gps_lat BETWEEN :lat1 AND :lat2
          AND gps_lng BETWEEN :lng1 AND :lng2
          AND created_at > :since AND status NOT IN('closed','rejected')
        ORDER BY created_at DESC LIMIT 1
        """, nativeQuery=true)
    Optional<Complaint> findDupByGps(@Param("cat") String cat,
        @Param("lat1") BigDecimal lat1, @Param("lat2") BigDecimal lat2,
        @Param("lng1") BigDecimal lng1, @Param("lng2") BigDecimal lng2,
        @Param("since") LocalDateTime since);

    long countByCategory(String category);

    Optional<Complaint> findByComplaintNo(String complaintNo);

    Optional<Complaint> findTopByAssignedToAndStatusNotInOrderByCreatedAtDesc(Long assignedTo, List<String> statuses);

    @Query(value="""
        SELECT COUNT(*) AS total,
               COUNT(*) FILTER(WHERE status IN('registered','forwarded','in_progress')) AS in_progress,
               COUNT(*) FILTER(WHERE is_escalated=TRUE AND status NOT IN('resolved','closed','rejected')) AS escalated,
               COUNT(*) FILTER(WHERE sla_breached=TRUE AND status NOT IN('resolved','closed','rejected')) AS sla_breached,
               ROUND(AVG(EXTRACT(EPOCH FROM (resolved_at-created_at))/3600) FILTER(WHERE resolved_at IS NOT NULL),1) AS avg_resolution_hours
        FROM complaints
        """, nativeQuery=true)
    List<Map<String,Object>> getStats();

    @Query(value="""
        SELECT u.enrollment_id,u.name,u.points,u.badge,
               COUNT(c.id) AS complaints_filed
        FROM users u LEFT JOIN complaints c ON c.reporter_id=u.id
        WHERE u.role IN('student','faculty') AND u.is_active=TRUE
        GROUP BY u.id,u.enrollment_id,u.name,u.points,u.badge
        ORDER BY u.points DESC LIMIT 20
        """, nativeQuery=true)
    List<Map<String,Object>> getLeaderboard();

    @Query(value="""
        SELECT d.name_en AS department,
               COUNT(*) FILTER(WHERE c.status NOT IN('resolved','closed')) AS open_count,
               COUNT(*) FILTER(WHERE c.status IN('resolved','closed')) AS resolved_count
        FROM departments d LEFT JOIN complaints c ON c.department_id=d.id
        GROUP BY d.id,d.name_en ORDER BY open_count DESC
        """, nativeQuery=true)
    List<Map<String,Object>> getDeptLoad();

    @Query(value="""
        SELECT COALESCE(l.name_en,c.location_text,'Unknown') AS location,
               COUNT(*) AS total,
               COUNT(*) FILTER(WHERE c.status NOT IN('resolved','closed','rejected')) AS open_count,
               COUNT(*) FILTER(WHERE c.is_escalated=TRUE) AS escalated_count
        FROM complaints c LEFT JOIN locations l ON l.id=c.location_id
        GROUP BY COALESCE(l.name_en,c.location_text,'Unknown')
        HAVING COUNT(*)>0 ORDER BY total DESC LIMIT 10
        """, nativeQuery=true)
    List<Map<String,Object>> getHotspots();
}
