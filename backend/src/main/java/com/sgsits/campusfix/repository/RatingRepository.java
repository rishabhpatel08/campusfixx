package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating,Long> {
    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);
}
