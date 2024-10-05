package top.monkeyfans.active;

import java.security.Key;

public interface LMKeyProvider {
   Key getEncryptionKey(LMProduct var1);

   Key getDecryptionKey(LMProduct var1);
}
