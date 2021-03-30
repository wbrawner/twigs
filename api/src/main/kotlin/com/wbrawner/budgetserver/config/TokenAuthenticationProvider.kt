package com.wbrawner.budgetserver.config

import com.wbrawner.budgetserver.session.UserSessionRepository
import com.wbrawner.budgetserver.twoWeeksFromNow
import com.wbrawner.budgetserver.user.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class TokenAuthenticationProvider(
    private val userSessionRepository: UserSessionRepository,
    private val userRepository: UserRepository
) : DaoAuthenticationProvider() {
    @Throws(AuthenticationException::class)
    override fun additionalAuthenticationChecks(
        userDetails: UserDetails,
        authentication: UsernamePasswordAuthenticationToken
    ) {
        if (authentication !is SessionAuthenticationToken) {
            // Additional checks aren't needed since they've already been handled
            super.additionalAuthenticationChecks(userDetails, authentication)
        }
    }

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        return if (authentication is SessionAuthenticationToken) {
            val session = userSessionRepository.findByToken(authentication.getCredentials() as String)
            if (session!!.isEmpty || session.get().expiration.before(Date())) {
                throw BadCredentialsException("Credentials expired")
            }
            val user = userRepository.findById(session.get().userId)
            if (user.isEmpty) {
                throw InternalAuthenticationServiceException("Failed to find user for token")
            }
            Thread {

                // Update the session on a background thread to avoid holding up the request longer than necessary
                val updatedSession = session.get()
                updatedSession.expiration = twoWeeksFromNow
                userSessionRepository.save(updatedSession)
            }.start()
            SessionAuthenticationToken(user.get(), authentication.getCredentials(), authentication.getAuthorities())
        } else {
            super.authenticate(authentication)
        }
    }
}