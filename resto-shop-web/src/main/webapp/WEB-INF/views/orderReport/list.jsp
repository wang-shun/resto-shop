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
		  	
		   	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
                 
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
          
             
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
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
						<th>品牌</th>
                        <th>订单总额(元)</th>
						<th>订单总数(份)</th>
						<th>订单平均金额(元)</th>
						<th>营销撬动率</th>
					</tr>
				</thead>
				<tbody>
						<td><strong>{{brandOrder.name}}</strong></td>
						<td>{{brandOrder.orderMoney}}</td>
                        <td>{{brandOrder.number}}</td>
                        <td>{{brandOrder.average}}</td>
						<td>{{brandOrder.marketPrize}}</td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		  
		  <div class="panel-body">
		  	<table id="shopOrderTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>店铺名称</th>
						<th>订单总额(元)</th>
                        <th>订单总数(份)</th>
						<th>订单平均金额(元)</th>
						<th>营销撬动率</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopOrderList">
						<td><strong>{{shop.name}}</strong></td>
						<td>{{shop.orderMoney}}</td>
                        <td>{{shop.number}}</td>
                        <td>{{shop.average}}</td>
						<td>{{shop.marketPrize}}</td>
						<td><button class="btn btn-sm btn-success" @click="showShopReport(shop.shopName,shop.shopDetailId)">查看详情</button></td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
		</div>
		
	  <div class="modal fade" id="reportModal" tabindex="-1" role="dialog" data-backdrop="static">
           <div class="modal-dialog modal-full">
               <div class="modal-content">
                   <div class="modal-header">
                       <button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>
                   </div>
                   <div class="modal-body"> </div>
                   <div class="modal-footer">
<!--                         <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button> -->
						<button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal">关闭</button>
                   </div>
               </div>
               <!-- /.modal-content -->
           </div>
           <!-- /.modal-dialog -->
       </div>
       
    </div>

 <script src="assets/customer/date.js" type="text/javascript"></script>

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
		countlist :[],
		appraiseCount:{},
		shopOrderList : [],
		brandOrder:{},
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
					that.shopOrderList = result.shopId;
					that.brandOrder = result.brandId;
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
			$("#reportModal").modal('show');
            vueObj.openModal("orderReport/show/shopReport", shopName,shopId);
		},
		openModal : function(url, modalTitle,shopId) {
			$.post(url, this.getDate(shopId),function(result) {
				var modal = $("#reportModal");
				modal.find(".modal-body").html(result);
				modal.find(".modal-title > strong").html(modalTitle);
				modal.modal()
			})
		
		},
		closeModal : function(){
			var modal = $("#reportModal");
			modal.find(".modal-body").html("");
			modal.modal({show:false});
		},
		today : function(){
			date = new Date().format("yyyy-MM-dd");
			this.searchDate.beginDate = date
			this.searchDate.endDate = date
			this.searchInfo();
		},
		yesterDay : function(){
			
			this.searchDate.beginDate = GetDateStr(-1);
			this.searchDate.endDate  = GetDateStr(-1);
			this.searchInfo();
		},
		
		week : function(){
			this.searchDate.beginDate  = getWeekStartDate();
			this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
			this.searchInfo();
		},
		month : function(){
			this.searchDate.beginDate  = getMonthStartDate();
			this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
			this.searchInfo();
		},
		
	},
	
	created : function() {
		var that = this;
		var date = new Date().format("yyyy-MM-dd");
		this.searchDate.beginDate = date;
		this.searchDate.endDate = date;
		this.searchInfo();
	}
	
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

