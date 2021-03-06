package me.test.oauth2.clientSso.conf

import me.test.oauth2.common.MyOAuth2Properties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean

//import me.test.oauth2.client.MyOAuth2Properties
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.token.AccessTokenProvider
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.http.converter.FormOAuth2AccessTokenMessageConverter
import org.springframework.security.oauth2.http.converter.FormOAuth2ExceptionHttpMessageConverter
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

/**
 *
 */
@Configuration
@EnableOAuth2Client
public class OAuth2ClientConf {

    @Autowired
    private MyOAuth2Properties myOAuth2Props

    @Bean
    MyOAuth2Properties myOAuth2Properties() {
        return new MyOAuth2Properties()
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter, SecurityProperties security) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(security.getFilterOrder() - 10);
        return registration;
    }

    // ---------------------------------------------  OAuth2 : authorization code 4 login

    @Bean
    public OAuth2ProtectedResourceDetails oAuthCodeLoginResourceDetails() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setId(myOAuth2Props.resource.id);
        details.setClientId(myOAuth2Props.client.id);
        details.setClientSecret(myOAuth2Props.client.secret);
        details.setAccessTokenUri(myOAuth2Props.auth.accessTokenUri);
        details.setUserAuthorizationUri(myOAuth2Props.auth.userAuthorizationUri);
        details.setScope(["LOGIN"]);
        return details;
    }

    @Bean
    public OAuth2RestTemplate oAuthCodeLoginRestTemplate(OAuth2ClientContext clientContext,
                                                         AccessTokenProvider accessTokenProvider) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(oAuthCodeLoginResourceDetails(), clientContext);
        restTemplate.setAccessTokenProvider(accessTokenProvider)
        return restTemplate;
    }

    // 以下配置是为了让 OAuth2RestTemplate 使用统一的 RestTemplateBuilder 接口

    @Bean
    FormOAuth2AccessTokenMessageConverter FormOAuth2AccessTokenMessageConverter() {
        return new FormOAuth2AccessTokenMessageConverter();
    }

    @Bean
    FormOAuth2ExceptionHttpMessageConverter FormOAuth2ExceptionHttpMessageConverter() {
        return new FormOAuth2ExceptionHttpMessageConverter();
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        return new JwtAccessTokenConverter()
    }

    // FIXME : 公用的话，是否有问题？
    @Bean
    AccessTokenProvider accessTokenProvider(RestTemplateBuilder restTemplateBuilder) {
        return new AccessTokenProviderChain(Arrays.asList(
                new AuthorizationCodeAccessTokenProvider() {
                    @Override
                    protected RestOperations getRestTemplate() {
                        RestTemplate restTemplate = restTemplateBuilder.build();
                        restTemplate.setErrorHandler(getResponseErrorHandler())
                        setMessageConverters(restTemplate.getMessageConverters());
                        return restTemplate
                    }
                },
                new ImplicitAccessTokenProvider() {
                    @Override
                    protected RestOperations getRestTemplate() {
                        RestTemplate restTemplate = restTemplateBuilder.build();
                        restTemplate.setErrorHandler(getResponseErrorHandler())
                        setMessageConverters(restTemplate.getMessageConverters());
                        return restTemplate
                    }
                },
                new ResourceOwnerPasswordAccessTokenProvider() {
                    @Override
                    protected RestOperations getRestTemplate() {
                        RestTemplate restTemplate = restTemplateBuilder.build();
                        restTemplate.setErrorHandler(getResponseErrorHandler())
                        setMessageConverters(restTemplate.getMessageConverters());
                        return restTemplate
                    }
                },
                new ClientCredentialsAccessTokenProvider() {
                    @Override
                    protected RestOperations getRestTemplate() {
                        RestTemplate restTemplate = restTemplateBuilder.build();
                        restTemplate.setErrorHandler(getResponseErrorHandler())
                        setMessageConverters(restTemplate.getMessageConverters());
                        return restTemplate
                    }
                }
        ));
    }

}