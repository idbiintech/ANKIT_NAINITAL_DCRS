package com.recon.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.recon.model.CompareSetupBean;
import com.recon.model.RupaySettlementBean;
import com.recon.model.RupayUploadBean;
import com.recon.model.SingleRRN;

public interface RupaySettlementService {
	public HashMap<String, Object> uploadExcelFile(RupaySettlementBean beanObj, MultipartFile file) throws Exception;

	public HashMap<String, Object> validatePrevFileUpload(RupaySettlementBean beanObj);

	public void generateRupaySettlmentTTum(String settlementDate, HttpServletResponse response);

	boolean readFile(RupayUploadBean beanObj, MultipartFile file);

	boolean readIntFile(RupayUploadBean beanObj, MultipartFile file);

	boolean checkFileUploaded(RupayUploadBean beanObj);

	HashMap<String, Object> validateRawfiles(RupayUploadBean beanObj);

	HashMap<String, Object> validateSettlementFiles(RupayUploadBean beanObj);

	boolean processSettlement(RupayUploadBean beanObj);

	boolean validateSettlementProcess(RupayUploadBean beanObj);

	List<Object> getSettlementData(RupayUploadBean beanObj);

	public Boolean validateSettlementTTUM(RupayUploadBean beanObj);
	
	public Boolean SettlementTTUMRollback(RupayUploadBean beanObj);

	
	public Boolean validatePresentmentUpload(RupayUploadBean beanObj , MultipartFile file);
	
	public Boolean validateNfsIssUpload(CompareSetupBean setupBean, MultipartFile file);

	public List<SingleRRN>  upiReport(String rrnNo,String task) throws Exception; 

	
	

	public Boolean validateFileUpload(RupayUploadBean beanObj);

	public List<String> getSettlementTTUMData(RupayUploadBean beanObj);

	boolean processSettlementTTUM(RupayUploadBean beanObj);

	boolean validateSettlementDiff(RupayUploadBean beanObj);

	boolean processRectification(RupayUploadBean beanObj);

	HashMap<String, Object> validateDiffAmount(RupayUploadBean beanObj);

	boolean checkNCMCFileUploaded(RupayUploadBean beanObj);

	boolean readNCMCFile(RupayUploadBean beanObj, MultipartFile file);

	boolean settlementRollback(RupayUploadBean beanObj);

	boolean settlementFilesRollback(RupayUploadBean beanObj);

	public String uploadPresentmentFile(RupayUploadBean beanObj, MultipartFile file)
			throws IOException, SQLException, Exception;

	public Boolean validatePresentmentProcess(String filedate);

	public Boolean processPresentment(String filedate);

	public Boolean validateCashProcess(String filedate);

	public Boolean processCashAtPos(String filedate);

	public Boolean validateLateRev(String filedate);

	public Boolean processLateRev(String filedate);

	public boolean checkRecord(String filedate);
	
	public boolean checkCbsRecordPresent(String filedate);
	
	public Boolean processCbs(String filedate);
	
	public Boolean deleteCbs(String filedate);

}
