package com.recon.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.web.multipart.MultipartFile;

import com.recon.model.CompareSetupBean;
import com.recon.model.FileUploadView;
import com.recon.model.ManualCompareBean;
import com.recon.model.Pos_Bean;

public interface ICompareConfigDao {
	
	public List<CompareSetupBean> getFileDetails();
	
	public boolean saveCompareDetails(CompareSetupBean setupBean) throws Exception;
	
	public boolean chkMain_ReconSetupDetails (CompareSetupBean setupBean);
	
	public ArrayList<CompareSetupBean> getCompareFiles(String type,String subcat) throws Exception;
	
	public ArrayList<CompareSetupBean> getmatchcrtlist(int rec_set_id,String Cate) throws Exception;
	
	public ArrayList<CompareSetupBean> getmatchcondnlist(int rec_set_id,String Cate) throws Exception;
	
	public ArrayList<CompareSetupBean> getrecparamlist(int rec_set_id,String Cate) throws Exception;

	public  ArrayList<CompareSetupBean> getFileList();
	
	public boolean DeleteUploadedFiles(CompareSetupBean setupBean);

	public List<FileUploadView> viewUploadFileList(String filedate);
	
	public List<FileUploadView> viewCbsUploadFileList(String filedate);
	
	public boolean chkFileupload(CompareSetupBean setupBean) throws Exception;
	
	public boolean chkSwitchCbsFileupload(CompareSetupBean setupBean) throws Exception;
	
	public HashMap<String, Object> uploadFile(CompareSetupBean setupBean,MultipartFile file) throws Exception;
	
	public boolean validateFile(CompareSetupBean setupBean,MultipartFile file) throws Exception;

	public List<CompareSetupBean> getlastUploadDetails() throws Exception;

	public boolean chkCompareFiles(CompareSetupBean setupBean) throws Exception;

	public String getTableName(int Fileid) throws Exception;

	public String chkFlag(String flag, CompareSetupBean setupBean) throws Exception;

	public boolean updateFlag(String flag, CompareSetupBean setupBean) throws Exception;
	
	public boolean validate_File(String Filedate,CompareSetupBean setupBean) throws Exception;

	public boolean saveManCompareDetails(ManualCompareBean manualCompareBean ) throws Exception;

	public List<CompareSetupBean> getFileList(String category);
	
	public boolean CheckAlreadyProcessed(CompareSetupBean setupBean) throws Exception;
	
	public String chkUploadFlag(String flag, CompareSetupBean setupBean);
	
	public boolean chkBeforeUploadFile(String flag, CompareSetupBean setupBean) throws Exception;

	public boolean insertFileTranDate(CompareSetupBean setupBean);
	
	public boolean insertUploadTRAN (CompareSetupBean setupBean);
	
	
	public int getrecordcount(CompareSetupBean setupBean);


	public HashMap<String, Object> uploadREV_File(CompareSetupBean setupBean, MultipartFile file);

	public int getREVrecordcount(CompareSetupBean setupBean);

	public  ArrayList<Pos_Bean> getFileNameList(String filedate) throws Exception;
	
	public HashMap<String , Object>  checkUploadedFileName(String category, String subCateg, String fileName);
	
	public HashMap<String , Object>  checkUploadedCycle(String fileDate, String subCateg, String fileName, String phyFileName);




	
}
