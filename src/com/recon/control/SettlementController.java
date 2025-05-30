package com.recon.control;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.poi.hssf.record.formula.TblPtg;
import org.apache.poi.hssf.util.HSSFColor.TAN;
import org.apache.poi.util.IOUtils;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.recon.model.CompareSetupBean;
import com.recon.model.Gl_bean;
import com.recon.model.LoginBean;
import com.recon.model.Mastercard_chargeback;
import com.recon.model.NetworkFileUpdateBean;
import com.recon.model.ReconDataJson;
import com.recon.model.Rupay_Gl_repo;
import com.recon.model.Rupay_gl_Lpcases;
import com.recon.model.Rupay_gl_autorev;
import com.recon.model.Rupay_sur_GlBean;
import com.recon.model.SettlementBean;
import com.recon.model.SettlementTypeBean;
import com.recon.model.SettlementTypeJson;
import com.recon.service.ISettelmentService;
import com.recon.service.ISourceService;
import com.recon.util.CSRFToken;
import com.recon.util.FileDetailsJson;

@Controller
public class SettlementController {

	private static final String ERROR_MSG = "error_msg";
	private static final String SUCCESS_MSG = "success_msg";
	private static final Logger logger = Logger.getLogger(SettlementController.class);

	public static final String CATALINA_HOME = "catalina.home";
	public static final String TTUM_FOLDER = System.getProperty(CATALINA_HOME);

	@Autowired
	ISettelmentService isettelmentservice;
	@Autowired
	ISourceService iSourceService;

	@RequestMapping(value = "Settlement", method = RequestMethod.GET)
	public ModelAndView Configuration(ModelAndView modelAndView) {
		modelAndView.setViewName("SettlementMenu");
		return modelAndView;

	}

	@RequestMapping(value = "ReconData", method = RequestMethod.GET)
	public String settlement(ModelAndView modelAndView, Model model, SettlementTypeBean settlementTypeBean,
			LoginBean loginBean, HttpServletRequest request, HttpServletResponse response) {

		try {

			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			modelAndView.setViewName("ReconSettlement");

			model.addAttribute("SettlementBean", settlementTypeBean);

			return "ReconSettlement";
		} catch (Exception ex) {

			ex.printStackTrace();
			return "redirect:Login.do";
		}

	}

	@RequestMapping(value = "UNReconData", method = RequestMethod.GET)
	public String UNReconData(ModelAndView modelAndView, Model model, SettlementTypeBean settlementTypeBean,
			LoginBean loginBean, HttpServletRequest request, HttpServletResponse response) {

		try {

			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());

			modelAndView.setViewName("ReconSettlement");

			model.addAttribute("SettlementBean", settlementTypeBean);

			return "UNReconSettlement";
		} catch (Exception ex) {

			ex.printStackTrace();
			return "redirect:Login.do";
		}

	}

	// ProcessedData.do

	@RequestMapping(value = "ProcessedData", method = RequestMethod.GET)
	public String KnockOffData(ModelAndView modelAndView, Model model, SettlementTypeBean settlementTypeBean,
			LoginBean loginBean, HttpServletRequest request, HttpServletResponse response) {

		try {

			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());

			modelAndView.setViewName("ProcessedData");

			model.addAttribute("SettlementBean", settlementTypeBean);

			return "ProcessedData";
		} catch (Exception ex) {

			ex.printStackTrace();
			return "redirect:Login.do";
		}

	}

	@RequestMapping(value = "/GetReconData", method = RequestMethod.GET)
	public String GetReconData(@RequestParam(value = "tbl") String table, @RequestParam("date") String date,
			@RequestParam("type") String type, @RequestParam("searchValue") String searchValue,
			HttpServletRequest request, LoginBean loginBean, RedirectAttributes redirectAttributes, Model model)
			throws Exception {

		try {
			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			String column = "";
			/*
			 * System.out.println(table); System.out.println(column);
			 * System.out.println(date);
			 */

			// System.out.println("in GetSettelmentType"+tableName);

			ArrayList<SettlementTypeBean> dataList = isettelmentservice.getReconData(table.trim(), type.trim(),
					date.trim(), searchValue.trim());

			System.out.println(column.split(","));
			model.addAttribute("table", table.trim());
			model.addAttribute("dataList", dataList);
			return "viewReconData";
			// return "ViewPWDRepo";

		} catch (Exception e) {

			logger.error(e.getMessage());
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return "redirect:Login.do";

		}
	}

	@RequestMapping(value = "/GetJtableReconData", method = RequestMethod.GET)
	public String GetJtableReconData(@RequestParam(value = "tbl") String table, @RequestParam("date") String date,
			@RequestParam("type") String type, @RequestParam("searchValue") String searchValue,
			HttpServletRequest request, LoginBean loginBean, RedirectAttributes redirectAttributes, Model model,
			HttpSession session) throws Exception {

		String split_table[] = table.split("_");

		String concat_table = split_table[0] + "_" + split_table[2];

		try {
			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());

			session.setAttribute("tbl", table);
			session.setAttribute("date", date);
			session.setAttribute("type", type);
			session.setAttribute("searchValue", searchValue);

			ArrayList<SettlementTypeBean> dataList = isettelmentservice.getReconData(table.trim(), type.trim(),
					date.trim(), searchValue.trim());

			model.addAttribute("table", table.trim());
			model.addAttribute("dataList", dataList);

			if (concat_table.trim().equalsIgnoreCase("SETTLEMENT_SWITCH")) {

				return "viewSwitchReconData";

			}
			if (concat_table.trim().equalsIgnoreCase("SETTLEMENT_CBS")) {

				return "viewCBSReconData";

			} else {

				return "Login.do";
			}

			// return "ViewPWDRepo";

		} catch (Exception e) {

			logger.error(e.getMessage());
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return "redirect:Login.do";

		}
	}

	@RequestMapping(value = "/GetJtableData", method = RequestMethod.POST)
	@ResponseBody
	public ReconDataJson GetJtableData(@ModelAttribute("settlementTypeBean") SettlementTypeBean settlementTypeBean,
			HttpServletRequest request, ReconDataJson dataJson, LoginBean loginBean, int jtStartIndex, int jtPageSize,
			RedirectAttributes redirectAttributes, Model model, HttpSession session) throws Exception {
		try {

			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String table, searchValue, type, date;
			table = (String) session.getAttribute("tbl");
			date = (String) session.getAttribute("date");
			type = (String) session.getAttribute("type");
			searchValue = (String) session.getAttribute("searchValue");

			ArrayList<SettlementTypeBean> dataList = isettelmentservice.getChngReconData(table.trim(), type.trim(),
					date.trim(), searchValue.trim(), jtStartIndex, jtPageSize);
			JSONROOT.put("Records", dataList);

			String jsonArray = gson.toJson(JSONROOT);

			int totalRecordcount = isettelmentservice.getReconDataCount(table.trim(), type.trim(), date.trim(),
					searchValue.trim());

			// System.out.println(totalRecordcount);
			dataJson.setParams("OK", dataList, totalRecordcount);
			dataJson.setTotalRecordCount(totalRecordcount);

			// return "ViewPWDRepo";

		} catch (Exception e) {

			logger.error(e.getMessage());
			dataJson.setParams("ERROR", e.getMessage());

		}
		return dataJson;
	}

	// editsave

	@RequestMapping(value = "/editsave", method = RequestMethod.POST)
	public ResponseEntity<Integer> editsave(@ModelAttribute("settlementTypeBean") SettlementTypeBean settlementTypeBean,
			HttpServletRequest request, LoginBean loginBean, RedirectAttributes redirectAttributes, Model model)
			throws Exception {

		int result = 200;
		try {

			/*
			 * System.out.println("pan:"+settlementTypeBean.getPan());
			 * System.out.println("pan:"+settlementTypeBean.getrEMARKS());
			 * System.out.println(settlementTypeBean.gettERMID());
			 * System.out.println(settlementTypeBean.gettRACE());
			 * System.out.println(settlementTypeBean.getSetltbl());
			 */

			result = isettelmentservice.updateRecord(settlementTypeBean);
			// System.out.println(request.getParameter("data"));
			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			result = isettelmentservice.updateRecord(settlementTypeBean);

			return new ResponseEntity<>(result, HttpStatus.OK);
			// return "ViewPWDRepo";

		} catch (Exception e) {

			logger.error(e.getMessage());
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);

		}
	}

	@RequestMapping(value = "GenerateReport", method = RequestMethod.POST)
	public String compareData(@ModelAttribute("SettlementBean") SettlementTypeBean typeBean,
			RedirectAttributes redirectAttributes, HttpSession httpsession, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		/*
		 * table=(String) session.getAttribute("tbl"); date=(String)
		 * session.getAttribute("date"); type= (String) session.getAttribute("type");
		 */

		try {

			// List<SettlementTypeBean> generatettumObj =
			// isettelmentservice.getReconData(typeBean.getSetltbl().trim(),typeBean.getDataType().trim(),typeBean.getDatepicker().trim(),typeBean.getSearchValue());
			/*
			 * model.addAttribute("processed_data",generatettumObj);
			 * model.addAttribute("table",typeBean.getSetltbl().trim());
			 * model.addAttribute("type", typeBean.getDataType().trim());
			 */

			List<List<String>> DATA = new ArrayList<>();

			DATA = isettelmentservice.getReconData1(typeBean.getSetltbl().trim(), typeBean.getDataType().trim(),
					typeBean.getDatepicker().trim(), typeBean.getSearchValue());

			String stFileName = isettelmentservice.getFileName(typeBean.getSetltbl());

			/* model.addAttribute("filename", typeBean.getDataType().trim()); */

			// List<String> stfilename = new ArrayList<>();
			String filename = stFileName + "-" + typeBean.getDataType().trim() + "_" + typeBean.getDatepicker().trim();
			// stfilename.add(filename);

			Map<String, Object> map = new HashMap<String, Object>();

			model.addAttribute("filename", filename);

			model.addAttribute("DATA", DATA);

			// isettelmentservice.buildExcelDocument1(map,filename,request,response);
			return "generateExcelReport";

		} catch (Exception e) {

			return "redirect:Login.do";
		}
	}

	@RequestMapping(value = "/GetReconDataCount", method = RequestMethod.POST)
	@ResponseBody
	public int GetReconDataCount(@RequestParam(value = "tbl") String table, @RequestParam("date") String date,
			@RequestParam("type") String type, @RequestParam("searchValue") String searchValue,
			HttpServletRequest request, LoginBean loginBean, RedirectAttributes redirectAttributes, Model model)
			throws Exception {

		int count = 0;

		try {
			// loginBean.setTable_name(table);
			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			String column = "";
			/*
			 * System.out.println(table); System.out.println(column);
			 * System.out.println(date);
			 */

			count = isettelmentservice.getReconDataCount(table.trim(), type.trim(), date.trim(), searchValue.trim());

			return count;
			// return "ViewPWDRepo";

		} catch (Exception e) {

			logger.error(e.getMessage());
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return count;

		}
	}

	@RequestMapping(value = "/GetColumnList", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<String> GetColumnList(@RequestParam("tableName") String tableName) {

		// System.out.println("in GetSettelmentType"+tableName);

		ArrayList<String> colList = isettelmentservice.getColumnList(tableName);

		return colList;

	}

	@RequestMapping(value = "/GetSettelmentType", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<String> GetSettelmentType(@RequestParam("tableName") String tableName) {

		// System.out.println("in GetSettelmentType"+tableName);

		ArrayList<String> typeList = isettelmentservice.gettype(tableName);

		return typeList;

	}

	@RequestMapping(value = "/GetSettelmentTypedtls", produces = { "application/json" }, method = RequestMethod.POST)
	public @ResponseBody SettlementTypeJson GetSettelmentTypesetails(SettlementTypeJson settlementTypeJson) {
		/*
		 * @RequestParam("action")String action,@RequestParam("tablename")String
		 * tablename ,
		 */
		// @RequestParam("action")String action,@RequestParam("tablename")String
		// tablename ,
		String action = "CIA GL", tablename = "settlement_cbs";
		/*
		 * System.out.println("in GetSettelmentType"+action);
		 * System.out.println("in GetSettelmentType"+tablename);
		 */

		try {
			HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			List<SettlementTypeBean> list = isettelmentservice.getSettlmentType(action.trim(), tablename.trim());

			JSONROOT.put("Records", list);

			// Convert Java Object to Json
			String jsonArray = gson.toJson(JSONROOT);
			/* System.out.println(jsonArray); */
			settlementTypeJson.setParams("OK", list);
			System.out.println(settlementTypeJson);
		} catch (Exception e) {
			settlementTypeJson.setParams("ERROR", e.getMessage());
		}
		return settlementTypeJson;
	}

	// ADDED BY INT5779 AS ON 14TH MARCH 2018 FOR DOWNLOADING REPORTS
	@RequestMapping(value = "DownloadReports", method = RequestMethod.GET)
	public ModelAndView getdownloadPage(ModelAndView modelAndView, HttpServletRequest request,
			SettlementBean settlementBean, @RequestParam("category") String category) throws Exception {
		List<String> subcat = new ArrayList<>();

		System.out.println("in GetHeaderList" + category);

		subcat = iSourceService.getSubcategories(category);

		// boolean val=iSourceService.generateCTF(settlementBean);

		modelAndView.addObject("category", category);
		modelAndView.addObject("subcategory", subcat);
		String csrf = CSRFToken.getTokenForSession(request.getSession());

		// redirectAttributes.addFlashAttribute("CSRFToken", csrf);
		modelAndView.addObject("CSRFToken", csrf);

		modelAndView.addObject("SettlementBean", settlementBean);
		modelAndView.setViewName("DownloadReports");

		return modelAndView;

	}

	@RequestMapping(value = "NetworkFileUpdt", method = RequestMethod.GET)
	public ModelAndView NetworkFileUpdate(ModelAndView modelAndView, NetworkFileUpdateBean settlementBean,
			@RequestParam("category") String category) throws Exception {
		List<String> subcat = new ArrayList<>();

		System.out.println("in GetHeaderList" + category);

		subcat = iSourceService.getSubcategories(category);

		// boolean val=iSourceService.generateCTF(settlementBean);

		modelAndView.addObject("category", category);
		modelAndView.addObject("subcategory", subcat);

		modelAndView.addObject("NetworkFileUpdateBean", settlementBean);
		modelAndView.setViewName("NetworkFileUpdate");

		return modelAndView;

	}

	/*
	 * @RequestMapping(value = "DownloadReports", method = RequestMethod.POST)
	 * 
	 * @ResponseBody // public void
	 * downloadReports(@ModelAttribute("SettlementBean")SettlementBean
	 * settlementBean) public String downloadReports
	 * (@RequestParam("category")String category,FileDetailsJson dataJson,
	 * ModelAndView modelAndView,CompareSetupBean setupBean,HttpSession httpSession,
	 * 
	 * @RequestParam("filedate") String filedate,@RequestParam("subCat") String
	 * subCat,@RequestParam("path") String stPath ) {
	 * 
	 * 
	 * SettlementBean settlementBean = new SettlementBean();
	 * settlementBean.setCategory(category);
	 * settlementBean.setStsubCategory(subCat);
	 * settlementBean.setDatepicker(filedate);
	 * 
	 * String userHome1 = System.getProperty("user.home"); userHome1 =
	 * userHome1+"\\Desktop\\Reports"; String userHome=userHome1.replace("/", "\\");
	 * 
	 * settlementBean.setStPath(userHome);
	 * 
	 * // settlementBean.setStPath(stPath); try {
	 * 
	 * 
	 * //1.CHECK WHETHER RECON HAS BEEN PROCESSED FOR THE SELECTED DATE //
	 * System.out.println("HELLO"); if(!subCat.equals("-")) {
	 * settlementBean.setStMergerCategory
	 * (settlementBean.getCategory()+"_"+settlementBean
	 * .getStsubCategory().substring(0, 3)); } else
	 * settlementBean.setStMergerCategory(settlementBean.getCategory());
	 * 
	 * //check whether recon has been performed for that day boolean check_process =
	 * isettelmentservice.checkfileprocessed(settlementBean);
	 * 
	 * if(check_process) { isettelmentservice.generate_Reports(settlementBean);
	 * return "Reports downloaded."; } else { return
	 * "Recon not processed for selected date"; }
	 * 
	 * } catch(Exception e) { System.out.println("Exception in downloadReports "+e);
	 * return e.getMessage(); }
	 * 
	 * 
	 * }
	 */

	// CHECK WHETHER RECON HAS BEEN PROCESSED FOR SELECTED DATE
	@RequestMapping(value = "checkfileprocessed", method = RequestMethod.POST)
	@ResponseBody
	public String checkfileProcess(@RequestParam("category") String category, FileDetailsJson dataJson,
			ModelAndView modelAndView, CompareSetupBean setupBean, HttpSession httpSession,
			@RequestParam("filedate") String filedate, @RequestParam("subCat") String subCat,
			@RequestParam("path") String stPath, HttpServletResponse response, HttpServletRequest request) {
		try {
			SettlementBean settlementBean = new SettlementBean();
			settlementBean.setCategory(category);
			settlementBean.setStsubCategory(subCat);
			settlementBean.setDatepicker(filedate);
			boolean check_process = isettelmentservice.checkfileprocessed(settlementBean);
			//boolean check_process = true ;
			if (check_process) {
				return "success";
			} else
				return "Recon not processed for selected date";
		} catch (Exception e) {
			return "Exception";
		}
	}

	@RequestMapping(value = "DownloadReports", method = RequestMethod.POST)
	// public void
	// downloadReports(@ModelAttribute("SettlementBean")SettlementBean
	// settlementBean)
	@ResponseBody
	/*
	 * public void downloadReports (@RequestParam("category")String
	 * category,FileDetailsJson dataJson, ModelAndView modelAndView,CompareSetupBean
	 * setupBean,HttpSession httpSession,
	 * 
	 * @RequestParam("filedate") String filedate,@RequestParam("subCat") String
	 * subCat,@RequestParam("path") String stPath ,HttpServletResponse
	 * response,HttpServletRequest request)
	 */
	public void downloadReports(@ModelAttribute("SettlementBean") SettlementBean SettlementBean,
			HttpServletResponse response, HttpServletRequest request, RedirectAttributes redirectAttributes) {

		String TEMP_DIR = System.getProperty("java.io.tmpdir");
		ServletContext context = request.getServletContext();
		// SettlementBean settlementBean = new SettlementBean();
		/*
		 * settlementBean.setCategory(category);
		 * settlementBean.setStsubCategory(subCat);
		 * settlementBean.setDatepicker(filedate);
		 */

		/*
		 * String userHome1 = System.getProperty("user.home"); userHome1 =
		 * userHome1+"\\Desktop\\Reports"; String userHome=userHome1.replace("/", "\\");
		 * 
		 * SettlementBean.setStPath(userHome);
		 */

		// settlementBean.setStPath(stPath);
		try {
			logger.info("INSIDE DOWNLOAD REPORTS");
			// DELETING FILES FROM DRIVE
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
			// SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			java.util.Date date = sdf.parse(SettlementBean.getDatepicker());

			sdf = new SimpleDateFormat("dd-MM-yyyy");

			String stnewDate = sdf.format(date);
			// isettelmentservice.DeleteFiles(userHome+"\\"+stnewDate);
			// String stpath = SettlementBean.getStPath() +
			// "\\"+SettlementBean.getCategory();
			// INT 8624
			String stpath = TEMP_DIR + File.separator + SettlementBean.getCategory();
			// String stpath = TTUM_FOLDER+File.separator+SettlementBean.getCategory();
			System.out.println("stpath is " + stpath);
			isettelmentservice.DeleteFiles(stpath);
			SettlementBean.setStPath(stpath);
			// String stpath = SettlementBean.getStPath() + "\\" +
			// stnewDate+"\\"+SettlementBean.getCategory();

			// 1.CHECK WHETHER RECON HAS BEEN PROCESSED FOR THE SELECTED DATE
			// System.out.println("HELLO");
			if (!SettlementBean.getStsubCategory().equals("-")) {
				SettlementBean.setStMergerCategory(
						SettlementBean.getCategory() + "_" + SettlementBean.getStsubCategory().substring(0, 3));
			} else
				SettlementBean.setStMergerCategory(SettlementBean.getCategory());

			if (SettlementBean.getCategory().equals("CARDTOCARD")) {
				String path = SettlementBean.getStPath();
				File myFile = new File(SettlementBean.getStPath() + File.separator);
				System.out.println("myFile" + myFile);
				// boolean val=iSourceService.generateCTF(SettlementBean);
			}
			isettelmentservice.generate_Reports(SettlementBean);

			// added by int5779 for downloading zip

			String stFilename = SettlementBean.getStMergerCategory();
			// File file = new
			// File(userHome+"\\"+stnewDate+"\\"+stFilename+".zip");
			File file = new File(SettlementBean.getStPath() + File.separator + "REPORTS.zip");
			logger.info("path of zip file " + SettlementBean.getStPath() + File.separator + "REPORTS.zip");
			FileInputStream inputstream = new FileInputStream(file);
			response.setContentLength((int) file.length());
			// response.setContentType(context.getMimeType(stpath +"\\"+ "Reports.zip"));
			// response.setContentType("application/zip");
			logger.info("before downloading zip file ");
			response.setContentType("application/zip");
			logger.info("download completed");

			/** Set Response header */
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
			response.setHeader(headerKey, headerValue);

			/** Write response. */
			OutputStream outStream = response.getOutputStream();
			IOUtils.copy(inputstream, outStream);
			// isettelmentservice.DeleteFiles(stpath);
			response.flushBuffer();

			// DELETING FILES FROM DRIVEx	
			// isettelmentservice.DeleteFiles(userHome+"\\"+stnewDate);
			// isettelmentservice.DeleteFiles(SettlementBean.getStPath()+"\\"+stnewDate);
			java.util.Date varDate = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
			try {
				varDate = dateFormat.parse(SettlementBean.getDatepicker());
				dateFormat = new SimpleDateFormat("MM/dd/yyyy");
				System.out.println("Date :" + dateFormat.format(varDate));
				SettlementBean.setDatepicker(dateFormat.format(varDate));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			// isettelmentservice.DeleteFiles(stpath);

		} catch (Exception e) {
			logger.info("Exception in downloadReports " + e);
			System.out.println("Exception in downloadReports " + e);
			// return e.getMessage();
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
		}

	}

	@RequestMapping(value = "/GetChargeback", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String GetChargeback(@RequestParam("ArnNo") String arnNo, HttpServletRequest request) {
		// your logic here
		String jsonArray = "";
		try {
			LoginBean loginBean = null, loginBean1 = (LoginBean) request.getSession().getAttribute("loginBean");
			// int userid = loginBean.getEmpCode();

			HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			// List<FinacleBean> userList = formService.getInterest(Emplyno);

			List<Mastercard_chargeback> list = isettelmentservice.getMastercardchargeback(arnNo);

			JSONROOT.put("Result", "OK");
			JSONROOT.put("Records", list);

			// Convert Java Object to Json
			jsonArray = gson.toJson(JSONROOT);
			System.out.println(jsonArray);
			return jsonArray;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	@RequestMapping(value = "/Savemastercardchargebk", method = RequestMethod.POST)
	public @ResponseBody String Savemastercard_chargebk(@RequestParam("Microfilmid") String microfilm,
			@RequestParam("Refid") String refid, @RequestParam("Settlmentamount") String settlmentamount,
			@RequestParam("SettlmentCurrid") String settlmentCurrid, @RequestParam("Txnamountid") String txnamountid,
			@RequestParam("Txtcurrencyid") String txtcurrencyid, @RequestParam("Reason") String reason,
			@RequestParam("Dcosid") String dcosid, @RequestParam("Remrk") String remrk, HttpServletRequest request)
			throws IOException {
		int message = 0;
		/*
		 * String settlmentamount = null; String settlmentCurrid = null; String
		 * txnamountid = null; String txtcurrencyid = null; String reason = null; String
		 * dcosid = null; //String refid = null; String remrk=null;
		 */
		// String microfilm=null;

		try {
			message = isettelmentservice.Savechargeback(microfilm, refid, settlmentamount, settlmentCurrid, txnamountid,
					txtcurrencyid, reason, dcosid, remrk);

		} catch (Exception e) {
			e.printStackTrace();
			return "Fail";
		}
		// model.addAttribute("FileUploadBean", new FileUploadBean());
		// model.addAttribute("message", message);
		return "Success";

	}

	// ENDS HERE

	@RequestMapping(value = "GenerateMastercardChargebk", method = RequestMethod.POST)
	public void GenerateMastercardChargebk(HttpServletRequest request, HttpServletResponse response, Model model)

	{
		// List<List<Mastercard_chargeback>> report1 =
		// isettelmentservice.GenerateReportChargebk();

//	model.addAttribute("report1",report1);

		// return "GenerateMastercardChargebk";*/
		String arn1 = null;
		String reason1 = null;
		String arndate = null;
		reason1 = request.getParameter("Reasonval");
		arn1 = request.getParameter("Arn");
		arndate = request.getParameter("Arndate");
		// String filepath="D:\\Master_Card\\ChargeBack";
		String filepath = System.getProperty("catalina.home");
		ServletOutputStream sou = null;

		try {
			String getFile = isettelmentservice.generateChargBk(arn1, reason1, arndate);
			System.out.println("File Received");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename= " + getFile);
			FileInputStream ins = new FileInputStream(new File(getFile));
			sou = response.getOutputStream();
			sou.write(IOUtils.toByteArray(ins));
			ins.close();
			sou.flush();
			sou.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				new File(filepath).delete();
			} catch (Exception e2) {
			}
		}

	}
	// ENDS HERE

	@RequestMapping(value = "/Get_glbalance", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String Get_glbalance(@RequestParam("Date_val") String filedate,
			@RequestParam("Date_val2") String filedate2, HttpServletRequest request, HttpServletResponse response) {
		// your logic here
		String jsonArray = "";
		try {
			LoginBean loginBean = null, loginBean1 = (LoginBean) request.getSession().getAttribute("loginBean");
			List<Gl_bean> list = null;
			// int userid = loginBean.getEmpCode();

			// String filedate = request.getParameter("filedate");
			/*
			 * String settlementAmount = request.getParameter("settlementAmount"); String
			 * settlementDate = request.getParameter("settlementDate");
			 */
			HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			// List<FinacleBean> userList = formService.getInterest(Emplyno);

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
			Date date1 = sdf.parse(filedate);
			Date date2 = sdf.parse(filedate2);

			System.out.println("date1 : " + sdf.format(date1));
			System.out.println("date2 : " + sdf.format(date2));

			Calendar start = Calendar.getInstance();
			start.setTime(date1);

			Calendar end = Calendar.getInstance();
			end.setTime(date2);
			int countval = 0;
			while (!start.after(end)) {
				Date targetDay = start.getTime();
				// Do Work Here

				System.out.println("targetDay-->" + targetDay);
				String newDate = sdf.format(start.getTime());
				System.out.println("o/p-->" + newDate);
				start.add(Calendar.DATE, 1);
				// countval++;

				list = isettelmentservice.getMastercardGet_glbalance(newDate);
			}
			List<Rupay_sur_GlBean> list1 = isettelmentservice.getRupaysurchargelist(filedate);
			List<Rupay_gl_autorev> list2 = isettelmentservice.getRupayAutorevlist(filedate);
			List<Rupay_gl_Lpcases> list3 = isettelmentservice.getRupayLpcaselist(filedate);

			JSONROOT.put("Result", "OK");
			JSONROOT.put("Records", list);
			JSONROOT.put("Records1", list1);
			JSONROOT.put("Records2", list2);
			JSONROOT.put("Records3", list3);

			// Convert Java Object to Json
			jsonArray = gson.toJson(JSONROOT);
			System.out.println(jsonArray);
			return jsonArray;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	@RequestMapping(value = "/Get_Settlemntdtamnt", method = RequestMethod.POST)
	public ResponseEntity Get_Settlemntdtamnt(HttpServletRequest request, HttpServletResponse response) {
		// your logic here
		String jsonArray = "";
		String message = null;
		try {
			LoginBean loginBean = null, loginBean1 = (LoginBean) request.getSession().getAttribute("loginBean");
			// int userid = loginBean.getEmpCode();

			// String filedate = request.getParameter("filedate");
			String settlementAmount = request.getParameter("settlementAmount");
			String settlementDate = request.getParameter("settlementDate");
			HashMap<String, Object> JSONROOT = new HashMap<String, Object>();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			// List<FinacleBean> userList = formService.getInterest(Emplyno);

			message = isettelmentservice.getSettlemntAmount(settlementDate, settlementAmount);

			return new ResponseEntity(message, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/Save", method = RequestMethod.POST)
	public @ResponseBody String updateAuditReport(Model model, HttpServletRequest request, HttpSession session) {

		try {

			String surcharge[] = request.getParameterValues("surchargeamount[]");
			String autorev[] = request.getParameterValues("autorevamount[]");
			String lpcase[] = request.getParameterValues("lpcases[]");

			String settlementmatch = request.getParameter("settlementmatch");
			String nxtdate = request.getParameter("nxtdate");
			String surcharge1 = request.getParameter("surcharge");
			String lpcase2 = request.getParameter("lpcase");
			String cbs_unrecon = request.getParameter("cbs_unrecon");
			String switch_unrecon = request.getParameter("switchunrecon");
			String surch_total = request.getParameter("surchtotal");
			String autorev_total = request.getParameter("autorevtotal");
			String lpcasetotal = request.getParameter("lpcasetotal");
			String nobase2 = request.getParameter("nobase2");
			String settlementTotal = request.getParameter("settlementTotal");
			String closingbal = request.getParameter("closingbal");
			String settlementid = request.getParameter("settlementid");
			String finaltotal = request.getParameter("finaltotal");
			String dateval = request.getParameter("dateval");

			String diff = request.getParameter("diff");
			System.out.println(Arrays.toString(surcharge));
			System.out.println(diff);

			int message = isettelmentservice.SaveGl(closingbal, settlementid, diff, cbs_unrecon, switch_unrecon,
					nobase2, settlementmatch, nxtdate, surcharge1, settlementTotal, lpcase2,
					Arrays.toString(surcharge).replaceAll("\\[", "").replaceAll("\\]", "").replace(",", ""),
					surch_total, Arrays.toString(autorev).replaceAll("\\[", "").replaceAll("\\]", "").replace(",", ""),
					autorev_total, Arrays.toString(lpcase).replaceAll("\\[", "").replaceAll("\\]", "").replace(",", ""),
					lpcasetotal, finaltotal, dateval);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error";
		}
		System.out.println("Insiee");

		return "Success";

	}

	@RequestMapping(value = "Download_Repo", method = RequestMethod.POST)
	public String Download_Repo(@RequestParam("dateval") String dateval, HttpServletRequest request,
			HttpServletResponse response, Model model) throws Exception

	{
		String[] startDate = null;
		String[] endDate = null;
		String startDatePicker = null;
		String endDatePicker = null;

		System.out.println("Inside method");

		List<List<Rupay_Gl_repo>> report1 = isettelmentservice.GenerateGL(dateval);
		model.addAttribute("report1", report1);

		return "GenerateRepoExcel";
	}

}
