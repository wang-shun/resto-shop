<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<form role="form" action="modulelist/edit_reward">
	<div class="form-body">
		<div class="form-group">
		    <label>title</label>
		    <input type="text" class="form-control" name="title" v-model="m.title">
		</div>
		<div class="form-group">
		    <label>moneyList</label>
		    <input type="text" class="form-control" name="moneyList" v-model="m.moneyList">
		</div>
		<div class="form-group">
		    <label>minLevel</label>
		    <input type="text" class="form-control" name="minLevel" v-model="m.minLevel">
		</div>
		<div class="form-group">
		    <label>minLength</label>
		    <input type="text" class="form-control" name="minLength" v-model="m.minLength">
		</div>
		<div class="form-group">
		    <label>isActivty</label>
		    <input type="text" class="form-control" name="isActivty" v-model="m.isActivty">
		</div>
		<div class="form-group">
		    <label>brandId</label>
		    <input type="text" class="form-control" name="brandId" v-model="m.brandId">
		</div>

	</div>
	<input type="hidden" name="id" v-model="m.id" />
	<input class="btn green"  type="submit"  value="保存"/>
	<a class="btn default" @click="cancel" >取消</a>
</form>