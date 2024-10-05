package top.monkeyfans.active;

public enum LMLicenseType {
   STANDARD('S', "Yearly subscription", true, true),
   YEAR_UPDATE('Y', "Perpetual", false, false),
   YEAR_CORPORATE('C', "Corporate", false, false),
   ULTIMATE('U', "Ultimate", false, false),
   LIMITED('L', "Limited", true, true),
   PARTNER('P', "Technical partner", false, false),
   TRIAL('T', "Trial", true, true),
   EAP('E', "Early Access Program", false, false),
   ACADEMIC('A', "Academic", true, true),
   TEAM('M', "Yearly subscription (Team)", true, true),
   CUSTOM('X', "Custom", false, false);

   private final char id;
   private final String displayName;
   private final boolean isExtendable;
   private final boolean needsEndTime;

   private LMLicenseType(char id, String displayName, boolean isExtendable, boolean needsEndTime) {
      this.id = id;
      this.displayName = displayName;
      this.isExtendable = isExtendable;
      this.needsEndTime = needsEndTime;
   }

   public byte getId() {
      return (byte)this.id;
   }

   public static LMLicenseType valueOf(byte id) {
      LMLicenseType[] var4;
      for (LMLicenseType format : var4 = values()) {
         if (format.id == id) {
            return format;
         }
      }

      throw new IllegalArgumentException(String.valueOf((int)id));
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isExtendable() {
      return this.isExtendable;
   }

   public boolean needsEndTime() {
      return this.needsEndTime;
   }
}
