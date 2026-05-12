package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.CoReporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoReporterRepository extends JpaRepository<CoReporter,Long> {
    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);
}
