package com.resto.shop.web.controller.business;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
import com.resto.brand.web.model.ShopDetail;
import com.resto.brand.web.service.BrandService;
import com.resto.brand.web.service.PictureService;
import com.resto.brand.web.service.ShopDetailService;
import com.resto.shop.web.controller.GenericController;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.service.ArticleService;

@RequestMapping("upload")
@RestController
public class UploadController extends GenericController {
	@Resource
	private PictureService pictureService;

	@Resource
	private BrandService brandService;
	
	@Resource
	private ShopDetailService shopDetailService; 

	@Resource
	private ArticleService articleService;

	// @RequestMapping("file")
	// public String uploadFile(MultipartFile file,HttpServletRequest request){
	// String type = request.getParameter("type");
	// String systemPath = request.getServletContext().getRealPath("");
	// systemPath = systemPath.replaceAll("\\\\", "/");
	// int lastR = systemPath.lastIndexOf("/");
	// systemPath = systemPath.substring(0,lastR)+"/";
	// String filePath = "upload/files/"+DateFormatUtils.format(new Date(),
	// "yyyy-MM-dd");
	// File finalFile = FileUpload.fileUp(file,
	// systemPath+filePath,UUID.randomUUID().toString(),type);
	// return filePath+"/"+finalFile.getName();
	// }

	@RequestMapping("file")
	@ResponseBody
	public String uploadFile(MultipartFile file, HttpServletRequest request) throws IOException {
		String type = request.getParameter("type");
		String systemPath = request.getServletContext().getRealPath("");
		systemPath = systemPath.replaceAll("\\\\", "/");
		int lastR = systemPath.lastIndexOf("/");
		systemPath = systemPath.substring(0, lastR) + "/";
		String filePath = "upload/files/" + DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		File finalFile = FileUpload.fileUp(file, systemPath + filePath, UUID.randomUUID().toString(), type);
		PictureResult result = pictureService.uploadPic(finalFile);
//		return result.getUrl();
		return filePath+"/"+finalFile.getName();
	}

	@RequestMapping("moveFile")
	@ResponseBody
	public Result moveFile() throws Exception {
		Brand barnd = brandService.selectById(getCurrentBrandId());
		String baseUrl = barnd.getWechatImgUrl();
		List<ShopDetail> shopDetails = shopDetailService.selectByBrandId(getCurrentBrandId());
//		for(ShopDetail shopDetail : shopDetails ){
			String shopId = getCurrentShopId();
			List<Article> list = articleService.selectList(shopId);
			List<Article> newList = new ArrayList<>();
			System.out.println("【店铺ID】："+shopId+"\n【菜品数量】："+list.size());
			for (Article article : list) {
				try {
		            Thread.sleep(1000);
		        } catch (InterruptedException e) {
		            System.out.println("睡眠失败");
		        }
				if (article.getPhotoSmall()!=null && article.getPhotoSmall().startsWith("upload/files/")) {
					String oldPath = baseUrl + article.getPhotoSmall();
					String localPath = download(article.getId(),oldPath);
					if(localPath!=null && localPath!=""){
						File file = new File(localPath);
						PictureResult result = pictureService.uploadPic(file);
						System.out.println("【上传到资源服务器成功】"+result.getUrl());
						if (result.getError() == 0) {//判断是否成功
							Article updateArticle = new Article();
							updateArticle.setId(article.getId());
							updateArticle.setPhotoSmall(result.getUrl());
							newList.add(updateArticle);
						}
					}
				} else {
					System.out.println("\n【该菜品没有上传图片】Id：" + article.getId());
				}
			}
			for (Article article : newList) {
				try {
		            Thread.sleep(500);
		        } catch (InterruptedException e) {
		            System.out.println("睡眠失败");
		        }
				articleService.updateArticleImg(article);
				System.out.println("【修改成功】articleId："+article.getId());
			}
			System.out.println("【修改菜品信息成功】\n shopID："+shopId+"\n 数量："+newList.size());
//		}
		return new Result("上传成功！", true);
	}

	public static String download(String id,String urlString) throws Exception {
		String localPath = "";
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			inStream.close();
			byte[] btImg = outStream.toByteArray();

			if (null != btImg && btImg.length > 0) {
				String fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
				File file = new File("D:\\temp\\" + fileName);
				FileOutputStream fops = new FileOutputStream(file);
				fops.write(btImg);
				fops.flush();
				fops.close();
				localPath = "D:\\temp\\" + urlString.substring(urlString.lastIndexOf("/") + 1);
			} else {
				System.out.println("没有从该连接获得内容");
			}

		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println("【上传失败】id："+id+"   url："+urlString);
		}
		return localPath;
	}
}
