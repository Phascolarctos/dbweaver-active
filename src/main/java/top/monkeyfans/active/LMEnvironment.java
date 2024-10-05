package top.monkeyfans.active;

public class LMEnvironment {
   private String productPurchaseURL;
   private String archiveURL;
   private boolean earlyAccessProgram;

   public String getProductPurchaseURL() {
      return this.productPurchaseURL;
   }

   public void setProductPurchaseURL(String productPurchaseURL) {
      this.productPurchaseURL = productPurchaseURL;
   }

   public String getArchiveURL() {
      return this.archiveURL;
   }

   public void setArchiveURL(String archiveURL) {
      this.archiveURL = archiveURL;
   }

   public boolean isEarlyAccessProgram() {
      return this.earlyAccessProgram;
   }

   public void setEarlyAccessProgram(boolean earlyAccessProgram) {
      this.earlyAccessProgram = earlyAccessProgram;
   }
}
