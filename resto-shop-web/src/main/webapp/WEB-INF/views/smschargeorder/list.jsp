<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div class="table-div">
	<div class="table-operator">
		<s:hasPermission name="notice/add">
			<button type="button" class="btn green pull-right"
				data-toggle="modal" data-target="#create">短信充值</button>
		</s:hasPermission>
	</div>
	<div class="clearfix"></div>
	<div class="table-filter"></div>
	<div class="table-body">
		<table class="table table-striped table-hover table-bordered"></table>
	</div>
</div>

<div class="modal fade" id="create" tabindex="-1" role="dialog">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title text-center">
					<strong>短信充值</strong>
				</h4>
			</div>
			<div class="modal-body">
				<form role="form" class="form-horizontal" action="smschargeorder/smsCharge" method="post" target="_blank">
<!-- 				<form role="form" class="form-horizontal" onsubmit="return false"> -->
					<div class="form-body">
						<div class="form-group">
							<label class="col-sm-3 control-label">充值品牌：</label>
							<div class="col-sm-8">
								<input type="text" disabled="disabled" class="form-control" required name="brandName"> 
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">短信单价：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" disabled="disabled" name="smsUnitPrice">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">充值金额：</label>
							<div class="col-sm-8">
								<input type="number" class="form-control" min="1" placeholder="请输入要充值的金额" onchange="computeSmsCount()" onkeyup="computeSmsCount()" required name="chargeMoney">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">短信条数：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" disabled="disabled" name="number">
							</div>
						</div>
					</div>
					<div class="text-center">
						<a class="btn default" data-dismiss="modal">取消</a> <input
							class="btn green" type="submit" value="充值" />
					</div>
				</form>
			</div>
		</div>
	</div>
</div>


<script>
	(function() {
		var cid = "#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "smschargeorder/list_all",
				dataSrc : "data"
			},
			columns : [
					{
						title : "品牌名称",
						data : "brandName",
						createdCell : function(td,tdData,rowData){
							$(":input[name='brandName']").val(tdData);
						}
					},
					{
						title : "充值金额（元）",
						data : "chargeMoney",
					},
					{
						title : "充值条数",
						data : "number",
					},
					{
						title : "短信单价（元）",
						data : "smsUnitPrice",
					},
					{
						title : "创建时间",
						data : "createTime",
						createdCell : function(td, tdData) {
							$(td).html(formatDate(tdData));
						}
					},
					{
						title : "完成时间",
						data : "pushOrderTime",
						createdCell : function(td, tdData) {
							$(td).html(formatDate(tdData));
						}
					},
					{
						title : "交易状态",
						data : "status",
						createdCell : function(td, tdData) {
							var str = tdData == 0 ? "<span class='label label-primary'>待支付</span>" : tdData == 1 ? "<span class='label label-success'>已完成</span>" : "<span class='label label-danger'>交易异常</span>";
							$(td).html(str);
						}
					},
					{
						title : "发票信息",
						data : "ticketId",
						createdCell : function(td, tdData,rowData) {
							var info = "";
							if(rowData.status==1){//订单已完成
								info = $("<button class='btn btn-sm btn-primary'>查看详情</button>");
								if (tdData == null || tdData == "") {
									info = $("<button class='btn btn-sm btn-info'>申请发票</button>");
								}
							}else{//订单未完成
								info = "<button class='btn btn-sm btn-success'>去支付</button>";
							}
							$(td).html(info);
						}
					} ],
		});
		
		//查询出当前品牌的短信单价
		$.post("smschargeorder/selectSmsUnitPrice",function(result){
			$(":input[name='smsUnitPrice']").val(result.data);
		})
		
	}());
	
	//自动计算出 对应的短信条数
	function computeSmsCount(){
		var chargeMoney = $(":input[name='chargeMoney']").val();
		if(chargeMoney>0){
			var smsUnitPrice = $(":input[name='smsUnitPrice']").val();
			var sum = chargeMoney/smsUnitPrice;  
			$(":input[name='number']").val(sum.toFixed(0));
		}else{
			$(":input[name='chargeMoney']").val("0");
			$(":input[name='number']").val("0");
		}
	}
	
	function formatDate(date){
		var temp = "";
		if(date!=null && date!=""){
			temp = new Date(date);
			temp = temp.format("yyyy-MM-dd hh:mm:ss");
		}
		return temp;
	}
</script>
