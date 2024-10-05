package top.monkeyfans.active;

public enum LMSubscriptionFormat implements LMSerializeFormat {
   STANDARD((byte)0, 59, "Initial subscription format");

   private final byte id;
   private final int encryptedLength;
   private final String description;

   private LMSubscriptionFormat(byte id, int encryptedLength, String description) {
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

   public static LMSubscriptionFormat valueOf(byte id) {
      LMSubscriptionFormat[] var4;
      for (LMSubscriptionFormat format : var4 = values()) {
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
