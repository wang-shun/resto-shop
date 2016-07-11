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
<br/>
<br/>
<table id="shopTable" class="table table-striped table-bordered table-hover"></table>

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
var sort = "desc";
var shopTable = $("#shopTable").DataTable({
	ajax : {
		url : "articleSell/brand_data",
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
	var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
	shopTable.ajax.reload();
 	toastr.success("查询成功");
	
})

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

//下载报表
$("#ExcelReport").click(function(){
	//获取select选中的值
	var selectValue = select[0].value;
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	location.href="articleSell/brand_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&selectValue="+selectValue+"&&sort="+sort;
})

//按菜品序号排序
$("#sortById").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	sort = "0";
	var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
	shopTable.ajax.reload();

})

//按销量排序

$("#sortByNum").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	sort = "desc";
	var data = {"beginDate":beginDate,"endDate":endDate,"sort":sort};
	shopTable.ajax.reload();
	toastr.success("排序成功");
})


</script>
