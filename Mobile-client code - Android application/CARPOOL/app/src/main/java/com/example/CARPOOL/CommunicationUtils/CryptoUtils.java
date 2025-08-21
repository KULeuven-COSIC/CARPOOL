package com.example.CARPOOL.CommunicationUtils;

import android.util.Log;

import com.macasaet.fernet.Key;
import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CryptoUtils {

    private static final String TAG = "CryptoUtils";

    private final static byte[] salt = new byte[] {9,-18,50,-9,-60,74,122,30,122,46,30,54,8,75,10,-17}; // b'\t\xee2\xf7\xc4Jz\x1ez.\x1e6\x08K\n\xef'

    /**
     * derives the key with fernet for java
     */
    private static byte[] deriveKey(String password, byte[] salt) {
        try {
            int iterations = 100000;
            int keyLength = 32 * 8;
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey key = factory.generateSecret(spec);
            return key.getEncoded();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * decrypt a string (of the format: "b'.....'" with a given secret)
     * the b and ' marks need to be removed first
     */
    public static String decrypt(String encrypted, String secret) {
        try {
            // make sure the token is in the correct form
            StringBuilder builder = new StringBuilder(encrypted);
            builder.deleteCharAt(0);
            encrypted = builder.toString();
            encrypted = encrypted.replace("'", "");

            // turn string into Token object
            // Sometimes errors seem to show up here, haven't looked into it yet but seems to only happen because of a wrong input caused by an activity/instance which is re-used
            Token token = Token.fromString(encrypted);

            // Derive fernet key
            byte[] key = deriveKey(secret, salt);
            //byte[] key = deriveKey("58180532491236541444287395032816871065368084765292838761769880049643509438734", salt); //hardcoded secret
            Key fernetKey = new Key(key);


            //decrypt
            //key is valid max time after creation
            //returns null if something goes wrong
            Validator<String> validator = new StringValidator() {
                public TemporalAmount getTimeToLive() {
                    return Duration.ofSeconds(Instant.MAX.getEpochSecond());
                }
            };

            String decrypted = token.validateAndDecrypt(fernetKey, validator);
            return decrypted;
        } catch (Exception e) {
            Log.e(TAG,"Decryption failed",e);
            return null;
        }


    }
}
