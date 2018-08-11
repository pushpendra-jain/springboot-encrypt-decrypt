package com.example.propertySourcesPlaceholderEncrypter.propertyResolvers;

import com.example.propertySourcesPlaceholderEncrypter.services.EncryptionAwareService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;

public class EncryptionAwarePropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private Environment environment;
    private EncryptionAwareService encryptionAwareService;

    public EncryptionAwarePropertyPlaceholderConfigurer(EncryptionAwareService encryptionAwareService) {
        this.encryptionAwareService = encryptionAwareService;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     ConfigurablePropertyResolver propertyResolver) throws BeansException {
        super.processProperties(beanFactoryToProcess,
                new EncryptionAwarePropertySourcesPlaceholderResolver(((ConfigurableEnvironment) environment).getPropertySources(), encryptionAwareService));
    }
}
