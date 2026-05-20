package model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class Tag extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public String slug;
    public Date deletedAt;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + id + ">";
    }
}
