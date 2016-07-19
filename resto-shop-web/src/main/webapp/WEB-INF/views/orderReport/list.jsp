<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
<h2 class="text-center"><strong>订单列表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate"   readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate"   readonly="readonly">
		  </div>
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;
		  <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>
		</form>
	</div>
</div>
<br/>
<br/>

<!-- 品牌订单列表 -->
    <div role="tabpanel" class="tab-pane" id="orderReport">
    	<div class="panel panel-primary" style="border-color:write;">
		  	<!-- 品牌订单 -->
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">品牌订单列表</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="brandOrderTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>店铺名称</th>
						<th>已消费订单(份)</th>
						<th>已消费订单金额(元)</th>
						<th>订单平均金额(元)</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopOrderList">
						<td><strong>{{shop.shopName}}</strong></td>
						<td>{{shop.number}}</td>	
						<td>{{shop.orderMoney}}</td>
						<td>{{shop.average}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.shopName,shop.shopDetailId)">查看详情</button></td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
		</div>
		
		<div class="modal fade bs-example-modal-lg" id="reportModal" 
		tabindex="-1" role="dialog" aria-labelledby="reportModal" 
		data-backdrop="static"> 
		<div class="modal-dialog modal-lg"> 
			<div class="modal-content"> 
				<div class="modal-header"> 
					<button type="button" class="close" data-dismiss="modal" 
						aria-label="Close"> 
						<span aria-hidden="true">&times;</span> 
					</button> 
					<h4 class="modal-title text-center"> 
						<strong>店铺充值记录</strong> 
					</h4> 
				</div> 
				<div class="modal-body"></div> 
				<div class="modal-footer"> 
					<button type="button" class="btn btn-info btn-block" @click="closeModal">关闭</button> 
				</div> 
			</div> 
		</div> 
	</div> 
		
		
    </div>
  </div>

<script>

//时间插件
$('.form_datetime').datetimepicker({
		endDate:new Date(),
		minView:"month",
		maxView:"month",
		autoclose:true,//选择后自动关闭时间选择器
		todayBtn:true,//在底部显示 当天日期
		todayHighlight:true,//高亮当前日期
		format:"yyyy-mm-dd",
		startView:"month",
		language:"zh-CN"
	});


//创建vue对象
var vueObj =  new Vue({
	el:"#control",
	data:{
		shopOrderList : [],
		searchDate : {
			beginDate : "",
			endDate : "",
		},
		modalInfo:{
			title:"",
			content:""
		},
	},
	methods:{
		searchInfo : function(beginDate, endDate) {
		var that = this;
		//判断 时间范围是否合法
		if(beginDate>endDate){
			toastr.error("开始时间不能大于结束时间")
			return ;
		}
			$.post("orderReport/brand_data", this.getDate(null), function(result) {
					that.shopOrderList = result;
					toastr.success("查询成功");
				});
		},
		getDate : function(shopId){
			var data = {
				beginDate : this.searchDate.beginDate,
				endDate : this.searchDate.endDate,
				shopId : shopId
			};
			return data;
		},
		showShopReport : function(shopName,shopId) {
			this.openModal("orderReport/show/shopReport", shopName,shopId);
		},
		openModal : function(url, modalTitle,shopId) {
			$.post(url, this.getDate(shopId),function(result) {
				//console.log(result)
				var modal = $("#reportModal");
				modal.find(".modal-body").html(result);
				modal.find(".modal-title > strong").html(modalTitle);
				modal.modal();
			})
		
		},
		closeModal : function(){
			var modal = $("#reportModal");
			modal.find(".modal-body").html("");
			modal.modal("hide");
		},
	},
	created : function() {
		var date = new Date().format("yyyy-MM-dd");
		this.searchDate.beginDate = date;
		this.searchDate.endDate = date;
		this.searchInfo();
	}
	
})


//查询报表
$("#searchReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	//判断 时间范围是否合法
	if(beginDate>endDate){
		toastr.error("开始时间不能大于结束时间");
		return ;
	}
	var data = {"beginDate":beginDate,"endDate":endDate};
	//更新数据
	tb1.ajax.reload();
	toastr.success("查询成功");
	
})

//下载报表

$("#brandreportExcel").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	//判断 时间范围是否合法
	if(beginDate>endDate){
		toastr.error("开始时间不能大于结束时间");
		return ;
	}
	
	location.href="orderReport/brand_excel?beginDate="+beginDate+"&&endDate="+endDate;
	
})





</script>

