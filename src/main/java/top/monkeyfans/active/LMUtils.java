package top.monkeyfans.active;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LMUtils {
   private static final Logger log = Logger.getLogger("LMUtils");
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
   public static final SimpleDateFormat HR_DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd");
   private static SecureRandom RND = new SecureRandom();
   private static short licenseCounter = (short)RND.nextInt(32767);
   protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();

   public static Date dateFromString(String s) {
      if (s != null && !s.isEmpty()) {
         try {
            return DATE_FORMAT.parse(s);
         } catch (ParseException var2) {
            log.log(Level.WARNING, "Error parsing date", (Throwable)var2);
            return null;
         }
      } else {
         return null;
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static Map<String, Map<String, String>> parseConfig(InputStream is) throws Throwable {
      Map<String, Map<String, String>> config = new LinkedHashMap<>();
      Map<String, String> curSection = null;
      Throwable var3 = null;
      Object var4 = null;

      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));

         try {
            while (true) {
               String line = reader.readLine();
               if (line == null) {
                  return config;
               }

               line = line.trim();
               if (!line.startsWith("#") && !line.startsWith(";")) {
                  if (line.startsWith("[") && line.endsWith("]")) {
                     String sectionName = line.substring(1, line.length() - 2);
                     curSection = new LinkedHashMap<>();
                     config.put(sectionName, curSection);
                  } else {
                     if (curSection == null) {
                        curSection = new LinkedHashMap<>();
                        config.put(null, curSection);
                     }

                     int divPos = line.indexOf(61);
                     String propName = divPos == -1 ? line : line.substring(0, divPos);
                     String propValue = divPos == -1 ? null : line.substring(divPos + 1);
                     curSection.put(propName, propValue);
                  }
               }
            }
         } finally {
            if (reader != null) {
               reader.close();
            }
         }
      } catch (Throwable var15) {
         if (var3 == null) {
            var3 = var15;
         } else if (var3 != var15) {
            var3.addSuppressed(var15);
         }

         throw var3;
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static byte[] readEncryptedString(InputStream stream) throws Throwable {
      Throwable var1 = null;
      Object var2 = null;

      try {
         Reader reader = new InputStreamReader(stream);

         byte[] var10000;
         try {
            var10000 = readEncryptedString(reader);
         } finally {
            if (reader != null) {
               reader.close();
            }
         }

         return var10000;
      } catch (Throwable var9) {
         if (var1 == null) {
            var1 = var9;
         } else if (var1 != var9) {
            var1.addSuppressed(var9);
         }

         throw var1;
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static byte[] readEncryptedString(Reader reader) throws Throwable {
      StringBuilder result = new StringBuilder(4000);
      Throwable licenseEncoded = null;
      Object var3 = null;

      try {
         BufferedReader br = new BufferedReader(reader);

         try {
            while (true) {
               String line = br.readLine();
               if (line == null || line.isEmpty()) {
                  break;
               }

               if (!line.startsWith("-") && !line.startsWith("#")) {
                  result.append(line);
               }
            }
         } finally {
            if (br != null) {
               br.close();
            }
         }
      } catch (Throwable var11) {
         if (licenseEncoded == null) {
            licenseEncoded = var11;
         } else if (licenseEncoded != var11) {
            licenseEncoded.addSuppressed(var11);
         }

         throw licenseEncoded;
      }

      String licenseEncodedx = result.toString();
      String var13 = licenseEncodedx.replaceAll("\\s", "");
      return Base64.getDecoder().decode(var13);
   }

   public static String generateLicenseId(LMProduct product) {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.setTime(new Date());
      int dayOfYear = cal.get(6);
      int year = cal.get(1);
      int time = cal.get(11) * 60 + cal.get(12);
      int counter = getLicenseCounter();
      String licenseId = product.getPrefix()
         + "-"
         + getNumString(dayOfYear, 2)
         + getNumString(year, 3)
         + getNumString(time, 3)
         + "-"
         + getNumString(counter, 4);
      if (licenseId.length() > 16) {
         licenseId = licenseId.substring(0, 16);
      }

      return licenseId;
   }

   private static String getNumString(int value, int minDigits) {
      String str = Integer.toString(value, 35).toUpperCase(Locale.ENGLISH);

      while (str.length() < minDigits) {
         str = "Z" + str;
      }

      char[] chars = str.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         if (chars[i] == '0' || chars[i] == '0') {
            chars[i] = 'Z';
         }
      }

      return new String(chars);
   }

   private static synchronized short getLicenseCounter() {
      licenseCounter++;
      return licenseCounter;
   }

   
   public static Date getDateFromBuffer( ByteBuffer buffer) {
      long time = buffer.getLong();
      return time == 0L ? null : new Date(time);
   }

   
   public static String getStringFromBuffer( ByteBuffer buffer, int length) {
      byte[] data = new byte[length];
      buffer.get(data);
      return new String(data, StandardCharsets.UTF_8).trim();
   }

   public static void putDateToBuffer( ByteBuffer buffer,  Date date) {
      buffer.putLong(date == null ? 0L : date.getTime());
   }

   public static void putStringToBuffer( ByteBuffer buffer,  String value, int length) {
      buffer.put(getStringData(value, length));
   }

   private static byte[] getStringData( String value, int length) {
      byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
      byte[] data = Arrays.copyOf(bytes, length);
      Arrays.fill(data, Math.min(bytes.length, length), length, (byte)32);
      return data;
   }

   public static boolean isUnlimitedUsersLicense(LMLicense productLicense) {
      return productLicense.getLicenseType() == LMLicenseType.ULTIMATE || (productLicense.getFlags() & 256L) != 0L;
   }

   public static void writeStringToBuffer( ByteArrayOutputStream outBuffer,  String value, int length) {
      outBuffer.writeBytes(getStringData(value, length));
   }

   public static void writeDateToBuffer( ByteArrayOutputStream outBuffer,  Date date) {
      long value = date == null ? 0L : date.getTime();
      writeLongToBuffer(outBuffer, value);
   }

   public static void writeLongToBuffer( ByteArrayOutputStream outBuffer, long value) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[8]);
      buffer.putLong(value);
      outBuffer.writeBytes(buffer.array());
   }

   public static void writeShortToBuffer( ByteArrayOutputStream outBuffer, short value) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[2]);
      buffer.putShort(value);
      outBuffer.writeBytes(buffer.array());
   }

   public static void writeIntToBuffer( ByteArrayOutputStream outBuffer, int value) {
      ByteBuffer buffer = ByteBuffer.wrap(new byte[4]);
      buffer.putInt(value);
      outBuffer.writeBytes(buffer.array());
   }

   public static boolean isEmpty( CharSequence value) {
      return value == null || value.isEmpty();
   }

   public static boolean isEmpty( Collection<?> value) {
      return value == null || value.isEmpty();
   }

   public static boolean isEmpty( Map<?, ?> value) {
      return value == null || value.isEmpty();
   }

   public static <OBJECT_TYPE> boolean contains(OBJECT_TYPE[] array, OBJECT_TYPE value) {
      if (array != null && array.length != 0) {
         for (OBJECT_TYPE object_type : array) {
            if (Objects.equals(value, object_type)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static String toHexString( byte[] bytes) {
      return bytes == null ? "" : toHexString(bytes, 0, bytes.length);
   }

   
   public static String toHexString( byte[] bytes, int offset, int length) {
      if (bytes != null && bytes.length != 0) {
         char[] hexChars = new char[length * 2];

         for (int i = 0; i < length; i++) {
            int v = bytes[offset + i] & 255;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 15];
         }

         return new String(hexChars);
      } else {
         return "";
      }
   }

   public static boolean toBoolean( Object object) {
      return object != null && Boolean.parseBoolean(object.toString());
   }

   
   public static String toString( Object object) {
      if (object == null) {
         return "";
      } else if (object instanceof String) {
         return (String)object;
      } else {
         String strValue = object.toString();
         return strValue == null ? "" : strValue;
      }
   }

   public static String toString( Object object, String def) {
      if (object == null) {
         return def;
      } else {
         return object instanceof String ? (String)object : object.toString();
      }
   }

   
   public static String truncateString( String str, int maxLength) {
      return str != null && str.length() > maxLength ? str.substring(0, maxLength) : str;
   }

   public static String splitLines(String bigString, int lineLength) {
      return bigString.replaceAll("(.{" + lineLength + "})", "$1\n");
   }

   public static int toInt( Object object, int def) {
      if (object == null) {
         return def;
      } else if (object instanceof Number n) {
         return n.intValue();
      } else {
         try {
            return Integer.parseInt(toString(object));
         } catch (NumberFormatException var5) {
            try {
               return (int)Double.parseDouble(toString(object));
            } catch (NumberFormatException var4) {
               var4.printStackTrace();
               return def;
            }
         }
      }
   }

   public static int toInt( Object object) {
      return toInt(object, 0);
   }

   public static long toLong( Object object) {
      return toLong(object, 0L);
   }

   public static long toLong( Object object, long defValue) {
      if (object == null) {
         return defValue;
      } else if (object instanceof Number n) {
         return n.longValue();
      } else {
         try {
            return Long.parseLong(toString(object));
         } catch (NumberFormatException var5) {
            try {
               return (long)((int)Double.parseDouble(toString(object)));
            } catch (NumberFormatException var4) {
               return defValue;
            }
         }
      }
   }

   
   public static <T extends Enum<T>> T valueOf( Class<T> type,  String name,  T defValue) {
      if (name == null) {
         return defValue;
      } else {
         name = name.trim();
         if (name.length() == 0) {
            return defValue;
         } else {
            try {
               return Enum.valueOf(type, name);
            } catch (Exception var4) {
               var4.printStackTrace();
               return defValue;
            }
         }
      }
   }

   public static boolean isValidLicenseStatus( String licenseStatusText) {
      try {
         BufferedReader br = new BufferedReader(new StringReader(licenseStatusText));
         String licenseStatusString = br.readLine();
         int divPos = licenseStatusString.indexOf(58);
         if (divPos == -1) {
            return false;
         } else {
            String statusCode = licenseStatusString.substring(0, divPos).trim();

            try {
               return LMLicenseStatus.VALID.equals(LMLicenseStatus.valueOf(statusCode));
            } catch (IllegalArgumentException var5) {
               return true;
            }
         }
      } catch (Exception var6) {
         log.log(Level.WARNING, "License status check error", (Throwable)var6);
         return false;
      }
   }
}
