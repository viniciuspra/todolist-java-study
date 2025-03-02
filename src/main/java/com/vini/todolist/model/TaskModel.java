package com.vini.todolist.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity(name = "tb_tasks")
public class TaskModel {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(length = 50)
    private String title;
    private String description;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;
    private String priority;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;


    @CreationTimestamp
    private LocalDateTime createdAt;

    public void setTitle(String title) throws IllegalArgumentException {
        if (title.length() > 50) {
            throw new IllegalArgumentException("Title cannot be longer than 50 characters");
        }
        this.title = title;
    }
}
