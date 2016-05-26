 package com.resto.shop.web.controller.business;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.junit.runners.Parameterized.Parameter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.exception.AppException;
import com.resto.brand.core.entity.Result;
import com.resto.shop.web.model.Customer;
import com.resto.shop.web.service.CustomerService;

@Controller
@RequestMapping("customer")
public class CustomerController extends GenericController{

	@Resource
	CustomerService customerService;
	
	@RequestMapping("/list")
    public void list(){
    }

	@RequestMapping("/list_all")
	@ResponseBody
	public List<Customer> listData(){
		return customerService.selectListByBrandId(getCurrentBrandId());
	}
	
	@RequestMapping("list_one")
	@ResponseBody
	public Result list_one(String id){
		Customer customer = customerService.selectById(id);
		return getSuccessResult(customer);
	}
	
	@RequestMapping("create")
	@ResponseBody
	public Result create(@Valid Customer brand){
		customerService.insert(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public Result modify(@Valid Customer brand){
		customerService.update(brand);
		return Result.getSuccess();
	}
	
	@RequestMapping("delete")
	@ResponseBody
	public Result delete(String id){
		customerService.delete(id);
		return Result.getSuccess();
	}
	
	//void bindPhone(String phone, String currentCustomerId) throws AppException;
	@RequestMapping("test")
	@ResponseBody
	public Result test(@RequestParam(value="phone",defaultValue="13317182430")String phone,@RequestParam(value="customerId",defaultValue="f2361f9ef9814ddbba10c3bdb93a3bc1")String customerId) throws AppException{
		customerService.bindPhone(phone, customerId);
		return Result.getSuccess();
	}
	
	
	
	
}
