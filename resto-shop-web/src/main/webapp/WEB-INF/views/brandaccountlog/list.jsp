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
					<form role="form" action="{{m.id?'brandaccountlog/modify':'brandaccountlog/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
								<label>money</label>
								<input type="text" class="form-control" name="money" v-model="m.money">
							</div>
							<div class="form-group">
								<label>groupName</label>
								<input type="text" class="form-control" name="groupName" v-model="m.groupName">
							</div>
							<div class="form-group">
								<label>behavior</label>
								<input type="text" class="form-control" name="behavior" v-model="m.behavior">
							</div>
							<div class="form-group">
								<label>foundChange</label>
								<input type="text" class="form-control" name="foundChange" v-model="m.foundChange">
							</div>
							<div class="form-group">
								<label>remain</label>
								<input type="text" class="form-control" name="remain" v-model="m.remain">
							</div>
							<div class="form-group">
								<label>detail</label>
								<input type="text" class="form-control" name="detail" v-model="m.detail">
							</div>
							<div class="form-group">
								<label>accountId</label>
								<input type="text" class="form-control" name="accountId" v-model="m.accountId">
							</div>
							<div class="form-group">
								<label>brandId</label>
								<input type="text" class="form-control" name="brandId" v-model="m.brandId">
							</div>
							<div class="form-group">
								<label>shopId</label>
								<input type="text" class="form-control" name="shopId" v-model="m.shopId">
							</div>
							<div class="form-group">
								<label>serialNumber</label>
								<input type="text" class="form-control" name="serialNumber" v-model="m.serialNumber">
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

	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="brandaccountlog/add">
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
                url : "brandaccountlog/list_all",
                dataSrc : ""
            },
            columns : [
				{
				  title:"时间",
				  data:"createTime",
				  createdCell:function (td,tdData) {
					  $(td).html(vueObj.formatDate(tdData))
                  }
				},
                {
                    title : "主题V",
                    data : "groupName"
                },
                {
                    title : "行为V",
                    data : "behavior",
					createdCell:function (td,tdData) {
						$(td).html(vueObj.BehaviorName(tdData))
                    }
                },
                {
                    title : "流水号",
                    data : "serialNumber"
                },
                {
                    title : "详情V",
                    data : "detail",
					createdCell:function (td,tdData) {
						$(td).html(vueObj.DetailName(tdData))
                    }
                },

                {
                    title : "资金变动",
                    data : "foundChange",
					createdCell:function (td,tdData) {
                        var temp = tdData;
						if(tdData>0){
						    temp = "+"+tdData;
						}
						$(td).html(temp);
                    }
                },
                {
                    title : "余额(￥)",
                    data : "remain",
                }
                <%--{--%>
                    <%--title : "操作",--%>
                    <%--data : "id",--%>
                    <%--createdCell:function(td,tdData,rowData,row){--%>
                        <%--var operator=[--%>
                            <%--<s:hasPermission name="brandaccountlog/delete">--%>
                            <%--C.createDelBtn(tdData,"brandaccountlog/delete"),--%>
                            <%--</s:hasPermission>--%>
                            <%--<s:hasPermission name="brandaccountlog/modify">--%>
                            <%--C.createEditBtn(rowData),--%>
                            <%--</s:hasPermission>--%>
                        <%--];--%>
                        <%--$(td).html(operator);--%>
                    <%--}--%>
                <%--}--%>
                ],
        });

        var C = new Controller(null,tb);
        var vueObj = new Vue({
            el:"#control",
            mixins:[C.formVueMix],
			methods:{
                formatDate:function (date) {
                    var temp = "";
                    if (date != null && date != "") {
                        temp = new Date(date);
                        temp = temp.format("yyyy-MM-dd hh:mm:ss");
                    }
                    return temp;
                },
				BehaviorName:function (data) {
                    var temp = "未知";
                    switch (data){
                        case 10:
                            temp = "注册";
                            break;
                        case 20:
                            temp = "消费";
                            break;
                        case 30:
                            temp = "短信";
                            break;
                        case 40:
                            temp = "充值";
                            break;
                        default:
                            break;
                    }
                    return  temp;
                },
				DetailName:function (data) {
                    var temp = "未知";
                    switch (data){
                        case 10:
                            temp = "新用户注册";
                            break;
                        case 20:
                            temp = "新用户消费";
                            break;
                        case 21:
                            temp = "回头用户消费";
                            break;
                        case 30:
                            temp = "注册验证吗";
                            break;
                        case 40:
                            temp = "账户充值";
                            break;
                        default:
                            break;
                    }
                    return  temp;
                }



			}
        });
        C.vue=vueObj;
    }());




</script>
