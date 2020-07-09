package com.fh.controller.Order.inorder;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
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
import com.fh.service.Order.inorder.InorderManager;

/** 
 * 说明：订单生产
 * 创建人：Xiangjun
 * 创建时间：2017-08-31
 */
@Controller
@RequestMapping(value="/inorder")
public class InorderController extends BaseController {
	
	String menuUrl = "inorder/list.do"; //菜单地址(权限用)
	@Resource(name="inorderService")
	private InorderManager inorderService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"新增Inorder");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("INORDER_ID", this.get32UUID());	//主键
		inorderService.save(pd);
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
		logBefore(logger, Jurisdiction.getUsername()+"删除Inorder");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		PageData pd = new PageData();
		pd = this.getPageData();
		inorderService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"修改Inorder");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		inorderService.edit(pd);
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
		logBefore(logger, Jurisdiction.getUsername()+"列表Inorder");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<PageData>	varList = inorderService.list(page);	//列出Inorder列表
		mv.setViewName("Order/inorder/inorder_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("QX",Jurisdiction.getHC());	//按钮权限
		return mv;
	}
	/**
	 * 订单详情页面
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/detailorder")
	public ModelAndView detailorder(Page page) throws Exception{
		logBefore(logger, Jurisdiction.getUsername()+"列表Inorder");
		//if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<PageData>	varList = inorderService.list(page);	//列出Inorder列表
		mv.setViewName("Order/inorder/inorder_detail");
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
		mv.setViewName("Order/inorder/inorder_edit");
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
		pd = inorderService.findById(pd);	//根据ID读取
		mv.setViewName("Order/inorder/inorder_edit");
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
		logBefore(logger, Jurisdiction.getUsername()+"批量删除Inorder");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			inorderService.deleteAll(ArrayDATA_IDS);
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
		logBefore(logger, Jurisdiction.getUsername()+"导出Inorder到excel");
		if(!Jurisdiction.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("客户名称");	//1
		titles.add("销售员");	//2
		titles.add("合同情况");	//3
		titles.add("截止日期");	//4
		titles.add("订单类型");	//5
		titles.add("订单日期");	//6
		titles.add("订单号");	//7
		titles.add("品种1ID");	//8
		titles.add("品种1数量");	//9
		titles.add("万米数1");	//10
		titles.add("上机时间1");	//11
		titles.add("品种2ID");	//12
		titles.add("品种2数量");	//13
		titles.add("万米数2");	//14
		titles.add("上机时间2");	//15
		titles.add("品种3ID");	//16
		titles.add("品种3数量");	//17
		titles.add("万米数3");	//18
		titles.add("上机时间3");	//19
		titles.add("品种4ID");	//20
		titles.add("品种4数量");	//21
		titles.add("万米数4");	//22
		titles.add("上机时间4");	//23
		titles.add("品种5ID");	//24
		titles.add("品种5数量");	//25
		titles.add("万米数5");	//26
		titles.add("上机时间5");	//27
		titles.add("品种6ID");	//28
		titles.add("品种6数量");	//29
		titles.add("万米数6");	//30
		titles.add("上机时间6");	//31
		titles.add("品种7ID");	//32
		titles.add("品种7数量");	//33
		titles.add("万米数7");	//34
		titles.add("上机时间7");	//35
		titles.add("品种8ID");	//36
		titles.add("品种8数量");	//37
		titles.add("万米数8");	//38
		titles.add("上机时间8");	//39
		titles.add("品种9ID");	//40
		titles.add("品种9数量");	//41
		titles.add("万米数9");	//42
		titles.add("上机时间9");	//43
		titles.add("品种10ID");	//44
		titles.add("品种10数量");	//45
		titles.add("万米数10");	//46
		titles.add("上机时间10");	//47
		titles.add("订单状态");	//48
		titles.add("备注");	//49
		dataMap.put("titles", titles);
		List<PageData> varOList = inorderService.listAll(pd);
		List<PageData> varList = new ArrayList<PageData>();
		for(int i=0;i<varOList.size();i++){
			PageData vpd = new PageData();
			vpd.put("var1", varOList.get(i).getString("KEHUNAME"));	    //1
			vpd.put("var2", varOList.get(i).getString("SELLER"));	    //2
			vpd.put("var3", varOList.get(i).getString("CONTRACT"));	    //3
			vpd.put("var4", varOList.get(i).getString("DEADLINE"));	    //4
			vpd.put("var5", varOList.get(i).getString("ORDERINFO"));	    //5
			vpd.put("var6", varOList.get(i).getString("ORDERTIME"));	    //6
			vpd.put("var7", varOList.get(i).getString("ORDERNO"));	    //7
			vpd.put("var8", varOList.get(i).getString("PZ1ID"));	    //8
			vpd.put("var9", varOList.get(i).get("PZ1NUM").toString());	//9
			vpd.put("var10", varOList.get(i).get("WANM1").toString());	//10
			vpd.put("var11", varOList.get(i).getString("START1"));	    //11
			vpd.put("var12", varOList.get(i).getString("PZ2ID"));	    //12
			vpd.put("var13", varOList.get(i).get("PZ2NUM").toString());	//13
			vpd.put("var14", varOList.get(i).get("WANM2").toString());	//14
			vpd.put("var15", varOList.get(i).getString("START2"));	    //15
			vpd.put("var16", varOList.get(i).getString("PZ3ID"));	    //16
			vpd.put("var17", varOList.get(i).get("PZ3NUM").toString());	//17
			vpd.put("var18", varOList.get(i).get("WANM3").toString());	//18
			vpd.put("var19", varOList.get(i).getString("START3"));	    //19
			vpd.put("var20", varOList.get(i).getString("PZ4ID"));	    //20
			vpd.put("var21", varOList.get(i).get("PZ4NUM").toString());	//21
			vpd.put("var22", varOList.get(i).get("WANM4").toString());	//22
			vpd.put("var23", varOList.get(i).getString("START4"));	    //23
			vpd.put("var24", varOList.get(i).getString("PZ5ID"));	    //24
			vpd.put("var25", varOList.get(i).get("PZ5NUM").toString());	//25
			vpd.put("var26", varOList.get(i).get("WANM5").toString());	//26
			vpd.put("var27", varOList.get(i).getString("START5"));	    //27
			vpd.put("var28", varOList.get(i).getString("PZ6ID"));	    //28
			vpd.put("var29", varOList.get(i).get("PZ6NUM").toString());	//29
			vpd.put("var30", varOList.get(i).get("WANM6").toString());	//30
			vpd.put("var31", varOList.get(i).getString("START6"));	    //31
			vpd.put("var32", varOList.get(i).getString("PZ7ID"));	    //32
			vpd.put("var33", varOList.get(i).get("PZ7NUM").toString());	//33
			vpd.put("var34", varOList.get(i).get("WANM7").toString());	//34
			vpd.put("var35", varOList.get(i).getString("START7"));	    //35
			vpd.put("var36", varOList.get(i).getString("PZ8ID"));	    //36
			vpd.put("var37", varOList.get(i).get("PZ8NUM").toString());	//37
			vpd.put("var38", varOList.get(i).get("WANM8").toString());	//38
			vpd.put("var39", varOList.get(i).getString("START8"));	    //39
			vpd.put("var40", varOList.get(i).getString("PZ9ID"));	    //40
			vpd.put("var41", varOList.get(i).get("PZ9NUM").toString());	//41
			vpd.put("var42", varOList.get(i).get("WANM9").toString());	//42
			vpd.put("var43", varOList.get(i).getString("START9"));	    //43
			vpd.put("var44", varOList.get(i).getString("PZ10ID"));	    //44
			vpd.put("var45", varOList.get(i).get("PZ10NUM").toString());	//45
			vpd.put("var46", varOList.get(i).get("WANM10").toString());	//46
			vpd.put("var47", varOList.get(i).getString("START10"));	    //47
			vpd.put("var48", varOList.get(i).getString("ORDERSTATE"));	    //48
			vpd.put("var49", varOList.get(i).getString("REMARKS"));	    //49
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		ObjectExcelView erv = new ObjectExcelView();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
