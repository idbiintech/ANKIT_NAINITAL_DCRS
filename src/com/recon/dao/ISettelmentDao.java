package com.recon.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.recon.model.Gl_bean;
import com.recon.model.Mastercard_chargeback;
import com.recon.model.Rupay_Gl_repo;
import com.recon.model.Rupay_gl_Lpcases;
import com.recon.model.Rupay_gl_autorev;
import com.recon.model.Rupay_sur_GlBean;
import com.recon.model.SettlementBean;
import com.recon.model.SettlementTypeBean;

public interface ISettelmentDao {
	
	
	public ArrayList<String> gettype(String tableName);
	public List<SettlementTypeBean> getSettlmentType(String type,String tablename);
	public ArrayList<SettlementTypeBean> getReconData(String tableName, String column, String date, String searchValue);
	public ArrayList<String> getColumnList(String tableName);
	public int getReconDataCount(String table, String type, String date, String searchValue);
	public ArrayList<SettlementTypeBean> getChngReconData(String table,
			String type, String date, String searchValue, int jtStartIndex,
			int jtPageSize);
	public void manualReconToSettlement(String table_name,String stFile_date)throws Exception;
	public int updateRecord(SettlementTypeBean settlementTypeBean);
	
	public List<List<String>> getReconData1(String stFileId,String dcrs_remarks,String date,String searchValue);
	public String getFileName(String stfileId);
	public Boolean checkfileprocessed(SettlementBean settlementbeanObj);
	public void generate_Reports(SettlementBean settlementBeanObj) throws Exception;
	public	String generate_Dhana_Reports(SettlementBean settlementBeanObj) throws Exception;
	
	public	String generate_Dhana_Reports_Knockoff(SettlementBean settlementBeanObj) throws Exception;
	
	public	String generate_Dhana_Reports_Failed(SettlementBean settlementBeanObj) throws Exception;
	public	String generate_Dhana_Reports_Matched(SettlementBean settlementBeanObj) throws Exception;
	
	public void DeleteFiles(String path);
	void generate_ipm(String outputString2) throws Exception;
	boolean generateCTF(SettlementBean settlementBean, List<String> files)
			throws IOException;
	
	public List<Mastercard_chargeback> getMastercardchargeback(String arnNo) throws Exception;
//	public void buildExcelDocument1(Map<String, Object> map, String stFilename,HttpServletRequest request,HttpServletResponse response) throws Exception;

	public int Savechargeback(String microfilm, String ref_id,
			String settlement_amount, String settlement_currency,
			String txn_amount, String txn_currency, String reason,
			String documentation,  String remarks) throws Exception;
	public List<List<Mastercard_chargeback>> GenerateReportChargebk()throws Exception;
	
	public String generateChargBk(String arn,String reason, String arn_date) throws Exception;

	public List<Gl_bean> getMastercardGet_glbalance(
			String filedate) throws Exception;
	
	public List<Rupay_sur_GlBean> getRupaysurchargelist(String filedate) throws Exception;	
	
	public List<Rupay_gl_autorev> getRupayAutorevlist(String filedate) throws Exception;
	public List<Rupay_gl_Lpcases> getRupayLpcaselist(String filedate) throws Exception;
	public int SaveGl(String closingbal, String settlementid, String diff,
			String cbs_unrecon, String switch_unrecon, String nobase2,
			String settlementmatch, String nxtdate, String surcharge1,
			String settlementTotal, String lpcase2, String val1,
			String surch_total, String val2,
			String autorev_total, String val3, String lpcasetotal,
			String finaltotal,String dateval) throws Exception;
	public List<List<Rupay_Gl_repo>> GenerateGL(String dateval) throws Exception;
	
	public String getSettlemntAmount(String settlementDate,
			String settlementAmount) throws Exception;
	

}
