// =======================================================
// book-detail.js
// book-detail.html - 書籍詳情 / 借閱 / 預約
// =======================================================

app.controller("BookDetailController", BookDetailController);


// ==============================
// Controller
// ==============================

function BookDetailController($scope, $http) {
    initBookDetailPage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initBookDetailPage($scope, $http) {
    $scope.loginUser = IRead.getLoginUser();
    IRead.bindCommonActions($scope);

    $scope.bookId = IRead.getQueryParam("bookId");
    $scope.book = null;
    $scope.copies = [];

    $scope.errorMessage = "";

    bindBookDetailActions($scope, $http);

    if (!$scope.bookId) {
        $scope.errorMessage = "缺少書籍 ID，無法查詢書籍詳情";
        return;
    }

    loadBookDetail($scope, $http);
}


// ==============================
// 綁定畫面事件
// ==============================

function bindBookDetailActions($scope, $http) {
    $scope.loadBookDetail = function () {
        loadBookDetail($scope, $http);
    };

    $scope.borrowBook = function () {
        borrowBook($scope, $http);
    };

    $scope.reserveBook = function () {
        reserveBook($scope, $http);
    };

    $scope.canShowBorrowButton = function () {
        return canShowBorrowButton($scope);
    };

    $scope.canShowReserveButton = function () {
        return canShowReserveButton($scope);
    };

    $scope.isBorrowDisabled = function () {
        return isBorrowDisabled($scope);
    };

    $scope.isBorrowSuspended = function () {
        return isBorrowSuspended($scope);
    };

    $scope.bookStatusText = function (status) {
        return bookStatusText(status);
    };

    $scope.copyStatusText = function (status) {
        return copyStatusText(status);
    };
}


// ==============================
// API：查詢書籍詳情
// ==============================

function loadBookDetail($scope, $http) {
    $scope.errorMessage = "";

    IRead.bookApi.get($http, $scope.bookId)
        .then(function (data) {
            console.log("書籍詳情：", data);
            handleBookDetailSuccess($scope, data);
        }, function (response) {
            handleBookDetailError($scope, response);
        });
}

function handleBookDetailSuccess($scope, data) {
    if (!data) {
        $scope.errorMessage = "查無書籍資料";
        return;
    }

    $scope.book = normalizeBookDetail(data);
    $scope.copies = normalizeCopyList(data);
}

function handleBookDetailError($scope, response) {
    console.log("書籍詳情查詢失敗：", response);

    if (response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = "書籍詳情載入失敗";
    }
}


// ==============================
// API：借閱
// ==============================

function borrowBook($scope, $http) {
    if (!validateBeforeReaderAction($scope)) {
        return;
    }

    if (isBorrowSuspended($scope)) {
        alert("你的借書權限已暫停，請聯繫管理員");
        return;
    }

    if (!confirm("確定要借閱《" + $scope.book.title + "》嗎？")) {
        return;
    }

    IRead.borrowApi.borrowBook($http, $scope.bookId)
        .then(function (data) {
            console.log("借閱成功：", data);
            alert("借閱成功");

            // 借閱成功後進入讀者中心｜我的借閱
            IRead.goReaderCenter("borrows");
        }, function (response) {
            handleActionError("借閱失敗", response);
        });
}


// ==============================
// API：預約
// ==============================

function reserveBook($scope, $http) {
    if (!validateBeforeReaderAction($scope)) {
        return;
    }

    if (!confirm("確定要預約《" + $scope.book.title + "》嗎？")) {
        return;
    }

    IRead.reservationApi.create($http, $scope.bookId)
        .then(function (data) {
            console.log("預約成功：", data);
            alert("預約成功");

            // 預約成功後進入讀者中心｜我的預約
            IRead.goReaderCenter("reservations");
        }, function (response) {
            handleActionError("預約失敗", response);
        });
}


// ==============================
// 操作前驗證
// ==============================

function validateBeforeReaderAction($scope) {
    if (!IRead.getToken()) {
        alert("請先登入");
        location.href = "login.html";
        return false;
    }

    if (!$scope.loginUser || $scope.loginUser.role !== "READER") {
        alert("此功能限讀者使用");
        return false;
    }

    if (!$scope.book) {
        alert("書籍資料尚未載入");
        return false;
    }

    return true;
}

function handleActionError(defaultMessage, response) {
    console.log(defaultMessage + "：", response);

    if (response.data && response.data.message) {
        alert(response.data.message);
    } else {
        alert(defaultMessage);
    }
}


// ==============================
// 顯示按鈕條件
// ==============================

function canShowBorrowButton($scope) {
    if (!$scope.loginUser) {
        return false;
    }

    if ($scope.loginUser.role !== "READER") {
        return false;
    }

    if (!$scope.book) {
        return false;
    }

    return getAvailableCopyCount($scope.book) > 0;
}

function canShowReserveButton($scope) {
    if (!$scope.loginUser) {
        return false;
    }

    if ($scope.loginUser.role !== "READER") {
        return false;
    }

    if (!$scope.book) {
        return false;
    }

    return getAvailableCopyCount($scope.book) <= 0;
}

function isBorrowDisabled($scope) {
    return isBorrowSuspended($scope);
}

function isBorrowSuspended($scope) {
    if (!$scope.loginUser) {
        return false;
    }

    return $scope.loginUser.borrowSuspended === true ||
           $scope.loginUser.borrowSuspended === "true" ||
           $scope.loginUser.borrow_suspended === true ||
           $scope.loginUser.borrow_suspended === "true";
}

function getAvailableCopyCount(book) {
    if (!book) {
        return 0;
    }

    if (book.availableCopyCount !== undefined && book.availableCopyCount !== null) {
        return Number(book.availableCopyCount);
    }

    if (book.availableCopies !== undefined && book.availableCopies !== null) {
        return Number(book.availableCopies);
    }

    return 0;
}


// ==============================
// 資料整理
// ==============================

function normalizeBookDetail(data) {
    var book = data;

    if (data.book) {
        book = data.book;
    }

    return book;
}

function normalizeCopyList(data) {
    if (!data) {
        return [];
    }

    if (Array.isArray(data.copies)) {
        return data.copies;
    }

    if (Array.isArray(data.bookCopies)) {
        return data.bookCopies;
    }

    if (Array.isArray(data.copyList)) {
        return data.copyList;
    }

    if (data.book && Array.isArray(data.book.copies)) {
        return data.book.copies;
    }

    if (data.book && Array.isArray(data.book.bookCopies)) {
        return data.book.bookCopies;
    }

    return [];
}


// ==============================
// 狀態文字
// ==============================

function bookStatusText(status) {
    if (status === "ACTIVE") {
        return "上架";
    }

    if (status === "DISABLED") {
        return "下架";
    }

    return status || "";
}

function copyStatusText(status) {
    if (status === "AVAILABLE") {
        return "可借閱";
    }

    if (status === "BORROWED") {
        return "借出中";
    }

    if (status === "RETURN_PENDING") {
        return "歸還待審核";
    }

    if (status === "RESERVED") {
        return "預約保留";
    }

    if (status === "DAMAGED") {
        return "毀損";
    }

    if (status === "LOST") {
        return "遺失";
    }

    if (status === "REMOVED") {
        return "下架";
    }

    return status || "";
}