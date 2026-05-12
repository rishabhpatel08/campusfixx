package com.sgsits.campusfix.repository;
import com.sgsits.campusfix.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEnrollmentId(String enrollmentId);
    Optional<User> findByEmail(String email);
    Optional<User> findByWhatsappNumber(String whatsappNumber);
    List<User> findByRoleIn(List<String> roles);
    List<User> findByRole(String role);
    boolean existsByEmail(String email);
    boolean existsByEnrollmentId(String enrollmentId);
}
