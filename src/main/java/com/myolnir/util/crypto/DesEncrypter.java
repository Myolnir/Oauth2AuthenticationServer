package com.myolnir.util.crypto;

import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;


/**
 * Util class for encryption and desencryption passwords.
 */
public class DesEncrypter {
    Cipher ecipher;
    Cipher dcipher;

    public DesEncrypter(SecretKey key) {
        try {
            this.ecipher = Cipher.getInstance("DES");
            this.dcipher = Cipher.getInstance("DES");
            this.ecipher.init(1, key);
            this.dcipher.init(2, key);
        } catch (Exception var3) {
            Logger.getRootLogger().error("Excepcion: ", var3);
        }

    }

    public String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = this.ecipher.doFinal(utf8);
            String var5 = (new BASE64Encoder()).encode(enc);
            return var5;
        } catch (Exception var6) {
            Logger.getRootLogger().error("Excepcion: ", var6);
            return null;
        }
    }

    public String decrypt(String str) {
        try {
            byte[] dec = (new BASE64Decoder()).decodeBuffer(str);
            byte[] utf8 = this.dcipher.doFinal(dec);
            String var5 = new String(utf8, "UTF8");
            return var5;
        } catch (Exception var6) {
            Logger.getRootLogger().error("Excepcion: ", var6);
            return null;
        }
    }

    public static byte[] addParity(byte[] in) {
        byte[] result = new byte[8];
        int resultIx = 1;
        int bitCount = 0;

        for (int i = 0; i < 56; ++i) {
            boolean bit = (in[6 - i / 8] & 1 << i % 8) > 0;
            if (bit) {
                result[7 - resultIx / 8] = (byte) (result[7 - resultIx / 8] | 1 << resultIx % 8 & 255);
                ++bitCount;
            }

            if ((i + 1) % 7 == 0) {
                if (bitCount % 2 == 0) {
                    result[7 - resultIx / 8] = (byte) (result[7 - resultIx / 8] | 1);
                }

                ++resultIx;
                bitCount = 0;
            }

            ++resultIx;
        }

        return result;
    }

    public static SecretKey getSecretKeyKLNetCenter() {
        byte[] raw = new byte[]{1, 16, 48, 74, 5, 11, 53};
        byte[] keyBytes = addParity(raw);

        try {
            DESKeySpec.isParityAdjusted(keyBytes, 0);
        } catch (InvalidKeyException var4) {
            Logger.getRootLogger().error("Excepcion: ", var4);
        }

        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        return key;
    }
}
