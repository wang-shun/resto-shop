<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<link rel="stylesheet" type="text/css" href="assets/global/plugins/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css">

<h2 class="text-center"><strong>短信记录</strong></h2><br/>
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
		  <button type="button" class="btn btn-primary" id="searchReport">查询短信记录</button>
		</form>
	</div>
</div>
<br/>
<p class="text-danger text-center" hidden="true"><strong>开始时间不能大于结束时间！</strong></p>
<br/>


<div id="smslog">
	<input type="checkbox" id="jack" value="蜜成" v-model="checkedNames">
	<label for="jack">Jack</label>
	<input type="checkbox" id="jack" value="蜜成平阳店" v-model="checkedNames">
	<label for="jack">Jack</label>
	<input type="checkbox" id="john" value="蜜成虹桥路店" v-model="checkedNames">
	<label for="john">John</label>
	<br>
	<span>Checked names: {{ checkedNames | json }}</span>

</div>



<!-- <!-- 日期框 --> 
<script src="assets/global/plugins/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="assets/global/plugins/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>


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

 var vue = new Vue({
	 el:'smslog',
	 data:{
		 checkedNames:[]
	 }
	 
 })
	

</script>
