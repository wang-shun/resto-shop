<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="controlShop">
    <a class="btn btn-info ajaxify" href="appraiseReport/list">
        <span class="glyphicon glyphicon-circle-arrow-left"></span>
        返回
    </a>
    <h2 class="text-center">
        <strong>${shopName}</strong>
    </h2>
    <br />
    <div class="row">
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
                <button type="button" class="btn btn-primary" @click="shopreportExcel">下载报表</button><br/>
            </form>
        </div>
    </div>
    <br /> <br />
    <!-- 店铺订单列表  -->
    <div class="panel panel-info">
        <div class="panel-heading text-center" style="font-size: 22px;">
            <strong>店铺评论列表</strong>
        </div>
        <div class="panel-body">
            <table class="table table-striped table-bordered table-hover" id="shopAppraise">
            </table>
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

    var beginDate = "${beginDate}";
	var endDate = "${endDate}";
	var shopId = "${shopId}";
	var vueObjShop = new Vue({
	    el : "#controlShop",
	    data : {
	        searchDate : {
	            beginDate : "",
	            endDate : "",
	        },
            shopAppraiseTable : {}
	    },
	    created : function() {
	        this.searchDate.beginDate = beginDate;
	        this.searchDate.endDate = endDate;
	        this.initDataTables();
	        this.searchInfo();
	    },
	    methods : {
	        initDataTables:function () {
	            //that代表 vue对象
	            var that = this;
                that.shopAppraiseTable = $("#shopAppraise").DataTable({
                    lengthMenu: [ [50, 75, 100, 150], [50, 75, 100, "All"] ],
                    autoWidth: false,
                    columns : [
                        {
                            title:'评分',
                            data:'appraise.level',
                            createdCell:function(td,tdData){
                                $(td).html(getLevel(tdData))
                            }
                        },
                        {
                            title : "评论对象",
                            data : "appraise.feedback"
                        },
                        {
                            title :"评论时间",
                            data : "appraise.createTime",
                            createdCell:function(td,tdData){
                                $(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
                            }
                        },
                        {
                            title : "手机号",
                            data : "customer.telephone",
                            defaultContent:""
                        },
                        {
                            title : "订单金额",
                            data : "orderMoney"
                        },
                        {
                            title : "评论金额",
                            data : "appraise.redMoney"
                        },
                        {
                            title : "评论内容",
                            data : "appraise.content" ,
                            defaultContent:"",
                        },
                    ]
                });
	        },
	        searchInfo : function() {
                toastr.clear();
                toastr.success("查询中...");
	        	try{
		            var that = this;
                    $.post("appraiseReport/shop_data", this.getDate(), function(result) {
                        that.shopAppraiseTable.clear();
                        that.shopAppraiseTable.rows.add(result).draw();
                        toastr.clear();
                        toastr.success("查询成功");
                    });
	        	}catch(e){
					toastr.clear();
                    toastr.error("系统异常，请刷新重试");
	        	}
	        },
	        getDate : function(){
	            var data = {
	                beginDate : this.searchDate.beginDate,
	                endDate : this.searchDate.endDate,
	                shopId : shopId
	            };
	            return data;
	        },
	        shopreportExcel : function(){
	            try {
                }catch (e){
					toastr.clear();
                    toastr.error("系统异常，请刷新重试");
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
	        }
	    }
	});

    function getLevel(level){
        var levelName = '';
        switch (level)
        {
            case 1:
                levelName="一星";
                break;
            case 2:
                levelName="二星";
                break;
            case 3:
                levelName="三星";
                break;
            case 4:
                levelName="四星";
                break;
            case 5:
                levelName="五星";
                break;

        }
        return levelName;
    }
</script>

