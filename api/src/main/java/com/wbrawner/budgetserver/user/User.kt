package com.wbrawner.budgetserver.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class User(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String = "",
        val passphrase: String = "",
        val email: String = "",
        val enabled: Boolean = true,
        val credentialsExpired: Boolean = false,
        val isExpired: Boolean = false,
        val isLocked: Boolean = false,
        @Transient val grantedAuthorities: MutableCollection<out GrantedAuthority>
        = mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("USER"))
) : UserDetails {
    override fun getUsername(): String = name

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = grantedAuthorities

    override fun isEnabled(): Boolean = enabled

    override fun isCredentialsNonExpired(): Boolean = !credentialsExpired

    override fun getPassword(): String = passphrase

    override fun isAccountNonExpired(): Boolean = !isExpired

    override fun isAccountNonLocked(): Boolean = !isLocked
}


data class UserResponse(val id: Long, val username: String, val email: String) {
    constructor(user: User) : this(user.id!!, user.name, user.email)
}

data class NewUserRequest(val username: String, val password: String, val email: String)

data class UpdateUserRequest(val username: String?, val password: String?, val email: String?)

data class LoginRequest(val username: String, val password: String)