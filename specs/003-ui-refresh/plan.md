# UI 優化方案 — 日系簡潔・淺色系

> **原則**：不動 HTML 結構與 Bootstrap 格線，僅透過 CSS 變數覆蓋與 class 微調達成風格轉換。

---

## 1. 設計語言

| 關鍵字 | 具體體現 |
|--------|----------|
| 日系簡潔 | 大量留白、細線邊框、少裝飾；去掉深色重陰影 |
| 淡米黃 | 頁面底色、側邊欄底色取自米白色系 |
| 淡天空藍 | 導覽列、主按鈕、表格 header、active 狀態 |
| 一致性 | 圓角統一 `6px`；字體粗細層次縮減至 2 級 |

---

## 2. 色彩系統（CSS 自訂變數）

```css
:root {
  /* 底色 */
  --ms-bg-page:       #F7F4EE;   /* 淡米黃 — 頁面背景 */
  --ms-bg-card:       #FDFCF8;   /* 接近白的米白 — 卡片/表格 */
  --ms-bg-panel:      #F0EDE5;   /* 稍深米黃 — 篩選側欄 */

  /* 天空藍系 */
  --ms-blue-nav:      #4A8FAB;   /* 導覽列背景（中深天空藍） */
  --ms-blue-light:    #D9EEF5;   /* 表格 header、標籤底色 */
  --ms-blue-mid:      #78B8D0;   /* hover、active 邊框 */
  --ms-blue-btn:      #3A7D96;   /* 主按鈕 */
  --ms-blue-btn-hov:  #2E6578;   /* 主按鈕 hover */

  /* 文字 */
  --ms-text-primary:  #2D2D2D;   /* 主文字（非純黑） */
  --ms-text-muted:    #7A7268;   /* 輔助文字（暖灰） */
  --ms-text-on-blue:  #FFFFFF;

  /* 邊框 */
  --ms-border:        #DDD8CF;   /* 卡片、輸入框邊框 */

  /* 價格色（保留語義，調柔） */
  --ms-meso:  #B07D2E;   /* 楓幣 — 暖金 */
  --ms-cs:    #2A7FC1;   /* CS   — 藍 */
  --ms-ws:    #3A9068;   /* WS   — 綠 */
}
```

---

## 3. 元件逐項說明

### 3.1 全域 / Body
- `body.bg-light` → 改為 `background-color: var(--ms-bg-page)`
- 預設字體加入 `'Noto Sans TC', sans-serif`（Google Fonts，繁中支援佳）
- 預設 `color: var(--ms-text-primary)`

### 3.2 導覽列（layout.html）
- 目前：`navbar-dark bg-dark`
- 改為：`navbar` + 自訂背景 `var(--ms-blue-nav)`，文字/按鈕白色
- 品牌名移除 emoji，改純文字 Logo 樣式（`letter-spacing: .05em`）
- 右側「管理後台」改為白色外框小按鈕

### 3.3 篩選側欄（index.html）
- card 背景改 `var(--ms-bg-panel)`
- `h6` 加細底線分隔（`border-bottom: 1px solid var(--ms-border)`）
- 表單控制項 `form-control`、`form-select`：border 改 `var(--ms-border)`，focus ring 改天空藍
- 搜尋按鈕：`btn-primary` 覆蓋為天空藍系

### 3.4 搜尋結果表格（index.html + item-*-row.html）
- `table-dark` thead → 改為 `thead` + 自訂背景 `var(--ms-blue-light)`，文字 `var(--ms-text-primary)`
- 表格邊框改 `var(--ms-border)`（Bootstrap `table-bordered`「繼承」）
- `table-hover` hover 顏色改淡藍 `#EEF6FA`
- 排序欄位 hover 加底線（`text-decoration: underline dotted`）
- 價格顏色套用新變數

### 3.5 結果卡片 / 空白提示
- 空搜尋提示區 → 加一個手繪風虛線框（`border: 2px dashed var(--ms-border)`，圓角）

### 3.6 按鈕統一
| 原 Bootstrap | 替換語義 | 新樣式 |
|---|---|---|
| `btn-primary` | 搜尋、儲存 | 天空藍實心 |
| `btn-outline-secondary` | 複製交易訊息 | 淡邊框 + 暖灰文字 |
| `btn-danger` | 刪除 | 保留紅色，調到 `#c0392b` |

### 3.7 後台 Dashboard（admin/dashboard.html）
- 統計徽章（在庫/售完）改為無色底、加色彩左邊框（`border-left: 3px solid`）
- 表格同前臺樣式統一

### 3.8 後台表單（admin/item-form.html）
- card 白底改 `var(--ms-bg-card)`
- label 顏色改 `var(--ms-text-muted)`
- 輸入框 focus：outline 天空藍

### 3.9 Footer
- 背景改 `var(--ms-bg-panel)`，加 `border-top: 1px solid var(--ms-border)`

---

## 4. 字體

```html
<!-- 加入 layout.html <head> -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500&display=swap" rel="stylesheet">
```

```css
body {
  font-family: 'Noto Sans TC', 'PingFang TC', 'Microsoft JhengHei', sans-serif;
  font-weight: 400;
}
h5, h6, .fw-bold, .fw-semibold { font-weight: 500; }
```

---

## 5. 實作範圍與順序

| 優先 | 工作項目 | 影響檔案 |
|------|----------|----------|
| P1 | CSS 變數 + 全域 reset | `custom.css` |
| P1 | 導覽列樣式 | `layout.html` (class 微調) |
| P1 | 表格 thead 改淺藍 | `index.html` → 移除 `table-dark` |
| P2 | 篩選側欄、按鈕覆蓋 | `custom.css` |
| P2 | 後台頁面統一 | `dashboard.html`、`item-form.html` |
| P3 | 字體引入 | `layout.html` |
| P3 | 空白提示虛線框 | `index.html` |

---

## 6. 不改動的部分

- Bootstrap 格線（col-*、container-fluid）
- HTML 結構與 Thymeleaf fragment 安排
- JavaScript 邏輯
- 所有 Java 後端程式碼

---

## 7. 視覺對照（文字描述）

```
Before:
  ┌─────────────────────────────────────────┐
  │ ██ 深色導覽列 (bg-dark)                  │
  ├─────────┬───────────────────────────────┤
  │ 白卡片   │ 純白表格，深藍 thead (table-dark)│
  │ 篩選欄   │ Bootstrap 預設間距              │
  └─────────┴───────────────────────────────┘

After:
  ┌─────────────────────────────────────────┐
  │ ░ 天空藍導覽列 (#4A8FAB)                 │
  ├─────────┬───────────────────────────────┤
  │ 米黃卡片  │ 米白表格，淡藍 thead (#D9EEF5) │
  │ 篩選欄   │ 細線邊框、暖灰輔助文字           │
  └─────────┴───────────────────────────────┘
```

---

## 8. 執行紀錄

**執行日期**：2026-03-19  
**最終建構狀態**：`compileJava BUILD SUCCESSFUL`

### 8.1 各優先項完成情況

| 優先 | 工作項目 | 影響檔案 | 狀態 |
|------|----------|----------|------|
| P1 | CSS 變數 + 全域 reset | `custom.css` | ✅ 完成 |
| P1 | 導覽列樣式 | `layout.html` | ✅ 完成 |
| P1 | 表格 thead 改淺藍 | `index.html` | ✅ 完成 |
| P2 | 篩選側欄、按鈕覆蓋 | `custom.css` | ✅ 完成 |
| P2 | 後台頁面統一 | `dashboard.html`、`item-form.html` | ✅ 完成 |
| P3 | 字體引入 Noto Sans TC | `layout.html` | ✅ 完成 |
| P3 | 空白提示虛線框 | `index.html` | ✅ 完成 |

### 8.2 計畫外實作項目

#### 表格欄標題中文化
所有搜尋結果表格欄位標題（名稱、數量、價格、賣家、位置、屬性等）全部改為中文顯示。

#### Sort JS 統一重構
原本 sort 邏輯只針對裝備表格，重構為通用函式 `initSort(tableId)`：
- 裝備表（`equipTable`）與一般商品表（`generalTable`）共用同一套排序機制
- `<tr>` 加入 `data-name`、`data-subcat`、`data-qty`、`data-price-group`、`data-price-val`、`data-seller`、`data-location` 等屬性
- 排序方向切換（▲ / ▼）指示統一顯示於欄位標頭

#### 名稱欄樣式修正
- 移除 `fw-semibold`（CJK 字元在小字號加粗會模糊）
- 名稱 `<td>` 加上 `padding-left: .75rem`
- 套用 `font-size: .82rem` 使列表緊湊不擁擠

#### 排序指示符號統一
改以 `▲` / `▼` 純文字符號；懸停欄位標頭時以 `text-decoration: underline dotted` 提示可排序。
