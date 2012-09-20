package com.scottagarman.android.imageloadersample.imageloader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashTool {
    private static final char[] HEX_CHARACTERS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
    * @param urlString Image URL
    * @return Hash for image URL
    */
    public static String getKeyForUrl(String urlString) {
        String result = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(urlString.getBytes(), 0, urlString.length());
            byte[] resultBytes = digest.digest();
            StringBuilder hexStringBuilder = new StringBuilder(2 * resultBytes.length);
            for(final byte b : resultBytes) {
                hexStringBuilder.append(HEX_CHARACTERS[(b & 0xf0) >> 4]).append(HEX_CHARACTERS[b & 0x0f]);
            }
            result = hexStringBuilder.toString();
        }catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }
}
