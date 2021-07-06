package com.wbrawner.twigs.server.config

import com.wbrawner.budgetserver.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
open class JdbcUserDetailsService @Autowired constructor(private val userRepository: UserRepository) : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        var userDetails: UserDetails?
        userDetails = userRepository.findByName(username).orElse(null)
        if (userDetails != null) {
            return userDetails
        }
        userDetails = userRepository.findByEmail(username).orElse(null)
        if (userDetails != null) {
            return userDetails
        }
        throw UsernameNotFoundException("Unable to find user with username \$username")
    }
}