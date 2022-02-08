$(function () {
    $(".follow-btn").click(follow);
    $(".block-btn").click(block);
});

/**
 * 关注
 */
function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        //异步请求处理
        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            //处理返回的结果
            function (data) {
                //转为json对象
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );

    } else {
        // 取消关注
        //异步请求处理
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            //处理返回的结果
            function (data) {
                //转为json对象
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    }
}

/**
 * 拉黑
 */
function block() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 拉黑TA
        //异步请求处理
        $.post(
            CONTEXT_PATH + "/block",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            //处理返回的结果
            function (data) {
                //转为json对象
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );


    } else {
        // 取消关注
        //异步请求处理
        $.post(
            CONTEXT_PATH + "/unblock",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            //处理返回的结果
            function (data) {
                //转为json对象
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    }
}

// function block() {
//     var btn = this;
//     if ($(btn).hasClass("btn-info")) {
//     $.ajax({
//         url: "localhost:8080/community/block",
//         type: "post",
//         data: {"entityType": 3, "entityId": $(btn).prev().val()},
//         success: function (data) {
//             //转为json对象
//             data = $.parseJSON(data);
//             if (data.code == 0) {
//                 window.location.reload();
//             } else {
//                 alert(data.msg);
//             }
//         }
//     });
//
//     } else {
//         $.ajax({
//             url: "localhost:8080/community/unblock",
//             type: "post",
//             data: {"entityType": 3, "entityId": $(btn).prev().val()},
//             success: function (data) {
//                 //转为json对象
//                 data = $.parseJSON(data);
//                 if (data.code == 0) {
//                     window.location.reload();
//                 } else {
//                     alert(data.msg);
//                 }
//             }
//         });
//     }
// }