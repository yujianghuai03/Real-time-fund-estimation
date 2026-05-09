package com.yujianghuai.auth.support.core;

import com.yujianghuai.auth.support.email.OAuth2ResourceOwnerEmailAuthenticationProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicClientCustomGrantAuthenticationProviderTest {

    private static final String CLIENT_ID = "yujianghuai-client";

    @Test
    void shouldAuthenticatePublicClientForEmailCodeGrant() {
        PublicClientCustomGrantAuthenticationConverter converter =
                new PublicClientCustomGrantAuthenticationConverter();
        PublicClientCustomGrantAuthenticationProvider provider =
                new PublicClientCustomGrantAuthenticationProvider(repository(emailCodeClient()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth2/token");
        request.setParameter(OAuth2ParameterNames.CLIENT_ID, CLIENT_ID);
        request.setParameter(OAuth2ParameterNames.GRANT_TYPE,
                OAuth2ResourceOwnerEmailAuthenticationProvider.GRANT_TYPE);

        Authentication authentication = provider.authenticate(converter.convert(request));

        assertThat(authentication).isInstanceOf(OAuth2ClientAuthenticationToken.class);
        assertThat(authentication.isAuthenticated()).isTrue();
        OAuth2ClientAuthenticationToken clientAuthentication =
                (OAuth2ClientAuthenticationToken) authentication;
        assertThat(clientAuthentication.getRegisteredClient().getClientId()).isEqualTo(CLIENT_ID);
        assertThat(clientAuthentication.getClientAuthenticationMethod())
                .isEqualTo(ClientAuthenticationMethod.NONE);
    }

    @Test
    void shouldSkipUnsupportedGrantType() {
        PublicClientCustomGrantAuthenticationConverter converter =
                new PublicClientCustomGrantAuthenticationConverter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth2/token");
        request.setParameter(OAuth2ParameterNames.CLIENT_ID, CLIENT_ID);
        request.setParameter(OAuth2ParameterNames.GRANT_TYPE,
                AuthorizationGrantType.AUTHORIZATION_CODE.getValue());

        assertThat(converter.convert(request)).isNull();
    }

    private RegisteredClient emailCodeClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(OAuth2ResourceOwnerEmailAuthenticationProvider.EMAIL_CODE)
                .scope("email")
                .build();
    }

    private RegisteredClientRepository repository(RegisteredClient registeredClient) {
        return new RegisteredClientRepository() {
            @Override
            public void save(RegisteredClient registeredClient) {
                throw new UnsupportedOperationException();
            }

            @Override
            public RegisteredClient findById(String id) {
                return registeredClient.getId().equals(id) ? registeredClient : null;
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                return registeredClient.getClientId().equals(clientId) ? registeredClient : null;
            }
        };
    }
}
