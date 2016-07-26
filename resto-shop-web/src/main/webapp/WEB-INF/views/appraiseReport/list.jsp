<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
<h2 class="text-center"><strong>评论报表</strong></h2><br/>
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
		  	
		   	 <button type="button" class="btn red" @click="today"> 今日</button>
                 
             <button type="button" class="btn green" @click="yesterDay">昨日</button>
          
             <button type="button" class="btn yellow" @click="benxun">本询</button>
             
             <button type="button" class="btn purple" @click="week">本周</button>
             <button type="button" class="btn purple" @click="month">本月</button>
             
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
		  	<strong style="margin-right:100px;font-size:22px">品牌评论列表</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="brandOrderTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>品牌</th>
						<th>评价单数(份)</th>
						<th>评价率</th>
						<th>评论红包总额(元)</th>
						<th>订单总额(元)</th>
						<th>评论红包撬动率</th>
						<th>五星评价数</th>
						<th>四星评价数</th>
						<th>三星评价数</th>
						<th>二星评价数</th>
						<th>一星评价数</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><strong>{{brandAppraise.brandName}}</strong></td>
						<td>{{brandAppraise.appraiseNum}}</td>	
						<td>{{brandAppraise.appraiseRatio}}</td>
						<td>{{brandAppraise.redMoney}}</td>
						<td>{{brandAppraise.totalMoney}}</td>
						<td>1</td>
						<td>{{brandAppraise.fivestar}}</td>
						<td>{{brandAppraise.fourstar}}</td>
						<td>{{brandAppraise.threestar}}</td>
						<td>{{brandAppraise.twostar}}</td>
						<td>{{brandAppraise.onestar}}</td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
		</div>
		
	<!-- 店铺订单列表 -->
    <div role="tabpanel" class="tab-pane" id="orderReport">
    	<div class="panel panel-primary" style="border-color:write;">
		  	<!-- 品牌订单 -->
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">店铺评论列表</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="brandOrderTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>店铺</th>
						<th>评价单数(份)</th>
						<th>评价率</th>
						<th>评论红包总额(元)</th>
						<th>订单总额(元)</th>
						<th>评论红包撬动率</th>
						<th>五星评价数</th>
						<th>四星评价数</th>
						<th>三星评价数</th>
						<th>二星评价数</th>
						<th>一星评价数</th>
						<th>查看详情</th>
					</tr>
				</thead>
				<tbody>
				
					<tr v-for="shopAppraise in shopAppraises">
						<td><strong>{{shopAppraise.shopName}}</strong></td>
						<td>{{shopAppraise.appraiseNum}}</td>	
						<td>{{shopAppraise.appraiseRatio}}</td>
						<td>{{shopAppraise.redMoney}}</td>
						<td>{{shopAppraise.totalMoney}}</td>
						<td>1</td>
						<td>{{shopAppraise.fivestar}}</td>
						<td>{{shopAppraise.fourstar}}</td>
						<td>{{shopAppraise.threestar}}</td>
						<td>{{shopAppraise.twostar}}</td>
						<td>{{shopAppraise.onestar}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shopAppraise.shopName , shopAppraise.shopId)">查看详情</button></td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
		</div>	
		
	  <div class="modal fade" id="reportModal" tabindex="-1" role="dialog" aria-hidden="true">
           <div class="modal-dialog modal-full">
               <div class="modal-content">
                   <div class="modal-header">
                       <button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>
                   </div>
                   <div class="modal-body"> </div>
                   <div class="modal-footer">
<!--                        <button type="button" class="btn btn-info btn-block"  @click="closeModal">关闭</button> -->
                        <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button>
                   </div>
               </div>
               <!-- /.modal-content -->
           </div>
           <!-- /.modal-dialog -->
       </div>
       
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
		brandAppraise:{},
		shopAppraises:[],
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
			$.post("appraiseReport/brand_data", this.getDate(null), function(result) {
					that.brandAppraise = result.data.brandAppraise;
					that.shopAppraises = result.data.shopAppraise;
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
			debugger;
			$("#reportModal").modal('show');
			this.openModal("appraiseReport/shopReport",shopName,shopId);
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


</script>

