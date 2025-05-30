package com.recon.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.recon.dao.ISettelmentDao;
import com.recon.model.Gl_bean;
import com.recon.model.Mastercard_chargeback;
import com.recon.model.Rupay_Gl_repo;
import com.recon.model.Rupay_gl_Lpcases;
import com.recon.model.Rupay_gl_autorev;
import com.recon.model.Rupay_sur_GlBean;
import com.recon.model.SettlementBean;
import com.recon.model.SettlementTypeBean;
import com.recon.service.ISettelmentService;

@Component
public class SettlementServImpl implements ISettelmentService {

	@Autowired
	ISettelmentDao settlementdao;

	@Override
	public ArrayList<String> gettype(String tableName) {
		// TODO Auto-generated method stub
		return settlementdao.gettype(tableName);
	}

	@Override
	public ArrayList<SettlementTypeBean> getReconData(String tableName, String type, String date, String searchValue) {
		// TODO Auto-generated method stub
		return settlementdao.getReconData(tableName, type, date, searchValue);
	}

	@Override
	public List<SettlementTypeBean> getSettlmentType(String type, String tablename) {
		// TODO Auto-generated method stub
		return settlementdao.getSettlmentType(type, tablename);
	}

	@Override
	public ArrayList<String> getColumnList(String tableName) {
		// TODO Auto-generated method stub
		return settlementdao.getColumnList(tableName);
	}

	@Override
	public int getReconDataCount(String table, String type, String date, String searchValue) {
		// TODO Auto-generated method stub
		return settlementdao.getReconDataCount(table, type, date, searchValue);
	}

	@Override
	public ArrayList<SettlementTypeBean> getChngReconData(String trim, String trim2, String trim3, String trim4,
			int jtStartIndex, int jtPageSize) {
		// TODO Auto-generated method stub
		return settlementdao.getChngReconData(trim, trim2, trim3, trim4, jtStartIndex, jtPageSize);
	}

	@Override
	public void manualReconToSettlement(String table_name, String stFile_date) throws Exception {
		// TODO Auto-generated method stub
		settlementdao.manualReconToSettlement(table_name, stFile_date);

	}

	@Override
	public int updateRecord(SettlementTypeBean settlementTypeBean) {
		// TODO Auto-generated method stub
		return settlementdao.updateRecord(settlementTypeBean);
	}

	@Override
	public List<List<String>> getReconData1(String stFileId, String dcrs_remarks, String date, String searchValue) {
		return settlementdao.getReconData1(stFileId, dcrs_remarks, date, searchValue);
	}

	@Override
	public String getFileName(String stfileId) {
		return settlementdao.getFileName(stfileId);
	}

	// @Override
	/*
	 * public void buildExcelDocument1(Map<String, Object> map, String
	 * stFilename,HttpServletRequest request,HttpServletResponse response) throws
	 * Exception { settlementdao.buildExcelDocument1(map,
	 * stFilename,request,response); }
	 */
	@Override
	public void generate_Reports(SettlementBean settlementBeanObj) throws Exception {
		settlementdao.generate_Reports(settlementBeanObj);
	}

	@Override
	public String generate_Dhana_Reports(SettlementBean settlementBeanObj) throws Exception {
		return settlementdao.generate_Dhana_Reports(settlementBeanObj);
	}
	
	@Override
	public String generate_Dhana_Reports_Knockoff(SettlementBean settlementBeanObj) throws Exception {
		return settlementdao.generate_Dhana_Reports_Knockoff(settlementBeanObj);
	}
	
	@Override
	public String generate_Dhana_Reports_Matched(SettlementBean settlementBeanObj) throws Exception {
		return settlementdao.generate_Dhana_Reports_Matched(settlementBeanObj);
	}
	
	@Override
	public String generate_Dhana_Reports_Failed(SettlementBean settlementBeanObj) throws Exception {
		return settlementdao.generate_Dhana_Reports_Failed(settlementBeanObj);
	}

	public Boolean checkfileprocessed(SettlementBean settlementbeanObj) {
		return settlementdao.checkfileprocessed(settlementbeanObj);
	}

	public void DeleteFiles(String path) {
		settlementdao.DeleteFiles(path);
	}

	@Override
	public boolean generateCTF(SettlementBean settlementBean, List<String> files) throws IOException {
		// TODO Auto-generated method stub
		return settlementdao.generateCTF(settlementBean, files);
	}

	@Override
	public List<Mastercard_chargeback> getMastercardchargeback(String arnNo) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getMastercardchargeback(arnNo);
	}

	@Override
	public int Savechargeback(String microfilm, String ref_id, String settlement_amount, String settlement_currency,
			String txn_amount, String txn_currency, String reason, String documentation, String remarks)
			throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.Savechargeback(microfilm, ref_id, settlement_amount, settlement_currency, txn_amount,
				txn_currency, reason, documentation, remarks);
	}

	@Override
	public List<List<Mastercard_chargeback>> GenerateReportChargebk() throws Exception {

		return settlementdao.GenerateReportChargebk();
	}

	@Override
	public String generateChargBk(String arn, String reason, String arn_date) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.generateChargBk(arn, reason, arn_date);
	}

	@Override
	public List<Gl_bean> getMastercardGet_glbalance(String filedate) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getMastercardGet_glbalance(filedate);
	}

	@Override
	public List<Rupay_sur_GlBean> getRupaysurchargelist(String filedate) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getRupaysurchargelist(filedate);
	}

	@Override
	public List<Rupay_gl_autorev> getRupayAutorevlist(String filedate) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getRupayAutorevlist(filedate);
	}

	@Override
	public List<Rupay_gl_Lpcases> getRupayLpcaselist(String filedate) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getRupayLpcaselist(filedate);
	}

	@Override
	public int SaveGl(String closingbal, String settlementid, String diff, String cbs_unrecon, String switch_unrecon,
			String nobase2, String settlementmatch, String nxtdate, String surcharge1, String settlementTotal,
			String lpcase2, String val1, String surch_total, String val2, String autorev_total, String val3,
			String lpcasetotal, String finaltotal, String dateval) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.SaveGl(closingbal, settlementid, diff, cbs_unrecon, switch_unrecon, nobase2,
				settlementmatch, nxtdate, surcharge1, settlementTotal, lpcase2, val1, surch_total, val2, autorev_total,
				val3, lpcasetotal, finaltotal, dateval);
	}

	@Override
	public List<List<Rupay_Gl_repo>> GenerateGL(String dateval) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.GenerateGL(dateval);
	}

	@Override
	public String getSettlemntAmount(String settlementDate, String settlementAmount) throws Exception {
		// TODO Auto-generated method stub
		return settlementdao.getSettlemntAmount(settlementDate, settlementAmount);
	}
}
