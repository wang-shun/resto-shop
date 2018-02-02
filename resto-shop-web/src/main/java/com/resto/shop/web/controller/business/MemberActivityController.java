package com.resto.shop.web.controller.business;

import com.resto.shop.web.controller.GenericController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("memberActivity")
public class MemberActivityController extends GenericController {


    @RequestMapping("/list")
    public void list(){
    }

}
