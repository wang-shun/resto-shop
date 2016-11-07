<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>
<style>
th {
	width: 30%;
}
dt,dd{
	height: 25px;
}
</style>
<h2 class="text-center">
	<strong>订单列表</strong>
</h2>
<br />
<div class="row">
	<div class="col-md-12">
		<form class="form-inline">
			<div class="form-group" style="margin-right: 50px;">
				<label for="beginDate2">开始时间：</label> <input type="text"
					class="form-control form_datetime2" id="beginDate2"
					readonly="readonly">
			</div>
			<div class="form-group" style="margin-right: 50px;">
				<label for="endDate2">结束时间：</label> <input type="text"
					class="form-control form_datetime2" id="endDate2"
					readonly="readonly">
			</div>

			<button type="button" class="btn btn-primary" id="today">今日</button>

			<button type="button" class="btn btn-primary" id="yesterDay">昨日</button>

			<!--              <button type="button" class="btn yellow" id="benxun">本询</button> -->

			<button type="button" class="btn btn-primary" id="week">本周</button>
			<button type="button" class="btn btn-primary" id="month">本月</button>

			<button type="button" class="btn btn-primary" id="searchInfo2">查询报表</button>
			&nbsp;
			<button type="button" class="btn btn-primary" id="shopreportExcel">下载报表</button>
			<br />

		</form>
	</div>
</div>
<br />
<br />
<!-- 店铺订单列表  -->
<div class="panel panel-info">
	<div class="panel-heading text-center" style="font-size: 22px;">
		<strong>店铺订单列表</strong>
	</div>
	<div class="panel-body">
		<table class="table table-striped table-bordered table-hover"
			id="shopOrder">
		</table>
	</div>
</div>

<!-- 查看 数据库配置 详细信息  Modal  start-->
<div class="modal fade" id="orderDetail" tabindex="-1" role="dialog">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					id="closeModal2" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title text-center">
					<strong>订单详情</strong>
				</h4>
			</div>
			<div class="modal-body">
				<dl class="dl-horizontal">
					<dt>店铺名称：</dt>
					<dd id="shopName"></dd>
					<dt>订单编号：</dt>
					<dd id="orderId"></dd>
					<dt>订单时间：</dt>
					<dd id="createTime"></dd>
					<dt>就餐模式：</dt>
					<dd id="distributionMode"></dd>
					<dt>验 证 码：</dt>
					<dd id="verCode"></dd>
					<dt>手 机 号：</dt>
					<dd id="telephone"></dd>
					<dt>订单金额：</dt>
					<dd id="orderMoney"></dd>
					<dt>评&nbsp;&nbsp;价：</dt>
					<dd id="appriase"></dd>
					<dt>评价内容：</dt>
					<dd id="content"></dd>
					<dt>状&nbsp;&nbsp;态：</dt>
					<dd id="orderState"></dd>
				</dl>
			</div>
			<div class="table-scrollable">
				<table class="table table-condensed table-hover">
					<thead>
						<tr>
							<!--                            <th>餐品类型</th> -->
							<th>餐品类别</th>
							<th>餐品名称</th>
							<th>餐品单价</th>
							<th>餐品数量</th>
							<th>小记</th>
						</tr>
					</thead>
					<tbody id="articleList" style="height: 300px;">
					</tbody>
				</table>
			</div>

			<div class="modal-footer">
				<button type="button" class="btn btn-block btn-primary" data-dismiss="modal"
					id="closeModal">关闭</button>
			</div>
		</div>
	</div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
	//时间插件

	$('.form_datetime2').datetimepicker({
		endDate : new Date(),
		minView : "month",
		maxView : "month",
		autoclose : true,//选择后自动关闭时间选择器
		todayBtn : true,//在底部显示 当天日期
		todayHighlight : true,//高亮当前日期
		format : "yyyy-mm-dd",
		startView : "month",
		language : "zh-CN"
	});

	var shopId = "${shopId}"
	$("#beginDate2").val("${beginDate}");
	$("#endDate2").val("${endDate}");

	var tb1 = $("#shopOrder").DataTable({
		"scrollY": "340px",
		"autoWidth": false,
		 "columnDefs": [
		                { "width": "8%", "targets":0  },
		                { "width": "8%", "targets":1  },
		                { "width": "8%", "targets":2  },
		                { "width": "6%", "targets":3  },
		                { "width": "6%", "targets":4  },
		                { "width": "6%", "targets":5  },
		                { "width": "6%", "targets":6  },
		                { "width": "8%", "targets":7  },
		                { "width": "8%", "targets":8  },
		                { "width": "6%", "targets":9  },
		                { "width": "6%", "targets":10  },
		                { "width": "6%", "targets":11  },
		                { "width": "3%", "targets":12  },
		              ],
		"lengthMenu" : [ [ 50, 75, 100, -1 ], [ 50, 75, 100, "All" ] ],
		ajax : {
			url : "orderReport/AllOrder",
			dataSrc : "",
			data : function(d) {
				d.beginDate = $("#beginDate2").val();
				d.endDate = $("#endDate2").val();
				d.shopId = shopId;
				return d;
			}
		},
		order: [[ 1, 'desc' ]],
		columns : [ {
			title : "店铺",
			data : "shopName",
		},

		{
			title : "下单时间",
			data : "beginTime",
			createdCell : function(td, tdData) {
				$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
			}
		}, {
			title : "手机号",
			data : "telephone",
			createdCell:function(td,tdData){
				if(tdData=="" || tdData==null){
					$(td).html("<span class='label label-danger'>没有填写</span>");
				}
			}
		}, {
			title : "订单金额",
			data : "orderMoney"
		}, {
			title : "微信支付",
			data : "weChatPay"
		}, {
			title : "红包支付",
			data : "accountPay"
		}, {
			title : "优惠券支付",
			data : "couponPay"
		}, {
			title : "充值金额支付",
			data : "chargePay"
		}, {
			title : "充值赠送金额支付",
			data : "rewardPay"
		},{
                title : "等位支付",
                data : "waitRedPay",
            },
            {
			title : "营销撬动率",
			data : 'incomePrize'
		}, {
			title : "评价",
			data : "level",
		}, {
			title : "订单状态",
			data : "orderState",
			createdCell : function(td,tdData){
				if(tdData == "异常订单"){
					$(td).html("<span class='label label-danger'>订单异常</span>");
				}
			}
		}, {
			title : "操作",
			data : "orderId",
			createdCell : function(td, tdData,rowData) {
				var button = $("<button class='btn green'>详情</button>");
				button.click(function() {
//					if(rowData.telephone=="" || rowData.telephone==null){
//						toastr.error("此订单没有填写手机号！");
//						return;
//					}
					showDetails(tdData);
				})
				$(td).html(button);
			}
		} ]
	});

	$("#searchInfo2").click(function() {
		var beginDate = $("#beginDate2").val();
		var endDate = $("#endDate2").val();
		search(beginDate, endDate);
	})

	function search(beginDate, endDate) {
		var data = {
			"beginDate" : beginDate,
			"endDate" : endDate,
			"shopId" : shopId
		};
		tb1.ajax.reload();
		toastr.success("查询成功");
	}

	$("#closeModal").click(function(e) {
		e.stopPropagation();
		var modal = $("#orderDetail");
		//modal.find(".modal-body").html("");
		modal.modal("hide");
	})

	function showDetails(orderId) {
		$.ajax({
			url : 'orderReport/detailInfo',
			method : 'post',
			data : {
				"orderId" : orderId
			},
			success : function(result) {
				if (result) {
					var data = result.data;
					$("#shopName").html(data.shopName);
					$("#orderId").html(data.id);
					$("#createTime").html(
							new Date(data.createTime)
									.format("yyyy-MM-dd hh:mm:ss"));
					$("#distributionMode").html(
							getDistriubtioMode(data.distributionModeId));
					$("#verCode").html(data.verCode);
                    var telephone = ""
                    if (typeof(data.customer) !="undefined") {
                        telephone = data.customer.telephone;
                    }
                    $("#telephone").html(telephone);
					$("#orderMoney").html(data.orderMoney + "元");
// 					if (data.appraise) {
						$("#appriase").html(data.appraise!=null ? getLevel(data.appraise.level):null);
						$("#content").html(data.appraise!=null ? data.appraise.content:null);
// 					}
					$("#orderState").html(getState(data.orderState,data.productionStatus));
					$('#articleList').text("");

					for (var i = 0; i < data.orderItems.length; i++) {
						var obj = data.orderItems[i];
						var article = "<tr><td>" + obj.articleFamily.name
								+ "</td><td>" + obj.articleName + "</td><td>"
								+ obj.unitPrice + "</td><td>" + obj.count
								+ "</td><td>" + obj.finalPrice + "</td></tr>";
						$('#articleList').append(article);
					}
					//-----------------------------------------------------------------------

					// 					 var oLogin = $("#orderDetail").html();
					// 					 console.log(oLogin);

					// 					 $('body').append( oLogin );

					// 					oLogin.css('left' , ($(window).width() - oLogin.outerWidth())/2 );
					// 					oLogin.css('top' , ($(window).height() - oLogin.outerHeight())/2 );

					// 					$(window).on('scroll',function(){

					// 						oLogin.css('left' , ($(window).width() - oLogin.outerWidth())/2 );
					// 						oLogin.css('top' , ($(window).height() - oLogin.outerHeight())/2 + $(window).scrollTop() );

					// 					});
					//----------------------------------------------------------------					 					
					$("#orderDetail").modal();
				}else{
                    $("#orderState").html("<h1>没有数据</h1>");
                }

			}
		});

	}

	function getLevel(level) {
		var levelName = '';
		switch (level) {
		case 1:
			levelName = "一星";
			break;
		case 2:
			levelName = "二星";
			break;
		case 3:
			levelName = "三星";
			break;
		case 4:
			levelName = "四星";
			break;
		case 5:
			levelName = "五星";
			break;

		}
		return levelName;
	}

	function getState(state,productionStatus) {
		var orderState = '';
		switch (state) {
            case 1:
                orderState = "未支付";
                break;
            case 2:
             if(productionStatus==0){
                orderState= "已付款";
             }else if(productionStatus==2){
                 orderState = "已消费";
             }else if(productionStatus==5){
                 orderState = "异常订单";
             }
			break;
		case 9:
			orderState = "已取消";
			break;
		case 10:
			orderState = "已确认";
             if(productionStatus==5){
                 orderState = "异常订单";
             } else {
                 orderState = "已消费";
             }
			break;
		case 11:
			orderState = "已评价";
			break;
		case 12:
			orderState = "已分享";
			break;
		}
		return orderState;
	}

	function getDistriubtioMode(mode) {
		var distributionMode = ''
		switch (mode) {
		case 1:
			distributionMode = "堂吃";
			break;
		case 2:
			distributionMode = "自提外卖";
			break;
		case 3:
			distributionMode = "外带";
			break;

		}
		return distributionMode;
	}

	$("#closeModal2").click(function(e) {
		e.stopPropagation();
		var modal = $("#orderDetail");
		modal.find(".modal-body").html("");
		modal.modal("hide");
	})

	//查询今日

	$("#today").click(function() {
		var date = new Date().format("yyyy-MM-dd");
		//赋值插件上的时间
		$("#beginDate2").val(date);
		$("#endDate2").val(date);

		//查询
		search(date, date);

	})

	//查询昨日
	$("#yesterDay").click(function() {
		var beginDate = GetDateStr(-1);
		var endDate = GetDateStr(-1);

		//赋值插件上时间
		$("#beginDate2").val(beginDate);
		$("#endDate2").val(endDate);
		//查询
		search(beginDate, endDate);

	})

	//查询本周
	$("#week").click(function() {
		var beginDate = getWeekStartDate();
		;
		var endDate = new Date().format("yyyy-MM-dd");

		//赋值插件上时间
		$("#beginDate2").val(beginDate);
		$("#endDate2").val(endDate);
		//查询
		search(beginDate, endDate);

	})

	//查询本月
	$("#month").click(function() {
		var beginDate = getMonthStartDate();
		var endDate = new Date().format("yyyy-MM-dd");

		//赋值插件上时间
		$("#beginDate2").val(beginDate);
		$("#endDate2").val(endDate);
		//查询
		search(beginDate, endDate);

	})

	//下载报表
	$("#shopreportExcel").click(
			function() {
				var beginDate = $("#beginDate2").val();
				var endDate = $("#endDate2").val();
				//判断 时间范围是否合法
				if (beginDate > endDate) {
					toastr.error("开始时间不能大于结束时间");
					return;
				}

				location.href = "orderReport/shop_excel?beginDate=" + beginDate
						+ "&&endDate=" + endDate + "&&shopId=" + shopId;

			})
</script>
