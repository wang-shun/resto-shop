<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="control">
<h2 class="text-center"><strong>店铺菜品销售报表</strong></h2><br/>
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
		    <input type="text" id="endDate" class="form-control form_datetime" :value="searchDate.endDate" v-model="searchDate.endDate" readonly="readonly">
            </label>
		 	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             <button type="button" class="btn btn-primary" @click="searchInfo()">查询报表</button>
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
				  		店铺菜品销售表
				  	</strong>
				  	<button type="button" class="btn btn-primary" @click="downloadBrnadArticleTotal" style="float: right;">下载报表</button>
				  </div>
				  <div class="panel-body">
				  	<table id="shopMarketing" class="table table-striped table-bordered table-hover" style="width: 100%">
				  		<thead> 
							<tr>
								<th>店铺名称</th>
								<th>菜品总销量(份)</th>
								<th>菜品销售总额(元)</th>
								<th>折扣总额(元)</th>
		                        <th>退菜总数(份)</th>
		                        <th>退菜总额(元)</th>
							</tr>
						</thead>
						<tbody>
							<tr v-if="shopReport.shopName != null">
								<td><strong>{{shopReport.shopName}}</strong></td>
								<td>{{shopReport.totalNum}}</td>
								<td>{{shopReport.sellIncome}}</td>
		                        <td>{{shopReport.discountTotal}}</td>
		                        <td>{{shopReport.refundCount}}</td>
		                        <td>{{shopReport.refundTotal}}</td>
							</tr>
							<tr v-else>
								<td align="center" colspan="5">载入中...</td>
							</tr>
						</tbody>
				  	</table>
				  </div>
				</div>
				
		    </div>
		</div>
	
	  <!-- Nav tabs -->
	  <ul class="nav nav-tabs" role="tablist" id="ulTab">
	    <li role="presentation" class="active" @click="chooseType(1)">
	    	<a href="#dayReport" aria-controls="dayReport" role="tab" data-toggle="tab">
	            <strong>单品</strong>
	       </a>
	    </li>
	    <li role="presentation" @click="chooseType(2)">
	        <a href="#revenueCount" aria-controls="revenueCount" role="tab" data-toggle="tab">
	            <strong>套餐</strong>
	        </a>
	    </li>
	  </ul>
	  <div class="tab-content">
	  	<!-- 单品 -->
	    <div role="tabpanel" class="tab-pane active" id="dayReport">
	    	<!-- 店铺菜品销售表(单品)   -->
	    	<div class="panel panel-success">
			  <div class="panel-heading text-center">
			  	   <strong style="margin-right:100px;font-size:22px">店铺菜品销售表(单品)</strong>
			  	   <button type="button" class="btn btn-primary" @click="shopreportExcel" style="float: right;">下载报表</button>
			  </div>
			  <div class="panel-body">
			  	<table id="shopArticleUnitTable" class="table table-striped table-bordered table-hover"
			  		style="width: 100%;">
			  	</table>
			  </div>
			</div>
	    </div>
	
	    <!-- 套餐 -->
	    <div role="tabpanel" class="tab-pane" id="revenueCount">
	    	<div class="panel panel-primary" style="border-color:white;">
			  	<!-- 店铺菜品销售表(套餐) -->
	    	<div class="panel panel-info">
			  <div class="panel-heading text-center">
			  	<strong style="margin-right:100px;font-size:22px">店铺菜品销售表(套餐)</strong>
			  	<button type="button" class="btn btn-primary" @click="shopreportExcel" style="float: right;">下载报表</button>
			  </div>
			  <div class="panel-body">
			  	<table id="shopArticleFamilyTable" class="table table-striped table-bordered table-hover"
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
	<div class="modal fade" id="mealAttrModal" tabindex="-1" role="dialog" data-backdrop="static">
        <div class="modal-dialog modal-full">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>
                </div>

                <div class="modal-body" id="reportModal1"></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal">关闭</button>
                    </button>
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
var shopUnitAPI = null;
var shopFamilyAPI = null;
var vueObj = new Vue({
    el : "#control",
    data : {
        shopReport : {
            shopName : "",
            totalNum : 0,
            sellIncome:0,
            discountTotal:0,
            refundTotal:0,
            refundCount:0,
        },
        searchDate : {
            beginDate : "",
            endDate : "",
        },
        currentType:1,//当前选中页面
        shopArticleUnitTable:{},//单品dataTables对象
        shopArticleFamilyTable:{},//套餐datatables对象
        api:{},
        resultData:[]
    },
    created : function() {
        var date = new Date().format("yyyy-MM-dd");
        this.searchDate.beginDate = date;
        this.searchDate.endDate = date;
        this.initDataTables();
        this.searchInfo();
    },
    methods : {
        initDataTables:function () {
            //that代表 vue对象
            var that = this;
            //单品datatable对象
            that.shopArticleUnitTable=$("#shopArticleUnitTable").DataTable({
                lengthMenu: [ [20,50, 75, 100, -1], [20,50, 75, 100, "All"] ],
                order: [[ 3, "desc" ]],
                columns : [
                    {
                        title : "菜品类型",
                        data : "typeName",
                        orderable : false
                    },
                    {
                        title : "菜名类别",
                        data : "articleFamilyName",
                        orderable : false,
                        s_filter: true
                    },
                    {
                        title : "菜品名称",
                        data : "articleName",
                        orderable : false
                    },
                    {
                        title : "销量(份)",
                        data : "shopSellNum"
                    },
                    {
                        title : "销量占比",
                        data : "numRatio",
                        orderable : false
                    },
                    {
                        title : "销售额(元)",
                        data : "salles"
                    },
                    {
                        title : "折扣金额(元)",
                        data : "discountMoney"
                    },
                    {
                        title : "销售额占比",
                        data : "salesRatio",
                        orderable : false
                    },
                    {
                        title:"退菜数量" ,
                        data:"refundCount"
                    },
                    {
                        title:"退菜金额" ,
                        data:"refundTotal"
                    },
                    {
                        title:"点赞数量" ,
                        data:"likes"
                    }
                ],
                initComplete: function () {
                	shopUnitAPI = this.api();
                	that.shopUnitTable();
                }
            });
            //套餐datatable对象
            that.shopArticleFamilyTable=$("#shopArticleFamilyTable").DataTable({
            	lengthMenu: [ [20,50, 75, 100, -1], [20,50, 75, 100, "All"] ],
                order: [[ 3, "desc" ]],
                columns : [
                    {
                        title : "菜品类型",
                        data : "typeName",
                        orderable : false
                    },
                    {
                        title : "菜名类别",
                        data : "articleFamilyName",
                        orderable : false,
                        s_filter: true
                    },
                    {
                        title : "菜品名称",
                        data : "articleName",
                        orderable : false
                    },
                    {
                        title : "销量(份)",
                        data : "shopSellNum",
                    },
                    {
                        title : "销量占比",
                        data : "numRatio",
                        orderable : false
                    },
                    {
                        title : "销售额(元)",
                        data : "salles"
                    },
                    {
                        title : "折扣金额(元)",
                        data : "discountMoney"
                    },
                    {
                        title : "销售额占比",
                        data : "salesRatio",
                        orderable : false
                    },
                    {
                        title:"退菜数量" ,
                        data:"refundCount"
                    },
                    {
                        title:"退菜金额" ,
                        data:"refundTotal"
                    },
                    {
                        title:"点赞数量" ,
                        data:"likes"
                    },
                    {
                        title: "套餐属性",
                        data: "articleId",
                        orderable : false,
                        createdCell: function (td, tdData, rowData) {
                            var button = $("<button class='btn green'>查看详情</button>");
                            button.click(function () {
                                openModal(tdData);
                            })
                            $(td).html(button);
                        }
                    }
                ],
                initComplete: function () {
                	shopFamilyAPI = this.api();
                	that.shopFamilyTable();
                }
            });
        },
        //切换单品、套餐 type 1:单品 2:套餐
        chooseType:function (type) {
          this.currentType= type;
          this.searchInfo();
        },
        searchInfo : function(isInit) {
        	try{
	            var that = this;
	            var api1 = shopUnitAPI;
	            var api2 = shopFamilyAPI;
	            //判断 时间范围是否合法
	            if (that.searchDate.beginDate > that.searchDate.endDate) {
	                toastr.error("开始时间不能大于结束时间");
	                toastr.clear();
	                return false;
	            }
	            that.selectshopArticleToatl();
	            switch (that.currentType)
	            {
	                case 1:
	                	//清空datatable的column搜索条件
	                	api1.search('');
	                	var column1 = api1.column(1);
	                	column1.search('', true, false);
	                	//清空表格
	                	that.shopArticleUnitTable.clear().draw();
	                    that.shopUnitTable();
	                    $.post("articleSell/queryShopOrderArtcile", this.getDate(), function(result) {
		                	if(result.success == true){
			                    that.shopArticleUnitTable.rows.add(result.data).draw();
			                  	//重绘搜索列
			                    that.shopUnitTable();
		            		}
	                    });
	                	break;
	                case 2:
                		//清空datatable的column搜索条件
                        api2.search('');
                    	var column1 = api2.column(1);
                    	column1.search('', true, false);
                    	//清空表格
                    	that.shopArticleFamilyTable.clear().draw();
                        that.shopFamilyTable();
	                    $.post("articleSell/queryShopOrderArtcile", this.getDate(), function(result) {
		                	if(result.success == true){
		                        that.shopArticleFamilyTable.rows.add(result.data).draw();
		                        //重绘搜索列
		                        that.shopFamilyTable();
		            		}
	                    });
	                   break;
	            }
        	}catch(e){
        		toastr.error("查询店铺菜品销售表失败!");
	            toastr.clear();
	            return;
        	}
            toastr.success("查询成功");
            toastr.clear(); 
        },
        selectshopArticleToatl : function(){
        	var that = this;
        	$.post("articleSell/queryShopArticleTotal", this.getDate(), function(result) {
                that.shopReport.shopName = result.shopName;
                that.shopReport.totalNum = result.totalNum;
                that.shopReport.sellIncome=result.sellIncome;
                that.shopReport.discountTotal=result.discountTotal;
                that.shopReport.refundCount=result.refundCount;
                that.shopReport.refundTotal=result.refundTotal;
            });
        },
        getDate : function(){
            var data = {
                beginDate : this.searchDate.beginDate,
                endDate : this.searchDate.endDate,
                type : this.currentType
            };
            return data;
        },
        shopreportExcel : function(){
            var that = this;
            var beginDate = that.searchDate.beginDate;
            var endDate = that.searchDate.endDate;
            switch(that.currentType){
                case 1:
                    location.href="articleSell/downloadShopArticle?beginDate="+beginDate+"&&endDate="+endDate+"&&type="+that.currentType;
                    break;
                case 2:
                    location.href="articleSell/downloadShopArticle?beginDate="+beginDate+"&&endDate="+endDate+"&&type="+that.currentType;
                    break;
            }
        },
        downloadBrnadArticleTotal : function(){
        	var that = this;
            var beginDate = that.searchDate.beginDate;
            var endDate = that.searchDate.endDate;
            location.href="articleSell/downloadShopArticleTotal?beginDate="+beginDate+"&&endDate="+endDate+"";
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
        shopUnitTable : function(){
         	var api = shopUnitAPI;
            api.search('');
            var data = api.data();
            var columnsSetting = api.settings()[0].oInit.columns;
            $(columnsSetting).each(function (i) {
                if (this.s_filter) {
                    var column = api.column(i);
                    var title = this.title;
                    var select = $('<select id=""><option value="">' + this.title + '(全部)</option></select>');
                    var that = this;
                    column.data().unique().each(function (d) {
                        select.append('<option value="' + d + '">' + d + '</option>')
                    });
                    select.appendTo($(column.header()).empty()).on('change', function () {
                        var val = $.fn.dataTable.util.escapeRegex(
                                $(this).val()
                        );
                        column.search(val ? '^' + val + '$' : '', true, false).draw();
                    });
                }
            });
        },
        shopFamilyTable : function(){
       		var api = shopFamilyAPI;
           	api.search('');
           	var data = api.data();
           	var columnsSetting = api.settings()[0].oInit.columns;
           	$(columnsSetting).each(function (i) {
               if (this.s_filter) {
                   var column = api.column(i);
                   var title = this.title;
                   var select = $('<select id=""><option value="">' + this.title + '(全部)</option></select>');
                   var that = this;
                   column.data().unique().each(function (d) {
                       select.append('<option value="' + d + '">' + d + '</option>')
                   });
                   select.appendTo($(column.header()).empty()).on('change', function () {
                       var val = $.fn.dataTable.util.escapeRegex(
                               $(this).val()
                       );
                       column.search(val ? '^' + val + '$' : '', true, false).draw();
                   });
               }
           });
        }
    }
});

function Trim(str)
{ 
    return str.replace(/(^\s*)|(\s*$)/g, ""); 
}

function openModal(articleId) {
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
    $.ajax({
        url: 'articleSell/showMealAttr',
        data: {
            'articleId': articleId,
            'beginDate': beginDate,
            'endDate': endDate,
            'shopId': 'shopId'
        },
        success: function (result) {
            var modal = $("#mealAttrModal");
            modal.find(".modal-body").html(result);
            modal.modal()
        },
        error: function () {
            toastr.error("系统异常请重新刷新");
        }
    });
}
</script>

