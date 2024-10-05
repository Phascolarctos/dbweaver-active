package top.monkeyfans.active;

import java.util.Date;

public class LMCustomer {
   public static final int EMAIL_MAX_LENGTH = 255;
   public static final int FIRST_NAME_MAX_LENGTH = 100;
   public static final int LAST_NAME_MAX_LENGTH = 100;
   public static final int COMPANY_NAME_MAX_LENGTH = 100;
   public static final int SOURCE_MAX_LENGTH = 64;
   public static final int COUNTRY_MAX_LENGTH = 100;
   public static final int STATE_MAX_LENGTH = 100;
   public static final int ZIP_MAX_LENGTH = 100;
   public static final int CITY_MAX_LENGTH = 100;
   public static final int ADDR_MAX_LENGTH = 200;
   private long id;
   private String email;
   private Long passwordHash;
   private Date registrationTime;
   private Date confirmationTime;
   private Date lastVisitTime;
   private String firstName;
   private String lastName;
   private String companyName;
   private LMAddress address;

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getEmail() {
      return this.email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public Long getPasswordHash() {
      return this.passwordHash;
   }

   public void setPasswordHash(Long passwordHash) {
      this.passwordHash = passwordHash;
   }

   public Date getRegistrationTime() {
      return this.registrationTime;
   }

   public void setRegistrationTime(Date registrationTime) {
      this.registrationTime = registrationTime;
   }

   public Date getConfirmationTime() {
      return this.confirmationTime;
   }

   public void setConfirmationTime(Date confirmationTime) {
      this.confirmationTime = confirmationTime;
   }

   public Date getLastVisitTime() {
      return this.lastVisitTime;
   }

   public void setLastVisitTime(Date lastVisitTime) {
      this.lastVisitTime = lastVisitTime;
   }

   public String getFirstName() {
      return this.firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return this.lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getCompanyName() {
      return this.companyName;
   }

   public void setCompanyName(String companyName) {
      this.companyName = companyName;
   }

   public LMAddress getAddress() {
      return this.address;
   }

   public void setAddress(LMAddress address) {
      this.address = address;
   }
}
