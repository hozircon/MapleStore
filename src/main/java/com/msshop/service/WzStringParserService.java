package com.msshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


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

    // ── Equipment-specific parse (with equip type/sub-type) ───────────────────

    /**
     * Carries the result of parsing a single equipment item from Eqp.img.xml.
     */
    public record EqpItem(Integer itemId, String name, String equipType, String equipSubType) {}

    /**
     * WZ sub-folder name → [equipType, equipSubType] mapping.
     * Weapon sub-type is determined separately by itemId prefix.
     */
    private static final Map<String, String[]> WZ_FOLDER_MAP = Map.ofEntries(
        Map.entry("Cap",        new String[]{"防具", "頭盔"}),
        Map.entry("Face",       new String[]{"防具", "帽子"}),
        Map.entry("Coat",       new String[]{"防具", "上衣"}),
        Map.entry("Longcoat",   new String[]{"防具", "套服"}),
        Map.entry("Pants",      new String[]{"防具", "褲裙"}),
        Map.entry("Glove",      new String[]{"防具", "手套"}),
        Map.entry("Shoes",      new String[]{"防具", "鞋子"}),
        Map.entry("Cape",       new String[]{"防具", "披風"}),
        Map.entry("Accessory",  new String[]{"防具", "飾品"}),
        Map.entry("Ring",       new String[]{"防具", "飾品"}),
        Map.entry("Shield",     new String[]{"武器", "盾牌"}),
        Map.entry("TamingMob",  new String[]{"其他", "坐騎"}),
        Map.entry("PetEquip",   new String[]{"其他", "寵物裝備"})
        // "Weapon" is handled dynamically by itemId prefix
    );

    /** Derive weapon sub-type from the first 4 digits of itemId. */
    static String weaponSubType(int itemId) {
        int prefix = itemId / 1000;
        return switch (prefix) {
            case 1300, 1301, 1302, 1303, 1304, 1305, 1306, 1307, 1308, 1309 -> "單手劍";
            case 1310, 1311, 1312, 1313, 1314, 1315, 1316, 1317, 1318, 1319 -> "單手斧";
            case 1320, 1321, 1322, 1323, 1324, 1325, 1326, 1327, 1328, 1329 -> "單手棍";
            case 1370, 1371, 1372, 1373, 1374, 1375, 1376, 1377, 1378, 1379 -> "短劍";
            case 1400, 1401, 1402, 1403, 1404, 1405, 1406, 1407, 1408, 1409 -> "雙手劍";
            case 1410, 1411, 1412, 1413, 1414, 1415, 1416, 1417, 1418, 1419 -> "雙手斧";
            case 1420, 1421, 1422, 1423, 1424, 1425, 1426, 1427, 1428, 1429 -> "雙手棍";
            case 1430, 1431, 1432, 1433, 1434, 1435, 1436, 1437, 1438, 1439 -> "矛";
            case 1440, 1441, 1442, 1443, 1444, 1445, 1446, 1447, 1448, 1449 -> "槍";
            case 1450, 1451, 1452, 1453, 1454, 1455, 1456, 1457, 1458, 1459 -> "弓";
            case 1460, 1461, 1462, 1463, 1464, 1465, 1466, 1467, 1468, 1469 -> "弩";
            case 1470, 1471, 1472, 1473, 1474, 1475, 1476, 1477, 1478, 1479 -> "火槍";
            case 1480, 1481, 1482, 1483, 1484, 1485, 1486, 1487, 1488, 1489 -> "拳套";
            case 1490, 1491, 1492, 1493, 1494, 1495, 1496, 1497, 1498, 1499 -> "指虎";
            case 1520, 1521, 1522, 1523, 1524, 1525, 1526, 1527, 1528, 1529 -> "短杖";
            case 1530, 1531, 1532, 1533, 1534, 1535, 1536, 1537, 1538, 1539 -> "長杖";
            case 1540, 1541, 1542, 1543, 1544, 1545, 1546, 1547, 1548, 1549 -> "盾牌";
            default -> null;
        };
    }

    /**
     * Parse Eqp.img.xml and return a map of {@code itemId -> EqpItem} with
     * equip type and sub-type derived from the WZ folder structure.
     */
    public Map<Integer, EqpItem> parseEqpFile(Path xmlFile) throws Exception {
        Map<Integer, EqpItem> result = new LinkedHashMap<>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        SAXParser parser = factory.newSAXParser();
        parser.parse(xmlFile.toFile(), new EqpItemHandler(result));

        log.debug("[WZ] Eqp → {} items parsed with equip classification", result.size());
        return result;
    }

    // ── SAX Handler (equipment) ───────────────────────────────────────────────

    private static final class EqpItemHandler extends DefaultHandler {

        private final Map<Integer, EqpItem> result;
        private int depth = 0;

        /** The WZ sub-folder name captured at depth 3 (e.g. "Cap", "Weapon"). */
        private String wzSubFolder = null;
        private int wzSubFolderDepth = -1;

        /** Current item being tracked. */
        private int itemDirDepth = -1;
        private Integer currentItemId = null;

        EqpItemHandler(Map<Integer, EqpItem> result) {
            this.result = result;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attrs) throws SAXException {
            depth++;

            if ("imgdir".equals(qName)) {
                String nameAttr = attrs.getValue("name");

                if (wzSubFolderDepth < 0 && depth == 3) {
                    // Depth 3 is always the sub-folder (Cap / Coat / Weapon …)
                    wzSubFolder = nameAttr;
                    wzSubFolderDepth = depth;
                } else if (itemDirDepth < 0 && isSevenDigit(nameAttr)) {
                    currentItemId = Integer.parseInt(nameAttr);
                    itemDirDepth = depth;
                }

            } else if ("string".equals(qName)
                    && itemDirDepth >= 0
                    && "name".equals(attrs.getValue("name"))) {
                String value = attrs.getValue("value");
                if (value != null && !value.isBlank() && wzSubFolder != null) {
                    result.computeIfAbsent(currentItemId, id -> {
                        String[] typeInfo  = WZ_FOLDER_MAP.get(wzSubFolder);
                        String equipType, equipSubType;
                        if (typeInfo != null) {
                            equipType    = typeInfo[0];
                            equipSubType = typeInfo[1];
                        } else if ("Weapon".equals(wzSubFolder)) {
                            equipType    = "武器";
                            equipSubType = weaponSubType(id);
                        } else {
                            equipType    = null;
                            equipSubType = null;
                        }
                        return new EqpItem(id, value.trim(), equipType, equipSubType);
                    });
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("imgdir".equals(qName)) {
                if (itemDirDepth == depth) {
                    itemDirDepth  = -1;
                    currentItemId = null;
                } else if (wzSubFolderDepth == depth) {
                    wzSubFolder      = null;
                    wzSubFolderDepth = -1;
                }
            }
            depth--;
        }

        private static boolean isSevenDigit(String s) {
            if (s == null || s.length() != 7) return false;
            for (int i = 0; i < 7; i++) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') return false;
            }
            return true;
        }
    }

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

    // ── XLSX sub-category parser ──────────────────────────────────────────────

    /**
     * Reads a sub-category xlsx file (e.g. 消耗分類.xlsx / 其他分類.xlsx) and
     * returns a map of {@code itemId -> subCategory}.
     *
     * <p>Expected column layout (row 1 = header):
     * <pre>A: img name  B: itemId  C: "name"  D: item name  E: 類別 (sub-category)</pre>
     *
     * <p>xlsx files are zip archives containing plain XML — no external library required.
     */
    public Map<Integer, String> parseXlsxSubCategories(Path xlsxFile) throws Exception {
        Map<Integer, String> result = new LinkedHashMap<>();

        try (ZipFile zip = new ZipFile(xlsxFile.toFile())) {
            // 1. Load shared string table
            List<String> sharedStrings = new ArrayList<>();
            ZipEntry ssEntry = zip.getEntry("xl/sharedStrings.xml");
            if (ssEntry != null) {
                try (InputStream is = zip.getInputStream(ssEntry)) {
                    sharedStrings = parseSharedStrings(is);
                }
            }

            // 2. Parse worksheet rows (skip header row 1)
            ZipEntry wsEntry = zip.getEntry("xl/worksheets/sheet1.xml");
            if (wsEntry == null) return result;

            try (InputStream is = zip.getInputStream(wsEntry)) {
                XlsxRowHandler handler = new XlsxRowHandler(sharedStrings, result);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                SAXParser sax = factory.newSAXParser();
                sax.parse(is, handler);
            }
        }

        log.debug("[WZ] {} → {} sub-category mappings loaded", xlsxFile.getFileName(), result.size());
        return result;
    }

    private List<String> parseSharedStrings(InputStream is) throws Exception {
        List<String> strings = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        SAXParser sax = factory.newSAXParser();
        sax.parse(is, new DefaultHandler() {
            private final StringBuilder current = new StringBuilder();
            private boolean inT = false;

            @Override
            public void startElement(String u, String l, String q, Attributes a) {
                if ("si".equals(q)) current.setLength(0);
                inT = "t".equals(q);
            }
            @Override
            public void characters(char[] ch, int start, int len) {
                if (inT) current.append(ch, start, len);
            }
            @Override
            public void endElement(String u, String l, String q) {
                if ("si".equals(q)) strings.add(current.toString());
                if ("t".equals(q)) inT = false;
            }
        });
        return strings;
    }

    /**
     * SAX handler for the worksheet XML.
     * Reads column B (itemId) and the last populated column (subCategory) per row.
     * Skips row 1 (header).
     */
    private static final class XlsxRowHandler extends DefaultHandler {
        private static final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

        private final List<String> ss;
        private final Map<Integer, String> result;

        private int currentRow = 0;
        private String cellRef = "";        // e.g. "B2"
        private String cellType = "";       // "s" = shared string, "" = number
        private final StringBuilder cellValue = new StringBuilder();

        private Integer rowItemId = null;
        private String  rowSubCat = null;

        XlsxRowHandler(List<String> sharedStrings, Map<Integer, String> result) {
            this.ss     = sharedStrings;
            this.result = result;
        }

        @Override
        public void startElement(String uri, String local, String q, Attributes a) {
            if ("row".equals(q)) {
                String rAttr = a.getValue("r");
                currentRow = rAttr != null ? Integer.parseInt(rAttr) : currentRow + 1;
                rowItemId  = null;
                rowSubCat  = null;
            } else if ("c".equals(q)) {
                cellRef   = a.getValue("r");   // e.g. "B3"
                cellType  = a.getValue("t") == null ? "" : a.getValue("t");
                cellValue.setLength(0);
            }
        }

        @Override
        public void characters(char[] ch, int start, int len) {
            cellValue.append(ch, start, len);
        }

        @Override
        public void endElement(String uri, String local, String q) {
            if ("v".equals(q) && cellRef != null && !cellRef.isEmpty()) {
                String col = cellRef.replaceAll("[0-9]", ""); // extract column letter(s)
                String raw = cellValue.toString().trim();
                String resolved = "s".equals(cellType) ? ss.get(Integer.parseInt(raw)) : raw;

                if ("B".equals(col)) {
                    try { rowItemId = (int) Double.parseDouble(resolved); } catch (NumberFormatException ignored) {}
                } else if ("E".equals(col)) {
                    rowSubCat = resolved;
                }
            } else if ("row".equals(q)) {
                // Commit row (skip header row 1)
                if (currentRow > 1 && rowItemId != null && rowSubCat != null && !rowSubCat.isBlank()) {
                    result.putIfAbsent(rowItemId, rowSubCat);
                }
            }
        }
    }
}
