package com.fh.controller.Order.order;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.scheduling.annotation.Scheduled;
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
import com.fh.util.DateUtil;
import com.fh.util.ExcelHandle;
import com.fh.util.FileDownload;
import com.fh.util.FileUpload;
import com.fh.util.ObjectExcelView;
import com.fh.util.PageData;
import com.fh.util.PathUtil;
import com.fh.util.Jurisdiction;
import com.fh.util.ObjectExcelRead;
import com.fh.util.Tools;
import com.fh.util.myutil.GenerateNO;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.fh.service.Order.customer.CustomerManager;
import com.fh.service.Order.order.OrderManager;
import com.fh.service.Prepare.preparing.PreparingManager;
import com.fh.service.Product.clothroller.ClothRollerManager;
import com.fh.service.Product.product.ProductManager;
import com.fh.service.Product.yarn.YarnManager;
import com.fh.service.scheduling.notification.NotificationManager;
import com.fh.service.system.fhlog.FHlogManager;

/** 
 * 说明：订单模块 订单实体
 * 创建人：Xiangjun
 * 创建时间：2018-08-09
 */
@Controller
@RequestMapping(value="/order")
public class OrderController extends BaseController {
	
	String menuUrl = "order/list.do"; //菜单地址(权限用)
	@Resource(name="orderService")
	private OrderManager orderService;
	@Resource(name="fhlogService")
	private FHlogManager FHLOG;
	@Resource(name="customerService")
	private CustomerManager customerService;
	@Resource(name="productService")
	private ProductManager productService;
	@Resource(name="yarnService")
	private YarnManager yarnService;
	
	@Resource(name="notificationService")
	private NotificationManager notificationService;
	
	@Resource(name="preparingService")
	private PreparingManager preparingService;
	@Resource(name="clothrollerService")
	private ClothRollerManager clothrollerService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"新增Order");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("ORDER_ID", this.get32UUID());	//主键
		String maxNo = orderService.findMAXNo();
		String ordCode = GenerateNO.getOrdNO(maxNo);
		pd.put("ORD_NO", ordCode);
		Double length=null;
		Double price=null;
		Double allPrice=null;
		
		if (!"".equals((String)pd.get("ORD_LENGTH")) && null!=pd.get("ORD_LENGTH")) {
			length=Double.parseDouble((String) pd.get("ORD_LENGTH"));
		}
		if (!"".equals((String)pd.get("ORD_PERPERICE")) && null!=pd.get("ORD_PERPERICE")) {
			price=Double.parseDouble((String) pd.get("ORD_PERPERICE"));
		}
		if (length!=null&&price!=null) {
			allPrice=length*price;
		}
		pd.put("ORD_ALLPRICE", allPrice);
		orderService.save(pd);
		
		///用纱量来进行原料准备
		PageData pdPre=new PageData();
		String pdtId = pd.getString("PDT_ID");
		if (!"".equals(pdtId) &&null!=pdtId) {
			pd.put("PRODUCT_ID", pdtId);
			PageData pdt = productService.findById(pd);
			if (pdt!=null) {
				for (int i = 1; i <= 5; i++) {//经纱准备
					String yarnId = pdt.getString("WARP"+i+"ID");
					if (yarnId!=null) {
						pd.put("YARN_ID", yarnId);
						PageData pdyarn = yarnService.findById(pd);
						String yarnName = pdt.getString("WARP"+i+"NAME");
						Double yarnUse = (Double) pdt.get("WARP"+i+"USE");
						if (yarnUse!=null && length!=null) {
							Double use=length*(yarnUse/100);
							pdPre.put("PREPARING_ID", this.get32UUID());
							pdPre.put("YARNID", yarnId);
							if(pdyarn!=null) {
								pdPre.put("YARNCODE", pdyarn.get("YARN_NAME"));
							}
							pdPre.put("PRE_PDTID", pdtId);
							pdPre.put("PRE_PDTCODE", pdt.get("PDT_CODE"));
							pdPre.put("PRE_ORDID", pd.get("ORDER_ID"));
							pdPre.put("PRE_ORDCODE", pd.get("ORD_NO"));
							pdPre.put("PRE_TIME", DateUtil.getTime());
							String deadLine = DateUtil.getBeforeAfterIntDay(DateUtil.getTime(), Const.PREPARING_DAYS.toString());
							pdPre.put("PRE_DEADLINE", deadLine);
							pdPre.put("PRE_STATE", "未准备");
							pdPre.put("PRE_NEED", use);
							pdPre.put("PRE_USING", "经纱");
							preparingService.save(pdPre);
						}
					}
				}
				for (int i = 1; i <= 5; i++) {//纬纱准备
					String yarnId = pdt.getString("WEFT"+i+"ID");
					if (yarnId!=null) {
						pd.put("YARN_ID", yarnId);
						PageData pdyarn = yarnService.findById(pd);
						String yarnName = pdt.getString("WEFT"+i+"NAME");
						Double yarnUse = (Double) pdt.get("WEFT"+i+"USE");
						if (yarnUse!=null && length!=null) {
							Double use=length*(yarnUse/100);
							pdPre.put("PREPARING_ID", this.get32UUID());
							pdPre.put("YARNID", yarnId);
							if(pdyarn!=null) {
								pdPre.put("YARNCODE", pdyarn.get("YARN_NAME"));
							}
							pdPre.put("PRE_PDTID", pdtId);
							pdPre.put("PRE_PDTCODE", pdt.get("PDT_CODE"));
							pdPre.put("PRE_ORDID", pd.get("ORDER_ID"));
							pdPre.put("PRE_ORDCODE", pd.get("ORD_NO"));
							pdPre.put("PRE_TIME", new Date());
							pdPre.put("PRE_STATE", "未准备");
							pdPre.put("PRE_NEED", use);
							pdPre.put("PRE_USING", "纬纱");
							preparingService.save(pdPre);
						}
					}
				}
				// 生成调度通知
				PageData pdNoti = new PageData();
				pdNoti.put("NOTIFICATION_ID", this.get32UUID());	//主键
				pdNoti.put("ORDID", pd.get("ORDER_ID"));
				pdNoti.put("ORDNO", ordCode);
				pdNoti.put("PDTID", pdtId);
				pdNoti.put("PDTCODE", pdt.get("PDT_CODE"));
				pdNoti.put("PDTDESC", pdt.get("PDE_DESC"));
				pdNoti.put("ORDLENGTH", pd.get("ORD_LENGTH"));
				PageData pdCust = customerService.findById(pd);
				if (pdCust!=null) {
					pdNoti.put("CUSTNAME",pdCust.get("CUST_NAME"));
				}
				pdNoti.put("ORDDATE", pd.get("ORD_END"));
				pdNoti.put("CREATETIME", new Date());
				notificationService.save(pdNoti);
			} 
		}
		
		///////////////////////
		mv.addObject("msg","success");
		mv.setViewName("Order/order/save_result");
		return mv;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"删除Order");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		PageData pd = new PageData();
		pd = this.getPageData();
		String orderID=pd.getString("ORDER_ID");
		if (orderID!=null) {
			List<PageData> pdPres= preparingService.findByOrderId(orderID);
			if (pdPres!=null && pdPres.size()!=0) {
				for (PageData pageData : pdPres) {
					preparingService.delete(pageData);
				}
			}
			// 同时修改调度通知
			PageData pdNoti= notificationService.findByOrderId(orderID);
			if (pdNoti!=null) {
				notificationService.delete(pdNoti);
			}
		}
		orderService.delete(pd);
		out.write("success");
		out.close();
	}
	/**修改优先级
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/changeLevel")
	public ModelAndView changeLevel() throws Exception{
		PageData pd = new PageData();
		ModelAndView mv=new ModelAndView();
		pd = this.getPageData();
		String DATA_IDS = pd.getString("DATA_IDS");
		String ORD_LEVEL = pd.getString("ORD_LEVEL");
		ORD_LEVEL = new String(ORD_LEVEL.getBytes("iso8859-1"),"UTF-8");
		if(null != DATA_IDS && !"".equals(DATA_IDS) &&null != ORD_LEVEL && !"".equals(ORD_LEVEL)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			for (String id : ArrayDATA_IDS) {
				PageData pdOrd = orderService.findLinkByIdStr(id);
				if (pdOrd!=null) {
					pdOrd.put("ORD_LEVEL", ORD_LEVEL);
					orderService.edit(pdOrd);
				}
			}
		}
		mv.setViewName("redirect:/order/list.do");
		return mv;
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"修改Order");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Double length=null;
		Double price=null;
		Double allPrice=null;
		if (!"".equals((String)pd.get("ORD_LENGTH")) && null!=pd.get("ORD_LENGTH")) {
			length=Double.parseDouble((String) pd.get("ORD_LENGTH"));
		}
		if (!"".equals((String)pd.get("ORD_PERPERICE")) && null!=pd.get("ORD_PERPERICE")) {
			price=Double.parseDouble((String) pd.get("ORD_PERPERICE"));
		}
		if (length!=null&&price!=null) {
			allPrice=length*price;
		}
		pd.put("ORD_ALLPRICE", allPrice);
		orderService.edit(pd);
		
		String orderID=pd.getString("ORDER_ID");
		if (orderID!=null) {
			List<PageData> pdPres= preparingService.findByOrderId(orderID);
			if (pdPres!=null && pdPres.size()!=0) {
				for (PageData pageData : pdPres) {
					preparingService.delete(pageData);
				}
			}
		}
		
		///用纱量来进行原料准备
		PageData pdPre=new PageData();
		String pdtId = pd.getString("PDT_ID");
		if (!"".equals(pdtId) &&null!=pdtId) {
			pd.put("PRODUCT_ID", pdtId);
			PageData pdt = productService.findById(pd);
			if (pdt!=null) {
				for (int i = 1; i <= 5; i++) {//经纱准备
					String yarnId = pdt.getString("WARP"+i+"ID");
					if (yarnId!=null) {
						String yarnName = pdt.getString("WARP"+i+"NAME");
						Double yarnUse = (Double) pdt.get("WARP"+i+"USE");
						if (yarnUse!=null && length!=null) {
							Double use=length*yarnUse/100;
							pdPre.put("PREPARING_ID", this.get32UUID());
							pdPre.put("YARNID", yarnId);
							pdPre.put("YARNCODE", yarnName);
							pdPre.put("PRE_PDTID", pdtId);
							pdPre.put("PRE_PDTCODE", pdt.get("PDT_CODE"));
							pdPre.put("PRE_ORDID", pd.get("ORDER_ID"));
							pdPre.put("PRE_ORDCODE", pd.get("ORD_NO"));
							pdPre.put("PRE_TIME", DateUtil.getTime());
							String deadLine = DateUtil.getBeforeAfterIntDay(DateUtil.getTime(), Const.PREPARING_DAYS.toString());
							pdPre.put("PRE_DEADLINE", deadLine);
							pdPre.put("PRE_STATE", "未准备");
							pdPre.put("PRE_NEED", use);
							pdPre.put("PRE_USING", "经纱");
							preparingService.save(pdPre);
						}
					}
				}
				for (int i = 1; i <= 5; i++) {//纬纱准备
					String yarnId = pdt.getString("WEFT"+i+"ID");
					if (yarnId!=null) {
						String yarnName = pdt.getString("WEFT"+i+"NAME");
						Double yarnUse = (Double) pdt.get("WEFT"+i+"USE");
						if (yarnUse!=null && length!=null) {
							Double use=length*yarnUse/100;
							pdPre.put("PREPARING_ID", this.get32UUID());
							pdPre.put("YARNID", yarnId);
							pdPre.put("YARNCODE", yarnName);
							pdPre.put("PRE_PDTID", pdtId);
							pdPre.put("PRE_PDTCODE", pdt.get("PDT_CODE"));
							pdPre.put("PRE_ORDID", pd.get("ORDER_ID"));
							pdPre.put("PRE_ORDCODE", pd.get("ORD_NO"));
							pdPre.put("PRE_TIME", new Date());
							pdPre.put("PRE_STATE", "未准备");
							pdPre.put("PRE_NEED", use);
							pdPre.put("PRE_USING", "纬纱");
							preparingService.save(pdPre);
						}
					}
				}
				// 同时修改调度通知
				if (orderID!=null) {
					PageData pdNoti= notificationService.findByOrderId(orderID);
					if (pdNoti!=null) {
						pdNoti.put("ORDID", pd.get("ORDER_ID"));
						pdNoti.put("ORDNO", pd.get("ORD_NO"));
						pdNoti.put("PDTID", pdtId);
						pdNoti.put("PDTCODE", pdt.get("PDT_CODE"));
						pdNoti.put("PDTDESC", pdt.get("PDE_DESC"));
						pdNoti.put("ORDLENGTH", pd.get("ORD_LENGTH"));
						PageData pdCust = customerService.findById(pd);
						if (pdCust!=null) {
							pdNoti.put("CUSTNAME",pdCust.get("CUST_NAME"));
						}
						pdNoti.put("ORDDATE", pd.get("ORD_END"));
						notificationService.edit(pdNoti);
					}
				}
			} 
		}
		mv.addObject("msg","success");
		mv.setViewName("Order/order/save_result");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"列表Order");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		
		
		String sortType = pd.getString("sortType");
		if(null != sortType && !"".equals(sortType)){
			pd.put("sortType", sortType.trim());
			if(Math.random()>0.5){
				pd.put("sortOrder", "desc");
			}else {
				pd.put("sortOrder", "asc");
			}
		}else {
			pd.put("sortType", "ORD_NO");
			pd.put("sortOrder", "desc");
		}
		
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		
		if(null != pd.getString("ORD_NO") && !"".equals(pd.getString("ORD_NO"))){
			pd.put("ORD_NO", pd.getString("ORD_NO").trim());
		}
		if(null != pd.getString("CUST_NAME") && !"".equals(pd.getString("CUST_NAME"))){
			pd.put("CUST_NAME", pd.getString("CUST_NAME").trim());
		}
		if(null != pd.getString("PDT_CODE") && !"".equals(pd.getString("PDT_CODE"))){
			pd.put("PDT_CODE", pd.getString("PDT_CODE").trim());
		}
		if(null != pd.getString("ORD_ADDRESS") && !"".equals(pd.getString("ORD_ADDRESS"))){
			pd.put("ORD_ADDRESS", pd.getString("ORD_ADDRESS").trim());
		}
		if(null != pd.getString("ORD_STARTgt") && !"".equals(pd.getString("ORD_STARTgt"))){
			pd.put("ORD_STARTgt", pd.getString("ORD_STARTgt").trim()+" 00:00:00");
		}
		if(null != pd.getString("ORD_STARTlt") && !"".equals(pd.getString("ORD_STARTlt"))){
			pd.put("ORD_STARTlt", pd.getString("ORD_STARTlt").trim()+" 23:59:59");
		}
		if(null != pd.getString("ORD_ENDgt") && !"".equals(pd.getString("ORD_ENDgt"))){
			pd.put("ORD_ENDgt", pd.getString("ORD_ENDgt").trim()+" 00:00:00");
		}
		if(null != pd.getString("ORD_ENDlt") && !"".equals(pd.getString("ORD_ENDlt"))){
			pd.put("ORD_ENDlt", pd.getString("ORD_ENDlt").trim()+" 23:59:59");
		}
		if(null != pd.getString("ORD_PERPERICEgt") && !"".equals(pd.getString("ORD_PERPERICEgt"))){
			pd.put("ORD_PERPERICEgt", pd.getString("ORD_PERPERICEgt").trim());
		}
		if(null != pd.getString("ORD_PERPERICElt") && !"".equals(pd.getString("ORD_PERPERICElt"))){
			pd.put("ORD_PERPERICElt", pd.getString("ORD_PERPERICElt").trim());
		}
		if("1".equals(pd.getString("ORD_SAMPLE"))){
			pd.put("ORD_SAMPLE", true);
		}else if("0".equals(pd.getString("ORD_SAMPLE"))){
			pd.put("ORD_SAMPLE", false);
		}else {
			pd.put("ORD_SAMPLE", null);
		}
		page.setPd(pd);
		
		List<PageData>	varList=orderService.listOPC(page);
		mv.setViewName("Order/order/order_list");
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
		
		List<PageData> customerList = customerService.listAll(pd);//列出所有系统用户角色
		mv.addObject("customerList",customerList);
		List<PageData> productList = productService.listAll(pd);//列出所有系统用户角色
		mv.addObject("productList",productList);
		
		
		mv.setViewName("Order/order/order_addoredit");
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
		pd = orderService.findById(pd);	//根据ID读取
		
		List<PageData> customerList = customerService.listAll(pd);//列出所有系统用户角色
		mv.addObject("customerList",customerList);
		List<PageData> productList = productService.listAll(pd);//列出所有系统用户角色
		mv.addObject("productList",productList);
		
		mv.setViewName("Order/order/order_addoredit");
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
	}	
	/**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goCopyPd")
	public ModelAndView goCopyPd()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = orderService.findById(pd);	//根据ID读取
		
		List<PageData> customerList = customerService.listAll(pd);//列出所有系统用户角色
		mv.addObject("customerList",customerList);
		List<PageData> productList = productService.listAll(pd);//列出所有系统用户角色
		mv.addObject("productList",productList);
		if (pd!=null) {
			pd.put("ORD_NO", pd.get("ORD_NO")+"基础上复制");
		}
		
		
		mv.setViewName("Order/order/order_addoredit");
		mv.addObject("msg", "save");
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
		logBefore(logger, Jurisdiction.getUsername()+"批量删除Order");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			for (String string : ArrayDATA_IDS) {//删除对应的织轴准备
				pd.put("ORDER_ID", string);
				PageData pdOrd=orderService.findById(pd);
				String orderID=pdOrd.getString("ORDER_ID");
				if (orderID!=null) {
					List<PageData> pdPres= preparingService.findByOrderId(orderID);
					if (pdPres!=null && pdPres.size()!=0) {
						for (PageData pageData : pdPres) {
							preparingService.delete(pageData);
						}
					}
					// 同时修改调度通知
					PageData pdNoti= notificationService.findByOrderId(orderID);
					if (pdNoti!=null) {
						notificationService.delete(pdNoti);
					}
				}
			}
			orderService.deleteAll(ArrayDATA_IDS);
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
		logBefore(logger, Jurisdiction.getUsername()+"导出Order到excel");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("客户ID");	//1
		titles.add("品种ID");	//2
		titles.add("订单编号");	//3
		titles.add("订单长度");	//4
		titles.add("订单地址");	//5
		titles.add("订单创建日期");	//6
		titles.add("订单交期日期");	//7
		titles.add("订单单价");	//8
		titles.add("订单总价");	//9
		titles.add("订单打样费");	//10
		titles.add("销售员ID");	//11
		titles.add("订单备注");	//12
		
		titles.add("客户编号");
		titles.add("客户名称");
		titles.add("品种编号");
		titles.add("品种名称");
		dataMap.put("titles", titles);
		
		List<PageData> varOList = new ArrayList<PageData>();;
		page.setPd(pd);
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			varOList=orderService.listAllLinkByIds(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			varOList = orderService.listOPC(page);
		}
		
		
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", varOList.get(i).getString("CUSTOMER_ID"));	    //1
			vpd.put("var2", varOList.get(i).getString("PRODUCT_ID"));	    //2
			vpd.put("var3", varOList.get(i).getString("ORD_NO"));	    //3
			
			if (varOList.get(i).get("ORD_LENGTH")==null  ) {
				vpd.put("var4", "");	//5
			}else {
				vpd.put("var4", varOList.get(i).get("ORD_LENGTH").toString());	//5
			}
			
			vpd.put("var5", varOList.get(i).getString("ORD_ADDRESS"));	    //5
			vpd.put("var6", varOList.get(i).getString("ORD_START"));	    //6
			vpd.put("var7", varOList.get(i).getString("ORD_END"));	    //7
			
			if (varOList.get(i).get("ORD_PERPERICE")==null  ) {
				vpd.put("var8", "");	//5
			}else {
				vpd.put("var8", varOList.get(i).get("ORD_PERPERICE").toString());	//5
			}
			
			if (varOList.get(i).get("ORD_ALLPRICE")==null  ) {
				vpd.put("var9", "");	//5
			}else {
				vpd.put("var9", varOList.get(i).get("ORD_ALLPRICE").toString());	//5
			}
			
			vpd.put("var10", varOList.get(i).getString("ORD_SAMPLE"));	    //10
			vpd.put("var11", varOList.get(i).getString("SELLER_ID"));	    //11
			vpd.put("var12", varOList.get(i).getString("ORD_REMARK"));	    //12
			vpd.put("var13", varOList.get(i).getString("CUST_NO"));	    //10
			vpd.put("var14", varOList.get(i).getString("CUST_NAME"));	    //11
			vpd.put("var15", varOList.get(i).getString("PDT_CODE"));	    //12
			vpd.put("var16", varOList.get(i).getString("PDT_NAME"));	    //12
			
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
		mv.setViewName("Order/order/uploadexcel");
		return mv;
	}
	
	/**下载模版
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/downExcel")
	public void downExcel(HttpServletResponse response)throws Exception{
		FileDownload.fileDownload(response, PathUtil.getClasspath() + Const.FILEPATHFILE + "Order.xls", "Order.xls");
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
		
			
			SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
			ArrayList<Integer> ids=new ArrayList<Integer>();
			
			for(int i=0;i<listPd.size();i++){		
				pd.put("ORDER_ID", this.get32UUID());										//ID
				String custNo=listPd.get(i).getString("var0");
				String pdtCode=listPd.get(i).getString("var1");
				PageData pdc=customerService.findByCustNo(custNo);
				if (pdc!=null) {
					pd.put("CUST_ID", pdc.get("CUSTOMER_ID"));
				}else {
					ids.add(i);
					continue;
				}
//				pdc=productService.findByPdtCode(pdtCode);
//				if (pdc!=null) {
//					if (!"".equals(pdc.getString("PDT_CODE"))) {
//						pd.put("PDT_CODE", pdc.getString("PDT_CODE"));
//					}else {
//						ids.add(i);
//						continue;
//					}
//				}else {
//					ids.add(i);
//					continue;
//				}
				pd.put("ORD_NO", 		listPd.get(i).getString("var2"));							//编号
				if (!listPd.get(i).getString("var3").trim().equals("")){pd.put("ORD_LENGTH", 	Double.parseDouble(listPd.get(i).getString("var3")));}
				pd.put("ORD_ADDRESS", 		listPd.get(i).getString("var4"));
				
				if (!listPd.get(i).getString("var5").trim().equals("")) {
					pd.put("ORD_START", format.parse(listPd.get(i).getString("var5")));							//编号
				}
				if (!listPd.get(i).getString("var6").trim().equals("")) {
					pd.put("ORD_END", 	format.parse(listPd.get(i).getString("var6")));							//编号
				}
				if (!listPd.get(i).getString("var7").trim().equals("")){pd.put("ORD_PERPERICE", 	Double.parseDouble(listPd.get(i).getString("var7")));}
				if (!listPd.get(i).getString("var8").trim().equals("")){pd.put("ORD_ALLPRICE", 	Double.parseDouble(listPd.get(i).getString("var8")));}
				pd.put("ORD_SAMPLE", 		listPd.get(i).getString("var9"));
				pd.put("SELLER_ID", 		listPd.get(i).getString("var10"));
				pd.put("ORD_REMARK", 		listPd.get(i).getString("var11"));
				orderService.save(pd);
			}
			/*存入数据库操作======================================*/
			if (!ids.isEmpty()) {
				mv.addObject("msg","defeat");
				mv.addObject("ids",ids);
			}else {
				mv.addObject("msg","success");
			}
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	
	/**查看订单
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/view")
	public ModelAndView view() throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String[] arrayDATA_IDS=new String[1];
		if (!"".equals(pd.get("ORDER_ID")) && null!=pd.get("ORDER_ID")) {
			pd=orderService.findLinkByIdStr((String) pd.get("ORDER_ID"));
		}
		mv.setViewName("Order/order/order_view");
		mv.addObject("pd", pd);
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
	
	/**
	 * 定时更新订单的完成情况
	 * @throws Exception 
	 */
	@Scheduled(fixedRate=600000)
	public void updateOrder() throws Exception {
		List<PageData> pdList =clothrollerService.getEveryOrderLength();//获取已经验好布的布卷所属每个订单的总长度（group）
		for (PageData pd : pdList) {
			String ordID = pd.getString("ORDID");
			if (ordID!=null && !"".equals(ordID)) {
				pd.put("ORDER_ID", ordID);
				PageData pdOrder = orderService.findById(pd);
				if (pdOrder!=null) {
					if (pdOrder.get("ORD_LENGTH")!=null &&pd.get("CLOTHROLLER_LENGTH")!=null) {
						Double ORD_LENGTH = (Double) pdOrder.get("ORD_LENGTH");
						Double CLOTHROLLER_LENGTH = (Double) pd.get("CLOTHROLLER_LENGTH");
						if (ORD_LENGTH<=CLOTHROLLER_LENGTH) {//如果验好布的布卷长度大于订单长度，可以认为此订单已准备完成
							pdOrder.put("ORD_STATUS", "已完成织造");
							orderService.edit(pdOrder);
						}
					}
				}
			}
		}
		
		//更新完成情况
		
	}
	// 生成合同
	/**从EXCEL导入到数据库
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goContract")
	public ModelAndView goContract(HttpServletRequest request,HttpServletResponse response) throws Exception{
		FHLOG.save(Jurisdiction.getUsername(), "生成合同");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;}
			String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
			String fileName = filePath+"orderContract.xlsx";						//执行上传
			//List<PageData> listPd = (List)ObjectExcelRead.readExcel(filePath, fileName, 2, 0, 0);		//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet
			/*搜集相关数据======================================*/
			pd = this.getPageData();
			String DATA_IDS = pd.getString("DATA_IDS");
			if(null != DATA_IDS && !"".equals(DATA_IDS)){
				PageData pdOrder = orderService.findLinkByIdStr(DATA_IDS); 
				List<String> dataListCell = new ArrayList<String>();
				dataListCell.add("CUST_NAME");
				dataListCell.add("ORD_NO");
				dataListCell.add("ORD_START");
				dataListCell.add("PDT_CODE");
				dataListCell.add("PDE_DESC");
				dataListCell.add("ORD_LENGTH");
				dataListCell.add("ORD_PERPERICE");
				dataListCell.add("ORD_ALLPRICE");
				dataListCell.add("ORD_END");
				Map<String,Object> dataMap = new  HashMap<String, Object>();
				dataMap.put("CUST_NAME", pdOrder.getString("CUST_NAME"));
				dataMap.put("ORD_NO", pdOrder.getString("ORD_NO"));
				dataMap.put("ORD_START", pdOrder.getString("ORD_START"));
				dataMap.put("PDT_CODE", pdOrder.getString("PDT_CODE"));
				dataMap.put("PDE_DESC", pdOrder.getString("PDE_DESC"));
				dataMap.put("ORD_LENGTH", pdOrder.get("ORD_LENGTH"));
				dataMap.put("ORD_PERPERICE", pdOrder.get("ORD_PERPERICE"));
				dataMap.put("ORD_ALLPRICE", pdOrder.get("ORD_ALLPRICE"));
				dataMap.put("ORD_END",Const.HT_DWTT+pdOrder.get("ORD_END").toString()+Const.HT_DWTW);
				
				ExcelHandle handle = new  ExcelHandle();
				handle.writeData(fileName, dataListCell, dataMap, 0);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				handle.writeAndClose(fileName, os);
				byte[] content = os.toByteArray();
			    InputStream is = new ByteArrayInputStream(content);
			    Date date = new Date();
				String outfilename = Tools.date2Str(date, "yyyyMMddHHmmss");
			    response.reset();
			    response.setContentType("application/vnd.ms-excel;charset=utf-8");
			    response.setHeader("Content-Disposition", "attachment;filename="+ new String((outfilename + ".xlsx").getBytes(), "iso-8859-1"));
			    ServletOutputStream out = response.getOutputStream();
			    BufferedInputStream bis = null;
			    BufferedOutputStream bos = null;
			    try {
			        bis = new BufferedInputStream(is);
			        bos = new BufferedOutputStream(out);
			        byte[] buff = new byte[2048];
			        int bytesRead;
			        // Simple read/write loop.
			        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
			            bos.write(buff, 0, bytesRead);
			        }
			    } catch (final IOException e) {
			        throw e;
			    } finally {
			        if (bis != null)
			            bis.close();
			        if (bos != null)
			            bos.close();
			    }
			    
			}
		    // 设置response参数，可以打开下载页面
		mv.setViewName("save_result");
		return mv;
	}
	
	
	
}
