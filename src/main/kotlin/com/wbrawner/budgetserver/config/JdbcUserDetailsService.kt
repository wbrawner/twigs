package com.wbrawner.budgetserver.config

import com.wbrawner.budgetserver.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class JdbcUserDetailsService @Autowired
constructor(private val userRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByName(username).orElse(null)
                ?: throw UsernameNotFoundException("Unable to find user with username $username")
    }
}