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
                            <input class="btn green"  @click="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
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
            <s:hasPermission name="scmMdBill/add">
                <button class="btn green pull-right" @click="create">新建</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>


<script>
    (function () {
        var cid = "#control";
        var $table = $(".table-body>table");
        var tb = $table.DataTable({
            ajax: {
                url: "scmMdBill/list_all",
                dataSrc: ""
            },
            columns: [
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
                    data: "gmtCreate | moment",
                },
                {
                    title: "状态",
                    data: "state",
                },

                {
                    title: "操作",
                    data: "id",
                    createdCell: function (td, tdData, rowData, row) {
                        var operator = [
                            <s:hasPermission name="scmMdBill/delete">
                            C.createDelBtn(tdData, "scmMdBill/delete"),
                            </s:hasPermission>
                            <s:hasPermission name="scmMdBill/modify">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
        });

        var C = new Controller(null, tb);
        var vueObj = new Vue({
            el: "#control",
            mixins: [C.formVueMix],
            data: {
                showform : false,
                m: {
                    stockPlanId: '',
                    stockPlanName: '',
                    stockPlanNumber: '',
                    billAmount: '',
                    supplierId: '',
                    supplierTax: '',
                    state: '',
                    remark: '',
                },

            },
            methods: {
                closeForm: function () { //关闭新增弹窗
                    this.showform = false;
                    this.m = {};
                },
                create: function () { //打开新增弹窗
                    var that = this;
                    //页面初始化数据
                    //$.get('scmMdBill/list_all',function (jsonData) {
                    //        that.productTypes=jsonData.data;
                    //});
                    this.showform = true;
                    this.m = {
                        stockPlanId: '',
                        stockPlanName: '',
                        stockPlanNumber: '',
                        billAmount: '',
                        supplierId: '',
                        supplierTax: '',
                        state: '',
                        remark: '',

                    };
                },
                edit: function (model) { //编辑打开弹窗
                    var that = this;
                    this.m = model;
                    //初始化查询
                    this.showform = true;
                },

                save: function () {//提交
                    var _this = this;
                    var saveObj = {};
                    //TODO
                    saveObj.stockPlanId = this.parameter.stockPlanId;
                    saveObj.stockPlanName = this.parameter.stockPlanName;
                    saveObj.stockPlanNumber = this.parameter.stockPlanNumber;
                    saveObj.billAmount = this.parameter.billAmount;
                    saveObj.supplierId = this.parameter.supplierId;
                    saveObj.supplierTax = this.parameter.supplierTax;
                    saveObj.state = this.parameter.state;
                    saveObj.remark = this.parameter.remark;

                    var url = 'scmMdBill/modify';
                    if (!this.m.id) {
                        url = 'scmMdBill/create';
                        _this.m;
                    }
                    var submit = false;
                    var message = '';
                    //TODO
                    if (!this.parameter.stockPlanName) message = '入库单';
                    if (!this.parameter.stockPlanNumber) message = '入库单号';
                    if (!this.parameter.billAmount) message = '账单金额';
                    if (!this.parameter.supplierId) message = '供应商id';
                    if (!this.parameter.supplierTax) message = '税人编号';
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

        C.vue = vueObj;
    }());


</script>
