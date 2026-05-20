package rest;

import java.util.List;

import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkiverse.renarde.Controller;
import model.Post;
import model.Tag;

public class Blog extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(List<Post> posts);
        public static native TemplateInstance show(Post post);
        public static native TemplateInstance byTag(String tagSlug, List<Post> posts, Tag tag);
        public static native TemplateInstance tags(List<Tag> tags);
    }

    @Path("/")
    public TemplateInstance index() {
        List<Post> posts = Post.list("deletedAt is null and type = 'post' order by createdAt desc");
        return Templates.index(posts);
    }

    @Path("/post/{slug}")
    public TemplateInstance show(@RestPath String slug) {
        Post post = Post.find("slug = ?1 and deletedAt is null", slug).firstResult();
        if (post == null) {
            flash("error", "Post not found");
            return index();
        }
        return Templates.show(post);
    }

    @Path("/tag/{slug}")
    public TemplateInstance byTag(@RestPath String slug) {
        Tag tag = Tag.find("slug = ?1 and deletedAt is null", slug).firstResult();
        if (tag == null) {
            flash("error", "Tag not found");
            return index();
        }
        List<Post> posts = Post.find(
            "select distinct p from Post p join p.tags t where t.id = ?1 and p.deletedAt is null order by p.createdAt desc",
            tag.id
        ).list();
        return Templates.byTag(slug, posts, tag);
    }

    @Path("/tags")
    public TemplateInstance tags() {
        List<Tag> tags = Tag.list("deletedAt is null order by name");
        return Templates.tags(tags);
    }
}
