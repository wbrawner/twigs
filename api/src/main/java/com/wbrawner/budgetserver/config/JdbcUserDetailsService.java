package com.wbrawner.budgetserver.config;

import com.wbrawner.budgetserver.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public JdbcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails;
        userDetails = userRepository.findByUsername(username).orElse(null);
        if (userDetails != null) {
            return userDetails;
        }
        userDetails = userRepository.findByEmail(username).orElse(null);
        if (userDetails != null) {
            return userDetails;
        }
        throw new UsernameNotFoundException("Unable to find user with username $username");
    }
}