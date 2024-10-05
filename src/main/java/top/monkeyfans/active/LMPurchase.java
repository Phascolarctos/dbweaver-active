package top.monkeyfans.active;

import java.util.Date;
import java.util.logging.Logger;

public class LMPurchase {
   private static final Logger log = Logger.getLogger("LMPurchase");
   private long purchaseId;
   private LMCustomer customer;
   private LMReseller reseller;
   private LMPaymentSystem paymentSystem;
   private String extOrderId;
   private Date purchaseTime;
   private int quantity;
   private float basePrice;
   private float totalPrice;
   private String description;

   public long getPurchaseId() {
      return this.purchaseId;
   }

   public void setPurchaseId(long purchaseId) {
      this.purchaseId = purchaseId;
   }

   public LMCustomer getCustomer() {
      return this.customer;
   }

   public void setCustomer(LMCustomer customer) {
      this.customer = customer;
   }

   public LMReseller getReseller() {
      return this.reseller;
   }

   public void setReseller(LMReseller reseller) {
      this.reseller = reseller;
   }

   public LMPaymentSystem getPaymentSystem() {
      return this.paymentSystem;
   }

   public void setPaymentSystem(LMPaymentSystem paymentSystem) {
      this.paymentSystem = paymentSystem;
   }

   public String getExtOrderId() {
      return this.extOrderId;
   }

   public void setExtOrderId(String extOrderId) {
      this.extOrderId = extOrderId;
   }

   public Date getPurchaseTime() {
      return this.purchaseTime;
   }

   public void setPurchaseTime(Date purchaseTime) {
      this.purchaseTime = purchaseTime;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public void setQuantity(int quantity) {
      this.quantity = quantity;
   }

   public float getBasePrice() {
      return this.basePrice;
   }

   public void setBasePrice(float basePrice) {
      this.basePrice = basePrice;
   }

   public float getTotalPrice() {
      return this.totalPrice;
   }

   public void setTotalPrice(float totalPrice) {
      this.totalPrice = totalPrice;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }
}
