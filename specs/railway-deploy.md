# Railway 部署手冊

**應用程式**：楓葉交易站 (ms-web) | **日期**：2026-03-19

---

## 前置條件

- GitHub 帳號（Railway 透過 GitHub 部署）
- 專案已推送到 GitHub repository

---

## 一、把專案推上 GitHub

如果還沒有 GitHub repo，先建立：

1. 前往 https://github.com/new，建立新 repository（名稱例如 `maple-store`）
2. 在本機執行：

```powershell
cd e:\MapleStore
git remote add origin https://github.com/<你的帳號>/maple-store.git
git branch -M main
git push -u origin main
```

> **確認 `.gitignore` 正確**：`application-prod.yml`、`data/`、`resource/` 都不應上傳。

---

## 二、建立 Railway 帳號並連接 GitHub

1. 前往 https://railway.app → 點「Login」→ 選「Login with GitHub」
2. 授權 Railway 存取你的 GitHub

---

## 三、建立新專案

1. 點右上角「**New Project**」
2. 選「**Deploy from GitHub repo**」
3. 找到並選擇你的 `maple-store` repository
4. Railway 會自動偵測到 `railway.toml` 並開始嘗試首次 build

> **先不要急著讓它成功** — 接下來要先加 MySQL 和設定環境變數，首次 build 失敗沒關係。

---

## 四、加入 MySQL 資料庫

1. 在專案頁面點左上「**+ New**」→「**Database**」→「**Add MySQL**」
2. Railway 會建立一個 MySQL service 並自動產生連線資訊
3. 點擊剛建立的 MySQL service，進入「**Variables**」頁籤
4. 記下（或稍後複製）以下值：
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLDATABASE`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`

---

## 五、設定應用程式環境變數

1. 回到你的 **Spring Boot service**（不是 MySQL service）
2. 點「**Variables**」頁籤 → 點「**New Variable**」，逐一加入：

| 變數名稱 | 值（來源） |
|---|---|
| `DB_HOST` | 填入上面的 `MYSQLHOST` 值 |
| `DB_PORT` | 填入上面的 `MYSQLPORT` 值（通常是 `3306`） |
| `DB_NAME` | 填入上面的 `MYSQLDATABASE` 值 |
| `DB_USER` | 填入上面的 `MYSQLUSER` 值 |
| `DB_PASSWORD` | 填入上面的 `MYSQLPASSWORD` 值 |
| `ADMIN_USERNAME` | 你想要的後台帳號（例如 `admin`） |
| `ADMIN_PASSWORD` | 你想要的後台密碼（請設強一點） |

> **小技巧**：Railway 支援 reference variable，你可以直接用 `${{MySQL.MYSQLHOST}}` 的寫法來引用同一個專案內 MySQL service 的變數，不用手動複製貼上。

---

## 六、更新 application.yml 支援環境變數注入帳密

目前後台帳密寫在 `application.yml` 的 `app.admin` 區塊。要讓 Railway 可以覆蓋，需要確認 application-prod.yml 也有這個設定，或讓 Railway 環境變數直接注入。

編輯 `src/main/resources/application.yml`，把 admin 帳密改成支援環境變數：

```yaml
app:
  admin:
    username: ${ADMIN_USERNAME:admin}
    password: ${ADMIN_PASSWORD:maple2026}
```

這樣本機開發時沿用預設值，生產環境由 Railway 環境變數覆蓋。

---

## 七、觸發部署

環境變數設定完畢後：

1. Railway 通常會自動重新 deploy
2. 若沒有自動觸發，點「**Deploy**」→「**Redeploy**」

點「**Build Logs**」觀察建置過程，應看到：
```
./gradlew bootJar -x test
BUILD SUCCESSFUL
```

點「**Deploy Logs**」應看到：
```
Started MsWebApplication in X seconds
```

---

## 八、設定對外網址

1. 點你的 Spring Boot service → 「**Settings**」頁籤
2. 找到「**Networking**」→「**Generate Domain**」
3. Railway 會給你一個 `xxx.up.railway.app` 的網址

點進去確認首頁可以正常顯示。

---

## 九、驗證部署結果

| 項目 | 確認方式 |
|---|---|
| 首頁 | `https://xxx.up.railway.app/` 顯示搜尋頁面 |
| 後台登入 | `https://xxx.up.railway.app/admin` 導向登入頁 |
| 後台功能 | 輸入帳密後進入管理後台，可新增商品 |
| 資料持久 | 重新部署後，手動新增的資料依然存在 |

---

## 十、日後更新部署

往後每次 push 到 `main` branch，Railway 會**自動偵測並重新部署**：

```powershell
git add .
git commit -m "update: 你的說明"
git push origin main
```

Railway 會自動觸發 build → deploy，通常 2–3 分鐘完成。

---

## 費用參考

| 項目 | 免費額度 |
|---|---|
| 運算時間 | 每月 $5 美元額度（約 500 小時） |
| MySQL 儲存 | 1 GB |
| 流量 | 100 GB/月 |

30人次/天的流量完全在免費額度內。若超出，Spring Boot service 可設為「**Sleep when inactive**」以節省額度。

---

## 常見問題

**Q：Build 失敗，錯誤是 `Could not find or load main class`**
確認 `railway.toml` 的 JAR 路徑 `build/libs/ms-web-0.0.1-SNAPSHOT.jar` 與實際 `build.gradle` 的 `version` 和 `artifactId` 一致。

**Q：啟動後連不到資料庫**
到 Variables 頁籤確認 `DB_HOST`、`DB_USER`、`DB_PASSWORD` 都有正確填入。

**Q：想看即時 log**
點 service → 「**Observability**」→「**Logs**」可以即時追蹤。
