package com.example.personalblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Модель пользователя блога")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = "posts")
@EqualsAndHashCode(exclude = "posts")
@Table(name = "users")
public class User {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            description = "Отображаемое имя пользователя",
            example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "visible_name", nullable = false)
    private String visibleName;

    @Schema(
            description = "Уникальный логин для входа",
            example = "ivan_ivanov",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Schema(
            description = "Электронная почта пользователя",
            example = "ivan@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Schema(
            description = "Список постов пользователя",
            implementation = Post.class,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();
}