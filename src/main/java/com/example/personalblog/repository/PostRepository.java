package com.example.personalblog.repository;

import com.example.personalblog.model.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    public Post findByTitle(String title);

    public boolean existsByTitle(String title);

    @Query("SELECT p FROM Post p JOIN FETCH p.author a WHERE a.username = :username")
    List<Post> findAllByAuthorUsername(@Param("username") String username);

    @Query("SELECT DISTINCT p FROM Post p "
            + "JOIN FETCH p.categories c "
            + "WHERE c.name = :categoryName")
    List<Post> findAllByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT DISTINCT p FROM Post p "
            + "JOIN FETCH p.author a "
            + "JOIN FETCH p.categories c "
            + "WHERE c.name = :categoryName "
            + "AND a.username = :authorUsername")
    List<Post> findAllByCategoryNameAndAuthorUsername(
            @Param("categoryName") String categoryName,
            @Param("authorUsername") String authorUsername
    );
}