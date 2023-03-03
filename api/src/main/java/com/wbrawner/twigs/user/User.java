package com.wbrawner.twigs.user;

import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.wbrawner.twigs.Utils.randomId;

@Entity
public class User implements UserDetails {
    @Id
    private final String id = randomId();
    @Transient
    private final List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
    private String username;
    private String password;
    private String email;

    public User() {
        this(null, null, null);
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @NonNull
    public String getId() {
        // This shouldn't ever need to be set manually, only through Hibernate
        //noinspection ConstantConditions
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}


