package com.example.propertySourcesPlaceholderEncrypter.propertyResolvers;

import com.example.propertySourcesPlaceholderEncrypter.services.EncryptionAwareService;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.Objects;

/**
 * A custom implementation of {@link PropertySourcesPropertyResolver} which can resolve encrypted properties as well.
 */
public class EncryptionAwarePropertySourcesPropertyResolver extends PropertySourcesPropertyResolver {

    private EncryptionAwareService encryptionAwareService;


    public EncryptionAwarePropertySourcesPropertyResolver(PropertySources propertySources,
                                                          EncryptionAwareService encryptionAwareService) {
        super(propertySources);
        this.encryptionAwareService = encryptionAwareService;
    }

    /**
     * Overriding method to add support for decrypting the text if it is encrypted.
     * @param text
     * @return
     */
    @Override
    public String resolvePlaceholders(String text) {
        String resolvedText = super.resolvePlaceholders(text);
        if (Objects.isNull(encryptionAwareService))
            return resolvedText;
        return encryptionAwareService.tryDecrypt(resolvedText);
    }

    /**
     * Overriding method to add support for decrypting the text if it is encrypted.
     * @param text
     * @return
     */
    @Override
    public String resolveRequiredPlaceholders(String text) {
        String resolvedText = super.resolveRequiredPlaceholders(text);
        if (Objects.isNull(encryptionAwareService))
            return resolvedText;
        return encryptionAwareService.tryDecrypt(resolvedText);
    }
}
