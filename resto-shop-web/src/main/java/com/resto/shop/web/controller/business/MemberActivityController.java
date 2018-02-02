package com.resto.shop.web.controller.business;

import com.resto.brand.core.entity.Result;
import com.resto.brand.web.model.BrandUser;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.MemberActivity;
import com.resto.shop.web.service.MemberActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("memberActivity")
public class MemberActivityController extends GenericController {

    @Resource
    MemberActivityService memberActivityService;

    @RequestMapping("/list")
    public void list(){
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public List<MemberActivity> listData(){
        return memberActivityService.selectList();
    }

    @RequestMapping("/create")
    @ResponseBody
    public Result create(@Valid MemberActivity memberActivity){
        memberActivity.setBrandId(getCurrentBrandId());
        memberActivity.setCreateTime(new Date());
        memberActivityService.insert(memberActivity);
        return Result.getSuccess();
    }


    @RequestMapping("/modify")
    @ResponseBody
    public Result modify(@Valid MemberActivity memberActivity){
        memberActivityService.update(memberActivity);
        return Result.getSuccess();
    }
}
