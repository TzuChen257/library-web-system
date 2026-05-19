# 圖書館館藏管理系統需求文件

> 版本：v1.4  
> 修訂依據：現有 Spring Boot 後端功能、AngularJS 前端整合頁面、Demo / PPT 功能展示需求  
> 後端：Eclipse + Spring Boot + Spring Data JPA + JWT + Scheduler + Mail  
> 前端：VSCode + AngularJS 1.x 風格（HTML + JS + `$scope` + `$http`），可少量搭配 jQuery  
> 資料庫：MySQL  
>
> 本版修訂重點：
> 1. 以現有已開發功能重新整理需求文件，將功能狀態區分為「已完成 / 已串接」、「已規劃待補強」、「後續優化」。
> 2. 保留「書目 `books`」與「實體館藏 `book_copies`」分離設計，借閱、歸還、毀損、遺失皆以實體館藏為操作單位。
> 3. 使用者與管理員統一存放於 `users`，以 `role` 區分 `READER`、`ADMIN`，並以 JWT 控制前後台權限。
> 4. 保留 `borrow_suspended` 設計，明確區分「帳號停用」與「暫停借書權限」。
> 5. 前端以 `reader.html` 整合讀者中心，以 `admin.html` 整合管理員中心；後台功能以 tab / card 方式呈現。
> 6. 管理員 Demo 展示區塊更新為：管理員登入、後台首頁統計、書目管理、封面 / Excel、館藏管理、借閱紀錄、歸還審核、預約管理、使用者管理。
> 7. 讀者 Demo 展示區塊更新為：讀者註冊 / 登入、首頁公開統計、館藏查詢、書籍詳情、借閱、申請歸還、預約、訊息中心、個人資料。
> 8. DTO 維持必要最小化原則：多欄位資料使用 Request DTO，單一或少量簡單欄位優先使用 `@PathVariable` / `@RequestParam`。
> 9. Excel 功能維持兩類：年度借閱統計報表下載、書目與館藏批次匯入。
> 10. 保留 NotificationService、MailService、OverdueNoticeScheduler 設計，作為通知與逾期處理基礎。
> 11. 移除所有讀書心得功能，前後端與資料庫皆不保留心得模組。
> 12. 本版新增「現有功能狀態總表」、「Demo / PPT 功能展示區塊對照」、「目前版本完成範圍與後續補強」。

---

## 0. 現有功能更新摘要

本版以目前專案實際展示與串接範圍為準，將系統定位為「前後端已可展示核心流程」的圖書館館藏管理系統。主要重點不是再新增大範圍模組，而是讓需求文件、API 規格、前端頁面、Demo / PPT 展示內容一致。

### 0.1 目前版本功能定位

| 類別 | 現有版本定位 | 說明 |
|---|---|---|
| 讀者端 | 核心借閱流程展示版 | 可註冊、登入、查書、看詳情、借閱、預約、申請歸還、查看訊息與個人資料。 |
| 管理員端 | 後台維護與審核展示版 | 可進行書目、館藏、借閱、歸還審核、預約、使用者與統計報表管理。 |
| 通知功能 | 站內訊息為核心，Email 為補強 | 借閱、歸還、預約、逾期等事件可建立訊息；Email 用於時效通知。 |
| Excel 功能 | 管理員資料維護與報表展示 | 包含報表下載、匯入範本下載、書目與館藏批次匯入。 |
| Demo / PPT | 功能區塊式展示 | 以畫面區塊標記管理員與讀者端的主要操作流程。 |

### 0.2 本版優先維持一致的內容

| 項目 | 調整方向 |
|---|---|
| 功能需求 | 以現有功能重新標示已完成與待補強項目。 |
| API 規格 | 保留目前已規劃與串接的 REST API，避免再拆成過多未實作端點。 |
| 前端頁面 | 維持 `reader.html` 與 `admin.html` 的整合式頁面架構。 |
| Demo 說明 | 新增可直接放進 PPT 的功能展示區塊對照。 |
| 後續建議 | 從原本大範圍開發順序，改成「完成範圍確認」與「補強優先順序」。 |

---

## 1. 專案概述

### 1.1 專案名稱

圖書館館藏管理系統

### 1.2 專案目標

本系統目標是將原本桌面式 Java Swing 圖書館管理系統改寫為前後端分離的 Web 系統。讀者可以在線上註冊、登入、查詢館藏、借閱書籍、申請歸還、預約書籍、查看訊息與接收逾期提醒；管理員可以維護書目資料、管理每一本實體館藏、審核歸還、管理使用者、處理預約通知與開通讀者借書權限。

系統通知以站內訊息為基礎，並針對時效性高的情境整合 Email，例如預約可取、到期前一天、逾期當下與逾期 7 日借書權限暫停通知。新版 Web 系統另補齊首頁公開統計、管理員後台統計、年度 Excel 報表下載，以及書目與館藏 Excel 批次匯入，讓系統除一般借還書流程外，也具備管理展示與資料維護能力。

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
| 首頁公開統計 | 訪客與登入者皆可查看館藏書目數、可借館藏數、今日借閱數、本月借閱數與本月熱門借閱 Top 5。 |
| 管理員統計 | 管理員可查看借閱中、逾期、歸還待審核、等待預約、已通知可取、今日借閱與本月借閱等統計。 |
| Excel 報表下載 | 管理員可下載年度借閱統計 Excel，包含每月借閱統計、熱門書籍排行與讀者借閱排行。 |
| Excel 書目匯入 | 管理員可下載匯入範本，並上傳 Excel 批次建立書目與館藏資料。 |

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
| FR-C-05 | 首頁公開統計 | 訪客與登入者皆可查看公開統計與本月熱門借閱 Top 5。 | 中 |

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
| FR-A-11 | 歸還審核 | 針對讀者歸還申請確認書況並更新狀態，支援批次正常歸還。 | 高 |
| FR-A-12 | 預約管理 | 查看預約清單，僅在有可借館藏且為第一順位時通知讀者可借。 | 中 |
| FR-A-13 | Excel 匯入書籍 | 下載範本並批次匯入書目與館藏資料。 | 中 |
| FR-A-14 | Excel 報表下載 | 管理員可下載年度借閱統計 Excel 報表。 | 中 |
| FR-A-15 | 管理員統計首頁 | 顯示借閱、預約、逾期與待審核等統計摘要。 | 中 |

### 4.4 系統背景功能

| 編號 | 功能名稱 | 功能說明 | 優先度 |
|---|---|---|---|
| FR-S-01 | 到期前一天通知 | 系統每日檢查隔日到期紀錄，建立站內訊息並寄 Email。 | 中 |
| FR-S-02 | 逾期當下通知 | 系統每日檢查已逾期紀錄，將狀態改為 `OVERDUE` 並通知讀者。 | 中 |
| FR-S-03 | 逾期 7 日停權 | 系統每日檢查逾期 7 日以上紀錄，暫停讀者借書功能並通知。 | 中 |
| FR-S-04 | 預約可取通知 | 管理員通知預約可借時，建立站內訊息並寄 Email。 | 中 |


### 4.5 現有功能狀態總表

> 狀態說明：
> - 已完成 / 已串接：目前可作為 Demo 展示主流程。
> - 已規劃待補強：需求文件保留，但需再確認後端或前端是否完整串接。
> - 後續優化：不影響核心 Demo，可列為未來擴充。

| 功能區塊 | 對應功能 | 目前狀態 | Demo / 文件呈現重點 |
|---|---|---|---|
| 管理員登入區塊 | ADMIN 登入、取得 JWT Token、依角色進入後台 | 已完成 / 已串接 | 可說明登入成功後前端儲存 token，後續 API 以 Authorization header 呼叫。 |
| 後台首頁區塊 | 借閱中、逾期、預約、歸還待審核、今日與本月借閱統計 | 已完成 / 已串接 | 可作為管理員進入後台後的第一個展示畫面。 |
| 書目管理區塊 | 新增、修改、上架、下架、查詢書目 | 已完成 / 已串接 | 強調 `books` 是書籍主檔，不直接代表每一本實體書。 |
| 封面 / Excel 區塊 | 上傳或設定封面、下載匯入範本、Excel 批次匯入書目與館藏 | 已完成 / 已串接 | 可說明 Excel 一列代表一筆實體館藏；同書多本用多列。 |
| 館藏管理區塊 | 新增、編輯、查詢館藏冊本，管理 copyCode、位置、狀態 | 已完成 / 已串接 | 強調借閱與歸還實際影響 `book_copies.copy_status`。 |
| 借閱紀錄區塊 | 管理員查詢全體借閱紀錄，可依狀態篩選 | 已完成 / 已串接 | 可展示借閱後 borrow record 與 copy 狀態同步更新。 |
| 歸還審核區塊 | 單筆審核正常、毀損、遺失；批次正常歸還 | 已完成 / 已串接 | 強調讀者申請歸還後不是直接可借，需管理員審核。 |
| 預約管理區塊 | 管理員查看預約、判斷可借數與第一順位、通知可取 | 已完成 / 已串接 | 強調預約以 `book_id` 為單位，不指定 copy。 |
| 使用者管理區塊 | 新增、編輯、查詢、啟用/停用、開通借書權限 | 已完成 / 已串接 | 說明 `status` 與 `borrow_suspended` 的差異。 |
| 讀者註冊 / 登入 | 訪客註冊 READER、讀者登入取得 JWT | 已完成 / 已串接 | 可展示一般讀者進入前台流程。 |
| 首頁公開統計 | 館藏書目數、可借館藏數、今日借閱、本月借閱、熱門 Top 5 | 已完成 / 已串接 | 訪客不登入也可看到公開統計。 |
| 館藏查詢 / 書籍詳情 | 查詢書目、看館藏數、可借數、分類與狀態 | 已完成 / 已串接 | 可說明查詢以 `books` 為主，詳細頁顯示 copy 統計。 |
| 讀者借閱 | 有可借館藏時借出一本 copy | 已完成 / 已串接 | 可說明系統自動挑選 AVAILABLE copy。 |
| 讀者歸還申請 | 借閱中或逾期紀錄可提出歸還申請 | 已完成 / 已串接 | 申請後進入 RETURN_PENDING，等待後台審核。 |
| 讀者預約 | 無可借館藏或需要排隊時建立預約 | 已完成 / 已串接 | 可展示等待、已通知、取消等預約狀態。 |
| 訊息中心 | 查看訊息、未讀數、標記已讀、刪除 | 已規劃待補強 | 若刪除 API 尚未完整串接，文件保留但 Demo 可先展示查詢與已讀。 |
| Email / 逾期排程 | 到期前、逾期、逾期 7 日停權通知 | 已規劃待補強 | 適合作為後端架構亮點說明，不一定放入現場操作 Demo。 |
| 密碼加密 | BCrypt 儲存密碼 | 後續優化 | 若目前仍為明文或測試密碼，文件列入後續建議。 |
| 預約自動配書 | 歸還後自動通知第一順位 | 後續優化 | 目前以管理員手動通知為主。 |


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
| BR-V-07 | 管理員通知可借前，後端必須確認該書目至少有一本 `copy_status = AVAILABLE` 的館藏。 |
| BR-V-08 | 管理員通知可借前，後端必須確認該預約為同書目 `WAITING` 狀態中的第一順位。 |
| BR-V-09 | 若無可借館藏，後端應回傳「目前無可借館藏，無法通知」類型錯誤訊息。 |
| BR-V-10 | 預約已通知後，若讀者完成借閱，系統應將對應預約狀態改為 `COMPLETED`。 |

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

### 5.10 統計、報表與 Excel 匯入規則

| 規則編號 | 規則內容 |
|---|---|
| BR-X-01 | 首頁公開統計 API 不需登入，僅回傳不涉及個資的彙總資料。 |
| BR-X-02 | 首頁本月熱門借閱 Top 5 以 `borrow_records.borrow_date` 落在當月區間內的借閱紀錄統計。 |
| BR-X-03 | 管理員統計 API 需驗證登入者為 `ADMIN`。 |
| BR-X-04 | Excel 報表下載需驗證登入者為 `ADMIN`，前端以 `$http`、`arraybuffer` 與 Authorization header 下載。 |
| BR-X-05 | 年度借閱統計 Excel 使用 Apache POI 產生 `.xlsx`，至少包含年度每月借閱統計、熱門書籍排行與讀者借閱排行。 |
| BR-X-06 | 書目與館藏匯入範本一列代表一筆實體館藏；同一本書有多本館藏時，應以多列表示，`copyCode` 不同。 |
| BR-X-07 | Excel 匯入時，`title`、`categoryId`、`copyCode` 為必要欄位，`copyCode` 必須唯一。 |
| BR-X-08 | Excel 匯入時，若 `isbn` 已存在，使用既有書目並新增館藏；若 `isbn` 不存在，建立新書目與館藏。 |
| BR-X-09 | Excel 匯入採單列錯誤不影響其他列的策略，最後回傳總列數、成功筆數、失敗筆數與錯誤明細。 |

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

### UC-12 首頁公開統計與熱門借閱

| 項目 | 說明 |
|---|---|
| 主要角色 | 訪客、讀者、管理員 |
| 前置條件 | 無。 |
| 主要流程 | 1. 使用者進入首頁。<br>2. 前端呼叫公開統計 API。<br>3. 後端回傳館藏書目數、可借館藏數、今日借閱數與本月借閱數。<br>4. 前端呼叫本月熱門借閱 Top 5 API。<br>5. 使用者可點擊熱門書籍進入書籍詳情頁。 |
| 替代流程 | 若尚無借閱資料，熱門借閱區顯示空狀態。 |
| 後置條件 | 首頁可提供未登入使用者有意義的圖書館資訊。 |

### UC-13 管理員統計首頁與 Excel 報表下載

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 管理員已登入。 |
| 主要流程 | 1. 管理員進入後台首頁。<br>2. 前端呼叫管理員統計摘要 API。<br>3. 後端回傳借閱中、逾期、歸還待審核、等待預約、已通知可取、今日借閱與本月借閱統計。<br>4. 管理員可輸入年度與排行榜筆數，下載年度借閱統計 Excel。<br>5. 後端使用 Apache POI 產生 `.xlsx` 檔案。 |
| 替代流程 | 非管理員呼叫時，系統拒絕存取。 |
| 後置條件 | 管理員可用 Excel 報表彙整借閱營運狀況。 |

### UC-14 書目與館藏 Excel 批次匯入

| 項目 | 說明 |
|---|---|
| 主要角色 | 管理員 |
| 前置條件 | 管理員已登入，且已建立必要的書籍分類。 |
| 主要流程 | 1. 管理員進入書目管理頁。<br>2. 下載匯入範本。<br>3. 依範本填寫書目與館藏資料。<br>4. 前端以 multipart/form-data 上傳 Excel。<br>5. 後端逐列讀取資料，建立書目與館藏。<br>6. 系統回傳總列數、成功筆數、失敗筆數與錯誤明細。 |
| 替代流程 | 若分類不存在、館藏條碼重複或必填欄位缺漏，該列記錄錯誤並繼續處理其他列。 |
| 後置條件 | 管理員可批次建立書目與實體館藏。 |

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
| AdminBorrow | PATCH | `/api/admin/borrows/approve-return/batch-normal` | Header Authorization; body: `List<Long>` | `List<BorrowResponse>` | 批次正常歸還審核 | 不新增 DTO；前端直接送借閱 ID 陣列 | UC-06 |

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


### 8.9 Statistics API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| PublicStatistics | GET | `/api/statistics/public/summary` | 無 | `Map(totalBookCount, availableCopyCount, todayBorrowCount, monthBorrowCount)` | 首頁公開統計 | 不需 token；訪客可看 | UC-12 |
| PublicStatistics | GET | `/api/statistics/public/top-borrowed-books` | query: `limit` | `List<Map(bookId, title, author, borrowCount)>` | 本月熱門借閱 Top N | 不需 token；首頁 Top 5 可點進詳情 | UC-12 |
| AdminStatistics | GET | `/api/admin/statistics/summary` | Header Authorization; query: `year`, `month` | `Map(borrowingCount, overdueCount, returnPendingCount, waitingReservationCount, availableNoticeCount, todayBorrowCount, monthBorrowCount)` | 管理員後台統計摘要 | 管理員限定；`year/month` 未傳時可用當月 | UC-13 |

### 8.10 Admin Report / Book Import API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| AdminReport | GET | `/api/admin/reports/borrow-statistics.xlsx` | Header Authorization; query: `year`, `topN` | `.xlsx` binary | 下載年度借閱統計 Excel | 前端需用 `$http`、`responseType: "arraybuffer"` 下載，不能用單純 `window.open` | UC-13 |
| AdminBook | GET | `/api/admin/books/import-template` | Header Authorization | `.xlsx` binary | 下載書目與館藏匯入範本 | API 整合於 `AdminBookController`；前端同樣用 arraybuffer 下載 | UC-14 |
| AdminBook | POST | `/api/admin/books/import` | Header Authorization; multipart/form-data: `file` | `Map(totalRows, successCount, failCount, errors)` | 上傳 Excel 批次匯入書目與館藏 | `Content-Type` 交由瀏覽器自動產生 multipart boundary；回傳錯誤列明細 | UC-14 |

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

| 模組 | Entity | Repository | Service | Controller | DTO / 回傳 |
|---|---|---|---|---|---|
| Auth | User | UserRepository | AuthService / AuthServiceImpl | AuthController | LoginRequest、LoginResponse、RegisterRequest |
| User | User | UserRepository | AdminUserService / AdminUserServiceImpl | UserController、AdminUserController | UserResponse、AdminUserRequest |
| Book Category | BookCategory | BookCategoryRepository | BookCategoryService / BookCategoryServiceImpl | BookCategoryController | BookCategoryResponse |
| Book | Book | BookRepository | BookService、AdminBookService | BookController、AdminBookController | BookRequest、BookListResponse、BookDetailResponse |
| Book Import | Book、BookCopy | BookRepository、BookCopyRepository、BookCategoryRepository | AdminBookImportService / AdminBookImportServiceImpl | AdminBookController | `Map<String,Object>`，不新增 DTO |
| Book Copy | BookCopy | BookCopyRepository | AdminBookCopyService | AdminBookCopyController | BookCopyResponse |
| Borrow | BorrowRecord | BorrowRecordRepository | BorrowService / BorrowServiceImpl | BorrowController、AdminBorrowController | BorrowResponse |
| Reservation | Reservation | ReservationRepository | ReservationService / ReservationServiceImpl | ReservationController、AdminReservationController | ReservationResponse |
| Message | Message | MessageRepository | MessageService / MessageServiceImpl | MessageController | MessageResponse |
| Statistics | Book、BookCopy、BorrowRecord | BookRepository、BookCopyRepository、BorrowRecordRepository | StatisticsService / StatisticsServiceImpl | StatisticsController | `Map<String,Object>`、`List<Map<String,Object>>` |
| Admin Statistics | BorrowRecord、Reservation | BorrowRecordRepository、ReservationRepository | AdminStatisticsService / AdminStatisticsServiceImpl | AdminStatisticsController | `Map<String,Object>` |
| Admin Report | BorrowRecord | BorrowRecordRepository | AdminReportService / AdminReportServiceImpl | AdminReportController | `.xlsx` byte[] |
| Notification | Message、BorrowRecord、Reservation | 無 | NotificationService / NotificationServiceImpl | 無 | 無 |
| Mail | 無 | 無 | MailService / MailServiceImpl | 無 | 無 |
| Scheduler | BorrowRecord、User | BorrowRecordRepository、UserRepository | NotificationService | Scheduler | 無 |
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

> 目前前端採 VSCode 開發，以 AngularJS 1.x 風格撰寫：HTML + JavaScript + `$scope` + `$http`。不使用 TypeScript router。頁面跳轉集中在 `app.js` 的 `IRead.goXXX()` 與 `IRead.bindCommonActions($scope)`。

### 11.1 前端共用架構

| 檔案 / 區塊 | 說明 |
|---|---|
| `js/app.js` | 建立 `LibraryApp`、`IRead` namespace、登入資訊、權限檢查、共用頁面跳轉與共用導覽事件。 |
| `js/api.js` | 集中管理所有後端 API 呼叫，API_BASE 必須包含 `/iread-library/api`。 |
| `partials/reader-nav.html` | 讀者與訪客共用導覽列，提供首頁、館藏查詢、讀者中心、訊息中心、個人資料等入口。 |
| `partials/admin-nav.html` | 管理員跨頁導覽列，僅保留首頁、後台管理、前台館藏查詢、訊息中心、個人資料、登出；後台功能由 `admin.html` tab 處理。 |
| `css/style.css` | 統一頁面色系、卡片、表格、按鈕與響應式樣式。 |

### 11.2 目前主要頁面

| 頁面 | 對應 JS | 功能 |
|---|---|---|
| `index.html` | `index.js` | 首頁；顯示公開統計、熱門借閱 Top 5、登入後摘要與主要功能入口。 |
| `login.html` | `auth.js` | 使用者登入。 |
| `register.html` | `auth.js` | 讀者註冊。 |
| `books.html` | `books.js` | 館藏查詢，訪客、讀者、管理員皆可使用。 |
| `book-detail.html` | `book-detail.js` | 書籍詳情，讀者可借閱或預約；借閱成功導向 `reader.html?tab=borrows`，預約成功導向 `reader.html?tab=reservations`。 |
| `reader.html` | `reader.js` | 讀者中心；以 tab 切換「我的借閱」與「我的預約」。 |
| `admin.html` | `admin.js` | 管理員中心；以 tab 切換 dashboard、書目、館藏、借閱、歸還審核、預約、使用者管理。 |
| `messages.html` | `messages.js` | 訊息中心；依登入角色載入 reader/admin 導覽列。 |
| `profile.html` | `profile.js` | 個人資料頁。 |

### 11.3 `reader.html` 讀者中心 tab

| Tab | 功能 |
|---|---|
| `borrows` | 查看目前借閱、逾期與歸還待審核紀錄，並可申請歸還。 |
| `reservations` | 查看預約紀錄、取消預約，若狀態為 `AVAILABLE_NOTICE` 可借閱。 |

### 11.4 `admin.html` 管理員中心 tab

| Tab | 功能 |
|---|---|
| `dashboard` | 後台統計摘要、Excel 年度借閱報表下載。 |
| `books` | 新增、編輯、查詢書目；下載匯入範本與上傳 Excel 匯入書目與館藏。 |
| `copies` | 新增、編輯、查詢館藏冊本；修改館藏狀態。 |
| `borrows` | 查詢借閱紀錄，可依借閱狀態篩選。 |
| `return-review` | 歸還審核，支援單筆審核與批次正常歸還。 |
| `reservations` | 預約管理，顯示可借館藏數、是否第一順位與可否通知。 |
| `users` | 使用者新增、編輯、查詢、啟用/停用與開通借書權限。 |

### 11.5 頁面跳轉規則

| 目的 | 統一入口 |
|---|---|
| 首頁 | `index.html` |
| 館藏查詢 | `books.html` |
| 書籍詳情 | `book-detail.html?bookId={bookId}` |
| 讀者中心 | `reader.html` |
| 我的借閱 | `reader.html?tab=borrows` |
| 我的預約 | `reader.html?tab=reservations` |
| 後台首頁 | `admin.html?tab=dashboard` |
| 書目管理 | `admin.html?tab=books` |
| 館藏管理 | `admin.html?tab=copies` 或 `admin.html?tab=copies&bookId={bookId}` |
| 借閱紀錄 | `admin.html?tab=borrows` |
| 歸還審核 | `admin.html?tab=return-review` |
| 預約管理 | `admin.html?tab=reservations` |
| 使用者管理 | `admin.html?tab=users` |


### 11.6 Demo / PPT 功能展示區塊對照

#### 11.6.1 管理員端展示區塊

| 展示區塊 | PPT 可呈現的功能說明 | 對應頁面 / Tab | 對應後端重點 |
|---|---|---|---|
| 管理員登入區塊 | 管理員輸入帳號密碼後取得 ADMIN JWT Token，登入後進入後台。 | `login.html` → `admin.html?tab=dashboard` | `AuthController`、JWT、角色判斷。 |
| 後台首頁區塊 | 顯示借閱中、逾期、歸還待審核、預約等待、今日借閱、本月借閱等統計。 | `admin.html?tab=dashboard` | `AdminStatisticsController`、`AdminStatisticsService`。 |
| 書目管理區塊 | 新增、修改、查詢、上架、下架書目資料。 | `admin.html?tab=books` | `AdminBookController`、`AdminBookService`。 |
| 封面 / Excel 區塊 | 上傳或設定封面、下載 Excel 匯入範本、批次匯入書目與館藏。 | `admin.html?tab=books` | `AdminBookImportService`、Apache POI。 |
| 館藏管理區塊 | 管理每一本實體冊本，維護館藏條碼、位置、狀態、備註。 | `admin.html?tab=copies` | `AdminBookCopyController`、`BookCopyRepository`。 |
| 借閱紀錄區塊 | 查看全體借閱紀錄，可依狀態篩選。 | `admin.html?tab=borrows` | `AdminBorrowController`、`BorrowRecordRepository`。 |
| 歸還審核區塊 | 審核讀者歸還申請，可正常歸還、標記毀損或遺失，並支援批次正常歸還。 | `admin.html?tab=return-review` | `approve-return`、`batch-normal` API。 |
| 預約管理區塊 | 查看等待預約，確認可借館藏與第一順位後通知讀者可取。 | `admin.html?tab=reservations` | `AdminReservationController`、`NotificationService`。 |
| 使用者管理區塊 | 新增、編輯、停用/啟用使用者，解除借書暫停狀態。 | `admin.html?tab=users` | `AdminUserController`、`borrow_suspended`。 |

#### 11.6.2 讀者端展示區塊

| 展示區塊 | PPT 可呈現的功能說明 | 對應頁面 / Tab | 對應後端重點 |
|---|---|---|---|
| 讀者註冊區塊 | 訪客可自行建立讀者帳號，預設為 READER。 | `register.html` | `AuthController.register`。 |
| 讀者登入區塊 | 讀者登入後取得 JWT Token，進入讀者功能頁。 | `login.html` → `reader.html` | `AuthController.login`、JWT。 |
| 首頁統計區塊 | 未登入也可看到館藏書目數、可借數、今日借閱、本月借閱與熱門書籍。 | `index.html` | `StatisticsController`。 |
| 館藏查詢區塊 | 依書名、作者、ISBN 查詢書籍，顯示總館藏數與可借數。 | `books.html` | `BookController`。 |
| 書籍詳情區塊 | 顯示書籍資料、館藏狀態，提供借閱或預約操作。 | `book-detail.html?bookId={bookId}` | `BookDetailResponse`、`BorrowController`、`ReservationController`。 |
| 我的借閱區塊 | 查看目前借閱、逾期與歸還待審核紀錄，可提出歸還申請。 | `reader.html?tab=borrows` | `BorrowController`。 |
| 我的預約區塊 | 查看等待、已通知、取消等預約紀錄，可取消預約或在可取後借閱。 | `reader.html?tab=reservations` | `ReservationController`。 |
| 訊息中心區塊 | 查看借閱、歸還、預約、逾期等系統通知，並可標記已讀。 | `messages.html` | `MessageController`。 |
| 個人資料區塊 | 查看目前登入者基本資料。 | `profile.html` | `UserController.getMe`。 |

#### 11.6.3 Demo 建議講法

```text
本系統將一本書的基本資料與實體館藏分開處理。
書目 books 負責記錄書名、作者、ISBN、分類與封面，
實體館藏 book_copies 才是真正會被借出、歸還、毀損或遺失的對象。

讀者端主要展示查詢、借閱、預約、申請歸還與訊息通知；
管理員端則負責書目與館藏維護、歸還審核、預約通知、使用者管理與統計報表。

權限部分使用 JWT，登入後前端把 token 放在 Authorization header，
後端 Interceptor 解析後再由 Service 判斷目前使用者是 READER 或 ADMIN。
```


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
| AC-20 | 前後端可串接 | AngularJS 前端可透過 REST API 完成查詢、借閱、歸還、預約、訊息與管理員維護流程。 |
| AC-21 | 首頁公開統計正常 | 未登入使用者可看到館藏書目數、可借館藏數、今日借閱數、本月借閱數與熱門借閱 Top 5。 |
| AC-22 | 管理員統計正常 | 管理員後台首頁可由單一統計 API 顯示借閱、預約、逾期與待審核摘要。 |
| AC-23 | Excel 報表可下載 | 管理員可下載年度借閱統計 `.xlsx`，且檔案可正常開啟並含多個 sheet。 |
| AC-24 | Excel 匯入書目與館藏 | 管理員可下載範本、上傳 Excel，並看到成功筆數、失敗筆數與錯誤列明細。 |
| AC-25 | 整合頁跳轉正確 | 讀者借閱/預約集中至 `reader.html`，管理員功能集中至 `admin.html`，不再導向已移除的舊頁。 |

---

## 13. 目前版本完成範圍與後續補強

### 13.1 目前版本完成範圍

| 範圍 | 完成內容 | Demo 價值 |
|---|---|---|
| 核心資料模型 | `users`、`books`、`book_copies`、`borrow_records`、`reservations`、`messages` | 可完整說明圖書館資料關聯與狀態轉換。 |
| 權限流程 | 註冊、登入、JWT、READER / ADMIN 角色區分 | 可說明前後端分離下如何保護 API。 |
| 讀者流程 | 查詢館藏、書籍詳情、借閱、申請歸還、預約、訊息 | 可展示完整讀者使用情境。 |
| 管理員流程 | 書目、館藏、借閱紀錄、歸還審核、預約、使用者管理 | 可展示後台維護與審核責任。 |
| 統計與報表 | 首頁公開統計、後台統計、Excel 報表下載 | 可展示系統不只處理 CRUD，也能提供營運資訊。 |
| 批次資料維護 | Excel 範本下載與匯入書目 / 館藏 | 可展示大量資料維護能力。 |

### 13.2 後續補強優先順序

| 優先順序 | 補強項目 | 說明 |
|---|---|---|
| 1 | 確認訊息刪除完整串接 | 若 `DELETE /api/messages/{messageId}` 尚未前後端完整驗證，建議優先補齊。 |
| 2 | 確認 Email 與排程可穩定測試 | 包含 Gmail SMTP 設定、排程觸發時間、測試資料與寄信失敗 log。 |
| 3 | 密碼加密 | 將目前測試密碼或明文儲存改為 BCrypt。 |
| 4 | 匯入錯誤報表下載 | 讓 Excel 匯入失敗列可下載成錯誤清單。 |
| 5 | 預約自動配書 | 歸還完成後自動檢查 WAITING 預約並通知第一順位。 |
| 6 | 圖表化統計 | 後台 dashboard 可加入年度趨勢、熱門排行等圖表。 |

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
    │   ├── StatisticsController.java
    │   ├── UserController.java
    │   └── admin/
    │       ├── AdminBookController.java
    │       ├── AdminBookCopyController.java
    │       ├── AdminBorrowController.java
    │       ├── AdminReportController.java
    │       ├── AdminReservationController.java
    │       ├── AdminStatisticsController.java
    │       └── AdminUserController.java
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
    ├── service/
    │   ├── AdminBookImportService.java
    │   ├── AdminReportService.java
    │   ├── AdminStatisticsService.java
    │   ├── StatisticsService.java
    │   └── impl/
    └── util/
        ├── IdGenerator.java
        └── security/
            ├── JwtUtil.java
            ├── LoginUser.java
            └── LoginUserHolder.java
```

### 15.2 前端實際目錄建議

```text
library-frontend/
├── index.html
├── login.html
├── register.html
├── books.html
├── book-detail.html
├── reader.html
├── admin.html
├── messages.html
├── profile.html
├── partials/
│   ├── reader-nav.html
│   └── admin-nav.html
├── css/
│   └── style.css
└── js/
    ├── app.js
    ├── api.js
    ├── auth.js
    ├── index.js
    ├── books.js
    ├── book-detail.js
    ├── reader.js
    ├── admin.js
    ├── messages.js
    └── profile.js
```

## 16. 後續建議

### 16.1 密碼加密

目前若仍使用明文密碼，建議後續改為 BCrypt：

```text
註冊 / 新增使用者：儲存 BCrypt hash
登入：使用 passwordEncoder.matches(rawPassword, encodedPassword)
```

### 16.2 批次新增館藏

目前已可透過 Excel 匯入達成同一本書多本館藏的批次建立。若後續要提供更快速的後台操作，可另補「同一本書一次新增 N 本館藏」：

```http
POST /api/admin/books/{bookId}/copies/batch
```

此功能適合管理員已建立書目後，直接輸入起始條碼、數量與位置，由系統自動建立多筆 `book_copies`。

### 16.3 Excel 匯入匯出優化

目前規劃已包含年度借閱統計報表下載，以及書目與館藏 Excel 匯入。後續可再優化：

```text
1. 匯入前預檢模式，不直接寫入資料。
2. 匯入結果下載錯誤報表。
3. 範本加入欄位註解、下拉選單與分類代碼參照 sheet。
4. 報表增加待處理清單 sheet，例如逾期、歸還待審核、等待預約通知。
```

### 16.4 預約自動配書

目前預約可取通知由管理員操作。後續可評估：

```text
館藏歸還成 AVAILABLE
→ 系統自動檢查同書目 WAITING 預約
→ 自動通知第一順位讀者
```

此功能需小心處理館藏保留狀態、取書期限與多管理員同時操作問題。

### 16.5 圖表化統計

目前前端以表格呈現統計。若時間允許，可於 `admin.html` dashboard 以簡易圖表顯示：

```text
1. 年度每月借閱趨勢
2. 熱門書籍排行
3. 讀者借閱排行
4. 逾期與歸還待審核數量
```

### 16.6 前端串接與維護建議

```text
1. 所有頁面跳轉集中使用 app.js 的 IRead.goXXX()。
2. 各頁 controller 初始化時呼叫 IRead.bindCommonActions($scope)。
3. 不要在各頁 JS 重新用 location.href 指向已移除的舊頁，例如 my-borrows.html、admin-books.html。
4. 讀者端集中於 reader.html，管理員端集中於 admin.html。
```



---

## 17. v1.4 修訂紀錄

| 修訂項目 | 說明 |
|---|---|
| 版本資訊 | 由 v1.3 更新為 v1.4。 |
| 現有功能摘要 | 新增第 0 章，說明目前版本的功能定位與文件調整方向。 |
| 功能狀態 | 新增 4.5 現有功能狀態總表，區分已完成 / 已串接、已規劃待補強與後續優化。 |
| Demo / PPT 對照 | 新增 11.6，整理管理員端與讀者端可展示功能區塊。 |
| 開發順序 | 將原第 13 章改為目前完成範圍與後續補強，不再以大階段開發為主。 |
| 文件一致性 | 維持 API、頁面、資料表與 Demo 展示內容一致，避免文件列出過多未展示功能。 |
