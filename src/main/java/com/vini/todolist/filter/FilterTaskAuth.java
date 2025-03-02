package com.vini.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vini.todolist.errors.ErrorResponse;
import com.vini.todolist.repository.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;


@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var requestPath = request.getRequestURI();
        var isTaskRequest = requestPath.startsWith("/task/");

        if (isTaskRequest) {
            String decodedCredentials = validateAuthHeader(request, response);
            if (decodedCredentials == null) {
                return;
            }

            String[] credentials = decodedCredentials.split(":");
            String userName = credentials[0];
            String password = credentials[1];
            if (userName == null || password == null) {
                sendErrorResponse(response, "Username or password is missing", 401);
                return;
            }

            var dbUser = this.userRepository.findByUsername(userName);
            if (dbUser == null) {
                sendErrorResponse(response, "User not found", 401);
                return;
            }

            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), dbUser.getPassword());
            if (!result.verified) {
                sendErrorResponse(response, "Invalid username or password", 401);
                return;
            }

            request.setAttribute("authenticatedUser", dbUser.getId());
            filterChain.doFilter(request, response);
            return;
        }


        filterChain.doFilter(request, response);
    }

    private String validateAuthHeader(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String BASIC_AUTH_PREFIX = "Basic ";

        var authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BASIC_AUTH_PREFIX)) {
            sendErrorResponse(response, "Authorization header is missing or malformed", 401);
            return null;
        }

        var encodedCredentials = authorization.substring(BASIC_AUTH_PREFIX.length());
        var decodedCredentials
                = new String(Base64.getDecoder().decode(encodedCredentials));
        if (!decodedCredentials.contains(":") || decodedCredentials.split(":").length != 2) {
            sendErrorResponse(response, "Authorization header is empty or missing", 400);
            return null;
        }

        return decodedCredentials;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message, status);
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
