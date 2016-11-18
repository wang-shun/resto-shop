package com.resto.shop.web.controller.business;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resto.brand.core.entity.PictureResult;
import com.resto.brand.core.entity.Result;
import com.resto.brand.core.util.FileUpload;
import com.resto.brand.web.model.Brand;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.PictureService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.service.ArticleService;


@RequestMapping("upload")
@RestController
public class UploadController extends GenericController{
	 @Resource
	 private PictureService pictureService;
	 
	 @Resource
	 private BrandService brandService;
	 
	 @Resource
	 private ArticleService articleService;
	 
	 
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
        return result.getUrl();
    }
    
    @RequestMapping("moveFile")
    @ResponseBody
    public Result moveFile() throws Exception{
    	Brand barnd = brandService.selectById(getCurrentBrandId());
    	String baseUrl = barnd.getWechatImgUrl();
    	List<Article> list = articleService.selectList();
    	for(Article article : list){
    		String oldPath =  baseUrl+article.getPhotoSmall();
    		System.out.println(oldPath);
    		if(!oldPath.endsWith("/null")){
    			String localPath = download(oldPath);
    			File file = new File(localPath);
    			PictureResult result = pictureService.uploadPic(file);
    			System.out.println(result);
    		}else{
    			System.out.println("\n\n该菜品没有上传图片\n\nId："+article.getId()+"\n\n");
    		}
    	}
    	return new Result("上传成功！",true);
    }
    
    
    public static String URL_PATH = "http://139.196.222.42:8380/upload/files/2016-10-31/2fdf7810-355e-4c0d-bdf0-a68f6cb6f6a8.jpg";
    
    public static void main(String[] args) throws Exception {
    	
    	System.out.println(download(URL_PATH));
    	
	}
    
    public static String download(String urlString) throws Exception {
    	String filename = URL_PATH.substring(URL_PATH.lastIndexOf("/")+1);
    	try {  
    		// 构造URL    
            URL url = new URL(urlString);  
            
            // 打开连接    
            URLConnection con = url.openConnection();  
            //设置请求超时为5s    
            con.setConnectTimeout(5 * 1000);  
            // 输入流    
            InputStream is = con.getInputStream();  
      
            // 1K的数据缓冲    
            byte[] bs = new byte[1024];  
            // 读取到的数据长度    
            int len;  
            // 输出的文件流    
            File sf = new File("D:\\temp\\");  
            if (!sf.exists()) {  
                sf.mkdirs();  
            }  
            OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);  
            // 开始读取    
            while ((len = is.read(bs)) != -1) {  
                os.write(bs, 0, len);  
            }  
            // 完毕，关闭所有链接    
            os.close();  
            is.close();  
       } catch (Exception e1) {  
            System.out.println("/n/n连接打不开!/n/n"+urlString+"/n/n");  
       }  
        
        return "D:\\temp\\"+URL_PATH.substring(URL_PATH.lastIndexOf("/")+1);
    }  
}
