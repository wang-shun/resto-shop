<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<h2 class="text-center">
		<strong>品牌菜品销售</strong>
	</h2>
	<br />
	<div class="row" id="searchTools">
		<div class="col-md-12">
			<form class="form-inline">
				<div class="form-group" style="margin-right: 50px;">
					<label for="beginDate">开始时间：</label> <input type="text"
						class="form-control form_datetime" v-model="beginDate"
						id="beginDate" readonly="readonly">
				</div>
				<div class="form-group" style="margin-right: 50px;">
					<label for="endDate">结束时间：</label> <input type="text"
						class="form-control form_datetime" id="endDate" v-model="endDate"
						readonly="readonly">
				</div>
				<button type="button" class="btn btn-primary" @click="search">查询报表</button>
			</form>
		</div>
	</div>
	<br /> <br />
	<!-- 品牌菜品销售总量  -->
	<!-- 		<div class="panel panel-info"> -->
	<!-- 			<div class="panel-heading text-center" style="font-size: 22px;"> -->
	<%-- 				<strong>品牌菜品销售总量</strong> --%>
	<!-- 			</div> -->
	<!-- 			<div class="panel-body"> -->
	<!-- 				<table class="table table-hover"> -->
	<!-- 					<thead> -->
	<!-- 						<tr> -->
	<!-- 							<th>品牌名称</th> -->
	<!-- 							<th>菜品总销量(份)</th> -->
	<!-- 							<th>销售详情</th> -->
	<!-- 						</tr> -->
	<!-- 					</thead> -->
	<!-- 					<tbody> -->
	<!-- 						<tr> -->
	<%-- 							<td><strong>{{brandReport.brandName}}</strong></td> --%>
	<!-- 							<td>{{getSumNum}}</td> -->
	<!-- 							<td><button class="btn btn-success" @click="showBrandReport">查看详情</button></td> -->
	<!-- 						</tr> -->
	<!-- 					</tbody> -->
	<!-- 				</table> -->
	<!-- 			</div> -->
	<!-- 		</div> -->

	<!-- 店铺菜品销售总量  -->
	<div class="panel panel-info">
		<div class="panel-heading text-center" style="font-size: 22px;">
			<strong>菜品销售总量 </strong>
		</div>
		<div class="panel-body">
			<table class="table table-hover">
				<thead>
					<tr>
						<th>店铺名称</th>
						<th>菜品总销量(份)</th>
						<th>销售详情</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopReportList">
						<td><strong>{{shop.shopName}}</strong></td>
						<td>{{shop.sellNum}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showBrandReport">查看详情</button></td>
					</tr>
					<tr class="success">
						<td><strong>【{{brandReport.brandName}}】 总计：</strong></td>
						<td>{{getSumNum}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showBrandReport">查看详情</button></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	
	<!-- 品牌报表 -->
	<div class="modal fade" id="brandReortModal" tabindex="-1" role="dialog" aria-labelledby="brandReortModal">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title text-center"><strong>菜品销售详情</strong></h4>
	      </div>
	      <div class="modal-body">
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
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
	//文本框默认值
	// 	$('.form_datetime').val(new Date().format("yyyy-MM-dd"));

	// var $table = $("#testTable");
	// tb = $table.DataTable({
	// 	ajax : {
	// 		url : "articleSell/list_all",
	// 		dataSrc : "data"
	// 	},
	// 	columns : [
	// 	    {
	// 			title : "店铺名称",
	// 			data : "shopName",
	// 		},                 
	// 		{                 
	// 			title : "菜品总销量(份)",
	// 			data : "sellNum",
	// 		},                 

	// 		{                 
	// 			title : "销售详情",
	// 			data : "shopId",
	// 			createdCell : function(td,tdData){
	// 				var btn = $("<button>").html("查看详情").addClass("btn green");
	// 				$(td).html(btn);
	// 			}
	// 		},                 
	// 		],
	// 		footerCallback: function () {
	//            var api = this.api();
	//            // 总计
	//            total = api.column(1).data().reduce( function (a, b) {
	//                return a+b;
	//            } , 0 );
	//            $( api.column(1).footer()).html(total);
	//        }
	// });

	var vueObj = new Vue({
		el : "#control",
		data : {
			brandReport : {
				brandName : "",
				sumNum : 0
			},
			shopReportList : [],
			beginDate : "",
			endDate : ""
		},
		computed : {
			getSumNum : function() {
				var sumNum = 0;
				$(this.shopReportList).each(function(i, item) {
					sumNum += item.sellNum;
				})
				return sumNum;
			}
		},
		methods : {
			showBrandReport : function() {
// 				$("#brandReortModal").modal();
				this.openModal("articleSell/show/brandReport",$("#brandReortModal"));
			},
			showShopReport : function() {
				alert("---shop");
			},
			search : function() {
				console.log("begin:" + this.beginDate);
				console.log("end:" + this.endDate);
				this.getSellInfo(this.beginDate, this.endDate);
			},
			getSellInfo : function(beginDate, endDate) {
				App.startPageLoading();
				var that = this;
				var data = {
					beginDate : beginDate,
					endDate : endDate
				};
				$.post("articleSell/list_all", data, function(result) {
					that.brandReport.brandName = result.data[0].brandName;
					that.shopReportList = result.data;
					App.stopPageLoading();
				});
			},
			openModal : function(url,modal){
				$.post(url,function(result){
					modal.find(".modal-body").html(result);
					modal.modal();
				})
			}
		},
		created : function() {
			var date = new Date().format("yyyy-MM-dd");
			this.beginDate = date;
			this.endDate = date;
			this.getSellInfo(date, date);
		}
	})
</script>
