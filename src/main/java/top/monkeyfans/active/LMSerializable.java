package top.monkeyfans.active;

public interface LMSerializable {
   LMSerializeFormat getFormat();

   byte[] getData();
}
