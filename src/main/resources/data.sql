-- H2 sample data for development (loaded via data.sql with JPA ddl-auto=create-drop)
-- Schema is auto-created by Hibernate from entity; this file only inserts rows.

-- EQUIPMENT items (IN_STOCK)
INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002357, '黑色勇士戒指', 'EQUIPMENT', NULL, 15, 0,  0,  0, 12, 0, 5, 'MESO', 5000000,  1, 'CH1 FM03', '楓葉戰士', 'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002800, '精靈戒指',   'EQUIPMENT', NULL,  0, 10,  8, 12,  0,15, 3, 'CS',   2,        1, 'CH2 FM01', '精靈射手',   'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002100, '暗影手套',   'EQUIPMENT', NULL,  5,  5,  0,  0, 18, 0, 7, 'MESO', 3000000, 1, 'CH1 FM05', '黑色教主',   'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002500, '神秘長袍',   'EQUIPMENT', NULL,  0,  0, 25,  5,  0,20, 4, 'WS',   3,        1, 'CH3 FM07', '大法師',     'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002350, '新手戒指',   'EQUIPMENT', NULL,  2,  2,  2,  2,  3, 3, 2, 'MESO', 500000,  1, 'CH1 FM03', '楓葉戰士',   'IN_STOCK', false, NOW(), NOW());

-- CONSUMABLE items (IN_STOCK)
INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (2040006, '裝備用力量卷軸 60%', 'CONSUMABLE', 'scroll', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'MESO', 800000, 5, 'CH1 FM03', '楓葉戰士', 'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (4000021, '礦石碎片', 'CONSUMABLE', 'material', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'MESO', 10000, 20, 'CH2 FM01', '精靈射手', 'IN_STOCK', false, NOW(), NOW());

INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (4000022, '玄武岩', 'CONSUMABLE', 'ore', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'CS', 1, 10, 'CH2 FM01', '精靈射手', 'IN_STOCK', false, NOW(), NOW());

-- OTHER item (IN_STOCK)
INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (3010001, '聖誕帽', 'DECORATIVE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'WS', 1, 1, 'CH3 FM07', '大法師', 'IN_STOCK', false, NOW(), NOW());

-- SOLD_OUT item (should not appear in buyer search)
INSERT INTO item (item_id, name, category, sub_category, str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, price_type, price_value, quantity, location, seller_name, status, featured, created_at, updated_at)
VALUES (1002358, '已售出耳環', 'EQUIPMENT', NULL, 8, 3, 0, 0, 5, 0, 3, 'MESO', 2000000, 1, 'CH1 FM03', '楓葉戰士', 'SOLD_OUT', false, NOW(), NOW());
