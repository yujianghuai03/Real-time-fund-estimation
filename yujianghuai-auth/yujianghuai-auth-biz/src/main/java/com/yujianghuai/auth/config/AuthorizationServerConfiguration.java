package com.yujianghuai.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.yujianghuai.auth.filter.TokenRedisValidationFilter;
import com.yujianghuai.auth.mapper.SysOauthClientMapper;
import com.yujianghuai.auth.matcher.SecurityPermitAllMatcher;
import com.yujianghuai.auth.repository.RedisOAuth2AuthorizationService;
import com.yujianghuai.auth.repository.SysOauthRegisteredClientRepository;
import com.yujianghuai.auth.service.AuthLoginPermissionService;
import com.yujianghuai.auth.service.AuthUserDetailsService;
import com.yujianghuai.auth.support.core.AuthTokenCustomizer;
import com.yujianghuai.auth.support.core.PublicClientCustomGrantAuthenticationConverter;
import com.yujianghuai.auth.support.core.PublicClientCustomGrantAuthenticationProvider;
import com.yujianghuai.auth.support.email.EmailCodeAuthenticationService;
import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationConverter;
import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationProvider;
import com.yujianghuai.auth.support.handler.AuthFailureHandler;
import com.yujianghuai.auth.support.handler.AuthSuccessHandler;
import com.yujianghuai.auth.support.password.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import com.yujianghuai.auth.support.password.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import com.yujianghuai.common.constant.SecurityConstants;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityPermitAllProperties.class)
public class AuthorizationServerConfiguration {
    @Bean
    public SecurityPermitAllMatcher securityPermitAllMatcher(SecurityPermitAllProperties properties) {
        return new SecurityPermitAllMatcher(properties);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationService authorizationService,
            AuthorizationServerSettings authorizationServerSettings,
            OAuth2TokenGenerator<?> tokenGenerator,
            AuthSuccessHandler successHandler,
            AuthFailureHandler failureHandler,
            AuthUserDetailsService authUserDetailsService,
            AuthLoginPermissionService loginPermissionService,
            EmailCodeAuthenticationService emailCodeAuthenticationService,
            RegisteredClientRepository registeredClientRepository,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        http.securityMatcher(endpointsMatcher);
        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(accessTokenRequestConverter())
                        .accessTokenResponseHandler(successHandler)
                        .errorResponseHandler(failureHandler))
                .clientAuthentication(client -> client
                        .authenticationConverters(converters -> converters.add(0,
                                new PublicClientCustomGrantAuthenticationConverter()))
                        .authenticationProviders(providers -> providers.add(0,
                                new PublicClientCustomGrantAuthenticationProvider(registeredClientRepository)))
                        .errorResponseHandler(failureHandler))
                .authorizationService(authorizationService)
                .authorizationServerSettings(authorizationServerSettings)
                .oidc(Customizer.withDefaults());

        addCustomOAuth2GrantAuthenticationProvider(http, authorizationService,
                tokenGenerator, authUserDetailsService, loginPermissionService,
                emailCodeAuthenticationService, passwordEncoder);

        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                        ))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))) //JWT认证
        ;


        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SecurityFilterChain applicationSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            OAuth2AuthorizationService authorizationService,
            SecurityPermitAllProperties permitAllProperties,
            SecurityPermitAllMatcher securityPermitAllMatcher) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    Map<String, List<String>> methods = permitAllProperties.getMethods();
                    if (methods != null) {
                        methods.forEach((method, paths) -> {
                            if (paths != null && !paths.isEmpty()) {
                                HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase(Locale.ROOT));
                                registry.requestMatchers(httpMethod, paths.toArray(String[]::new)).permitAll();
                            }
                        });
                    }

                    List<String> paths = permitAllProperties.getPaths();
                    if (paths != null && !paths.isEmpty()) {
                        registry.requestMatchers(paths.toArray(String[]::new)).permitAll();
                    }

                    registry.anyRequest().authenticated();
                })
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"code\":401,\"message\":\"" + SecurityConstants.AUTH_UNAUTHORIZED_MESSAGE + "\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"code\":403,\"message\":\"" + SecurityConstants.AUTH_ACCESS_DENIED_MESSAGE + "\",\"data\":null}");
                        }))
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))//JWT认证
                .addFilterAfter(new TokenRedisValidationFilter(authorizationService,securityPermitAllMatcher), BearerTokenAuthenticationFilter.class) //Redis 认证
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }
            Object claim = jwt.getClaim(AuthTokenCustomizer.CLAIM_AUTHORITIES);
            if (claim instanceof Collection<?> claimAuthorities) {
                for (Object authority : claimAuthorities) {
                    if (authority != null && StringUtils.hasText(authority.toString())) {
                        authorities.add(new SimpleGrantedAuthority(authority.toString()));
                    }
                }
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    public AuthenticationConverter accessTokenRequestConverter() {
        return new DelegatingAuthenticationConverter(Arrays.asList(
                new OAuth2ResourceOwnerPasswordAuthenticationConverter(),
                new OAuth2ResourceOwnerEmailAuthenticationConverter(),
                new org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter(),
                new org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter(),
                new org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter(),
                new org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeRequestAuthenticationConverter()));
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(SysOauthClientMapper clientMapper) {
        return new SysOauthRegisteredClientRepository(clientMapper);
    }

    @Bean
    public UserDetailsService userDetailsService(AuthUserDetailsService authUserDetailsService) {
        return authUserDetailsService;
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder, AuthTokenCustomizer tokenCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(tokenCustomizer);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2RefreshTokenGenerator());
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(StringRedisTemplate stringRedisTemplate) {
        return new RedisOAuth2AuthorizationService(stringRedisTemplate);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void addCustomOAuth2GrantAuthenticationProvider(
            HttpSecurity http,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<?> tokenGenerator,
            AuthUserDetailsService authUserDetailsService,
            AuthLoginPermissionService loginPermissionService,
            EmailCodeAuthenticationService emailCodeAuthenticationService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(authUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        AuthenticationManager authenticationManager = new ProviderManager(daoAuthenticationProvider);
        http.authenticationProvider(new OAuth2ResourceOwnerPasswordAuthenticationProvider(
                authenticationManager, authorizationService, tokenGenerator, loginPermissionService));
        http.authenticationProvider(new OAuth2ResourceOwnerEmailAuthenticationProvider(
                authenticationManager, authorizationService, tokenGenerator,
                authUserDetailsService, emailCodeAuthenticationService));
    }

    private RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate RSA key pair", exception);
        }
    }
}
