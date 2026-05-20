package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Todo {

    public Long id;
    public String task;
    public Date completed;

    private static final AtomicLong counter = new AtomicLong(0);
    private static final List<Todo> all = new ArrayList<>();

    public Todo() {
        this.id = counter.incrementAndGet();
    }

    public static List<Todo> listAll() {
        return all;
    }

    public static Todo findById(Long id) {
        return all.stream().filter(t -> t.id.equals(id)).findFirst().orElse(null);
    }

    public void persist() {
        all.add(this);
    }

    public static void update(Long id, String task) {
        Todo todo = findById(id);
        if (todo != null) {
            todo.task = task;
        }
    }

    public static void delete(Long id) {
        all.removeIf(t -> t.id.equals(id));
    }
}
