package com.resto.shop.web.controller.business;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("pos")
public class PosController extends GenericController{
	@RequestMapping("operater")
	public ModelAndView operater(){
		
		return null;
	}
}
