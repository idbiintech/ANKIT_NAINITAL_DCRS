package com.recon.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.recon.dao.IReconProcessDao;
import com.recon.model.CompareBean;
import com.recon.model.CompareSetupBean;
import com.recon.model.FilterationBean;
import com.recon.model.KnockOffBean;
import com.recon.service.CompareRupayService;
import com.recon.service.CompareService;
import com.recon.service.ICompareConfigService;
import com.recon.util.GeneralUtil;
import com.recon.util.demo;

@Component
public class ReconProcessDaoImpl extends JdbcDaoSupport implements IReconProcessDao {

	@Autowired
	CompareService compareService;

	@Autowired
	GeneralUtil generalUtil;

	@Autowired
	CompareRupayService compareRupayService;

	@Autowired
	ICompareConfigService icompareConfigService;

	private static final Logger logger = Logger.getLogger(ReconProcessDaoImpl.class);

	/*
	 * @Override public String chkFileUpload(String Category, String filedate,
	 * List<CompareSetupBean> compareSetupBeans, String subCat) throws Exception {
	 * logger.info("***** ReconProcessDaoImpl.chkFileUpload Start ****"); String msg
	 * = null, flg, compareflag; try { for (CompareSetupBean setupBean :
	 * compareSetupBeans) {
	 * 
	 * if (!(setupBean.getStFileName().equalsIgnoreCase("REV_REPORT"))) {
	 * 
	 * String query =
	 * "SELECT UPLOAD_FLAG FROM MAIN_FILE_UPLOAD_DTLS WHERE filedate = '" + filedate
	 * + "' " + " AND FileId = " + setupBean.getInFileId();
	 * 
	 * query = " SELECT CASE WHEN exists (" + query + ") then (" + query +
	 * ") else 'N' end as FLAG from dual";
	 * 
	 * logger.info("Query==" + query); flg = getJdbcTemplate().queryForObject(query,
	 * String.class);
	 * 
	 * query = "";
	 * 
	 * query = "SELECT COMAPRE_FLAG FROM MAIN_FILE_UPLOAD_DTLS WHERE filedate = '" +
	 * filedate + "' " + " AND FileId = " + setupBean.getInFileId();
	 * 
	 * query = " SELECT CASE WHEN exists (" + query + ") then (" + query +
	 * ") else 'N' end as FLAG from dual";
	 * 
	 * compareflag = getJdbcTemplate().queryForObject(query, String.class);
	 * 
	 * if (compareflag.equalsIgnoreCase("N")) { if (flg.equalsIgnoreCase("N")) {
	 * 
	 * msg = "Files are Not Uploaded."; logger.info("msg==" + msg); return msg; }
	 * else // CHANGES MADE BY INT5779 AS ON 03 MARCH TO // check whether manual
	 * file is uploaded for CBS // file { if
	 * (setupBean.getStFileName().equalsIgnoreCase( "CBS")) {
	 * 
	 * String MANUAL_FILE_CHECK =
	 * "SELECT MANUPLOAD_FLAG FROM MAIN_FILE_UPLOAD_DTLS WHERE filedate =  '" +
	 * filedate + "' " + "AND FileId = " + setupBean.getInFileId();
	 * 
	 * MANUAL_FILE_CHECK = " SELECT CASE WHEN exists (" + MANUAL_FILE_CHECK +
	 * ") then (" + MANUAL_FILE_CHECK + ") else 'N' end as FLAG from dual";
	 * logger.info("MANUAL_FILE_CHECK== " + MANUAL_FILE_CHECK);
	 * 
	 * flg = getJdbcTemplate().queryForObject( MANUAL_FILE_CHECK, String.class); if
	 * (setupBean.getInFileId() == 39) { flg = "Y"; } if (flg.equalsIgnoreCase("N"))
	 * { msg = "Manual File is not uploaded"; logger.info("msg==" + msg); return
	 * msg; } }
	 * 
	 * } } else {
	 * 
	 * msg = "Files are Already Processed."; logger.info("msg==" + msg); return msg;
	 * 
	 * }
	 * 
	 * } } logger.info("***** ReconProcessDaoImpl.chkFileUpload End ****");
	 * 
	 * } catch (Exception ex) {
	 * logger.error(" error in ReconProcessDaoImpl.chkFileUpload", new
	 * Exception("ReconProcessDaoImpl.chkFileUpload", ex)); throw ex; }
	 * 
	 * return msg;
	 * 
	 * }
	 */
// METHOD MODIFIED BY INT8624 for file upload proper validation	
	@Override
	public String chkFileUpload(String Category, String filedate, List<CompareSetupBean> compareSetupBeans,
			String subCat) throws Exception {
		logger.info("***** ReconProcessDaoImpl.chkFileUpload Start **");
		String msg = null;
		try {
			for (CompareSetupBean setupBean : compareSetupBeans) {

				String getMainFileCount = "select file_count from main_filesource where fileid = '"
						+ setupBean.getInFileId() + "'";

				int fileCount = getJdbcTemplate().queryForObject(getMainFileCount, new Object[] {}, Integer.class);

				if (!setupBean.getStFileName().equalsIgnoreCase("RUPAY")) {
					getMainFileCount = "select count(1) from main_file_upload_dtls where filedate = to_date('"
							+ filedate + "','dd/mm/yyyy') and fileid = '" + setupBean.getInFileId()
							+ "' and file_count = '" + fileCount + "'";

					logger.info("getMainFileCount " + getMainFileCount);

					int uploadCount = getJdbcTemplate().queryForObject(getMainFileCount, new Object[] {},
							Integer.class);

					if (uploadCount == 0) {
						if (msg == null)
							msg = setupBean.getStFileName();
						else
							msg = msg + "," + setupBean.getStFileName();
					}
				}

			}
			if (msg != null) {
				msg = msg + " Files are not uploaded ";
			}
			logger.info("***** ReconProcessDaoImpl.chkFileUpload End ****");

		} catch (Exception ex) {
			logger.error(" error in ReconProcessDaoImpl.chkFileUpload",
					new Exception("ReconProcessDaoImpl.chkFileUpload", ex));
			throw ex;
		}

		return msg;

	}

	/*
	 * @Override public List<CompareSetupBean> getFileList(String category, String
	 * filedate ,String subcat) {
	 * 
	 * try { List<CompareSetupBean>compareSetupBeans = new
	 * ArrayList<CompareSetupBean>(); String query = ""; String stSubCate = "";
	 * if(!subcat.equals("-")) { //"'"+subcat+"'"; stSubCate = ""; query =
	 * "SELECT distinct regexp_substr(FILE1_CATEGORY,'[^_]+'," +(category.length()
	 * +1)+") AS SUBCATEGORIES FROM MAIN_RECON_SEQUENCE  WHERE RECON_CATEGORY =?" ;
	 * 
	 * List<String> stSub_Category = getJdbcTemplate().query(query , new Object[]
	 * {category+"_"+subcat} ,new AllSubCategories()); for(int i = 0
	 * ;i<stSub_Category.size() ; i++) { if(i>0) stSubCate =
	 * stSubCate+",'"+stSub_Category.get(i)+"'"; else stSubCate =
	 * "'"+stSub_Category.get(i)+"'";
	 * 
	 * }
	 * 
	 * query = "SELECT distinct regexp_substr(FILE2_CATEGORY,'[^_]+',"+(category.
	 * length()
	 * +1)+") AS SUBCATEGORIES FROM MAIN_RECON_SEQUENCE  WHERE RECON_CATEGORY =?" +
	 * " AND regexp_substr(FILE2_CATEGORY,'[^_]+',6) NOT IN("+stSubCate+")";
	 * 
	 * stSub_Category = getJdbcTemplate().query(query , new Object[]
	 * {category+"_"+subcat} ,new AllSubCategories()); for(int i = 0
	 * ;i<stSub_Category.size() ; i++) { stSubCate =
	 * stSubCate+",'"+stSub_Category.get(i)+"'";
	 * 
	 * } } else { stSubCate = "'"+subcat+"'"; }
	 * 
	 * 
	 * query =
	 * "Select FileId as inFileId , Filename as stFileName,FILTERATION as filter_Flag,KNOCKOFF as knockoff_Flag,FILE_SUBCATEGORY AS stSubCategory "
	 * + "FROM MAIN_FILESOURCE WHERE FILE_CATEGORY = '"+category+
	 * "' and FILE_SUBCATEGORY in ("+stSubCate+")" +
	 * " order by (case  when stFileName = 'SWITCH' then 1 when stFileName ='CBS' then 2  end) ASC"
	 * ;
	 * 
	 * 
	 * compareSetupBeans = getJdbcTemplate().query(query,new
	 * BeanPropertyRowMapper(CompareSetupBean.class));
	 * 
	 * 
	 * return compareSetupBeans; }catch(Exception ex) {
	 * 
	 * ex.printStackTrace(); return null; } }
	 */

	/*
	 * @Override public List<CompareSetupBean> getFileList(String category, String
	 * filedate, String subcat) throws Exception {
	 * 
	 * try { logger.info("***** ReconProcessDaoImpl.getFileList Start ****");
	 * List<CompareSetupBean> compareSetupBeans = new ArrayList<CompareSetupBean>();
	 * String query = ""; String stSubCate = ""; if (!subcat.equals("-")) { //
	 * "'"+subcat+"'"; stSubCate = ""; query =
	 * "SELECT distinct regexp_substr(FILE1_CATEGORY,'[^_]+'," + (category.length()
	 * + 1) +
	 * ") AS SUBCATEGORIES FROM MAIN_RECON_SEQUENCE  WHERE RECON_CATEGORY =?";
	 * 
	 * List<String> stSub_Category = getJdbcTemplate().query(query, new Object[] {
	 * category + "_" + subcat }, new AllSubCategories()); for (int i = 0; i <
	 * stSub_Category.size(); i++) { if (i > 0) stSubCate = stSubCate + ",'" +
	 * stSub_Category.get(i) + "'"; else stSubCate = "'" + stSub_Category.get(i) +
	 * "'";
	 * 
	 * }
	 * 
	 * query = "SELECT distinct LTRIM(regexp_substr(FILE2_CATEGORY,'[^-]+'," +
	 * (category.length() + 1) +
	 * "),'_') AS SUBCATEGORIES FROM MAIN_RECON_SEQUENCE  WHERE RECON_CATEGORY =?" +
	 * " AND regexp_substr(FILE2_CATEGORY,'[^-]+',6) NOT IN(" + stSubCate + ")";
	 * 
	 * stSub_Category = getJdbcTemplate().query(query, new Object[] { category + "_"
	 * + subcat }, new AllSubCategories()); for (int i = 0; i <
	 * stSub_Category.size(); i++) { stSubCate = stSubCate + ",'" +
	 * stSub_Category.get(i) + "'";
	 * 
	 * } } else { stSubCate = "'" + subcat + "'"; }
	 * 
	 * query =
	 * "Select FileId as inFileId , Filename as stFileName,FILTERATION as filter_Flag,KNOCKOFF as knockoff_Flag,FILE_SUBCATEGORY AS stSubCategory "
	 * + "FROM MAIN_FILESOURCE WHERE FILE_CATEGORY = '" + category +
	 * "' and FILE_SUBCATEGORY in (" + stSubCate + ")" +
	 * " order by (case  when stFileName = 'SWITCH' then 1 when stFileName ='CBS' then 2  end) ASC"
	 * ;
	 * 
	 * logger.info("FILE ID== " + query);
	 * 
	 * 
	 * 
	 * compareSetupBeans = getJdbcTemplate().query(query, new
	 * BeanPropertyRowMapper(CompareSetupBean.class));
	 * 
	 * return compareSetupBeans; } catch (Exception ex) { demo.logSQLException(ex,
	 * "ReconProcessDaoImpl.getFileList");
	 * logger.error(" error in ReconProcessDaoImpl.getFileList", new
	 * Exception("ReconProcessDaoImpl.getFileList", ex)); // ex.printStackTrace();
	 * return null; } }
	 */

	/**** simplified code by int8624 ***/
	@Override
	public List<CompareSetupBean> getFileList(String category, String filedate, String subcat) throws Exception {
		List<CompareSetupBean> compareSetupBeans = new ArrayList<CompareSetupBean>();
		try {
			String query = "select fileid as infileid , filename as stfilename,filteration as filter_flag,knockoff as knockoff_flag,file_subcategory as stsubcategory "
					+ "from main_filesource where file_category = '" + category + "' and file_subcategory in ('"
					+ subcat + "')"
					+ " order by (case  when stfilename = 'SWITCH' then 1 when stfilename ='CBS' then 2  end) asc";

			logger.info("FILE ID== " + query);

			compareSetupBeans = getJdbcTemplate().query(query, new BeanPropertyRowMapper(CompareSetupBean.class));
		} catch (Exception ex) {
			demo.logSQLException(ex, "ReconProcessDaoImpl.getFileList");
			logger.error(" error in ReconProcessDaoImpl.getFileList",
					new Exception("ReconProcessDaoImpl.getFileList", ex));
			// ex.printStackTrace();
			return null;
		}
		return compareSetupBeans;
	}

	// added by int5779
	private static class AllSubCategories implements RowMapper<String> {

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			String stSubCat = rs.getString("SUBCATEGORIES");

			return stSubCat;

		}
	}

	/*
	 * @Override public String validateFile(String category, List<CompareSetupBean>
	 * compareSetupBeans, String filedate) throws Exception {
	 * logger.info("***** ReconProcessDaoImpl.validateFile Start ****"); String msg
	 * = null; int count = 0; try {
	 * 
	 * 
	 * for (CompareSetupBean setupBean : compareSetupBeans) {
	 * 
	 * // get FILTER, KNOCKOFF FLAGS String GET_FLAGS =
	 * "SELECT FILTERATION FROM MAIN_FILESOURCE WHERE FILEID = ?";
	 * 
	 * String stFliter_Flag = getJdbcTemplate().queryForObject( GET_FLAGS, new
	 * Object[] { setupBean.getInFileId() }, String.class);
	 * 
	 * GET_FLAGS = "SELECT KNOCKOFF FROM MAIN_FILESOURCE WHERE FILEID = ?";
	 * 
	 * String stKnockoff_Flag = getJdbcTemplate().queryForObject( GET_FLAGS, new
	 * Object[] { setupBean.getInFileId() }, String.class);
	 * 
	 * String stCompare_Flag = "Y";
	 * 
	 * if (setupBean.getStFileName().equalsIgnoreCase("REV_REPORT")) {
	 * 
	 * stCompare_Flag = "N"; }
	 * 
	 * String tablename = getJdbcTemplate().queryForObject(
	 * "select tablename from MAIN_FILESOURCE where FILEID = " +
	 * setupBean.getInFileId(), String.class); String chkData = null; // Get table
	 * name from main table if (category.equals("CARDTOCARD")) { chkData =
	 * "select count(*) from CARD_TO_CARD_CBS_RAWDATA " +
	 * " where TO_CHAR(to_date(CREATEDDATE,'dd/MM/YY'),'dd-MON-YY') < TO_CHAR(sysdate,'DD/MM/YYYY')"
	 * ; } else if (category.equals("MASTERCARD")) { if
	 * (setupBean.getStFileName().equalsIgnoreCase("CBS") ||
	 * setupBean.getStFileName().equalsIgnoreCase( "SWITCH")) { chkData =
	 * "select count(*) from " + tablename +
	 * " where TO_CHAR(CREATEDDATE,'DD/MM/YYYY') < TO_CHAR(sysdate,'DD/MM/YYYY')"; }
	 * else { chkData = "select count(*) from " + tablename +
	 * " where TO_CHAR(FILEDATE,'DD/MM/YYYY') < TO_CHAR(sysdate,'DD/MM/YYYY')"; } }
	 * 
	 * else if (category.equals("RUPAY")) {
	 * 
	 * // insertDailyRawData(filedate);
	 * 
	 * chkData = "select count(*) from " + tablename +
	 * " where TO_CHAR(CREATEDDATE,'DD/MM/YYYY') < TO_CHAR(sysdate,'DD/MM/YYYY')"; }
	 * 
	 * else {
	 * 
	 * chkData = "select count(*) from " + tablename +
	 * " where TO_CHAR(CREATEDDATE,'DD/MM/YYYY') < TO_CHAR(sysdate,'DD/MM/YYYY')";
	 * 
	 * } logger.info("chkData == " + chkData);
	 * 
	 * // CHECKING WHETHER ANY DATA IS PRESENT int dataCount =
	 * getJdbcTemplate().queryForObject(chkData, Integer.class); String query = "";
	 * if (dataCount > 0) {
	 * 
	 * // CHECKING RECORDS FOR PREVIOUS DAY // IF FILE IS SWITCH OR CBS THEN CHECK
	 * MANUAL FILE FLAG TOO if (setupBean.getStFileName().equalsIgnoreCase("CBS") ||
	 * setupBean.getStFileName().equalsIgnoreCase( "SWITCH")) { query =
	 * "SELECT count (*) FROM MAIN_FILE_UPLOAD_DTLS WHERE filedate = (TRUNC (TO_DATE ('"
	 * + filedate + "', 'dd/mm/yyyy') - 1) ) " + "	AND Fileid =" +
	 * setupBean.getInFileId() + " AND category='" + category + "' " +
	 * " AND FILTER_FLAG= ? AND KNOCKOFF_FLAG=? AND COMAPRE_FLAG='Y' " +
	 * " AND UPLOAD_FLAG='Y' AND MANUALCOMPARE_FLAG = 'Y'  ";
	 * 
	 * } else { query =
	 * "SELECT count (*) FROM MAIN_FILE_UPLOAD_DTLS WHERE filedate = (TRUNC (TO_DATE ('"
	 * + filedate + "', 'dd/mm/yyyy') - 1) ) " + "	AND Fileid =" +
	 * setupBean.getInFileId() + " AND category='" + category + "' " +
	 * " AND FILTER_FLAG= ? AND KNOCKOFF_FLAG=? AND COMAPRE_FLAG='" + stCompare_Flag
	 * + "' " + " AND UPLOAD_FLAG='Y'  ";
	 * 
	 * } logger.info("query==" + query); if
	 * (!setupBean.getStFileName().equalsIgnoreCase( "REV_REPORT")) { count =
	 * getJdbcTemplate() .queryForObject( query, new Object[] { stFliter_Flag,
	 * stKnockoff_Flag }, Integer.class); if (count > 0) {
	 * 
	 * // Previous File Processed.
	 * 
	 * } else { msg = msg + "Previous File not Processed."; logger.info("msg==" +
	 * msg);
	 * 
	 * } } }
	 * 
	 * } logger.info("***** ReconProcessDaoImpl.validateFile End ****"); return msg;
	 * 
	 * } catch (Exception ex) { demo.logSQLException(ex,
	 * "ReconProcessDaoImpl.validateFile");
	 * logger.error(" error in ReconProcessDaoImpl.validateFile", new
	 * Exception("ReconProcessDaoImpl.validateFile", ex)); return msg; }
	 * 
	 * }
	 */

	/***** SIMPLIFIED CODING BY INT8624 *****/
	public String validateFile(String category, List<CompareSetupBean> compareSetupBeans, String filedate)
			throws Exception {
		logger.info("***** ReconProcessDaoImpl.validateFile Start ****");
		String msg = null;
		try {
			// check whether recon is already processed
			String error_msg = checkReconAlreadyProcessed(category, compareSetupBeans, filedate);

			if (error_msg == null) {
				String checkFirstTime = "select count(*) from main_file_upload_dtls where filedate < to_date(?,'dd/mm/yyyy')";
				int prevCount = getJdbcTemplate().queryForObject(checkFirstTime, new Object[] { filedate },
						Integer.class);

				if (prevCount == 0) {
					return null;
				} else {
					for (CompareSetupBean setupBean : compareSetupBeans) {

						String query = "select count(*) from main_file_upload_dtls where filedate  = to_date('"
								+ filedate + "','dd/mm/yyyy')-1" + " and fileid = '" + setupBean.getInFileId()
								+ "' and filter_flag = (select filteration from main_filesource where fileid = '"
								+ setupBean.getInFileId() + "')"
								+ " and knockoff_flag = (select knockoff from main_filesource where fileid = '"
								+ setupBean.getInFileId() + "') " + " and comapre_flag = 'Y' ";

						logger.info("Query is " + query);

						int count = getJdbcTemplate().queryForObject(query, new Object[] {}, Integer.class);
						logger.info("Count is " + count);

						if (count == 0) {
							msg = "Previous File is not Processed";
							return msg;

						}
					}
				}
			} else {
				return error_msg;
			}
		} catch (Exception ex) {
			demo.logSQLException(ex, "ReconProcessDaoImpl.validateFile");
			logger.error(" error in ReconProcessDaoImpl.validateFile",
					new Exception("ReconProcessDaoImpl.validateFile", ex));
		}
		return msg;

	}

	public String checkReconAlreadyProcessed(String category, List<CompareSetupBean> compareSetupBeans,
			String filedate) {
		String msg = null;
		try {
			for (CompareSetupBean setupBean : compareSetupBeans) {
				
				String getcompareFlag = "select comapre_flag from main_file_upload_dtls where filedate = to_date('"
						+ filedate + "','dd/mm/yyyy') and fileid = '" + setupBean.getInFileId() + "'";

				System.out.println("here the checking query is"+getcompareFlag);
				String compareFlag = getJdbcTemplate().queryForObject(getcompareFlag, new Object[] {}, String.class);

				if (compareFlag.equals("Y")) {
					msg = "Recon is already processed";
					return msg;
				}
			}
		} catch (Exception e) {
			logger.info("Exception while checking recon process " + e);
			msg = "Exception while checcking recon processed or not ";
		}
		return msg;
	}

	@Override
	public boolean processFile(String category, List<CompareSetupBean> compareSetupBeans, String filedate,
			String Createdby, String subcat) throws Exception {

		String monthdate = generalUtil.DateFunction(filedate);

		boolean result = false;
		String StMerger_Category = "";
		String stCategory = category;

		logger.info("***** ReconProcessDaoImpl.processFile Start ****");

		try {
			if (category.equalsIgnoreCase("RUPAY")) {
				if (subcat.equalsIgnoreCase("DOMESTIC")) {
					logger.info("Inside rupay Domestic classification");
					String getCount = "select count(1) from main_file_upload_dtls a where fileid in ( select fileid from main_filesource where file_category = 'RUPAY' and file_subcategory = 'DOMESTIC') "
							+ " AND filedate = to_date(?,'dd/mm/yyyy') and filter_flag != (select filteration FROM main_filesource b where a.fileid = b.fileid)";

//					int pendingClass = getJdbcTemplate().queryForObject(getCount, new Object[] {filedate},Integer.class);
					int pendingClass = getJdbcTemplate().queryForObject(getCount, new Object[] { monthdate },
							Integer.class);

					if (pendingClass > 0)
						DomesticClassifydata(category, subcat, filedate, Createdby);
				} else if (subcat.equalsIgnoreCase("INTERNATIONAL")) {
					logger.info("Inside rupay International classification");
					String getCount = "select count(*) from main_file_upload_dtls a where fileid in ( SELECT FILEID FROM MAIN_FILESOURCE WHERE FILE_CATEGORY = 'RUPAY' AND FILE_SUBCATEGORY = 'INTERNATIONAL') "
							+ " AND FILEDATE = ? and FILTER_FLAG != (select FILTERATION FROM main_filesource b where a.fileid = b.fileid)";

					int pendingClass = getJdbcTemplate().queryForObject(getCount, new Object[] { filedate },
							Integer.class);

					if (pendingClass > 0)
						InternationalClassifydata(category, subcat, filedate, Createdby);
				}
			} else if (category.equalsIgnoreCase("VISA")) {

				if (subcat.equalsIgnoreCase("ISSUER")) {
					logger.info("Inside VISA ISSUER classification");
					String getCount = "select count(1) from main_file_upload_dtls a where fileid in ( select fileid from main_filesource where file_category = 'VISA' and file_subcategory = 'ISSUER') "
							+ " AND filedate = to_date(?,'dd/mm/yyyy') and filter_flag != (select filteration FROM main_filesource b where a.fileid = b.fileid)";

					int pendingClass = getJdbcTemplate().queryForObject(getCount, new Object[] { filedate },
							Integer.class);

					if (pendingClass > 0)
						VisaIssClassifydata(category, subcat, filedate, Createdby);
				} else if (subcat.equalsIgnoreCase("ACQUIRER")) {
					logger.info("Inside VISA ACQUIRER classification");
					String getCount = "select count(*) from main_file_upload_dtls a where fileid in ( select fileid from main_filesource where file_category  = 'VISA' AND FILE_SUBCATEGORY = 'ACQUIRER') "
							+ " AND filedate = to_date(?,'dd/mm/yyyy') and filter_flag != (select filteration FROM main_filesource b where a.fileid = b.fileid)";

					int pendingClass = getJdbcTemplate().queryForObject(getCount, new Object[] { filedate },
							Integer.class);

					if (pendingClass > 0)
						VisaAcqClassifydata(category, subcat, filedate, Createdby);
				}

			} else {

				if (category.equals("NFS")) {
					logger.info("******** In NFS ***********");
					if (subcat.equals("ISSUER")) {
						logger.info("******** In NFS - ISSUER ***********");
						ISSClassifydata(category, subcat, filedate, Createdby);

					} else if (subcat.equals("ACQUIRER")) {
						logger.info("******** In NFS - ACQUIRER ***********");
						AcqClassifydata(category, subcat, filedate, Createdby);

					}
				} else if (category.equals("CASHNET")) {
					logger.info("******** In CASHNET ***********");
					if (subcat.equals("ISSUER")) {
						logger.info("******** In CASHNET - ISSUER ***********");
						cashnetISSClassifydata(category, subcat, filedate, Createdby);

					} else if (subcat.equals("ACQUIRER")) {
						logger.info("******** In CASHNET - ACQUIRER ***********");
						cashnetAcqClassifydata(category, subcat, filedate, Createdby);

					}

				}

			}

			CompareSetupBean setupBean = chkStatus(compareSetupBeans, category, filedate);

			logger.info("knockoff" + setupBean.getKnockoff_Flag() + "filter" + setupBean.getFilter_Flag());

			if (setupBean.getKnockoff_Flag().equals("Completed") && setupBean.getFilter_Flag().equals("Completed")) {

				result = true;

			} else {

				result = false;
			}
			logger.info("result==" + result);
			logger.info("***** ReconProcessDaoImpl.processFile End ****");

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.processFile");
			logger.error(" error in ReconProcessDaoImpl.processFile", e);
			result = false;
		}

		return result;
	}

	private static class recSetIDMapper implements RowMapper<Integer> {

		@Override
		public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {

			int rec_id_list = (rs.getInt("REC_SET_ID"));

			return rec_id_list;

		}
	}

	@Override
	public boolean compareFiles(String category, String filedate, CompareBean compareBean, String subcat,
			String dollar_val) throws Exception {

		boolean result = false;
		try {
			logger.info("***** ReconProcessDaoImpl.compareFiles Start ****");

			compareBean.setStCategory(category);
			compareBean.setStSubCategory(subcat);
			compareBean.setStFile_date(filedate);

			if (category.contains("ONUS")) {
				logger.info("********* In ONUS ******");
				result = OnusComparedata(category, subcat, filedate, compareBean.getStEntryBy());
			} else if (category.equals("NFS")) {

				logger.info("********* In NFS ******");
				if (subcat.equals("ISSUER")) {
					logger.info("********* In ISSUER ******");
					result = ISSComparedata(category, subcat, filedate, compareBean.getStEntryBy());

				} else if (subcat.equals("ACQUIRER")) {
					logger.info("********* In ACQUIRER ******");
					result = AcqComparedata(category, subcat, filedate, compareBean.getStEntryBy());

				}

			} else if (category.equals("RUPAY")) {

				logger.info("*********** In Rupay ******************");
				if (subcat.equals("DOMESTIC")) {
					logger.info("******** In Domestic ***********");
					result = DomesticComparedata(category, subcat, filedate, compareBean.getStEntryBy());
				} else if (subcat.equals("INTERNATIONAL")) {
					logger.info("******** In International ***********");
					result = InternationalComparedata(category, subcat, filedate, compareBean.getStEntryBy());
				}
			} else if (category.equals("VISA")) {

				logger.info("*********** In Visa ******************");
				if (subcat.equals("ISSUER")) {
					logger.info("******** In ISSUER ***********");
					result = VisaCompareData(category, subcat, filedate, compareBean.getStEntryBy());
				} else {
					logger.info("******** In ACQUIRER ***********");
					result = VisaAcqCompareData(category, subcat, filedate, compareBean.getStEntryBy());
				}
			} else if (category.equals("CARDTOCARD")) {

				logger.info("*********** In CARDTOCARD ******************");
				// if(subcat.equals("ISSUER"))
				{
					logger.info("******** In ISSUER ***********");
					result = CardtoCardCompareData(category, filedate, compareBean.getStEntryBy());
				}
			} else if (category.equals("CASHNET")) {

				logger.info("*********** In CASHNET ******************");
				if (subcat.equals("ISSUER")) {
					logger.info("******** In ISSUER ***********");
					result = CashnetIssCompareData(category, filedate, compareBean.getStEntryBy());
				} else if (subcat.equals("ACQUIRER")) {
					logger.info("******** In ACQUIRER ***********");
					result = CashnetAcqCompareData(category, filedate, compareBean.getStEntryBy());
				}
			}
			logger.info("***** ReconProcessDaoImpl.compareFiles End ****");
			// redirectAttributes.addFlashAttribute(SUCCESS_MSG,
			// "Filteration Completed Successfully.");
			return result;
		} catch (Exception e) {
			// redirectAttributes.addFlashAttribute(ERROR_MSG,
			// "Configuration already Exists.");
			System.out.println("Exception in reconprocessdaoImpl " + e);
			demo.logSQLException(e, "ReconProcessDaoImpl.compareFiles");
			logger.error(" error in  ReconProcessDaoImpl.compareFiles",
					new Exception(" ReconProcessDaoImpl.compareFiles", e));
			return false;
		}

	}

	public boolean VisaIssDLeg(String category, String subCat, String filedate, int rec_set_id)
			throws ParseException, Exception {
		try {
			logger.info("***** VISA ISSUER D LEG COMPARE ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat.substring(0, 3));
			inParams.put("I_REC_SET_ID", rec_set_id);
			inParams.put("I_FILE_DATE", filedate);

			VisaIssDLeg knockoffTTUM = new VisaIssDLeg(getJdbcTemplate());
			Map<String, Object> outParams = knockoffTTUM.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.KnockoffTTUMdata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.KnockoffTTUMdata");
			logger.error(" error in  ReconProcessDaoImpl.KnockoffTTUMdata",
					new Exception(" ReconProcessDaoImpl.KnockoffTTUMdata", e));
			return false;
		}

	}

	class VisaIssDLeg extends StoredProcedure {
		// private static final String procName = "Rupay_TTUM_Knockoff_Proc";

		private static final String procName = "Visa_Iss_D_Compare";

		VisaIssDLeg(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean KnockoffTTUMdata(String category, String subCat, String filedate, int rec_set_id)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.KnockoffTTUMdata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat.substring(0, 3));
			inParams.put("I_REC_SET_ID", rec_set_id);
			inParams.put("I_FILE_DATE", filedate);

			KnockoffTTUM knockoffTTUM = new KnockoffTTUM(getJdbcTemplate());
			Map<String, Object> outParams = knockoffTTUM.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.KnockoffTTUMdata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.KnockoffTTUMdata");
			logger.error(" error in  ReconProcessDaoImpl.KnockoffTTUMdata",
					new Exception(" ReconProcessDaoImpl.KnockoffTTUMdata", e));
			return false;
		}

	}

	class KnockoffTTUM extends StoredProcedure {
		private static final String procName = "Rupay_TTUM_Knockoff_Proc";

		KnockoffTTUM(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_REC_SET_ID", Types.INTEGER));
			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean KnockoffVISATTUMdata(String category, String subCat, String filedate, String entry_by,
			int rec_set_id) throws ParseException, Exception {
		try {
			logger.info("***** FilterationDaoImpl.KnockoffVISATTUMdata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat.substring(0, 3));
			inParams.put("I_REC_SET_ID", rec_set_id);

			KnockoffvisaTTUM knockoffTTUM = new KnockoffvisaTTUM(getJdbcTemplate());
			Map<String, Object> outParams = knockoffTTUM.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** FilterationDaoImpl.KnockoffVISATTUMdata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				System.out.println(outParams.get("ERROR_MESSAGE"));

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "FilterationDaoImpl.KnockoffTTUMdata");
			logger.error(" error in  FilterationDaoImpl.KnockoffTTUMdata",
					new Exception(" FilterationDaoImpl.KnockoffTTUMdata", e));
			return false;
		}

	}

	class KnockoffvisaTTUM extends StoredProcedure {
		private static final String procName = "VISA_TTUM_Knockoff_Proc";

		KnockoffvisaTTUM(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_REC_SET_ID", Types.INTEGER));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	@Override
	public CompareSetupBean chkStatus(List<CompareSetupBean> compareSetupBeans, String category, String filedate)
			throws Exception {

		logger.info("***** ReconProcessDaoImpl.chkStatus Start ****");
		boolean upload_flag = false, Filter_flag = false, knockoff_flag = false, COMAPRE_FLAG = false;

		int upload = 0, knockoff = 0, compare = 0, filter = 0;
		CompareSetupBean bean = new CompareSetupBean();
		try {
			for (CompareSetupBean setupBean : compareSetupBeans) {

				logger.info("fileid" + setupBean.getInFileId());

				logger.info("knockoff" + setupBean.getKnockoff_Flag());

				logger.info("filter" + setupBean.getFilter_Flag());

				if (getStatus("UPLOAD_FLAG", filedate, category, setupBean.getInFileId()) > 0) {

					upload++;

				}
				if (getStatus("COMAPRE_FLAG", filedate, category, setupBean.getInFileId()) > 0) {

					compare++;

				}
				if (setupBean.getKnockoff_Flag().equalsIgnoreCase("Y")) {

					if (getStatus("FILTER_FLAG", filedate, category, setupBean.getInFileId()) > 0) {

						filter++;

					}
				}
				// added by int6345

				// end
				else {

					filter++;

				}
				if (setupBean.getKnockoff_Flag().equalsIgnoreCase("Y")) {

					if (getStatus("KNOCKOFF_FLAG", filedate, category, setupBean.getInFileId()) > 0) {

						knockoff++;

					}
				} else {

					knockoff++;
				}

			}

			logger.info(upload);
			logger.info(filter);
			logger.info(knockoff);
			logger.info(compare);
			if (upload == compareSetupBeans.size()) {

				bean.setUpload_Flag("Completed");
			} else {

				bean.setUpload_Flag("Pending");
			}
			if (filter == compareSetupBeans.size()) {

				bean.setFilter_Flag("Completed");
			} else {
				if (category.equals("MASTERCARD")) {
					bean.setFilter_Flag("Completed");
				} else {
					bean.setFilter_Flag("Pending");
				}
			}
			if (knockoff == compareSetupBeans.size()) {

				bean.setKnockoff_Flag("Completed");
			} else {

				if (category.equals("MASTERCARD")) {
					bean.setKnockoff_Flag("Completed");
				} else {
					bean.setKnockoff_Flag("Pending");
				}
			}
			if (compare == compareSetupBeans.size()) {

				bean.setComapre_Flag("Completed");
			} else {

				bean.setComapre_Flag("Pending");
			}

			logger.info("***** ReconProcessDaoImpl.chkStatus End ****");

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.chkStatus");
			logger.error(" error in  ReconProcessDaoImpl.chkStatus",
					new Exception(" ReconProcessDaoImpl.chkStatus", e));
			throw e;
		}

		return bean;
	}

	public int getStatus(String Flag, String filedate, String category, int fileid) throws Exception {
		logger.info("***** ReconProcessDaoImpl.getStatus Start ****");
		try {
			String query = "Select count(*)  FROM main_file_upload_dtls  where category='" + category + "'"
					+ " and filedate = to_date('" + filedate + "','dd/mm/yyyy') and " + " fileid=" + fileid + " and "
					+ Flag + " = 'Y'";

			logger.info(query);

			logger.info("***** ReconProcessDaoImpl.getStatus End ****");

			return getJdbcTemplate().queryForObject(query, Integer.class);

		} catch (Exception ex) {
			demo.logSQLException(ex, "ReconProcessDaoImpl.getStatus");
			logger.error(" error in  ReconProcessDaoImpl.getStatus",
					new Exception(" ReconProcessDaoImpl.getStatus", ex));
			// throw e;
			return 1;

		}

	}

	public boolean chkStatus(CompareSetupBean setupBean, List<String> tables) throws Exception {
		logger.info("***** ReconProcessDaoImpl.chkStatus Start ****");
		try {
			String query = "select FILEID from main_filesource where upper(filename) = '" + tables.get(0)
					+ "' and FILE_CATEGORY='" + setupBean.getCategory() + "' and FILE_SUBCATEGORY='"
					+ setupBean.getStSubCategory() + "'";

			logger.info("query==" + query);

			int fileid1 = getJdbcTemplate().queryForObject(query, Integer.class);
			logger.info("fileid1==" + fileid1);

			query = "select FILEID from main_filesource where upper(filename) = '" + tables.get(1)
					+ "' and FILE_CATEGORY='" + setupBean.getCategory() + "' and FILE_SUBCATEGORY='"
					+ setupBean.getStSubCategory() + "'";
			logger.info("query==" + query);

			int fileid2 = getJdbcTemplate().queryForObject(query, Integer.class);
			logger.info("fileid2==" + fileid2);

			String knockoffFlag1, FilterFlag1, KnockoffFlag2, filterflag2;

			FilterFlag1 = getJdbcTemplate().queryForObject(
					"select FILTERATION from main_filesource where FILEID =" + fileid1 + "  ", String.class);
			logger.info("FilterFlag1==" + FilterFlag1);

			knockoffFlag1 = getJdbcTemplate().queryForObject(
					"select KNOCKOFF from main_filesource where FILEID =" + fileid1 + "  ", String.class);
			logger.info("knockoffFlag1==" + knockoffFlag1);

			filterflag2 = getJdbcTemplate().queryForObject(
					"select FILTERATION from main_filesource where FILEID =" + fileid2 + "  ", String.class);
			logger.info("filterflag2==" + filterflag2);

			KnockoffFlag2 = getJdbcTemplate().queryForObject(
					"select KNOCKOFF from main_filesource where FILEID =" + fileid2 + " ", String.class);
			logger.info("KnockoffFlag2==" + KnockoffFlag2);

			String sql = "";
			int result1 = 0, result2 = 0;
			if (FilterFlag1.equalsIgnoreCase("Y") && knockoffFlag1.equalsIgnoreCase("Y")) {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where  FILEID = " + fileid1
						+ " and Knockoff_FLAG='Y' AND Upload_FLAG = 'Y' and FILTER_FLAG = 'Y' and COMAPRE_FLAG='N' and  filedate =  to_date('"
						+ setupBean.getFileDate() + "','dd/mm/yyyy')  ";
				result1 = getJdbcTemplate().queryForObject(sql, Integer.class);

			} else {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where FILEID = " + fileid1
						+ " and  Upload_FLAG = 'Y' and  COMAPRE_FLAG='N' and filedate =  to_date('"
						+ setupBean.getFileDate() + "','dd/mm/yyyy') ";
				result1 = getJdbcTemplate().queryForObject(sql, Integer.class);
			}
			logger.info("sql==" + sql);
			logger.info("result1==" + result1);

			if (filterflag2.equalsIgnoreCase("Y") && KnockoffFlag2.equalsIgnoreCase("Y")) {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where  FILEID = " + fileid2
						+ " and Knockoff_FLAG='Y' AND Upload_FLAG = 'Y' and FILTER_FLAG = 'Y' and COMAPRE_FLAG='N' "
						+ "and filedate =  to_date('" + setupBean.getFileDate() + "','dd/mm/yyyy') ";
				result2 = getJdbcTemplate().queryForObject(sql, Integer.class);

			} else {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where FILEID = " + fileid2
						+ " and  Upload_FLAG = 'Y' and  COMAPRE_FLAG='N' " + "and filedate = to_date('"
						+ setupBean.getFileDate() + "','dd/mm/yyyy') ";
				result2 = getJdbcTemplate().queryForObject(sql, Integer.class);

			}
			logger.info("sql==" + sql);
			logger.info("result2==" + result2);

			logger.info("***** ReconProcessDaoImpl.chkStatus end ****");

			if (result1 > 0 && result2 > 0) {

				return true;

			} else {

				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.chkStatus");
			logger.error(" error in  ReconProcessDaoImpl.chkStatus",
					new Exception(" ReconProcessDaoImpl.chkStatus", e));
			throw e;
		}
	}

	public boolean checkCompareStatus(String stFileDate, String stCategory, String stTable1_Subcat, String stTable2_cat,
			List<String> tables) throws Exception {

		logger.info("***** ReconProcessDaoImpl.checkCompareStatus Start ****");

		try {
			String query = "select FILEID from main_filesource where upper(filename) = '" + tables.get(0)
					+ "' and FILE_CATEGORY='" + stCategory + "' and FILE_SUBCATEGORY='" + stTable1_Subcat + "'";
			logger.info("query==" + query);
			int fileid1 = getJdbcTemplate().queryForObject(query, Integer.class);
			logger.info("fileid1==" + fileid1);
			query = "select FILEID from main_filesource where upper(filename) = '" + tables.get(1)
					+ "' and FILE_CATEGORY='" + stCategory + "' and FILE_SUBCATEGORY='" + stTable2_cat + "'";
			logger.info("query==" + query);
			int fileid2 = getJdbcTemplate().queryForObject(query, Integer.class);
			logger.info("fileid2==" + fileid2);
			String knockoffFlag1, FilterFlag1, KnockoffFlag2, filterflag2;

			FilterFlag1 = getJdbcTemplate().queryForObject(
					"select FILTERATION from main_filesource where FILEID =" + fileid1 + "  ", String.class);
			logger.info("FilterFlag1==" + FilterFlag1);
			knockoffFlag1 = getJdbcTemplate().queryForObject(
					"select KNOCKOFF from main_filesource where FILEID =" + fileid1 + "  ", String.class);
			logger.info("knockoffFlag1==" + knockoffFlag1);

			filterflag2 = getJdbcTemplate().queryForObject(
					"select FILTERATION from main_filesource where FILEID =" + fileid2 + "  ", String.class);
			logger.info("filterflag2==" + filterflag2);
			KnockoffFlag2 = getJdbcTemplate().queryForObject(
					"select KNOCKOFF from main_filesource where FILEID =" + fileid2 + " ", String.class);
			logger.info("KnockoffFlag2==" + KnockoffFlag2);

			String sql = "";
			int result1 = 0, result2 = 0;
			if (FilterFlag1.equalsIgnoreCase("Y") && knockoffFlag1.equalsIgnoreCase("Y")) {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where  FILEID = " + fileid1
						+ " and Knockoff_FLAG='Y' AND Upload_FLAG = 'Y' and FILTER_FLAG = 'Y' and COMAPRE_FLAG='N' "
						+ "and  filedate =  '" + stFileDate + "'  ";

				result1 = getJdbcTemplate().queryForObject(sql, Integer.class);

			} else {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where FILEID = " + fileid1
						+ " and  Upload_FLAG = 'Y' and  COMAPRE_FLAG='N' and filedate =  '" + stFileDate + "'  ";
				result1 = getJdbcTemplate().queryForObject(sql, Integer.class);
			}
			logger.info("sql==" + sql);
			logger.info("result1==" + result1);

			if (filterflag2.equalsIgnoreCase("Y") && KnockoffFlag2.equalsIgnoreCase("Y")) {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where  FILEID = " + fileid2
						+ " and Knockoff_FLAG='Y' AND Upload_FLAG = 'Y' and FILTER_FLAG = 'Y' and COMAPRE_FLAG='N'"
						+ " and filedate =  '" + stFileDate + "'  ";
				result2 = getJdbcTemplate().queryForObject(sql, Integer.class);

			} else {

				sql = "Select count(*) from MAIN_FILE_UPLOAD_DTLS where FILEID = " + fileid2
						+ " and  Upload_FLAG = 'Y' and  COMAPRE_FLAG='N' and filedate = '" + stFileDate + "'  ";
				result2 = getJdbcTemplate().queryForObject(sql, Integer.class);

			}
			logger.info("sql==" + sql);
			logger.info("result2==" + result2);

			logger.info("***** ReconProcessDaoImpl.checkCompareStatus End ****");

			if (result1 > 0 && result2 > 0) {

				return true;

			} else {

				return false;
			}
		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.checkCompareStatus");
			logger.error(" error in  ReconProcessDaoImpl.checkCompareStatus",
					new Exception(" ReconProcessDaoImpl.checkCompareStatus", e));
			throw e;
		}

	}

	public boolean updatereconstatus(CompareSetupBean setupBean, List<String> tables, List<String> categories)
			throws Exception {
		logger.info("***** ReconProcessDaoImpl.updatereconstatus Start ****");
		try {

			for (int i = 0; i < tables.size(); i++) {

				String query = "select FILEID from main_filesource where upper(filename) = '" + tables.get(i)
						+ "' and FILE_CATEGORY='" + setupBean.getCategory() + "' and FILE_SUBCATEGORY='"
						+ categories.get(i) + "'";
				logger.info("query==" + query);

				int fileid1 = getJdbcTemplate().queryForObject(query, Integer.class);
				logger.info("fileid1==" + fileid1);

				setupBean.setInFileId(fileid1);
				/*
				 * if(setupBean.getCategory().equals("ONUS") ||
				 * setupBean.getCategory().equals("AMEX")) {
				 */
				icompareConfigService.updateFlag("MANUALCOMPARE_FLAG", setupBean);
				icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);
				/*
				 * } else icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);
				 */

			}
			logger.info("***** ReconProcessDaoImpl.updatereconstatus End ****");

			return true;

		} catch (Exception ex) {
			demo.logSQLException(ex, "ReconProcessDaoImpl.updatereconstatus");
			logger.error(" error in  ReconProcessDaoImpl.updatereconstatus",
					new Exception(" ReconProcessDaoImpl.updatereconstatus", ex));

			return false;
		}

	}

	public boolean updateRupayreconstatus(CompareSetupBean setupBean, Set<String> tables, Set<String> subCategory)
			throws Exception {
		logger.info("***** ReconProcessDaoImpl.updateRupayreconstatus Start ****");
		try {

			for (String fileName : tables) {
				for (String stSubCat : subCategory) {
					try {
						if (fileName.equals("RUPAY") && stSubCat.equals("SURCHARGE")
								&& setupBean.getCategory().equals("RUPAY")) {

							logger.info("IN RUPAY-SURCHARGE");
						} else if (fileName.equals("SWITCH") && stSubCat.equals("SURCHARGE")
								&& setupBean.getCategory().equals("RUPAY")) {

							logger.info("IN RUPAY-SURCHARGE");
						} else if (fileName.equals("VISA") && stSubCat.equals("SURCHARGE")
								&& setupBean.getCategory().equals("VISA")) {

							logger.info("IN RUPAY-SURCHARGE");
						} else if (fileName.equals("SWITCH") && stSubCat.equals("SURCHARGE")
								&& setupBean.getCategory().equals("VISA")) {

							logger.info("IN RUPAY-SURCHARGE");
						} else {
							String query = "select FILEID from main_filesource where upper(filename) = '" + fileName
									+ "' and FILE_CATEGORY='" + setupBean.getCategory() + "' and FILE_SUBCATEGORY='"
									+ stSubCat + "'";
							logger.info("query==" + query);
							int fileid1 = getJdbcTemplate().queryForObject(query, Integer.class);
							logger.info("fileid1==" + fileid1);
							setupBean.setInFileId(fileid1);
							icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);
							// ADDED BY INT5779 AS ON 12TH MARCH FOR MERGING MAN
							// FILE CODE
							icompareConfigService.updateFlag("MANUALCOMPARE_FLAG", setupBean);
						}
					} catch (Exception e) {
						demo.logSQLException(e, "ReconProcessDaoImpl.updateRupayreconstatus");
						logger.error(" error in ReconProcessDaoImpl.updateRupayreconstatus",
								new Exception("ReconProcessDaoImpl.updateRupayreconstatus", e));
						throw e;
					}
				}

			}

			/*
			 * for (int i = 0; i < tables.size(); i++) {
			 * 
			 * 
			 * String query ="select FILEID from main_filesource where upper(filename) = '"
			 * +tables.get(i)+"' and FILE_CATEGORY='"+setupBean.getCategory()+
			 * "' and FILE_SUBCATEGORY='" +categories.get(i)+"'"; int
			 * fileid1=getJdbcTemplate().queryForObject(query, Integer.class);
			 * 
			 * 
			 * setupBean.setInFileId(fileid1);
			 * icompareConfigService.updateFlag("COMAPRE_FLAG", setupBean);
			 * 
			 * 
			 * 
			 * 
			 * }
			 */
			logger.info("***** ReconProcessDaoImpl.updateRupayreconstatus End ****");

			return true;

		} catch (Exception ex) {
			demo.logSQLException(ex, "ReconProcessDaoImpl.updateRupayreconstatus");
			logger.error(" error in ReconProcessDaoImpl.updateRupayreconstatus",
					new Exception("ReconProcessDaoImpl.updateRupayreconstatus", ex));

			return false;
		}

	}

	// added for mastercard and cardtpcard on 16_03_2018

	public boolean Mastercard_Iss1(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss1 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			MasterCard_proc1 cbrmatching = new MasterCard_proc1(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss1 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Iss1");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Iss1",
					new Exception(" ReconProcessDaoImpl.Mastercard_Iss1", e));
			return false;
		}

	}

	// s

	// CARDTOCARD

	public boolean CardtoCard(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.CardtoCard Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			CardtoCard_proc1 cbrmatching = new CardtoCard_proc1(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.CardtoCard End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.CardtoCard");
			logger.error(" error in  ReconProcessDaoImpl.CardtoCard",
					new Exception(" ReconProcessDaoImpl.CardtoCard", e));
			return false;
		}

	}

	// Onus_Pos

	public boolean Onus_Pos_Cycle1(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle1 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			pos_onus_cycle1 cbrmatching = new pos_onus_cycle1(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle1 End ****");

			if (outParams.get("msg1") != null) {
				logger.info("outParams " + outParams.get("msg1"));
				return true;
			} else {

				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Onus_Pos_Cycle1");
			logger.error(" error in  ReconProcessDaoImpl.Onus_Pos_Cycle1",
					new Exception(" ReconProcessDaoImpl.Onus_Pos_Cycle1", e));
			return false;
		}

	}

	public boolean Onus_Pos_Cycle2(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle2 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			pos_onus_cycle2 cbrmatching = new pos_onus_cycle2(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle2 End ****");
			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Onus_Pos_Cycle2");
			logger.error(" error in  ReconProcessDaoImpl.Onus_Pos_Cycle2",
					new Exception(" ReconProcessDaoImpl.Onus_Pos_Cycle2", e));
			return false;
		}

	}

	public boolean Onus_Pos_Cycle3(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle3 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			pos_onus_cycle3 cbrmatching = new pos_onus_cycle3(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Cycle3 End ****");
			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Onus_Pos_Cycle3");
			logger.error(" error in  ReconProcessDaoImpl.Onus_Pos_Cycle3",
					new Exception(" ReconProcessDaoImpl.Onus_Pos_Cycle3", e));
			return false;
		}

	}

	public boolean Onus_Pos_Settlment(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Settlment Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			// inParams.put("rec_set_id", value);

			onus_pos_settlmnt cbrmatching = new onus_pos_settlmnt(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Onus_Pos_Settlment End ****");
			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Onus_Pos_Settlment");
			logger.error(" error in  ReconProcessDaoImpl.Onus_Pos_Settlment",
					new Exception(" ReconProcessDaoImpl.Onus_Pos_Settlment", e));
			return false;
		}

	}

	public boolean Mastercard_Iss_CD(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss_CD Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);

			MasterCard_proc_CD cbrmatching = new MasterCard_proc_CD(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams Msg==" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss_CD End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Iss_CD");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Iss_CD",
					new Exception(" ReconProcessDaoImpl.Mastercard_Iss_CD", e));
			return false;
		}

	}

	public boolean CardtoCard_settle(String categ, String filedate) throws ParseException, Exception {
		try {

			logger.info("***** ReconProcessDaoImpl.CardtoCard_settle Start ****");
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);

			CardtoCard_set cbrmatching = new CardtoCard_set(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);
			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.CardtoCard_settle End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.CardtoCard_settle");
			logger.error(" error in  ReconProcessDaoImpl.CardtoCard_settle",
					new Exception(" ReconProcessDaoImpl.CardtoCard_settle", e));
			return false;
		}

	}

	public boolean Mastercard_Acq1(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq1 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			MasterCard_proc_Acq cbrmatching = new MasterCard_proc_Acq(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq1 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Acq1");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Acq1",
					new Exception(" ReconProcessDaoImpl.Mastercard_Acq1", e));
			return false;
		}

	}

	public boolean Mastercard_Acq2(String categ, String filedate, String value, String dollar_val)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq2 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);
			inParams.put("dollar_val", dollar_val);

			MasterCard_proc_Acq2 cbrmatching = new MasterCard_proc_Acq2(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq2 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Acq2");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Acq2",
					new Exception(" ReconProcessDaoImpl.Mastercard_Acq2", e));
			return false;
		}

	}

	// For recon Settlement

	public boolean Mastercard_Recon_Settlement_iss(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Recon_Settlement_iss Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);

			MasterCard_recon_sett_pro cbrmatching = new MasterCard_recon_sett_pro(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Recon_Settlement_iss End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Recon_Settlement_iss");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Recon_Settlement_iss",
					new Exception(" ReconProcessDaoImpl.Mastercard_Recon_Settlement_iss", e));
			return false;
		}

	}

	public boolean Mastercard_Recon_Settlement_acq(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Recon_Settlement_acq Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);

			MasterCard_recon_sett_pro1 cbrmatching = new MasterCard_recon_sett_pro1(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Recon_Settlement_acq End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Recon_Settlement_acq");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Recon_Settlement_acq",
					new Exception(" ReconProcessDaoImpl.Mastercard_Recon_Settlement_acq", e));
			return false;
		}
	}

	public boolean Mastercard_Iss2(String categ, String filedate, String value, String dollar_val)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss2 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);
			inParams.put("dollar_val", dollar_val);
			MasterCard_proc2 cbrmatching = new MasterCard_proc2(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss2 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Iss2");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Iss2",
					new Exception(" ReconProcessDaoImpl.Mastercard_Iss2", e));
			// logger.error(e.getMessage());
			return false;
		}

	}

	// call auto-reversal

	public boolean Mastercard_Acq_auto_rev(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq_auto_rev Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			// inParams.put("rec_set_id", value);

			MasterCard_proc_Acq_auto_rev cbrmatching = new MasterCard_proc_Acq_auto_rev(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Acq_auto_rev End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Acq_auto_rev");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Acq_auto_rev",
					new Exception(" ReconProcessDaoImpl.Mastercard_Acq_auto_rev", e));
			return false;
		}

	}

	// man_file_process

	public boolean Mastercard_man_file_process(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_man_file_process Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			// inParams.put("rec_set_id", value);

			MasterCard_proc_man_file cbrmatching = new MasterCard_proc_man_file(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_man_file_process End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_man_file_process");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_man_file_process",
					new Exception(" ReconProcessDaoImpl.Mastercard_man_file_process", e));
			return false;
		}

	}

	public boolean Mastercard_man_file_process1(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_man_file_process1 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			// inParams.put("rec_set_id", value);

			MasterCard_proc_man_file1 cbrmatching = new MasterCard_proc_man_file1(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_man_file_process1 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_man_file_process1");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_man_file_process1",
					new Exception(" ReconProcessDaoImpl.Mastercard_man_file_process1", e));
			return false;
		}

	}

	public boolean Mastercard_Iss_auto_rev(String categ, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss_auto_rev Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			// inParams.put("rec_set_id", value);

			MasterCard_proc_Iss_auto_rev cbrmatching = new MasterCard_proc_Iss_auto_rev(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss_auto_rev End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Iss_auto_rev");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Iss_auto_rev",
					new Exception(" ReconProcessDaoImpl.Mastercard_Iss_auto_rev", e));
			return false;
		}

	}

	private class MasterCard_proc2 extends StoredProcedure {
		private static final String procName = "recon_switch_mastercrd";

		MasterCard_proc2(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlParameter("dollar_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	public boolean Mastercard_Iss3(String categ, String filedate, String value) throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss3 Start ****");
			// boolean resp=false;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("category_name", categ);
			inParams.put("date_val", filedate);
			inParams.put("rec_set_id", value);

			MasterCard_proc3 cbrmatching = new MasterCard_proc3(getJdbcTemplate());
			Map<String, Object> outParams = cbrmatching.execute(inParams);

			logger.info("outParams msg1" + outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.Mastercard_Iss3 End ****");

			if (outParams.get("msg1") != null) {

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.Mastercard_Iss3");
			logger.error(" error in  ReconProcessDaoImpl.Mastercard_Iss3",
					new Exception(" ReconProcessDaoImpl.Mastercard_Iss3", e));
			return false;
		}

	}

	private class MasterCard_proc3 extends StoredProcedure {
		private static final String procName = "recon_mastercrd_surcharge";

		MasterCard_proc3(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc1 extends StoredProcedure {
		private static final String procName = "recon_switch_cbs";

		MasterCard_proc1(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class CardtoCard_proc1 extends StoredProcedure {
		private static final String procName = "card_to_card";

		CardtoCard_proc1(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class pos_onus_cycle1 extends StoredProcedure {
		private static final String procName = "pos_onus_cycle1";

		pos_onus_cycle1(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class pos_onus_cycle2 extends StoredProcedure {
		private static final String procName = "pos_onus_cycle2";

		pos_onus_cycle2(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class pos_onus_cycle3 extends StoredProcedure {
		private static final String procName = "pos_onus_cycle3";

		pos_onus_cycle3(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class onus_pos_settlmnt extends StoredProcedure {
		private static final String procName = "onus_pos_settlmnt";

		onus_pos_settlmnt(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			// declareParameter(new
			// SqlParameter("rec_set_id",Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc_CD extends StoredProcedure {
		private static final String procName = "recon_cbs_ttum_c_d";

		MasterCard_proc_CD(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class CardtoCard_set extends StoredProcedure {
		private static final String procName = "card2_settlement";

		CardtoCard_set(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc_Acq extends StoredProcedure {
		private static final String procName = "a_recon_switch_cbs";

		MasterCard_proc_Acq(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc_Acq2 extends StoredProcedure {
		private static final String procName = "a_recon_switch_mastercrd";

		MasterCard_proc_Acq2(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlParameter("rec_set_id", Types.VARCHAR));
			declareParameter(new SqlParameter("dollar_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_recon_sett_pro extends StoredProcedure {
		private static final String procName = "recon_settlement_iss";

		MasterCard_recon_sett_pro(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_recon_sett_pro1 extends StoredProcedure {
		private static final String procName = "recon_settlement_acq";

		MasterCard_recon_sett_pro1(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	// call auto-rev

	private class MasterCard_proc_Acq_auto_rev extends StoredProcedure {
		private static final String procName = "move_acq_temp";

		MasterCard_proc_Acq_auto_rev(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc_Iss_auto_rev extends StoredProcedure {
		private static final String procName = "move_iss_temp";

		MasterCard_proc_Iss_auto_rev(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	// man_file_process

	private class MasterCard_proc_man_file extends StoredProcedure {
		private static final String procName = "manual_knockoff";

		MasterCard_proc_man_file(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	private class MasterCard_proc_man_file1 extends StoredProcedure {
		private static final String procName = "manual_knockoff_iss";

		MasterCard_proc_man_file1(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("category_name", Types.VARCHAR));
			declareParameter(new SqlParameter("date_val", Types.VARCHAR));
			declareParameter(new SqlOutParameter("msg1", Types.VARCHAR));

			compile();
		}
	}

	// END
	public void clearTables(List<String> tables, CompareBean compareBeanObj) throws Exception {
		logger.info("***** ReconProcessDaoImpl.clearTables Start ****");
		try {
			for (int i = 0; i < tables.size(); i++) {
				String TRUNCATE_QUERY = "TRUNCATE TABLE " + compareBeanObj.getStMergeCategory() + "_" + tables.get(i);
				logger.info("TRUNCATE QUERY IS " + TRUNCATE_QUERY);
				getJdbcTemplate().execute(TRUNCATE_QUERY);
				TRUNCATE_QUERY = "TRUNCATE TABLE " + compareBeanObj.getStMergeCategory() + "_" + tables.get(i)
						+ "_KNOCKOFF";
				logger.info("TRUNCATE QUERY IS " + TRUNCATE_QUERY);
				getJdbcTemplate().execute(TRUNCATE_QUERY);
				TRUNCATE_QUERY = "TRUNCATE TABLE " + compareBeanObj.getStMergeCategory() + "_" + tables.get(i)
						+ "_MATCHED";
				logger.info("TRUNCATE QUERY IS " + TRUNCATE_QUERY);
				getJdbcTemplate().execute(TRUNCATE_QUERY);
				TRUNCATE_QUERY = "TRUNCATE TABLE TEMP_" + compareBeanObj.getStMergeCategory() + "_" + tables.get(i);
				logger.info("TRUNCATE QUERY IS " + TRUNCATE_QUERY);
				getJdbcTemplate().execute(TRUNCATE_QUERY);
				TRUNCATE_QUERY = "TRUNCATE TABLE RECON_" + compareBeanObj.getStMergeCategory() + "_" + tables.get(i);
				logger.info("TRUNCATE QUERY IS " + TRUNCATE_QUERY);
				getJdbcTemplate().execute(TRUNCATE_QUERY);

			}
			logger.info("***** ReconProcessDaoImpl.clearTables End ****");
		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.clearTables");
			logger.error(" error in  ReconProcessDaoImpl.clearTables",
					new Exception(" ReconProcessDaoImpl.clearTables", e));
			throw e;
		}
	}

	// Nfs Issuer Process
	public boolean ISSClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.ISSClassifydata Start ****");

			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			IssClassificaton acqclassificaton = new IssClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.ISSClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.ISSClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.ISSClassifydata",
					new Exception(" ReconProcessDaoImpl.ISSClassifydata", e));
			return false;
		}

	}

	class IssClassificaton extends StoredProcedure {
		// private static final String procName = "TEMP_NFS_ISS_CLASSIFY";
		private static final String procName = "NFS_ISS_CLASSIFY_NAINITAL";

		IssClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean ISSComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.ISSComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			IssCompare issCompare = new IssCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.ISSComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.ISSComparedata");
			logger.error(" error in  ReconProcessDaoImpl.ISSComparedata",
					new Exception(" ReconProcessDaoImpl.ISSComparedata", e));
			return false;
		}

	}

	class IssCompare extends StoredProcedure {
		// private static final String procName = "NFS_Iss_COMPARE_Proc";
//		private static final String procName = "RECON_NFS_ISS_PROC";
		private static final String procName = "RECON_NFS_ISS_PROC";

		// teST_NFS_Iss_COMPARE_Proc_rev2
		IssCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// Nfs Acquirer Process

	public boolean AcqClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.AcqClassifydata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			AcqClassificaton acqclassificaton = new AcqClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.AcqClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.AcqClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.AcqClassifydata",
					new Exception(" ReconProcessDaoImpl.AcqClassifydata", e));
			return false;
		}

	}

	class AcqClassificaton extends StoredProcedure {
		private static final String procName = "NFS_ACQ_CLASSIFY";

		AcqClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean AcqComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.AcqComparedata Start ****" + "");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			AcqCompare acqComparedata = new AcqCompare(getJdbcTemplate());
			Map<String, Object> outParams = acqComparedata.execute(inParams);

			logger.info("Outparam is " + outParams);

			logger.info("***** ReconProcessDaoImpl.AcqComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				System.out.println(outParams.get("ERROR_MESSAGE"));

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.AcqComparedata");
			logger.error(" error in  ReconProcessDaoImpl.AcqComparedata",
					new Exception(" ReconProcessDaoImpl.AcqComparedata", e));
			return false;
		}

	}

	class AcqCompare extends StoredProcedure {
		private static final String procName = "RECON_NFS_ACQ_PROC";

		AcqCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// CASHNET Issuer Process

	public boolean cashnetISSClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.cashnetISSClassifydata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			cashnetIssClassificaton acqclassificaton = new cashnetIssClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);
			logger.info("***** ReconProcessDaoImpl.cashnetISSClassifydata End ****");
			if (outParams.get("ERROR_MESSAGE") != null) {
				logger.info("Error is " + outParams.get("ERROR_MESSAGE"));
				return false;
			} else {
				logger.info("Procedure executed successfully");
				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.cashnetISSClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.cashnetISSClassifydata",
					new Exception(" ReconProcessDaoImpl.cashnetISSClassifydata", e));
			return false;
		}

	}

	class cashnetIssClassificaton extends StoredProcedure {
		private static final String procName = "CASHNET_ISS_CLASSIFY";

		cashnetIssClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	/*
	 * public boolean cashnetISSComparedata(String category, String subCat, String
	 * filedate, String entry_by) throws ParseException, Exception { try {
	 * logger.info("***** ReconProcessDaoImpl.cashnetISSComparedata Start ****");
	 * String response = null; Map<String, Object> inParams = new HashMap<String,
	 * Object>();
	 * 
	 * inParams.put("I_CATEGORY", category); inParams.put("I_SUBCATEGORY", subCat);
	 * inParams.put("I_FILE_DATE", filedate); inParams.put("I_ENTRY_BY", entry_by);
	 * 
	 * cashnetIssCompare issCompare = new cashnetIssCompare( getJdbcTemplate());
	 * Map<String, Object> outParams = issCompare.execute(inParams);
	 * logger.info("***** ReconProcessDaoImpl.cashnetISSComparedata End ****"); if
	 * (outParams.get("ERROR_MESSAGE") != null) {
	 * 
	 * return false; } else {
	 * 
	 * return true; }
	 * 
	 * } catch (Exception e) { demo.logSQLException(e,
	 * "ReconProcessDaoImpl.cashnetISSComparedata"); logger.error(
	 * " error in  ReconProcessDaoImpl.cashnetISSComparedata", new
	 * Exception(" ReconProcessDaoImpl.cashnetISSComparedata", e)); return false; }
	 * 
	 * }
	 * 
	 * class cashnetIssCompare extends StoredProcedure { private static final String
	 * procName = "CASHNET_ISS_COMPARE_PROC";
	 * 
	 * cashnetIssCompare(JdbcTemplate JdbcTemplate) { super(JdbcTemplate, procName);
	 * setFunction(false);
	 * 
	 * declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
	 * declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
	 * declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
	 * compile(); } }
	 */

	// cashnet Acquirer Process

	public boolean cashnetAcqClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.cashnetAcqClassifydata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			cashnetAcqClassificaton acqclassificaton = new cashnetAcqClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);
			logger.info("***** ReconProcessDaoImpl.cashnetAcqClassifydata End ****");
			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.cashnetAcqClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.cashnetAcqClassifydata",
					new Exception(" ReconProcessDaoImpl.cashnetAcqClassifydata", e));
			return false;
		}

	}

	class cashnetAcqClassificaton extends StoredProcedure {
		private static final String procName = "CASHNET_ACQ_Classify";

		cashnetAcqClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	/*
	 * public boolean cashnetAcqComparedata(String category, String subCat, String
	 * filedate, String entry_by) throws ParseException, Exception { try {
	 * logger.info("***** ReconProcessDaoImpl.cashnetAcqComparedata Start ****");
	 * String response = null; Map<String, Object> inParams = new HashMap<String,
	 * Object>();
	 * 
	 * inParams.put("I_CATEGORY", category); inParams.put("I_SUBCATEGORY", subCat);
	 * inParams.put("I_FILE_DATE", filedate); inParams.put("I_ENTRY_BY", entry_by);
	 * 
	 * cashnetAcqCompare acqComparedata = new cashnetAcqCompare( getJdbcTemplate());
	 * Map<String, Object> outParams = acqComparedata.execute(inParams);
	 * logger.info("***** ReconProcessDaoImpl.cashnetAcqComparedata End ****"); if
	 * (outParams.get("ERROR_MESSAGE") != null) {
	 * 
	 * return false; } else {
	 * 
	 * return true; }
	 * 
	 * } catch (Exception e) { demo.logSQLException(e,
	 * "ReconProcessDaoImpl.cashnetAcqComparedata"); logger.error(
	 * " error in  ReconProcessDaoImpl.cashnetAcqComparedata", new
	 * Exception(" ReconProcessDaoImpl.cashnetAcqComparedata", e)); return false; }
	 * 
	 * }
	 */

	/*
	 * class cashnetAcqCompare extends StoredProcedure { private static final String
	 * procName = "CASHNET_ACQ_COMPARE_PROC";
	 * 
	 * cashnetAcqCompare(JdbcTemplate JdbcTemplate) { super(JdbcTemplate, procName);
	 * setFunction(false);
	 * 
	 * declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
	 * declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
	 * declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
	 * declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
	 * compile(); } }
	 */

	public boolean WCCComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.WCCComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			WCCCompare wccComparedata = new WCCCompare(getJdbcTemplate());
			Map<String, Object> outParams = wccComparedata.execute(inParams);
			logger.info("***** ReconProcessDaoImpl.WCCComparedata End ****");
			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.WCCComparedata");
			logger.error(" error in  ReconProcessDaoImpl.WCCComparedata",
					new Exception(" ReconProcessDaoImpl.WCCComparedata", e));
			return false;
		}

	}

	class WCCCompare extends StoredProcedure {
		private static final String procName = "WCC_COMPARE_PROC";

		WCCCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean insertDailyRawData(String date) throws Exception {

		String updattab = " select count(1) from CBS_RUPAY_RAWDATA_copy  ";
		logger.info(updattab);

		int count = getJdbcTemplate().queryForObject(updattab, Integer.class);

		logger.info(count);

		if (count == 0) {
			String query = " insert into CBS_RUPAY_RAWDATA_copy ( Select * from  CBS_RUPAY_RAWDATA where filedate = to_date('"
					+ date + "', 'DD-MON-RRRR') )";
			logger.info(query);
			getJdbcTemplate().execute(query);
		}

		/*
		 * String updattab = "update MAIN_FILESOURCE set 'CBS_RUPAY_RAWDATA' " +
		 * " where TABLENAME ='CBS_RUPAY_RAWDATA_COPY' "+ " and FILE_CATEGORY= 'RUPAY' "
		 * ; logger.info(updattab);
		 * 
		 * getJdbcTemplate().execute(updattab);
		 */

		return true;
	}

	public boolean truncateDailyRawData() throws Exception {

		String query = " truncate table CBS_RUPAY_RAWDATA_copy  ";
		logger.info(query);
		getJdbcTemplate().execute(query);

		/*
		 * String updattab = "update MAIN_FILESOURCE set 'CBS_RUPAY_RAWDATA' " +
		 * " where TABLENAME ='CBS_RUPAY_RAWDATA_COPY' "+ " and FILE_CATEGORY= 'RUPAY' "
		 * ; logger.info(updattab); getJdbcTemplate().execute(updattab);
		 */

		return true;
	}

	public boolean OnusCardLessCompare(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.cardless Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			cardlessCompare issCompare = new cardlessCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);
			logger.info("***** ReconProcessDaoImpl.ONUScardless End ****");
			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.cashnetISSComparedata");
			logger.error(" error in  ReconProcessDaoImpl.cashnetISSComparedata",
					new Exception(" ReconProcessDaoImpl.cashnetISSComparedata", e));
			return false;
		}

	}

	class cardlessCompare extends StoredProcedure {
		private static final String procName = "ONUS_CARDLESS_COMPARE_PROC";

		cardlessCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	class VisaIssDCBS extends StoredProcedure {
		// private static final String procName = "Rupay_TTUM_Knockoff_Proc";

		private static final String procName = "VISA_ISS_D_COMPARE";

		VisaIssDCBS(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean VisaIssDCBS(String category, String subCat, String filedate) throws ParseException, Exception {
		try {
			logger.info("***** VISA ISSUER D LEG COMPARE ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);

			VisaIssDCBS knockoffTTUM = new VisaIssDCBS(getJdbcTemplate());
			Map<String, Object> outParams = knockoffTTUM.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.KnockoffTTUMdata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.KnockoffTTUMdata");
			logger.error(" error in  ReconProcessDaoImpl.KnockoffTTUMdata",
					new Exception(" ReconProcessDaoImpl.KnockoffTTUMdata", e));
			return false;
		}

	}

//ADDED BY INT8624 FOR UPDATING RESPONSECODE IN ONUS_POS 
	public void getResponseCodeForOnusPos(String fileDate) throws ParseException, Exception {
		try {
			String tableName = "RESPONSECODE_ONUS_POS_DATA";
			// first dropping the table if exists
			if (getJdbcTemplate().queryForObject("SELECT count (*) FROM tab WHERE tname  = '" + tableName + "'",
					new Object[] {}, Integer.class) > 0) {
				getJdbcTemplate().execute("DROP TABLE " + tableName);
				logger.info("table dropped");
			}

			logger.info("Now Creating table");
			String createTemp = "CREATE TABLE " + tableName
					+ " AS ( SELECT DISTINCT (T1.DCRS_REMARKS || ' ('||T2.RESPCODE||')') AS DCRS_REMARKS,t1.SEG_TRAN_ID,t1.CREATEDBY,t1.CREATEDDATE,t1.FILEDATE,t1.FORACID,t1.TRAN_DATE,t1.E,t1.AMOUNT,t1.BALANCE,t1.TRAN_ID,t1.VALUE_DATE,t1.REMARKS,t1.REF_NO,"
					+ "t1.PARTICULARALS,t1.CONTRA_ACCOUNT,t1.PSTD_USER_ID,t1.ENTRY_DATE,t1.VFD_DATE,t1.PARTICULARALS2,t1.ORG_ACCT,t1.SYS_REF,t1.MAN_CONTRA_ACCOUNT "
					+ "FROM SETTLEMENT_pos_CBS t1 INNER JOIN SETTLEMENT_pos_SWITCH t2 ON(  ( t1.REMARKS =  t2.PAN) AND  (SUBSTR( t1.REF_NO,2,6) =  SUBSTR( t2.TRACE,2,6)) "
					+ "AND  TO_NUMBER( REPLACE(t1.AMOUNT,',','')) =  TO_NUMBER( REPLACE(t2.AMOUNT,',','')) ) where t1.DCRS_REMARKS = 'POS_ONU_CBS_UNMATCHED' AND t2.DCRS_REMARKS = 'POS_ONU' "
					+ "and T1.FILEDATE = '" + fileDate + "')";
			logger.info("create table query is " + createTemp);

			getJdbcTemplate().execute(createTemp);

			String deleteFromSettlement = "DELETE from SETTLEMENT_POS_CBS OS1 WHERE FILEDATE = '" + fileDate
					+ "' AND OS1.DCRS_REMARKS= 'POS_ONU_CBS_UNMATCHED' AND EXISTS " + "(SELECT 1 FROM " + tableName
					+ " OS2 WHERE OS1.REMARKS = OS2.REMARKS AND SUBSTR( OS1.REF_NO,2,6) = SUBSTR( OS2.REF_NO,2,6) "
					+ "AND TO_NUMBER( REPLACE(OS1.AMOUNT,',','')) = TO_NUMBER( REPLACE(OS2.AMOUNT,',','')))";
			logger.info("DELETE QUERY IS " + deleteFromSettlement);
			getJdbcTemplate().execute(deleteFromSettlement);

			String InsertQuery = "INSERT INTO SETTLEMENT_POS_CBS (DCRS_REMARKS,SEG_TRAN_ID,CREATEDBY,CREATEDDATE,FILEDATE,FORACID,TRAN_DATE,E,AMOUNT,BALANCE,TRAN_ID,VALUE_DATE,REMARKS,REF_NO,"
					+ "PARTICULARALS,CONTRA_ACCOUNT,PSTD_USER_ID,ENTRY_DATE,VFD_DATE,PARTICULARALS2,ORG_ACCT,SYS_REF,MAN_CONTRA_ACCOUNT) ( SELECT * FROM "
					+ tableName + " )";
			logger.info("Insert query is " + InsertQuery);
			getJdbcTemplate().execute(InsertQuery);

			logger.info("Completed inserting in settlement table");

			// DROPPING TEMP TABLE
			getJdbcTemplate().execute("DROP TABLE " + tableName);
			logger.info("Table dropped");

		} catch (Exception e) {
			logger.info("Exception in getResponseCodeForOnusPos " + e);
			demo.logSQLException(e, "ReconProcessDaoImpl.getResponseCodeForOnusPos");
		}
	}

	// RUPAY DOM CLASSIFICATION
	public boolean DomesticClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			String passdate = generalUtil.DateFunction(filedate);
			logger.info("***** ReconProcessDaoImpl.DomesticClassifydata Start ****");

			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			// inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_FILE_DATE", passdate);
			inParams.put("I_ENTRY_BY", entry_by);

			DomesticClassificaton acqclassificaton = new DomesticClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.DomesticClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticClassifydata",
					new Exception(" ReconProcessDaoImpl.DomesticClassifydata", e));
			return false;
		}

	}

	class DomesticClassificaton extends StoredProcedure {
		private static final String procName = "RUPAY_DOM_CLASSIFY";

		DomesticClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean InternationalClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.DomesticClassifydata Start ****");

			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILEDATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			InternationalClassificaton acqclassificaton = new InternationalClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.DomesticClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticClassifydata",
					new Exception(" ReconProcessDaoImpl.DomesticClassifydata", e));
			return false;
		}

	}

	class InternationalClassificaton extends StoredProcedure {
		private static final String procName = "RUPAY_INT_CLASSIFY";

		InternationalClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILEDATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean DomesticComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {

		String monthdate = generalUtil.DateFunction(filedate);

		try {
			logger.info("***** ReconProcessDaoImpl.DomesticComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			// inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_FILE_DATE", monthdate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_ENTRY_BY", entry_by);

			DomesticCompare issCompare = new DomesticCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.DomesticComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticComparedata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticComparedata",
					new Exception(" ReconProcessDaoImpl.DomesticComparedata", e));
			return false;
		}

	}

	class DomesticCompare extends StoredProcedure {
		private static final String procName = "RECON_RUPAY_DOM_PROC";

		DomesticCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean InternationalComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.DomesticComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("FILEDT", filedate);
			inParams.put("USER_ID", entry_by);

			InternationalCompare issCompare = new InternationalCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.DomesticComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticComparedata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticComparedata",
					new Exception(" ReconProcessDaoImpl.DomesticComparedata", e));
			return false;
		}

	}

	class InternationalCompare extends StoredProcedure {
		private static final String procName = "RECON_RUPAY_INT";

		InternationalCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("FILEDT", Types.VARCHAR));
			declareParameter(new SqlParameter("USER_ID", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// VISA ISSUER CLASSIFICATION
	public boolean VisaIssClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.VisaIssClassifydata Start ****");

			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			VisaIssClassificaton acqclassificaton = new VisaIssClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.VisaIssClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.VisaIssClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.VisaIssClassifydata",
					new Exception(" ReconProcessDaoImpl.VisaIssClassifydata", e));
			return false;
		}

	}

	class VisaIssClassificaton extends StoredProcedure {
		private static final String procName = "VISA_ISS_CLASSIFY";

		VisaIssClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// VISA ISSUER COMPARE
	public boolean VisaCompareData(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.DomesticComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_ENTRY_BY", entry_by);

			VisaIssCompare issCompare = new VisaIssCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.DomesticComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticComparedata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticComparedata",
					new Exception(" ReconProcessDaoImpl.DomesticComparedata", e));
			return false;
		}

	}

	class VisaIssCompare extends StoredProcedure {
		private static final String procName = "RECON_VISA_ISS_PROC";

		VisaIssCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// VISA ISSUER COMPARE
	public boolean VisaAcqCompareData(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.DomesticComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_ENTRY_BY", entry_by);

			VisaAcqCompare issCompare = new VisaAcqCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.DomesticComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.DomesticComparedata");
			logger.error(" error in  ReconProcessDaoImpl.DomesticComparedata",
					new Exception(" ReconProcessDaoImpl.DomesticComparedata", e));
			return false;
		}

	}

	class VisaAcqCompare extends StoredProcedure {
		private static final String procName = "RECON_VISA_ACQ_PROC";

		VisaAcqCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	// VISA ISSUER CLASSIFICATION
	public boolean VisaAcqClassifydata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.VisaIssClassifydata Start ****");

			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", subCat);
			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			VisaAcqClassificaton acqclassificaton = new VisaAcqClassificaton(getJdbcTemplate());
			Map<String, Object> outParams = acqclassificaton.execute(inParams);

			// logger.info("outParams msg1"+outParams.get("msg1"));
			logger.info("***** ReconProcessDaoImpl.VisaIssClassifydata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.VisaIssClassifydata");
			logger.error(" error in  ReconProcessDaoImpl.VisaIssClassifydata",
					new Exception(" ReconProcessDaoImpl.VisaIssClassifydata", e));
			return false;
		}

	}

	class VisaAcqClassificaton extends StoredProcedure {
		private static final String procName = "VISA_ACQ_CLASSIFY";

		VisaAcqClassificaton(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean CardtoCardCompareData(String category, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.CardtoCardCompareData Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_ENTRY_BY", entry_by);

			CardtoCardIssCompare issCompare = new CardtoCardIssCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.CardtoCardCompareData End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.CardtoCardCompareData");
			logger.error(" error in  ReconProcessDaoImpl.CardtoCardCompareData",
					new Exception(" ReconProcessDaoImpl.CardtoCardCompareData", e));
			return false;
		}

	}

	class CardtoCardIssCompare extends StoredProcedure {
		private static final String procName = "RECON_CARDTOCARD_PROC";

		CardtoCardIssCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean CashnetIssCompareData(String category, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.CashnetCompareData Issuer Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", "");
			inParams.put("I_ENTRY_BY", entry_by);

			CashnetIssCompare issCompare = new CashnetIssCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.CardtoCardCompareData End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.CardtoCardCompareData");
			logger.error(" error in  ReconProcessDaoImpl.CardtoCardCompareData",
					new Exception(" ReconProcessDaoImpl.CardtoCardCompareData", e));
			return false;
		}

	}

	class CashnetIssCompare extends StoredProcedure {
		private static final String procName = "RECON_CASHNET_ISS_PROC";

		CashnetIssCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	public boolean CashnetAcqCompareData(String category, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.CashnetCompareData Acquirer Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("I_FILE_DATE", filedate);
			inParams.put("I_CATEGORY", category);
			inParams.put("I_SUBCATEGORY", "");
			inParams.put("I_ENTRY_BY", entry_by);

			CashnetAcqCompare issCompare = new CashnetAcqCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.CardtoCardCompareData End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.CardtoCardCompareData");
			logger.error(" error in  ReconProcessDaoImpl.CardtoCardCompareData",
					new Exception(" ReconProcessDaoImpl.CardtoCardCompareData", e));
			return false;
		}

	}

	class CashnetAcqCompare extends StoredProcedure {
		private static final String procName = "RECON_CASHNET_ACQ_PROC";

		CashnetAcqCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("I_FILE_DATE", Types.VARCHAR));
			declareParameter(new SqlParameter("I_CATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_SUBCATEGORY", Types.VARCHAR));
			declareParameter(new SqlParameter("I_ENTRY_BY", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_CODE", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	@Override
	public HashMap<String, Object> checkRupayIntRecon(String fileDate) {
		HashMap<String, Object> output = new HashMap<String, Object>();

		try {
			String checkReconProcess = "select COUNT(*) from main_file_upload_dtls T1 where filedate = ? AND FILE_SUBCATEGORY = 'INTERNATIONAL' "
					+ "AND CATEGORY = 'RUPAY' AND T1.COMAPRE_FLAG = 'Y' "
					+ "and filter_flag = (SELECT FILTERATION FROM MAIN_FILESOURCE T2 WHERE T1.FILEID = T2.FILEID AND T1.CATEGORY = T2.FILE_CATEGORY "
					+ "AND T1.FILE_SUBCATEGORY = T2.FILE_SUBCATEGORY AND T1.FILTER_FLAG = T2.FILTERATION AND T1.KNOCKOFF_FLAG = T2.KNOCKOFF) ";

			int checkReconCount = getJdbcTemplate().queryForObject(checkReconProcess, new Object[] { fileDate },
					Integer.class);

			if (checkReconCount > 0) {
				output.put("result", true);
			} else {
				output.put("result", false);
				output.put("msg", "Recon is not processed");
			}

		} catch (Exception e) {
			logger.info("Exception in checkRupayIntRecon " + e);
			output.put("result", false);
			output.put("msg", "Exception in checkRupayIntRecon");
		}
		return output;
	}

	@Override
	public HashMap<String, Object> processRupayIntRecon(String fileDate, String entryBy) {
		HashMap<String, Object> output = new HashMap<String, Object>();

		try {
			logger.info("***** ReconProcessDaoImpl.processRupayIntRecon Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("FILEDT", fileDate);
			inParams.put("USER_ID", entryBy);

			RupayIntProcess issCompare = new RupayIntProcess(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.DomesticComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				output.put("result", false);
				output.put("msg", "Recon not processed");
			} else {
				output.put("result", true);
			}

		} catch (Exception e) {
			output.put("result", false);
			output.put("msg", "Exception is " + e);
		}
		return output;
	}

	class RupayIntProcess extends StoredProcedure {
		private static final String procName = "RECON_RUPAY_INT";

		RupayIntProcess(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("FILEDT", Types.VARCHAR));
			declareParameter(new SqlParameter("USER_ID", Types.VARCHAR));
			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

	@Override
	public HashMap<String, Object> checkCardtoCardRecon(String fileDate) {
		HashMap<String, Object> output = new HashMap<String, Object>();

		try {
			String checkReconProcess = "select COUNT(*) from main_file_upload_dtls T1 where filedate = ? "
					+ "AND CATEGORY = 'CARDTOCARD' AND T1.COMAPRE_FLAG = 'Y' "
					+ "and filter_flag = (SELECT FILTERATION FROM MAIN_FILESOURCE T2 WHERE T1.FILEID = T2.FILEID AND T1.CATEGORY = T2.FILE_CATEGORY "
					+ "AND T1.FILE_SUBCATEGORY = T2.FILE_SUBCATEGORY AND T1.FILTER_FLAG = T2.FILTERATION AND T1.KNOCKOFF_FLAG = T2.KNOCKOFF) ";

			int checkReconCount = getJdbcTemplate().queryForObject(checkReconProcess, new Object[] { fileDate },
					Integer.class);

			if (checkReconCount > 0) {
				output.put("result", true);
				output.put("msg", "Recon is already Processed");
			} else {
				output.put("result", false);
				output.put("msg", "Recon is not processed");
			}

		} catch (Exception e) {
			logger.info("Exception in checkRupayIntRecon " + e);
			output.put("result", false);
			output.put("msg", "Exception in checkRupayIntRecon");
		}
		return output;
	}

	public HashMap<String, Object> checkCardtoCardRawFiles(String filedate) {
		HashMap<String, Object> output = new HashMap<String, Object>();

		try {
			String checkHostFileUpload = "SELECT COUNT(*) FROM MAIN_FILE_UPLOAD_DTLS WHERE FILEDATE = ? AND CATEGORY = 'CARDTOCARD'";
			String checkNFSFileUpload = "SELECT COUNT(*) FROM MAIN_FILE_UPLOAD_DTLS WHERE FILEDATE = ? AND CATEGORY = 'NFS' AND FILE_SUBCATEGORY = 'ISSUER'"
					+ " AND FILEID = (SELECT FILEID FROM MAIN_FILESOURCE WHERE FILENAME = 'NFS' AND FILE_CATEGORY = 'CARDTOCARD' AND FILE_SUBCATEGORY = 'ISSUER')";

			int hostFileCount = getJdbcTemplate().queryForObject(checkHostFileUpload, new Object[] { filedate },
					Integer.class);
			int NFSFileCount = getJdbcTemplate().queryForObject(checkNFSFileUpload, new Object[] { filedate },
					Integer.class);

			if (hostFileCount == 0) {
				output.put("result", false);
				output.put("msg", "Host File is not uploaded");
			} else if (NFSFileCount == 0) {
				output.put("result", false);
				output.put("msg", "NFS File is not uploaded");
			} else {
				output.put("result", true);
			}

		} catch (Exception e) {
			logger.info("Exception in checkCardtoCardRawFiles " + e);
			output.put("result", false);
			output.put("msg", "Exception while checking file upload");
		}
		return output;
	}

	public HashMap<String, Object> checkCardtoCardPrevRecon(String filedate) {
		HashMap<String, Object> output = new HashMap<String, Object>();
		try {
			String checkPrevRecon = "SELECT COUNT(*) FROM main_file_upload_dtls where filedate = to_date('" + filedate
					+ "','dd/mm/yyyy')-1 and category = 'CARDTOCARD'";

			int prevCount = getJdbcTemplate().queryForObject(checkPrevRecon, new Object[] {}, Integer.class);

			if (prevCount == 0) {
				output.put("result", false);
				output.put("msg", "Previous day recon is not processed!");
			} else {
				output.put("result", true);
			}

		} catch (Exception e) {
			logger.info("Exception in checkCardtoCardPrevRecon " + e);
			output.put("result", false);
			output.put("msg", "Exception while checking previous date recon!");
		}
		return output;
	}

	public boolean OnusComparedata(String category, String subCat, String filedate, String entry_by)
			throws ParseException, Exception {
		try {
			logger.info("***** ReconProcessDaoImpl.OnusComparedata Start ****");
			String response = null;
			Map<String, Object> inParams = new HashMap<String, Object>();

			inParams.put("i_filedate", filedate);
			inParams.put("i_entry_by", entry_by);

			OnusCompare issCompare = new OnusCompare(getJdbcTemplate());
			Map<String, Object> outParams = issCompare.execute(inParams);

			logger.info("***** ReconProcessDaoImpl.OnusComparedata End ****");

			if (outParams.get("ERROR_MESSAGE") != null) {

				return false;
			} else {

				return true;
			}

		} catch (Exception e) {
			demo.logSQLException(e, "ReconProcessDaoImpl.OnusComparedata");
			logger.error(" error in  ReconProcessDaoImpl.OnusComparedata",
					new Exception(" ReconProcessDaoImpl.OnusComparedata", e));
			return false;
		}

	}

	class OnusCompare extends StoredProcedure {
		private static final String procName = "recon_onus_proc";

		OnusCompare(JdbcTemplate JdbcTemplate) {
			super(JdbcTemplate, procName);
			setFunction(false);

			declareParameter(new SqlParameter("i_filedate", Types.VARCHAR));
			declareParameter(new SqlParameter("i_entry_by", Types.VARCHAR));

			declareParameter(new SqlOutParameter("ERROR_MESSAGE", Types.VARCHAR));
			compile();
		}
	}

}
