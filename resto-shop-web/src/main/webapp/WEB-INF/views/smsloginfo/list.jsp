<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<link rel="stylesheet" type="text/css"
	href="assets/global/plugins/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css">

<h2 class="text-center">
	<strong>短信记录</strong>
</h2>
<br />
<div class="row">
	<div class="col-md-8 col-md-offset-2">
		<form class="form-horizontal" id="smsForm">
			<div class="form-group">
				<div class="col-sm-6">
					<label for="beginDate">开始时间：</label> <input type="text"
						class="form-control form_datetime" id="beginDate" name="beginDate"
						readonly="readonly">
				</div>
				<div class="col-sm-6">
					<label for="endDate">结束时间：</label> <input type="text"
						class="form-control form_datetime" id="endDate" name="endDate"
						readonly="readonly">
				</div>
			</div>
			<%-- 			<s:hasPermission name="order/isAdmin"> --%>
			<div class="form-group">
				<label for="choiceShop" class="col-sm-2 control-label">店铺选择</label>
				<div class="col-sm-10" id="choiceShop">
					<c:if test="${shopDetails}!=null">
						<c:forEach items="${shopDetails}" var="item">
							<label class='checkbox-inline'> <input type='checkbox'
								name='shopIds' value="${item.id}" />${item.name}
							</label>
						</c:forEach>
					</c:if>

				</div>
			</div>
			<%-- 			</s:hasPermission> --%>


			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="button" id="querySms" class="btn btn-primary">查询短信记录</button>
				</div>
			</div>

		</form>
	</div>
</div>
<br />
<p class="text-danger text-center" hidden="true">
	<strong>开始时间不能大于结束时间！</strong>
</p>
<br />
<div class="panel panel-default">
	<div class="panel-heading">短信记录详情</div>
	<div class="panel-body">
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered"
				id="selectList"></table>
		</div>
	</div>
</div>

<!-- <!-- 日期框 -->
<script
	src="assets/global/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script
	src="assets/global/plugins/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>


<script>
	$(function() {
		//时间插件
		$('.form_datetime').datetimepicker({
			endDate : new Date(),
			minView : "month",
			maxView : "month",
			autoclose : true,//选择后自动关闭时间选择器
			todayBtn : true,//在底部显示 当天日期
			todayHighlight : true,//高亮当前日期
			format : "yyyy-mm-dd",
			startView : "month",
			language : "zh-CN"
		});

		//时间默认值
		$('.form_datetime').val(new Date().format("yyyy-MM-dd"));

		//查询店铺
			$.ajax({
					url : 'smsloginfo/shopName',
					success : function(data) {
						$(data).each(
									function(i, shop) {
										var str = "<label class='checkbox-inline'>"
												+ "<input type='checkbox' name='shopIds' value='"+shop.id+"'/>"
												+ shop.name + "</label>";
										$("#choiceShop").append(str);
									})
						//默认选择所有店铺
						$(":checkbox[name='shopIds']").prop("checked", true);
						$("#choiceShop").trigger("create");
					}
				})
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "smsloginfo/listByShop",
				dataSrc : "",
				data : function(d) {
					d.begin = $("#beginDate").val();
					d.end = $("#endDate").val();
					var shopIds = "86d0cb619e224a85a1419060d3fba8de";
					/* $("input[name='shopIds'] :checked").each(
							function() {
								if($(this).attr("checked")){
									shopIds += $(this).val()+","
								}
								console.log(shopIds);
							})
					d.shopIds = shopIds; */
					d.shopIds=shopIds;
					return d;
				}

			},
			columns : [ {
				title : "手机号",
				data : "phone",
			}, {
				title : "内容",
				data : "content",
			}, {
				title : "发送类型",
				data : "smsLogTyPeName",
			},

			{
				title : "创建时间",
				data : "createTime",
				createdCell : function(td, tdData) {
					$(td).html(new Date().format("yyyy-mm-dd hh:ss"));
				}

			},

			{
				title : "返回结果",
				data : "smsResult",
			},

			{
				title : "是否成功",
				data : "isSuccess",
			} ]

		})

		//查询
		// 		$("#querySms").click(function(){
		// 			var begin = $("#beginDate").val();
		// 			var end = $("#endDate").val();
		// 			//判断时间是否合法
		// 			if(begin>end){
		// 				$(".text-danger").show();
		// 				return ;
		// 			}
		// 			$(".text-danger").hide();//隐藏提示
		// 			var shopIds=[];
		// 			$(':checkbox[name="shopIds"]:checked').each(function(){
		// 				console.log("11");
		// 				console.log($(this).val());
		// 				shopIds.put($(this).val());
		// 			});
		// 			var data = {"begin":begin,"end":end};
		// 			data.shopIds=shopIds;
		// 			console.log("22222222222222222222");
		// 			console.log(data);
		// 			tb.ajax.reload();
		// 		})

		$("#querySms").click(function() {

			tb.ajax.reload();
		})

	})
</script>
