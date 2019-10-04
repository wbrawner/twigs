package com.wbrawner.budgetserver.user

import com.wbrawner.budgetserver.ErrorResponse
import com.wbrawner.budgetserver.budget.BudgetRepository
import com.wbrawner.budgetserver.getCurrentUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Authorization
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
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
class UserController @Autowired constructor(
        private val budgetRepository: BudgetRepository,
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val authenticationProvider: DaoAuthenticationProvider
) {

    @Transactional
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "getUsers", nickname = "getUsers", tags = ["Users"])
    fun getUsers(budgetId: Long): ResponseEntity<List<UserResponse>> {
        val budget = budgetRepository.findByUsersContainsAndId(getCurrentUser()!!, budgetId).orElse(null)
                ?: return ResponseEntity.notFound().build()
        Hibernate.initialize(budget.users)
        return ResponseEntity.ok(budget.users.map { UserResponse(it) })
    }

    @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "login", nickname = "login", tags = ["Users"])
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val authReq = UsernamePasswordAuthenticationToken(request.username, request.password)
        val auth = try {
            authenticationProvider.authenticate(authReq)
        } catch (e: AuthenticationException) {
            return ResponseEntity.notFound().build()
        }
        SecurityContextHolder.getContext().authentication = auth
        return ResponseEntity.ok(UserResponse(getCurrentUser()!!))
    }

    @Transactional
    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "searchUsers", nickname = "searchUsers", tags = ["Users"])
    fun searchUsers(query: String): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userRepository.findByNameContains(query).map { UserResponse(it) })
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getUser", nickname = "getUser", tags = ["Users"])
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> = userRepository.findById(id).orElse(null)
            ?.let {
                ResponseEntity.ok(UserResponse(it))
            }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/new", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "newUser", nickname = "newUser", tags = ["Users"])
    fun newUser(@RequestBody request: NewUserRequest): ResponseEntity<Any> {
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
    fun updateUser(@PathVariable id: Long, @RequestBody request: UpdateUserRequest): ResponseEntity<Any> {
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
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        if(getCurrentUser()!!.id != id) return ResponseEntity.status(403).build()
        userRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}