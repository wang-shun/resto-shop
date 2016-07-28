<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="control">
<h2 class="text-center"><strong>菜品销售报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" v-model="searchDate.beginDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" v-model="searchDate.endDate" readonly="readonly">
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
	<div>
		
</div>
<br/>
<br/>
<div>
  <!-- Nav tabs -->
  <ul class="nav nav-tabs" role="tablist" id="ulTab">
    <li role="presentation" class="active">
    	<a href="#dayReport" aria-controls="dayReport" role="tab" data-toggle="tab"><strong>品牌菜品报表</strong>
    </a>
    </li>
    <li role="presentation"><a href="#revenueCount" aria-controls="revenueCount" role="tab" data-toggle="tab"><strong>店铺菜品销售报表</strong></a></li>
  </ul>
  <!-- Tab panes -->
  <div class="tab-content">
  	<!-- 菜品销售报表 -->
    <div role="tabpanel" class="tab-pane active" id="dayReport">
    	<!-- 品牌菜品销售表 -->
    	<div class="panel panel-success">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">品牌菜品销售表
		  	</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="brandArticleTable" class="table table-striped table-bordered table-hover" width="100%">
		  		<thead> 
					<tr>
						<th>品牌名称</th>
						<th>菜品总销量(份)</th>
						<th>菜品销售总额</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><strong>{{brandReport.brandName}}</strong></td>
						<td>{{brandReport.totalNum}}</td>
						<td>{{brandReport.totalNum}}</td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		
		
		<div class="panel panel-success">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">品牌菜品销售表详情
		  	</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="articleSellTable" class="table table-striped table-bordered table-hover" width="100%"></table>
		  	</table>
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
                   <br/>
                   <div class="modal-footer">
<!--                        <button type="button" class="btn btn-info btn-block"  @click="closeModal">关闭</button> -->
                        <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button>
                   </div>
               </div>
               <!-- /.modal-content -->
           </div>
           <!-- /.modal-dialog -->
       </div>
    <!-- 店铺菜品销售表 -->
    <div role="tabpanel" class="tab-pane" id="revenueCount">
    	<div class="panel panel-primary" style="border-color:write;">
		  	<!-- 店铺菜品销售表 -->
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">店铺菜品销售记录</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="shopArticleTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>店铺名称</th>
						<th>菜品销量(份)</th>
						<th>菜品销售额</th>
						<th>品牌销售占比</th>
						<th>销售详情</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopReportList">
						<td><strong>{{shop.shopName}}</strong></td>
						<td>{{shop.totalNum}}</td>	
						<td>{{shop.sellIncome}}</td>
						<td>{{shop.occupy}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.shopName,shop.shopId)">查看详情</button></td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
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
				var that = this;
				//判断 时间范围是否合法
				if(beginDate>endDate){
					toastr.error("开始时间不能大于结束时间")
					return ;
				}
				var num = this.getNumActive();
				console.log(num);
				switch (num)
				{
				case 1:
					$.post("articleSell/list_brand", this.getDate(null), function(result) {
	 					that.brandReport.brandName = result.brandName;
	 					that.brandReport.totalNum = result.totalNum;
	 					tb2.ajax.reload();
	 					toastr.success("查询成功");
	 				});
				  break;
				case 2:
					$.post("articleSell/list_shop", this.getDate(null), function(result) {
						that.shopReportList = result;
	 					toastr.success("查询成功");
	 				});
				  break;
				}
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
			},
			getNumActive:function(){
				var value = $("#ulTab li.active a").text();
				value  = Trim(value)//去空格
				if(value=='品牌菜品报表'){
					return 1;
				}else if(value=="店铺菜品销售报表"){
					return 2;
				}
				
			},
			brandreportExcel : function(){
				var that = this;
				var beginDate = that.searchDate.beginDate;
				var endDate = that.searchDate.endDate;
				var num = this.getNumActive()
				 switch(num){
				  case 1:
					  location.href="articleSell/brand_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
					  break;
					case 2:
						location.href="articleSell/shop_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
					  break;
				  }
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
			var date = new Date().format("yyyy-MM-dd");
			this.searchDate.beginDate = date;
			this.searchDate.endDate = date;
			this.searchInfo();
		}
	})
	
var sort = "desc";	
var tb2 = $("#articleSellTable").DataTable({
	"lengthMenu": [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
	ajax : {
		url : "articleSell/brand_id_data",
		dataSrc : "data",
		data:function(d){
			d.beginDate = vueObj.searchDate.beginDate;
			d.endDate = vueObj.searchDate.endDate ;
			d.sort = sort;//默认按销量排序
			return d;
		}
	},
	ordering:false,
	columns : [
		{
			title : "分类",
			data : "articleFamilyName",
		},  
		{
			title : "菜名",
			data : "articleName",
		},  
		{
			title : "销量(份)",
			data : "brandSellNum",
		},
		{
			title : "销量占比",
			data : "numRatio",
		},
		{
			title : "销售额(元)",
			data : "salles",
		},
		{
			title : "销售额占比",
			data : "salesRatio",
		},
	],
	
})
	

$('#ulTab a').click(function (e) {
	var beginDate = vueObj.searchDate.beginDate;
	var endDate = vueObj.searchDate.endDate;
	e.preventDefault()
	 $(this).tab('show');
	  var num = vueObj.getNumActive()
	  switch(num){
	  case 1:
		  
		  $.post("articleSell/list_brand", vueObj.getDate(null), function(result) {
				vueObj.brandReport.brandName = result.brandName;
				vueObj.brandReport.totalNum = result.totalNum;
			});
		  break;
		case 2:
			$.post("articleSell/list_shop", vueObj.getDate(null), function(result) {
					vueObj.shopReportList =result;
				});
		  break;
	  }
	})

//datatables语言设置
var language = {
    "sProcessing": "处理中...",
    "sLengthMenu": "显示 _MENU_ 项结果",
    "sZeroRecords": "没有匹配结果",
    "sInfo": "显示第 _START_ 至 _END_ 项结果，共 _TOTAL_ 项",
    "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
    "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
    "sInfoPostFix": "",
    "sSearch": "搜索:",
    "sUrl": "",
    "sEmptyTable": "表中数据为空",
    "sLoadingRecords": "载入中...",
    "sInfoThousands": ",",
    "oPaginate": {
        "sFirst": "首页",
        "sPrevious": "上页",
        "sNext": "下页",
        "sLast": "末页"
    },
    "oAria": {
        "sSortAscending": ": 以升序排列此列",
        "sSortDescending": ": 以降序排列此列"
    }
};
function Trim(str)
{ 
    return str.replace(/(^\s*)|(\s*$)/g, ""); 
}

</script>

