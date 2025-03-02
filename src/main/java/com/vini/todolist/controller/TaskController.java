package com.vini.todolist.controller;

import com.vini.todolist.errors.ErrorResponse;
import com.vini.todolist.exceptions.TaskNotFoundException;
import com.vini.todolist.exceptions.UserNotFoundException;
import com.vini.todolist.model.UserModel;
import com.vini.todolist.repository.ITaskRepository;
import com.vini.todolist.model.TaskModel;
import com.vini.todolist.repository.IUserRepository;
import com.vini.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var user = getUserFromRequest(request);
        taskModel.setUser(user);

        var currentDate = LocalDateTime.now();
        var startedAt = taskModel.getStartedAt();
        var endAt = taskModel.getEndAt();

        if(currentDate.isAfter(startedAt) || currentDate.isAfter(endAt)) {
            throw new IllegalArgumentException("Start/end date cannot be earlier than the current date");
        }

        if (startedAt.isAfter(endAt) || startedAt.isEqual(endAt)) {
            throw new IllegalArgumentException("The end date cannot be earlier than or equal to the start date.");
        }

        var savedTask = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @GetMapping("/")
    public ResponseEntity<?> list(HttpServletRequest request) {
        var user = getUserFromRequest(request);
        var tasks = this.taskRepository.findByUserId(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID taskId) {
        var user = getUserFromRequest(request);
        var task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var updatedTask = this.taskRepository.save(task);
        return ResponseEntity.ok().body(updatedTask);
    }


    private UserModel getUserFromRequest(HttpServletRequest request) {
        UUID userId = UUID.fromString(request.getAttribute("authenticatedUser").toString());
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

}
