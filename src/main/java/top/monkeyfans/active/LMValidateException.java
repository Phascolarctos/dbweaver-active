package top.monkeyfans.active;

public class LMValidateException extends LMException {
   private LMLicense license;
   private LMLicenseStatus status;

   public LMValidateException(LMLicense license, LMLicenseStatus status, String message) {
      super(message);
      this.license = license;
      this.status = status;
   }

   public LMValidateException(LMLicense license, LMLicenseStatus status, String message, Throwable cause) {
      super(message, cause);
      this.license = license;
      this.status = status;
   }

   public LMLicense getLicense() {
      return this.license;
   }

   public LMLicenseStatus getStatus() {
      return this.status;
   }
}
