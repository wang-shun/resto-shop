<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div id="controller">
    <c:if test="${!empty couponId}">
        <a class="btn btn-info ajaxify" href="newcustomcoupon/list">
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
                    <input type="text" class="form-control" placeholder="请录入消费次数">&nbsp;&nbsp;次
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
                    <input type="text" class="form-control" placeholder="请录入消费总额">&nbsp;&nbsp;元
                </div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <div class="form-group">
                    <label>平均消费金额&nbsp;大于</label>&nbsp;&nbsp;
                    <input type="text" class="form-control" placeholder="请录入消费总额">&nbsp;&nbsp;元
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>最后消费日期距今&nbsp;超过</label>&nbsp;&nbsp;
                    <input type="text" class="form-control" placeholder="请录入天数">&nbsp;&nbsp;天
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>注册时间</label>&nbsp;&nbsp;
                    <div class="form-group" style="margin-right: 10px;">
                        <input type="text" class="form-control form_datetime" placeholder="选择日期" readonly>
                    </div>
                    至
                    <div class="form-group" style="margin-left: 10px;">
                        <input type="text" class="form-control form_datetime" placeholder="选择日期" readonly>
                    </div>
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>是否注册</label>&nbsp;&nbsp;
                    <input type="radio" name="register">注册&nbsp;&nbsp;<input type="radio" name="register">未注册&nbsp;&nbsp;<input type="radio" name="register">不限
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>是否储值</label>&nbsp;&nbsp;
                    <input type="radio" name="value">是&nbsp;&nbsp;<input type="radio" name="value">否&nbsp;&nbsp;<input type="radio" name="value">不限
                </div>
            </form>
            <br/>
            <form class="form-inline">
                <div class="form-group">
                    <label>性别</label>&nbsp;&nbsp;
                    <input type="radio" name="sex">男&nbsp;&nbsp;<input type="radio" name="sex">女&nbsp;&nbsp;<input type="radio" name="sex">未知&nbsp;&nbsp;<input type="radio" name="sex">不限
                </div>
            </form>
            <br/>&nbsp;&nbsp;
            <button type="button" class="btn btn-success">查询</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <button type="button" class="btn btn-primary">发放</button>
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
                    <input type="text" class="form-control" placeholder="请录入手机号/昵称">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" class="btn btn-success">查询</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" class="btn btn-primary">发放</button>
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
    var groupReleaseTableAPI;
    var personalLoansTableAPI;
    new Vue({
        el : "#controller",
        data : {
            groupReleaseTable : {},
            personalLoansTable : {},
            selectAll : false
        },
        created : function() {
            this.initDataTables();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.groupReleaseTable=$("#groupReleaseTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 7, "desc" ]],
                    columns : [
                        {
                            title : "全选",
                            data : "sellectAll",
                            s_filter: true,
                            orderable : false
                        },
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
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 7, "desc" ]],
                    columns : [
                        {
                            title : "全选",
                            data : "sellectAll",
                            s_filter: true,
                            orderable : false
                        },
                        {
                            title : "用户类型",
                            data : "customerType",
                            orderable : false
                        },
                        {
                            title : "储值",
                            data : "isValue",
                            orderable : false
                        },
                        {
                            title : "昵称",
                            data : "nickname",
                            orderable : false
                        },
                        {
                            title : "性别",
                            data : "sex",
                            orderable : false
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
                this.currentType= type;
            },
            searchInfo : function() {
                var that = this;
                toastr.clear();
                toastr.success("查询中...");
                try{
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            download : function(){
            },
            groupReleaseTables : function(){
                var api = groupReleaseTableAPI;
                var columnsSetting = api.settings()[0].oInit.columns;
                $(columnsSetting).each(function (i) {
                    if (this.s_filter) {
                        var column = api.column(i);
                        if (this.title != "全选"){
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
                        }else {
                            var select = $("<input type='checkbox' @click='selectAll' :checked='selectAll'>");
                            select.appendTo($(column.header()).empty()).on('click', function () {
                            });
                        }
                    }
                });
            },
            personalLoansTables : function(){
                var api = personalLoansTableAPI;
                var columnsSetting = api.settings()[0].oInit.columns;
                $(columnsSetting).each(function (i) {
                    if (this.s_filter) {
                        var column = api.column(i);
                        if (this.title != "全选"){
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
                        }else {
                            var select = $("<input type='checkbox' @click='selectAll' :checked='selectAll'>");
                            select.appendTo($(column.header()).empty()).on('click', function () {
                            });
                        }
                    }
                });
            },
            selectAll : function () {
                this.selectAll = true;
            }
        }
    });

</script>