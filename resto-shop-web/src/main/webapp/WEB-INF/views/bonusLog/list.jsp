<%@ page language="java" pageEncoding="utf-8"%>
<div id="control">
    <%--<div class="row form-div" v-if="showform">--%>
        <%--<div class="col-md-offset-3 col-md-6" >--%>
            <%--<div class="portlet light bordered">--%>
                <%--<div class="portlet-title">--%>
                    <%--<div class="caption">--%>
                        <%--<span class="caption-subject bold font-blue-hoki">分红设置</span>--%>
                    <%--</div>--%>
                <%--</div>--%>

                <%--<div class="portlet-body">--%>
                    <%--<form role="form" class="form-horizontal" @submit.prevent="save">--%>
                        <%--<input type="hidden" name="id" v-model="bonusLog.id"/>--%>
                        <%--<div class="form-body">--%>
                            <%--<div class="form-group">--%>
                                <%--<label  class="col-sm-2 control-label">分红比例：</label>--%>
                                <%--<div class="col-sm-8">--%>
                                    <%--<div class="input-group">--%>
                                        <%--<input class="form-control" type="number" name="chargeBonusRatio" @keyup="setChargeBonusRatio" v-model="bonusLog.chargeBonusRatio" min="0"  max="100" required placeholder="请输入1-100整数值">--%>
                                        <%--<div class="input-group-addon">%</div>--%>
                                    <%--</div>--%>
                                    <%--<span class="help-block">请输入0-100整数值</span>--%>
                                <%--</div>--%>
                            <%--</div>--%>
                            <%--<div class="form-group">--%>
                                <%--<label  class="col-sm-2 control-label">店长分红：</label>--%>
                                <%--<div class="col-sm-8">--%>
                                    <%--<div class="input-group">--%>
                                        <%--<input class="form-control" type="number" name="shopownerBonusRatio" @keyup="setEmployeeBonusRatio" @change="setEmployeeBonusRatio" v-model="bonusLog.shopownerBonusRatio" min="0"  max="100" required placeholder="请输入1-100整数值">--%>
                                        <%--<div class="input-group-addon">%</div>--%>
                                    <%--</div>--%>
                                    <%--<span class="help-block">请输入0-100整数值</span>--%>
                                <%--</div>--%>
                            <%--</div>--%>
                            <%--<div class="form-group">--%>
                                <%--<label  class="col-sm-2 control-label">员工分红：</label>--%>
                                <%--<div class="col-sm-8">--%>
                                    <%--<div class="input-group">--%>
                                        <%--<input class="form-control" type="number" name="employeeBonusRatio" @keyup="setShopownerBonusRatio" @change="setShopownerBonusRatio" v-model="bonusLog.employeeBonusRatio" min="0"  max="100" required placeholder="请输入1-100整数值">--%>
                                        <%--<div class="input-group-addon">%</div>--%>
                                    <%--</div>--%>
                                    <%--<span class="help-block">请输入0-100整数值</span>--%>
                                <%--</div>--%>
                            <%--</div>--%>
                            <%--<div class="form-group">--%>
                                <%--<label class="col-md-2 control-label">是否启用：</label>--%>
                                <%--<div  class="col-md-8">--%>
                                    <%--<label class="radio-inline">--%>
                                        <%--<input type="radio" name="state" value="1" v-model="bonusLog.state"> 启用--%>
                                    <%--</label>--%>
                                    <%--<label class="radio-inline">--%>
                                        <%--<input type="radio" name="state" value="0" v-model="bonusLog.state"> 不启用--%>
                                    <%--</label>--%>
                                <%--</div>--%>
                            <%--</div>--%>
                        <%--</div>--%>
                        <%--<div class="form-group text-center">--%>
                            <%--<input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;--%>
                            <%--<a class="btn default" @click="colseShowForm" >取消</a>--%>
                        <%--</div>--%>
                    <%--</form>--%>
                <%--</div>--%>
            <%--</div>--%>
        <%--</div>--%>
    <%--</div>--%>
	
	<div class="table-div">
		<div class="table-operator">
		</div>
		<div class="clearfix"></div>
		<div class="table-filter"></div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered" id = "bonusLogTable"></table>
		</div>
	</div>
</div>


<script>
    var bonusTableAPI;
    var vueObj = new Vue({
        el : "#control",
        data : {
            showform : false,
            bonusLogTable : {},
            bonusLog : {}
        },
        created : function() {
            this.initDataTables();
            this.searchInfo();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.bonusLogTable = $("#bonusLogTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 0, 'asc' ]],
                    columns : [
                        {
                            title : "充值时间",
                            data : "chargeTime"
                        },
                        {
                            title : "店铺",
                            data : "shopName",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "充值方式",
                            data : "chargeType",
                            orderable : false,
                            s_filter: true
                        },
                        {
                            title : "手机号",
                            data : "telephone",
                            orderable : false
                        },
                        {
                            title : "充值金额",
                            data : "chargeMoney"
                        },
                        {
                            title : "分红金额",
                            data : "bonusMoney"
                        },
                        {
                            title : "状态",
                            data : "stateValue",
                            orderable : false,
                            s_filter: true,
                            createdCell: function (td, tdData) {
                                var state;
                                if (tdData == "已分红"){
                                    state = "<span class='label label-success'>"+tdData+"</span>";
                                }else if (tdData == "未分红"){
                                    state = "<span class='label label-danger'>"+tdData+"</span>";
                                }else{
                                    state = "<span class='label label-primary'>"+tdData+"</span>";
                                }
                                $(td).html(state);
                            }
                        },
                        {
                            title : "操作",
                            data : "id",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var state = rowData.stateValue;
                                var operatorButton = (state == "已分红" ? $("<button class='btn btn-primary btn-sm'>查看</button>") : $("<button class='btn btn-success btn-sm'>分红</button>"));
                                operatorButton.click(function () {
                                    that.operatorBonusLog(tdData);
                                });
                                var operator = [operatorButton];
                                $(td).html(operator);
                            }
                        }
                    ],
                    initComplete: function () {
                        bonusTableAPI = this.api();
                        that.bonusTable();
                    }
                });
            },
            searchInfo : function() {
                toastr.clear();
                toastr.success("查询中...");
                var that = this;
                try{
                    $.post("bonusLog/list_all",function (result) {
                        if (result.success){
                            var api = bonusTableAPI;
                            api.search('');
                            var column1 = api.column(1);
                            column1.search('', true, false);
                            var column2 = api.column(2);
                            column2.search('', true, false);
                            var column6 = api.column(6);
                            column6.search('', true, false);
                            that.bonusLogTable.clear();
                            that.bonusLogTable.rows.add(result.data).draw();
                            that.bonusTable();
                            toastr.clear();
                            toastr.success("查询成功");
                        } else{
                            toastr.clear();
                            toastr.error("网络异常，请刷新重试");
                        }
                    });
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            save : function () {
                toastr.clear();
                var that = this;
                try{
//                    $.post("",that.bonusLog,function (result) {
//                        if (result.success){
//                            that.colseShowForm();
//                            that.searchInfo();
//                        } else{
//                            toastr.error("网络异常，请刷新重试");
//                        }
//                    });
                }catch(e){
                    toastr.error("系统异常，请刷新重试");
                }
            },
            colseShowForm : function () {
                this.showform = false;
            },
            operatorBonusLog : function () {

            },
            bonusTable : function () {
                var api = bonusTableAPI;
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
