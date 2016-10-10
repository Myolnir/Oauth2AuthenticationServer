package com.myolnir.configuration;

import com.myolnir.model.UserAccountDO;
import com.myolnir.repository.UserRepository;
import com.myolnir.util.crypto.DesEncrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * Custom authentication provider, in this case it connects with my database, search the user and check if the
 * provided password is the same that database password, in case it not the same throws an exception.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserRepository userRepository;

    private DesEncrypter desEncrypter = new DesEncrypter(DesEncrypter.getSecretKeyKLNetCenter());


    private static List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>(2) {
        {
            add(new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT"));
            add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        }
    };

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Authenticating user " + authentication.getName());
        String email = authentication.getName();
        String pwd = (String) authentication.getCredentials();
        UserAccountDO databaseUser = userRepository.findByEmail(email);
        if (databaseUser.getEmail().equals(authentication.getName())
                && desEncrypter.decrypt(databaseUser.getPassword()).equals(authentication.getCredentials())) {
            log.debug("User was successfully authenticated");
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(authentication.getName(), authentication.getCredentials(), AUTHORITIES);
            return usernamePasswordAuthenticationToken;
        }
        throw new BadCredentialsException("Username/Password does not match for " + authentication.getPrincipal());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
