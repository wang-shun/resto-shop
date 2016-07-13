<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<h2 class="text-center"><strong>结算报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
		  </div>
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;
		  <button type="button" class="btn btn-primary" id="shopReport">下载报表</button>
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
		  	<table id="brandArticleTable" class="table table-striped table-bordered table-hover" width="100%"></table>
		  </div>
		</div>
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
		  	<table id="shopArticleTable" class="table table-striped table-bordered table-hover" width="100%"></table>
		  </div>
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

//文本框默认值
$('.form_datetime').val(new Date().format("yyyy-MM-dd"));

function isEmpty(str){
	return str == null || str == "" ? true:false;
}

var brandTable = $("#brandArticleTable").DataTable({
	language:language,
	dom:'',
	ajax : {
		url : "articleSell/list_brand",   
		dataSrc : "",
		data:function(d){
			d.beginDate=$("#beginDate").val();
			d.endDate=$("#endDate").val();
			return d;
		}
	},
	ordering:false,
	columns : [
		{ 
			title : "品牌", 
			data : "brandName"
		},                 
		{ 
			title : "菜品总销量(份)", 
			data : "totalNum" 
		},
		{ 
			title : "销售详情", 
			data : "brandName" ,
			createdCell:function(td,tdData){
				$(td).html("<button class=\"btn btn-success\" onclick=\"showBrandReport(td)\">查看详情</button>");
														
			}
		}
	]
});

var shopTable = $("#shopArticleTable").DataTable({
	language:language,
	dom:'',
	ajax : {
		url : "articleSell/list_shop",   
		dataSrc : "",
		data:function(d){
			d.beginDate=$("#beginDate").val();
			d.endDate=$("#endDate").val();
			return d;
		}
	},
	ordering:false,
	columns : [
		{ 
			title : "id", 
			data : "shopId"
		},              
		{ 
			title : "店铺", 
			data : "shopName"
		},                 
		{ 
			title : "菜品总销量(份)", 
			data : "totalNum" 
		},
		{ 
			title : "菜品销售额(元)", 
			data : "sellIncome" 
		},
		{ 
			title : "品牌销售占比", 
			data : "proportion" 
		},
		{ 
			title : "销售详情", 
			data : "shopName" ,
			createdCell:function(td,tdData){
				$(td).html("<button class=\"btn btn-success\" onclick=\"showShopReport(td)\">查看详情</button>");
														
			}
		}
	]
});



//搜索
$("#searchReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	//判断 时间范围是否合法
	if(beginDate>endDate){
		toastr.error("开始时间不能大于结束时间")
		return ;
	}
	
	var num = getNumActive();
	switch (num)
	{
	case 1:
		var data = {"beginDate":beginDate,"endDate":endDate};
		tb1.ajax.reload();
		toastr.success("查询成功");
	  break;
	case 2:
		var selectValue = select[0].value;
		var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
		tb2.ajax.reload();
		toastr.success("查询成功");
	  break;
	}
	
})

//判断活动的选项卡是第几个
function getNumActive(){
	var value = $("#ulTab li.active a").text();
	value  = Trim(value)//去空格
	if(value=='营业收入报表'){
		return 1;
	}else if(value=="菜品销售报表"){
		return 2;
	}
}

$('#ulTab a').click(function (e) {
	  e.preventDefault()
	  $(this).tab('show')
	  var num = getNumActive()
	  switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate};
			tb1.ajax.reload();
		  break;
		case 2:
			var selectValue = select[0].value;
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
			tb2.ajax.reload();
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


function showBrandReport(brandName){
	
	alert("显示品牌菜品销售详情");
}
</script>

