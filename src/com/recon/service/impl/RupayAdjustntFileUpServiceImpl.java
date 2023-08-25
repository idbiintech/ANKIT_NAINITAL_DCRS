package com.recon.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.SystemOutLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.recon.model.NFSSettlementBean;
import com.recon.service.RupayAdjustntFileUpService;
import com.recon.util.GeneralUtil;

public class RupayAdjustntFileUpServiceImpl extends JdbcDaoSupport implements RupayAdjustntFileUpService {

	@Autowired
	GeneralUtil genetalUtil;
	private static final String O_ERROR_MESSAGE = "o_error_message";

	public HashMap<String, Object> validateAdjustmentUpload(String fileDate, String cycle, String network,
			String subcategory, boolean presentmentFile) {
		HashMap<String, Object> output = new HashMap<String, Object>();
		String mdate =  genetalUtil.DateFunction(fileDate);
		try {
			String tableName = "";
			if (network.equalsIgnoreCase("RUPAY")) {
				if (subcategory.equalsIgnoreCase("DOMESTIC")) {
					tableName = "rupay_network_adjustment";
					//tableName = "rupay_network_adjustment_bkpbkp2222";
				} else {
					if (!presentmentFile)
						tableName = "RUPAY_INTERNATIONAL_ADJUSTMENT";
					else
						tableName = "RUPAY_INTERNATIONAL_PRESENTMENT";
				}

			} else {
				tableName = "RUPAY_NCMC_NETWORK_ADJUSTMENT";
			}

			System.out.println("tablename is"+tableName);
			String checkUpload = "select count(1) from " + tableName.toLowerCase()
					+ " where filedate = to_date(?,'dd/mm/yyyy') and cycle = ?";
			
			System.out.println("query is"+checkUpload);
			int uploadCount = getJdbcTemplate().queryForObject(checkUpload, new Object[] { mdate, cycle },
					Integer.class);
			
			System.out.println("checkupload is"+checkUpload );

			if (uploadCount == 0) {
				output.put("result", true);
			} else {
				output.put("result", false);
				output.put("msg", "File is already uploaded");
			}

		} catch (Exception e) {
			e.printStackTrace();
			output.put("result", false);
			output.put("msg", "Exception Occurred While checking");
			System.out.println("Exception is " + e);
		}
		return output;
	}

	@Override
	public HashMap<String, Object> rupayAdjustmentFileUpload(String fileDate, String createdBy, String cycle,
			String network, MultipartFile file, String subcategory) {

		HashMap<String, Object> output = new HashMap<String, Object>();
		int totalCount = 0;
		String res = "";
		String row = "";
		String line = "";
		char quote = '"';
		char BLANK = ' ';
		try {
			BufferedReader csvReader1 = new BufferedReader(new InputStreamReader(file.getInputStream()));
			Connection con = getConnection();
			String tableName = "";

			if (network.equalsIgnoreCase("RUPAY")) {
				if (subcategory.equalsIgnoreCase("DOMESTIC"))
					tableName = "RUPAY_NETWORK_ADJUSTMENT";
					//tableName = "rupay_network_adjustment_bkpbkp2222";
				else
					tableName = "RUPAY_INTERNATIONAL_ADJUSTMENT";
			} else {
				tableName = "RUPAY_NCMC_NETWORK_ADJUSTMENT";
			}

//			String sql = "INSERT INTO " + tableName.toLowerCase()
//					+ " (report_date,dispute_raise_date,dispute_raised_settl_date,case_number,function_code,function_code_description,primary_account_number,processing_code,"
//					+ "transaction_date,transaction_amount,txn_currency_code,settlement_amount,settlement_ccy_code,txn_settlement_date,amounts_additional,control_number,dispute_originator_pid,"
//					+ "dispute_destination_pid,acquire_ref_data,approval_code,originator_point,pos_entry_mode,pos_condition_code,acquirer_instituteid_code,acquirer_name_country,issuer_insti_id_code,"
//					+ "issuer_name_country,card_type,card_brand,card_acceptor_terminalid,card_acceptor_name,card_accept_location_add,card_accept_country_code,card_accept_buss_code,"
//					+ "dispute_reason_code,dispute_reason_cd_desc,dispute_amt,full_partial_indicator,dispute_member_msg_text,dispute_document_indicator,document_attached_date,mti,"
//					+ "incentive_amount,tier_cd_nonfullfill,tier_cd_fulfill,deadline_date,days_to_act,direction_iw_ow,last_adj_stage, filedate, createdby, cycle) "
//					+ "VALUES(?,?,?,?,?,?,?,?,"
//					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			String sql = "INSERT INTO " + tableName.toLowerCase()
			+ " (report_date,dispute_raise_date,dispute_raised_settl_date,case_number,function_code,function_code_description,primary_account_number,processing_code,"
			+ "transaction_date,transaction_amount,txn_currency_code,settlement_amount,settlement_ccy_code,txn_settlement_date,amounts_additional,control_number,dispute_originator_pid,"
			+ "dispute_destination_pid,acquire_ref_data,approval_code,originator_point,pos_entry_mode,pos_condition_code,acquirer_instituteid_code,acquirer_name_country,issuer_insti_id_code,"
			+ "issuer_name_country,card_type,card_brand,card_acceptor_terminalid,card_acceptor_name,card_accept_location_add,card_accept_country_code,card_accept_buss_code,"
			+ "dispute_reason_code,dispute_reason_cd_desc,dispute_amt,full_partial_indicator,dispute_member_msg_text,dispute_document_indicator,document_attached_date,mti,"
			+ "incentive_amount,tier_cd_nonfullfill,tier_cd_fulfill,deadline_date,days_to_act,direction_iw_ow,last_adj_stage,  createdby,filedate,cycle) "
			+ "VALUES(?,?,?,?,?,?,?,?,"
			+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ? , ?, to_date(?,'dd/mm/yy'),?)";

			if (subcategory.equalsIgnoreCase("INTERNATIONAL")) {
				sql = "INSERT INTO " + tableName
						+ " (REPORT_DATE,DISPUTE_RAISE_DATE,DISPUTE_RAISED_SETTL_DATE,CASE_NUMBER,Scheme_Name,Transaction_Flag,"
						+ "FUNCTION_CODE,FUNCTION_CODE_DESCRIPTION,PRIMARY_ACCOUNT_NUMBER,PROCESSING_CODE,"
						+ "TRANSACTION_DATE,TRANSACTION_AMOUNT,TXN_CURRENCY_CODE,SETTLEMENT_AMOUNT,SETTLEMENT_CCY_CODE,Conversion_Rate,TXN_SETTLEMENT_DATE,AMOUNTS_ADDITIONAL,CONTROL_NUMBER,DISPUTE_ORIGINATOR_PID,"
						+ "DISPUTE_DESTINATION_PID,ACQUIRE_REF_DATA,APPROVAL_CODE,ORIGINATOR_POINT,POS_ENTRY_MODE,POS_CONDITION_CODE,ACQUIRER_INSTITUTEID_CODE,ACQUIRER_NAME_COUNTRY,ISSUER_INSTI_ID_CODE,"
						+ "ISSUER_NAME_COUNTRY,CARD_TYPE,CARD_BRAND,CARD_ACCEPTOR_TERMINALID,CARD_ACCEPTOR_NAME,CARD_ACCEPT_LOCATION_ADD,CARD_ACCEPT_COUNTRY_CODE,CARD_ACCEPT_BUSS_CODE,"
						+ "DISPUTE_REASON_CODE,DISPUTE_REASON_CD_DESC,DISPUTE_AMT,FULL_PARTIAL_INDICATOR,DISPUTE_MEMBER_MSG_TEXT,DISPUTE_DOCUMENT_INDICATOR,DOCUMENT_ATTACHED_DATE,MTI,"
						+ "INCENTIVE_AMOUNT,TIER_CD_NONFULLFILL,TIER_CD_FULFILL,DEADLINE_DATE,DAYS_TO_ACT,DIRECTION_IW_OW, FILEDATE, CREATEDBY, CYCLE) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,"
						+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, to_date(?,'dd/mm/yy') , ?, ?)";
			}

//			PreparedStatement ps = con.prepareStatement(sql);
//			int count = 1;
//			int Number = 0;
//			con.setAutoCommit(false);
//			while ((row = csvReader1.readLine()) != null) {
//				int sr_no = 1;
//				Number++;
//				totalCount++;
//				if (row.contains("---END OF REPORT---") || row.contains("---End of Report---")) {
//					break;
//				}
//				if (count == 1) {
//					count++;
//					continue;
//				}
//
//				if (subcategory.equalsIgnoreCase("INTERNATIONAL")) {
//					line = row.replaceAll(",", "|");
//				} else {
//					line = row.replaceAll("\",\"", "|");
//					line = line.replace("\",", "|");
//					line = line.replace(",", "|");
//					line = line.replace("\"", "");
//					// System.out.println(line);
//				}
//				// String[] data = row.split(",");
//
//				String[] data = line.split("\\|");
//
//				for (int i = 0; i < data.length - 1; i++) {
//					//ps.setString(sr_no, data[i]);
//					System.out.println("" + sr_no + " , " + data[i]);
//					sr_no++;
//
//				}
//
//				
//				  ps.setString(sr_no++, fileDate); 
//				  ps.setString(sr_no++, createdBy);
//				  ps.setString(sr_no++, cycle);
//				 
//
//				ps.addBatch();
//
//				if (Number == 1000) {
//					System.out.print("Executed batch");
//					ps.executeBatch();
//				}
//
//			}
//			ps.executeBatch();
//
//			con.commit();
//			res = "success";
//			csvReader1.close();
//
//			output.put("result", true);
//			output.put("count", totalCount);
			
			// ,DISPUTE_RAISE_DATE,DISPUTE_RAISED_SETTL_DATE,CASE_NUMBER,FUNCTION_CODE,FUNCTION_CODE_DESCRIPTION,PRIMARY_ACCOUNT_NUMBER,PROCESSING_CODE
			// ,TO_DATE(?,'DD-MMM-YYYY'),TO_DATE(?,'DD-MMM-YYYY'),?,?,?,?,?
//			PreparedStatement ps = con.prepareStatement(sql);
//			int count = 1;
//			int Number = 0;
//			con.setAutoCommit(false);
//			while ((row = csvReader1.readLine()) != null) {
//				int sr_no = 1;
//				Number++;
//				totalCount++;
//				if (row.contains("---END OF REPORT---") || row.contains("---End of Report---")) {
//					break;
//				}
//				if (count == 1) {
//					count++;
//					continue;
//				}
//
//				if (subcategory.equalsIgnoreCase("INTERNATIONAL")) {
//					line = row.replaceAll(",", "|");
//				} else {
////					line = row.replaceAll("\",\"", "|");
////					line = line.replace("\",", "|");
////					line = line.replace("\"", "");
//					line = row.replaceAll(",", "|");
//					//line = line.replace(",", "|");
//					System.out.println("line is"+line);
//				}
//				// String[] data = row.split(",");
//				String[] data = line.split("\\|");
//
//				for (int i = 0; i < data.length ; i++) {
//					
//					ps.setString(sr_no, data[i]);
//					System.out.println("" + sr_no + " , " + data[i]);
//					sr_no++;
//				}
//			ps.setString(sr_no++, fileDate);
//				ps.setString(sr_no++, createdBy);
//////				ps.setString(sr_no++, cycle);
//
//				ps.addBatch();
//
//				if (Number == 1000) {
//					System.out.print("Executed batch");
//					ps.executeBatch();
//				}
//
//			}
//			ps.executeBatch();
//
//			con.commit();
//			res = "success";
//			csvReader1.close();
//
//			output.put("result", true);
//			output.put("count", totalCount);
			
			// ,DISPUTE_RAISE_DATE,DISPUTE_RAISED_SETTL_DATE,CASE_NUMBER,FUNCTION_CODE,FUNCTION_CODE_DESCRIPTION,PRIMARY_ACCOUNT_NUMBER,PROCESSING_CODE
			// ,TO_DATE(?,'DD-MMM-YYYY'),TO_DATE(?,'DD-MMM-YYYY'),?,?,?,?,?
			PreparedStatement ps = con.prepareStatement(sql);
			int count = 1;
			int Number = 0;
			con.setAutoCommit(false);
			while ((row = csvReader1.readLine()) != null) {
				int sr_no = 1;
				Number++;
				totalCount++;
				if (row.contains("---END OF REPORT---") || row.contains("---End of Report---")) {
					break;
				}
				if (count == 1) {
					count++;
					continue;
				}

				if (subcategory.equalsIgnoreCase("INTERNATIONAL")) {
					line = row.replaceAll(",", "|");
				} else {
					line = row.replaceAll("\",\"", "|");
					line = line.replace("\",", "|");
					line = line.replace("\"", "");
					// System.out.println(line);
				}
				// String[] data = row.split(",");
				String[] data = line.split("\\|");

				for (int i = 0; i < data.length ; i++) {
//					if(data[i].equalsIgnoreCase("1")) {
//						continue;
//					}
					ps.setString(sr_no, data[i].replaceAll("^\"|\"$", "").replaceAll("-", ""));
					System.out.println("" + sr_no + " , " + data[i]);
					sr_no++;
				}
				ps.setString(sr_no++, fileDate);
//				ps.setString(sr_no++, createdBy);
				ps.setString(sr_no++, cycle);

				ps.addBatch();

				if (Number == 1000) {
					System.out.print("Executed batch");
					ps.executeBatch();
				}

			}
			ps.executeBatch();

			con.commit();
			res = "success";
			csvReader1.close();

			output.put("result", true);
			output.put("count", totalCount);

		} catch (Exception e) {			
			e.printStackTrace();

			
			res = "fail";
			output.put("result", false);
			output.put("count", totalCount);
			System.out.println("issue at Line number " + totalCount);
			System.out.println("Line issue " + line);
			System.out.println("Exception in reading adjustment is " + e);
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public HashMap<String, Object> rupayIntPresentFileUpload(String fileDate, String createdBy, String cycle,
			String network, MultipartFile file, String subcategory) {

		HashMap<String, Object> output = new HashMap<String, Object>();
		int totalCount = 0;
		String res = "";
		String row = "";
		String line = "";
		try {
			BufferedReader csvReader1 = new BufferedReader(new InputStreamReader(file.getInputStream()));
			Connection con = getConnection();
			String tableName = "";

			if (network.equalsIgnoreCase("RUPAY")) {
				if (subcategory.equalsIgnoreCase("INTERNATIONAL"))
					tableName = "RUPAY_INTERNATIONAL_PRESENTMENT";
			}

			String sql = "insert into " + tableName
					+ "(Report_Date, Presentment_Raise_Date, Presentment_Settlement_Date, Case_Number, Function_Code, Scheme_Name, Transaction_Flag, Primary_Account_Number, Date_Local_Transaction, Transaction_Settlement_Date, Acquirer_Reference_Data, Processing_Code, Currency_Code_txn, ECommerce_Indicator, Amount_Transaction, Amount_Additional, Currency_Code, Settlement_Amount, Settlement_Amount_Additional, Settlement_Amount_Presentment, Approval_Code, Originator_Point, POS_Entry_Mode, POS_Condition_Code, Acquirer_Institution_ID_code, Transaction_Originator_Institution_ID_code, Acquirer_Name_Country, Issuer_Institution_ID_code, Transaction_Destination_code, Issuer_Name_Country, Card_Acceptor_Terminal_ID, Card_Acceptor_Name, Card_Acceptor_Location, Card_Acceptor_Country_Code, Card_Acceptor_Business_Code, Card_Acceptor_ID_Code, Card_Acceptor_State_Name, Card_Acceptor_City, Days_Aged, MTI, filedate, createdby, cycle) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_dATE(?,'DD/MM/YYYY'), ?, ?)";

			// ,DISPUTE_RAISE_DATE,DISPUTE_RAISED_SETTL_DATE,CASE_NUMBER,FUNCTION_CODE,FUNCTION_CODE_DESCRIPTION,PRIMARY_ACCOUNT_NUMBER,PROCESSING_CODE
			// ,TO_DATE(?,'DD-MMM-YYYY'),TO_DATE(?,'DD-MMM-YYYY'),?,?,?,?,?
			PreparedStatement ps = con.prepareStatement(sql);
			int count = 1;
			int Number = 0;
			con.setAutoCommit(false);
			while ((row = csvReader1.readLine()) != null) {
				int sr_no = 1;
				Number++;
				totalCount++;
				if (row.contains("---END OF REPORT---") || row.contains("---End of Report---")) {
					break;
				}
				if (count == 1) {
					count++;
					continue;
				}

				if (subcategory.equalsIgnoreCase("INTERNATIONAL")) {
					line = row.replaceAll(",", "|");
				} else {
					line = row.replaceAll("\",\"", "|");
					line = line.replace("\",", "|");
					line = line.replace("\"", "");
					// System.out.println(line);
				}
				// String[] data = row.split(",");
				String[] data = line.split("\\|");

				for (int i = 0; i < data.length; i++) {
					ps.setString(sr_no, data[i].replaceAll("^\"|\"$", "").replaceAll("-", ""));
					sr_no++;
				}

				ps.setString(sr_no++, fileDate);
				ps.setString(sr_no++, createdBy);
				ps.setString(sr_no++, cycle);

				ps.addBatch();

				if (Number == 1000) {
					System.out.print("Executed batch");
					ps.executeBatch();
				}

			}
			ps.executeBatch();

			con.commit();
			res = "success";
			csvReader1.close();

			output.put("result", true);
			output.put("count", totalCount);

		} catch (Exception e) {
			res = "fail";
			output.put("result", false);
			output.put("count", totalCount);
			System.out.println("issue at Line number " + totalCount);
			System.out.println("Line issue " + line);
			System.out.println("Exception in reading adjustment is " + e);
			e.printStackTrace();
		}
		return output;
	}

	/******************* ADJUSTMENT TTUM **************************/
	public HashMap<String, Object> validateAdjustmentTTUM(String fileDate, String adjType) {
		HashMap<String, Object> output = new HashMap<String, Object>();
		String fdate = genetalUtil.DateFunction(fileDate);
		System.out.println("fdate is"+ fdate);
		int adjTTUMCount = 0;
		try {
			System.out.println("inside the 1st validation");
			// 1. Dispute file is uploaded or not
			String checkUpload = "select count(cycle) from(select distinct cycle from rupay_network_adjustment where filedate = ?) ";
			int uploadedCount = getJdbcTemplate().queryForObject(checkUpload, new Object[] { fdate }, Integer.class);

			String rawUpload = "SELECT COUNT(1) FROM RUPAY_RUPAY_RAWDATA_nainital WHERE FILEDATE = ? ";
			int rawCount = getJdbcTemplate().queryForObject(rawUpload, new Object[] { fdate }, Integer.class);

			if (uploadedCount > 0 && rawCount > 0) {
				// 2. Adjustment TTUM is already processed
				if (!adjType.equalsIgnoreCase("FEE")) {
					String checKAdjTTUM = "select count(1) from rupay_adjustment_Ttum where filedate = ? and adjtype != 'FEE'";
					adjTTUMCount = getJdbcTemplate().queryForObject(checKAdjTTUM, new Object[] { fdate },
							Integer.class);
				} else {
					String checKAdjTTUM = "select count(1) from rupay_adjustment_Ttum where filedate = ? and adjtype = ?";
					adjTTUMCount = getJdbcTemplate().queryForObject(checKAdjTTUM, new Object[] { fdate, adjType },
							Integer.class);
				}

				if (adjTTUMCount > 0) {
					output.put("result", false);
					output.put("msg", "TTUM is already processed");
				} else {
					// check whether data is present for ttum generation
					String checkData = "select count(1) from rupay_network_adjustment where filedate = to_date(?,'dd/mm/yyyy') ";
					/*
					 * + " and function_code_description not like '%262-Refund%'" +
					 * "    and DIRECTION_IW_OW is not null and upper(direction_iw_ow) = 'INWARD'" +
					 * "    and function_code_description like '%Acceptance%'";
					 */

					int getData = getJdbcTemplate().queryForObject(checkData, new Object[] { fileDate }, Integer.class);

					if (getData > 0)
						output.put("result", true);
					else {
						output.put("result", false);
						output.put("msg", "Data is not present for TTUM generation");
					}

				}

			} else {
				if (rawCount == 0) {
					output.put("result", false);
					output.put("msg", "Raw files are not uploaded");
				} else {
					output.put("result", false);
					output.put("msg", "All disputes files are not uploaded");
				}
			}
		} catch (Exception e) {
			output.put("result", false);
			output.put("msg", "Exception Occurred While checking");
			System.out.println("Exception is " + e);
		}
		return output;
	}

	public HashMap<String, Object> validateAdjustmentTTUMProcess(String fileDate, String adjType) {
		HashMap<String, Object> output = new HashMap<String, Object>();
		int adjTTUMCount = 0;

		String fdate = genetalUtil.DateFunction(fileDate);

		try {
			// 2. Adjustment TTUM is already processed
			if (!adjType.equalsIgnoreCase("FEE")) {
				// String checKAdjTTUM = "select count(1) from rupay_adjustment_Ttum where
				// filedate = to_date(?,'dd/mm/yyyy') and adjtype != 'FEE'";
				String checKAdjTTUM = "select count(1) from rupay_adjustment_Ttum where filedate = ? and adjtype != 'FEE'";

				adjTTUMCount = getJdbcTemplate().queryForObject(checKAdjTTUM, new Object[] { fdate }, Integer.class);
				System.out.println("query of processed check is" + checKAdjTTUM);
			} else {
				String checKAdjTTUM = "select count(1) from rupay_adjustment_Ttum where filedate = to_date(?,'dd/mm/yyyy') and adjtype = ?";
				adjTTUMCount = getJdbcTemplate().queryForObject(checKAdjTTUM, new Object[] { fdate, adjType },
						Integer.class);
			}

			if (adjTTUMCount > 0) {
				output.put("result", true);

			} else {
				output.put("result", false);
				output.put("msg", "TTUM is not processed");

			}

		} catch (Exception e) {
			output.put("result", false);
			output.put("msg", "Exception Occurred While checking");
			System.out.println("Exception is " + e);
		}
		return output;
	}

	@Override
	public boolean runAdjTTUM(String fileDate, String adjType, String createdBy) {
		Map<String, Object> inParams = new HashMap<>();
		Map<String, Object> outParams2 = new HashMap<String, Object>();
		String passdate = genetalUtil.DateFunction(fileDate);
		System.out.println("filedate is"+ fileDate);
		System.out.println("date is passsing is " + passdate);
		System.out.println("data is saved");

		try {

			// run adj ttum
			AdjTTUMProc exe = new AdjTTUMProc(getJdbcTemplate());
			inParams.put("FILEDT", passdate);
			inParams.put("USER_ID", createdBy);
			inParams.put("ADJTYPE", adjType);
			inParams.put("SUBCATE", "DOMESTIC");
			outParams2 = exe.execute(inParams);
			if (outParams2 != null && outParams2.get("msg") != null) {
				logger.info("OUT PARAM IS " + outParams2.get("msg"));
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			logger.info("Exception is " + e);
			return false;
		}
	}

	// ADJ ttum detailed
	private class AdjTTUMProc extends StoredProcedure {
		private static final String insert_proc = "RUPAY_ADJ_TTUM_NEW";

		public AdjTTUMProc(JdbcTemplate jdbcTemplate) {
			super(jdbcTemplate, insert_proc);
			setFunction(false);
			declareParameter(new SqlParameter("FILEDT", Types.VARCHAR));
			declareParameter(new SqlParameter("USER_ID", Types.VARCHAR));
			// declareParameter(new SqlParameter("ENTERED_CYCLE",Types.INTEGER));
			declareParameter(new SqlParameter("ADJTYPE", Types.VARCHAR));
			declareParameter(new SqlParameter("SUBCATE", Types.VARCHAR));
			declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
			compile();
		}

	}

	@Override
	public List<Object> getAdjTTUM(String fileDate, String adjType) {
//		DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//		String mainDate = "";
//		System.out.println("1. "+fileDate);
//		try {
//			Date date = sdf.parse(fileDate);
//			System.out.println("2. "+date.toString());
//			sdf = new SimpleDateFormat("dd/MM/yy");
//			mainDate = sdf.format(date);
//			System.out.println("3. "+mainDate);
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//		}

		System.out.println("DATE IS COMING IS"+fileDate);
		String[] dateSplit = fileDate.split("/");
		String MMM = "";
		switch (dateSplit[1]) {
		case "01":
			MMM = "JAN";
			break;
		case "02":
			MMM = "FEB";
			break;
		case "03":
			MMM = "MAR";
			break;
		case "04":
			MMM = "APR";
			break;
		case "05":
			MMM = "MAY";
			break;
		case "06":
			MMM = "JUN";
			break;
		case "07":
			MMM = "JUL";
			break;
		case "08":
			MMM = "AUG";
			break;
		case "09":
			MMM = "SEP";
			break;
		case "10":
			MMM = "OCT";
			break;
		case "11":
			MMM = "NOV";
			break;
		case "12":
			MMM = "DEC";
			break;

		}
		String mainDate = dateSplit[0] + "-" + MMM + "-" + dateSplit[2].substring(2);

		System.out.println("NEW DATE WE ARE PASSING IS : " + mainDate);

		String passdate = genetalUtil.DateFunction(fileDate);
		//String ddate [] = fileDate.split("/");
 		String mdate = fileDate.replace("/", "-");
 		String ddate [] = mdate.split("-");
		String sdate  = ddate[0] + "-" + ddate[1] + "-" + ddate[2].substring(2);
		
		String ydate [] = passdate.split("-");
		String zdate  = ydate[0] + "-" + ydate[1] + "-" + ydate[2].substring(2);

		System.out.println("new date is"+ sdate);

		List<Object> data = new ArrayList<Object>();
		try {
			String getData1 = null;// ,getData2 = null;
			List<Object> DailyData = new ArrayList<Object>();

			if (adjType.equals("FEE")) {
				/*
				 * getData1 =
				 * "SELECT RPAD(ACCOUNT_NUMBER,14,' ') AS ACCOUNT_NUMBER,PART_TRAN_TYPE,"
				 * +"LPAD(TRANSACTION_AMOUNT,17,' ') as TRANSACTION_AMOUNT,"
				 * +"rpad(TRANSACTION_PARTICULAR,30,' ') as TRANSACTION_PARTICULAR," +
				 * "LPAD(NVL(REFERENCE_NUMBER,' '),16,' ') AS REMARKS"
				 * +",to_char(TO_DATE(FILEDATE,'DD/MON/YYYY'),'DD/MM/YYYY') AS FILEDATE,ADJTYPE"
				 * +
				 * " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = TO_DATE(?,'DD/MM/YYYY') and SUBCATEGORY = 'DOMESTIC' "
				 * + "AND upper(adjtype) like '%"+adjType+"%'";
				 */

				getData1 = "SELECT ACCOUNT_NUMBER AS ACCOUNT_NUMBER, ACCOUNT_REPORT_CODE , PART_TRAN_TYPE,"
						+ "REFERENCE_AMOUNT as TRANSACTION_AMOUNT,"
						+ "TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,REFERENCE_NUMBER AS REMARKS"
						+ ",TO_DATE(FILEDATE,'DD/MM/YYYY') AS FILEDATE"
						+ " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = TO_DATE(?,'dd-mm-yyyy')";

				DailyData = getJdbcTemplate().query(getData1, new Object[] { fileDate },
						new ResultSetExtractor<List<Object>>() {
							public List<Object> extractData(ResultSet rs) throws SQLException {
								List<Object> beanList = new ArrayList<Object>();

								while (rs.next()) {
									logger.info("Inside rset");

									Map<String, String> table_Data = new HashMap<String, String>();
									table_Data.put("ACCOUNT_NUMBER", rs.getString("ACCOUNT_NUMBER"));
									table_Data.put("INR", "INR");
									table_Data.put("ACCOUNT_REPORT_CODE", rs.getString("ACCOUNT_REPORT_CODE"));
									table_Data.put("PART_TRAN_TYPE", rs.getString("PART_TRAN_TYPE"));
									table_Data.put("TRANSACTION_AMOUNT", rs.getString("TRANSACTION_AMOUNT"));
									table_Data.put("TRANSACTION_PARTICULAR", rs.getString("TRANSACTION_PARTICULAR"));
									table_Data.put("REMARKS", rs.getString("REMARKS"));
									table_Data.put("FILEDATE", rs.getString("FILEDATE"));
									// table_Data.put("ADJTYPE", rs.getString("ADJTYPE"));

									beanList.add(table_Data);
								}
								return beanList;
							}
						});
			} else {
				/*
				 * getData1 = "SELECT ACCOUNT_NUMBER AS ACCOUNT_NUMBER,PART_TRAN_TYPE,"
				 * +"TRANSACTION_AMOUNT as TRANSACTION_AMOUNT,"
				 * +"TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,REFERENCE_NUMBER AS REMARKS"
				 * +",to_char(TO_DATE(FILEDATE,'DD/MON/YYYY'),'DD/MM/YYYY') AS FILEDATE,ADJTYPE"
				 * +
				 * " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = TO_DATE(?,'dd-mm-yyyy') and SUBCATEGORY = 'DOMESTIC' "
				 * //+ "AND (ADJTYPE) = ? " + "AND adjtype not like '%Penalty%'";
				 */

				/*
				 * getData1 =
				 * "SELECT ACCOUNT_NUMBER AS ACCOUNT_NUMBER, ACCOUNT_REPORT_CODE , PART_TRAN_TYPE,"
				 * + "REFERENCE_AMOUNT as TRANSACTION_AMOUNT," +
				 * "TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,REFERENCE_NUMBER AS REMARKS"
				 * + ",TO_DATE(FILEDATE,'DD-MM-RRRR') AS FILEDATE" +
				 * " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = '" + mainDate + "'";
				 */

				getData1 = "SELECT LPAD(ACCOUNT_NUMBER, 16,' ') AS ACCOUNT_NUMBER ,PART_TRAN_TYPE ,SUBSTR(account_number ,0,3) AS ACCOUNT_REPORT_CODE,TO_CHAR(LPAD(REFERENCE_AMOUNT,16,' '),'999999.99') as TRANSACTION_AMOUNT,"
						+ " TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,LPAD(NVL(REFERENCE_NUMBER,' '),12,' ') AS REMARKS , "
						+ "  CYCLE AS FILEDATE " + " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = '" + zdate +"' "
						+ " order by PART_TRAN_TYPE desc ";
				
				
				
//				getData1 = "SELECT LPAD(ACCOUNT_NUMBER, 16,' ') AS ACCOUNT_NUMBER ,PART_TRAN_TYPE ,SUBSTR(account_number ,0,3) AS ACCOUNT_REPORT_CODE,LPAD(REFERENCE_AMOUNT,16,' ') as TRANSACTION_AMOUNT,"
//						+ " TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,LPAD(NVL(REFERENCE_NUMBER,' '),12,' ') AS REMARKS , "
//						+ "  CYCLE AS FILEDATE " + " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE =  to_char(to_date('" + sdate +"','dd-mm-yy'),'dd-mm-yy') "
//						+ " order by PART_TRAN_TYPE desc ";
				
//				getData1 = "SELECT LPAD(ACCOUNT_NUMBER, 16,' ') AS ACCOUNT_NUMBER ,PART_TRAN_TYPE ,SUBSTR(account_number ,0,3) AS ACCOUNT_REPORT_CODE,LPAD(REFERENCE_AMOUNT,16,' ') as TRANSACTION_AMOUNT,"
//						+ " TRANSACTION_PARTICULAR as TRANSACTION_PARTICULAR,LPAD(NVL(REFERENCE_NUMBER,' '),12,' ') AS REMARKS , "
//						+ "  CYCLE AS FILEDATE " + " FROM RUPAY_ADJUSTMENT_TTUM WHERE FILEDATE = to_date('" + fileDate +"','dd-mm-yy') "
//						+ " order by PART_TRAN_TYPE desc ";
				
				

				System.out.println("query is : " + getData1);

				// + " and SUBCATEGORY = 'DOMESTIC' "
				// + "AND (ADJTYPE) = ? "
				// + "AND adjtype not like '%Penalty%'";

				DailyData = getJdbcTemplate().query(getData1, new Object[] {}, new ResultSetExtractor<List<Object>>() {
					public List<Object> extractData(ResultSet rs) throws SQLException {
						List<Object> beanList = new ArrayList<Object>();

						while (rs.next()) {
							logger.info("Inside rset");

							Map<String, String> table_Data = new HashMap<String, String>();
							table_Data.put("ACCOUNT_NUMBER", rs.getString("ACCOUNT_NUMBER"));
							table_Data.put("INR", "INR");
							table_Data.put("ACCOUNT_REPORT_CODE", rs.getString("ACCOUNT_REPORT_CODE"));
							table_Data.put("PART_TRAN_TYPE", rs.getString("PART_TRAN_TYPE"));
							table_Data.put("TRANSACTION_AMOUNT", rs.getString("TRANSACTION_AMOUNT"));
							table_Data.put("TRANSACTION_PARTICULAR", rs.getString("TRANSACTION_PARTICULAR"));
							table_Data.put("REMARKS", rs.getString("REMARKS"));
							table_Data.put("FILEDATE", rs.getString("FILEDATE"));
							// table_Data.put("ADJTYPE", rs.getString("ADJTYPE"));

							beanList.add(table_Data);
						}
						return beanList;
					}
				});
			}

			data.add(DailyData);

			// ADDING REPORT 2 DATA

			return data;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in getInterchangeData " + e);
			return null;

		}

	}

	@Override
	public boolean rupayAdjustmentFileRollback(String fileDate, String cycle, String network, String subcategory) {
		try {
			String deleteQuery = "delete from rupay_network_adjustment where filedate  = to_date('" + fileDate
					+ "','dd/mm/yyyy') and cycle = '" + cycle + "'";
			getJdbcTemplate().execute(deleteQuery);

			return true;

		} catch (Exception e) {
			logger.info("Exception while rolling back adjustment file " + e);
			return false;
		}

	}

}
