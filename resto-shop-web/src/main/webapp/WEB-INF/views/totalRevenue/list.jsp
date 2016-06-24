<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<!-- datatables的buttons扩展 -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.bootstrap.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.bootstrap4.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.dataTables.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.foundation.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.jqueryui.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/buttons.semanticui.min.css">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/common.scss">  -->
<!-- <link rel="stylesheet" type="text/css" href="assets/global/css/buttons/mixins.scss">  -->
 <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.12/css/jquery.dataTables.min.css">  
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.2.1/css/buttons.dataTables.min.css">  

<h2 class="text-center"><strong>结算报表</strong></h2>
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
		  <br></div>
		  <button type="button" class="btn btn-primary" id="searchReport">查询报表</button><form>
			<input type="hidden" id="brandDataTable">
			<input type="hidden" id="shopDataTable">
		</form>&nbsp;&nbsp;&nbsp;
		</form>
	</div>
</div>
<button type="button" class="btn btn-primary" id="reportExcel">导出excel</button><br/>
<br/>
<div>
  	<!-- 每日报表 -->
    	<div id="report-editor">
	    	<div class="panel panel-success">
			  <div class="panel-heading text-center">
			  	<strong style="margin-right:100px;font-size:22px">收入条目</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="brandReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
			  	<br/>
			  	<table id="shopReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
			  	<br/>
			  	<table id="example" class="table table-striped table-bordered table-hover" width="100%">
        <thead>
            <tr>
                <th>Name</th>
                <th>Position</th>
                <th>Office</th>
                <th>Age</th>
                <th>Start date</th>
                <th>Salary</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Fiona Green</td>
                <td>Chief Operating Officer (COO)</td>
                <td>San Francisco</td>
                <td>48</td>
                <td>2010/03/11</td>
                <td>$850,000</td>
            </tr>
            <tr>
                <td>Shou Itou</td>
                <td>Regional Marketing</td>
                <td>Tokyo</td>
                <td>20</td>
                <td>2011/08/14</td>
                <td>$163,000</td>
            </tr>
            <tr>
                <td>Michelle House</td>
                <td>Integration Specialist</td>
                <td>Sidney</td>
                <td>37</td>
                <td>2011/06/02</td>
                <td>$95,400</td>
            </tr>
            <tr>
                <td>Suki Burks</td>
                <td>Developer</td>
                <td>London</td>
                <td>53</td>
                <td>2009/10/22</td>
                <td>$114,500</td>
            </tr>
            <tr>
                <td>Prescott Bartlett</td>
                <td>Technical Author</td>
                <td>London</td>
                <td>27</td>
                <td>2011/05/07</td>
                <td>$145,000</td>
            </tr>
            <tr>
                <td>Gavin Cortez</td>
                <td>Team Leader</td>
                <td>San Francisco</td>
                <td>22</td>
                <td>2008/10/26</td>
                <td>$235,500</td>
            </tr>
            <tr>
                <td>Olivia Liang</td>
                <td>Support Engineer</td>
                <td>Singapore</td>
                <td>64</td>
                <td>2011/02/03</td>
                <td>$234,500</td>
            </tr>
            <tr>
                <td>Bruno Nash</td>
                <td>Software Engineer</td>
                <td>London</td>
                <td>38</td>
                <td>2011/05/03</td>
                <td>$163,500</td>
            </tr>
            <tr>
                <td>Sakura Yamamoto</td>
                <td>Support Engineer</td>
                <td>Tokyo</td>
                <td>37</td>
                <td>2009/08/19</td>
                <td>$139,575</td>
            </tr>
            <tr>
                <td>Thor Walton</td>
                <td>Developer</td>
                <td>New York</td>
                <td>61</td>
                <td>2013/08/11</td>
                <td>$98,540</td>
            </tr>
            <tr>
                <td>Finn Camacho</td>
                <td>Support Engineer</td>
                <td>San Francisco</td>
                <td>47</td>
                <td>2009/07/07</td>
                <td>$87,500</td>
            </tr>
            <tr>
                <td>Serge Baldwin</td>
                <td>Data Coordinator</td>
                <td>Singapore</td>
                <td>64</td>
                <td>2012/04/09</td>
                <td>$138,575</td>
            </tr>
            <tr>
                <td>Zenaida Frank</td>
                <td>Software Engineer</td>
                <td>New York</td>
                <td>63</td>
                <td>2010/01/04</td>
                <td>$125,250</td>
            </tr>
            <tr>
                <td>Zorita Serrano</td>
                <td>Software Engineer</td>
                <td>San Francisco</td>
                <td>56</td>
                <td>2012/06/01</td>
                <td>$115,000</td>
            </tr>
            <tr>
                <td>Jennifer Acosta</td>
                <td>Junior Javascript Developer</td>
                <td>Edinburgh</td>
                <td>43</td>
                <td>2013/02/01</td>
                <td>$75,650</td>
            </tr>
            <tr>
                <td>Cara Stevens</td>
                <td>Sales Assistant</td>
                <td>New York</td>
                <td>46</td>
                <td>2011/12/06</td>
                <td>$145,600</td>
            </tr>
            <tr>
                <td>Hermione Butler</td>
                <td>Regional Director</td>
                <td>London</td>
                <td>47</td>
                <td>2011/03/21</td>
                <td>$356,250</td>
            </tr>
            <tr>
                <td>Lael Greer</td>
                <td>Systems Administrator</td>
                <td>London</td>
                <td>21</td>
                <td>2009/02/27</td>
                <td>$103,500</td>
            </tr>
            <tr>
                <td>Jonas Alexander</td>
                <td>Developer</td>
                <td>San Francisco</td>
                <td>30</td>
                <td>2010/07/14</td>
                <td>$86,500</td>
            </tr>
            <tr>
                <td>Shad Decker</td>
                <td>Regional Director</td>
                <td>Edinburgh</td>
                <td>51</td>
                <td>2008/11/13</td>
                <td>$183,000</td>
            </tr>
            <tr>
                <td>Michael Bruce</td>
                <td>Javascript Developer</td>
                <td>Singapore</td>
                <td>29</td>
                <td>2011/06/27</td>
                <td>$183,000</td>
            </tr>
            <tr>
                <td>Donna Snider</td>
                <td>Customer Support</td>
                <td>New York</td>
                <td>27</td>
                <td>2011/01/25</td>
                <td>$112,000</td>
            </tr>
        </tbody>
    </table>
			  </div>
			</div>
    	</div>
    </div>
<%-- <script src="assets/global/scripts/buttons/buttons.bootstrap.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.bootstrap4.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.colVis.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.flash.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.foundation.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.html5.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.jqueryui.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.print.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/buttons.semanticui.min.js"></script>   --%>
<%-- <script src="assets/global/scripts/buttons/dataTables.buttons.min.js"></script>   --%>
<script src="https://code.jquery.com/jquery-1.12.3.js"></script>  
<script src="https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js"></script>  
<script src="https://cdn.datatables.net/buttons/1.2.1/js/dataTables.buttons.min.js"></script>  
<script src="https://cdn.datatables.net/buttons/1.2.1/js/buttons.flash.min.js"></script>  
<script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/2.5.0/jszip.min.js"></script>  
<script src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/pdfmake.min.js"></script>  
<script src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/vfs_fonts.js"></script>  
<script src="https://cdn.datatables.net/buttons/1.2.1/js/buttons.html5.min.js"></script>  
<script src="https://cdn.datatables.net/buttons/1.2.1/js/buttons.print.min.js"></script>  

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

var beginDate = $("#beginDate").val();
var endDate = $("#endDate").val();
var dataSource;
$.ajax( {  
    url:'totalRevenue/reportIncome',
    async:false,
    data:{  
    	'beginDate':beginDate,
    	'endDate':endDate
    },  
    success:function(data) { 
    	dataSource=data;
     },  
     error : function() { 
    	 toastr.error("系统异常请重新刷新");
     }  
});

$('#example').DataTable( {
    dom: 'Bfrtip',
    buttons: [
              'copy', 'csv', 'excel', 'pdf', 'print'
          ]
} );

var tb1 = $("#brandReportTable").DataTable({
	data:dataSource.brandIncome,
	dom:'i',
	columns : [
		{                 
			title : "品牌",
			data : "brandName",
		},       
		{                 
			title : "营收总额(元)",
			data : "totalIncome",
		},       
		{                 
			title : "红包支付(元)",
			data : "redIncome",
		},       
		{                 
			title : "优惠券支付收入(元)",
			data : "couponIncome",
		},       
		{                 
			title : "微信支付收入(元)",
			data : "wechatIncome",
		},       
		{                 
			title : "充值账户支付(元)",
			data : "chargeAccountIncome",
		},       
		{                 
			title : "充值赠送账户支付(元)",
			data : "chargeGifAccountIncome",
		},       
	]
	
});

var tb2 = $("#shopReportTable").DataTable({
	//dom:'Bfrtip',
	dom:'Bfrtip',
	 buttons: [
	           'copyHtml5',
	           'excelHtml5',
	           'csvHtml5',
	           'pdfHtml5'
	       ],
	data:dataSource.shopIncome,
	columns : [
		{                 
			title : "店铺名称",
			data : "shopName",
		},       
		{                 
			title : "营收总额(元)",
			data : "totalIncome",
		},       
		{                 
			title : "红包支付收入(元)",
			data : "redIncome",
		},       
		{                 
			title : "优惠券支付收入(元)",
			data : "couponIncome",
		},       
		{                 
			title : "微信支付收入(元)",
			data : "wechatIncome",
		},     
		{                 
			title : "充值账户支付(元)",
			data : "chargeAccountIncome",
		},     
		{                 
			title : "充值赠送账户支付(元)",
			data : "chargeGifAccountIncome",
		}     
	]
	
});

$("#searchReport").click(function(){
	 beginDate = $("#beginDate").val();
	 endDate = $("#endDate").val();
	//更新数据源
	 $.ajax( {  
		    url:'totalRevenue/reportIncome',
		    data:{  
		    	'beginDate':beginDate,
		    	'endDate':endDate
		    },  
		    success:function(result) {
		    	dataSource=result;
		    	tb1.clear().draw();
		    	tb2.clear().draw();
		    	tb1.rows.add(result.brandIncome).draw();
		    	tb2.rows.add(result.shopIncome).draw();
		     },  
		     error : function() { 
		    	 toastr.error("系统异常请重新刷新");
		     }  
		});
})

$("#reportExcel").click(function(){
	var reportData = dataSource;
	console.log(reportData)
	$.ajax({
		type:"post",
		url:'totalRevenue/reportExcel',
		data:{"reportData":reportData},
	})
	
	
})



</script>
