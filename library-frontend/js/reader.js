// =======================================================
// reader.js
// reader.html - 讀者中心：我的借閱 / 我的預約
// =======================================================

app.controller("ReaderCenterController", ReaderCenterController);


// ==============================
// Controller
// ==============================

function ReaderCenterController($scope, $http) {
    initReaderCenterPage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initReaderCenterPage($scope, $http) {
    if (!IRead.requireReader()) {
        return;
    }

    IRead.bindCommonActions($scope);

    $scope.currentTab = IRead.getQueryParam("tab") || "borrows";
    $scope.errorMessage = "";

    // 我的借閱
    $scope.borrows = [];
    $scope.borrowPage = 0;
    $scope.borrowPageSize = 8;

    // 我的預約
    $scope.reservations = [];
    $scope.reservationPage = 0;
    $scope.reservationPageSize = 8;

    bindReaderCenterActions($scope, $http);

    loadBorrows($scope, $http);
    loadReservations($scope, $http);
}


// ==============================
// 綁定事件
// ==============================

function bindReaderCenterActions($scope, $http) {
    $scope.setReaderTab = function (tab) {
        setReaderTab($scope, $http, tab);
    };

    // 我的借閱
    $scope.loadBorrows = function () {
        loadBorrows($scope, $http);
    };

    $scope.requestReturn = function (borrow) {
        requestReturn($scope, $http, borrow);
    };

    $scope.canRequestReturn = function (borrow) {
        return canRequestReturn(borrow);
    };

    $scope.borrowStatusText = function (status) {
        return borrowStatusText(status);
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

    // 我的預約
    $scope.loadReservations = function () {
        loadReservations($scope, $http);
    };

    $scope.cancelReservation = function (reservation) {
        cancelReservation($scope, $http, reservation);
    };

    $scope.borrowReservedBook = function (reservation) {
        borrowReservedBook($scope, $http, reservation);
    };

    $scope.canBorrowReservedBook = function (reservation) {
        return canBorrowReservedBook(reservation);
    };

    $scope.canCancelReservation = function (reservation) {
        return canCancelReservation(reservation);
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
}


// ==============================
// tab 切換
// ==============================

function setReaderTab($scope, $http, tab) {
    $scope.currentTab = tab;

    if (tab === "borrows") {
        loadBorrows($scope, $http);
        return;
    }

    if (tab === "reservations") {
        loadReservations($scope, $http);
        return;
    }
}


// =======================================================
// 我的借閱
// =======================================================

function loadBorrows($scope, $http) {
    $scope.errorMessage = "";
    $scope.borrowPage = 0;

    IRead.borrowApi.myCurrent($http)
        .then(function (data) {
            console.log("我的借閱：", data);
            $scope.borrows = normalizeList(data);
        }, function (response) {
            handleReaderError($scope, response, "借閱資料載入失敗");
        });
}

function requestReturn($scope, $http, borrow) {
    if (!borrow || !borrow.borrowId) {
        alert("缺少借閱 ID，無法申請歸還");
        return;
    }

    if (!canRequestReturn(borrow)) {
        alert("此筆借閱目前不可申請歸還");
        return;
    }

    var title = borrow.title || borrow.bookTitle || "此書籍";

    if (!confirm("確定要申請歸還《" + title + "》嗎？")) {
        return;
    }

    IRead.borrowApi.requestReturn($http, borrow.borrowId)
        .then(function () {
            alert("已送出歸還申請");
            loadBorrows($scope, $http);
        }, function (response) {
            handleActionError(response, "申請歸還失敗");
        });
}

function canRequestReturn(borrow) {
    if (!borrow) {
        return false;
    }

    return borrow.borrowStatus === "BORROWED" ||
           borrow.borrowStatus === "OVERDUE";
}


// ==============================
// 借閱分頁
// ==============================

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
// 我的預約
// =======================================================

function loadReservations($scope, $http) {
    $scope.errorMessage = "";
    $scope.reservationPage = 0;

    IRead.reservationApi.myList($http)
        .then(function (data) {
            console.log("我的預約：", data);
            $scope.reservations = normalizeList(data);
        }, function (response) {
            handleReaderError($scope, response, "預約資料載入失敗");
        });
}

function cancelReservation($scope, $http, reservation) {
    if (!reservation || !reservation.reservationId) {
        alert("缺少預約 ID，無法取消");
        return;
    }

    if (!canCancelReservation(reservation)) {
        alert("此筆預約目前不可取消");
        return;
    }

    var title = reservation.title || reservation.bookTitle || "此書籍";

    if (!confirm("確定要取消《" + title + "》的預約嗎？")) {
        return;
    }

    IRead.reservationApi.cancel($http, reservation.reservationId)
        .then(function () {
            alert("預約已取消");
            loadReservations($scope, $http);
        }, function (response) {
            handleActionError(response, "取消預約失敗");
        });
}

function borrowReservedBook($scope, $http, reservation) {
    if (!reservation || !reservation.bookId) {
        alert("缺少書籍 ID，無法借閱");
        return;
    }

    if (!canBorrowReservedBook(reservation)) {
        alert("此筆預約目前不可借閱");
        return;
    }

    var title = reservation.title || reservation.bookTitle || "此書籍";

    if (!confirm("確定要借閱《" + title + "》嗎？")) {
        return;
    }

    IRead.borrowApi.borrowBook($http, reservation.bookId)
        .then(function () {
            alert("借閱成功");
            loadReservations($scope, $http);
            loadBorrows($scope, $http);
            $scope.currentTab = "borrows";
        }, function (response) {
            handleActionError(response, "借閱失敗");
        });
}

function canBorrowReservedBook(reservation) {
    if (!reservation) {
        return false;
    }

    return reservation.reservationStatus === "AVAILABLE_NOTICE";
}

function canCancelReservation(reservation) {
    if (!reservation) {
        return false;
    }

    return reservation.reservationStatus === "WAITING" ||
           reservation.reservationStatus === "AVAILABLE_NOTICE";
}


// ==============================
// 預約分頁
// ==============================

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
// 共用文字
// =======================================================

function borrowStatusText(status) {
    if (status === "BORROWED") {
        return "借閱中";
    }

    if (status === "RETURN_PENDING") {
        return "歸還待審核";
    }

    if (status === "RETURNED") {
        return "已歸還";
    }

    if (status === "OVERDUE") {
        return "逾期";
    }

    if (status === "DAMAGED") {
        return "毀損";
    }

    if (status === "LOST") {
        return "遺失";
    }

    return status || "";
}

function reservationStatusText(status) {
    if (status === "WAITING") {
        return "等待中";
    }

    if (status === "AVAILABLE_NOTICE") {
        return "可取書通知";
    }

    if (status === "COMPLETED") {
        return "已完成";
    }

    if (status === "CANCELLED") {
        return "已取消";
    }

    if (status === "EXPIRED") {
        return "已逾期";
    }

    return status || "";
}


// =======================================================
// 共用工具
// =======================================================

function normalizeList(data) {
    if (Array.isArray(data)) {
        return data;
    }

    if (data && Array.isArray(data.content)) {
        return data.content;
    }

    if (data && Array.isArray(data.items)) {
        return data.items;
    }

    if (data && Array.isArray(data.records)) {
        return data.records;
    }

    if (data && Array.isArray(data.borrows)) {
        return data.borrows;
    }

    if (data && Array.isArray(data.borrowList)) {
        return data.borrowList;
    }

    if (data && Array.isArray(data.reservations)) {
        return data.reservations;
    }

    if (data && Array.isArray(data.reservationList)) {
        return data.reservationList;
    }

    return [];
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    return String(value).replace("T", " ").substring(0, 16);
}

function handleReaderError($scope, response, defaultMessage) {
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