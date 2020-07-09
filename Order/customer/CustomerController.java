package com.fh.controller.Order.customer;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.entity.system.Role;
import com.fh.util.AppUtil;
import com.fh.util.Const;
import com.fh.util.FileDownload;
import com.fh.util.FileUpload;
import com.fh.util.GetPinyin;
import com.fh.util.ObjectExcelView;
import com.fh.util.PageData;
import com.fh.util.PathUtil;
import com.fh.util.Jurisdiction;
import com.fh.util.ObjectExcelRead;
import com.fh.util.Tools;
import com.fh.util.myutil.GenerateNO;
import com.fh.service.Order.customer.CustomerManager;
import com.fh.service.system.fhlog.FHlogManager;

/** 
 * 说明：订单模块 客户实体
 * 创建人：Xiangjun
 * 创建时间：2018-08-09
 */
@Controller
@RequestMapping(value="/customer")
public class CustomerController extends BaseController {
	
	String menuUrl = "customer/list.do"; //菜单地址(权限用)
	@Resource(name="customerService")
	private CustomerManager customerService;
	@Resource(name="fhlogService")
	private FHlogManager FHLOG;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"新增Customer");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String uuid=this.get32UUID();
		
		//////////////
		pd.put("CUSTOMER_ID",uuid);	//主键
		String maxCustNo = customerService.findMAXCustNo();
		String custNO = GenerateNO.getCustNO(maxCustNo);
		pd.put("CUST_NO", custNO);
		customerService.save(pd);
		String newName=pd.getString("CUST_NAME");
		String source=pd.getString("source");
		if (source!=null && !source.equals("")) {//这是订单新增 增加品种的界面
			source=uuid;
			mv.addObject("source",source);
			mv.addObject("newName",newName);
			mv.addObject("newCode",custNO);
			mv.addObject("msg","source");
		}else {
			mv.addObject("msg","success");
		}
		///////
		mv.setViewName("Order/customer/save_result");
		return mv;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"删除Customer");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		PageData pd = new PageData();
		pd = this.getPageData();
		customerService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"修改Customer");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		customerService.edit(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"列表Customer");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		if(null != pd.getString("CUST_NAME") && !"".equals(pd.getString("CUST_NAME"))){
			pd.put("CUST_NAME", pd.getString("CUST_NAME").trim());
		}
		if(null != pd.getString("CUST_NO") && !"".equals(pd.getString("CUST_NO"))){
			pd.put("CUST_NO", pd.getString("CUST_NO").trim());
		}
		if(null != pd.getString("CUST_ADDRESS") && !"".equals(pd.getString("CUST_ADDRESS"))){
			pd.put("CUST_ADDRESS", pd.getString("CUST_ADDRESS").trim());
		}
		if(null != pd.getString("CUST_LEVEL") && !"".equals(pd.getString("CUST_LEVEL"))){
			pd.put("CUST_LEVEL", pd.getString("CUST_LEVEL").trim());
		}
		if(null != pd.getString("CUST_LINKMAN") && !"".equals(pd.getString("CUST_LINKMAN"))){
			pd.put("CUST_LINKMAN", pd.getString("CUST_LINKMAN").trim());
		}
		page.setPd(pd);
		List<PageData>	varList = customerService.list(page);	//列出Customer列表
		mv.setViewName("Order/customer/customer_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("QX",Jurisdiction.getHC());	//按钮权限
		return mv;
	}
	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		////对新增纱线的来源进行判断   
		String source=pd.getString("source");
		if (!"".equals(source)&& null!=source) {
			mv.addObject("source", source);
		}
		///
		mv.setViewName("Order/customer/customer_edit");
		mv.addObject("msg", "save");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = customerService.findById(pd);	//根据ID读取
		mv.setViewName("Order/customer/customer_edit");
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"批量删除Customer");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			customerService.deleteAll(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			pd.put("msg", "no");
		}
		pdList.add(pd);
		map.put("list", pdList);
		return AppUtil.returnObject(pd, map);
	}
	
	 /**导出到excel
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"导出Customer到excel");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("客户编号");	//1
		titles.add("客户姓名");	//2
		titles.add("客户地址");	//3
		titles.add("客户级别");	//4
		titles.add("客户联系人");	//5
		titles.add("客户联系方式");	//6
		titles.add("客户邮箱");	//7
		titles.add("客户唛头");	//8
		titles.add("客户备注");	//9
		dataMap.put("titles", titles);
		List<PageData> varOList= new ArrayList<PageData>();
		///////////////
		page.setPd(pd);
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			varOList=customerService.listAllByIds(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			varOList = customerService.list(page);
		}
		///////////////
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", varOList.get(i).getString("CUST_NO"));	    //1
			vpd.put("var2", varOList.get(i).getString("CUST_NAME"));	    //2
			vpd.put("var3", varOList.get(i).getString("CUST_ADDRESS"));	    //3
			vpd.put("var4", varOList.get(i).getString("CUST_LEVEL"));	    //4
			vpd.put("var5", varOList.get(i).getString("CUST_LINKMAN"));	    //5
			vpd.put("var6", varOList.get(i).getString("CUST_MOBILE"));	    //6
			vpd.put("var7", varOList.get(i).getString("CUST_EMAIL"));	    //7
			vpd.put("var8", varOList.get(i).getString("CUST_MARK"));	    //8
			vpd.put("var9", varOList.get(i).getString("CUST_REMARK"));	    //9
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		ObjectExcelView erv = new ObjectExcelView();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	
	/**打开上传EXCEL页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goUploadExcel")
	public ModelAndView goUploadExcel()throws Exception{
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("Order/customer/uploadexcel");
		return mv;
	}
	
	/**下载模版
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/downExcel")
	public void downExcel(HttpServletResponse response)throws Exception{
		FileDownload.fileDownload(response, PathUtil.getClasspath() + Const.FILEPATHFILE + "Customer.xls", "Customer.xls");
	}
	
	/**从EXCEL导入到数据库
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/readExcel")
	public ModelAndView readExcel(
			@RequestParam(value="excel",required=false) MultipartFile file
			) throws Exception{
		FHLOG.save(Jurisdiction.getUsername(), "从EXCEL导入到数据库");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;}
		if (null != file && !file.isEmpty()) {
			String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
			String fileName =  FileUpload.fileUp(file, filePath, "customerexcel");							//执行上传
			List<PageData> listPd = (List)ObjectExcelRead.readExcel(filePath, fileName, 2, 0, 0);		//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet
			/*存入数据库操作======================================*/
			/**
			 * var0 :编号
			 * var1 :姓名
			 * var2 :地址
			 * var3 :级别
			 * var4 :联系人
			 * var5 :联系方式
			 * var6 :邮箱
			 * var7 :唛头
			 * var8 :备注
			 */
			for(int i=0;i<listPd.size();i++){		
				pd.put("CUSTOMER_ID", this.get32UUID());										//ID
				pd.put("CUST_NO", listPd.get(i).getString("var0"));							//编号
				pd.put("CUST_NAME", listPd.get(i).getString("var1"));							//编号
				pd.put("CUST_ADDRESS", listPd.get(i).getString("var2"));							//编号
				pd.put("CUST_LEVEL", listPd.get(i).getString("var3"));							//编号
				pd.put("CUST_LINKMAN", listPd.get(i).getString("var4"));							//编号
				pd.put("CUST_MOBILE", listPd.get(i).getString("var5"));							//编号
				pd.put("CUST_EMAIL", listPd.get(i).getString("var6"));							//编号
				pd.put("CUST_MARK", listPd.get(i).getString("var7"));							//编号
				pd.put("CUST_REMARK", listPd.get(i).getString("var8"));							//编号
				customerService.save(pd);
			}
			/*存入数据库操作======================================*/
			mv.addObject("msg","success");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	/**查看客户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/view")
	public ModelAndView view() throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		
		if (!"".equals(pd.get("CUSTOMER_ID")) && null!=pd.get("CUSTOMER_ID")) {
			pd = customerService.findById(pd);
		}
		
		mv.setViewName("Order/customer/customer_view");
		mv.addObject("pd", pd);
		return mv;
	}
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
