package com.recon.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.recon.dao.impl.DataCountDaoImpl;
import com.recon.model.CompareSetupBean;
import com.recon.model.NFSSettlementBean;
import com.recon.service.CompareService;
import com.recon.service.ICompareConfigService;
import com.recon.service.IReconProcessService;
import com.recon.service.ISourceService;
import com.recon.util.CSRFToken;
import com.recon.util.FileDetailsJson;
import com.recon.util.demo;

@Controller
public class DataCountController {
	
 
	@Autowired ISourceService iSourceService;

	private static final Logger logger = Logger.getLogger(DataCountController.class);
	
	@RequestMapping(value = "DataCount", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView DataCountget(ModelAndView modelAndView,@RequestParam("category")String category,HttpServletRequest request) throws Exception {
	 
		logger.info("***** DataCount Start ****");
		List<CompareSetupBean> setupBeans = new ArrayList<CompareSetupBean>(); 
		String display="";
		//logger.info("RECON PROCESS GET");

		List<String> subcat = new ArrayList<>();
		 
		 logger.info("in GetHeaderList"+category);
		 System.out.println("in GetHeaderList"+category);
		 
         subcat = iSourceService.getSubcategories(category);
         if(category.equals("ONUS") || category.equals("AMEX") ||category.equals("CARDTOCARD") ||category.equals("WCC") ) {
        	 
        	 display="none";
         }
          
         String csrf = CSRFToken.getTokenForSession(request.getSession());
         
        modelAndView.addObject("category", category);
		modelAndView.setViewName("DataCount");
		logger.info("***** DataCount Start ****");
		return modelAndView;
	}
	
	
	
	@RequestMapping(value = "DataCount", method = RequestMethod.POST)
    
	public String DataCountpost(HttpServletRequest request ,HttpSession httpSession,NFSSettlementBean nfsSettlementBean) throws Exception {
	 
		System.out.println("here ");
		
		   
     	System.out.println("in GetHeaderList post"+nfsSettlementBean.getCategory());
     	System.out.println("in GetHeaderList post"+nfsSettlementBean.getStSubCategory());


		return "Success";
		
	}
	
}
