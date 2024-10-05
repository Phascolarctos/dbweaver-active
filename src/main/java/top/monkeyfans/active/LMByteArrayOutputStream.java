package top.monkeyfans.active;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class LMByteArrayOutputStream extends ByteArrayOutputStream {
   public LMByteArrayOutputStream(int size) {
      super(size);
   }

   byte[] getBuffer() {
      return Arrays.copyOf(this.buf, this.buf.length);
   }
}
