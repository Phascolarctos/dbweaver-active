package top.monkeyfans.active;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LMLicenseManager {
   private static final Logger log = Logger.getLogger("LMLicenseManager");
   private static final String DEFAULT_PURCHASE_URL = "https://dbeaver.com/buy";
   private static final File LEGACY_CONFIG_PATH = new File(System.getProperty("user.home"), ".jkiss-lm");
   private static final boolean DELETE_EMPTY_LICENSE_FILE = false;
   private final LMEnvironment environment;
   private final LMKeyProvider keyProvider;
   private final LMLicenseValidator validator;
   private final Map<String, Map<String, LMLicense>> licenseCache = new HashMap<>();
   private final Map<String, LMSubscription> subscriptionCache = new LinkedHashMap<>();
   private final List<LMLicenseListener> licenseListeners = new ArrayList<>();
   private File configPath;
   private static Path licensePath;
   private static final List<Path> licenseSearchPath = new ArrayList<>();

   public static Path getLicenseCustomPath() {
      return licensePath;
   }

   public static void setLicenseCustomPath(String path) {
      licensePath = Path.of(path);
   }

   public static void addLicenseSearchPath(Path file) {
      licenseSearchPath.add(file);
   }

   public LMLicenseManager(LMEnvironment environment, LMKeyProvider keyProvider, LMLicenseValidator validator) {
      this(environment, keyProvider, validator, LEGACY_CONFIG_PATH);
   }

   public LMLicenseManager(LMEnvironment environment, LMKeyProvider keyProvider, LMLicenseValidator validator, File configPath) {
      this.environment = environment;
      this.keyProvider = keyProvider;
      this.validator = validator;
      this.configPath = configPath;
   }

   public File getConfigPath() {
      return this.configPath;
   }

   public void setConfigPath(File configPath) {
      this.configPath = configPath;
   }

   public static void setLicensePath(Path licensePath) {
      LMLicenseManager.licensePath = licensePath;
   }

   public void addLicenseListener(LMLicenseListener listener) {
      if (listener == null) {
         log.severe("Ignored attempt to add null LMLicenseListener");
      } else {
         boolean remove = this.licenseListeners.remove(listener);
         if (remove) {
            log.warning(String.format("Removed existing LMLicenseListener %s", listener));
         }

         boolean add = this.licenseListeners.add(listener);
         if (add) {
            log.fine(String.format("Added LMLicenseListener %s", listener));
         } else {
            log.severe(String.format("Failed to add LMLicenseListener %s", listener));
         }
      }
   }

   public void removeLicenseListener(LMLicenseListener listener) {
      if (listener == null) {
         log.severe("Ignored attempt to remove null LMLicenseListener");
      } else {
         boolean remove = this.licenseListeners.remove(listener);
         if (remove) {
            log.fine(String.format("Removed LMLicenseListener %s", listener));
         } else {
            log.warning(String.format("Nothing to remove for LMLicenseListener %s", listener));
         }
      }
   }

   private void fireLicenseChanged(String id, LMLicense[] value) {
      LMLicense[] copy = Arrays.copyOf(value, value.length);

      for (LMLicenseListener listener : this.licenseListeners) {
         try {
            listener.licenseChanged(id, copy);
         } catch (Throwable var9) {
            String pattern = "Error while processing licenseChanged for %s";
            String message = String.format(pattern, listener);
            log.log(Level.SEVERE, message, var9);
         }
      }
   }

   
   public LMLicense[] getProductLicenses( LMProduct product) {
      String id = product.getId();
      Map<String, LMLicense> licenseMap = this.licenseCache.get(id);
      if (licenseMap != null) {
         return licenseMap.values().toArray(new LMLicense[0]);
      } else {
         LMLicense[] licenses = this.readProductLicenses(product);
         this.fireLicenseChanged(id, licenses);
         return licenses;
      }
   }

   public LMLicense importLicense( LMProduct product,  String clientId,  byte[] licenseData) throws LMException {
      Key decryptionKey = this.keyProvider.getDecryptionKey(product);
      if (decryptionKey == null) {
         throw new LMException("Product '" + product.getId() + "' decryption key not found");
      } else {
         LMLicense license = new LMLicense(licenseData, decryptionKey);
         if (this.validator != null) {
            this.validator.validateLicense(this, clientId, product, license);
            LMLicense updatedLicense = this.findImportedLicenseById(product, license.getLicenseId());
            if (updatedLicense != null && updatedLicense.getLicenseId().equals(license.getLicenseId())) {
               return updatedLicense;
            }
         }

         this.importLicense(product, license, false);
         return license;
      }
   }

   public void importLicense( LMProduct product, LMLicense license, boolean forceImport) throws LMException {
      if (!forceImport && license.isExpired() && !license.isSubscription()) {
         String exceptionMessageTemplate = "License %s is expired. You can purchase a new license on our website: %s.";
         String purchaseUrl = this.environment.getProductPurchaseURL();
         if (purchaseUrl == null || purchaseUrl.isBlank()) {
            purchaseUrl = "https://dbeaver.com/buy";
         }

         throw new LMException(String.format(exceptionMessageTemplate, license.getLicenseId(), purchaseUrl));
      } else if (!license.isValidForProduct(product)) {
         String exceptionMessageTemplate = "License %s doesn't match product %s. If you want to access this DBeaver version, you need to extend your license support or purchase a new license.";
         throw new LMException(String.format(exceptionMessageTemplate, license.getProductId(), product.getId()));
      } else if (!forceImport && !license.isValidFor(product, null, false)) {
         String exceptionMessageTemplate = "License %s is out of support. This product version is not available for your license. You need to extend the support and maintenance period to access this and upcoming DBeaver versions. Otherwise, you can download the previous versions from the archive";
         String archiveLink = this.environment.getArchiveURL();
         if (archiveLink != null && !archiveLink.isBlank()) {
            exceptionMessageTemplate = exceptionMessageTemplate + ": " + archiveLink;
         } else {
            exceptionMessageTemplate = exceptionMessageTemplate + ".";
         }

         throw new LMException(String.format(exceptionMessageTemplate, license.getLicenseId()));
      } else {
         LMLicense[] currentLicenses = this.getProductLicenses(product);
         Map<String, LMLicense> licenseMap = new LinkedHashMap<>();

         for (LMLicense lic : currentLicenses) {
            licenseMap.put(lic.getLicenseId(), lic);
         }

         if (license.getLicenseType() == LMLicenseType.TRIAL) {
            for (LMLicense oldLic : currentLicenses) {
               if (oldLic.getLicenseType() == LMLicenseType.TRIAL
                  && !oldLic.getLicenseId().equals(license.getLicenseId())
                  && oldLic.getProductVersion().equalsIgnoreCase(license.getProductVersion())) {
                  String exceptionMessageTemplate = "You can't import a trial license for %s %s more than once.";
                  throw new LMException(String.format(exceptionMessageTemplate, license.getProductId(), license.getProductVersion()));
               }

               if (oldLic.getLicenseType() == LMLicenseType.EAP) {
                  throw new LMException("You can't import a trial license when participating in the early access program.");
               }
            }
         }

         licenseMap.put(license.getLicenseId(), license);
         this.licenseCache.put(product.getId(), licenseMap);
         this.saveProductLicenses(product);
         this.fireLicenseChanged(product.getId(), currentLicenses);
      }
   }

   public void updateSubscription( LMProduct product,  LMSubscription subscription) throws LMException {
      this.subscriptionCache.put(subscription.getLicenseId(), subscription);
      this.saveProductLicenses(product);
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void saveProductLicenses(LMProduct product) throws LMException {
      File prodLicenseFile = this.getProductLicensesFile(product, false);
      Map<String, LMLicense> cache = this.licenseCache.get(product.getId());
      File lmDir = prodLicenseFile.getParentFile();
      if (!lmDir.exists() && !lmDir.mkdirs()) {
         log.warning("Can't create directory '" + lmDir.getAbsolutePath() + "'");
      }

      try {
         Throwable e = null;
         Object var6 = null;

         try {
            OutputStream out = new FileOutputStream(prodLicenseFile);

            try {
               XMLBuilder xml = new XMLBuilder(out, "utf-8");
               xml.setButify(true);
               Throwable var9 = null;
               Object var10 = null;

               try {
                  XMLBuilder.Element el1 = xml.startElement("product");

                  try {
                     xml.addAttribute("id", product.getId());

                     for (LMLicense license : cache.values()) {
                        byte[] encodedData = license.getEncoded();
                        if (encodedData == null) {
                           log.warning("License '" + license.getLicenseId() + "' is not encoded");
                        } else {
                           Throwable var15 = null;
                           Object var16 = null;

                           try {
                              XMLBuilder.Element el2 = xml.startElement("license");

                              try {
                                 xml.addAttribute("type", "standard");
                                 xml.addText(Base64.getEncoder().encodeToString(encodedData));
                              } finally {
                                 if (el2 != null) {
                                    el2.close();
                                 }
                              }
                           } catch (Throwable var93) {
                              if (var15 == null) {
                                 var15 = var93;
                              } else if (var15 != var93) {
                                 var15.addSuppressed(var93);
                              }

                              throw var15;
                           }
                        }
                     }

                     for (LMSubscription subscription : this.subscriptionCache.values()) {
                        byte[] encodedData = subscription.getEncoded();
                        if (encodedData == null) {
                           log.warning("Subscription '" + subscription.getLicenseId() + "' is not encoded");
                        } else {
                           Throwable var102 = null;
                           Object var103 = null;

                           try {
                              XMLBuilder.Element el2 = xml.startElement("subscription");

                              try {
                                 xml.addAttribute("type", "standard");
                                 xml.addText(Base64.getEncoder().encodeToString(encodedData));
                              } finally {
                                 if (el2 != null) {
                                    el2.close();
                                 }
                              }
                           } catch (Throwable var91) {
                              if (var102 == null) {
                                 var102 = var91;
                              } else if (var102 != var91) {
                                 var102.addSuppressed(var91);
                              }

                              throw var102;
                           }
                        }
                     }
                  } finally {
                     if (el1 != null) {
                        el1.close();
                     }
                  }
               } catch (Throwable var95) {
                  if (var9 == null) {
                     var9 = var95;
                  } else if (var9 != var95) {
                     var9.addSuppressed(var95);
                  }

                  throw var9;
               }

               xml.flush();
            } finally {
               if (out != null) {
                  out.close();
               }
            }
         } catch (Throwable var97) {
            if (e == null) {
               e = var97;
            } else if (e != var97) {
               e.addSuppressed(var97);
            }

            throw e;
         }
      } catch (Throwable var98) {
         throw new LMException("IO error while saving license file", var98);
      }
   }

   
   private LMLicense[] readProductLicenses( LMProduct product) {
      this.licenseCache.clear();
      this.subscriptionCache.clear();
      Map<String, LMLicense> licenses = new LinkedHashMap<>();
      List<LMSubscription> subscriptions = new ArrayList<>();
      if (licensePath != null) {
         if (!Files.exists(licensePath)) {
            log.warning("License file '" + licensePath.toAbsolutePath() + "' doesn't exist");
         } else {
            this.readLicenseFromFile(product, licensePath, licenses);
         }
      } else {
         for (Path licenseFile : licenseSearchPath) {
            if (Files.exists(licenseFile)) {
               this.readLicenseFromFile(product, licenseFile, licenses);
            }
         }
      }

      File legacyProdLicenseFile = this.getProductLicensesFile(product, true);
      File modernProdLicenseFile = this.getProductLicensesFile(product, false);
      if (!this.configPath.equals(LEGACY_CONFIG_PATH) && !modernProdLicenseFile.exists()) {
         this.getLicensesFromLMDirectory(product, licenses, subscriptions, legacyProdLicenseFile);
      }

      this.getLicensesFromLMDirectory(product, licenses, subscriptions, modernProdLicenseFile);
      this.licenseCache.put(product.getId(), licenses);

      for (LMSubscription subscription : subscriptions) {
         this.subscriptionCache.put(subscription.getLicenseId(), subscription);
      }

      return licenses.values().toArray(new LMLicense[0]);
   }

   private void getLicensesFromLMDirectory(
       LMProduct product, Map<String, LMLicense> licenses, List<LMSubscription> subscriptions,  File prodLicenseFile
   ) {
      if (prodLicenseFile.exists()) {
         try {
            Document document = XMLUtils.parseDocument(prodLicenseFile);
            Element rootElement = document.getDocumentElement();
            if (!"product".equals(rootElement.getTagName()) || !product.getId().equals(rootElement.getAttribute("id"))) {
               throw new LMException("Bad license file structure");
            }

            for (Element licenseElement : XMLUtils.getChildElementList(rootElement, "license")) {
               String licenseType = licenseElement.getAttribute("type");
               if (licenseType != null && !licenseType.isBlank()) {
                  String licenseEncoded = XMLUtils.getElementBody(licenseElement);
                  if ("standard".equals(licenseType) && licenseEncoded != null) {
                     LMLicense license = this.readStandardLicense(product, licenseEncoded);
                     if (license != null) {
                        licenses.put(license.getLicenseId(), license);
                     }
                  } else {
                     log.warning("Unsupported license type: " + licenseType);
                  }
               } else {
                  log.warning("No license type");
               }
            }

            for (Element subElement : XMLUtils.getChildElementList(rootElement, "subscription")) {
               String licenseType = subElement.getAttribute("type");
               if (licenseType != null && !licenseType.isBlank()) {
                  String subEncoded = XMLUtils.getElementBody(subElement);
                  if ("standard".equals(licenseType) && subEncoded != null) {
                     LMSubscription subscription = this.readStandardSubscription(product, subEncoded);
                     if (subscription != null) {
                        subscriptions.add(subscription);
                     }
                  } else {
                     log.warning("Unsupported subscription type: " + licenseType);
                  }
               } else {
                  log.warning("No license type");
               }
            }
         } catch (XMLException | LMException var12) {
            log.log(Level.SEVERE, "Error parse product license file '" + prodLicenseFile, (Throwable)var12);
         }
      }
   }

   private void readLicenseFromFile( LMProduct product,  Path path,  Map<String, LMLicense> licenses) {
      try {
         String licenseEncoded = Files.readString(path);
         LMLicense license = this.readStandardLicense(product, licenseEncoded);
         if (license != null) {
            licenses.put(license.getLicenseId(), license);
         }
      } catch (Exception var6) {
         log.warning("Error loading custom license from " + path.toAbsolutePath() + ": " + var6.getMessage());
      }
   }

   
   private File getProductLicensesFile( LMProduct product, boolean legacy) {
      return !legacy ? new File(this.configPath, product.getId() + ".lic") : new File(LEGACY_CONFIG_PATH, product.getId() + ".lic");
   }

   public LMLicense readStandardLicense( LMProduct product,  String licenseEncoded) throws LMException {
      Key decryptionKey = this.keyProvider.getDecryptionKey(product);
      if (decryptionKey == null) {
         throw new LMException("Product '" + product.getId() + "' decryption key not found");
      } else {
         byte[] licenseEncrypted;
         try {
            licenseEncrypted = LMUtils.readEncryptedString(new StringReader(licenseEncoded));
         } catch (IOException var8) {
            log.log(Level.SEVERE, "Error reading license", (Throwable)var8);
            return null;
         } catch (Throwable e) {
             throw new RuntimeException(e);
         }

          LMLicense license = null;

         try {
            license = new LMLicense(licenseEncrypted, decryptionKey);
         } catch (LMException var7) {
            log.log(Level.SEVERE, "Error parsing license", (Throwable)var7);
         }

         if (license != null && license.getLicenseType() == LMLicenseType.EAP && !this.environment.isEarlyAccessProgram()) {
            license = null;
         }

         return license;
      }
   }

   private LMSubscription readStandardSubscription( LMProduct product,  String subEncoded) throws LMException {
      Key decryptionKey = this.keyProvider.getDecryptionKey(product);
      if (decryptionKey == null) {
         throw new LMException("Product '" + product.getId() + "' decryption key not found");
      } else {
         byte[] subEncrypted;
         try {
            subEncrypted = LMUtils.readEncryptedString(new StringReader(subEncoded));
         } catch (IOException var7) {
            log.log(Level.SEVERE, "Error reading subscription", (Throwable)var7);
            return null;
         } catch (Throwable e) {
             throw new RuntimeException(e);
         }

          try {
            return new LMSubscription(subEncrypted, decryptionKey);
         } catch (LMException var6) {
            log.log(Level.SEVERE, "Error parsing subscription", (Throwable)var6);
            return null;
         }
      }
   }

   public LMLicense getValidProductLicense(String clientId,  LMProduct product) throws LMValidateException {
      LMLicense[] licenses = this.getProductLicenses(product);
      if (licenses.length == 0) {
         return null;
      } else {
         LMValidateException validateError = null;
         List<LMLicense> validLicenses = new ArrayList<>();

         for (LMLicense license : licenses) {
            if (license.isValidFor(product, null, true)) {
               if (clientId != null && this.validator != null && (validLicenses.isEmpty() || license.getLicenseType() != LMLicenseType.TRIAL)) {
                  try {
                     this.validator.validateLicense(this, clientId, product, license);
                  } catch (LMValidateException var11) {
                     validateError = var11;
                     continue;
                  }
               }

               validLicenses.add(license);
            }
         }

         if (!validLicenses.isEmpty()) {
            validLicenses.sort(Comparator.comparing(LMLicense::getProductVersion).thenComparing(LMLicense::getLicenseStartTime));

            for (int i = validLicenses.size(); i > 0; i--) {
               LMLicense licensex = validLicenses.get(i - 1);
               if (licensex.getLicenseType() != LMLicenseType.TRIAL) {
                  return licensex;
               }
            }

            return validLicenses.get(validLicenses.size() - 1);
         } else if (validateError != null) {
            throw validateError;
         } else {
            return null;
         }
      }
   }

   public boolean hasProductLicense( LMProduct product) throws LMValidateException {
      return this.getValidProductLicense(null, product) != null;
   }

   public LMLicense findImportedLicenseById( LMProduct product,  String licenseId) {
      return Arrays.stream(this.getProductLicenses(product)).filter(license -> license.getLicenseId().equals(licenseId)).findFirst().orElse(null);
   }

   public LMLicense findTrialLicense(String clientId, LMProduct product) throws LMValidateException {
      LMValidateException validateError = null;
      LMLicense[] licenses = this.getProductLicenses(product);
      LMLicense[] var8 = licenses;
      int var7 = licenses.length;
      int var6 = 0;

      LMLicense license;
      while (true) {
         if (var6 >= var7) {
            if (validateError != null) {
               if (validateError.getStatus() == LMLicenseStatus.EXPIRED) {
                  return null;
               }

               throw validateError;
            }

            return null;
         }

         license = var8[var6];
         if (license.getLicenseType() == LMLicenseType.TRIAL && license.isValidFor(product, null, false)) {
            if (this.validator == null) {
               break;
            }

            try {
               this.validator.validateLicense(this, clientId, product, license);
               break;
            } catch (LMValidateException var10) {
               validateError = var10;
            }
         }

         var6++;
      }

      return license;
   }

   public void deleteLicense( LMProduct product,  LMLicense license) throws LMException {
      Map<String, LMLicense> licenseMap = this.licenseCache.get(product.getId());
      if (licenseMap == null) {
         throw new LMException("Internal error: product licenses not found");
      } else {
         licenseMap.remove(license.getLicenseId());
         this.subscriptionCache.remove(license.getLicenseId());
         this.validator.clearLicenseCache(license.getLicenseId());
         this.saveProductLicenses(product);
         this.fireLicenseChanged(product.getId(), licenseMap.values().toArray(new LMLicense[0]));
      }
   }

   public void clearLicenseCache() {
      this.validator.clearLicenseCache();
   }

   public void validateLicense(String clientId, LMProduct product, LMLicense license) throws LMValidateException {
      if (this.validator != null) {
         this.validator.validateLicense(this, clientId, product, license);
      }
   }

   public LMSubscription getSubscriptionInfo(LMLicense license) {
      return this.subscriptionCache.get(license.getLicenseId());
   }

   public LMSubscription readSubscriptionFromData( LMProduct product, byte[] encrypted) throws LMException {
      return new LMSubscription(encrypted, this.keyProvider.getDecryptionKey(product));
   }

   public String getLicenseValidationStatus(LMProduct product, LMLicense license) {
      LMStatusDetails licenseStatus = license.getLicenseStatus(product);
      if (licenseStatus.isValid()) {
         String validationStatus = this.validator.getLicenseValidationStatus(license);
         if (validationStatus != null) {
            return validationStatus;
         }
      }

      return licenseStatus.getMessage();
   }
}
