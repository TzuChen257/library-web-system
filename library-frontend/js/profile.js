// =======================================================
// profile.js
// profile.html - 個人資料
// =======================================================

app.controller("ProfileController", ProfileController);


// ==============================
// Controller
// ==============================

function ProfileController($scope, $http) {
    initProfilePage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initProfilePage($scope, $http) {
    if (!IRead.requireLogin()) {
        return;
    }
    IRead.bindCommonActions($scope);

    $scope.loginUser = IRead.getLoginUser();

    $scope.user = null;
    $scope.errorMessage = "";
    $scope.loading = false;

    bindProfileActions($scope, $http);

    loadProfile($scope, $http);
}


// ==============================
// 綁定畫面事件
// ==============================

function bindProfileActions($scope, $http) {
    $scope.loadProfile = function () {
        loadProfile($scope, $http);
    };

    $scope.isBorrowSuspended = function (user) {
        return isBorrowSuspended(user);
    };

    $scope.formatDateTime = function (value) {
        return formatDateTime(value);
    };
}


// ==============================
// API：查詢個人資料
// ==============================

function loadProfile($scope, $http) {
    $scope.errorMessage = "";
    $scope.loading = true;

    IRead.userApi.me($http)
        .then(function (data) {
            console.log("個人資料：", data);

            $scope.user = data || null;

            if ($scope.user) {
                updateLocalLoginUser($scope.user);
                $scope.loginUser = IRead.getLoginUser();
            }

            $scope.loading = false;
        }, function (response) {
            handleLoadProfileError($scope, response);
        });
}

function handleLoadProfileError($scope, response) {
    console.log("個人資料查詢失敗：", response);

    $scope.loading = false;

    if (response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = "個人資料載入失敗";
    }
}


// ==============================
// 更新 localStorage 使用者資訊
// ==============================

function updateLocalLoginUser(user) {
    if (!user) {
        return;
    }

    var current = IRead.getLoginUser() || {};

    current.userId = user.userId || current.userId;
    current.username = user.username || current.username;
    current.name = user.name || current.name;
    current.role = user.role || current.role;
    current.status = user.status || current.status;
    current.borrowSuspended = user.borrowSuspended;

    IRead.setLoginUser(current);
}


// ==============================
// 狀態判斷
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
// 日期格式
// ==============================

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    return String(value).replace("T", " ").substring(0, 16);
}