//在页面加载完之后调用（页面加载事件）
$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
    $("#deleteBtn2").click(setDelete);
    $("#forwardBtn").click(forwardArticle);
});

function like(btn, entityType, entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            } else {
                alert(data.msg);
            }
            //刷新页面
          /*  setTimeout(function(){
                location.reload();
            }, 1000);*/
        }
    );
}


// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {//返回的数据
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);//解析json
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );

}
    // 转发文章
    function forwardArticle() {
        // 获取标题和内容
        var title = "【转载】"+$("#title_backup").html();
        var content = $("#content_backup").text();
        var originalUserId=$("#originalUserId").val();
        // 发送异步请求(POST)
        $.post(
            CONTEXT_PATH + "/discuss/forward",
            {"title":title,"content":content,"originalUserId":originalUserId},
            function(data) {
                data = $.parseJSON(data);
                // 在提示框中显示返回消息
                $("#hintBody").text(data.msg);
                // 显示提示框
                $("#hintModal").modal("show");
                // 2秒后,自动隐藏提示框
                setTimeout(function(){
                    $("#hintModal").modal("hide");
                    // 刷新页面
                    if(data.code == 0) {
                        window.location.reload();
                    }
                }, 2000);
            }
        );

    }
