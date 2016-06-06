<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<!-- vue对象开始 -->
<div id="control">
<h2 class="text-center"><strong>结算报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form role="form" class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="beginDate">开始时间：</label>
		    <input type="text" class="form-control form_datetime" id="beginDate" name="beginDate" readonly="readonly">
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label for="endDate">结束时间：</label>
		    <input type="text" class="form-control form_datetime" id="endDate" name="endDate" readonly="readonly">
		  </div>
		  <button type="button" class="btn btn-primary" id="searchReport" @click="queryOrder">查询报表</button>
		</form>
	</div>
</div>
<br/>
<!-- datatable开始 -->
<div class="table-div">
	<div class="clearfix"></div>
	<div class="table-filter"></div>
	<div class="table-body">
		<table class="table table-striped table-hover table-bordered ">
		 <tfoot>
            <tr>
                <th colspan="1" style="text-align:right">总共充值金额为:</th>
                <th></th>
            </tr>
       	 </tfoot>
		</table>
	</div>
</div>
<!-- datatable结束 -->

</div>
<!-- vue对象结束 -->
<br/>


<script>
	(function(){
		//初始化时间插件
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
		//给开始和结束时间赋默认初始值
		$("#endDate").val(new Date().format("yyyy-MM-dd"));
		$("#beginDate").val(GetDateStr(-7));
		
		//定义总的充值金额
		var sum = 0;
		var cid="#control";
		//加载datatable
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "wechatCharge/list_all",
				dataSrc : "",
				data:function(d){
					d.beginDate= $("#beginDate").val();
					d.endDate=$("#endDate").val();
					return d;
				},
			},
			columns : [
				{                 
					title : "充值时间",
					data : "createTime",
					createdCell:function(td,tdData){
						$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"));
						
					}
				},                 
				{                 
					title : "充值金额(元)",
					data : "paymentMoney",
					createdCell:function(td,tdData,row,rowData){
						sum+=tdData;
						console.log(sum);
					},
				},                 
				{                 
					title : "返还的金额(元)",
					data : "rewardMoney",
				},
				{
					title:"充值的手机",
					data:"telephone",
					defaultContent:'无'
				},
				{
					title:"充值的品牌",
					data:"brandName",
				},
				{
					title:"充值的店铺",
					data:"shopDetailName",
				},
				
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
// 						var operator=[
// 							<s:hasPermission name="kitchen/delete">
// 							C.createDelBtn(tdData,"kitchen/delete"),
// 							</s:hasPermission>
// 							<s:hasPermission name="kitchen/edit">
// 							C.createEditBtn(rowData),
// 							</s:hasPermission>
			//			];
					//	$(td).html(operator);
					}
				}],
				
				
				"footerCallback": function( tfoot, data, start, end, display ) {
				    $(tfoot).find('th').eq(1).html( " "+sum+"元" );
				  }
		});
	
		
 		var C = new Controller(cid,tb);
		var vueObj = new Vue({
			el:"#control",
			data:{
			},
			//保留原vue对象中的内容和方法
			mixins:[C.formVueMix],
			methods:{
	 			queryOrder :function(){
	 				var beginDate = $("#beginDate").val();
	 				var endDate = $("#endDate").val();
	 				//判断 时间范围是否合法
	 				if(beginDate>endDate){
	 					toastr.error("开始时间不能大于结束时间");
	 					return ;
	 				}
	 				tb.ajax.reload();
	 			}
			},
		})
		
		function GetDateStr(AddDayCount){
			var dd = new Date();
			dd.setDate(dd.getDate()+AddDayCount);
			var y = dd.getFullYear(); 
			var m = dd.getMonth()+1;//获取当前月份的日期 
			var d = dd.getDate(); 
			return y+"-"+m+"-"+d; 
		}
		
	}());
	
	
</script>


