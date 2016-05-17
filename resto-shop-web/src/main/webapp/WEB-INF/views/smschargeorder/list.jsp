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
		<table class="table table-striped table-hover table-bordered "></table>
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
				<form role="form" class="form-horizontal">
					<div class="form-body">
						<div class="form-group">
							<label class="col-sm-3 control-label">充值品牌：</label>
							<div class="col-sm-8">
								<input type="text" disabled="disabled" class="form-control" required name="brandName"> 
								<input type="hidden" name="brandId">
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
								<input type="text" class="form-control" required name="chargeMoney">
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
							$(":input[name='brandId']").val(rowData.brandId);
							$(":input[name='smsUnitPrice']").val(rowData.smsUnitPrice);
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
						title : "创建时间",
						data : "createTime",
						createdCell : function(td, tdData, rowData, row) {
							var temp = new Date(tdData);
							temp = temp.format("yyyy-MM-dd hh:mm:ss");
							$(td).html(temp);
						}
					},
					{
						title : "完成时间",
						data : "pushOrderTime",
						createdCell : function(td, tdData, rowData, row) {
							var temp = new Date(tdData);
							temp = temp.format("yyyy-MM-dd hh:mm:ss");
							$(td).html(temp);
						}
					},
					{
						title : "发票信息",
						data : "ticketId",
						createdCell : function(td, tdData) {
							var info = $("<button class='btn btn-xs blue'>查看详情</button>");
							if (tdData == null || tdData == "") {
								var info = $("<button class='btn btn-xs green'>申请发票</button>");
							}
							$(td).html(info);
						}
					} ],
		});
		
		//自动计算出 对应的短信条数
		$(":input[name='chargeMoney']").keyup(function(){
			var chargeMoney = $(this).val();
			if(!isNaN(chargeMoney)){
				var smsUnitPrice = $(":input[name='smsUnitPrice']").val();
				var sum = smsUnitPrice * chargeMoney * 100;  
				$(":input[name='number']").val(sum.toFixed(0));
			}else{
				$(this).val(chargeMoney.substring(0,chargeMoney.length-1));
			}
		})
	}());
</script>
