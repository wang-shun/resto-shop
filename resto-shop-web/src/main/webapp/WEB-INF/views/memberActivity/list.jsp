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
	            	<form role="form"  action="{{m.id?'memberActivity/modify':'memberActivity/create'}}" @submit.prevent="save">
						< class="form-body">
							<div class="form-group">
						    <label>活动名称</label>
						    <input type="text" class="form-control" name="name" v-model="m.name" required="required">
						</div>
						<div class="form-group">
						    <label>活动折扣(0-1之间)</label>
						    <input type="text" class="form-control" name="discount" v-model="m.discount" required="required" >
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label">是否开启：</label>
							<div class="col-sm-6 radio-list">
								<label class="radio-inline">
									<input type="radio" name="type" v-model="m.type" value="0"> 不开启
								</label>
								<label class="radio-inline">
									<input type="radio" name="type" v-model="m.type" value="1" checked> 开启
								</label>
							</div>
						</div>
						<input type="hidden" name="id" v-model="m.id" />
						<input class="btn green"  type="submit"  value="保存" id="saveBrandUser" >
						<a class="btn default" @click="cancel" >取消</a>
					</form>
	            </div>
	        </div>
		</div>
	</div>
	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="memberActivity/add">
			<button class="btn green pull-right" @click="create">添加活动</button>
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
				url : "memberActivity/list_all",
				dataSrc : ""
			},
			columns : [
                {
                    title : "活动名称",
                    data : "name",
		    	},
                {
                    title : "活动折扣",
                    data : "discount",
                },
                {
                    title : "是否开启",
                    data : "type",
                },
                {
                    title : "创建时间",
                    data : "createTime",
                },
                {
                    title : "修改时间",
                    data : "updateTime",
                },
                {
                    title:"操作",
                    data:"id",
                    createdCell:function (td,tdData,rowData,row) {
                        var operator=[
                            <s:hasPermission name="memberActivity/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }
			],
		});
		
	 var C = new Controller(null,tb);
		 var vueObj = new Vue({
			el:"#control",
			data:{
                showform: false,
			},
			mixins:[C.formVueMix],
			methods:{
				 create:function(){
                     this.showform = true;
				},
                edit:function (model) {

                }
			}
			
		});
		
		 C.vue=vueObj; 
	}());
	
</script>
