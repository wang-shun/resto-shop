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
     <div class="col-md-4 col-md-offset-2">
         <div class="dashboard-stat blue">
             <div class="visual">
                 <i class="fa fa-comments"></i>
             </div>
             <div class="details">
                 <div class="number">
                <span data-counter="counterup" data-value="${smsAcount.usedNum}">${smsAcount.usedNum}</span>条
                 </div>
                 <div class="desc"> 已经使用的短信数量 </div>
             </div>
         </div>
     </div>
     <div class="col-md-4">
         <div class="dashboard-stat green">
             <div class="visual">
                 <i class="fa fa-bar-chart-o"></i>
             </div>
             <div class="details">
                 <div class="number">
                     <span data-counter="counterup" data-value="smsAcount.remainderNum">${smsAcount.remainderNum}</span>条 </div>
                 <div class="desc"> 剩余短信数量 </div>
             </div>
         </div>
     </div>
 </div>
                    
                    
                    
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
			<s:hasPermission name="smsloginfo/isBrand">
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
			</s:hasPermission>


			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="button" id="querySms" class="btn btn-primary">查询短信记录</button>
				</div>
			</div>

		</form>
		
		
	</div>
</div>
<br />
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
<script src="assets/global/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
	
<script src="assets/global/plugins/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
	
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
												+ "<input type='checkbox' name='shopName' value='"+shop.id+"'/>"
												+ shop.name + "</label>";
										$("#choiceShop").append(str);
									})
						//默认选择所有店铺
						$(":checkbox[name='shopName']").prop("checked", true);
					}
				})
		var $table = $(".table-body>table");
		
		var tb = $table.DataTable({
			ajax : {
				url : "smsloginfo/listByShopAndDate",
				dataSrc : "",
				type : "POST",
				data : function(d) {
					d.begin = $("#beginDate").val();
					d.end = $("#endDate").val();
					var temp="";
					 $(":checkbox[name='shopName']:checked").each(
							function() {
 								if($(this).attr("checked")){
 									temp += $(this).val()+","
								}
								temp += $(this).val()+",";
								
						})
						d.shopIds=temp;
					console.log(temp);
					return d;
				},

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
				title : "是否成功",
				data : "isSuccess",
			} ]

		})

		//查询
				$("#querySms").click(function(){
					var begin = $("#beginDate").val();
					var end = $("#endDate").val();
					//判断时间是否合法
					if(begin>end){
						toastr.error("开始时间不能大于结束时间");
						return ;
					}
					//检验是否选择了店铺
					var checkboxes =$("input[type='checkbox']");
					if(!checkboxes.is(":checked")){
						toastr.error("请至少选择一个店铺");
						return ;
					}
					
					$("#smsForm").serialize();
					tb.ajax.reload();
				})
	})
</script>
