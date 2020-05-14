package com.wbrawner.budgetserver.user

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.budget.BudgetRepository
import com.wbrawner.budgetserver.getCurrentUser
import com.wbrawner.budgetserver.permission.UserPermissionRepository
import com.wbrawner.budgetserver.permission.UserPermissionResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@RequestMapping("/users")
@Api(value = "Users", tags = ["Users"], authorizations = [Authorization("basic")])
@Transactional
open class UserController(
        private val budgetRepository: BudgetRepository,
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val userPermissionsRepository: UserPermissionRepository,
        private val authenticationProvider: DaoAuthenticationProvider
) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getUsers", nickname = "getUsers", tags = ["Users"])
    open fun getUsers(budgetId: Long): ResponseEntity<List<UserPermissionResponse>> {
        val userPermissions = budgetRepository.findById(budgetId)
                .orElse(null)
                ?.run {
                    userPermissionsRepository.findAllByBudget(this, null)
                }
                ?: return ResponseEntity.notFound().build()
        if (userPermissions.none { it.user!!.id == getCurrentUser()!!.id }) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(userPermissions.map { UserPermissionResponse(it) })
    }

    @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "login", nickname = "login", tags = ["Users"])
    open fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val authReq = UsernamePasswordAuthenticationToken(request.username, request.password)
        val auth = try {
            authenticationProvider.authenticate(authReq)
        } catch (e: AuthenticationException) {
            return ResponseEntity.notFound().build()
        }
        SecurityContextHolder.getContext().authentication = auth
        return ResponseEntity.ok(UserResponse(getCurrentUser()!!))
    }

    @GetMapping("/me", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getProfile", nickname = "getProfile", tags = ["Users"])
    open fun getProfile(): ResponseEntity<UserResponse> {
        val user = getCurrentUser()?: return ResponseEntity.status(401).build()
        return ResponseEntity.ok(UserResponse(user))
    }

    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "searchUsers", nickname = "searchUsers", tags = ["Users"])
    open fun searchUsers(query: String): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userRepository.findByNameContains(query).map { UserResponse(it) })
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getUser", nickname = "getUser", tags = ["Users"])
    open fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> = userRepository.findById(id).orElse(null)
            ?.let {
                ResponseEntity.ok(UserResponse(it))
            }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newUser", nickname = "newUser", tags = ["Users"])
    open fun newUser(@RequestBody request: NewUserRequest): ResponseEntity<Any> {
        if (userRepository.findByName(request.username).isPresent)
            return ResponseEntity.badRequest()
                    .body(ErrorResponse("Username taken"))
        if (userRepository.findByEmail(request.email).isPresent)
            return ResponseEntity.badRequest()
                    .body(ErrorResponse("Email taken"))
        if (request.password.isBlank())
            return ResponseEntity.badRequest()
                    .body(ErrorResponse("Invalid password"))
        return ResponseEntity.ok(UserResponse(userRepository.save(User(
                name = request.username,
                passphrase = passwordEncoder.encode(request.password),
                email = request.email
        ))))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "updateUser", nickname = "updateUser", tags = ["Users"])
    open fun updateUser(@PathVariable id: Long, @RequestBody request: UpdateUserRequest): ResponseEntity<Any> {
        if (getCurrentUser()!!.id != id) return ResponseEntity.status(403)
                .body(ErrorResponse("Attempting to modify another user's budget"))
        var user = userRepository.findById(getCurrentUser()!!.id!!).orElse(null)?: return ResponseEntity.notFound().build()
        if (request.username != null) {
            if (userRepository.findByName(request.username).isPresent) throw RuntimeException("Username taken")
            user = user.copy(name = request.username)
        }
        if (request.email != null) {
            if (userRepository.findByEmail(request.email).isPresent) throw RuntimeException("Email taken")
            user = user.copy(email = request.email)
        }
        if (request.password != null) {
            if (request.password.isBlank()) throw RuntimeException("Invalid password")
            user = user.copy(passphrase = passwordEncoder.encode(request.password))
        }
        return ResponseEntity.ok(UserResponse(userRepository.save(user)))
    }

    @DeleteMapping("/{id}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "deleteUser", nickname = "deleteUser", tags = ["Users"])
    open fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        if(getCurrentUser()!!.id != id) return ResponseEntity.status(403).build()
        userRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}