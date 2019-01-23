# PropertySourcesPlaceholderEncrypter spring-boot example

[PropertySourcesPlaceholderEncrypter](https://github.com/pushpendra-jain/spring-examples/tree/master/PropertySourcesPlaceholderEncrypter) is an easiest example I could write to add support for using encrypted values as properties and resolve and decrypt those on the fly.

Anywhere I looked to add support for keeping encrypted values are using PropertyPlaceholderConfigurer but rarely are when using PropertySourcesPlaceholderConfigurer which is the new recommanded version in new spring projects. Most alternative out there ask for using jasypt which I found to be more baggage to do the simple thing and more or less use to do the same way which is done in its predecessor PropertyPlaceholderConfigurer but not in PropertySourcesPlaceholderConfigurer way. So looked into the PropertySourcesPlaceholderConfigurer code and found this easy way to add support for encrypted properties.

1.First we need some class which we will use to do the Decryption. In the example we will write an implementation of EncryptionAwareService and create an static bean of the type.

```java
 /**
     * This is just a demo impl of the bean. Write your own implementation of {@link EncryptionAwareService} 
     * and return that service as bean here.
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
  ```
2.Next we need a custom implementation of **"PropertySourcesPropertyResolver"**. In the example its called **"EncryptionAwarePropertySourcesPropertyResolver"** and override its two methods and that is where we will do the magic of decrypting the encrypted values if needed. We will inject our EncryptionAwareService to do so.
  
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
```
See the line **return encryptionAwareService.tryDecrypt(resolvedText);** in both resolve methods where our tryDecrypt method of **EncryptionAwareService** will decrypt the text if need otherwise will return the same text which was passed.

3.Now in next step we will integrate the resolver written in above step. For this we will create our own **EncryptionAwarePropertySourcesPlaceholderConfigurer** by extending **PropertySourcesPlaceholderConfigurer** and overriding its **processProperties** method and passing it our own **EncryptionAwarePropertySourcesPropertyResolver** instead of using its default **PropertySourcesPropertyResolver** (in its 2nd argument). We are not changing any existing functionality because we are just extending and keeping all the same behavior by calling its super first and using result of those to decrypt that is only if needed.

```java
package com.example.propertySourcesPlaceholderEncrypter.propertyResolvers;

import com.example.propertySourcesPlaceholderEncrypter.services.EncryptionAwareService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;

public class EncryptionAwarePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private Environment environment;
    private EncryptionAwareService encryptionAwareService;

    public EncryptionAwarePropertySourcesPlaceholderConfigurer(EncryptionAwareService encryptionAwareService) {
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
4.We are now ready to use our **EncryptionAwarePropertyPlaceholderConfigurer**. For this we would just create a static bean of the type.
```java
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment environment,
                                                                                            EncryptionAwareService encryptionAwareService) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer
                = new EncryptionAwarePropertySourcesPlaceholderConfigurer(encryptionAwareService);
        propertySourcesPlaceholderConfigurer.setEnvironment(environment);
        propertySourcesPlaceholderConfigurer.setPropertySources(environment.getPropertySources());
        return propertySourcesPlaceholderConfigurer;

    }
```    
and that is it. We would now have support for keeping encrypted values in properties file with **PropertySourcesPlaceholderConfigurer**.
    
In the example I have encrypted string **"text"** and created **SampleController** to return decrypted string.
