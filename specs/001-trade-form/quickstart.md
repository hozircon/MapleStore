# 開發者快速入門指南：輕量化交易展示站

**功能**: `001-trade-form` | **日期**: 2026-03-18 | **規格文件**: [spec.md](spec.md)

---

## 一、環境需求

在開始之前，請確認本機已安裝以下工具：

| 工具 | 版本需求 | 確認指令 |
|---|---|---|
| Java JDK | 21（LTS） | `java -version` |
| Gradle | 8.x（或使用專案內建 `gradlew`） | `./gradlew --version` |
| Git | 任意現代版本 | `git --version` |
| Docker Desktop | 任意現代版本（執行 Testcontainers 整合測試時需要） | `docker --version` |

> **提醒**：本地開發環境使用 H2 記憶體資料庫，**不需要**安裝 MySQL 或啟動任何資料庫服務。MySQL 僅在正式環境部署與整合測試時使用。

---

## 二、取得原始碼

```bash
git clone <repository-url>
cd ms-web
```

若已在工作目錄中，請確認目前分支：

```bash
git checkout 001-trade-form
```

---

## 三、本機執行

### 3.1 以 H2 記憶體資料庫啟動（開發模式）

**macOS / Linux：**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Windows PowerShell：**
```powershell
.\gradlew.bat bootRun "--args=--spring.profiles.active=dev"
```

> **Windows 注意事項**：必須使用 `.\gradlew.bat`（非 `./gradlew`），且 `--args` 的值需以雙引號包裹；單引號在 PowerShell 中語義不同，會導致 Gradle 解析失敗。

> **若出現「Port 8080 was already in use」**：代表上一次的 Java 程序尚未結束。在 PowerShell 執行以下指令清除後再重新啟動：
> ```powershell
> # 找出並終止佔用 8080 的程序
> $pid = (netstat -ano | Select-String ":8080\s.*LISTENING").ToString().Trim().Split()[-1]
> Stop-Process -Id $pid -Force
> ```

應用程式啟動後，瀏覽以下位址：

| 功能 | 位址 |
|---|---|
| 買家搜尋主頁 | http://localhost:8080/ |
| 管理後台 | http://localhost:8080/admin |
| H2 資料庫主控台 | http://localhost:8080/h2-console |

**H2 主控台連線設定**：

| 設定項 | 值 |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:msshopdev` |
| User Name | `sa` |
| Password | （空白） |

> H2 主控台僅在 `dev` profile 下啟用（`spring.h2.console.enabled=true`）。正式環境的 `application-prod.yml` 中此設定為 `false`。

### 3.2 直接以 JAR 執行

**macOS / Linux：**
```bash
./gradlew bootJar
java -jar build/libs/ms-web-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**Windows PowerShell：**
```powershell
.\gradlew.bat bootJar
java -jar build/libs/ms-web-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## 四、樣本資料

### 4.1 自動載入（推薦）

`dev` profile 啟動時，Spring Boot 會自動執行 `src/main/resources/data.sql`，載入以下樣本資料集合，涵蓋搜尋與篩選功能測試所需的各種情境：

| 樣本類型 | 數量 | 說明 |
|---|---|---|
| 裝備（IN_STOCK） | 10 筆 | 含各種 STR/DEX/INT/LUK/ATK/MATK 組合，包含捲軸孔為 0、3、7 的情況 |
| 裝備（SOLD_OUT） | 3 筆 | 用於驗證已售完商品不出現在搜尋結果中 |
| 消耗品（IN_STOCK） | 5 筆 | 含次分類 scroll、material、ore |
| 裝飾（IN_STOCK） | 3 筆 | |
| 其他（IN_STOCK） | 2 筆 | |
| MESO 定價商品 | 8 筆 | 用於驗證貨幣排序：MESO 排最前 |
| CS 定價商品 | 8 筆 | |
| WS 定價商品 | 7 筆 | |
| featured = true | 2 筆 | 預留欄位，測試資料中標記以備未來使用 |

`data.sql` 路徑：`src/main/resources/data.sql`

### 4.2 手動新增樣本資料（Admin 介面）

也可透過管理後台手動新增測試資料：

1. 開啟瀏覽器，前往 http://localhost:8080/admin
2. 點選「新增商品」按鈕
3. 填寫商品資料後送出表單

### 4.3 搜尋功能測試快速腳本

在樣本資料載入後，可依以下步驟快速驗證核心搜尋情境：

**情境 A：裝備 ATK 門檻篩選**
1. 大類選擇「裝備」→ 展開進階篩選 → 攻擊力（ATK） >= `10`
2. 點擊搜尋 → 應僅顯示 `atk_bonus >= 10` 的在庫裝備

**情境 B：貨幣優先排序驗證**
1. 大類不設限，貨幣全選（MESO + CS + WS）
2. 點擊搜尋 → 確認結果依序出現：所有 MESO 商品在前，CS 商品居中，WS 商品在後；同貨幣群組內以價格由低至高排列

**情境 C：已售完商品不可見**
1. 搜尋任意關鍵字
2. 確認樣本資料中的 3 筆 `SOLD_OUT` 裝備未出現在結果中

**情境 D：空搜尋（無條件件）**
1. 不填寫任何搜尋條件，直接點擊搜尋
2. 應回傳所有在庫商品（依貨幣優先排序）

---

## 五、執行測試

### 5.1 所有測試（含整合測試）

執行整合測試前，請確認 Docker Desktop 已啟動（Testcontainers 需要 Docker 執行 MySQL 容器）。

```bash
# macOS / Linux
./gradlew test
# Windows PowerShell
.\gradlew.bat test
```

### 5.2 僅執行單元測試（不需 Docker）

```bash
# macOS / Linux
./gradlew test -PexcludeIntegration
# Windows PowerShell
.\gradlew.bat test -PexcludeIntegration
```

或使用標籤篩選（如 JUnit 5 TagExpression 設定於 `build.gradle`）：

```bash
# macOS / Linux
./gradlew test --tests "com.msshop.service.*" --tests "com.msshop.controller.*"
# Windows PowerShell
.\gradlew.bat test --tests "com.msshop.service.*" --tests "com.msshop.controller.*"
```

### 5.3 僅執行 Repository 整合測試（需 Docker）

```bash
# macOS / Linux
./gradlew test --tests "com.msshop.repository.*"
# Windows PowerShell
.\gradlew.bat test --tests "com.msshop.repository.*"
```

### 5.4 測試報告

測試完成後，HTML 報告位於：

```
build/reports/tests/test/index.html
```

以瀏覽器開啟查看各測試案例執行結果。

---

## 六、正式環境部署（MySQL 8）

### 6.1 設定環境變數

正式環境的資料庫連線資訊透過環境變數注入，不寫入原始碼：

```bash
export MSSHOP_DB_URL=jdbc:mysql://your-mysql-host:3306/msshop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Taipei
export MSSHOP_DB_USERNAME=msshop_user
export MSSHOP_DB_PASSWORD=your_secure_password
```

### 6.2 建立資料庫與執行 Schema

在首次部署前，手動在 MySQL 建立資料庫並執行 Hibernate 產生的 DDL（或使用 Flyway/Liquibase 管理，視後續迭代決策）：

```sql
CREATE DATABASE msshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 6.3 啟動正式環境

```bash
java -jar build/libs/ms-web-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

> `application-prod.yml` 中 `spring.jpa.hibernate.ddl-auto=validate`，Hibernate 啟動時僅驗證 Schema 而不自動建表，確保正式資料安全。
