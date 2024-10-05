package top.monkeyfans.active;

public class LMException extends Exception {
   public LMException(String message) {
      super(message);
   }

   public LMException(String message, Throwable cause) {
      super(message, cause);
   }

   public LMException(Throwable cause) {
      super(cause);
   }
}
