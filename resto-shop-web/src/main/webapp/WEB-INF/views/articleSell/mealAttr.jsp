<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div>
    	<div class="panel panel-info">
	<div class="panel-heading text-center" style="font-size: 22px;">
		<strong>套餐属性列表</strong>
	</div>
	<div class="panel-body">
		<table class="table table-striped table-bordered table-hover"
			id="mealAttrList">
		</table>
	</div>
</div>
    </div>
 <script src="assets/customer/date.js" type="text/javascript"></script>
<script>
	var dataSource;
	var customerId = "asdasd4343";
	$.ajax( {  
	    url:'member/list_all_shopId',
	    async:false,
	    data:{  
	    	'customerId':customerId
	    },  
	    success:function(result) { 
	    	if(result.success == true){
	    		dataSource=[];
	    	}else{
	    		dataSource=[];
	    	}
	     },  
	     error : function() { 
	    	 toastr.error("系统异常请重新刷新");
	     }   
	});
	
	var tb = $("#mealAttrList").DataTable({
		data:dataSource,
		bSort:false,
		columns : [
			{
				title : "属性类型",
				data : "isUsed"
			},
			{
				title : "属性名称",
				data : "brandId"
			},{
				title : "销量",
				data : "couponType"
			}
		]
	});
</script>
