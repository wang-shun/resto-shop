<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<h2 class="text-center"><strong>订单列表</strong></h2><br/>
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
		  <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>
		</form>
	</div>
</div>
<br/>
<br/>

<table id="shopTable" class="table table-striped table-bordered table-hover">
<!-- 定义tfoot 否则回调函数没用 -->
<tfoot>
	<tr class="success">
		<th>总计</th><td colspan="3"></td>
	</tr>
</tfoot>

</table>

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

$('.form_datetime').val(new Date().format("yyyy-MM-dd"));

var tb1 = $("#shopTable").DataTable({
	ajax : {
		url : "orderReport/brand_data",
		dataSrc : "",
		data:function(d){
			d.beginDate = $("#beginDate").val();
			d.endDate = $("#endDate").val();
			return d;
		}
	},
	columns : [
		{
			title : "店铺",
			data : "shopName",
		},  
		{
			title : "已消费订单数（份）",
			data : "number",
		},  
		{
			title : "已消费订单金额（元）",
			data : "orderMoney",
		},
		{                 
			title : "订单平均金额（元）",
			data : "average",
			
		},                 
		{                 
			title : "操作",
			data : "shopDetailId",
			createdCell:function(td,tdData){
				$(td).html("<button class='btn btn-primary'>查看详情<>");
			}
		},
	],
	footerCallback:function(){
		var  api = this.api();

        // Update footer
        $( api.column( 4 ).footer() ).html(
            '$'+pageTotal +' ( $'+ total +' total)'
        );
		
	}
	
} );

//查询报表
$("#searchReport").click(function(){
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	//判断 时间范围是否合法
	if(beginDate>endDate){
		toastr.error("开始时间不能大于结束时间");
		return ;
	}
	var data = {"beginDate":beginDate,"endDate":endDate};
	//更新数据
	tb1.ajax.reload();
	toastr.success("查询成功");
	
})


</script>

