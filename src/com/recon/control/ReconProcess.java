package com.recon.control;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.Module.SetupContext;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.recon.model.CompareBean;
import com.recon.model.CompareSetupBean;
import com.recon.model.FilterationBean;
import com.recon.model.KnockOffBean;
import com.recon.model.LoginBean;
import com.recon.service.CompareService;
import com.recon.service.ICompareConfigService;
import com.recon.service.IReconProcessService;
import com.recon.service.ISourceService;
import com.recon.util.CSRFToken;
import com.recon.util.FileDetailsJson;
import com.recon.util.demo;

@Controller
public class ReconProcess {

	private static final String ERROR_MSG = "error_msg";
	private static final String SUCCESS_MSG = "success_msg";
	private static final Logger logger = Logger.getLogger(ReconProcess.class);
	@Autowired ICompareConfigService icompareConfigService;
	@Autowired CompareService compareService;
	@Autowired IReconProcessService reconProcess;
	@Autowired ISourceService iSourceService;
	
	
	@RequestMapping(value = "ReconProcess", method = RequestMethod.GET)
	public ModelAndView ReconProcess1(ModelAndView modelAndView,@RequestParam("category")String category,HttpServletRequest request) throws Exception {
		logger.info("***** ReconProcess.ReconProcess1 Start ****");
		List<CompareSetupBean> setupBeans = new ArrayList<CompareSetupBean>(); 
		String display="";
		logger.info("RECON PROCESS GET");
		
		
		 List<String> subcat = new ArrayList<>();
		 
		 logger.info("in GetHeaderList"+category);
		 
         subcat = iSourceService.getSubcategories(category);
         if(category.equals("ONUS") || category.equals("AMEX") ||category.equals("CARDTOCARD") ||category.equals("WCC") ) {
        	 
        	 display="none";
         }
         
         //modelAndView.addObject(CSRFToken.CSRF_PARAM_NAME,  CSRFToken.getTokenForSession(request.getSession()));
         
         String csrf = CSRFToken.getTokenForSession(request.getSession());
		 
 		//redirectAttributes.addFlashAttribute("CSRFToken", csrf);
 		modelAndView.addObject("CSRFToken", csrf);
         modelAndView.addObject("category", category);
		modelAndView.addObject("subcategory",subcat );
		modelAndView.addObject("display",display);
		modelAndView.setViewName("ReconProcess");
		logger.info("***** ReconProcess.ReconProcess1 End ****");
		return modelAndView;

	}
	@RequestMapping(value = "SeeRule", method = RequestMethod.GET)
	public ModelAndView seeRule(ModelAndView modelAndView) {
		modelAndView.setViewName("SeeRule");
		return modelAndView;

	}
	
	 @RequestMapping(value = "/Filedetails", method = RequestMethod.POST)
	 @ResponseBody
	 public  FileDetailsJson GetHeaderList (@RequestParam("category")String category,FileDetailsJson dataJson) throws Exception{
		 logger.info("***** ReconProcess.GetHeaderList Start ****");
		 try{
		 JSONObject objJSON = new JSONObject();
			List<CompareSetupBean> setupBeans = new ArrayList<CompareSetupBean>(); 
			 
			 logger.info("in GetHeaderList"+category);
			 HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
	         Gson gson = new GsonBuilder().setPrettyPrinting().create();
			 
			 setupBeans =  icompareConfigService.getFileList(category);
			 JSONROOT.put("Records", setupBeans);
			 String jsonArray = gson.toJson(JSONROOT);
			 dataJson.setParams("OK", setupBeans,0);
			 
			 objJSON.put("setupBeans", setupBeans);
			 
			 logger.info("***** ReconProcess.GetHeaderList End ****");
			 
		 }catch(Exception e){
			 demo.logSQLException(e, "ReconProcess.GetHeaderList");
			 logger.error(" error in ReconProcess.GetHeaderList", new Exception("ReconProcess.GetHeaderList",e));
			 dataJson.setParams("ERROR", e.getMessage());
			 
		 }
			 
			 
			return dataJson;
			
		 
	 }
	
	 
	 @RequestMapping(value = "runProcess", method = RequestMethod.POST)
	 @ResponseBody
	 public  String runProcess (@RequestParam("category")String category,FileDetailsJson dataJson, ModelAndView modelAndView,CompareSetupBean setupBean,HttpSession httpSession,
			@RequestParam("filedate") String filedate,@RequestParam("subCat") String subCat ,@RequestParam("dollar_val") String dollar_val){
		
		 
		 try{
			 logger.info("***** ReconProcess.runProcess Start ****");
		 
			 String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();

			 
			/* java.util.Date varDate=null;
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
				try {
				     varDate=dateFormat.parse(filedate);
				    dateFormat=new SimpleDateFormat("dd/MMM/yyyy");
				    logger.info("Date :"+dateFormat.format(varDate));
				}catch (Exception e) {
					demo.logSQLException(e, "GenerateRupayTTUMDaoImpl.generateDisputeTTUM");
					logger.error(" error in GenerateRupayTTUMDaoImpl.generateDisputeTTUM", new Exception("GenerateRupayTTUMDaoImpl.generateDisputeTTUM",e));
					 throw e;
				}
*/
			 
			 List<CompareSetupBean> compareSetupBeans = reconProcess.getFileList(category, filedate,subCat);
			 CompareBean compareBean = new CompareBean();
			 compareBean.setStEntryBy(Createdby);
			 if(compareSetupBeans !=null){
				  
				 String msg = reconProcess.chkFileUpload(category, filedate,compareSetupBeans,subCat);
				 logger.info("msg for chkFileUpload is "+msg);


				
				 
				 //logger.info(msg);
//                 msg = null;
				 if(msg == null){

			 //if(reconProcess.validateFile(category, compareSetupBeans, filedate)==null)  {
					 
					 if(!category.equalsIgnoreCase("CARDTOCARD"))  {
						 logger.info("Inside recon block");
						 if((msg = reconProcess.validateFile(category, compareSetupBeans, filedate))==null)
						 {

							 if(reconProcess.processFile(category,compareSetupBeans,filedate, Createdby,subCat)) 
							 {

								 //temporary commented for testing
								 if(reconProcess.compareFiles(category, filedate, compareBean,subCat,dollar_val))
								 {
									 // logger.info("DOne1111");
									 logger.info("RECON COMPLETED AT "+new java.sql.Timestamp(new java.util.Date().getTime()));

									 msg = "All process are completed. Please go-to settlement to see the result."; 
								 }else {

									 msg="Recon Process Not Completed";
									 logger.info(msg);
								 }	

							 }
							 else {

								 msg="classification not completed";
								 logger.info(msg);
							 }

						 }
						 else
						 {
							 return msg;
						 }
					 }
					 else {

						 //for rupay international
						 if(category.equalsIgnoreCase("RUPAY") && subCat.equalsIgnoreCase("INTERNATIONAL"))
						 {
							 logger.info("Inside international loop");
							 //1. check if recon is already processed
							 HashMap<String, Object> output = reconProcess.checkRupayIntRecon(filedate);
							 
							 if(output != null && !(Boolean) output.get("result")){
								//2. process recon
								 output = reconProcess.processRupayIntRecon(filedate, Createdby);
								 
								 if(output != null && (Boolean) output.get("result")) {
									 msg = "Recon Processing completed";
								 }
								 else
								 {
									 msg = "Recon is not processed";
								 }
							 }
							 
							 
						 }
						 else if(category.equalsIgnoreCase("CARDTOCARD"))
						 {
							 logger.info("Inside CardtoCardRecon loop");
							 // CHECK WHETHER prev day recon is done
							 HashMap<String, Object>  output =  reconProcess.checkCardtoCardPrevRecon(filedate);
							 
							 //HashMap<String, Object> output = reconProcess.checkCardtoCardRawFiles(filedate);
							 
							if(output != null && (Boolean) output.get("result"))
							 {
								 //1. check if recon is already processed
								 output = reconProcess.checkCardtoCardRecon(filedate);

								 if(output != null && !(Boolean) output.get("result")){
									 //2. process recon
									 boolean result = reconProcess.CardtoCardCompareData(category, filedate, compareBean.getStEntryBy());

									 if(result)
									 {
										 return "Recon is processed";
									 }
									 else
									 {
										 return "Issue while processing recon";
									 }
								 }
								 else
								 {
									 return output.get("msg").toString();
								 }
							 }
							 else
							 {
								 return output.get("msg").toString();
							 }
						 }
						 else
						 {
						 msg = "Previous File Not processed.";
						 logger.info(msg);
						 }
					 }

				 }else {

					 return msg;
				 }

				 logger.info("***** ReconProcess.runProcess End ****");
				 
				 return msg;

			 }else {

				 logger.info("Files are Not Configure For Selected Category");
				 return "Files are Not Configure For Selected Category";
				 
			 }


		 }catch(Exception e){
			 
			 try {
				demo.logSQLException(e, "ReconProcess.runProcess");
			} catch (SQLException e1) {
				 logger.error(" error in ReconProcess.runProcess", new Exception("ReconProcess.runProcess",e));
			}
			 logger.error(" error in ReconProcess.runProcess", new Exception("ReconProcess.runProcess",e));
			// e.printStackTrace();
			 dataJson.setParams("ERROR", e.getMessage());
			 return "Exception";
			 
		 }
	
	 }
	 
	
	 
	 
	/* private void logSQLException(Exception e, String methodName) throws SQLException {
		 Connection conn = null;
		 PreparedStatement ps=null;
		 try {
	            String query = "insert into dcrs_error_log values (?,?,sysdate)";
	            //System.out.println("logSQLException query==" + query);
	           
	            ps = ((Statement) conn).getConnection().prepareStatement(query);
	            String errorCode = "";
	            if (e.getClass().getSimpleName().equalsIgnoreCase("SQLException")) {
	                errorCode += ((SQLException) e).getErrorCode();
	            }

	            ps.setString(1, "DCRS " + " --> " + e.getClass().getSimpleName() +  " --> " + e.getMessage());
	            ps.setString(2, methodName);
	            ps.executeUpdate();
	        } catch (Exception ex) {
	        	logger.error(" error in ReconProcess.logSQLException", new Exception("ReconProcess.logSQLException",ex));
	        } finally {
	        	if(ps!=null){
	        		ps.close();
	        	}
	        	if(conn!=null){
	        		conn.close();
	        	}
	        }
		
	}*/
	 
	 //CheckStatus
	 
	@RequestMapping(value = "CheckStatus", method = RequestMethod.POST)
	 @ResponseBody
	 public  FileDetailsJson CheckStatus (@RequestParam("category")String category,FileDetailsJson dataJson, ModelAndView modelAndView,CompareSetupBean setupBean,HttpSession httpSession,
			@RequestParam("filedate") String filedate,@RequestParam("subcat")String subcat ){
		 logger.info("***** ReconProcess.CheckStatus Start ****");
		 
		 try{
			 
			 CompareSetupBean bean = null;
		//	 setupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
			 String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
			 
				List<CompareSetupBean> compareSetupBeans = reconProcess.getFileList(category, filedate,subcat);
				if(compareSetupBeans !=null){
				
					 bean = reconProcess.chkStatus(compareSetupBeans,category,filedate);
					 
					 
					 JSONObject objJSON = new JSONObject();
						
						 
						 logger.info("in GetHeaderList"+category);
						 HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
				         Gson gson = new GsonBuilder().setPrettyPrinting().create();
						 
						
						 JSONROOT.put("Records", bean);
						 String jsonArray = gson.toJson(JSONROOT);
						 dataJson.setParams("OK", bean,0);
						 
						 objJSON.put("setupBeans", setupBean);
				}
				
				 logger.info("***** ReconProcess.CheckStatus End ****");
				
		 
		 }catch(Exception e){
			 try {
				demo.logSQLException(e, "ReconProcess.CheckStatus");
			} catch (SQLException e1) {
				 logger.error(" error in ReconProcess.CheckStatus", new Exception("ReconProcess.CheckStatus",e));
			}
			 logger.error(" error in ReconProcess.CheckStatus", new Exception("ReconProcess.CheckStatus",e));
			 dataJson.setParams("ERROR", e.getMessage());
		
			 
		 }
		 return dataJson;
	
	 }
	 
}
