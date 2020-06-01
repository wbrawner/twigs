package com.wbrawner.budgetserver.user;

import com.wbrawner.budgetserver.ErrorResponse;
import com.wbrawner.budgetserver.budget.BudgetRepository;
import com.wbrawner.budgetserver.permission.UserPermissionRepository;
import com.wbrawner.budgetserver.permission.UserPermissionResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
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

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.wbrawner.budgetserver.Utils.getCurrentUser;

@RestController
@RequestMapping("/users")
@Api(value = "Users", tags = {"Users"}, authorizations = {@Authorization("basic")})
@Transactional
public class UserController {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionRepository userPermissionsRepository;
    private final DaoAuthenticationProvider authenticationProvider;

    @Autowired
    public UserController(BudgetRepository budgetRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          UserPermissionRepository userPermissionsRepository,
                          DaoAuthenticationProvider authenticationProvider) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPermissionsRepository = userPermissionsRepository;
        this.authenticationProvider = authenticationProvider;
    }


    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getUsers", nickname = "getUsers", tags = {"Users"})
    ResponseEntity<List<UserPermissionResponse>> getUsers(Long budgetId) {
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
        return ResponseEntity.ok(userPermissions.stream().map(UserPermissionResponse::new).collect(Collectors.toList()));
    }

    @PostMapping(path = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "login", nickname = "login", tags = {"Users"})
    ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        var authReq = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication auth;
        try {
            auth = authenticationProvider.authenticate(authReq);
        } catch (AuthenticationException e) {
            return ResponseEntity.notFound().build();
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.ok(new UserResponse(getCurrentUser()));
    }

    @GetMapping(path = "/me", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "getProfile", nickname = "getProfile", tags = {"Users"})
    ResponseEntity<UserResponse> getProfile() {
        var user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new UserResponse(user));
    }

    @GetMapping(path = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "searchUsers", nickname = "searchUsers", tags = {"Users"})
    ResponseEntity<List<UserResponse>> searchUsers(String query) {
        return ResponseEntity.ok(
                userRepository.findByUsernameContains(query)
                        .stream()
                        .map(UserResponse::new)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping(path = "/{id}")
    @ApiOperation(value = "getUser", nickname = "getUser", tags = {"Users"})
    ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping(path = "", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "newUser", nickname = "newUser", tags = {"Users"})
    ResponseEntity<Object> newUser(@RequestBody NewUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            return ResponseEntity.badRequest().body(new ErrorResponse("Username taken"));
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            return ResponseEntity.badRequest().body(new ErrorResponse("Email taken"));
        if (request.getPassword().isBlank())
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid password"));
        return ResponseEntity.ok(new UserResponse(userRepository.save(new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail()
        ))));
    }

    @PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "updateUser", nickname = "updateUser", tags = {"Users"})
    ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        if (!getCurrentUser().getId().equals(id)) return ResponseEntity.status(403).build();
        var user = userRepository.findById(getCurrentUser().getId()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (request.getUsername() != null) {
            if (userRepository.findByUsername(request.getUsername()).isPresent())
                return ResponseEntity.badRequest().body(new ErrorResponse("Username taken"));
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            if (userRepository.findByEmail(request.getEmail()).isPresent())
                return ResponseEntity.badRequest().body(new ErrorResponse("Email taken"));
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            if (request.getPassword().isBlank())
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid password"));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return ResponseEntity.ok(new UserResponse(userRepository.save(user)));
    }

    @DeleteMapping(path = "/{id}", produces = {MediaType.TEXT_PLAIN_VALUE})
    @ApiOperation(value = "deleteUser", nickname = "deleteUser", tags = {"Users"})
    ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!getCurrentUser().getId().equals(id)) return ResponseEntity.status(403).build();
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}