package com.myolnir.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter{

    private static final Logger log = LoggerFactory.getLogger(OAuth2Configuration.class);

    @Value("${authorization.server.client}")
    private String client;

    @Value("${authorization.server.secret}")
    private String secret;

    @Value("${config.oauth2.privateKey}")
    private String privateKey;

    @Value("${config.oauth2.publicKey}")
    private String publicKey;

    @Value("${authorization.server.redirectionUrl}")
    private String redirectionUrl;

    @Value("${spring.datasource.driverClassName}")
    private String oauthClass;

    @Value("${spring.datasource.url}")
    private String oauthUrl;

    @Value("${spring.datasource.username}")
    private String databaseUser;

    @Value("${spring.datasource.password}")
    private String databasePassword;


    @Autowired
    private DataSource oauthDataSource;

    @Bean
    public JwtAccessTokenConverter tokenEnhancer() {
        log.info("Initializing JWT with public key");
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(privateKey);
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(oauthDataSource);
    }

    @Bean
    public ApprovalStore approvalStore() {
        return new JdbcApprovalStore(oauthDataSource);
    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(oauthDataSource);
    }

    /**
     * Defines the security constraints on the token endpoints /oauth/token_key and /oauth/check_token
     * Client credentials are required to access the endpoints
     *
     * @param oauthServer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .allowFormAuthenticationForClients();
    }

    @Bean
    public OAuth2AuthenticationEntryPoint customEntryPoint() {
        OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        oAuth2AuthenticationEntryPoint.setRealmName("springsec/client");
        oAuth2AuthenticationEntryPoint.setTypeName("Basic");
        return oAuth2AuthenticationEntryPoint;
    }

    /**
     * Defines the authorization and token endpoints and the token services
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints

                // Which authenticationManager should be used for the password grant
                // If not provided, ResourceOwnerPasswordTokenGranter is not configured
                .authenticationManager(authenticationManager)
                //
                .approvalStore(approvalStore())
                .authorizationCodeServices(authorizationCodeServices())
                // Use JwtTokenStore and our jwtAccessTokenConverter
                .tokenStore(tokenStore())
                .accessTokenConverter(tokenEnhancer())
        ;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(oauthDataSource)
                // Confidential client where client secret can be kept safe (e.g. server side)
                .withClient(client).secret(secret)
                .authorities("ROLE_TRUSTED_CLIENT", "ROLE_CLIENT", "ADMIN", "ROLE_ANONYMOUS")
                .authorizedGrantTypes("client_credentials", "authorization_code", "refresh_token")
                .scopes("read", "write", "trust")
                .redirectUris(redirectionUrl)
                .accessTokenValiditySeconds(0)

                .and()

                // Public client where client secret is vulnerable (e.g. mobile apps, browsers)
                .withClient(client) // No secret!
                .authorities("ROLE_TRUSTED_CLIENT", "ADMIN", "ROLE_ANONYMOUS")
                .authorizedGrantTypes("client_credentials", "implicit")
                .scopes("read")
                .redirectUris(redirectionUrl)
                .accessTokenValiditySeconds(0)

                .and()

                // Trusted client: similar to confidential client but also allowed to handle user password
                .withClient("password").secret(secret)
                .authorities("ROLE_TRUSTED_CLIENT", "ROLE_CLIENT", "ADMIN", "ROLE_ANONYMOUS")
                .authorizedGrantTypes("client_credentials", "password", "authorization_code", "refresh_token")
                .scopes("read", "write", "trust")
                .redirectUris(redirectionUrl)
                .accessTokenValiditySeconds(0);
    }

    /**
     * We expose the JdbcClientDetailsService because it has extra methods that the Interface does not have. E.g.
     * {@link org.springframework.security.oauth2.provider.client.JdbcClientDetailsService#listClientDetails()} which we need for the
     * admin page.
     */
    @Bean
    public JdbcClientDetailsService clientDetailsService() {
        return new JdbcClientDetailsService(oauthDataSource);
    }

}
