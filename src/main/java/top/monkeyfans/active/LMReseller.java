package top.monkeyfans.active;

import java.util.Date;

public class LMReseller {
   private long id;
   private String code;
   private String description;
   private boolean active;
   private String email;
   private String country;
   private int discount;
   private Date createDate;

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public String getCountry() {
      return this.country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   public int getDiscount() {
      return this.discount;
   }

   public void setDiscount(int discount) {
      this.discount = discount;
   }

   public Date getCreateDate() {
      return this.createDate;
   }

   public void setCreateDate(Date createDate) {
      this.createDate = createDate;
   }
}
