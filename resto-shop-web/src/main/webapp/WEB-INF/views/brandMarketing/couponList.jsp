<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="control">
<h2 class="text-center"><strong>优惠券报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label>发放周期：
                <input type="text" class="form-control form_datetime" :value="grantSearchDate.beginDate" v-model="grantSearchDate.beginDate" readonly="readonly">
            </label>
            &nbsp;至&nbsp;
            <label>
                <input type="text" class="form-control form_datetime" :value="grantSearchDate.endDate" v-model="grantSearchDate.endDate" readonly="readonly">
            </label>
		  </div>
          <div class="form-group" style="margin-right: 50px;">
            <label>使用周期：
                <input type="text" class="form-control form_datetime" :value="useSearchDate.beginDate" v-model="useSearchDate.beginDate" readonly="readonly">
            </label>
            &nbsp;至&nbsp;
            <label>
                <input type="text" class="form-control form_datetime" :value="useSearchDate.endDate" v-model="useSearchDate.endDate" readonly="readonly">
            </label>
          </div>
		  <div class="form-group" style="margin-right: 50px;">
		 	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             <button type="button" class="btn btn-primary" @click="searchInfo()">查询报表</button>
             <button type="button" class="btn btn-primary" @click="downloadExcel">下载报表</button>
             <br/>
          </div>
		</form>
	<div>
<br/>
<br/>
<div>
	<div class="tab-content">
		    <div role="tabpanel" class="tab-pane active">
				<div class="panel panel-success">
				  <div class="panel-heading text-center">
				  	<strong style="margin-right:100px;font-size:22px">
				  		品牌优惠券报表
				  	</strong>
				  </div>
				  <div class="panel-body">
				  	<table id="brandCouponList" class="table table-striped table-bordered table-hover" style="width: 100%">
				  		<thead> 
							<tr>
								<th>品牌名称</th>
								<th>发放总数</th>
								<th>发放总额</th>
								<th>使用总数</th>
		                        <th>使用总额</th>
		                        <th>优惠券使用占比</th>
                                <th>拉动订单总数</th>
								<th>拉动订单总额</th>
                                <th>拉动注册用户数</th>
							</tr>
						</thead>
						<tbody>
                            <template v-if="brandCouponInfo.brandName != null">
                                <tr>
                                    <td><strong>{{brandCouponInfo.brandName}}</strong></td>
                                    <td>{{brandCouponInfo.couponCount}}</td>
                                    <td>{{brandCouponInfo.couponMoney}}</td>
                                    <td>{{brandCouponInfo.useCouponCount}}</td>
                                    <td>{{brandCouponInfo.useCouponMoney}}</td>
                                    <td>{{brandCouponInfo.useCouponCountRatio}}</td>
                                    <td>{{brandCouponInfo.useCouponOrderCount}}</td>
									<td>{{brandCouponInfo.useCouponOrderMoney}}</td>
                                    <td>{{brandCouponInfo.customerCount}}</td>
                                </tr>
                            </template>
                            <template v-else>
                                <tr>
                                    <td align="center" colspan="9">
                                        暂时没有数据...
                                    </td>
                                </tr>
                            </template>
						</tbody>
				  	</table>
				  </div>
				</div>
		    </div>
		</div>
	  <div class="tab-content">
	    <div role="tabpanel" class="tab-pane active">
	    	<div class="panel panel-success">
			  <div class="panel-heading text-center">
			  	   <strong style="margin-right:100px;font-size:22px">店铺优惠券报表</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="shopCouponList" class="table table-striped table-bordered table-hover"
			  		style="width: 100%;">
			  	</table>
			  </div>
			</div>
	    </div>
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

    var vueObj = new Vue({
        el : "#control",
        data : {
            brandCouponInfo : {},
            shopCouponInfoList : [],
            grantSearchDate : {
                beginDate : "",
                endDate : "",
            },
            useSearchDate : {
                beginDate : "",
                endDate : "",
            },
            shopCouponInfoTable:{}
        },
        created : function() {
            var date = new Date().format("yyyy-MM-dd");
            this.grantSearchDate.beginDate = date;
            this.grantSearchDate.endDate = date;
            this.useSearchDate.beginDate = date;
            this.useSearchDate.endDate = date;
            this.initDataTables();
            this.searchInfo();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.shopCouponInfoTable = $("#shopCouponList").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    columns : [
                        {
                            title : "优惠券类型",
                            data : "couponType",
                            orderable : false
                        },
                        {
                            title : "优惠券所属",
                            data : "couponSoure",
                            orderable : false
                        },
                        {
                            title : "所属店铺",
                            data : "couponShopName",
                            orderable : false
                        },
                        {
                            title : "优惠券名称",
                            data : "couponName",
                            orderable : false
                        },
                        {
                            title : "发放总数",
                            data : "couponCount"
                        },
                        {
                            title : "发放总额",
                            data : "couponMoney"
                        },
                        {
                            title : "使用总数",
                            data : "useCouponCount"
                        },
                        {
                            title : "使用总额",
                            data : "useCouponMoney"
                        },
                        {
                            title : "优惠券使用占比",
                            data : "useCouponCountRatio"
                        },
                        {
                            title : "拉动订单总数",
                            data : "useCouponOrderCount"
                        },
                        {
                            title : "拉动订单总额",
                            data : "useCouponOrderMoney"
                        },
                        {
                            title : "拉动注册用户数" ,
                            data : "customerCount"
                        }
                    ]
                });
            },
            searchInfo : function() {
                var timeCha1 = new Date(this.grantSearchDate.endDate).getTime() - new Date(this.grantSearchDate.beginDate).getTime();
                var timeCha2 = new Date(this.useSearchDate.endDate).getTime() - new Date(this.useSearchDate.beginDate).getTime();
                if(timeCha1 < 0){
                    toastr.clear();
                    toastr.error("发放周期开始时间应该少于结束时间！");
                    return false;
                }else if(timeCha2 < 0){
                    toastr.clear();
                    toastr.error("使用周期开始时间应该少于结束时间！");
                    return false
                }else if(timeCha1 > 2678400000){
                    toastr.clear();
                    toastr.error("发放周期暂时未开放大于一月以内的查询！");
                    return false;
                }else if(timeCha2 > 2678400000){
                    toastr.clear();
                    toastr.error("使用周期暂时未开放大于一月以内的查询！");
                    return false;
                }
                var nowDate = new Date().format("HH");
                nowDate = parseInt(nowDate);
                if (nowDate >= 11 && nowDate <= 13){
                    toastr.clear();
                    toastr.error("亲，报表查询功能正在维护中，请您多多谅解~维护时间段： 11:00-13:00 17:00-19:00");
                    return false;
                }else if (nowDate >= 17 && nowDate <= 20){
                    toastr.clear();
                    toastr.error("亲，报表查询功能正在维护中，请您多多谅解~维护时间段： 11:00-13:00 17:00-19:00");
                    return false;
                }
                toastr.clear();
                toastr.success("查询中...");
                var that = this;
                try{
                    $.post("brandMarketing/selectCouponList",that.getDate(),function(result){
                        if (result.success){
                            //清空表格
                            that.shopCouponInfoTable.clear().draw();
                            //重绘表格
                            that.shopCouponInfoTable.rows.add(result.data.shopCouponInfoList).draw();
                            that.shopCouponInfoList = result.data.shopCouponInfoList;
                            that.brandCouponInfo = result.data.brandCouponInfo;
                            toastr.clear();
                            toastr.success("查询成功");
                        }else {
                            toastr.clear();
                            toastr.error("查询报表出错");
                        }
                    });
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            downloadExcel : function(){
                $.post("brandMarketing/createCouponExcel",{"grantBeginDate":this.grantSearchDate.beginDate,
                    "grantEndDate" : this.grantSearchDate.endDate,
                    "useBeginDate" : this.useSearchDate.beginDate,
                    "useEndDate" : this.useSearchDate.endDate,
                    "brandCouponInfo":this.brandCouponInfo,"shopCouponInfoList" : this.shopCouponInfoList},function(result){
                    if (result.success){
                        window.location.href = "brandMarketing/downloadCouponDto?path="+result.data+"";
                    }else {
                        toastr.clear();
                        toastr.error("下载红包报表失败！");
                    }
                });
            },
            getDate : function(){
                var data = {
                    grantBeginDate : this.grantSearchDate.beginDate,
                    grantEndDate : this.grantSearchDate.endDate,
                    useBeginDate : this.useSearchDate.beginDate,
                    useEndDate : this.useSearchDate.endDate
                };
                return data;
            },
            today : function(){
                var date = new Date().format("yyyy-MM-dd");
                this.grantSearchDate.beginDate = date
                this.grantSearchDate.endDate = date
                this.useSearchDate.beginDate = date
                this.useSearchDate.endDate = date
                this.searchInfo();
            },
            yesterDay : function(){
                this.grantSearchDate.beginDate = GetDateStr(-1);
                this.grantSearchDate.endDate  = GetDateStr(-1);
                this.useSearchDate.beginDate = GetDateStr(-1);
                this.useSearchDate.endDate  = GetDateStr(-1);
                this.searchInfo();
            },
            week : function(){
                this.grantSearchDate.beginDate  = getWeekStartDate();
                this.grantSearchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.useSearchDate.beginDate  = getWeekStartDate();
                this.useSearchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            month : function(){
                this.useSearchDate.beginDate  = getMonthStartDate();
                this.useSearchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.grantSearchDate.beginDate  = getMonthStartDate();
                this.grantSearchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            }
        }
    });

    function Trim(str)
    {
        return str.replace(/(^\s*)|(\s*$)/g, "");
    }
</script>