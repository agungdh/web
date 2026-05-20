package rest;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkiverse.renarde.Controller;
import model.Tag;

public class Tags extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Tag> tags);
        public static native TemplateInstance newTag();
        public static native TemplateInstance edit(Tag tag);
    }

    public TemplateInstance index() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        return Templates.index(tags);
    }

    @Path("/Tags/new")
    public TemplateInstance newTag() {
        return Templates.newTag();
    }

    @POST
    @Transactional
    public void add(@RestForm @NotBlank String name, @RestForm String slug) {
        if (validationFailed()) {
            newTag();
        }
        Tag tag = new Tag();
        tag.name = name;
        String slugBase = (slug != null && !slug.isBlank()) ? slug : name;
        tag.slug = Tag.generateUniqueSlug(slugBase, null);
        tag.persist();
        index();
    }

    @Path("/Tags/{id}/edit")
    public TemplateInstance edit(@RestPath Long id) {
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag == null) {
            index();
        }
        return Templates.edit(tag);
    }

    @Path("/Tags/{id}/update")
    @POST
    @Transactional
    public void update(@RestPath Long id, @RestForm @NotBlank String name, @RestForm String slug) {
        if (validationFailed()) {
            edit(id);
        }
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag != null) {
            tag.name = name;
            String slugBase = (slug != null && !slug.isBlank()) ? slug : name;
            tag.slug = Tag.generateUniqueSlug(slugBase, tag.id);
            tag.persist();
        }
        index();
    }

    @Path("/Tags/{id}/delete")
    @POST
    @Transactional
    public void delete(@RestPath Long id) {
        Tag tag = Tag.find("id = ?1 and deletedAt is null", id).firstResult();
        if (tag != null) {
            tag.deletedAt = new Date();
            tag.persist();
        }
        index();
    }
}
