<%@ page language="java" pageEncoding="utf-8"%>
<div id="control">
	<div class="row form-div" v-show="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki">新建备注</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" class="form-horizontal" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
			           			<label class="col-sm-3 control-label">备注名称：</label>
							    <div class="col-sm-8">
						    		<input type="text" class="form-control" maxlength="50" placeholder="建议输入五十个字以内" required v-model="orderRemark.remarkName">
							    </div>
							</div>
							<div class="form-group">
			           			<label class="col-sm-3 control-label">排序：</label>
							    <div class="col-sm-8">
							    	<input type="number" class="form-control"placeholder="建议输入正整数" required v-model="orderRemark.sort" min="1">
							    </div>
							</div>
                            <div class="form-group">
                                <label class="col-md-3 control-label">是否启用：</label>
                                <div class="col-sm-8">
                                    <label class="radio-inline">
                                        <input type="radio" name="state" v-model="orderRemark.state" value="1"> 启用
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" name="state" v-model="orderRemark.state" value="0"> 不启用
                                    </label>
                                </div>
                            </div>
							<div class="text-center">
								<input type="hidden" name="id"/>
								<input class="btn green" type="submit" value="保存"/>
								<a class="btn default" @click="closeShowForm">取消</a>
							</div>
						</div>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<button class="btn green pull-right" @click="createOrderRemark">新建</button>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter"></div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered" id = "orderRemarkTable"></table>
		</div>
	</div>
</div>


<script>
    var vueObj = new Vue({
        el : "#control",
        data : {
            orderRemarkTable : {},
            orderRemark : {state : 1},
            showform : false
        },
        created : function() {
            this.initDataTables();
            this.searchInfo();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.orderRemarkTable = $("#orderRemarkTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 2, 'asc' ]],
                    columns : [
                        {
                            title : "备注名称",
                            data : "shopName",
                            orderable : false
                        },
                        {
                            title : "充值活动",
                            data : "chargeName",
                            orderable : false,
                        },
                        {
                            title : "充值分红比例",
                            data : "chargeBonusRatio"
                        },
                        {
                            title : "店长分红比例",
                            data : "shopownerBonusRatio"
                        },
                        {
                            title : "员工分红比例",
                            data : "employeeBonusRatio"
                        },
                        {
                            title : "启用分红",
                            data : "state",
                            createdCell: function (td, tdData) {
                                var state = "";
                                if (tdData == 0){
                                    state = "<span class='label label-danger'>未启用</span>";
                                }else {
                                    state = "<span class='label label-primary'>启用</span>";
                                }
                                $(td).html(state);
                            }
                        },
                        {
                            title : "操作",
                            data : "id",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var updateButton = $("<button class='btn btn-info btn-sm'>设置</button>");
                                updateButton.click(function () {
                                });
                                var operator = [updateButton];
                                $(td).html(operator);
                            }
                        }
                    ]
                });
            },
            searchInfo : function() {
                toastr.clear();
                toastr.success("查询中...");
                var that = this;
                try{
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            save : function () {
                var that = this;
            }
        }
    });

    function Trim(str)
    {
        return str.replace(/(^\s*)|(\s*$)/g, "");
    }
</script>
