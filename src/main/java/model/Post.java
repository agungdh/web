package model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Post extends PanacheEntity {

    public String title;
    public String slug;

    @Lob
    public String content;

    public String type;
    public Date createdAt;
    public Date updatedAt;
    public Date deletedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public Set<Tag> tags = new HashSet<>();

    public static String slugify(String input) {
        if (input == null || input.isBlank()) return "";
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public static String generateUniqueSlug(String input, Long excludeId) {
        String base = slugify(input);
        if (base.isBlank()) base = "untitled";
        String slug = base;
        int counter = 2;
        long exclude = (excludeId != null) ? excludeId : -1L;
        while (count("slug = ?1 and deletedAt is null and id != ?2", slug, exclude) > 0) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
