package com.seamfix.nimc.maybeach.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.seamfix.nimc.maybeach.dto.DeviceActivationDataPojo;
import com.seamfix.nimc.maybeach.dto.DeviceActivationResultPojo;
import com.seamfix.nimc.maybeach.utils.EncryptionKeyUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestEncryptionKeyUtils {
    @Autowired
    EncryptionKeyUtil encryptionKeyUtil;

    @Autowired
    Gson gson;

    @Test
    public void decryptData(){
        String encryptedData = "h7YiaI4hkMZL6ibUYCxUTHgdl3sn0aT6gt0KiK9Eogi/Zmnru+EWu2hcaiHjIPdHR0AAGZG+f6l7oUg/CILlIyc1eBVMBxK4a8CShW1VB5FLtYekvjLEhP6s3L3Wn3INTNknpNb6g+QkLfsZS+oCAw3s2INZRDyw5YdlmOA2uNgjhEPXROTZ+jMmNtzcr+Nhhe8pfvDN1zA+ccCppYynowf2qmCkQCHX0iVBTQDQWSvTnC0rwpOcUlwCG4AsngNUl0UGdwflNHZsvxqp0rf/0sEY6PNYqoTgdrB+6dD3ZRyhKXzPt8Y4TFeto8nEOcIP0Nu1VdJxddUxOi23mtIt3+BwbXi8ysN8edRbY9HwTtZJVNuZ0B3TjbD2uN3v+YbjEvRjvzg5KSPox6mnVu8CG4ZWuIpe+38xQK/8wmtuD8zZob8yWk1KQatIx9oGg9ZI9Jg1/c/n4UjHdWE15YyGFwBaJYUwPk5ZsmGSqx7hDrsZI7ymeW7MrSkMU1oVbOsWTtR74yDpAKfGUoyBJtvURkiLUSrnnn/Qu5FfDGP0rbiNW6kdJsrOr6N9qMDFwE3iLrIZnWQkuCLcKYIq5RQFPDtQLlimpYFXfELPTGlgKFxAX71RAUPMvyPjtIicmC9lfSzvTieoWadEncCTe84+uZk23UMg6i6YZNtCKTlpkL7SDT0+lbf7WOr8gIjfeg8gdfLkZGrc1ugoGx3KSFP3oMFrOOY+VVMzIen202HH/hw="; // replace with your encrypted data
        String key = "_w0TxwOU4pZ8-FuI"; // Last 16 digits of your API Token
        String data = encryptionKeyUtil.decryptData(encryptedData, key);

        DeviceActivationResultPojo decryptedData = gson.fromJson(data, DeviceActivationResultPojo.class);
//        DeviceActivationResultPojo decryptedData = objectMapper.convertValue(data, DeviceActivationResultPojo.class);
        System.out.println(decryptedData);
        Assertions.assertTrue(data.length() > 0);
    }
}