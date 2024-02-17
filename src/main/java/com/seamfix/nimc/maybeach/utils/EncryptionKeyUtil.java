package com.seamfix.nimc.maybeach.utils;


import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class EncryptionKeyUtil {

    private EncryptionKeyUtil(){
    }

    public static String decryptData (String encryptedData, String key) {
        int ivSize = 16; // IV size for AES is always 16 bytes

        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            byte[] ivPadding = new byte[ivSize];
            System.arraycopy(encryptedBytes, 0, ivPadding, 0, ivPadding.length);

            // Extract encrypted part.
            int encryptedSize = encryptedBytes.length - ivSize;
            byte[] encryptedPart = new byte[encryptedSize];
            System.arraycopy(encryptedBytes, ivSize, encryptedPart, 0, encryptedSize);

            // Decrypt.
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] keyBytes = new byte[32];
            byte[] bytesOfKey = key.getBytes(StandardCharsets.UTF_8);
            int len = bytesOfKey.length;
            if (len > keyBytes.length){
                len = keyBytes.length;
            }
            System.arraycopy(bytesOfKey, 0, keyBytes, 0, len);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivPadding);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedPart);

            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
            return decrypted;
        }catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e){
            Utility.logError("Error decrypting data: {}", e.getMessage());
            return "";
        }
    }

}