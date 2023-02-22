function like(btn,entityType,entityId,entityUserId) {
    $.post(
        CONTET_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1?'已赞':'赞');
            }else{
                alert(data.msg);
            }
        }
    )
}
