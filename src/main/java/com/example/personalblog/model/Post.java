package com.example.personalblog.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Модель, представляющая пост блога")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"author", "categories"})
@EqualsAndHashCode(exclude = {"author", "categories"})
@Table(name = "posts")
public class Post {

    @Schema(description = "Уникальный идентификатор поста", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            description = "Заголовок поста",
            example = "Мой первый пост",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "title", nullable = false)
    private String title;

    @Schema(
            description = "Основное содержание поста",
            example = "Это текст моего первого поста...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Schema(
            description = "Дата и время создания поста",
            example = "2023-07-20T10:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(
            description = "Дата и время последнего обновления поста",
            example = "2023-07-21T14:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "Автор поста", implementation = User.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Schema(
            description = "Категории, связанные с постом",
            implementation = Category.class,
            example = "[\"Технологии\", \"Программирование\"]"
    )
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_categories",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnoreProperties("posts")
    private Set<Category> categories = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}