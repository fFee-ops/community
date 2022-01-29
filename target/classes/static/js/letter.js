$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName=$("#recipient-name").val();
	var content=$("#message-text").val();
	$.post(
		//访问路径
		CONTEXT_PATH +"/letter/send",
		//传的参数
		{"toName":toName,"content":content},
		//处理服务端返回的结果
		function (data) {
			//将其转为json对象
			data= $.parseJSON(data);
			//对返回的结果进行判断
			if(data.code==0){
				$("#hintBody").text("发送成功！");
			}else {
				$("#hintBody").text(data.msg);
			}

			//刷新页面
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}

	);

}

function delete_msg() {
	// 删除数据
	var btn = this;
	var id = $(btn).prev().val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"id":id},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$(btn).parents(".media").remove();
			} else {
				alert(data.msg);
			}
		}
	);
}