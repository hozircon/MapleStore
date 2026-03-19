# 資料模型設計：輕量化交易展示站

**功能**: `001-trade-form` | **日期**: 2026-03-18 | **規格文件**: [spec.md](spec.md)

---

## 一、實體概覽

本應用僅有單一核心實體 `Item`，代表管理員發布的一筆交易商品。無買家帳戶實體（本迭代不實作買家登入），管理員亦無獨立實體（本迭代以信任存取處理）。

---

## 二、Entity：`Item`

### 2.1 欄位定義

| 欄位名稱 | Java 類型 | 資料庫類型 | 可為 NULL | 說明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` AUTO_INCREMENT | 否 | 主鍵，系統自動產生 |
| `item_id` | `Integer` | `INT` | 否 | 遊戲道具 ID（對應靜態圖示檔名） |
| `name` | `String` | `VARCHAR(100)` | 否 | 商品顯示名稱；用於關鍵字模糊搜尋 |
| `category` | `Category`（Enum） | `VARCHAR(20)` | 否 | 大類：`EQUIPMENT` / `CONSUMABLE` / `DECORATIVE` / `OTHER` |
| `sub_category` | `String` | `VARCHAR(50)` | 是 | 次分類（非裝備類使用）；例如：`scroll`, `material`, `ore` |
| `str_bonus` | `Integer` | `INT` | 是 | 裝備附加力量（STR），非裝備類填 `null` |
| `dex_bonus` | `Integer` | `INT` | 是 | 裝備附加敏捷（DEX） |
| `int_bonus` | `Integer` | `INT` | 是 | 裝備附加智力（INT） |
| `luk_bonus` | `Integer` | `INT` | 是 | 裝備附加幸運（LUK） |
| `atk_bonus` | `Integer` | `INT` | 是 | 裝備附加攻擊力（ATK） |
| `matk_bonus` | `Integer` | `INT` | 是 | 裝備附加魔法攻擊（MATK） |
| `scroll_slots_remaining` | `Integer` | `INT` | 是 | 裝備剩餘捲軸孔數 |
| `price_type` | `PriceType`（Enum） | `VARCHAR(10)` | 否 | 貨幣類型：`MESO` / `CS` / `WS` |
| `price_value` | `Long` | `BIGINT` | 否 | 標價數值（>= 0） |
| `quantity` | `Integer` | `INT` | 否 | 庫存數量（>= 1），預設 `1` |
| `location` | `String` | `VARCHAR(50)` | 否 | 遊戲內賣家位置（人工輸入，例：`CH1 FM03`） |
| `seller_name` | `String` | `VARCHAR(50)` | 否 | 賣家角色名稱 |
| `status` | `ItemStatus`（Enum） | `VARCHAR(20)` | 否 | 在庫狀態：`IN_STOCK` / `SOLD_OUT`，預設 `IN_STOCK` |
| `featured` | `Boolean` | `TINYINT(1)` | 否 | 主打商品標記（預留，未來功能用），預設 `false` |
| `created_at` | `LocalDateTime` | `DATETIME` | 否 | 建立時間（寫入時自動填入） |
| `updated_at` | `LocalDateTime` | `DATETIME` | 否 | 最後更新時間（更新時自動刷新） |

### 2.2 Enum 定義

**`Category`**

| 值 | 中文說明 |
|---|---|
| `EQUIPMENT` | 裝備 |
| `CONSUMABLE` | 消耗品 |
| `DECORATIVE` | 裝飾 |
| `OTHER` | 其他 |

**`PriceType`**

| 值 | 中文說明 | 排序優先級 |
|---|---|---|
| `MESO` | 楓幣 | 1（最高） |
| `CS` | 混沌卷軸點數（CS） | 2 |
| `WS` | 祝福卷軸點數（WS） | 3 |

**`ItemStatus`**

| 值 | 中文說明 |
|---|---|
| `IN_STOCK` | 在庫（買家可見） |
| `SOLD_OUT` | 已售完（買家不可見） |

### 2.3 JPA Entity 映射說明

```java
@Entity
@Table(name = "item", indexes = {
    @Index(name = "idx_item_status", columnList = "status"),
    @Index(name = "idx_item_category", columnList = "category"),
    @Index(name = "idx_item_price_type_value", columnList = "price_type, price_value"),
    @Index(name = "idx_item_search_composite", columnList = "status, category, price_type, price_value")
})
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer itemId;          // item_id — 對應道具圖示

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(length = 50)
    private String subCategory;      // 非裝備類次分類

    // 裝備數值欄位（裝備類才有意義，其餘為 null）
    private Integer strBonus;
    private Integer dexBonus;
    private Integer intBonus;
    private Integer lukBonus;
    private Integer atkBonus;
    private Integer matkBonus;
    private Integer scrollSlotsRemaining;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PriceType priceType;

    @Column(nullable = false)
    private Long priceValue;

    @Column(nullable = false)
    private Integer quantity;        // 預設 1

    @Column(nullable = false, length = 50)
    private String location;

    @Column(nullable = false, length = 50)
    private String sellerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status;       // 預設 IN_STOCK

    @Column(nullable = false)
    private Boolean featured;        // 預設 false，預留未來功能

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

---

## 三、實體關聯圖（Entity Relationship Diagram）

本迭代僅有單一實體，無跨實體外鍵關聯。圖示展示欄位分組結構：

```
┌──────────────────────────────────────────────────────────────┐
│                          Item                                │
├──────────────────────────────────────────────────────────────┤
│  PK  id                   BIGINT     AUTO_INCREMENT          │
├──────────────────────────────────────────────────────────────┤
│  [商品識別]                                                   │
│      item_id              INT        NOT NULL                │
│      name                 VARCHAR(100) NOT NULL              │
├──────────────────────────────────────────────────────────────┤
│  [分類]                                                       │
│      category             VARCHAR(20) NOT NULL               │
│      sub_category         VARCHAR(50) NULLABLE               │
├──────────────────────────────────────────────────────────────┤
│  [裝備數值 — 僅 EQUIPMENT 有效]                               │
│      str_bonus            INT        NULLABLE                │
│      dex_bonus            INT        NULLABLE                │
│      int_bonus            INT        NULLABLE                │
│      luk_bonus            INT        NULLABLE                │
│      atk_bonus            INT        NULLABLE                │
│      matk_bonus           INT        NULLABLE                │
│      scroll_slots_remaining INT      NULLABLE                │
├──────────────────────────────────────────────────────────────┤
│  [交易資訊]                                                   │
│      price_type           VARCHAR(10) NOT NULL               │
│      price_value          BIGINT     NOT NULL                │
│      quantity             INT        NOT NULL  DEFAULT 1     │
│      location             VARCHAR(50) NOT NULL               │
│      seller_name          VARCHAR(50) NOT NULL               │
├──────────────────────────────────────────────────────────────┤
│  [狀態]                                                       │
│      status               VARCHAR(20) NOT NULL  DEFAULT IN_STOCK │
│      featured             TINYINT(1)  NOT NULL  DEFAULT 0    │
├──────────────────────────────────────────────────────────────┤
│  [稽核欄位]                                                   │
│      created_at           DATETIME   NOT NULL               │
│      updated_at           DATETIME   NOT NULL               │
└──────────────────────────────────────────────────────────────┘
```

---

## 四、資料庫設計說明

### 4.1 索引策略

| 索引名稱 | 索引欄位 | 建立理由 |
|---|---|---|
| `idx_item_status` | `status` | 所有買家搜尋查詢均以 `status = 'IN_STOCK'` 為必要條件篩選，單欄索引提升過濾效率 |
| `idx_item_category` | `category` | 買家搜尋常以大類篩選；Admin 後台亦依分類列表商品 |
| `idx_item_price_type_value` | `price_type, price_value` | 排序查詢以貨幣類型群組後再以價格升序排列，複合索引可直接滿足 ORDER BY 需求 |
| `idx_item_search_composite` | `status, category, price_type, price_value` | 涵蓋最常見的完整查詢路徑（status 過濾 → category 過濾 → 排序），單次掃描完成，減少回表次數 |

> **注意**：`name` 欄位的 `LIKE %keyword%` 查詢因前綴萬用字元無法使用 B-tree 索引。在小規模（< 500 並發、< 10,000 筆資料）情境下，全表掃描仍可在 ≤ 2 秒目標內完成。若資料量成長，可考慮升級為 MySQL FULLTEXT INDEX 並改用 `MATCH(...) AGAINST(?)` 查詢。

### 4.2 扁平化裝備數值 vs. JSON 欄位的決策理由

本設計選擇以 7 個獨立 INT 欄位（`str_bonus`, `dex_bonus`, `int_bonus`, `luk_bonus`, `atk_bonus`, `matk_bonus`, `scroll_slots_remaining`）存放裝備數值，而非使用單一 `stats_json TEXT` 欄位。

**選擇扁平化的理由**：
- 需支援 `>= :threshold` 數值篩選（FR-002、FR-004）：JSON 欄位在 JPA JPQL 層無法直接進行數值比較，需依賴資料庫特定的 JSON_EXTRACT 函式，破壞 H2/MySQL 相容性。
- 可對數值欄位建立資料庫索引，加速篩選效能。
- JPQL 查詢可使用標準 `WHERE item.atkBonus >= :atkMin` 語法，不需原生 SQL。
- 欄位數量有限（7 個），不會導致欄位爆炸問題。

**JSON 欄位的潛在優勢（本設計放棄）**：
- 裝備屬性種類未來可能新增時，無需資料庫 ALTER TABLE。
- 但在當前需求規模下，此優勢不足以補償查詢限制的代價。

### 4.3 驗證規則

| 欄位 | 規則 |
|---|---|
| `name` | 非空白，長度 1–100 字元 |
| `item_id` | 正整數，> 0 |
| `price_value` | 非負整數，>= 0 |
| `quantity` | 正整數，>= 1 |
| `location` | 非空白，長度 1–50 字元（人工輸入，例：`CH1 FM03`） |
| `seller_name` | 非空白，長度 1–50 字元 |
| 裝備數值欄位 | category = EQUIPMENT 時可填寫；category != EQUIPMENT 時應為 `null`（Service 層強制清除） |
| `sub_category` | category = EQUIPMENT 時應為 `null`（Service 層清除）；其他分類可選填 |

---

## 五、H2 與 MySQL 8 相容性說明

| 特性 | H2（開發/測試） | MySQL 8（正式） | 處理方式 |
|---|---|---|---|
| DDL 自動建表 | `spring.jpa.hibernate.ddl-auto=create-drop` | `validate`（正式環境不允許自動 DDL） | 以 `application-dev.yml` 和 `application-prod.yml` 分別設定 |
| Enum 儲存 | `VARCHAR` 相容 | `VARCHAR` 相容 | `@Enumerated(EnumType.STRING)` 兩環境均正常運作 |
| `BOOLEAN`/`TINYINT` | H2 原生支援 `BOOLEAN` | MySQL 以 `TINYINT(1)` 表示 | Hibernate 自動映射，無需特別處理 |
| `DATETIME` 精度 | H2 支援 | MySQL 8 `DATETIME(6)` 支援微秒 | 使用預設 `DATETIME`（秒精度）即可，稽核欄位不需高精度 |
| `LIKE` 查詢 | 相容 | 相容（注意 MySQL 預設大小寫不敏感） | 使用 `LOWER(item.name) LIKE LOWER(CONCAT('%', :keyword, '%'))` 確保兩環境行為一致 |
| `CASE WHEN` 排序 | 相容 | 相容 | JPQL `CASE WHEN` 編譯為標準 SQL，兩環境均支援 |
| Auto-increment | `IDENTITY` | `IDENTITY` | `@GeneratedValue(strategy = GenerationType.IDENTITY)` 兩環境均正常運作 |
| Testcontainers | 不使用 | MySQL 8 映像檔 | 整合測試（`ItemRepositoryIntegrationTest`）以 `@Testcontainers` + `mysql:8.0` 容器執行，確保與正式環境行為一致 |
