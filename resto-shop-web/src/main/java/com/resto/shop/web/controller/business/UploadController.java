package com.resto.shop.web.controller.business;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resto.brand.core.util.FileUpload;

@RequestMapping("upload")
@RestController
public class UploadController {
	@RequestMapping("file")
	public String uploadFile(MultipartFile file,HttpServletRequest request){
		String type = request.getParameter("type");
		System.out.println(type);
		String systemPath = request.getServletContext().getRealPath("");
		systemPath = systemPath.replaceAll("\\\\", "/");
		int lastR = systemPath.lastIndexOf("/");
		systemPath = systemPath.substring(0,lastR)+"/";
		String filePath = "upload/files/"+DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		File finalFile = FileUpload.fileUp(file, systemPath+filePath,UUID.randomUUID().toString());
		return filePath+"/"+finalFile.getName();
	}
}
