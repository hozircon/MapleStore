# 實作計畫：輕量化交易展示站

**分支**: `001-trade-form` | **日期**: 2026-03-18 | **規格文件**: [spec.md](spec.md)

---

## 摘要

本功能實作一套輕量化的交易展示網站，專為楓之谷私服社群設計，取代截圖分享方式。後端採用 Java 21 + Spring Boot 3.x + Spring Data JPA，前端以 Thymeleaf 伺服器端渲染搭配 Bootstrap 5，資料庫開發期使用 H2，正式環境切換至 MySQL 8。核心功能涵蓋買家關鍵字與多條件篩選搜尋（支援裝備數值門檻過濾）、雙版型結果展示（裝備/一般）、複製交易暗號，以及管理員商品狀態維護（個別下架 + 批量清除）。整體採伺服器端渲染單一應用架構，不引入前端框架，以最小技術棧達成所有需求。

---

## 技術背景

| 項目 | 內容 |
|---|---|
| **語言/版本** | Java 21 |
| **主要相依套件** | Spring Boot 3.x, Spring Data JPA, Thymeleaf, Bootstrap 5 |
| **資料庫** | H2（開發/測試）/ MySQL 8（正式環境） |
| **測試框架** | JUnit 5, Mockito, Spring Boot Test, Testcontainers（MySQL 整合測試） |
| **建置工具** | Gradle 8.x（Spring Boot Gradle Plugin） |
| **目標平台** | Web 應用程式（伺服器端渲染，非 SPA） |
| **效能目標** | 搜尋回應時間 ≤ 2 秒；支援小規模社群使用（< 500 並發） |
| **主要限制** | 無即時聊天、無金流、無買家登入、無外部 API 同步 |

---

## 架構核心決策

- **伺服器端渲染（SSR）優先**：以 Thymeleaf 完成所有頁面渲染，不引入 React/Vue，降低維護與部署複雜度；JavaScript 僅用於「複製交易暗號」的 Clipboard API 互動與 Clipboard fallback 顯示。

- **扁平化裝備數值欄位**：`str_bonus`、`dex_bonus`、`int_bonus`、`luk_bonus`、`atk_bonus`、`matk_bonus`、`scroll_slots_remaining` 各為獨立資料庫欄位而非 JSON 欄位，以支援 JPA JPQL/Criteria Query 中的 `>= :threshold` 數值篩選及資料庫層索引。

- **貨幣優先排序於資料庫層**：利用 JPQL `CASE WHEN` 表達式在 SQL 層完成三段式貨幣群組排序（MESO → CS → WS），再以 `price_value ASC` 作為次排序，避免在應用層進行手動分組或二次排序。

- **Enum 欄位以字串儲存**：`category`（EQUIPMENT/CONSUMABLE/DECORATIVE/OTHER）、`price_type`（MESO/CS/WS）、`status`（IN_STOCK/SOLD_OUT）均以 JPA `@Enumerated(EnumType.STRING)` 儲存，提升可讀性並規避序號（ordinal）因 Enum 成員順序異動而導致的資料遷移問題。

- **LIKE 模糊搜尋**：在 < 500 並發且資料量有限的場景下，`name LIKE %keyword%` 搭配複合索引可穩定達成 ≤ 2 秒目標，無需引入全文搜尋引擎（Elasticsearch/Lucene），保持技術棧簡單。

- **無認證管理端（本迭代）**：Admin 路徑（`/admin/**`）於本迭代視為可信任存取，不實作登入流程；但保留 `spring-boot-starter-security` 依賴宣告於 `build.gradle`（已加上 `/* TODO: enable auth */` 注解），以便後續迭代直接啟用，不需大幅重構。

- **靜態遊戲圖示**：道具圖示依 `item_id` 命名（`{item_id}.png`）並存放於 `src/main/resources/static/images/items/`，由瀏覽器直接請求靜態資源路徑，Controller 層不需額外路由處理。

- **雙版型 Thymeleaf Fragment**：以 `th:replace` 在 `index.html` 中依 `item.category == 'EQUIPMENT'` 條件切換 `item-equipment-row.html` 與 `item-general-row.html` 兩個 fragment，實現裝備列（數值展開）與一般商品列（數量＋次分類）的差異化顯示，避免單一模板條件分支過多。

---

## 專案結構

### 規格文件（本功能）

```
specs/
└── 001-trade-form/
    ├── spec.md                     規格文件（需求、情境與驗收條件）
    ├── spec_1st.md                 原始草稿（含實作細節，僅供參考）
    ├── plan.md                     本實作計畫（當前文件）
    ├── data-model.md               資料模型設計
    ├── quickstart.md               開發者快速入門指南
    ├── contracts/
    │   ├── search-api.md           搜尋頁面 Controller 契約
    │   └── admin-api.md            管理端 Controller 契約
    └── checklists/
        └── requirements.md         規格品質查核清單
```

### 原始碼（儲存庫根目錄）

```
ms-web/
├── build.gradle                    Gradle 建置腳本（Spring Boot dependencies）
├── settings.gradle                 專案名稱設定
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/msshop/
│   │   │       ├── MsWebApplication.java           Spring Boot 啟動入口
│   │   │       ├── controller/
│   │   │       │   ├── SearchController.java        買家搜尋頁面（GET /）
│   │   │       │   └── AdminController.java         管理後台（POST/PUT/DELETE /admin/**）
│   │   │       ├── domain/
│   │   │       │   ├── Item.java                   JPA Entity（主實體）
│   │   │       │   ├── Category.java               Enum：EQUIPMENT/CONSUMABLE/DECORATIVE/OTHER
│   │   │       │   ├── PriceType.java              Enum：MESO/CS/WS
│   │   │       │   └── ItemStatus.java             Enum：IN_STOCK/SOLD_OUT
│   │   │       ├── repository/
│   │   │       │   └── ItemRepository.java         JPA Repository（含自訂 JPQL 搜尋方法）
│   │   │       ├── service/
│   │   │       │   ├── ItemService.java            商品 CRUD 與狀態管理（Admin 用途）
│   │   │       │   └── SearchService.java          搜尋條件組裝與排序邏輯
│   │   │       └── dto/
│   │   │           ├── SearchRequest.java          搜尋表單參數封裝（keyword, category, 數值門檻等）
│   │   │           ├── ItemDto.java                商品顯示 DTO（解耦 Entity 直接暴露）
│   │   │           └── AdminItemRequest.java       管理端新增/更新商品請求 DTO
│   │   └── resources/
│   │       ├── application.yml                     共用設定（port, JPA DDL-auto 等）
│   │       ├── application-dev.yml                 H2 資料來源設定
│   │       ├── application-prod.yml                MySQL 8 資料來源設定（credentials 由環境變數注入）
│   │       ├── data.sql                            H2 測試樣本資料（dev profile 自動載入）
│   │       ├── templates/
│   │       │   ├── index.html                      搜尋主頁（含搜尋欄 + 結果區）
│   │       │   ├── fragments/
│   │       │   │   ├── layout.html                 共用頁面框架（head, navbar, footer）
│   │       │   │   ├── item-equipment-row.html     裝備商品列 fragment
│   │       │   │   └── item-general-row.html       一般商品列 fragment
│   │       │   └── admin/
│   │       │       ├── dashboard.html              管理後台首頁（商品列表＋操作按鈕）
│   │       │       └── item-form.html              商品新增/編輯表單
│   │       └── static/
│   │           ├── css/
│   │           │   └── custom.css                  客製化樣式覆寫
│   │           ├── js/
│   │           │   └── trade-code.js               複製交易暗號（Clipboard API + fallback）
│   │           └── images/
│   │               └── items/                      遊戲道具圖示（{item_id}.png）
│   └── test/
│       └── java/
│           └── com/msshop/
│               ├── controller/
│               │   ├── SearchControllerTest.java   搜尋頁面單元/整合測試
│               │   └── AdminControllerTest.java    管理端操作整合測試
│               ├── service/
│               │   ├── ItemServiceTest.java        商品服務單元測試（Mockito）
│               │   └── SearchServiceTest.java      搜尋條件組裝單元測試（Mockito）
│               └── repository/
│                   └── ItemRepositoryIntegrationTest.java  JPA 查詢整合測試（Testcontainers MySQL）
└── specs/
    └── 001-trade-form/             （規格文件，如上節所示）
```

**結構說明**：採用標準 Gradle + Spring Boot 多層架構（Controller → Service → Repository），以 `com.msshop` 作為根 package 名稱。`domain` 層集中管理 JPA Entity 與所有 Enum 定義；`dto` 層隔離 HTTP 請求/回應資料結構，避免 Entity 直接暴露至 Controller 層或序列化至回應中。Thymeleaf `fragments/` 子目錄存放可重用模板片段，透過 `th:replace` 機制在搜尋結果頁按商品類型切換裝備列與一般商品列，避免單一模板中充斥大量條件分支。測試依層次切分為 Controller 整合測試（`@WebMvcTest`）、Service 單元測試（Mockito）、Repository 整合測試（Testcontainers + MySQL 容器），確保各層職責獨立可測。
