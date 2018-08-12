# spring-examples

[PropertySourcesPlaceholderEncrypter](https://github.com/pushpendra-jain/spring-examples/tree/master/PropertySourcesPlaceholderEncrypter) is an easiest example I could write to add support for using encrypted values as properties and resolve and decrypt those on the fly.

Anywhere I looked to add support for keeping encrypted values are using PropertyPlaceholderConfigurer but rarely are when using PropertySourcesPlaceholderConfigurer which is the new recommanded version in new spring projects. Most alternative out there ask for using jasypt which I found to be more baggage to do the simple thing and more or less use to do the same way which is done in its predecessor PropertyPlaceholderConfigurer but not in PropertySourcesPlaceholderConfigurer way. So looked into the PropertySourcesPlaceholderConfigurer code and found this easy way to add support for encrypted properties.

1. First we need some class which we will use to do the Decryption. In the example we will write an implementation of EncryptionAwareService and create an static bean of the type.

```java
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

    @Override
    public String resolvePlaceholders(String text) {
        String resolvedText = super.resolvePlaceholders(text);
        if (Objects.isNull(encryptionAwareService))
            return resolvedText;
        return encryptionAwareService.tryDecrypt(resolvedText);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) {
        String resolvedText = super.resolveRequiredPlaceholders(text);
        if (Objects.isNull(encryptionAwareService))
            return resolvedText;
        return encryptionAwareService.tryDecrypt(resolvedText);
    }
}
```
  
See the line **return encryptionAwareService.tryDecrypt(resolvedText);** in both resolve methods where our tryDecrypt method of **EncryptionAwareService** will decrypt the text if need otherwise will return the same text which was passed.

3. Now in next step we will integrate the resolver written in above step. For this we will create our own **EncryptionAwarePropertyPlaceholderConfigurer** by extending **PropertySourcesPlaceholderConfigurer** and overriding its **processProperties** method and passing it our own **EncryptionAwarePropertySourcesPlaceholderResolver** instead of using its default **PropertySourcesPlaceholderConfigurer** (in its 2nd argument). We are not changing any existing functionality because we are just extending and keeping all the same behavior by calling its super first and using result of those to decrypt that is only if needed.

```java
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
                new EncryptionAwarePropertySourcesPropertyResolver(((ConfigurableEnvironment) environment).getPropertySources(), encryptionAwareService));
    }
}
```
4. We are now ready to use our **EncryptionAwarePropertyPlaceholderConfigurer**. For this we would just create a static bean of the type.

```java

@Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment environment,
                                                                                            EncryptionAwareService encryptionAwareService) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer
                = new EncryptionAwarePropertyPlaceholderConfigurer(encryptionAwareService);
        propertySourcesPlaceholderConfigurer.setEnvironment(environment);
        propertySourcesPlaceholderConfigurer.setPropertySources(environment.getPropertySources());
        return propertySourcesPlaceholderConfigurer;

    }
```    
and that is it. We would now have support for keeping encrypted values in properties file with **PropertySourcesPlaceholderConfigurer**.
    
In the example I have encrypted string **"text"** and created **SampleController** to return decrypted string.
