# 圖書館館藏管理系統需求文件

> 版本：v1.1  
> 修訂依據：新版 SQL 設計  
> 修訂重點：  
> 1. 移除所有讀書心得功能。  
> 2. 保留訊息通知功能。  
> 3. 新增 `book_copies` 後端表格，支援「同一本書有多本館藏」的借閱邏輯。  
> 4. 使用者與管理員統一放在 `users` 表，以 `role` 區分。  
> 5. 書目主檔使用 `books`，不再使用 `book_titles` 命名。  
> 6. 只有紀錄型表格使用 `AUTO_INCREMENT`。  
> 7. 非紀錄型主資料表 ID 由後端 `IdGenerator` 產生。  
> 8. 書籍分類直接引用中文圖書分類法代碼，例如 `000`、`100`、`900`。  
> 9. 系統規則不建立資料表，固定寫在 Service 中。  
>
> 後端：Eclipse + Spring Boot  
> 前端：VSCode + Angular 6+，可少量搭配 jQuery  
> 資料庫：MySQL

---

## 1. 專案概述

### 1.1 專案名稱

圖書館館藏管理系統

### 1.2 專案目標

本系統目標是將原本桌面式 Java 圖書館管理系統改寫為前後端分離的 Web 系統，使讀者可以在線上查詢館藏、借閱書籍、申請歸還、預約書籍與查看通知；管理員可以維護書籍資料、管理每一本實體館藏、審核歸還、管理使用者資料，訊息由系統事件自動產生，例如借閱成功、歸還審核、預約通知與逾期提醒。

### 1.3 系統設計理念

| 設計理念 | 說明 |
|---|---|
| 書目與實體館藏分離 | 同一本書可能有多本實體館藏，因此系統不應只用一本書一筆資料表示。新版設計將「書籍基本資料」放在 `books`，將每一本可借閱的實體書放在 `book_copies`。 |
| 借閱狀態以實體書為單位 | 借書、還書、毀損、遺失都應綁定到某一本 `book_copy`，避免同一本書有多本時狀態互相干擾。 |
| 預約以書目為單位 | 預約時讀者通常是預約某一本書，而不是指定某一本實體館藏，因此 `reservations` 綁定 `book_id`。 |
| 使用者與管理員統一設計 | 讀者與管理員皆存放於 `users`，以 `role` 區分 `READER` 與 `ADMIN`，降低帳號管理複雜度。 |
| 主資料 ID 由後端控制 | `users`、`books`、`book_copies` 等主資料表 ID 由後端 `IdGenerator` 產生，方便使用固定格式，例如 `U00000001`、`BK00000001`、`CP00000001`。 |
| 紀錄型資料自動編號 | `borrow_records`、`reservations`、`messages` 屬於流水紀錄，使用 `AUTO_INCREMENT`。 |
| 分類採中文圖書分類法 | 書籍分類直接引用中文圖書分類法代碼，例如 `000`、`100`、`900`，本系統先不混用西洋圖書分類。 |
| 前後端分離 | 後端提供 REST API，前端 Angular 負責頁面與互動。 |
| 保留訊息通知機制 | 借閱成功、歸還審核、預約到書、逾期提醒等事件可透過 `messages` 通知讀者。 |
| 移除讀書心得功能 | 原系統中的 `reviews`、`review_drafts`、心得審核、公開心得等功能全部不納入新版系統。 |

---

## 2. 系統範圍

| 模組 | 說明 |
|---|---|
| 登入功能 | 讀者與管理員登入，帳號來源皆為 `users`。 |
| 使用者資料管理 | 管理員維護使用者資料；讀者查看個人資料。 |
| 書籍資料管理 | 書目資料使用 `books`，實體館藏使用 `book_copies`。 |
| 書籍分類 | 使用 `book_categories`，分類 ID 採中文圖書分類法代碼。 |
| 借閱功能 | 借閱對象改為 `book_copies.copy_id`。 |
| 歸還申請與審核 | 讀者提出歸還申請，管理員確認歸還與書況。 |
| 預約功能 | 預約對象為 `books.book_id`。 |
| 訊息通知 message | 借閱、歸還、預約、系統通知使用。 |
| Excel 匯入匯出 | 若時間不足，可列為第二階段。 |

---

## 3. 使用者角色

| 角色 | 說明 | `users.role` | 主要權限 |
|---|---|---|---|
| 訪客 | 未登入使用者 | 無 | 可查看公開館藏查詢頁，不能借閱與預約。 |
| 讀者 | 已註冊並登入的使用者 | `READER` | 查詢館藏、借書、預約、申請歸還、查看借閱紀錄、查看訊息。 |
| 管理員 | 圖書館後台管理者 | `ADMIN` | 維護書目與館藏、管理使用者、審核歸還、處理預約、查看借閱紀錄。 |

---

## 4. 功能需求總表

### 4.1 讀者端功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-R-01 | 讀者登入 | 讀者使用帳號密碼登入系統。 | 高 |
| FR-R-02 | 查詢館藏 | 依書名、作者、ISBN、出版社、中文圖書分類法分類查詢書籍。 | 高 |
| FR-R-03 | 查看書籍詳情 | 查看書籍基本資料、館藏總數、可借數量、目前可借館藏。 | 高 |
| FR-R-04 | 借閱書籍 | 當有可借館藏時，讀者可借閱一本實體館藏。 | 高 |
| FR-R-05 | 查看目前借閱 | 讀者查看自己尚未完成歸還的借閱紀錄。 | 高 |
| FR-R-06 | 申請歸還 | 讀者針對借閱紀錄提出歸還申請，等待管理員審核。 | 高 |
| FR-R-07 | 預約書籍 | 當書籍沒有可借館藏，讀者可預約該書目。 | 中 |
| FR-R-08 | 查看預約紀錄 | 讀者查看自己的預約狀態。 | 中 |
| FR-R-09 | 查看訊息 | 讀者查看系統訊息與通知。 | 高 |
| FR-R-10 | 標記訊息已讀 | 讀者可將訊息標記為已讀。 | 中 |
| FR-R-11 | 刪除訊息 | 讀者可刪除自己的訊息。 | 低 |
| FR-R-12 | 查看個人資料 | 讀者查看姓名、電話、Email 等資訊。 | 中 |

### 4.2 管理員端功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-A-01 | 管理員登入 | 管理員使用帳號密碼登入後台。 | 高 |
| FR-A-02 | 新增書目 | 建立一本書的基本資料，例如 ISBN、書名、作者、出版社、分類。 | 高 |
| FR-A-03 | 編輯書目 | 修改書籍基本資料。 | 高 |
| FR-A-04 | 新增館藏冊本 | 在既有書目下新增一本或多本實體館藏。 | 高 |
| FR-A-05 | 編輯館藏冊本 | 修改館藏條碼、館藏狀態、位置等。 | 高 |
| FR-A-06 | 館藏狀態管理 | 可將實體館藏標記為可借、借出中、歸還待審核、毀損、遺失、下架。 | 高 |
| FR-A-07 | 使用者資料管理 | 新增、查詢、修改讀者與管理員資料。 | 高 |
| FR-A-08 | 借閱紀錄管理 | 查看全體借閱紀錄，依使用者、書籍、狀態篩選。 | 高 |
| FR-A-09 | 歸還審核 | 針對讀者歸還申請確認書況，並更新館藏狀態。 | 高 |
| FR-A-10 | 預約管理 | 查看預約清單，當有可借館藏時通知讀者。 | 中 |
| FR-A-11 | 訊息管理 | 系統觸發訊息。(確認應放置位置) | 中 |
| FR-A-12 | Excel 匯入書籍 | 批次匯入書目與館藏資料。 | 低 |

---

## 5. 核心業務規則

### 5.1 ID 規則

| 規則編號 | 規則內容 |
|---|---|
| BR-ID-01 | 只有紀錄型資料表使用資料庫 `AUTO_INCREMENT`。 |
| BR-ID-02 | `borrow_records.borrow_id`、`reservations.reservation_id`、`messages.message_id` 使用 `AUTO_INCREMENT`。 |
| BR-ID-03 | `users.user_id` 由後端 `IdGenerator` 產生，建議格式為 `U00000001`。 |
| BR-ID-04 | `books.book_id` 由後端 `IdGenerator` 產生，建議格式為 `BK00000001`。 |
| BR-ID-05 | `book_copies.copy_id` 由後端 `IdGenerator` 產生，建議格式為 `CP00000001`。 |
| BR-ID-06 | `book_categories.category_id` 不由 `IdGenerator` 產生，直接使用中文圖書分類法代碼，例如 `000`、`100`、`900`。 |

### 5.2 書目與館藏規則

| 規則編號 | 規則內容 |
|---|---|
| BR-01 | `books` 儲存書籍基本資料，例如 ISBN、書名、作者、出版社、出版年、分類。 |
| BR-02 | `book_copies` 儲存每一本實體館藏，每本實體館藏必須隸屬於一筆 `books`。 |
| BR-03 | 同一本書若有 3 本館藏，應為 1 筆 `books` + 3 筆 `book_copies`。 |
| BR-04 | 借閱、歸還、毀損、遺失等狀態必須更新在 `book_copies.copy_status`。 |
| BR-05 | 書籍查詢頁以 `books` 為主，顯示該書的總館藏數與可借館藏數。 |
| BR-06 | 書籍分類使用 `book_categories.category_id`，其值為中文圖書分類法代碼。 |

### 5.3 借閱規則

| 規則編號 | 規則內容 |
|---|---|
| BR-07 | 讀者借書時，系統必須檢查使用者是否存在、角色是否為 `READER`，且帳號狀態正常。 |
| BR-08 | 系統必須檢查讀者目前借閱數是否超過 Service 中設定的上限。 |
| BR-09 | 只有 `copy_status = AVAILABLE` 的館藏可以被借閱。 |
| BR-10 | 借閱成功後，建立一筆 `borrow_records`，並將該館藏狀態改為 `BORROWED`。 |
| BR-11 | 借閱成功後，系統可新增一筆 `messages` 通知讀者借閱成功與到期日。 |
| BR-12 | 若同書目仍有其他可借館藏，其他讀者仍可借閱同一本書的其他冊本。 |
| BR-13 | 若同書目沒有任何可借館藏，讀者只能預約，不能借閱。 |

### 5.4 歸還規則

| 規則編號 | 規則內容 |
|---|---|
| BR-14 | 讀者只能對自己的借閱紀錄提出歸還申請。 |
| BR-15 | 讀者申請歸還後，`borrow_records.borrow_status` 改為 `RETURN_PENDING`，`book_copies.copy_status` 改為 `RETURN_PENDING`。 |
| BR-16 | 管理員審核歸還後，若書況正常，`borrow_records.borrow_status` 改為 `RETURNED`，`book_copies.copy_status` 改為 `AVAILABLE`。 |
| BR-17 | 管理員審核歸還後，若書籍毀損或遺失，`borrow_records.borrow_status` 改為 `DAMAGED` 或 `LOST`，`book_copies.copy_status` 改為 `DAMAGED` 或 `LOST`。 |
| BR-18 | 歸還審核完成後，系統新增 `messages` 通知讀者審核結果。 |

### 5.5 預約規則

| 規則編號 | 規則內容 |
|---|---|
| BR-19 | 預約綁定 `books.book_id`，而不是綁定單一本 `book_copies.copy_id`。 |
| BR-20 | 同一位讀者不可重複預約同一本尚未完成的書目。 |
| BR-21 | 當館藏歸還並轉為可借時，系統可依預約順序通知第一位讀者。 |
| BR-22 | 預約通知可透過 `messages` 表建立通知。 |

### 5.6 訊息規則

| 規則編號 | 規則內容 |
|---|---|
| BR-23 | 訊息必須綁定接收者 `receiver_id`。 |
| BR-24 | 訊息類型可包含一般訊息、借閱通知、歸還通知、預約通知、逾期提醒。 |
| BR-25 | 讀者只能查看、標記、刪除自己的訊息。 |
| BR-26 | 訊息主要由系統事件自動產生，例如借閱成功、歸還審核、預約通知與逾期提醒；後台人員不作為個別訊息寄件者。 |

### 5.7 Service 固定規則

| 規則 | 建議位置 | 說明 |
|---|---|---|
| 借閱天數 | `BorrowServiceImpl` | 例如固定 14 天。 |
| 最多借閱本數 | `BorrowServiceImpl` | 例如固定 5 本。 |
| 預約保留時間 | `ReservationServiceImpl` | 例如固定 48 小時。 |
| 預約順位 | `ReservationServiceImpl` | 可依 `reservation_date` 排序。 |
| 系統訊息建立 | `MessageServiceImpl` | 借閱、歸還、預約事件建立訊息。 |

---

## 6. 資料庫設計

### 6.1 資料表總覽

| 資料表 | 說明 | 主鍵產生方式 |
|---|---|---|
| `users` | 使用者資料，包含讀者與管理員 | 後端 `IdGenerator` |
| `book_categories` | 中文圖書分類法分類 | 直接使用分類代碼 |
| `books` | 書目主檔 | 後端 `IdGenerator` |
| `book_copies` | 實體館藏冊本 | 後端 `IdGenerator` |
| `borrow_records` | 借閱紀錄 | `AUTO_INCREMENT` |
| `reservations` | 預約紀錄 | `AUTO_INCREMENT` |
| `messages` | 訊息通知 | `AUTO_INCREMENT` |

### 6.2 `users` 使用者資料表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `user_id` | VARCHAR(20), PK | 使用者 ID，由後端 `IdGenerator` 產生，例如 `U00000001` |
| `username` | VARCHAR(50), UNIQUE | 登入帳號 |
| `password` | VARCHAR(255) | 密碼，正式系統應儲存加密雜湊值 |
| `name` | VARCHAR(50) | 使用者姓名 |
| `email` | VARCHAR(100), UNIQUE | Email |
| `phone` | VARCHAR(20) | 電話 |
| `role` | VARCHAR(20) | `READER`、`ADMIN` |
| `status` | VARCHAR(20) | `ACTIVE`、`DISABLED` |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

### 6.3 `book_categories` 書籍分類表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `category_id` | VARCHAR(3), PK | 中文圖書分類法代碼，例如 `000`、`100`、`900` |
| `category_name` | VARCHAR(50), UNIQUE | 分類名稱 |
| `description` | VARCHAR(255) | 分類說明 |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

### 6.4 中文圖書分類法大類

| 分類代碼 | 分類名稱 |
|---|---|
| `000` | 總類 |
| `100` | 哲學類 |
| `200` | 宗教類 |
| `300` | 自然科學類 |
| `400` | 應用科學類 |
| `500` | 社會科學類 |
| `600` | 史地類 |
| `700` | 世界史地類 |
| `800` | 語文類 |
| `900` | 藝術類 |

### 6.5 `books` 書目主檔

| 欄位 | 型別 | 說明 |
|---|---|---|
| `book_id` | VARCHAR(20), PK | 書目 ID，由後端 `IdGenerator` 產生，例如 `BK00000001` |
| `category_id` | VARCHAR(3), FK | 對應 `book_categories.category_id` |
| `isbn` | VARCHAR(30) | ISBN |
| `title` | VARCHAR(150) | 書名 |
| `author` | VARCHAR(100) | 作者 |
| `publisher` | VARCHAR(100) | 出版社 |
| `publish_year` | INT | 出版年份 |
| `description` | TEXT | 書籍簡介 |
| `cover_url` | VARCHAR(500) | 封面圖片網址 |
| `status` | VARCHAR(20) | `ACTIVE`、`DISABLED` |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

### 6.6 `book_copies` 實體館藏表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `copy_id` | VARCHAR(20), PK | 館藏複本 ID，由後端 `IdGenerator` 產生，例如 `CP00000001` |
| `book_id` | VARCHAR(20), FK | 對應 `books.book_id` |
| `copy_code` | VARCHAR(50), UNIQUE | 每一本實體書的唯一條碼或館藏編號，例如 `B00000001` |
| `location` | VARCHAR(100) | 館藏位置，例如 A 區 1 櫃 |
| `copy_status` | VARCHAR(30) | `AVAILABLE`、`BORROWED`、`RETURN_PENDING`、`RESERVED`、`DAMAGED`、`LOST`、`REMOVED` |
| `note` | VARCHAR(255) | 館藏備註 |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

#### `copy_status` 狀態說明

| 狀態 | 中文 | 可否被借閱 | 說明 |
|---|---|---:|---|
| `AVAILABLE` | 可借閱 | 是 | 館藏可被讀者借出。 |
| `BORROWED` | 借出中 | 否 | 已被借出。 |
| `RETURN_PENDING` | 歸還待審核 | 否 | 讀者已申請歸還，等待管理員確認。 |
| `RESERVED` | 預約保留 | 否 | 已保留給預約讀者取書，可視專案時間決定是否實作。 |
| `DAMAGED` | 毀損 | 否 | 館藏毀損，不可借。 |
| `LOST` | 遺失 | 否 | 館藏遺失，不可借。 |
| `REMOVED` | 下架 | 否 | 館藏不顯示於讀者端。 |

### 6.7 `borrow_records` 借閱紀錄表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `borrow_id` | BIGINT, PK, AUTO_INCREMENT | 借閱紀錄 ID |
| `user_id` | VARCHAR(20), FK | 借閱者，對應 `users.user_id` |
| `copy_id` | VARCHAR(20), FK | 實際借出的館藏，對應 `book_copies.copy_id` |
| `borrow_date` | DATE | 借閱日期 |
| `due_date` | DATE | 到期日 |
| `return_request_date` | DATE | 讀者申請歸還日期 |
| `actual_return_date` | DATE | 管理員確認歸還日期 |
| `borrow_status` | VARCHAR(30) | `BORROWED`、`RETURN_PENDING`、`RETURNED`、`OVERDUE`、`LOST`、`DAMAGED` |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

### 6.8 `reservations` 預約紀錄表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `reservation_id` | BIGINT, PK, AUTO_INCREMENT | 預約 ID |
| `user_id` | VARCHAR(20), FK | 預約者，對應 `users.user_id` |
| `book_id` | VARCHAR(20), FK | 預約書目，對應 `books.book_id` |
| `reservation_date` | DATETIME | 預約時間 |
| `expire_date` | DATETIME | 保留到期時間 |
| `reservation_status` | VARCHAR(30) | `WAITING`、`AVAILABLE_NOTICE`、`COMPLETED`、`CANCELLED`、`EXPIRED` |
| `queue_order` | INT | 預約順位，可由後端計算或寫入 |
| `created_at` | DATETIME | 建立時間 |
| `updated_at` | DATETIME | 更新時間 |

### 6.9 `messages` 訊息通知表

| 欄位 | 型別 | 說明 |
|---|---|---|
| `message_id` | BIGINT, PK, AUTO_INCREMENT | 訊息 ID |
| `receiver_id` | VARCHAR(20), FK | 接收者，對應 `users.user_id` |
| `title` | VARCHAR(100) | 訊息標題 |
| `content` | TEXT | 訊息內容 |
| `message_type` | VARCHAR(30) | `GENERAL`、`BORROW`、`RETURN`、`RESERVATION`、`OVERDUE` |
| `is_read` | TINYINT(1) | 是否已讀 |
| `related_borrow_id` | BIGINT, FK, NULL | 關聯借閱紀錄 |
| `related_reservation_id` | BIGINT, FK, NULL | 關聯預約紀錄 |
| `created_at` | DATETIME | 建立時間 |
| `read_at` | DATETIME | 讀取時間 |

---

## 7. 主要使用案例

### UC-01 使用者登入

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者、管理員 |
| 前置條件 | 使用者已建立帳號。 |
| 主要流程 | 1. 使用者輸入帳號密碼。<br>2. 前端呼叫登入 API。<br>3. 後端查詢 `users` 並驗證帳號密碼。<br>4. 後端依 `role` 判斷為讀者或管理員。<br>5. 驗證成功後回傳使用者資訊與 token 或 session 資訊。<br>6. 前端依角色導向讀者首頁或管理員後台。 |
| 替代流程 | 帳號密碼錯誤時，系統回傳錯誤訊息。 |
| 後置條件 | 使用者可依角色操作對應功能。 |

### UC-02 查詢館藏

| 項目 | 說明 |
|---|---|
| 主要角色 | 訪客、讀者 |
| 前置條件 | 無。 |
| 主要流程 | 1. 使用者輸入書名、作者、ISBN 或分類。<br>2. 前端呼叫查詢 API。<br>3. 後端查詢 `books`，並統計每本書的總館藏數與可借數。<br>4. 前端顯示查詢結果。 |
| 替代流程 | 查無資料時顯示「無符合結果」。 |
| 後置條件 | 使用者可點入查看書籍詳情。 |

### UC-03 借閱書籍

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已登入，且該書有 `AVAILABLE` 館藏。 |
| 主要流程 | 1. 讀者在書籍詳情頁點選借閱。<br>2. 系統檢查讀者借閱上限。<br>3. 系統尋找該 `book_id` 下第一本 `AVAILABLE` 的 `book_copy`。<br>4. 系統建立 `borrow_records`。<br>5. 系統將該 `book_copies.copy_status` 改為 `BORROWED`。<br>6. 系統建立借閱成功 `messages`。<br>7. 前端顯示借閱成功與到期日。 |
| 替代流程 A | 無可借館藏時，顯示可預約。 |
| 替代流程 B | 已達借閱上限時，拒絕借閱並顯示原因。 |
| 後置條件 | 該實體館藏不可再被其他讀者借閱。 |

### UC-04 申請歸還

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已有 `BORROWED` 狀態借閱紀錄。 |
| 主要流程 | 1. 讀者進入目前借閱頁。<br>2. 選擇要歸還的借閱紀錄。<br>3. 系統確認該借閱紀錄屬於目前登入讀者。<br>4. 系統將 `borrow_records.borrow_status` 改為 `RETURN_PENDING`。<br>5. 系統將 `book_copies.copy_status` 改為 `RETURN_PENDING`。<br>6. 前端顯示等待管理員審核。 |
| 替代流程 | 非本人借閱紀錄不可操作。 |
| 後置條件 | 管理員可在後台看到歸還待審核清單。 |

### UC-05 管理員審核歸還

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 存在 `RETURN_PENDING` 的借閱紀錄。 |
| 主要流程 | 1. 管理員進入歸還審核頁。<br>2. 查看待審核清單。<br>3. 選擇一筆紀錄並輸入書況。<br>4. 若書況正常，系統將借閱紀錄改為 `RETURNED`，館藏改為 `AVAILABLE`。<br>5. 若毀損或遺失，系統將借閱紀錄改為 `DAMAGED` 或 `LOST`，館藏改為 `DAMAGED` 或 `LOST`。<br>6. 系統建立歸還審核結果 `messages` 給讀者。 |
| 替代流程 | 找不到紀錄時回傳錯誤。 |
| 後置條件 | 借閱流程完成，或進入異常書況紀錄。 |

### UC-06 預約書籍

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已登入。 |
| 主要流程 | 1. 讀者在書籍詳情頁點選預約。<br>2. 系統檢查是否已有同書目未完成預約。<br>3. 系統建立 `reservations`，狀態為 `WAITING`。<br>4. 前端顯示預約成功。 |
| 替代流程 | 若已有未完成預約，系統拒絕重複預約。 |
| 後置條件 | 當該書有可借館藏時，可通知讀者取書或借閱。 |

### UC-07 查看與管理訊息

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已登入。 |
| 主要流程 | 1. 讀者進入訊息中心。<br>2. 系統查詢該讀者的 `messages` 清單。<br>3. 讀者可查看內容、標記已讀或刪除。 |
| 替代流程 | 無訊息時顯示空狀態。 |
| 後置條件 | 訊息讀取狀態被更新。 |

---

## 8. API 規格草案

> 建議後端統一使用 `/api` 作為 REST API 前綴。  
> 需要登入的 API 應在 Header 傳入：`Authorization: Bearer <token>`。  
> 若尚未實作 JWT，可先以 Session 或簡化 token 處理，但文件仍保留 Header 欄位，方便後續擴充。

### 8.1 Auth API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| POST | `/api/auth/login` | 無 | 使用者登入，依 `role` 判斷讀者或管理員 | body: username, password | token, userInfo |
| POST | `/api/auth/logout` | 已登入 | 登出 | Header Authorization | success message |

### 8.2 Book API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| GET | `/api/books` | 無 | 查詢書目清單 | query: keyword, categoryId, page, size | 書目清單、總筆數、可借數 |
| GET | `/api/books/{bookId}` | 無 | 查詢書目詳情 | path: bookId | 書目詳情、館藏統計、館藏清單 |
| POST | `/api/admin/books` | 管理員 | 新增書目 | Header Authorization; body: categoryId, isbn, title, author, publisher, publishYear, description, coverUrl | 新增後書目 |
| PUT | `/api/admin/books/{bookId}` | 管理員 | 修改書目 | Header Authorization; path: bookId; body: 書目欄位 | 修改後書目 |
| PATCH | `/api/admin/books/{bookId}/status` | 管理員 | 上架 / 下架書目 | Header Authorization; path: bookId; body: status | 修改後書目 |

### 8.3 Book Category API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| GET | `/api/book-categories` | 無 | 查詢中文圖書分類法分類 | 無 | 分類清單 |
| GET | `/api/book-categories/{categoryId}` | 無 | 查詢單一分類 | path: categoryId | 分類資料 |

### 8.4 Book Copy API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| GET | `/api/admin/book-copies` | 管理員 | 查詢館藏冊本 | Header Authorization; query: bookId, status, keyword | 館藏冊本清單 |
| GET | `/api/admin/book-copies/{copyId}` | 管理員 | 查詢單一館藏 | Header Authorization; path: copyId | 館藏詳情 |
| POST | `/api/admin/books/{bookId}/copies` | 管理員 | 在書目下新增館藏 | Header Authorization; path: bookId; body: copyCode, location, note | 新增後館藏 |
| POST | `/api/admin/books/{bookId}/copies/batch` | 管理員 | 批次新增多本館藏 | Header Authorization; path: bookId; body: copies[] | 新增結果 |
| PUT | `/api/admin/book-copies/{copyId}` | 管理員 | 修改館藏資訊 | Header Authorization; path: copyId; body: copyCode, location, copyStatus, note | 修改後館藏 |
| PATCH | `/api/admin/book-copies/{copyId}/status` | 管理員 | 修改館藏狀態 | Header Authorization; path: copyId; body: copyStatus | 修改後館藏 |

### 8.5 Borrow API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| POST | `/api/borrows` | 讀者 | 借閱書籍 | Header Authorization; body: bookId | 借閱紀錄、到期日、實體館藏資訊 |
| GET | `/api/borrows/me/current` | 讀者 | 查詢目前借閱 | Header Authorization | 借閱中與歸還待審核清單 |
| GET | `/api/borrows/me/history` | 讀者 | 查詢個人借閱歷史 | Header Authorization; query: borrowStatus, page, size | 借閱歷史 |
| POST | `/api/borrows/{borrowId}/return-request` | 讀者 | 申請歸還 | Header Authorization; path: borrowId | 更新後借閱紀錄 |
| GET | `/api/admin/borrows` | 管理員 | 查詢全部借閱紀錄 | Header Authorization; query: userId, borrowStatus, keyword, page, size | 借閱紀錄清單 |
| GET | `/api/admin/borrows/return-pending` | 管理員 | 查詢歸還待審核 | Header Authorization | 待審核清單 |
| POST | `/api/admin/borrows/{borrowId}/approve-return` | 管理員 | 審核歸還 | Header Authorization; path: borrowId; body: resultStatus, adminNote | 審核後結果 |

### 8.6 Reservation API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| POST | `/api/reservations` | 讀者 | 預約書籍 | Header Authorization; body: bookId | 預約紀錄 |
| GET | `/api/reservations/me` | 讀者 | 查詢我的預約 | Header Authorization | 預約清單 |
| PATCH | `/api/reservations/{reservationId}/cancel` | 讀者 | 取消預約 | Header Authorization; path: reservationId | success message |
| GET | `/api/admin/reservations` | 管理員 | 查詢所有預約 | Header Authorization; query: reservationStatus, bookId, userId | 預約清單 |
| PATCH | `/api/admin/reservations/{reservationId}/notify` | 管理員 | 通知讀者可取書 | Header Authorization; path: reservationId | message 與預約狀態 |

### 8.7 Message API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| GET | `/api/messages/me` | 讀者 | 查詢我的訊息 | Header Authorization; query: read, page, size | 訊息清單 |
| GET | `/api/messages/me/unread-count` | 讀者 | 查詢未讀數 | Header Authorization | unreadCount |
| PATCH | `/api/messages/{messageId}/read` | 讀者 | 標記已讀 | Header Authorization; path: messageId | success message |
| DELETE | `/api/messages/{messageId}` | 讀者 | 刪除訊息 | Header Authorization; path: messageId | success message |
| POST | `/api/admin/messages` | 管理員 | 管理員發送訊息 | Header Authorization; body: receiverId, title, content, messageType | message |

### 8.8 User API

| 方法 | 路徑 | 權限 | 說明 | 傳入值 | 回傳值 |
|---|---|---|---|---|---|
| GET | `/api/users/me` | 已登入 | 查看個人資料 | Header Authorization | userInfo |
| PUT | `/api/users/me` | 已登入 | 修改部分個人資料 | Header Authorization; body: phone, email | 更新後 userInfo |
| GET | `/api/admin/users` | 管理員 | 查詢使用者清單 | Header Authorization; query: keyword, role, status, page, size | 使用者清單 |
| POST | `/api/admin/users` | 管理員 | 新增使用者 | Header Authorization; body: username, password, name, email, phone, role, status | 新增後使用者 |
| PUT | `/api/admin/users/{userId}` | 管理員 | 修改使用者 | Header Authorization; path: userId; body: user fields | 修改後使用者 |

---

## 9. 前端頁面規劃

### 9.1 Angular 模組建議

| 模組 | 說明 | 主要頁面 |
|---|---|---|
| `auth` | 登入與登出 | login |
| `reader` | 讀者端功能 | reader-home、book-search、book-detail、my-borrows、my-reservations、message-center、profile |
| `admin` | 管理員端功能 | admin-dashboard、book-manage、book-copy-manage、user-manage、borrow-manage、return-review、reservation-manage、message-manage |
| `shared` | 共用元件 | navbar、sidebar、pagination、alert、confirm-dialog |
| `core` | 核心服務 | auth.service、api.service、token.interceptor、auth.guard |

### 9.2 讀者端頁面

| 頁面 | 路由 | 功能 |
|---|---|---|
| 登入頁 | `/login` | 使用者登入。 |
| 讀者首頁 | `/reader/home` | 顯示借閱摘要、未讀訊息、推薦入口。 |
| 館藏查詢頁 | `/books` | 查詢書籍、顯示可借數。 |
| 書籍詳情頁 | `/books/:id` | 顯示書目資料與館藏狀態，可借閱或預約。 |
| 我的借閱頁 | `/reader/borrows` | 查看目前借閱、申請歸還。 |
| 我的預約頁 | `/reader/reservations` | 查看與取消預約。 |
| 訊息中心 | `/reader/messages` | 查看、標記已讀、刪除訊息。 |
| 個人資料頁 | `/reader/profile` | 查看與修改基本資料。 |

### 9.3 管理員端頁面

| 頁面 | 路由 | 功能 |
|---|---|---|
| 後台首頁 | `/admin/dashboard` | 顯示今日借閱、待審核歸還、預約通知等摘要。 |
| 書目管理頁 | `/admin/books` | 新增、編輯、查詢書目。 |
| 館藏冊本管理頁 | `/admin/book-copies` | 新增、批次新增、編輯實體館藏。 |
| 使用者管理頁 | `/admin/users` | 新增、編輯、查詢使用者。 |
| 借閱紀錄管理頁 | `/admin/borrows` | 查看全部借閱紀錄。 |
| 歸還審核頁 | `/admin/return-review` | 審核讀者歸還申請。 |
| 預約管理頁 | `/admin/reservations` | 查看預約清單與通知讀者。 |
| 訊息管理頁 | `/admin/messages` | 發送或查看系統訊息。 |

### 9.4 jQuery 使用範圍建議

| 可使用情境 | 說明 |
|---|---|
| 舊版日期套件 | 若 Angular 6+ 不方便整合某些日期選擇套件，可少量使用 jQuery。 |
| 表格外掛 | 若使用 DataTables 類型套件，可由 jQuery 處理，但要注意與 Angular 生命週期整合。 |
| DOM 動畫 | 簡單展開、收合、提示效果可少量使用。 |

> 注意：主要資料綁定、API 呼叫、路由切換與表單驗證仍建議由 Angular 負責，不建議大量使用 jQuery 直接操作畫面狀態，避免資料不同步。

---

## 10. 後端架構規劃

### 10.1 Spring Boot 分層架構

| 層級 | 套件範例 | 職責 |
|---|---|---|
| Controller | `com.library.controller` | 接收 HTTP request，回傳 API response。 |
| Service | `com.library.service` | 處理業務邏輯，例如借閱上限、狀態轉換。 |
| Repository | `com.library.repository` | 使用 Spring Data JPA 存取資料庫。 |
| Entity | `com.library.entity` | 對應資料表。 |
| DTO | `com.library.dto` | 只有必要時定義 request / response 格式，避免過度建立類別。 |
| Mapper | `com.library.mapper` | Entity 與 DTO 轉換；若資料簡單可由 Service 直接組裝。 |
| Util | `com.library.util` | 放置 `IdGenerator` 等工具類別。 |
| Exception | `com.library.exception` | 統一錯誤處理。 |
| Config | `com.library.config` | CORS、安全設定、攔截器等。 |

### 10.2 後端 Class 清單建議

| 模組 | Entity | Repository | Service | Controller | DTO / Mapper |
|---|---|---|---|---|---|
| Auth | User | UserRepository | AuthService | AuthController | LoginRequest、LoginResponse |
| User | User | UserRepository | UserService | UserController、AdminUserController | UserRequest、UserResponse |
| Book Category | BookCategory | BookCategoryRepository | BookCategoryService | BookCategoryController | BookCategoryResponse |
| Book | Book | BookRepository | BookService | BookController、AdminBookController | BookRequest、BookResponse |
| Book Copy | BookCopy | BookCopyRepository | BookCopyService | AdminBookCopyController | BookCopyRequest、BookCopyResponse |
| Borrow | BorrowRecord | BorrowRecordRepository | BorrowService | BorrowController、AdminBorrowController | BorrowRequest、BorrowResponse、ApproveReturnRequest |
| Reservation | Reservation | ReservationRepository | ReservationService | ReservationController、AdminReservationController | ReservationRequest、ReservationResponse |
| Message | Message | MessageRepository | MessageService | MessageController、AdminMessageController | MessageRequest、MessageResponse |
| Common | 無 | 無 | 無 | GlobalExceptionHandler | ApiResponse、PageResponse |
| Util | 無 | 無 | 無 | 無 | IdGenerator |

### 10.3 IdGenerator 建議

| 目標 | 格式 | 範例 |
|---|---|---|
| 使用者 ID | `U` + 8 碼流水號 | `U00000001` |
| 書目 ID | `BK` + 8 碼流水號 | `BK00000001` |
| 館藏複本 ID | `CP` + 8 碼流水號 | `CP00000001` |
| 分類 ID | 中文圖書分類法代碼 | `000`、`400`、`900` |

---

## 11. 重要流程設計

### 11.1 借閱流程

```text
讀者點選借閱
→ 前端送出 bookId
→ 後端驗證讀者登入狀態
→ 檢查 users.role 是否為 READER
→ 檢查 users.status 是否為 ACTIVE
→ 檢查讀者借閱上限
→ 查詢該 bookId 下是否有 AVAILABLE 的 book_copy
→ 若有，建立 borrow_record
→ 更新 book_copy 狀態為 BORROWED
→ 建立 message：借閱成功
→ 回傳借閱紀錄與 dueDate
```

### 11.2 歸還流程

```text
讀者點選申請歸還
→ 後端確認 borrow_record 屬於該讀者
→ borrow_record.borrow_status = RETURN_PENDING
→ book_copy.copy_status = RETURN_PENDING
→ 管理員進入歸還審核頁
→ 管理員確認書況
→ 正常：borrow_record.borrow_status = RETURNED，book_copy.copy_status = AVAILABLE
→ 毀損：borrow_record.borrow_status = DAMAGED，book_copy.copy_status = DAMAGED
→ 遺失：borrow_record.borrow_status = LOST，book_copy.copy_status = LOST
→ 建立 message：歸還審核結果
```

### 11.3 預約通知流程

```text
讀者預約書目
→ reservations.reservation_status = WAITING
→ 某一本 book_copy 歸還並變為 AVAILABLE
→ 系統查詢該 book 的最早 WAITING 預約
→ 將預約狀態改為 AVAILABLE_NOTICE
→ 建立 message：可取書或可借閱通知
→ 若專案時間足夠，可將 book_copy 狀態暫改為 RESERVED
```

---

## 12. 驗收標準

| 編號 | 驗收項目 | 驗收條件 |
|---|---|---|
| AC-01 | 書目與館藏分離 | 同一本書可建立多本 `book_copies`，且每本有不同條碼。 |
| AC-02 | 查詢書籍可顯示可借數 | 書籍清單能顯示總館藏數與可借數。 |
| AC-03 | 借閱只影響一本實體館藏 | 借出一本 copy 後，同書其他 AVAILABLE copy 仍可借。 |
| AC-04 | 無可借館藏不能借閱 | 若所有 copy 都非 AVAILABLE，借閱 API 需拒絕並提示可預約。 |
| AC-05 | 歸還需管理員審核 | 讀者申請歸還後，館藏狀態為 RETURN_PENDING，不會直接變 AVAILABLE。 |
| AC-06 | 管理員可完成歸還審核 | 管理員審核後可將館藏改回 AVAILABLE 或改為 DAMAGED / LOST。 |
| AC-07 | 訊息功能正常 | 借閱成功、歸還審核結果可產生 message，讀者可查看。 |
| AC-08 | 讀書心得完全移除 | 前端無心得頁面，後端無 Review 相關 API，資料表不建立 reviews / review_drafts。 |
| AC-09 | 權限區分 | 讀者不可呼叫管理員 API，讀者只能查自己的借閱、預約、訊息。 |
| AC-10 | 前後端可串接 | Angular 可透過 REST API 完成查詢、借閱、歸還、訊息流程。 |
| AC-11 | ID 規則正確 | 只有 `borrow_records`、`reservations`、`messages` 使用 `AUTO_INCREMENT`。 |
| AC-12 | 中文圖書分類法正確 | `book_categories.category_id` 可使用 `000`、`100`、`900` 等分類代碼。 |

---

## 13. 第一階段開發優先順序

| 階段 | 目標 | 功能 |
|---|---|---|
| 第一階段 | 完成核心借閱閉環 | 登入、書籍查詢、`books`、`book_copies`、借閱、申請歸還、歸還審核、`messages`。 |
| 第二階段 | 補齊管理功能 | 使用者管理、館藏批次新增、預約管理、借閱歷史查詢。 |
| 第三階段 | 優化與擴充 | Excel 匯入、逾期提醒、自動通知、儀表板統計。 |

---

## 14. 從舊系統到新系統的改寫對照

| 舊系統項目 | 新系統對應 | 處理方式 |
|---|---|---|
| Swing UI Controller | Angular Component | 原 UI 邏輯改為前端頁面與服務。 |
| DAO / JDBC | Spring Data JPA Repository | 改用 Entity + Repository。 |
| ServiceImpl | Spring Boot Service | 保留業務邏輯概念，改為 Spring Bean。 |
| Readers / Admins | User | 合併成 `users`，用 `role` 區分。 |
| Books | Book + BookCopy | 拆分書目與實體館藏。 |
| BorrowRecords | BorrowRecord | 保留，但關聯改為 `copyId`。 |
| Messages | Message | 保留並強化 `messageType`、`title`、`receiverId`。 |
| Reviews / ReviewDrafts | 無 | 完全移除。 |
| AppException | Custom BusinessException | 改由 GlobalExceptionHandler 統一回應。 |

---

## 15. 建議專案目錄

### 15.1 後端 Spring Boot

```text
library-backend/
└── src/main/java/com/library/
    ├── LibraryApplication.java
    ├── config/
    ├── controller/
    │   ├── AuthController.java
    │   ├── BookController.java
    │   ├── BookCategoryController.java
    │   ├── BorrowController.java
    │   ├── MessageController.java
    │   ├── ReservationController.java
    │   ├── UserController.java
    │   └── admin/
    │       ├── AdminBookController.java
    │       ├── AdminBookCopyController.java
    │       ├── AdminBorrowController.java
    │       ├── AdminUserController.java
    │       ├── AdminReservationController.java
    │       └── AdminMessageController.java
    ├── dto/
    ├── entity/
    ├── exception/
    ├── mapper/
    ├── repository/
    ├── service/
    └── util/
        └── IdGenerator.java
```

### 15.2 前端 Angular

```text
library-frontend/
└── src/app/
    ├── core/
    │   ├── services/
    │   ├── guards/
    │   └── interceptors/
    ├── shared/
    │   └── components/
    ├── auth/
    ├── reader/
    │   ├── book-search/
    │   ├── book-detail/
    │   ├── my-borrows/
    │   ├── my-reservations/
    │   ├── message-center/
    │   └── profile/
    └── admin/
        ├── dashboard/
        ├── book-manage/
        ├── book-copy-manage/
        ├── user-manage/
        ├── borrow-manage/
        ├── return-review/
        ├── reservation-manage/
        └── message-manage/
```
