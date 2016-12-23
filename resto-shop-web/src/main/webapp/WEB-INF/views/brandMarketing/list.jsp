<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<div id="control">
	<h2 class="text-center"><strong>营销报表</strong></h2><br/><br/>
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate" readonly="readonly">
		  </div>
		  
		 	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
                 
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
          
<!--              <button type="button" class="btn yellow" @click="benxun">本询</button> -->
             
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
		  	 <button type="button" class="btn btn-primary" @click="brandreportExcel">下载报表</button><br/>
		  
		</form>
		<br/>
	</div>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	<div>
		  <ul class="nav nav-tabs"></ul>
		  <div class="tab-content">
		    <div role="tabpanel" class="tab-pane active">
				
				<div class="panel panel-success">
				  <div class="panel-heading text-center">
				  	<strong style="margin-right:100px;font-size:22px">品牌营销表
				  	</strong>
				  </div>
				  <div class="panel-body">
				  	<table id="brandMarketing" class="table table-striped table-bordered table-hover" width="100%"></table>
				  </div>
				</div>
				
		    </div>
		</div>
	</div>

<script src="assets/customer/date.js" type="text/javascript"></script>
<script type="text/javascript">
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
	var date = new Date().format("yyyy-MM-dd");
	$("#beginDate").val(date);
	$("#endDate").val(date);
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	
	var table = $("#brandMarketing").DataTable({
		ajax : {
			url : "brandMarketing/selectAll",
			dataSrc : "data"
		},
		ordering:false,
		paging:false,
		info:false,
		searching:false,
		columns : [
			{
				title : "品牌",
				data : "brandName",
			},  
			{
				title : "红包总额(元)",
				data : "redMoneyAll",
				defaultContent:"0"
			},
	        {
	            title : "评论红包(元)",
	            data : "plRedMoney",
	            defaultContent:"0"
	        },
	        {
	          title : "充值赠送红包(元)",
	           data : "czRedMoney",
	           defaultContent:"0"
	        },
			{
				title : "分享返利红包(元)",
				data : "fxRedMoney",
				defaultContent:"0"
			},
			{
				title : "等位红包(元)",
				data : "dwRedMoney",
				defaultContent:"0"
			},
			{
				title : "退菜红包(元)",
				data : "tcRedMoney",
				defaultContent:"0"
			},
			{
				title : "优惠券总额(元)",
				data : "couponAllMoney",
				defaultContent:"0"
			},
			{
				title : "注册优惠券(元)",
				data : "zcCouponMoney",
				defaultContent:"0"
			},
			{
				title : "邀请优惠券(元)",
				data : "yqCouponMoney",
				defaultContent:"0"
			}
		]
	});
</script>