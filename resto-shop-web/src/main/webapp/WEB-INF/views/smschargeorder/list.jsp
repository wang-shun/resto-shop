<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div class="table-div">
	<div class="table-operator">
		<s:hasPermission name="notice/add">
			<button type="button" class="btn blue"
				data-toggle="modal" data-target="#applyInvoice">申请发票</button>&nbsp;&nbsp;&nbsp;
			<button type="button" class="btn green "
				data-toggle="modal" data-target="#createChargeOrder">短信充值</button>
		</s:hasPermission>
	</div>
	<div class="clearfix"></div>
	<div class="table-filter"></div>
	<div class="table-body">
		<table class="table table-striped table-hover table-bordered"></table>
	</div>
</div>

<!-- 短信充值 -->
<div class="modal fade" id="createChargeOrder" tabindex="-1" role="dialog" data-backdrop="static">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title text-center">
					<strong>短信充值</strong>
				</h4>
			</div>
			<div class="modal-body">
				<form role="form" class="form-horizontal"
					action="smschargeorder/smsCharge" method="post" target="_blank" onsubmit="showChargeInfo()">
					<!-- 				<form role="form" class="form-horizontal" onsubmit="return false"> -->
					<div class="form-body">
						<div class="form-group">
							<label class="col-sm-3 control-label">充值品牌：</label>
							<div class="col-sm-8">
								<input type="text" disabled="disabled" class="form-control"
									required name="brandName">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">短信单价：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" disabled="disabled"
									name="smsUnitPrice">
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">充值金额：</label>
							<div class="col-sm-8">
								<div class="input-group">
									<input type="number" class="form-control" max="10000"
										placeholder="请输入要充值的金额" onchange="computeSmsCount()"
										onkeyup="computeSmsCount()" required name="chargeMoney">
									<div class="input-group-addon">元</div>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">短信条数：</label>
							<div class="col-sm-8">
								<div class="input-group">
									<input type="text" class="form-control" disabled="disabled"
										name="number">
									<div class="input-group-addon">条</div>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-sm-3 control-label">支付方式：</label>
							<div class="col-sm-8">
								<div class="md-radio-list">
									<div class="md-radio">
										<input type="radio" id="alipay" name="paytype"
											checked="checked" class="md-radiobtn" value="1"> <label
											for="alipay"> <span></span> <span class="check"></span>
											<span class="box"></span>&nbsp;<img alt="支付宝支付"
											src="assets/pages/img/alipay.png" width="23px" height="23px">&nbsp;支付宝支付
										</label>
									</div>
									<div class="md-radio">
										<input type="radio" id="wxpay" name="paytype"
											class="md-radiobtn" value="2"> <label for="wxpay">
											<span></span> <span class="check"></span> <span class="box"></span>&nbsp;<img
											alt="微信支付" src="assets/pages/img/wxpay.png" width="23px"
											height="23px">&nbsp;微信支付
										</label>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="text-center" id="chargeBtn">
						<a class="btn default" data-dismiss="modal">取消</a> <input
							class="btn green" type="submit" value="充值" />
					</div>
				</form>
			</div>
		</div>
	</div>
</div>


<!-- 申请发票 -->
<div class="modal fade" id="applyInvoice" tabindex="-1" role="dialog" aria-labelledby="myModlLabel">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title text-center">
					<strong>申请发票</strong>
				</h4>
			</div>
			<div class="modal-body">
				<form class="form-horizontal" onsubmit="return applyInvoiceForm()" id="applyInvoiceForm">
					<div class="form-group">
						<label for="header" class="col-sm-3 control-label">发票抬头：</label>
						<div class="col-sm-8">
							<input type="text" class="form-control" required name="header">
						</div>
					</div>
					<div class="form-group">
						<label for="header" class="col-sm-3 control-label">发票内容：</label>
						<div class="col-sm-8">
							<div class="md-radio-inline">
								<div class="md-radio">
									<input type="radio" id="type_1" name="type" checked="checked" class="md-radiobtn">
									<label for="type_1"> <span> </span>
									<span class="check"></span> <span class="box"></span> 明细
									</label>
								</div>
							</div>
						</div>
					</div>
					<div class="text-center">
						<input type="hidden" name="chargeOrderId"/>
						<a class="btn default" data-dismiss="modal">取消</a> <input
							class="btn green" type="submit" value="申请"/>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<script>
	var tb ;
	(function() {
		var cid = "#control";
		var $table = $(".table-body>table");
		tb = $table.DataTable({
					ajax : {
						url : "smschargeorder/list_all",
						dataSrc : "data"
					},
					columns : [
							{
								title : "品牌名称",
								data : "brandName",
								createdCell : function(td, tdData, rowData) {
									$(":input[name='brandName']").val(tdData);//充值
									$(":input[name='header']").val(tdData);//发票
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
									$(td).html(tdData!=null?formatDate(tdData):"未完成");
								}
							},
							{
								title : "支付类型",
								data : "payType",
								createdCell : function(td, tdData) {
									var payType = tdData==1?
											"<img alt=\"微信支付\" src=\"assets/pages/img/alipay.png\" width=\"23px\" height=\"23px\">&nbsp;支付宝"
											:"<img alt=\"微信支付\" src=\"assets/pages/img/wxpay.png\" width=\"23px\" height=\"23px\">&nbsp;微&nbsp;信";
									$(td).html(payType);
								}
							},
							{
								title : "交易状态",
								data : "status",
								createdCell : function(td, tdData) {
									var str = tdData == 0 ? "<span class='label label-primary'>待 支 付</span>"
											: tdData == 1 ? "<span class='label label-success'>已 完 成</span>"
													: "<span class='label label-danger'>交易异常</span>";
									$(td).html(str);
								}
							},
							{
								title : "发票信息",
								data : "ticketId",
								createdCell : function(td, tdData, rowData) {
									var info = "";
									if (rowData.status == 1) {//订单已完成
										if (tdData == null || tdData == "") {
											info = createBtn(rowData.id,"申请发票","btn-sm green-meadow",function() {
												$(":input[name='chargeOrderId']").val(rowData.id);//发票
												$("#applyInvoice").modal();
											})
										} else {
											info = createBtn(null,"查看详情","btn-sm btn-primary",function() {
												alert("我是发票详情");
											})
										}
									} else {//订单未完成
										info = $("<a href='smschargeorder/payAgain?chargeOrderId="+rowData.id+"' class='btn btn-sm btn-success' target='_blank'>去 支 付</a>")
									}
									$(td).html(info);
								}
							} ],
				});

		//查询出当前品牌的短信单价
		$.post("smschargeorder/selectSmsUnitPrice", function(result) {
			$(":input[name='smsUnitPrice']").val(result.data);
		})
	}());

	//自动计算出 对应的短信条数
	function computeSmsCount() {
		var chargeMoney = $(":input[name='chargeMoney']").val();
		if (chargeMoney > 0) {
			var smsUnitPrice = $(":input[name='smsUnitPrice']").val();
			var sum = chargeMoney / smsUnitPrice;
			$(":input[name='number']").val(sum.toFixed(0));
		} else {
			$(":input[name='chargeMoney']").val("0");
			$(":input[name='number']").val("0");
		}
	}
	
	//短信充值，显示订单详情
	function showChargeInfo(){
		$("#successBtn").show();
		var successBtn = createBtn(null,"充值成功","green",function(){
			$("#createChargeOrder").modal("hide");
			tb.ajax.reload();//刷新
			$("#chargeBtn").html("<a class='btn default' data-dismiss='modal'>取消</a> <input class='btn green' type='submit' value='充值' />");
			$(":input[name='chargeMoney']").val("");
			$(":input[name='number']").val("");
		});
		$("#chargeBtn").html(successBtn);
	}

	//申请发票  ajax提交
	function applyInvoiceForm(){
		var data = $("#applyInvoiceForm").serialize();
		$.post("smschargeorder/applyInvoice",data,function(result){
			if(result.success){
				$("#applyInvoice").modal("hide");
				toastr.success("申请成功！");
			}else{
				$("#applyInvoice").modal("hide");
				toastr.error("申请失败！请重新操作！");
			}
			tb.ajax.reload();
		});
		return false;
	}
	
	//格式化时间
	function formatDate(date) {
		var temp = "";
		if (date != null && date != "") {
			temp = new Date(date);
			temp = temp.format("yyyy-MM-dd hh:mm:ss");
		}
		return temp;
	}
	
	//创建一个按钮
	function createBtn(btnName,btnValue,btnClass,btnfunction){
		return $('<input />', {
			name : btnName,
			value : btnValue,
			type : "button",
			class : "btn " + btnClass,
			click : btnfunction
		})
	}
</script>
