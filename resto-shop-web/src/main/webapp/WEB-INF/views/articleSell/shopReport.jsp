<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
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
		  
		   <button type="button" class="btn btn-primary" id="today"> 今日</button>
                 
             <button type="button" class="btn btn-primary" id="yesterDay">昨日</button>
          
<!--              <button type="button" class="btn yellow" @click="benxun">本询</button> -->
             
             <button type="button" class="btn btn-primary" id="week">本周</button>
             <button type="button" class="btn btn-primary" id="month">本月</button>
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button> &nbsp;
		  <button type="button" class="btn btn-primary" id="ExcelReport">下载报表</button>
		</form>
	</div>
</div>
<br/>
<br/>
<table id="shopTable" class="table table-striped table-bordered table-hover"></table>
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

//文本框默认值  --同步为首页选择的时间
$("#beginDate").val("${beginDate}");
$("#endDate").val("${endDate}");

var tbApi = null;
var isFirst = true;
var shopTable = $("#shopTable").DataTable({
	ajax : {
		url : "articleSell/shop_data",
		dataSrc : "data",
		data:function(d){
			d.beginDate = $("#beginDate").val();
			d.endDate = $("#endDate").val();
			d.shopId = "${shopId}";
			return d;
		}
	},
	"lengthMenu": [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
	//order: [[ 2, "desc" ]],//默认以店铺销量降序
	columnDefs:[{
                 orderable:false,//禁用排序
                 targets:[0,1]   //指定的列
             }],
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
            title : "菜品类别",
            data : "typeName",
        },
        {
            title: "编号",
            data : "numberCode",
			defaultContent:""
        },
		{
			title : "销量(份)",
			data : "shopSellNum",
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
} );

//搜索
$("#searchReport").click(function(){
	
	searchInfo();
	
})

function searchInfo(){
	
	shopTable.ajax.reload();
	toastr.success("查询成功")
}


function isEmpty(str){
	return str == null || str == "" ? true:false;
}

//添加分类下拉框
var select;
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

$("#ExcelReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	var shopId = "${shopId}"
	location.href="articleSell/shop_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort+"&&shopId="+shopId;
})

//今日
$("#today").click(function(){
	date = new Date().format("yyyy-MM-dd");
	//给时间插件赋值
	$("#beginDate").val(date);
	$("#endDate").val(date);
	
	//查询
	searchInfo()
	
})

//昨日
$("#yesterDay").click(function(){
	//给时间插件赋值
	$("#beginDate").val(GetDateStr(-1));
	$("#endDate").val(GetDateStr(-1));
	
	//查询
	searchInfo()
	
})

//本周
$("#week").click(function(){
	//给时间插件赋值
	$("#beginDate").val(getWeekStartDate());
	$("#endDate").val(new Date().format("yyyy-MM-dd"));
	
	//查询
	searchInfo()
	
})

//本月
$("#month").click(function(){
	//给时间插件赋值
	$("#beginDate").val(getMonthStartDate);
	$("#endDate").val(new Date().format("yyyy-MM-dd"));
	
	//查询
	searchInfo()
	
})


</script>
