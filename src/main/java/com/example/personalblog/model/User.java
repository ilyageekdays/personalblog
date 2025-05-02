package com.example.personalblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = "posts")
@EqualsAndHashCode(exclude = "posts")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    public void addPost(Post post) {
        this.posts.add(post);
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        this.posts.remove(post);
        post.setAuthor(null);
    }
}
