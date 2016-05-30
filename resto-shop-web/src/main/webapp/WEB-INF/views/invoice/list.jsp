<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
dt, dd {
	line-height: 35px;
}
</style>

<div id="control">

	<!-- 申请发票	begin -->
	<div class="modal fade" id="applyInvoice" tabindex="-1" role="dialog"
		aria-labelledby="myModlLabel">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title text-center">
						<strong>申请发票</strong>
					</h4>
				</div>
				<div class="modal-body">
					<div>
						<!-- 选项卡 -->
						<ul class="nav nav-tabs" role="tablist">
							<li role="presentation" class="active"><a href="#general"
								aria-controls="general" role="tab" data-toggle="tab">普通发票</a></li>
							<li role="presentation"><a href="#increment"
								aria-controls="increment" role="tab" data-toggle="tab">增值发票</a></li>
						</ul>
						<!-- 选项卡内容 -->
						<div class="tab-content">
							<div role="tabpanel" class="tab-pane active" id="general">
								<form class="form-horizontal" action="invoice/create"  @submit.prevent="save">
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">发票抬头：</label>
										<div class="col-sm-8">
											<input type="text" class="form-control" required name="header" v-model="invoice.header">
										</div>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">发票内容：</label>
										<div class="col-sm-8">
											<div class="md-radio-inline">
												<div class="md-radio">
													<input type="radio" id="type_1" name="content"
														checked="checked" class="md-radiobtn" value="明细"> <label
														for="type_1"> <span> </span> <span class="check"></span>
														<span class="box"></span> 明细
													</label>
												</div>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">发票金额：</label>
										<div class="col-sm-8">
											<input class="bs-select form-control" type="number" required name="money" min="100"/>
										</div>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">收件地址：</label>
										<div class="col-sm-8">
											<select class="bs-select form-control" name="consigneceId" v-model="currentAddress">
												<option v-for="item in addressInfo" value="{{item.id}}">{{item.address}}</option>
											</select>
											<input type="hidden" type="text" name="address"/>
										</div>
										<button type="button" class="col-sm-1 btn btn-sm green-meadow"
											data-toggle="modal" data-target="#addressInfoModal">
											添加</button>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">收 件
											人：</label>
										<div class="col-sm-8">
											<input type="text" class="form-control" required
												name="name" v-model="currentAddress.name">
										</div>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">联系电话：</label>
										<div class="col-sm-8">
											<input type="text" class="form-control" required
												name="phone">
										</div>
									</div>
									<div class="form-group">
										<label for="header" class="col-sm-3 control-label">备&nbsp;&nbsp;注：</label>
										<div class="col-sm-8">
											<textarea class="form-control" name="remark"></textarea>
										</div>
									</div>
									<input type="hidden" name="type" value="1" />
									<div class="text-center">
										<a class="btn default" data-dismiss="modal">取消</a> <input
											class="btn green" type="submit" value="申请" />
									</div>
								</form>
							</div>
							<div role="tabpanel" class="tab-pane" id="increment">
								<form class="form-horizontal" action="invoice/create"  @submit.prevent="save">
									<div id="increment_info">
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">单位名称：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="header">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">纳税人识别码：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="taxpayerCode">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">注册地址：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="registeredAddress">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">注册电话：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="registeredPhone">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">开户银行：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="bankName">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">银行账户：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="bankAccount">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">发票金额：</label>
											<div class="col-sm-8">
												<input class="bs-select form-control" type="number" required name="money" min="100"/>
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">发票内容：</label>
											<div class="col-sm-8">
												<div class="md-radio-inline">
													<div class="md-radio">
														<input type="radio" id="type_1" name="content"
															checked="checked" class="md-radiobtn" value="明细"> <label
															for="type_1"> <span> </span> <span class="check"></span>
															<span class="box"></span> 明细
														</label>
													</div>
												</div>
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">收件地址：</label>
											<div class="col-sm-8">
												<select class="bs-select form-control" name="consigneceId">
													<option v-for="item in addressInfo" value="{{item.id}}">{{item.address}}</option>
												</select>
												<input type="hidden" type="text" name="address"/>
											</div>
											<button type="button"
												class="col-sm-1 btn btn-sm green-meadow" data-toggle="modal"
												data-target="#addressInfoModal">添加</button>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">收
												件 人：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="name">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">联系电话：</label>
											<div class="col-sm-8">
												<input type="text" class="form-control" required
													name="phone">
											</div>
										</div>
										<div class="form-group">
											<label for="header" class="col-sm-3 control-label">备&nbsp;&nbsp;注：</label>
											<div class="col-sm-8">
												<textarea class="form-control" name="remark"></textarea>
											</div>
										</div>
									<input type="hidden" name="type" value="2" />
										<div class="text-center">
											<a class="btn default" data-dismiss="modal">取消</a> <input
												class="btn green" type="submit" value="申请" />
										</div>
									</div>
								</form>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- 申请发票	end -->
	
	<!-- 添加地址 	begin-->
	<div class="modal fade" id="addressInfoModal" tabindex="-1"
		role="dialog" aria-labelledby="addressInfoModal">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title text-center">
						<strong>添加地址</strong>
					</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" onsubmit="return addressInfoForm()"
						id="addressInfoForm">
						<div class="form-group">
							<label for="header" class="col-sm-3 control-label">收 件 人：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" required name="name">
							</div>
						</div>
						<div class="form-group">
							<label for="header" class="col-sm-3 control-label">联系电话：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" required name="phone">
							</div>
						</div>
						<div class="form-group">
							<label for="header" class="col-sm-3 control-label">收件地址：</label>
							<div class="col-sm-8">
								<input type="text" class="form-control" required name="address">
							</div>
						</div>
						<div class="text-center">
							<a class="btn default" data-dismiss="modal">取消</a> <input
								class="btn green" type="submit" value="添加" />
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	<!-- 添加地址 	end-->
	
	
	<!-- 增值发票详情   begin -->
	<div class="modal fade" id="consigneeModal" tabindex="-1" role="dialog"
		aria-labelledby="consigneeModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title text-center">
						<strong>增值发票详情</strong>
					</h4>
				</div>
				<div class="modal-body">
					<dl class="dl-horizontal">
						<dt>单位名称：</dt>
						<dd>{{smsticketInfo.header}}</dd>
						<dt>纳税人识别码：</dt>
						<dd>{{smsticketInfo.taxpayerCode}}</dd>
						<dt>注册地址：</dt>
						<dd>{{smsticketInfo.registeredAddress}}</dd>
						<dt>注册电话：</dt>
						<dd>{{smsticketInfo.registeredPhone}}</dd>
						<dt>开户银行：</dt>
						<dd>{{smsticketInfo.bankName}}</dd>
						<dt>银行账户：</dt>
						<dd>{{smsticketInfo.bankAccount}}</dd>
						<dt>收件人姓名：</dt>
						<dd>{{smsticketInfo.name}}</dd>
						<dt>收件人电话：</dt>
						<dd>{{smsticketInfo.phone}}</dd>
						<dt>收件人地址：</dt>
						<dd>{{smsticketInfo.address}}</dd>
					</dl>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭
					</button>
				</div>
			</div>
		</div>
	</div>
	<!-- 增值发票详情   end -->
	
	<!-- 普通发票详情   begin-->
	<div class="modal fade" id="generalModal" tabindex="-1" role="dialog"
		aria-labelledby="generalModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title text-center">
						<strong> 普通发票详情</strong>
					</h4>
				</div>
				<div class="modal-body">
					<dl class="dl-horizontal">
						<dt>收件人姓名：</dt>
						<dd>{{smsticketInfo.name}}</dd>
						<dt>收件人电话：</dt>
						<dd>{{smsticketInfo.phone}}</dd>
						<dt>收件人地址：</dt>
						<dd>{{smsticketInfo.address}}</dd>
					</dl>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭
					</button>
				</div>
			</div>
		</div>
	</div>
	<!-- 普通发票详情   end -->
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="notice/add">
				<button type="button" class="btn green " data-toggle="modal"
					data-target="#applyInvoice">申请发票</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
</div>

<script>
	$(document).ready(function() {
	//载入 表格数据
	tb = $('.table-body>table').DataTable(
	{
		ajax : {
			url : "invoice/list_all",
			dataSrc : "data"
		},
		columns : [
				{
					title : "发票抬头",
					data : "header",
				},
				{
					title : "内容",
					data : "content",
				},
				{
					title : "金额(人民币)",
					data : "money",
				},
				{
					title : "备注",
					data : "remark",
				},
				{
					title : "快递单号",
					data : "expersage",
					createdCell : function(td, tdData) {
						$(td).html(tdData != null ? "<a href='http://m.kuaidi100.com/result.jsp?com=&nu="+ tdData+ "' target='_blank'>"+ tdData+ "</a>": "未完成");
					}
				},
				{
					title : "发票类型",
					data : "type",
					createdCell : function(td, tdData) {
						$(td).html(tdData == 1 ? "普通发票": "增值税发票");
					}
				},
				{
					title : "申请时间",
					data : "createTime",
					createdCell : function(td, tdData) {
						$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
					}
				},
				{
					title : "完成时间",
					data : "pushTime",
					createdCell : function(td, tdData) {
						$(td).html(tdData != null ? new Date(tdData).format("yyyy-MM-dd hh:mm:ss"): "未完成")
					}
				},
				{
					title : "状态",
					data : "ticketStatus",
					createdCell : function(td, tdData) {
						$(td).html(tdData == 0 ? "申请中": "已完成");
					}
				},
				{
					title : "操作",
					data : "id",
					createdCell : function(td, tdData,rowData) {
						var info = [];
						if (rowData.type == 2) {
							var btn = createBtn("查看详情",function() {
								vueObj.showDetailInfo(rowData);
								$("#consigneeModal").modal();
							})
						} else if (rowData.type == 1) {
							var btn = createBtn("查看详情",function() {
								vueObj.showDetailInfo(rowData);
								$("#generalModal").modal();
							})
						}
						info.push(btn);
						$(td).html(info);
					}
				} ],
	});

		var C = new Controller("#control", tb);

		var vueObj = new Vue({
			el : "#control",
			data : {
				smsticketInfo : {},
				invoice:{},
				addressInfo:[],
				currentAddress:"",
			},
			methods : {
				create : function() {
				},
				showDetailInfo : function(smsticketInfo) {
					this.smsticketInfo = smsticketInfo;
				},
				cancel:function(){
					$("#applyInvoice").modal("hide");
					$("form")[0].reset();
				},
				save : function(e) {
					var that = this;
					var formDom = e.target;
					C.ajaxFormEx(formDom,function(){
						that.cancel();
						tb.ajax.reload();
					});
				},
				queryAddress : function(){
					var that=this;
					$.post("addressinfo/list_all",function(result){
						that.addressInfo=result.data;
						that.currentAddress=result.data[0].id;
					})
				},
				changedTest : function(){
					console.log("change");
				}
			},
			created : function(){
				this.queryAddress();
			},
			 watch: {
				 currentAddress: function(val) {
// 					 $(this.addressInfo).each(function(i,item){
// 						 console.log(item);
// 						 if(item.id==val){
// 							 this.currentAddress.name=item.name;
// 							 this.currentAddress.phone=item.phone;
// 						 }
// 					 })
// 					 console.log(this.currentAddress);
					 console.log(val);
                }
            }
		});

		//创建一个按钮
		function createBtn(btnValue, btnfunction) {
			return $('<input />', {
				value : btnValue,
				type : "button",
				class : "btn btn-sm btn-primary",
				click : btnfunction
			})
		}

	});

	
</script>