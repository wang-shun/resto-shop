<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
    <h2 class="text-center"><strong>会员信息列表</strong></h2>
    <!-- <div class="row" id="searchTools">
        <div class="col-md-12">
            <form class="form-inline">
              <div class="form-group" style="margin-right: 50px;">
                <label for="beginDate">开始时间：</label>
                <input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
              </div>
              <div class="form-group" style="margin-right: 50px;">
                <label for="endDate">结束时间：</label>
                <input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
              <br></div>
                   <button type="button" class="btn btn-primary" id="today"> 今日</button>
                 <button type="button" class="btn btn-primary" id="yesterDay">昨日</button>
                 <button type="button" class="btn yellow" id="benxun">本询</button>
                 <button type="button" class="btn btn-primary" id="week">本周</button>
                 <button type="button" class="btn btn-primary" id="month">本月</button>

              <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;
              <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>

            </form>
        </div>
    </div> -->
    <div class="row" id="searchTools">
        <div class="col-md-12">
            <form class="form-inline">
                <%--<div class="form-group" style="margin-right: 50px;">--%>
                <%--<label for="beginDate">开始时间：</label>--%>
                <%--<input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate"   readonly="readonly">--%>
                <%--</div>--%>
                <%--<div class="form-group" style="margin-right: 50px;">--%>
                <%--<label for="endDate">结束时间：</label>--%>
                <%--<input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate"   readonly="readonly">--%>
                <%--</div>--%>
                <%----%>
                <%--<button type="button" class="btn btn-primary" id="today"> 今日</button>--%>
                <%----%>
                <%--<button type="button" class="btn btn-primary" id="yesterDay">昨日</button>--%>
                <%----%>
                <%--<!--              <button type="button" class="btn btn-primary" @click="benxun">本询</button> -->--%>
                <%----%>
                <%--<button type="button" class="btn btn-primary" id="week">本周</button>--%>
                <%--<button type="button" class="btn btn-primary" id="month">本月</button>--%>
                <%----%>
                <%--<button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;--%>
                <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button>
                <br/>
            </form>

        </div>
    </div>
    <br/>
    <br/>

    <br/>
    <!-- 每日报表 -->
    <div role="tabpanel" class="tab-pane" id="orderReport">
        <div class="panel panel-primary" style="border-color:write;">
            <!-- 品牌订单 -->
            <div class="panel panel-info">
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">会员信息表</strong>
                </div>
                <div class="panel-body">
                    <table id="brandReportTable" class="table table-striped table-bordered table-hover"
                           width="100%"></table>
                    <br/>
                    <table id="shopReportTable" class="table table-striped table-bordered table-hover"
                           width="100%"></table>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="reportModal" tabindex="-1" role="dialog" data-backdrop="static">
        <div class="modal-dialog modal-full">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"
                            @click="closeModal"></button>
                </div>
                <div class="modal-body" id="reportModal1"></div>
                <div class="modal-footer">
                    <!--                         <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button> -->
                    <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true"
                            @click="closeModal">关闭
                    </button>
                </div>
            </div>
            <!-- /.modal-content -->
        </div>
        <!-- /.modal-dialog -->
    </div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
    //时间插件
    $('.form_datetime').datetimepicker({
        endDate: new Date(),
        minView: "month",
        maxView: "month",
        autoclose: true,//选择后自动关闭时间选择器
        todayBtn: true,//在底部显示 当天日期
        todayHighlight: true,//高亮当前日期
        format: "yyyy-mm-dd",
        startView: "month",
        language: "zh-CN"
    });

    //文本框默认值
    $('.form_datetime').val(new Date().format("yyyy-MM-dd"));

    var beginDate = $("#beginDate").val();
    var endDate = $("#endDate").val();
    var dataSource;
    var customerId;
    $.ajax({
        url: 'member/userList',
        async: false,
        data: {
            'beginDate': beginDate,
            'endDate': endDate
        },
        success: function (data) {
            dataSource = data;
        },
        error: function () {
            toastr.error("系统异常请重新刷新");
        }
    });

    var tb1 = $("#shopReportTable").DataTable({
        data: dataSource.data,
        bSort: false,
        columns: [
            {
                title: "用户类型",
                data: "isBindPhone",
                createdCell: function (td, tdData) {
                    if (tdData == 1) {
                        $(td).html("会员")
                    }else{
                        $(td).html("非会员")
                    }
                }
            },
            {
                title: "昵称",
                data: "nickname",
            },
            {
                title: "头像",
                data: "head_photo",
                defaultContent: "",
                createdCell: function (td, tdData) {
                    if (tdData != null && tdData.substring(0, 4) == "http") {
                        $(td).html("<img src=\"" + tdData + "\" class=\"img-rounded\" onerror=\"this.src='assets/pages/img/defaultImg.png'\" style=\"height:40px;width:80px;\"/>");
                    } else {
                        $(td).html("<img src=\"/" + tdData + "\" class=\"img-rounded\" onerror=\"this.src='assets/pages/img/defaultImg.png'\" style=\"height:40px;width:80px;\"/>");
                    }
                }
            },
            {
                title: "性别",
                data: "sex",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("--")
                    } else if (tdData == 1) {
                        $(td).html("男")
                    } else if (tdData == 2) {
                        $(td).html("女")
                    }
                }
            },
            {
                title: "手机号码",
                data: "telephone",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("--")
                    }
                }
            },
            /* {
             title : "生日",
             data : "birthday",
             createdCell:function (td,tdData) {
             if(tdData==null || tdData==""){
             $(td).html("--")
             }
             }
             },       */

            /* {
             title : "星座",
             data : "constellation",
             createdCell:function (td,tdData) {
             if(tdData==null || tdData==""){
             $(td).html("--")
             }
             }
             },  */
            {
                title: "省/市",
                data: "province",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("--")
                    }
                }
            },
            {
                title: "城/区",
                data: "city",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("--")
                    }
                }
            },
            {
                title: "账户余额",
                data: "remain",
                defaultContent: '0'
            },
            {
                title: "优惠券",
                data: "customerId",
                createdCell: function (td, tdData, rowData) {
                    var button = $("<button class='btn green'>查看详情</button>");
                    button.click(function () {
                        /* $("#reportModal").modal('show'); */
                        openModal1(tdData);
                    })
                    $(td).html(button);
                }
            },
            {
                title: "订单总额",
                data: "sumMoney",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("0")
                    }
                }
            },
            {
                title: "订单总数",
                data: "amount",
                defaultContent: '0'
            },
            {
                title: "订单平均金额",
                data: "money",
                createdCell: function (td, tdData) {
                    if (tdData == null || tdData == "") {
                        $(td).html("0")
                    }
                }
            },
            {
                title: "订单记录",
                data: "customerId",
                createdCell: function (td, tdData, rowData) {
                     ;
                    var button = $("<button class='btn green'>查看详情</button>");
                    button.click(function () {
                        $("#reportModal").modal('show');
                        /* this.openModal("member/show/orderReport", customerId); */
                        openModal(beginDate, endDate, tdData);
                    })
                    $(td).html(button);
                }
            },
        ]

    });


    //查询
    $("#searchReport").click(function () {
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        searchInfo(beginDate, endDate);
    })

    //今日

    $("#today").click(function () {
        date = new Date().format("yyyy-MM-dd");
        beginDate = date;
        endDate = date;
        searchInfo(beginDate, endDate);
    });

    //昨日
    $("#yesterDay").click(function () {
        beginDate = GetDateStr(-1);
        endDate = GetDateStr(-1);
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate, endDate);

    });


    //本周
    $("#week").click(function () {
        beginDate = getWeekStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate, endDate);

    });


    //关闭页面
    $("#closeModal").click(function (e) {
        e.stopPropagation();
        var modal = $("#reportModal");
        modal.find(".modal-body").html("");
        modal.modal({show: false});
    });

    //本月
    $("#month").click(function () {
        beginDate = getMonthStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate, endDate);

    });


    function searchInfo(beginDate, endDate) {
        //更新数据源
        $.ajax({
            url: 'member/userList',
            data: {
                'beginDate': beginDate,
                'endDate': endDate
            },
            success: function (result) {
                dataSource = result;
                tb1.clear().draw();
                tb1.rows.add(result.data).draw();
                toastr.success('查询成功');
            },
            error: function () {
                toastr.error("系统异常请重新刷新");
            }
        });
    }

    function openModal(beginDate, endDate, customerId) {
        //更新数据源
        $.ajax({
            url: 'member/show/orderReport',
            data: {
                'beginDate': beginDate,
                'endDate': endDate,
                'customerId': customerId
            },
            success: function (result) {
                var modal = $("#reportModal");
                modal.find(".modal-body").html(result);
                /* 			modal.find(".modal-title > strong").html(modalTitle);*/
                modal.modal()
            },
            error: function () {
                toastr.error("系统异常请重新刷新");
            }
        });
    }

    function openModal1(customerId) {
        //更新数据源
        $.ajax({
            url: 'member/show/billReport',
            data: {
                'customerId': customerId
            },
            success: function (result) {
//                console.log(result);
                var modal = $("#reportModal");
                modal.find(".modal-body").html(result);
                /* modal.find(".modal-title > strong").html(modalTitle); */
                modal.modal()
            },
            error: function () {
                toastr.error("系统异常请重新刷新");
            }
        });
    }


    //导出会员信息数据
    $("#brandreportExcel").click(function () {
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        location.href = "member/member_excel?beginDate=" + beginDate + "&&endDate=" + endDate;

    })

    //导出店铺数据
    /*
     $("#shopreportExcel").click(function(){
     beginDate=$("#beginDate").val();
     endDate = $("#endDate").val();
     location.href="totalIncome/shopExprotExcel?beginDate="+beginDate+"&&endDate="+endDate;
     }) */

</script>
