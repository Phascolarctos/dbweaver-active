package top.monkeyfans.active;

public interface LMLicenseValidator {
   void validateLicense(LMLicenseManager var1, String var2, LMProduct var3, LMLicense var4) throws LMValidateException;

   void clearLicenseCache(String var1);

   void clearLicenseCache();

   String getLicenseValidationStatus(LMLicense var1);
}
