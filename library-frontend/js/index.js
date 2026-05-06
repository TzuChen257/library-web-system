// =======================================================
// index.js
// index.html - 系統首頁入口
// =======================================================

app.controller("IndexController", IndexController);


function IndexController($scope, $http) {
    initIndexPage($scope, $http);
}


function initIndexPage($scope, $http) {
    IRead.bindCommonActions($scope);

    $scope.errorMessage = "";

    $scope.publicSummary = {
        totalBookCount: "-",
        availableCopyCount: "-",
        todayBorrowCount: "-",
        monthBorrowCount: "-"
    };

    $scope.topBorrowedBooks = [];

    $scope.readerSummary = {
        borrowingCount: "-",
        waitingReservationCount: "-",
        unreadMessageCount: "-"
    };

    $scope.adminSummary = {
        returnPendingCount: "-",
        waitingReservationCount: "-",
        overdueCount: "-"
    };

    bindIndexActions($scope, $http);

    loadPublicIndexSummary($scope, $http);
    loadUserIndexSummary($scope, $http);
}


function bindIndexActions($scope, $http) {
    $scope.loadIndex = function () {
        loadPublicIndexSummary($scope, $http);
        loadUserIndexSummary($scope, $http);
    };

    $scope.openBookDetail = function (book) {
        if (!book || !book.bookId) {
            alert("缺少書目 ID");
            return;
        }

        IRead.goBookDetail(book.bookId);
    };
}


// =======================================================
// 公開首頁統計：未登入也可看
// =======================================================

function loadPublicIndexSummary($scope, $http) {
    loadPublicSummary($scope, $http);
    loadTopBorrowedBooks($scope, $http);
}

// =======================================================
// 首頁公開統計
// =======================================================

function loadPublicSummary($scope, $http) {
    $scope.errorMessage = "";

    IRead.publicStatisticsApi.summary($http)
        .then(function (data) {
            console.log("首頁公開統計：", data);

            data = data || {};

            $scope.publicSummary.totalBookCount = valueOrDash(data.totalBookCount);
            $scope.publicSummary.availableCopyCount = valueOrDash(data.availableCopyCount);
            $scope.publicSummary.todayBorrowCount = valueOrDash(data.todayBorrowCount);
            $scope.publicSummary.monthBorrowCount = valueOrDash(data.monthBorrowCount);
        }, function (response) {
            console.log("首頁公開統計載入失敗：", response);

            $scope.publicSummary.totalBookCount = "-";
            $scope.publicSummary.availableCopyCount = "-";
            $scope.publicSummary.todayBorrowCount = "-";
            $scope.publicSummary.monthBorrowCount = "-";
        });
}

function valueOrDash(value) {
    if (value === 0) {
        return 0;
    }

    return value || "-";
}

function loadTopBorrowedBooks($scope, $http) {
    if (!IRead.publicStatisticsApi || !IRead.publicStatisticsApi.topBorrowedBooks) {
        console.log("尚未定義 IRead.publicStatisticsApi.topBorrowedBooks");
        $scope.topBorrowedBooks = [];
        return;
    }

    IRead.publicStatisticsApi.topBorrowedBooks($http, 5)
        .then(function (data) {
            console.log("本月熱門借閱 Top 5：", data);
            $scope.topBorrowedBooks = normalizeList(data).slice(0, 5);
        }, function (response) {
            console.log("本月熱門借閱載入失敗：", response);
            $scope.topBorrowedBooks = [];
        });
}


// =======================================================
// 登入後摘要
// =======================================================

function loadUserIndexSummary($scope, $http) {
    var user = IRead.getLoginUser();

    if (!user) {
        return;
    }

    if (user.role === "READER") {
        loadReaderSummary($scope, $http);
        return;
    }

    if (user.role === "ADMIN") {
        loadAdminSummary($scope, $http);
        return;
    }
}

function loadReaderSummary($scope, $http) {
    IRead.borrowApi.myCurrent($http)
        .then(function (data) {
            $scope.readerSummary.borrowingCount = normalizeList(data).length;
        }, function () {
            $scope.readerSummary.borrowingCount = "-";
        });

    IRead.reservationApi.myList($http)
        .then(function (data) {
            $scope.readerSummary.waitingReservationCount =
                countByStatus(normalizeList(data), "reservationStatus", "WAITING");
        }, function () {
            $scope.readerSummary.waitingReservationCount = "-";
        });

    IRead.messageApi.unreadCount($http)
        .then(function (data) {
            $scope.readerSummary.unreadMessageCount = normalizeCount(data);
        }, function () {
            $scope.readerSummary.unreadMessageCount = "-";
        });
}

function loadAdminSummary($scope, $http) {
    IRead.adminBorrowApi.returnPending($http)
        .then(function (data) {
            $scope.adminSummary.returnPendingCount = normalizeList(data).length;
        }, function () {
            $scope.adminSummary.returnPendingCount = "-";
        });

    IRead.adminReservationApi.list($http, {
        reservationStatus: "WAITING"
    }).then(function (data) {
        $scope.adminSummary.waitingReservationCount = normalizeList(data).length;
    }, function () {
        $scope.adminSummary.waitingReservationCount = "-";
    });

    IRead.adminBorrowApi.list($http, {
        borrowStatus: "OVERDUE"
    }).then(function (data) {
        $scope.adminSummary.overdueCount = normalizeList(data).length;
    }, function () {
        $scope.adminSummary.overdueCount = "-";
    });
}


// =======================================================
// 工具
// =======================================================

function normalizeList(data) {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    if (data && Array.isArray(data.items)) return data.items;
    if (data && Array.isArray(data.records)) return data.records;
    if (data && Array.isArray(data.books)) return data.books;
    if (data && Array.isArray(data.bookList)) return data.bookList;
    if (data && Array.isArray(data.borrows)) return data.borrows;
    if (data && Array.isArray(data.reservations)) return data.reservations;
    return [];
}

function normalizeCount(data) {
    if (typeof data === "number") return data;
    if (data && data.unreadCount !== undefined) return Number(data.unreadCount);
    if (data && data.count !== undefined) return Number(data.count);
    return 0;
}

function countByStatus(list, field, status) {
    var count = 0;

    for (var i = 0; i < list.length; i++) {
        if (list[i][field] === status) {
            count++;
        }
    }

    return count;
}

function valueOrDash(value) {
    if (value === 0) {
        return 0;
    }

    return value || "-";
}