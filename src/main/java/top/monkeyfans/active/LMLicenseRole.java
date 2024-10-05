package top.monkeyfans.active;

public class LMLicenseRole {
   private LMRole role;
   private int usersNumber;

   public LMLicenseRole(LMRole role, int usersNumber) {
      this.role = role;
      this.usersNumber = usersNumber;
   }

   public LMRole getRole() {
      return this.role;
   }

   public void setRole(LMRole role) {
      this.role = role;
   }

   public int getUsersNumber() {
      return this.usersNumber;
   }

   public void setUsersNumber(int usersNumber) {
      this.usersNumber = usersNumber;
   }
}
