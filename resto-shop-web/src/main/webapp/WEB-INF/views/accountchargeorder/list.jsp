<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki"> 表单</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" action="{{m.id?'accountchargeorder/modify':'accountchargeorder/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
                            <label>brandId</label>
                            <input type="text" class="form-control" name="brandId" v-model="m.brandId">
                        </div>
                        <div class="form-group">
                            <label>orderStatus</label>
                            <input type="text" class="form-control" name="orderStatus" v-model="m.orderStatus">
                        </div>
                        <div class="form-group">
                            <label>pushOrderTime</label>
                            <input type="text" class="form-control" name="pushOrderTime" v-model="m.pushOrderTime">
                        </div>
                        <div class="form-group">
                            <label>chargeMoney</label>
                            <input type="text" class="form-control" name="chargeMoney" v-model="m.chargeMoney">
                        </div>
                        <div class="form-group">
                            <label>tradeNo</label>
                            <input type="text" class="form-control" name="tradeNo" v-model="m.tradeNo">
                        </div>
                        <div class="form-group">
                            <label>payType</label>
                            <input type="text" class="form-control" name="payType" v-model="m.payType">
                        </div>
                        <div class="form-group">
                            <label>remark</label>
                            <input type="text" class="form-control" name="remark" v-model="m.remark">
                        </div>
                        <div class="form-group">
                            <label>status</label>
                            <input type="text" class="form-control" name="status" v-model="m.status">
                        </div>

						</div>
						<input type="hidden" name="id" v-model="m.id" />
						<input class="btn green"  type="submit"  value="保存"/>
						<a class="btn default" @click="cancel" >取消</a>
					</form>
	            </div>
	        </div>
		</div>
	</div>

	<div>

        <form role="form" class="form-horizontal"
              action="accountchargeorder/charge" method="post" target="_blank" @submit="showChargeModal('createChargeOrder')">
            <div class="form-body">

                <div class="form-group">
                    <label class="col-sm-3 control-label">充值金额：</label>
                    <div class="col-sm-4">
                        <div class="input-group">
                            <input type="text" class="form-control"
                                   placeholder="请输入要充值的金额"  name="chargeMoney"  v-model="chargeMoney">
                            <div class="input-group-addon"><span class="glyphicon glyphicon-yen"></span></div>
                        </div>
                    </div>
                    <div class="col-sm-5 text-center">
                        <a class="btn btn-info" @click="changeMoney(500)">500</a>&nbsp;
                        <a class="btn btn-info" @click="changeMoney(1000)">1000</a>&nbsp;
                        <a class="btn btn-info" @click="changeMoney(2000)">2000</a>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label">支付方式：</label>
                    <div class="col-sm-8">
                        <div class="md-radio-list">
                            <div class="md-radio">
                                <input type="radio" id="alipay" name="payType"
                                       checked="checked" class="md-radiobtn" value="1"> <label
                                    for="alipay"> <span></span> <span class="check"></span>
                                <span class="box"></span>&nbsp;<img alt="支付宝支付"
                                                                    src="assets/pages/img/alipay.png" width="23px" height="23px">&nbsp;支付宝支付
                            </label>
                            </div>
                            <div class="md-radio">
                                <input type="radio" id="wxpay" name="payType"
                                       class="md-radiobtn" value="2"> <label for="wxpay">
                                <span></span> <span class="check"></span> <span class="box"></span>&nbsp;<img
                                    alt="微信支付" src="assets/pages/img/wxpay.png" width="23px"
                                    height="23px">&nbsp;微信支付
                            </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="text-center">
                <a class="btn default" data-dismiss="modal">取消</a> <input
                    class="btn green" type="submit" value="充值"/>
            </div>
        </form>


    </div>


    <div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="accountchargeorder/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
</div>


<script>
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "accountchargeorder/list_all",
				dataSrc : ""
			},
			columns : [
				{
                    title : "创建时间",
                    data : "createTime",
                    createdCell:function (td,tdData) {
                        $(td).html(vueObj.formatDate(tdData));
                    }
                },

                {
                    title : "充值金额",
                    data : "chargeMoney",
                },
                {
                    title : "交易号",
                    data : "tradeNo",
                },
                {
                    title : "支付方式",
                    data : "payType",
                },

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="accountchargeorder/delete">
							C.createDelBtn(tdData,"accountchargeorder/delete"),
							</s:hasPermission>
							<s:hasPermission name="accountchargeorder/modify">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(null,tb);
		var vueObj = new Vue({
			el:"#control",
			data:{
			    order:{
			        chargeMoney:0,
			        payType:''
			    }
			},
			mixins:[C.formVueMix],
			methods:{
                showChargeModal:function (createChargeOrder) {

                },
                //格式化时间
                formatDate:function (date) {
                    var temp = "";
                    if (date != null && date != "") {
                        temp = new Date(date);
                        temp = temp.format("yyyy-MM-dd hh:mm:ss");
                    }
                    return temp;
                }
			}
		});
		C.vue=vueObj;
	}());
	
	

	
</script>
