<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div>
    <div class="panel panel-info">
        <div class="panel-heading text-center" style="font-size: 22px;">
            <strong>会员优惠券列表</strong>
        </div>
        <div class="panel-body">
            <table class="table table-striped table-bordered table-hover"
                id="shopBill">
            </table>
        </div>
    </div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
	var shopList = new HashMap();
    var dataSource;
    var customerId = "${customerId}"
    $.ajax( {
        url:'member/list_all_shopId',
        async:false,
        data:{
            'customerId':customerId
        },
        success:function(data) {
            if(data != null ){
                dataSource=data.data.coupons;
                for(var i = 0;i < data.data.shopDetails.length;i++){
                    shopList.put(data.data.shopDetails[i].id,data.data.shopDetails[i].name);
                }
            }
         },
         error : function() {
             toastr.clear();
             toastr.error("系统异常，请刷新重试");
         }
    });

    var tb1 = $("#shopBill").DataTable({
        data:dataSource,
        bSort:false,
        columns : [
            {
                title : "优惠券状态",
                data : "isUsed",
                defaultContent:"",
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
                data : "brandId",
                createdCell:function(td,tdData){
                    if(tdData!=null && tdData!=""){
                        $(td).html("品牌");
                    }else{
                        $(td).html("店铺");
                    }
                }
            }, {
                title : "优惠券类型",
                data : "couponType",
                createdCell:function(td,tdData){
                    if(tdData==1){
                        $(td).html("邀请注册");
                    }else if(tdData == 0){
                        $(td).html("新用户注册");
                    }else if(tdData == 2){
                        $(td).html("生日赠送");
                    }else{
                        $(td).html("通用");
                    }
                }
            }, {
                title : "所属门店",
                data : "shopDetailId",
                createdCell:function (td,tdData) {
                   if(tdData==null||tdData==""){
                        $(td).html("--")
                    }else{
                       $(td).html(shopList.get(tdData));
                   }
                }
            },
            {
                title : "优惠券名称",
                data : "name",
                createdCell:function (td,tdData) {
                    if(tdData==null||tdData==""){
                        $(td).html("--")
                    }
                }
            }, {
                title : "优惠券金额",
                data : "value",
                createdCell:function (td,tdData) {
                    if(tdData==null||tdData==""){
                        $(td).html("--")
                    }
                }
            }, {
                title : "是否和余额叠加",
                data : "useWithAccount",
                createdCell:function(td,tdData){
                    if(tdData==true){
                        $(td).html("是");
                    }else{
                        $(td).html("否");
                    }
                }
            }, {
                title : "优惠券生效时间",
                data : "beginDate",
                createdCell:function (td,tdData) {
                    if (tdData != null) {
                        $(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"));
                    }

                }
            },
            {
                title : "优惠券有效期至",
                data : "endDate",
                createdCell:function (td,tdData) {
                    if (tdData != null) {
                        $(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"));
                    }

                }
            },
            {
                    title : "最低消费额",
                    data : "minAmount",
                    createdCell:function (td,tdData) {
                        if(tdData==null||tdData==""){
                            $(td).html("--")
                        }
                    }
                },
                {
                title : "开始时间",
                data : 'beginTime',
                createdCell:function (td,tdData) {
                    if (tdData != null) {
                        $(td).html(new Date(tdData).format("hh:mm"));
                    }

                }
            }, {
                title : "结束时间",
                data : "endTime",
                createdCell:function (td,tdData) {
                    if (tdData != null) {
                        $(td).html(new Date(tdData).format("hh:mm"));
                    }
                }
            }
        ]
    });

</script>
