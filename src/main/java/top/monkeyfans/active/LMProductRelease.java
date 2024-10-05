package top.monkeyfans.active;

import java.util.Date;

public class LMProductRelease {
   private String productId;
   private String productVersion;
   private Date releaseDate;
   private String releaseInfo;

   public LMProductRelease(String productId, String productVersion, Date releaseDate, String releaseInfo) {
      this.productId = productId;
      this.productVersion = productVersion;
      this.releaseDate = releaseDate;
      this.releaseInfo = releaseInfo;
   }

   public String getProductId() {
      return this.productId;
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }

   public String getProductVersion() {
      return this.productVersion;
   }

   public void setProductVersion(String productVersion) {
      this.productVersion = productVersion;
   }

   public Date getReleaseDate() {
      return this.releaseDate;
   }

   public void setReleaseDate(Date releaseDate) {
      this.releaseDate = releaseDate;
   }

   public String getReleaseInfo() {
      return this.releaseInfo;
   }

   public void setReleaseInfo(String releaseInfo) {
      this.releaseInfo = releaseInfo;
   }
}
