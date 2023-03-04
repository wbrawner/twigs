package com.wbrawner.twigs.user;

import com.wbrawner.twigs.ErrorResponse;
import com.wbrawner.twigs.budget.BudgetRepository;
import com.wbrawner.twigs.permission.UserPermissionRepository;
import com.wbrawner.twigs.permission.UserPermissionResponse;
import com.wbrawner.twigs.session.Session;
import com.wbrawner.twigs.session.SessionResponse;
import com.wbrawner.twigs.session.UserSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wbrawner.twigs.Utils.getCurrentUser;

@RestController
@RequestMapping("/users")
@Transactional
public class UserController {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionRepository userPermissionsRepository;
    private final UserSessionRepository userSessionRepository;
    private final DaoAuthenticationProvider authenticationProvider;

    @Autowired
    public UserController(
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            PasswordEncoder passwordEncoder,
            UserPermissionRepository userPermissionsRepository,
            DaoAuthenticationProvider authenticationProvider
    ) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPermissionsRepository = userPermissionsRepository;
        this.authenticationProvider = authenticationProvider;
    }


    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<List<UserPermissionResponse>> getUsers(String budgetId) {
        var budget = budgetRepository.findById(budgetId).orElse(null);
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        var userPermissions = userPermissionsRepository.findAllByBudget(budget, null);

        var userInBudget = userPermissions.stream()
                .anyMatch(userPermission ->
                        userPermission.getUser().getId().equals(getCurrentUser().getId()));
        if (!userInBudget) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userPermissions.stream()
                .map(UserPermissionResponse::new)
                .collect(Collectors.toList()));
    }

    @PostMapping(path = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<SessionResponse> login(@RequestBody LoginRequest request) {
        var authReq = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication auth;
        try {
            auth = authenticationProvider.authenticate(authReq);
        } catch (AuthenticationException e) {
            return ResponseEntity.notFound().build();
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        var user = Objects.requireNonNull(getCurrentUser());
        var session = userSessionRepository.save(new Session(user.getId()));
        return ResponseEntity.ok(new SessionResponse(session));
    }

    @GetMapping(path = "/me", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<UserResponse> getProfile() {
        var user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new UserResponse(user));
    }

    @GetMapping(path = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<List<UserResponse>> searchUsers(String query) {
        return ResponseEntity.ok(
                userRepository.findByUsernameContains(query)
                        .stream()
                        .map(UserResponse::new)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{id}")
    ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping(
            path = "",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<Object> newUser(@RequestBody NewUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username taken"));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email taken"));
        }
        if (request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid password"));
        }
        return ResponseEntity.ok(new UserResponse(userRepository.save(new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail()
        ))));
    }

    @PutMapping(
            path = "/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        if (!getCurrentUser().getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        var user = userRepository.findById(getCurrentUser().getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (request.getUsername() != null) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Username taken"));
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email taken"));
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            if (request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid password"));
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return ResponseEntity.ok(new UserResponse(userRepository.save(user)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!getCurrentUser().getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}