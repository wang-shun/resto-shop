<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<form role="form" action="data_share/edit">
	<div class="form-body">
		<div class="form-group">
		    <label>分享标题</label>
		    <input type="text" class="form-control" name="shareTitle" v-model="m.shareTitle">
		</div>
		<div class="form-group">
		    <label>分享图标</label>
		    <input type="text" class="form-control" name="shareIcon" v-model="m.shareIcon">
		</div>
		<div class="form-group">
		    <label>最低分享分数</label>
		    <input type="text" class="form-control" name="minLevel" v-model="m.minLevel">
		</div>
		<div class="form-group">
		    <label>最少分享字数</label>
		    <input type="text" class="form-control" name="minLength" v-model="m.minLength">
		</div>
		<div class="form-group">
		    <label>返利(%)</label>
		    <input type="text" class="form-control" name="rebate" v-model="m.rebate">
		</div>
		<div class="form-group">
		    <label>是否启用</label>
		    <input type="text" class="form-control" name="isActivity" v-model="m.isActivity">
		</div>
		<div class="form-group">
		    <label>分享弹窗文本</label>
		    <textarea class="ueditor-textarea" name="dialogText">{{m.dialogText}}</textarea>
		</div>
	</div>
</form>
<script>
	var obj = new Vue({
		
	});
</script>