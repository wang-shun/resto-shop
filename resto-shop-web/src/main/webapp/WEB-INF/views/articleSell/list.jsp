<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
</style>
<div id="control">
	<h2 class="text-center">
		<strong>品牌菜品销售</strong>
	</h2>
	<br />
	<div class="row">
		<div class="col-md-12">
			<form class="form-inline">
				<div class="form-group" style="margin-right: 50px;">
					<label for="beginDate">开始时间：</label> <input type="text"
						class="form-control form_datetime" v-model="searchDate.beginDate"
						readonly="readonly">
				</div>
				<div class="form-group" style="margin-right: 50px;">
					<label for="endDate">结束时间：</label> <input type="text"
						class="form-control form_datetime" v-model="searchDate.endDate"
						readonly="readonly">
				</div>
				<button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>
			</form>
		</div>
	</div>
	<br /> <br />
	<!-- 品牌菜品销售总量 -->
	<div class="panel panel-info">
		<div class="panel-heading text-center" style="font-size: 22px;">
			<strong>品牌菜品销售总量</strong>
		</div>
		<div class="panel-body">
			<table class="table table-striped table-bordered table-hover">
				<thead>
					<tr>
						<th>品牌名称</th>
						<th>菜品总销量(份)</th>
						<th>销售详情</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><strong>{{brandReport.brandName}}</strong></td>
						<td>{{brandReport.totalNum}}</td>
						<td><button class="btn btn-success"
								@click="showBrandReport(brandReport.brandName)">查看详情</button></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>

	<!-- 店铺菜品销售总量  -->
	<div class="panel panel-info">
		<div class="panel-heading text-center" style="font-size: 22px;">
			<strong>菜品销售总量 </strong>
		</div>
		<div class="panel-body">
			<table class="table table-striped table-bordered table-hover">
				<thead>
					<tr>
						<th>店铺名称</th>
						<th>菜品总销量(份)</th>
						<th>销售详情</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopReportList">
						<td><strong>{{shop.name}}</strong></td>
						<td>{{shop.articleSellNum}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.name,shop.id)">查看详情</button></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>

	<!-- 报表详情 -->
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
						<strong>菜品销售详情</strong>
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
<script>
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

	var vueObj = new Vue({
		el : "#control",
		data : {
			brandReport : {
				brandName : "",
				totalNum : 0
			},
			shopReportList : [],
			searchDate : {
				beginDate : "",
				endDate : "",
			},
			modalInfo:{
				title:"",
				content:""
			},
		},
		methods : {
			showBrandReport : function(brandName) {
				this.openModal("articleSell/show/brandReport", brandName,null);
			},
			showShopReport : function(shopName,shopId) {
				this.openModal("articleSell/show/shopReport", shopName,shopId);
			},
			searchInfo : function(beginDate, endDate) {
				console.log("begin:" + this.searchDate.beginDate);
				console.log("end:" + this.searchDate.endDate);
				App.startPageLoading();
				var that = this;
				$.post("articleSell/list_all", this.getDate(null), function(result) {
					that.brandReport.brandName = result.brandName;
					that.brandReport.totalNum = result.totalNum;
					that.shopReportList = result.shopReportList;
					App.stopPageLoading();
				});
			},
			openModal : function(url, modalTitle,shopId) {
				$.post(url, this.getDate(shopId),function(result) {
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
			getDate : function(shopId){
				var data = {
					beginDate : this.searchDate.beginDate,
					endDate : this.searchDate.endDate,
					shopId : shopId
				};
				return data;
			}
		},
		created : function() {
			var date = new Date().format("yyyy-MM-dd");
			this.searchDate.beginDate = date;
			this.searchDate.endDate = date;
			this.searchInfo();
		}
	})
</script>
