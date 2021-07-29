package com.wbrawner.twigs.server.user

import com.wbrawner.twigs.server.ErrorResponse
import com.wbrawner.twigs.server.budget.BudgetRepository
import com.wbrawner.twigs.server.currentUser
import com.wbrawner.twigs.server.permission.UserPermission
import com.wbrawner.twigs.server.permission.UserPermissionRepository
import com.wbrawner.twigs.server.permission.UserPermissionResponse
import com.wbrawner.twigs.server.session.Session
import com.wbrawner.twigs.server.session.SessionResponse
import com.wbrawner.twigs.server.session.UserSessionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/users")
@Transactional
open class UserController @Autowired constructor(
    private val budgetRepository: BudgetRepository,
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userPermissionsRepository: UserPermissionRepository,
    private val authenticationProvider: DaoAuthenticationProvider
) {
    @GetMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getUsers(budgetId: String): ResponseEntity<List<UserPermissionResponse>> {
        val budget = budgetRepository.findById(budgetId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val userPermissions = userPermissionsRepository.findAllByBudget(budget, null)
        val userInBudget = userPermissions.stream()
            .anyMatch { userPermission: UserPermission -> userPermission.user!!.id == currentUser!!.id }
        return if (!userInBudget) {
            ResponseEntity.notFound().build()
        } else ResponseEntity.ok(
            userPermissions.map { userPermission: UserPermission -> UserPermissionResponse(userPermission) }
        )
    }

    @PostMapping(path = ["/login"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun login(@RequestBody request: LoginRequest): ResponseEntity<SessionResponse> {
        val authReq = UsernamePasswordAuthenticationToken(request.username, request.password)
        val auth: Authentication
        auth = try {
            authenticationProvider.authenticate(authReq)
        } catch (e: AuthenticationException) {
            return ResponseEntity.notFound().build()
        }
        SecurityContextHolder.getContext().authentication = auth
        val session = userSessionRepository.save(Session(currentUser!!.id))
        return ResponseEntity.ok(SessionResponse(session))
    }

    @GetMapping(path = ["/me"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun getProfile(): ResponseEntity<UserResponse> {
        val user = currentUser
            ?: return ResponseEntity.status(401).build()
        return ResponseEntity.ok(UserResponse(user))
    }

    @GetMapping(path = ["/search"], produces = [MediaType.APPLICATION_JSON_VALUE])
    open fun searchUsers(query: String?): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(
            userRepository.findByNameContains(query)
                .map { user: User -> UserResponse(user) }
        )
    }

    @GetMapping(path = ["/{id}"])
    open fun getUser(@PathVariable id: String): ResponseEntity<UserResponse> {
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse(user))
    }

    @PostMapping(
        path = [""],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun newUser(@RequestBody request: NewUserRequest): ResponseEntity<Any> {
        if (userRepository.findByName(request.username).isPresent) return ResponseEntity.badRequest()
            .body(ErrorResponse("Username taken"))
        if (userRepository.findByEmail(request.email).isPresent) return ResponseEntity.badRequest()
            .body(ErrorResponse("Email taken"))
        return if (request.password.isBlank()) ResponseEntity.badRequest()
            .body(ErrorResponse("Invalid password")) else ResponseEntity.ok(
            UserResponse(
                userRepository.save(
                    User(
                        name = request.username,
                        passphrase = passwordEncoder.encode(request.password),
                        email = request.email
                    )
                )
            )
        )
    }

    @PutMapping(
        path = ["/{id}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateUser(@PathVariable id: String, @RequestBody request: UpdateUserRequest): ResponseEntity<Any> {
        if (currentUser?.id != id) return ResponseEntity.status(403).build()
        var user = userRepository.findById(currentUser!!.id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (request.username != null) {
            if (userRepository.findByName(request.username).isPresent) return ResponseEntity.badRequest()
                .body(ErrorResponse("Username taken"))
            user = user.copy(name = request.username)
        }
        if (request.email != null) {
            if (userRepository.findByEmail(request.email).isPresent) return ResponseEntity.badRequest()
                .body(ErrorResponse("Email taken"))
            user = user.copy(email = request.email)
        }
        if (request.password != null) {
            if (request.password.isBlank()) return ResponseEntity.badRequest().body(ErrorResponse("Invalid password"))
            user = user.copy(passphrase = passwordEncoder.encode(request.password))
        }
        return ResponseEntity.ok(UserResponse(userRepository.save(user)))
    }

    @DeleteMapping(path = ["/{id}"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun deleteUser(@PathVariable id: String): ResponseEntity<Void> {
        if (currentUser?.id != id) return ResponseEntity.status(403).build()
        userRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}