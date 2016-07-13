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

var brandTable = $("#brandArticleTable").DataTable({
	language:language,
	dom:'i',
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
				console.log(tdData);
				$(td).html("<button class='btn btn-succes' onclick='showBrandReport("+tdData+")'>查看详情</button>");
			}
		}
	]
});

var shopTable = $("#shopArticleTable").DataTable({
	language:language,
	dom:'i',
	ajax : {
		url : "articleSell/list_shop",   
		dataSrc : "",
		data:function(d){
			d.beginDate=$("#beginDate").val();
			d.endDate=$("#endDate").val();
			return d;
		}
	},
	
	"columnDefs":[{
		 "targets":[0],
		   "visible":false
		},
	  
	 ],
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
			data : "occupy" ,
			
		},
		{ 
			title : "销售详情", 
			data : "shopName" ,
			createdCell:function(td,tdData){
				$(td).html("<button class='btn btn-success' onclick=\"showShopReport(td)\">查看详情</button>");
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
	console.log(num);
	switch (num)
	{
	case 1:
		var data = {"beginDate":beginDate,"endDate":endDate};
		brandTable.ajax.reload();
		toastr.success("查询成功");
	  break;
	case 2:
		var data = {"beginDate":beginDate,"endDate":endDate}
		shopTable.ajax.reload();
		toastr.success("查询成功");
	  break;
	}
	
})

//判断活动的选项卡是第几个
function getNumActive(){
	var value = $("#ulTab li.active a").text();
	value  = Trim(value)//去空格
	if(value=='品牌菜品报表'){
		return 1;
	}else if(value=="店铺菜品销售报表"){
		return 2;
	}
}

$('#ulTab a').click(function (e) {
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	  e.preventDefault()
	  $(this).tab('show')
	  var num = getNumActive()
	  switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate};
			brandTable.ajax.reload();
		  break;
		case 2:
			var data = {"beginDate":beginDate,"endDate":endDate}
			shopTable.ajax.reload();
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

function showBrandReport(brandName){
	console.log(brandName)
	alert(brandName)
	//this.openModal("articleSell/show/brandReport", brandName,null);
}

function openModal(url,modalTitle,shopId){
	$.post(url, this.getDate(shopId),function(result) {
		console.log(result)
		var modal = $("#reportModal");
		modal.find(".modal-body").html(result);
		modal.find(".modal-title > strong").html(modalTitle);
		modal.modal();
	})
}

</script>

