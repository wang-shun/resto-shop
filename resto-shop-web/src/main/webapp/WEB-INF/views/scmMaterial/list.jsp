<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
	th,td{text-align: center;}
</style>
<div id="control">
	<div class="row form-div" v-show="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki">原料管理</span>
					</div>
				</div>
				<div class="portlet-body">
						<input type="hidden" name="id" v-model="m.id" />
						<div class="form-body">
							<div class="form-group row">
								<label class="col-md-2 control-label">类型</label>
								<div class="col-md-3">
									<select name="materialType" v-model="m.materialType" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="materialType in materialTypes" value="{{materialType.code}}">
											{{materialType.name}}
										</option>
									</select>
								</div>
								<label class="col-md-2 control-label">一级类别</label>
								<div class="col-md-3">
									<select name="categoryOneId" v-model="m.categoryOneId" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
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
										<option disabled selected value>请选择</option>
										<option  v-for="categoryTwo in categoryTwos" value="{{categoryTwo.id}}" v-if="m.categoryOneId == categoryTwo.parentId">
											{{categoryTwo.categoryName}}
										</option>
									</select>
								</div>
								<label class="col-md-2 control-label">品牌</label>
								<div class="col-md-3">
									<select name="categoryThirdId" v-model="m.categoryThirdId" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
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
									<input type="text" class="form-control" name="materialCode" v-model="m.materialCode" disabled
										   :readonly="materReadonly" >
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
									<select name="specList" v-model="specList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="spec in specLists" value="{{[spec.unitName,spec.id]}}">
											{{spec.unitName}}
										</option>
									</select>
								</div>
							</div>
							<div class="form-group row">
								<label class="col-md-2 control-label">规格的核算单位值</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="measureUnit" v-model="m.measureUnit" @change="com"
										   required="required">
								</div>
								<label class="col-md-2 control-label">标准单位</label>
								<div class="col-md-3">
									<select name="unitList" v-model="unitList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="unit in unitLists" value="{{[unit.unitName,unit.id]}}">
											{{unit.unitName}}
										</option>
									</select>
								</div>
							</div>
							<div class="form-group row">
								<label class="col-md-2 control-label">转换率</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="rate" v-model="m.rate" @change="com"
										   required="required">
								</div>

								<label class="col-md-2 control-label">转换单位</label>
								<div class="col-md-3">
									<select name="convertUnitList" v-model="convertUnitList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="unit in convertUnitLists" value="{{[unit.unitName,unit.id]}}">
											{{unit.unitName}}
										</option>
									</select>
								</div>
							</div>
							<div class="form-group row">

								<label class="col-md-2 control-label">核算最小单位值</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="minMeasureUnit" v-model="m.minMeasureUnit"  required="required" @change="com"
										   style="display: inline-block;text-align: center;width:70%;">
									<span style="text-align: center">{{m.convertUnitName}}</span>
								</div>
								<label class="col-md-2 control-label">转换系数</label>
								<div class="col-md-3">
									<input type="text" class="form-control" name="coefficient" :value="coefficients"  v-model="m.coefficient" readOnly="true">
								</div>
							</div>

							<div class="form-group row">
								<label class="col-md-2 control-label">省份</label>
								<input type="hidden" name="provinceName" v-model="m.provinceName" />
								<input type="hidden" name="provinceId" v-model="m.provinceId" />
								<div class="col-md-3">
									<select  v-model="provinceNameList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="province in provinceNameLists" value="{{[province.provinceName,province.id]}}">
											{{province.provinceName}}
										</option>
									</select>
								</div>

								<label class="col-md-2 control-label">城市</label>
								<input type="hidden" name="cityName" v-model="m.cityName" />
								<input type="hidden" name="cityId" v-model="m.cityId" />
								<div class="col-md-3">
									<select  v-model="cityNameList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="city in cityNameLists" value="{{[city.cityName,city.id]}}" v-if="city.provinceId == provinceNameList.split(',')[1]">
											{{city.cityName}}
										</option>
									</select>
								</div>
							</div>

							<div class="form-group row">
								<label class="col-md-2 control-label">区（县）</label>
								<input type="hidden" name="districtName" v-model="m.districtName" />
								<input type="hidden" name="districtId" v-model="m.districtId" />
								<div class="col-md-3">
									<select v-model="districtNameList" class="bs-select form-control" >
										<option disabled selected value>请选择</option>
										<option  v-for="district in districtNameLists" value="{{[district.districtName,district.id]}}" v-if="district.cityId == cityNameList.split(',')[1]">
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
							<button class="btn green"  @click="save">保存</button>
							<a class="btn default" @click="cancel" >取消</a>
						</div>
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
                    createdCell:function (td,tdData) {
                        console.log(tdData);
                        switch (tdData){
							case 'INGREDIENTS':tdData='主料';break;
                            case 'ACCESSORIES':tdData='辅料';break;
                            case 'SEASONING':tdData='调料';break;
						}
                        $(td).html(tdData);
                    }

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
                    createdCell:function (td,tdData,rowData) {
                        $(td).html(rowData.measureUnit+rowData.unitName+'/'+rowData.specName);
                    }
                },
                {
                    title : "转化率",
                    data : "rate",
                },
                {
                    title : "转换单位",
                    data : "convertUnitName",
                },
				{
                    title : "最小核算单位",
                    data : "minMeasureUnit",
                    createdCell:function (td,tdData,rowData) {
                        $(td).html(tdData+rowData.convertUnitName);
                    }
                },

                {
                    title : "转化系数",
                    data : "coefficient",
                },

                {
                    title : "产地",
                    data : "provinceName",
                    createdCell:function (td,tdData,rowData) {
                        $(td).html(tdData+rowData.cityName+rowData.districtName);
                    }
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
        var C = new Controller(null,tb);
        var vueObj = new Vue({
            mixins:[C.formVueMix],
            el:"#control",
            data:{
                materialTypes: [ //类型
                    {code:"INGREDIENTS" , name:"主料"},
                    {code:"ACCESSORIES" , name:"辅料"},
                    {code:"SEASONING" ,name:"调料"},
                ],
                categoryOnes:[],//一级类别集合
                categoryTwos:[],//二级类别集合
                categoryThirds:[],//品牌类别集合
                specLists:[],//规格
                specList:[],//规格
                unitLists:[],//各种单位
                unitList:[],//各种单位
                convertUnitLists:[],//转换单位
                convertUnitList:[],//转换单位
                provinceNameLists:[],//省份
                provinceNameList:'',//接收省份
                cityNameLists:[],//市
                cityNameList:'',//接收市
                districtNameLists:[],//区
                districtNameList:'',//接收区
                coefficients:'',//计算的值
                materReadonly:false,//编码

                m:{
                    materialType: 'INGREDIENTS',
                    categoryOneId :'',
                    categoryTwoId :'',
                    categoryThirdId :'',
                    materialName :'',
                    materialCode :'',
                    specId:'',//规格
                    specName:'',//规格
                    priority:'',
                    unitId:'',//标准单位
                    unitName:'',//标准单位
                    convertUnitId :'',//转换单位
                    convertUnitName:'',//转换单位
                    minConvertUnitId  :'',
                    provinceId :'',//省id
                    provinceName :'',//省名
                    cityId:'',//市id
                    cityName:'',//市id
                    districtId :'',
                    description :'',
                    minMeasureUnit:'',
                    coefficient:'',
                },
            },
            watch:{ //监控事件
                provinceNameList:function (a,b) {//监控省
                    var provinces=a.split(',');
                    this.m.provinceName=provinces[0];
                    this.m.provinceId=parseInt(provinces[1]);
                    console.log(this.m);
                },
                cityNameList:function (a,b) {//监控市
                    var provinces=a.split(',')
                    this.m.cityName=provinces[0];
                    this.m.cityId=provinces[1];
                },
                districtNameList:function (a,b) {//监控区
                    var provinces=a.split(',')
                    this.m.districtName=provinces[0];
                    this.m.districtId=provinces[1];
                },

                specList:function (a,b) {//规格
                    var provinces=a.split(',')
                    this.m.specName=provinces[0];
                    this.m.specId=provinces[1];
                },
                unitList:function (a,b) {//标准单位
                    var provinces=a.split(',')
                    this.m.unitName=provinces[0];
                    this.m.unitId=provinces[1];
                },
                convertUnitList:function (a,b) {//转换单位
                    var provinces=a.split(',')
                    this.m.convertUnitName=provinces[0];
                    this.m.convertUnitId=provinces[1];
                },
            },
            methods:{
                com:function () { //计算系数
                    var Mcoefficient=0;
                    Mcoefficient=parseFloat(this.m.measureUnit)*parseFloat(this.m.rate)/parseFloat(this.m.minMeasureUnit);
                    console.log(Mcoefficient);
                    if(Mcoefficient){
                        this.coefficients=Mcoefficient;
                    }
                },
                closeForm:function(){ //新增
                    this.materReadonly=true;
                    this.m={};
                    this.showform=false;
                },
                create:function(){//编辑
                    this.materReadonly=false;
                    this.m={};
                    this.showform=true;
                },
                edit:function(model){
                    this.m= model;
                    this.m={
                        id:model.id,
                        materialType: 'INGREDIENTS',
                            categoryOneId :model.categoryOneId,
                            categoryTwoId :model.categoryTwoId,
                            categoryThirdId :model.categoryThirdId,
                            materialName :model.materialName,
                            materialCode :model.materialCode,
                            specId:model.specId,//规格
                            specName:model.specName,//规格
                            priority:model.priority,
                            unitId:model.unitId,//标准单位
                            unitName:model.unitName,//标准单位
                            convertUnitId :model.convertUnitId,//转换单位
                            convertUnitName:model.convertUnitName,//转换单位
                            minConvertUnitId:model.minConvertUnitId,
                            provinceId :model.provinceId,//省id
                            provinceName :model.provinceName,//省名
                            cityId:model.cityId,//市id
                            cityName:model.cityName,//市id
                            districtId :model.districtId,
                            description :model.description,
                            minMeasureUnit:model.minMeasureUnit,
                            coefficient:model.coefficient,
                    },
                    this.openForm();
                },
                save:function(){
					if(this.m.id) C.systemButton('scmMaterial/modify',this.m,['编辑成功','编辑失败']);
					else C.systemButton('scmMaterial/create',this.m,['新增成功','新增失败']);
                    this.showform=false;
                }
            },
            ready:function () { //钩子函数
                var that = this;
                $.post("scmUnit/list_type?type=1", null, function (data) {
                    that.unitLists = data.data
                    that.convertUnitLists = data.data
//                    if(!that.m.unitId ) {
//                        that.m.unitId = data.data[0].id;
//                    }
//                    if(that.m.convertUnitId){
//                        that.m.convertUnitId = data.data[0].id;
//                    }
                });

                $.post("scmUnit/list_type?type=2", null, function (data) {
                    that.specLists = data.data;
//                    if(!that.m.specId) {
//                        that.m.specId = data.data[0].id;
//                    }
                });
                $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=1", null, function (data) {
                    that.categoryOnes = data.data;
//                    if(!that.m.categoryOneId){
//                        that.m.categoryOneId= data.data[0].id;
//                    }
                });
                $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=2", null, function (data) {
                    that.categoryTwos = data.data;
//                    if(!that.m.categoryTwoId){
//                        that.m.categoryTwoId= data.data[0].id;
//                    }
                });

                $.post("scmCategory/list_categoryHierarchy?categoryHierarchy=3", null, function (data) {
                    that.categoryThirds = data.data;
//                    if(!that.m.categoryThirdId){
//                        that.m.categoryThirdId = data.data[0].id;
//                    }
                });

                $.post("province/list_province", null, function (data) {
                    that.provinceNameLists = data;
                    //that.m.provinceId = data[0].id;
                });

                $.post("province/list_city", null, function (data) {
                    that.cityNameLists = data;
                    //that.m.cityId= data[0].id;
                });

                $.post("province/list_district", null, function (data) {
                    that.districtNameLists = data;
                    //that.m.districtId = data[0].id;
                });
            },

        });
        C.vue=vueObj;
    }());

</script>
