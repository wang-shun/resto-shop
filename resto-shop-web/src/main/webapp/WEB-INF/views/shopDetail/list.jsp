@<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags"%>

<div id="control">
	<div class="row form-div">
		<div class="col-md-offset-3 col-md-6">
			<div class="portlet light bordered">
				<div class="portlet-title">
					<div class="caption">
						<span class="caption-subject bold font-blue-hoki"> 表单</span>
					</div>
				</div>
				<div class="portlet-body">
					<form role="form" action="{{'shopdetail/modify'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
								<label>店铺名称</label> 
								<input type="text" class="form-control" name="name" :value="m.name" placeholder="必填" required="required">
							</div>
							<div class="form-group">
								<label>店铺电话</label> 
								<input type="text" class="form-control" name="phone" :value="m.phone">
							</div>
							<div class="form-group">
								<label>店铺地址</label> 
								<input type="text" class="form-control" name="address" :value="m.address" @blur="showjwd" placeholder="必填" required="required">
							</div>
							<div class="form-group">
								<label>经度</label> 
								<input type="text" class="form-control" name="longitude" :value="m.longitude">
							</div>
							<div class="form-group">
								<label>纬度</label> 
								<input type="text" class="form-control" name="latitude" :value="m.latitude">
							</div>
							<div class="form-group">
                                       <labe>营业时间</label>
                                          <div class="input-group">
                                        <input type="text" class="form-control timepicker timepicker-no-seconds" name="openTime" @focus="initTime" :value="m.openTime" readonly="readonly">
                                        <span class="input-group-btn">
                                            <button class="btn default" type="button">
                                                <i class="fa fa-clock-o"></i>
                                            </button>
                                        </span>
                                    </div>
                            </div>
							<div class="form-group">
                                       <labe>关门时间</label>
                                          <div class="input-group">
                                        <input type="text" class="form-control timepicker timepicker-no-seconds" name="closeTime" :value="m.closeTime" @focus="initTime" readonly="readonly">
                                        <span class="input-group-btn">
                                            <button class="btn default" type="button">
                                                <i class="fa fa-clock-o"></i>
                                            </button>
                                        </span>
                                    </div>
                             </div>
						</div>
						<input class="btn green" type="submit" value="保存" /> 
						<a class="btn default" @click="cancel">取消</a>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>

<script>
	
	$(document).ready(function(){
		initContent();
		toastr.options = {
				  "closeButton": true,
				  "debug": false,
				  "positionClass": "toast-top-right",
				  "onclick": null,
				  "showDuration": "500",
				  "hideDuration": "500",
				  "timeOut": "3000",
				  "extendedTimeOut": "500",
				  "showEasing": "swing",
				  "hideEasing": "linear",
				  "showMethod": "fadeIn",
				  "hideMethod": "fadeOut"
				}
		var temp;
		var vueObj = new Vue({
			el:"#control",
			data:{
				m:{},
			},
			methods:{
				initTime :function(){
					$(".timepicker-no-seconds").timepicker({
						 autoclose: true,
						 showMeridian:false,
			             minuteStep: 5
					  });
				},
				save:function(e){
					var formDom = e.target;
					$.ajax({
						url:"shopDetail/modify",
						data:$(formDom).serialize(),
						success:function(result){
							if(result.success){
								toastr.clear();
								toastr.success("保存成功！");
							}else{
								toastr.clear();
								toastr.error("保存失败");
							}
						},
						error:function(){
							toastr.clear();
							toastr.error("保存失败");
						}
					})
					
				},
				cancel:function(){
					initContent();
					
				}
			}
		});
		
		function initContent(){
			$.ajax({
				url:"shopDetail/list_one",
				success:function(result){
					var tem1 = result.data.openTime;
					var tem2 = result.data.closeTime;
					var open;
	 				var close;
					open = new Date(tem1).format("hh:mm"); 
	 				close = new Date(tem2).format("hh:mm");
	 				if(open=='aN:aN'){
						open = tem1;
					}
					if(close=='aN:aN'){
						close=tem2;
	 				}
	 				result.data.openTime=open;
	 				result.data.closeTime=close;
	 				objectName = result.data;
	 				vueObj.m=result.data;
				}
			})
		}
		
		
	}());
	
</script>
