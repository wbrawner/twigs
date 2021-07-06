package com.wbrawner.twigs.server.user

import com.wbrawner.budgetserver.randomString
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Transient

@Entity
data class User(
    @Id
    val id: String = randomString(),
    @field:Column(name = "username")
    var name: String = "",
    @field:Column(name = "password")
    var passphrase: String = "",
    @Transient
    private val _authorities: Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("USER")),
    var email: String? = null
) : UserDetails {

    override fun getUsername(): String = name

    override fun getPassword(): String = passphrase

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return _authorities
    }
}

data class NewUserRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
)

data class LoginRequest(val username: String? = null, val password: String? = null)

data class UserResponse(val id: String, val username: String, val email: String?) {
    constructor(user: User) : this(user.id, user.username, user.email)
}