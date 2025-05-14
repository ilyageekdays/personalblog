package com.example.personalblog.repository;

import com.example.personalblog.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u "
            + "JOIN u.posts p "
            + "JOIN p.categories c "
            + "WHERE c.name = :categoryName")
    List<User> findUsersByPostCategory(@Param("categoryName") String categoryName);
}