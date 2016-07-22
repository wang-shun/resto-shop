<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-12">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki">新增店铺介绍</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form" class="form-horizontal" action="{{m.id?'advert/modify':'advert/create'}}" @submit.prevent="save">
						<div class="form-body" style="overflow-y: hidden; overflow-x: hidden;" >
							<div class="form-group">
			           			<label class="col-sm-2 control-label">标题：</label>
							    <div class="col-sm-8">
									<input type="text" class="form-control" required name="slogan" v-model="m.slogan">
							    </div>
							</div>
							<div class="form-group " >
			           			<label class="col-sm-2 control-label">描述：</label>
							    <div class="col-sm-8" >
							    	<textarea :id="randomId" name="description" v-model="m.description"
											    ></textarea>
							    </div>
							</div>
							<div class="validataMsg" ></div>

						</div>
						<div class="text-center">
							<input type="hidden" name="id" v-model="m.id" />
							<input class="btn green"  type="submit"  value="保存"/>
							<a class="btn default" @click="cancel" >取消</a>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>

	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="advert/add">
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

<div class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title text-center"><strong></strong></h4>
      </div>
      <div class="modal-body">
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
      </div>
    </div>
  </div>
</div>
<script>

	(function(){
		
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "advert/list_all",
				dataSrc : ""
			},
			columns : [
				{                 
					title : "标题",
					data : "slogan",
				},             
				{                 
					title : "详情",
					defaultContent:"",
					createdCell:function(td,tdData,rowData){
						var button = $("<button class='btn green'>详情</button>");
						button.click(function(){
							showDetails(rowData);
						})
						$(td).html(button);
					}
				},                  
				{                 
					title : "状态",
					data : "state",
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="advert/delete">
							C.createDelBtn(tdData,"advert/delete"),
							</s:hasPermission>
							<s:hasPermission name="advert/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var ue=null;
		var C = new Controller(null,tb);
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			data:function(){
				return{
					randomId:"ueditor",
				}
			},
			methods:{
				openForm:function(){
					this.showform = true;
					var that = this;
					this.randomId = new Date().getTime()+"-ueditor";
					Vue.nextTick(function(){
						var ta = $(that.$el).find("textarea");
						var id = ta.attr("id");
						ue = UE.getEditor(id);
						$(".validataMsg").html("");
					});
				},
				closeForm:function(){
					this.m={};
					this.showform = false;
					ue.setContent("");
					$(".validataMsg").html("");
				},
				cancel:function(){
					this.m={};
					this.closeForm();
					ue.setContent("");
					$(".validataMsg").html("");
				},
				create:function(){
					this.m={};
					this.openForm();
					ue.setContent("");
					$(".validataMsg").html("");
				},
				edit:function(model){
					this.m= model;
					this.openForm();
					$(".validataMsg").html("");
					this.randomId = new Date().getTime()+"-ueditor";
					Vue.nextTick(function(){
						var ta = $(that.$el).find("textarea");
						var id = ta.attr("id");
						ue = UE.getEditor(id);
					});
				},
				save:function(e){
					if(ue.getContent().length<=0){
						if($(".validataMsg").html().length<=0){
							$(".validataMsg").append("<p class='text-danger text-center'><strong>店铺描述不能为空！</strong></p>");	
						}
						return;
					}
// 					$("[name='description']").val(ue.getContent());
					var that = this;
					var formDom = e.target;
					C.ajaxFormEx(formDom,function(){
						that.cancel();
						tb.ajax.reload();
					});
				}
			}
		});
		C.vue=vueObj;
	}());
	
	//用于显示描述详情
	function showDetails(obj){
 		$(".modal-title > strong").html(obj.slogan);
 		$(".modal-body").html(obj.description);
		$(".modal").modal();
	}
	
</script>
