package com.fh.controller.Order.pinzhong;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.util.AppUtil;
import com.fh.util.ObjectExcelView;
import com.fh.util.PageData;
import com.fh.util.Jurisdiction;
import com.fh.util.Tools;
import com.fh.service.Order.pinzhong.PinzhongManager;
import com.fh.service.storage.fiber.FiberManager;
import com.fh.service.storage.inkucheck.InkuCheckManager;
import com.fh.service.storage.translog.TranslogManager;
import com.fh.service.storage.yarninfo.YarninfoManager;
import com.fh.service.storage.yarnnamecreat.YarnNameCreatManager;

/** 
 * 说明：订单生产
 * 创建人：Xiangjun
 * 创建时间：2017-08-03
 */
@Controller
@RequestMapping(value="/pinzhong")
public class PinzhongController extends BaseController {
	
	String menuUrl = "pinzhong/list.do"; //菜单地址(权限用)
	@Resource(name="pinzhongService")
	private PinzhongManager pinzhongService;
	@Resource(name="translogService")
	private TranslogManager translogService;
	@Resource(name="yarninfoService")
	private YarninfoManager yarninfoService;
	@Resource(name="yarnnamecreatService")
	private YarnNameCreatManager yarnNameCreatService;
	@Resource(name="inkucheckService")
	private InkuCheckManager inkucheckService;
	@Resource(name="fiberService")
	private FiberManager fiberService;
	
	
	private String tempyarnvalueString=null;
	private String tempyarnvalueStringweft=null;
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"新增Pinzhong");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("PINZHONG_ID", this.get32UUID());	//主键
		pinzhongService.save(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"删除Pinzhong");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		PageData pd = new PageData();
		pd = this.getPageData();
		pinzhongService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"修改Pinzhong");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pinzhongService.edit(pd);
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
		logBefore(logger, Jurisdiction.getUsername()+"列表Pinzhong");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<PageData>	varList = pinzhongService.list(page);	//列出Pinzhong列表
		mv.setViewName("Order/pinzhong/pinzhong_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("QX",Jurisdiction.getHC());	//按钮权限
		return mv;
	}
	
	
	@RequestMapping(value="/viewdetail")
	public ModelAndView viewdetail(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"列表Pinzhong");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<PageData>	varList = pinzhongService.list(page);	//列出Pinzhong列表
		mv.setViewName("Order/pinzhong/pinzhong_detail");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("QX",Jurisdiction.getHC());	//按钮权限
		return mv;
	}
	
	
	/**品种详情页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		//Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		//List<PageData> pdList = new ArrayList<PageData>();
		List<PageData>	varList=yarnNameCreatService.listAll(pd);
		if (varList.size()!=0) {
			pd=varList.get(0);
			//pdList.add(pd);
			yarnNameCreatService.deleteAllData(pd);
				
		}
		//map.put("list", pdList);
		mv.setViewName("Order/pinzhong/pinzhong_edit");
		mv.addObject("msg", "save");
		mv.addObject("pd", pd);
		//return AppUtil.returnObject(pd, map);
		return   mv;
	}	
	
	@RequestMapping(value="/getyarn")
	@ResponseBody 
	public  Map<String,Object> getyarn(HttpServletRequest request,HttpServletResponse response) throws IOException{
		//System.out.println("jinlai");
		Map<String,Object> map = new HashMap<String,Object>();  
		map.put("msg", tempyarnvalueString); 
		return map;  
	}
	
	@RequestMapping(value="/getyarnweft")
	@ResponseBody 
	public  Map<String,Object> getyarnweft(HttpServletRequest request,HttpServletResponse response) throws IOException{
		System.out.println("jinlai");
		Map<String,Object> map = new HashMap<String,Object>();  
		map.put("msg", tempyarnvalueStringweft); 
		return map;  
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
		pd = pinzhongService.findById(pd);	//根据ID读取
		mv.setViewName("Order/pinzhong/pinzhong_edit");
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
		logBefore(logger, Jurisdiction.getUsername()+"批量删除Pinzhong");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			pinzhongService.deleteAll(ArrayDATA_IDS);
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
	public ModelAndView exportExcel() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"导出Pinzhong到excel");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("品种代号");	//1
		titles.add("品种名称");	//2
		titles.add("筘符");	//3
		titles.add("筘号");	//4
		titles.add("布幅");	//5
		titles.add("经纱1名称");	//6
		titles.add("经纱2名称");	//7
		titles.add("经纱3名称");	//8
		titles.add("经纱4名称");	//9
		titles.add("经纱1号数");	//10
		titles.add("经纱2号数");	//11
		titles.add("经纱3号数");	//12
		titles.add("经纱4号数");	//13
		titles.add("经纱1用量");	//14
		titles.add("经纱2用量");	//15
		titles.add("经纱3用量");	//16
		titles.add("经纱4用量");	//17
		titles.add("纬纱1名称");	//18
		titles.add("纬纱2名称");	//19
		titles.add("纬纱3名称");	//20
		titles.add("纬纱4名称");	//21
		titles.add("纬纱1号数");	//22
		titles.add("纬纱2号数");	//23
		titles.add("纬纱3号数");	//24
		titles.add("纬纱4号数");	//25
		titles.add("纬纱1用量");	//26
		titles.add("纬纱2用量");	//27
		titles.add("纬纱3用量");	//28
		titles.add("纬纱4用量");	//29
		titles.add("纬比1");	//30
		titles.add("纬比2");	//31
		titles.add("纬比3");	//32
		titles.add("纬比4");	//33
		titles.add("经比1");	//34
		titles.add("经比2");	//35
		titles.add("经比3");	//36
		titles.add("经比4");	//37
		titles.add("总经1");	//38
		titles.add("总经2");	//39
		titles.add("总经3");	//40
		titles.add("总经4");	//41
		titles.add("纬密");	//42
		titles.add("纬环");	//43
		titles.add("经环");	//44
		titles.add("经缩");	//45
		titles.add("经伸长");	//46
		titles.add("纬缩");	//47
		titles.add("经1单价");	//48
		titles.add("经2单价");	//49
		titles.add("经3单价");	//50
		titles.add("经4单价");	//51
		titles.add("纬1单价");	//52
		titles.add("纬2单价");	//53
		titles.add("纬3单价");	//54
		titles.add("纬4单价");	//55
		titles.add("经纬成本");	//56
		titles.add("经纬总量");	//57
		titles.add("门幅");	//58
		titles.add("工费/米");	//59
		titles.add("单价");	//60
		titles.add("工费/纬");	//61
		titles.add("产量");	//62
		titles.add("工费");	//63
		dataMap.put("titles", titles);
		List<PageData> varOList = pinzhongService.listAll(pd);
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", varOList.get(i).getString("CODE"));	    //1
			vpd.put("var2", varOList.get(i).getString("PINZHONGNAME"));	    //2
			vpd.put("var3", varOList.get(i).get("KOUFU").toString());	//3
			vpd.put("var4", varOList.get(i).get("KOUHAO").toString());	//4
			vpd.put("var5", varOList.get(i).get("BUFU").toString());	//5
			vpd.put("var6", varOList.get(i).getString("WARP1NAME"));	    //6
			vpd.put("var7", varOList.get(i).getString("WARP2NAME"));	    //7
			vpd.put("var8", varOList.get(i).getString("WARP3NAME"));	    //8
			vpd.put("var9", varOList.get(i).getString("WARP4NAME"));	    //9
			vpd.put("var10", varOList.get(i).get("WARP1NO").toString());	//10
			vpd.put("var11", varOList.get(i).get("WARP2NO").toString());	//11
			vpd.put("var12", varOList.get(i).get("WARP3NO").toString());	//12
			vpd.put("var13", varOList.get(i).get("WARP4NO").toString());	//13
			vpd.put("var14", varOList.get(i).get("WARP1USE").toString());	//14
			vpd.put("var15", varOList.get(i).get("WARP2USE").toString());	//15
			vpd.put("var16", varOList.get(i).get("WARP3USE").toString());	//16
			vpd.put("var17", varOList.get(i).get("WARP4USE").toString());	//17
			vpd.put("var18", varOList.get(i).getString("WEFT1NAME"));	    //18
			vpd.put("var19", varOList.get(i).getString("WEFT2NAME"));	    //19
			vpd.put("var20", varOList.get(i).getString("WEFT3NAME"));	    //20
			vpd.put("var21", varOList.get(i).getString("WEFT4NAME"));	    //21
			vpd.put("var22", varOList.get(i).get("WEFT1NO").toString());	//22
			vpd.put("var23", varOList.get(i).get("WEFT2NO").toString());	//23
			vpd.put("var24", varOList.get(i).get("WEFT3NO").toString());	//24
			vpd.put("var25", varOList.get(i).get("WEFT4NO").toString());	//25
			vpd.put("var26", varOList.get(i).get("WEFT1USE").toString());	//26
			vpd.put("var27", varOList.get(i).get("WEFT2USE").toString());	//27
			vpd.put("var28", varOList.get(i).get("WEFT3USE").toString());	//28
			vpd.put("var29", varOList.get(i).get("WEFT4USE").toString());	//29
			vpd.put("var30", varOList.get(i).get("WEFT1BI").toString());	//30
			vpd.put("var31", varOList.get(i).get("WEFT2BI").toString());	//31
			vpd.put("var32", varOList.get(i).get("WEFT3BI").toString());	//32
			vpd.put("var33", varOList.get(i).get("WEFT4BI").toString());	//33
			vpd.put("var34", varOList.get(i).get("WARP1BI").toString());	//34
			vpd.put("var35", varOList.get(i).get("WARP2BI").toString());	//35
			vpd.put("var36", varOList.get(i).get("WARP3BI").toString());	//36
			vpd.put("var37", varOList.get(i).get("WARP4BI").toString());	//37
			vpd.put("var38", varOList.get(i).get("WARP1ZONG").toString());	//38
			vpd.put("var39", varOList.get(i).get("WARP2ZONG").toString());	//39
			vpd.put("var40", varOList.get(i).get("WARP3ZONG").toString());	//40
			vpd.put("var41", varOList.get(i).get("WARP4ZONG").toString());	//41
			vpd.put("var42", varOList.get(i).get("WEFTDENSITY").toString());	//42
			vpd.put("var43", varOList.get(i).get("WARPHUAN").toString());	//43
			vpd.put("var44", varOList.get(i).get("WEFTHUAN").toString());	//44
			vpd.put("var45", varOList.get(i).get("WEFTSUO").toString());	//45
			vpd.put("var46", varOList.get(i).get("WEFTSHENCHANG").toString());	//46
			vpd.put("var47", varOList.get(i).get("WARPSUO").toString());	//47
			vpd.put("var48", varOList.get(i).get("WEFT1PRICE").toString());	//48
			vpd.put("var49", varOList.get(i).get("WEFT2PRICE").toString());	//49
			vpd.put("var50", varOList.get(i).get("WEFT3PRICE").toString());	//50
			vpd.put("var51", varOList.get(i).get("WEFT4PRICE").toString());	//51
			vpd.put("var52", varOList.get(i).get("WRAP1PRICE").toString());	//52
			vpd.put("var53", varOList.get(i).get("WRAP2PRICE").toString());	//53
			vpd.put("var54", varOList.get(i).get("WRAP3PRICE").toString());	//54
			vpd.put("var55", varOList.get(i).get("WRAP4PRICE").toString());	//55
			vpd.put("var56", varOList.get(i).get("TOTALPRICE").toString());	//56
			vpd.put("var57", varOList.get(i).get("TOTALAMOUNT").toString());	//57
			vpd.put("var58", varOList.get(i).get("MENFU").toString());	//58
			vpd.put("var59", varOList.get(i).get("GONGFEE").toString());	//59
			vpd.put("var60", varOList.get(i).get("DANJIA").toString());	//60
			vpd.put("var61", varOList.get(i).get("GONGFEE2").toString());	//61
			vpd.put("var62", varOList.get(i).get("CHANLIANG").toString());	//62
			vpd.put("var63", varOList.get(i).get("TOTGONGFEE").toString());	//63
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		ObjectExcelView erv = new ObjectExcelView();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	

	 /**去纱线名称生成页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goyarnname_edit")
	public ModelAndView goYarnname_edit()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		
		
		List<PageData> manListfiber = fiberService.listAll(pd);
//		for(PageData add :manListfiber){
//			System.out.println(add);
//		}
		mv.setViewName("Order/pinzhong/yarnname_edit");
		mv.addObject("msg", "yarnname_edit");
		mv.addObject("pd", pd);
		mv.addObject("manListfiber", manListfiber);
		return mv;
	}	
	 /**纱线名称生成页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/yarnname_edit")
	public ModelAndView yarnname_edit()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		
		String yarnname="";
		String yarnnum="";
		int proportion=0;
		if (pd.get("COTTON") != null && !"".equals(pd.get("COTTON"))) {
			yarnname=yarnname+"C"+pd.get("COTTON")+"/";
			yarnnum=yarnnum+"01";
			proportion=proportion+Integer.valueOf((String) pd.get("COTTON"));
		}
		if (pd.get("JCOTTON") != null && !"".equals(pd.get("JCOTTON"))) {
			yarnname=yarnname+"JC"+pd.get("JCOTTON")+"/";
			yarnnum=yarnnum+"02";
			proportion=proportion+Integer.valueOf((String) pd.get("JCOTTON"));
		}
		if (pd.get("TERYLENE") != null && !"".equals(pd.get("TERYLENE"))) {
			
				yarnname=yarnname+"T"+pd.get("TERYLENE")+"/";
			yarnnum=yarnnum+"03";
			proportion=proportion+Integer.valueOf((String) pd.get("TERYLENE"));
		}
		if (pd.get("NYLON") != null && !"".equals(pd.get("NYLON"))) {
			yarnname=yarnname+"N"+pd.get("NYLON")+"/";
			yarnnum=yarnnum+"05";
			proportion=proportion+Integer.valueOf((String) pd.get("NYLON"));
		}
		if (pd.get("VISCOSE") != null && !"".equals(pd.get("VISCOSE"))) {
			yarnname=yarnname+"R"+pd.get("VISCOSE")+"/";
			yarnnum=yarnnum+"06";
			proportion=proportion+Integer.valueOf((String) pd.get("VISCOSE"));
		}
		
		
		
		if (pd.get("SPANDEX") != null && !"".equals(pd.get("SPANDEX"))) {
			yarnname=yarnname+"SP"+pd.get("SPANDEX")+"/";
			yarnnum=yarnnum+"14";
			proportion=proportion+Integer.valueOf((String) pd.get("SPANDEX"));
		}
		
		
		if (pd.get("TUSSAHats") != null && !"".equals(pd.get("TUSSAHats"))) {
			yarnname=yarnname+"ATS"+pd.get("TUSSAHats")+"/";
			yarnnum=yarnnum+"08";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAHats"));
		}
		
		if (pd.get("TUSSAHgts") != null && !"".equals(pd.get("TUSSAHgts"))) {
			yarnname=yarnname+"GTS"+pd.get("TUSSAHgts")+"/";
			yarnnum=yarnnum+"09";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAHgts"));
		}
		
		if (pd.get("TUSSAHts") != null && !"".equals(pd.get("TUSSAHts"))) {
			yarnname=yarnname+"TS"+pd.get("TUSSAHts")+"/";
			yarnnum=yarnnum+"07";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAHts"));
		}
		
		
		if (pd.get("OtherFiberoneText") != null && !"".equals(pd.get("OtherFiberoneText"))&&pd.get("OtherFiberoneInput") != null && !"".equals(pd.get("OtherFiberoneInput"))) {
			//分割其他纤维
			String fibernum= (String) pd.get("OtherFiberoneText");
			
			String []fiString= fibernum.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiString[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiString[1]+pd.get("OtherFiberoneInput")+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFiberoneInput"));
			
		}
		if (pd.get("OtherFibertwoText") != null && !"".equals(pd.get("OtherFibertwoText"))&&pd.get("OtherFibertwoInput") != null && !"".equals(pd.get("OtherFibertwoInput"))) {
			//分割其他纤维
			
			String fibernumtwo= (String) pd.get("OtherFibertwoText");
			
			
			String []fiStringtwo= fibernumtwo.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiStringtwo[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiStringtwo[1]+pd.get("OtherFibertwoInput")+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFibertwoInput"));
			
		}
		if (pd.get("OtherFiberthreeText") != null && !"".equals(pd.get("OtherFiberthreeText"))&&pd.get("OtherFiberthreeInput") != null && !"".equals(pd.get("OtherFiberthreeInput"))) {
			//分割其他纤维
			String fibernumthree= (String) pd.get("OtherFiberthreeText");
			
			String []fiStringthree= fibernumthree.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiStringthree[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiStringthree[1]+pd.get("OtherFiberthreeInput")+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFiberthreeInput"));
			
		}
	//	System.out.println(	yarnnum);
			
			
			//查询序号
			// PageData pageDatanumone =fiberService.listFBNObydaihao(pageDatanum);
			 	//	System.out.println(pageDatanumone);
			
		
		
			
		if (yarnname.length()>1) {
			yarnname=yarnname.substring(0,yarnname.length()-1);//去除最后一个斜杠
		}
		int numberint=yarnname.indexOf("100");
		
		if (numberint>=1) {
			int s=yarnname.length();
			yarnname=yarnname.substring(0, s-3);
		
		}

		String yarncount="";
		if (pd.get("YARNUINT") != null && !"".equals(pd.get("YARNUINT"))
				&& pd.get("UINTVALUE") != null && !"".equals(pd.get("UINTVALUE"))) {
			yarncount=pd.getString("UINTVALUE")+pd.getString("YARNUINT");
			yarnname=yarnname+" "+yarncount;
		}
		
		if (pd.get("FUSINUM") != null && !"".equals(pd.get("FUSINUM"))) {
			yarnname=yarnname+"/"+pd.getString("FUSINUM")+"F";	
		}
		
		if (pd.get("ELITWIST") != null && !"".equals(pd.get("ELITWIST"))) {				
			yarnname=yarnname+" /"+pd.getString("ELITWIST");
		}
		
		if (pd.get("COREYARN") != null && !"".equals(pd.get("COREYARN"))) {
			yarnname=yarnname+"+"+pd.getString("COREYARN");	
		}
		
		//获取today年月日
		String temp_str="";     
	    Date dt = new Date();     
	    //最后的aa表示“上午”或“下午”    HH表示24小时制    如果换成hh表示12小时制 
	   
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");     
	    temp_str=sdf.format(dt);  
	    temp_str=temp_str+" 00:00:00";
		String year=temp_str.substring(2,4);
		String month=temp_str.substring(5,7);
		String date=temp_str.substring(8,10);
		if("自纺".equals(pd.get("TRANSTYPE"))){
			yarnname=year+yarnname;
		}
		String prop="success";
		//System.out.println(proportion);
		if (proportion%100!=0) {
			yarnname=yarnname+"---输入的所有纱线种类比重的和为100！";
			prop="failure";
		}else if ( "".equals(yarncount)) {
			prop="failure";
			yarnname=yarnname+"---纱线细度需要填写";
		}
		if (yarnnum.length()<9&&yarnnum.length()>0) {
			yarnnum =String.format("%0" + 10 + "d", Integer.parseInt(yarnnum) );
		}
		if (yarnnum.length()>10) {
			yarnnum =yarnnum.substring(0, 10);
		}
		
		String YARNNUMBER=yarnnum;
		Object transtype=pd.getString("TRANSTYPE");
		PageData pageData = new PageData();
		String barcode=year+month+date+yarnnum;
		pageData.put("compose", barcode);			
	    pageData.put("lastStart", temp_str);
	    String indexoflog =translogService.selectTodayIndex(pageData)+"";
	    indexoflog =String.format("%0" + 3 + "d", Integer.parseInt(indexoflog)+1);
	    barcode=barcode+indexoflog;
	    pd.put("YARNNAMECREAT_ID", this.get32UUID());	//主键
		pd.put("YARNNAME", yarnname);
		//pd.put("COMPOSE", YARNNUMBER);
		//pd.put("YARNCOUNT", yarncount);
		pd.put("TRANSTYPE", transtype);  //入库方式
		//pd.put("WARP1NAME", yarnname);
		
		if (prop=="success") {
			mv.setViewName("save_result");
			yarnNameCreatService.deleteAllData(pd);
			yarnNameCreatService.save(pd);
			
			mv.addObject("msg", "success");
			mv.addObject("pd", pd);
			
		}else{
		mv.setViewName("Order/pinzhong/yarnname_edit");
		mv.addObject("msg", "yarnname_edit");
		mv.addObject("pd", pd);
		}
		
		tempyarnvalueString=yarnname;
		return mv;
	}	
	/**去纱线名称生成页面2222222222222222222
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goyarnname_edittwo")
	public ModelAndView goYarnname_edittwo()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		List<PageData> manListfiber = fiberService.listAll(pd);
		mv.setViewName("Order/pinzhong/yarnname_edittwo");
		mv.addObject("msg", "yarnname_edittwo");
		mv.addObject("manListfiber", manListfiber);
		mv.addObject("pd", pd);
		return mv;
	}	
	 /**纱线名称生成页面222222222222222222
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/yarnname_edittwo")
	public ModelAndView yarnname_edittwo()throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String yarnname="";
		String yarnnum="";
		String yarnxiString="";
		int proportion=0;
		if (pd.get("COTTON") != null && !"".equals(pd.get("COTTON"))) {
			
			yarnxiString=pd.getString("COTTONS");
			yarnname=yarnname+"C"+pd.get("COTTON")+yarnxiString+"/";
			
			yarnnum=yarnnum+"01";
			proportion=proportion+Integer.valueOf((String) pd.get("COTTON"));
		}
		if (pd.get("JCOTTON") != null && !"".equals(pd.get("JCOTTON"))) {
			yarnxiString=pd.getString("JCOTTONS");
			yarnname=yarnname+"JC"+pd.get("JCOTTON")+yarnxiString+"/";
			yarnnum=yarnnum+"02";
			proportion=proportion+Integer.valueOf((String) pd.get("JCOTTON"));
		}
		if (pd.get("TERYLENE") != null && !"".equals(pd.get("TERYLENE"))) {
			
			yarnxiString=pd.getString("TERYLENES");
				yarnname=yarnname+"T"+pd.get("TERYLENE")+yarnxiString+"/";
				if (pd.get("FUSINUM") != null && !"".equals(pd.get("FUSINUM"))) {
					yarnname=yarnname+pd.getString("FUSINUM")+"F"+" ";
				}
			yarnnum=yarnnum+"03";
			proportion=proportion+Integer.valueOf((String) pd.get("TERYLENE"));
		}
		if (pd.get("NYLON") != null && !"".equals(pd.get("NYLON"))) {
			yarnxiString=pd.getString("NYLONS");
			yarnname=yarnname+"N"+pd.get("NYLON")+yarnxiString+"/";
				if (pd.get("FUSINUM") != null && !"".equals(pd.get("FUSINUM"))) {
						yarnname=yarnname+pd.getString("FUSINUM")+"F"+" ";
				}
			yarnnum=yarnnum+"05";
			proportion=proportion+Integer.valueOf((String) pd.get("NYLON"));
		}
		if (pd.get("VISCOSE") != null && !"".equals(pd.get("VISCOSE"))) {
			yarnxiString=pd.getString("VISCOSES");
			yarnname=yarnname+"R"+pd.get("VISCOSE")+yarnxiString+"/";
			yarnnum=yarnnum+"06";
			proportion=proportion+Integer.valueOf((String) pd.get("VISCOSE"));
		}
		if (pd.get("TUSSAHats") != null && !"".equals(pd.get("TUSSAHats"))) {
			yarnxiString=pd.getString("TERYLENES");
			yarnname=yarnname+"ATS"+pd.get("TUSSAHats")+yarnxiString+"/";
			yarnnum=yarnnum+"07";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAHats"));
		}
		
		if (pd.get("TUSSAHgts") != null && !"".equals(pd.get("TUSSAHgts"))) {
			yarnxiString=pd.getString("TUSSAHgtsS");
			yarnname=yarnname+"GTS"+pd.get("TUSSAHgts")+yarnxiString+"/";
			yarnnum=yarnnum+"07";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAHgts"));
		}
		
		if (pd.get("TUSSAts") != null && !"".equals(pd.get("TUSSAts"))) {
			yarnxiString=pd.getString("TUSSAtsS");
			yarnname=yarnname+"TS"+pd.get("TUSSAts")+yarnxiString+"/";
			yarnnum=yarnnum+"07";
			proportion=proportion+Integer.valueOf((String) pd.get("TUSSAts"));
		}
		
		if (pd.get("SPANDEX") != null && !"".equals(pd.get("SPANDEX"))) {
			yarnxiString=pd.getString("SPANDEXS");
			yarnname=yarnname+"SP"+pd.get("SPANDEX")+yarnxiString+"/";
			yarnnum=yarnnum+"14";
			proportion=proportion+Integer.valueOf((String) pd.get("SPANDEX"));
		}
		
		
		if (pd.get("OtherFiberoneText") != null && !"".equals(pd.get("OtherFiberoneText"))&&pd.get("OtherFiberoneInput") != null && !"".equals(pd.get("OtherFiberoneInput"))) {
			//分割其他纤维
			yarnxiString=pd.getString("OtherFiberoneInputS");
			String fibernum= (String) pd.get("OtherFiberoneText");
			
			String []fiString= fibernum.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiString[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiString[1]+pd.get("OtherFiberoneInput")+yarnxiString+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFiberoneInput"));
			
		}
		if (pd.get("OtherFibertwoText") != null && !"".equals(pd.get("OtherFibertwoText"))&&pd.get("OtherFibertwoInput") != null && !"".equals(pd.get("OtherFibertwoInput"))) {
			//分割其他纤维
			yarnxiString=pd.getString("OtherFibertwoInputS");
			String fibernum= (String) pd.get("OtherFibertwoText");
			
			String []fiString= fibernum.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiString[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiString[1]+pd.get("OtherFibertwoInput")+yarnxiString+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFibertwoInput"));
			
		}
		if (pd.get("OtherFiberthreeText") != null && !"".equals(pd.get("OtherFiberthreeText"))&&pd.get("OtherFiberthreeInput") != null && !"".equals(pd.get("OtherFiberthreeInput"))) {
			//分割其他纤维
			yarnxiString=pd.getString("OtherFiberthreeInputS");
			String fibernum= (String) pd.get("OtherFiberthreeText");
			
			String []fiString= fibernum.split(" ");
			
			PageData pageDatanum= new PageData();
			pageDatanum.put("FBDAIHAO", fiString[1]);
			PageData  pageData1= fiberService.ListNumByDaihao(pageDatanum);
			//System.out.println(pageData1);
			yarnname=yarnname+fiString[1]+pd.get("OtherFiberthreeInput")+yarnxiString+"/";
			yarnnum=yarnnum+pageData1.getString("FBNO");
			proportion=proportion+Integer.valueOf((String) pd.get("OtherFiberthreeInput"));
			
		}
		
		
		
		if (yarnname.length()>1) {
			yarnname=yarnname.substring(0,yarnname.length()-1);//去除最后一个斜杠
		}
		int numberint=yarnname.indexOf("100");
		
		if (numberint>=1) {
			int s=yarnname.length();
			yarnname=yarnname.substring(0, s-3);
		
		}

		
//		if (pd.get("FUSINUM") != null && !"".equals(pd.get("FUSINUM"))) {
//			yarnname=yarnname+"/"+pd.getString("FUSINUM")+"F";	
//		}
		if (pd.get("ELITWIST") != null && !"".equals(pd.get("ELITWIST"))) {				
			
			yarnname=yarnname+" /"+pd.getString("ELITWIST");
		}
		
		if (pd.get("COREYARN") != null && !"".equals(pd.get("COREYARN"))) {
			
			yarnname=yarnname+"+"+pd.getString("COREYARN");	
		}
		
		//获取today年月日
		String temp_str="";     
	    Date dt = new Date();     
	    //最后的aa表示“上午”或“下午”    HH表示24小时制    如果换成hh表示12小时制 
	   
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");     
	    temp_str=sdf.format(dt);  
	    temp_str=temp_str+" 00:00:00";
		String year=temp_str.substring(2,4);
		String month=temp_str.substring(5,7);
		String date=temp_str.substring(8,10);
		if("自纺".equals(pd.get("TRANSTYPE"))){
			yarnname=year+yarnname;
		}
		
//		if (proportion!=100) {
//			yarnname=yarnname+"---输入的所有纱线种类比重的和为100！";
//			prop="failure";
//		}else if ( "".equals(yarncount)) {
//			prop="failure";
//			yarnname=yarnname+"---纱线细度需要填写";
//		}
		if (yarnnum.length()<9&&yarnnum.length()>0) {
			yarnnum =String.format("%0" + 10 + "d", Integer.parseInt(yarnnum) );
		}
		if (yarnnum.length()>10) {
			yarnnum =yarnnum.substring(0, 10);
		}
		
		String YARNNUMBER=yarnnum;
		Object transtype=pd.getString("TRANSTYPE");
		PageData pageData = new PageData();
		String barcode=year+month+date+yarnnum;
		pageData.put("compose", barcode);			
	    pageData.put("lastStart", temp_str);
	    String indexoflog =translogService.selectTodayIndex(pageData)+"";
	    indexoflog =String.format("%0" + 3 + "d", Integer.parseInt(indexoflog)+1);
	    barcode=barcode+indexoflog;
	    pd.put("YARNNAMECREAT_ID", this.get32UUID());	//主键
	  //  pd.put("BARCODE", barcode);
		pd.put("YARNNAME", yarnname);
	//	pd.put("COMPOSE", YARNNUMBER);
		//pd.put("YARNCOUNT", yarncount);
		pd.put("TRANSTYPE", transtype);  //入库方式
		String prop="success";
		if (prop=="success") {
			mv.setViewName("save_result");
			yarnNameCreatService.deleteAllData(pd);
			yarnNameCreatService.save(pd);
			mv.addObject("msg", "success");
			mv.addObject("pd", pd);
		}else{
		mv.setViewName("Order/pinzhong/yarnname_edittwo");
		mv.addObject("msg", "yarnname_edittwo");
		mv.addObject("pd", pd);
		}
		tempyarnvalueStringweft=yarnname;
		return mv;
	}	
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
