$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTET_PATH + "/follow",
			{"entityId":$(btn).prev().val(),"entityType":3},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		)
		//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTET_PATH + "/unfollow",
			{"entityId":$(btn).prev().val(),"entityType":3},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0){
					window.location.reload();
				}else{
					alert(data.msg);
				}
			}
		)
		//$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}
