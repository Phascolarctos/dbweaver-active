package top.monkeyfans.active;

import java.nio.ByteBuffer;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

public class LMSubscription implements LMSerializable {
   private static final Logger log = Logger.getLogger("LMSubscription");
   private LMSubscriptionFormat format;
   private String licenseId;
   private LMSubscriptionPeriod period;
   private int periodDays;
   private Date lastRenewDate;
   private Date expirationDate;
   private Date activationDate;
   private Date deactivationDate;
   private int totalRenewCount;
   private byte[] encoded;
   private boolean active;

   public LMSubscription(String licenseId, LMSubscriptionPeriod period, Date lastRenewDate, Date expirationDate, int totalRenewCount, boolean active) {
      this.format = LMSubscriptionFormat.STANDARD;
      this.licenseId = licenseId;
      this.period = period;
      this.lastRenewDate = lastRenewDate;
      this.expirationDate = expirationDate;
      this.totalRenewCount = totalRenewCount;
      this.active = active;
   }

   public LMSubscription(byte[] encryptedData, Key key) throws LMException {
      this.encoded = encryptedData;

      ByteBuffer buffer;
      try {
         buffer = ByteBuffer.wrap(LMEncryption.decrypt(encryptedData, key));
      } catch (LMException var6) {
         throw new LMException("Corrupted subscription text:\n" + var6.getMessage());
      }

      try {
         this.format = LMSubscriptionFormat.valueOf(buffer.get());
      } catch (Exception var5) {
         log.warning("Unsupported subscription format: " + buffer.get(0));
         this.format = LMSubscriptionFormat.STANDARD;
      }

      if (buffer.capacity() != this.format.getEncryptedLength()) {
         throw new LMException("Bad " + this.format + " subscription length (" + buffer.capacity() + ")");
      } else {
         this.licenseId = LMUtils.getStringFromBuffer(buffer, 16);
         this.period = LMSubscriptionPeriod.getById((char)buffer.get());
         this.periodDays = buffer.getInt();
         this.lastRenewDate = LMUtils.getDateFromBuffer(buffer);
         this.expirationDate = LMUtils.getDateFromBuffer(buffer);
         this.activationDate = LMUtils.getDateFromBuffer(buffer);
         this.deactivationDate = LMUtils.getDateFromBuffer(buffer);
         this.totalRenewCount = buffer.getInt();
         this.active = buffer.get() != 0;
      }
   }

   public byte[] getEncoded() {
      return this.encoded;
   }

   public String getLicenseId() {
      return this.licenseId;
   }

   public void setLicenseId(String licenseId) {
      this.licenseId = licenseId;
   }

   public LMSubscriptionPeriod getPeriod() {
      return this.period;
   }

   public void setPeriod(LMSubscriptionPeriod period) {
      this.period = period;
   }

   public int getPeriodDays() {
      return this.periodDays;
   }

   public void setPeriodDays(int periodDays) {
      this.periodDays = periodDays;
   }

   public Date getLastRenewDate() {
      return this.lastRenewDate;
   }

   public void setLastRenewDate(Date lastRenewDate) {
      this.lastRenewDate = lastRenewDate;
   }

   public Date getExpirationDate() {
      return this.expirationDate;
   }

   public void setExpirationDate(Date expirationDate) {
      this.expirationDate = expirationDate;
   }

   public Date getActivationDate() {
      return this.activationDate;
   }

   public void setActivationDate(Date activationDate) {
      this.activationDate = activationDate;
   }

   public Date getDeactivationDate() {
      return this.deactivationDate;
   }

   public void setDeactivationDate(Date deactivationDate) {
      this.deactivationDate = deactivationDate;
   }

   public int getTotalRenewCount() {
      return this.totalRenewCount;
   }

   public void setTotalRenewCount(int totalRenewCount) {
      this.totalRenewCount = totalRenewCount;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   @Override
   public LMSerializeFormat getFormat() {
      return this.format;
   }

   @Override
   public byte[] getData() {
      byte[] data = new byte[this.format.getEncryptedLength()];
      ByteBuffer buffer = ByteBuffer.wrap(data);
      buffer.put(this.format.getId());
      LMUtils.putStringToBuffer(buffer, this.licenseId, 16);
      buffer.put((byte)this.period.getId());
      buffer.putInt(this.periodDays);
      LMUtils.putDateToBuffer(buffer, this.lastRenewDate);
      LMUtils.putDateToBuffer(buffer, this.expirationDate);
      LMUtils.putDateToBuffer(buffer, this.activationDate);
      LMUtils.putDateToBuffer(buffer, this.deactivationDate);
      buffer.putInt(this.totalRenewCount);
      buffer.put((byte)(this.active ? 1 : 0));
      return data;
   }

   public boolean isExpired() {
      LocalDateTime curDate = LocalDateTime.now();
      LocalDateTime expDate = this.expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      return curDate.isAfter(expDate);
   }
}
