package com.resto.shop.web.controller.business;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import com.resto.brand.core.entity.Result;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.util.ImageUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resto.brand.core.util.FileUpload;

@RequestMapping("upload")
@RestController
public class UploadController  extends GenericController {
	@RequestMapping("file")
	public String uploadFile(MultipartFile file,HttpServletRequest request){
		String type = request.getParameter("type");
		String systemPath = request.getServletContext().getRealPath("");
		systemPath = systemPath.replaceAll("\\\\", "/");
		int lastR = systemPath.lastIndexOf("/");
		systemPath = systemPath.substring(0,lastR)+"/";
		String filePath = "upload/files/"+DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		File finalFile = FileUpload.fileUp(file, systemPath+filePath,UUID.randomUUID().toString(),type);
		return filePath+"/"+finalFile.getName();
	}

	@RequestMapping("file/square")
	public Result uploadFileSquare(MultipartFile file, HttpServletRequest request) throws IOException {
		String type = request.getParameter("type");
		String systemPath = request.getServletContext().getRealPath("");
		systemPath = systemPath.replaceAll("\\\\", "/");
		int lastR = systemPath.lastIndexOf("/");
		systemPath = systemPath.substring(0,lastR)+"/";
		String filePath = "upload/files/"+DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		File finalFile = FileUpload.fileUp(file, systemPath+filePath, UUID.randomUUID().toString(),type);
		//截图
		BufferedImage bis = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
		String finalFileSquare = ImageUtil.imageBytesScale(bis, systemPath, filePath + "/");
		JSONObject json = new JSONObject();
		Map<String, String> map=new HashMap<String, String>();
		map.put("big",filePath+"/"+finalFile.getName());
		map.put("small",finalFileSquare);
		return getSuccessResult(map);
	}
}
