package rest;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkiverse.renarde.Controller;
import model.Todo;

public class Todos extends Controller {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();
        public static native TemplateInstance todos(List<Todo> todos);
        public static native TemplateInstance edit(Todo todo);
    }

    @Path("/renarde")
    public TemplateInstance index() {
        return Templates.index();
    }

    public TemplateInstance todos() {
        return Templates.todos(Todo.listAll());
    }

    @POST
    public void add(@RestForm @NotBlank String task) {
        if (validationFailed()) {
            todos();
        }
        Todo todo = new Todo();
        todo.task = task;
        todo.persist();
        todos();
    }

    @Path("/Todos/{id}/edit")
    public TemplateInstance edit(@RestPath Long id) {
        Todo todo = Todo.findById(id);
        if (todo == null) {
            todos();
        }
        return Templates.edit(todo);
    }

    @Path("/Todos/{id}/update")
    @POST
    public void update(@RestPath Long id, @RestForm @NotBlank String task) {
        if (validationFailed()) {
            edit(id);
        }
        Todo.update(id, task);
        todos();
    }

    @Path("/Todos/{id}/delete")
    @POST
    public void delete(@RestPath Long id) {
        Todo.delete(id);
        todos();
    }

    @Path("/Todos/{id}/toggle")
    @POST
    public void toggle(@RestPath Long id) {
        Todo todo = Todo.findById(id);
        if (todo != null) {
            todo.completed = (todo.completed == null) ? new Date() : null;
        }
        todos();
    }
}
