package top.monkeyfans.active;

import java.util.Date;

public class LMProduct {
   
   private final String id;
   
   private final String prefix;
   
   private final String name;
   
   private final String description;
   
   private final String version;
   
   private final Date releaseDate;
   
   private final String[] umbrellaProducts;
   private final LMProductType type;

   public LMProduct(
       String id,
       String prefix,
       String name,
       String description,
       String version,
       LMProductType type,
       Date releaseDate,
       String[] umbrellaProducts
   ) {
      this.id = id;
      this.prefix = prefix;
      this.name = name;
      this.description = description;
      this.version = version;
      this.type = type;
      this.releaseDate = releaseDate;
      this.umbrellaProducts = umbrellaProducts;
   }

   
   public String getId() {
      return this.id;
   }

   
   public String getPrefix() {
      return this.prefix;
   }

   
   public String getName() {
      return this.name;
   }

   
   public String getDescription() {
      return this.description;
   }

   
   public String getVersion() {
      return this.version;
   }

   public LMProductType getType() {
      return this.type;
   }

   
   public Date getReleaseDate() {
      return this.releaseDate;
   }

   
   public String[] getUmbrellaProducts() {
      return this.umbrellaProducts;
   }
}
