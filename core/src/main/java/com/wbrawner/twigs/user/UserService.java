package com.wbrawner.twigs.user;

import com.wbrawner.twigs.session.Session;
import com.wbrawner.twigs.session.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.wbrawner.twigs.Utils.getCurrentUser;

@Service
public class UserService {
    private final DaoAuthenticationProvider authenticationProvider;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    @Autowired
    public UserService(DaoAuthenticationProvider authenticationProvider, UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.authenticationProvider = authenticationProvider;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public Session login(String username, String password) throws AuthenticationException {
        var authReq = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationProvider.authenticate(authReq);
        SecurityContextHolder.getContext().setAuthentication(auth);
        var user = Objects.requireNonNull(getCurrentUser());
        return userSessionRepository.save(new Session(user.getId()));
    }
}