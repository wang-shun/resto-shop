<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="control">
<h2 class="text-center"><strong>菜品销售报表</strong></h2><br/>
<div class="row" id="searchTools">
	<div class="col-md-12">
		<form class="form-inline">
		  <div class="form-group" style="margin-right: 50px;">
		    <label>开始时间：
                <input type="text" id="beginDate" class="form-control form_datetime" :value="searchDate.beginDate" v-model="searchDate.beginDate" readonly="readonly">
            </label>
		  </div>
		  <div class="form-group" style="margin-right: 50px;">
		    <label>结束时间：
		    <input type="text" class="form-control form_datetime" :value="searchDate.endDate" v-model="searchDate.endDate" readonly="readonly">
            </label>
		 	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>
              &nbsp;
		  	 <button type="button" class="btn btn-primary" @click="brandreportExcel">下载报表</button>
              <br/>
          </div>
		</form>
	<div>

<br/>
<br/>
<div>
  <!-- Nav tabs -->
  <ul class="nav nav-tabs" role="tablist" id="ulTab">
    <li role="presentation" class="active" @click="chooseType(1)">
    	<a href="#dayReport" aria-controls="dayReport" role="tab" data-toggle="tab">
            <strong>品牌菜品报表</strong>
       </a>
    </li>
    <li role="presentation" @click="chooseType(2)">
        <a href="#revenueCount" aria-controls="revenueCount" role="tab" data-toggle="tab">
            <strong>店铺菜品销售报表</strong>
        </a>
    </li>
  </ul>
  <!-- Tab panes -->
  <div class="tab-content">
  	<!-- 菜品销售报表 -->
    <div role="tabpanel" class="tab-pane active" id="dayReport">
    	<!-- 品牌菜品销售表   -->
    	<div class="panel panel-success">
		  <div class="panel-heading text-center">
		  	   <strong style="margin-right:100px;font-size:22px">品牌菜品销售表</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="brandArticleTable" class="table table-striped table-bordered table-hover" width="100%">
		  		<thead> 
					<tr>
						<th>品牌名称</th>
						<th>菜品总销量(份)</th>
						<th>菜品销售总额(元)</th>
                        <th>退菜总数</th>
                        <th>退菜总额(元)</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><strong>{{brandReport.brandName}}</strong></td>
						<td>{{brandReport.totalNum}}</td>
						<td>{{brandReport.sellIncome}}</td>
                        <td>{{brandReport.refundCount}}</td>
                        <td>{{brandReport.refundTotal}}</td>
					</tr>
				</tbody>
		  	</table>
		  </div>
		</div>
		
		
		<div class="panel panel-success">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">
                品牌菜品销售表详情
            </strong>
		  </div>
		  <div class="panel-body">
		  	<table id="articleSellTable" class="table table-striped table-bordered table-hover" width="100%"></table>
		  	</table>
		  </div>
		</div>
    </div>
    
    <%--<div class="modal fade" id="reportModal" tabindex="-1" role="dialog" aria-hidden="true">--%>
           <%--<div class="modal-dialog modal-full">--%>
               <%--<div class="modal-content">--%>
                   <%--<div class="modal-header">--%>
                       <%--<button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>--%>
                   <%--</div>--%>
                   <%--<div class="modal-body"> </div>--%>
                   <%--<br/>--%>
                   <%--<div class="modal-footer">--%>
<%--<!--                        <button type="button" class="btn btn-info btn-block"  @click="closeModal">关闭</button> -->--%>
                        <%--<button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button>--%>
                   <%--</div>--%>
               <%--</div>--%>
               <%--<!-- /.modal-content -->--%>
           <%--</div>--%>
           <%--<!-- /.modal-dialog -->--%>
       <%--</div>--%>


    <!-- 店铺菜品销售表 -->
    <div role="tabpanel" class="tab-pane" id="revenueCount">
    	<div class="panel panel-primary" style="border-color:white;">
		  	<!-- 店铺菜品销售表 -->
    	<div class="panel panel-info">
		  <div class="panel-heading text-center">
		  	<strong style="margin-right:100px;font-size:22px">店铺菜品销售记录</strong>
		  </div>
		  <div class="panel-body">
		  	<table id="shopArticleTable" class="table table-striped table-bordered table-hover" width="100%">
		  			<thead>
					<tr>
						<th>店铺名称</th>
						<th>菜品销量(份)</th>
						<th>菜品销售额</th>
						<th>品牌销售占比</th>
                        <th>退菜总数</th>
                        <th>退菜总额</th>
						<th>销售详情</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="shop in shopReportList">
						<td><strong>{{shop.shopName}}</strong></td>
						<td>{{shop.totalNum}}</td>	
						<td>{{shop.sellIncome}}</td>
						<td>{{shop.occupy}}</td>
                        <td>{{shop.refundCount}}</td>
                        <td>{{shop.refundTotal}}</td>
						<td><button class="btn btn-sm btn-success"
								@click="showShopReport(shop.shopName,shop.shopId)">查看详情</button></td>
					</tr>
				</tbody>
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

var sort = "desc";

var vueObj = new Vue({
    el : "#control",
    data : {
        brandReport : {
            brandName : "",
            totalNum : 0,
            sellIncome:0,
            refundTotal:0,
            refundCount:0,
        },
        shopReportList : [],
        searchDate : {
            beginDate : "",
            endDate : "",
        },
        modalInfo:{
            title:"",
            content:""
        },
        currentType:1,//当前选中页面
        tb:{},//dataTables对象
        api:{},
    },
    created : function() {
        var date = new Date().format("yyyy-MM-dd");
        this.searchDate.beginDate = date;
        this.searchDate.endDate = date;
        this.searchInfo(true);
        this.initDataTables();
    },
    methods : {
        initDataTables:function () {
            //that代表 vue对象
            var that = this;
            that.tb=$("#articleSellTable").DataTable({
                "lengthMenu": [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
                ajax : {
                    url : "articleSell/brand_id_data",
                    dataSrc : "data",
                    data:function(d){
                            d.beginDate = that.searchDate.beginDate;
                            d.endDate = that.searchDate.endDate;
                            d.sort = sort;//默认按销量排序
                             return d;
                    }
                },
                ordering:false,
                columns : [
                    {
                        title : "菜品类别",
                        data : "articleFamilyName",
                    },
                    {
                        title : "菜名",
                        data : "articleName",
                    },
                    {
                        title : "菜品类型",
                        data : "typeName",
                    },
                    {
                        title : "编号",
                        data : "numberCode",
                        defaultContent:"",
                    },
                    {
                        title : "销量(份)",
                        data : "brandSellNum",
                    },
                    {
                        title : "销量占比",
                        data : "numRatio",
                    },
                    {
                        title : "销售额(元)",
                        data : "salles",
                    },
                    {
                        title : "销售额占比",
                        data : "salesRatio",
                    },
                    {
                        title:"退菜数量" ,
                        data:"refundCount"
                    },
                    {
                        title:"退菜金额" ,
                        data:"refundTotal"
                    }
                ],
            });


        },
        chooseType:function (type) {
          this.currentType= type;
          this.searchInfo();
        },
//        showBrandReport: function (brandName) {
//            this.openModal("articleSell/show/brandReport", brandName, null);
//        },
        showShopReport : function(shopName,shopId) {
            this.openModal("articleSell/show/shopReport", shopName,shopId);
        },
        searchInfo : function(isInit) {
            var that = this;
            //判断 时间范围是否合法
            if (this.searchDate.beginDate > this.searchDate.endDate) {
                toastr.error("开始时间不能大于结束时间")
                return false;
            }
//            var num = this.getNumActive();
            switch (this.currentType)
            {
                case 1:
                    $.post("articleSell/list_brand", this.getDate(null), function(result) {
                        that.brandReport.brandName = result.brandName;
                        that.brandReport.totalNum = result.totalNum;
                        that.brandReport.sellIncome=result.sellIncome;
                        that.brandReport.refundCount=result.refundCount;
                        that.brandReport.refundTotal=result.refundTotal;
                        if(!isInit){
                            that.tb.ajax.reload();
                        }
                        toastr.success("查询成功");
                    });
                    break;
                case 2:
                    $.post("articleSell/list_shop", this.getDate(null), function(result) {
                        that.shopReportList = result;
                        toastr.success("查询成功");
                    });
                    break;
            }
        },
        openModal : function(url, modalTitle,shopId) {
            $.post(url, this.getDate(shopId),function(result) {
                var modal = $("#reportModal");
                modal.find(".modal-body").html(result);
                modal.find(".modal-title > strong").html(modalTitle);
                modal.modal();
            })

        },
        closeModal : function(){
            var modal = $("#reportModal");
            modal.find(".modal-body").html("");
            modal.modal("hide");
        },
        getDate : function(shopId){
            var data = {
                beginDate : this.searchDate.beginDate,
                endDate : this.searchDate.endDate,
                shopId : shopId
            };
            return data;
        },
        getNumActive:function(){
            var value = $("#ulTab li.active a").text();
            value  = Trim(value)//去空格
            if(value=='品牌菜品报表'){
                return 1;
            }else if(value=="店铺菜品销售报表"){
                return 2;
            }

        },
        brandreportExcel : function(){
            var that = this;
            var beginDate = that.searchDate.beginDate;
            var endDate = that.searchDate.endDate;
            var num = this.getNumActive()
            switch(num){
                case 1:
                    location.href="articleSell/brand_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
                    break;
                case 2:
                    location.href="articleSell/shop_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
                    break;
            }
        },

        today : function(){
            date = new Date().format("yyyy-MM-dd");
            this.searchDate.beginDate = date
            this.searchDate.endDate = date
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
        },

    },

});

function Trim(str)
{ 
    return str.replace(/(^\s*)|(\s*$)/g, ""); 
}

</script>

