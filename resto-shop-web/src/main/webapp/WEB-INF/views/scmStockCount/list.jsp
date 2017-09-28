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

    .col-md-12{
         margin:3px;
        text-align: left;
    }
</style>
<div id="control">
    <div class="modal fade" id="article-dialog" v-show="showform">
        <div class="modal-dialog " style="width:50%;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" style="text-align:center">入库单详情</h4>
                </div>
                <div class="form-horizontal" role="form"
                      @submit.prevent="save">
                    <div class="modal-body auto">
                        <div class="form-body" >
                            <div class="col-md-12">
                                <div class="col-md-6 ">
                                    <div class="col-md-3">盘点单号</div>
                                    <div class="col-md-3" id="pddh"></div>
                                </div>
                                <%--<div class="col-md-6">--%>
                                    <div class="col-md-2">盘点时间</div>
                                    <div class="col-md-4" id="pdsj"></div>
                                <%--</div>--%>
                        </div>
                            </div>
                            <div class="col-md-12 ">
                                <div class="col-md-6">
                                    <div class="col-md-3">类型</div>
                                    <div class="col-md-3" id="lx"></div>
                                </div>
                                <%--<div class="col-md-6">--%>
                                    <div class="col-md-2">物料种类</div>
                                    <div class="col-md-4" id="zl"></div>
                                <%--</div>--%>
                                <div class="clearfix"></div>
                            </div>
                            <div class="col-md-12 ">
                                <div class="col-md-6 ">
                                    <div class="col-md-3">盘点人</div>
                                    <div class="col-md-3" id="pdr"></div>
                                </div>
                                <%--<div class="col-md-6 ">--%>
                                    <div class="col-md-2">备注</div>
                                    <div class="col-md-4" id="bz"></div>
                                <%--</div>--%>
                                <div class="clearfix"></div>
                            </div>
                            <div class="col-md-12">
                                <div class="col-md-6">
                                    <div class="col-md-3">入库人</div>
                                    <div class="col-md-3"></div>
                                </div>
                                <%--<div class="col-md-6 ">--%>
                                    <div class="col-md-2">审核人</div>
                                    <div class="col-md-4"></div>
                                <%--</div>--%>
                            </div>
                            <div id="pandian">
                                <table class="table table-bordered" >
                                    <thead>
                                    <tr>
                                        <th>类型</th>
                                        <th>一级类别</th>
                                        <th>二级类别</th>
                                        <th>品牌名</th>
                                        <th>材料名</th>
                                        <th>编码</th>
                                        <th>规格</th>
                                        <th>产地</th>
                                        <th>理论库存</th>
                                        <th>盘点库存</th>
                                        <th>差异数量</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr v-for="(index,item) in parameter.stockCountDetailList">
                                        <td>{{index+1}}</td>
                                        <td>{{item.materialType}}</td>
                                        <td>{{item.INGREDIENTS}}</td>
                                        <td>{{item.materialName}}</td>
                                        <td>{{item.unitName}}</td>
                                        <td>{{item.minMeasureUnit}}</td>
                                        <td>{{item.minMeasureUnit}}</td>
                                        <td>{{item.minMeasureUnit}}</td>
                                        <td>{{item.minMeasureUnit}}</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>

                        </div>
                    </div>
                </div>
                    </div>
                    <div class="modal-footer" style="border:0;text-align:center;">
                        <input type="hidden" name="id" v-model="m.id"/>
                        <button type="button" class="btn btn-default" @click="cancel">关闭</button>
                    </div>
            </div>


    <div class="modal fade" id="article-choice-dialog" v-if="showform&&choiceArticleShow.show">
        <div class="modal-dialog " style="width:90%;">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">添加 菜品项</h4>
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
                                <tr v-for="art in choiceArticleShow.itemsLess">
                                    <td>{{art.articleFamilyName}}</td>
                                    <td>{{art.name}}</td>
                                    <td>
                                        <button class="btn blue" type="button" @click="addArticleItem(art)">添加</button>
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
            <s:hasPermission name="unit/add">
                <button class="btn green pull-right" @click="create(2)">新建</button>
            </s:hasPermission>
        </div>
        <div class="clearfix"></div>
        <div class="table-filter"></div>
        <div class="table-body">
            <table class="table table-striped table-hover table-bordered "></table>
        </div>
    </div>

<div class="modal fade">
	<div class="modal-dialog modal-lg">
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
</div>
</div>
<script>
    (function () {
        var cid = "#control";
        var action;
        var unitId = null;

        var $table = $(".table-body>table");
        var allArticles = [];
        var articleList = [];
        var all = [];
        var articleType = {
            1: "单品",
            2: "套餐"
        }


        var tb = $table.DataTable({
            ajax: {
                url: "scmStockCount/list_all",
                dataSrc: "data"
            },
            columns: [
                {
                    title: "盘点单号",
                    data: "id",
                },
                {
                  title:"盘点单名",
                    data:"orderName"
                },
                {
                  title:"盘点日期",
                    data:"publishedTime"
                },
                {
                    title:"盘点人",
                    data:"createrName"
                },
                {
                    title:"备注",
                    data:"orderStatus"
                },
                {
                    title : "操作",
                    defaultContent:"",
                    createdCell:function(td,tdData,rowData){
                        var button = $("<button class='btn btn-xs btn-primary'>查看</button>");
                        button.click(function(){
                            showDetails(rowData);
                        })
                        $(td).html(button);
                    }
                }
                <%--,--%>
                 <%--{--%>
                    <%--title : "操作",--%>
                    <%--data : "id",--%>
                    <%--createdCell:function(td,tdData,rowData,row){--%>
                        <%--var operator=[--%>
                            <%--<s:hasPermission name="scmStockCount/find">--%>
                            <%--C.findBtn(rowData),                           --%>
                            <%--<s:hasPermission name="scmMaterial/delete">--%>
                            <%--C.createDelBtn(tdData,"scmMaterial/delete"),--%>
                            <%--</s:hasPermission>--%>
                            <%--</s:hasPermission>--%>
                            <%----%>
                        <%--];--%>
                        <%--$(td).html(operator);--%>
                    <%--}--%>
                <%--}--%>
                ],
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
                        parameter:{
                            materialType:'',//类型
                            categoryOneName:'',//一级分类
                            categoryTwoName:'',//二级分类

                        }
                    },
                    methods: {
                        itemDefaultChange: function (attr, item) {
                            for (var i in attr.mealItems) {
                                var m = attr.mealItems[i];
                                if (m != item) {
                                    m.isDefault = false;
                                }
                            }
                        },

                        updateAttrItems: function () {
//                            this.choiceArticleShow.mealAttr.mealItems = $.extend(true, {}, this.choiceArticleShow).items;
                            var items = $.extend(true, {}, this.choiceArticleShow).items;
                            this.articles = [];
                            for (var i = 0; i < items.length; i++) {
                                this.articles.push(items[i]);
                            }

                            $("#article-choice-dialog").modal('hide');
                        },
                        removeMealItem: function (item) {
                            this.unit.detailList.$remove(item);
//                            this.unit.detailList.$remove(attr);

//                            articleList.push(attr);

//                            this.choiceArticleShow.items = this.articles;

                        },
                        removeArticleItem: function (mealItem) {
                            this.choiceArticleShow.items.$remove(mealItem);
                            articleList.push(mealItem);
                        },

                        addArticleItem: function (art) {
                            var item = {
                                name: art.name,
                                sort: art.sort,
                                articleName: art.name,
//                                priceDif: 0,
                                articleId: art.id,
//                                photoSmall: art.photoSmall,
                                isDefault: false,
                                price: art.price,
                                articleFamilyName: art.articleFamilyName
                            };

                            for (var i = 0; i < articleList.length; i++) {
                                if (articleList[i].id == art.id) {
                                    articleList.$remove(art);
                                }
                            }

                            if (!this.choiceArticleShow.items.length) {
                                item.isDefault = true;
                            }
                            this.choiceArticleShow.items.push(item);
                        },
                        addItem: function () {
                            if(!this.unit.detailList){
                                this.unit.detailList = [{
                                    name : null,
                                    sort : null
                                }];
                            }else{
                                this.unit.detailList.push({
                                    name : null,
                                    sort : null
                                });
                            }


//
//                            if(attr.detailList){
//                                attr.detailList.push({
//                                    name: null,
//                                    spread: null,
//                                    sort: null
//                                });
//                            }else{
//                                attr.detailList = [{ name: null,
//                                    spread: null,
//                                    sort: null}];
//                            }



                        },
                        addMealItem: function (arr) {
//                            this.choiceArticleShow.show = true;
//                            this.choiceArticleShow.items = this.articles;
//                            this.choiceArticleShow.itemsLess = articleList;
                            var len = this.unit.familyList.length;
                            var family = {
                                no: len,
                                name: null,
                                sort: null,
                                type: null,
                                detailList: []
                            }

                            this.unit.familyList.push(family);
//                            this.$nextTick(function () {
//                                $("#article-choice-dialog").modal('show');
//                                var that = this;
//                                $("#article-choice-dialog").on('hidden.bs.modal', function () {
//                                    that.choiceArticleShow.show = false;
//                                });
//                            })
                        },

                        delMealAttr: function (meal) {
                            this.unit.familyList.$remove(meal);
                        }
                        ,
                        addMealAttr: function () {
                            var sort = this.maxarticlesort + 1;
                            this.m.articles.push({
                                article_name: "餐品属性" + sort,
                                sort: sort,
                                mealItems: [],
                            });
                        }
                        ,
                        choiceMealTemp: function (e) {
                            var that = this;
                            C.confirmDialog("切换模板后，所有套餐编辑的内容将被清空，你确定要切换模板吗?", "提示", function () {
                                that.lastChoiceTemp = $(e.target).val();
                                var articles = [];
                                for (var i = 0; i < that.mealtempList.length; i++) {
                                    var temp = that.mealtempList[i];
                                    if (temp.id == that.lastChoiceTemp) {
                                        for (var n = 0; n < temp.attrs.length; n++) {
                                            var attr = temp.attrs[n];
                                            articles.push({
                                                name: attr.name,
                                                sort: attr.sort,
                                                mealItems: [],
                                            });
                                        }
                                        that.m.articles = articles;
                                        return false;
                                    }
                                }
                                that.m.articles = [];
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


//                            this.unit = null;

                            var unit = {
                                detailList : []
                            }




                            this.unit = unit;

                            action = "create";
                            unitId = null;
                            this.m = {
                                articleFamilyId: this.articlefamilys[0].id,
                                articleList: [],
//                                supportTimes: [],
//                                kitchenList: [],

                                isRemind: false,
                                activated: true,
                                showDesc: true,
                                showBig: true,
                                isEmpty: false,
//                                sort: 0,
//                                articleType: article_type,
                            };
                            this.checkedUnit = [];
                            this.showform = true;
//                            articleList = [];
                            this.articles = [];
                            articleList = all.concat();


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
                            var that = this;
                            action = "edit";

                            unitId = model.id;
                            that.showform = true;
                            $.post("unit/getUnitById", {id: model.id}, function (result) {
                                $('#uName').val(result.name);
                                $('#uSort').val(result.sort);
                                var arr = [];
                                for (var i = 0; i < result.details.length; i++) {

                                    var detail = {
                                        id:result.details[i].id,
                                        no: i,
                                        name: result.details[i].name,
                                        sort: result.details[i].sort

                                    }
                                    arr.push(detail);
                                }
                                that.unit = null;
                                var unit = {
                                    detailList : arr
                                }
                                that.unit = unit;
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
                        }
                        ,

                        save: function (e) {
                            var that = this;
                            this.m.prices = this.unitPrices;
                            this.m.hasUnit = this.checkedUnit.join() || " ";
                            var m = this.m;

                            var data = {
                                id: unitId,
                                name : $('#uName').val(),
                                sort : $('#uSort').val()
                            };

                            data.details = this.unit.detailList;
                            
//                            if($('#id').val())
                            var jsonData = JSON.stringify(this.data);
                            var url;
                            if (action == "edit") {
                                url = "unit/modify";
                            } else {
                                url = "unit/create";
                            }

                            $.ajax({
                                contentType: "application/json",
                                type: "post",
                                url: url,
                                data: JSON.stringify(data),
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
                        maxarticlesort: function () {
                            var sort = 0;
                            for (var i in this.m.articles) {
                                var meal = this.m.articles[i];
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


                        $.post("article/singo_article", null, function (data) {
                            that.articleList = data;
                            articleList = data;
                            all = data;
                        });
                        $.post("articlefamily/list_all", null, function (data) {
                            that.articlefamilys = data;
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
        //用于显示描述查看
        function showDetails(data){
            debugger
            console.log()
            $("#pddh").html(data.id);//盘点单号
            $("#pdsj").html(data.publishedTime);//盘点时间
            $("#lx").html(data.materialType);//类型
            $("#zl").html(data.size);//物料种类
            $("#pdr").html(data.createrName);//盘点人
            $("#bz").html(data.createrName);//备注
            var html='';
            for(var i=0;i<data.stockCountDetailList.length;i++){
                html+='<tr><td>'+data.stockCountDetailList[i].materialType+'</td><td>'+data.stockCountDetailList[i].categoryOneName+'</td>+' +
                    '<td>'+data.stockCountDetailList[i].categoryTwoName+'</td><td>'+data.stockCountDetailList[i].defferentDeason+'</td>+' +
                    '<td>'+data.stockCountDetailList[i].materialName+'</td><td>'+data.stockCountDetailList[i].materialCode+'</td>+' +
                    '<td>'+data.stockCountDetailList[i].unitName+'</td><td>'+data.stockCountDetailList[i].produceArea+'</td>+' +
                    '<td>'+data.stockCountDetailList[i].theoryStockCount+'</td><td>'+data.stockCountDetailList[i].actStockCount+'</td>+' +
                    '<td>'+data.stockCountDetailList[i].actStockCount+'</td></tr>'
            }
            $("#pandian tbody").html(html);
            vueObj.showform=true;
        }
    }());
</script>
