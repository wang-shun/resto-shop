<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
<h2 class="text-center"><strong>会员信息列表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline" action="member/userList" id="formInfo" @submit.prevent="methods">
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
          
<!--              <button type="button" class="btn btn-primary" @click="benxun">本询</button> -->
             
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
		  	 <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>
			 <div class="text-danger" id="searchStart" style="float: right"></div>
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
		  	<strong style="margin-right:100px;font-size:22px">会员信息管理</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="shopOrderTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>用户ID</th>
						<th>昵称</th>
                        <th>头像</th>
						<th>性别</th>
						<th>手机号</th>
						<th>生日</th>
						<th>星座</th>
						<th>省/市</th>
						<th>城市/区</th>
						<th>账户余额</th>
						<th>优惠券</th>
						<th>订单总额</th>
						<th>订单总数</th>
						<th>订单平均金额</th>
						<th>订单记录</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopOrderList">
						<td><strong>{{shop.customerId}}</strong></td>
						<td>{{shop.nickname}}</td>
                        <td>
                        <img :src="shop.head_photo" :alt="shop.nickname" onerror="this.src='assets/pages/img/defaultImg.png'" width="60px" height="60px" class="img-rounded">
                        </td>
                        <td>{{shop.sex}}</td>
						<td>{{shop.telephone}}</td>
						<td>--</td>
						<td>--</td>
						<td>{{shop.province}}</td>
						<td>{{shop.city}}</td>
						<td>{{shop.remain}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.shopName,shop.shopDetailId)">查看详情</button></td>
						<td>{{shop.sumMoney}}</td>
						<td>{{shop.amount}}</td>
						<td>{{shop.money}}</td>
								<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.shopName,shop.shopDetailId)">查看详情</button></td>
					</tr>
					<tr v-if="shopOrderList.length <= 0 ">
						<th colspan="15" class="text-center">暂无数据！</th>
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

//文本框默认值
var beginDate = $("#beginDate").val();
var endDate = $("#endDate").val();
//创建vue对象
var vueObj =  new Vue({
	el:"#control",
	data:{
		appraiseCount:{},
		shopOrderList : [],
		brandOrder:{},
		searchDate : {
			beginDate : "beginDate",
			endDate : "endDate",
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
		/* 	$.post("member/userList", this.getDate(null), function(data) {
					/* that.shopOrderList = result.shopId;
					that.brandOrder = result.brandId;
					that.shopOrderList = data;
					toastr.success("查询成功");
					console.log(data);
					console.log(that.shopOrderList);
					console.log(data.customerId);
					
					
			}); */
			$("#searchStart").html("正在查询...");
			this.basePost("member/userList", $("#formInfo").serialize(), function(data) {
				/* if (!data.customerId) {
					that.errorMsg("该手机号码没有注册！");
				} */
				$("#searchStart").html("正在查询...");
				setTimeout(function(){
					$("#searchStart").html();
				},2000);
				that.shopOrderList = data;
				console.log(that.shopOrderList);
			});

		},
		getDate : function(shopId){
			var data = {
				beginDate : this.searchDate.beginDate,
				endDate : this.searchDate.endDate,
			};
			return data;
		},
		showShopReport : function(shopName,shopId) {
			$("#reportModal").modal('show');
			this.openModal("orderReport/show/shopReport", shopName,shopId);
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
		basePost : function(url, params, callback) {
			var that = this;
			$.post(url, params, function(result) {
				if (result.success) {
					callback(result.data);
				} else {
					that.errorMsg(result.message);
				}
			})
		},
	},
	
	created : function() {
		var that = this;
		var date = new Date().format("yyyy-MM-dd");
		this.searchDate.beginDate = date;
		this.searchDate.endDate = date;
		
// 		getAppraiseCount(function(appraiseCount,countList){
// 			that.appraiseCount = appraiseCount;
// 			that.countList = countList;
// 		});
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
