# iRead Library｜圖書館館藏管理系統 Web 版

本專案為 **iRead Library 圖書館館藏管理系統 Web 版**，以 Spring Boot 打包成可直接執行的 `.jar` 檔。專案內主要包含資料庫 SQL、上傳檔案資料夾 `uploads/`，以及已打包完成的後端與前端整合執行檔。

> 本 Repository 不放完整原始碼，主要用於展示與部署 Web 版系統。

---

## 專案內容

```text
library-system/
├── iread.jar          # Spring Boot 可執行 JAR，內含 Web 前端頁面與後端 API
├── sql/               # MySQL 資料庫 schema / demo data
├── uploads/           # 書籍封面或上傳檔案存放資料夾
└── README.md
```

---

## 系統簡介

iRead Library 是一套前後端整合的圖書館館藏管理系統，提供訪客查詢館藏、讀者借閱與預約、管理員維護書目與館藏等功能。系統將「書目資料」與「實體館藏冊本」分離設計，因此同一本書可以對應多本實體館藏，借閱、歸還、毀損、遺失等狀態皆以單一本實體館藏為單位管理。

本 Web 版已將前端頁面打包進 Spring Boot JAR 的靜態資源中，啟動 JAR 後即可透過瀏覽器操作系統，不需另外啟動 VS Code Live Server。

---

## 技術架構

| 類別 | 技術 |
|---|---|
| 後端 | Java、Spring Boot、Spring Data JPA、REST API |
| 前端 | HTML、CSS、JavaScript、AngularJS 1.x 風格 |
| 資料庫 | MySQL |
| 認證 | JWT Token |
| 檔案 | uploads 資料夾存放封面與匯入相關檔案 |
| 打包 | Spring Boot Executable JAR |

---

## 主要功能

### 訪客功能

- 查看首頁館藏統計
- 查看本月熱門借閱 Top 5
- 查詢館藏書籍
- 查看書籍詳情

### 讀者功能

- 讀者註冊與登入
- 查詢館藏與查看可借數量
- 借閱書籍
- 預約書籍
- 查看目前借閱紀錄
- 申請歸還
- 查看預約紀錄
- 查看站內訊息與未讀提醒
- 查看個人資料

### 管理員功能

- 管理員登入
- 後台統計摘要
- 書目新增、查詢、修改、上架 / 下架
- 實體館藏新增、查詢、修改與狀態管理
- 借閱紀錄查詢
- 歸還審核，包含正常歸還、毀損、遺失
- 預約管理與可取通知
- 使用者管理、啟用 / 停用帳號、開通借書權限
- Excel 借閱統計報表下載
- Excel 書目與館藏批次匯入

---

## 核心設計重點

### 1. 書目與實體館藏分離

系統將書籍基本資料與實體館藏冊本分開管理：

```text
books        ：書名、作者、ISBN、出版社、分類、封面等書目資料
book_copies  ：每一本實體館藏的條碼、位置、狀態
```

例如同一本書有 3 本館藏時，系統會建立 1 筆書目資料與 3 筆實體館藏資料。借閱時只會借出其中一本可借館藏，不會影響同書其他可借冊本。

### 2. 權限與登入驗證

系統以 JWT Token 驗證登入狀態，使用者角色分為：

| 角色 | 說明 |
|---|---|
| 訪客 | 可查詢公開館藏與首頁統計 |
| 讀者 | 可借閱、預約、申請歸還、查看訊息 |
| 管理員 | 可進入後台管理書目、館藏、借閱、預約與使用者 |

### 3. 借閱與歸還流程

借閱流程會檢查讀者身分、帳號狀態、借書權限、借閱上限與可借館藏。歸還採「讀者申請、管理員審核」模式，避免讀者申請後館藏直接變回可借。

### 4. 通知與逾期處理

系統支援站內訊息通知，並可搭配 Email 通知處理預約可取、到期提醒與逾期提醒。逾期 7 日以上可自動暫停讀者借書權限，管理員可於後台重新開通。

---

## 執行方式

### 1. 確認 Java 環境

建議使用 JDK 執行，先確認 Java 是否可用：

```bash
java -version
```

若 Windows 環境有多個 Java 版本，請確認 `where java` 第一個路徑指向正確的 JDK。

```bash
where java
```

---

### 2. 建立 MySQL 資料庫

請先啟動 MySQL，並依 `sql/` 資料夾內的 SQL 檔建立 schema 與測試資料。

範例流程：

```sql
CREATE DATABASE iread_library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

接著匯入 `sql/` 內提供的 schema / demo data。

---

### 3. 確認 uploads 資料夾

`uploads/` 用於存放書籍封面或匯入過程中使用的檔案。執行 JAR 時，請保留 `uploads/` 與 JAR 在專案預期的位置，避免封面路徑或檔案讀取失敗。

---

### 4. 啟動系統

在 JAR 所在資料夾執行：

```bash
java -jar iread.jar
```

若啟動成功，終端機會看到 Spring Boot / Tomcat 啟動訊息，例如：

```text
Tomcat started on port 8080
Started ...Application
```

---

## 系統入口

本專案的前端頁面已打包進 Spring Boot JAR，因此啟動 JAR 後請使用：

```text
http://localhost:8080/iread-library/index.html
```

---

## 展示重點

此專案適合展示以下 Web 後端與前後端整合能力：

- Spring Boot JAR 部署
- REST API 串接前端頁面
- JWT 登入驗證與角色權限控管
- MySQL 資料表設計與 JPA 操作
- 書目與實體館藏分離的資料建模
- 借閱、預約、歸還審核等完整業務流程
- Excel 匯入 / 匯出功能
- 圖書館管理後台與讀者端功能整合
