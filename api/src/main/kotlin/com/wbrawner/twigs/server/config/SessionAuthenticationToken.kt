package com.wbrawner.twigs.server.config

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Creates a token with the supplied array of authorities.
 *
 * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
 * represented by this authentication object.
 * @param credentials
 * @param principal
 */
class SessionAuthenticationToken(
    principal: Any?,
    credentials: Any?,
    authorities: Collection<GrantedAuthority>
) : UsernamePasswordAuthenticationToken(principal, credentials, authorities)