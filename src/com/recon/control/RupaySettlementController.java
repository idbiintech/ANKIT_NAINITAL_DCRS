package com.recon.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recon.dao.impl.RupaySettelementDaoImpl;
import com.recon.model.LoginBean;
import com.recon.model.NFSSettlementBean;
import com.recon.model.RupaySettlementBean;
import com.recon.model.RupayUploadBean;
import com.recon.service.ISourceService;
import com.recon.service.NFSSettlementTTUMService;
import com.recon.service.RupaySettlementService;
import com.recon.util.CSRFToken;
import com.recon.util.GeneralUtil;
import com.recon.util.GenerateDLBVoucher;
import com.recon.util.GenerateUCOTTUM;

@Controller
public class RupaySettlementController {
	@Autowired
	RupaySettlementService rupaySettlementService;

	@Autowired
	GeneralUtil generalUtil;

	@Autowired
	RupaySettelementDaoImpl rupayDao;

	@Autowired
	ISourceService iSourceService;

	@Autowired
	NFSSettlementTTUMService NFSttumService;

	private static final Logger logger = Logger.getLogger(RupaySettlementController.class);
	private static final String ERROR_MSG = "error_msg";

	@RequestMapping(value = "rupSettlementFilUpload", method = RequestMethod.GET)
	public String rupsettlementFilUpload(HttpServletRequest request) throws Exception {
		logger.info("***** settlementFilUpload Start get method  ****");
		// ModelAndView modelAndView=new ModelAndView("RupaySettlementFileUpload");

		return "RupaySettlementFileUpload";
	}

	@RequestMapping(value = "postUploadRupSettlFile", method = RequestMethod.POST)
	@ResponseBody
	public String postUploadRupSettlFile(@ModelAttribute("rupaySettlementBean") RupaySettlementBean rupSettlementBean,
			ModelAndView modelAndView, HttpServletRequest request, @RequestParam("file") MultipartFile file,
			String stSubCategory, String datepicker, String cycle, HttpSession httpSession) throws Exception {
		logger.info("***** settlementFilUpload Start ****");
		HashMap<String, Object> output = null;
		try {
			String createdBy = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
			String fileName = file.getOriginalFilename();

			if (fileName.contains("@")) {
				String[] fileArr = fileName.split("@");
				String[] fileCyleArr = fileArr[1].split("\\.");
				int fileCycle = Integer.parseInt(fileCyleArr[0]);
				int selectedCycle = rupSettlementBean.getCycle();
				if (fileCycle != selectedCycle) {
					return "File uploaded and Cycle Selected are different";
				}
			} else {
				return "File format should be like ......FileName@cycleNumber.Extention";
			}

			rupSettlementBean.setCreatedBy(createdBy);
			rupSettlementBean.setCategory("RUPAY_SETTLEMENT");
			rupSettlementBean.setStSubCategory(stSubCategory);
			rupSettlementBean.setFileName("RUPAY_SETTLEMENT");
			HashMap<String, Object> result = rupaySettlementService.validatePrevFileUpload(rupSettlementBean);
			if (result != null && !(boolean) result.get("result")) {
				// rupaySettelementService.uploadExcelFile(file, 2);
				output = rupaySettlementService.uploadExcelFile(rupSettlementBean, file);

				// 3. INSERTING IN UPLOAD TABLE
				logger.info("***** RupayettlementController.RupayFileUpload POST End ****");
				if ((boolean) output.get("result")) {
					return "File Uploaded Successfully "; // \n Count is "+(Integer)output.get("count"
				} else {
					return "Error while Uploading file";
				}
			} else {
				return result.get("msg").toString();
			}

		} catch (Exception e) {
			logger.info("Exception in RupaySettlementController " + e);
			if (output != null && !(boolean) output.get("result")) {
				return "Error Occured  ";
			} else
				return "Error Occurred in reading";
		}

	}

	@RequestMapping(value = "rupSettlemenTtum", method = RequestMethod.GET)
	public String rupSettlemenTtum(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("***** rupSettlemenTtum Start get method  ****");
		// ModelAndView modelAndView=new ModelAndView("RupaySettlementFileUpload");
		logger.info("***** getRupSettlttum Start post method  ****");

		// return "RupayCooperativeTtum";
		return "RupaySettlementTtum";
	}

	@RequestMapping(value = "getRupSettlttum", method = RequestMethod.POST)
	public void getRupSettlttum(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("***** getRupSettlttum Start post method  ****");
		String settlementDate = request.getParameter("fileDate");
		/*
		 * String formatedDate="";
		 * 
		 * SimpleDateFormat s = new SimpleDateFormat("dd-MMM-yyyy"); Date
		 * dt=s.parse(settlementDate); SimpleDateFormat formatter = new
		 * SimpleDateFormat("dd-MM-yyyy");
		 * 
		 * formatedDate=formatter.format(dt);
		 * System.out.println("busDate   "+formatedDate);
		 */

		rupaySettlementService.generateRupaySettlmentTTum(settlementDate, response);

	}

	/***** uploading other files *************************/

	@RequestMapping(value = "RupayFileUpload", method = RequestMethod.GET)
	public ModelAndView nfsFileUploadGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** RupayFileUpload.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupayFileUpload GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("RupayFileUpload");

		logger.info("***** RupaySettlementController.RupayFileUpload GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "PresentmentFileUpload", method = RequestMethod.GET)
	public ModelAndView presentmentFileUploadGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** RupayFileUpload.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupayFileUpload GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("PresentmentFileUpload");

		logger.info("***** RupaySettlementController.PresentmentFileUpload GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "CashAtPos", method = RequestMethod.GET)
	public ModelAndView cashAtPOSProcessGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** CashAtPOS.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("CashAtPOS GET");
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("CashAtPos");

		logger.info("***** CashAtPOS GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "LateRevTtum", method = RequestMethod.GET)
	public ModelAndView LateRevTtumGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** LateRevTtum.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("LateRevTtum GET");
		modelAndView.addObject("category", category);
//		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("LateRevTtum");

		logger.info("***** CashAtPOS GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "PresentmentFileUpload", method = RequestMethod.POST)
	@ResponseBody
	public String PresentmentFileUploadPost(@ModelAttribute("rupaySettlementBean") RupayUploadBean rupayBean,
			HttpServletRequest request, @RequestParam("file") MultipartFile file, HttpSession httpSession, Model model,
			ModelAndView modelAndView, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("***** RupayFileUpload.post Start ****");
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		rupayBean.setCreatedBy(Createdby);
		logger.info("Subcategory is " + rupayBean.getSubcategory());
		logger.info(file.getOriginalFilename());

		String response = "";
		if (file.getOriginalFilename().contains("Presentment")) {
			response = rupaySettlementService.uploadPresentmentFile(rupayBean, file);
		}
		return response;
	}

	@RequestMapping(value = "downloadCashAtPosReport", method = RequestMethod.POST)
	public String CashAtPosDownload(@ModelAttribute("nfsSettlementBean") NFSSettlementBean nfsSettlementBean,
			@RequestParam("fileDate") String filedate, HttpServletRequest request, HttpServletResponse response,
			HttpSession httpSession, RedirectAttributes redirectAttributes, Model model) throws Exception {
		logger.info("***** DownloadSettlement.DownloadSettlement.Get Start ****");
		String display = "";
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		System.out.println("DATE IS DATE IS " + filedate);
		List<String> data = new ArrayList<String>();

		String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator;
		logger.info("TEMP_DIR" + TEMP_DIR);

		String stpath = TEMP_DIR;
		stpath = stpath + "CAST_TTUM";
		data = NFSttumService.getCashAtPos(filedate);
		String fileName = "CASH_AT_POS_TTUM.txt";
		GenerateDLBVoucher vouchObj = new GenerateDLBVoucher();
		vouchObj.generateTTUMFile(stpath, fileName, data);
		File file = new File(stpath + File.separator + fileName);
		logger.info("path of zip file " + stpath);
		FileInputStream inputstream = new FileInputStream(file);
		response.setContentLength((int) file.length());
		logger.info("before downloading zip file ");
		response.setContentType("application/txt");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
		response.setHeader(headerKey, headerValue);
		OutputStream outStream = response.getOutputStream();
		IOUtils.copy(inputstream, outStream);
		response.flushBuffer();
		logger.info("***** VisaSettlementController.DownloadVisaSettlementReport POST End ****");
		return "GenerateVisaSettlementReport";
	}

	@RequestMapping(value = "downloadLateRevReport", method = RequestMethod.POST)
	public String LateRevDownload(@ModelAttribute("nfsSettlementBean") NFSSettlementBean nfsSettlementBean,
			@RequestParam("fileDate") String filedate, HttpServletRequest request, HttpServletResponse response,
			HttpSession httpSession, RedirectAttributes redirectAttributes, Model model) throws Exception {
		logger.info("***** DownloadSettlement.DownloadSettlement.Get Start ****");
		String display = "";
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		System.out.println("DATE IS DATE IS " + filedate);
		List<String> data = new ArrayList<String>();

		String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator;
		logger.info("TEMP_DIR" + TEMP_DIR);

		String stpath = TEMP_DIR;
		stpath = stpath + "CAST_TTUM";
		data = NFSttumService.getLateRev(filedate);
		String fileName = "Late_rev_text.txt";
		GenerateDLBVoucher vouchObj = new GenerateDLBVoucher();
		vouchObj.generateTTUMFile(stpath, fileName, data);
		File file = new File(stpath + File.separator + fileName);
		logger.info("path of zip file " + stpath);
		FileInputStream inputstream = new FileInputStream(file);
		response.setContentLength((int) file.length());
		logger.info("before downloading zip file ");
		response.setContentType("application/txt");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
		response.setHeader(headerKey, headerValue);
		OutputStream outStream = response.getOutputStream();
		IOUtils.copy(inputstream, outStream);
		response.flushBuffer();
		logger.info("***** VisaSettlementController.DownloadVisaSettlementReport POST End ****");
		return "GenerateVisaSettlementReport";
	}

	@RequestMapping(value = "downloadReport", method = RequestMethod.POST)
	public void InetSummuryDownload(@RequestParam("fileDate") String filedate, HttpServletRequest request,
			HttpServletResponse response, HttpSession httpSession, RedirectAttributes redirectAttributes, Model model)
			throws Exception {
		logger.info("***** PresentmentDownload.POST Start ****");
		System.out.println("inside the excel download rupaysettlementcontroller 316");
		List<Object> Excel_data = new ArrayList<Object>();
		System.out.println("rahul code " + filedate);
		// GET DATA FOR REPORT
//		Excel_data = rupayDao.getSummuryDownloadReport(filedate);
		model.addAttribute("ReportName", "Presentment_Data_" + filedate);
		model.addAttribute("data", Excel_data);
		logger.info("***** PresentmentDownload Daily POST End ****");
		// return "GenerateNFSDailyReport";

		ServletOutputStream sou = null;
		ServletOutputStream sou2 = null;
		String fileName = null;
		String fileName2 = null;

		try {

			// System.out.println("REQUEST RECE");
			fileName = NFSttumService.OutwardReport(filedate, filedate, "", "report");
			model.addAttribute("message", "DONE");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename= " + fileName);
			FileInputStream ins = new FileInputStream(new File(fileName));
			sou = response.getOutputStream();
			sou.write(IOUtils.toByteArray(ins));
			sou.flush();
			ins.close();
			sou.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				new File(fileName).delete();
				sou.flush();
				sou.close();
				System.gc();
			} catch (Exception e2) {
			}

		}

	}

	@RequestMapping(value = "RupayFileUpload", method = RequestMethod.POST)
	@ResponseBody
	public String RupayFileUploadPost(@ModelAttribute("rupaySettlementBean") RupayUploadBean rupayBean,
			HttpServletRequest request, @RequestParam("file") MultipartFile file, HttpSession httpSession, Model model,
			ModelAndView modelAndView, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("***** RupayFileUpload.post Start ****");
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		rupayBean.setCreatedBy(Createdby);
		logger.info("Subcategory is " + rupayBean.getSubcategory());

		if (rupayBean.getFileName().equalsIgnoreCase("CHARGEBACK")) {
			logger.info("Cycle is " + file.getOriginalFilename().substring(2, 3));
			rupayBean.setCycle(file.getOriginalFilename().substring(2, 3));
		} else {
			if (file.getOriginalFilename().contains("-")) {
				String[] fileName = file.getOriginalFilename().split("\\.");
				logger.info("Cycle is " + fileName[0].substring(fileName[0].length() - 1, fileName[0].length()));
				rupayBean.setCycle(fileName[0].substring(fileName[0].length() - 1, fileName[0].length()));
			} else {
				return "Invalid file uploaded";
			}
		}

		// VALIDATE WHETHER FILE IS ALREADY UPLOADED
		if (rupaySettlementService.checkFileUploaded(rupayBean)) {
			return "File is already uploaded!";
		} else {
			Boolean readFlag = false;
			if (rupayBean.getSubcategory().equalsIgnoreCase("DOMESTIC")) {
				readFlag = rupaySettlementService.readFile(rupayBean, file);
			} else {
				readFlag = rupaySettlementService.readIntFile(rupayBean, file);
			}
			if (readFlag) {
				return "File Uploaded Successfully";
			} else {
				return "Issue while uploading file";
			}
		}
	}

	/********************* rupay settlement processing *******************/
	@RequestMapping(value = "RupaySettlementProcess", method = RequestMethod.GET)
	public ModelAndView RupaySettlementProcessGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** RupayFileUpload.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupayFileUpload GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("RupaySettlementProcess");

		logger.info("***** RupaySettlementController.RupayFileUpload GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "CashAtPOSRecon", method = RequestMethod.POST)
	@ResponseBody
	public String CashAtPOSRecon(@RequestParam("fileDate") String filedate, HttpServletRequest request,
			HttpSession httpSession) throws Exception {

		System.out.println("date is" + filedate);
		boolean validateDate = false;

		String mdate1 = generalUtil.DateFunction(filedate);
		validateDate = rupaySettlementService.validateCashProcess(mdate1);
		if (validateDate) {
			boolean executeFlag = rupaySettlementService.processCashAtPos(filedate);
			if (executeFlag)
				return "Recon Process is Done!!";
			else
				return "Recon Process Error!!";
		} else
			return "Recon is already processed.";

	}

	@RequestMapping(value = "LateRevRecon", method = RequestMethod.POST)
	@ResponseBody
	public String LateRevRecon(@RequestParam("fileDate") String filedate, HttpServletRequest request,
			HttpSession httpSession) throws Exception {

		System.out.println("date is" + filedate);
		boolean validateDate = false;
		boolean checkrecord = false;

		checkrecord = rupaySettlementService.checkRecord(filedate);

		if (checkrecord) {
			validateDate = rupaySettlementService.validateLateRev(filedate);
			if (validateDate) {
				boolean executeFlag = rupaySettlementService.processLateRev(filedate);

				if (executeFlag)
					return "Recon Process is Done!!";
				else
					return "Recon Process Error!!";
			} else
				return "Recon is already processed.";
		} else {
			return "No records for processing";
		}
	}

	@RequestMapping(value = "PresentmentRecon", method = RequestMethod.POST)
	@ResponseBody
	public String PresentmentRecon(@RequestParam("fileDate") String filedate, HttpServletRequest request,
			HttpSession httpSession) throws Exception {
		System.out.println(filedate);
		boolean validateDate = false;
		validateDate = rupaySettlementService.validatePresentmentProcess(filedate);
		if (validateDate) {
			boolean executeFlag = rupaySettlementService.processPresentment(filedate);
			if (executeFlag)
				return "Recon Process is Done!!";
			else
				return "Recon Process Error!!";
		} else
			return "Recon is already processed.";
	}

	@RequestMapping(value = "RupaySettlementProcess", method = RequestMethod.POST)
	@ResponseBody
	public String RupaySettlementProcessPost(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.RupaySettlementProcess post Start ****");
		logger.info("RupaySettlementProcess POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		System.out.println("File name is " + beanObj.getFileName());
		boolean executionFlag = false;

		// check whether settlement is already processed
		boolean checkProcess = rupaySettlementService.validateSettlementProcess(beanObj);
		if (checkProcess) {
			return "Settlement for selected date and cycle is already processed";
		} else {
			// 1. VALIDATE WHETHER RAW FILES ARE UPLOADED FOR SELECTED DATE
			HashMap<String, Object> output = rupaySettlementService.validateRawfiles(beanObj);

			// 2. VALIDATE WHETHER chargeback and rest 4 FILES ARE UPLOADED FOR SELECTED
			// DATE
			if (output != null && (Boolean) output.get("result")) {
				output = rupaySettlementService.validateSettlementFiles(beanObj);

				if (output != null && (Boolean) output.get("result")) {
					// PROCESS SETTLEMENT
					boolean executeFlag = rupaySettlementService.processSettlement(beanObj);

					if (executeFlag) {
						return "Settlement Processing completed";
					} else {
						return "Settlement Processing Failed";
					}
				} else {
					return output.get("msg").toString();
				}
			} else {
				return output.get("msg").toString();
			}
		}

	}

	@RequestMapping(value = "ValidateRupaySettlement", method = RequestMethod.POST)
	@ResponseBody
	public String ValidateRupaySettlement(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.ValidateRupaySettlement post Start ****");
		logger.info("ValidateRupaySettlement POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		System.out.println("File name is " + beanObj.getFileName());
		boolean executionFlag = false;

		Boolean checkFlag = rupaySettlementService.validateSettlementProcess(beanObj);

		if (checkFlag) {
			return "success";
		} else {
			return "Settlement is not processed for selected date and cycle";
		}

	}

	@RequestMapping(value = "DownloadSettlement", method = RequestMethod.POST)
	public String DownloadSettlement(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession, Model model) throws Exception {
		logger.info("***** RupaySettlementController.DownloadSettlement post Start ****");
		logger.info("DownloadSettlement POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		System.out.println("File name is " + beanObj.getFileName());
		boolean executionFlag = false;
		List<Object> Excel_data = new ArrayList<Object>();

		// GET DATA FOR REPORT
		Excel_data = rupaySettlementService.getSettlementData(beanObj);

		model.addAttribute("ReportName", "Rupay_Settlement_" + beanObj.getFileDate() + "_cycle_" + beanObj.getCycle());
		model.addAttribute("data", Excel_data);

		logger.info("***** RupaySettlementController.DownloadSettlement POST End ****");
		return "GenerateRupaySettlementReport";

	}

	/**************** RUPAY SETTLEMENT TTUM ***********************/
	@RequestMapping(value = "RupaySettlementProcessTTUM", method = RequestMethod.GET)
	public ModelAndView RupaySettlementTTUMGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("HELLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupaySettlementTTUM GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("RupaySettlementProcessTTUM");

		logger.info("***** RupaySettlementController.RupaySettlementTTUM GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "RupaySettlementTTUM", method = RequestMethod.POST)
	@ResponseBody
	public String RupaySettlementTTUMPost(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.RupaySettlementTTUMPost post Start ****");
		logger.info("RupaySettlementTTUMPost POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		boolean executionFlag = false;
		logger.info("Subcategory is " + beanObj.getSubcategory());

		System.out.println("here technologies");

		// VALIDATE THERE FILE IS UPLOADED OR NOT
		boolean checkfileUpload = rupaySettlementService.validateFileUpload(beanObj);
		if (!checkfileUpload) {

			// VALIDATE WHETHER ALREADY PROCESSED validateFileUpload
			boolean checkTTUMProcess = rupaySettlementService.validateSettlementTTUM(beanObj);

//		if(!checkTTUMProcess)
//		{
//			// CHECK WHETHER SETTLEMENT IS PROCESSED OR NOT
//			boolean settlementProcess = rupaySettlementService.validateSettlementProcess(beanObj);

			// if(settlementProcess)
			if (checkTTUMProcess) {
				// now process ttum
				if (rupaySettlementService.processSettlementTTUM(beanObj)) {
					return "Settlement is Completed Successfully! \n Please download Reports";
				} else {
					return "Issue while processing TTUM";
				}

			} else {
				return "Already Processed";
			}
		}

		return "File is Not Uploaded for Selected Date";
	}
//		else
//		{
//			return "Settlement TTUM is already processed for selected date and cycle";
//		}

	@RequestMapping(value = "ValidateSettlementTTUM", method = RequestMethod.POST)
	@ResponseBody
	public String ValidateSettlementTTUM(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.ValidateSettlementTTUM post Start ****");
		logger.info("ValidateSettlementTTUM POST");
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);

		Boolean checkFlag = rupaySettlementService.validateSettlementTTUM(beanObj);

		if (checkFlag) {
			return "success";
		} else {
			return "success";
		}

	}

	@RequestMapping(value = "DownloadRupaySettlementTTUM", method = RequestMethod.POST)
	@ResponseBody
	public String DownloadSettlementTTUM(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletResponse response, HttpServletRequest request, HttpSession httpSession, Model model)
			throws Exception {
		logger.info("***** RupaySettlementController.DownloadSettlement post Start ****");
		logger.info("DownloadSettlement POST");
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		boolean executionFlag = false;
		List<Object> Excel_data = new ArrayList<Object>();
		List<String> data = new ArrayList<String>();

		// GET DATA FOR REPORT
//		TTUMData = rupaySettlementService.getSettlementTTUMData(beanObj);
//
//		/*model.addAttribute("ReportName", "Rupay_Settlement_"+beanObj.getSubcategory()+"_TTUM");
//		model.addAttribute("data", Excel_data);
//
//		logger.info("***** RupaySettlementController.DownloadSettlementTTUM POST End ****");
//
//		return "GenerateRupaySettlementReport";*/
//
//		// Downloading TEXT TTUM FOR UCO
//		String fileName = "RUPAY_SETTLEMENT_TTUM1.txt";
//
//		String stPath = System.getProperty("java.io.tmpdir");
//		
//		//String stPath = System.getProperty("D:\\saurabh");
//		logger.info("TEMP_DIR"+stPath);
//		
//		
//
//		GenerateUCOTTUM obj = new GenerateUCOTTUM();
//		stPath = obj.checkAndMakeDirectory(beanObj.getFileDate(), "RUPAY");
//		List<Object> TTUM_data = (List<Object>) TTUMData.get(0);
//		obj.generateTTUMFile(stPath, fileName, TTUM_data);
//		logger.info("File is created");
//
//
//		/*** creating drm ttum*****/
//		/*TTUM_data = (List<Object>) TTUMData.get(1);
//		obj.generateDRMTTUM(stPath, "RUPAY_SETTLEMENT_TTUM2.txt", TTUM_data, "RUPAY");*/
//
//		/*** creating excel ***/
//		List<String> Column_list = new ArrayList<String>();
//		Column_list.add("ACCOUNT_NUMBER");
//		Column_list.add("PART_TRAN_TYPE");	
//		Column_list.add("TRANSACTION_AMOUNT");	
//		Column_list.add("TRANSACTION_PARTICULAR");	
//		Column_list.add("CYCLE");
//
//		Excel_data.add(Column_list);
//		Excel_data.add(TTUMData);
//
//		fileName = "RUPAY_SETTLEMENT_TTUM.xls";
//		String zipName = "Rupay_Settlement.zip"; 
//		obj.generateExcelTTUM(stPath, fileName, Excel_data,"SETTLEMENT",zipName);
//		logger.info("File is created");
//
//
//		File file = new File(stPath +File.separator+zipName);
//		logger.info("path of zip file "+stPath +File.separator+zipName);
//		FileInputStream inputstream = new FileInputStream(file);
//		response.setContentLength((int) file.length());
//		logger.info("before downloading zip file ");
//		response.setContentType("application/txt");
//		logger.info("download completed");
//
//		/** Set Response header */
//		String headerKey = "Content-Disposition";
//		String headerValue = String.format("attachment; filename=\"%s\"",
//				file.getName());
//		response.setHeader(headerKey, headerValue);
//
//		/** Write response. */
//		OutputStream outStream = response.getOutputStream();
//		IOUtils.copy(inputstream, outStream);
//		response.flushBuffer();

		String stpath = System.getProperty("java.io.tmpdir");
		;
		stpath = stpath + "SETTLEMENT_TTUM";
		data = rupaySettlementService.getSettlementTTUMData(beanObj);
		String fileName = "ECOM_POS_SETTLEMENT_TTUM.txt";

		GenerateDLBVoucher vouchObj = new GenerateDLBVoucher();

		vouchObj.generateTTUMFile(stpath, fileName, data);

		File file = new File(stpath + File.separator + fileName);
		logger.info("path of zip file " + stpath);
		FileInputStream inputstream = new FileInputStream(file);
		response.setContentLength((int) file.length());
		logger.info("before downloading zip file ");
		response.setContentType("application/txt");

		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
		response.setHeader(headerKey, headerValue);

		OutputStream outStream = response.getOutputStream();
		IOUtils.copy(inputstream, outStream);

		response.flushBuffer();

//		model.addAttribute("ReportName", "NFS_Settlement_Voucher");
//		model.addAttribute("data", Excel_data);

		logger.info("***** VisaSettlementController.DownloadVisaSettlementReport POST End ****");
		return "GenerateVisaSettlementReport";

	}

	@RequestMapping(value = "RupaySettlementRectify", method = RequestMethod.POST)
	@ResponseBody
	public String SettlementRectify(@ModelAttribute("nfsSettlementBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession, RedirectAttributes redirectAttributes, Model model)
			throws Exception {
		logger.info("***** SettlementRectify.post Start ****");
		HashMap<String, Object> output = null;
		try {
			logger.info("SettlementRectify : Post");
			String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
			logger.info("Created by is " + Createdby);
			beanObj.setCreatedBy(Createdby);

			boolean result = rupaySettlementService.validateSettlementDiff(beanObj);

			if (result) {
				// validate whether entered amt and diff amt are same
				output = rupaySettlementService.validateDiffAmount(beanObj);

				if (output != null && (Boolean) output.get("result")) {
					// process rectification
					boolean rectify_flag = rupaySettlementService.processRectification(beanObj);

					if (rectify_flag)
						return "Amount is Rectified !";
					else
						return "Issue while rectification !";
				} else {
					return output.get("msg").toString();
				}

			} else {
				return "No data for rectification !";
			}

		} catch (Exception e) {
			logger.info("Exception in NFSSettlementController " + e);

			return "Error Occurred in getting Difference";

		}
	}

	/****************** NCMC SETTLEMENT ***********************/
	@RequestMapping(value = "NCMCFileUpload", method = RequestMethod.GET)
	public ModelAndView NCMCFileUploadGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** RupayFileUpload.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupayFileUpload GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("NCMCFileUpload");

		logger.info("***** RupaySettlementController.RupayFileUpload GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "NCMCFileUpload", method = RequestMethod.POST)
	@ResponseBody
	public String NCMCFileUploadPost(@ModelAttribute("rupaySettlementBean") RupayUploadBean rupayBean,
			HttpServletRequest request, @RequestParam("file") MultipartFile file, HttpSession httpSession, Model model,
			ModelAndView modelAndView, RedirectAttributes redirectAttributes) throws Exception {
		logger.info("***** RupayFileUpload.post Start ****");
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		rupayBean.setCreatedBy(Createdby);
		logger.info("Subcategory is " + rupayBean.getSubcategory());

		if (file.getOriginalFilename().contains("-")) {
			String[] fileName = file.getOriginalFilename().split("\\.");
			logger.info("Cycle is " + fileName[0].substring(fileName[0].length() - 1, fileName[0].length()));
			rupayBean.setCycle(fileName[0].substring(fileName[0].length() - 1, fileName[0].length()));
		}

		// VALIDATE WHETHER FILE IS ALREADY UPLOADED
		if (rupaySettlementService.checkNCMCFileUploaded(rupayBean)) {
			return "File is already uploaded!";
		} else {
			Boolean readFlag = false;
			if (rupayBean.getSubcategory().equalsIgnoreCase("DOMESTIC")) {
				readFlag = rupaySettlementService.readNCMCFile(rupayBean, file);
			} else {
				readFlag = rupaySettlementService.readIntFile(rupayBean, file);
			}
			if (readFlag) {
				return "File Uploaded Successfully";
			} else {
				return "Issue while uploading file";
			}
		}

	}

	// Rupay Settlement Rollback
	@RequestMapping(value = "RupaySettRollback", method = RequestMethod.POST)
	@ResponseBody
	public String RupaySettRollbackPost(@ModelAttribute("mastercardUploadBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.RupaySettRollback post Start ****");
		logger.info("RupaySettRollback POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		boolean executionFlag = false;
		logger.info("Subcategory is " + beanObj.getSubcategory());

		// VALIDATE WHETHER ALREADY PROCESSED
		boolean checkProcess = rupaySettlementService.validateSettlementProcess(beanObj);

		if (checkProcess) {
			// check ttum processed
			// rollback settlement
			if (rupaySettlementService.settlementRollback(beanObj)) {
				return " Settlement Rollback completed";
			} else {
				return "issue while rolling Back";
			}
		} else {
			return "Settlement is not processed";
		}

	}

	/************** RUPAY SETTLEMENT FILE ROLLBACK *************************/
	@RequestMapping(value = "RupaySettFileRollback", method = RequestMethod.GET)
	public ModelAndView RupaySettFileRollbackGet(ModelAndView modelAndView, @RequestParam("category") String category,
			HttpServletRequest request) throws Exception {
		logger.info("***** RupaySettFileRollback.Get Start ****");
		RupayUploadBean rupaySettlementBean = new RupayUploadBean();
		logger.info("RupaySettFileRollback GET");
		String display = "";
		logger.info("in GetHeaderList" + category);

		modelAndView.addObject("category", category);
		// modelAndView.addObject("nfsSettlementBean",nfsSettlementBean);
		modelAndView.addObject("rupaySettlementBean", rupaySettlementBean);
		modelAndView.setViewName("RupaySettFilesRollback");

		logger.info("***** RupaySettlementController.RupayFileUpload GET End ****");
		return modelAndView;
	}

	@RequestMapping(value = "RupaySettFileRollback", method = RequestMethod.POST)
	@ResponseBody
	public String RupaySettFileRollbackPost(@ModelAttribute("rupaySettlementBean") RupayUploadBean beanObj,
			HttpServletRequest request, HttpSession httpSession) throws Exception {
		logger.info("***** RupaySettlementController.RupaySettFileRollback post Start ****");
		logger.info("RupaySettFileRollback POST");
		logger.info("File Type is " + beanObj.getFileType() + " Date selected " + beanObj.getFileDate());
		logger.info("cycle selected is " + beanObj.getCycle());
		String Createdby = ((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();
		logger.info("Created by is " + Createdby);
		beanObj.setCreatedBy(Createdby);
		boolean executionFlag = false;
		logger.info("Subcategory is " + beanObj.getSubcategory());

		// VALIDATE WHETHER SETTLEMENT FILE IS UPLOADED
		boolean checkUpload = rupaySettlementService.checkFileUploaded(beanObj);

		if (checkUpload) {
			// check if settlement is processed
			boolean checkSettProcess = rupaySettlementService.validateSettlementProcess(beanObj);
			if (!checkSettProcess) {
				// rollback settlement File
				if (rupaySettlementService.settlementFilesRollback(beanObj)) {
					return beanObj.getFileName() + " File Rollback completed";
				} else {
					return "issue while rolling Back";
				}
			} else {
				return "Settlement is processed.\n Please rollback settlement first";
			}
		} else {
			return "Selected file is not uploaded";
		}

	}

	@RequestMapping(value = "CbsDataFetch", method = RequestMethod.POST)
	@ResponseBody
	public String CbsDataFetch(@RequestParam("fileDate") String filedate, HttpServletRequest request,
			HttpSession httpSession) throws Exception {

		System.out.println("date is" + filedate);
		boolean validateDate = false;
		boolean checkrecord = false;

		checkrecord = rupaySettlementService.checkCbsRecordPresent(filedate);

		if (!checkrecord) {
			boolean executeFlag = rupaySettlementService.processCbs(filedate);

			if (executeFlag)
				return "CBS data Fetching is Done!!";
			else
				return "CBS data Fetching Error!!";
		} else
			return "CBS data Fetching is already processed.";
	}

}
