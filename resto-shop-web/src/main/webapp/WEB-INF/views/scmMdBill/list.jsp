<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6">
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki"> 表单</span>
                    </div>
                </div>
                <div class="portlet-body">
                    <form role="form" action="{{m.id?'scmMdBill/modify':'scmMdBill/create'}}" @submit.prevent="save">
                        <div class="form-body">
                            <div class="form-group row">
                                <label class="col-md-2 control-label">入库单名称</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="stockPlanName" v-model="m.stockPlanName">
                                </div>
                                <label class="col-md-2 control-label">入库单号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="stockPlanNumber" v-model="m.stockPlanNumber">
                                </div>
                            </div>
                            <div class="form-group row">
                                <label class="col-md-2 control-label">门店</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="shopDetailName" v-model="m.shopDetailName">
                                </div>
                                <label class="col-md-2 control-label">账单金额</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="billAmount" v-model="m.billAmount">
                                </div>
                            </div>

                            <div class="form-group row">
                                <label class="col-md-2 control-label">供应商</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supplierName" v-model="m.supplierName">
                                </div>
                                <label class="col-md-2 control-label">纳税人识别号</label>
                                <div class="col-md-3">
                                    <input type="text" class="form-control" name="supplierTax" v-model="m.supplierTax">
                                </div>
                            </div>

                        </div>
                        <input type="hidden" name="id" v-model="m.id"/>

                        <div class="text-center" style="padding: 20px 0">
                            <input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
                            <a class="btn default" @click="cancel" >取消</a>
                        </div>
                        <%--<input class="btn green" type="submit" value="保存"/>--%>
                        <%--<a class="btn default" @click="cancel">取消</a>--%>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="table-div">
        <div class="table-operator">
            <%--暂时不需要--%>
            <%--<s:hasPermission name="scmMdBill/add">--%>
                <%--<button class="btn green pull-right" @click="create">新建</button>--%>
            <%--</s:hasPermission>--%>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>

        <div class="row" id="searchTools">
            <div class="col-md-12">
                <form class="form-inline">
                    <div class="form-group" style="margin-right: 50px;">
                        <label for="beginDate">开始时间：</label>
                        <input type="text" class="form-control form_datetime" id="beginDate" v-model="grantSearchDate.beginDate"  name="beginDate"  readonly="readonly">
                    </div>
                    <div class="form-group" style="margin-right: 50px;">
                        <label for="endDate">结束时间：</label>
                        <input type="text" class="form-control form_datetime" id="endDate" v-model="grantSearchDate.endDate"  name="endDate"  readonly="readonly">
                    </div>
                    <button type="button" class="btn btn-primary" @click="today"> 今日</button>
                    <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>
                    <button type="button" class="btn btn-primary" @click="week">本周</button>
                    <button type="button" class="btn btn-primary" @click="month">本月</button>
                    <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
                    <%--<button type="button" class="btn btn-primary" @click="createOrderExcel">下载报表</button><br/>--%>
                </form>

            </div>
        </div>
        <br/>
        <br/>

        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>

<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
    (function () {
        var vueObj = new Vue({
            el: "#control",
            data: {
                showform : false,
                grantSearchDate : {
                    beginDate : '',
                    endDate : '',
                },
                mdBillTable:{},
                mdBillList : [],

            },
            /***
             *  这是它的一个生命周期钩子函数，就是一个vue实例被生成后调用这个函数。
             */
            created: function () { //打开新增弹窗
                $("#beginDate").datetimepicker({
                    format: 'yyyy-mm-dd',
                    minView:'month',
                    language: 'zh-CN',
                    autoclose:true,
                    startDate:new Date(),
                }).on("click",function(){
                    $("#beginDate").datetimepicker("setEndDate",$("#endDate").val())
                });
                $("#endDate").datetimepicker({
                    format: 'yyyy-mm-dd',
                    minView:'month',
                    language: 'zh-CN',
                    autoclose:true,
                    startDate:new Date(),
                }).on("click",function(){
                    $("#endDate").datetimepicker("setStartDate",$("#beginDate").val())
                });
                this.initDataTables();
                this.searchInfo();
            },
            methods: {
                create: function () { //打开新增弹窗
                    var that = this;
                    this.showform = true;
                },

                closeForm: function () { //关闭新增弹窗
                    this.showform = false;
                    this.m = {};
                },
                initDataTables:function () {
                    //that代表 vue对象
                    var that = this;
                    that.mdBillTable = $(".table-body>table").DataTable({
                        lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                        columns : [
                            {
                                title: "账单单号",
                                data: "billNumber",
                            },
                            {
                                title: "门店",
                                data: "shopDetailName",
                            },
                            {
                                title: "入库单名称",
                                data: "stockPlanName",
                            },
                            {
                                title: "入库单号",
                                data: "stockPlanNumber",
                            },
                            {
                                title: "账单金额",
                                data: "billAmount",
                            },
                            {
                                title: "供应商",
                                data: "supplierName",
                            },
                            {
                                title: "纳税人识别号",
                                data: "supplierTax",
                            },
                            {
                                title: "生成时间",
                                data: "gmtCreate",
                            },
                        ]
                    });
                },
                searchInfo : function() {
                    var that = this;
                    var timeCha1 = new Date(this.grantSearchDate.endDate).getTime() - new Date(this.grantSearchDate.beginDate).getTime();
                    if(timeCha1 < 0){
                        toastr.clear();
                        toastr.error("开始时间应该少于结束时间！");
                        return false;
                    }
                    toastr.clear();
                    toastr.success("查询中...");
                    try{
                        $.post("scmMdBill/list_all",that.getDate(),function(result){
                            if (result.success){
                                //清空表格
                                that.mdBillTable.clear()
                                //重绘表格
                                that.mdBillTable.rows.add(result.data).draw();
                                that.mdBillList = result.data;
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
                // downloadExcel : function(){
                //  $.post("brandMarketing/createCouponExcel",{"grantBeginDate":this.grantSearchDate.beginDate,
                //  "grantEndDate" : this.grantSearchDate.endDate,
                //  "useBeginDate" : this.useSearchDate.beginDate,
                //   "useEndDate" : this.useSearchDate.endDate,
                //   "brandCouponInfo":this.brandCouponInfo,"shopCouponInfoList" : this.shopCouponInfoList},function(result){
                //    if (result.success){
                //          window.location.href = "brandMarketing/downloadCouponDto?path="+result.data+"";
                //      }else {
                //             toastr.clear();
                //             toastr.error("下载红包报表失败！");
                //            }
                //       });
                //   },

                getDate : function(){
                    var data = {
                        beginDate : this.grantSearchDate.beginDate,
                        endDate : this.grantSearchDate.endDate,
                    };
                    return data;
                },
                today : function(){
                    var date = new Date().format("yyyy-MM-dd");
                    this.grantSearchDate.beginDate = date
                    this.grantSearchDate.endDate = date
                    this.initDataTables();
                    this.searchInfo();
                },
                yesterDay : function(){
                    this.grantSearchDate.beginDate = GetDateStr(-1);
                    this.grantSearchDate.endDate  = GetDateStr(-1);

                    this.searchInfo();
                },
                week : function(){
                    this.grantSearchDate.beginDate  = getWeekStartDate();
                    this.grantSearchDate.endDate  = new Date().format("yyyy-MM-dd")

                    this.searchInfo();
                },
                month : function(){
                    this.grantSearchDate.beginDate  = getMonthStartDate();
                    this.grantSearchDate.endDate  = new Date().format("yyyy-MM-dd")
                    this.searchInfo();
                },
                save: function () {//提交
                    var _this = this;
                    var saveObj = {};
                    saveObj.stockPlanId = this.m.stockPlanId;
                    saveObj.stockPlanName = this.m.stockPlanName;
                    saveObj.stockPlanNumber = this.m.stockPlanNumber;
                    saveObj.billAmount = this.m.billAmount;
                    saveObj.supplierId = this.m.supplierId;
                    saveObj.supplierTax = this.m.supplierTax;
                    saveObj.state = this.m.state;
                    saveObj.remark = this.m.remark;

                    var url = 'scmMdBill/modify';
                    if (!this.m.id) {
                        url = 'scmMdBill/create';
                        _this.m;
                    }
                    var submit = false;
                    var message = '';
                    if (!this.m.stockPlanName) message = '入库单';
                    if (!this.m.stockPlanNumber) message = '入库单号';
                    if (!this.m.billAmount) message = '账单金额';
                    if (!this.m.supplierId) message = '供应商id';
                    if (!this.m.supplierTax) message = '税人编号';
                    else submit = true;
                    if (this.m.state = '' || !this.m.state) {
                        this.m.state = 1;
                    } else {
                        this.m.state = 0;
                    }
                    if (submit) {
                        $.ajax({
                            type: "POST",
                            url: url,
                            contentType: "application/json",
                            datatype: "json",
                            data: JSON.stringify(saveObj),
                            success: function (data) { //成功后返回
                                C.systemButtonNo('success', '成功');
                                _this.showform = false;
                            },
                            error: function () { //失败后执行
                                C.systemButtonNo('error', '失败');
                                _this.showform = false;
                            }
                        });
                    } else {
                        C.systemButtonNo('error', '请填写' + message);
                    }
                }

            },
        });

        Vue.filter('moment', function (value, formatString) {
            formatString = formatString || 'YYYY-MM-DD HH:mm:ss';
            return moment(value).format(formatString);
        });
    }());


</script>
