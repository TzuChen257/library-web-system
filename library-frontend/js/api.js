// =======================================================
// iRead Library - API
// 需先載入 app.js
// =======================================================

if (!window.IRead) {
    console.error("請先載入 app.js，再載入 api.js");
}


// ==============================
// 基礎 API Wrapper
// ==============================

IRead.api = {};

IRead.api.qs = function (params) {
    if (!params) {
        return "";
    }

    var query = [];

    for (var key in params) {
        if (!params.hasOwnProperty(key)) {
            continue;
        }

        var value = params[key];

        if (value === undefined || value === null || value === "") {
            continue;
        }

        query.push(
            encodeURIComponent(key) + "=" + encodeURIComponent(value)
        );
    }

    if (query.length === 0) {
        return "";
    }

    return "?" + query.join("&");
};

IRead.api.get = function ($http, path, needAuth) {
    var config = {
        method: "GET",
        url: IRead.API_BASE + path
    };

    if (needAuth) {
        config.headers = IRead.authHeaders();
    }

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.post = function ($http, path, body, needAuth) {
    var config = {
        method: "POST",
        url: IRead.API_BASE + path,
        data: angular.toJson(body || {}),
        headers: needAuth ? IRead.authJsonHeaders() : IRead.jsonHeaders()
    };

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.postNoBody = function ($http, path, needAuth) {
    var config = {
        method: "POST",
        url: IRead.API_BASE + path
    };

    if (needAuth) {
        config.headers = IRead.authHeaders();
    }

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.put = function ($http, path, body, needAuth) {
    var config = {
        method: "PUT",
        url: IRead.API_BASE + path,
        data: angular.toJson(body || {}),
        headers: needAuth ? IRead.authJsonHeaders() : IRead.jsonHeaders()
    };

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.putNoBody = function ($http, path, needAuth) {
    var config = {
        method: "PUT",
        url: IRead.API_BASE + path
    };

    if (needAuth) {
        config.headers = IRead.authHeaders();
    }

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.patch = function ($http, path, needAuth) {
    var config = {
        method: "PATCH",
        url: IRead.API_BASE + path
    };

    if (needAuth) {
        config.headers = IRead.authHeaders();
    }

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.patchBody = function ($http, path, body, needAuth) {
    var config = {
        method: "PATCH",
        url: IRead.API_BASE + path,
        data: angular.toJson(body || []),
        headers: needAuth ? IRead.authJsonHeaders() : IRead.jsonHeaders()
    };

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};

IRead.api.del = function ($http, path, needAuth) {
    var config = {
        method: "DELETE",
        url: IRead.API_BASE + path
    };

    if (needAuth) {
        config.headers = IRead.authHeaders();
    }

    return $http(config).then(function (response) {
        return IRead.extractData(response);
    });
};


// =======================================================
// Auth API
// =======================================================

IRead.authApi = {
    register: function ($http, body) {
        return IRead.api.post($http, "/auth/register", body, false);
    },

    login: function ($http, body) {
        return IRead.api.post($http, "/auth/login", body, false);
    },

    logout: function ($http) {
        return IRead.api.postNoBody($http, "/auth/logout", true);
    },

    me: function ($http) {
        return IRead.api.get($http, "/users/me", true);
    }
};


// =======================================================
// Book Category API
// =======================================================

IRead.bookCategoryApi = {
    list: function ($http) {
        return IRead.api.get($http, "/book-categories", false);
    },

    get: function ($http, categoryId) {
        return IRead.api.get(
            $http,
            "/book-categories/" + encodeURIComponent(categoryId),
            false
        );
    }
};


// =======================================================
// Book API - 讀者 / 訪客查詢
// =======================================================

IRead.bookApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/books" + IRead.api.qs(params),
            false
        );
    },

    get: function ($http, bookId) {
        return IRead.api.get(
            $http,
            "/books/" + encodeURIComponent(bookId),
            false
        );
    }
};


// =======================================================
// Borrow API - 讀者借閱
// =======================================================

IRead.borrowApi = {
    borrowBook: function ($http, bookId) {
        return IRead.api.postNoBody(
            $http,
            "/borrows/" + encodeURIComponent(bookId),
            true
        );
    },

    myCurrent: function ($http) {
        return IRead.api.get(
            $http,
            "/borrows/me/current",
            true
        );
    },

    requestReturn: function ($http, borrowId) {
        return IRead.api.patch(
            $http,
            "/borrows/" + encodeURIComponent(borrowId) + "/return-request",
            true
        );
    }
};


// =======================================================
// Reservation API - 讀者預約
// =======================================================

IRead.reservationApi = {
    create: function ($http, bookId) {
        return IRead.api.postNoBody(
            $http,
            "/reservations" + IRead.api.qs({
                bookId: bookId
            }),
            true
        );
    },

    myList: function ($http) {
        return IRead.api.get(
            $http,
            "/reservations/me",
            true
        );
    },

    cancel: function ($http, reservationId) {
        return IRead.api.patch(
            $http,
            "/reservations/" + encodeURIComponent(reservationId) + "/cancel",
            true
        );
    }
};


// =======================================================
// Message API - 訊息中心
// =======================================================

IRead.messageApi = {
    myList: function ($http) {
        return IRead.api.get(
            $http,
            "/messages/me",
            true
        );
    },

    unreadCount: function ($http) {
        return IRead.api.get(
            $http,
            "/messages/me/unread-count",
            true
        );
    },

    markAsRead: function ($http, messageId) {
        return IRead.api.patch(
            $http,
            "/messages/" + encodeURIComponent(messageId) + "/read",
            true
        );
    },

    remove: function ($http, messageId) {
        return IRead.api.del(
            $http,
            "/messages/" + encodeURIComponent(messageId),
            true
        );
    }
};


// =======================================================
// User API - 個人資料
// =======================================================

IRead.userApi = {
    me: function ($http) {
        return IRead.api.get(
            $http,
            "/users/me",
            true
        );
    }
};


// =======================================================
// Admin Book API - 管理員書目管理
// =======================================================

IRead.adminBookApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/books" + IRead.api.qs(params),
            true
        );
    },

    get: function ($http, bookId) {
        return IRead.api.get(
            $http,
            "/admin/books/" + encodeURIComponent(bookId),
            true
        );
    },

    create: function ($http, body) {
        return IRead.api.post(
            $http,
            "/admin/books",
            body,
            true
        );
    },

    update: function ($http, bookId, body) {
        return IRead.api.put(
            $http,
            "/admin/books/" + encodeURIComponent(bookId),
            body,
            true
        );
    },

    changeStatus: function ($http, bookId, status) {
        return IRead.api.patch(
            $http,
            "/admin/books/" + encodeURIComponent(bookId) + "/status" + IRead.api.qs({
                status: status
            }),
            true
        );
    }
};

// =======================================================
// Admin Book Import API - 書目與館藏 Excel 匯入
// =======================================================

IRead.adminBookImportApi = {
    downloadTemplate: function ($http) {
        var config = {
            method: "GET",
            url: IRead.API_BASE + "/admin/books/import-template",
            responseType: "arraybuffer",
            headers: IRead.authHeaders()
        };

        return $http(config).then(function (response) {
            return response;
        });
    },

    importBooks: function ($http, file) {
        var formData = new FormData();
        formData.append("file", file);

        var config = {
            method: "POST",
            url: IRead.API_BASE + "/admin/books/import",
            data: formData,
            headers: {
                "Authorization": "Bearer " + IRead.getToken(),
                "Content-Type": undefined
            },
            transformRequest: angular.identity
        };

        return $http(config).then(function (response) {
            return IRead.extractData(response);
        });
    }
};


// =======================================================
// Admin Book Copy API - 管理員館藏冊本管理
// =======================================================

IRead.adminBookCopyApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/book-copies" + IRead.api.qs(params),
            true
        );
    },

    get: function ($http, copyId) {
        return IRead.api.get(
            $http,
            "/admin/book-copies/" + encodeURIComponent(copyId),
            true
        );
    },

    create: function ($http, bookId, body) {
        return IRead.api.postNoBody(
            $http,
            "/admin/books/" + encodeURIComponent(bookId) + "/copies" + IRead.api.qs({
                copyCode: body.copyCode,
                location: body.location,
                note: body.note
            }),
            true
        );
    },

    update: function ($http, copyId, body) {
        return IRead.api.putNoBody(
            $http,
            "/admin/book-copies/" + encodeURIComponent(copyId) + IRead.api.qs({
                copyCode: body.copyCode,
                location: body.location,
                copyStatus: body.copyStatus,
                note: body.note
            }),
            true
        );
    },

    changeStatus: function ($http, copyId, copyStatus) {
        return IRead.api.patch(
            $http,
            "/admin/book-copies/" + encodeURIComponent(copyId) + "/status" + IRead.api.qs({
                copyStatus: copyStatus
            }),
            true
        );
    }
};


// =======================================================
// Admin Borrow API - 管理員借閱紀錄 / 歸還審核
// =======================================================

IRead.adminBorrowApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/borrows" + IRead.api.qs(params),
            true
        );
    },

    returnPending: function ($http) {
        return IRead.api.get(
            $http,
            "/admin/borrows/return-pending",
            true
        );
    },

    approveReturn: function ($http, borrowId, resultStatus) {
        return IRead.api.patch(
            $http,
            "/admin/borrows/" + encodeURIComponent(borrowId) + "/approve-return" + IRead.api.qs({
                resultStatus: resultStatus
            }),
            true
        );
    },

    batchApproveNormalReturn: function ($http, borrowIds) {
        return IRead.api.patchBody(
            $http,
            "/admin/borrows/approve-return/batch-normal",
            borrowIds,
            true
        );
    }
};


// =======================================================
// Admin Reservation API - 管理員預約管理
// =======================================================

IRead.adminReservationApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/reservations" + IRead.api.qs(params),
            true
        );
    },

    notify: function ($http, reservationId) {
        return IRead.api.patch(
            $http,
            "/admin/reservations/" + encodeURIComponent(reservationId) + "/notify",
            true
        );
    }
};


// =======================================================
// Admin User API - 管理員使用者管理
// =======================================================

IRead.adminUserApi = {
    list: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/users" + IRead.api.qs(params),
            true
        );
    },

    get: function ($http, userId) {
        return IRead.api.get(
            $http,
            "/admin/users/" + encodeURIComponent(userId),
            true
        );
    },

    create: function ($http, body) {
        return IRead.api.post(
            $http,
            "/admin/users",
            body,
            true
        );
    },

    update: function ($http, userId, body) {
        return IRead.api.put(
            $http,
            "/admin/users/" + encodeURIComponent(userId),
            body,
            true
        );
    },

    changeStatus: function ($http, userId, status) {
        return IRead.api.patch(
            $http,
            "/admin/users/" + encodeURIComponent(userId) + "/status" + IRead.api.qs({
                status: status
            }),
            true
        );
    },

    restoreBorrowPermission: function ($http, userId) {
        return IRead.api.patch(
            $http,
            "/admin/users/" + encodeURIComponent(userId) + "/restore-borrow-permission",
            true
        );
    }
};

// =======================================================
// Public Statistics API - 首頁公開統計
// 未登入也可以看
// =======================================================

IRead.publicStatsApi = {
    summary: function ($http) {
        return IRead.api.get(
            $http,
            "/statistics/public/summary",
            false
        );
    },

    topBorrowedBooks: function ($http, limit) {
        return IRead.api.get(
            $http,
            "/statistics/public/top-borrowed-books" + IRead.api.qs({
                limit: limit || 5
            }),
            false
        );
    }
};

// =======================================================
// Admin Statistics API - 管理員統計
// =======================================================

IRead.adminStatisticsApi = {
    summary: function ($http, params) {
        return IRead.api.get(
            $http,
            "/admin/statistics/summary" + IRead.api.qs(params),
            true
        );
    }
};

// =======================================================
// Admin Report API - 管理員報表下載
// =======================================================

IRead.adminReportApi = {
    downloadBorrowStatistics: function ($http, year, topN) {
        var config = {
            method: "GET",
            url: IRead.API_BASE + "/admin/reports/borrow-statistics.xlsx" + IRead.api.qs({
                year: year,
                topN: topN || 10
            }),
            responseType: "arraybuffer",
            headers: IRead.authHeaders()
        };

        return $http(config).then(function (response) {
            return response;
        });
    }
};