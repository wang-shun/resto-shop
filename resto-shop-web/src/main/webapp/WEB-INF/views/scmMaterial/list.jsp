<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
	th,td{text-align: center;}
</style>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki">原料管理</span>
	                </div>
	            </div>

				<div class="portlet-body">
		            <form role="form" class="form-horizontal" action="{{m.id?'scmMaterial/modify':'scmMaterial/create'}}" @submit.prevent="save">
						<input type="hidden" name="id" v-model="m.id" />
						<div class="form-body">


							<div class="form-group row">
								<label class="col-md-2 control-label">类型</label>
								<div class="col-md-3">
								<select name="materialType" v-model="m.materialType" class="bs-select form-control" >
										<option  v-for="materialType in materialTypes" value="{{materialType.code}}">
												{{materialType.name}}
										</option>
								</select>
								</div>

								<label class="col-md-2 control-label">一级类别</label>
								<div class="col-md-3">
								<select name="categoryOneId" v-model="m.categoryOneId" class="bs-select form-control" >
									<option  v-for="categoryOne in categoryOnes" value="{{categoryOne.id}}">
										{{categoryOne.categoryName}}
									</option>
								</select>
								</div>
							</div>

							<div class="form-group row" >

								<label class="col-md-2 control-label">二级类别</label>
								<div class="col-md-3">
								<select name="categoryTwoId" v-model="m.categoryTwoId" class="bs-select form-control" >
									<option  v-for="categoryTwo in categoryTwos" value="{{categoryTwo.id}}" v-if="m.categoryOneId == categoryTwo.parentId">
										{{categoryTwo.categoryName}}
									</option>
								</select>
								</div>

								<label class="col-md-2 control-label">品牌</label>
								<div class="col-md-3">
								<select name="categoryThirdId" v-model="m.categoryThirdId" class="bs-select form-control" >
									<%--<option  v-for="categoryThird in categoryThirds | filterBy m.categoryTwoId in 'parentId'" value="{{categoryThird.id}}">--%>
										<%--{{categoryThird.categoryName}} </option>--%>
									<option  v-for="categoryThird in categoryThirds" value="{{categoryThird.id}}" v-if="m.categoryTwoId == categoryThird.parentId">
										{{categoryThird.categoryName}}
									</option>
								</select>
								</div>
							</div>


							<div class="form-group row">
								<label class="col-md-2 control-label">原材料名</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="materialName" v-model="m.materialName"
										   required="required">
								</div>

								<label class="col-md-2 control-label">原材编码</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="materialCode" v-model="m.materialCode"
										   required="required">
								</div>

							</div>

							<div class="form-group row">

								<label class="col-md-2 control-label">序号</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="priority" v-model="m.priority"
										   required="required">
								</div>

								<label class="col-md-2 control-label">规格</label>
								<div class="col-md-3">
								<select name="specId" v-model="m.specId" class="bs-select form-control" >
									<option  v-for="spec in specLists" value="{{spec.id}}">
										{{spec.unitName}}
									</option>
								</select>
								</div>
							</div>


							<div class="form-group row">
								<label class="col-md-2 control-label">标准单位</label>
								<div class="col-md-3">
								<select name="unitId" v-model="m.unitId" class="bs-select form-control" >
									<option  v-for="unit in unitLists" value="{{unit.id}}">
										{{unit.unitName}}
									</option>
								</select>
								</div>

								<label class="col-md-2 control-label">转换单位</label>
								<div class="col-md-3">
								<select name="convertUnitId" v-model="m.convertUnitId" class="bs-select form-control" >
									<option  v-for="unit in unitLists" value="{{unit.id}}">
										{{unit.unitName}}
									</option>
								</select>
								</div>
							</div>
							<div class="form-group row">
								<label class="col-md-2 control-label">最小单位</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="minConvertUnitId" v-model="m.minConvertUnitId" required="required"> g
								</div>
								<label class="col-md-2 control-label">最小转换系数</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="minMeasureUnit" v-model="m.minMeasureUnit" required="required">
								</div>
								<input type="hidden" class="form-control" name="measureUnit" v-model="m.measureUnit" required="required">
							</div>



							<div class="form-group row">
								<label class="col-md-2 control-label">省份</label>
								<div class="col-md-3">
								<select name="provinceId" v-model="m.provinceId" class="bs-select form-control" >
									<option  v-for="province in provinceNameLists" value="{{province.id}}">
										{{province.provinceName}}
									</option>
								</select>
								</div>

								<label class="col-md-2 control-label">城市</label>
								<div class="col-md-3">
								<select name="cityId" v-model="m.cityId" class="bs-select form-control" >
										<option  v-for="city in cityNameLists" value="{{city.id}}" v-if="city.provinceId == m.provinceId">
											{{city.cityName}}
										</option>
								</select>
								</div>
							</div>

							<div class="form-group row">

								<label class="col-md-2 control-label">区（县）</label>
								<div class="col-md-3">
								<select name="districtId" v-model="m.districtId" class="bs-select form-control" >
									<option  v-for="district in districtNameLists" value="{{district.id}}" v-if="district.cityId == m.cityId">
										{{district.districtName}}
									</option>
								</select>
								</div>

								<label  class="col-md-2 control-label">描述</label>
								<div class="col-sm-3">
									<input type="text" class="form-control" name="description" v-model="m.description">
								</div>
							</div>


					</div>
						<div class="form-group text-center">
							<input class="btn green"  type="submit"  value="保存"/>&nbsp;&nbsp;&nbsp;
							<a class="btn default" @click="cancel" >取消</a>
						</div>
					</form>
	            </div>
	        </div>
		</div>
	</div>

	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="scmMaterial/add">
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
				url : "scmMaterial/list_all",
				dataSrc : "data"
			},
			columns : [
				{
					title : "类型",
					data : "materialType",
				},
				{
					title : "一级类别",
					data : "categoryOneName",
				},
                {
                    title : "二级类别",
                    data : "categoryTwoName",
                }
                ,
                {
                    title : "品牌",
                    data : "categoryThirdName",
//					createdCell : function(td,tdData){
//						$(td).html("<span class='label label-primary'>"+tdData+"%</span>");
//					}
                },
				{
					title : "材料名",
					data : "materialName",
				},
				{                 
					title : "序号 ",
					data : "priority",
				},
                {
                    title : "编码",
                    data : "materialCode",
                },

                {
                    title : "规格",
                    data : "specName",
                },

                {
                    title : "标准单位",
                    data : "unitName",
                },
                {
                    title : "转换单位",
                    data : "convertUnitName",
                },
                {
                    title : "标准单位转换系数",
                    data : "measureUnit",
                },
                {
                    title : "最小转换单位",
                    data : "minUnitName",
                },
                {
                    title : "最小转换单位转换系数",
                    data : "minMeasureUnit",
                },
                {
                    title : "产地(省份)",
                    data : "provinceName",
                },
                {
                    title : "城市",
                    data : "cityName",
                },
                {
                    title : "区（县）",
                    data : "districtName",
                },

				{                 
					title : "描述",
					data : "description",
				},                 
				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="scmMaterial/delete">
							C.createDelBtn(tdData,"scmMaterial/delete"),
							</s:hasPermission>
							<s:hasPermission name="scmMaterial/edit">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		//debugger
		console.log(tb);
		var C = new Controller(null,tb);
		var vueObj = new Vue({
			mixins:[C.formVueMix],
			el:"#control",
			data:{
				checkedValues: [],
                categoryOnes:[],
                categoryTwos:[],
                categoryThirds:[],
                specLists:[],
                unitLists:[],
                provinceNameLists:[],
                cityNameLists:[],
                districtNameLists:[],
                materialTypes: [
                    {
                      code:"INGREDIENTS" ,
				      name:"主料"
				   },
					{
                        code:"ACCESSORIES" ,
                        name:"辅料"
				   },{
                        code:"SEASONING" ,
                        name:"调料"
				   }],
			},
			methods:{
				closeForm:function(){
					this.m={};
					this.showform=false;
					this.checkedValues=[];
				},
				create:function(){
                    var that = this;
					this.m={
                        materialType: 'INGREDIENTS',
                        categoryOneId :'',
                        categoryTwoId :'',
                        categoryThirdId :'',
                        materialName :'',
                        materialCode :'',
                        specId	 :'',
                        priority :'',
                        unitId	 :'',
                        convertUnitId :'',
                        minConvertUnitId  :'',
                        provinceId :'',
                        cityId	 :'',
                        districtId :'',
                        description :''

                    };
                    that.unitLists = [];
                    $.post("scmUnit/list_type?type=1", null, function (data) {
                        that.unitLists = data.data
                        if(!that.m.unitId ) {
                            that.m.unitId = data.data[0].id;

                        }
                        if(that.m.convertUnitId){
                            that.m.convertUnitId = data.data[0].id;
						}
                    });

                    this.specLists = [];
                    $.post("scmUnit/list_type?type=2", null, function (data) {
                        that.specLists = data.data;
                        if(!that.m.specId) {
                            that.m.specId = data.data[0].id;
                        }
                    });
                    this.categoryOnes = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=1", null, function (data) {
                        that.categoryOnes = data.data;
                        if(!that.m.categoryOneId){
                            that.m.categoryOneId= data.data[0].id;
						}
                    });

                    this.categoryTwos = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=2", null, function (data) {
                        that.categoryTwos = data.data;
                        if(!that.m.categoryTwoId){
                            that.m.categoryTwoId= data.data[0].id;
						}


                    });

                    this.categoryThirds = [];
                    $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=3", null, function (data) {
                        that.categoryThirds = data.data;
                        if(!that.m.categoryThirdId){
                            that.m.categoryThirdId = data.data[0].id;
						}

                    });

                    this.provinceNameLists = [];
                    $.post("province/list_province", null, function (data) {
                        that.provinceNameLists = data;
                            that.m.provinceId = data[0].id;

                    });

                    this.cityNameLists = [];
                    $.post("province/list_city", null, function (data) {
                        that.cityNameLists = data;
                            that.m.cityId= data[0].id;
                    });

                    this.districtNameLists = [];
                    $.post("province/list_district", null, function (data) {
                        that.districtNameLists = data;
                            that.m.districtId = data[0].id;
                    });
                    this.showform=true;

				},
				edit:function(model){
					var that = this;
					this.m= model;
					this.openForm();


				},
				save:function(e){
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
	
</script>
