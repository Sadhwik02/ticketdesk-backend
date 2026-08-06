package com.ticketdesk.repository;

import com.ticketdesk.entity.User;
import com.ticketdesk.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByActive(boolean active, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR
                   LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.username)  LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:role IS NULL OR u.role = :role)
            AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> searchUsers(@Param("search") String search,
                           @Param("role") Role role,
                           @Param("active") Boolean active,
                           Pageable pageable);
}
