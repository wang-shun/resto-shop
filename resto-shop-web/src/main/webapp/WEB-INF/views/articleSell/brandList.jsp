<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="control">
<h2 class="text-center"><strong>品牌菜品销售报表</strong></h2><br/>
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
             <button type="button" class="btn btn-primary" @click="searchInfo(false)">查询报表</button>
              &nbsp;
		  	 <button type="button" class="btn btn-primary" @click="brandreportExcel">下载报表</button>
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
				  		品牌菜品销售表
				  	</strong>
				  </div>
				  <div class="panel-body">
				  	<table id="brandMarketing" class="table table-striped table-bordered table-hover" style="width: 100%">
				  		<thead> 
							<tr>
								<th>品牌名称</th>
								<th>菜品总销量(份)</th>
								<th>菜品销售总额(元)</th>
								<th>折扣总额(元)</th>
		                        <th>退菜总数(份)</th>
		                        <th>退菜总额(元)</th>
							</tr>
						</thead>
						<tbody>
							<tr v-if="brandReport.brandName != null">
								<td><strong>{{brandReport.brandName}}</strong></td>
								<td>{{brandReport.totalNum}}</td>
								<td>{{brandReport.sellIncome}}</td>
		                        <td>{{brandReport.discountTotal}}</td>
		                        <td>{{brandReport.refundCount}}</td>
		                        <td>{{brandReport.refundTotal}}</td>
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
	    	<!-- 品牌菜品销售表(单品)   -->
	    	<div class="panel panel-success">
			  <div class="panel-heading text-center">
			  	   <strong style="margin-right:100px;font-size:22px">品牌菜品销售表(单品)</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="brandArticleUnitTable" class="table table-striped table-bordered table-hover"
			  		style="width: 100%;">
			  	</table>
			  </div>
			</div>
	    </div>
	
	    <!-- 套餐 -->
	    <div role="tabpanel" class="tab-pane" id="revenueCount">
	    	<div class="panel panel-primary" style="border-color:white;">
			  	<!-- 品牌菜品销售表(套餐) -->
	    	<div class="panel panel-info">
			  <div class="panel-heading text-center">
			  	<strong style="margin-right:100px;font-size:22px">品牌菜品销售表(套餐)</strong>
			  </div>
			  <div class="panel-body">
			  	<table id="brandArticleFamilyTable" class="table table-striped table-bordered table-hover"
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
            discountTotal:0,
            refundTotal:0,
            refundCount:0,
        },
        searchDate : {
            beginDate : "",
            endDate : "",
        },
        currentType:1,//当前选中页面
        brandArticleUnitTable:{},//单品dataTables对象
        brandArticleFamilyTable:{},//套餐datatables对象
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
            //单品datatable对象
            that.brandArticleUnitTable=$("#brandArticleUnitTable").DataTable({
                lengthMenu: [ [20,50, 75, 100, -1], [20,50, 75, 100, "All"] ],
                ajax : {
                    url : "articleSell/queryOrderArtcile",
                    dataSrc : "data",
                    data:function(d){
                            d.beginDate = that.searchDate.beginDate;
                            d.endDate = that.searchDate.endDate;
                            d.type = that.currentType;
                            return d;
                    }
                },
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
                        orderable : false
                    },
                    {
                        title : "菜品名称",
                        data : "articleName",
                        orderable : false
                    },
                    {
                        title : "销量(份)",
                        data : "brandSellNum"
                    },
                    {
                        title : "销量占比",
                        data : "numRatio",
                        orderable : false,
                        createdCell: function (td, tdData, rowData) {
                        	if(rowData.numRatio != 0){
	                        	var brandSellNum = rowData.brandSellNum;
	                        	var brandSellNumAll = rowData.numRatio;
	                        	var numRatio = ((brandSellNum/brandSellNumAll).toFixed(2));
	                        	$(td).html((numRatio * 100).toFixed(2) + "%");
                        	}else{
                        		$(td).html("0.00%");
                        	}
                       	}
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
                        orderable : false,
                        createdCell: function (td, tdData, rowData) {
                        	if(rowData.salesRatio != 0){
	                        	var salles = rowData.salles;
	                        	var sallesAll = rowData.salesRatio;
	                        	var salesRatio = ((salles/sallesAll).toFixed(2));
	                        	$(td).html((salesRatio * 100).toFixed(2) + "%");
                        	}
                        	else{
                        		$(td).html("0.00%");
                        	}
                       	}
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
                ]
            });
            //套餐datatable对象
            that.brandArticleFamilyTable=$("#brandArticleFamilyTable").DataTable({
            	lengthMenu: [ [20,50, 75, 100, -1], [20,50, 75, 100, "All"] ],
                ajax : {
                    url : "articleSell/queryOrderArtcile",
                    dataSrc : "data",
                    data:function(d){
                            d.beginDate = that.searchDate.beginDate;
                            d.endDate = that.searchDate.endDate;
                            d.type = that.currentType;
                            return d;
                    }
                },
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
                        orderable : false
                    },
                    {
                        title : "菜品名称",
                        data : "articleName",
                        orderable : false
                    },
                    {
                        title : "销量(份)",
                        data : "brandSellNum",
                    },
                    {
                        title : "销量占比",
                        data : "numRatio",
                        orderable : false,
                        createdCell: function (td, tdData, rowData) {
                        	if(rowData.numRatio != 0){
	                        	var brandSellNum = rowData.brandSellNum;
	                        	var brandSellNumAll = rowData.numRatio;
	                        	var numRatio = ((brandSellNum/brandSellNumAll).toFixed(2));
	                        	$(td).html((numRatio * 100).toFixed(2) + "%");
                        	}else{
                        		$(td).html("0.00%");
                        	}
                       	}
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
                        orderable : false,
                        createdCell: function (td, tdData, rowData) {
                        	if(rowData.salesRatio != 0){
	                        	var salles = rowData.salles;
	                        	var sallesAll = rowData.salesRatio;
	                        	var salesRatio = ((salles/sallesAll).toFixed(2));
	                        	$(td).html((salesRatio * 100).toFixed(2) + "%");
                        	}
                        	else{
                        		$(td).html("0.00%");
                        	}
                       	}
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
                        data: "mealAttrId",
                        createdCell: function (td, tdData, rowData) {
                            var button = $("<button class='btn green'>查看详情</button>");
                            button.click(function () {
                                openModal(tdData);
                            })
                            $(td).html(button);
                        }
                    }
                ]
            });
        },
        //切换单品、套餐 type 1:单品 2:套餐
        chooseType:function (type) {
          this.currentType= type;
          this.searchInfo();
        },
        searchInfo : function(isInit) {
            var that = this;
            //判断 时间范围是否合法
            if (this.searchDate.beginDate > this.searchDate.endDate) {
                toastr.error("开始时间不能大于结束时间");
                toastr.clear();
                return false;
            }
            switch (this.currentType)
            {
                case 1:
                    $.post("articleSell/list_brand", this.getDate(), function(result) {
                        that.brandReport.brandName = result.brandName;
                        that.brandReport.totalNum = result.totalNum;
                        that.brandReport.sellIncome=result.sellIncome;
                        that.brandReport.discountTotal=result.discountTotal;
                        that.brandReport.refundCount=result.refundCount;
                        that.brandReport.refundTotal=result.refundTotal;
                        if(!isInit){
                            that.brandArticleUnitTable.ajax.reload();
                        }
                        toastr.success("查询成功");
                        toastr.clear();
                    });
                    break;
                case 2:
                    $.post("articleSell/list_brand", this.getDate(), function(result) {
                    	that.brandReport.brandName = result.brandName;
                        that.brandReport.totalNum = result.totalNum;
                        that.brandReport.sellIncome=result.sellIncome;
                        that.brandReport.discountTotal=result.discountTotal;
                        that.brandReport.refundCount=result.refundCount;
                        that.brandReport.refundTotal=result.refundTotal;
                        that.brandArticleFamilyTable.ajax.reload();
                        toastr.success("查询成功");
                        toastr.clear();
                    });
                    break;
            }
        },
        getDate : function(){
            var data = {
                beginDate : this.searchDate.beginDate,
                endDate : this.searchDate.endDate,
            };
            return data;
        },
        brandreportExcel : function(){
            /* var that = this;
            var beginDate = that.searchDate.beginDate;
            var endDate = that.searchDate.endDate;
            switch(this.currentType){
                case 1:
                    location.href="articleSell/brand_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
                    break;
                case 2:
                    location.href="articleSell/shop_articleId_excel?beginDate="+beginDate+"&&endDate="+endDate+"&&sort="+sort;
                    break;
            } */
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

function openModal(mealAttrId) {
    //更新数据源
    /* $.ajax({
        url: 'member/show/billReport',
        data: {
            'customerId': customerId
        },
        success: function (result) {
            var modal = $("#endModal");
            modal.find(".modal-body").html(result);
            modal.modal()
        },
        error: function () {
            toastr.error("系统异常请重新刷新");
        }
    }); */
}
</script>

