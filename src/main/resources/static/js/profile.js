$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		//异步请求处理
		$.post(
			CONTEXT_PATH +"/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			//处理返回的结果
			function (data) {
				//转为json对象
				data=$.parseJSON(data);
				if(data.code==0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);


		//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		//异步请求处理
		$.post(
			CONTEXT_PATH +"/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			//处理返回的结果
			function (data) {
				//转为json对象
				data=$.parseJSON(data);
				if(data.code==0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		);
		//$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}