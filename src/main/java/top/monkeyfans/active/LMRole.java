package top.monkeyfans.active;

public enum LMRole {
   ADMINISTRATOR(0),
   DEVELOPER(1),
   MANAGER(2),
   EDITOR(3),
   VIEWER(4);

   private final int rolePriority;

   private LMRole(int rolePriority) {
      this.rolePriority = rolePriority;
   }

   public static LMRole getDefaultRole() {
      return DEVELOPER;
   }

   public int getRolePriority() {
      return this.rolePriority;
   }
}
