
package com.recon.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.recon.dao.ICompareConfigDao;
import com.recon.model.CompareBean;
import com.recon.model.CompareSetupBean;
import com.recon.model.FileColumnDtls;
import com.recon.model.LoginBean;
import com.recon.model.ManualCompareBean;
import com.recon.model.ManualFileColumnDtls;
import com.recon.service.CompareService;
import com.recon.service.ICompareConfigService;
import com.recon.util.CSRFToken;
import com.recon.util.demo;

@Controller
public class CompareConfigController {
	private static final String ERROR_MSG = "error_msg";
	private static final String SUCCESS_MSG = "success_msg";
	private static final Logger logger = Logger.getLogger(SourceController.class);
	@Autowired
	ICompareConfigService icompareConfigService;
	@Autowired
	CompareService compareService;
	@Autowired
	ICompareConfigDao compareconfigDao;

	// Main Menu URL
	@RequestMapping(value = "Compare", method = RequestMethod.GET)
	public ModelAndView Configuration(ModelAndView modelAndView) {
		modelAndView.setViewName("CompareMenu");
		return modelAndView;

	}

	// Sub Menu URL
	@RequestMapping(value = "CompareSetup", method = RequestMethod.GET)
	public ModelAndView comparesetup(Model model, CompareSetupBean compareSetupBean) {
		logger.info("***** CompareConfigController.comparesetup Start ****");
		List<CompareSetupBean> file_list = new ArrayList<CompareSetupBean>();
		file_list = icompareConfigService.getFileDetails();

		List<CompareSetupBean> setup_dtl_list = new ArrayList<CompareSetupBean>();
		for (int i = setup_dtl_list.size() + 1; i <= 1; i++) {

			setup_dtl_list.add(new CompareSetupBean());
		}

		List<FileColumnDtls> columnDtls = new ArrayList<FileColumnDtls>();
		/** Add new Objects if less or none are available. */
		for (int i = columnDtls.size() + 1; i <= 1; i++) {
			columnDtls.add(new FileColumnDtls());
		}

		compareSetupBean.setColumnDtls(columnDtls);
		compareSetupBean.setSetup_dtl_list(setup_dtl_list);

		ModelAndView mav = new ModelAndView("CompareSetup");
		mav.addObject("setup_dtl_list", setup_dtl_list);
		mav.addObject("file_list", file_list);
		mav.addObject("CompareSetupBean", compareSetupBean);
		mav.addObject("columnDtls", columnDtls);

		logger.info("***** CompareConfigController.comparesetup End ****");

		return mav;

	}

	// Sub Menu URL
	@RequestMapping(value = "ViewCompareSetup", method = RequestMethod.GET)
	public ModelAndView viewcomparesetup(Model model, CompareSetupBean compareSetupBean, ModelAndView modelAndView) {

		/* try{ */
		modelAndView.setViewName("ViewCompareSetup");
		modelAndView.addObject("CompareSetupBean", compareSetupBean);
		return modelAndView;
		/*
		 * }catch(Exception e){
		 * 
		 * logger.error(e.getMessage()); redirectAttributes.addFlashAttribute(ERROR_MSG,
		 * e.getMessage());
		 * 
		 * return "redirect:Login.do"; }
		 */

	}
	// AutoCompare.do

	@RequestMapping(value = "AutoCompare", method = RequestMethod.GET)
	public ModelAndView AutoCompare(Model model, CompareSetupBean compareSetupBean, ModelAndView modelAndView) {

		modelAndView.setViewName("AutoCompare");
		modelAndView.addObject("CompareSetupBean", compareSetupBean);
		return modelAndView;

	}

	// ManualCompareSetup.do
	@RequestMapping(value = "ManualCompareSetup", method = RequestMethod.GET)
	public ModelAndView ManualCompareSetup(Model model, ManualCompareBean manualCompBean, ModelAndView modelAndView,
			HttpSession httpSession) {
		logger.info("***** CompareConfigController.ManualCompareSetup Start ****");
		((LoginBean) httpSession.getAttribute("loginBean")).getUser_id();

		List<CompareSetupBean> file_list = new ArrayList<CompareSetupBean>();
		file_list = icompareConfigService.getFileDetails();

		List<ManualCompareBean> comp_dtl_list = new ArrayList<ManualCompareBean>();
		/** Add new Objects if less or none are available. */
		for (int i = comp_dtl_list.size() + 1; i <= 1; i++) {
			comp_dtl_list.add(new ManualCompareBean());
		}

		manualCompBean.setComp_dtl_list(comp_dtl_list);

		List<ManualFileColumnDtls> columnDtls = new ArrayList<ManualFileColumnDtls>();
		/** Add new Objects if less or none are available. */
		for (int i = columnDtls.size() + 1; i <= 1; i++) {
			columnDtls.add(new ManualFileColumnDtls());
		}

		manualCompBean.setColumnDtls(columnDtls);
		ModelAndView mav = new ModelAndView("ManualCompareSetup");
		mav.addObject("file_list", file_list);
		mav.addObject("ManualCompBean", manualCompBean);
		mav.addObject("comp_dtl_list", comp_dtl_list);
		mav.addObject("columnDtls", columnDtls);

		logger.info("***** CompareConfigController.ManualCompareSetup End ****");

		return mav;

	}

	// ManualUpload.do
	@RequestMapping(value = "ManualUpload", method = RequestMethod.GET)
	public ModelAndView ManualUpload(Model model, CompareSetupBean compareSetupBean, ModelAndView modelAndView,
			HttpSession httpSession, HttpServletRequest request) throws Exception {
		logger.info("***** CompareConfigController.ManualUpload start ****");
		try {

			compareSetupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
			ArrayList<CompareSetupBean> setupBeanslist = null;

			setupBeanslist = icompareConfigService.getFileList();

			String csrf = CSRFToken.getTokenForSession(request.getSession());

			// redirectAttributes.addFlashAttribute("CSRFToken", csrf);
			modelAndView.addObject("CSRFToken", csrf);
			model.addAttribute("configBeanlist", setupBeanslist);
			modelAndView.setViewName("ManualUpload");
			modelAndView.addObject("CompareSetupBean", compareSetupBean);

			logger.info("***** CompareConfigController.ManualUpload End ****");

			return modelAndView;
		} catch (Exception ex) {
			demo.logSQLException(ex, "CompareConfigController.ManualUpload");
			logger.error(" error in CompareConfigController.ManualUpload",
					new Exception("CompareConfigController.ManualUpload", ex));
			modelAndView.setViewName("Login");
			return modelAndView;
		}

	}

	@RequestMapping(value = "CbsDatafetch", method = RequestMethod.GET)
	public ModelAndView CbsDatafetch(Model model, CompareSetupBean compareSetupBean, ModelAndView modelAndView,
			HttpSession httpSession, HttpServletRequest request) throws Exception {
		logger.info("***** CompareConfigController.ManualUpload start ****");
		try {

			compareSetupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
			ArrayList<CompareSetupBean> setupBeanslist = null;

			setupBeanslist = icompareConfigService.getFileList();

			String csrf = CSRFToken.getTokenForSession(request.getSession());

			// redirectAttributes.addFlashAttribute("CSRFToken", csrf);
			modelAndView.addObject("CSRFToken", csrf);
			model.addAttribute("configBeanlist", setupBeanslist);
			modelAndView.setViewName("CbsDatafetch");
			modelAndView.addObject("CompareSetupBean", compareSetupBean);

			logger.info("***** CompareConfigController.ManualUpload End ****");

			return modelAndView;
		} catch (Exception ex) {
			demo.logSQLException(ex, "CompareConfigController.ManualUpload");
			logger.error(" error in CompareConfigController.ManualUpload",
					new Exception("CompareConfigController.ManualUpload", ex));
			modelAndView.setViewName("Login");
			return modelAndView;
		}

	}

	@RequestMapping(value = "ManualCompare", method = RequestMethod.GET)
	public ModelAndView ManualCompare(Model model, CompareSetupBean compareSetupBean, ModelAndView modelAndView,
			HttpSession httpSession) throws Exception {
		logger.info("***** CompareConfigController.ManualCompare Start ****");
		try {
			compareSetupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
			ArrayList<CompareSetupBean> setupBeanslist = null;

			setupBeanslist = icompareConfigService.getFileList();

			model.addAttribute("configBeanlist", setupBeanslist);
			modelAndView.setViewName("ManualCompare");
			modelAndView.addObject("CompareSetupBean", compareSetupBean);

			logger.info("***** CompareConfigController.ManualCompare End ****");

			return modelAndView;

		} catch (Exception ex) {
			demo.logSQLException(ex, "CompareConfigController.ManualCompare");
			logger.error(" error in CompareConfigController.ManualCompare",
					new Exception("CompareConfigController.ManualCompare", ex));
			modelAndView.setViewName("Login");
			return modelAndView;

		}

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "manualUploadFile", method = RequestMethod.POST)

	public ResponseEntity UploadFile(@ModelAttribute("CompareSetupBean") CompareSetupBean setupBean,
			HttpServletRequest request, // @RequestParam("dataFile1") MultipartFile file,
			@RequestParam("file") MultipartFile file, String filename, String excelType, String fileType,
			String category, String stSubCategory, String fileDate, HttpSession httpSession, Model model,
			ModelAndView modelAndView, RedirectAttributes redirectAttributes) throws Exception {

		String phyFileName = "";
		logger.info("***** CompareConfigController.UploadFile Start ****");
		HashMap<String, Object> output = new HashMap<String, Object>();
		String date = setupBean.getFileDate();
		int fileid = setupBean.getInFileId();
		System.out.println(fileid);

		setupBean.setFileType(fileType);
		setupBean.setExcelType(excelType);
		setupBean.setFilename(filename);
		setupBean.setCategory(category);
		if (setupBean.getFilename().equalsIgnoreCase("REV_REPORT"))
			setupBean.setStSubCategory("ACQUIRER");
		else
			setupBean.setStSubCategory(stSubCategory);
		setupBean.setFileDate(fileDate);
		setupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());

		logger.info(file);
		logger.info(date);
		logger.info(fileid);

		// Check File already uploaded or not

		// icompareConfigService.chkFileupload(setupBean);
		// icompareConfigService.chkFileupload(setupBean);
		String chkFlag = "";

		if (setupBean.getFileType().equalsIgnoreCase("Manual")) {

			chkFlag = "ManUpload_FLAG";

		} else {

			chkFlag = "Upload_FLAG";

		}
		logger.info("chkFlag==" + chkFlag);

		/* jit-Start //create a method for check filename first exist or not */

		phyFileName = file.getOriginalFilename();
		setupBean.setP_FILE_NAME(phyFileName);
		System.out.println("Physical File name::>" + phyFileName);

		String arr[] = phyFileName.split("\\.");
		System.out.println("arr 0 is :" + arr[0]);
		System.out.println("arr 1 is :" + arr[1]);

		if (!(arr[1].equals("html") || arr[1].contains("txt"))) {

			try {
				Boolean checkFileName = false; // earlier false changed to true
				if (setupBean.getCategory().equalsIgnoreCase("RUPAY")) {
					checkFileName = icompareConfigService.chkBeforeUploadFile(chkFlag, setupBean);
				}

				if (!checkFileName) {
					if (!setupBean.getFilename().equalsIgnoreCase("REV_REPORT")) {
						output = icompareConfigService.checkUploadedFileName(setupBean.getCategory(),
								setupBean.getStSubCategory(), phyFileName);

					} else
						output.put("result", true);

					if (output != null && (Boolean) output.get("result")) {
						if (!setupBean.getFilename().equalsIgnoreCase("REV_REPORT")) {

						} else
							output.put("result", true);

						if (output != null && (Boolean) output.get("result")) {
							if (icompareConfigService.chkUploadFlag(chkFlag, setupBean).equalsIgnoreCase("N")
									&& (!setupBean.getFilename().equals("REV_REPORT"))) {
								if (!file.isEmpty()) {

									try {

										/*
										 * byte[] bytes = file.getBytes();
										 * 
										 * 
										 * File serverFile = new File("\\\\10.144.143.191\\led\\DCRS\\test"+
										 * File.separator + file+".txt"); BufferedOutputStream stream = new
										 * BufferedOutputStream(new FileOutputStream(serverFile)); stream.write(bytes);
										 * stream.close();
										 */

										// Validating File.
										// if(icompareConfigService.validateFile(setupBean,file)){

										// Uploading File.
										// ADDED BY INT8624 FOR DISPLAYING ERROR TO USER
										output = icompareConfigService.uploadFile(setupBean, file); // here
										boolean result = (boolean) output.get("result");
										if (result) {

											int recordcount = icompareConfigService.getrecordcount(setupBean);

											logger.info("recordcount==" + recordcount);

											// ADDED BY INT8624 FOR EXCEPTION HANDLING
											try {
												if (output.containsKey("msg")
														&& !setupBean.getCategory().equalsIgnoreCase("RUPAY")) {
													String msg = (String) output.get("msg");
													return new ResponseEntity(
															"File Uploaded Successfuly!! \n  Total Record Count :"
																	+ recordcount,
															HttpStatus.OK);
												} else {
													return new ResponseEntity(
															"File Uploaded Successfuly!! \n Total Record Count : "
																	+ recordcount + "",
															HttpStatus.OK);
												}
											} catch (Exception e) {
												logger.error("Exception while getting msg from hashmap");
											}

											return new ResponseEntity(
													"File Uploaded Successfuly!! \n Total Record Count : " + recordcount
															+ "",
													HttpStatus.OK);

										} else {
											// ADDED BY BHAGYASHREE FOR EXCEPTION WHILE FILE READING FILE
											if (output.containsKey("msg")) {
												logger.info("Inside else code " + output.containsKey("msg"));
												String msg = (String) output.get("msg");
												return new ResponseEntity("File not Uploaded!! \n " + msg,
														HttpStatus.OK);
											} else {
												return new ResponseEntity("File not Uploaded!!", HttpStatus.OK);
											}

										}
										// icompareConfigService.updateFlag("Upload_FLAG", setupBean);

										// logger.info("***** CompareConfigController.UploadFile End ****");

									} catch (Exception e) {
										demo.logSQLException(e, "CompareConfigController.UploadFile");
										logger.error(" error in CompareConfigController.UploadFile",
												new Exception("CompareConfigController.UploadFile", e));
										redirectAttributes.addFlashAttribute(ERROR_MSG,
												"error occured while uploading file");
										return new ResponseEntity(ERROR_MSG, HttpStatus.OK);
									}
								}
							} else if ((setupBean.getFilename().equals("REV_REPORT"))) {
								output = icompareConfigService.uploadREV_File(setupBean, file);
								if (output != null && (Boolean) output.get("result")) {
									String count = output.get("msg").toString();
									// int recordcount = icompareConfigService.getREVrecordcount(setupBean);
									logger.info("recordcount==" + count);
									return new ResponseEntity(
											"File Uploaded Successfuly!! \n Total Record Count : " + count + "",
											HttpStatus.OK);

								} else {
									logger.info("msg is " + output.get("msg").toString());
									return new ResponseEntity(output.get("msg").toString(), HttpStatus.OK);
								}
							} else {
								// ADDED BY INT8624 FOR EMPTY FILES
								if (file.isEmpty()) {

									logger.info("File is Empty");
									return new ResponseEntity("Uploaded File is Empty", HttpStatus.OK);

								} else {
									redirectAttributes.addFlashAttribute(ERROR_MSG, "File already uploaded");
									logger.info("File already uploaded_for file not empty");
								}
							}

						} else {
							return new ResponseEntity(output.get("msg").toString(), HttpStatus.OK); // direct jumps here

						}
					} else {
						return new ResponseEntity(output.get("msg").toString(), HttpStatus.OK);

					}
				} else {

					redirectAttributes.addFlashAttribute(ERROR_MSG, "File already uploaded");
					logger.info("File already uploaded_else of checkfilename");
				}
			} catch (Exception e) {
				return new ResponseEntity(e.getMessage(), HttpStatus.OK);
			}
		} else {
			return new ResponseEntity("Please check the format of the File ", HttpStatus.OK);
		}

		if (file.isEmpty()) {
			logger.info("File is Empty");
			return new ResponseEntity("Uploaded File is Empty", HttpStatus.OK);
		} else
			return new ResponseEntity("File already uploaded_for empty file", HttpStatus.OK);

	}

	// Main Menu Url ="classify file"
	@RequestMapping(value = "classifyFile", method = RequestMethod.GET)

	public ModelAndView ClassifyFile(@ModelAttribute("CompareSetupBean") CompareSetupBean setupBean,
			HttpServletRequest request, HttpSession httpSession, Model model, ModelAndView modelAndView,
			RedirectAttributes redirectAttributes) throws Exception {
		logger.info("***** CompareConfigController.ClassifyFile Start ****");
		try {
			setupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
			ArrayList<CompareSetupBean> setupBeanslist = null;

			setupBeanslist = icompareConfigService.getFileList();

			model.addAttribute("configBeanlist", setupBeanslist);
			modelAndView.setViewName("classifyFile");
			modelAndView.addObject("CompareSetupBean", setupBean);
			logger.info("***** CompareConfigController.ClassifyFile End ****");

			return modelAndView;
		} catch (Exception ex) {
			demo.logSQLException(ex, "CompareConfigController.ClassifyFile");
			logger.error(" error in CompareConfigController.ClassifyFile",
					new Exception("CompareConfigController.ClassifyFile", ex));
			modelAndView.setViewName("Login");
			return modelAndView;
		}

	}

	@RequestMapping(value = "manualCompareFiles", method = RequestMethod.POST)

	public String CompareFile(@ModelAttribute("CompareSetupBean") CompareSetupBean setupBean,
			HttpServletRequest request, HttpSession httpSession, Model model, ModelAndView modelAndView,
			RedirectAttributes redirectAttributes) throws Exception {
		logger.info("***** CompareConfigController.CompareFile Start ****");

		logger.info(setupBean.getCompareFile1());
		logger.info(setupBean.getCompareFile2());
		logger.info(setupBean.getFileDate());
		logger.info(setupBean.getCompareLvl());

		boolean result = false;

		// boolean result = icompareConfigService.chkCompareFiles(setupBean);
		setupBean.setInFileId(setupBean.getCompareFile1());
		setupBean.setCategory("ONUS");
		String Knockoff_FLAG1 = icompareConfigService.chkFlag("Knockoff_FLAG", setupBean);
		String Upload_FLAG1 = icompareConfigService.chkFlag("Upload_FLAG", setupBean);
		String FILTER_FLAG1 = icompareConfigService.chkFlag("Upload_FLAG", setupBean);
		String COMAPRE_FLAG1 = icompareConfigService.chkFlag("COMAPRE_FLAG", setupBean);

		logger.info("Knockoff_FLAG1==" + Knockoff_FLAG1);
		logger.info("Upload_FLAG1==" + Upload_FLAG1);
		logger.info("FILTER_FLAG1==" + FILTER_FLAG1);
		logger.info("COMAPRE_FLAG1==" + COMAPRE_FLAG1);

		setupBean.setInFileId(setupBean.getCompareFile2());
		String Knockoff_FLAG2 = icompareConfigService.chkFlag("Knockoff_FLAG", setupBean);
		String Upload_FLAG2 = icompareConfigService.chkFlag("Upload_FLAG", setupBean);
		String FILTER_FLAG2 = icompareConfigService.chkFlag("FILTER_FLAG", setupBean);
		String COMAPRE_FLAG2 = icompareConfigService.chkFlag("COMAPRE_FLAG", setupBean);

		logger.info("Knockoff_FLAG2==" + Knockoff_FLAG2);
		logger.info("Upload_FLAG2==" + Upload_FLAG2);
		logger.info("FILTER_FLAG2==" + FILTER_FLAG2);
		logger.info("COMAPRE_FLAG2==" + COMAPRE_FLAG2);

		if (Knockoff_FLAG1.equalsIgnoreCase("Y") && Upload_FLAG1.equalsIgnoreCase("Y")
				&& FILTER_FLAG1.equalsIgnoreCase("Y") && COMAPRE_FLAG1.equalsIgnoreCase("N")
				&& Knockoff_FLAG2.equalsIgnoreCase("Y") && Upload_FLAG2.equalsIgnoreCase("Y")
				&& FILTER_FLAG2.equalsIgnoreCase("Y") && COMAPRE_FLAG2.equalsIgnoreCase("N")) {
			try {
				CompareBean compareBean = new CompareBean();
				compareBean.setStFile_date(setupBean.getFileDate());

				compareBean.setStEntryBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());
				logger.info("file date is " + compareBean.getStFile_date());
				List<String> table_list = new ArrayList<>();
				table_list.add("ONUS_SWITCH");// icompareConfigService.getTableName(setupBean.getCompareFile1())
				table_list.add("ONUS_CBS");// icompareConfigService.getTableName(setupBean.getCompareFile2())
				compareBean.setStFile_date(setupBean.getFileDate());
				int i = 1;// compareService.moveData(table_list,compareBean.getStFile_date());
				if (i == 1) {
					// compare logic
					// compareService.updateMatchedRecords(table_list,compareBean.getStFile_date());
					// logger.info("completed matching of records");
					// compareService.moveToRecon(table_list,compareBean.getStFile_date());
					// compareService.TTUMRecords(table_list,compareBean.getStFile_date());
					setupBean.setInFileId(setupBean.getCompareFile1());
					icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);
					setupBean.setInFileId(setupBean.getCompareFile2());
					icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);

					logger.info("Comparision Completed");
					redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Comparision Completed");

				}
				logger.info("***** CompareConfigController.CompareFile End ****");

			} catch (Exception ex) {
				demo.logSQLException(ex, "CompareConfigController.CompareFile");
				logger.error(" error in CompareConfigController.CompareFile",
						new Exception("CompareConfigController.CompareFile", ex));
				redirectAttributes.addFlashAttribute(ERROR_MSG, "Configuration already Exists.");
			}

		} else {
			logger.info("Files Are Not Uploaded and Configured For Respective Date");
			redirectAttributes.addFlashAttribute(ERROR_MSG,
					"Files Are Not Uploaded and Configured For Respective Date ");
		}
		logger.info("result==" + result);

		return "redirect:ManualCompare.do";

	}
	// GetUplodedFile.do

	@RequestMapping(value = "GetUplodedFile", method = RequestMethod.GET)
	public String compareData(@ModelAttribute("CompareSetupBean") CompareSetupBean setupBean,
			RedirectAttributes redirectAttributes, HttpSession httpsession, Model model, HttpSession httpSession)
			throws Exception {
		logger.info("***** CompareConfigController.compareData Start ****");
		try {
			setupBean.setCreatedBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());

			List<CompareSetupBean> setupBeans = icompareConfigService.getlastUploadDetails();

			model.addAttribute("setupBeans", setupBeans);

			logger.info("***** CompareConfigController.compareData End ****");
			return "UploadedFileDetails";

		} catch (Exception e) {
			demo.logSQLException(e, "CompareConfigController.compareData");
			logger.error(" error in CompareConfigController.compareData",
					new Exception("CompareConfigController.compareData", e));
			return "redirect:Login.do";
		}
	}

	@RequestMapping(value = "saveCompareSetup", method = RequestMethod.POST)
	public String SaveCompareDetails(@ModelAttribute("CompareSetupBean") CompareSetupBean setupBean, Model model,
			RedirectAttributes redirectAttributes, HttpSession httpSession) throws Exception {
		logger.info("***** CompareConfigController.SaveCompareDetails Start ****");
		try {
			boolean setupresult = icompareConfigService.chkMain_ReconSetupDetails(setupBean);
			boolean result = false;
			setupBean.setEntryBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());

			if (setupresult) {

				result = icompareConfigService.saveCompareDetails(setupBean);

				logger.info("***** CompareConfigController.SaveCompareDetails End ****");

			} else {
				logger.info("Setup already Configured");
				redirectAttributes.addFlashAttribute(ERROR_MSG, "Setup already Configured.");
				return "redirect:CompareSetup.do";

			}
			if (result) {
				logger.info("Configuration Completed Successfully");
				redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Configuration Completed Successfully.");

				return "redirect:CompareSetup.do";

			} else {
				logger.info("Error occured while inserting data");
				redirectAttributes.addFlashAttribute(ERROR_MSG, "Error occured while inserting data.");
				return "redirect:CompareSetup.do";
			}

		} catch (Exception e) {
			demo.logSQLException(e, "CompareConfigController.SaveCompareDetails");
			logger.error(" error in CompareConfigController.SaveCompareDetails",
					new Exception("CompareConfigController.SaveCompareDetails", e));
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return "redirect:Login.do";

		}

	}

	@RequestMapping(value = "saveManCompareSetup", method = RequestMethod.POST)
	public String SaveManualCompareDetails(@ModelAttribute("ManualCompBean") ManualCompareBean manualCompBean,
			Model model, RedirectAttributes redirectAttributes, HttpSession httpSession) throws Exception {
		logger.info("***** CompareConfigController.SaveManualCompareDetails Start ****");
		try {

			logger.info(manualCompBean);

			manualCompBean.setEntryBy(((LoginBean) httpSession.getAttribute("loginBean")).getUser_id());

			boolean result = icompareConfigService.saveManCompareDetails(manualCompBean);
			if (result) {
				logger.info("Manual Compare setup Saved Successfully");
				redirectAttributes.addFlashAttribute(SUCCESS_MSG, "Manual Compare setup Saved Successfully.");
			} else {
				logger.info("Error Occured While Saving Data");
				redirectAttributes.addFlashAttribute(ERROR_MSG, "Error Occured While Saving Data.");
			}

			logger.info("***** CompareConfigController.SaveManualCompareDetails End ****");
			return "redirect:ManualCompareSetup.do";

		} catch (Exception e) {
			demo.logSQLException(e, "CompareConfigController.SaveManualCompareDetails");
			logger.error(" error in CompareConfigController.SaveManualCompareDetails",
					new Exception("CompareConfigController.SaveManualCompareDetails", e));
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return "redirect:Login.do";

		}

	}

	@RequestMapping(value = "/getCompareFiles", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<CompareSetupBean> getCompareFiles(@RequestParam("type") String type,
			@RequestParam("subcat") String subcat, HttpSession httpSession, LoginBean loginBean,
			HttpServletRequest request) throws Exception {
		logger.info("***** CompareConfigController.getCompareFiles Start ****");
		loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
		logger.info("in getCompareFiles" + type);

		ArrayList<CompareSetupBean> filelist = icompareConfigService.getCompareFiles(type, subcat);

		logger.info("***** CompareConfigController.getCompareFiles End ****");
		return filelist;

	}

	@RequestMapping(value = "/ViewCompareDetls", method = RequestMethod.GET)

	public String ViewCompareDetls(@RequestParam("recId") int recId, @RequestParam("Cate") String Cate,
			LoginBean loginBean, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		logger.info("***** CompareConfigController.ViewCompareDetls Start ****");

		logger.info("in getCompareFiles" + recId);
		logger.info("in getCompareFiles" + Cate);

		if (Cate.equals("POS")) {
			Cate = "POS_ONUS";
		}
		try {
			loginBean.setUser_id(((LoginBean) request.getSession().getAttribute("loginBean")).getUser_id().trim());
			logger.info("ViewCompareDetls");

			ArrayList<CompareSetupBean> matchcrtlist = icompareConfigService.getmatchcrtlist(recId, Cate);
			ArrayList<CompareSetupBean> matchcondlist = icompareConfigService.getmatchcondnlist(recId, Cate);
			ArrayList<CompareSetupBean> recparamlist = icompareConfigService.getrecparamlist(recId, Cate);

			logger.info(matchcrtlist);
			logger.info(matchcondlist);
			logger.info(recparamlist);

			model.addAttribute("matchcrtlist", matchcrtlist);
			model.addAttribute("matchcondlist", matchcondlist);
			model.addAttribute("recparamlist", recparamlist);

			logger.info("***** CompareConfigController.ViewCompareDetls End ****");

			return "CompareDetails";
		} catch (Exception e) {
			logger.error(" error in CompareConfigController.ViewCompareDetls",
					new Exception("CompareConfigController.ViewCompareDetls", e));
			redirectAttributes.addFlashAttribute(ERROR_MSG, e.getMessage());
			return "redirect:Login.do";
		}

	}

}
