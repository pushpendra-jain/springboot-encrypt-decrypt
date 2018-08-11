package com.example.propertySourcesPlaceholderEncrypter;

import com.example.propertySourcesPlaceholderEncrypter.propertyResolvers.EncryptionAwarePropertyPlaceholderConfigurer;
import com.example.propertySourcesPlaceholderEncrypter.services.EncryptionAwareService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

@Configuration
public class AppConfig {

    public static final String APP_SOME_ENC_PWD_PREFIX = "app.someEncPwdPrefix";

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment environment,
                                                                                            EncryptionAwareService encryptionAwareService) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer
                = new EncryptionAwarePropertyPlaceholderConfigurer(encryptionAwareService);
        propertySourcesPlaceholderConfigurer.setEnvironment(environment);
        propertySourcesPlaceholderConfigurer.setPropertySources(environment.getPropertySources());
        return propertySourcesPlaceholderConfigurer;

    }

    /**
     * This is just a demo impl of the bean. Write your own implementation of {@link EncryptionAwareService} and return that service as bean here.
     *
     * @return
     */
    @Bean
    public static EncryptionAwareService encryptionAwareService(Environment environment) {
        return new EncryptionAwareService() {
            @Override
            public String tryDecrypt(String text) {
                if (canTxtDecrypted(text)) {
                    TextEncryptor encryptor = Encryptors.queryableText(environment.getProperty("app.somePassword"),
                            environment.getProperty("app.someSalt"));
                    return encryptor.decrypt(text.split(environment.getProperty(APP_SOME_ENC_PWD_PREFIX))[1]);

                }

                return text;

            }
            // Whatever logic  you need to put to identify encrypted string
            @Override
            public boolean canTxtDecrypted(String text) {
                return StringUtils.hasText(environment.getProperty(APP_SOME_ENC_PWD_PREFIX))
                        && text.startsWith(environment.getProperty(APP_SOME_ENC_PWD_PREFIX));
            }
        };
    }
}
