package com.resto.shop.web.controller.business;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.resto.brand.core.entity.PictureResult;
import com.resto.brand.core.util.FileUpload;
import com.resto.brand.core.util.JsonUtils;
import com.resto.brand.web.service.PictureService;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;


@RequestMapping("upload")
@RestController
public class UploadController {
//	@RequestMapping("file")
//	public String uploadFile(MultipartFile file,HttpServletRequest request){
//		String type = request.getParameter("type");
//		String systemPath = request.getServletContext().getRealPath("");
//		systemPath = systemPath.replaceAll("\\\\", "/");
//		int lastR = systemPath.lastIndexOf("/");
//		systemPath = systemPath.substring(0,lastR)+"/";
//		String filePath = "upload/files/"+DateFormatUtils.format(new Date(), "yyyy-MM-dd");
//		File finalFile = FileUpload.fileUp(file, systemPath+filePath,UUID.randomUUID().toString(),type);
//		return filePath+"/"+finalFile.getName();
//	}

    @Resource
    private PictureService pictureService;
    @RequestMapping("file")
    @ResponseBody
    public String uploadFile(MultipartFile file,HttpServletRequest request)throws IOException{

        String type = request.getParameter("type");
        String systemPath = request.getServletContext().getRealPath("");
        systemPath = systemPath.replaceAll("\\\\", "/");
        int lastR = systemPath.lastIndexOf("/");
        systemPath = systemPath.substring(0,lastR)+"/";
        String filePath = "upload/files/"+DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        File finalFile = FileUpload.fileUp(file, systemPath+filePath,UUID.randomUUID().toString(),type);
        PictureResult result = pictureService.uploadPic(finalFile);
        //java 转jsion
        String json = JsonUtils.objectToJson(result);
        return json;
    }





}
