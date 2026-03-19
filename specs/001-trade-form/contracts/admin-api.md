# 管理端 Controller 契約：輕量化交易展示站

**功能**: `001-trade-form` | **日期**: 2026-03-18 | **規格文件**: [spec.md](spec.md)

---

## 一、概述

本文件定義管理員後台的 Controller 輸入／輸出契約。管理端採用 Thymeleaf 表單提交（HTML form POST）搭配 Spring MVC `@PostMapping`/`@DeleteMapping`，使用 PRG（Post-Redirect-Get）模式避免表單重複提交問題。

**Controller 類別**：`com.msshop.controller.AdminController`
**模板路徑**：`src/main/resources/templates/admin/`

> **本迭代安全說明**：Admin 路徑（`/admin/**`）本迭代不實作認證，視為可信任存取。`spring-boot-starter-security` 依賴已保留於 `build.gradle`，後續迭代可直接啟用表單登入或 Basic Auth，不需重構 Controller。

---

## 二、端點總覽

| HTTP 方法 | 路徑 | 功能 | 回應 |
|---|---|---|---|
| `GET` | `/admin` | 管理後台首頁（商品列表） | 渲染 `admin/dashboard` |
| `GET` | `/admin/items/new` | 顯示新增商品表單 | 渲染 `admin/item-form` |
| `POST` | `/admin/items` | 新增商品 | Redirect `/admin?created=true` |
| `GET` | `/admin/items/{id}/edit` | 顯示編輯商品表單 | 渲染 `admin/item-form` |
| `POST` | `/admin/items/{id}` | 更新商品（含價格、數量、位置、狀態） | Redirect `/admin?updated=true` |
| `POST` | `/admin/items/{id}/soldout` | 標記單筆商品為已售完（SOLD_OUT） | Redirect `/admin?soldout=true` |
| `POST` | `/admin/items/soldout/delete` | 批量刪除所有已售完商品 | Redirect `/admin?bulkDeleted=true` |

> **關於 PUT/DELETE 方法**：HTML 表單原生僅支援 `GET` 與 `POST`。Spring Boot 預設啟用 `HiddenHttpMethodFilter`，可透過在表單中加入 `<input type="hidden" name="_method" value="PUT">` 模擬 RESTful 方法。本契約改以純 `POST` 路徑設計，以降低前端複雜度並確保最大瀏覽器相容性。

---

## 三、`GET /admin` — 管理後台首頁

**功能描述**：列出所有商品（含在庫與已售完），提供狀態切換與批量刪除操作入口。

**請求參數（Query String，來自 Redirect）**：

| 參數名稱 | 類型 | 說明 |
|---|---|---|
| `created` | `boolean`（選填） | 新增成功後 Redirect 帶入，模板顯示「商品已新增」成功訊息 |
| `updated` | `boolean`（選填） | 更新成功後帶入，顯示「商品已更新」 |
| `soldout` | `boolean`（選填） | 標記售完後帶入，顯示「已標記為售完」 |
| `bulkDeleted` | `boolean`（選填） | 批量刪除後帶入，顯示「已清除所有售完商品」 |

**Model 屬性**：

| 屬性名稱 | Java 類型 | 說明 |
|---|---|---|
| `items` | `List<ItemDto>` | 所有商品（不過濾 status），依 `created_at DESC` 排列 |
| `inStockCount` | `int` | 在庫商品筆數（顯示於頁面摘要） |
| `soldOutCount` | `int` | 已售完商品筆數（顯示批量刪除按鈕時的確認數量） |
| `flashMessage` | `String`（選填） | 由 Redirect 參數轉換的人性化操作回饋訊息 |

---

## 四、`GET /admin/items/new` — 顯示新增商品表單

**功能描述**：渲染空白的商品新增表單。

**Model 屬性**：

| 屬性名稱 | Java 類型 | 說明 |
|---|---|---|
| `item` | `AdminItemRequest` | 空白 DTO（供 Thymeleaf `th:object` 綁定） |
| `categories` | `Category[]` | `Category.values()`，供大類下拉選單渲染 |
| `priceTypes` | `PriceType[]` | `PriceType.values()`，供貨幣類型下拉選單渲染 |
| `isEdit` | `boolean` | `false`，模板依此決定顯示「新增」或「更新」按鈕文字 |

---

## 五、`POST /admin/items` — 新增商品

**功能描述**：接收表單提交，建立新商品並以 IN_STOCK 狀態儲存。

### 5.1 請求

**Content-Type**：`application/x-www-form-urlencoded`（HTML 表單 POST）

**請求參數（`AdminItemRequest` DTO 欄位一覽）**：

| 參數名稱 | Java 類型 | 必填 | 說明 |
|---|---|---|---|
| `itemId` | `Integer` | 是 | 遊戲道具 ID（正整數，> 0） |
| `name` | `String` | 是 | 商品顯示名稱（1–100 字元） |
| `category` | `String` | 是 | 大類代碼：`EQUIPMENT` / `CONSUMABLE` / `DECORATIVE` / `OTHER` |
| `subCategory` | `String` | 否 | 次分類（category ≠ EQUIPMENT 時可填） |
| `strBonus` | `Integer` | 否 | 裝備 STR 加成（category = EQUIPMENT 時填寫，>= 0） |
| `dexBonus` | `Integer` | 否 | 裝備 DEX 加成（>= 0） |
| `intBonus` | `Integer` | 否 | 裝備 INT 加成（>= 0） |
| `lukBonus` | `Integer` | 否 | 裝備 LUK 加成（>= 0） |
| `atkBonus` | `Integer` | 否 | 裝備 ATK 加成（>= 0） |
| `matkBonus` | `Integer` | 否 | 裝備 MATK 加成（>= 0） |
| `scrollSlotsRemaining` | `Integer` | 否 | 剩餘捲軸孔數（>= 0） |
| `priceType` | `String` | 是 | 貨幣類型：`MESO` / `CS` / `WS` |
| `priceValue` | `Long` | 是 | 標價數值（>= 0） |
| `quantity` | `Integer` | 是 | 庫存數量（>= 1，預設 1） |
| `location` | `String` | 是 | 遊戲內位置，1–50 字元（例：`CH1 FM03`） |
| `sellerName` | `String` | 是 | 賣家角色名稱，1–50 字元 |

### 5.2 回應

| 情境 | HTTP 回應 |
|---|---|
| 成功 | `302 Redirect` → `GET /admin?created=true` |
| 驗證失敗（Bean Validation 錯誤） | `200 OK`，重新渲染 `admin/item-form`，附 `BindingResult` 錯誤訊息 |

### 5.3 驗證規則（Bean Validation）

```java
public class AdminItemRequest {
    @NotNull @Min(1)
    private Integer itemId;

    @NotBlank @Size(min = 1, max = 100)
    private String name;

    @NotBlank
    private String category;

    @Size(max = 50)
    private String subCategory;

    @Min(0) private Integer strBonus;
    @Min(0) private Integer dexBonus;
    @Min(0) private Integer intBonus;
    @Min(0) private Integer lukBonus;
    @Min(0) private Integer atkBonus;
    @Min(0) private Integer matkBonus;
    @Min(0) private Integer scrollSlotsRemaining;

    @NotBlank
    private String priceType;

    @NotNull @Min(0)
    private Long priceValue;

    @NotNull @Min(1)
    private Integer quantity;

    @NotBlank @Size(min = 1, max = 50)
    private String location;

    @NotBlank @Size(min = 1, max = 50)
    private String sellerName;
}
```

### 5.4 Service 層業務邏輯

- 若 `category` ≠ `EQUIPMENT`，`ItemService` 強制將所有裝備數值欄位清為 `null`（`strBonus`、`dexBonus`、`intBonus`、`lukBonus`、`atkBonus`、`matkBonus`、`scrollSlotsRemaining`）。
- 若 `category` = `EQUIPMENT`，`ItemService` 強制將 `subCategory` 清為 `null`。
- 新增商品的初始 `status` 固定為 `IN_STOCK`（不由表單傳入）。
- 新增商品的初始 `featured` 固定為 `false`（不由表單傳入）。

---

## 六、`GET /admin/items/{id}/edit` — 顯示編輯商品表單

**功能描述**：以現有商品資料預填表單。

**路徑參數**：

| 參數名稱 | 類型 | 說明 |
|---|---|---|
| `id` | `Long` | 商品主鍵 ID |

**Model 屬性**：

| 屬性名稱 | Java 類型 | 說明 |
|---|---|---|
| `item` | `AdminItemRequest` | 以現有 `Item` Entity 資料填入的 DTO |
| `itemId_path` | `Long` | 商品 ID（表單 `action` 路徑用），以 `model.addAttribute` 加入 |
| `categories` | `Category[]` | `Category.values()` |
| `priceTypes` | `PriceType[]` | `PriceType.values()` |
| `isEdit` | `boolean` | `true` |

**例外情境**：若 `id` 不存在，回傳 `404 Not Found`（`ItemService` 拋出 `ItemNotFoundException`，由 `@ControllerAdvice` 處理）。

---

## 七、`POST /admin/items/{id}` — 更新商品

**功能描述**：更新既有商品的可編輯欄位（價格、數量、位置、狀態、裝備數值、大類、次分類）。

**路徑參數**：

| 參數名稱 | 類型 | 說明 |
|---|---|---|
| `id` | `Long` | 商品主鍵 ID |

**請求參數**：與「新增商品」（第五節）相同的 `AdminItemRequest` DTO 欄位，另加：

| 參數名稱 | Java 類型 | 必填 | 說明 |
|---|---|---|---|
| `status` | `String` | 否 | 在庫狀態：`IN_STOCK` / `SOLD_OUT`；不填時維持原狀 |

**回應**：

| 情境 | HTTP 回應 |
|---|---|
| 成功 | `302 Redirect` → `GET /admin?updated=true` |
| 驗證失敗 | `200 OK`，重新渲染 `admin/item-form` 附錯誤訊息 |
| 商品不存在 | `404 Not Found` |

**Service 層業務邏輯**：與新增相同的分類聯動清除邏輯（見 5.4 節）。只更新請求 DTO 中已提供的欄位；未提供的欄位維持原值（使用 `Optional` 欄位或直接全量覆寫均可，以全量覆寫為預設實作）。

---

## 八、`POST /admin/items/{id}/soldout` — 標記單筆商品為已售完

**功能描述**：快速將指定商品狀態切換為 `SOLD_OUT`，不需進入完整編輯表單。此端點通常由管理後台列表頁的「標記售完」按鈕觸發。

**路徑參數**：

| 參數名稱 | 類型 | 說明 |
|---|---|---|
| `id` | `Long` | 商品主鍵 ID |

**請求體**：無（表單無額外欄位，以空表單 POST 觸發）

**業務邏輯**：
1. 以 `id` 查詢 `Item`；若不存在則拋出 `ItemNotFoundException`。
2. 將 `status` 設為 `ItemStatus.SOLD_OUT`。
3. 儲存更新。

**回應**：

| 情境 | HTTP 回應 |
|---|---|
| 成功 | `302 Redirect` → `GET /admin?soldout=true` |
| 商品不存在 | `404 Not Found` |

> **已售完商品的可見性**：`ItemStatus.SOLD_OUT` 的商品在買家搜尋中會被 `status = 'IN_STOCK'` 過濾條件排除（規格 FR-014），買家即時不可見。

---

## 九、`POST /admin/items/soldout/delete` — 批量刪除所有已售完商品

**功能描述**：一次性刪除資料庫中所有 `status = 'SOLD_OUT'` 的商品，用於定期清理庫存清單（對應規格 FR-013、SC-006）。

**請求體**：無（以空表單 POST 觸發；建議前端以 `confirm()` 對話框或 Bootstrap Modal 確認後再提交）

**業務邏輯**：
1. 執行 `ItemRepository.deleteByStatus(ItemStatus.SOLD_OUT)`（JPQL DELETE 查詢）。
2. 回傳受影響筆數（可選，供 Redirect 後顯示刪除數量）。

**回應**：

| 情境 | HTTP 回應 |
|---|---|
| 成功（含 0 筆可刪除的情境） | `302 Redirect` → `GET /admin?bulkDeleted=true` |

**幂等性說明**：若執行時已無 `SOLD_OUT` 商品，操作回傳成功（刪除 0 筆），不視為錯誤，Redirect 後頁面顯示「目前無售完商品可清除」訊息。
