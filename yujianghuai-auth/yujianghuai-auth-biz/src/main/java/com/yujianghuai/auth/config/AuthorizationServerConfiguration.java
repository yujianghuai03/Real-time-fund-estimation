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
import org.springframework.beans.factory.annotation.Value;
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

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * OAuth2 授权服务器核心安全配置。
 *
 * <p>
 * 主要负责授权服务器端点、业务资源接口、JWT 签发验签、客户端加载、
 * 自定义授权模式以及 Redis token 二次校验等安全能力。
 * </p>
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityPermitAllProperties.class)
public class AuthorizationServerConfiguration {

    /**
     * 默认公钥，仅用于开发环境兜底。
     * <p>
     * 生产环境请通过 JWT_PUBLIC_KEY / JWT_PRIVATE_KEY 覆盖。
     * </p>
     */
    private static final String DEFAULT_PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwJkhCgqkG0wjmhyKgJRs
            tJIdZyxu0qUKKNL8mEwZGYZxFzIZU5O8VmdEd31x3kOwuQ3/zXhSGCRPnRDRvPfy
            6D76q4suF+bHPWQLdfrHjs4aW9RQeNn1N+TkFytobvQJy1AmSUrYP7c2NslqdIQv
            8t17BQr0MJzz8mLYh0dZKldZq6E9qosnBhWJZBkE2fHGKbN28vB28Vr7hFkUS2ka
            uLdD0iB8dxUO5I9VmpOiRWwUF4PrrnP6XWTTJO6IIJLmWO+yFuUY8RjA5HhO4fdm
            GspCBQJQb0ToXfh4C2PGu5A0PoOPYYn5x0XqFzzZY4dRZWL8Sbc27cYpU8BSUOYh
            RwIDAQAB
            -----END PUBLIC KEY-----
            """;

    /**
     * 默认私钥，仅用于开发环境兜底。
     * <p>
     * 生产环境请通过 JWT_PUBLIC_KEY / JWT_PRIVATE_KEY 覆盖。
     * </p>
     */
    private static final String DEFAULT_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDAmSEKCqQbTCOa
            HIqAlGy0kh1nLG7SpQoo0vyYTBkZhnEXMhlTk7xWZ0R3fXHeQ7C5Df/NeFIYJE+d
            ENG89/LoPvqriz4X5sc9ZAt1+seOzhrb1FB42fU35OQXK2hu9AnLUCZJStg/tzY2
            yWp0hC/y3XsFCvQwnPPyYtiHR1kqV1mroT2qiycGFYlkGQTZ8cYps3by8HbxWvuE
            WRRLaRq4t0PSIHx3FQ7kj1Wak6JFbBQXg+uuc/pdZNMk7oggluZY77IW5RjxGMDk
            eE7h92YaykIFAlBvROhd+HgLY8a7kDQ+g49hifnHReoXPNljh1FlYvxJtzbtxilT
            wFJQ5iFHAgMBAAECggEAC6LfgQ+MQPuAm1e1TfFqE55LA6Af3+f+AfzPXDoQvRI7
            1qLoArQaNRuNRTTgD98acFRIJqVDKsKDrOXs99UcymuV68o5dcSmA9N1N6cFQDBq
            JtnTrDRjPrtNf1F/ku1pHYqa3MZMd07MJ7n1q5hDNhyQNoaJTA5Y5qxAhdB7wXmf
            pDI2RGrhvvr77Mn1TEP1hHYcztVxQ1EGIFHNLt/jni8Pp4a8Y7Ogb3UKKRsyKeUG
            E4vLr3YDQEMoqCgfA48fc9g9jHj6p0R3MWaflTfu89jJ80kHu7KqLxroB+OJ4xp7
            yctbgA2nrhl5z/2lRB7w9kLkzZqVs++/Rvuu69mXUQKBgQD/FhD5MmIudE2j2ueK
            Bms5PUC4npMqAlp+Al0vJ57jLgF2R5kP3XMbQodXcXRO08wGMEP6CKdbQV+yStKy
            VyXxiFeRsVpKq80HT/f2DPqaNzwLjLQ/yxIe/8ozKTxk9E6PF5aj2DYmVz4QJLqQ
            WNJe2YPrBwMLpnh2v0Ki4z7Y/wKBgQDBxTR0v3CqZorN7x7qFtMJpGiiZksMZ+aV
            a5eG1aMezFi+Z2OFYFsr+hB/XdsbY5k5YV1WUNxHUJMLJiTQW3Bif5KiO4NX59Rc
            B6SGzXwSq0CBPZYCAvDtdP+smOE/5fDTKf8BwsGcz7knc4sAUddLtq1Uce5nDvHf
            rG+UODeI4QKBgQDoZzznd39AIWqShyA5x10FjhfMA4T3cqQWsJ3iONYoDDNCxssR
            /FhpSrr3SYOFBiOrR91j9KZz6aC/SR2IEbXQHtnru1Fb0P5cT4PtfSNfA4TRtdJn
            vLTAuHxqIqgZGMsfm4J8fSk+D9yWegQWtgMi+7gDnfMaG4ubcwFfd97gGwKBgDzE
            aYeAcM1vBqIQp3uzpzYF9NHPWhKLCCOJQlN/k/FkE7Rjw60n//YlXD+hEtfEUb3z
            DBjj0gdgqEshSxbSMURuV79XmMh29BhYLs9U02+3Na0ZbEWWs04+1zLYbADuJ1da
            koRxy8eUeO+Q1w7yYn2LU0hEPnMoK3XPwckW+twBAoGAAjQuZg3e4SodWcM7g5HZ
            lUSYmKYNQTEYu6E5LGJ0VpGz1qYvQJWcQx9tJ+gNYMiFG3xyRyJaDjVfT0wGycL9
            aLrQW5KUeoRYgT10P2gkGKYItpI56TbUAtdWhnJS2apRbTDzzS76HRHm5kzZJHQF
            bh8P3OeZz6hqPEWU97uX+Vo=
            -----END PRIVATE KEY-----
            """;

    /**
     * JWT 密钥编号。
     *
     * <p>
     * 该值会写入 JWK 的 kid 字段，也会出现在 JWT Header 中，
     * 网关或资源服务器可以通过该编号定位验签公钥。
     * </p>
     */
    @Value("${app.security.jwt.key-id:yujianghuai-default-key}")
    private String jwtKeyId;

    /**
     * 外部配置的 JWT 公钥。
     *
     * <p>
     * 支持通过 application.yml、环境变量 JWT_PUBLIC_KEY 等方式注入。
     * 如果没有配置，则回退到 DEFAULT_PUBLIC_KEY。
     * </p>
     */
    @Value("${app.security.jwt.public-key:}")
    private String jwtPublicKey;

    /**
     * 外部配置的 JWT 私钥。
     *
     * <p>
     * 支持通过 application.yml、环境变量 JWT_PRIVATE_KEY 等方式注入。
     * 如果没有配置，则回退到 DEFAULT_PRIVATE_KEY。
     * </p>
     */
    @Value("${app.security.jwt.private-key:}")
    private String jwtPrivateKey;

    /**
     * 注册安全白名单匹配器。
     *
     * <p>
     * 用于统一判断请求是否命中 permit-all 白名单，
     * 例如 Redis token 二次校验过滤器会使用它跳过公开接口。
     * </p>
     */
    @Bean
    public SecurityPermitAllMatcher securityPermitAllMatcher(SecurityPermitAllProperties properties) {
        // 基于配置文件中的白名单路径创建匹配器
        return new SecurityPermitAllMatcher(properties);
    }

    /**
     * OAuth2 Authorization Server 端点过滤链。
     *
     * <p>
     * 该过滤链只处理 OAuth2 标准端点，例如 /oauth2/token、/oauth2/jwks 等。
     * 优先级最高，避免被普通业务接口过滤链提前拦截。
     * </p>
     */
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

        // 应用 Spring Authorization Server 默认安全配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        // 获取授权服务器配置器，用于定制 token 端点、客户端认证等行为
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        // 获取授权服务器所有端点的请求匹配器
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        // 限定当前过滤链只作用于 OAuth2 授权服务器端点
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
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * 普通业务接口安全过滤链。
     *
     * <p>
     * 负责处理非 OAuth2 标准端点的业务请求：
     * 1. 放行配置中的公开接口；
     * 2. 其他接口要求 JWT 认证；
     * 3. 追加 Redis token 二次校验。
     * </p>
     */
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
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(new TokenRedisValidationFilter(authorizationService, securityPermitAllMatcher), BearerTokenAuthenticationFilter.class)
                .build();
    }

    /**
     * JWT 权限转换器。
     *
     * <p>
     * 除了 Spring Security 默认的 scope 权限外，
     * 还会读取 JWT 中的 authorities claim 并转换成 GrantedAuthority。
     * </p>
     */
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

    /**
     * token 请求参数转换器。
     *
     * <p>
     * 用于把 /oauth2/token 请求转换成对应的 Authentication，
     * 这里同时支持自定义密码模式、邮箱验证码模式以及 OAuth2 默认授权模式。
     * </p>
     */
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

    /**
     * OAuth2 客户端仓库。
     *
     * <p>
     * 从数据库 sys_oauth_client 中读取客户端配置，而不是使用内存客户端。
     * </p>
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(SysOauthClientMapper clientMapper) {
        return new SysOauthRegisteredClientRepository(clientMapper);
    }

    /**
     * 用户详情服务。
     *
     * <p>
     * Spring Security 通过该服务加载用户、密码、角色和权限信息。
     * </p>
     */
    @Bean
    public UserDetailsService userDetailsService(AuthUserDetailsService authUserDetailsService) {
        return authUserDetailsService;
    }

    /**
     * OAuth2 token 生成器。
     *
     * <p>
     * JwtGenerator 用于生成 access_token，OAuth2RefreshTokenGenerator 用于生成 refresh_token。
     * </p>
     */
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder, AuthTokenCustomizer tokenCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(tokenCustomizer);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, new OAuth2RefreshTokenGenerator());
    }

    /**
     * OAuth2 授权信息存储服务。
     *
     * <p>
     * 使用 Redis 保存授权信息，便于服务端主动让 token 失效。
     * </p>
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(StringRedisTemplate stringRedisTemplate) {
        return new RedisOAuth2AuthorizationService(stringRedisTemplate);
    }

    /**
     * OAuth2 授权确认存储服务。
     *
     * <p>
     * 当前使用内存实现，后续如果需要持久化授权确认，可替换为 Redis 或数据库实现。
     * </p>
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    /**
     * 授权服务器基础配置。
     *
     * <p>
     * 当前使用默认配置，生产环境如有固定域名，可显式设置 issuer。
     * </p>
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * JWK 密钥源。
     *
     * <p>
     * JWT 签名和验签依赖这里返回的 RSAKey：
     * 1. 私钥用于授权服务器签发 JWT；
     * 2. 公钥通过 /oauth2/jwks 暴露给网关或资源服务器验签；
     * 3. kid 用于标识当前密钥。
     * </p>
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // 从配置文件或默认内置密钥中加载 RSA JWK
        RSAKey rsaKey = loadRsaKey();
        // JWKSet 是 JWKS 的集合结构，/oauth2/jwks 会基于它输出公钥信息
        JWKSet jwkSet = new JWKSet(rsaKey);
        // 使用不可变 JWKSource，供 JwtEncoder 和 JwtDecoder 使用
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * JWT 解码器。
     *
     * <p>
     * 用于资源服务器校验 JWT 签名并解析 claim。
     * </p>
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * JWT 编码器。
     *
     * <p>
     * 授权服务器签发 access_token 时会使用该编码器完成 JWT 签名。
     * </p>
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 密码编码器。
     *
     * <p>
     * 用于校验用户密码和客户端密钥。
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册自定义 OAuth2 授权模式 Provider。
     *
     * <p>
     * 当前注册密码模式和邮箱验证码模式。
     * </p>
     */
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

    /**
     * 加载 RSA 密钥。
     *
     * <p>
     * 加载优先级：
     * 1. 如果配置了 app.security.jwt.public-key 和 app.security.jwt.private-key，
     *    则使用用户配置的 RSA 密钥；
     * 2. 如果用户没有配置，则使用 DEFAULT_PUBLIC_KEY 和 DEFAULT_PRIVATE_KEY；
     * 3. 不再每次启动随机生成密钥。
     * </p>
     *
     * <p>
     * 这样可以保证服务重启后 JWT 签名密钥保持一致，
     * 避免 access_token 因服务重启全部失效。
     * </p>
     */
    private RSAKey loadRsaKey() {
        try {
            // 如果配置文件中有公钥，则使用配置公钥；否则使用内置默认公钥
            String publicKeyText = StringUtils.hasText(jwtPublicKey) ? jwtPublicKey : DEFAULT_PUBLIC_KEY;
            // 如果配置文件中有私钥，则使用配置私钥；否则使用内置默认私钥
            String privateKeyText = StringUtils.hasText(jwtPrivateKey) ? jwtPrivateKey : DEFAULT_PRIVATE_KEY;

            // 创建 RSA KeyFactory，用于把 PEM/Base64 文本转换成 Java RSA Key 对象
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 去掉 PEM 头尾和换行，只保留 Base64 内容，然后解码成公钥字节数组
            byte[] publicKeyBytes = Base64.getDecoder().decode(normalizePem(publicKeyText));
            // 去掉 PEM 头尾和换行，只保留 Base64 内容，然后解码成私钥字节数组
            byte[] privateKeyBytes = Base64.getDecoder().decode(normalizePem(privateKeyText));

            // X509EncodedKeySpec 用于解析标准 SubjectPublicKeyInfo 格式的 RSA 公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            // PKCS8EncodedKeySpec 用于解析 PKCS#8 格式的 RSA 私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            // 构建 RSA JWK，公钥用于验签，私钥用于签名
            return new RSAKey.Builder(publicKey)
                    // 设置私钥，JwtEncoder 签发 JWT 时会使用
                    .privateKey(privateKey)
                    // 设置 key id，JWT Header 和 JWKS 中都会出现该值
                    .keyID(jwtKeyId)
                    // 构建 RSAKey
                    .build();
        } catch (Exception exception) {
            // 密钥加载失败时直接阻止服务启动，避免系统在无法签发/验证 JWT 的状态下运行
            throw new IllegalStateException("Unable to load RSA key pair", exception);
        }
    }

    /**
     * 标准化 PEM 密钥内容。
     *
     * <p>
     * 配置文件或环境变量中的密钥可能是以下形式：
     * 1. 带 PEM 头尾的多行格式；
     * 2. 带换行转义的单行格式；
     * 3. 纯 Base64 格式。
     * </p>
     *
     * <p>
     * 该方法会移除 PEM 头尾、空格、换行符、制表符，
     * 最终得到可以被 Base64 解码的纯密钥内容。
     * </p>
     */
    private String normalizePem(String pem) {
        return pem
                // 移除公钥 PEM 开始标记
                .replace("-----BEGIN PUBLIC KEY-----", "")
                // 移除公钥 PEM 结束标记
                .replace("-----END PUBLIC KEY-----", "")
                // 移除私钥 PEM 开始标记
                .replace("-----BEGIN PRIVATE KEY-----", "")
                // 移除私钥 PEM 结束标记
                .replace("-----END PRIVATE KEY-----", "")
                // 移除所有空白字符，包括换行、空格、Tab
                .replaceAll("\\s", "")
                // 去除首尾空白
                .trim();
    }
}