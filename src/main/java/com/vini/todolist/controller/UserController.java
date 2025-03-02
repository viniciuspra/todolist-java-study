package com.vini.todolist.controller;

import com.vini.todolist.errors.ErrorResponse;
import com.vini.todolist.exceptions.UserAlreadyExistsException;
import com.vini.todolist.repository.IUserRepository;
import com.vini.todolist.model.UserModel;
import com.vini.todolist.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody UserModel userModel) {
        var dbUser = this.userRepository.findByUsername(userModel.getUsername());

        if (dbUser != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        var hashedPassword = Utils.hashPassword(userModel.getPassword());
        userModel.setPassword(hashedPassword);
        var savedUser = this.userRepository.save(userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

}
