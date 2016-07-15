<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 33px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 33px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 33px;">
		  	<button type="button" class="btn btn-primary" id="searchReport">查询报表</button>
		  	<button type="button" class="btn btn-primary" id="ExcelReport">下载报表</button>
		  </div>
		</form>
		<br/>
		<div>
			<button type="button" class="btn green-haze" id="sortById">按菜品序号排序</button>
			<button type="button" class="btn green-haze" id="sortByNum">按销量排序</button>
		</div>
	</div>
</div>


<!-- Nav tabs -->
  <ul class="nav nav-tabs" role="tablist" id="articleTab">
    <li role="presentation" class="active">
    	<a href="#articleDayReport" aria-controls="articleDayReport" role="tab" data-toggle="tab"><strong>分类销售记录</strong>
    </a>
    </li>
    <li role="presentation"><a href="#aritcleRevenueCount" aria-controls="aritcleRevenueCount" role="tab" data-toggle="tab"><strong>菜品销售记录</strong></a></li>
  </ul>
  <!-- Tab panes -->
  <div class="tab-content">
    <div role="tabpanel" class="tab-pane active" id="articleDayReport">
    	<!-- 收入条目 -->
    	<div class="panel panel-success">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">分类销售记录
		  	</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="aritcleFamilySellTable" class="table table-striped table-bordered table-hover" width="100%"></table>
		  </div>
		</div>
    </div>
    <div role="tabpanel" class="tab-pane" id="aritcleRevenueCount">
    	<div class="panel panel-primary" style="border-color:write;">
		  	<!-- 菜品销售记录 -->
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">菜品销售记录</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="articleSellTable" class="table table-striped table-bordered table-hover" width="100%"></table>
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

//文本框默认值  --同步为首页选择的时间
$("#beginDate").val("${beginDate}");
$("#endDate").val("${endDate}");


//判断活动的选项卡是第几个
function getNumActive(){
	var value = $("#articleTab li.active a").text();
	value  = Trim(value)//去空格
	if(value=='分类销售记录'){
		return 1;
	}else if(value=="菜品销售记录"){
		return 2;
	}
}

$('#articleTab a').click(function (e) {
	  e.preventDefault()
	  var num = getNumActive()
	  $(this).tab('show')
	  switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
			tb1.ajax.reload();
		  break;
		case 2:
			var selectValue = select[0].value;
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
			tb2.ajax.reload();
		  break;
	  }
	})

var tb1Api = null;
var isFirst = true;
var sort = "desc";
var tb1 = $("#aritcleFamilySellTable").DataTable({
	ajax : {
		url : "articleSell/brand_familyId_data",
		dataSrc : "data",
		data:function(d){
			d.beginDate = $("#beginDate").val();
			d.endDate = $("#endDate").val();
			d.sort = sort;//默认按销量排序
			return d;
		}
	},
	ordering:false,
	columns : [
		{
			title : "菜品分类",
			data : "articleFamilyName",
			createdCell:function(td,tdData,rowData){
				if(isEmpty(tdData)){
					var lab = $("<span>");
					if(isEmpty(rowData.articleFamilyId)){
						lab.html("分类不详").addClass("label label-warning");
					}else{
						lab.html("该分类已不存在").addClass("label label-warning");
					}
					$(td).html(lab);
				}
			}
		}, 
		{
			title : "菜品销量(份)",
			data : "brandSellNum",
		},
		{
			title : "菜品销售额(元)",
			data : "salles",
		},  
		{
			title : "菜品销售占比",
			data : "salesRatio",
			
		},  
		
	],
	initComplete: function () {//列筛选
		tbApi = this.api();
		appendSelect(tbApi);
		isFirst = false;
    }
}).on( 'xhr.dt', function (e) { 
	if(!isFirst){
		appendSelect(tbApi);
	}
} );



var tb2 = $("#articleSellTable").DataTable({
	ajax : {
		url : "articleSell/brand_id_data",
		dataSrc : "data",
		data:function(d){
			d.beginDate = $("#beginDate").val();
			d.endDate = $("#endDate").val();
			d.sort = sort;//默认按销量排序
			return d;
		}
	},
	ordering:false,
	columns : [
		{
			title : "菜品分类",
			data : "articleFamilyName",
			createdCell:function(td,tdData,rowData){
				if(isEmpty(tdData)){
					var lab = $("<span>");
					if(isEmpty(rowData.articleFamilyId)){
						lab.html("分类不详").addClass("label label-warning");
					}else{
						lab.html("该分类已不存在").addClass("label label-warning");
					}
					$(td).html(lab);
				}
			}
		},  
		{
			title : "菜品名称",
			data : "articleName",
			createdCell:function(td,tdData,rowData){
				if(isEmpty(rowData.articleFamilyId) && isEmpty(rowData.articleFamilyName)){
					$(td).html(tdData+"&nbsp;<span class='label label-danger'>已被删除</span>");
				}
			}
		},  
		{
			title : "菜品销量(份)",
			data : "brandSellNum",
		},
		{
			title : "菜品销售额",
			data : "salles",
		},
		{
			title : "占比",
			data : "salesRatio",
		},
	],
	initComplete: function () {//列筛选
		tbApi = this.api();
		appendSelect(tbApi);
		isFirst = false;
    }
}).on( 'xhr.dt', function (e) { 
	if(!isFirst){
		appendSelect(tbApi);
	}
} );




//搜索
$("#searchReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	sort = "desc";
	var num = getNumActive()
	 switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
			tb1.ajax.reload();
			toastr.success("查询成功");
		  break;
		case 2:
			var selectValue = select[0].value;
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
			tb2.ajax.reload();
			toastr.success("查询成功")
		  break;
	  }
	
})

function isEmpty(str){
	return str == null || str == "" ? true:false;
}

//添加分类下拉框
var select = $(select);
function appendSelect(api){
	api.columns().indexes().flatten().each(function (i) {
        if (i == 0) {
            var column = api.column(i);
            $(column.header()).html("菜品分类");
            var $span = $('<span class="addselect">▾</span>').appendTo($(column.header()))
             select = $('<select><option value="">全部</option></select>')
                    .appendTo($(column.header()))
                    .on('click', function (evt) {
                        evt.stopPropagation();
                        var val = $.fn.dataTable.util.escapeRegex(
                                $(this).val()
                        );
                        column.search(val ? '^' + val + '$' : '', true, false).draw();
                    });
            	column.data().unique().sort().each(function (d, j) {
            	if(d!=null && d!=""){
            		select.append('<option value="' + d + '">' + d + '</option>')
                    $span.append(select)
            	}
            });
        }
    });
}

//下载报表
$("#ExcelReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	var num = getNumActive()
	 switch(num){
	  case 1:
		  debugger;
		  //获取tr第一个td
		  var selectValue = tb1.table().row().data().articleFamilyName;
		  location.href="articleSell/brand_articlefamily_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&selectValue="+selectValue+"&&sort="+sort;
		  break;
		case 2:
			 var selectValue = tb1.table().row().data().articleFamilyName;
			location.href="articleSell/brand_article_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&selectValue="+selectValue+"&&sort="+sort;
		  break;
	  }
})

//按菜品序号排序
$("#sortById").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	sort = "0";
	var num = getNumActive()
	 switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
			tb1.ajax.reload();
			toastr.success("查询成功");
		  break;
		case 2:
			var selectValue = select[0].value;
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
			tb2.ajax.reload();
			toastr.success("查询成功")
		  break;
	  }

})

//按销量排序

$("#sortByNum").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	sort = "desc";
	var num = getNumActive()
	 switch(num){
	  case 1:
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
			tb1.ajax.reload();
			toastr.success("查询成功");
		  break;
		case 2:
			var selectValue = select[0].value;
			var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort}
			tb2.ajax.reload();
			toastr.success("查询成功")
		  break;
	  }
})


</script>
