<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
<h2 class="text-center"><strong>充值报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
              <div class="form-group" style="margin-right: 50px;">
                <label for="beginDate">开始时间：</label>
                <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate"   readonly="readonly">
              </div>
              <div class="form-group" style="margin-right: 50px;">
                <label for="endDate">结束时间：</label>
                <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate"   readonly="readonly">
              </div>
		   	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
		  	 <button type="button" class="btn btn-primary" @click="download">下载报表</button><br/>
		</form>

	</div>
</div>
<br/>
<br/>

    <div role="tabpanel" class="tab-pane">
    	<div class="panel panel-primary" style="border-color:write;">
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">品牌充值记录</strong>
		  </div>
		  <div class="panel-body">
		  	<table class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>品牌名称</th>
						<th>充值次数</th>
						<th>充值总额(元)</th>
                        <th>充值赠送总额(元)</th>
                        <th>微信端充值(元)</th>
                        <th>POS端充值(元)</th>
                        <th>充值消费总额(元)</th>
                        <th>充值赠送消费总额(元)</th>
                        <th>充值剩余总额(元)</th>
                        <th>充值赠送剩余总额(元)</th>
					</tr>
				</thead>
				<tbody>
                    <template v-if="brandCharges.brandName != null">
                        <tr>
                            <td>{{brandCharges.brandName}}</td>
                            <td>{{brandCharges.rechargeCount}}</td>
                            <td>{{brandCharges.rechargeNum}}</td>
                            <td>{{brandCharges.rechargeGaNum}}</td>
                            <td>{{brandCharges.rechargeWeChat}}</td>
                            <td>{{brandCharges.rechargePos}}</td>
                            <td>{{brandCharges.rechargeCsNum}}</td>
                            <td>{{brandCharges.rechargeGaCsNum}}</td>
                            <td>{{brandCharges.rechargeSpNum}}</td>
                            <td>{{brandCharges.rechargeGaSpNum}}</td>
                        </tr>
                    </template>
                    <template v-else>
                        <tr>
                            <td colspan="10" align="center">暂时没有数据...</td>
                        </tr>
                    </template>
				</tbody>
		  	</table>
		  </div>
		</div>
		  </div>
		</div>

    <div role="tabpanel" class="tab-pane">
    	<div class="panel panel-primary" style="border-color:write;">
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">店铺充值记录</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="shopChargeLogTable" class="table table-striped table-bordered table-hover" width="100%">
		  	</table>
		  </div>
		</div>
		  </div>
		</div>
    </div>
  </div>

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


    //创建vue对象
    var vueObj =  new Vue({
        el:"#control",
        data:{
            brandCharges:{},
            shopChargeLogs:[],
            shopChargeLogTable:{},
            searchDate : {
                beginDate : "",
                endDate : ""
            }
        },
        created : function() {
            var date = new Date().format("yyyy-MM-dd");
            this.searchDate.beginDate = date;
            this.searchDate.endDate = date;
            this.initDataTables();
            this.searchInfo();
        },
        methods:{
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.shopChargeLogTable = $("#shopChargeLogTable").DataTable({
                    lengthMenu: [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
                    order: [[ 1, "desc" ]],
                    columns : [
                        {
                            title:"店铺名称",
                            data:"shopName",
                            orderable : false
                        },
                        {
                            title : "充值单数",
                            data : "shopCount"
                        },
                        {
                            title :"充值总额(元)",
                            data : "shopNum"
                        },
                        {
                            title : "充值赠送总额(元)",
                            data : "shopGaNum"
                        },
                        {
                            title : "微信端充值(元)",
                            data : "shopWeChat"
                        },
                        {
                            title : "POS端充值(元)",
                            data : "shopPos"
                        },
                        {
                            title : "充值消费(元)",
                            data : "shopCsNum"
                        },
                        {
                            title : "充值赠送消费(元)",
                            data : "shopGaCsNum"
                        },
                        {
                            title : "操作",
                            data : "shopId",
                            orderable : false,
                            createdCell: function (td, tdData) {
                                var button = $("<a href='recharge/shopRechargeLog?beginDate="+that.searchDate.beginDate+"&&endDate="+that.searchDate.endDate+"&&shopId="+tdData+"' class='btn green ajaxify '>查看详情</a>");
                                $(td).html(button);
                            }
                        },
                    ]
                });
            },
            searchInfo : function() {
                var that = this;
                toastr.clear();
                toastr.success("查询中...");
                try {
                    $.post("recharge/rechargeLog", this.getDate(), function (result) {
                        if (result.success) {
                            that.brandCharges = result.data.brandInit;
                            that.shopChargeLogs = result.data.shopRrchargeLogs;
                            that.shopChargeLogTable.clear();
                            that.shopChargeLogTable.rows.add(result.data.shopRrchargeLogs).draw();
                            toastr.clear();
                            toastr.success("查询成功");
                        }else{
                            toastr.clear();
                            toastr.error("查询出错");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            download : function () {
                var that = this;
                try {
                    var object = that.getDate();
                    object.brandChargeLogs = that.brandCharges;
                    object.shopChargeLogs = that.shopChargeLogs;
                    $.post("recharge/brandOrShop_excel",object,function (result) {
                        if (result.success){
                            window.location.href = "recharge/download_brand_excel?path="+result.data+"";
                        }else{
                            toastr.clear();
                            toastr.error("下载报表出错");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            getDate : function(){
                var data = {
                    beginDate : this.searchDate.beginDate,
                    endDate : this.searchDate.endDate
                };
                return data;
            },
            today : function(){
                date = new Date().format("yyyy-MM-dd");
                this.searchDate.beginDate = date;
                this.searchDate.endDate = date;
                this.searchInfo();
            },
            yesterDay : function(){
                this.searchDate.beginDate = GetDateStr(-1);
                this.searchDate.endDate  = GetDateStr(-1);
                this.searchInfo();
            },
            week : function(){
                this.searchDate.beginDate  = getWeekStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            month : function(){
                this.searchDate.beginDate  = getMonthStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            }
        }
    })


</script>
