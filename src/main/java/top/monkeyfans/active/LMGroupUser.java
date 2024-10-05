package top.monkeyfans.active;

public class LMGroupUser {
   
   private final String email;
   
   private final String userName;
  
   private final LMRole licenseRole;

   public LMGroupUser( String email,  String userName, LMRole licenseRole) {
      this.email = email;
      this.userName = userName;
      this.licenseRole = licenseRole;
   }

   
   public String getEmail() {
      return this.email;
   }

   
   public String getUserName() {
      return this.userName;
   }

  
   public LMRole getLicenseRole() {
      return this.licenseRole;
   }
}
