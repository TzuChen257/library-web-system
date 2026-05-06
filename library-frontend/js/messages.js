// =======================================================
// messages.js
// messages.html - 訊息中心
// =======================================================

app.controller("MessagesController", MessagesController);


// ==============================
// Controller
// ==============================

function MessagesController($scope, $http) {
    initMessagesPage($scope, $http);
}


// ==============================
// 初始化
// ==============================

function initMessagesPage($scope, $http) {
    if (!IRead.requireLogin()) {
        return;
    }
    IRead.bindCommonActions($scope);

    $scope.loginUser = IRead.getLoginUser();

    $scope.messages = [];
    $scope.unreadCount = 0;
    $scope.errorMessage = "";

    $scope.filterType = "ALL";

    $scope.currentPage = 0;
    $scope.pageSize = 8;

    bindMessagesActions($scope, $http);

    loadUnreadCount($scope, $http);
    loadMessages($scope, $http);
}


// ==============================
// 綁定畫面事件
// ==============================

function bindMessagesActions($scope, $http) {
    $scope.reloadMessages = function () {
        reloadMessages($scope, $http);
    };

    $scope.markAsRead = function (message) {
        markAsRead($scope, $http, message);
    };

    $scope.deleteMessage = function (message) {
        deleteMessage($scope, $http, message);
    };

    $scope.setFilter = function (filterType) {
        setFilter($scope, filterType);
    };

    $scope.filteredMessages = function () {
        return filteredMessages($scope);
    };

    $scope.pagedMessages = function () {
        return pagedMessages($scope);
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

    $scope.messageTypeText = function (messageType) {
        return messageTypeText(messageType);
    };
}


// ==============================
// API：查詢訊息
// ==============================

function reloadMessages($scope, $http) {
    loadUnreadCount($scope, $http);
    loadMessages($scope, $http);
}

function loadMessages($scope, $http) {
    $scope.errorMessage = "";
    $scope.currentPage = 0;

    IRead.messageApi.myList($http)
        .then(function (data) {
            console.log("我的訊息：", data);
            $scope.messages = normalizeMessageList(data);
        }, function (response) {
            handleLoadMessagesError($scope, response);
        });
}

function loadUnreadCount($scope, $http) {
    IRead.messageApi.unreadCount($http)
        .then(function (data) {
            console.log("未讀訊息數：", data);
            $scope.unreadCount = normalizeUnreadCount(data);
        }, function (response) {
            console.log("未讀訊息數查詢失敗：", response);
            $scope.unreadCount = 0;
        });
}

function handleLoadMessagesError($scope, response) {
    console.log("訊息查詢失敗：", response);

    if (response.data && response.data.message) {
        $scope.errorMessage = response.data.message;
    } else {
        $scope.errorMessage = "訊息資料載入失敗";
    }
}


// ==============================
// API：標記已讀
// ==============================

function markAsRead($scope, $http, message) {
    if (!message || !message.messageId) {
        alert("缺少訊息 ID，無法標記已讀");
        return;
    }

    IRead.messageApi.markAsRead($http, message.messageId)
        .then(function () {
            reloadMessages($scope, $http);
        }, function (response) {
            handleMessageActionError("標記已讀失敗", response);
        });
}


// ==============================
// API：刪除訊息
// ==============================

function deleteMessage($scope, $http, message) {
    if (!message || !message.messageId) {
        alert("缺少訊息 ID，無法刪除");
        return;
    }

    if (!confirm("確定要刪除此訊息嗎？")) {
        return;
    }

    IRead.messageApi.remove($http, message.messageId)
        .then(function () {
            alert("訊息已刪除");
            reloadMessages($scope, $http);
        }, function (response) {
            handleMessageActionError("刪除訊息失敗", response);
        });
}

function handleMessageActionError(defaultMessage, response) {
    console.log(defaultMessage + "：", response);

    if (response.data && response.data.message) {
        alert(response.data.message);
    } else {
        alert(defaultMessage);
    }
}


// ==============================
// 資料整理
// ==============================

function normalizeMessageList(data) {
    if (Array.isArray(data)) {
        return data;
    }

    if (data && Array.isArray(data.content)) {
        return data.content;
    }

    if (data && Array.isArray(data.items)) {
        return data.items;
    }

    if (data && Array.isArray(data.messages)) {
        return data.messages;
    }

    if (data && Array.isArray(data.messageList)) {
        return data.messageList;
    }

    return [];
}

function normalizeUnreadCount(data) {
    if (typeof data === "number") {
        return data;
    }

    if (data && data.unreadCount !== undefined) {
        return Number(data.unreadCount);
    }

    if (data && data.count !== undefined) {
        return Number(data.count);
    }

    return 0;
}


// ==============================
// 篩選
// ==============================

function setFilter($scope, filterType) {
    $scope.filterType = filterType;
    $scope.currentPage = 0;
}

function filteredMessages($scope) {
    if (!$scope.messages) {
        return [];
    }

    if ($scope.filterType === "UNREAD") {
        return $scope.messages.filter(function (message) {
            return !message.isRead;
        });
    }

    if ($scope.filterType === "READ") {
        return $scope.messages.filter(function (message) {
            return message.isRead;
        });
    }

    return $scope.messages;
}


// ==============================
// 分頁
// ==============================

function pagedMessages($scope) {
    var list = filteredMessages($scope);
    var start = $scope.currentPage * $scope.pageSize;

    return list.slice(start, start + $scope.pageSize);
}

function numberOfPages($scope) {
    var list = filteredMessages($scope);

    if (!list || list.length === 0) {
        return 1;
    }

    return Math.ceil(list.length / $scope.pageSize);
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
// 畫面文字
// ==============================

function messageTypeText(messageType) {
    if (messageType === "GENERAL") {
        return "一般";
    }

    if (messageType === "BORROW") {
        return "借閱";
    }

    if (messageType === "RETURN") {
        return "歸還";
    }

    if (messageType === "RESERVATION") {
        return "預約";
    }

    if (messageType === "OVERDUE") {
        return "逾期";
    }

    if (messageType === "DUE_SOON") {
        return "即將到期";
    }

    return messageType || "";
}