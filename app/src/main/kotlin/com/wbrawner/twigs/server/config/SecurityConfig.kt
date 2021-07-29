package com.wbrawner.twigs.server.config

import com.wbrawner.twigs.server.passwordresetrequest.PasswordResetRequestRepository
import com.wbrawner.twigs.server.session.UserSessionRepository
import com.wbrawner.twigs.server.user.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.web.cors.CorsConfiguration
import java.util.*
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
open class SecurityConfig(
    private val env: Environment,
    private val datasource: DataSource,
    private val userSessionRepository: UserSessionRepository,
    private val userRepository: UserRepository,
    private val passwordResetRequestRepository: PasswordResetRequestRepository,
    private val userDetailsService: JdbcUserDetailsService,
    private val environment: Environment
) : WebSecurityConfigurerAdapter() {
    @get:Bean
    open val userDetailsManager: JdbcUserDetailsManager
        get() {
            val userDetailsManager = JdbcUserDetailsManager()
            userDetailsManager.dataSource = datasource
            return userDetailsManager
        }

    @get:Bean
    open val authenticationProvider: DaoAuthenticationProvider
        get() {
            val authProvider = TokenAuthenticationProvider(userSessionRepository, userRepository)
            authProvider.setPasswordEncoder(passwordEncoder)
            authProvider.setUserDetailsService(userDetailsService)
            return authProvider
        }

    @get:Bean
    open val passwordEncoder: PasswordEncoder
        get() = BCryptPasswordEncoder()

    public override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider)
    }

    @Throws(Exception::class)
    public override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/users/new", "/users/login")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(SilentAuthenticationEntryPoint())
            .and()
            .cors()
            .configurationSource {
                val corsConfig = CorsConfiguration()
                corsConfig.applyPermitDefaultValues()
                val corsDomains = environment.getProperty("twigs.cors.domains", "*")
                corsConfig.allowedOrigins = Arrays.asList(*corsDomains.split(",").toTypedArray())
                corsConfig.allowedMethods = listOf(
                    HttpMethod.GET,
                    HttpMethod.POST,
                    HttpMethod.PUT,
                    HttpMethod.DELETE,
                    HttpMethod.OPTIONS
                ).map { obj: HttpMethod -> obj.name }
                corsConfig.allowCredentials = true
                corsConfig
            }
            .and()
            .csrf()
            .disable()
            .addFilter(TokenAuthenticationFilter(authenticationManager()))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}