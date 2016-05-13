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
	            	<form role="form" action="{{m.id?'showphoto/modify':'showphoto/create'}}" @submit.prevent="save">
						<div class="form-body">
						<div class="form-group">
							<div label for="showType" class="control-label">选择图片类型</div>
							<div>
								<select class="form-control" name="showType" v-model="m.showType">
									<option v-for="typeName in typeNames" :value="typeName.id">
										{{typeName.value}}
									</option>
								</select>							
							</div>
						</div>
						
<!-- 						<div class="form-group col-md-4"> -->
<!-- 					    <label class="col-md-5 control-label">餐品类别</label> -->
<!-- 					    <div class="col-md-7"> -->
<%-- 						    <select class="form-control" name="articleFamilyId" v-model="m.articleFamilyId"> --%>
<!-- 						    	<option :value="f.id" v-for="f in articlefamilys"> -->
<!-- 						    		{{f.name}} -->
<!-- 						    	</option> -->
<%-- 						    </select> --%>
<!-- 					    </div> -->
<!-- 						</div> -->
						
						<div class="form-group">
						    <label>主题</label>
						    <input type="text" class="form-control" name="title" v-model="m.title">
						</div>
						
						<div class="form-group">
						    <label>图片地址</label>
						    <img src="" id="picUrl"/>
							<input type="hidden" name="picUrl" v-model="m.picUrl">
							<img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
											    
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
			<s:hasPermission name="showphoto/add">
			<button class="btn green pull-right" @click="create">新建</button>
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
				url : "showphoto/list_all",
				dataSrc : ""
			},
			columns : [
								{                 
					title : "展示类型",
					data : "showType",
					createdCell:function(td,tdData,rowData,row){
						var typeName;
						if(tdData==1){
							typeName='餐品图片';
						}else if(tdData==2){
							typeName='展示的图片';
						}else if(tdData==4){
							typeName='差评';
						}
						$(td).html(typeName);
					}
				},                 
				{                 
					title : "主题",
					data : "title",
				},                 
				{                 
					title : "图片地址",
					data : "picUrl",
					defaultContent:'',
					createdCell:function(td,tdData){
						$(td).html("<img src='/"+tdData+"' style='height:40px;width:80px;'/>")
					}
				},                 
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="showphoto/delete">
							C.createDelBtn(tdData,"showphoto/delete"),
							</s:hasPermission>
							<s:hasPermission name="showphoto/edit">
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
				typeNames:[{"id":"1","value":"餐品图片"},{"id":"2","value":"展示的图片"},{"id":"4","value":"差评"}],
			},
			mixins:[C.formVueMix],
			methods:{
				uploadSuccess:function(url){
					$("[name='picUrl']").val(url).trigger("change");
					C.simpleMsg("上传成功");
					$("#picUrl").attr("src","/"+url);
				},
				uploadError:function(msg){
					C.errorMsg(msg);
				},
			}
		});
		C.vue=vueObj;
	}());

</script>
