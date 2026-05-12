package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.ComplaintLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintLogRepository extends JpaRepository<ComplaintLog,Long> {
    List<ComplaintLog> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
