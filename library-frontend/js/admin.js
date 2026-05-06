// =======================================================
// admin.js
// admin.html - 管理員中心整合頁
// =======================================================

app.controller("AdminCenterController", AdminCenterController);


// ==============================
// Controller
// ==============================

function AdminCenterController($scope, $http) {
    initAdminCenterPage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initAdminCenterPage($scope, $http) {
    if (!IRead.requireAdmin()) {
        return;
    }

    IRead.bindCommonActions($scope);

    $scope.currentTab = IRead.getQueryParam("tab") || "dashboard";
    $scope.errorMessage = "";

    $scope.summary = {
        borrowingCount: 0,
        overdueCount: 0,
        returnPendingCount: 0,
        waitingReservationCount: 0,
        availableNoticeCount: 0,
        todayBorrowCount: 0,
        monthBorrowCount: 0
    };
    
    // 報表下載條件
    $scope.reportForm = {
        year: new Date().getFullYear(),
        topN: 10
    };

    // 歸還審核狀態
    $scope.pendingBorrows = [];
    $scope.checkAll = false;
    $scope.selectedBorrowIds = [];
    $scope.returnReviewPage = 0;
    $scope.returnReviewPageSize = 8;
    
    // 預約管理狀態
    $scope.reservations = [];
    $scope.reservationSearchForm = {
        reservationStatus: IRead.getQueryParam("reservationStatus") || ""
    };
    $scope.reservationPage = 0;
    $scope.reservationPageSize = 8;

    // 借閱紀錄狀態
    $scope.borrows = [];
    $scope.borrowSearchForm = {
        borrowStatus: IRead.getQueryParam("borrowStatus") || ""
    };
    $scope.borrowPage = 0;
    $scope.borrowPageSize = 8;

    // 書目管理狀態
    $scope.books = [];
    $scope.categories = [];
    $scope.bookSearchForm = {
        keyword: "",
        status: ""
    };
    $scope.bookForm = emptyBookForm();
    $scope.bookPage = 0;
    $scope.bookPageSize = 8;

    // 書目 Excel 匯入狀態
    $scope.bookImportFile = null;
    $scope.bookImportResult = null;

    // 館藏管理狀態
    $scope.fromBookId = IRead.getQueryParam("bookId");
    $scope.copies = [];
    $scope.copySearchForm = {
        bookId: $scope.fromBookId || "",
        copyStatus: "",
        keyword: ""
    };
    $scope.copyForm = emptyCopyForm($scope.fromBookId);
    $scope.copyPage = 0;
    $scope.copyPageSize = 8;

    // 使用者管理狀態
    $scope.users = [];
    $scope.userSearchForm = {
        keyword: "",
        role: "",
        status: ""
    };
    $scope.userForm = emptyUserForm();
    $scope.userPage = 0;
    $scope.userPageSize = 8;

    bindAdminCenterActions($scope, $http);

    loadAdminSummary($scope, $http);
    loadPendingBorrows($scope, $http);
    loadReservations($scope, $http);
    loadBorrows($scope, $http);
    loadCategories($scope, $http);
    loadBooks($scope, $http);
    loadCopies($scope, $http);
    loadUsers($scope, $http);
}


// ==============================
// 綁定事件
// ==============================

function bindAdminCenterActions($scope, $http) {
    $scope.setAdminTab = function (tab) {
        setAdminTab($scope, $http, tab);
    };

    // dashboard
    $scope.loadAdminSummary = function () {
        loadAdminSummary($scope, $http);
    };

    // return-review
    $scope.loadPendingBorrows = function () {
        loadPendingBorrows($scope, $http);
    };

    $scope.approveReturn = function (borrow, resultStatus) {
        approveReturn($scope, $http, borrow, resultStatus);
    };

    $scope.batchApproveNormalReturn = function () {
        batchApproveNormalReturn($scope, $http);
    };

    $scope.toggleCheckAll = function () {
        toggleCheckAll($scope);
    };

    $scope.refreshSelectedBorrowIds = function () {
        refreshSelectedBorrowIds($scope);
    };

    $scope.pagedPendingBorrows = function () {
        return pagedPendingBorrows($scope);
    };

    $scope.numberOfReturnReviewPages = function () {
        return numberOfReturnReviewPages($scope);
    };

    $scope.prevReturnReviewPage = function () {
        prevReturnReviewPage($scope);
    };

    $scope.nextReturnReviewPage = function () {
        nextReturnReviewPage($scope);
    };

    $scope.borrowStatusText = function (status) {
        return borrowStatusText(status);
    };
    // reservations
    $scope.loadReservations = function () {
        loadReservations($scope, $http);
    };

    $scope.clearReservationSearch = function () {
        clearReservationSearch($scope, $http);
    };

    $scope.notifyReservation = function (reservation) {
        notifyReservation($scope, $http, reservation);
    };

    $scope.canNotifyReservation = function (reservation) {
        return canNotifyReservation(reservation);
    };

    $scope.notifyUnavailableReason = function (reservation) {
        return notifyUnavailableReason(reservation);
    };

    $scope.reservationStatusText = function (status) {
        return reservationStatusText(status);
    };

    $scope.pagedReservations = function () {
        return pagedReservations($scope);
    };

    $scope.numberOfReservationPages = function () {
        return numberOfReservationPages($scope);
    };

    $scope.prevReservationPage = function () {
        prevReservationPage($scope);
    };

    $scope.nextReservationPage = function () {
        nextReservationPage($scope);
    };

    $scope.formatDateTime = function (value) {
        return formatDateTime(value);
    };

    // borrows
    $scope.loadBorrows = function () {
        loadBorrows($scope, $http);
    };

    $scope.clearBorrowSearch = function () {
        clearBorrowSearch($scope, $http);
    };

    $scope.pagedBorrows = function () {
        return pagedBorrows($scope);
    };

    $scope.numberOfBorrowPages = function () {
        return numberOfBorrowPages($scope);
    };

    $scope.prevBorrowPage = function () {
        prevBorrowPage($scope);
    };

    $scope.nextBorrowPage = function () {
        nextBorrowPage($scope);
    };

    // books
    $scope.loadBooks = function () {
        loadBooks($scope, $http);
    };

    $scope.clearBookSearch = function () {
        clearBookSearch($scope, $http);
    };

    $scope.submitBook = function () {
        submitBook($scope, $http);
    };

    $scope.editBook = function (book) {
        editBook($scope, book);
    };

    $scope.clearBookForm = function () {
        clearBookForm($scope);
    };

    $scope.changeBookStatus = function (book, status) {
        changeBookStatus($scope, $http, book, status);
    };

    $scope.bookStatusText = function (status) {
        return bookStatusText(status);
    };

    $scope.pagedBooks = function () {
        return pagedBooks($scope);
    };

    $scope.numberOfBookPages = function () {
        return numberOfBookPages($scope);
    };

    $scope.prevBookPage = function () {
        prevBookPage($scope);
    };

    $scope.nextBookPage = function () {
        nextBookPage($scope);
    };

    $scope.downloadBookImportTemplate = function () {
        downloadBookImportTemplate($scope, $http);
    };

    $scope.handleBookImportFileChange = function (input) {
        handleBookImportFileChange($scope, input);
    };

    $scope.importBooksFromExcel = function () {
        importBooksFromExcel($scope, $http);
    };
    // copies
    $scope.loadCopies = function () {
        loadCopies($scope, $http);
    };

    $scope.clearCopySearch = function () {
        clearCopySearch($scope, $http);
    };

    $scope.submitCopy = function () {
        submitCopy($scope, $http);
    };

    $scope.editCopy = function (copy) {
        editCopy($scope, copy);
    };

    $scope.clearCopyForm = function () {
        clearCopyForm($scope);
    };

    $scope.changeCopyStatus = function (copy, copyStatus) {
        changeCopyStatus($scope, $http, copy, copyStatus);
    };

    $scope.copyStatusText = function (status) {
        return copyStatusText(status);
    };

    $scope.pagedCopies = function () {
        return pagedCopies($scope);
    };

    $scope.numberOfCopyPages = function () {
        return numberOfCopyPages($scope);
    };

    $scope.prevCopyPage = function () {
        prevCopyPage($scope);
    };

    $scope.nextCopyPage = function () {
        nextCopyPage($scope);
    };
    
    // users
    $scope.loadUsers = function () {
        loadUsers($scope, $http);
    };

    $scope.clearUserSearch = function () {
        clearUserSearch($scope, $http);
    };

    $scope.submitUser = function () {
        submitUser($scope, $http);
    };

    $scope.editUser = function (user) {
        editUser($scope, user);
    };

    $scope.clearUserForm = function () {
        clearUserForm($scope);
    };

    $scope.changeUserStatus = function (user, status) {
        changeUserStatus($scope, $http, user, status);
    };

    $scope.restoreBorrowPermission = function (user) {
        restoreBorrowPermission($scope, $http, user);
    };

    $scope.isBorrowSuspended = function (user) {
        return isBorrowSuspended(user);
    };

    $scope.pagedUsers = function () {
        return pagedUsers($scope);
    };

    $scope.numberOfUserPages = function () {
        return numberOfUserPages($scope);
    };

    $scope.prevUserPage = function () {
        prevUserPage($scope);
    };

    $scope.nextUserPage = function () {
        nextUserPage($scope);
    };

    // report
    $scope.downloadBorrowStatisticsReport = function () {
        downloadBorrowStatisticsReport($scope, $http);
    };
}


// ==============================
// 切換 tab
// ==============================

function setAdminTab($scope, $http, tab) {
    $scope.currentTab = tab;

    if (tab === "dashboard") {
        loadAdminSummary($scope, $http);
        return;
    }

    if (tab === "return-review") {
        loadPendingBorrows($scope, $http);
        return;
    }

    if (tab === "reservations") {
        loadReservations($scope, $http);
        return;
    }

    if (tab === "borrows") {
        loadBorrows($scope, $http);
        return;
    }
    
    if (tab === "books") {
        loadCategories($scope, $http);
        loadBooks($scope, $http);
        return;
    }

    if (tab === "copies") {
        loadCopies($scope, $http);
        return;
    }

    if (tab === "users") {
        loadUsers($scope, $http);
        return;
    }
}


// =======================================================
// Dashboard
// =======================================================

function loadAdminSummary($scope, $http) {
    $scope.errorMessage = "";

    var now = new Date();

    var params = {
        year: now.getFullYear(),
        month: now.getMonth() + 1
    };

    IRead.adminStatisticsApi.summary($http, params)
        .then(function (data) {
            console.log("管理員統計摘要：", data);

            data = data || {};

            $scope.summary.borrowingCount = valueOrZero(data.borrowingCount);
            $scope.summary.overdueCount = valueOrZero(data.overdueCount);
            $scope.summary.returnPendingCount = valueOrZero(data.returnPendingCount);
            $scope.summary.waitingReservationCount = valueOrZero(data.waitingReservationCount);
            $scope.summary.availableNoticeCount = valueOrZero(data.availableNoticeCount);
            $scope.summary.todayBorrowCount = valueOrZero(data.todayBorrowCount);
            $scope.summary.monthBorrowCount = valueOrZero(data.monthBorrowCount);
        }, function (response) {
            handleAdminError($scope, response, "管理員統計摘要載入失敗");
        });
}

// =======================================================
// Excel 報表下載
// =======================================================

function downloadBorrowStatisticsReport($scope, $http) {
    var year = Number($scope.reportForm.year);
    var topN = Number($scope.reportForm.topN);

    if (!year || year < 2000) {
        alert("請輸入正確年度");
        return;
    }

    if (!topN || topN <= 0) {
        topN = 10;
    }

    IRead.adminReportApi.downloadBorrowStatistics($http, year, topN)
        .then(function (response) {
            var fileName = "borrow-statistics-" + year + ".xlsx";

            downloadExcelFile(response.data, fileName);
        }, function (response) {
            handleActionError(response, "下載報表失敗");
        });
}

function downloadExcelFile(arrayBuffer, fileName) {
    var blob = new Blob([arrayBuffer], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    });

    var url = window.URL.createObjectURL(blob);

    var link = document.createElement("a");
    link.href = url;
    link.download = fileName;

    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
}


// =======================================================
// 歸還審核 return-review
// 從 admin-return-review.js 移植而來
// =======================================================

function loadPendingBorrows($scope, $http) {
    $scope.errorMessage = "";
    $scope.returnReviewPage = 0;

    IRead.adminBorrowApi.returnPending($http)
        .then(function (data) {
            console.log("待審核歸還：", data);
            $scope.pendingBorrows = normalizeList(data);
            clearReturnReviewSelection($scope);
        }, function (response) {
            handleAdminError($scope, response, "待審核資料載入失敗");
        });
}

function approveReturn($scope, $http, borrow, resultStatus) {
    if (!borrow || !borrow.borrowId) {
        alert("缺少借閱 ID，無法審核");
        return;
    }

    var resultText = approveResultText(resultStatus);
    var title = borrow.title || borrow.bookTitle || "此書籍";

    if (!confirm("確定要將《" + title + "》審核為「" + resultText + "」嗎？")) {
        return;
    }

    IRead.adminBorrowApi.approveReturn($http, borrow.borrowId, resultStatus)
        .then(function () {
            alert("審核完成：" + resultText);
            loadPendingBorrows($scope, $http);
            loadAdminSummary($scope, $http);
        }, function (response) {
            handleActionError(response, "歸還審核失敗");
        });
}

function batchApproveNormalReturn($scope, $http) {
    refreshSelectedBorrowIds($scope);

    if (!$scope.selectedBorrowIds || $scope.selectedBorrowIds.length === 0) {
        alert("請先選擇要批次審核的紀錄");
        return;
    }

    if (!confirm("確定要將選取的 " + $scope.selectedBorrowIds.length + " 筆資料批次審核為正常歸還嗎？")) {
        return;
    }

    IRead.adminBorrowApi.batchApproveNormalReturn($http, $scope.selectedBorrowIds)
        .then(function () {
            alert("批次正常歸還完成");
            loadPendingBorrows($scope, $http);
            loadAdminSummary($scope, $http);
        }, function (response) {
            handleActionError(response, "批次正常歸還失敗");
        });
}

function toggleCheckAll($scope) {
    for (var i = 0; i < $scope.pendingBorrows.length; i++) {
        $scope.pendingBorrows[i].selected = $scope.checkAll;
    }

    refreshSelectedBorrowIds($scope);
}

function refreshSelectedBorrowIds($scope) {
    $scope.selectedBorrowIds = [];

    for (var i = 0; i < $scope.pendingBorrows.length; i++) {
        var borrow = $scope.pendingBorrows[i];

        if (borrow.selected) {
            $scope.selectedBorrowIds.push(borrow.borrowId);
        }
    }

    $scope.checkAll =
        $scope.pendingBorrows.length > 0 &&
        $scope.selectedBorrowIds.length === $scope.pendingBorrows.length;
}

function clearReturnReviewSelection($scope) {
    $scope.checkAll = false;
    $scope.selectedBorrowIds = [];
}

function pagedPendingBorrows($scope) {
    var start = $scope.returnReviewPage * $scope.returnReviewPageSize;
    return $scope.pendingBorrows.slice(start, start + $scope.returnReviewPageSize);
}

function numberOfReturnReviewPages($scope) {
    if (!$scope.pendingBorrows || $scope.pendingBorrows.length === 0) {
        return 1;
    }

    return Math.ceil($scope.pendingBorrows.length / $scope.returnReviewPageSize);
}

function prevReturnReviewPage($scope) {
    if ($scope.returnReviewPage > 0) {
        $scope.returnReviewPage--;
    }
}

function nextReturnReviewPage($scope) {
    if ($scope.returnReviewPage < numberOfReturnReviewPages($scope) - 1) {
        $scope.returnReviewPage++;
    }
}

// =======================================================
// 預約管理 reservations
// 從 admin-reservations.js 移植而來
// =======================================================

function loadReservations($scope, $http) {
    $scope.errorMessage = "";
    $scope.reservationPage = 0;

    IRead.adminReservationApi.list($http, $scope.reservationSearchForm)
        .then(function (data) {
            console.log("管理員預約紀錄：", data);
            $scope.reservations = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "預約紀錄載入失敗");
        });
}

function clearReservationSearch($scope, $http) {
    $scope.reservationSearchForm = {
        reservationStatus: ""
    };

    loadReservations($scope, $http);
}

function notifyReservation($scope, $http, reservation) {
    if (!reservation || !reservation.reservationId) {
        alert("缺少預約 ID，無法通知");
        return;
    }

    if (!canNotifyReservation(reservation)) {
        alert("此筆預約目前不可通知");
        return;
    }

    var title = reservation.title || reservation.bookTitle || "此書籍";
    var reader = reservation.username ||
                 reservation.userName ||
                 reservation.readerName ||
                 reservation.name ||
                 reservation.userId ||
                 "該讀者";

    if (!confirm("確定要通知「" + reader + "」可借閱《" + title + "》嗎？")) {
        return;
    }

    IRead.adminReservationApi.notify($http, reservation.reservationId)
        .then(function () {
            alert("已通知讀者可借");

            loadReservations($scope, $http);
            loadAdminSummary($scope, $http);
        }, function (response) {
            handleActionError(response, "通知可借失敗");
        });
}

function canNotifyReservation(reservation) {
    if (!reservation) {
        return false;
    }

    return reservation.canNotify === true;
}

function notifyUnavailableReason(reservation) {
    if (!reservation) {
        return "-";
    }

    if (reservation.reservationStatus === "AVAILABLE_NOTICE") {
        return "已通知";
    }

    if (reservation.reservationStatus === "CANCELLED") {
        return "已取消";
    }

    if (reservation.reservationStatus === "COMPLETED") {
        return "已完成";
    }

    if (reservation.reservationStatus === "EXPIRED") {
        return "已逾期";
    }

    if (reservation.reservationStatus !== "WAITING") {
        return "-";
    }

    if (Number(reservation.availableCopyCount || 0) <= 0) {
        return "無可借館藏";
    }

    if (reservation.firstInQueue !== true) {
        return "非第一順位";
    }

    return "-";
}

function pagedReservations($scope) {
    var start = $scope.reservationPage * $scope.reservationPageSize;
    return $scope.reservations.slice(start, start + $scope.reservationPageSize);
}

function numberOfReservationPages($scope) {
    if (!$scope.reservations || $scope.reservations.length === 0) {
        return 1;
    }

    return Math.ceil($scope.reservations.length / $scope.reservationPageSize);
}

function prevReservationPage($scope) {
    if ($scope.reservationPage > 0) {
        $scope.reservationPage--;
    }
}

function nextReservationPage($scope) {
    if ($scope.reservationPage < numberOfReservationPages($scope) - 1) {
        $scope.reservationPage++;
    }
}

// =======================================================
// 借閱紀錄 borrows
// 從 admin-borrows.js 移植而來
// =======================================================

function loadBorrows($scope, $http) {
    $scope.errorMessage = "";
    $scope.borrowPage = 0;

    IRead.adminBorrowApi.list($http, $scope.borrowSearchForm)
        .then(function (data) {
            console.log("管理員借閱紀錄：", data);
            $scope.borrows = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "借閱紀錄載入失敗");
        });
}

function clearBorrowSearch($scope, $http) {
    $scope.borrowSearchForm = {
        borrowStatus: ""
    };

    loadBorrows($scope, $http);
}

function pagedBorrows($scope) {
    var start = $scope.borrowPage * $scope.borrowPageSize;
    return $scope.borrows.slice(start, start + $scope.borrowPageSize);
}

function numberOfBorrowPages($scope) {
    if (!$scope.borrows || $scope.borrows.length === 0) {
        return 1;
    }

    return Math.ceil($scope.borrows.length / $scope.borrowPageSize);
}

function prevBorrowPage($scope) {
    if ($scope.borrowPage > 0) {
        $scope.borrowPage--;
    }
}

function nextBorrowPage($scope) {
    if ($scope.borrowPage < numberOfBorrowPages($scope) - 1) {
        $scope.borrowPage++;
    }
}

// =======================================================
// 書目管理 books
// 從 admin-books.js 移植而來
// =======================================================

function emptyBookForm() {
    return {
        bookId: "",
        isbn: "",
        title: "",
        author: "",
        publisher: "",
        publishYear: "",
        categoryId: "",
        description: "",
        coverUrl: "",
        status: "ACTIVE"
    };
}

function clearBookForm($scope) {
    $scope.bookForm = emptyBookForm();
    $scope.errorMessage = "";
}

function editBook($scope, book) {
    $scope.bookForm = {
        bookId: book.bookId,
        isbn: book.isbn,
        title: book.title,
        author: book.author,
        publisher: book.publisher,
        publishYear: book.publishYear,
        categoryId: book.categoryId,
        description: book.description,
        coverUrl: book.coverUrl,
        status: book.status || "ACTIVE"
    };

    window.scrollTo(0, 0);
}

function validateBookForm($scope) {
    if (!$scope.bookForm.title) {
        $scope.errorMessage = "請輸入書名";
        return false;
    }

    if (!$scope.bookForm.author) {
        $scope.errorMessage = "請輸入作者";
        return false;
    }

    if (!$scope.bookForm.categoryId) {
        $scope.errorMessage = "請選擇分類";
        return false;
    }

    if (!$scope.bookForm.status) {
        $scope.errorMessage = "請選擇書目狀態";
        return false;
    }

    return true;
}

function buildBookRequest(form) {
    return {
        isbn: form.isbn,
        title: form.title,
        author: form.author,
        publisher: form.publisher,
        publishYear: form.publishYear,
        categoryId: form.categoryId,
        description: form.description,
        coverUrl: form.coverUrl,
        status: form.status
    };
}

// =======================================================
// 書目與館藏 Excel 匯入
// =======================================================

function downloadBookImportTemplate($scope, $http) {
    IRead.adminBookImportApi.downloadTemplate($http)
        .then(function (response) {
            downloadExcelFile(response.data, "book-import-template.xlsx");
        }, function (response) {
            handleActionError(response, "下載匯入範本失敗");
        });
}

function handleBookImportFileChange($scope, input) {
    $scope.$apply(function () {
        if (input.files && input.files.length > 0) {
            $scope.bookImportFile = input.files[0];
        } else {
            $scope.bookImportFile = null;
        }

        $scope.bookImportResult = null;
    });
}

function importBooksFromExcel($scope, $http) {
    if (!$scope.bookImportFile) {
        alert("請先選擇 Excel 檔案");
        return;
    }

    var fileName = $scope.bookImportFile.name || "";

    if (!isExcelFile(fileName)) {
        alert("請選擇 .xlsx 或 .xls 檔案");
        return;
    }

    if (!confirm("確定要匯入這份書目與館藏 Excel 嗎？")) {
        return;
    }

    IRead.adminBookImportApi.importBooks($http, $scope.bookImportFile)
        .then(function (data) {
            console.log("書目匯入結果：", data);

            $scope.bookImportResult = data || {
                totalRows: 0,
                successCount: 0,
                failCount: 0,
                errors: []
            };

            alert("匯入完成，成功 " + valueOrZero($scope.bookImportResult.successCount) + " 筆，失敗 " + valueOrZero($scope.bookImportResult.failCount) + " 筆");

            loadBooks($scope, $http);
            loadCopies($scope, $http);
            loadAdminSummary($scope, $http);
        }, function (response) {
            handleActionError(response, "書目與館藏匯入失敗");
        });
}

function isExcelFile(fileName) {
    fileName = String(fileName || "").toLowerCase();

    return fileName.endsWith(".xlsx") || fileName.endsWith(".xls");
}

// ==============================
// 分類
// ==============================

function loadCategories($scope, $http) {
    IRead.bookCategoryApi.list($http)
        .then(function (data) {
            console.log("分類清單：", data);
            $scope.categories = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "分類資料載入失敗");
        });
}


// ==============================
// 查詢書目
// ==============================

function loadBooks($scope, $http) {
    $scope.errorMessage = "";
    $scope.bookPage = 0;

    IRead.adminBookApi.list($http, $scope.bookSearchForm)
        .then(function (data) {
            console.log("管理員書目清單：", data);
            $scope.books = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "書目資料載入失敗");
        });
}

function clearBookSearch($scope, $http) {
    $scope.bookSearchForm = {
        keyword: "",
        status: ""
    };

    loadBooks($scope, $http);
}


// ==============================
// 新增 / 編輯書目
// ==============================

function submitBook($scope, $http) {
    $scope.errorMessage = "";

    if (!validateBookForm($scope)) {
        return;
    }

    var body = buildBookRequest($scope.bookForm);

    if ($scope.bookForm.bookId) {
        updateBook($scope, $http, $scope.bookForm.bookId, body);
    } else {
        createBook($scope, $http, body);
    }
}

function createBook($scope, $http, body) {
    IRead.adminBookApi.create($http, body)
        .then(function () {
            alert("新增書目成功");
            clearBookForm($scope);
            loadBooks($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "新增書目失敗");
        });
}

function updateBook($scope, $http, bookId, body) {
    IRead.adminBookApi.update($http, bookId, body)
        .then(function () {
            alert("修改書目成功");
            clearBookForm($scope);
            loadBooks($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "修改書目失敗");
        });
}


// ==============================
// 上架 / 下架
// ==============================

function changeBookStatus($scope, $http, book, status) {
    if (!book || !book.bookId) {
        alert("缺少書目 ID");
        return;
    }

    var text = status === "ACTIVE" ? "上架" : "下架";

    if (!confirm("確定要將《" + book.title + "》" + text + "嗎？")) {
        return;
    }

    IRead.adminBookApi.changeStatus($http, book.bookId, status)
        .then(function () {
            alert(text + "成功");
            loadBooks($scope, $http);
        }, function (response) {
            handleActionError(response, text + "失敗");
        });
}


// ==============================
// 書目分頁
// ==============================

function pagedBooks($scope) {
    var start = $scope.bookPage * $scope.bookPageSize;
    return $scope.books.slice(start, start + $scope.bookPageSize);
}

function numberOfBookPages($scope) {
    if (!$scope.books || $scope.books.length === 0) {
        return 1;
    }

    return Math.ceil($scope.books.length / $scope.bookPageSize);
}

function prevBookPage($scope) {
    if ($scope.bookPage > 0) {
        $scope.bookPage--;
    }
}

function nextBookPage($scope) {
    if ($scope.bookPage < numberOfBookPages($scope) - 1) {
        $scope.bookPage++;
    }
}

// =======================================================
// 館藏管理 copies
// 從 admin-book-copies.js 移植而來
// =======================================================

function emptyCopyForm(bookId) {
    return {
        copyId: "",
        bookId: bookId || "",
        copyCode: "",
        location: "",
        copyStatus: "AVAILABLE",
        note: ""
    };
}

function clearCopyForm($scope) {
    $scope.copyForm = emptyCopyForm($scope.fromBookId);
    $scope.errorMessage = "";
}

function editCopy($scope, copy) {
    $scope.copyForm = {
        copyId: copy.copyId,
        bookId: copy.bookId,
        copyCode: copy.copyCode,
        location: copy.location,
        copyStatus: copy.copyStatus || "AVAILABLE",
        note: copy.note || ""
    };

    window.scrollTo(0, 0);
}

function validateCopyForm($scope) {
    if (!$scope.copyForm.bookId) {
        $scope.errorMessage = "請輸入書目 ID";
        return false;
    }

    if (!$scope.copyForm.copyCode) {
        $scope.errorMessage = "請輸入館藏條碼";
        return false;
    }

    if ($scope.copyForm.copyId && !$scope.copyForm.copyStatus) {
        $scope.errorMessage = "請選擇館藏狀態";
        return false;
    }

    return true;
}

function buildCopyRequest(form) {
    return {
        copyCode: form.copyCode,
        location: form.location,
        copyStatus: form.copyStatus,
        note: form.note
    };
}


// ==============================
// 查詢館藏
// ==============================

function loadCopies($scope, $http) {
    $scope.errorMessage = "";
    $scope.copyPage = 0;

    IRead.adminBookCopyApi.list($http, $scope.copySearchForm)
        .then(function (data) {
            console.log("館藏冊本清單：", data);
            $scope.copies = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "館藏冊本資料載入失敗");
        });
}

function clearCopySearch($scope, $http) {
    $scope.copySearchForm = {
        bookId: $scope.fromBookId || "",
        copyStatus: "",
        keyword: ""
    };

    loadCopies($scope, $http);
}


// ==============================
// 新增 / 編輯館藏
// ==============================

function submitCopy($scope, $http) {
    $scope.errorMessage = "";

    if (!validateCopyForm($scope)) {
        return;
    }

    var body = buildCopyRequest($scope.copyForm);

    if ($scope.copyForm.copyId) {
        updateCopy($scope, $http, $scope.copyForm.copyId, body);
    } else {
        createCopy($scope, $http, $scope.copyForm.bookId, body);
    }
}

function createCopy($scope, $http, bookId, body) {
    IRead.adminBookCopyApi.create($http, bookId, body)
        .then(function () {
            alert("新增館藏成功");
            clearCopyForm($scope);
            loadCopies($scope, $http);
            loadBooks($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "新增館藏失敗");
        });
}

function updateCopy($scope, $http, copyId, body) {
    IRead.adminBookCopyApi.update($http, copyId, body)
        .then(function () {
            alert("修改館藏成功");
            clearCopyForm($scope);
            loadCopies($scope, $http);
            loadBooks($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "修改館藏失敗");
        });
}


// ==============================
// 修改館藏狀態
// ==============================

function changeCopyStatus($scope, $http, copy, copyStatus) {
    if (!copy || !copy.copyId) {
        alert("缺少館藏 ID");
        return;
    }

    var text = copyStatusText(copyStatus);

    if (!confirm("確定要將館藏「" + copy.copyCode + "」改為「" + text + "」嗎？")) {
        return;
    }

    IRead.adminBookCopyApi.changeStatus($http, copy.copyId, copyStatus)
        .then(function () {
            alert("館藏狀態修改成功");
            loadCopies($scope, $http);
            loadBooks($scope, $http);
        }, function (response) {
            handleActionError(response, "館藏狀態修改失敗");
        });
}


// ==============================
// 館藏分頁
// ==============================

function pagedCopies($scope) {
    var start = $scope.copyPage * $scope.copyPageSize;
    return $scope.copies.slice(start, start + $scope.copyPageSize);
}

function numberOfCopyPages($scope) {
    if (!$scope.copies || $scope.copies.length === 0) {
        return 1;
    }

    return Math.ceil($scope.copies.length / $scope.copyPageSize);
}

function prevCopyPage($scope) {
    if ($scope.copyPage > 0) {
        $scope.copyPage--;
    }
}

function nextCopyPage($scope) {
    if ($scope.copyPage < numberOfCopyPages($scope) - 1) {
        $scope.copyPage++;
    }
}

// =======================================================
// 使用者管理 users
// 從 admin-users.js 移植而來
// =======================================================

function emptyUserForm() {
    return {
        userId: "",
        username: "",
        password: "",
        name: "",
        email: "",
        phone: "",
        role: "READER",
        status: "ACTIVE"
    };
}

function clearUserForm($scope) {
    $scope.userForm = emptyUserForm();
    $scope.errorMessage = "";
}

function editUser($scope, user) {
    $scope.userForm = {
        userId: user.userId,
        username: user.username,
        password: "",
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role,
        status: user.status
    };

    window.scrollTo(0, 0);
}

function validateUserForm($scope) {
    if (!$scope.userForm.username) {
        $scope.errorMessage = "請輸入帳號";
        return false;
    }

    if (!$scope.userForm.userId && !$scope.userForm.password) {
        $scope.errorMessage = "新增使用者時請輸入密碼";
        return false;
    }

    if (!$scope.userForm.name) {
        $scope.errorMessage = "請輸入姓名";
        return false;
    }

    if (!$scope.userForm.email) {
        $scope.errorMessage = "請輸入 Email";
        return false;
    }

    if (!$scope.userForm.role) {
        $scope.errorMessage = "請選擇角色";
        return false;
    }

    if (!$scope.userForm.status) {
        $scope.errorMessage = "請選擇帳號狀態";
        return false;
    }

    return true;
}

function buildUserRequest(form) {
    var body = {
        username: form.username,
        password: form.password,
        name: form.name,
        email: form.email,
        phone: form.phone,
        role: form.role,
        status: form.status
    };

    if (form.userId && !form.password) {
        delete body.password;
    }

    return body;
}


// ==============================
// 查詢使用者
// ==============================

function loadUsers($scope, $http) {
    $scope.errorMessage = "";
    $scope.userPage = 0;

    IRead.adminUserApi.list($http, $scope.userSearchForm)
        .then(function (data) {
            console.log("使用者清單：", data);
            $scope.users = normalizeList(data);
        }, function (response) {
            handleAdminError($scope, response, "使用者資料載入失敗");
        });
}

function clearUserSearch($scope, $http) {
    $scope.userSearchForm = {
        keyword: "",
        role: "",
        status: ""
    };

    loadUsers($scope, $http);
}


// ==============================
// 新增 / 編輯使用者
// ==============================

function submitUser($scope, $http) {
    $scope.errorMessage = "";

    if (!validateUserForm($scope)) {
        return;
    }

    var body = buildUserRequest($scope.userForm);

    if ($scope.userForm.userId) {
        updateUser($scope, $http, $scope.userForm.userId, body);
    } else {
        createUser($scope, $http, body);
    }
}

function createUser($scope, $http, body) {
    IRead.adminUserApi.create($http, body)
        .then(function () {
            alert("新增使用者成功");
            clearUserForm($scope);
            loadUsers($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "新增使用者失敗");
        });
}

function updateUser($scope, $http, userId, body) {
    IRead.adminUserApi.update($http, userId, body)
        .then(function () {
            alert("修改使用者成功");
            clearUserForm($scope);
            loadUsers($scope, $http);
        }, function (response) {
            handleAdminError($scope, response, "修改使用者失敗");
        });
}


// ==============================
// 啟用 / 停用帳號
// ==============================

function changeUserStatus($scope, $http, user, status) {
    if (!user || !user.userId) {
        alert("缺少使用者 ID");
        return;
    }

    var text = status === "ACTIVE" ? "啟用" : "停用";

    if (!confirm("確定要" + text + "此帳號嗎？")) {
        return;
    }

    IRead.adminUserApi.changeStatus($http, user.userId, status)
        .then(function () {
            alert(text + "帳號成功");
            loadUsers($scope, $http);
        }, function (response) {
            handleActionError(response, text + "帳號失敗");
        });
}


// ==============================
// 開通借書權限
// ==============================

function restoreBorrowPermission($scope, $http, user) {
    if (!user || !user.userId) {
        alert("缺少使用者 ID");
        return;
    }

    if (!confirm("確定要開通此使用者的借書權限嗎？")) {
        return;
    }

    IRead.adminUserApi.restoreBorrowPermission($http, user.userId)
        .then(function () {
            alert("借書權限已開通");
            loadUsers($scope, $http);
        }, function (response) {
            handleActionError(response, "開通借書權限失敗");
        });
}


// ==============================
// 借書權限判斷
// ==============================

function isBorrowSuspended(user) {
    if (!user) {
        return false;
    }

    return user.borrowSuspended === true ||
           user.borrowSuspended === "true" ||
           user.borrow_suspended === true ||
           user.borrow_suspended === "true";
}


// ==============================
// 使用者分頁
// ==============================

function pagedUsers($scope) {
    var start = $scope.userPage * $scope.userPageSize;
    return $scope.users.slice(start, start + $scope.userPageSize);
}

function numberOfUserPages($scope) {
    if (!$scope.users || $scope.users.length === 0) {
        return 1;
    }

    return Math.ceil($scope.users.length / $scope.userPageSize);
}

function prevUserPage($scope) {
    if ($scope.userPage > 0) {
        $scope.userPage--;
    }
}

function nextUserPage($scope) {
    if ($scope.userPage < numberOfUserPages($scope) - 1) {
        $scope.userPage++;
    }
}

// =======================================================
// 共用文字
// =======================================================

function borrowStatusText(status) {
    if (status === "BORROWED") return "借閱中";
    if (status === "RETURN_PENDING") return "歸還待審核";
    if (status === "RETURNED") return "已歸還";
    if (status === "OVERDUE") return "逾期";
    if (status === "DAMAGED") return "毀損";
    if (status === "LOST") return "遺失";
    return status || "";
}

function approveResultText(resultStatus) {
    if (resultStatus === "RETURNED") return "正常歸還";
    if (resultStatus === "DAMAGED") return "毀損";
    if (resultStatus === "LOST") return "遺失";
    return resultStatus || "";
}

function reservationStatusText(status) {
    if (status === "WAITING") return "等待中";
    if (status === "AVAILABLE_NOTICE") return "可取書通知";
    if (status === "COMPLETED") return "已完成";
    if (status === "CANCELLED") return "已取消";
    if (status === "EXPIRED") return "已逾期";
    return status || "";
}

function formatDateTime(value) {
    if (!value) return "";
    return String(value).replace("T", " ").substring(0, 16);
}

function bookStatusText(status) {
    if (status === "ACTIVE") return "上架";
    if (status === "DISABLED") return "下架";
    return status || "";
}

function copyStatusText(status) {
    if (status === "AVAILABLE") return "可借閱";
    if (status === "BORROWED") return "借出中";
    if (status === "RETURN_PENDING") return "歸還待審核";
    if (status === "RESERVED") return "預約保留";
    if (status === "DAMAGED") return "毀損";
    if (status === "LOST") return "遺失";
    if (status === "REMOVED") return "下架";
    return status || "";
}


// =======================================================
// 共用工具
// =======================================================

function normalizeList(data) {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    if (data && Array.isArray(data.items)) return data.items;
    if (data && Array.isArray(data.records)) return data.records;
    if (data && Array.isArray(data.borrows)) return data.borrows;
    if (data && Array.isArray(data.borrowList)) return data.borrowList;
    if (data && Array.isArray(data.reservations)) return data.reservations;
    if (data && Array.isArray(data.reservationList)) return data.reservationList;
    return [];
}

function handleAdminError($scope, response, defaultMessage) {
    console.log(defaultMessage + "：", response);

    if (response && response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = defaultMessage;
    }
}

function handleActionError(response, defaultMessage) {
    console.log(defaultMessage + "：", response);

    if (response && response.data && response.data.message) {
        alert(response.data.message);
    } else {
        alert(defaultMessage);
    }
}

function valueOrZero(value) {
    if (value === 0) {
        return 0;
    }

    return value || 0;
}