package com.wbrawner.budgetserver.config

import com.wbrawner.budgetserver.passwordresetrequest.PasswordResetRequestRepository
import com.wbrawner.budgetserver.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig @Autowired
constructor(
        private val env: Environment,
        private val datasource: DataSource,
        private val userRepository: UserRepository,
        private val passwordResetRequestRepository: PasswordResetRequestRepository,
        private val userDetailsService: JdbcUserDetailsService) : WebSecurityConfigurerAdapter() {

    val userDetailsManager: JdbcUserDetailsManager
        @Bean
        get() {
            val userDetailsManager = JdbcUserDetailsManager()
            userDetailsManager.setDataSource(datasource)
            return userDetailsManager
        }

    val authenticationProvider: DaoAuthenticationProvider
        @Bean
        get() = DaoAuthenticationProvider().apply {
            this.setPasswordEncoder(passwordEncoder)
            this.setUserDetailsService(userDetailsService)
        }

    val passwordEncoder: PasswordEncoder
        @Bean
        get() = BCryptPasswordEncoder()

    public override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.authenticationProvider(authenticationProvider)
    }

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers("/users/new", "/login")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and()
                .cors()
                .and()
                .csrf()
                .disable()
    }
}

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class MethodSecurity : GlobalMethodSecurityConfiguration()

