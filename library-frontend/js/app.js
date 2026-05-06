// =======================================================
// iRead Library - App / Common
// AngularJS + $scope + $http 版本
// =======================================================

// 建立 AngularJS App
var app = angular.module("LibraryApp", []);

// 建立全域命名空間，讓 api.js 和各頁面 JS 共用
var IRead = window.IRead || {};
window.IRead = IRead;


// =======================================================
// 後端 API 基礎路徑
// 後端有設定：server.servlet.context-path=/iread-library
// 所以前端 API 一律要加 /iread-library/api
// =======================================================

IRead.API_BASE = "http://localhost:8080/iread-library/api";


// =======================================================
// localStorage key
// =======================================================

IRead.TOKEN_KEY = "iread-library.token";
IRead.USER_KEY = "iread-library.user";


// =======================================================
// 登入資訊處理
// =======================================================

IRead.getToken = function () {
    return localStorage.getItem(IRead.TOKEN_KEY);
};

IRead.setToken = function (token) {
    localStorage.setItem(IRead.TOKEN_KEY, token);
};

IRead.getLoginUser = function () {
    var userJson = localStorage.getItem(IRead.USER_KEY);

    if (!userJson) {
        return null;
    }

    try {
        return JSON.parse(userJson);
    } catch (e) {
        return null;
    }
};

IRead.setLoginUser = function (user) {
    localStorage.setItem(IRead.USER_KEY, JSON.stringify(user));
};

IRead.saveLogin = function (token, user) {
    IRead.setToken(token);
    IRead.setLoginUser(user);
};

IRead.clearLogin = function () {
    localStorage.removeItem(IRead.TOKEN_KEY);
    localStorage.removeItem(IRead.USER_KEY);
};

// =======================================================
// Header
// =======================================================

IRead.authHeaders = function () {
    var token = IRead.getToken();

    if (!token) {
        return {};
    }

    return {
        "Authorization": "Bearer " + token
    };
};

IRead.jsonHeaders = function () {
    return {
        "Content-Type": "application/json"
    };
};

IRead.authJsonHeaders = function () {
    var headers = {
        "Content-Type": "application/json"
    };

    var token = IRead.getToken();

    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    return headers;
};


// =======================================================
// Response / Error
// =======================================================

IRead.extractData = function (response) {
    if (response && response.data && response.data.data !== undefined) {
        return response.data.data;
    }

    if (response && response.data !== undefined) {
        return response.data;
    }

    return response;
};

IRead.extractMessage = function (response) {
    if (response && response.data && response.data.message) {
        return response.data.message;
    }

    return "操作完成";
};

IRead.showError = function (response) {
    if (response && response.data && response.data.message) {
        alert(response.data.message);
        return;
    }

    if (response && response.status === 401) {
        alert("請先登入");
        IRead.logout();
        return;
    }

    if (response && response.status === 403) {
        alert("權限不足");
        return;
    }

    alert("系統發生錯誤，請稍後再試");
};


// =======================================================
// 頁面權限檢查
// =======================================================

IRead.requireLogin = function () {
    if (!IRead.getToken()) {
        alert("請先登入");
        location.href = "login.html";
        return false;
    }

    return true;
};

IRead.requireReader = function () {
    var user = IRead.getLoginUser();

    if (!user || user.role !== "READER") {
        alert("此頁面限讀者使用");
        location.href = "login.html";
        return false;
    }

    return true;
};

IRead.requireAdmin = function () {
    var user = IRead.getLoginUser();

    if (!user || user.role !== "ADMIN") {
        alert("此頁面限管理員使用");
        location.href = "login.html";
        return false;
    }

    return true;
};

// =======================================================
// 頁面跳轉統一管理
// =======================================================

IRead.pages = {
    index: "index.html",

    login: "login.html",
    register: "register.html",
    profile: "profile.html",
    messages: "messages.html",

    books: "books.html",
    bookDetail: "book-detail.html",

    reader: "reader.html",
    admin: "admin.html"
};

IRead.go = function (page) {
    location.href = page;
};

IRead.goIndex = function () {
    IRead.go(IRead.pages.index);
};

IRead.goLogin = function () {
    IRead.go(IRead.pages.login);
};

IRead.goRegister = function () {
    IRead.go(IRead.pages.register);
};

IRead.goProfile = function () {
    IRead.go(IRead.pages.profile);
};

IRead.goMessages = function () {
    IRead.go(IRead.pages.messages);
};

IRead.goBooks = function () {
    IRead.go(IRead.pages.books);
};

IRead.goBookDetail = function (bookId) {
    if (!bookId) {
        alert("缺少書目 ID");
        return;
    }

    IRead.go(IRead.pages.bookDetail + "?bookId=" + encodeURIComponent(bookId));
};


// =======================================================
// 讀者中心跳轉
// =======================================================

IRead.goReaderCenter = function (tab) {
    if (tab) {
        IRead.go(IRead.pages.reader + "?tab=" + encodeURIComponent(tab));
        return;
    }

    IRead.go(IRead.pages.reader);
};

IRead.goMyBorrows = function () {
    IRead.goReaderCenter("borrows");
};

IRead.goMyReservations = function () {
    IRead.goReaderCenter("reservations");
};


// =======================================================
// 管理員中心跳轉
// =======================================================

IRead.goAdminCenter = function (tab) {
    if (tab) {
        IRead.go(IRead.pages.admin + "?tab=" + encodeURIComponent(tab));
        return;
    }

    IRead.go(IRead.pages.admin + "?tab=dashboard");
};

IRead.goAdminDashboard = function () {
    IRead.goAdminCenter("dashboard");
};

IRead.goAdminBooks = function () {
    IRead.goAdminCenter("books");
};

IRead.goAdminBookCopies = function (bookId) {
    if (bookId) {
        IRead.go(IRead.pages.admin + "?tab=copies&bookId=" + encodeURIComponent(bookId));
        return;
    }

    IRead.goAdminCenter("copies");
};

IRead.goAdminBorrows = function (borrowStatus) {
    if (borrowStatus) {
        IRead.go(IRead.pages.admin + "?tab=borrows&borrowStatus=" + encodeURIComponent(borrowStatus));
        return;
    }

    IRead.goAdminCenter("borrows");
};

IRead.goAdminReturnReview = function () {
    IRead.goAdminCenter("return-review");
};

IRead.goAdminReservations = function (reservationStatus) {
    if (reservationStatus) {
        IRead.go(IRead.pages.admin + "?tab=reservations&reservationStatus=" + encodeURIComponent(reservationStatus));
        return;
    }

    IRead.goAdminCenter("reservations");
};

IRead.goAdminUsers = function () {
    IRead.goAdminCenter("users");
};


// =======================================================
// 登出 / 登入後導向
// =======================================================

IRead.logout = function () {
    IRead.clearLogin();
    IRead.goLogin();
};

IRead.goHomeByRole = function () {
    var user = IRead.getLoginUser();

    if (!user) {
        IRead.goLogin();
        return;
    }

    if (user.role === "ADMIN") {
        IRead.goAdminDashboard();
        return;
    }

    IRead.goIndex();
};

// =======================================================
// 共用導覽列綁定
// 各頁 controller 初始化時呼叫：IRead.bindCommonActions($scope)
// =======================================================

// =======================================================
// 共用導覽列綁定
// 各頁 controller 初始化時呼叫：IRead.bindCommonActions($scope)
// =======================================================

IRead.bindCommonActions = function ($scope) {
    $scope.loginUser = IRead.getLoginUser();

    // ==============================
    // 基本頁面
    // ==============================

    $scope.goIndex = function () {
        IRead.goIndex();
    };

    $scope.goLogin = function () {
        IRead.goLogin();
    };

    $scope.goRegister = function () {
        IRead.goRegister();
    };

    $scope.goProfile = function () {
        IRead.goProfile();
    };

    $scope.goMessages = function () {
        IRead.goMessages();
    };

    $scope.goBooks = function () {
        IRead.goBooks();
    };

    $scope.goBookDetail = function (book) {
        if (!book) {
            alert("缺少書目資料");
            return;
        }

        IRead.goBookDetail(book.bookId || book.id);
    };


    // ==============================
    // 讀者中心
    // ==============================

    $scope.goReaderCenter = function () {
        IRead.goReaderCenter();
    };

    $scope.goReaderBorrows = function () {
        IRead.goReaderCenter("borrows");
    };

    $scope.goReaderReservations = function () {
        IRead.goReaderCenter("reservations");
    };

    $scope.goMyBorrows = function () {
        IRead.goReaderCenter("borrows");
    };

    $scope.goMyReservations = function () {
        IRead.goReaderCenter("reservations");
    };


    // ==============================
    // 管理員中心
    // ==============================

    $scope.goAdminCenter = function () {
        IRead.goAdminDashboard();
    };

    $scope.goAdminDashboard = function () {
        IRead.goAdminDashboard();
    };

    $scope.goAdminBooks = function () {
        IRead.goAdminBooks();
    };

    $scope.goAdminBookCopies = function (book) {
        if (book && book.bookId) {
            IRead.goAdminBookCopies(book.bookId);
            return;
        }

        IRead.goAdminBookCopies();
    };

    $scope.goAdminBorrows = function () {
        IRead.goAdminBorrows();
    };

    $scope.goAdminBorrowsWithStatus = function (borrowStatus) {
        IRead.goAdminBorrows(borrowStatus);
    };

    $scope.goReturnReview = function () {
        IRead.goAdminReturnReview();
    };

    $scope.goAdminReservations = function () {
        IRead.goAdminReservations();
    };

    $scope.goAdminReservationsWithStatus = function (reservationStatus) {
        IRead.goAdminReservations(reservationStatus);
    };

    $scope.goAdminUsers = function () {
        IRead.goAdminUsers();
    };


    // ==============================
    // 共用動作 / 文字
    // ==============================

    $scope.logout = function () {
        IRead.logout();
    };

    $scope.roleText = function (role) {
        return IRead.roleText(role);
    };

    $scope.statusText = function (status) {
        return IRead.statusText(status);
    };
};


// =======================================================
// 常用工具
// =======================================================

IRead.getQueryParam = function (name) {
    var url = new URL(location.href);
    return url.searchParams.get(name);
};

IRead.statusText = function (status) {
    if (status === "ACTIVE") return "啟用";
    if (status === "DISABLED") return "停用";
    return status || "";
};

IRead.roleText = function (role) {
    if (role === "READER") return "讀者";
    if (role === "ADMIN") return "管理員";
    return role || "";
};