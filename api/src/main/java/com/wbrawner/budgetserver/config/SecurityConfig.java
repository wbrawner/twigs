package com.wbrawner.budgetserver.config;

import com.wbrawner.budgetserver.passwordresetrequest.PasswordResetRequestRepository;
import com.wbrawner.budgetserver.session.UserSessionRepository;
import com.wbrawner.budgetserver.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final Environment env;
    private final DataSource datasource;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final JdbcUserDetailsService userDetailsService;
    private final Environment environment;

    public SecurityConfig(Environment env,
                          DataSource datasource,
                          UserSessionRepository userSessionRepository,
                          UserRepository userRepository,
                          PasswordResetRequestRepository passwordResetRequestRepository,
                          JdbcUserDetailsService userDetailsService,
                          Environment environment) {
        this.env = env;
        this.datasource = datasource;
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    @Bean
    public JdbcUserDetailsManager getUserDetailsManager() {
        var userDetailsManager = new JdbcUserDetailsManager();
        userDetailsManager.setDataSource(datasource);
        return userDetailsManager;
    }

    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider() {
        var authProvider = new TokenAuthenticationProvider(userSessionRepository, userRepository);
        authProvider.setPasswordEncoder(getPasswordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(getAuthenticationProvider());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/users/new", "/users/login")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(new SilentAuthenticationEntryPoint())
                .and()
                .cors()
                .configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.applyPermitDefaultValues();
                    var corsDomains = environment.getProperty("twigs.cors.domains", "*");
                    corsConfig.setAllowedOrigins(Arrays.asList(corsDomains.split(",")));
                    corsConfig.setAllowedMethods(
                            Stream.of(
                                    HttpMethod.GET,
                                    HttpMethod.POST,
                                    HttpMethod.PUT,
                                    HttpMethod.DELETE,
                                    HttpMethod.OPTIONS
                            )
                                    .map(Enum::name)
                                    .collect(Collectors.toList())
                    );
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                })
                .and()
                .csrf()
                .disable()
                .addFilter(new TokenAuthenticationFilter(authenticationManager()))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}

