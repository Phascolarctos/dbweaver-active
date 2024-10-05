package top.monkeyfans.active;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {
   public static Document parseDocument(String fileName) throws XMLException {
      return parseDocument(new File(fileName));
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static Document parseDocument(File file) throws XMLException {
      try {
         Throwable e = null;
         Object var2 = null;

         try {
            InputStream is = new FileInputStream(file);

            Document var10000;
            try {
               var10000 = parseDocument(new InputSource(is));
            } finally {
               if (is != null) {
                  is.close();
               }
            }

            return var10000;
         } catch (Throwable var11) {
            if (e == null) {
               e = var11;
            } else if (e != var11) {
               e.addSuppressed(var11);
            }

            throw e;
         }
      } catch (Throwable var12) {
         throw new XMLException("Error opening file '" + file + "'", var12);
      }
   }

   public static Document parseDocument(InputStream is) throws XMLException {
      return parseDocument(new InputSource(is));
   }

   public static Document parseDocument(Reader is) throws XMLException {
      return parseDocument(new InputSource(is));
   }

   public static Document parseDocument(InputSource source) throws XMLException {
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
         dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         DocumentBuilder xmlBuilder = dbf.newDocumentBuilder();
         return xmlBuilder.parse(source);
      } catch (Exception var3) {
         throw new XMLException("Error parsing XML document", var3);
      }
   }

   public static Document createDocument() throws XMLException {
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder xmlBuilder = dbf.newDocumentBuilder();
         return xmlBuilder.newDocument();
      } catch (Exception var2) {
         throw new XMLException("Error creating XML document", var2);
      }
   }

   public static Element getChildElement(Element element,  String childName) {
      if (element == null) {
         return null;
      } else {
         for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1 && ((Element)node).getTagName().equals(childName)) {
               return (Element)node;
            }
         }

         return null;
      }
   }

   
   public static String getChildElementBody(Element element,  String childName) {
      if (element == null) {
         return null;
      } else {
         for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1 && ((Element)node).getTagName().equals(childName)) {
               return getElementBody((Element)node);
            }
         }

         return null;
      }
   }

   
   public static String getElementBody( Element element) {
      return element.getTextContent();
   }

   
   public static List<Element> getChildElementList(Element parent, String nodeName) {
      List<Element> list = new ArrayList<>();
      if (parent != null) {
         for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1 && nodeName.equals(node.getNodeName())) {
               list.add((Element)node);
            }
         }
      }

      return list;
   }

   
   public static Collection<Element> getChildElementListNS(Element parent, String nsURI) {
      List<Element> list = new ArrayList<>();
      if (parent != null) {
         for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1 && node.getNamespaceURI().equals(nsURI)) {
               list.add((Element)node);
            }
         }
      }

      return list;
   }

   public static Collection<Element> getChildElementListNS(Element parent, String nodeName, String nsURI) {
      List<Element> list = new ArrayList<>();

      for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
         if (node.getNodeType() == 1 && node.getLocalName().equals(nodeName) && node.getNamespaceURI().equals(nsURI)) {
            list.add((Element)node);
         }
      }

      return list;
   }

   
   public static Collection<Element> getChildElementList(Element parent, String[] nodeNameList) {
      List<Element> list = new ArrayList<>();

      for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
         if (node.getNodeType() == 1) {
            for (String s : nodeNameList) {
               if (node.getNodeName().equals(s)) {
                  list.add((Element)node);
               }
            }
         }
      }

      return list;
   }

   
   public static Element findChildElement(Element parent) {
      for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
         if (node.getNodeType() == 1) {
            return (Element)node;
         }
      }

      return null;
   }

   public static Object escapeXml(Object obj) {
      if (obj == null) {
         return null;
      } else {
         return obj instanceof CharSequence ? escapeXml((CharSequence)obj) : obj;
      }
   }

   public static String escapeXml(CharSequence str) {
      if (str == null) {
         return null;
      } else {
         StringBuilder res = null;
         int strLength = str.length();

         for (int i = 0; i < strLength; i++) {
            char c = str.charAt(i);
            String repl = encodeXMLChar(c);
            if (repl == null) {
               if (res != null) {
                  res.append(c);
               }
            } else {
               if (res == null) {
                  res = new StringBuilder(str.length() + 5);

                  for (int k = 0; k < i; k++) {
                     res.append(str.charAt(k));
                  }
               }

               res.append(repl);
            }
         }

         return res == null ? str.toString() : res.toString();
      }
   }

   public static boolean isValidXMLChar(char c) {
      return c >= ' ' || c == '\n' || c == '\r' || c == '\t';
   }

   public static String encodeXMLChar(char ch) {
      return switch (ch) {
         case '"' -> "&quot;";
         case '&' -> "&amp;";
         case '\'' -> "&#39;";
         case '<' -> "&lt;";
         case '>' -> "&gt;";
         default -> null;
      };
   }

   public static XMLException adaptSAXException(Exception toCatch) {
      if (toCatch instanceof XMLException) {
         return (XMLException)toCatch;
      } else if (toCatch instanceof SAXException) {
         String message = toCatch.getMessage();
         Exception embedded = ((SAXException)toCatch).getException();
         return embedded != null && embedded.getMessage() != null && embedded.getMessage().equals(message)
            ? adaptSAXException(embedded)
            : new XMLException(message, embedded != null ? adaptSAXException(embedded) : null);
      } else {
         return new XMLException(toCatch.getMessage(), toCatch);
      }
   }

   public static Collection<Element> getChildElementList(Element element) {
      List<Element> children = new ArrayList<>();
      if (element != null) {
         for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == 1) {
               children.add((Element)node);
            }
         }
      }

      return children;
   }
}
