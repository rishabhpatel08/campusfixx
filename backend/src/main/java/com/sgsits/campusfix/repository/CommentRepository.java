package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
