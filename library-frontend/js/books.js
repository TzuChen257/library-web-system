// =======================================================
// books.js
// books.html - 館藏查詢
// =======================================================

app.controller("BookController", BookController);


// ==============================
// Controller
// ==============================

function BookController($scope, $http) {
    initBookPage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initBookPage($scope, $http) {
    $scope.loginUser = IRead.getLoginUser();
    IRead.bindCommonActions($scope);

    $scope.books = [];
    $scope.categories = [];

    $scope.currentPage = 0;
    $scope.pageSize = 5;

    $scope.errorMessage = "";

    $scope.searchForm = {
        keyword: "",
        categoryId: "",
        status: "ACTIVE"
    };

    bindBookPageActions($scope, $http);

    loadCategories($scope, $http);
    loadBooks($scope, $http);
}


// ==============================
// 綁定畫面事件
// ==============================

function bindBookPageActions($scope, $http) {
    $scope.loadBooks = function () {
        loadBooks($scope, $http);
    };

    $scope.clearSearch = function () {
        clearSearch($scope, $http);
    };

    $scope.pagedBooks = function () {
        return pagedBooks($scope);
    };

    $scope.numberOfPages = function () {
        return numberOfPages($scope);
    };

    $scope.prevPage = function () {
        prevPage($scope);
    };

    $scope.nextPage = function () {
        nextPage($scope);
    };

    $scope.goDetail = function (book) {
        goBookDetail(book);
    };

    $scope.bookStatusText = function (status) {
        return bookStatusText(status);
    };
}


// ==============================
// API 查詢
// ==============================

function loadCategories($scope, $http) {
    IRead.bookCategoryApi.list($http)
        .then(function (data) {
            $scope.categories = data || [];
        }, function (response) {
            console.log("分類查詢失敗：", response);
            $scope.errorMessage = "分類資料載入失敗";
        });
}

function loadBooks($scope, $http) {
    $scope.errorMessage = "";
    $scope.currentPage = 0;

    IRead.bookApi.list($http, $scope.searchForm)
        .then(function (data) {
            console.log("書籍查詢結果：", data);
            $scope.books = normalizeBookList(data);
        }, function (response) {
            console.log("書籍查詢失敗：", response);

            if (response.data && response.data.message) {
                $scope.errorMessage = response.data.message;
            } else {
                $scope.errorMessage = "書籍資料載入失敗";
            }
        });
}

function normalizeBookList(data) {
    if (Array.isArray(data)) {
        return data;
    }

    if (data && Array.isArray(data.content)) {
        return data.content;
    }

    if (data && Array.isArray(data.items)) {
        return data.items;
    }

    if (data && Array.isArray(data.books)) {
        return data.books;
    }

    return [];
}


// ==============================
// 查詢條件
// ==============================

function clearSearch($scope, $http) {
    $scope.searchForm = {
        keyword: "",
        categoryId: "",
        status: "ACTIVE"
    };

    loadBooks($scope, $http);
}


// ==============================
// 分頁
// ==============================

function pagedBooks($scope) {
    var start = $scope.currentPage * $scope.pageSize;
    return $scope.books.slice(start, start + $scope.pageSize);
}

function numberOfPages($scope) {
    if (!$scope.books || $scope.books.length === 0) {
        return 1;
    }

    return Math.ceil($scope.books.length / $scope.pageSize);
}

function prevPage($scope) {
    if ($scope.currentPage > 0) {
        $scope.currentPage--;
    }
}

function nextPage($scope) {
    if ($scope.currentPage < numberOfPages($scope) - 1) {
        $scope.currentPage++;
    }
}


// ==============================
// 跳頁
// ==============================

function goBookDetail(book) {
    location.href = "book-detail.html?bookId=" + encodeURIComponent(book.bookId);
}


// ==============================
// 畫面文字
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