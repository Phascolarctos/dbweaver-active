package top.monkeyfans.active;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLBuilder {
   public static final String XMLNS = "xmlns";
   public static final String PREFIX_XML = "xml";
   public static final String NS_XML = "http://www.w3.org/TR/REC-xml";
   private static final int STATE_NOTHING = 0;
   private static final int STATE_ELEM_OPENED = 1;
   private static final int STATE_TEXT_ADDED = 2;
   private static final int IO_BUFFER_SIZE = 8192;
   private Writer writer;
   private int state = 0;
   private Element element = null;
   private boolean butify = false;
   private final List<Element> trashElements = new ArrayList<>();

   public static String XML_HEADER(String encoding) {
      return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
   }

   public static String XML_HEADER() {
      return "<?xml version=\"1.0\"?>";
   }

   public XMLBuilder(OutputStream stream, String documentEncoding) throws IOException {
      this(stream, documentEncoding, true);
   }

   public XMLBuilder(OutputStream stream, String documentEncoding, boolean printHeader) throws IOException {
      if (documentEncoding == null) {
         this.init(new OutputStreamWriter(stream), null, printHeader);
      } else {
         this.init(new OutputStreamWriter(stream, documentEncoding), documentEncoding, printHeader);
      }
   }

   public XMLBuilder(Writer writer, String documentEncoding) throws IOException {
      this(writer, documentEncoding, true);
   }

   public XMLBuilder(Writer writer, String documentEncoding, boolean printHeader) throws IOException {
      this.init(writer, documentEncoding, printHeader);
   }

   private Element createElement(Element parent, String name) {
      if (this.trashElements.isEmpty()) {
         return new Element(parent, name);
      } else {
         Element element = this.trashElements.remove(this.trashElements.size() - 1);
         element.init(parent, name);
         return element;
      }
   }

   private void deleteElement(Element element) {
      this.trashElements.add(element);
   }

   private void init(Writer writer, String documentEncoding, boolean printHeader) throws IOException {
      this.writer = new BufferedWriter(writer, 8192);
      if (printHeader) {
         if (documentEncoding != null) {
            this.writer.write(XML_HEADER(documentEncoding));
         } else {
            this.writer.write(XML_HEADER());
         }
      }
   }

   public boolean isButify() {
      return this.butify;
   }

   public void setButify(boolean butify) {
      this.butify = butify;
   }

   public Element startElement(String elementName) throws IOException {
      return this.startElement(null, null, elementName);
   }

   public Element startElement(String nsURI, String elementName) throws IOException {
      return this.startElement(nsURI, null, elementName);
   }

   public Element startElement(String nsURI, String nsPrefix, String elementName) throws IOException {
      switch (this.state) {
         case 1:
            this.writer.write(62);
         case 0:
            if (this.butify) {
               this.writer.write(10);
            }
      }

      if (this.butify && this.element != null) {
         for (int i = 0; i <= this.element.getLevel(); i++) {
            this.writer.write(9);
         }
      }

      this.writer.write(60);
      boolean addNamespace = nsURI != null;
      if (nsURI != null && nsPrefix == null && this.element != null) {
         nsPrefix = this.element.getNamespacePrefix(nsURI);
         if (nsPrefix != null) {
            addNamespace = false;
         }
      }

      if (nsPrefix != null) {
         elementName = nsPrefix + ":" + elementName;
      }

      this.writer.write(elementName);
      this.state = 1;
      this.element = this.createElement(this.element, elementName);
      if (addNamespace) {
         this.addNamespace(nsURI, nsPrefix);
         this.element.addNamespace(nsURI, nsPrefix);
      }

      return this.element;
   }

   public XMLBuilder endElement() throws IOException, IllegalStateException {
      if (this.element == null) {
         throw new IllegalStateException("Close tag without open");
      } else {
         switch (this.state) {
            case 0:
               if (this.butify) {
                  this.writer.write(10);

                  for (int i = 0; i < this.element.getLevel(); i++) {
                     this.writer.write(9);
                  }
               }
            case 2:
               this.writer.write("</");
               this.writer.write(this.element.getName());
               this.writer.write(62);
               break;
            case 1:
               this.writer.write("/>");
         }

         this.deleteElement(this.element);
         this.element = this.element.parent;
         this.state = 0;
         return this;
      }
   }

   public XMLBuilder addNamespace(String nsURI, String nsPrefix) throws IOException, IllegalStateException {
      if (this.element == null) {
         throw new IllegalStateException("Namespace outside of element");
      } else {
         String attrName = "xmlns";
         if (nsPrefix != null) {
            attrName = attrName + ":" + nsPrefix;
            this.element.addNamespace(nsURI, nsPrefix);
         }

         this.addAttribute(null, attrName, nsURI, true);
         return this;
      }
   }

   public XMLBuilder addAttribute(String attributeName, String attributeValue) throws IOException {
      return this.addAttribute(null, attributeName, attributeValue, true);
   }

   private XMLBuilder addAttribute(String nsURI, String attributeName, String attributeValue, boolean escape) throws IOException, IllegalStateException {
      switch (this.state) {
         case 0:
         case 2:
            throw new IllegalStateException("Attribute ouside of element");
         case 1:
            if (nsURI != null) {
               String nsPrefix = this.element.getNamespacePrefix(nsURI);
               if (nsPrefix == null) {
                  throw new IllegalStateException(
                     "Unknown attribute '" + attributeName + "' namespace URI '" + nsURI + "' in element '" + this.element.getName() + "'"
                  );
               }

               attributeName = nsPrefix + ":" + attributeName;
            }

            this.writer.write(32);
            this.writer.write(attributeName);
            this.writer.write("=\"");
            this.writer.write(escape ? XMLUtils.escapeXml((CharSequence)attributeValue) : attributeValue);
            this.writer.write(34);
         default:
            return this;
      }
   }

   public XMLBuilder addText(CharSequence textValue) throws IOException {
      return this.addText(textValue, true);
   }

   public XMLBuilder addText(CharSequence textValue, boolean escape) throws IOException {
      switch (this.state) {
         case 1:
            this.writer.write(62);
         case 0:
         case 2:
         default:
            this.writeText(textValue, escape);
            this.state = 2;
            return this;
      }
   }

   public XMLBuilder addText(Reader reader) throws IOException {
      switch (this.state) {
         case 1:
            this.writer.write(62);
         case 0:
         case 2:
         default:
            this.writer.write("<![CDATA[");
            char[] writeBuffer = new char[8192];

            for (int br = reader.read(writeBuffer); br != -1; br = reader.read(writeBuffer)) {
               this.writer.write(new String(writeBuffer, 0, br));
            }

            this.writer.write("]]>");
            this.state = 2;
            return this;
      }
   }

   public XMLBuilder flush() throws IOException {
      this.writer.flush();
      return this;
   }

   private XMLBuilder writeText(CharSequence textValue, boolean escape) throws IOException {
      if (textValue != null) {
         this.writer.write(escape ? XMLUtils.escapeXml(textValue) : textValue.toString());
      }

      return this;
   }

   public final class Element implements AutoCloseable {
      private Element parent;
      private String name;
      private Map<String, String> nsStack = null;
      private int level;

      Element(Element parent, String name) {
         this.init(parent, name);
      }

      void init(Element parent, String name) {
         this.parent = parent;
         this.name = name;
         this.nsStack = null;
         this.level = parent == null ? 0 : parent.level + 1;
      }

      public String getName() {
         return this.name;
      }

      public int getLevel() {
         return this.level;
      }

      public void addNamespace(String nsURI, String nsPrefix) {
         if (this.nsStack == null) {
            this.nsStack = new HashMap<>();
         }

         this.nsStack.put(nsURI, nsPrefix);
      }

      public String getNamespacePrefix(String nsURI) {
         if (nsURI.equals("http://www.w3.org/TR/REC-xml")) {
            return "xml";
         } else {
            String prefix = this.nsStack == null ? null : this.nsStack.get(nsURI);
            return prefix != null ? prefix : (this.parent != null ? this.parent.getNamespacePrefix(nsURI) : null);
         }
      }

      @Override
      public void close() throws IOException {
         XMLBuilder.this.endElement();
      }
   }
}
