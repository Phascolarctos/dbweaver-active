package top.monkeyfans.active;

public enum LMLicenseFormat implements LMSerializeFormat {
   STANDARD((byte)0, 218, "Initial basic license format"),
   EXTENDED((byte)1, 238, "Extended format with owner email and corporate license info"),
   ADVANCED((byte)2, 490, "Advanced format for role-based licenses");

   private final byte id;
   private final int encryptedLength;
   private final String description;

   private LMLicenseFormat(byte id, int encryptedLength, String description) {
      this.id = id;
      this.encryptedLength = encryptedLength;
      this.description = description;
   }

   @Override
   public byte getId() {
      return this.id;
   }

   @Override
   public String getDescription() {
      return this.description;
   }

   public static LMLicenseFormat valueOf(byte id) {
      LMLicenseFormat[] var4;
      for (LMLicenseFormat format : var4 = values()) {
         if (format.id == id) {
            return format;
         }
      }

      throw new IllegalArgumentException(String.valueOf((int)id));
   }

   @Override
   public int getEncryptedLength() {
      return this.encryptedLength;
   }
}
