<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div>
    	<div id="report-editor">
	    	<div class="panel panel-success">
			  <div class="panel-heading text-center">
			  	<strong style="margin-right:100px;font-size:22px">会员优惠券详情</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="brandReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
			  	<br/>
			  	<table id="shopReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
			  </div>
			</div>
    	</div>
    </div>
 <script src="assets/customer/date.js" type="text/javascript"></script>
<script>
var dataSource;
$.ajax( {  
    url:'member/list_all_shopId',
    async:false,
    data:{  
    	
    },  
    success:function(data) { 
    	dataSource=data;
     },  
     error : function() { 
    	 toastr.error("系统异常请重新刷新");
     }   
});

var tb1 = $("#shopReportTable").DataTable({
	data:dataSource.data,
	bSort:false,
	columns : [
		{
			title : "优惠券状态",
			data : "is_used",
			createdCell:function(td,tdData){
				if(tdData==true){
					$(td).html("已使用");
				}else{
					$(td).html("未使用");
				}
			}
		},

		{
			title : "优惠券所属",
			data : "brand_id",
			createdCell:function(td,tdData){
				if(tdData!=null || tdData!=""){
					$(td).html("店铺");
				}else if(tdData==null || tdData==""){
					$(td).html("品牌");
				}
			}
		}, {
			title : "优惠券类型",
			data : "coupon_type",
			createdCell:function(td,tdData){
				if(tdData==true){
					$(td).html("邀请注册");
				}else{
					$(td).html("新用户注册");
				}
			}
		}, {
			title : "所属门店",
			data : "shopname",
			createdCell:function (td,tdData) {
	           if(tdData==null||tdData==""){
	                $(td).html("--")
	            }
	        }
		}, {
			title : "活动名称",
			data : "coupon_name",
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }

		}, {
			title : "优惠券名称",
			data : "myname",
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
		}, {
			title : "优惠券金额",
			data : "coupon_validay",
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
		}, {
			title : "是否和余额叠加",
			data : "use_with_account",
			createdCell:function(td,tdData){
				if(tdData==true){
					$(td).html("可以");
				}else{
					$(td).html("不可以");
				}
			}
		}, {
			title : "优惠券生效时间",
			data : "begin_date",
            createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
		},
		{
			title : "优惠券有效期至",
			data : "end_date",
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
		},
		{
                title : "最低消费额",
                data : "coupon_value",
                createdCell:function (td,tdData) {
                    if(tdData==null||tdData==""){
                        $(td).html("--")
                    }
                }
            },
            {
			title : "开始时间",
			data : 'begin_time',
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
		}, {
			title : "结束时间",
			data : "end_time",
			createdCell:function (td,tdData) {
                if(tdData==null||tdData==""){
                    $(td).html("--")
                }
            }
            }
	]
	
});

</script>
