package top.monkeyfans.active;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LMEncryption {
   private static final String ASYNC_TRANSFORMATION_PADDING = "RSA/ECB/PKCS1Padding";
   private static final String SYNC_ALGORITHM = "md5";
   private static final String SYNC_SECRET_KEY_ALGORITHM = "DESede";
   private static final String SYNC_TRANSFORMATION_PADDING = "DESede/CBC/PKCS5Padding";
   private static final String ALGORITHM = "RSA";
   private static final int RSA_CHUNK_MAX_SIZE = 245;
   private static final int RSA_BLOCK_SIZE = 256;

   public static void main(String[] args) {
      String key="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk7ciFU/aUCIgH5flBbGD0t7B3KOmfL0l\n" +
              "BMf2ENuLA0w/T8A1RvteUYk2EQo3UrZ7kMZ8rK93nmDjituN7jlv/bsxGyAox87BbKYSs9oH5f9P\n" +
              "hYHAiTE0PxoMODnl4NgR+Bpc+Ath8wDLHMC+BzYkOy4JQo8EX/ff58TT9UYP8eoDeGdSxQmW3FJC\n" +
              "i82UiC5zIk75dx20Al9ql0fdxnzo31q/2MbnNCAfSchsqrKtzBtheex4JvvqZjxn98wk5Te1QgZz\n" +
              "Caz4ay9dkLVjSt79QYm5hKb8Jt3O5SxSUsrjmYVeG+k2bQlidw8dENwLZmvJkIJi8kb94yEwY/dq\n" +
              "lENDkQIDAQAB";
      byte[] bytes=key.getBytes();

   }

   public static PublicKey generatePublicKey(byte[] publicKeyBytes) throws LMException {
      try {
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
         return keyFactory.generatePublic(publicKeySpec);
      } catch (Exception var3) {
         throw new LMException(var3);
      }
   }

   public static PrivateKey generatePrivateKey(byte[] privateKeyBytes) throws LMException {
      PrivateKey privateKey = null;
      if (privateKeyBytes != null) {
         try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
         } catch (Exception var4) {
            throw new LMException(var4);
         }
      }

      return privateKey;
   }

   public static PrivateKey generatePrivateKey(String encodedPrivateKey) throws LMException {
      byte[] privateKeyBytes = Base64.getDecoder().decode(encodedPrivateKey);
      return generatePrivateKey(privateKeyBytes);
   }

   public static byte[] decrypt(byte[] data, Key key) throws LMException {
      return cipherAsymmetric(data, key, 2);
   }

   private static byte[] cipherAsymmetric(byte[] data, Key key, int mode) throws LMException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int chunkSize = mode == 2 ? 256 : 245;
      int chunkCount = data.length / chunkSize;
      if (data.length % chunkSize > 0) {
         chunkCount++;
      }

      try {
         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

         for (int i = 0; i < chunkCount; i++) {
            cipher.init(mode, key);
            int offset = i * chunkSize;
            int length = chunkSize;
            if (offset + chunkSize > data.length) {
               length = data.length - chunkSize * i;
            }

            byte[] segment = Arrays.copyOfRange(data, offset, offset + length);
            byte[] segmentEncrypted = cipher.doFinal(segment);
            buffer.write(segmentEncrypted);
         }

         return buffer.toByteArray();
      } catch (Exception var12) {
         throw new LMException(var12);
      }
   }

   public static byte[] decrypt(byte[] data, String digestString) throws LMException {
      return cipherSymmetric(data, digestString, 2);
   }

   private static byte[] cipherSymmetric(byte[] data, String digestString, int mode) throws LMException {
      try {
         MessageDigest md = MessageDigest.getInstance("md5");
         byte[] digestOfPassword = md.digest(digestString.getBytes("utf-8"));
         byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
         int j = 0;
         int k = 16;

         while (j < 8) {
            keyBytes[k++] = keyBytes[j++];
         }

         SecretKey key = new SecretKeySpec(keyBytes, "DESede");
         Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
         cipher.init(mode, key, new IvParameterSpec(new byte[8]));
         return cipher.doFinal(data);
      } catch (Exception var8) {
         throw new LMException(var8);
      }
   }

   public static byte[] encrypt(byte[] data, Key key) throws LMException {
      return cipherAsymmetric(data, key, 1);
   }

   public static byte[] encrypt(byte[] data, String digestString) throws LMException {
      return cipherSymmetric(data, digestString, 1);
   }

   public static KeyPair generateKeyPair() throws LMException {
      return generateKeyPair(1024);
   }

   public static KeyPair generateKeyPair(int keySize) throws LMException {
      try {
         KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
         SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
         keyGen.initialize(keySize, random);
         return keyGen.generateKeyPair();
      } catch (Exception var3) {
         throw new LMException(var3);
      }
   }

   private KeyPair generateKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes) throws LMException {
      PublicKey publicKey = generatePublicKey(publicKeyBytes);
      PrivateKey privateKey = generatePrivateKey(privateKeyBytes);
      return new KeyPair(publicKey, privateKey);
   }
}
