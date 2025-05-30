package com.recon.dao.impl;

import static com.recon.util.GeneralUtil.GET_COLS;
import static com.recon.util.GeneralUtil.GET_FILE_HEADERS;
import static com.recon.util.GeneralUtil.GET_FILE_ID;
import static com.recon.util.GeneralUtil.GET_SETTLEMENT_ID;
import static com.recon.util.GeneralUtil.GET_SETTLEMENT_PARAM;
import static com.recon.util.GeneralUtil.insertBatch;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.hibernate.annotations.Columns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import com.informix.util.stringUtil;
import com.mastercard.main.ChargeBackImpl;
import com.mastercard.model.MasterCardModel;
import com.recon.dao.ISettelmentDao;
import com.recon.model.FilterationBean;
import com.recon.model.GenerateTTUMBean;
import com.recon.model.Gl_bean;
import com.recon.model.Mastercard_chargeback;
import com.recon.model.Rupay_Gl_repo;
import com.recon.model.Rupay_gl_Lpcases;
import com.recon.model.Rupay_gl_autorev;
import com.recon.model.Rupay_sur_GlBean;
import com.recon.model.SessionModel;
import com.recon.model.SettlementBean;
import com.recon.model.SettlementTypeBean;
import com.recon.util.GeneralUtil;
import com.recon.util.OracleConn;
import com.recon.util.Utility;
import com.recon.util.demo;

@Component
public class SettlmentDaoImpl extends JdbcDaoSupport implements ISettelmentDao {

	@Autowired
	GeneralUtil generalUtil;

	@Override
	public ArrayList<String> gettype(String tableName) {

		String query = "SELECT distinct remarks FROM " + tableName + "";
		System.out.println(query);

		ArrayList<String> typeList = (ArrayList<String>) getJdbcTemplate().query(query, new RowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});

		return typeList;
	}

	@Override
	public List<SettlementTypeBean> getSettlmentType(String type, String tablename) {

		String query = "SELECT  * FROM " + tablename + " where remarks='" + type + "'";
		System.out.println(query);

		List<SettlementTypeBean> typeList = getJdbcTemplate().query(query,
				new BeanPropertyRowMapper(SettlementTypeBean.class));

		return typeList;
	}

	@Override
	public ArrayList<SettlementTypeBean> getReconData(String tableName, String type, String date, String searchValue) {

		// getReconData1(tableName, type, date, searchValue);

		ArrayList<SettlementTypeBean> settlementTypeBeans;

		// String character = tableName.next();
		String result = null;
		String splitype[] = null;
		String formTablename = null;
		char c = tableName.charAt(0);

		if (Character.isDigit(c)) {

			result = formTablename;
			String getTable = "select filename from main_filesource where fileid=" + tableName + "";

			formTablename = getJdbcTemplate().queryForObject(getTable, String.class);
			String stcat = getJdbcTemplate().queryForObject(
					"SELECT FILE_CATEGORY FROM MAIN_FILESOURCE WHERE FILEID = ?", new Object[] { tableName },
					String.class);
			// splitype=type.split("\\_");
			tableName = "SETTLEMENT" + "_" + stcat + "_" + formTablename;
		}

		String split_table[] = tableName.split("_");

		String concat_table = split_table[0] + "_" + split_table[2];

		try {
			String query = "";

			settlementTypeBeans = null;

			if (concat_table.trim().equals("SETTLEMENT_SWITCH")) {
				if (split_table[1].equals("AMEX") || split_table[1].equals("RUPAY")) {

					query = "SELECT PAN,TERMID,TRACE,dcrs_remarks FROM " + tableName + " WHERE   dcrs_remarks='" + type
							+ "' " // rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND (PAN LIKE '%" + searchValue + "%' OR TERMID LIKE '%" + searchValue
								+ "%'  OR TRACE LIKE '%" + searchValue + "%')";

					}
					;

				} else if (split_table[1].equals("ONUS")) {
					query = "SELECT PAN,TERMID,TRACE,dcrs_remarks FROM " + tableName + " WHERE   dcrs_remarks='" + type
							+ "' " // rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND (PAN LIKE '%" + searchValue + "%' OR TERMID LIKE '%" + searchValue
								+ "%'  OR TRACE LIKE '%" + searchValue + "%')";

					}
					;

					System.out.println(query);

				}
			} else if (concat_table.trim().equals("SETTLEMENT_CBS")) {

				if (split_table[1].equals("AMEX") || split_table[1].equals("RUPAY")) {

					query = "SELECT  foracid,CONTRA_ACCOUNT,Tran_Date,dcrs_remarks ,PARTICULARALS  FROM " + tableName
							+ " WHERE dcrs_remarks='" + type + "' "// rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND( foracid LIKE '%" + searchValue + "%' OR CONTRA_ACCOUNT LIKE '%"
								+ searchValue + "%' " + "PARTICULARALS '%" + searchValue + "%')";

					}

				} else if (split_table[1].equals("ONUS")) {
					query = "SELECT  ACCOUNT_NUMBER,CONTRA_ACCOUNT,TranDate,TRAN_PARTICULAR,dcrs_remarks  FROM "
							+ tableName + " WHERE dcrs_remarks='" + type + "' "// rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND( ACCOUNT_NUMBER LIKE '%" + searchValue + "%' OR CONTRA_ACCOUNT LIKE '%"
								+ searchValue + "%' " + " OR TRACE LIKE '%" + searchValue
								+ "%' OR TRAN_PARTICULAR LIKE '%" + searchValue + "%')";

					}

					logger.info(query);

				}
			}

			settlementTypeBeans = (ArrayList<SettlementTypeBean>) getJdbcTemplate().query(query,
					new BeanPropertyRowMapper(SettlementTypeBean.class));

		} catch (Exception ex) {

			ex.printStackTrace();
			settlementTypeBeans = null;
		}
		return settlementTypeBeans;
	}

	// ADDED BY INT5779
	@Override
	public String getFileName(String stfileId) {
		String fileName = "";
		try {
			fileName = getJdbcTemplate().queryForObject("SELECT FILENAME FROM MAIN_FILESOURCE WHERE FILEID = ?",
					new Object[] { stfileId }, String.class);

		} catch (Exception e) {
			System.out.println("Exception in getFileName");
		}
		return fileName;
	}

	public List<List<String>> getReconData1(String stFileId, String dcrs_remarks, String date, String searchValue) {
		ResultSet rset = null;
		PreparedStatement pstmt = null;
		try {
			String GET_FILENAME = "SELECT FILENAME FROM MAIN_FILESOURCE WHERE FILEID = ?";
			String fileName = getJdbcTemplate().queryForObject(GET_FILENAME, new Object[] { stFileId }, String.class);

			String stcategory = getJdbcTemplate().queryForObject(
					"SELECT FILE_CATEGORY FROM MAIN_FILESOURCE WHERE FILEID = ?", new Object[] { stFileId },
					String.class);
			String stTableName = "SETTLEMENT_" + stcategory + "_" + fileName;

			String stsubcategory = getJdbcTemplate().queryForObject(
					"SELECT FILE_SUBCATEGORY FROM MAIN_FILESOURCE WHERE FILEID = ?", new Object[] { stFileId },
					String.class);

			if (stcategory.equals("NFS") || stcategory.equals("CASHNET")) {

				stTableName = "SETTLEMENT_" + stcategory + "_" + stsubcategory.substring(0, 3) + "_" + fileName;
			}

			List<String> Column_list = getColumnList(stTableName);
			final List<String> cols = getColumnList(stTableName);

			// int column_count = getJdbcTemplate().queryForObject("SELECT
			// COUNT(column_name) FROM all_tab_cols WHERE table_name = ?",new Object[]
			// {stTableName},Integer.class);
			String columns = "";
			for (int i = 0; i < Column_list.size(); i++) {
				if (i == (Column_list.size() - 1)) {
					columns = columns + Column_list.get(i);
				} else {
					columns = columns + Column_list.get(i) + ",";
				}

			}
			String GET_DATA = "";
			if (dcrs_remarks.contains("-UNRECON"))
				GET_DATA = "SELECT " + columns + " FROM " + stTableName + " WHERE (DCRS_REMARKS LIKE '%" + dcrs_remarks
						+ "%' OR DCRS_REMARKS LIKE '%GENERATED%') AND TO_DATE(FILEDATE,'DD/MM/YY') = TO_DATE('" + date
						+ "','DD/MM/YY')";
			else
				GET_DATA = "SELECT " + columns + " FROM " + stTableName + " WHERE DCRS_REMARKS = '" + dcrs_remarks
						+ "' AND TO_DATE(FILEDATE,'DD/MM/YY') = TO_DATE('" + date + "','DD/MM/YY')";

			/*
			 * if(stcategory.equals("NFS") &&
			 * dcrs_remarks.equalsIgnoreCase("MATCHED_UNSUCCESSFUL")) {
			 */
			if (stcategory.equals("NFS") && (dcrs_remarks.equalsIgnoreCase("MATCHED_UNSUCCESSFUL")
					|| dcrs_remarks.equalsIgnoreCase("NFS-UPI") || dcrs_remarks.equalsIgnoreCase("NFS-DFS")
					|| dcrs_remarks.equalsIgnoreCase("NFS-JCB"))) {
				GET_DATA = "SELECT " + columns + " FROM " + stTableName + " WHERE DCRS_REMARKS like '" + dcrs_remarks
						+ "%' AND TO_DATE(FILEDATE,'DD/MM/YY') = TO_DATE('" + date + "','DD/MM/YY')";

			}

			List<List<String>> DATA = new ArrayList<List<String>>();
			// 1st raw will be headers
			DATA.add(Column_list);

			/*
			 * List<List<String>> final_data = getJdbcTemplate().query(GET_DATA,new Object[]
			 * {} , new RowMapper(){ public List<String> mapRow(ResultSet rset, int
			 * row)throws SQLException { List<String> DbData1 = new ArrayList<String>();
			 * for(String colName : cols) { if(rset.getString(colName) == null)
			 * DbData1.add(" "); else DbData1.add(rset.getString(colName)); } //return
			 * "abc"; return DbData1;
			 * 
			 * } });
			 */

			pstmt = getConnection().prepareStatement(GET_DATA);
			rset = pstmt.executeQuery();

			while (rset.next()) {
				List<String> DbData = new ArrayList<String>();
				for (String colName : Column_list) {

					if (rset.getString(colName) == null)
						DbData.add("");
					else

						DbData.add(rset.getString(colName));
					/*
					 * if(colName.equalsIgnoreCase("PAN") || colName.equalsIgnoreCase("CARD_NUMBER")
					 * || colName.equalsIgnoreCase("PrimaryAccountNumber") ||
					 * colName.equalsIgnoreCase("REMARKS") ) { String card_number =
					 * rset.getString(colName); String MAsk_cardNumber = card_number.substring(1, 6)
					 * + "*******"+ card_number.substring(card_number.length(), -4);
					 * 
					 * DbData.add(MAsk_cardNumber);
					 * 
					 * }else { DbData.add(rset.getString(colName)); }
					 */
				}
				DATA.add(DbData);
			}

			return DATA;

		} catch (Exception e) {
			System.out.println("Exception is " + e);
			return null;
		} finally {
			try {
				if (rset != null) {
					pstmt.close();
					rset.close();

				}
			} catch (Exception e) {
				System.out.println("Exception in closing rset");
			}
		}

	}

	@Override
	public ArrayList<String> getColumnList(String tableName) {

		String query = "SELECT column_name FROM   all_tab_cols WHERE  table_name = '" + tableName.toUpperCase()
				+ "' and owner = 'DEBITCARD_RECON' and column_name not like '%$%'";
		System.out.println(query);

		ArrayList<String> typeList = (ArrayList<String>) getJdbcTemplate().query(query, new RowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});

		System.out.println(typeList);
		return typeList;

	}

	@Override
	public int getReconDataCount(String table, String type, String date, String searchValue) {

		String query = "";

		int count = 0;

		String split_table[] = table.split("_");

		String concat_table = split_table[0] + "_" + split_table[2];
		try {
			if (concat_table.equals("SETTLEMENT_SWITCH")) {
				query = "SELECT count(*) FROM " + table + " WHERE   dcrs_remarks='" + type + "' " // rownum <= 500 AND
						+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

				if (!searchValue.equals("")) {

					query = query + " AND (PAN LIKE '%" + searchValue + "%' OR TERMID LIKE '%" + searchValue
							+ "%'  OR TRACE LIKE '%" + searchValue + "%')";

				}

				logger.info(query);

			} else if (concat_table.trim().equals("SETTLEMENT_CBS")) {

				query = "SELECT  count(*)  FROM " + table + " WHERE dcrs_remarks='" + type + "' "// rownum <= 500 AND
						+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

				if (!searchValue.equals("")) {

					query = query + " AND( ACCOUNT_NUMBER LIKE '%" + searchValue + "%' OR CONTRA_ACCOUNT LIKE '%"
							+ searchValue + "%' " + " OR TRACE LIKE '%" + searchValue + "%')";

				}

				logger.info(query);

			}

			count = getJdbcTemplate().queryForObject(query, Integer.class);
		} catch (Exception ex) {

			ex.printStackTrace();
		}

		return count;
	}

	@Override
	public ArrayList<SettlementTypeBean> getChngReconData(String table, String type, String date, String searchValue,
			int jtStartIndex, int jtPageSize) {

		ArrayList<SettlementTypeBean> settlementTypeBeans;
		String split_table[] = table.split("_");

		String concat_table = split_table[0] + "_" + split_table[2];
		try {
			String query = "";

			settlementTypeBeans = null;

			if (concat_table.trim().equals("SETTLEMENT_SWITCH")) {
				if (split_table[1].equals("AMEX") || split_table[1].equals("RUPAY")) {

					query = "SELECT * FROM ( SELECT ROWNUM,PAN,TERMID,TRACE,FileDate,dcrs_remarks , '" + table
							+ "' as SETLTBL, ROW_NUMBER() OVER( ORDER BY PAN) AS RN FROM " + table
							+ " WHERE   dcrs_remarks='" + type + "' " // rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND (PAN LIKE '%" + searchValue + "%' OR TERMID LIKE '%" + searchValue
								+ "%'  OR TRACE LIKE '%" + searchValue + "%')";

					}
					query = query + ") where RN between " + (jtStartIndex + 1) + " and " + (jtPageSize + jtStartIndex)
							+ "";

					logger.info(query);

				} else if (split_table[1].equals("ONUS")) {
					query = "SELECT * FROM ( SELECT ROWNUM,PAN,TERMID,TRACE,FileDate,dcrs_remarks , '" + table
							+ "' as SETLTBL, ROW_NUMBER() OVER( ORDER BY PAN) AS RN FROM " + table
							+ " WHERE   dcrs_remarks='" + type + "' " // rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND (PAN LIKE '%" + searchValue + "%' OR TERMID LIKE '%" + searchValue
								+ "%'  OR TRACE LIKE '%" + searchValue + "%')";

					}
					query = query + ") where RN between " + (jtStartIndex + 1) + " and " + (jtPageSize + jtStartIndex)
							+ "";

					logger.info(query);

				}
			} else if (concat_table.trim().equals("SETTLEMENT_CBS")) {
				if (split_table[1].equals("AMEX") || split_table[1].equals("RUPAY")) {

					query = "SELECT * FROM ( SELECT  foracid,CONTRA_ACCOUNT,Tran_Date,dcrs_remarks,PARTICULARALS ,'"
							+ table + "' as SETLTBL, ROW_NUMBER() OVER( ORDER BY foracid ) AS RN  FROM " + table
							+ " WHERE dcrs_remarks='" + type + "' "// rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND( foracid LIKE '%" + searchValue + "%' OR CONTRA_ACCOUNT LIKE '%"
								+ searchValue + "%' OR PARTICULARALS LIKE '%" + searchValue + "%')";

					}
					query = query + ") where RN between " + (jtStartIndex + 1) + " and " + (jtPageSize + jtStartIndex)
							+ "";

					logger.info(query);

				} else if (split_table[1].equals("ONUS")) {
					query = "SELECT * FROM ( SELECT  ACCOUNT_NUMBER,CONTRA_ACCOUNT,TranDate,FileDate,TRAN_PARTICULAR,remarks ,'"
							+ table + "' as SETLTBL, ROW_NUMBER() OVER( ORDER BY ACCOUNT_NUMBER ) AS RN  FROM " + table
							+ " WHERE remarks='" + type + "' "// rownum <= 500 AND
							+ " AND to_date(filedate,'dd/mm/yy')= to_date('" + date + "','dd/mm/yy') ";

					if (!searchValue.equals("")) {

						query = query + " AND( ACCOUNT_NUMBER LIKE '%" + searchValue + "%' OR CONTRA_ACCOUNT LIKE '%"
								+ searchValue + "%' " + " OR TRACE LIKE '%" + searchValue
								+ "%' OR TRAN_PARTICULAR LIKE '%" + searchValue + "%')";

					}
					query = query + ") where RN between " + (jtStartIndex + 1) + " and " + (jtPageSize + jtStartIndex)
							+ "";

					logger.info(query);

				}
			}

			settlementTypeBeans = (ArrayList<SettlementTypeBean>) getJdbcTemplate().query(query,
					new BeanPropertyRowMapper(SettlementTypeBean.class));

		} catch (Exception ex) {

			ex.printStackTrace();
			settlementTypeBeans = null;
		}

		return settlementTypeBeans;

	}

	@Override
	public void manualReconToSettlement(String table_Name, String stFile_date) throws Exception {

		String[] a = table_Name.split("_");
		String table_cols = "CREATEDDATE DATE,CREATEDBY VARCHAR(100 BYTE),FILEDATE DATE, DCR_TRAN_ID NUMBER, REMARKS VARCHAR(100 BYTE)";
		String CREATE_QUERY = "";
		String CHECK_TABLE = "";
		String temp_param = "";
		String condition = "";
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rset = null;

		try {

			// check if table exist if not then create it
			int file_id = getJdbcTemplate().queryForObject(GET_FILE_ID, new Object[] { a[2], a[0] }, Integer.class);
			logger.info("File id is " + file_id);

			String stFile_headers = getJdbcTemplate().queryForObject(GET_FILE_HEADERS, new Object[] { file_id },
					String.class);
			// String Update_cols = stFile_headers;
			stFile_headers = "MAN_CONTRA_ACCOUNT," + stFile_headers;

			CHECK_TABLE = "SELECT count (*) FROM tab WHERE tname  = 'SETTLEMENT_" + a[2].toUpperCase() + "'";
			int tableExist = getJdbcTemplate().queryForObject(CHECK_TABLE, new Object[] {}, Integer.class);

			logger.info("CHECK TABLE QUERY IS " + CHECK_TABLE);
			// get connection
			conn = getConnection();
			if (tableExist == 0) {
				String[] cols = stFile_headers.split(",");
				for (int i = 0; i < cols.length; i++) {
					table_cols = table_cols + "," + cols[i] + " VARCHAR(100 BYTE)";
				}

				CREATE_QUERY = "CREATE TABLE SETTLEMENT_" + a[3] + " (" + table_cols + ")";
				logger.info("CREATE TABLE QUERY IS " + CREATE_QUERY);
				pstmt = conn.prepareStatement(CREATE_QUERY);
				rset = pstmt.executeQuery();

				pstmt = null;
				rset = null;

			} else if (tableExist == 1 && a[2].equalsIgnoreCase("CBS")) {
				boolean CheckForCol = false;
				// now check for the field MAN_CONTRA_ACCOUNT IF NOT PRESENT THEN ALTER TABLE
				List<String> Columns = getJdbcTemplate().query(GET_COLS, new Object[] { "SETTLEMENT_CBS" },
						new ColumnsMapper());

				for (int i = 0; i < Columns.size(); i++) {
					if (Columns.get(i).equalsIgnoreCase("MAN_CONTRA_ACCOUNT")) {
						CheckForCol = true;
					}
				}

				if (!CheckForCol) {
					String ALTER_QUERY = "ALTER TABLE SETTLEMENT_" + a[2]
							+ " ADD MAN_CONTRA_ACCOUNT VARCHAR2(100 BYTE)";
					getJdbcTemplate().execute(ALTER_QUERY);
				}
			}

			// ------------------------------------------ FILTER MANUAL RECON RECORDS USING
			// CRITEIRA PROVIDED IN
			// DOC----------------------------------------------------------

			List<SettlementBean> settlement_id = getJdbcTemplate().query(GET_SETTLEMENT_ID, new Object[] { file_id },
					new SettlementId());

			for (int id = 0; id < settlement_id.size(); id++) {
				condition = "";
				logger.info("SETTLEMENT ID IS " + settlement_id.get(id).getInSettlement_id());
				List<FilterationBean> search_params = getJdbcTemplate().query(GET_SETTLEMENT_PARAM,
						new Object[] { file_id, settlement_id.get(id).getInSettlement_id() },
						new SearchParameterMaster());

				logger.info("got the search params" + search_params.size());
				for (int i = 0; i < search_params.size(); i++) {
					FilterationBean filterBeanObj = new FilterationBean();
					filterBeanObj = search_params.get(i);
					temp_param = filterBeanObj.getStSearch_header().trim();

					if ((filterBeanObj.getStSearch_padding().trim()).equals("Y")) {

						if ((filterBeanObj.getStSearch_Condition().trim()).equalsIgnoreCase("like")) {
							condition = condition + "(SUBSTR(TRIM(" + filterBeanObj.getStSearch_header() + "),"
									+ filterBeanObj.getStsearch_Startcharpos() + ","
									+ filterBeanObj.getStsearch_Endcharpos() + ") "
									+ filterBeanObj.getStSearch_Condition().trim() + "'%"
									+ filterBeanObj.getStSearch_pattern().trim() + "%' ";
						} else if ((filterBeanObj.getStSearch_Condition().trim()).equals("!=")) {
							if (i == (search_params.size() - 1)) {
								condition = condition + "(SUBSTR(TRIM(NVL(" + filterBeanObj.getStSearch_header()
										+ ",'!NULL!'))," + filterBeanObj.getStsearch_Startcharpos() + ","
										+ filterBeanObj.getStsearch_Endcharpos() + ") " + "NOT IN ('"
										+ filterBeanObj.getStSearch_pattern().trim() + "') ";
							} else {

								condition = condition + "(SUBSTR(TRIM(NVL(" + filterBeanObj.getStSearch_header()
										+ ",'!NULL!'))," + filterBeanObj.getStsearch_Startcharpos() + ","
										+ filterBeanObj.getStsearch_Endcharpos() + ") " + "NOT IN ('"
										+ filterBeanObj.getStSearch_pattern().trim() + "' ";
							}
						} else {
							if (filterBeanObj.getStSearch_Datatype() != null) {
								if (filterBeanObj.getStSearch_Datatype().equals("NUMBER")) {
									condition = condition + "(SUBSTR(TRIM(" + filterBeanObj.getStSearch_header() + "),"
											+ filterBeanObj.getStsearch_Startcharpos() + ","
											+ filterBeanObj.getStsearch_Endcharpos() + ") "
											+ filterBeanObj.getStSearch_Condition().trim()
											+ filterBeanObj.getStSearch_pattern().trim();
								}
							} else {
								condition = condition + "(SUBSTR(TRIM(" + filterBeanObj.getStSearch_header() + "),"
										+ filterBeanObj.getStsearch_Startcharpos() + ","
										+ filterBeanObj.getStsearch_Endcharpos() + ") "
										+ filterBeanObj.getStSearch_Condition().trim() + "'"
										+ filterBeanObj.getStSearch_pattern().trim() + "' ";
							}

						}

					} else {
						if (filterBeanObj.getStSearch_Condition().equalsIgnoreCase("like")) {
							condition = condition + "(TRIM(" + filterBeanObj.getStSearch_header() + ") "
									+ filterBeanObj.getStSearch_Condition().trim() + " " + "'%"
									+ filterBeanObj.getStSearch_pattern().trim() + "%'";
						} else if ((filterBeanObj.getStSearch_Condition().trim()).equals("!=")) {
							if (i == (search_params.size() - 1)) {
								condition = condition + "(TRIM(NVL(" + filterBeanObj.getStSearch_header()
										+ ",'!NULL!')) " + " NOT IN ('" + filterBeanObj.getStSearch_pattern().trim()
										+ "') ";
							} else {
								condition = condition + "(TRIM(NVL(" + filterBeanObj.getStSearch_header()
										+ ",'!NULL!')) " + " NOT IN ('" + filterBeanObj.getStSearch_pattern().trim()
										+ "' ";
							}
						} else {
							if (filterBeanObj.getStSearch_Datatype() != null) {
								if (filterBeanObj.getStSearch_Datatype().equals("NUMBER")) {
									condition = condition + "(TRIM(" + filterBeanObj.getStSearch_header() + ") "
											+ filterBeanObj.getStSearch_Condition().trim()
											+ filterBeanObj.getStSearch_pattern().trim();
								}
							} else {
								condition = condition + "(TRIM(" + filterBeanObj.getStSearch_header() + ") "
										+ filterBeanObj.getStSearch_Condition().trim() + " '"
										+ filterBeanObj.getStSearch_pattern().trim() + "'";
							}
						}

					}

					for (int j = (i + 1); j < search_params.size(); j++) {
						// System.out.println("CHECK THE VALUE IN J "+j+" value =
						// "+search_params.get(j).getStSearch_header());
						if (temp_param.equals(search_params.get(j).getStSearch_header())) {

							if (search_params.get(j).getStSearch_padding().equals("Y")) {
								if ((search_params.get(j).getStSearch_Condition().trim()).equalsIgnoreCase("like")) {
									condition = condition + " OR SUBSTR(TRIM("
											+ search_params.get(j).getStSearch_header() + ") , "
											+ search_params.get(j).getStsearch_Startcharpos() + ","
											+ search_params.get(j).getStsearch_Endcharpos() + ") "
											+ search_params.get(j).getStSearch_Condition().trim() + "'%"
											+ search_params.get(j).getStSearch_pattern().trim() + "%'";
								} else if ((filterBeanObj.getStSearch_Condition().trim()).equals("!=")) {
									if (j == (search_params.size() - 1)) {
										/*
										 * condition = condition + " OR SUBSTR(" +
										 * search_params.get(j).getStSearch_header()+" , "+search_params.get(j).
										 * getStsearch_Startcharpos()+","+
										 * search_params.get(j).getStsearch_Endcharpos()+") "+search_params.get(j).
										 * getStSearch_Condition()+ search_params.get(j).getStSearch_pattern();
										 */
										condition = condition + ", '"
												+ search_params.get(j).getStSearch_pattern().trim() + "')";
									} else {
										condition = condition + ", '"
												+ search_params.get(j).getStSearch_pattern().trim() + "' ";
									}

								} else {
									if (filterBeanObj.getStSearch_Datatype() != null) {
										if (filterBeanObj.getStSearch_Datatype().equals("NUMBER")) {
											condition = condition + " OR SUBSTR(TRIM("
													+ search_params.get(j).getStSearch_header() + ") , "
													+ search_params.get(j).getStsearch_Startcharpos() + ","
													+ search_params.get(j).getStsearch_Endcharpos() + ") "
													+ search_params.get(j).getStSearch_Condition().trim()
													+ search_params.get(j).getStSearch_pattern().trim();
										}
									} else {
										condition = condition + " OR SUBSTR(TRIM("
												+ search_params.get(j).getStSearch_header() + ") , "
												+ search_params.get(j).getStsearch_Startcharpos() + ","
												+ search_params.get(j).getStsearch_Endcharpos() + ") "
												+ search_params.get(j).getStSearch_Condition().trim() + "'"
												+ search_params.get(j).getStSearch_pattern().trim() + "'";
									}

								}
							} else {
								if ((search_params.get(j).getStSearch_Condition().trim()).equalsIgnoreCase("like")) {
									condition = condition + " OR TRIM(" + search_params.get(j).getStSearch_header()
											+ ") " + search_params.get(j).getStSearch_Condition().trim() + " " + "'%"
											+ search_params.get(j).getStSearch_pattern().trim() + "%'";

								} else if ((filterBeanObj.getStSearch_Condition().trim()).equals("!=")) {
									if (j == (search_params.size() - 1)) {
										condition = condition + " , '"
												+ search_params.get(j).getStSearch_pattern().trim() + "')";
									} else {
										condition = condition + " , '"
												+ search_params.get(j).getStSearch_pattern().trim() + "' ";
									}

								} else {
									System.out.println("check the datatype " + filterBeanObj.getStSearch_Datatype());
									if (filterBeanObj.getStSearch_Datatype() != null) {
										if (filterBeanObj.getStSearch_Datatype().equals("NUMBER")) {
											condition = condition + " OR TRIM("
													+ search_params.get(j).getStSearch_header() + ") "
													+ search_params.get(j).getStSearch_Condition().trim()
													+ search_params.get(j).getStSearch_pattern().trim();
										}
									} else {
										condition = condition + " OR TRIM(" + search_params.get(j).getStSearch_header()
												+ ") " + search_params.get(j).getStSearch_Condition().trim() + " '"
												+ search_params.get(j).getStSearch_pattern().trim() + "'";
									}

								}
							}

							i = j;

						}

					}
					// System.out.println("i value is "+i);
					if (i != (search_params.size()) - 1) {
						condition = condition + " ) AND ";
					} else
						condition = condition + ")";

					// System.out.println("condition is "+condition);
				}

				// System.out.println("Condition "+condition);
				String RECON_CATEGORIZATION = "SELECT * FROM " + table_Name + " OS1 ";
				String DELETE_QUERY = "DELETE FROM " + table_Name + " OS1 ";

				if (!condition.equals("")) {
					RECON_CATEGORIZATION = RECON_CATEGORIZATION + " WHERE " + condition
							+ " AND TO_CHAR(CREATEDDATE ,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY')"
							+ " AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '" + stFile_date + "'";

					DELETE_QUERY = DELETE_QUERY + " WHERE " + condition
							+ " AND TO_CHAR(CREATEDDATE ,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY')"
							+ " AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '" + stFile_date + "'";

				} else {
					RECON_CATEGORIZATION = RECON_CATEGORIZATION
							+ " WHERE TO_CHAR(CREATEDDATE ,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY')"
							+ " AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '" + stFile_date + "'";
					DELETE_QUERY = DELETE_QUERY
							+ " WHERE TO_CHAR(CREATEDDATE ,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY')"
							+ " AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '" + stFile_date + "'";
				}

				// String CHECK_SETTLEMENT = "SELECT * FROM SETTLEMENT_"+a[2] +" OS2 ";
				// NO NEED OF KNOCKOFF CONDITION AS WE ARE DELETING THE RECORDS THE SAME TIME
				/*
				 * int reversal_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new
				 * Object[] { (file_id), a[0]},Integer.class); List<KnockOffBean>
				 * knockoff_Criteria = getJdbcTemplate().query(GET_KNOCKOFF_CRITERIA, new
				 * Object[] { reversal_id , file_id}, new KnockOffCriteriaMaster());
				 */
				// String knockoffCond = getKnockOffCondition(knockoff_Criteria);

				// System.out.println("check here"+knockoffCond);

				/*
				 * if(!knockoffCond.equals("")) { CHECK_SETTLEMENT = CHECK_SETTLEMENT +
				 * " WHERE "+knockoffCond+" AND trunc(OS2.CREATEDDATE) = trunc(SYSDATE)"; }
				 */

				// RECON_CATEGORIZATION = RECON_CATEGORIZATION + "("+ CHECK_SETTLEMENT +")";
				// DELETE_QUERY = DELETE_QUERY + "("+CHECK_SETTLEMENT + ")";

				System.out.println("recon categorization QUERY IS " + RECON_CATEGORIZATION);

				pstmt = conn.prepareStatement(RECON_CATEGORIZATION);
				ResultSet CIA_GL_RECORDS = pstmt.executeQuery();

				String INSERT_RECORDS = "INSERT INTO SETTLEMENT_" + a[2]
						+ " (CREATEDDATE, CREATEDBY,FILEDATE, REMARKS, " + stFile_headers + ")"
						+ " VALUES (SYSDATE, 'INT5779',TO_DATE('" + stFile_date + "','DD/MM/YYYY'),'ONUS-CIA-GL'";
				String[] tab_cols = stFile_headers.split(",");
				for (int count = 0; count < tab_cols.length; count++) {
					INSERT_RECORDS = INSERT_RECORDS + ",?";
				}

				INSERT_RECORDS = INSERT_RECORDS + ")";

				System.out.println("INSERT QUERY IS " + INSERT_RECORDS);

				insertBatch(INSERT_RECORDS, CIA_GL_RECORDS, tab_cols, conn);

				// DELETE THE RECORDS FROM ONUS TABLE AFTER INSERTION IN SETTLEMENT TABLE
				System.out.println(
						"DELETING RECON CATEGORIZED RECORDS------------------------------------------------------");
				System.out.println("DELETE QUERY IS " + DELETE_QUERY);
				getJdbcTemplate().execute(DELETE_QUERY);
				System.out.println(
						"COMPLETED DELETION--------------------------------------------------------------------");

				/*
				 * String DELETE_QUERY = "DELETE FROM "+table_Name +" WHERE ";
				 * 
				 * for(int count = 0 ; count <tab_cols.length ; count++) { if(count ==
				 * (tab_cols.length-1)) { DELETE_QUERY = DELETE_QUERY + "NVL("+tab_cols[count] +
				 * ",'!null!') = ? "; } else DELETE_QUERY = DELETE_QUERY +
				 * "NVL("+tab_cols[count] + ",'!null!') = ? AND "; }
				 * System.out.println("DELETE QUERY IS "+DELETE_QUERY);
				 * 
				 * PreparedStatement delete_statement =
				 * conn.prepareStatement(RECON_CATEGORIZATION); ResultSet delete_set =
				 * delete_statement.executeQuery();
				 * 
				 * deleteBatch(DELETE_QUERY, delete_set, tab_cols, conn);
				 */

			}

			System.out.println(
					"COMPLETED CIA GL--------------------------------------------------------------------------------------------------------------------------------------------------");
			// insert remaining records from recon table in ONLINE ONUS table
			/*
			 * String REMAINING_RECON_RECORDS = "SELECT * FROM "+table_Name
			 * +" OS1 WHERE NOT EXISTS " +
			 * " ( SELECT * FROM SETTLEMENT_"+a[3]+" OS2 WHERE ";
			 */

			/*
			 * int reversal_id = getJdbcTemplate().queryForObject(GET_REVERSAL_ID, new
			 * Object[] { (file_id), a[1]},Integer.class); List<KnockOffBean>
			 * knockoff_Criteria = getJdbcTemplate().query(GET_KNOCKOFF_CRITERIA, new
			 * Object[] { reversal_id , file_id}, new KnockOffCriteriaMaster());
			 * 
			 * String knockoffCond = getKnockOffCondition(knockoff_Criteria);
			 * 
			 * REMAINING_RECON_RECORDS = REMAINING_RECON_RECORDS + knockoffCond + ")";
			 */
			String REMAINING_RECON_RECORDS = "SELECT * FROM " + table_Name
					+ " WHERE TO_CHAR(CREATEDDATE,'DD/MM/YYYY') = TO_CHAR(SYSDATE,'DD/MM/YYYY')"
					+ " AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '" + stFile_date + "'";
			System.out.println("REMANING RECON RECORDS " + REMAINING_RECON_RECORDS);

			PreparedStatement remainingStatement = conn.prepareStatement(REMAINING_RECON_RECORDS);
			ResultSet remaining_set = remainingStatement.executeQuery();

			String INSERT_REMAINING_RECORDS = "INSERT INTO " + a[0] + "_" + a[2] + " (CREATEDDATE, CREATEDBY,FILEDATE,"
					+ stFile_headers + ") VALUES(SYSDATE, 'INT5779',TO_DATE('" + stFile_date + "','DD/MM/YYYY')";
			String[] tab_cols = stFile_headers.split(",");
			for (int count = 0; count < tab_cols.length; count++) {
				INSERT_REMAINING_RECORDS = INSERT_REMAINING_RECORDS + ",?";
			}
			INSERT_REMAINING_RECORDS = INSERT_REMAINING_RECORDS + ")";

			System.out.println("INSERT REMAINING RECORDS " + INSERT_REMAINING_RECORDS);

			insertBatch(INSERT_REMAINING_RECORDS, remaining_set, tab_cols, conn);

			System.out.println("COMPLETED INSERTING IN ONLINE ONUS TABLE!!!!!!");

			// TRUNCATE MANUAL ONUS TABLE
			String TRUNCATE_QUERY = "TRUNCATE TABLE " + table_Name;
			// getJdbcTemplate().execute(TRUNCATE_QUERY);

			System.out.println("TRUNCATED TABLE " + table_Name);

			// ------------------------------------------AS ON 22 JUNE 2017 NO NEED TO
			// UPDATE RAW TABLE-----------------------------------------
			// **********************************************************update raw
			// table***************************
			/*
			 * PreparedStatement psrecon = conn.prepareStatement(REMAINING_RECON_RECORDS);
			 * ResultSet rs = psrecon.executeQuery(); //get Raw table name String
			 * stRaw_Table = getJdbcTemplate().queryForObject(GET_TABLE_NAME , new
			 * Object[]{file_id}, String.class); String UPDATE_QUERY
			 * ="UPDATE "+stRaw_Table.toUpperCase()
			 * +" SET NEXT_TRAN_DATE = TO_CHAR(SYSDATE,'DD/MON/YYYY') WHERE ( ";
			 * //System.out.println("cols lenght is "+col_names.length);
			 * System.out.println("update columns are "+Update_cols); String[] col_names =
			 * Update_cols.split(","); for(int i=0; i<col_names.length ; i++) {
			 * if(i==(col_names.length-1)) UPDATE_QUERY = UPDATE_QUERY + "NVL("+col_names[i]
			 * + ",'!null!') = ?"; else UPDATE_QUERY = UPDATE_QUERY + "NVL("+ col_names[i] +
			 * ",'!null!') = ? AND "; }
			 * 
			 * UPDATE_QUERY = UPDATE_QUERY + ")";
			 * 
			 * System.out.println("UPDATE QUERY "+UPDATE_QUERY);
			 * System.out.println("start time FOR UPDATING RAW TABLE "+new
			 * java.sql.Timestamp(new java.util.Date().getTime()));
			 * deleteBatch(UPDATE_QUERY, rs, col_names,conn);//IT IS UPDATE QUERY FOR RAW
			 * TABLE //updateBatch(UPDATE_QUERY, rs, tab_cols);
			 * System.out.println("End time FOR UPDATING RAW TABLE "+new
			 * java.sql.Timestamp(new java.util.Date().getTime()));
			 */

		} catch (Exception e) {
			System.out.println("Exception is " + e);
		}
	}

	private static class ColumnsMapper implements RowMapper<String> {

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			String stColumns = rs.getString("COLUMN_NAME");

			return stColumns;

		}
	}

	private static class SettlementId implements RowMapper<SettlementBean> {

		@Override
		public SettlementBean mapRow(ResultSet rs, int rowNum) throws SQLException {

			SettlementBean settlementBean = new SettlementBean();
			settlementBean.setInSettlement_id(rs.getInt("ID"));
			return settlementBean;
		}
	}

	private static class SearchParameterMaster implements RowMapper<FilterationBean> {

		@Override
		public FilterationBean mapRow(ResultSet rs, int rowNum) throws SQLException {
			// System.out.println("row num is "+rowNum);
			// System.out.println("header is "+rs.getString("FILE_HEADER"));
			/*
			 * while(rs.next()) {
			 */
			// System.out.println("header is "+rs.getString("TABLE_HEADER"));
			FilterationBean filterBean = new FilterationBean();

			filterBean.setStSearch_header(rs.getString("TABLE_HEADER"));
			filterBean.setStSearch_pattern(rs.getString("PATTERN"));
			filterBean.setStSearch_padding(rs.getString("PADDING"));
			// filterBean.setStsearch_charpos(rs.getString("CHARPOSITION"));
			filterBean.setStsearch_Startcharpos(rs.getString("START_CHARPOS"));
			filterBean.setStsearch_Endcharpos(rs.getString("CHARSIZE"));
			filterBean.setStSearch_Condition(rs.getString("CONDITION"));
			filterBean.setStSearch_Datatype(rs.getString("DATATYPE"));

			// search_params.add(filterBean);
			// }
			return filterBean;

		}
	}

	@Override
	public int updateRecord(SettlementTypeBean settlementTypeBean) {
		try {
			int result = 0;

			String query = "update " + settlementTypeBean.getSetltbl() + " set remarks='"
					+ settlementTypeBean.getrEMARKS() + "' WHERE 1=1 AND remarks='ONUS-RECON'"
					+ "	AND to_date(filedate,'dd/mm/yy')= to_date('" + settlementTypeBean.getFileDate()
					+ "','dd/mm/yy') ";

			if (settlementTypeBean.getSetltbl().trim().equalsIgnoreCase("settlement_switch")) {

				query = query + " AND pan='" + settlementTypeBean.getPan() + "' " + "AND TERMID ='"
						+ settlementTypeBean.gettERMID() + "'" + " AND TRACE ='" + settlementTypeBean.gettRACE() + "'";

			} else if (settlementTypeBean.getSetltbl().trim().equalsIgnoreCase("settlement_cbs")) {

				query = query + " AND ACCOUNT_NUMBER='" + settlementTypeBean.getaCCOUNT_NUMBER() + "' "
						+ "AND CONTRA_ACCOUNT ='" + settlementTypeBean.getcONTRA_ACCOUNT() + "'" + " AND TRANDATE ='"
						+ settlementTypeBean.gettRANDATE() + "'" + " AND TRAN_PARTICULAR ='"
						+ settlementTypeBean.gettRAN_PARTICULAR() + "'";

			}

			System.out.println("query" + query);
			result = getJdbcTemplate().update(query);

			if (result > 0) {

				return 1;
			} else {

				return 0;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;

		}

	}

	/*
	 * @Override public void buildExcelDocument1(Map<String, Object> map, String
	 * stFilename,HttpServletRequest request,HttpServletResponse response) throws
	 * Exception {
	 * 
	 * XSSFWorkbook book = new XSSFWorkbook();
	 * 
	 * List<List<String>> generatettum_list = null; generatettum_list=
	 * (List<List<String>>) map.get("DATA");
	 * 
	 * List<String> File_Headers = generatettum_list.get(0);
	 * 
	 * generatettum_list.remove(0);
	 * 
	 * //get filename String filename = (String)map.get("filename");
	 * 
	 * List<String> Records = new ArrayList<>(); Date date = new Date();
	 * SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyhhmm");
	 * 
	 * String strDate = sdf.format(date);
	 * 
	 * System.out.println(strDate);
	 * response.setContentType("application/vnd.ms-excel");
	 * response.setHeader("Content-disposition",
	 * "attachment; filename="+filename+".xls");
	 * 
	 * XSSFSheet sheet = book.createSheet("Records"); //HSSFSheet sheet =
	 * workbook.createSheet("Records");
	 * 
	 * // CellStyle style =book.createCellStyle();
	 * 
	 * //CellStyle style = workbook.createCellStyle(); //CellStyle style =
	 * book.createCellStyle(); //Font font = workbook.createFont(); Font font =
	 * book.createFont(); font.setFontName("Arial");
	 * font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD); font.setColor()
	 * style.setFont(font);
	 * 
	 * //CellStyle numberStyle = workbook.createCellStyle(); ///CellStyle
	 * numberStyle = book.createCellStyle();
	 * numberStyle.setDataFormat(book.getCreationHelper().createDataFormat().
	 * getFormat("0.00"));
	 * 
	 * XSSFRow header = sheet.createRow(0); //HSSFRow header = sheet.createRow(0);
	 * 
	 * for(int i =0 ;i < File_Headers.size(); i++) {
	 * header.createCell(i).setCellValue(File_Headers.get(i));
	 * //header.getCell(i).setCellStyle(style); }
	 * 
	 * 
	 * int inRowCount = 1; int sheet_No = 2; for(int i = 0
	 * ;i<generatettum_list.size() ; i++) { if(inRowCount > 1000) { inRowCount = 1;
	 * sheet = workbook.createSheet("Records "+sheet_No);
	 * 
	 * HSSFRow header1 = sheet.createRow(0);
	 * 
	 * for(int k =0 ;k < File_Headers.size(); k++) {
	 * header1.createCell(k).setCellValue(File_Headers.get(k));
	 * header1.getCell(k).setCellStyle(style); } sheet_No++; } // HSSFRow header2 =
	 * sheet.createRow(inRowCount); XSSFRow header2 = sheet.createRow(inRowCount);
	 * Records = generatettum_list.get(i);
	 * 
	 * for(int j = 0 ;j<Records.size(); j++) {
	 * header2.createCell(j).setCellValue(Records.get(j)); }
	 * 
	 * inRowCount++;
	 * 
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

	// ADDED BY INT5779 AS ON 14TH MARCH 2018 FOR DOWNLOADING REPORTS
	/*
	 * @Override public Boolean checkfileprocessed(SettlementBean settlementbeanObj)
	 * { String GET_FILES = "";
	 * 
	 * if(settlementbeanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC") ||
	 * settlementbeanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) { GET_FILES
	 * = "SELECT FILEID FROM MAIN_FILESOURCE WHERE FILE_CATEGORY = '"
	 * +settlementbeanObj.getCategory()+"' AND (FILE_SUBCATEGORY = '"
	 * +settlementbeanObj.getStsubCategory()+"' " +
	 * " OR FILE_SUBCATEGORY = 'SURCHARGE')"; } else { GET_FILES =
	 * "SELECT FILEID FROM MAIN_FILESOURCE WHERE FILE_CATEGORY = '"
	 * +settlementbeanObj.getCategory()+"' AND FILE_SUBCATEGORY = '"
	 * +settlementbeanObj.getStsubCategory()+"'"; } List<String> fileids =
	 * getJdbcTemplate().query(GET_FILES,new Object[] {} , new RowMapper<String>() {
	 * 
	 * @Override public String mapRow(ResultSet rs,int row)throws SQLException {
	 * return rs.getString("FILEID"); } }); for(String fileid : fileids) { String
	 * CHECK_IT = "SELECT COUNT(*) FROM MAIN_FILE_UPLOAD_DTLS WHERE FILEID = '"
	 * +fileid+"' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+settlementbeanObj.
	 * getDatepicker()+"'"+
	 * "AND FILTER_FLAG = (SELECT FILTERATION FROM MAIN_FILESOURCE WHERE FILEID = '"
	 * +fileid+"') AND KNOCKOFF_FLAG = (SELECT KNOCKOFF FROM MAIN_FILESOURCE WHERE FILEID = '"
	 * +fileid+"')"+ "AND COMAPRE_FLAG = 'Y'"; try { if((!fileid.equals("54")) &&
	 * (!fileid.equals("55")) && (!fileid.equals("41"))){
	 * System.out.println(CHECK_IT); int count =
	 * getJdbcTemplate().queryForObject(CHECK_IT, new Object[]{},Integer.class);
	 * if(count == 0) { return false; } } } catch(Exception e) { return false; } }
	 * return true; }
	 */

	// changes made by int5688 2 May 2018
	@Override
	public Boolean checkfileprocessed(SettlementBean settlementbeanObj) {
		String GET_FILES = "";

		if (settlementbeanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")
				|| settlementbeanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
			GET_FILES = "select fileid from main_filesource where file_category = '" + settlementbeanObj.getCategory()
					+ "' and (file_subcategory = '" + settlementbeanObj.getStsubCategory() + "' "
					+ " or file_subcategory = 'surcharge')";
		} else {
			GET_FILES = "select fileid from main_filesource where file_category = '" + settlementbeanObj.getCategory()
					+ "' and file_subcategory = '" + settlementbeanObj.getStsubCategory() + "'";
		}
		List<String> fileids = getJdbcTemplate().query(GET_FILES, new Object[] {}, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int row) throws SQLException {
				return rs.getString("FILEID");
			}
		});
		for (String fileid : fileids) {
			/*
			 * String CHECK_IT =
			 * "SELECT COUNT(*) FROM MAIN_FILE_UPLOAD_DTLS WHERE FILEID = '"
			 * +fileid+"' AND TO_CHAR(FILEDATE,'DD/MM/YYYY') = '"+settlementbeanObj.
			 * getDatepicker()+"'"+
			 * "AND FILTER_FLAG = (SELECT FILTERATION FROM MAIN_FILESOURCE WHERE FILEID = '"
			 * +fileid+"') AND KNOCKOFF_FLAG = (SELECT KNOCKOFF FROM MAIN_FILESOURCE WHERE FILEID = '"
			 * +fileid+"')"+ "AND COMAPRE_FLAG = 'Y'";
			 */

			String CHECK_IT = "select count(1) from main_file_upload_dtls where fileid = '" + fileid
					+ "' and filedate = to_date('" + settlementbeanObj.getDatepicker() + "','dd/mm/yyyy') "
					+ "and filter_flag = (select filteration from main_filesource where fileid = '" + fileid
					+ "') and knockoff_flag = (select knockoff from main_filesource where fileid = '" + fileid + "')"
					+ "AND comapre_flag = 'Y'";
			try {
				if ((!fileid.equals("54")) && (!fileid.equals("55"))
						&& (!fileid.equals("41") && (!fileid.equals("53") && (!fileid.equals("43"))))) {
					System.out.println(CHECK_IT);
					int count = getJdbcTemplate().queryForObject(CHECK_IT, new Object[] {}, Integer.class);
					if (count == 0) {
						return false;
					}
				}
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void generate_Reports(SettlementBean settlementBeanObj) throws Exception {

		Connection con;
		Statement st;
		ResultSet rset = null;
		List<String> files = new ArrayList<>();
		// Row header = sheet.createRow(0);
		List<List<String>> Records = new ArrayList<>();
		// ArrayList<Excel_Bean> arr=new ArrayList<Excel_Bean>();
		OracleConn conn;
		String stTableName = "";
		try {

			FileOutputStream fos = null;
			ZipOutputStream zipOut = null;
			FileInputStream fis = null;

			con = getConnection();
			// 1. GET FILES FROM main recon sequence table
			String GET_FILES = "select filename from main_filesource where file_category = '"
					+ settlementBeanObj.getCategory() + "' and file_subcategory = '"
					+ settlementBeanObj.getStsubCategory() + "'";
			List<String> Filenames = getJdbcTemplate().query(GET_FILES, new Object[] {}, new RowMapper<String>() {
				@Override
				public String mapRow(ResultSet rs, int row) throws SQLException {
					return rs.getString("FILENAME");
				}
			});

			// CREATE FOLDERS
			checkNcreateFolder(settlementBeanObj, Filenames);
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

			java.util.Date date = sdf.parse(settlementBeanObj.getDatepicker());

			sdf = new SimpleDateFormat("dd-MM-yyyy");

			String stnewDate = sdf.format(date);

			if (settlementBeanObj.getCategory().equals("POS")) {
				Filenames.add("C");
				Filenames.add("E");
			}

			for (String filename : Filenames) {
				String GET_REMARKS = "";

				// 2. GET HEADERS FROM SETTLEMENT TABLE AND ADD IT IN EXCEL SHEET

				// List<String> Column_list = new ArrayList<>();//getColumnList(stTableName);
				// String stTableName =
				// "SETTLEMENT_"+settlementBeanObj.getCategory()+"_"+settlementBeanObj.getStsubCategory().substring(0,3)+"_"+filename;
				if (!settlementBeanObj.getStsubCategory().equalsIgnoreCase("-")) {
					stTableName = "SETTLEMENT_" + settlementBeanObj.getCategory() + "_"
							+ settlementBeanObj.getStsubCategory().substring(0, 3) + "_" + filename;
				} else if (settlementBeanObj.getCategory().equals("FISDOM")) {
					stTableName = "SETTLEMENT_" + settlementBeanObj.getCategory() + "_"
							+ filename.replaceAll("FISDOM_", "");
				} else {
					stTableName = "SETTLEMENT_" + settlementBeanObj.getCategory() + "_" + filename;
				}

				List<String> Column_list = getColumnList(stTableName);

				if (settlementBeanObj.getCategory().equalsIgnoreCase("FISDOM")) {
					Column_list = getFisdomColumnList(stTableName);
				}

				GET_REMARKS = "select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
						+ " from " + stTableName + "  where  FILEDATE = '" + settlementBeanObj.getDatepicker() + "' ";

				// }

				logger.info("GET_REMARKS" + GET_REMARKS);

				List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS, new Object[] {}, new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet rs, int rowcount) throws SQLException {
						return rs.getString("DCRS_REMARKS");
					}

				});

				for (String dcrs_remarks : stRemarks) {
					/* SessionModel.req.getSession(); */

					int colcount = 0;
					String headercolumns = "";
					for (String headercols : Column_list) {
						if (colcount == 0) {

							headercolumns = headercols;
						} else {

							headercolumns = headercolumns + "|" + headercols;
						}
						colcount++;
					}

					// String stfile_name = filename+"-"+dcrs_remarks+".dat";

					String stfile_name = filename.replace("FISDOM_", "") + "-" + dcrs_remarks + ".dat";
					if (settlementBeanObj.getCategory().equalsIgnoreCase("FISDOM")) {
						stfile_name = filename.replace("FISDOM_", "") + "-" + dcrs_remarks + ".csv";
					}

					String GET_DATA = "";

					if (dcrs_remarks.contains("UNRECON") || dcrs_remarks.contains("UNMATCHED")) // &&
																								// !settlementBeanObj.getCategory().equals("NFS")
																								// &&
																								// !settlementBeanObj.getCategory().equalsIgnoreCase("CASHNET")
																								// )
					{
						GET_DATA = "SELECT * FROM  " + stTableName + " WHERE DCRS_REMARKS LIKE '%" + dcrs_remarks
								+ "%' AND FILEDATE = '" + settlementBeanObj.getDatepicker() + "'";
					} else {

						GET_DATA = "SELECT * FROM  " + stTableName + " WHERE trim(DCRS_REMARKS)='" + dcrs_remarks
								+ "' AND FILEDATE = '" + settlementBeanObj.getDatepicker() + "'";
					}

					if (settlementBeanObj.getCategory().equals("NFS")
							&& dcrs_remarks.equalsIgnoreCase("MATCHED_UNSUCCESSFUL")) {
						// changes done by sushant on 20apr2019
						/*
						 * GET_DATA = "SELECT * FROM "+stTableName
						 * +" WHERE trim(DCRS_REMARKS) like '"+dcrs_remarks.trim()
						 * +"%' AND TO_DATE(FILEDATE,'DD/MM/YY') = TO_DATE('"+settlementBeanObj.
						 * getDatepicker()+"','DD/MM/YY')";
						 */

						GET_DATA = "SELECT * FROM " + stTableName + " WHERE trim(DCRS_REMARKS) like '"
								+ dcrs_remarks.trim() + "%' AND FILEDATE = '" + settlementBeanObj.getDatepicker() + "'";

					} else if ((settlementBeanObj.getCategory().equals("CASHNET")
							|| settlementBeanObj.getCategory().equals("NFS")) && dcrs_remarks.contains("UNRECON")) {

						// changes done by sushant on 20apr2019
						/*
						 * GET_DATA = "SELECT * FROM "+stTableName
						 * +" WHERE trim(DCRS_REMARKS) like '"+dcrs_remarks.trim()
						 * +"%' AND TO_DATE(FILEDATE,'DD/MM/rrrr') = TO_DATE('"+settlementBeanObj.
						 * getDatepicker()+"','DD/MM/rrrr')";
						 */

						GET_DATA = "SELECT * FROM " + stTableName + " WHERE trim(DCRS_REMARKS) like '"
								+ dcrs_remarks.trim() + "%' AND FILEDATE = '" + settlementBeanObj.getDatepicker() + "'";

					}

					logger.info(GET_DATA);
					PreparedStatement ps = con.prepareStatement(GET_DATA);
					rset = ps.executeQuery();
					File myFile = new File(settlementBeanObj.getStPath() + File.separator + stfile_name);
					logger.info(settlementBeanObj.getStPath() + File.separator + stfile_name);
					myFile.createNewFile();
					String col = null;

					try {

						BufferedWriter out = new BufferedWriter(
								new FileWriter(settlementBeanObj.getStPath() + "//" + stfile_name, true));

						out.write(headercolumns);

						while (rset.next()) {
							String tabledata = "";
							for (int i = 0; i < Column_list.size(); i++) {
								if (i == 0) {

									if (rset.getString(Column_list.get(i)) == null) {
										tabledata = rset.getString(Column_list.get(i));
									} else {
										tabledata = rset.getString(Column_list.get(i));
									}
								} else {
									// System.out.println("check here "+Column_list.get(i));
									col = Column_list.get(i);
									if (rset.getString(Column_list.get(i)) == null) {

										tabledata = tabledata + "|" + rset.getString(Column_list.get(i));

									} else {

										if (Column_list.get(i).equals("COMPLTD_AMNT_TRAN")
												|| Column_list.get(i).equals("COMPLTD_AMT_SETTMNT")
												|| Column_list.get(i).equals("REQ_AMNT_TRAN")) {
											System.out.println("Output-->>" + rset.getString(Column_list.get(i)));
											String val = rset.getString(Column_list.get(i));
											double cmpl_amount = Double.parseDouble(val) / 100;
											tabledata = tabledata + "|" + cmpl_amount;
										} else {
											/*
											 * if(Column_list.get(i).equalsIgnoreCase("PAN")||Column_list.get(i).
											 * equalsIgnoreCase("REMARKS")||
											 * Column_list.get(i).equalsIgnoreCase("PRIMARY_ACCOUNT_NUMBER") ||
											 * Column_list.get(i).equalsIgnoreCase("PAN_NUMBER") ||
											 * Column_list.get(i).equalsIgnoreCase("CARD_NUMBER") ) {
											 * 
											 * String RAW_CARD = rset.getString(Column_list.get(i)); String Mask_Card
											 * =RAW_CARD; if(!RAW_CARD.contains("/")) { Mask_Card =
											 * RAW_CARD.substring(0, 5)+"******"+
											 * RAW_CARD.substring(RAW_CARD.length()-4); } tabledata
											 * =tabledata+"|"+Mask_Card;
											 * 
											 * 
											 * 
											 * }else {
											 */

											tabledata = tabledata + "|" + rset.getString(Column_list.get(i));
											/* } */

										}
									}

								}
							}
							out.write("\n");
							out.write(tabledata);

						}

						out.flush();
						out.close();

						files.add(settlementBeanObj.getStPath() + File.separator + stfile_name);

					} catch (Exception e) {
						System.out.println("col issue at " + col);
						System.out.println("eXCEPTION IS IN fileoutputstream " + e);
					}

				}

			}
			// ADDED BY INT8624 FOR PROVIDING EXTRA REPORT IN MASTERCARD
			// fos = new FileOutputStream(settlementBeanObj.getStPath()+"\\REPORTS.zip");
			// CHANGES MADE BY INT8624 ON 07 NOV
			// fos = new FileOutputStream("D:\\Reports\\"+settlementBeanObj.getCategory()+
			// "\\REPORTS.zip");
			/* GENERATE SUMMARY REPORT */

			try {
				// BufferedWriter out = new BufferedWriter(new
				// FileWriter(settlementBeanObj.getStPath()+"//"+"SUMMARY_REPORT.txt", true));

			} catch (Exception e) {

			}

			fos = new FileOutputStream(settlementBeanObj.getStPath() + File.separator + "REPORTS.zip");
			zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			try {
				for (String filespath : files) {
					File input = new File(filespath);
					fis = new FileInputStream(input);
					ZipEntry ze = new ZipEntry(input.getName());
					// System.out.println("Zipping the file: "+input.getName());
					zipOut.putNextEntry(ze);
					byte[] tmp = new byte[4 * 1024];
					int size = 0;
					while ((size = fis.read(tmp)) != -1) {
						zipOut.write(tmp, 0, size);
					}
					zipOut.flush();
					fis.close();
				}
				zipOut.close();
				// System.out.println("Done... Zipped the files...");
			} catch (Exception fe) {
				System.out.println("Exception in zipping is " + fe);
			}

			// zos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (rset != null) {
					rset = null;
				}
			} catch (Exception e) {

			}
		}

	}

	public boolean checkNcreateFolder(SettlementBean settlementBeanObj, List<String> stFileNames) {
		try {
			File directory = new File(settlementBeanObj.getStPath());

			if (!directory.exists()) {
				directory.mkdirs();
			}
			/*
			 * Date date = new Date(settlementBeanObj.getDatepicker()); SimpleDateFormat sdf
			 * = new SimpleDateFormat("dd-MM-yy"); String formatteddate = sdf.format(date);
			 * System.out.println("formatteddate is "+formatteddate);
			 */

			// SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM-yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");

			java.util.Date date = sdf.parse(settlementBeanObj.getDatepicker());

			sdf = new SimpleDateFormat("dd-mm-yyyy");

			String stnewDate = sdf.format(date);

			// String stnewPath = settlementBeanObj.getStPath() + File.separator +
			// stnewDate;

			// settlementBeanObj.setStPath(stnewPath);

			directory = new File(settlementBeanObj.getStPath());
			String stPath = settlementBeanObj.getStPath();
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// CHANGES DONE BY INT8624 FOR DUPLICATE RECORDS ISSUE
			logger.info(directory.listFiles() + " size is " + directory.listFiles().length);

			/*
			 * stnewPath = settlementBeanObj.getStPath() + File.separator + stnewDate +
			 * File.separator + settlementBeanObj.getStMergerCategory();
			 */

			/*
			 * if (directory.listFiles() != null && directory.listFiles().length > 0) {
			 * stnewPath = stnewPath + "_" + (directory.listFiles().length + 1);
			 * logger.info("stnewPath is " + stnewPath); } else { stnewPath = stnewPath +
			 * "_" + directory.listFiles().length; logger.info("stnewPath is " + stnewPath);
			 * 
			 * }
			 */

			settlementBeanObj.setStPath(stPath);

			directory = new File(stPath);
			if (!directory.exists()) {
				directory.mkdirs();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public void DeleteFiles(String stpath) {
		try {
			logger.info("Inside delete foleder path is " + stpath);
			File directory = new File(stpath);

			if (directory.exists()) {
				FileUtils.forceDelete(directory);
				logger.debug(directory.exists());
			}

		} catch (Exception e) {
			System.out.println("Exception e" + e);

		}
	}

	@Override
	public void generate_ipm(String outputString2) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean generateCTF(SettlementBean settlementBean, List<String> files) throws IOException {
		// TODO Auto-generated method stub
		String path = settlementBean.getStPath() + File.separator + "Bankaway.ctf";
		String path2 = settlementBean.getStPath() + File.separator;
//	String path = "D:/Bankaway.ctf";
		BufferedWriter output = null;
		OracleConn conn;
		int row_lenth_1 = 168;
		int row_lenth_2 = 45;
		int row_lenth_3 = 21;
		int count_1 = 0;
		int count_2 = 0;
		int count_3 = 0;
		String julian_date = null;
		int result_count = 0;
		String result_count_val = null;
		String sun_val = null;
		int main_count = 0;
		ArrayList<String> arr_test = new ArrayList<String>();
		ArrayList<String> arr_test2 = new ArrayList<String>();
		ArrayList<SettlementTypeBean> arr = new ArrayList<SettlementTypeBean>();
		/*
		 * Scanner scan=new Scanner(System.in);
		 * System.out.println("Enter Date in mm/dd/yyyy format :: "); String
		 * dt_va=scan.next(); System.out.println("Date :: "+dt_va);
		 */
		String julian_date1 = null;
		try {
			conn = new OracleConn();
			Connection con = conn.getconn();

			File file = new File(path);
			output = new BufferedWriter(new FileWriter(file));

			String get_bankrepo = "select * from SETTLEMENT_CARDTOCARD_BANKREPO where DCRS_REMARKS='CARDTOCARD_BANKWAY_MATCED' and TO_CHAR (filedate, 'dd/MM/yyyy') = "
					+ " TO_CHAR (TO_DATE ('" + settlementBean.getDatepicker() + "', 'dd/MM/YYYY'), 'dd/MM/YYYY')";

			PreparedStatement st = con.prepareStatement(get_bankrepo);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {

				SettlementTypeBean sb = new SettlementTypeBean();
				String visa_card_no = rs.getString("VISA_CARD_NO");
				String mobile_no = rs.getString("MOBILE_NO");
				String amount1 = rs.getString("AMOUNT");
				String amount = amount1.replaceAll("[,.]", "");
				String sol_id = rs.getString("SOL_ID");
				String debit_acc = rs.getString("DEBIT_ACC");
				String acc_name = rs.getString("ACC_NAME");
				String payment_id = rs.getString("PAYMENT_ID");
				String channel = rs.getString("CHANNEL");
				String date_time = rs.getString("DATE_TIME");
				String file_date = rs.getString("FILEDATE");

				long random_number = Utility.generateRandom();
				DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
				Date dateinput = inputFormat.parse(rs.getString("FILEDATE"));
				SimpleDateFormat sdfoffsite1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
				DateFormat outputFormat_new = new SimpleDateFormat("ddMMyyyy");
				String outputString_new = outputFormat_new.format(dateinput);
				java.util.Date datecisb;

				datecisb = sdfoffsite1.parse(file_date);

				sdfoffsite1 = new SimpleDateFormat("MM/dd/yyyy");

				System.out.println(sdfoffsite1.format(datecisb));

				String dataenew = sdfoffsite1.format(datecisb);

				int julian_dt = Utility.convertToJulian(outputString_new);
				int julian_dt1 = Utility.convertToJulian2(outputString_new);
				julian_date = String.valueOf(julian_dt);

				if (String.valueOf(julian_date).length() <= 1) {
					julian_date = "000" + julian_date + "";
				}
				if (String.valueOf(julian_date).length() == 2) {
					julian_date = "00" + julian_date;
				}
				if (String.valueOf(julian_date).length() == 3) {
					julian_date = "0" + julian_date;
				}

				julian_date1 = String.valueOf(julian_dt1);
				String split_dt[] = dataenew.split("/");
				System.out.println(split_dt[0] + split_dt[1]);
				String amount_val = Utility.appnd_zeros(amount);
				System.out.println("12 digit Amount" + amount_val);
				String get_acc_name = Utility.appnd_space(acc_name);

				arr_test.add("06");// 1-2
				arr_test.add("20");// 3-4
				arr_test.add(visa_card_no);// 5-20
				arr_test.add("000");// 21-23
				arr_test.add("   ");// 24-26
				// arr_test.add("7421426" + julian_date + random_number);// 27-49
				arr_test.add(Utility.get_mod("7421426" + julian_date + random_number));
				arr_test.add("00000000");// 50-57
				arr_test.add(split_dt[0] + split_dt[1]);// 58-61
				arr_test.add("000000000000");// 62-73
				arr_test.add("   ");// 74-76
				arr_test.add(amount_val);// 77-88
				arr_test.add("356");// 89-91
				arr_test.add(get_acc_name);// 92-116
				/* arr_test.add("IDBI VISA CC PAYMENT "); */// 92-116 // MODIFIED BY INT 8624 FOR CTF FILE CHANGES AS
															// PER VISA COMPLIANCE
				arr_test.add("VISAMONEYTXFR");// 117-129
				arr_test.add("IN ");// 130-132
				// arr_test.add("6051");// 133-136 // MODIFIED BY INT 8624 FOR CTF FILE CHANGES
				// AS PER VISA COMPLIANCE
				arr_test.add("6012");// 133-136
				arr_test.add("00000");// 137-141
				arr_test.add("   ");// 142-144
				arr_test.add(" ");// 145
				arr_test.add(" ");// 146
				arr_test.add("1");// 147
				arr_test.add("00");// 148-149
				arr_test.add("9");// 150
				arr_test.add(" ");// 151
				arr_test.add("      ");// 152-157
				arr_test.add(" ");// 158
				arr_test.add(" ");// 159
				arr_test.add(" ");// 160
				arr_test.add(" ");// 161
				arr_test.add("0");// 162-163
				arr_test.add(julian_date1);// 164-167
				arr_test.add("0");// 168

				// Newly added on 02/12/2018

				arr_test.add("06");// 1-2
				arr_test.add("21");// 3-4
				String sub_str = visa_card_no.substring(0, 6);
				String concat = sub_str + "4214260000";
				arr_test.add(concat);// 5-20
				arr_test.add("00");// 21-23
				arr_test.add(" ");// 23-24
				arr_test.add("PYMNT ID NO-");// 25-35
				arr_test.add(" ");// 36-37
				arr_test.add(payment_id);// 58-61
				arr_test.add("                                                         ");// 47-103
				arr_test.add("000000000000");// 104-115
				arr_test.add(" ");// 116-117
				arr_test.add("             ");// 117-129
				arr_test.add("0");// 130-131
				arr_test.add("                           ");// 131-157
				arr_test.add("000000000");// 158-166
				arr_test.add("  ");// 167-168
				arr_test.add("06");// 1-2
				arr_test.add("23");// 3-4
				arr_test.add("            ");
				arr_test.add("CRCP");

			}
			FileWriter writer = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(writer);
			// arr_test2=arr_test.iterator().next();
			String bind_rec = "";
			// arr_test.indexOf(i);
			String bind_2 = "";
			String bind_rec1 = "";

			String bind_3 = "";
			int counter_value = 0;
			for (String str : arr_test) {
				counter_value++;
				bw.append(str);
				// bind_rec=str;
				// System.out.println("Index value ::"+arr_test.indexOf(str));
				// bw.append("\n");

				count_1 = count_1 + 1;
				System.out.println("Count ::" + count_1);
				if (str.equals(julian_date1)) {
					bind_rec = "";
					bind_rec = bind_rec + str;
					// System.out.println("Bind 1 :: "+bind_rec);
				}
				if (!bind_rec.equals("")) {
					count_3 = count_3 + 1;
					if (count_3 == 2) {
						bind_2 = bind_rec + "0";
						// System.out.println("Bind 2 :: "+bind_2);
					}
				}
				if (str.equals("000000000")) {
					bind_rec1 = "";
					bind_rec1 = bind_rec1 + str;
					System.out.println("Bind 1 :: " + bind_rec1);
				}

				if (!bind_rec1.equals("")) {
					count_3 = count_3 + 1;
					if (count_3 == 2) {
						bind_3 = bind_rec1 + "  ";
						// System.out.println("Bind 2 :: "+bind_3);
					}
				}
				// bind_rec=bind_rec+str;
				if (bind_2.equals(julian_date1 + "0")) {
					bw.append("\n");
					main_count = main_count + 1;
					bind_2 = "";
					count_3 = 0;
					bind_rec = "";
					// bw.newLine();
					/*
					 * count_2=count_2+1; if(count_2==1) { bw.append("TEST"); }
					 */
//				/bw.newLine();
					count_1 = 0;
				} else if (bind_3.equals("000000000  ")) {
					bw.append("\n");
					main_count = main_count + 1;
					bind_3 = "";
					count_3 = 0;
					bind_rec1 = "";
					bind_2 = "";
					count_3 = 0;
					bind_rec = "";
					// bw.newLine();
					/*
					 * count_2=count_2+1; if(count_2==1) { bw.append("TEST"); }
					 */
//				/bw.newLine();
					count_1 = 0;
				} else if (str.equals("CRCP")) {
					System.out.println("Counter value-->>" + counter_value);
					System.out.println("Array value-->>" + arr_test.size());
					if (counter_value != arr_test.size()) {
						bw.append("\n");
					} else {
						// bw.append(String.format("%s%n", counter_value));
						System.out.println("Inside else");

					}
					main_count = main_count + 1;

					bind_3 = "";
					count_3 = 0;
					bind_rec1 = "";
					bind_2 = "";
					count_3 = 0;
					bind_rec = "";
					// bw.newLine();
					/*
					 * count_2=count_2+1; if(count_2==1) { bw.append("TEST"); }
					 */
//				/bw.newLine();
					count_1 = 0;
				}
			}
			/*
			 * main_count=main_count+1;
			 * 
			 * System.out.println("Main Count"+ main_count); String
			 * count_val=Integer.toString(main_count); String
			 * main_cnt=Utility.auto_append(count_val); int
			 * count_6=Integer.parseInt(main_cnt); int main_count2=count_6+1; String
			 * count_val2=Integer.toString(main_count2); String
			 * main_cnt2=Utility.auto_append(count_val2); int
			 * count_7=Integer.parseInt(main_cnt2);
			 */

			String get_rec_count = "select count(*) as count,sum(to_number(replace((replace(os1.amount,',')),'.'))) as total from SETTLEMENT_CARDTOCARD_BANKREPO os1";
			PreparedStatement pst = con.prepareStatement(get_rec_count);
			ResultSet rst = pst.executeQuery();
			while (rst.next()) {
				/*
				 * //result_count=rst.getInt("count"); System.out.println("Count"+result_count);
				 * String val=Integer.toString(result_count);
				 * result_count_val=Utility.auto_append(val);
				 */
				sun_val = rst.getString("total");
				System.out.println("Sum" + sun_val);

			}
			/*
			 * int get_row1=result_count+1; String val2=Integer.toString(get_row1); String
			 * row_count=Utility.auto_append3(val2); int
			 * get_count=Integer.parseInt(row_count); int get_row2=get_row1+1; String
			 * get_rw=Integer.toString(get_row2); String
			 * row_count2=Utility.auto_append3(get_rw); String
			 * get_total=Utility.auto_append2(sun_val);
			 */

			/*
			 * bw.append("9100426365"+julian_date1+"000000000000000"+result_count_val+
			 * "000000"+main_cnt+"000000"+"00000001"+row_count+"000000000000000000"+
			 * get_total+"000000000000000"+"00000000000000"+"0000000000000000"+"0000000");
			 * bw.append("\n");
			 * bw.append("9200426365"+julian_date1+"000000000000000"+result_count_val+
			 * "000000"+main_cnt2+"000000"+"00000000"+row_count2+"000000000000000000"+
			 * get_total+"000000000000000"+"00000000000000"+"0000000000000000"+"0000000");
			 */

			bw.close();
			writer.close();
			System.out.println("CTF file created");

			// Reading file and getting no. of files to be generated
			// String inputfile = "D:/ctf_test.txt"; // Source File Name.
			double nol = 990.0; // No. of lines to be split and saved in each
								// output file.
			File file1 = new File(path);
			Scanner scanner = new Scanner(file1);
			int count = 0;
			int count_lines = 0;
			while (scanner.hasNextLine()) {
				scanner.nextLine();
				/*
				 * if(count_lines==3) { count++; count_lines=0; continue; } count_lines++;
				 */
				count++;
			}
			System.out.println("Lines in the file: " + count); // Displays no.
																// of lines in
																// the input
																// file.

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			System.out.println("No. of files to be generated :" + nof); // Displays
																		// no.
																		// of
																		// files
																		// to be
																		// generated.

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			int new_count = 0;
			int total_sum = 0;
			int total_rows = 0;
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			for (int j = 1; j <= nof; j++) {
				boolean newLineForLastFile = false;
				total_rows = 0;
				total_sum = 0;
				new_count = 0;
				result_count = 0;
				FileWriter fstream1 = new FileWriter(path2 + j + ".ctf"); // Destination File Location
				BufferedWriter out = new BufferedWriter(fstream1);
				for (int i = 1; i <= nol; i++) {

					strLine = br.readLine();
					if (strLine != null) {
						total_rows++;
						out.write(strLine);
						if (i != nol) {
							newLineForLastFile = true;
							out.newLine();
							if (new_count == 3) {
								result_count++;
								new_count = 0;
							}
							try {
								new_count++;
								// result_count++;
								String get_amount = strLine.substring(77, 88);
								int get_tot = Integer.parseInt(get_amount);
								total_sum += get_tot;
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						}
						// added by int8624 for handling issue if count is 990 in all ctf files
						else {
							System.out.println("Inside else of i != nol");
							newLineForLastFile = false;
						}

					} else {
						result_count++;

						break;
					}

				}
				// if(j!=nof)//modified if condition by int8624 for handling issue if count is
				// 990 in all ctf files
				if (j != nof || !newLineForLastFile) {
					result_count++;
					out.newLine();
					System.out.println("Inside no of lines" + nof);
				}

				System.out.println("Total Sum::" + total_sum);
				total_rows = total_rows + 1;

				System.out.println("Main Count" + total_rows);
				String count_val = Integer.toString(total_rows);
				String main_cnt = Utility.auto_append(count_val);
				int count_6 = Integer.parseInt(main_cnt);
				int main_count2 = count_6 + 1;
				String count_val2 = Integer.toString(main_count2);
				String main_cnt2 = Utility.auto_append(count_val2);
				System.out.println("Total count ::" + result_count);
				String val = Integer.toString(result_count);
				result_count_val = Utility.auto_append(val);
				int get_row1 = result_count + 1;
				String val2 = Integer.toString(get_row1);
				String row_count = Utility.auto_append3(val2);
				int get_count = Integer.parseInt(row_count);
				int get_row2 = get_row1 + 1;
				String get_rw = Integer.toString(get_row2);
				String row_count2 = Utility.auto_append3(get_rw);
				String tot_val = Integer.toString(total_sum);
				String get_total = Utility.auto_append2(tot_val);
				// out.newLine();
				out.append("9100426365" + julian_date1 + "000000000000000" + result_count_val + "000000" + main_cnt
						+ "000000" + "00000001" + row_count + "000000000000000000" + get_total + "000000000000000"
						+ "00000000000000" + "0000000000000000" + "0000000");
				out.newLine();
				out.append("9200426365" + julian_date1 + "000000000000000" + result_count_val + "000000" + main_cnt2
						+ "000000" + "00000000" + row_count2 + "000000000000000000" + get_total + "000000000000000"
						+ "00000000000000" + "0000000000000000" + "0000000");
				out.close();
				files.add(path2 + j + ".ctf");
			}

			in.close();

		}

		/*
		 * PrintWriter pw = new PrintWriter(file); for(int
		 * index=0;index<arr.size();index++) {
		 * System.out.println("Output ::"+arr.get(index).getVisa_card_no() );
		 * pw.println(arr.get(index).getVisa_card_no().toString()); }
		 */

		/*
		 * String arr1=arr.toString(); for (int k = 0; k < arr.size(); k++){
		 * output.writ(arr.get(k)); }int count=1; for(String str: arr_test) {
		 * if(count==2) { output.write("//n"); count=1; } output.write(str);
		 * System.out.println("CTF file created"); count=count+1; } }
		 */

		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to CTF file created");
		}
		output.close();
		return true;

	}

	@Override
	public List<Mastercard_chargeback> getMastercardchargeback(String arnNo) throws Exception {
		logger.info("***** SourceDAoImpl.getSubcategories Start ****");
		List<Mastercard_chargeback> SubCategories = new ArrayList<Mastercard_chargeback>();
		try {
			String GET_SUBCATE = "select os1.AMOUNT_RECON,os1.AMOUNT,os1.CURRENCY_CODE_RECON,os1.CURRENCY_CODE_TRAN from mastercard_pos_rawdata os1 where "
					+ " os1.PROCESSING_CODE!=200000 and os1.AQUIERER_REF_NO='" + arnNo + "'";
			return getJdbcTemplate().query(GET_SUBCATE, new RowMapper<Mastercard_chargeback>() {
				public Mastercard_chargeback mapRow(ResultSet rs, int row) throws SQLException {
					Mastercard_chargeback e = new Mastercard_chargeback();
					e.setSettlement_amount(rs.getString("AMOUNT_RECON"));
					e.setSettlement_currency(rs.getString("CURRENCY_CODE_RECON"));
					e.setTxn_amount(rs.getString("AMOUNT"));
					e.setTxn_currency(rs.getString("CURRENCY_CODE_TRAN"));

					return e;

				}
			});

		} catch (Exception e) {
			demo.logSQLException(e, "SourceDAoImpl.getSubcategories");
			logger.error(" error in SourceDAoImpl.getSubcategories",
					new Exception("SourceDAoImpl.getSubcategories", e));
			return null;
		}

	}

	@Override
	public int Savechargeback(String microfilm, String ref_id, String settlement_amount, String settlement_currency,
			String txn_amount, String txn_currency, String reason, String documentation, String remarks)
			throws Exception {

		String query = "insert into MASTERCARD_CHARGE" + " values( '" + microfilm + "','" + ref_id + "','"
				+ settlement_amount + "','" + settlement_currency + "','" + txn_amount + "'" + ",'" + txn_currency
				+ "','" + reason + "','" + documentation + "','" + remarks + "')";
		return getJdbcTemplate().update(query);
	}

	@Override
	public List<List<Mastercard_chargeback>> GenerateReportChargebk() throws Exception {
		// String GET_DATA="";
		List<List<Mastercard_chargeback>> Data = new ArrayList<>();
		List<Mastercard_chargeback> TTUM_Data = new ArrayList<>();
		List<String> ExcelHeaders = new ArrayList<>();
		List<Mastercard_chargeback> Excel_header = new ArrayList<>();

		try {

			// generatettumBeanObj.setStExcelHeader(ExcelHeaders);

			// Excel_header.add(generatettumBeanObj);

			String GET_DATA = "select * from mastercard_charge";

			System.out.println(GET_DATA);
			Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(GET_DATA);
			ResultSet rset = pstmt.executeQuery();

			while (rset.next()) {

				Mastercard_chargeback generateBean = new Mastercard_chargeback();
				generateBean.setMicrofilm(rset.getString("MICROFILM"));
				generateBean.setRef_id(rset.getString("REF_ID"));
				generateBean.setSettlement_amount(rset.getString("SETTLEMENT_AMOUNT"));
				generateBean.setSettlement_currency(rset.getString("SETTLEMENT_CURRENCY"));
				generateBean.setTxn_amount(rset.getString("TXN_AMOUNT"));
				generateBean.setTxn_currency(rset.getString("TXN_CURRENCY"));
				generateBean.setReason(rset.getString("REASON"));
				generateBean.setDocumentation(rset.getString("DOCUMENTATION"));
				generateBean.setRemarks(rset.getString("REMARKS"));

				TTUM_Data.add(generateBean);
			}

			/*
			 * String query2="truncate table mastercard_cbs_res_ttum";
			 * getJdbcTemplate().execute(query2);
			 */

			Data.add(Excel_header);
			Data.add(TTUM_Data);

		} catch (Exception e) {
			System.out.println("EXCEPTION IN generateTTUMForAMEX " + e);
			logger.error(e.getMessage());
		}
		return Data;
	}

	@Override
	public String generateChargBk(String arn1, String reason, String arn_date) throws Exception {
		// TODO Auto-generated method stub
		String filename = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ChargeBackImpl chr = new ChargeBackImpl();
		String filepath = System.getProperty("catalina.home");
		StringBuilder sb = new StringBuilder();
		sb.append(
				" SELECT rw.pan, rw.processing_code, rw.amount_recon,   to_char(to_date(rw.DATE_VAL,'DDMMRRRR'),'MM/DD/RRRR') DATE_VAL  , rw.data_code, "
						+ " rw.card_seq_num, rw.funcation_code, rw.msg_res_code, rw.card_acc_code, "
						+ " rw.aquierer_ref_no, rw.fi_id_code, rw.approval_code, rw.service_code, rw.card_acc_id_code, "
						+ " rw.currency_code_tran, rw.currency_code_recon, rw.tran_dest_id_code, "
						+ " rw.tran_org_id_code, rw.card_iss_ref_data, rw.recv_inst_idcode, "
						+ " rw.terminal_type, rw.elec_com_indic, rw.processing_mode, "
						+ " rw.currency_exponent, rw.business_act, rw.settlement_ind, "
						+ " rw.card_accp_name_loc, ch.ref_id, ch.settlement_amount, "
						+ " ch.settlement_currency, ch.txn_amount, ch.txn_currency, ch.reason, "
						+ " ch.documentation, ch.remarks "
						+ " FROM mastercard_pos_rawdata rw JOIN mastercard_charge ch "
						+ " ON rw.aquierer_ref_no = ch.microfilm " + " WHERE ch.microfilm in ( ");

		try {

			File f = new File(filepath);

			if (!(f.isDirectory())) {

				if (f.mkdir()) {

					System.out.println("directory created");
				}

			}
			OracleConn conn = new OracleConn();
			con = conn.getconn();
			String[] arns = new String[] { arn1 };
			for (int arn = 0; arn < arns.length; arn++) {
				if ((arn + 1) == arns.length) {
					sb.append(" ? ");
				} else {
					sb.append(" ?, ");
				}
			}
			sb.append(" ) ");

			pstmt = con.prepareStatement(sb.toString());
			for (int arn = 0; arn < arns.length; arn++) {
				pstmt.setString((arn + 1), arns[arn]);
			}

			rs = pstmt.executeQuery();

			filename = chr.createFile(new MasterCardModel(reason, arn_date, "Filename", filepath), rs); // D:\\Master_Card\\
																										// ChargeBack

			System.out.println("filename:::::::::::::::::::::::::::::::::::::::::::::::::::::" + filename);

		} catch (Exception e) {

			e.printStackTrace();
			return "Failed";
		}
		return filename;
	}

	@Override
	public List<Gl_bean> getMastercardGet_glbalance(String filedate) throws Exception {
		logger.info("***** SourceDAoImpl.getSubcategories Start ****");
		List<Gl_bean> SubCategories = new ArrayList<Gl_bean>();
		String sql = null;
		String sql_query = null;
		String sql_query2 = null;

		String sql_autorev = null;
		String sql_query_autorev = null;
		String sql_query2_autorev = null;
		String sql_lpcase = null;
		String sql_query_lpcase = null;
		String sql_settle = null;
		String sql_query_settle = null;
		String sql_settlement = null;
		String sql_query_settlement = null;

		// a
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");

		Date date1 = sdf.parse(filedate);

		sdf = new SimpleDateFormat("yyMMdd");
		System.out.println(sdf.format(date1));
		String dt = sdf.format(date1);

		// Specifying date format that matches the given date

		Calendar c = Calendar.getInstance();
		try {
			// Setting the date to the given date
			c.setTime(sdf.parse(sdf.format(date1)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Number of Days to add
		c.add(Calendar.DAY_OF_MONTH, 1);
		// Date after adding the days to the given date
		String newDate = sdf.format(c.getTime());
		// Displaying the new Date after addition of Days
		System.out.println("Date after Addition: " + newDate);

		SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MMM/yyyy");

		Date date12 = sdf1.parse(filedate);

		System.out.println(sdf1.format(date12));
		// Specifying date format that matches the given date

		Calendar c1 = Calendar.getInstance();
		try {
			// Setting the date to the given date
			c1.setTime(sdf1.parse(sdf1.format(date12)));

			// Number of Days to add
			c1.add(Calendar.DAY_OF_MONTH, 1);
			// Date after adding the days to the given date
			String newDate12 = sdf1.format(c1.getTime());
			// Displaying the new Date after addition of Days
			System.out.println("Date after Addition: " + newDate12);

			sdf = new SimpleDateFormat("dd-MM-YYYY");
			System.out.println(sdf.format(date1));
			String dt1 = sdf.format(date1);

			String sql1 = "select sum(os1.AMOUNT_equiv) as settlement_amount from SETTLEMENT_RUPAY_switch_bk os1 where os1.FILEDATE=? and os1.DCRS_REMARKS='RUPAY_DOM-MATCHED-2'";
			String sql2 = "select sum(os1.AMOUNT_SETTLEMENT) from SETTLEMENT_RUPAY_RUPAY_bk os1 where (os1.DATE_SETTLEMENT=?) and os1.DCRS_REMARKS='RUPAY_DOM-UNRECON-2' "
					+ "and os1.FILEDATE=? and os1.TXNFUNCTION_CODE='200'";
			String sql3 = "select sum(os1.DIFF_AMOUNT) from SETTLEMENT_RUPAY_switch_bk os1 where os1.FILEDATE=? and "
					+ "os1.DCRS_REMARKS like '%RUPAY_SUR%'";
			String sql4 = "select sum(os1.AMOUNT_SETTLEMENT ) from SETTLEMENT_RUPAY_RUPAY_bk os1 where   os1.DCRS_REMARKS='RUPAY_DOM-UNRECON-2' and os1.FILEDATE=? "
					+ "and os1.TXNFUNCTION_CODE ='200' and os1.DATE_SETTLEMENT=?";
			String sql5 = "select sum(os1.AMOUNT_EQUIV) from SETTLEMENT_RUPAY_switch_bk os1 where os1.FILEDATE=? and os1.DCRS_REMARKS='RUPAY_DOM-UNRECON-2'";
			String sql6 = "select sum((replace(os1.AMOUNT,',','' ))) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=? and os1.DCRS_REMARKS like '%RUPAY_DOM-UNRECON-1%'  "
					+ "and os1.E='C' and  os1.PSTD_USER_ID='CDCIADM'";
			String sql7 = "select sum((replace(os1.AMOUNT,',','' ))) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=? and os1.DCRS_REMARKS like '%RUPAY_DOM-UNRECON-1%'  "
					+ "and os1.E='D' and  os1.PSTD_USER_ID='CDCIADM' AND OS1.VALUE_DATE=?";

			String settlenmnt_match = (String) getJdbcTemplate().queryForObject(sql1, new Object[] { filedate },
					String.class);
			String nxtdt = (String) getJdbcTemplate().queryForObject(sql2, new Object[] { newDate, filedate },
					String.class);
			String surcharge = (String) getJdbcTemplate().queryForObject(sql3, new Object[] { filedate }, String.class);
			String lp_case = (String) getJdbcTemplate().queryForObject(sql4, new Object[] { newDate12, dt },
					String.class);
			String switch_unrecon = (String) getJdbcTemplate().queryForObject(sql5, new Object[] { filedate },
					String.class);
			String cbs_unrecon = (String) getJdbcTemplate().queryForObject(sql6, new Object[] { filedate },
					String.class);
			String auto_rev = (String) getJdbcTemplate().queryForObject(sql7, new Object[] { newDate12, dt1 },
					String.class);

			// Surcharge
			String countval = "select count(*) from RUPAY_GL_CHARGEBK os1 where os1.SURHCRAGE_AMOUNT=? and os1.filedate=?";

			String status = "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=?  AND replace(os1.AMOUNT,',','' ) LIKE ? and os1.E=?";

			String surchargecount = (String) getJdbcTemplate().queryForObject(countval,
					new Object[] { surcharge, filedate }, String.class);

			String surchargecount1 = (String) getJdbcTemplate().queryForObject(status,
					new Object[] { filedate, "%" + surcharge + "%", "C" }, String.class);

			if (surchargecount.equalsIgnoreCase("0")) {
				sql = "INSERT into RUPAY_GL_CHARGEBK " + "(SURHCRAGE_AMOUNT, FILEDATE, STATUS) VALUES (?, ?, ?)";

				getJdbcTemplate().update(sql, new Object[] { surcharge, filedate, "N" });
			} else {
				sql = "update RUPAY_GL_CHARGEBK os1 set os1.SURHCRAGE_AMOUNT=?,os1.FILEDATE=?,os1.status=? where "
						+ " os1.SURHCRAGE_AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql, new Object[] { surcharge, filedate, "N", surcharge, filedate });
			}
			if (!(surchargecount1).equals("0")) {
				sql_query = "update RUPAY_GL_CHARGEBK os1 set os1.status=? where "
						+ " os1.SURHCRAGE_AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_query, new Object[] { "Y", surcharge, filedate });

			}
			// End surcharge

			// Auto rev

			//

			String countval1_autorev = "select count(*) from RUPAY_GL_autorev os1 where os1.AMOUNT=? and os1.filedate=?";
			String status_autorev = "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=?  AND replace(os1.AMOUNT,',','' ) LIKE ? and os1.E=?";

			String surchargecount_autorev = (String) getJdbcTemplate().queryForObject(countval1_autorev,
					new Object[] { auto_rev, filedate }, String.class);

			String surchargecount1_autorev = (String) getJdbcTemplate().queryForObject(status_autorev,
					new Object[] { filedate, "%" + auto_rev + "%", "C" }, String.class);

			if (surchargecount_autorev.equalsIgnoreCase("0")) {
				sql_autorev = "INSERT into RUPAY_GL_autorev " + "(AMOUNT, FILEDATE, STATUS) VALUES (?, ?, ?)";

				getJdbcTemplate().update(sql_autorev, new Object[] { auto_rev, filedate, "N" });
			} else {
				sql_autorev = "update RUPAY_GL_autorev os1 set os1.AMOUNT=?,os1.FILEDATE=?,os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_autorev, new Object[] { auto_rev, filedate, "N", auto_rev, filedate });
			}
			if (!(surchargecount1_autorev).equals("0")) {
				sql_query_autorev = "update RUPAY_GL_autorev os1 set os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_query_autorev, new Object[] { "Y", auto_rev, filedate });

			}

			// end

			// Lp cases

			String countval1_lpcases = "select count(*) from RUPAY_GL_LPCASES os1 where os1.AMOUNT=? and os1.filedate=?";
			String status_lpcase = "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=?  AND replace(os1.AMOUNT,',','' ) LIKE ? and os1.E=?";

			String surchargecount_lpcase = (String) getJdbcTemplate().queryForObject(countval1_lpcases,
					new Object[] { lp_case, filedate }, String.class);

			String surchargecount1_lpcase = (String) getJdbcTemplate().queryForObject(status_lpcase,
					new Object[] { filedate, "%" + lp_case + "%", "C" }, String.class);

			if (surchargecount_lpcase.equalsIgnoreCase("0")) {
				sql_lpcase = "INSERT into RUPAY_GL_LPCASES " + "(AMOUNT, FILEDATE, STATUS) VALUES (?, ?, ?)";

				getJdbcTemplate().update(sql_lpcase, new Object[] { lp_case, filedate, "N" });
			} else {
				sql_lpcase = "update RUPAY_GL_LPCASES os1 set os1.AMOUNT=?,os1.FILEDATE=?,os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_lpcase, new Object[] { lp_case, filedate, "N", lp_case, filedate });
			}
			if (!(surchargecount1_autorev).equals("0")) {
				sql_query_lpcase = "update RUPAY_GL_LPCASES os1 set os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_query_lpcase, new Object[] { "Y", lp_case, filedate });

			}

			// End

			// End

			// Settlement matched

			/*
			 * String countval1_settle =
			 * "select count(*) from rupay_gl_settlement os1 where os1.AMOUNT=? and os1.filedate=?"
			 * ; String status_settle =
			 * "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=?  AND replace(os1.AMOUNT,',','' ) LIKE ? and os1.E=?"
			 * ;
			 * 
			 * String surchargecount_settle = (String) getJdbcTemplate().queryForObject(
			 * countval1_settle, new Object[] { lp_case,filedate }, String.class);
			 * 
			 * String surchargecount1_settle = (String) getJdbcTemplate().queryForObject(
			 * status_settle, new Object[] {filedate, "'%"+lp_case+"%'","D" },
			 * String.class);
			 * 
			 * 
			 * 
			 * if (surchargecount_settle.equalsIgnoreCase("0")) { sql_settle =
			 * "INSERT into RUPAY_GL_LPCASES " +
			 * "(AMOUNT, FILEDATE, STATUS) VALUES (?, ?, ?)";
			 * 
			 * getJdbcTemplate().update(sql_settle, new Object[] { lp_case, filedate, "N"
			 * }); } else { sql_settle =
			 * "update RUPAY_GL_LPCASES os1 set os1.AMOUNT=?,os1.FILEDATE=?,os1.status=? where "
			 * + " os1.AMOUNT=? and os1.filedate=?";
			 * 
			 * getJdbcTemplate().update( sql_settle, new Object[] { lp_case, filedate, "N",
			 * lp_case, filedate }); } if(!(surchargecount1_settle).equals("0")) {
			 * sql_query_settle = "update RUPAY_GL_LPCASES os1 set os1.status=? where " +
			 * " os1.AMOUNT=? and os1.filedate=?";
			 * 
			 * getJdbcTemplate().update( sql_query_settle, new Object[] {"Y", lp_case,
			 * filedate });
			 * 
			 * }
			 */

			// end

			/*
			 * sql_query2= "select * from RUPAY_GL_CHARGEBK os1 where os1.STATUS='N'";
			 * List<String>data=getJdbcTemplate().queryForList(sql_query2,String.class);
			 * 
			 * 
			 * Rupay_sur_GlBean surgl = (Rupay_sur_GlBean)getJdbcTemplate().queryForObject(
			 * sql_query2, new Object[] {}, new
			 * BeanPropertyRowMapper(Rupay_sur_GlBean.class));
			 */

			Gl_bean e = new Gl_bean();
			e.setSettlement_matched(settlenmnt_match);
			e.setNxtdate(nxtdt);
			e.setSurcharge(surcharge);
			e.setLpcase(lp_case);
			e.setSwitch_unrecon(switch_unrecon);
			e.setCbs_unrecon(cbs_unrecon);
			SubCategories.add(e);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return SubCategories;
	}

	@Override
	public List<Rupay_sur_GlBean> getRupaysurchargelist(String filedate) throws Exception {
		String query = "select * from RUPAY_GL_CHARGEBK os1 where os1.STATUS='N'";
		System.out.println(query);

		List<Rupay_sur_GlBean> typeList = getJdbcTemplate().query(query,
				new BeanPropertyRowMapper(Rupay_sur_GlBean.class));

		return typeList;
	}

	@Override
	public List<Rupay_gl_autorev> getRupayAutorevlist(String filedate) throws Exception {
		String query = "select * from RUPAY_GL_autorev os1 where os1.STATUS='N'";
		System.out.println(query);

		List<Rupay_gl_autorev> typeList = getJdbcTemplate().query(query,
				new BeanPropertyRowMapper(Rupay_gl_autorev.class));

		return typeList;
	}

	@Override
	public List<Rupay_gl_Lpcases> getRupayLpcaselist(String filedate) throws Exception {
		String query = "select * from RUPAY_GL_LPCASES os1 where os1.STATUS='N'";
		System.out.println(query);

		List<Rupay_gl_Lpcases> typeList = getJdbcTemplate().query(query,
				new BeanPropertyRowMapper(Rupay_gl_Lpcases.class));

		return typeList;
	}

	@Override
	public int SaveGl(String closingbal, String settlementid, String diff, String cbs_unrecon, String switch_unrecon,
			String nobase2, String settlementmatch, String nxtdate, String surcharge1, String settlementTotal,
			String lpcase2, String surcharge, String surch_total, String autorev, String autorev_total, String lpcase,
			String lpcasetotal, String finaltotal, String dateval) throws Exception {

		String query = "insert into rupay_gl_report" + " values( '" + closingbal + "','" + settlementid + "','" + diff
				+ "','" + cbs_unrecon + "','" + switch_unrecon + "'" + ",'" + nobase2 + "','" + settlementmatch + "','"
				+ nxtdate + "','" + lpcase2 + "','" + surcharge1 + "','" + settlementTotal + "','" + surcharge + "','"
				+ surch_total + "','" + autorev + "'," + "'" + autorev_total + "','" + lpcase + "','" + lpcasetotal
				+ "','" + finaltotal + "','" + dateval + "')";
		return getJdbcTemplate().update(query);
	}

	@Override
	public List<List<Rupay_Gl_repo>> GenerateGL(String dateval) throws Exception {
		// String GET_DATA="";
		List<List<Rupay_Gl_repo>> Data = new ArrayList<>();
		List<Rupay_Gl_repo> TTUM_Data = new ArrayList<>();
		List<String> ExcelHeaders = new ArrayList<>();
		List<Rupay_Gl_repo> Excel_header = new ArrayList<>();

		try {

			// generatettumBeanObj.setStExcelHeader(ExcelHeaders);

			// Excel_header.add(generatettumBeanObj);
			// select * from switch_cbs_cia where
			// processing_date=to_date('01/20/2018','MM/dd/yyyy')
			String GET_DATA = null;

			GET_DATA = "select * from rupay_gl_report os1 where os1.FILEDATE='" + dateval + "'";

			System.out.println(GET_DATA);
			Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(GET_DATA);
			ResultSet rset = pstmt.executeQuery();

			while (rset.next()) {

				Rupay_Gl_repo generateBean = new Rupay_Gl_repo();
				generateBean.setClosingbal(rset.getString("CLOSINGBAL"));
				generateBean.setSettlementid(rset.getString("SETTLEMENTID"));
				generateBean.setDiff(rset.getString("DIFF"));
				generateBean.setCbs_unrecon(rset.getString("CBS_UNRECON"));
				generateBean.setSwitch_unrecon(rset.getString("SWITCH_UNRECON"));
				generateBean.setNobase2(rset.getString("NOBASE2"));
				generateBean.setSettlementmatch(rset.getString("SETTLEMENTMATCH"));
				generateBean.setNxtdate(rset.getString("NXTDATE"));
				generateBean.setSurcharge1(rset.getString("SURCHARGE1"));
				generateBean.setLpcase2(rset.getString("LPCASE2"));
				generateBean.setSettlementtotal(rset.getString("SETTLEMENTTOTAL"));
				generateBean.setSurcharge(rset.getString("SURCHARGE"));
				generateBean.setSurch_total(rset.getString("SURCH_TOTAL"));
				generateBean.setAutorev(rset.getString("AUTOREV"));
				generateBean.setAutorev_total(rset.getString("AUTOREV_TOTAL"));
				generateBean.setLpcase(rset.getString("LPCASE"));
				generateBean.setLpcasetotal(rset.getString("LPCASETOTAL"));
				generateBean.setFinaltotal(rset.getString("FINALTOTAL"));

				TTUM_Data.add(generateBean);
			}
			Data.add(Excel_header);
			Data.add(TTUM_Data);

		} catch (Exception e) {
			System.out.println("EXCEPTION IN generateTTUMForAMEX " + e);
			logger.error(e.getMessage());
		}
		return Data;
	}

	@Override
	public String getSettlemntAmount(String settlementDate, String settlementAmount) throws Exception {
		// TODO Auto-generated method stub
		// Settlement start
		// List<Gl_bean> SubCategories = new ArrayList<Gl_bean>();
		String sql_settlement = null;
		String sql_query_settlement = null;
		String amount_val = null;

		try {

			String status_settlement = "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE=?  AND replace(os1.AMOUNT,',','' ) LIKE ? and os1.E=?";
			int surchargecount1_settlement = getJdbcTemplate().queryForObject(status_settlement,
					new Object[] { settlementDate, "%" + settlementAmount + "%", "D" }, Integer.class);

			String countval1_settlement = "select count(*) from RUPAY_GL_SETTLEMENT os1 where os1.AMOUNT=? and os1.filedate=?";
			String surchargecount_settlement = (String) getJdbcTemplate().queryForObject(countval1_settlement,
					new Object[] { settlementAmount, settlementDate }, String.class);

			String query = "select count(*) from SETTLEMENT_RUPAY_CBS_bk os1 where os1.FILEDATE='" + settlementDate
					+ "'  AND replace(os1.AMOUNT,',','' ) LIKE '%" + settlementAmount + "%' and os1.E='D'";

			if (surchargecount_settlement.equalsIgnoreCase("0")) {
				sql_settlement = "INSERT into RUPAY_GL_SETTLEMENT " + "(AMOUNT, FILEDATE, STATUS) VALUES (?, ?, ?)";

				getJdbcTemplate().update(sql_settlement, new Object[] { settlementAmount, settlementDate, "N" });
			} else {
				sql_settlement = "update RUPAY_GL_SETTLEMENT os1 set os1.AMOUNT=?,os1.FILEDATE=?,os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_settlement,
						new Object[] { settlementAmount, settlementDate, "N", amount_val, settlementDate });
			}
			if ((surchargecount1_settlement) != 0) {
				sql_query_settlement = "update RUPAY_GL_SETTLEMENT os1 set os1.status=? where "
						+ " os1.AMOUNT=? and os1.filedate=?";

				getJdbcTemplate().update(sql_query_settlement, new Object[] { "Y", settlementAmount, settlementDate });

				return "Data already exist";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		}
		return "Not exist";

	}

//ADDED BY INT8624 FOR FISDOM
	public ArrayList<String> getFisdomColumnList(String tableName) {

		String query = "SELECT column_name FROM   all_tab_cols WHERE  table_name = '" + tableName.toUpperCase()
				+ "' and owner = 'debitcard_recon' and column_name not like '%$%' "
				+ " and column_name not like '%$%' AND COLUMN_NAME NOT IN ('DCRS_REMARKS','CREATEDDATE','CREATEDBY','FILEDATE')";
		System.out.println(query);

		ArrayList<String> typeList = (ArrayList<String>) getJdbcTemplate().query(query, new RowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});

		System.out.println(typeList);
		return typeList;

	}

	/*
	 * public void generate_Dhana_Reports(SettlementBean beanObj) throws Exception {
	 * int sr_no = 1, index = 1; String stFileName = ""; FileOutputStream fos =
	 * null; ZipOutputStream zipOut = null; FileInputStream fis = null; int colcount
	 * = 0; try { //create folder first checkNcreateFolder(beanObj, new
	 * ArrayList<String>());
	 * 
	 * List<String> tables = new ArrayList<String>(); List<String> files = new
	 * ArrayList<>();
	 * tables.add("SETTLEMENT_"+beanObj.getCategory()+"_"+beanObj.getStsubCategory()
	 * .substring(0, 3)+"_CBS");
	 * tables.add("SETTLEMENT_"+beanObj.getCategory()+"_"+beanObj.getStsubCategory()
	 * .substring(0, 3)+"_"+beanObj.getCategory());
	 * 
	 * for(String tableName : tables) { sr_no = 1; List<String> columns = new
	 * ArrayList<String>(); columns = getColumnList(tableName); String column = "";
	 * colcount = 0; String headercolumns=""; for(String headercols : columns) {
	 * if(colcount== 0) {
	 * 
	 * headercolumns=headercols; }else {
	 * 
	 * headercolumns= headercolumns+"|"+headercols; } colcount++; }
	 * 
	 * for(String col : columns) { if(sr_no == 1) { column = col; sr_no++; } else
	 * column = column+"||'|'||"+col; } logger.info("Col is "+column);
	 * 
	 * 
	 * String GET_REMARKS
	 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
	 * + " from "+tableName+"  where  FILEDATE = '" +beanObj.getDatepicker()
	 * +"' and (dcrs_remarks like '%UNRECON-2%' or dcrs_remarks like '%NFS-RUPAY-ONUS%')"
	 * ;
	 * 
	 * 
	 * logger.info("GET_REMARKS"+GET_REMARKS);
	 * 
	 * List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS,new
	 * Object[]{},new RowMapper<String>() {
	 * 
	 * @Override public String mapRow(ResultSet rs,int rowcount)throws SQLException
	 * { return rs.getString("DCRS_REMARKS"); }
	 * 
	 * });
	 * 
	 * 
	 * for(String remark : stRemarks) { String getData =
	 * "select "+column+" as data from "+tableName
	 * +" where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and "
	 * +" dcrs_remarks like '%"+remark+"%'";
	 * 
	 * logger.info("GetData is "+getData);
	 * 
	 * PreparedStatement ps = getConnection().prepareStatement(getData); ResultSet
	 * rset = ps.executeQuery();
	 * 
	 * stFileName =
	 * //beanObj.getCategory()+"_"+beanObj.getStsubCategory().substring(0, 3)+"-"
	 * (tableName.contains("CBS")?"CBS":beanObj.getCategory());
	 * 
	 * if(remark.contains("UNRECON")) { stFileName = stFileName+"_UNMATCH"; } else
	 * if(remark.contains("MATCHED")) { stFileName = stFileName+"_MATCHED"; } else {
	 * stFileName = stFileName+"_"+remark; }
	 * 
	 * File myFile = new File(beanObj.getStPath()+File.separator+stFileName);
	 * logger.info("Path is "+beanObj.getStPath()+File.separator+remark);
	 * myFile.createNewFile();
	 * 
	 * BufferedWriter out = new BufferedWriter(new
	 * FileWriter(beanObj.getStPath()+File.separator+"//"+stFileName, true));
	 * 
	 * out.write(headercolumns);
	 * 
	 * 
	 * while(rset.next()) { out.write("\n"); out.write(rset.getString("data"));
	 * 
	 * } out.flush(); out.close();
	 * 
	 * files.add(beanObj.getStPath()+File.separator+stFileName); }
	 * 
	 * }
	 * 
	 * fos = new FileOutputStream(beanObj.getStPath()+File.separator+
	 * beanObj.getCategory()+"-"+beanObj.getStsubCategory().substring(0, 3)
	 * +"-REPORTS.zip"); zipOut = new ZipOutputStream(new
	 * BufferedOutputStream(fos)); try { for(String filespath : files) { File input
	 * = new File(filespath); fis = new FileInputStream(input); ZipEntry ze = new
	 * ZipEntry(input.getName()); zipOut.putNextEntry(ze); byte[] tmp = new
	 * byte[4*1024]; int size = 0; while((size = fis.read(tmp)) != -1){
	 * zipOut.write(tmp, 0, size); } zipOut.flush(); fis.close(); } zipOut.close();
	 * } catch(Exception fe) { System.out.println("Exception in zipping is "+fe); }
	 * 
	 * } catch(Exception e) { logger.info("Exception in generate_dhana_reports "+e);
	 * 
	 * } }
	 */

	public String generate_Dhana_Reports(SettlementBean beanObj) throws Exception {
		int sr_no = 1, index = 1;
		String stFileName = "";
		FileOutputStream fos = null;
		ZipOutputStream zipOut = null;
		FileInputStream fis = null;
		int colcount = 0;
		List<String> files = new ArrayList<>();

		String monthdate = generalUtil.DateFunction(beanObj.getDatepicker());

		try {
			// create folder first
			checkNcreateFolder(beanObj, new ArrayList<String>());

			List<String> tables = new ArrayList<String>();
			if (beanObj.getCategory().equals("NFS")) {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs_nainital");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase() + "_nainital");
			} else {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase());
			}

			List<String> Headers = new ArrayList<String>();
			List<List<String>> Data = new ArrayList<List<String>>();

			for (String tableName : tables) {
				sr_no = 1;
				List<String> columns = new ArrayList<String>();
				String headercolumns = "";
				String column = "";
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					headercolumns = "filedate|dcrs_remarks|seg_tran_id|tc|tcr_code|card_number|source_amount|authorization_code|dcrs_seq_no|trace|reference_number|response_code|req_msgtype";
					// headercolumns =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(filedate,'|'),dcrs_remarks),'|'),seg_tran_id),'|'),tc),'|'),tcr_code),'|'),
					// card_number),'|'),source_amount
					// ),'|'),authorization_code),'|'),dcrs_seq_no),'|'),trace),'|'),reference_number),'|'),response_code),'|'),req_msgtype)";
				} else {
					columns = getDhanaColumnList(tableName);
					

					ArrayList<String> columns1 = new ArrayList<String>();
					columns1.add("TRAN_DATE|TRAN_TIME|ACCOUNTID|NARRATION|TXN_TYPE|RRN|TXN_INDCTR|AMOUNT|BRANCH_CODE|ACCOUNT_NAME|FROM_ACCOUNT|TO_ACCOUNT|CUSTOMER_ID|UNQ_TXN_ID||FILEDATE|DCRS_REMARKS");
					
					colcount = 0;

					for (String headercols : columns) {
						if (colcount == 0) {

							headercolumns = headercols;
						} else {

							headercolumns = headercolumns + "|" + headercols;

						//headercolumns = headercolumns ;
						}
						colcount++;
					}

				}
				Headers.add(headercolumns);
				logger.info("HEaders are :" + headercolumns);

				for (String col : columns) {
					if (sr_no == 1) {
						column = col;
						sr_no++;
					} else { // column = column+"||'|'||"+col;

						if (col.equalsIgnoreCase("fpan")) {
							// cast(AES_DECRYPT(aes_encrypt(fpan ,'key_dbank'),'key_dbank')as char)
							// column =
							// "concat(concat("+column+",'|'),trim(cast(AES_DECRYPT("+col+",'key_dbank')as
							// char) ))";
//							column = column + "||'|'||ibkl_encrypt_decrypt.ibkl_get_encrypt_val(fpan)";
							column = column + "||'|'||(fpan)";
						} else if (col.equalsIgnoreCase("account_1_number")) {
							// column = "concat(concat("+column+",'|'),substring(account_1_number,-15))";
							column = column + "||'|'||substr(account_1_number,-15)";
						} else {
							// column = "concat(concat("+column+",'|'),ifnull(trim("+col+"),''))";
							column = column + "||'|'||nvl(trim(" + col + "),'')";
						}
					}
				}
				logger.info("column is " + column);
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					// column =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(ifnull(filedate,''),'|'),ifnull(dcrs_remarks,'')),'|'),ifnull(seg_tran_id,'')),'|'),ifnull(tc,'')),'|')
					// ,ifnull(tcr_code,'')),'|'),ifnull(card_number,'')),'|'),ifnull(source_amount,'')),'|'),ifnull(authorization_code,'')),'|'),ifnull(dcrs_seq_no,'')),'|'),ifnull(trace,'')),'|')
					// ,ifnull(reference_number,'')),'|'),ifnull(response_code,'')),'|'),ifnull(req_msgtype,''))";
					column = "FILEDATE||'|'||DCRS_REMARKS||'|'||SEG_TRAN_ID||'|'||TC||'|'||TCR_CODE||'|'||CARD_NUMBER||'|'||SOURCE_AMOUNT||'|'||AUTHORIZATION_CODE||'|'||DCRS_SEQ_NO||'|'||TRACE||'|'||REFERENCE_NUMBER||'|'||RESPONSE_CODE||'|'||REQ_MSGTYPE";
				}

				logger.info("Col is " + column);

				/*
				 * String GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and dcrs_remarks like '%UNRECON-2%' ";
				 * 
				 * if(tableName.contains("CBS")) {
				 * if(beanObj.getCategory().equalsIgnoreCase("NFS")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')";// or
				 * dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } else
				 * if(beanObj.getCategory().equalsIgnoreCase("VISA") &&
				 * beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } }
				 * 
				 * logger.info("GET_REMARKS"+GET_REMARKS);
				 * 
				 * List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS,new
				 * Object[]{},new RowMapper<String>() {
				 * 
				 * @Override public String mapRow(ResultSet rs,int rowcount)throws SQLException
				 * { return rs.getString("DCRS_REMARKS"); }
				 * 
				 * });
				 */

				String getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
						+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-2%'";

				if (tableName.contains("cbs")) {
					if (beanObj.getCategory().equalsIgnoreCase("NFS")) {
						if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
							getData = "select " + column + " as data from " + tableName
									+ " where filedate = to_date('"+ monthdate + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS_ISS-UNRECON-2%'  OR dcrs_remarks like '%UNMATCHED_REVERSAL%' )"
									+ " and amount > 0 ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc ";

						} else {
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS-RUPAY-ONUS%'  or dcrs_remarks like '%NFS-ACQ-UNRECON-2%' ) and amount > 0 ";
						}
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref != '2316'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref = '2316'";
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " (dcrs_remarks like '%UNRECON%')";
					} else if (beanObj.getCategory().equalsIgnoreCase("RUPAY")
							&& beanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")
							&& beanObj.getFileName().equalsIgnoreCase("ECOM")) {
						/*
						 * getData = "select " + column + " as data from " + tableName +
						 * " where filedate = to_date('" + beanObj.getDatepicker() +
						 * "','dd/mm/yyyy') and " + " dcrs_remarks like '%SUR-UNRECON-3%' ";
						 */

						getData = "select  " + column + " as data from " + tableName
								+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
								+ " (dcrs_remarks like '%UNRECON-2%' ) AND " + "TXN_TYPE = 'COM' "
								+ "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc ";

						// AND E = 'C'
					} else if (beanObj.getCategory().equalsIgnoreCase("RUPAY")
							&& beanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")
							&& beanObj.getFileName().equalsIgnoreCase("POS")) {
						/*
						 * getData = "select " + column + " as data from " + tableName +
						 * " where filedate = to_date('" + beanObj.getDatepicker() +
						 * "','dd/mm/yyyy') and " + " dcrs_remarks like '%SUR-UNRECON-3%' ";
						 */
						// OR dcrs_remarks like '%FAILED%'

						getData = "select  " + column + " as data from " + tableName
								+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
								+ "  dcrs_remarks like '%UNRECON-2%'  AND " + "TXN_TYPE = 'POS' "
								+ "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC  , substr(narration,19,2) asc ";

					}

					else if (beanObj.getCategory().equalsIgnoreCase("CASHNET")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks = 'CBS-CASHNET-FAILED'";

					}

				} else if (tableName.contains("visa")) {
					if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC != '07'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC = '07'";
					} else if (beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks like '%UNRECON-2%' and response_code = '00' and cast(source_amount as unsigned) > 0 and "
								+ " req_msgtype = '200'";
					}
				} else if (tableName.contains("rupay")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				} else if (!tableName.contains("nfs")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				}

				logger.info("GetData is " + getData);

				PreparedStatement ps = getConnection().prepareStatement(getData);
				ResultSet rset = ps.executeQuery();
				List<String> report_data = new ArrayList<String>();
				while (rset.next()) {
					report_data.add(rset.getString("data"));
				}
				Data.add(report_data);
				ps.close();
			}

			// create single file on top cbs data and below tie up data
			// stFileName = "UNMATCH_" + beanObj.getStMergerCategory() + ".csv";
			stFileName = "UNMATCH_" + beanObj.getStMergerCategory() + ".csv";

			if (beanObj.getCategory().equalsIgnoreCase("VISA")) {
				stFileName = "UNMATCH_" + beanObj.getCategory() + "_" + beanObj.getFileName() + ".csv";
			}

			File myFile = new File(beanObj.getStPath() + File.separator + stFileName);
			logger.info("Path is " + beanObj.getStPath() + File.separator + stFileName);
			myFile.createNewFile();

			/*
			 * BufferedWriter out = new BufferedWriter( new FileWriter(beanObj.getStPath() +
			 * File.separator + "//" + stFileName, true));
			 */

			BufferedWriter out = new BufferedWriter(
					new FileWriter(beanObj.getStPath() + File.separator + stFileName, true));
			/*
			 * for(int i = 0; i< Data.size() ;i++) { index = 0; logger.info(Headers.get(i));
			 * out.write(Headers.get(i)); out.write("\n"); List<String> repoData =
			 * Data.get(i); for(int j = 0; j< repoData.size(); j++) //for(List<String>
			 * repoData : Data) { out.write(repoData.get(j)); out.write("\n"); } }
			 */
			for (int i = 0; i < Headers.size(); i++) {
				index = 0;
				logger.info("HEaders are " + Headers.get(i));
				out.write(Headers.get(i));
				out.write("\n");
				List<String> repoData = Data.get(i);

				for (int j = 0; j < repoData.size(); j++) {
					out.write(repoData.get(j));
					out.write("\n");
				}
				out.write("\n");
			}
			out.flush();
			out.close();
			
			logger.info("INSIDE generate dhana reports");

			// File file = new File(beanObj.getStPath());
			// String[] filelist = file.list();
			/*
			 * String [] filelist = file.list();
			 * 
			 * for(String Names : filelist ) { logger.info("name is "+Names);
			 * files.add(beanObj.getStPath()+File.separator+Names); }
			 * 
			 * fos = new
			 * FileOutputStream(beanObj.getStPath()+File.separator+beanObj.getCategory()+
			 * ".zip"); zipOut = new ZipOutputStream(new BufferedOutputStream(fos)); try {
			 * for(String filespath : files) { File input = new File(filespath); fis = new
			 * FileInputStream(input); ZipEntry ze = new ZipEntry(input.getName()); //
			 * System.out.println("Zipping the file: "+input.getName());
			 * zipOut.putNextEntry(ze); byte[] tmp = new byte[4*1024]; int size = 0;
			 * while((size = fis.read(tmp)) != -1){ zipOut.write(tmp, 0, size); }
			 * zipOut.flush(); fis.close(); } zipOut.close(); //
			 * System.out.println("Done... Zipped the files..."); } catch(Exception fe) {
			 * System.out.println("Exception in zipping is "+fe); }
			 */

		} catch (Exception e) {
			logger.info("Exception in generate_dhana_reports " + e);

		}
		return stFileName;
	}

	public String generate_Dhana_Reports_Knockoff(SettlementBean beanObj) throws Exception {
		int sr_no = 1, index = 1;
		String stFileName2 = "";
		FileOutputStream fos = null;
		ZipOutputStream zipOut = null;
		FileInputStream fis = null;
		int colcount = 0;
		List<String> files = new ArrayList<>();

		String monthdate = generalUtil.DateFunction(beanObj.getDatepicker());

		try {
			// create folder first
			checkNcreateFolder(beanObj, new ArrayList<String>());

			List<String> tables = new ArrayList<String>();
			if (beanObj.getCategory().equals("NFS")) {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs_nainital");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase() + "_nainital");
			} else {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase());
			}

			List<String> Headers = new ArrayList<String>();
			List<List<String>> Data = new ArrayList<List<String>>();

			for (String tableName : tables) {
				sr_no = 1;
				List<String> columns = new ArrayList<String>();
				String headercolumns = "";
				String column = "";
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					headercolumns = "filedate|dcrs_remarks|seg_tran_id|tc|tcr_code|card_number|source_amount|authorization_code|dcrs_seq_no|trace|reference_number|response_code|req_msgtype";
					// headercolumns =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(filedate,'|'),dcrs_remarks),'|'),seg_tran_id),'|'),tc),'|'),tcr_code),'|'),
					// card_number),'|'),source_amount
					// ),'|'),authorization_code),'|'),dcrs_seq_no),'|'),trace),'|'),reference_number),'|'),response_code),'|'),req_msgtype)";
				} else {
					columns = getDhanaColumnList(tableName);

					colcount = 0;

					for (String headercols : columns) {
						if (colcount == 0) {

							headercolumns = headercols;
						} else {

							headercolumns = headercolumns + "|" + headercols;
						}
						colcount++;
					}

				}
				Headers.add(headercolumns);
				logger.info("HEaders are :" + headercolumns);

				for (String col : columns) {
					if (sr_no == 1) {
						column = col;
						sr_no++;
					} else { // column = column+"||'|'||"+col;

						if (col.equalsIgnoreCase("fpan")) {
							// cast(AES_DECRYPT(aes_encrypt(fpan ,'key_dbank'),'key_dbank')as char)
							// column =
							// "concat(concat("+column+",'|'),trim(cast(AES_DECRYPT("+col+",'key_dbank')as
							// char) ))";
//							column = column + "||'|'||ibkl_encrypt_decrypt.ibkl_get_encrypt_val(fpan)";
							column = column + "||'|'||(fpan)";
						} else if (col.equalsIgnoreCase("account_1_number")) {
							// column = "concat(concat("+column+",'|'),substring(account_1_number,-15))";
							column = column + "||'|'||substr(account_1_number,-15)";
						} else {
							// column = "concat(concat("+column+",'|'),ifnull(trim("+col+"),''))";
							column = column + "||'|'||nvl(trim(" + col + "),'')";
						}
					}
				}
				logger.info("column is " + column);
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					// column =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(ifnull(filedate,''),'|'),ifnull(dcrs_remarks,'')),'|'),ifnull(seg_tran_id,'')),'|'),ifnull(tc,'')),'|')
					// ,ifnull(tcr_code,'')),'|'),ifnull(card_number,'')),'|'),ifnull(source_amount,'')),'|'),ifnull(authorization_code,'')),'|'),ifnull(dcrs_seq_no,'')),'|'),ifnull(trace,'')),'|')
					// ,ifnull(reference_number,'')),'|'),ifnull(response_code,'')),'|'),ifnull(req_msgtype,''))";
					column = "FILEDATE||'|'||DCRS_REMARKS||'|'||SEG_TRAN_ID||'|'||TC||'|'||TCR_CODE||'|'||CARD_NUMBER||'|'||SOURCE_AMOUNT||'|'||AUTHORIZATION_CODE||'|'||DCRS_SEQ_NO||'|'||TRACE||'|'||REFERENCE_NUMBER||'|'||RESPONSE_CODE||'|'||REQ_MSGTYPE";
				}

				logger.info("Col is " + column);

				/*
				 * String GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and dcrs_remarks like '%UNRECON-2%' ";
				 * 
				 * if(tableName.contains("CBS")) {
				 * if(beanObj.getCategory().equalsIgnoreCase("NFS")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')";// or
				 * dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } else
				 * if(beanObj.getCategory().equalsIgnoreCase("VISA") &&
				 * beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } }
				 * 
				 * logger.info("GET_REMARKS"+GET_REMARKS);
				 * 
				 * List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS,new
				 * Object[]{},new RowMapper<String>() {
				 * 
				 * @Override public String mapRow(ResultSet rs,int rowcount)throws SQLException
				 * { return rs.getString("DCRS_REMARKS"); }
				 * 
				 * });
				 */

				String getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
						+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-2%'";

				if (tableName.contains("cbs")) {
					if (beanObj.getCategory().equalsIgnoreCase("NFS")) {
						if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
							getData = "select  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS-CBS_KNOCKOFF%' ) and amount > 0 AND SUBSTR(NARRATION , 0 , 4) = 'CWDR' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc  ";

						} else {
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS-RUPAY-ONUS%'  or dcrs_remarks like '%NFS-ACQ-UNRECON-2%' ) and amount > 0 ";
						}
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref != '2316'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref = '2316'";
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " (dcrs_remarks like '%UNRECON%')";
					} else if (beanObj.getCategory().equalsIgnoreCase("RUPAY")
							&& beanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")) {
						/*
						 * getData = "select " + column + " as data from " + tableName +
						 * " where filedate = to_date('" + beanObj.getDatepicker() +
						 * "','dd/mm/yyyy') and " + " dcrs_remarks like '%SUR-UNRECON-3%' ";
						 */

						if (beanObj.getFileName().equalsIgnoreCase("POS")) {
							getData = "select  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%KNOCKOFF%' and  TXN_TYPE = 'POS' " + "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc ";
						} else {
							getData = "select   " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%KNOCKOFF%' and  TXN_TYPE = 'COM' " + "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc ";
						}

						// AND E = 'C'
					} else if (beanObj.getCategory().equalsIgnoreCase("CASHNET")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks = 'CBS-CASHNET-FAILED'";

					}

				} else if (tableName.contains("visa")) {
					if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC != '07'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC = '07'";
					} else if (beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks like '%UNRECON-2%' and response_code = '00' and cast(source_amount as unsigned) > 0 and "
								+ " req_msgtype = '200'";
					}
				} else if (tableName.contains("rupay")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				} else if (tableName.contains("nfs")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				}

				logger.info("GetData is " + getData);

				PreparedStatement ps = getConnection().prepareStatement(getData);
				ResultSet rset = ps.executeQuery();
				List<String> report_data = new ArrayList<String>();
				while (rset.next()) {
					report_data.add(rset.getString("data"));
				}
				Data.add(report_data);
				ps.close();
				getConnection().close();

			}

			// create single file on top cbs data and below tie up data
			// stFileName2 = "KNOCKOFF_" + beanObj.getStMergerCategory() + ".csv";
			stFileName2 = "AUTOREVERSE_" + beanObj.getStMergerCategory() + ".csv";

			if (beanObj.getCategory().equalsIgnoreCase("VISA")) {
				stFileName2 = "AUTOREVERSE_" + beanObj.getCategory() + "_" + beanObj.getFileName() + ".csv";
			}

			File myFile = new File(beanObj.getStPath() + File.separator + stFileName2);
			logger.info("Path is " + beanObj.getStPath() + File.separator + stFileName2);
			myFile.createNewFile();

			BufferedWriter out = new BufferedWriter(
					new FileWriter(beanObj.getStPath() + File.separator + "//" + stFileName2, true));
			/*
			 * for(int i = 0; i< Data.size() ;i++) { index = 0; logger.info(Headers.get(i));
			 * out.write(Headers.get(i)); out.write("\n"); List<String> repoData =
			 * Data.get(i); for(int j = 0; j< repoData.size(); j++) //for(List<String>
			 * repoData : Data) { out.write(repoData.get(j)); out.write("\n"); } }
			 */
			for (int i = 0; i < Headers.size(); i++) {
				index = 0;
				logger.info("HEaders are " + Headers.get(i));
				out.write(Headers.get(i));
				out.write("\n");
				List<String> repoData = Data.get(i);

				for (int j = 0; j < repoData.size(); j++) {
					out.write(repoData.get(j));
					out.write("\n");
				}
				out.write("\n");
			}
			out.flush();
			out.close();

			File file = new File(beanObj.getStPath());
			// String[] filelist = file.list();
			String[] filelist = file.list();

			for (String Names : filelist) {
				logger.info("name is " + Names);
				files.add(beanObj.getStPath() + File.separator + Names);
			}

//	    	 fos = new FileOutputStream(beanObj.getStPath(beanObj.getFileName().equalsIgnoreCase(""pos))+File.separator+beanObj.getCategory()+ ".zip");
			if (!beanObj.getFileName().equalsIgnoreCase("POS") && !beanObj.getFileName().equalsIgnoreCase("ECOM")) {
				beanObj.setFileName("ATM");
			}
			fos = new FileOutputStream(beanObj.getStPath() + File.separator + beanObj.getFileName() + ".zip");
			zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			try {
				for (String filespath : files) {
					File input = new File(filespath);
					fis = new FileInputStream(input);
					ZipEntry ze = new ZipEntry(input.getName());
					// System.out.println("Zipping the file: "+input.getName());
					zipOut.putNextEntry(ze);
					byte[] tmp = new byte[4 * 1024];
					int size = 0;
					while ((size = fis.read(tmp)) != -1) {
						zipOut.write(tmp, 0, size);
					}
					zipOut.flush();
					fis.close();
					
					logger.info("INSIDE knockoff REPORTS");
				}
				zipOut.close();
				// System.out.println("Done... Zipped the files...");
			} catch (Exception fe) {
				System.out.println("Exception in zipping is " + fe);
			}

		} catch (Exception e) {
			logger.info("Exception in generate_dhana_reports " + e);

		}
		return beanObj.getFileName();
	}

	public String generate_Dhana_Reports_Failed(SettlementBean beanObj) throws Exception {
		int sr_no = 1, index = 1;
		String stFileName3 = "";
		FileOutputStream fos = null;
		ZipOutputStream zipOut = null;
		FileInputStream fis = null;
		int colcount = 0;
		List<String> files = new ArrayList<>();

		String monthdate = generalUtil.DateFunction(beanObj.getDatepicker());

		try {
			// create folder first
			checkNcreateFolder(beanObj, new ArrayList<String>());

			List<String> tables = new ArrayList<String>();
			if (beanObj.getCategory().equals("NFS")) {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs_nainital");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase() + "_nainital");
			} else {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase());
			}

			List<String> Headers = new ArrayList<String>();
			List<List<String>> Data = new ArrayList<List<String>>();

			for (String tableName : tables) {
				sr_no = 1;
				List<String> columns = new ArrayList<String>();
				String headercolumns = "";
				String column = "";
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					headercolumns = "filedate|dcrs_remarks|seg_tran_id|tc|tcr_code|card_number|source_amount|authorization_code|dcrs_seq_no|trace|reference_number|response_code|req_msgtype";
					// headercolumns =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(filedate,'|'),dcrs_remarks),'|'),seg_tran_id),'|'),tc),'|'),tcr_code),'|'),
					// card_number),'|'),source_amount
					// ),'|'),authorization_code),'|'),dcrs_seq_no),'|'),trace),'|'),reference_number),'|'),response_code),'|'),req_msgtype)";
				} else {
					columns = getDhanaColumnList(tableName);

					colcount = 0;

					for (String headercols : columns) {
						if (colcount == 0) {

							headercolumns = headercols;
						} else {

							headercolumns = headercolumns + "|" + headercols;
						}
						colcount++;
					}

				}
				Headers.add(headercolumns);
				logger.info("HEaders are :" + headercolumns);

				for (String col : columns) {
					if (sr_no == 1) {
						column = col;
						sr_no++;
					} else { // column = column+"||'|'||"+col;

						if (col.equalsIgnoreCase("fpan")) {
							// cast(AES_DECRYPT(aes_encrypt(fpan ,'key_dbank'),'key_dbank')as char)
							// column =
							// "concat(concat("+column+",'|'),trim(cast(AES_DECRYPT("+col+",'key_dbank')as
							// char) ))";
//							column = column + "||'|'||ibkl_encrypt_decrypt.ibkl_get_encrypt_val(fpan)";
							column = column + "||'|'||(fpan)";
						} else if (col.equalsIgnoreCase("account_1_number")) {
							// column = "concat(concat("+column+",'|'),substring(account_1_number,-15))";
							column = column + "||'|'||substr(account_1_number,-15)";
						} else {
							// column = "concat(concat("+column+",'|'),ifnull(trim("+col+"),''))";
							column = column + "||'|'||nvl(trim(" + col + "),'')";
						}
					}
				}
				logger.info("column is " + column);
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					// column =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(ifnull(filedate,''),'|'),ifnull(dcrs_remarks,'')),'|'),ifnull(seg_tran_id,'')),'|'),ifnull(tc,'')),'|')
					// ,ifnull(tcr_code,'')),'|'),ifnull(card_number,'')),'|'),ifnull(source_amount,'')),'|'),ifnull(authorization_code,'')),'|'),ifnull(dcrs_seq_no,'')),'|'),ifnull(trace,'')),'|')
					// ,ifnull(reference_number,'')),'|'),ifnull(response_code,'')),'|'),ifnull(req_msgtype,''))";
					column = "FILEDATE||'|'||DCRS_REMARKS||'|'||SEG_TRAN_ID||'|'||TC||'|'||TCR_CODE||'|'||CARD_NUMBER||'|'||SOURCE_AMOUNT||'|'||AUTHORIZATION_CODE||'|'||DCRS_SEQ_NO||'|'||TRACE||'|'||REFERENCE_NUMBER||'|'||RESPONSE_CODE||'|'||REQ_MSGTYPE";
				}

				logger.info("Col is " + column);

				/*
				 * String GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and dcrs_remarks like '%UNRECON-2%' ";
				 * 
				 * if(tableName.contains("CBS")) {
				 * if(beanObj.getCategory().equalsIgnoreCase("NFS")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')";// or
				 * dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } else
				 * if(beanObj.getCategory().equalsIgnoreCase("VISA") &&
				 * beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } }
				 * 
				 * logger.info("GET_REMARKS"+GET_REMARKS);
				 * 
				 * List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS,new
				 * Object[]{},new RowMapper<String>() {
				 * 
				 * @Override public String mapRow(ResultSet rs,int rowcount)throws SQLException
				 * { return rs.getString("DCRS_REMARKS"); }
				 * 
				 * });
				 */

				String getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
						+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-2%'";

				if (tableName.contains("cbs")) {
					if (beanObj.getCategory().equalsIgnoreCase("NFS")) {
						if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
							getData = "select  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%FAILED%' ) and amount > 0 ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc  ";

						} else {
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS-RUPAY-ONUS%'  or dcrs_remarks like '%NFS-ACQ-UNRECON-2%' ) and amount > 0 ";
						}
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref != '2316'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref = '2316'";
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " (dcrs_remarks like '%UNRECON%')";
					} else if (beanObj.getCategory().equalsIgnoreCase("RUPAY")
							&& beanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")) {
						/*
						 * getData = "select " + column + " as data from " + tableName +
						 * " where filedate = to_date('" + beanObj.getDatepicker() +
						 * "','dd/mm/yyyy') and " + " dcrs_remarks like '%SUR-UNRECON-3%' ";
						 */
						if (beanObj.getFileName().equalsIgnoreCase("POS")) {
							getData = "select  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ "dcrs_remarks like '%FAILED%'  and  TXN_TYPE = 'POS'  " + "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc";

						} else {
							getData = "select  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ "dcrs_remarks like '%FAILED%'  and  TXN_TYPE = 'COM'  " + "and TXN_INDCTR = 'D' ORDER BY SUBSTR(NARRATION,30,8) ASC , substr(narration,19,2) asc";
						}

						// AND E = 'C'
					} else if (beanObj.getCategory().equalsIgnoreCase("CASHNET")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks = 'CBS-CASHNET-FAILED'";

					}

				} else if (tableName.contains("visa")) {
					if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC != '07'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC = '07'";
					} else if (beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks like '%UNRECON-2%' and response_code = '00' and cast(source_amount as unsigned) > 0 and "
								+ " req_msgtype = '200'";
					}
				} else if (tableName.contains("rupay")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				} else if (tableName.contains("nfs")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				}

				logger.info("GetData is " + getData);

				PreparedStatement ps = getConnection().prepareStatement(getData);
				ResultSet rset = ps.executeQuery();
				List<String> report_data = new ArrayList<String>();
				while (rset.next()) {
					report_data.add(rset.getString("data"));
				}
				Data.add(report_data);

			}

			// create single file on top cbs data and below tie up data
			stFileName3 = "FAILED_" + beanObj.getStMergerCategory() + ".csv";

			if (beanObj.getCategory().equalsIgnoreCase("VISA")) {
				stFileName3 = "FAILED_" + beanObj.getCategory() + "_" + beanObj.getFileName() + ".csv";
			}

			File myFile = new File(beanObj.getStPath() + File.separator + stFileName3);
			logger.info("Path is " + beanObj.getStPath() + File.separator + stFileName3);
			myFile.createNewFile();

			BufferedWriter out = new BufferedWriter(
					new FileWriter(beanObj.getStPath() + File.separator + "//" + stFileName3, true));
			/*
			 * for(int i = 0; i< Data.size() ;i++) { index = 0; logger.info(Headers.get(i));
			 * out.write(Headers.get(i)); out.write("\n"); List<String> repoData =
			 * Data.get(i); for(int j = 0; j< repoData.size(); j++) //for(List<String>
			 * repoData : Data) { out.write(repoData.get(j)); out.write("\n"); } }
			 */
			for (int i = 0; i < Headers.size(); i++) {
				index = 0;
				logger.info("HEaders are " + Headers.get(i));
				out.write(Headers.get(i));
				out.write("\n");
				List<String> repoData = Data.get(i);

				for (int j = 0; j < repoData.size(); j++) {
					out.write(repoData.get(j));
					out.write("\n");
				}
				out.write("\n");
			}
			out.flush();
			out.close();
			
			logger.info("INSIDE dhana reports failed ");

			// File file = new File(beanObj.getStPath());
			// String[] filelist = file.list();
			/*
			 * String [] filelist = file.list();
			 * 
			 * for(String Names : filelist ) { logger.info("name is "+Names);
			 * files.add(beanObj.getStPath()+File.separator+Names); }
			 * 
			 * fos = new
			 * FileOutputStream(beanObj.getStPath()+File.separator+beanObj.getCategory()+
			 * ".zip"); zipOut = new ZipOutputStream(new BufferedOutputStream(fos)); try {
			 * for(String filespath : files) { File input = new File(filespath); fis = new
			 * FileInputStream(input); ZipEntry ze = new ZipEntry(input.getName()); //
			 * System.out.println("Zipping the file: "+input.getName());
			 * zipOut.putNextEntry(ze); byte[] tmp = new byte[4*1024]; int size = 0;
			 * while((size = fis.read(tmp)) != -1){ zipOut.write(tmp, 0, size); }
			 * zipOut.flush(); fis.close(); } zipOut.close(); //
			 * System.out.println("Done... Zipped the files..."); } catch(Exception fe) {
			 * System.out.println("Exception in zipping is "+fe); }
			 */

		} catch (Exception e) {
			logger.info("Exception in generate_dhana_reports " + e);

		}
		return stFileName3;
	}

	public String generate_Dhana_Reports_Matched(SettlementBean beanObj) throws Exception {
		int sr_no = 1, index = 1;
		String stFileName3 = "";
		FileOutputStream fos = null;
		ZipOutputStream zipOut = null;
		FileInputStream fis = null;
		int colcount = 0;
		List<String> files = new ArrayList<>();

		String monthdate = generalUtil.DateFunction(beanObj.getDatepicker());

		try {
			// create folder first
			checkNcreateFolder(beanObj, new ArrayList<String>());

			List<String> tables = new ArrayList<String>();
			if (beanObj.getCategory().equals("NFS")) {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs_nainital");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase() + "_nainital");
			} else {
				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_cbs");

				tables.add("settlement_" + beanObj.getCategory().toLowerCase() + "_"
						+ beanObj.getStsubCategory().substring(0, 3).toLowerCase() + "_"
						+ beanObj.getCategory().toLowerCase());
			}

			List<String> Headers = new ArrayList<String>();
			List<List<String>> Data = new ArrayList<List<String>>();

			for (String tableName : tables) {
				sr_no = 1;
				List<String> columns = new ArrayList<String>();
				String headercolumns = "";
				String column = "";
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					headercolumns = "filedate|dcrs_remarks|seg_tran_id|tc|tcr_code|card_number|source_amount|authorization_code|dcrs_seq_no|trace|reference_number|response_code|req_msgtype";
					// headercolumns =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(filedate,'|'),dcrs_remarks),'|'),seg_tran_id),'|'),tc),'|'),tcr_code),'|'),
					// card_number),'|'),source_amount
					// ),'|'),authorization_code),'|'),dcrs_seq_no),'|'),trace),'|'),reference_number),'|'),response_code),'|'),req_msgtype)";
				} else {
					columns = getDhanaColumnList(tableName);

					colcount = 0;

					for (String headercols : columns) {
						if (colcount == 0) {

							headercolumns = headercols;
						} else {

							headercolumns = headercolumns + "|" + headercols;
						}
						colcount++;
					}

				}
				Headers.add(headercolumns);
				logger.info("HEaders are :" + headercolumns);

				for (String col : columns) {
					if (sr_no == 1) {
						column = col;
						sr_no++;
					} else { // column = column+"||'|'||"+col;

						if (col.equalsIgnoreCase("fpan")) {
							// cast(AES_DECRYPT(aes_encrypt(fpan ,'key_dbank'),'key_dbank')as char)
							// column =
							// "concat(concat("+column+",'|'),trim(cast(AES_DECRYPT("+col+",'key_dbank')as
							// char) ))";
//							column = column + "||'|'||ibkl_encrypt_decrypt.ibkl_get_encrypt_val(fpan)";
							column = column + "||'|'||(fpan)";
						} else if (col.equalsIgnoreCase("account_1_number")) {
							// column = "concat(concat("+column+",'|'),substring(account_1_number,-15))";
							column = column + "||'|'||substr(account_1_number,-15)";
						} else {
							// column = "concat(concat("+column+",'|'),ifnull(trim("+col+"),''))";
							column = column + "||'|'||nvl(trim(" + col + "),'')";
						}
					}
				}
				logger.info("column is " + column);
				if (beanObj.getCategory().equalsIgnoreCase("VISA")
						&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER") && !tableName.contains("cbs")) {
					// column =
					// "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(ifnull(filedate,''),'|'),ifnull(dcrs_remarks,'')),'|'),ifnull(seg_tran_id,'')),'|'),ifnull(tc,'')),'|')
					// ,ifnull(tcr_code,'')),'|'),ifnull(card_number,'')),'|'),ifnull(source_amount,'')),'|'),ifnull(authorization_code,'')),'|'),ifnull(dcrs_seq_no,'')),'|'),ifnull(trace,'')),'|')
					// ,ifnull(reference_number,'')),'|'),ifnull(response_code,'')),'|'),ifnull(req_msgtype,''))";
					column = "FILEDATE||'|'||DCRS_REMARKS||'|'||SEG_TRAN_ID||'|'||TC||'|'||TCR_CODE||'|'||CARD_NUMBER||'|'||SOURCE_AMOUNT||'|'||AUTHORIZATION_CODE||'|'||DCRS_SEQ_NO||'|'||TRACE||'|'||REFERENCE_NUMBER||'|'||RESPONSE_CODE||'|'||REQ_MSGTYPE";
				}

				logger.info("Col is " + column);

				/*
				 * String GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and dcrs_remarks like '%UNRECON-2%' ";
				 * 
				 * if(tableName.contains("CBS")) {
				 * if(beanObj.getCategory().equalsIgnoreCase("NFS")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')";// or
				 * dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } else
				 * if(beanObj.getCategory().equalsIgnoreCase("VISA") &&
				 * beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) { GET_REMARKS
				 * ="select distinct regexp_substr(translate(REGEXP_REPLACE (DCRS_REMARKS, '\', ''),'()',' '),'[^ - ]+',1,1) as DCRS_REMARKS"
				 * + " from "+tableName+"  where  FILEDATE = '"
				 * +beanObj.getDatepicker()+"' and (dcrs_remarks like '%NFS-RUPAY-ONUS%')"; } }
				 * 
				 * logger.info("GET_REMARKS"+GET_REMARKS);
				 * 
				 * List<String> stRemarks = getJdbcTemplate().query(GET_REMARKS,new
				 * Object[]{},new RowMapper<String>() {
				 * 
				 * @Override public String mapRow(ResultSet rs,int rowcount)throws SQLException
				 * { return rs.getString("DCRS_REMARKS"); }
				 * 
				 * });
				 */

				String getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
						+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-2%'";

				if (tableName.contains("cbs")) {
					if (beanObj.getCategory().equalsIgnoreCase("NFS")) {
						if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
							getData = "select distinct  " + column + " as data from " + tableName
									+ " where filedate = to_date('" + beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%MATCHED-2%' ) and amount > 0   ";

						} else {
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " (dcrs_remarks like '%NFS-RUPAY-ONUS%'  or dcrs_remarks like '%NFS-ACQ-UNRECON-2%' ) and amount > 0 ";
						}
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref != '2316'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%SUR-UNRECON-3%' and sys_ref = '2316'";
					} else if (beanObj.getCategory().equalsIgnoreCase("VISA")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and " + " (dcrs_remarks like '%UNRECON%')";
					} else if (beanObj.getCategory().equalsIgnoreCase("RUPAY")
							&& beanObj.getStsubCategory().equalsIgnoreCase("DOMESTIC")) {
						/*
						 * getData = "select " + column + " as data from " + tableName +
						 * " where filedate = to_date('" + beanObj.getDatepicker() +
						 * "','dd/mm/yyyy') and " + " dcrs_remarks like '%SUR-UNRECON-3%' ";
						 */
						if (beanObj.getFileName().equalsIgnoreCase("POS")) {
							getData = "select distinct   " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ "dcrs_remarks like '%MATCHED-2%'  and  TXN_TYPE = 'POS'  "
									+ "and TXN_INDCTR = 'D' ";

						} else {
							getData = "select distinct   " + column + " as data from " + tableName
									+ " where filedate = to_date('" + monthdate + "','dd/mm/yyyy') and "
									+ "dcrs_remarks like '%MATCHED-2%'  and  TXN_TYPE = 'COM'  "
									+ "and TXN_INDCTR = 'D' ";
						}

						// AND E = 'C'
					} else if (beanObj.getCategory().equalsIgnoreCase("CASHNET")
							&& beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks = 'CBS-CASHNET-FAILED'";

					}

				} else if (tableName.contains("visa")) {
					if (beanObj.getStsubCategory().equalsIgnoreCase("ISSUER")) {
						if (beanObj.getFileName().equalsIgnoreCase("POS"))
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC != '07'";
						else
							getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
									+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
									+ " dcrs_remarks like '%UNRECON-3%' AND TC = '07'";
					} else if (beanObj.getStsubCategory().equalsIgnoreCase("ACQUIRER")) {
						getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
								+ beanObj.getDatepicker() + "','dd/mm/yyyy') and "
								+ " dcrs_remarks like '%UNRECON-2%' and response_code = '00' and cast(source_amount as unsigned) > 0 and "
								+ " req_msgtype = '200'";
					}
				} else if (tableName.contains("rupay")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				} else if (tableName.contains("nfs")) {
					getData = "select " + column + " as data from " + tableName + " where filedate = to_date('"
							+ monthdate + "','dd/mm/yyyy') and " + " dcrs_remarks like '%UNRECON-5%'";
				}

				logger.info("GetData is " + getData);

				PreparedStatement ps = getConnection().prepareStatement(getData);
				ResultSet rset = ps.executeQuery();
				List<String> report_data = new ArrayList<String>();
				while (rset.next()) {
					report_data.add(rset.getString("data"));
				}
				Data.add(report_data);

			}

			// create single file on top cbs data and below tie up data
			stFileName3 = "MATCHED_" + beanObj.getStMergerCategory() + ".csv";

			if (beanObj.getCategory().equalsIgnoreCase("VISA")) {
				stFileName3 = "MATCHED_" + beanObj.getCategory() + "_" + beanObj.getFileName() + ".csv";
			}

			File myFile = new File(beanObj.getStPath() + File.separator + stFileName3);
			logger.info("Path is " + beanObj.getStPath() + File.separator + stFileName3);
			myFile.createNewFile();

			BufferedWriter out = new BufferedWriter(
					new FileWriter(beanObj.getStPath() + File.separator + "//" + stFileName3, true));
			/*
			 * for(int i = 0; i< Data.size() ;i++) { index = 0; logger.info(Headers.get(i));
			 * out.write(Headers.get(i)); out.write("\n"); List<String> repoData =
			 * Data.get(i); for(int j = 0; j< repoData.size(); j++) //for(List<String>
			 * repoData : Data) { out.write(repoData.get(j)); out.write("\n"); } }
			 */
			for (int i = 0; i < Headers.size(); i++) {
				index = 0;
				logger.info("HEaders are " + Headers.get(i));
				out.write(Headers.get(i));
				out.write("\n");
				List<String> repoData = Data.get(i);

				for (int j = 0; j < repoData.size(); j++) {
					out.write(repoData.get(j));
					out.write("\n");
				}
				out.write("\n");
			}
			out.flush();
			out.close();
			
			logger.info("INSIDE matched REPORTS");

			// File file = new File(beanObj.getStPath());
			// String[] filelist = file.list();
			/*
			 * String [] filelist = file.list();
			 * 
			 * for(String Names : filelist ) { logger.info("name is "+Names);
			 * files.add(beanObj.getStPath()+File.separator+Names); }
			 * 
			 * fos = new
			 * FileOutputStream(beanObj.getStPath()+File.separator+beanObj.getCategory()+
			 * ".zip"); zipOut = new ZipOutputStream(new BufferedOutputStream(fos)); try {
			 * for(String filespath : files) { File input = new File(filespath); fis = new
			 * FileInputStream(input); ZipEntry ze = new ZipEntry(input.getName()); //
			 * System.out.println("Zipping the file: "+input.getName());
			 * zipOut.putNextEntry(ze); byte[] tmp = new byte[4*1024]; int size = 0;
			 * while((size = fis.read(tmp)) != -1){ zipOut.write(tmp, 0, size); }
			 * zipOut.flush(); fis.close(); } zipOut.close(); //
			 * System.out.println("Done... Zipped the files..."); } catch(Exception fe) {
			 * System.out.println("Exception in zipping is "+fe); }
			 */

		} catch (Exception e) {
			logger.info("Exception in generate_dhana_reports " + e);

		}
		return stFileName3;
	}

	public ArrayList<String> getDhanaColumnList(String tableName) {

		/*
		 * String query =
		 * "select column_name from information_schema.columns where table_schema = database() and table_name = '"
		 * +tableName.toLowerCase()+"' "
		 * +"and column_name not in('id','createdby','createddate','dcrs_tran_no','next_tran_date','part_id','foracid','balance','pstd_user_id','particularals2','org_acct',"
		 * +"'tran_type','seg_tran_id','man_contra_account','balance')";
		 * 
		 * System.out.println(query);
		 */
		// DEBITCARDRECON_UCO
		/*
		 * String query = "SELECT column_name FROM   all_tab_cols WHERE  table_name = '"
		 * + tableName.toUpperCase() +
		 * "' and owner = 'debitcard_recon' and column_name not like '%$%'" +
		 * " and column_name not in ('CREATEDBY','CREATEDDATE','DCRS_TRAN_NO','NEXT_TRAN_DATE','PART_ID','FORACID','BALANCE','PSTD_USER_ID','PARTICULARALS2','ORG_ACCT',"
		 * + "'TRAN_TYPE')";
		 */

		String query = "SELECT column_name FROM all_tab_cols WHERE table_name = '" + tableName.toUpperCase()
				+ "' and column_name not like '%$%'"
				+ " and column_name not in ('CREATEDBY','CREATEDDATE','DCRS_TRAN_NO','NEXT_TRAN_DATE','PART_ID','FORACID','BALANCE','PSTD_USER_ID','PARTICULARALS2','ORG_ACCT',"
				+ "'TRAN_TYPE', 'ISS_SETTLE_FEE', 'ISS_SETTLE_PROCESS_FEE', 'CARDHOLDER_BILL_CURRNCY_C', 'CARDHOLDER_BILL_AMOUNT', 'CARDHOLDER_BILL_ACT_FEE', 'CARDHOLDER_BILL_PROCESS_F', 'CARDHOLDER_BILL_SERV_FEE', 'TXN_CARDHOLDER_CONV_RT', 'BANK_NAME', 'ACCOUNT_1_BRANCH_ID' ,'ACCOUNT_2_NUMBER', 'ACCOUNT_2_BRANCH_ID')";
		System.out.println(query);

		ArrayList<String> typeList = (ArrayList<String>) getJdbcTemplate().query(query, new RowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});

		System.out.println(typeList);
		return typeList;

	}
}
