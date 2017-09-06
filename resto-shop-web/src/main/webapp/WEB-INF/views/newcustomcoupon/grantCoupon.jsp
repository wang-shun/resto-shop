<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="controller">
    <c:if test="${intoType eq 2}">
        <a class="btn btn-info ajaxify" href="newcustomcoupon/list">
            <span class="glyphicon glyphicon-circle-arrow-left"></span>
            返回
        </a>
    </c:if>
    <c:if test="${intoType eq 1}">
        <a class="btn btn-info ajaxify" href="member/myList">
            <span class="glyphicon glyphicon-circle-arrow-left"></span>
            返回
        </a>
    </c:if>
    <br/><br/>
    <ul class="nav nav-tabs" role="tablist" id="ulTab">
        <li role="presentation" class="active" @click="chooseType(1)">
            <a href="#groupRelease" aria-controls="groupRelease" role="tab" data-toggle="tab">
                <strong>群体发放</strong>
            </a>
        </li>
        <li role="presentation" @click="chooseType(2)">
            <a href="#personalLoans" aria-controls="personalLoans" role="tab" data-toggle="tab">
                <strong>个人发放</strong>
            </a>
        </li>
    </ul>
    <br/>
    <div class="tab-content">
        <!-- 群体发放 -->
        <div role="tabpanel" class="tab-pane active" id="groupRelease">
            <form class="form-inline">
                <div class="form-group">
                    <label>消费次数&nbsp;大于</label>&nbsp;&nbsp;
                    <input type="number" class="form-control" v-model="selectObject.orderCount" placeholder="请录入消费次数">&nbsp;&nbsp;次
                </div>
                <%--&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--%>
                <%--<div class="form-group">--%>
                    <%--<label>消费次数<select></select>大于</label>&nbsp;&nbsp;--%>
                    <%--<input type="text" class="form-control" placeholder="请录入消费次数">&nbsp;&nbsp;次--%>
                <%--</div>--%>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>消费总额&nbsp;大于</label>&nbsp;&nbsp;
                    <input type="text" class="form-control" v-model="selectObject.orderTotal" placeholder="请录入消费总额">&nbsp;&nbsp;元
                </div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <div class="form-group">
                    <label>平均消费金额&nbsp;大于</label>&nbsp;&nbsp;
                    <input type="text" class="form-control" v-model="selectObject.avgOrderMoney" placeholder="请录入消费总额">&nbsp;&nbsp;元
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>最后消费日期距今&nbsp;超过</label>&nbsp;&nbsp;
                    <input type="number" class="form-control" v-model="selectObject.lastOrderDay" placeholder="请录入天数">&nbsp;&nbsp;天
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>注册时间</label>&nbsp;&nbsp;
                    <div class="form-group" style="margin-right: 10px;">
                        <input type="text" class="form-control form_datetime" v-model="selectObject.registerBeginDate" placeholder="选择日期" readonly>
                    </div>
                    至
                    <div class="form-group" style="margin-left: 10px;">
                        <input type="text" class="form-control form_datetime" v-model="selectObject.registerEndDate" placeholder="选择日期" readonly>
                    </div>
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>是否注册</label>&nbsp;&nbsp;
                    <input type="radio" name="register" v-model="selectObject.register" value="1">注册
                    &nbsp;&nbsp;
                    <input type="radio" name="register" v-model="selectObject.register" value="0">未注册
                    &nbsp;&nbsp;
                    <input type="radio" name="register" v-model="selectObject.register" checked value="2">不限
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>是否储值</label>&nbsp;&nbsp;
                    <input type="radio" name="value" v-model="selectObject.isValue" value="1">是
                    &nbsp;&nbsp;
                    <input type="radio" name="value" v-model="selectObject.isValue" value="0">否
                    &nbsp;&nbsp;
                    <input type="radio" name="value" v-model="selectObject.isValue" checked value="2">不限
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>性别</label>&nbsp;&nbsp;
                    <input type="radio" name="sex" v-model="selectObject.sex" value="1">男
                    &nbsp;&nbsp;
                    <input type="radio" name="sex" v-model="selectObject.sex" value="2">女
                    &nbsp;&nbsp;
                    <input type="radio" name="sex" v-model="selectObject.sex" value="0">未知
                    &nbsp;&nbsp;
                    <input type="radio" name="sex" v-model="selectObject.sex" checked value="3">不限
                </div>
            </form>
            <br/>&nbsp;&nbsp;
            <button type="button" class="btn btn-success" @click="searchInfo">查询</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <button type="button" class="btn btn-primary" @click="grantCoupon">发放</button>
            <br/><br/>
            <table id="groupReleaseTable" class="table table-striped table-bordered table-hover"
                   style="width: 100%;">
            </table>
        </div>

        <!-- 个人发放 -->
        <div role="tabpanel" class="tab-pane" id="personalLoans">
            <form class="form-inline">
                <div class="form-group">
                    <label>查询用户</label>&nbsp;&nbsp;
                    <input type="text" class="form-control" v-model="personalLoanSelectObject.text" placeholder="请录入手机号/昵称">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" class="btn btn-success" @click="searchInfo">查询</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" class="btn btn-primary" @click="grantCoupon">发放</button>
                </div>
            </form>
            <br/><br/>
            <table id="personalLoansTable" class="table table-striped table-bordered table-hover"
                   style="width: 100%;">
            </table>
        </div>
    </div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
    String.prototype.trim = function () {
        return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "");
    };
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
    //得到当前流失唤醒优惠券的Id
    var couponId = "${couponId}";
    var intoType = ${intoType};
    var groupReleaseTableAPI;
    var personalLoansTableAPI;
    new Vue({
        el : "#controller",
        data : {
            groupReleaseTable : {}, //群体发放datatables对象
            personalLoansTable : {}, //个人发放datatables对象
            selectObject : null, //群体发放查询对象
            personalLoanSelectObject : null, //个人发放查询对象
            currentType : 1, //当前所在模块位置 1：群体发放 2：个人发放
            groupReleaseCustomerIds : "", //群体发放用户的Id
            personalLoansCustomerIds : "" //个人发放用户的Id
        },
        created : function() {
            this.initDataTables();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.groupReleaseTable=$("#groupReleaseTable").DataTable({
                    lengthMenu: [ [100, 50, 10], [100, 50, 10] ],
                    order: [[ 6, "desc" ]],
                    columns : [
                        {
                            title : "用户类型",
                            data : "customerType",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "储值",
                            data : "isValue",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "昵称",
                            data : "nickname",
                            orderable : false
                        },
                        {
                            title : "性别",
                            data : "sex",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "手机号",
                            data : "telephone",
                            orderable : false
                        },
                        {
                            title : "生日",
                            data : "birthday"
                        },
                        {
                            title : "订单总数",
                            data : "orderCount"
                        },
                        {
                            title:"订单总额" ,
                            data:"orderMoney"
                        },
                        {
                            title:"平均消费金额" ,
                            data:"AVGOrderMoney"
                        }
                    ],
                    initComplete: function () {
                        groupReleaseTableAPI = this.api();
                        that.groupReleaseTables();
                    }
                });
                that.personalLoansTable=$("#personalLoansTable").DataTable({
                    lengthMenu: [ [100, 50, 10], [100, 50, 10] ],
                    order: [[ 6, "desc" ]],
                    columns : [
                        {
                            title : "用户类型",
                            data : "customerType",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "储值",
                            data : "isValue",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "昵称",
                            data : "nickname",
                            orderable : false
                        },
                        {
                            title : "性别",
                            data : "sex",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "手机号",
                            data : "telephone",
                            orderable : false
                        },
                        {
                            title : "生日",
                            data : "birthday"
                        },
                        {
                            title : "订单总数",
                            data : "orderCount"
                        },
                        {
                            title:"订单总额" ,
                            data:"orderMoney"
                        },
                        {
                            title:"平均消费金额" ,
                            data:"AVGOrderMoney"
                        }
                    ],
                    initComplete: function () {
                        personalLoansTableAPI = this.api();
                        that.personalLoansTables();
                    }
                });
            },
            //切换单品、套餐 type 1:单品 2:套餐 3:类别
            chooseType:function (type) {
                this.currentType = type;
            },
            searchInfo : function() {
                var that = this;
                if (that.currentType == 1) {
                    if (that.selectObject == null || ((that.selectObject.orderCount == null || that.selectObject.orderCount.trim() == "")
                        && (that.selectObject.orderTotal == null || that.selectObject.orderTotal.trim() == "")
                        && (that.selectObject.avgOrderMoney == null || that.selectObject.avgOrderMoney.trim() == "")
                        && (that.selectObject.lastOrderDay == null || that.selectObject.lastOrderDay.trim() == "")
                        && (that.selectObject.registerBeginDate == null || that.selectObject.registerBeginDate.trim() == "")
                        && (that.selectObject.registerEndDate == null || that.selectObject.registerEndDate.trim() == "")
                        && (that.selectObject.register == null || that.selectObject.register.trim() == "")
                        && (that.selectObject.isValue == null || that.selectObject.isValue.trim() == "")
                        && (that.selectObject.sex == null || that.selectObject.sex.trim() == "")
                        )) {
                        that.showDialog(function () {
                            that.selectFunction();
                        });
                    }else {
                        that.selectFunction();
                    }
                }else {
                    if (that.personalLoanSelectObject == null || that.personalLoanSelectObject.text == null || that.personalLoanSelectObject.text.trim() == "") {
                        that.showDialog(function () {
                            that.selectFunction();
                        });
                    }else {
                        that.selectFunction();
                    }
                }
            },
            showDialog : function (successcbk) {
                var cDialog = new dialog({
                    title:"提示",
                    content:"未录入查询条件，你确定要查询所有用户吗？",
                    width:350,
                    ok:function(){
                        if(typeof successcbk=="function"){
                            successcbk();
                        }
                    },
                    cancel:function(){
                        if(typeof cancelcbk=="function"){
                            cancelcbk();
                        }
                    }
                });
                cDialog.showModal();
            },
            selectFunction : function () {
                var that = this;
                toastr.clear();
                toastr.success("查询中...");
                try{
                    var api1 = groupReleaseTableAPI;
                    var api2 = personalLoansTableAPI;
                    switch (that.currentType){
                        case 1:
                            $.post("newcustomcoupon/selectCustomer", that.selectObject, function (result) {
                                toastr.clear();
                                if (result.success) {
                                    that.groupReleaseCustomerIds = "";
                                    api1.search('');
                                    var column1 = api1.column(1);
                                    column1.search('', true, false);
                                    var column2 = api1.column(2);
                                    column2.search('', true, false);
                                    var column4 = api1.column(4);
                                    column4.search('', true, false);
                                    for (var index in result.data){
                                        that.groupReleaseCustomerIds = that.groupReleaseCustomerIds + result.data[index].customerId + ",";
                                    }
                                    that.groupReleaseCustomerIds = that.groupReleaseCustomerIds.substring(0,that.groupReleaseCustomerIds.length-1);
                                    //清空表格
                                    that.groupReleaseTable.clear();
                                    that.groupReleaseTable.rows.add(result.data).draw();
                                    //重绘搜索列
                                    that.groupReleaseTables();
                                    toastr.success("查询成功");
                                    console.log(that.groupReleaseCustomerIds);
                                }else {
                                    toastr.error("查询失败");
                                }
                            });
                            break;
                        case 2:
                            that.personalLoanSelectAll = false;
                            $.post("newcustomcoupon/selectCustomer", that.personalLoanSelectObject, function (result) {
                                toastr.clear();
                                if (result.success) {
                                    that.personalLoansCustomerIds = "";
                                    api2.search('');
                                    var column1 = api2.column(1);
                                    column1.search('', true, false);
                                    var column2 = api2.column(2);
                                    column2.search('', true, false);
                                    var column4 = api2.column(4);
                                    column4.search('', true, false);
                                    for (var index in result.data){
                                        that.personalLoansCustomerIds = that.personalLoansCustomerIds + result.data[index].customerId + ",";
                                    }
                                    that.personalLoansCustomerIds = that.personalLoansCustomerIds.substring(0,that.personalLoansCustomerIds.length-1);
                                    //清空表格
                                    that.personalLoansTable.clear();
                                    that.personalLoansTable.rows.add(result.data).draw();
                                    //重绘搜索列
                                    that.personalLoansTables();
                                    toastr.success("查询成功");
                                    console.log(that.personalLoansCustomerIds);
                                }else {
                                    toastr.error("查询失败");
                                }
                            });
                            break;
                    }
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            grantCoupon : function(){
                var that = this;
                try{
                    switch (that.currentType){
                        case 1:
                            if (that.groupReleaseCustomerIds.trim() == ""){
                                toastr.clear();
                                toastr.error("暂无发放用户");
                                return;
                            }
                            toastr.clear();
                            toastr.success("发放中，请稍后");
                            $.post("newcustomcoupon/grantCoupon",{customerId : that.groupReleaseCustomerIds, couponId : couponId} , function (result) {
                                if (result.success){
                                    toastr.clear();
                                    toastr.success("发放成功");
                                }else {
                                    toastr.clear();
                                    toastr.error("发放失败");
                                }
                            });
                            break;
                        case 2:
                            if (that.personalLoansCustomerIds.trim() == ""){
                                toastr.clear();
                                toastr.error("暂无发放用户");
                                return;
                            }
                            toastr.clear();
                            toastr.success("发放中，请稍后");
                            $.post("newcustomcoupon/grantCoupon",{customerId : that.personalLoansCustomerIds, couponId : couponId} , function (result) {
                                if (result.success){
                                    toastr.clear();
                                    toastr.success("发放成功");
                                }else {
                                    toastr.clear();
                                    toastr.error("发放失败");
                                }
                            });
                            break;
                    }
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            groupReleaseTables : function(){
                var api = groupReleaseTableAPI;
                var columnsSetting = api.settings()[0].oInit.columns;
                $(columnsSetting).each(function (i) {
                    if (this.s_filter) {
                        var column = api.column(i);
                        var select = $('<select id=""><option value="">' + this.title + '(全部)</option></select>');
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
            personalLoansTables : function(){
                var api = personalLoansTableAPI;
                var columnsSetting = api.settings()[0].oInit.columns;
                $(columnsSetting).each(function (i) {
                    if (this.s_filter) {
                        var column = api.column(i);
                        var select = $('<select id=""><option value="">' + this.title + '(全部)</option></select>');
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

</script>