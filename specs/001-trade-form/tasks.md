# Tasks: 輕量化交易展示站

**Input**: `specs/001-trade-form/` 設計文件
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, contracts/search-api.md ✅, contracts/admin-api.md ✅

## 格式說明：`[ID] [P?] [Story?] 描述 + 檔案路徑`

- **[P]**：可與其他標記 [P] 的任務並行執行（不同檔案、無相依）
- **[US?]**：所屬 User Story（US1–US5）
- 無 Story 標記 = 基礎建設任務（Setup / Foundational）

---

## Phase 1：建置環境（Setup）

**目的**：建立 Spring Boot 專案基礎結構，讓所有後續任務可在同一程式碼庫上進行

- [X] T001 初始化 Gradle 8.x + Spring Boot 3.x 專案，設定 `settings.gradle`（`rootProject.name = 'ms-web'`）並建立 `build.gradle`（套件：`spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`（暫不啟用）, `h2`, `mysql-connector-j`, `lombok`，測試：`spring-boot-test`, `mockito-core`, `testcontainers`）
- [X] T002 建立 Spring Boot 入口類別 `src/main/java/com/msshop/MsWebApplication.java`
- [X] T003 [P] 建立三份設定檔：`src/main/resources/application.yml`（port 8080, JPA ddl-auto create-drop, 啟用 H2 console）、`src/main/resources/application-dev.yml`（H2 in-memory DataSource）、`src/main/resources/application-prod.yml`（MySQL 8 DataSource，credentials 由環境變數注入）
- [X] T004 [P] 建立目錄骨架：`src/main/java/com/msshop/{controller,domain,repository,service,dto}/`、`src/main/resources/{templates/fragments,templates/admin,static/{css,js,images/items}}/`、`src/test/java/com/msshop/{controller,service,repository}/`

**Checkpoint**：`./gradlew build`（僅建置，無業務邏輯）應順利完成

---

## Phase 2：基礎建設（Foundational）

**目的**：建立所有 User Story 共用的核心 Entity、Repository、DTO 及共用模板框架；此 Phase 必須在所有 US 開始前完成

⚠️ **關鍵阻擋**：US1–US5 均依賴此 Phase 的 `Item` entity 與 `ItemDto`

- [X] T005 建立 `Category` Enum `src/main/java/com/msshop/domain/Category.java`（`EQUIPMENT`, `CONSUMABLE`, `DECORATIVE`, `OTHER`）
- [X] T006 [P] 建立 `PriceType` Enum `src/main/java/com/msshop/domain/PriceType.java`（`MESO`, `CS`, `WS`）
- [X] T007 [P] 建立 `ItemStatus` Enum `src/main/java/com/msshop/domain/ItemStatus.java`（`IN_STOCK`, `SOLD_OUT`）
- [X] T008 建立 `Item` JPA Entity `src/main/java/com/msshop/domain/Item.java`（依 data-model.md 完整欄位：`id`, `itemId`, `name`, `category`, `subCategory`, `strBonus`, `dexBonus`, `intBonus`, `lukBonus`, `atkBonus`, `matkBonus`, `scrollSlotsRemaining`, `priceType`, `priceValue`, `quantity`, `location`, `sellerName`, `status`, `featured`, `createdAt`, `updatedAt`；搭配 `@Table` 四個索引宣告；`@CreationTimestamp` / `@UpdateTimestamp`）（依賴 T005–T007）
- [X] T009 建立 `ItemRepository` `src/main/java/com/msshop/repository/ItemRepository.java`（繼承 `JpaRepository<Item, Long>`；宣告自訂 JPQL 查詢方法簽名，搜尋 + 排序邏輯由 SearchService 動態組裝，此處留存 `findAllByStatus()` 基礎方法即可；另宣告 `deleteAllByStatus(ItemStatus status)`（批量刪除售完商品））
- [X] T010 建立 `ItemDto` `src/main/java/com/msshop/dto/ItemDto.java`（欄位：`id`, `itemId`, `name`, `category`（`Category`）, `subCategory`, `strBonus`, `dexBonus`, `intBonus`, `lukBonus`, `atkBonus`, `matkBonus`, `scrollSlotsRemaining`, `priceType`（`PriceType`）, `priceValue`, `quantity`, `location`, `sellerName`, `status`（`ItemStatus`）；靜態 factory `ItemDto.from(Item item)`）
- [X] T011 建立 Thymeleaf 共用頁面框架 fragment `src/main/resources/templates/fragments/layout.html`（`head` 含 Bootstrap 5 CDN、`custom.css`；`navbar`；`footer`；`th:fragment="page(title, content)"` 供所有頁面繼承）
- [X] T012 [P] 建立 H2 樣本資料 `src/main/resources/data.sql`（至少 10 筆：含 EQUIPMENT / CONSUMABLE / OTHER 各類型、三種 PriceType、IN_STOCK 與 SOLD_OUT 各數筆、裝備數值欄位含有值與 null 兩種情況）

**Checkpoint**：`./gradlew bootRun --args='--spring.profiles.active=dev'` 啟動不報錯；存取 `http://localhost:8080/h2-console` 可見 `ITEM` 資料表與樣本資料

---

## Phase 3：US1 — 買家關鍵字與條件搜尋（Priority: P1）🎯 MVP

**目標**：買家能透過關鍵字與篩選條件搜尋在庫商品，並在點擊搜尋按鈕後取得過濾結果

**獨立測試標準**：啟動應用後，在搜尋欄輸入「戒指」、選擇裝備類、輸入 ATK ≥ 10，點擊搜尋，結果僅顯示符合條件的在庫裝備；空搜尋回傳全部在庫商品

### US1 實作

- [X] T013 [US1] 建立 `SearchRequest` DTO `src/main/java/com/msshop/dto/SearchRequest.java`（欄位：`keyword`, `category`, `subCategory`, `priceTypes`（`List<String>`）, `strMin`, `dexMin`, `intMin`, `lukMin`, `atkMin`, `matkMin`, `scrollSlotsMin`；`priceTypes` 空值預設全選邏輯加入 getter）
- [X] T014 [US1] 實作 `SearchService` `src/main/java/com/msshop/service/SearchService.java`（方法 `search(SearchRequest req): List<ItemDto>`；以 `JpaSpecification<Item>` 或 JPQL 動態組裝：`status = IN_STOCK` 必要條件；`name LIKE %keyword%`（大小寫不敏感；keyword 非空才加入）；`category = ?`（非空才加入）；`subCategory = ?`（category ≠ EQUIPMENT 且非空才加入）；`priceType IN (?)`；裝備數值門檻以 AND 邏輯（category = EQUIPMENT 時才加入）；排序：`CASE price_type WHEN 'MESO' THEN 1 WHEN 'CS' THEN 2 WHEN 'WS' THEN 3 END ASC, price_value ASC`）（依賴 T008–T010）
- [X] T015 [US1] 實作 `SearchController` `src/main/java/com/msshop/controller/SearchController.java`（`GET /`：綁定 `SearchRequest`，呼叫 `SearchService.search()`，注入 Model：`items`, `searchRequest`, `totalCount`, `hasResults`, `categories`，渲染 `index`）（依賴 T014）
- [X] T016 [US1] 建立搜尋主頁模板 `src/main/resources/templates/index.html`（以 `layout.html` 框架；左側 Filter Panel（20-25%寬）：關鍵字輸入框、大類選單（動態 `th:each="cat : ${categories}"`）、貨幣 Checkbox 三選、進階篩選區（依 category 以 JS 顯示/隱藏：裝備類顯示職業/部位/數值輸入框，非裝備類顯示次分類下拉）、搜尋按鈕（`method="GET"` 表單）；右側 Result Panel（75-80%寬）：`th:if="${hasResults}"` 結果列表 + `th:unless="${hasResults}"` 空結果提示；表單送出後以 `th:value="${searchRequest.keyword}"` 等保留篩選值）（依賴 T011、T015）

**Checkpoint**：瀏覽 `http://localhost:8080/`，執行含關鍵字 + 類別 + 數值門檻的搜尋，結果正確過濾且不含 SOLD_OUT 商品

---

## Phase 4：US2 — 貨幣優先排序（Priority: P2）

**目標**：搜尋結果依 MESO → CS → WS 分組，每組內依價格升序排列

**獨立測試標準**：資料集內含 MESO、CS、WS 各至少一筆商品；執行任意搜尋，確認所有 MESO 商品出現在所有 CS 商品之前，CS 商品出現在 WS 商品之前，且各組內依 `priceValue` 升序排列

### US2 實作

- [X] T017 [US2] 確認 `SearchService.search()` 的 ORDER BY 子句正確實作三段式貨幣群組排序：在 `src/main/java/com/msshop/service/SearchService.java` 中驗證 sort expression 為 `CASE WHEN i.priceType = 'MESO' THEN 1 WHEN i.priceType = 'CS' THEN 2 ELSE 3 END ASC, i.priceValue ASC`（若 T014 已正確實作則此任務為驗證；若尚未完整實作則補齊）
- [X] T018 [US2] 在 `src/main/resources/templates/index.html` 結果區加入貨幣群組視覺分隔：在裝備列與一般商品列的價格欄位旁以 Thymeleaf 條件顯示對應貨幣圖示（楓幣/CS/WS icon；圖示以 `static/images/` 靜態資源或 Bootstrap badge 文字標籤代替，視圖示資源是否備妥而定）（依賴 T016）

**Checkpoint**：搜尋含三種貨幣的結果，手動確認排序符合 MESO → CS → WS → priceValue ASC 規則

---

## Phase 5：US3 — 裝備 / 一般商品雙版型展示（Priority: P2）

**目標**：裝備類商品列顯示完整數值（STR/DEX/INT/LUK/ATK/MATK/捲次），一般商品列顯示數量與次分類；兩種版型並存於同一結果頁

**獨立測試標準**：搜尋結果同時包含裝備與一般商品；裝備列可見 STR–MATK 欄位（缺值顯示 0），一般商品列可見 Qty 欄位

### US5 實作

- [X] T019 [P] [US3] 建立裝備商品列 fragment `src/main/resources/templates/fragments/item-equipment-row.html`（`th:fragment="equipmentRow(item)"`；顯示欄位：道具圖示（`/images/items/{itemId}.png`）、名稱、STR、DEX、INT、LUK、ATK、MATK、剩餘捲次（null 以 `?:0` 顯示為 0）、貨幣圖示 + 價格、賣家名稱、遊戲位置）
- [X] T020 [P] [US3] 建立一般商品列 fragment `src/main/resources/templates/fragments/item-general-row.html`（`th:fragment="generalRow(item)"`；顯示欄位：列號（`${iterStat.count}`）、道具圖示、名稱、次分類、數量、貨幣圖示 + 價格、賣家名稱、遊戲位置）
- [X] T021 [US3] 在 `src/main/resources/templates/index.html` 結果迴圈中依 `item.category == T(com.msshop.domain.Category).EQUIPMENT` 條件以 `th:replace` 切換 fragment：裝備類套用 `equipmentRow`，其餘套用 `generalRow`（依賴 T019、T020、T016）

**Checkpoint**：搜尋混合類型結果，裝備列與一般商品列各以正確欄位呈現，缺值欄位歸零不報錯

---

## Phase 6：US4 — 複製交易暗號（Priority: P3）

**目標**：每筆商品列有「複製交易暗號」按鈕；點擊後剪貼簿取得格式化訊息（含商品名、價格、貨幣、賣家位置）；剪貼簿不可用時以可選取文字 fallback 顯示

**獨立測試標準**：點擊任一商品的「複製交易暗號」按鈕，剪貼簿內容為格式如 `【交易暗號】{name} / {priceValue} {priceType} / {location}` 的字串；Clipboard API 不支援時 fallback `<input>` 可見且可全選複製

### US4 實作

- [X] T022 [US4] 在 `item-equipment-row.html` 與 `item-general-row.html` 兩個 fragment 的商品列末端加入「複製交易暴語」按鈕（`data-trade-code="..."` 屬性存放格式化字串；按鈕附 `class="copy-btn"`）（依賴 T019、T020）
- [X] T023 [US4] 實作 `src/main/resources/static/js/trade-code.js`（監聽 `.copy-btn` 的 `click` 事件；以 `navigator.clipboard.writeText()` 寫入 `data-trade-code` 值；成功後在按鈕旁短暫顯示「已複製！」tooltip；`catch` 區塊 fallback：顯示隱藏的 `<input readonly>` 並 select 全文，讓使用者手動複製）
- [X] T024 [US4] 在 `src/main/resources/templates/index.html` 底部引入 `trade-code.js`（`<script th:src="@{/js/trade-code.js}">`）（依賴 T023、T016）

**Checkpoint**：點擊裝備商品與一般商品各一次「複製交易暗號」，確認剪貼簿內容包含正確的名稱、價格與賣家位置；在無 Clipboard API 環境（可透過 DevTools 暫時 override）確認 fallback input 可見

---

## Phase 7：US5 — 管理員庫存狀態管理（Priority: P4）

**目標**：管理員可在後台查看所有商品（含在庫/售完）、個別標記售完、批量刪除已售完商品、以及新增/編輯商品

**獨立測試標準**：在後台將一筆在庫商品標記為售完，回到買家搜尋頁確認該商品不再出現；使用批量刪除後所有 SOLD_OUT 商品從後台列表移除

### US5 實作

- [X] T025 [US5] 建立 `AdminItemRequest` DTO `src/main/java/com/msshop/dto/AdminItemRequest.java`（欄位與 Bean Validation 規則詳見 contracts/admin-api.md §5.3；`@NotNull @Min(1) itemId`、`@NotBlank @Size(max=100) name`、`@NotBlank category`、選填裝備數值欄位 `@Min(0)`、`@NotBlank priceType`、`@NotNull @Min(0) priceValue`、`@NotNull @Min(1) quantity`、`@NotBlank @Size(max=50) location/sellerName`）
- [X] T026 [US5] 實作 `ItemService` `src/main/java/com/msshop/service/ItemService.java`（方法：`findAll(): List<ItemDto>`（依 `createdAt DESC`）；`findById(Long id): Item`；`create(AdminItemRequest req): Item`（status 預設 `IN_STOCK`）；`update(Long id, AdminItemRequest req): Item`；`markSoldOut(Long id): void`（設 status = SOLD_OUT）；`deleteAllSoldOut(): int`（刪除並回傳刪除筆數）；`countByStatus(ItemStatus): int`）（依賴 T008–T010）
- [X] T027 [US5] 實作 `AdminController` `src/main/java/com/msshop/controller/AdminController.java`（完整 7 個端點，詳見 contracts/admin-api.md §二；PRG 模式：所有 POST 成功後 Redirect 附 QueryString flash 參數；繫結 `AdminItemRequest` 並呼叫 `@Valid`；驗證失敗時回傳表單頁）（依賴 T025–T026）
- [X] T028 [US5] 建立管理後台首頁模板 `admin/dashboard.html` `src/main/resources/templates/admin/dashboard.html`（`layout.html` 框架；顯示商品列表（全部，含 status badge）；每列有「標記售完」POST 按鈕（`method="POST" action="/admin/items/{id}/soldout"`）；頁頂顯示在庫/售完數量摘要；批量刪除按鈕（`action="/admin/items/soldout/delete"`，含確認提示）；`flashMessage` 成功訊息 banner）（依賴 T027）
- [X] T029 [US5] 建立商品新增/編輯表單模板 `admin/item-form.html` `src/main/resources/templates/admin/item-form.html`（`layout.html` 框架；`th:object="${item}"` 綁定 `AdminItemRequest`；所有欄位含 `th:field`；`category` 聯動邏輯：JavaScript 依所選大類顯示/隱藏裝備數值欄位與次分類欄位；Bean Validation 錯誤訊息 `th:errors`；`isEdit` 控制按鈕文字「新增商品」/「更新商品」）（依賴 T027）

**Checkpoint**：透過 `POST /admin/items` 新增商品後在買家搜尋頁可見；將商品標記售完後搜尋頁不顯示；批量刪除後後台商品列表中無 SOLD_OUT 項目

---

## Phase 8：收尾與橫切關注點（Polish）

**目的**：補齊細節，確保整體品質與一致性

- [X] T030 [P] 建立 `src/main/resources/static/css/custom.css`（左右分欄版面微調：`.filter-panel`（20% 寬）、`.result-panel`（80% 寬）、裝備列數值欄位 nowrap 設定、售完商品 badge 樣式、flash message banner 樣式）
- [ ] T031 [P] 在 `src/main/resources/templates/index.html` Filter Panel 加入進階篩選動態顯示 JavaScript（頁內 `<script>`：監聽 category 選單 `change` 事件，依值切換顯示 `#equipment-filters`（裝備數值輸入區）與 `#subcategory-filter`（次分類下拉）；頁面載入時依當前表單值還原狀態（搜尋後 reload 保持正確顯示））（依賴 T016）
- [X] T032 新增 Security 設定，確認 `/admin/**` 本迭ai不需登入（或設定 `SecurityFilterChain` bean 全開放所有請求），確認 `/admin/**` 路徑於本迭代不需登入即可存取；在 `build.gradle` 對 `spring-boot-starter-security` 依賴加上 `/* TODO: enable auth in next iteration */` 注解
- [X] T033 [P] 驗證 quickstart.md 流程：依照 `specs/001-trade-form/quickstart.md` 執行完整步驟（`./gradlew bootRun`、H2 console 確認資料、執行搜尋情境驗證腳本），並補齊 quickstart.md 中任何與實際實作不符的步驟描述
- [X] T034 [P] 確認道具圖示路徑規則與 fallback 處理：在 `item-equipment-row.html` 與 `item-general-row.html` 中，圖示 `<img>` 以 `th:src="@{/images/items/{itemId}(itemId=${item.itemId})}.png"` 呈現；在 `src/main/resources/static/images/items/` 放入至少一個測試用佔位圖示（`0.png`），When `itemId` 無對應圖示時以 `onerror="this.src='/images/items/0.png'"` fallback 處理

---

## 相依關係與執行順序

### Phase 相依

| Phase | 依賴 | 說明 |
|---|---|---|
| Phase 1（Setup） | 無 | 可立即開始 |
| Phase 2（Foundational） | Phase 1 完成 | 阻擋所有 US |
| Phase 3（US1）| Phase 2 完成 | P1 最優先，MVP 核心 |
| Phase 4（US2）| Phase 2 完成；US2 依賴 T014（SearchService 排序）已在 US1 實作 | 可與 Phase 5 並行 |
| Phase 5（US3）| Phase 2 完成；依賴 T016（index.html 基本結構）已在 US1 完成 | 可與 Phase 4 並行 |
| Phase 6（US4）| US3 完成（T019、T020 fragment 已建立） | 需在 fragment 中加入按鈕 |
| Phase 7（US5）| Phase 2 完成（`Item` entity 可獨立進行） | 可與 Phase 3–6 並行 |
| Phase 8（Polish）| 所有 US Phase 完成 | 最後執行 |

### User Story 相依關係

- **US1（P1）**：Phase 2 完成後即可開始；無其他 US 相依 — **MVP 最小可交付範圍**
- **US2（P2）**：排序邏輯已含於 US1 的 SearchService 中；US2 任務主要為驗證與 UI 補充，可與 US3 並行
- **US3（P2）**：依賴 US1 建立的 `index.html` 基本結構；fragment 部分（T019、T020）可與 US1 並行
- **US4（P3）**：依賴 US3 的 fragment（需在其中加入按鈕）
- **US5（P4）**：可與 US1–US4 完全並行（共用 `Item` entity，操作不同 Controller 與模板）

### 各 User Story 內部順序

```
DTO / Request 物件 → Service 邏輯 → Controller → Thymeleaf 模板
```

### 並行機會

| 可並行的任務組合 | 說明 |
|---|---|
| T005、T006、T007 | 三個 Enum 無相互依賴 |
| T003、T004 | 設定檔與目錄建立無相依 |
| T019、T020 | 兩個 fragment 操作不同檔案 |
| T030、T031、T033、T034 | Polish 任務各自獨立 |
| US5（T025–T029）與 US1–US4 | 管理端 Controller 與買家端完全獨立 |
| US2（T017–T018）與 US3（T019–T021）| Phase 4 與 Phase 5 可同時進行 |

---

## 並行執行範例：US1（MVP sprint）

```
Day 1
├── T001 初始化 Gradle 專案
└── T002 建立入口類別

Day 2 (並行)
├── Thread A: T003 設定檔 + T004 目錄骨架
└── Thread B: T005 Category + T006 PriceType + T007 ItemStatus Enum

Day 3
└── T008 Item Entity（依賴 T005–T007）
    └── T009 ItemRepository
        └── T010 ItemDto
            └── T011 layout.html fragment
                └── T012 data.sql（可與 T011 並行）

Day 4
└── T013 SearchRequest DTO
    └── T014 SearchService（核心排序邏輯）
        └── T015 SearchController
            └── T016 index.html（搜尋主頁模板）

→ US1 MVP 可測試 ✅
```

---

## 實作策略

### MVP 範圍（建議第一優先）

完成 **Phase 1 + Phase 2 + Phase 3（US1）** 即為最小可示範版本：
- 買家可用關鍵字 + 類別 + 貨幣篩選搜尋商品
- 結果依正確排序顯示
- 涵蓋 FR-001, FR-002, FR-003, FR-006, FR-009, FR-014 共 6 條核心需求

### 增量交付順序建議

1. **Sprint 1**：Phase 1 + Phase 2 + Phase 3（US1 MVP）
2. **Sprint 2**：Phase 4（US2 排序）+ Phase 5（US3 雙版型）+ Phase 7（US5 管理端）
3. **Sprint 3**：Phase 6（US4 複製暗號）+ Phase 8（收尾）

### 總任務數量

| Phase | 任務數 |
|---|---|
| Phase 1 Setup | 4 |
| Phase 2 Foundational | 8 |
| Phase 3 US1（P1） | 4 |
| Phase 4 US2（P2） | 2 |
| Phase 5 US3（P2） | 3 |
| Phase 6 US4（P3） | 3 |
| Phase 7 US5（P4） | 5 |
| Phase 8 Polish | 5 |
| **合計** | **34** |
