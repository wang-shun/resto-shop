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
	<strong>店铺充值记录</strong>
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
<!-- 店铺充值详细列表  -->
<div class="panel panel-info">
	<div class="panel-heading text-center" style="font-size: 22px;">
		<strong>店铺充值记录</strong>
	</div>
	<div class="panel-body">
		<table class="table table-striped table-bordered table-hover"
			id="shopOrder">
		</table>
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
		                { "width": "8%", "targets":4  },
		                { "width": "6%", "targets":5  },
		                { "width": "6%", "targets":6  },
		              ],
		"lengthMenu" : [ [ 15,50, 75, 100, -1 ], [15, 50, 75, 100, "All" ] ],
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
		order: [[2,'desc'],[ 1, 'desc' ]],
		columns : [ {
			title : "店铺",
			data : "shopName",
		},

		{
			title : "充值方式",
			data : "beginTime",
			createdCell : function(td, tdData) {
				$(td).html(new Date(tdData).format("yyyy-MM-dd hh:mm:ss"))
			}
		}, {
			title : "充值手机",
			data : "telephone",
			createdCell:function(td,tdData){
				if(tdData=="" || tdData==null){
					$(td).html("<span class='label label-danger'>没有填写</span>");
				}
			}
		}, {
			title : "充值金额（元）",
			data : "orderMoney"
		},
			{
				title : "充值赠送金额(元)",
				data : "orderMoney"
			},
			{
			title : "充值时间",
			data : "weChatPay",
            createdCell:function (td,tdData,row) {
			    console.log(row);
                if(row.childOrder==true&&row.orderMode==5){
                    $(td).html("--")
                }
            }

		},{
			title : "操作人手机",
			data : "orderState",
			createdCell : function(td,tdData){
				if(tdData == "异常订单"){
					$(td).html("<span class='label label-danger'>订单异常</span>");
				}
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
