// =======================================================
// auth.js
// login.html / register.html
// =======================================================

app.controller("LoginController", LoginController);
app.controller("RegisterController", RegisterController);


// ==============================
// Login Controller
// ==============================

function LoginController($scope, $http) {
    initLoginPage($scope, $http);
}

function initLoginPage($scope, $http) {
    $scope.form = {
        username: "",
        password: ""
    };

    $scope.errorMessage = "";

    $scope.login = function () {
        login($scope, $http);
    };

    $scope.goRegister = function () {
        location.href = "register.html";
    };

    $scope.goBooks = function () {
        location.href = "books.html";
    };
}

function login($scope, $http) {
    $scope.errorMessage = "";

    if (!$scope.form.username || !$scope.form.password) {
        $scope.errorMessage = "請輸入帳號與密碼";
        return;
    }

    IRead.authApi.login($http, $scope.form)
        .then(function (data) {
            handleLoginSuccess($scope, data);
        }, function (response) {
            handleLoginError($scope, response);
        });
}

function handleLoginSuccess($scope, data) {
    console.log("登入成功回傳 data：", data);

    if (!data || !data.token) {
        $scope.errorMessage = "登入回傳資料異常，請確認後端 LoginResponse";
        return;
    }

    var userInfo = {
        userId: data.userId,
        username: data.username,
        name: data.name,
        role: data.role
    };

    IRead.saveLogin(data.token, userInfo);
    IRead.goHomeByRole();
}

function handleLoginError($scope, response) {
    console.log("登入失敗 response：", response);

    if (response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = "登入失敗，請確認帳號密碼";
    }
}


// ==============================
// Register Controller
// ==============================

function RegisterController($scope, $http) {
    initRegisterPage($scope, $http);
}

function initRegisterPage($scope, $http) {
    $scope.form = {
        username: "",
        password: "",
        name: "",
        email: "",
        phone: ""
    };

    $scope.confirmPassword = "";
    $scope.errorMessage = "";

    $scope.register = function () {
        register($scope, $http);
    };

    $scope.goLogin = function () {
        location.href = "login.html";
    };

    $scope.goBooks = function () {
        location.href = "books.html";
    };
}

function register($scope, $http) {
    $scope.errorMessage = "";

    if (!validateRegisterForm($scope)) {
        return;
    }

    IRead.authApi.register($http, $scope.form)
        .then(function () {
            alert("註冊成功，請登入");
            location.href = "login.html";
        }, function (response) {
            handleRegisterError($scope, response);
        });
}

function validateRegisterForm($scope) {
    if (!$scope.form.username) {
        $scope.errorMessage = "請輸入帳號";
        return false;
    }

    if (!$scope.form.password) {
        $scope.errorMessage = "請輸入密碼";
        return false;
    }

    if ($scope.form.password !== $scope.confirmPassword) {
        $scope.errorMessage = "兩次輸入的密碼不一致";
        return false;
    }

    if (!$scope.form.name) {
        $scope.errorMessage = "請輸入姓名";
        return false;
    }

    if (!$scope.form.email) {
        $scope.errorMessage = "請輸入 Email";
        return false;
    }

    return true;
}

function handleRegisterError($scope, response) {
    console.log("註冊失敗 response：", response);

    if (response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = "註冊失敗，請稍後再試";
    }
}