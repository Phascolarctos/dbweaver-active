package top.monkeyfans.active;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

public class LMMain {
   private static final Logger log = Logger.getLogger("LMMain");
   private static final LMProduct TEST_PRODUCT = new LMProduct(
      "dbeaver-ue", "DB", "DBeaver Ultimate", "DBeaver Ultimate Edition", "24.2.0", LMProductType.DESKTOP, new Date(), new String[0]
   );


   public static void main(String[] args) throws Throwable {
      System.out.println("LM 2.0");
      if (args.length > 0 && args[0].equals("gen-keys")) {
         System.out.println("Test key generation");
         generateKeyPair();
      } else if (args.length > 0 && args[0].equals("encrypt-license")) {
         System.out.println("Encrypt license");
         encryptLicense();
      } else if (args.length > 0 && args[0].equals("decrypt-license")) {
         System.out.println("Decrypt license");
         decryptLicense();
      } else if (args.length > 0 && args[0].equals("import-license")) {
         System.out.println("Import license");
         importLicense();
      } else {
         System.out.println("Test license generation");
         generateLicense();
      }
   }

   private static void encryptLicense() throws Exception {
      PrivateKey privateKey = readPrivateKey();
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String licenseID = LMUtils.generateLicenseId(TEST_PRODUCT);
      System.out.println("License ID: " + licenseID);
      System.out.println("Product ID (" + TEST_PRODUCT.getId() + "):");
      String productID = in.readLine();
      if (productID.isEmpty()) {
         productID = TEST_PRODUCT.getId();
      }

      System.out.println("Product version (" + TEST_PRODUCT.getVersion() + "):");
      String productVersion = in.readLine();
      if (productVersion.isEmpty()) {
         productVersion = TEST_PRODUCT.getVersion();
      }

      System.out.println("Owner ID (1):");
      String ownerID = in.readLine();
      if (ownerID.isEmpty()) {
         ownerID = "1";
      }

      System.out.println("Owner company (JKISS):");
      String ownerCompany = in.readLine();
      if (ownerCompany.isEmpty()) {
         ownerCompany = "JKISS";
      }

      System.out.println("Owner name:");
      String ownerName = in.readLine();
      System.out.println("Owner email:");
      String ownerEmail = in.readLine();
      LMLicense license = new LMLicense(
         licenseID, LMLicenseType.YEAR_UPDATE, new Date(), new Date(), null, 0L, productID, productVersion, ownerID, ownerCompany, ownerName, ownerEmail
      );
      byte[] licenseData = license.getData();
      byte[] licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey);
      System.out.println("--- LICENSE ---");
      System.out.println(LMUtils.splitLines(Base64.getEncoder().encodeToString(licenseEncrypted), 76));
   }

   private static void decryptLicense() throws Throwable {
      PublicKey publicKey = readPublicKey();
      System.out.println("License:");
      byte[] encryptedLicense = LMUtils.readEncryptedString(System.in);
      LMLicense license = new LMLicense(encryptedLicense, publicKey);
      System.out.println(license);
   }

   private static void importLicense() throws Throwable {
      LMEnvironment lmEnvironment = new LMEnvironment();
      lmEnvironment.setProductPurchaseURL("");
      lmEnvironment.setArchiveURL("");
      final PrivateKey privateKey = readPrivateKey();
      final PublicKey publicKey = readPublicKey();
      System.out.println("License:");
      byte[] encryptedLicense = LMUtils.readEncryptedString(System.in);
      LMLicenseManager lm = new LMLicenseManager(lmEnvironment, new LMKeyProvider() {
         @Override
         public Key getEncryptionKey(LMProduct product) {
            return privateKey;
         }

         @Override
         public Key getDecryptionKey(LMProduct product) {
            return publicKey;
         }
      }, null);
      lm.importLicense(TEST_PRODUCT, "test-client", encryptedLicense);
   }

   private static void generateKeyPair() throws LMException {
      KeyPair keyPair = LMEncryption.generateKeyPair(2048);
      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();
      System.out.println("--- PUBLIC KEY ---");
      System.out.println(LMUtils.splitLines(Base64.getEncoder().encodeToString(publicKey.getEncoded()), 76));
      System.out.println("--- PRIVATE KEY ---");
      System.out.println(LMUtils.splitLines(Base64.getEncoder().encodeToString(privateKey.getEncoded()), 76));
   }

   private static void generateLicense() throws LMException {
      System.out.println("Gen keys");
      KeyPair keyPair = LMEncryption.generateKeyPair(2048);
      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();
      System.out.println("Gen Test license");
      LMLicense license = new LMLicense(
         "JL-0FB16-000A2GC",
         LMLicenseType.YEAR_UPDATE,
         new Date(),
         new Date(),
         null,
         0L,
         TEST_PRODUCT.getId(),
         TEST_PRODUCT.getVersion(),
         "123123",
         "JKISS",
         "Serge Rider",
         "serge@dbeaver.com"
      );
      byte[] data = license.getData();
      byte[] encrypted = LMEncryption.encrypt(data, privateKey);
      String encodedBase64 = LMUtils.splitLines(Base64.getEncoder().encodeToString(encrypted), 76);
      byte[] encodedBinary = Base64.getDecoder().decode(encodedBase64);
      LMLicense licenseCopy = new LMLicense(encodedBinary, publicKey);
      System.out.println(licenseCopy);
      System.out.println("Gen subscription");
      LMSubscription subscription = new LMSubscription("XXX-123", LMSubscriptionPeriod.MONTH, new Date(), new Date(), 1, true);
      data = LMEncryption.encrypt(subscription.getData(), privateKey);
      String subBase64 = LMUtils.splitLines(Base64.getEncoder().encodeToString(data), 76);
      byte[] subBinary = Base64.getDecoder().decode(subBase64);
      LMSubscription subCopy = new LMSubscription(subBinary, publicKey);
      System.out.println(subCopy);
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private static PrivateKey readPrivateKey() throws LMException {
      File keyFile = new File(new File(System.getProperty("user.home"), ".jkiss-lm"), "private-key.txt");
      if (!keyFile.exists()) {
         throw new LMException("Cannot find private key file (" + keyFile.getAbsolutePath() + ")");
      } else {
         try {
            Throwable e = null;
            Object var2 = null;

            try {
               InputStream keyStream = new FileInputStream(keyFile);

               PrivateKey var10000;
               try {
                  byte[] privateKeyData = LMUtils.readEncryptedString(keyStream);
                  var10000 = LMEncryption.generatePrivateKey(privateKeyData);
               } finally {
                  if (keyStream != null) {
                     keyStream.close();
                  }
               }

               return var10000;
            } catch (Throwable var12) {
               if (e == null) {
                  e = var12;
               } else if (e != var12) {
                  e.addSuppressed(var12);
               }

               throw e;
            }
         } catch (Throwable var13) {
            throw new LMException(var13);
         }
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private static PublicKey readPublicKey() throws LMException {
      File keyFile = new File(new File(System.getProperty("user.home"), ".jkiss-lm"), "public-key.txt");
      if (!keyFile.exists()) {
         throw new LMException("Cannot find public key file (" + keyFile.getAbsolutePath() + ")");
      } else {
         try {
            Throwable e = null;
            Object var2 = null;

            try {
               InputStream keyStream = new FileInputStream(keyFile);

               PublicKey var10000;
               try {
                  byte[] keyData = LMUtils.readEncryptedString(keyStream);
                  var10000 = LMEncryption.generatePublicKey(keyData);
               } finally {
                  if (keyStream != null) {
                     keyStream.close();
                  }
               }

               return var10000;
            } catch (Throwable var12) {
               if (e == null) {
                  e = var12;
               } else if (e != var12) {
                  e.addSuppressed(var12);
               }

               throw e;
            }
         } catch (Throwable var13) {
            throw new LMException(var13);
         }
      }
   }
}
