package com.example.personalblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность, представляющая категорию для постов блога.
 * Категории используются для организации и классификации постов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
@Schema(description = "Модель категории для постов блога")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Уникальный идентификатор категории",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 2, max = 50, message = "Название категории должно быть от 2 до 50 символов")
    @Column(name = "name", nullable = false, unique = true)
    @Schema(
            description = "Уникальное название категории",
            example = "Программирование",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 50
    )
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @Schema(
            description = "Связанные посты (скрыто в API)",
            accessMode = Schema.AccessMode.READ_ONLY,
            hidden = true
    )
    private Set<Post> posts = new HashSet<>();

    /**
     * Добавляет пост к категории.
     * Поддерживает целостность двусторонней связи.
     *
     * @param post Пост для добавления
     */
    public void addPost(Post post) {
        this.posts.add(post);
        post.getCategories().add(this);
    }

    /**
     * Удаляет пост из категории.
     * Поддерживает целостность двусторонней связи.
     *
     * @param post Пост для удаления
     */
    public void removePost(Post post) {
        this.posts.remove(post);
        post.getCategories().remove(this);
    }
}