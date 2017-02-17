<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="control">
<h2 class="text-center"><strong>红包报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
          <div>
            <label>
                <span>红包类型：</span>
                <select id="redType" class="form-control" style="width: 173px;"
                        :value="redType" v-model="redType">
                    <option v-for="type in redTypeList" value="{{type.typeValue}}">{{type.typeName}}</option>
                </select>
            </label>
          </div>
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
				  		品牌红包报表
				  	</strong>
				  </div>
				  <div class="panel-body">
				  	<table id="brandRedList" class="table table-striped table-bordered table-hover" style="width: 100%">
				  		<thead> 
							<tr>
								<th>品牌名称</th>
								<th>发放总数</th>
								<th>发放总额</th>
								<th>使用总数</th>
		                        <th>使用总额</th>
		                        <th>红包使用数占比</th>
                                <%--<th>红包使用额占比</th>--%>
                                <th>拉动订单总数</th>
                                <th>拉动订单总额</th>
							</tr>
						</thead>
						<tbody>
                            <tr>
								<td><strong>{{brandRedInfo.brandName}}</strong></td>
								<td>{{brandRedInfo.redCount}}</td>
								<td>{{brandRedInfo.redMoney}}</td>
		                        <td>{{brandRedInfo.useRedCount}}</td>
		                        <td>{{brandRedInfo.useRedMoney}}</td>
		                        <td>{{brandRedInfo.useRedCountRatio}}</td>
                                <%--<td>{{brandRedInfo.useRedMoneyRatio}}</td>--%>
                                <td>{{brandRedInfo.useRedOrderCount}}</td>
                                <td>{{brandRedInfo.useRedOrderMoney}}</td>
							</tr>
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
			  	   <strong style="margin-right:100px;font-size:22px">店铺红包报表</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="shopRedList" class="table table-striped table-bordered table-hover"
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
            brandRedInfo : {
                brandName : "--",
                redCount : 0,
                redMoney:0,
                useRedCount:0,
                useRedMoney:0,
                useRedCountRatio:"--",
                useRedMoneyRatio:"--",
                useRedOrderCount:0,
                useRedOrderMoney:0
            },
            shopRedInfoList : [{
                shopName : "--",
                redCount : 0,
                redMoney:0,
                useRedCount:0,
                useRedMoney:0,
                useRedCountRatio:"--",
                useRedMoneyRatio:"--",
                useRedOrderCount:0,
                useRedOrderMoney:0
            }],
            grantSearchDate : {
                beginDate : "",
                endDate : "",
            },
            useSearchDate : {
                beginDate : "",
                endDate : "",
            },
            redTypeList:[
                {typeName:"全部红包",typeValue:"0"},
                {typeName:"评论红包",typeValue:"1"},
                {typeName:"分享返利红包",typeValue:"2"},
                {typeName:"退菜红包",typeValue:"3"},
                {typeName:"充值赠送红包",typeValue:"4"},
                {typeName:"等位红包",typeValue:"5"}
            ],
            redType:0,
            shopRedInfoTable:{}
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
                that.shopRedInfoTable = $("#shopRedList").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    data:that.shopRedInfoList,
                    columns : [
                        {
                            title : "店铺名称",
                            data : "shopName",
                            orderable : false
                        },
                        {
                            title : "发放总数",
                            data : "redCount"
                        },
                        {
                            title : "发放总额",
                            data : "redMoney"
                        },
                        {
                            title : "使用总数",
                            data : "useRedCount"
                        },
                        {
                            title : "使用总额",
                            data : "useRedMoney"
                        },
                        {
                            title : "红包使用数占比",
                            data : "useRedCountRatio"
                        },
                        /*{
                            title : "红包使用额占比",
                            data : "useRedMoneyRatio"
                        },*/
                        {
                            title : "拉动订单总数",
                            data : "useRedOrderCount"
                        },
                        {
                            title:"拉动订单总额" ,
                            data:"useRedOrderMoney"
                        }
                    ]
                });
            },
            searchInfo : function() {
                var that = this;
                try{
                    $.post("brandMarketing/selectRedList",that.getDate(),function(result){
                        if (result.success){
                            //清空表格
                            that.shopRedInfoTable.clear().draw();
                            //重绘表格
                            that.shopRedInfoTable.rows.add(result.data.shopRedInfoList).draw();
                            that.shopRedInfoList = result.data.shopRedInfoList;
                            that.brandRedInfo = result.data.brandRedInfo;
                            toastr.success("查询成功");
                            toastr.clear();
                        }else {
                            toastr.error("查询红包报表失败!");
                            toastr.clear();
                            return;
                        }
                    });
                }catch(e){
                    toastr.error("查询红包报表失败!");
                    toastr.clear();
                    return;
                }
            },
            downloadExcel : function(){
                $.post("brandMarketing/createExcel",{"grantBeginDate":this.grantSearchDate.beginDate,
                    "grantEndDate" : this.grantSearchDate.endDate,
                    "useBeginDate" : this.useSearchDate.beginDate,
                    "useEndDate" : this.useSearchDate.endDate,
                    "brandRedInfo":this.brandRedInfo,"shopRedInfoList" : this.shopRedInfoList},function(result){
                    if (result.success){
                        window.location.href = "brandMarketing/downloadRedPacketDto?path="+result.data+"";
                    }else {
                        toastr.error("下载红包报表失败！");
                        toastr.clear();
                    }
                });
            },
            getDate : function(){
                var data = {
                    grantBeginDate : this.grantSearchDate.beginDate,
                    grantEndDate : this.grantSearchDate.endDate,
                    useBeginDate : this.useSearchDate.beginDate,
                    useEndDate : this.useSearchDate.endDate,
                    redType : this.redType
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