package com.example.propertySourcesPlaceholderEncrypter.services;

import org.springframework.lang.Nullable;

public interface EncryptionAwareService {

    /**
     * This method should first checks to see passed text has expected prefix and then only it will try to decrypt it otherwise returns the passed text as it is.
     * @param text
     * @return
     */
    @Nullable
    String tryDecrypt(String text);

    // Whatever logic  you need to put to identify encrypted string
    boolean canTxtDecrypted(String text);
}
