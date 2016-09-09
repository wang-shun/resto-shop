<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form role="form">
	<input type="hidden" name="id" value="${m.id }">
	<div class="form-body">
		<div class="form-group">
			<label>员工姓名</label>
			<input type="text" class="form-control" name="name" value="${m.name }">
		</div>
		<div class="form-group">
			<label>员工性别</label>
			<input type="text" class="form-control" name="sex" value="${m.sex }">
		</div>

		<div class="form-group">
			<label class="control-label">员工性别</label>
			<div class="radio-list" style="margin-left: 20px;">
				<label class="radio-inline">
					<input type="radio" class="md-radiobtn" name="timeConsType" value="男" v-model="m.sex" >男</label>
				<label class="radio-inline">
					<input type="radio" class="md-radiobtn" name="timeConsType" value="女" v-model="m.sex" >女</label>
			</div>
		</div>

		<div class="form-group">
			<label>手机号</label>
			<input type="text" class="form-control" name="telephone" value="${m.telephone }">
		</div>
		<div class="form-group">
			<label>额度</label>
			<input type="text" class="form-control" name="money" value="${m.money }">
		</div>
	</div>
</form>
