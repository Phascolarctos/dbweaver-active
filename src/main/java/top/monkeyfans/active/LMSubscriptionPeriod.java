package top.monkeyfans.active;

public enum LMSubscriptionPeriod {
   YEAR('Y'),
   QUARTER('Q'),
   MONTH('M'),
   WEEK('W'),
   DAY('D'),
   CUSTOM('C');

   private final char id;

   private LMSubscriptionPeriod(char id) {
      this.id = id;
   }

   public char getId() {
      return this.id;
   }

   public static LMSubscriptionPeriod getById(char id) {
      LMSubscriptionPeriod[] var4;
      for (LMSubscriptionPeriod period : var4 = values()) {
         if (period.getId() == id) {
            return period;
         }
      }

      throw new IllegalArgumentException("Pad period: " + id);
   }
}
