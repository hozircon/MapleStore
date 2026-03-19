# 搜尋頁面 Controller 契約：輕量化交易展示站

**功能**: `001-trade-form` | **日期**: 2026-03-18 | **規格文件**: [spec.md](spec.md)

---

## 一、概述

本文件定義買家搜尋頁面的 Controller 輸入／輸出契約。由於本應用採用 Thymeleaf 伺服器端渲染（非 REST API），此契約描述的是 HTML 表單提交至 Spring MVC Controller 的請求參數規格，以及 Controller 回傳至 Thymeleaf 模板的 Model 屬性。

**Controller 類別**：`com.msshop.controller.SearchController`
**模板路徑**：`src/main/resources/templates/index.html`

---

## 二、端點定義

### GET `/`（搜尋主頁）

| 項目 | 說明 |
|---|---|
| HTTP 方法 | `GET` |
| 路徑 | `/` |
| 觸發時機 | 買家進入網站或點擊搜尋按鈕 |
| 回應類型 | Thymeleaf 模板渲染後的 HTML |
| 模板名稱 | `index` |

---

## 三、請求參數（Query String 表單參數）

表單以 `method="GET"` 提交至 `/`，所有參數均為選填（伺服器端展現寬鬆處理），參數預設值如下說明。

### 3.1 基本搜尋參數

| 參數名稱 | Java 對應欄位 | 類型 | 必填 | 預設值 | 說明 |
|---|---|---|---|---|---|
| `keyword` | `SearchRequest.keyword` | `String` | 否 | `null`（無關鍵字限制） | 商品名稱關鍵字；Server 端以 `LIKE %keyword%` 模糊比對（大小寫不敏感） |
| `category` | `SearchRequest.category` | `String` | 否 | `null`（所有大類） | 大類代碼：`EQUIPMENT` / `CONSUMABLE` / `DECORATIVE` / `OTHER` |
| `subCategory` | `SearchRequest.subCategory` | `String` | 否 | `null` | 次分類；僅 category ≠ EQUIPMENT 時有意義，例：`scroll`、`material`、`ore` |

### 3.2 貨幣篩選參數

| 參數名稱 | Java 對應欄位 | 類型 | 必填 | 預設值 | 說明 |
|---|---|---|---|---|---|
| `priceTypes` | `SearchRequest.priceTypes` | `List<String>` | 否 | `["MESO", "CS", "WS"]`（全選） | 多值參數（HTML `<input type="checkbox" name="priceTypes" value="MESO">`）；允許值：`MESO`、`CS`、`WS` |

> **注意**：`priceTypes` 為多值參數，HTTP 請求中以重複參數名稱傳遞，例：`?priceTypes=MESO&priceTypes=CS`。若使用者不勾選任何貨幣（全不選），Server 端視為「全選」（避免空結果集造成困擾），以 `null` 或空 List 進入時自動套用全部三種貨幣。

### 3.3 裝備進階篩選參數（僅 category = EQUIPMENT 時有效）

| 參數名稱 | Java 對應欄位 | 類型 | 必填 | 預設值 | 說明 |
|---|---|---|---|---|---|
| `strMin` | `SearchRequest.strMin` | `Integer` | 否 | `null`（不設下限） | STR 最小值門檻（`str_bonus >= strMin`） |
| `dexMin` | `SearchRequest.dexMin` | `Integer` | 否 | `null` | DEX 最小值門檻 |
| `intMin` | `SearchRequest.intMin` | `Integer` | 否 | `null` | INT 最小值門檻 |
| `lukMin` | `SearchRequest.lukMin` | `Integer` | 否 | `null` | LUK 最小值門檻 |
| `atkMin` | `SearchRequest.atkMin` | `Integer` | 否 | `null` | ATK 最小值門檻（`atk_bonus >= atkMin`） |
| `matkMin` | `SearchRequest.matkMin` | `Integer` | 否 | `null` | MATK 最小值門檻 |
| `scrollSlotsMin` | `SearchRequest.scrollSlotsMin` | `Integer` | 否 | `null` | 剩餘捲軸孔數最小值門檻（`scroll_slots_remaining >= scrollSlotsMin`） |

> **注意**：所有裝備進階篩選條件以 AND 邏輯套用。若 category ≠ EQUIPMENT，Server 端忽略這些參數（`SearchService` 不加入 JPQL 條件）。

### 3.4 完整請求範例

**範例 A：搜尋含"戒指"的在庫裝備，ATK ≥ 10，僅限楓幣**

```
GET /?keyword=戒指&category=EQUIPMENT&priceTypes=MESO&atkMin=10
```

**範例 B：搜尋所有在庫消耗品，次分類為 scroll，含楓幣與 CS**

```
GET /?category=CONSUMABLE&subCategory=scroll&priceTypes=MESO&priceTypes=CS
```

**範例 C：空搜尋（回傳所有在庫商品）**

```
GET /
```

---

## 四、`SearchRequest` DTO 定義

```java
public class SearchRequest {
    private String keyword;
    private String category;
    private String subCategory;
    private List<String> priceTypes;   // 空或 null 時 Service 層視為全選

    // 裝備進階篩選（以下欄位 null 代表不設限）
    private Integer strMin;
    private Integer dexMin;
    private Integer intMin;
    private Integer lukMin;
    private Integer atkMin;
    private Integer matkMin;
    private Integer scrollSlotsMin;
}
```

---

## 五、Controller 回傳 Model 屬性

`SearchController.search()` 方法將以下屬性加入 Spring MVC `Model`，供 Thymeleaf 模板 `index.html` 使用：

| 屬性名稱 | Java 類型 | 說明 |
|---|---|---|
| `items` | `List<ItemDto>` | 搜尋結果商品列表（依排序邏輯排列）；無結果時為空 List（不為 `null`） |
| `searchRequest` | `SearchRequest` | 原始搜尋請求物件，供模板保留表單狀態（重新填回搜尋欄位值） |
| `totalCount` | `int` | 搜尋結果總筆數（等同 `items.size()`，供模板顯示「共 N 筆結果」） |
| `hasResults` | `boolean` | `totalCount > 0`；模板以此決定顯示結果區或空結果提示訊息 |
| `categories` | `Category[]` | 所有大類 Enum 值（`Category.values()`），供側欄大類選單渲染 |

### `ItemDto` 欄位定義

```java
public class ItemDto {
    private Long id;
    private Integer itemId;                 // 用於圖示路徑 /images/items/{itemId}.png
    private String name;
    private String category;               // Enum 名稱字串，例"EQUIPMENT"
    private String subCategory;
    private Integer strBonus;
    private Integer dexBonus;
    private Integer intBonus;
    private Integer lukBonus;
    private Integer atkBonus;
    private Integer matkBonus;
    private Integer scrollSlotsRemaining;
    private String priceType;              // Enum 名稱字串，例"MESO"
    private Long priceValue;
    private Integer quantity;
    private String location;
    private String sellerName;
    private String status;
    private Boolean featured;

    // 衍生計算欄位（方便模板使用）
    public boolean isEquipment() {
        return "EQUIPMENT".equals(this.category);
    }

    public String getIconPath() {
        return "/images/items/" + itemId + ".png";
    }

    // 交易暗號格式（用於 JavaScript 複製按鈕的 data attribute）
    public String getTradeCode() {
        return name + " " + priceValue + " " + priceType + " @" + location;
    }
}
```

---

## 六、排序邏輯契約

搜尋結果**在資料庫層**完成排序，不在應用層手動分組。`ItemRepository` 或 `SearchService` 中的 JPQL 查詢必須使用以下排序表達式：

```java
// JPQL ORDER BY 子句
ORDER BY
    CASE item.priceType
        WHEN com.msshop.domain.PriceType.MESO THEN 1
        WHEN com.msshop.domain.PriceType.CS   THEN 2
        WHEN com.msshop.domain.PriceType.WS   THEN 3
        ELSE 4
    END ASC,
    item.priceValue ASC
```

**排序規則摘要**：
1. **第一排序鍵**：貨幣優先級（MESO = 1 → CS = 2 → WS = 3）
2. **第二排序鍵**：同一貨幣群組內，以 `price_value` 由小至大排列

此排序邏輯為硬性契約，不可在 Controller 或 Service 層覆寫或重排（對應規格 FR-009、SC-004）。

---

## 七、邊界情境處理

| 情境 | 處理方式 |
|---|---|
| 關鍵字為空白字串 | Server 端 trim 後視為 `null`，不套用 keyword 篩選條件 |
| `priceTypes` 為空（使用者未勾選任何貨幣） | `SearchService` 自動視為全選（MESO + CS + WS），避免空結果 |
| 非裝備大類但帶有裝備進階篩選參數 | `SearchService` 忽略裝備進階篩選參數，不加入查詢條件 |
| 搜尋結果為零筆 | 回傳空 `items` List，`hasResults = false`，模板顯示「無符合條件的商品」提示訊息 |
| 數值門檻使用者輸入負數 | Controller 層以 `@Min(0)` Bean Validation 攔截；若驗證失敗，重新渲染表單並顯示錯誤訊息 |
