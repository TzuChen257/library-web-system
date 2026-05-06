# 圖書館館藏管理系統需求文件

> 版本：v1.2  
> 修訂依據：目前 Spring Boot 後端架構與 API 實作狀態  
> 後端：Eclipse + Spring Boot + Spring Data JPA + JWT + Scheduler + Mail  
> 前端：VSCode + Angular 6+，可少量搭配 jQuery  
> 資料庫：MySQL  
>
> 本版修訂重點：
> 1. 保留「書目 `books`」與「實體館藏 `book_copies`」分離設計。
> 2. 使用者與管理員統一存放於 `users`，以 `role` 區分 `READER`、`ADMIN`。
> 3. 新增讀者可自行註冊功能。
> 4. 採 JWT 登入驗證，前端以 `Authorization: Bearer <token>` 呼叫需登入 API。
> 5. `IdGenerator` 改為時間戳加隨機碼格式，避免依賴 `count()` 或查最大 ID。
> 6. 新增 `borrow_suspended`，區分「帳號停用」與「暫停借書權限」。
> 7. 新增 `NotificationService` 統一整合站內訊息與 Email 通知。
> 8. 預約可取、到期前一天、逾期當下、逾期 7 日通知支援站內訊息與 Mail。
> 9. 逾期 7 日後暫停讀者借書功能，管理員可重新開通。
> 10. DTO 採必要最小化原則：多欄位資料使用 Request DTO，單一或少量簡單欄位優先使用 `@PathVariable` / `@RequestParam`。
> 11. 移除所有讀書心得功能。
> 12. Excel 匯入匯出保留為後續擴充項目。

---

## 1. 專案概述

### 1.1 專案名稱

圖書館館藏管理系統

### 1.2 專案目標

本系統目標是將原本桌面式 Java Swing 圖書館管理系統改寫為前後端分離的 Web 系統。讀者可以在線上註冊、登入、查詢館藏、借閱書籍、申請歸還、預約書籍、查看訊息與接收逾期提醒；管理員可以維護書目資料、管理每一本實體館藏、審核歸還、管理使用者、處理預約通知與開通讀者借書權限。

系統通知以站內訊息為基礎，並針對時效性高的情境整合 Email，例如預約可取、到期前一天、逾期當下與逾期 7 日借書權限暫停通知。

### 1.3 系統設計理念

| 設計理念 | 說明 |
|---|---|
| 書目與實體館藏分離 | 同一本書可能有多本實體館藏，因此系統將書籍基本資料放在 `books`，將每一本可借閱實體書放在 `book_copies`。 |
| 借閱狀態以實體館藏為單位 | 借書、還書、毀損、遺失都綁定 `book_copies.copy_id`，避免同書多本館藏狀態互相干擾。 |
| 預約以書目為單位 | 讀者預約的是某一本書目，而不是指定某一本實體館藏，因此 `reservations` 綁定 `book_id`。 |
| 使用者與管理員統一設計 | 讀者與管理員皆存放於 `users`，以 `role` 區分 `READER` 與 `ADMIN`。 |
| 帳號狀態與借書權限分離 | `users.status` 控制帳號是否可登入；`borrow_suspended` 控制讀者是否可借書。 |
| 主資料 ID 由後端產生 | `users`、`books`、`book_copies` 由後端 `IdGenerator` 產生 ID，不依賴資料筆數或查最大 ID。 |
| 紀錄型資料自動編號 | `borrow_records`、`reservations`、`messages` 屬於流水紀錄，使用 `AUTO_INCREMENT`。 |
| 分類採中文圖書分類法 | 書籍分類直接引用中文圖書分類法代碼，例如 `000`、`100`、`900`。 |
| 前後端分離 | 後端提供 REST API，前端 Angular 負責頁面、路由、表單與互動。 |
| 通知服務集中化 | `NotificationService` 統一呼叫 `MessageService` 與 `MailService`，避免業務 Service 分散建立通知。 |
| DTO 最小化 | 只有多欄位或結構化資料使用 Request DTO；單一或少量簡單欄位使用 `@PathVariable` 或 `@RequestParam`。 |
| 移除讀書心得功能 | 原系統中的心得、草稿、審核、公開心得全部不納入新版系統。 |

---

## 2. 系統範圍

| 模組 | 說明 |
|---|---|
| Auth 認證 | 讀者註冊、讀者與管理員登入、登出、JWT 驗證。 |
| 使用者資料管理 | 管理員新增、查詢、修改、停用使用者；讀者查看個人資料。 |
| 書籍分類 | 使用 `book_categories`，分類 ID 採中文圖書分類法代碼。 |
| 書目管理 | 書目資料使用 `books`，包含書名、作者、ISBN、出版社、出版年、分類、封面等。 |
| 實體館藏管理 | 實體館藏使用 `book_copies`，支援同一本書多本館藏。 |
| 借閱功能 | 讀者借閱書籍時，系統自動挑選一本可借館藏。 |
| 歸還申請與審核 | 讀者提出歸還申請，管理員審核正常、毀損或遺失。 |
| 預約功能 | 無可借館藏時，讀者可預約書目；管理員可通知讀者可借。 |
| 訊息通知 | 借閱、歸還、預約、逾期等系統事件建立站內訊息。 |
| Email 通知 | 預約可取、到期前一天、逾期當下、逾期 7 日通知以 Email 強化提醒。 |
| 逾期排程 | 每日排程檢查到期與逾期紀錄，逾期 7 日自動暫停借書。 |
| Excel 匯入匯出 | 可列為後續擴充。 |

---

## 3. 使用者角色

| 角色 | 說明 | `users.role` | 主要權限 |
|---|---|---|---|
| 訪客 | 未登入使用者 | 無 | 可查看公開館藏查詢頁與書籍詳情，不能借閱、預約或查看個人資料。 |
| 讀者 | 已註冊並登入的使用者 | `READER` | 查詢館藏、借書、預約、申請歸還、查看借閱紀錄、查看訊息、查看個人資料。 |
| 管理員 | 圖書館後台管理者 | `ADMIN` | 維護書目、管理館藏、管理使用者、審核歸還、處理預約、查看借閱與預約紀錄。 |

---

## 4. 功能需求總表

### 4.1 Auth / 共用功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-C-01 | 讀者註冊 | 讀者可自行註冊帳號，預設角色為 `READER`，狀態為 `ACTIVE`。 | 高 |
| FR-C-02 | 使用者登入 | 讀者與管理員使用帳號密碼登入，成功後取得 JWT token。 | 高 |
| FR-C-03 | 使用者登出 | 前端呼叫登出 API 後清除 token。 | 中 |
| FR-C-04 | 查看個人資料 | 已登入使用者可查看自己的基本資料。 | 中 |

### 4.2 讀者端功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-R-01 | 查詢館藏 | 依書名、作者、ISBN 查詢書籍，並顯示館藏總數與可借數。 | 高 |
| FR-R-02 | 查看書籍詳情 | 查看書籍基本資料、分類、館藏統計與館藏狀態。 | 高 |
| FR-R-03 | 借閱書籍 | 當有可借館藏且讀者未被暫停借書時，可借閱一本實體館藏。 | 高 |
| FR-R-04 | 查看目前借閱 | 讀者查看自己尚未完成歸還的借閱紀錄。 | 高 |
| FR-R-05 | 申請歸還 | 讀者針對自己的借閱紀錄提出歸還申請。 | 高 |
| FR-R-06 | 預約書籍 | 當書籍沒有可借館藏或讀者需要排隊時，可預約該書目。 | 中 |
| FR-R-07 | 查看預約紀錄 | 讀者查看自己的預約狀態。 | 中 |
| FR-R-08 | 取消預約 | 讀者可取消自己尚未完成的預約。 | 中 |
| FR-R-09 | 查看訊息 | 讀者查看系統訊息與通知。 | 高 |
| FR-R-10 | 標記訊息已讀 | 讀者可將自己的訊息標記為已讀。 | 中 |
| FR-R-11 | 刪除訊息 | 讀者可刪除自己的訊息。 | 低 |

### 4.3 管理員端功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-A-01 | 新增書目 | 建立一本書的基本資料。 | 高 |
| FR-A-02 | 編輯書目 | 修改書籍基本資料。 | 高 |
| FR-A-03 | 書目狀態管理 | 可將書目設為 `ACTIVE` 或 `DISABLED`。 | 高 |
| FR-A-04 | 新增館藏冊本 | 在既有書目下新增一本實體館藏。 | 高 |
| FR-A-05 | 編輯館藏冊本 | 修改館藏條碼、位置、狀態、備註。 | 高 |
| FR-A-06 | 館藏狀態管理 | 可將館藏標記為可借、借出中、歸還待審核、毀損、遺失、下架。 | 高 |
| FR-A-07 | 使用者管理 | 新增、查詢、修改讀者與管理員資料。 | 高 |
| FR-A-08 | 使用者狀態管理 | 管理員可啟用或停用帳號。 | 高 |
| FR-A-09 | 開通借書權限 | 管理員可解除讀者因逾期造成的借書暫停狀態。 | 中 |
| FR-A-10 | 借閱紀錄管理 | 查看全體借閱紀錄，可依狀態篩選。 | 高 |
| FR-A-11 | 歸還審核 | 針對讀者歸還申請確認書況並更新狀態。 | 高 |
| FR-A-12 | 預約管理 | 查看預約清單，並通知讀者預約書籍可借。 | 中 |
| FR-A-13 | Excel 匯入書籍 | 批次匯入書目與館藏資料。 | 低 |

### 4.4 系統背景功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-S-01 | 到期前一天通知 | 系統每日檢查隔日到期紀錄，建立站內訊息並寄 Email。 | 中 |
| FR-S-02 | 逾期當下通知 | 系統每日檢查已逾期紀錄，將狀態改為 `OVERDUE` 並通知讀者。 | 中 |
| FR-S-03 | 逾期 7 日停權 | 系統每日檢查逾期 7 日以上紀錄，暫停讀者借書功能並通知。 | 中 |
| FR-S-04 | 預約可取通知 | 管理員通知預約可借時，建立站內訊息並寄 Email。 | 中 |

---

## 5. 核心業務規則

### 5.1 ID 規則

| 規則編號 | 規則內容 |
|---|---|
| BR-ID-01 | 只有紀錄型資料表使用資料庫 `AUTO_INCREMENT`。 |
| BR-ID-02 | `borrow_records.borrow_id`、`reservations.reservation_id`、`messages.message_id` 使用 `AUTO_INCREMENT`。 |
| BR-ID-03 | `users.user_id`、`books.book_id`、`book_copies.copy_id` 由後端 `IdGenerator` 產生。 |
| BR-ID-04 | 目前 ID 格式採 `prefix + yyyyMMddHHmmss + 4碼隨機碼`，確保長度不超過 `VARCHAR(20)`。 |
| BR-ID-05 | 使用者 ID 範例：`U20260506123456A1B2`。 |
| BR-ID-06 | 書目 ID 範例：`BK20260506123456A1B2`。 |
| BR-ID-07 | 館藏 ID 範例：`CP20260506123456A1B2`。 |
| BR-ID-08 | `book_categories.category_id` 不由 `IdGenerator` 產生，直接使用中文圖書分類法代碼，例如 `000`、`100`、`900`。 |

### 5.2 使用者與權限規則

| 規則編號 | 規則內容 |
|---|---|
| BR-U-01 | 讀者註冊後預設 `role = READER`，`status = ACTIVE`，`borrow_suspended = false`。 |
| BR-U-02 | `users.status` 控制帳號是否可登入，允許值為 `ACTIVE`、`DISABLED`。 |
| BR-U-03 | `users.borrow_suspended` 控制讀者是否可借書，值為 `true` 時仍可登入、查訊息、申請歸還，但不可借書。 |
| BR-U-04 | 管理員 API 必須驗證目前登入者為 `ADMIN`。 |
| BR-U-05 | 讀者借閱、預約、申請歸還等 API 必須驗證目前登入者為 `READER`。 |
| BR-U-06 | 「我的資料」、「我的訊息」類 API 必須驗證登入者身分，但不一定限制角色。 |

### 5.3 書目與館藏規則

| 規則編號 | 規則內容 |
|---|---|
| BR-B-01 | `books` 儲存書籍基本資料，例如 ISBN、書名、作者、出版社、出版年、分類。 |
| BR-B-02 | `book_copies` 儲存每一本實體館藏，每本實體館藏必須隸屬於一筆 `books`。 |
| BR-B-03 | 同一本書若有 3 本館藏，應為 1 筆 `books` + 3 筆 `book_copies`。 |
| BR-B-04 | 借閱、歸還、毀損、遺失、下架等狀態必須更新在 `book_copies.copy_status`。 |
| BR-B-05 | 書籍查詢頁以 `books` 為主，顯示總館藏數與可借館藏數。 |
| BR-B-06 | `copy_code` 必須唯一，用於辨識實體館藏條碼或館藏編號。 |

### 5.4 借閱規則

| 規則編號 | 規則內容 |
|---|---|
| BR-L-01 | 讀者借書時，系統必須檢查登入者是否為 `READER`。 |
| BR-L-02 | 讀者帳號必須為 `ACTIVE`，且 `borrow_suspended = false`。 |
| BR-L-03 | 系統必須檢查讀者目前借閱數是否超過 Service 中設定的上限。 |
| BR-L-04 | 只有 `copy_status = AVAILABLE` 的館藏可以被借閱。 |
| BR-L-05 | 借閱成功後，建立一筆 `borrow_records`，並將該館藏狀態改為 `BORROWED`。 |
| BR-L-06 | 借閱成功後，由 `NotificationService` 建立站內訊息。 |
| BR-L-07 | 若同書目仍有其他可借館藏，其他讀者仍可借閱同一本書的其他冊本。 |

### 5.5 歸還規則

| 規則編號 | 規則內容 |
|---|---|
| BR-R-01 | 讀者只能對自己的借閱紀錄提出歸還申請。 |
| BR-R-02 | 讀者申請歸還後，`borrow_records.borrow_status` 改為 `RETURN_PENDING`。 |
| BR-R-03 | 讀者申請歸還後，`book_copies.copy_status` 改為 `RETURN_PENDING`。 |
| BR-R-04 | 管理員審核正常歸還後，`borrow_records.borrow_status` 改為 `RETURNED`，`book_copies.copy_status` 改為 `AVAILABLE`。 |
| BR-R-05 | 管理員審核毀損或遺失後，`borrow_records.borrow_status` 與 `book_copies.copy_status` 改為 `DAMAGED` 或 `LOST`。 |
| BR-R-06 | 歸還審核完成後，由 `NotificationService` 建立站內訊息。 |

### 5.6 預約規則

| 規則編號 | 規則內容 |
|---|---|
| BR-V-01 | 預約綁定 `books.book_id`，不是綁定單一本 `book_copies.copy_id`。 |
| BR-V-02 | 同一位讀者不可重複預約同一本尚未完成的書目。 |
| BR-V-03 | 預約建立後，狀態為 `WAITING`。 |
| BR-V-04 | 管理員通知讀者可借時，預約狀態改為 `AVAILABLE_NOTICE`，並設定 `expire_date`。 |
| BR-V-05 | 預約可取通知由 `NotificationService` 建立站內訊息並寄送 Email。 |
| BR-V-06 | 讀者可取消 `WAITING` 或 `AVAILABLE_NOTICE` 狀態的預約。 |

### 5.7 訊息與 Email 規則

| 規則編號 | 規則內容 |
|---|---|
| BR-N-01 | 所有系統通知優先建立站內訊息 `messages`。 |
| BR-N-02 | 預約可取、到期前一天、逾期當下、逾期 7 日通知需同時寄送 Email。 |
| BR-N-03 | Email 寄送失敗不應導致主要業務流程 rollback，系統可記錄 log 後繼續流程。 |
| BR-N-04 | 訊息必須綁定接收者 `receiver_id`。 |
| BR-N-05 | 使用者只能查看、標記已讀、刪除自己的訊息。 |
| BR-N-06 | 訊息可關聯 `related_borrow_id` 或 `related_reservation_id`。 |

### 5.8 逾期規則

| 規則編號 | 規則內容 |
|---|---|
| BR-O-01 | 系統每日固定時間檢查借閱到期與逾期紀錄。 |
| BR-O-02 | 到期前一天建立 `DUE_SOON` 站內訊息並寄送 Email。 |
| BR-O-03 | 到期日後仍未歸還者，系統將借閱狀態改為 `OVERDUE`，建立 `OVERDUE` 訊息並寄送 Email。 |
| BR-O-04 | 逾期 7 日以上仍未處理者，系統將 `users.borrow_suspended` 改為 `true`。 |
| BR-O-05 | 管理員可於使用者管理頁手動開通借書權限，將 `borrow_suspended` 改回 `false`。 |
| BR-O-06 | `due_soon_notice_sent_at`、`overdue_notice_sent_at`、`overdue_7_notice_sent_at` 用於避免重複寄送通知。 |

### 5.9 Service 固定規則

| 規則 | 建議位置 | 說明 |
|---|---|---|
| 借閱天數 | `BorrowServiceImpl` | 例如固定 14 天。 |
| 最多借閱本數 | `BorrowServiceImpl` | 例如固定 5 本。 |
| 預約保留時間 | `ReservationServiceImpl` | 例如固定 48 小時。 |
| 預約順位 | `ReservationServiceImpl` | 可依等待中的預約數計算。 |
| 系統通知整合 | `NotificationServiceImpl` | 統一建立站內訊息與寄送 Email。 |
| 站內訊息建立 | `MessageServiceImpl` | 實際寫入 `messages`。 |
| Email 寄送 | `MailServiceImpl` | 使用 `JavaMailSender` 透過 SMTP 寄送。 |
| 逾期檢查 | `OverdueNoticeScheduler` | 每日排程檢查到期與逾期紀錄。 |

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
| `user_id` | VARCHAR(20), PK | 使用者 ID，由 `IdGenerator` 產生，例如 `U20260506123456A1B2` |
| `username` | VARCHAR(50), UNIQUE | 登入帳號 |
| `password` | VARCHAR(255) | 密碼，正式系統建議儲存加密雜湊值 |
| `name` | VARCHAR(50) | 使用者姓名 |
| `email` | VARCHAR(100), UNIQUE | Email |
| `phone` | VARCHAR(20) | 電話 |
| `role` | VARCHAR(20) | `READER`、`ADMIN` |
| `status` | VARCHAR(20) | `ACTIVE`、`DISABLED` |
| `borrow_suspended` | TINYINT(1) | 是否暫停借書，預設 `0` |
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
| `book_id` | VARCHAR(20), PK | 書目 ID，由 `IdGenerator` 產生，例如 `BK20260506123456A1B2` |
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
| `copy_id` | VARCHAR(20), PK | 館藏複本 ID，由 `IdGenerator` 產生，例如 `CP20260506123456A1B2` |
| `book_id` | VARCHAR(20), FK | 對應 `books.book_id` |
| `copy_code` | VARCHAR(50), UNIQUE | 每一本實體書唯一條碼或館藏編號，例如 `B00000001` |
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
| `due_soon_notice_sent_at` | DATETIME | 到期前一天通知寄送時間 |
| `overdue_notice_sent_at` | DATETIME | 逾期通知寄送時間 |
| `overdue_7_notice_sent_at` | DATETIME | 逾期 7 日通知寄送時間 |
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
| `message_type` | VARCHAR(30) | `GENERAL`、`BORROW`、`RETURN`、`RESERVATION`、`OVERDUE`、`DUE_SOON` |
| `is_read` | TINYINT(1) | 是否已讀 |
| `related_borrow_id` | BIGINT, FK, NULL | 關聯借閱紀錄 |
| `related_reservation_id` | BIGINT, FK, NULL | 關聯預約紀錄 |
| `created_at` | DATETIME | 建立時間 |
| `read_at` | DATETIME | 讀取時間 |

---

## 7. 主要使用案例

### UC-01 讀者註冊

| 項目 | 說明 |
|---|---|
| 主要角色 | 訪客 |
| 前置條件 | 使用者尚未註冊。 |
| 主要流程 | 1. 使用者輸入帳號、密碼、姓名、Email、電話。<br>2. 前端呼叫註冊 API。<br>3. 後端檢查帳號與 Email 是否重複。<br>4. 後端建立 `users`，角色預設 `READER`，狀態為 `ACTIVE`。<br>5. 前端顯示註冊成功，可導向登入頁。 |
| 替代流程 | 帳號或 Email 重複時，系統回傳錯誤訊息。 |
| 後置條件 | 新讀者帳號建立完成。 |

### UC-02 使用者登入

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者、管理員 |
| 前置條件 | 使用者已建立帳號。 |
| 主要流程 | 1. 使用者輸入帳號密碼。<br>2. 前端呼叫登入 API。<br>3. 後端查詢 `users` 並驗證帳號密碼。<br>4. 後端檢查 `status = ACTIVE`。<br>5. 驗證成功後回傳 token 與使用者資訊。<br>6. 前端依角色導向讀者首頁或管理員後台。 |
| 替代流程 | 帳號密碼錯誤或帳號停用時，系統回傳錯誤訊息。 |
| 後置條件 | 使用者可依角色操作對應功能。 |

### UC-03 查詢館藏

| 項目 | 說明 |
|---|---|
| 主要角色 | 訪客、讀者、管理員 |
| 前置條件 | 無。 |
| 主要流程 | 1. 使用者輸入書名、作者、ISBN 或分類。<br>2. 前端呼叫查詢 API。<br>3. 後端查詢 `books`，並統計每本書的總館藏數與可借數。<br>4. 前端顯示查詢結果。 |
| 替代流程 | 查無資料時顯示「無符合結果」。 |
| 後置條件 | 使用者可點入查看書籍詳情。 |

### UC-04 借閱書籍

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已登入，該書有 `AVAILABLE` 館藏，且讀者未被暫停借書。 |
| 主要流程 | 1. 讀者在書籍詳情頁點選借閱。<br>2. 系統檢查角色、帳號狀態、借書權限與借閱上限。<br>3. 系統尋找該 `book_id` 下第一本 `AVAILABLE` 的 `book_copy`。<br>4. 系統建立 `borrow_records`。<br>5. 系統將該 `book_copies.copy_status` 改為 `BORROWED`。<br>6. 系統建立借閱成功站內訊息。<br>7. 前端顯示借閱成功與到期日。 |
| 替代流程 A | 無可借館藏時，系統拒絕借閱並提示可預約。 |
| 替代流程 B | 已達借閱上限時，系統拒絕借閱。 |
| 替代流程 C | `borrow_suspended = true` 時，系統拒絕借閱並提示聯繫管理員。 |
| 後置條件 | 該實體館藏不可再被其他讀者借閱。 |

### UC-05 申請歸還

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已有 `BORROWED` 或 `OVERDUE` 狀態借閱紀錄。 |
| 主要流程 | 1. 讀者進入目前借閱頁。<br>2. 選擇要歸還的借閱紀錄。<br>3. 系統確認該借閱紀錄屬於目前登入讀者。<br>4. 系統將借閱紀錄改為 `RETURN_PENDING`。<br>5. 系統將館藏狀態改為 `RETURN_PENDING`。<br>6. 前端顯示等待管理員審核。 |
| 替代流程 | 非本人借閱紀錄不可操作。 |
| 後置條件 | 管理員可在後台看到歸還待審核清單。 |

### UC-06 管理員審核歸還

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 存在 `RETURN_PENDING` 的借閱紀錄。 |
| 主要流程 | 1. 管理員進入歸還審核頁。<br>2. 查看待審核清單。<br>3. 選擇一筆紀錄並指定審核結果。<br>4. 若書況正常，借閱紀錄改為 `RETURNED`，館藏改為 `AVAILABLE`。<br>5. 若毀損或遺失，借閱紀錄與館藏改為 `DAMAGED` 或 `LOST`。<br>6. 系統建立歸還審核結果站內訊息。 |
| 替代流程 | 找不到紀錄或狀態不正確時回傳錯誤。 |
| 後置條件 | 借閱流程完成，或進入異常書況紀錄。 |

### UC-07 預約書籍

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者 |
| 前置條件 | 讀者已登入。 |
| 主要流程 | 1. 讀者在書籍詳情頁點選預約。<br>2. 系統檢查是否已有同書目未完成預約。<br>3. 系統建立 `reservations`，狀態為 `WAITING`。<br>4. 系統計算預約順位。<br>5. 前端顯示預約成功。 |
| 替代流程 | 若已有未完成預約，系統拒絕重複預約。 |
| 後置條件 | 管理員可在後台通知讀者預約可借。 |

### UC-08 預約可取通知

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 存在 `WAITING` 狀態預約紀錄。 |
| 主要流程 | 1. 管理員進入預約管理頁。<br>2. 選擇一筆等待中的預約。<br>3. 系統將預約狀態改為 `AVAILABLE_NOTICE`。<br>4. 系統設定保留到期時間。<br>5. 系統建立站內訊息並寄送 Email。 |
| 替代流程 | 非 `WAITING` 狀態不可通知。 |
| 後置條件 | 讀者收到預約可取通知。 |

### UC-09 查看與管理訊息

| 項目 | 說明 |
|---|---|
| 主要角色 | 讀者、管理員 |
| 前置條件 | 使用者已登入。 |
| 主要流程 | 1. 使用者進入訊息中心。<br>2. 系統查詢該使用者的 `messages` 清單。<br>3. 使用者可查看內容、標記已讀或刪除。 |
| 替代流程 | 無訊息時顯示空狀態。 |
| 後置條件 | 訊息讀取狀態被更新。 |

### UC-10 管理員使用者管理

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 管理員已登入。 |
| 主要流程 | 1. 管理員查詢使用者清單。<br>2. 可依關鍵字、角色、狀態篩選。<br>3. 管理員可新增讀者或管理員。<br>4. 管理員可修改使用者姓名、Email、電話、角色、狀態。<br>5. 管理員可停用或啟用帳號。<br>6. 管理員可開通因逾期暫停的借書權限。 |
| 替代流程 | 帳號或 Email 重複時回傳錯誤。 |
| 後置條件 | 使用者資料被更新。 |

### UC-11 逾期通知與借書暫停

| 項目 | 說明 |
|---|---|
| 主要角色 | 系統排程、讀者、管理員 |
| 前置條件 | 存在尚未歸還的借閱紀錄。 |
| 主要流程 | 1. 系統每日排程檢查借閱紀錄。<br>2. 到期前一天建立站內訊息並寄 Email。<br>3. 到期後仍未歸還，借閱狀態改為 `OVERDUE` 並通知。<br>4. 逾期 7 日以上仍未處理，系統將 `borrow_suspended` 改為 `true` 並通知讀者。<br>5. 管理員完成處理後，可手動開通借書權限。 |
| 替代流程 | Email 寄送失敗時，系統保留站內訊息並記錄錯誤，不中斷排程。 |
| 後置條件 | 逾期讀者被提醒，嚴重逾期者暫停借書。 |

---

## 8. API 規格

> 後端統一使用 `/api` 作為 REST API 前綴。  
> 需要登入的 API 應在 Header 傳入：`Authorization: Bearer <token>`。  
> 公開 API 可不帶 token；若有帶有效 token，後端可解析目前登入者資訊。  
> DTO 設計採必要最小化：多欄位物件使用 `@RequestBody`；單一或少量簡單參數使用 `@PathVariable` 或 `@RequestParam`。

### 8.1 Auth API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Auth | POST | `/api/auth/register` | body: `username`, `password`, `name`, `email`, `phone` | `UserResponse` | 讀者自行註冊 | 不需 token；註冊成功後可導向登入頁 | UC-01 |
| Auth | POST | `/api/auth/login` | body: `username`, `password` | `LoginResponse(token, userInfo)` | 使用者登入 | 儲存 token；依 `role` 導向不同首頁 | UC-02 |
| Auth | POST | `/api/auth/logout` | Header Authorization | success message | 使用者登出 | 前端收到成功後清除 token 與使用者資訊 | UC-02 |

### 8.2 Book API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Book | GET | `/api/books` | query: `keyword`, `status` | `List<BookListResponse>` | 查詢公開書目清單 | 訪客可查；顯示總館藏數與可借數 | UC-03 |
| Book | GET | `/api/books/{bookId}` | path: `bookId` | 書目詳情、館藏統計、館藏清單 | 查詢書籍詳情 | 詳情頁依可借數決定顯示借閱或預約 | UC-03 |
| AdminBook | GET | `/api/admin/books` | Header Authorization; query: `keyword`, `status` | `List<BookListResponse>` | 管理員查詢書目 | 管理員頁使用；需 token | UC-10 |
| AdminBook | POST | `/api/admin/books` | Header Authorization; body: `BookRequest` | `BookListResponse` | 新增書目 | 書目欄位較多，使用 DTO | UC-10 |
| AdminBook | PUT | `/api/admin/books/{bookId}` | Header Authorization; path: `bookId`; body: `BookRequest` | `BookListResponse` | 修改書目 | 使用完整表單送出 | UC-10 |
| AdminBook | PATCH | `/api/admin/books/{bookId}/status` | Header Authorization; path: `bookId`; query: `status` | `BookListResponse` | 上架 / 下架書目 | 單一欄位用 `@RequestParam` | UC-10 |

### 8.3 Book Category API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| BookCategory | GET | `/api/book-categories` | 無 | `List<BookCategoryResponse>` | 查詢中文圖書分類法分類 | 書目新增、編輯、查詢頁下拉選單使用 | UC-03 |
| BookCategory | GET | `/api/book-categories/{categoryId}` | path: `categoryId` | `BookCategoryResponse` | 查詢單一分類 | 可用於分類詳情顯示 | UC-03 |

### 8.4 Book Copy API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| AdminBookCopy | GET | `/api/admin/book-copies` | Header Authorization; query: `bookId`, `copyStatus`, `keyword` | `List<BookCopyResponse>` | 查詢館藏冊本 | 支援依書目、狀態、條碼查詢 | UC-10 |
| AdminBookCopy | GET | `/api/admin/book-copies/{copyId}` | Header Authorization; path: `copyId` | `BookCopyResponse` | 查詢單一館藏 | 編輯館藏前載入資料 | UC-10 |
| AdminBookCopy | POST | `/api/admin/books/{bookId}/copies` | Header Authorization; path: `bookId`; query: `copyCode`, `location`, `note` | `BookCopyResponse` | 在書目下新增館藏 | 館藏欄位較少，使用 `@RequestParam` | UC-10 |
| AdminBookCopy | PUT | `/api/admin/book-copies/{copyId}` | Header Authorization; path: `copyId`; query: `copyCode`, `location`, `copyStatus`, `note` | `BookCopyResponse` | 修改館藏資料 | 館藏條碼不可重複 | UC-10 |
| AdminBookCopy | PATCH | `/api/admin/book-copies/{copyId}/status` | Header Authorization; path: `copyId`; query: `copyStatus` | `BookCopyResponse` | 修改館藏狀態 | 單一欄位用 `@RequestParam` | UC-10 |

### 8.5 Borrow API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Borrow | POST | `/api/borrows/{bookId}` | Header Authorization; path: `bookId` | `BorrowResponse` | 讀者借閱書籍 | 讀者限定；若暫停借書需顯示提示 | UC-04 |
| Borrow | GET | `/api/borrows/me/current` | Header Authorization | `List<BorrowResponse>` | 查詢目前借閱 | 顯示借閱中、逾期、歸還待審核 | UC-05 |
| Borrow | PATCH | `/api/borrows/{borrowId}/return-request` | Header Authorization; path: `borrowId` | success message | 讀者申請歸還 | 操作後狀態變為待審核 | UC-05 |
| AdminBorrow | GET | `/api/admin/borrows` | Header Authorization; query: `borrowStatus` | `List<BorrowResponse>` | 管理員查詢借閱紀錄 | 可篩選 `RETURN_PENDING`、`BORROWED` 等 | UC-06 |
| AdminBorrow | GET | `/api/admin/borrows/return-pending` | Header Authorization | `List<BorrowResponse>` | 查詢歸還待審核 | 歸還審核頁使用 | UC-06 |
| AdminBorrow | PATCH | `/api/admin/borrows/{borrowId}/approve-return` | Header Authorization; path: `borrowId`; query: `resultStatus` | `BorrowResponse` | 審核歸還 | `resultStatus` 限 `RETURNED`、`DAMAGED`、`LOST` | UC-06 |

### 8.6 Reservation API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Reservation | POST | `/api/reservations` | Header Authorization; query: `bookId` | `ReservationResponse` | 讀者預約書籍 | 單一欄位用 `@RequestParam` | UC-07 |
| Reservation | GET | `/api/reservations/me` | Header Authorization | `List<ReservationResponse>` | 查詢我的預約 | 顯示等待、已通知、取消等狀態 | UC-07 |
| Reservation | PATCH | `/api/reservations/{reservationId}/cancel` | Header Authorization; path: `reservationId` | success message | 取消預約 | 只能取消自己的未完成預約 | UC-07 |
| AdminReservation | GET | `/api/admin/reservations` | Header Authorization; query: `reservationStatus` | `List<ReservationResponse>` | 管理員查詢預約 | 可篩選等待或已通知 | UC-08 |
| AdminReservation | PATCH | `/api/admin/reservations/{reservationId}/notify` | Header Authorization; path: `reservationId` | `ReservationResponse` | 通知讀者可借 | 會建立站內訊息並寄 Email | UC-08 |

### 8.7 Message API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Message | GET | `/api/messages/me` | Header Authorization | `List<MessageResponse>` | 查詢我的訊息 | 只回傳自己的訊息 | UC-09 |
| Message | GET | `/api/messages/me/unread-count` | Header Authorization | `unreadCount` | 查詢未讀數 | 可顯示於導覽列徽章 | UC-09 |
| Message | PATCH | `/api/messages/{messageId}/read` | Header Authorization; path: `messageId` | success message | 標記訊息已讀 | 只能操作自己的訊息 | UC-09 |
| Message | DELETE | `/api/messages/{messageId}` | Header Authorization; path: `messageId` | success message | 刪除訊息 | 只能刪除自己的訊息 | UC-09 |

### 8.8 User API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| User | GET | `/api/users/me` | Header Authorization | `UserResponse` | 查看個人資料 | 不回傳 password | UC-09 |
| AdminUser | GET | `/api/admin/users` | Header Authorization; query: `keyword`, `role`, `status` | `List<UserResponse>` | 管理員查詢使用者 | 不回傳 password | UC-10 |
| AdminUser | GET | `/api/admin/users/{userId}` | Header Authorization; path: `userId` | `UserResponse` | 管理員查詢單一使用者 | 編輯前載入資料 | UC-10 |
| AdminUser | POST | `/api/admin/users` | Header Authorization; body: `AdminUserRequest` | `UserResponse` | 管理員新增使用者 | 可新增讀者或管理員 | UC-10 |
| AdminUser | PUT | `/api/admin/users/{userId}` | Header Authorization; path: `userId`; body: `AdminUserRequest` | `UserResponse` | 管理員修改使用者 | 空密碼表示不修改密碼 | UC-10 |
| AdminUser | PATCH | `/api/admin/users/{userId}/status` | Header Authorization; path: `userId`; query: `status` | `UserResponse` | 啟用 / 停用帳號 | 單一欄位用 `@RequestParam` | UC-10 |
| AdminUser | PATCH | `/api/admin/users/{userId}/restore-borrow-permission` | Header Authorization; path: `userId` | `UserResponse` | 開通借書權限 | 將 `borrow_suspended` 改為 `false` | UC-11 |

---

## 9. 後端架構規劃

### 9.1 Spring Boot 分層架構

| 層級 | 套件範例 | 職責 |
|---|---|---|
| Controller | `com.library.controller` | 接收 HTTP request，回傳 `ApiResponse`。 |
| Controller Admin | `com.library.controller.admin` | 管理員後台 API。 |
| Service Interface | `com.library.service` | 定義業務服務介面。 |
| Service Impl | `com.library.service.impl` | 實作業務邏輯、狀態轉換、權限檢查。 |
| Repository | `com.library.repository` | 使用 Spring Data JPA 存取資料庫。 |
| Entity | `com.library.entity` | 對應資料表。 |
| DTO | `com.library.dto` | 僅在必要時定義 request / response 格式。 |
| Exception | `com.library.exception` | `LibraryBusinessException`、`ResponseCode`、全域例外處理。 |
| Config | `com.library.config` | CORS、Interceptor、安全設定。 |
| Util | `com.library.util` | `IdGenerator` 等工具。 |
| Security Util | `com.library.util.security` | JWT、登入者 ThreadLocal、`LoginUserHolder`。 |
| Scheduler | `com.library.scheduler` | 逾期通知排程。 |

### 9.2 後端 Class 清單

| 模組 | Entity | Repository | Service | Controller | DTO |
|---|---|---|---|---|---|
| Auth | User | UserRepository | AuthService / AuthServiceImpl | AuthController | LoginRequest、LoginResponse、RegisterRequest |
| User | User | UserRepository | AdminUserService / AdminUserServiceImpl | UserController、AdminUserController | UserResponse、AdminUserRequest |
| Book Category | BookCategory | BookCategoryRepository | BookCategoryService / BookCategoryServiceImpl | BookCategoryController | BookCategoryResponse |
| Book | Book | BookRepository | BookService、AdminBookService | BookController、AdminBookController | BookRequest、BookListResponse、BookDetailResponse |
| Book Copy | BookCopy | BookCopyRepository | AdminBookCopyService | AdminBookCopyController | BookCopyResponse |
| Borrow | BorrowRecord | BorrowRecordRepository | BorrowService / BorrowServiceImpl | BorrowController、AdminBorrowController | BorrowResponse |
| Reservation | Reservation | ReservationRepository | ReservationService / ReservationServiceImpl | ReservationController、AdminReservationController | ReservationResponse |
| Message | Message | MessageRepository | MessageService / MessageServiceImpl | MessageController | MessageResponse |
| Notification | Message、BorrowRecord、Reservation | 無 | NotificationService / NotificationServiceImpl | 無 | 無 |
| Mail | 無 | 無 | MailService / MailServiceImpl | 無 | 無 |
| Scheduler | BorrowRecord、User | BorrowRecordRepository、UserRepository | NotificationService | OverdueNoticeScheduler | 無 |
| Common | 無 | 無 | 無 | GlobalExceptionHandler | ApiResponse |
| Util | 無 | 無 | 無 | 無 | IdGenerator、JwtUtil、LoginUserHolder |

### 9.3 DTO 使用原則

| 情境 | 建議 |
|---|---|
| 登入、註冊、多欄位新增修改 | 使用 Request DTO，例如 `LoginRequest`、`RegisterRequest`、`BookRequest`、`AdminUserRequest`。 |
| 單一 ID 操作 | 使用 `@PathVariable`。 |
| 單一狀態修改 | 使用 `@RequestParam`。 |
| 少量簡單欄位 | 可使用 `@RequestParam`，例如新增館藏的 `copyCode`、`location`、`note`。 |
| 回傳資料 | 使用 Response DTO，避免直接回傳 Entity，尤其不可回傳 password。 |

---

## 10. 重要流程設計

### 10.1 登入與 JWT 流程

```text
使用者登入
→ 後端驗證 username / password
→ 檢查 users.status 是否 ACTIVE
→ 產生 JWT token
→ 前端儲存 token
→ 後續 API 以 Authorization: Bearer <token> 呼叫
→ AuthInterceptor 解析 token 並放入 LoginUserHolder
→ Service 使用 requireReader / requireAdmin / requireUserId 判斷權限
```

### 10.2 借閱流程

```text
讀者點選借閱
→ 前端送出 bookId
→ 後端驗證 READER
→ 檢查 users.status 是否 ACTIVE
→ 檢查 borrow_suspended 是否 false
→ 檢查借閱上限
→ 查詢該 bookId 下第一本 AVAILABLE 的 book_copy
→ 建立 borrow_record
→ book_copy.copy_status = BORROWED
→ NotificationService 建立借閱成功站內訊息
→ 回傳 BorrowResponse
```

### 10.3 歸還流程

```text
讀者點選申請歸還
→ 後端驗證 READER
→ 確認 borrow_record 屬於目前讀者
→ borrow_record.borrow_status = RETURN_PENDING
→ book_copy.copy_status = RETURN_PENDING
→ 管理員進入歸還審核頁
→ 管理員指定 resultStatus
→ 正常：borrow_record = RETURNED，book_copy = AVAILABLE
→ 毀損：borrow_record = DAMAGED，book_copy = DAMAGED
→ 遺失：borrow_record = LOST，book_copy = LOST
→ NotificationService 建立歸還審核結果站內訊息
```

### 10.4 預約通知流程

```text
讀者預約書目
→ reservations.reservation_status = WAITING
→ 管理員於預約管理頁選擇通知
→ reservations.reservation_status = AVAILABLE_NOTICE
→ 設定 expire_date
→ NotificationService 建立站內訊息
→ MailService 寄送 Email
```

### 10.5 逾期通知流程

```text
每日排程啟動
→ 查詢明天到期且尚未通知的 BORROWED 紀錄
→ 建立 DUE_SOON 站內訊息並寄 Email

→ 查詢昨天到期且尚未逾期通知的 BORROWED 紀錄
→ borrow_status = OVERDUE
→ 建立 OVERDUE 站內訊息並寄 Email

→ 查詢 due_date <= 今日 - 7 且尚未通知的 OVERDUE 紀錄
→ users.borrow_suspended = true
→ 建立逾期 7 日通知並寄 Email
```

---

## 11. 前端頁面規劃

### 11.1 Angular 模組建議

| 模組 | 說明 | 主要頁面 |
|---|---|---|
| `auth` | 註冊、登入、登出 | register、login |
| `reader` | 讀者端功能 | reader-home、book-search、book-detail、my-borrows、my-reservations、message-center、profile |
| `admin` | 管理員端功能 | admin-dashboard、book-manage、book-copy-manage、user-manage、borrow-manage、return-review、reservation-manage |
| `shared` | 共用元件 | navbar、sidebar、pagination、alert、confirm-dialog |
| `core` | 核心服務 | auth.service、api.service、token.interceptor、auth.guard |

### 11.2 讀者端頁面

| 頁面 | 路由 | 功能 |
|---|---|---|
| 註冊頁 | `/register` | 讀者自行註冊。 |
| 登入頁 | `/login` | 使用者登入。 |
| 讀者首頁 | `/reader/home` | 顯示借閱摘要、未讀訊息、功能入口。 |
| 館藏查詢頁 | `/books` | 查詢書籍、顯示可借數。 |
| 書籍詳情頁 | `/books/:id` | 顯示書目資料與館藏狀態，可借閱或預約。 |
| 我的借閱頁 | `/reader/borrows` | 查看目前借閱、申請歸還。 |
| 我的預約頁 | `/reader/reservations` | 查看與取消預約。 |
| 訊息中心 | `/reader/messages` | 查看、標記已讀、刪除訊息。 |
| 個人資料頁 | `/reader/profile` | 查看個人資料。 |

### 11.3 管理員端頁面

| 頁面 | 路由 | 功能 |
|---|---|---|
| 後台首頁 | `/admin/dashboard` | 顯示待審核歸還、預約通知、逾期摘要。 |
| 書目管理頁 | `/admin/books` | 新增、編輯、查詢書目。 |
| 館藏冊本管理頁 | `/admin/book-copies` | 新增、編輯、查詢實體館藏。 |
| 使用者管理頁 | `/admin/users` | 新增、編輯、查詢、停用使用者，開通借書權限。 |
| 借閱紀錄管理頁 | `/admin/borrows` | 查看全部借閱紀錄。 |
| 歸還審核頁 | `/admin/return-review` | 審核讀者歸還申請。 |
| 預約管理頁 | `/admin/reservations` | 查看預約清單與通知讀者。 |

---

## 12. 驗收標準

| 編號 | 驗收項目 | 驗收條件 |
|---|---|---|
| AC-01 | 讀者可自行註冊 | 訪客可建立 `READER` 帳號，且不需管理員事前建立。 |
| AC-02 | 使用者可登入並取得 JWT | 登入成功回傳 token，前端可用 token 呼叫需登入 API。 |
| AC-03 | 書目與館藏分離 | 同一本書可建立多本 `book_copies`，且每本有不同條碼。 |
| AC-04 | 查詢書籍可顯示可借數 | 書籍清單能顯示總館藏數與可借數。 |
| AC-05 | 借閱只影響一本實體館藏 | 借出一本 copy 後，同書其他 AVAILABLE copy 仍可借。 |
| AC-06 | 無可借館藏不能借閱 | 若所有 copy 都非 AVAILABLE，借閱 API 需拒絕。 |
| AC-07 | 暫停借書者不可借閱 | `borrow_suspended = true` 時，讀者可登入但不可借書。 |
| AC-08 | 歸還需管理員審核 | 讀者申請歸還後，館藏狀態為 RETURN_PENDING，不會直接變 AVAILABLE。 |
| AC-09 | 管理員可完成歸還審核 | 管理員審核後可將館藏改回 AVAILABLE 或改為 DAMAGED / LOST。 |
| AC-10 | 預約可取通知正常 | 管理員通知後，預約狀態改為 AVAILABLE_NOTICE，並建立訊息與寄送 Email。 |
| AC-11 | 訊息功能正常 | 系統事件可產生 message，使用者可查看、標記已讀、刪除。 |
| AC-12 | 逾期通知正常 | 排程可產生到期前、逾期、逾期 7 日通知。 |
| AC-13 | 逾期 7 日暫停借書 | 系統可自動將使用者 `borrow_suspended` 改為 true。 |
| AC-14 | 管理員可開通借書權限 | 管理員可將 `borrow_suspended` 改為 false。 |
| AC-15 | 權限區分 | 讀者不可呼叫管理員 API，訪客不可呼叫讀者功能 API。 |
| AC-16 | 不直接回傳密碼 | API 回傳使用者資料時不可包含 password。 |
| AC-17 | 讀書心得完全移除 | 前端無心得頁面，後端無 Review API，資料表不建立 reviews / review_drafts。 |
| AC-18 | ID 長度正確 | `user_id`、`book_id`、`copy_id` 不超過 `VARCHAR(20)`。 |
| AC-19 | 中文圖書分類法正確 | `book_categories.category_id` 可使用 `000`、`100`、`900` 等分類代碼。 |
| AC-20 | 前後端可串接 | Angular 可透過 REST API 完成查詢、借閱、歸還、預約、訊息與管理員維護流程。 |

---

## 13. 開發優先順序

| 階段 | 目標 | 功能 |
|---|---|---|
| 第一階段 | 完成核心借閱閉環 | 註冊、登入、查書、書目詳情、借閱、申請歸還、歸還審核、訊息。 |
| 第二階段 | 補齊管理功能 | 管理員書目維護、館藏維護、使用者管理、預約管理、借閱紀錄管理。 |
| 第三階段 | 補齊通知與逾期處理 | NotificationService、MailService、預約可取 Email、逾期排程、借書權限暫停。 |
| 第四階段 | 優化與擴充 | Excel 匯入匯出、儀表板統計、密碼加密、批次新增館藏、預約自動配書。 |

---

## 14. 從舊系統到新系統的改寫對照

| 舊系統項目 | 新系統對應 | 處理方式 |
|---|---|---|
| Swing UI Controller | Angular Component | 原 UI 邏輯改為前端頁面、路由與 Service。 |
| DAO / JDBC | Spring Data JPA Repository | 改用 Entity + Repository。 |
| ServiceImpl | Spring Boot Service | 保留業務邏輯概念，改為 Spring Bean。 |
| Readers / Admins | User | 合併成 `users`，用 `role` 區分。 |
| 管理員建立讀者 | 讀者自行註冊 + 管理員使用者管理 | 新系統支援讀者自行註冊，管理員仍可新增使用者。 |
| Books | Book + BookCopy | 拆分書目與實體館藏。 |
| BorrowRecords | BorrowRecord | 保留，但關聯改為 `copyId`。 |
| Messages | Message + NotificationService | 保留站內訊息，新增 NotificationService 統一整合 Email。 |
| 無排程通知 | OverdueNoticeScheduler | 新增每日逾期檢查與停權流程。 |
| Reviews / ReviewDrafts | 無 | 完全移除。 |
| AppException | LibraryBusinessException | 改由 GlobalExceptionHandler 統一回應。 |

---

## 15. 建議專案目錄

### 15.1 後端 Spring Boot

```text
library-backend/
└── src/main/java/com/library/
    ├── LibraryBackendApplication.java
    ├── config/
    │   ├── AuthInterceptor.java
    │   └── WebConfig.java
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
    │       └── AdminReservationController.java
    ├── dto/
    │   ├── auth/
    │   ├── book/
    │   ├── borrow/
    │   ├── common/
    │   ├── message/
    │   ├── reservation/
    │   └── user/
    ├── entity/
    ├── exception/
    ├── repository/
    ├── scheduler/
    │   └── OverdueNoticeScheduler.java
    ├── service/
    │   └── impl/
    └── util/
        ├── IdGenerator.java
        └── security/
            ├── JwtUtil.java
            ├── LoginUser.java
            └── LoginUserHolder.java
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
    │   ├── login/
    │   └── register/
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
        └── reservation-manage/
```

---

## 16. 後續建議

### 16.1 密碼加密

目前若仍使用明文密碼，建議後續改為 BCrypt：

```text
註冊 / 新增使用者：儲存 BCrypt hash
登入：使用 passwordEncoder.matches(rawPassword, encodedPassword)
```

### 16.2 批次新增館藏

目前已支援單本新增館藏。若後台需要大量新增，可以補：

```http
POST /api/admin/books/{bookId}/copies/batch
```

### 16.3 Excel 匯入匯出

可列為展示後續功能，包含：

```text
書目匯入
館藏匯入
借閱紀錄匯出
```

### 16.4 預約自動配書

目前預約可取通知由管理員操作。後續可擴充為：

```text
館藏歸還審核正常
→ 系統自動查詢最早 WAITING 預約
→ 自動通知第一位讀者
→ 可選擇將館藏狀態改為 RESERVED
```

### 16.5 前端串接優先順序

建議前端先做：

```text
1. 登入 / 註冊 / 登出
2. 公開查書 / 書籍詳情
3. 讀者借閱 / 我的借閱 / 申請歸還
4. 訊息中心
5. 管理員書目 / 館藏維護
6. 管理員歸還審核
7. 管理員使用者管理
8. 預約管理與逾期通知展示
```
