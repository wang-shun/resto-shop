<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
	.article-attr-label{
		min-width:50px;
	}
	.article-units>label{
		display: inline-block;
		min-width: 70px;
	}
</style>
<div id="control">
	<div class="modal fade" id="article-dialog" v-if="showform">
	  <div class="modal-dialog " style="width:90%;">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h4 class="modal-title">表单</h4>
	      </div>
	      <div class="modal-body">
	             <form class="form-horizontal" role="form " action="article/save" @submit.prevent="save">
				<div class="form-body">
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">餐品类别</label>
					    <div class="col-md-7">
						    <select class="form-control" name="articleFamilyId" v-model="m.articleFamilyId">
						    	<option :value="f.id" v-for="f in articlefamilys">
						    		{{f.name}}
						    	</option>
						    </select>
					    </div>
					</div>
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">餐品名称</label>
					    <div class="col-md-7">
						    <input type="text" class="form-control" name="name" v-model="m.name" required="required">
					    </div>
					</div>
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">餐品单位</label>
					    <div class="col-md-7">
						    <input type="text" class="form-control" name="unit" v-model="m.unit">
					    </div>
					</div>
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">价格</label>
					    <div class="col-md-7">
						    <input type="text" class="form-control" name="price" v-model="m.price" required="required">
					    </div>
					</div>
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">粉丝价</label>
					    <div class="col-md-7">
						    <input type="text" class="form-control" name="fansPrice" v-model="m.fansPrice">
					    </div>
					</div>
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">排序</label>
					    <div class="col-md-7">
						    <input type="number" class="form-control" name="sort" v-model="m.sort">
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					 	<label class="col-md-5 control-label">是否上架</label>
					    <div class="col-md-7 radio-list">
					    	<label class="radio-inline">
						    	<input type="radio" name="activated" value="1"  v-model="m.activated">是
						    </label>
						    <label class="radio-inline">
						    	 <input type="radio" name="activated" value="0"  v-model="m.activated">否
					    	</label>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					 	<label class="col-md-5 control-label">是否沽清</label>
					    <div class="col-md-7 radio-list">
					    	<label class="radio-inline">
						    	<input type="radio" name="isEmpty" value="1"  v-model="m.isEmpty">是
						    </label>
						    <label class="radio-inline">
						    	 <input type="radio" name="isEmpty" value="0"  v-model="m.isEmpty">否
					    	</label>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					 	<label class="col-md-5 control-label">未点提示</label>
					    <div class="col-md-7 radio-list">
					    	<label class="radio-inline">
						    	<input type="radio" name="isRemind" value="1"  v-model="m.isRemind">是
						    </label>
						    <label class="radio-inline">
						    	 <input type="radio" name="isRemind" value="0"  v-model="m.isRemind">否
					    	</label>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					 	<label class="col-md-5 control-label">显示大图</label>
					    <div class="col-md-7 radio-list">
					    	<label class="radio-inline">
						    	<input type="radio" name="showBig" value="1"  v-model="m.showBig">是
						    </label>
						    <label class="radio-inline">
						    	 <input type="radio" name="showBig" value="0"  v-model="m.showBig">否
					    	</label>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					 	<label class="col-md-5 control-label">显示描述</label>
					    <div class="col-md-7 radio-list">
					    
					    	<label class="radio-inline">
						    	<input type="radio" name="showDesc" value="1"  v-model="m.showDesc">是
						    </label>
						    <label class="radio-inline">
						    	 <input type="radio" name="showDesc" value="0"  v-model="m.showDesc">否
					    	</label>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
				        <label class="col-md-5 control-label">按钮颜色</label>
				        <div class="col-md-2">
				            <input type="text"  class="form-control color-mini" name="controlColor" data-position="bottom left" :value="m.controlColor||'#ffffff'" > 
				        </div>
				        <div class="col-md-5">
				        	<span class="btn dark" @click="changeColor('#000')">黑</span>
				            <span class="btn btn-default" @click="changeColor('#fff')">白</span>
				        </div>
				    </div>
					
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">餐品编号</label>
					    <div class="col-md-7">
						    <input type="text" class="form-control" name="peference" v-model="m.peference">
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">餐品图片</label>
					    <div class="col-md-7">
						    <input type="hidden" name="photoSmall" v-model="m.photoSmall">
						    <img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
					    </div>
					</div>
					
					<div class="form-group col-md-4">
					    <label class="col-md-5 control-label">描述</label>
					    <div class="col-md-7">
						    <textarea rows="3" class="form-control" name="description" v-model="m.description"></textarea>
					    </div>
					</div>
					<div class="clearfix"></div>
					
					<div class="form-group">
					    <label class="col-md-3 control-label">出餐厨房</label>
					    <div class="col-md-9">
						    <label v-for="kitchen in kitchenList">
						    	<input type="checkbox" name="kitchenList" :value="kitchen.id"  v-model="m.kitchenList"> {{kitchen.name}} &nbsp;&nbsp;
						    </label>
					    </div>
					</div>
					<div class="form-group">
					    <label class="col-md-3 control-label">供应时间</label>
					    <div class="col-md-9">
						    <label v-for="time in supportTimes">
						    	<input type="checkbox" name="supportTimes" :value="time.id"  v-model="m.supportTimes"> {{time.name}} &nbsp;&nbsp;
						    </label>
						    <label>
							    <input type="checkbox" @change="selectAllTimes(m,$event)"/> 全选
						    </label>
					    </div>
					</div>
					
					
					<div class="form-group">
						<label class="col-md-3 control-label">餐品规格</label>
						<div class="col-md-9">
							<div class="article-attr" v-for="attr in articleattrs" v-if="attr.articleUnits">
								<label class="article-attr-label">{{attr.name}}:</label>
								<span class="article-units">
									<label v-for="unit in attr.articleUnits" >
								    	<input type="checkbox" name="hasUnitIds" :value="unit.id" v-model="checkedUnit"> {{unit.name}} 
								    </label>
								</span> 
							</div>
						</div>
					</div>
					<div class="form-group" v-if="allUnitPrice.length">
						<label class="col-md-3 control-label">规格价格</label>
						<div class="col-md-8">
							<div class="flex-row">
								<div class="flex-1 text-right">规格</div>
								<div class="flex-2">价格</div>
								<div class="flex-2">粉丝价</div>
								<div class="flex-2">编号</div>
							</div>
							<div class="flex-row" v-for="u in unitPrices">
								<div class="flex-1 text-right">{{u.name}}</div>
								<div class="flex-2">
									<input type="hidden" name="unitNames" :value="u.name"/>
									<input type="hidden" name="unit_ids" :value="u.unitIds"/>
									<input type="text" class="form-control" name="unitPrices" required="required" :value="u.price" v-model="u.price"/>
								</div>
								<div class="flex-2">
									<input type="text" class="form-control" name="unitFansPrices" v-model="u.fansPrice"/>
								</div>
								<div class="flex-2">
									<input type="text" class="form-control" name="unitPeferences" v-model="u.peference"/>
								</div>
							</div>
						</div>
					</div>
					
				</div>
				<div class="form-actions">
                    <div class="row">
                        <div class="col-md-offset-3 col-md-5">
                            <input type="hidden" name="id" v-model="m.id" />
						    <input class="btn green"  type="submit"  value="保存"/>
						    <a class="btn default" @click="cancel" >取消</a>
                        </div>
                     </div>
                 </div>
			</form>
	      </div>
	    </div>
	  </div>
	</div>
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="article/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">
			<div class="form-group">
			    <label class="col-md-1 control-label">菜品类别:</label>
			    <div class="col-md-3">
			    	<select class="form-control" @change="filterTable">
			    		<option value="-1">全部</option>
			    		<option :value="af.name" v-for="af in articlefamilys">{{af.name}}</option>
			    	</select>
			    </div>
			</div>
		</div>
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
				url : "article/list_all",
				dataSrc : ""
			},
			stateSave:true,
			deferRender:true,
			columns : [
			    {
			    	title:"餐品类别",
			    	data:"articleFamilyName",
			    },
				{                 
					title : "餐品名称",
					data : "name",
				},                 
				{                 
					title : "餐品价格",
					data : "price",
				},                 
				{                 
					title : "粉丝价",
					data : "fansPrice",
					defaultContent:"",
				},                 
				{                 
					title : "餐品图片",
					data : "photoSmall",
					createdCell:function(td,tdData){
						$(td).html("<img src='/"+tdData+"' style='height:40px;width:80px;'/>")
					}
				},                 
// 				{                 
// 					title : "餐品描述",
// 					data : "description",
// 					createdCell:function(td,tdData){
// 						if(tdData.length>10){
// 							tdData = tdData.substring(0,10);
// 							$(td).html(tdData+"...");
// 						}
// 					}
// 				},                 
				{                 
					title : "餐品排序",
					data : "sort",
				},                 
				{                 
					title : "上架",
					data : "activated",
					createdCell:function(td,tdData){
						$(td).html(tdData?"是":"否");
					}
				},           
				{
					title:"沽清",
					data:"isEmpty",
					createdCell:function(td,tdData){
						$(td).html(tdData?"是":"否");
					}
				},
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="article/delete">
							C.createDelBtn(tdData,"article/delete"),
							</s:hasPermission>
							<s:hasPermission name="article/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
				initComplete:function(){
					
				}
		});
		
		var C = new Controller(null,tb);
		
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			data:{
				articlefamilys:[],
				supportTimes:[],
				kitchenList:[],
				checkedUnit:[],
				articleattrs:[],
				articleunits:{},
				unitPrices:[],
			},
			methods:{
				selectAllTimes:function(m,e){
					var isCheck = $(e.target).is(":checked");
					if(isCheck){
						for(var i=0;i<this.supportTimes.length;i++){
							var t = this.supportTimes[i];
							m.supportTimes.push(t.id);
						}
					}else{
						m.supportTimes=[];
					}
					Vue.nextTick(function(){
						App.updateUniform();
					});
				},
				create:function(){
					this.m= {
						supportTimes:[],
						kitchenList:[],
						isRemind:false,
						activated:true,
						showDesc:true,
						showBig:true,
						isEmpty:false,
						sort:0,
					};
					this.checkedUnit=[];
					this.showform=true;
				},
				uploadSuccess:function(url){
					$("[name='photoSmall']").val(url).trigger("change");
					C.simpleMsg("上传成功");
					$("#photoSmall").attr("src","/"+url);
				},
				uploadError:function(msg){
					C.errorMsg(msg);
				},
				edit:function(model){
					var that = this;
					that.showform=true;
					$.post("article/list_one_full",{id:model.id},function(result){
						var article=result.data;
						that.m= article;
						if(article.hasUnit&&article.hasUnit.length){
							var unit = article.hasUnit.split(",");
							for(var i in  unit){
								that.checkedUnit.push(parseInt(unit[i]));
							}
						}else{
							that.checkedUnit=[];
						}
						that.unitPrices = article.articlePrices;
					});
				},
				filterTable:function(e){
					var s  = $(e.target);
					var val = s.val();
					if(val=="-1"){
						tb.search("").draw();
						return;
					}
					tb.search(val).draw();
				},
				changeColor:function(val){
					$(".color-mini").minicolors("value",val);
				}
			},
			computed:{
				allUnitPrice:function(){
					var result = [];
					for(var i=0;i<this.articleattrs.length;i++){
						var attr = this.articleattrs[i];
						var checked =[];
						for(var j=0;j<attr.articleUnits.length;j++){
							var c = attr.articleUnits[j];
							for(var n in this.checkedUnit){
								if(c.id==this.checkedUnit[n]){
									checked.push({
										unitIds:c.id,
										name:"("+c.name+")"
									})
									break;
								}
							}
						}
						checked.length&&result.push(checked);
					}
					
					
					function getAll(allData){
						var root = [];
					 	for(var i in allData){
					 		var currentData = allData[i];
					 		if(i>0){
					 			for(var p  in allData[i-1]){
					 				var parent = allData[i-1][p];
					 				parent.children = currentData;
					 			}
					 		}else{
					 			root = currentData;
					 		}
					 	}
					 	var allItems = [];
					 	for(var n in root){
					 		var r = root[n];
					 		getTreeAll(r,allItems);
					 	}
					 	return allItems;
					}
					
					function getTreeAll(tree,allItems){
						tree = $.extend({},tree);
						if(!tree.children){
							allItems.push($.extend({},tree));
							return allItems;
						}
						for(var i in tree.children){
							var c = tree.children[i];
							c = $.extend({},c);
							c.unitIds = tree.unitIds+","+c.unitIds;
							c.name = tree.name+ c.name;
							if(!c.children){
								allItems.push(c);
							}else{
								getTreeAll(c,allItems);
							}
						}
						return allItems;
					} 
					
					var allItems = getAll(result);
					for(var i in allItems){
						var item = allItems[i];
						for(var i in this.unitPrices){
							var p = this.unitPrices[i];
							if(item.unitIds==p.unitIds){
								item  = $.extend(item,p);
							}
						}
					}
					this.unitPrices = allItems;
					return allItems;
				}
			},
			created:function(){
				tb.search("").draw();
				var that = this;
				this.$watch("showform",function(){
					if(this.showform){
						$("#article-dialog").modal("show");
						var n=$('.color-mini').minicolors({
				            change: function(hex, opacity) {
				                if (!hex) return;
				                if (typeof console === 'object') {
				                    $(this).attr("value",hex);
				                }
				            },
				            theme: 'bootstrap'
				        });
						$("#article-dialog").on("hidden.bs.modal",function(){
							that.showform=false;
						});
					}else{
						$("#article-dialog").modal("hide");
						$(".modal-backdrop.fade.in").remove();
					}
				});
				this.$watch("m",function(){
					if(this.m.id){
						$('.color-mini').minicolors("value",this.m.controlColor);
					}
				});
				
				$.post("articlefamily/list_all",null,function(data){
					that.articlefamilys = data;
				});
				$.post("supporttime/list_all",null,function(data){
					that.supportTimes = data;
				});
				$.post("kitchen/list_all",null,function(data){
					that.kitchenList = data;
				});
				$.post("articleattr/list_all",null,function(data){
					var article_units = {};
					for(var i in data){
						var attr = data[i];
						attr.checkedUnit=[];
   					  	var units = attr.articleUnits;
   					  	for(var i in units){
   					  		var unit = units[i];
   					  		unit.attr=attr;
   					  		article_units[unit.id]=unit;
   					  	}
					}
					that.articleunits=article_units;
					that.articleattrs=data;
				});
			}
		});
		C.vue=vueObj;
		
	}());
	
	

	
</script>
