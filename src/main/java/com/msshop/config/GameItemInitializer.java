package com.msshop.config;

import com.msshop.domain.GameItem;
import com.msshop.repository.GameItemRepository;
import com.msshop.service.WzStringParserService;
import com.msshop.service.WzStringParserService.EqpItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Populates the {@code game_item} catalog table from String.wz XML files
 * on every clean application start (skipped when the table already has data).
 *
 * <p>Supported XML files and their WZ category labels:
 * <ul>
 *   <li>Eqp.img.xml     → "Eqp"     (equipment: cap, coat, weapon, …)</li>
 *   <li>Consume.img.xml → "Consume" (potions, scrolls, …)</li>
 *   <li>Etc.img.xml     → "Etc"     (materials, monster drops, …)</li>
 *   <li>Cash.img.xml    → "Cash"    (cash-shop cosmetics)</li>
 *   <li>Ins.img.xml     → "Ins"     (install / furniture)</li>
 *   <li>Pet.img.xml     → "Pet"     (pets)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class GameItemInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GameItemInitializer.class);
    private static final int BATCH_SIZE = 500;

    /** XML filename → wzCategory label, in parse order. */
    private static final Map<String, String> XML_FILES = Map.of(
            "Eqp.img.xml",     "Eqp",
            "Consume.img.xml", "Consume",
            "Etc.img.xml",     "Etc",
            "Cash.img.xml",    "Cash",
            "Ins.img.xml",     "Ins",
            "Pet.img.xml",     "Pet"
    );

    private final GameItemRepository gameItemRepository;
    private final WzStringParserService parser;

    @PersistenceContext
    private EntityManager em;

    @Value("${app.wz.string-path}")
    private String wzStringPath;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long existing = gameItemRepository.count();
        if (existing > 0) {
            log.info("[WZ] GameItem catalog already loaded ({} entries) – skipping parse.", existing);
            return;
        }

        Path basePath = Path.of(wzStringPath);
        if (!Files.isDirectory(basePath)) {
            log.warn("[WZ] String.wz directory not found at '{}'. " +
                     "Item catalog will be empty. " +
                     "Set app.wz.string-path in application.yml to the correct path.", basePath.toAbsolutePath());
            return;
        }

        // Pre-load xlsx sub-category mappings (itemId → subCategory)
        Map<Integer, String> consumeSubCat = loadXlsxSubCat(basePath, "consume-subcat.xlsx");
        Map<Integer, String> etcSubCat     = loadXlsxSubCat(basePath, "etc-subcat.xlsx");

        int total = 0;
        for (Map.Entry<String, String> entry : XML_FILES.entrySet()) {
            String filename   = entry.getKey();
            String wzCategory = entry.getValue();
            Path xmlFile = basePath.resolve(filename);

            if (!Files.exists(xmlFile)) {
                log.warn("[WZ] {} not found, skipping.", xmlFile);
                continue;
            }

            try {
                int count = 0;
                if ("Eqp.img.xml".equals(filename)) {
                    // Use specialised parser that also derives equip type/sub-type
                    Map<Integer, EqpItem> parsed = parser.parseEqpFile(xmlFile);
                    for (EqpItem eqp : parsed.values()) {
                        em.persist(new GameItem(eqp.itemId(), eqp.name(), wzCategory,
                                                eqp.equipType(), eqp.equipSubType()));
                        count++;
                        if (count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
                    }
                } else {
                    Map<Integer, String> subCatMap = "Consume.img.xml".equals(filename) ? consumeSubCat
                                                   : "Etc.img.xml".equals(filename)     ? etcSubCat
                                                   : Map.of();
                    Map<Integer, String> parsed = parser.parseFile(xmlFile, wzCategory);
                    for (Map.Entry<Integer, String> item : parsed.entrySet()) {
                        String subCat = subCatMap.get(item.getKey());
                        GameItem gi = subCat != null
                            ? new GameItem(item.getKey(), item.getValue(), wzCategory, subCat, true)
                            : new GameItem(item.getKey(), item.getValue(), wzCategory);
                        em.persist(gi);
                        count++;
                        if (count % BATCH_SIZE == 0) { em.flush(); em.clear(); }
                    }
                }
                em.flush();
                em.clear();
                log.info("[WZ] {} → {} items loaded.", filename, count);
                total += count;
            } catch (Exception e) {
                log.error("[WZ] Failed to parse {}: {}", filename, e.getMessage(), e);
            }
        }

        log.info("[WZ] GameItem catalog ready: {} items total.", total);
    }

    private Map<Integer, String> loadXlsxSubCat(Path basePath, String filename) {
        Path xlsxFile = basePath.resolve(filename);
        if (!Files.exists(xlsxFile)) {
            log.warn("[WZ] Sub-category file {} not found, skipping.", filename);
            return Map.of();
        }
        try {
            Map<Integer, String> map = parser.parseXlsxSubCategories(xlsxFile);
            log.info("[WZ] {} → {} sub-category mappings loaded.", filename, map.size());
            return map;
        } catch (Exception e) {
            log.error("[WZ] Failed to parse {}: {}", filename, e.getMessage(), e);
            return Map.of();
        }
    }
}
