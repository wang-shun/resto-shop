<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki"> 新建员工</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" action="{{m.id?'employee/modify':'employee/create'}}" @submit.prevent="save">
						<div class="form-body">
						<div class="form-group">
                            <label>员工姓名</label>
                            <input type="text" class="form-control" name="name" v-model="m.name">
                        </div>

                        <div class="form-group">
                            <label>员工性别</label>
                            <div class="radio-list">
                                <label class="radio-inline">
                                  <input type="radio" name="sex" v-model="m.sex" value="男"> 男
                                </label>
                                <label class="radio-inline">
                                  <input type="radio" name="sex" v-model="m.sex" value="女"> 女
                                </label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>手机号</label>
                            <input type="text" class="form-control" name="telephone" v-model="m.telephone">
                        </div>

                        <div class="form-group">
                            <label>额度</label>
                            <input type="text" class="form-control" name="money" v-model="m.money">
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
			<s:hasPermission name="employee/add">
			<button class="btn green pull-right" @click="create">新增员工</button>
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
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "employee/list_all",
				dataSrc : ""
			},
			columns : [

			    {
                    title : "姓名",
                    data : "name",
                },
                {
                    title : "性别",
                    data : "sex",
                },
				{                 
                    title : "手机号",
                    data : "telephone",
                },

               // {
                   // title : "createUser",
                   // data : "createUser",
                //},
               // {
                 //   title : "lastLoginTime",
                   // data : "lastLoginTime",
                //},
               // {
                 //   title : "updateUser",
                   // data : "updateUser",
                //},

                //{
                  //  title : "state",
                   // data : "state",
                //},
                {
                    title : "额度",
                    data : "money",
                },
                {
                    title : "二维码",
                    data : "qrCode",
                },

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="employee/delete">
							C.createDelBtn(tdData,"employee/delete"),
							</s:hasPermission>
							<s:hasPermission name="employee/modify">
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
			mixins:[C.formVueMix]
		});
		C.vue=vueObj;
	}());
	
	

	
</script>
