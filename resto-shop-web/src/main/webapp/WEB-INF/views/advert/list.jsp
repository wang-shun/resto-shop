<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<style>
	#editor {
		max-height: 250px;
		height: 250px;
		background-color: white;
		border-collapse: separate;
		border: 1px solid rgb(204, 204, 204);
		padding: 4px;
		box-sizing: content-box;
		-webkit-box-shadow: rgba(0, 0, 0, 0.0745098) 0px 1px 1px 0px inset;
		box-shadow: rgba(0, 0, 0, 0.0745098) 0px 1px 1px 0px inset;
		overflow: scroll;
		outline: none;
	}
</style>

<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6">
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
								<input type="hidden" id="description" name="description">
								<div class="col-sm-8" >
										<div>
											<!--		富文本工具栏	-->
											<div class="btn-toolbar" data-role="editor-toolbar" data-target="#editor">
												<div class="btn-group">
													<a class="btn dropdown-toggle" data-toggle="dropdown" title="字体大小"><i class="icon-text-height"></i>&nbsp;<b class="caret"></b></a>
													<ul class="dropdown-menu">
														<li><a data-edit="fontSize 5"><font size="5">大号</font></a></li>
														<li><a data-edit="fontSize 3"><font size="3">正常</font></a></li>
														<li><a data-edit="fontSize 1"><font size="1">小号</font></a></li>
													</ul>
												</div>
												<div class="btn-group">
													<a class="btn" data-edit="bold" title="加粗 (Ctrl/Cmd+B)"><i class="icon-bold"></i></a>
													<a class="btn" data-edit="italic" title="斜体 (Ctrl/Cmd+I)"><i class="icon-italic"></i></a>
													<a class="btn" data-edit="strikethrough" title="删除线"><i class="icon-strikethrough"></i></a>
													<a class="btn" data-edit="underline" title="下划线 (Ctrl/Cmd+U)"><i class="icon-underline"></i></a>
												</div>
												<div class="btn-group">
													<a class="btn" data-edit="insertunorderedlist" title="无序列表"><i class="icon-list-ul"></i></a>
													<a class="btn" data-edit="insertorderedlist" title="有序列表"><i class="icon-list-ol"></i></a>
													<a class="btn" data-edit="outdent" title="删除缩进(Shift+Tab)"><i class="icon-indent-left"></i></a>
													<a class="btn" data-edit="indent" title="缩进(Tab)"><i class="icon-indent-right"></i></a>
												</div>
												<div class="btn-group">
													<a class="btn" data-edit="justifyleft" title="左对齐 (Ctrl/Cmd+L)"><i class="icon-align-left"></i></a>
													<a class="btn" data-edit="justifycenter" title="居中 (Ctrl/Cmd+E)"><i class="icon-align-center"></i></a>
													<a class="btn" data-edit="justifyright" title="右对齐 (Ctrl/Cmd+R)"><i class="icon-align-right"></i></a>
													<a class="btn" data-edit="justifyfull" title="还原 (Ctrl/Cmd+J)"><i class="icon-align-justify"></i></a>
												</div>
												<!--		超链接	-->
												<%--<div class="btn-group">--%>
													<%--<a class="btn dropdown-toggle" data-toggle="dropdown" title="链接"><i class="icon-link"></i></a>--%>
													<%--<div class="dropdown-menu input-append">--%>
														<%--<input class="span2" placeholder="URL" type="text" data-edit="createLink"/>--%>
														<%--<button class="btn" type="button">Add</button>--%>
													<%--</div>--%>
													<%--<a class="btn" data-edit="unlink" title="Remove Hyperlink"><i class="icon-cut"></i></a>--%>
												<%--</div>--%>
												<div class="btn-group">
													<a class="btn" title="插入图片 (or just drag & drop)" id="pictureBtn"><i class="icon-picture"></i></a>
													<input type="file" data-role="magic-overlay" data-target="#pictureBtn" data-edit="insertImage" accept="image/*" />
												</div>
												<div class="btn-group">
													<a class="btn" data-edit="undo" title="撤销 (Ctrl/Cmd+Z)"><i class="icon-undo"></i></a>
													<a class="btn" data-edit="redo" title="取消撤销 (Ctrl/Cmd+Y)"><i class="icon-repeat"></i></a>
												</div>
												<input type="text" data-edit="inserttext" id="voiceBtn" x-webkit-speech="">
											</div>
											<!--		富文本框	-->
											<div id="editor"></div>
										</div>
								</div>
							</div>
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
			<div class="modal-body" style="word-wrap: break-word;">
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

		var C = new Controller(null,tb);
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			methods:{
				openForm:function(){
					this.showform = true;
				},
				closeForm:function(){
					this.m={};
					this.showform = false;
				},
				cancel:function(){
					this.m={};
					this.closeForm();
				},
				create:function(){
					this.m={};
					this.openForm();
					var that = this;
					Vue.nextTick(function(){
						that.initWysiwyg("");
					});
				},
				edit:function(model){
					this.m= model;
					this.openForm();
					var that = this;
					Vue.nextTick(function(){
						that.initWysiwyg(model.description);
					});
				},
				save:function(e){
					var that = this;
					var formDom = e.target;
					if($('#editor').html().length>0){
						$("#description").val($('#editor').html());
						C.ajaxFormEx(formDom,function(){
							that.cancel();
							tb.ajax.reload();
						});
					}else{
						toastr.error("描述不能为空！");
						$("#editor").focus();
					}
				},
				initWysiwyg : function(description){
					//工具栏设置		begin
					$('a[title]').tooltip({container:'body'});
					$('.dropdown-menu input').click(function() {return false;})
							.change(function () {$(this).parent('.dropdown-menu').siblings('.dropdown-toggle').dropdown('toggle');})
							.keydown('esc', function () {this.value='';$(this).change();});

					$('[data-role=magic-overlay]').each(function () {
						var overlay = $(this), target = $(overlay.data('target'));
						overlay.css('opacity', 0).css('position', 'absolute').offset(target.offset()).width(target.outerWidth()).height(target.outerHeight());
					});
					if ("onwebkitspeechchange"  in document.createElement("input")) {
						var editorOffset = $('#editor').offset();
						$('#voiceBtn').css('position','absolute').offset({top: editorOffset.top, left: editorOffset.left+$('#editor').innerWidth()-35});
					} else {
						$('#voiceBtn').hide();
					}
					//工具栏设置		end

					//图片上传代码具体写在 bootstrap-wysiwyg.js 文件的  64 行
					$('#editor').wysiwyg();

					//初始化内容
					$('#editor').html(description);
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
