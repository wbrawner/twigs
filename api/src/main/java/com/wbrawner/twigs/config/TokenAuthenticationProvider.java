package com.wbrawner.twigs.config;

import com.wbrawner.twigs.session.UserSessionRepository;
import com.wbrawner.twigs.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static com.wbrawner.twigs.Utils.twoWeeksFromNow;

public class TokenAuthenticationProvider extends DaoAuthenticationProvider {
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public TokenAuthenticationProvider(UserSessionRepository userSessionRepository, UserRepository userRepository) {
        this.userSessionRepository = userSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void additionalAuthenticationChecks(
            UserDetails userDetails, UsernamePasswordAuthenticationToken authentication
    ) throws AuthenticationException {
        if (!(authentication instanceof SessionAuthenticationToken)) {
            // Additional checks aren't needed since they've already been handled
            super.additionalAuthenticationChecks(userDetails, authentication);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof SessionAuthenticationToken) {
            var session = userSessionRepository.findByToken((String) authentication.getCredentials());
            if (session.isEmpty() || session.get().getExpiration().before(new Date())) {
                throw new BadCredentialsException("Credentials expired");
            }
            var user = userRepository.findById(session.get().getUserId());
            if (user.isEmpty()) {
                throw new InternalAuthenticationServiceException("Failed to find user for token");
            }
            new Thread(() -> {
                // Update the session on a background thread to avoid holding up the request longer than necessary
                var updatedSession = session.get();
                updatedSession.setExpiration(twoWeeksFromNow());
                userSessionRepository.save(updatedSession);
            }).start();
            return new SessionAuthenticationToken(
                    user.get(),
                    authentication.getCredentials(),
                    authentication.getAuthorities()
            );
        } else {
            return super.authenticate(authentication);
        }
    }
}
