<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<h3 style="text-align: center"><STRONG>账户管理</STRONG></h3>
	<div class="row">
		<div class="col-md-4">
			<div class="panel panel-primary">
				<div class="panel-heading">
					<div  style="line-height: 34px;font-size: 22px">
						账户余额
					</div>
				</div>
				<div class="panel-body">
					<h1 style="color: #ffc459">￥{{accountBalance}}</h1>
					<div style="text-align: right">
						<button class="btn btn-success" type="button">账户充值</button>
					</div>
				</div>
			</div>
		</div>
		<div class="col-md-8">
			<div class="panel panel-primary">
				<div class="panel-heading">
					<div  style="line-height: 34px;font-size: 22px">
						今日记录
					</div>
				</div>
				<div class="panel-body">
					<div class="row">
						<div class="col-md-3">
							<h3 style="color: #ffc459">{{logInfo.registerCustomer}}</h3>
							<h3 style="color: #ffc459">注册用户</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459">{{logInfo.smsSend}}</h3>
							<h3 style="color: #ffc459">发送短信</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459">{{logInfo.orderNum}}</h3>
							<h3 style="color: #ffc459">订单数</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459">￥{{logInfo.orderMoney}}</h3>
							<h3 style="color: #ffc459">订单额</h3>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<div class="panel panel-primary">
				<div class="panel-heading">
					<div class="row">
						<div class="col-md-6" style="line-height: 34px;font-size: 22px">
							账户概要
						</div>
						<div class="col-md-6" style="text-align: right">
							<button class="btn btn-success" type="button">今日</button>
							<button class="btn btn-success" type="button">昨日</button>
							<button class="btn btn-success" type="button">本周</button>
							<button class="btn btn-success" type="button">本月</button>
							<button class="btn btn-success" type="button">查看详情</button>
						</div>
					</div>
				</div>
				<div class="panel-body">
					<div class="row">
						<div class="col-md-3">
							<h3 style="color: #ffc459">注册用户支出</h3>
							<h3 style="color: #ffc459">{{accountLog.registerCustomerOrder}}元</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459"> 短信支出</h3>
							<h3 style="color: #ffc459">{{accountLog.smsOrder}}元</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459"> 订单支出</h3>
							<h3 style="color: #ffc459">{{accountLog.orderMoney}}元</h3>
						</div>
						<div class="col-md-3">
							<h3 style="color: #ffc459">账户充值</h3>
							<h3 style="color: #ffc459">￥+{{accountLog.chargeMoney}}元</h3>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>


<script>
    (function () {
        var vueObj = new Vue({
			el:"#control",
			data:{
                accountBalance:0,
				logInfo:{
                    registerCustomer:0,//注册用户数
					smsSend:0,//发短信数
					orderNum:0,//订单数
					orderMoney:0//订单总额
				},
				accountLog:{
                    registerCustomerOrder:0,//注册用户支出
					smsOrder:0, //短信支出
					orderMoney:0, //订单支出
					chargeMoney:0 //账户充值
				}
			},
			methods:{

			},
			created:function () {
			    var that = this;

			    $.ajax({
					url:"brandaccount/initData",
					data:{

					},
					success:function (result) {
						var obj = result.data;
                        //初始化账户余额
                        that.accountBalance = obj.accountBalance;
                        //初始化今日记录
                        that.logInfo.registerCustomer = obj.registerCustoemrNum;
                        that.logInfo.smsSend = obj.smsNum;
                        that.logInfo.orderNum = obj.orderNum;
                        that.logInfo.orderMoney = obj.orderMoney;

                        //初始化账户概要
                        that.accountLog.registerCustomerOrder = obj.registerCustomerMoney;
                        that.accountLog.smsOrder = obj.smsMoney;
                        that.accountLog.orderMoney = obj.orderOutMoney;
                        that.accountLog.chargeMoney = obj.brandAccountCharge
                    }
				});
            }


		})



    }());

</script>
