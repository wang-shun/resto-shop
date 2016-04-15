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
	<div class="row form-div" v-if="showform">
		<div class="col-md-12" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki"> 表单</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form class="form-horizontal" role="form " action="article/save" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
							    <label class="col-md-3 control-label">餐品类别</label>
							    <div class="col-md-5">
								    <select class="form-control" name="articleFamilyId" v-model="m.articleFamilyId">
								    	<option :value="f.id" v-for="f in articlefamilys">
								    		{{f.name}}
								    	</option>
								    </select>
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">餐品名称</label>
							    <div class="col-md-5">
								    <input type="text" class="form-control" name="name" v-model="m.name" required="required">
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">价格</label>
							    <div class="col-md-5">
								    <input type="text" class="form-control" name="price" v-model="m.price" required="required">
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">粉丝价</label>
							    <div class="col-md-5">
								    <input type="text" class="form-control" name="fansPrice" v-model="fansPrice">
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">描述</label>
							    <div class="col-md-5">
								    <textarea rows="3" class="form-control" name="description" v-model="m.description"></textarea>
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">排序</label>
							    <div class="col-md-5">
								    <input type="number" class="form-control" name="sort" v-model="m.sort">
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">供应时间</label>
							    <div class="col-md-5">
								    <label v-for="time in supportTimes">
								    	<input type="checkbox" name="supportTimes" :value="time.id"  v-model="m.supportTimes"> {{time.name}} &nbsp;&nbsp;
								    </label>
							    </div>
							</div>
							<div class="form-group">
							    <label class="col-md-3 control-label">餐品图片</label>
							    <div class="col-md-5">
							   	 	<img src="" id="photoSmall"/>
								    <input type="hidden" name="photoSmall" v-model="m.photoSmall">
								    <img-file-upload  class="form-control" @success="uploadSuccess" @error="uploadError"></img-file-upload>
							    </div>
							</div>
							
							<div class="form-group">
							 	<label class="col-md-3 control-label">是否上架</label>
							    <div class="col-md-5">
							    	<label class="radio-inline">
								    	<input type="radio" name="activated" value="1"  v-model="m.activated">是
								    </label>
								    <label class="radio-inline">
								    	 <input type="radio" name="activated" value="0"  v-model="m.activated">否
							    	</label>
							    </div>
							</div>
							<div class="form-group">
								<label class="col-md-3 control-label">餐品规格</label>
								<div class="col-md-5">
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
								<div class="col-md-5">
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
											<input type="text" class="form-control" name="unitPrices" required="required" v-model="u.price"/>
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
				url : "article/list_all",
				dataSrc : ""
			},
			columns : [
			    {
			    	title:"餐品类别",
			    	data:"articleFamilyId",
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
				},                 
				{                 
					title : "餐品图片",
					data : "photoSmall",
				},                 
				{                 
					title : "餐品描述",
					data : "description",
				},                 
				{                 
					title : "餐品排序",
					data : "sort",
				},                 
				{                 
					title : "是否上架",
					data : "activated",
					createdCell:function(td,tdData){
						$(td).html(tdData?"是":"否");
					}
				},                 
				{                 
					title : "所属店铺",
					data : "shopDetailId",
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
		});
		
		var C = new Controller(null,tb);
		
		var vueObj = new Vue({
			el:"#control",
			mixins:[C.formVueMix],
			data:{
				articlefamilys:[],
				supportTimes:[],
				checkedUnit:[],
				articleattrs:[],
				articleunits:{},
				unitPrices:[],
			},
			methods:{
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
						that.unitPrices = article.articlePrises;
					});
				},
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
				var that = this;
				$.post("articlefamily/list_all",null,function(data){
					that.articlefamilys = data;
				});
				$.post("supporttime/list_all",null,function(data){
					that.supportTimes=data;
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
