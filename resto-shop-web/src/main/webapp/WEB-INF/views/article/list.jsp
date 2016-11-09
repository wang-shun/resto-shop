<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<style>
    .article-attr-label {
        min-width: 50px;
    }

    .article-units > label {
        display: inline-block;
        min-width: 70px;
    }

    .modal-body.auto-height {
        max-height: 80vh;
        overflow-y: auto;
    }

    .print-sort {

    }
</style>
<div id="control">

    <div class="modal fade" id="article-dialog" v-if="showform" @click="cleanRemark">
        <div class="modal-dialog " style="width:90%;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">表单</h4>
                </div>
                <form class="form-horizontal" role="form " action="article/save" @submit.prevent="save">
                    <div class="modal-body auto-height">
                        <div class="form-body">
                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">餐品类别</label>
                                <div class="col-md-7">
                                    <select class="form-control" name="articleFamilyId" v-model="m.articleFamilyId"
                                            required="required">
                                        <option :value="f.id" v-for="f in articlefamilys">
                                            {{f.name}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">餐品名称</label>
                                <div class="col-md-7">
                                    <input type="text" class="form-control" name="name" v-model="m.name"
                                           required="required">
                                </div>
                            </div>

                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">饿了么名称</label>
                                <div class="col-md-7">
                                    <input type="text" class="form-control" name="elemeName" v-model="m.elemeName"
                                           required="required">
                                </div>
                            </div>

                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">餐品单位</label>
                                <div class="col-md-7">
                                    <input type="text" class="form-control" name="unit" v-model="m.unit"
                                           required="required">
                                </div>
                            </div>
                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">价格</label>
                                <div class="col-md-7">
                                    <input type="text" class="form-control" name="price" v-model="m.price"
                                           required="required" pattern="\d{1,10}(\.\d{1,2})?$"
                                           title="价格只能输入数字,且只能保存两位小数！">
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
                                <label class="col-md-5 control-label">上架沽清</label>
                                <div class="col-md-7 radio-list">
                                    <label class="radio-inline">
                                        <input type="checkbox" v-bind:true-value="true" v-bind:false-value="false"
                                               v-model="m.activated">上架
                                    </label>
                                    <span v-if="m.articleType != 2">
                                        <label class="radio-inline">
                                            <input type="checkbox" v-bind:true-value="true" v-bind:false-value="false"
                                                   v-model="m.isEmpty">沽清
                                        </label>
                                    </span>
                                    <span>
                                        <label class="radio-inline">
                                            <input type="checkbox" v-bind:true-value="1" v-bind:false-value="0"
                                                   v-model="m.isHidden">隐藏
                                        </label>
                                    </span>
                                </div>
                            </div>

                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">显示</label>
                                <div class="col-md-7 radio-list">
                                    <label class="radio-inline">
                                        <input type="checkbox" v-bind:true-value="true" v-bind:false-value="false"
                                               v-model="m.showBig">大图
                                    </label>
                                    <label class="radio-inline">
                                        <input type="checkbox" v-bind:true-value="true" v-bind:false-value="false"
                                               v-model="m.showDesc">描述
                                    </label>
                                </div>
                            </div>

                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">未点提示</label>
                                <div class="col-md-7 radio-list">
                                    <label class="radio-inline" v-if="m.articleType==1">
                                        <input type="checkbox" v-bind:true-value="true" v-bind:false-value="false"
                                               v-model="m.isRemind">提示
                                    </label>
                                    <label class="radio-inline" v-else>
                                        <input v-else type="checkbox" value="false" v-model="m.isRemind" disabled>提示
                                    </label>
                                </div>
                            </div>

                            <div class="form-group col-md-4">
                                <label class="col-md-5 control-label">按钮颜色</label>
                                <div class="col-md-2">
                                    <input type="text" class="form-control color-mini" name="controlColor"
                                           data-position="bottom left" v-model="m.controlColor">
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
                                    <img-file-upload class="form-control" @success="uploadSuccess"
                                                     @error="uploadError"></img-file-upload>
                                </div>
                            </div>

                            <div class="form-group col-md-5">
                                <label class="col-md-3 control-label">描述</label>
                                <div class="col-md-7">
                                    <textarea rows="3" class="form-control" name="description"
                                              v-model="m.description"></textarea>
                                </div>
                            </div>
                            <div class="form-group col-md-7">
                                <div class="row">
                                    <div class="form-group col-md-12">
                                        <label class="col-md-2 text-right">出餐厨房</label>
                                        <div class="col-md-8">
                                            <label v-for="kitchen in kitchenList">
                                                <input type="checkbox" name="kitchenList" :value="kitchen.id"
                                                       v-model="m.kitchenList"> {{kitchen.name}} &nbsp;&nbsp;
                                            </label>
                                            <div id="kitchenRemark"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group  col-md-12">
                                        <label class="col-md-2 text-right">供应时间</label>
                                        <div class="col-md-8">
                                            <label v-for="time in supportTimes">
                                                <input type="checkbox" name="supportTimes" :value="time.id"
                                                       v-model="m.supportTimes"> {{time.name}} &nbsp;&nbsp;
                                            </label>
                                            <label v-if="supportTimes.length>0">
                                                <input type="checkbox" @change="selectAllTimes(m,$event)"/> 全选
                                            </label>
                                            <div id="supportTimeRemark"></div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row" v-if="m.articleType!=2">
                                    <div class="form-group  col-md-12">
                                        <label class="col-md-2 text-right" style="margin-top: 20px">库存</label>
                                        <div class="col-md-8">
                                            <div>
                                                <label>
                                                    <input name="stockWorkingDay"
                                                           class="form-control" v-model="m.stockWorkingDay"
                                                           id="stockWorkingDay"/> (工作日)
                                                </label>
                                            </div>
                                            <div>
                                                <label>
                                                    <input name="stockWeekend"
                                                           class="form-control" v-model="m.stockWeekend"
                                                           id="stockWeekend"/> (假期)
                                                </label>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                            </div>
                            <div class="clearfix"></div>


                            <div class="form-group col-md-10" v-if="m.articleType==1">
                                <label class="col-md-2 text-right">推荐餐品包</label>
                                <div class="col-md-10">
                                    <select name="recommendId" v-model="m.recommendId">
                                        <option value="">未选择餐品包</option>
                                        <option :value="f.id" v-for="f in recommendList">
                                            {{f.name}}
                                        </option>
                                    </select>
                                </div>
                            </div>
                            <%--<div class="form-group col-md-10" v-if="m.articleType==1">--%>
                            <%--<label class="col-md-2 text-right">选择规格包<label style="color: red">(将会覆盖原有规格)</label></label>--%>
                            <%--<div class="col-md-10">--%>
                            <%--<select  name="unitId"  v-model="m.unitId" >--%>
                            <%--<option value="selected">未选择规格包</option>--%>
                            <%--<option :value="f.id" v-for="f in unitList">--%>
                            <%--{{f.name}}--%>
                            <%--</option>--%>
                            <%--</select>--%>
                            <%--</div>--%>
                            <%--</div>--%>


                            <div class="form-group col-md-10" v-if="m.articleType==1">
                                <label class="col-md-2 text-right">规格属性（新）<label style="color:red">注：与旧版规格只能使用一个</label></label>
                                <div class="col-md-10">
                                    <%--<div class="article-attr" v-for="attr in unitList">--%>
                                    <%--<label class="article-attr-label">{{attr.name}}:</label>--%>
									<span class="article-units">
										<label v-for="attr in unitList">
                                            <input type="checkbox"
                                                   v-model="attr.isUsed" v-bind:true-value="1" v-bind:false-value="0"
                                                   id="{{attr.id}}" @click="clickUnit(attr)">
                                            {{attr.name}}
                                        </label>
									</span>
                                    <%--</div>--%>
                                </div>
                            </div>


                            <div class="form-group col-md-10" v-for="select in selectedUnit.unitList">
                                <div class="col-md-10">
                                    <label class="col-md-2 ">规格属性: {{select.name}}</label>
                                    <label class="col-md-2 ">是否单选: <input type="checkbox" v-bind:true-value="0"
                                                                          v-bind:false-value="1"
                                                                          v-model="select.choiceType"></label>
                                    <%--<div class="col-md-7 radio-list">--%>


                                    <%--</div>--%>

                                </div>
                                <%--<div class="col-md-10">--%>


                                <%--&lt;%&ndash;<label for="choice{{select.id}}">是否单选: </label>&ndash;%&gt;--%>
                                <%--&lt;%&ndash;<input type="checkbox" id="choice{{select.id}}"&ndash;%&gt;--%>
                                <%--&lt;%&ndash;v-model="select.choiceType" v-bind:true-value="0" v-bind:false-value="1"/>&ndash;%&gt;--%>
                                <%--&lt;%&ndash;</span>&ndash;%&gt;--%>
                                <%--</div>--%>

                                <div class="col-md-10">
                                    <div class="flex-row">
                                        <div class="flex-1 text-right">规格</div>
                                        <div class="flex-1">差价</div>
                                        <div class="flex-1">排序</div>
                                        <div class="flex-1">是否启用</div>

                                    </div>
                                    <div class="flex-row " v-for="detail in select.details">
                                        <label class="flex-1 control-label">{{detail.name}}</label>
                                        <div class="flex-1">
                                            <input type="text" class="form-control"
                                                   v-model="detail.price" :value="detail.price==null?0:detail.price" id="price{{detail.id}}" required="required"/>
                                        </div>
                                        <div class="flex-1">
                                            <input type="text" class="form-control" name="sort"
                                                   v-model="detail.sort" id="sort{{detail.id}}" required="required"
                                            />
                                        </div>
                                        <div class="flex-1">
                                            <input type="checkbox" v-bind:true-value="1" v-bind:false-value="0"
                                                   @click="changeUsed(select,detail)" v-model="detail.isUsed"
                                                   style="width:70px;height:30px">
                                            <%--<input type="radio" required name="type{{detail.id}}"--%>
                                            <%--checked v-model="detail.isUsed"     @click="changeUsed(select,detail,1)"> 是--%>
                                            <%--<input type="radio" required name="type{{detail.id}}"--%>
                                            <%--v-model="detail.isUsed"   @click="changeUsed(select,detail,0)"> 否--%>

                                            <%--<input type="checkbox" id="{{detail.id}}" checked="detail.isUsed == 1" v-model="detail.isUsed"--%>
                                            <%--@click="changeUsed(select,detail)" style="width:70px;height:30px">--%>
                                        </div>


                                    </div>
                                </div>
                            </div>


                            <div class="form-group col-md-10" v-if="m.articleType==1">
                                <label class="col-md-2 text-right">餐品规格</label>
                                <div class="col-md-10">
                                    <div class="article-attr" v-for="attr in articleattrs" v-if="attr.articleUnits">
                                        <label class="article-attr-label">{{attr.name}}:</label>
									<span class="article-units">
										<label v-for="unit in attr.articleUnits">
                                            <input type="checkbox" :value="unit.id" v-model="checkedUnit"> {{unit.name}}
                                        </label>
									</span>
                                    </div>
                                </div>
                            </div>


                            <div class="form-group col-md-10" v-if="allUnitPrice.length">
                                <label class="col-md-2 control-label">规格价格</label>
                                <div class="col-md-10">
                                    <div class="flex-row">
                                        <div class="flex-1 text-right">规格</div>
                                        <div class="flex-2">价格</div>
                                        <div class="flex-2">粉丝价</div>
                                        <div class="flex-2">编号</div>
                                        <div class="flex-2">工作日库存</div>
                                        <div class="flex-2">周末库存</div>
                                    </div>
                                    <div class="flex-row " v-for="u in unitPrices">
                                        <label class="flex-1 control-label">{{u.name}}</label>
                                        <div class="flex-2">
                                            <input type="hidden" name="unitNames" :value="u.name"/>
                                            <input type="hidden" name="unit_ids" :value="u.unitIds"/>
                                            <input type="text" class="form-control" name="unitPrices"
                                                   required="required" :value="u.price" v-model="u.price"/>
                                        </div>
                                        <div class="flex-2">
                                            <input type="text" class="form-control" name="unitFansPrices"
                                                   v-model="u.fansPrice"/>
                                        </div>
                                        <div class="flex-2">
                                            <input type="text" class="form-control" name="unitPeferences"
                                                   v-model="u.peference"/>
                                        </div>
                                        <div class="flex-2">
                                            <input type="text" class="form-control" name="stockWorkingDay"
                                                   id="workingDay" v-model="u.stockWorkingDay"
                                                   onchange="changeStockWeekend()"/>
                                        </div>
                                        <div class="flex-2">
                                            <input type="text" class="form-control" name="stockWeekend"
                                                   v-model="u.stockWeekend" onchange="changeStockWeekend()"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12" v-if="m.articleType==2">
                                <div class="portlet light bordered">
                                    <div class="portlet-title">
                                        <div class="caption font-green-sharp">
                                            <i class="icon-speech font-green-sharp"></i>
                                            <span class="caption-subject bold uppercase"> 编辑套餐</span>
                                        </div>
                                        <div class="actions">
                                            <select class="form-control" @change="choiceMealTemp" v-model="choiceTemp">
                                                <option value="">不选择模板</option>
                                                <option :value="meal.id" v-for="meal in mealtempList">{{meal.name}}
                                                </option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="portlet-body">
                                        <div class="portlet box blue-hoki"
                                             v-for="attr in m.mealAttrs | orderBy  'sort'">
                                            <div class="portlet-title">
                                                <div class="caption">
                                                    <label class="control-label">&nbsp;</label>
                                                    <div class="pull-right">
                                                        <input class="form-control" type="text" v-model="attr.name"
                                                               required="required">
                                                    </div>
                                                </div>
                                                <div class="caption">
                                                    <label class="control-label col-md-4">排序&nbsp;</label>
                                                    <div class="col-md-4">
                                                        <input class="form-control" type="text" v-model="attr.sort"
                                                               required="required" lazy>
                                                    </div>

                                                </div>

                                                <div class="caption">
                                                    <label class="control-label col-md-4"
                                                           style="width:120px">打印排序&nbsp;</label>
                                                    <div class="col-md-4">
                                                        <input class="form-control" type="text" v-model="attr.printSort"
                                                               required="required" name="printSort" lazy
                                                               onblur="checkSort(this)">
                                                    </div>

                                                </div>


                                                <div class="caption">
                                                    <label class="control-label col-md-4"
                                                           style="width:120px">选择类型&nbsp;</label>
                                                    <div class="col-md-4">
                                                        <select class="form-control" style="width:150px"
                                                                name="choiceType" v-model="attr.choiceType"
                                                                v-on:change="choiceTypeChange(attr)">
                                                            <option value="0">必选</option>
                                                            <option value="1">任选</option>
                                                        </select>


                                                    </div>
                                                    <div class="col-md-4" v-if="attr.choiceType == 0">
                                                        <input class="form-control" type="text"
                                                               v-model="attr.choiceCount"
                                                               required="required">
                                                    </div>
                                                </div>

                                                <div class="tools">
                                                    <a href="javascript:;" class="remove"
                                                       @click="delMealAttr(attr)"></a>
                                                </div>
                                            </div>
                                            <div class="portlet-body">
                                                <div class="form-group col-md-12" v-if="attr.mealItems.length">
                                                    <div class="flex-row">
                                                        <div class="flex-1">餐品原名</div>
                                                        <div class="flex-2">餐品名称</div>
                                                        <div class="flex-2">差价</div>
                                                        <div class="flex-1">排序</div>
                                                        <div class="flex-1">默认</div>
                                                        <div class="flex-1">指定厨房出单</div>
                                                        <div class="flex-1">移除</div>
                                                    </div>
                                                    <div class="flex-row"
                                                         v-for="item in attr.mealItems | orderBy 'sort' ">
                                                        <div class="flex-1">
                                                            <p class="form-control-static">{{item.articleName}}</p>
                                                        </div>
                                                        <div class="flex-2">
                                                            <input type="text" class="form-control" v-model="item.name"
                                                                   required="required"/>
                                                        </div>
                                                        <div class="flex-2">
                                                            <input type="text" class="form-control"
                                                                   v-model="item.priceDif" required="required"/>
                                                        </div>
                                                        <div class="flex-1">
                                                            <input type="text" class="form-control" v-model="item.sort"
                                                                   required="required" lazy/>
                                                        </div>
                                                        <div class="flex-1 radio-list">
                                                            <label class="radio-inline">
                                                                <input type="checkbox" :name="attr.name" :value="true"  v-bind:disabled="attr.choiceType == 1"
                                                                       v-model="item.isDefault && attr.choiceType == 0"
                                                                       @change="itemDefaultChange(attr,item)"/>
                                                                设为默认
                                                            </label>
                                                        </div>
                                                        <div class="flex-1 radio-list">
                                                            <select class="form-control" name="kitchenId"
                                                                    v-model="item.kitchenId">
                                                                <option value="-1">(选择厨房)</option>
                                                                <option :value="k.id" v-for="k in kitchenList">
                                                                    {{k.name}}
                                                                </option>
                                                            </select>
                                                        </div>
                                                        <div class="flex-1">
                                                            <button class="btn red" type="button"
                                                                    @click="removeMealItem(attr,item)">移除
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="col-md-4 col-md-offset-8">
                                                    <button class="btn btn-block blue" type="button"
                                                            @click="addMealItem(attr)"><i class="fa fa-cutlery"></i>
                                                        添加{{attr.name}}
                                                    </button>
                                                </div>
                                                <div class="clearfix"></div>
                                            </div>
                                        </div>
                                        <div class="col-md-4 col-md-offset-4">
                                            <button class="btn btn-block blue" type="button" @click="addMealAttr">
                                                <i class="fa fa-plus"></i>
                                                添加套餐属性
                                            </button>
                                        </div>
                                        <div class="clearfix"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="clearfix"></div>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" name="id" v-model="m.id"/>
                        <button type="button" class="btn btn-default" @click="cancel">取消</button>
                        <button type="submit" class="btn btn-primary">保存</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="article-choice-dialog" v-if="showform&&choiceArticleShow.show">
        <div class="modal-dialog " style="width:90%;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">添加 {{choiceArticleShow.mealAttr.name}} 菜品项</h4>
                </div>
                <div class="modal-body auto-height">
                    <div class="row">
                        <div class="col-md-6">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th>
                                        <select v-model="choiceArticleShow.currentFamily">
                                            <option value="">餐品分类(全部)</option>
                                            <option :value="f.name" v-for="f in articlefamilys">{{f.name}}</option>
                                        </select>
                                    </th>
                                    <th>餐品名称</th>
                                    <th>添加</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr v-for="art in choiceArticleCanChoice">
                                    <td>{{art.articleFamilyName}}</td>
                                    <td>{{art.name}}</td>
                                    <td>
                                        <button class="btn blue" type="button" @click="addArticleItem(art)">添加</button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="col-md-6">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th>餐品名称(已添加)</th>
                                    <th>移除</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr v-for="art in choiceArticleShow.items">
                                    <td>{{art.articleName}}</td>
                                    <td>
                                        <button class="btn red" type="button" @click="removeArticleItem(art)">移除
                                        </button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn green" @click="updateAttrItems">确定</button>
                </div>
            </div>
        </div>
    </div>
    <div class="table-div">
        <div class="table-operator">
            <s:hasPermission name="article/add">
                <button class="btn blue" @click="create(2)">新建套餐</button>
                <button class="btn green" @click="create(1)">新建餐品</button>
            </s:hasPermission>
            <div class="clearfix"></div>
        </div>
        <div class="table-filter form-horizontal">
        </div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>
</div>


<script>

    var action;
    function checkSort(t) {
        if ($(t).val() == '') {
            return;
        }
        var v = $(t).val();
        var count = 0;
        var attr = document.getElementsByName("printSort");
        for (var i = 0; i < attr.length; i++) {
            if (v == attr[i].value) {
                count++;
            }
        }

        if (count > 1) {
            alert("打印排序添加重复");
            $(t).val('');
        }

    }


    Vue.config.debug = true;
    (function () {
        var cid = "#control";
        var $table = $(".table-body>table");
        var allArticles = [];
        var articleType = {
            1: "单品",
            2: "套餐"
        }
        var tb = $table.DataTable({
            "lengthMenu": [[50, 75, 100, 150], [50, 75, 100, "All"]],
            ajax: {
                url: "article/list_all",
                dataSrc: ""
            },
            stateSave: true,
            deferRender: true,
            ordering: false,
            columns: [
                {
                    title: "餐品类别",
                    data: "articleFamilyName",
                    s_filter: true,
                },
                {
                    title: "类型",
                    data: "articleType",
                    createdCell: function (td, tdData) {
                        $(td).html(articleType[tdData]);
                    },
                    s_filter: true,
                    s_render: function (d) {
                        return articleType[d];
                    }
                },
                {
                    title: "名称",
                    data: "name",
                },
                {
                    title: "价格",
                    data: "price",
                },
                {
                    title: "粉丝价",
                    data: "fansPrice",
                    defaultContent: "",
                },
                {
                    title: "图片",
                    data: "photoSmall",
                    defaultContent: "",
                    createdCell: function (td, tdData) {
                        $(td).html("<img src='/" + tdData + "' style='height:40px;width:80px;'/>")
                    }
                },
                {
                    title: "排序",
                    data: "sort",
                },
                {
                    title: "上架",
                    data: "activated",
                    s_filter: true,
                    s_render: function (d) {
                        return d ? "是" : "否"
                    },
                    createdCell: function (td, tdData) {
                        $(td).html(tdData ? "是" : "否");
                    }
                },
                {
                    title: "沽清",
                    data: "isEmpty",
                    s_filter: true,
                    s_render: function (d) {
                        return d ? "是" : "否"
                    },
                    createdCell: function (td, tdData) {
                        $(td).html(tdData ? "是" : "否");
                    }
                },
                {
                    title: "工作日库存",
                    data: "stockWorkingDay",
                    defaultContent: "0"
                },
                {
                    title: "周末库存",
                    data: "stockWeekend",
                    defaultContent: "0"
                },
                {
                    title: "操作",
                    data: "id",
                    createdCell: function (td, tdData, rowData, row) {
                        var operator = [
                            <s:hasPermission name="article/delete">
                            C.createDelBtn(tdData, "article/delete"),
                            </s:hasPermission>
                            <s:hasPermission name="article/edit">
                            C.createEditBtn(rowData),
                            </s:hasPermission>
                        ];
                        $(td).html(operator);
                    }
                }],
            initComplete: function () {
                var api = this.api();
                api.search('');
                var data = api.data();
                for (var i = 0; i < data.length; i++) {
                    allArticles.push(data[i]);
                }
                var columnsSetting = api.settings()[0].oInit.columns;
                $(columnsSetting).each(function (i) {
                    if (this.s_filter) {
                        var column = api.column(i);
                        var title = this.title;
                        var select = $('<select><option value="">' + this.title + '(全部)</option></select>');
                        var that = this;
                        column.data().unique().each(function (d) {
                            select.append('<option value="' + d + '">' + ((that.s_render && that.s_render(d)) || d) + '</option>')
                        });

                        select.appendTo($(column.header()).empty()).on('change', function () {
                            var val = $.fn.dataTable.util.escapeRegex(
                                    $(this).val()
                            );
                            column.search(val ? '^' + val + '$' : '', true, false).draw();
                        });
                    }
                });
            }
        });
        var C = new Controller(null, tb);
        var vueObj = new Vue({
                    el: "#control",
                    mixins: [C.formVueMix],
                    data: {
                        articlefamilys: [],
                        recommendList: [],
                        supportTimes: [],
                        kitchenList: [],
                        checkedUnit: [],
                        articleattrs: [],
                        unitList: [],
                        selectedUnit: new HashMap(),
                        articleunits: {},
                        unitPrices: [],
                        mealtempList: [],
                        choiceTemp: "",
                        lastChoiceTemp: "",
                        allArticles: allArticles,
                        choiceArticleShow: {show: false, mealAttr: null, items: [], currentFamily: ""}
                    },
                    methods: {
                        itemDefaultChange: function (attr, item) {
                            if(item.isDefault){
                                item.isDefault = false;
                            }else{
                                item.isDefault = true;
                            }

//                            for (var i in attr.mealItems) {
//                                var m = attr.mealItems[i];
//                                if (m != item) {
//                                    m.isDefault = false;
//                                }
//                            }
                        },
                        updateAttrItems: function () {
                            this.choiceArticleShow.mealAttr.mealItems = $.extend(true, {}, this.choiceArticleShow).items;
                            $("#article-choice-dialog").modal('hide');
                        },
                        removeMealItem: function (attr, item) {
                            attr.mealItems.$remove(item);
                        },
                        removeArticleItem: function (mealItem) {
                            this.choiceArticleShow.items.$remove(mealItem);
                        },

                        changeUsed: function (select, item, type) {
                            var use;
                            if (item.isUsed == 0 || !item.isUsed) {
                                use = 1;
                                item.isUsed = 1;

                            } else {
                                use = 0;
                                item.isUsed = 0;
                            }
                            for (var i = 0; i < this.selectedUnit.unitList.length; i++) {
                                if (this.selectedUnit.unitList[i].id == select.id) {
                                    for (var k = 0; i < this.selectedUnit.unitList[i].details.length; k++) {
                                        if (this.selectedUnit.unitList[i].details[k].id == item.id) {
                                            this.selectedUnit.unitList[i].details[k].isUsed = use;
                                            break;
                                        }

                                    }
                                }
                            }
                        },
                        addArticleItem: function (art) {
                            var item = {
                                name: art.name,
                                sort: art.sort,
                                articleName: art.name,
                                priceDif: 0,
                                articleId: art.id,
                                photoSmall: art.photoSmall,
                                isDefault: false,
                            };
                            console.log(this.choiceArticleShow.items.length);
                            if (!this.choiceArticleShow.items.length) {
                                item.isDefault = true;
                            }
                            this.choiceArticleShow.items.push(item);
                        },
                        clickUnit: function (attr) {
                            if (!this.selectedUnit.unitList) {
                                this.selectedUnit.unitList = [];
                            }

                            var contains = false;
                            for (var i = 0; i < this.selectedUnit.unitList.length; i++) {

                                if (this.selectedUnit.unitList[i].id == attr.id) {
                                    contains = true;
                                    this.selectedUnit.unitList.$remove(attr);
                                    this.selectedUnit.unitList.$remove(this.selectedUnit.unitList[i]);
                                    break;
                                }
                            }

                            if (!contains) {
                                for (var i = 0; i < attr.details.length; i++) {
                                    attr.details[i].isUsed = 1;
                                    attr.details[i].price = null;
                                }
                                this.selectedUnit.unitList.push(attr);
                            }


                        },
                        addMealItem: function (meal) {
                            this.choiceArticleShow.show = true;
                            this.choiceArticleShow.mealAttr = meal;
                            this.choiceArticleShow.items = $.extend(true, {}, meal).mealItems || [];
                            this.$nextTick(function () {
                                $("#article-choice-dialog").modal('show');
                                var that = this;
                                $("#article-choice-dialog").on('hidden.bs.modal', function () {
                                    that.choiceArticleShow.show = false;
                                });
                            })
                        },

                        delMealAttr: function (meal) {
                            this.m.mealAttrs.$remove(meal);
                        }
                        ,
                        choiceTypeChange: function (attr) {
                            if (attr.choiceType == 1) {

                            }else{

                            }
                        },
                        addMealAttr: function () {
                            var sort = this.maxMealAttrSort + 1;
                            this.m.mealAttrs.push({
                                name: "套餐属性" + sort,
                                sort: sort,
                                mealItems: [],
                            });
                        }
                        ,
                        choiceMealTemp: function (e) {
                            var that = this;
                            C.confirmDialog("切换模板后，所有套餐编辑的内容将被清空，你确定要切换模板吗?", "提示", function () {
                                that.lastChoiceTemp = $(e.target).val();
                                var mealAttrs = [];
                                for (var i = 0; i < that.mealtempList.length; i++) {
                                    var temp = that.mealtempList[i];
                                    if (temp.id == that.lastChoiceTemp) {
                                        for (var n = 0; n < temp.attrs.length; n++) {
                                            var attr = temp.attrs[n];
                                            mealAttrs.push({
                                                name: attr.name,
                                                sort: attr.sort,
                                                mealItems: [],
                                            });
                                        }
                                        that.m.mealAttrs = mealAttrs;
                                        return false;
                                    }
                                }
                                that.m.mealAttrs = [];
                            }, function () {
                                that.choiceTemp = that.lastChoiceTemp.toString();
                            });
                        }
                        ,
                        selectAllTimes: function (m, e) {
                            var isCheck = $(e.target).is(":checked");
                            if (isCheck) {
                                for (var i = 0; i < this.supportTimes.length; i++) {
                                    var t = this.supportTimes[i];
                                    m.supportTimes.push(t.id);
                                }
                            } else {
                                m.supportTimes = [];
                            }
                        }
                        ,
                        create: function (article_type) {
                            var that = this;
                            action = "create";
                            this.m = {
                                articleFamilyId: this.articlefamilys[0].id,
//                                recommendId:this.recommendList[0].id,
                                supportTimes: [],
                                kitchenList: [],
                                mealAttrs: [],
                                isRemind: false,
                                activated: true,
                                showDesc: true,
                                showBig: true,
                                isEmpty: false,
                                stockWorkingDay: 100,
                                stockWeekend: 50,
                                sort: 0,
                                units: [],
                                articleType: article_type,
                            };

                            this.showform = true;
                            this.selectedUnit = [];


                            var list = {
                                unitList: []
                            }
                            this.selectedUnit = list;
                            $.post("unit/list_all", null, function (data) {
                                that.unitList = data;
                            });

                        }
                        ,
                        uploadSuccess: function (url) {
                            console.log(url);
                            $("[name='photoSmall']").val(url).trigger("change");
                            C.simpleMsg("上传成功");
                            $("#photoSmall").attr("src", "/" + url);
                        }
                        ,
                        uploadError: function (msg) {
                            C.errorMsg(msg);
                        }
                        ,
                        edit: function (model) {
                            this.selectedUnit = [];


                            var list = {
                                unitList: []
                            }
                            this.selectedUnit = list;

                            var that = this;


                            action = "edit";

                            $.post("article/list_one_full", {id: model.id}, function (result) {
                                var article = result.data;

                                that.checkedUnit = [];
                                that.showform = true;

                                that.selectedUnit.unitList = [];
                                for (var i = 0; i < article.units.length; i++) {
                                    that.selectedUnit.unitList.push(article.units[i]);
                                }

                                article.mealAttrs || (article.mealAttrs = []);
                                that.m = article;
                                if (article.hasUnit && article.hasUnit != " " && article.hasUnit.length) {
                                    var unit = article.hasUnit.split(",");
                                    unit = $.unique(unit);
                                    for (var i in  unit) {
                                        that.checkedUnit.push(parseInt(unit[i]));
                                    }
                                }
                                that.unitPrices = article.articlePrices;
                                if (!article.kitchenList) {
                                    article.kitchenList = [];
                                }


                            });
                            $.post("unit/list_all_id", {articleId: model.id}, function (data) {
                                that.unitList = data;
                            });

                        }
                        ,
                        filterTable: function (e) {
                            var s = $(e.target);
                            var val = s.val();
                            if (val == "-1") {
                                tb.search("").draw();
                                return;
                            }
                            tb.search(val).draw();
                        }
                        ,
                        changeColor: function (val) {
                            $(".color-mini").minicolors("value", val);
                        },
                        cleanRemark: function () {
                            $("#kitchenRemark").html("");
                            $("#supportTimeRemark").html("");
                        },
                        checkNull: function () {
                            if (this.supportTimes.length <= 0) {//判断当前店铺是否创建了供应时间
                                $("#supportTimeRemark").html("<font color='red'>请先创建至少一个菜品供应时间！</span>");
                                return true;
                            }
                            if (this.m.supportTimes.length <= 0) {//供应时间 非空验证
                                $("#supportTimeRemark").html("<font color='red'>请选择餐品供应时间！</span>");
                                return true;
                            }
                          	//if (this.kitchenList.length <= 0) {//判断当前店铺是否创建了出餐厨房
                            //    $("#kitchenRemark").html("<font color='red'>请先创建至少一个出餐厨房！</span>");
                            //    return true;
                            //}
                            if (this.m.kitchenList.length <= 0) {//出餐厨房 非空验证
                                //$("#kitchenRemark").html("<font color='red'>请选择出餐厨房！</span>");
                                //return true;
                                if(!confirm("是否不选择出餐厨房！")){
                                	return true;
                                }
                            }
                            if (this.supportTimes.length <= 0) {//判断当前店铺是否创建了供应时间
                                $("#supportTimeRemark").html("<font color='red'>请先创建至少一个菜品供应时间！</span>");
                                return true;
                            }
                            if (this.m.supportTimes.length <= 0) {//供应时间 非空验证
                                $("#supportTimeRemark").html("<font color='red'>请选择餐品供应时间！</span>");
                                return true;
                            }
                          	//if (this.kitchenList.length <= 0) {//判断当前店铺是否创建了出餐厨房
                            //    $("#kitchenRemark").html("<font color='red'>请先创建至少一个出餐厨房！</span>");
                            //    return true;
                            //}
                            if (this.m.kitchenList.length <= 0) {//出餐厨房 非空验证
                                //$("#kitchenRemark").html("<font color='red'>请选择出餐厨房！</span>");
                                //return true;
                                if(!confirm("是否不选择出餐厨房！")){
                                	return true;
                                }
                            }
                            return false;
                        },
                        save: function (e) {

                            var attrs = this.m.mealAttrs;
                            for (var i = 0; i < attrs.length; i++) {
                                var attr = attrs[i];
                                var count = 0;
                                for (var k = 0; k < attr.mealItems.length; k++) {
                                    if (attr.mealItems[k].isDefault) {
                                        count++;
                                    }
                                }
                                if (attr.choiceType == 0) {
                                    if (attr.choiceCount != count) {
                                        C.errorMsg("默认选中项与必选数量不等");
                                        return;
                                    }
                                }
                            }


                            if (this.checkNull()) {//验证必选项(出参厨房和供应时间)
                                return;
                            }
                            var that = this;
                            var action = $(e.target).attr("action");
                            this.m.articlePrices = this.unitPrices;
                            this.m.hasUnit = this.checkedUnit.join() || " ";
                            this.m.units = [];
                            for (var i = 0; i < that.selectedUnit.unitList.length; i++) {
                                this.m.units.push({
                                    id: that.selectedUnit.unitList[i].id,
                                    choiceType: that.selectedUnit.unitList[i].choiceType,
                                    details: that.selectedUnit.unitList[i].details,
                                });
                            }


                            var m = this.m;

                            var jsonData = JSON.stringify(this.m);
                            $.ajax({
                                contentType: "application/json",
                                type: "post",
                                url: action,
                                data: jsonData,
                                success: function (result) {
                                    if (result.success) {
                                        that.showform = false;
                                        that.m = {};
                                        C.simpleMsg("保存成功");
                                        tb.ajax.reload(null, false);
                                    } else {
                                        C.errorMsg(result.message);
                                    }
                                },
                                error: function (xhr, msg, e) {
                                    var errorText = xhr.status + " " + xhr.statusText + ":" + action;
                                    C.errorMsg(errorText);
                                }
                            });
                        }
                    },
                    computed: {
                        choiceArticleCanChoice: function () {
                            var arts = [];
                            for (var i in this.allArticles) {
                                var art = this.allArticles[i];
                                var has = false;
                                for (var n in this.choiceArticleShow.items) {
                                    var mealItem = this.choiceArticleShow.items[n];
                                    if (mealItem.articleId == art.id) {
                                        has = true;
                                        break;
                                    }
                                }
                                if (!has && art.articleType == 1 && (this.choiceArticleShow.currentFamily == art.articleFamilyName || this.choiceArticleShow.currentFamily == "")) {
                                    arts.push(art);
                                }
                            }
                            return arts;
                        }
                        ,
                        maxMealAttrSort: function () {
                            var sort = 0;
                            for (var i in this.m.mealAttrs) {
                                var meal = this.m.mealAttrs[i];
                                if (meal.sort > sort) {
                                    sort = meal.sort;
                                }
                            }
                            return parseInt(sort);
                        }
                        ,
                        allUnitPrice: function () {
                            var result = [];
                            console.log(this.checkedUnit);
                            for (var i = 0; i < this.articleattrs.length; i++) {
                                var attr = this.articleattrs[i];
                                var checked = [];
                                if (!attr.articleUnits) {
                                    continue;
                                }
                                for (var j = 0; j < attr.articleUnits.length; j++) {
                                    var c = attr.articleUnits[j];
                                    for (var n in this.checkedUnit) {
                                        if (c.id == this.checkedUnit[n]) {
                                            checked.push({
                                                unitIds: c.id,
                                                name: "(" + c.name + ")"
                                            })
                                            break;
                                        }
                                    }
                                }
                                checked.length && result.push(checked);
                            }


                            function getAll(allData) {
                                var root = [];
                                for (var i in allData) {
                                    var currentData = allData[i];
                                    if (i > 0) {
                                        for (var p  in allData[i - 1]) {
                                            var parent = allData[i - 1][p];
                                            parent.children = currentData;
                                        }
                                    } else {
                                        root = currentData;
                                    }
                                }
                                var allItems = [];
                                for (var n in root) {
                                    var r = root[n];
                                    getTreeAll(r, allItems);
                                }
                                return allItems;
                            }

                            function getTreeAll(tree, allItems) {
                                tree = $.extend({}, tree);
                                if (!tree.children) {
                                    allItems.push($.extend({}, tree));
                                    return allItems;
                                }
                                for (var i in tree.children) {
                                    var c = tree.children[i];
                                    c = $.extend({}, c);
                                    c.unitIds = tree.unitIds + "," + c.unitIds;
                                    c.name = tree.name + c.name;
                                    if (!c.children) {
                                        allItems.push(c);
                                    } else {
                                        getTreeAll(c, allItems);
                                    }
                                }
                                return allItems;
                            }

                            var allItems = getAll(result);
                            for (var i in allItems) {
                                var item = allItems[i];
                                for (var i in this.unitPrices) {
                                    var p = this.unitPrices[i];
                                    if (item.unitIds == p.unitIds) {
                                        item = $.extend(item, p);
                                    }
                                }
                            }
                            this.unitPrices = allItems;

                            return allItems;
                        }
                    }
                    ,
                    created: function () {
                        tb.search("").draw();
                        var that = this;
                        this.$watch("showform", function () {
                            if (this.showform) {
                                $("#article-dialog").modal("show");
                                var n = $('.color-mini').minicolors({
                                    change: function (hex, opacity) {
                                        if (!hex) return;
                                        if (typeof console === 'object') {
                                            $(this).attr("value", hex);
                                        }
                                    },
                                    theme: 'bootstrap'
                                });
                                $("#article-dialog").on("hidden.bs.modal", function () {
                                    that.showform = false;
                                });
                            } else {
                                $("#article-dialog").modal("hide");
                                $(".modal-backdrop.fade.in").remove();
                            }
                        });

                        this.$watch("m", function () {
                            if (this.m.id) {
                                $('.color-mini').minicolors("value", this.m.controlColor);
                            }
                        });


                        $.post("articlefamily/list_all", null, function (data) {
                            that.articlefamilys = data;
                        });

                        $.post("recommend/list_all", null, function (data) {
                            that.recommendList = data;
                        });

                        $.post("supporttime/list_all", null, function (data) {
                            that.supportTimes = data;
                        });
                        $.post("kitchen/list_all", null, function (data) {
                            that.kitchenList = data;
                        });
                        $.post("mealtemp/list_all", null, function (data) {
                            that.mealtempList = data;
                        });
                        $.post("articleattr/list_all", null, function (data) {
                            var article_units = {};
                            for (var i in data) {
                                var attr = data[i];
                                attr.checkedUnit = [];
                                var units = attr.articleUnits;
                                for (var i in units) {
                                    var unit = units[i];
                                    unit.attr = attr;
                                    article_units[unit.id] = unit;
                                }
                            }
                            that.articleunits = article_units;
                            that.articleattrs = data;
                        });
                    }
                })
                ;
        C.vue = vueObj;

    }());


</script>
