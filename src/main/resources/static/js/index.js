$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function(e,xhr,options){
	// 	xhr.setRequestHeader(header,token);
	// });
	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTET_PATH + "/discuss/add",
		{"title":title,"content":content},
		function(data){
			data = $.parseJSON(data);
			//在提示框中显示提示内容
			$("#hintBody").text(data.msg);
			//显示提示框，两秒后隐藏
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	)

}
