# 改動計畫：分類重構、資料表分拆、前端搜尋升級

**建立日期**：2026-03-19  
**狀態**：待審核，尚未執行

---

## 一、改動總覽

| # | 項目 | 影響範圍 | 難度 |
|---|---|---|---|
| A | 分類選單中文化 | 前端 + enum | 低 |
| B | 消耗/其他子分類修正 | 前端 + SQL | 中 |
| C | 裝備新增「種類 + 子分類」篩選 | entity / SearchRequest / SearchService / 前端 | 中高 |
| D | 資料表分拆（4 分表） | 架構大改 | 高 |
| E | GameItem 目錄加裝備分類欄位 | WZ 解析器 + entity | 中 |

---

## 二、各項改動詳述

---

### A｜分類選單中文化

#### 現況
`Category` enum 目前值為 EQUIPMENT / CONSUMABLE / DECORATIVE / OTHER，
前端 `th:text="${cat.name()}"` 直接顯示英文。

#### 修改內容

**`Category.java`** — 加入中文 label 欄位：
```java
public enum Category {
    EQUIPMENT("裝備"),
    CONSUMABLE("消耗"),
    DECORATIVE("裝飾"),
    OTHER("其他");

    private final String label;
    Category(String label) { this.label = label; }
    public String getLabel() { return label; }
}
```

**`index.html`** — 分類下拉改顯示中文：
```html
<!-- 原本 -->
<option th:text="${cat.name()}">

<!-- 改為 -->
<option th:text="${cat.label}">
```

**影響的其他模板**：
- `admin/item-form.html` — category 選單同樣改為顯示 `${cat.label}`
- `admin/dashboard.html` — 列表中的 category 文字顯示也要改

---

### B｜消耗 / 裝飾 / 其他 子分類修正

#### 現況問題
1. `index.html` 次分類選項只綁到 CONSUMABLE，並且顯示英文（scroll/material/ore）
2. 礦石類商品目前分類為 CONSUMABLE，應改為 OTHER
3. 裝飾無子分類但前端未做空值處理

#### 新的子分類定義

| 大類 | 子分類選項 |
|---|---|
| 裝備 (EQUIPMENT) | — 見 C 項（改用種類/子分類） |
| 消耗 (CONSUMABLE) | 藥水、卷軸、飛鏢、其他 |
| 裝飾 (DECORATIVE) | — 無子分類 |
| 其他 (OTHER) | 礦石、材料 |

#### 修改內容

**`index.html`** — `subCategoryFilter` 區塊改為多個隱藏 select，依大類切換顯示：
```html
<!-- 消耗 -->
<select id="subCatConsumable" name="subCategory" style="display:none">
  <option value="">-- 全部 --</option>
  <option value="藥水">藥水</option>
  <option value="卷軸">卷軸</option>
  <option value="飛鏢">飛鏢</option>
  <option value="其他">其他</option>
</select>

<!-- 其他 -->
<select id="subCatOther" name="subCategory" style="display:none">
  <option value="">-- 全部 --</option>
  <option value="礦石">礦石</option>
  <option value="材料">材料</option>
</select>
```

JavaScript 根據大類切換顯示哪個 select；裝飾類不顯示任何子分類選單。

**`data.sql`** — 修正現有樣本資料分類：
- `礦石碎片`：CONSUMABLE + material → **OTHER + 礦石**
- `玄武岩`：CONSUMABLE + ore → **OTHER + 礦石**
- `裝備用力量卷軸 60%`（若存在） → sub_category 值改為**卷軸**

> **重要**：子分類儲存值從英文（scroll/material/ore）統一改為中文（卷軸/礦石/材料等）。
> 建議日後以 enum 或 DB lookup table 管理，以防手打錯字。

---

### C｜裝備新增「種類 + 子分類」篩選

#### 分類體系（來源：武器分類.xlsx）

**種類**（第一層）

| 種類值 | 說明 |
|---|---|
| 防具 | 可穿戴的防禦裝備 |
| 武器 | 攻擊用各類武器 |
| 其他 | 坐騎、寵物裝備 |

**子分類**（第二層，依種類）

| 種類 | 子分類列表 |
|---|---|
| 防具 | 頭盔、帽子、上衣、套服、褲裙、手套、鞋子、披風、飾品 |
| 武器 | 單手劍、雙手劍、單手斧、雙手斧、單手棍、雙手棍、短劍、指虎、拳套、矛、槍、弓、弩、火槍、短杖、長杖、盾牌 |
| 其他 | 坐騎、寵物裝備 |

#### 修改內容

**`Item.java`** — 新增兩個欄位：
```java
/** 裝備種類：防具 / 武器 / 其他（非裝備類為 null） */
@Column(name = "equip_type", length = 10)
private String equipType;

/** 裝備子分類：頭盔 / 上衣 / 單手劍 …（非裝備類為 null） */
@Column(name = "equip_sub_type", length = 20)
private String equipSubType;
```

**`SearchRequest.java`** — 新增：
```java
private String equipType;      // 防具 / 武器 / 其他
private String equipSubType;   // 頭盔 / 單手劍 …
```

**`SearchService.java`** — 在 EQUIPMENT 分支新增篩選條件：
```java
if (cat == Category.EQUIPMENT) {
    if (req.getEquipType() != null && !req.getEquipType().isBlank()) {
        jpql.append(" AND i.equipType = :equipType");
        params.put("equipType", req.getEquipType());
    }
    if (req.getEquipSubType() != null && !req.getEquipSubType().isBlank()) {
        jpql.append(" AND i.equipSubType = :equipSubType");
        params.put("equipSubType", req.getEquipSubType());
    }
    // 原有 STR/DEX/ATK 等篩選照舊
}
```

**`index.html`** — 裝備進階篩選區新增種類 + 子分類（JavaScript 動態切換子分類清單）：
```html
<div id="equipTypeFilter">
  <label>種類</label>
  <select id="equipTypeSelect" name="equipType">
    <option value="">-- 全部 --</option>
    <option value="防具">防具</option>
    <option value="武器">武器</option>
    <option value="其他">其他（坐騎/寵物）</option>
  </select>
</div>

<div id="equipSubTypeFilter">
  <label>子分類</label>
  <select id="equipSubTypeSelect" name="equipSubType">
    <option value="">-- 全部 --</option>
    <!-- 由 JS 動態填入 -->
  </select>
</div>
```

JavaScript 種類對應表（硬編碼於前端）：
```js
const EQUIP_SUB = {
  '防具': ['頭盔','帽子','上衣','套服','褲裙','手套','鞋子','披風','飾品'],
  '武器': ['單手劍','雙手劍','單手斧','雙手斧','單手棍','雙手棍',
           '短劍','指虎','拳套','矛','槍','弓','弩','火槍','短杖','長杖','盾牌'],
  '其他': ['坐騎','寵物裝備']
};
// 種類變更時清空並重填子分類 select
document.getElementById('equipTypeSelect').addEventListener('change', function () {
  const subs = EQUIP_SUB[this.value] || [];
  const subSel = document.getElementById('equipSubTypeSelect');
  subSel.innerHTML = '<option value="">-- 全部 --</option>'
    + subs.map(s => `<option value="${s}">${s}</option>`).join('');
});
```

**`admin/item-form.html`** — 新增商品時，顯示種類 + 子分類欄位（裝備分類才顯示）：
- `equipType` select（防具/武器/其他）
- `equipSubType` select（依種類動態切換）
- 若 GameItem 有對應分類（E 項完成後），可在選取道具時自動帶入

**`data.sql`** — 現有裝備樣本補上 equip_type / equip_sub_type：
```sql
-- 例：黑色勇士戒指 → equip_type='防具', equip_sub_type='飾品'
-- 例：暗影手套     → equip_type='防具', equip_sub_type='手套'
-- 例：精靈短劍     → equip_type='武器', equip_sub_type='短劍'
```

---

### D｜資料表分拆（4 分表）

> **建議在 A/B/C 完成並驗證後，再評估是否執行本項。**  
> 工作量大，若本版本功能需求已能用現有結構滿足，可推遲。

#### 現況問題
單一 `item` 資料表含所有欄位；裝備用的 STR/DEX/ATK/卷次欄位對消耗/裝飾/其他類全是 NULL，造成欄位浪費及查詢複雜。

#### 目標架構

| 新資料表 | 對應 Entity | 特有欄位 |
|---|---|---|
| `equip_item` | `EquipItem` | str_bonus, dex_bonus, int_bonus, luk_bonus, atk_bonus, matk_bonus, scroll_slots_remaining, equip_type, equip_sub_type |
| `consume_item` | `ConsumeItem` | sub_category（藥水/卷軸/飛鏢/其他） |
| `deco_item` | `DecoItem` | 無特有欄位 |
| `other_item` | `OtherItem` | sub_category（礦石/材料） |

**共用欄位**（四個表都有）：
`id`, `item_id`, `name`, `price_type`, `price_value`, `quantity`,  
`location`, `seller_name`, `status`, `featured`, `created_at`, `updated_at`

#### 架構方案比較

| 方案 | 說明 | 優點 | 缺點 |
|---|---|---|---|
| **方案 1：完全分表** | 4 個獨立 Entity + Repository | 索引清晰、欄位精確 | SearchService 需合併各表查詢結果 |
| **方案 2：JPA TABLE_PER_CLASS 繼承** | 抽象 `BaseItem`，子類各自建表 | 共用程式碼，Hibernate 管理繼承 | UNION ALL 查詢複雜，分頁較難 |
| **方案 3：JPA SINGLE_TABLE 繼承** | 保持單表，加 dtype 欄位 | 改動最小，分頁簡單 | NULL 欄位問題未解決 |

**建議採用方案 1**（完全分表），各類別搜尋邏輯本來就不同，分開最清晰。

#### 需要變更的檔案清單

| 類型 | 動作 |
|---|---|
| Entity | 新增 `EquipItem`, `ConsumeItem`, `DecoItem`, `OtherItem`；移除或廢棄舊 `Item` |
| Repository | 新增 `EquipItemRepository`, `ConsumeItemRepository`, `DecoItemRepository`, `OtherItemRepository` |
| SearchService | 改為分表查詢 + 合併排序（或拆成 4 個 Service）；需重新設計分頁邏輯 |
| SearchController | 改呼叫新 Service |
| AdminController | 新增/修改分流邏輯，依 category 路由到對應 Service |
| admin/item-form.html | 依類別動態顯示對應欄位 |
| data.sql | INSERT 改為寫入新表 |

---

### E｜GameItem 目錄加裝備分類欄位

當前 `GameItem` 只有 itemId、name、wzCategory，無法在 Admin 新增裝備時自動帶入種類/子分類。

#### 修改內容

**`GameItem.java`** — 新增欄位：
```java
/** 裝備種類：防具 / 武器 / 其他（非裝備類為 null） */
@Column(name = "equip_type", length = 10)
private String equipType;

/** 裝備子分類：頭盔 / 單手劍 …（非裝備類為 null） */
@Column(name = "equip_sub_type", length = 20)
private String equipSubType;
```

**`WzStringParserService.java`** — 解析 `Eqp.img.xml` 時，從 XML 路徑的第 3 層資料夾名稱（`name3`，例如 Cap / Coat / Weapon）透過下方靜態映射表對應 equipType 和 equipSubType。

**WZ 子資料夾 → 種類/子分類對應表**：

| WZ name3 | 種類 | 子分類 |
|---|---|---|
| Cap | 防具 | 頭盔 |
| Face | 防具 | 帽子 |
| Coat | 防具 | 上衣 |
| Longcoat | 防具 | 套服 |
| Pants | 防具 | 褲裙 |
| Glove | 防具 | 手套 |
| Shoes | 防具 | 鞋子 |
| Cape | 防具 | 披風 |
| Accessory | 防具 | 飾品 |
| Ring | 防具 | 飾品 |
| Shield | 武器 | 盾牌 |
| TamingMob | 其他 | 坐騎 |
| PetEquip | 其他 | 寵物裝備 |
| Weapon | 武器 | （需再依 itemId 前碼判斷，見下） |

**武器子分類：依 itemId 前三碼判斷**（需完整確認，以下為範例）：

| itemId 前碼 | 武器子分類 |
|---|---|
| 130X | 單手劍 |
| 131X | 單手斧 |
| 132X | 單手棍 |
| 137X | 短劍 |
| 140X | 雙手劍 |
| 141X | 雙手斧 |
| 142X | 雙手棍 |
| 143X | 矛 |
| 144X | 槍 |
| 145X | 弓 |
| 146X | 弩 |
| 147X | 火槍 |
| 148X | 拳套 |
| 149X | 指虎 |
| 152X | 短杖 |
| 153X | 長杖 |
| 154X | 盾牌（已在 name3=Shield，重複確認） |

**`GameItemDto.java`** — 新增 `equipType`, `equipSubType` 欄位回傳給前端，  
供 `admin/item-form.html` autocomplete 選取道具後自動帶入種類/子分類。

---

## 三、執行優先順序建議

```
Phase 1（低風險，立即可做）
  ├── A：分類選單中文化（Category enum + 3 個 template）
  └── B：子分類修正（index.html + data.sql）

Phase 2（中風險，需新增欄位）
  ├── C：裝備種類/子分類欄位 + 首頁篩選
  └── E：GameItem 加裝備分類（Admin 新增時自動帶入）

Phase 3（高風險，架構大改，確認需求後再做）
  └── D：資料表分拆（4 分表）
```

---

## 四、待確認事項

| # | 問題 | 影響 |
|---|---|---|
| 1 | 子分類儲存值是否統一中文？（卷軸、礦石 vs scroll, ore） | B 項、SearchService JPQL |
| 2 | `套服（Longcoat）` vs `上衣（Coat）` 是否分開子分類？ | C/E 項 |
| 3 | `Cap（頭盔）` 和一般帽子（Face）是否合併為「頭盔」？ | C/E 項 |
| 4 | 分表（D 項）是否納入本版本？或下個 Sprint？ | 架構決策 |
| 5 | 武器子分類 itemId 前碼范圍是否正確？需對照完整 xlsx | E 項 |
