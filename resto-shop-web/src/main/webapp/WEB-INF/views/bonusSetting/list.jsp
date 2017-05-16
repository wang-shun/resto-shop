<%@ page language="java" pageEncoding="utf-8"%>
<div id="control">
    <div class="row form-div" v-if="showform">
        <div class="col-md-offset-3 col-md-6" >
            <div class="portlet light bordered">
                <div class="portlet-title">
                    <div class="caption">
                        <span class="caption-subject bold font-blue-hoki">分红设置</span>
                    </div>
                </div>

                <div class="portlet-body">
                    <form role="form" class="form-horizontal" @submit.prevent="save">
                        <input type="hidden" name="id" v-model="bonusSetting.id"/>
                        <div class="form-body">
                            <div class="form-group" v-show="bonusSetting.id == null">
                                <label  class="col-sm-2 control-label">充值活动：</label>
                                <div class="col-sm-8">
                                    <select class="form-control" v-model="bonusSetting.chargeSettingId">
                                        <option value="0">全部活动</option>
                                        <option value="{{chargeSetting.id + ':' + chargeSetting.shopDetailId}}" v-for="chargeSetting in chargeSettings">
                                            {{chargeSetting.labelText}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label  class="col-sm-2 control-label">分红比例：</label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <input class="form-control" type="number" name="chargeBonusRatio" v-model="bonusSetting.chargeBonusRatio" min="1"  max="100" required placeholder="请输入1-100整数值">
                                        <div class="input-group-addon">%</div>
                                    </div>
                                    <span class="help-block">请输入1-100整数值</span>
                                </div>
                            </div>
                            <div class="form-group">
                                <label  class="col-sm-2 control-label">店长分红：</label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <input class="form-control" type="number" name="shopownerBonusRatio" v-model="bonusSetting.shopownerBonusRatio" min="0"  max="100" required placeholder="请输入1-100整数值">
                                        <div class="input-group-addon">%</div>
                                    </div>
                                    <span class="help-block">请输入0-100整数值</span>
                                </div>
                            </div>
                            <div class="form-group">
                                <label  class="col-sm-2 control-label">员工分红：</label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <input class="form-control" type="number" name="employeeBonusRatio" v-model="bonusSetting.employeeBonusRatio" min="0"  max="100" required placeholder="请输入1-100整数值">
                                        <div class="input-group-addon">%</div>
                                    </div>
                                    <span class="help-block">请输入0-100整数值</span>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-md-2 control-label">是否启用：</label>
                                <div  class="col-md-8">
                                    <label class="radio-inline">
                                        <input type="radio" name="state" value="1" v-model="bonusSetting.state"> 启用
                                    </label>
                                    <label class="radio-inline">
                                        <input type="radio" name="state" value="0" v-model="bonusSetting.state"> 不启用
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group text-center">
                            <input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
                            <a class="btn default" @click="colseShowForm" >取消</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
	
	<div class="table-div">
		<div class="table-operator">
			<button class="btn green pull-right" @click="openShowForm">新建</button>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter"></div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered" id = "bonusSettingTable"></table>
		</div>
	</div>
</div>


<script>
    var vueObj = new Vue({
        el : "#control",
        data : {
            showform : false,
            bonusSettingTable : {},
            chargeSettings :[],
            bonusSetting : {}
        },
        created : function() {
            this.initDataTables();
            this.searchInfo();
            this.getBonusSetting();
        },
        methods : {
            initDataTables:function () {
                //that代表 vue对象
                var that = this;
                that.bonusSettingTable = $("#bonusSettingTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 2, 'asc' ]],
                    columns : [
                        {
                            title : "店铺",
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
                            data : "chargeBonusRatio",
                            createdCell: function (td, tdData) {
                                $(td).html(tdData+"%");
                            }
                        },
                        {
                            title : "店长分红比例",
                            data : "shopownerBonusRatio",
                            createdCell: function (td, tdData) {
                                $(td).html(tdData+"%");
                            }
                        },
                        {
                            title : "员工分红比例",
                            data : "employeeBonusRatio",
                            createdCell: function (td, tdData) {
                                $(td).html(tdData+"%");
                            }
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
                                    that.updateBonusSetting(rowData);
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
                    $.post("bonusSetting/list_all",function (result) {
                        if (result.success){
                            that.bonusSettingTable.clear();
                            that.bonusSettingTable.rows.add(result.data.bonusSettings).draw();
                            that.chargeSettings = result.data.chargeSettings;
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
                    var chargeId = that.bonusSetting.chargeSettingId;
                    var bonusRatio = parseInt(that.bonusSetting.shopownerBonusRatio) + parseInt(that.bonusSetting.employeeBonusRatio);
                    if (chargeId == "0"){
                        toastr.error("请选择充值活动");
                        return;
                    }else if(bonusRatio > 100 || bonusRatio < 100){
                        toastr.error("店长分红比例与员工分红比例之和必须为100%");
                        return;
                    }
                    if (that.bonusSetting.id != null){
                        that.bonusSetting.createTime = new Date(that.bonusSetting.createTime);
                    }
                    $.post("bonusSetting/"+(that.bonusSetting.id != null ? "modify" : "create")+"",that.bonusSetting,function (result) {
                        if (result.success){
                            that.searchInfo();
                        } else{
                            toastr.error("网络异常，请刷新重试");
                        }
                        that.getBonusSetting();
                        that.showform = false;
                    });
                }catch(e){
                    toastr.error("系统异常，请刷新重试");
                }
            },
            openShowForm : function () {
                this.getBonusSetting();
                this.showform = true;
            },
            colseShowForm : function () {
                this.getBonusSetting();
                this.showform = false;
            },
            getBonusSetting : function () {
                this.bonusSetting = {chargeSettingId : "0", state : 1};
            },
            updateBonusSetting : function (bonusSetting) {
                this.showform = true;
                this.bonusSetting = bonusSetting;
            }
        }
    });

    function Trim(str)
    {
        return str.replace(/(^\s*)|(\s*$)/g, "");
    }
</script>
