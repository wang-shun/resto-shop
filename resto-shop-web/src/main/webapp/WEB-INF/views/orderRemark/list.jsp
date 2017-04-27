<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
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
                                        <input type="radio" name="state" v-model="orderRemark.state" value="1" checked> 启用
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
            orderRemarkList : [],
            orderRemark : {},
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
                    columns : [
                        {
                            title : "备注名称",
                            data : "remarkName",
                            orderable : false
                        },
                        {
                            title : "排序",
                            data : "sort"
                        },
                        {
                            title : "是否启用",
                            data : "state",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var state = "";
                                if (tdData == 0){
                                    state = "不启用";
                                }else {
                                    state = "启用";
                                }
                                $(td).html(state);
                            }
                        },
                        {
                            title : "操作",
                            data : "id",
                            orderable : false,
                            createdCell: function (td, tdData) {
                                var updateButton = $("<button class='btn btn-info btn-sm'>编辑</button>");
                                updateButton.click(function () {
                                    that.updateOrderRemark(tdData);
                                });
                                var deleteButton = $("<button class='btn btn-danger btn-sm'>删除</button>");
                                deleteButton.click(function () {
                                    that.deleteOrderRemark(tdData);
                                });
                                var operator = [updateButton,deleteButton];
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
                    $.post("orderRemark/selectAll",function(result){
                        if (result.success){
                            that.orderRemarkTable.clear();
                            that.orderRemarkTable.rows.add(result.data).draw();
                            that.orderRemarkList = result.data;
                            toastr.clear();
                            toastr.success("查询成功");
                            return;
                        }else {
                            toastr.clear();
                            toastr.error("查询出错");
                            return;
                        }
                    });
                }catch(e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            save : function () {
                var that = this;
                if (that.orderRemark.id != null){
                    $.post("orderRemark/update",that.orderRemark,function (result) {
                        if (result.success){
                            toastr.clear();
                            toastr.success("修改成功");
                            that.orderRemark = {};
                            that.showform = false;
                            that.searchInfo();
                        }else{
                            toastr.clear();
                            toastr.error("修改失败");
                            that.orderRemark = {};
                            that.showform = false;
                        }
                    });
                }else{
                    $.post("orderRemark/create",that.orderRemark,function (result) {
                        if (result.success){
                            toastr.clear();
                            toastr.success("新增成功");
                            that.orderRemark = {};
                            that.showform = false;
                            that.searchInfo();
                        }else{
                            toastr.clear();
                            toastr.error("新增失败");
                            that.orderRemark = {};
                            that.showform = false;
                        }
                    });
                }
            },
            createOrderRemark : function () {
                this.showform = true;
            },
            closeShowForm : function () {
                this.showform = false;
                this.orderRemark = {};
            },
            updateOrderRemark : function (orderRemarkId) {
                var that = this;
                $.post("orderRemark/selectOne",{"orderRemarkId" : orderRemarkId}, function (result) {
                    if (result.success){
                        that.orderRemark = result.data;
                        that.showform = true;
                    }else{
                        toastr.clear();
                        toastr.error("系统异常，请刷新重试");
                    }
                });
            },
            deleteOrderRemark : function (orderRemarkId) {
                var that = this;
                $.post("orderRemark/delete",{"orderRemarkId" : orderRemarkId}, function (result) {
                    if (result.success){
                        toastr.clear();
                        toastr.success("删除成功");
                    }else{
                        toastr.clear();
                        toastr.error("系统异常，请刷新重试");
                    }
                });
            }
        }
    });

    function Trim(str)
    {
        return str.replace(/(^\s*)|(\s*$)/g, "");
    }
</script>
