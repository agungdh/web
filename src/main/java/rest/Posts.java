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
import model.Post;
import model.Tag;

public class Posts extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Post> posts);
        public static native TemplateInstance newPost(List<Tag> tags);
        public static native TemplateInstance edit(Post post, List<Tag> tags);
    }

    public TemplateInstance index() {
        List<Post> posts = Post.list("deletedAt is null order by createdAt desc");
        return Templates.index(posts);
    }

    @Path("/Posts/new")
    public TemplateInstance newPost() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        return Templates.newPost(tags);
    }

    @POST
    @Transactional
    public void add(@RestForm @NotBlank String title,
                    @RestForm String slug,
                    @RestForm String content,
                    @RestForm String type,
                    @RestForm List<Long> tagIds) {
        if (validationFailed()) {
            newPost();
        }
        Post post = new Post();
        post.title = title;
        String slugBase = (slug != null && !slug.isBlank()) ? slug : title;
        post.slug = Post.generateUniqueSlug(slugBase, null);
        post.content = content;
        post.type = (type != null && !type.isBlank()) ? type : "post";
        post.createdAt = new Date();
        post.updatedAt = new Date();
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                Tag tag = Tag.findById(tagId);
                if (tag != null) post.tags.add(tag);
            }
        }
        post.persist();
        index();
    }

    @Path("/Posts/{id}/edit")
    public TemplateInstance edit(@RestPath Long id) {
        Post post = Post.find("id = ?1 and deletedAt is null", id).firstResult();
        if (post == null) {
            index();
        }
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        return Templates.edit(post, tags);
    }

    @Path("/Posts/{id}/update")
    @POST
    @Transactional
    public void update(@RestPath Long id,
                       @RestForm @NotBlank String title,
                       @RestForm String slug,
                       @RestForm String content,
                       @RestForm String type,
                       @RestForm List<Long> tagIds) {
        if (validationFailed()) {
            edit(id);
        }
        Post post = Post.find("id = ?1 and deletedAt is null", id).firstResult();
        if (post != null) {
            post.title = title;
            String slugBase = (slug != null && !slug.isBlank()) ? slug : title;
            post.slug = Post.generateUniqueSlug(slugBase, post.id);
            post.content = content;
            post.type = (type != null && !type.isBlank()) ? type : "post";
            post.updatedAt = new Date();
            post.tags.clear();
            if (tagIds != null && !tagIds.isEmpty()) {
                for (Long tagId : tagIds) {
                    Tag tag = Tag.findById(tagId);
                    if (tag != null) post.tags.add(tag);
                }
            }
            post.persist();
        }
        index();
    }

    @Path("/Posts/{id}/delete")
    @POST
    @Transactional
    public void delete(@RestPath Long id) {
        Post post = Post.find("id = ?1 and deletedAt is null", id).firstResult();
        if (post != null) {
            post.deletedAt = new Date();
            post.persist();
        }
        index();
    }
}
