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
		    <input type="text" id="endDate" class="form-control form_datetime" :value="searchDate.endDate" v-model="searchDate.endDate" readonly="readonly">
            </label>
		 	 <button type="button" class="btn btn-primary" @click="today"> 今日</button>
             <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
             <button type="button" class="btn btn-primary" @click="week">本周</button>
             <button type="button" class="btn btn-primary" @click="month">本月</button>
             <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>
             <button type="button" class="btn btn-primary" @click="createBrnadArticleTotal" v-if="state == 1">下载报表</button>
             <button type="button" class="btn btn-default" disabled="disabled" v-if="state == 2">下载数据过多，正在生成中</button>
             <button type="button" class="btn btn-success" @click="download" v-if="state == 3">已完成，点击下载</button>
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
                            <template v-if="brandReport.brandName != ''">
                                <tr>
                                    <td><strong>{{brandReport.brandName}}</strong></td>
                                    <td>{{brandReport.totalNum}}</td>
                                    <td>{{brandReport.sellIncome}}</td>
                                    <td>{{brandReport.discountTotal}}</td>
                                    <td>{{brandReport.refundCount}}</td>
                                    <td>{{brandReport.refundTotal}}</td>
                                </tr>
                            </template>
                            <template v-else>
                                <tr>
                                    <td align="center" colspan="5">暂时没有数据...</td>
                                </tr>
                            </template>
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
var brandUnitAPI = null;
var brandFamilyAPI = null;
var vueObj = new Vue({
    el : "#control",
    data : {
        brandReport : {
            brandName : ""
        },
        searchDate : {
            beginDate : "",
            endDate : "",
        },
        currentType:1,//当前选中页面
        brandArticleUnitTable:{},//单品dataTables对象
        brandArticleFamilyTable:{},//套餐datatables对象
        api:{},
        resultData:[],
        state : 1,
        brandArticleUnit :[],
        brandArticleFamily　:[],
        path : null
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
            that.brandArticleUnitTable=$("#brandArticleUnitTable").DataTable({
                lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
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
                        data : "brandSellNum"
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
                	brandUnitAPI = this.api();
                	that.brandUnitTable();
                }
            });
            //套餐datatable对象
            that.brandArticleFamilyTable=$("#brandArticleFamilyTable").DataTable({
            	lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
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
                        data : "brandSellNum",
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
                	brandFamilyAPI = this.api();
                	that.brandFamilyTable();
                }
            });
        },
        //切换单品、套餐 type 1:单品 2:套餐
        chooseType:function (type) {
          this.currentType= type;
        },
        searchInfo : function() {
            toastr.success("查询中...");
        	try{
	            var that = this;
	            var api1 = brandUnitAPI;
	            var api2 = brandFamilyAPI;
                $.post("articleSell/queryOrderArtcile", this.getDate(), function(result) {
                    if(result.success == true){
                        that.brandReport = result.data.brandReport;
                        //清空brandUnitDatatable的column搜索条件
                        api1.search('');
                        var column1 = api1.column(1);
                        column1.search('', true, false);
                        //清空brandFamilyDatatable的column搜索条件
                        api2.search('');
                        var column2 = api2.column(1);
                        column2.search('', true, false);
                        that.brandArticleUnit = result.data.brandArticleUnit;
                        that.brandArticleUnitTable.clear().draw();
                        that.brandArticleUnitTable.rows.add(result.data.brandArticleUnit).draw();
                        //重绘搜索列
                        that.brandUnitTable();
                        that.brandArticleFamily = result.data.brandArticleFamily;
                        that.brandArticleFamilyTable.clear().draw();
                        that.brandArticleFamilyTable.rows.add(result.data.brandArticleFamily).draw();
                        //重绘搜索列
                        that.brandFamilyTable();
                        toastr.success("查询成功");
                    }else{
                        toastr.error("查询失败");
                    }
                });
        	}catch(e){
        		toastr.error("查询品牌菜品销售表失败!");
        	}
        },
        download : function(){
            window.location.href = "articleSell/downloadBrnadArticle?path="+this.path+"";
            this.state = 1;
        },
        getDate : function(){
            var data = {
                beginDate : this.searchDate.beginDate,
                endDate : this.searchDate.endDate,
            };
            return data;
        },
        createBrnadArticleTotal : function(){
            try {
                var that = this;
                var object = {
                    beginDate : that.searchDate.beginDate,
                    endDate : that.searchDate.endDate,
                    type : that.currentType,
                    brandReport : that.brandReport
                }
                switch (that.currentType) {
                    case 1:
                        var articleUnit = that.brandArticleUnit;
                        if (articleUnit.length <= 1000) {
                            object.brandArticleUnit = articleUnit;
                            $.post("articleSell/createBrnadArticle",object,function(result){
                                if(result.success){
                                    window.location.href = "articleSell/downloadBrnadArticle?path="+result.data+"";
                                }else{
                                    toastr.error("下载报表出错!");
                                }
                            });
                        }else{
                            that.state = 2;
                            var length = Math.ceil(articleUnit.length/1000);
                            var start = 0;
                            var end = 1000;
                            var startPosition = 1006;
                            var path = null;
                            for(var i = 1;i <= length;i++){
                                if (i != length){
                                    object.brandArticleUnit = articleUnit.slice(start,end);
                                }else{
                                    object.brandArticleUnit = articleUnit.slice(start);
                                }
                                object.startPosition = startPosition;
                                object.path = that.path;
                                if(i == 1){
                                    $.ajax({
                                        url:"articleSell/createBrnadArticle",
                                        type:"POST",
                                        async: false,
                                        data:object,
                                        dataType:"json",
                                        success:function(result){
                                            if(result.success){
                                                that.path = result.data;
                                                start = end;
                                                end = start + 1000;
                                            }else{
                                                toastr.error("生成报表出错!");
                                                return;
                                            }
                                        }
                                    });
                                }else if(i == length){
                                    $.post("articleSell/appendToExcel",object,function(result){
                                        if(result.success){
                                            that.state = 3;
                                        }else{
                                            toastr.error("生成报表出错!");
                                            return;
                                        }
                                    });
                                }else{
                                    $.ajax({
                                        url:"articleSell/appendToExcel",
                                        type:"POST",
                                        async: false,
                                        data:object,
                                        dataType:"json",
                                        success:function(result){
                                            if(result.success){
                                                start = end;
                                                end = start + 1000;
                                                startPosition = startPosition + 1000;
                                            }else{
                                                toastr.error("生成报表出错!");
                                                return;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    case 2:
                        var articleFamily = that.brandArticleFamily;
                        if (articleFamily.length <= 1000) {
                            object.brandArticleFamily = articleFamily;
                            $.post("articleSell/createBrnadArticle",object,function(result){
                                if(result.success){
                                    window.location.href = "articleSell/downloadBrnadArticle?path="+result.data+"";
                                }else{
                                    toastr.error("下载报表出错!");
                                }
                            });
                        }else{
                            that.state = 2;
                            var length = Math.ceil(articleFamily.length/1000);
                            var start = 0;
                            var end = 1000;
                            var startPosition = 1006;
                            var path = null;
                            for(var i = 1;i <= length;i++){
                                if (i != length){
                                    object.brandArticleFamily = articleFamily.slice(start,end);
                                }else{
                                    object.brandArticleFamily = articleFamily.slice(start);
                                }
                                object.startPosition = startPosition;
                                object.path = that.path;
                                if(i == 1){
                                    $.ajax({
                                        url:"articleSell/createBrnadArticle",
                                        type:"POST",
                                        async: false,
                                        data:object,
                                        dataType:"json",
                                        success:function(result){
                                            if(result.success){
                                                that.path = result.data;;
                                                start = end;
                                                end = start + 1000;
                                            }else{
                                                toastr.error("生成报表出错!");
                                                return;
                                            }
                                        }
                                    });
                                }else if(i == length){
                                    $.post("articleSell/appendToExcel",object,function(result){
                                        if(result.success){
                                            that.state = 3;
                                        }else{
                                            toastr.error("生成报表出错!");
                                            return;
                                        }
                                    });
                                }else{
                                    $.ajax({
                                        url:"articleSell/appendToExcel",
                                        type:"POST",
                                        async: false,
                                        data:object,
                                        dataType:"json",
                                        success:function(result){
                                            if(result.success){
                                                start = end;
                                                end = start + 1000;
                                                startPosition = startPosition + 1000;
                                            }else{
                                                toastr.error("生成报表出错!");
                                                return;
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        break;
                }
            }catch (e){
                toastr.error("下载报表出错!");
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
        brandUnitTable : function(){
         	var api = brandUnitAPI;
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
        brandFamilyTable : function(){
       		var api = brandFamilyAPI;
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

function openModal(articleId) {
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
    $.ajax({
        url: 'articleSell/showMealAttr',
        data: {
            'articleId': articleId,
            'beginDate': beginDate,
            'endDate': endDate
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

