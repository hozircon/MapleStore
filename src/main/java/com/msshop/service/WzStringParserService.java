package com.msshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses MapleStory String.wz XML files (extracted) to build a
 * {@code Map<itemId, itemName>} catalog.
 *
 * <p>XML structure example (Eqp.img.xml):
 * <pre>
 *   &lt;imgdir name="Eqp"&gt;
 *     &lt;imgdir name="Eqp"&gt;
 *       &lt;imgdir name="Cap"&gt;
 *         &lt;imgdir name="1002357"&gt;
 *           &lt;string name="name" value="黑色勇士戒指"/&gt;
 *           &lt;string name="desc" value="..."/&gt;
 *         &lt;/imgdir&gt;
 *       &lt;/imgdir&gt;
 *     &lt;/imgdir&gt;
 *   &lt;/imgdir&gt;
 * </pre>
 * Item IDs are always 7-digit integers. The parser locates every
 * {@code <imgdir>} whose {@code name} is a 7-digit number and captures
 * its first direct {@code <string name="name">} child.
 */
@Service
public class WzStringParserService {

    private static final Logger log = LoggerFactory.getLogger(WzStringParserService.class);

    /**
     * Parse an XML file and return a map of {@code itemId -> itemName}.
     *
     * @param xmlFile path to the *.img.xml file
     * @param wzCategory label used only for logging
     * @return ordered map of itemId to Chinese name (no duplicates; first wins)
     */
    public Map<Integer, String> parseFile(Path xmlFile, String wzCategory) throws Exception {
        Map<Integer, String> result = new LinkedHashMap<>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        // Prevent XXE attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        SAXParser parser = factory.newSAXParser();
        parser.parse(xmlFile.toFile(), new ItemNameHandler(result));

        log.debug("[WZ] {} → {} names parsed", wzCategory, result.size());
        return result;
    }

    // ── SAX Handler ───────────────────────────────────────────────────────────

    private static final class ItemNameHandler extends DefaultHandler {

        private final Map<Integer, String> result;

        /** Nesting depth of the current element (root = 0). */
        private int depth = 0;

        /**
         * Depth at which the innermost 7-digit imgdir was opened.
         * -1 when not inside any item imgdir.
         */
        private int itemDirDepth = -1;

        /**
         * The itemId of the currently tracked item imgdir.
         */
        private Integer currentItemId = null;

        ItemNameHandler(Map<Integer, String> result) {
            this.result = result;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attrs) throws SAXException {
            depth++;

            if ("imgdir".equals(qName)) {
                String nameAttr = attrs.getValue("name");
                if (itemDirDepth < 0 && isSevenDigit(nameAttr)) {
                    // Enter an item-level imgdir
                    currentItemId = Integer.parseInt(nameAttr);
                    itemDirDepth = depth;
                }

            } else if ("string".equals(qName)
                    && itemDirDepth >= 0
                    && "name".equals(attrs.getValue("name"))) {
                // We are directly inside an item imgdir and it is the name string
                String value = attrs.getValue("value");
                if (value != null && !value.isBlank()) {
                    result.putIfAbsent(currentItemId, value.trim());
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("imgdir".equals(qName) && itemDirDepth == depth) {
                // Leaving the item-level imgdir
                itemDirDepth = -1;
                currentItemId = null;
            }
            depth--;
        }

        /** Returns true when s is exactly 7 ASCII digits. */
        private static boolean isSevenDigit(String s) {
            if (s == null || s.length() != 7) return false;
            for (int i = 0; i < 7; i++) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') return false;
            }
            return true;
        }
    }
}
