package com.recon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Logger;
import com.recon.auto.Switch_POS;
import com.recon.model.CompareSetupBean;
import com.recon.model.FileSourceBean;

public class ReadMGBGLFiles {

	String partid;
	private int Part_id;
	private static final String O_ERROR_MESSAGE = "o_error_message";

	public static void main(String[] args) {

		ReadMGBGLFiles readcbs = new ReadMGBGLFiles();

		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter file path: ");
		System.out.flush();
		String filename = scanner.nextLine();
		File file = new File(filename);
		// Subcategory IS NOT NEEDED IN CASE OF AS SINGLE FILE IS USED FOR BOTH
		/*
		 * System.out.println("Enter Sub Category "); System.out.flush(); String
		 * stSubCategory = scanner.nextLine();
		 */

		// System.out.println(file.getName());

		/*
		 * File f = new File("\\\\10.143.11.50\\led\\DCRS\\AMEXCBS"); if(!(f.exists()))
		 * {
		 * 
		 * if(f.mkdir()) {
		 * 
		 * System.out.println("directory created"); } }
		 */

		/*
		 * if(file.renameTo(new File("\\\\10.143.11.50\\led\\DCRS\\AMEXCBS\\" +
		 * file.getName()))) {
		 * 
		 * System.out.println("File Moved Successfully");
		 * 
		 * readcbs.uploadCBSData(file.getName());
		 * 
		 * System.out.println("Process Completed");
		 * 
		 * }else {
		 * 
		 * System.out.println("Error Occured while moving file"); }
		 */

		/*
		 * if(readcbs.uploadCBSData(file.getName(),file.getPath())) {
		 * System.out.println("File uploaded successfully"); } else
		 * System.out.println("File uploading failed");
		 */

	}

	public HashMap<String, Object> uploadCBSData(CompareSetupBean setupBean, Connection connection, MultipartFile file,
			FileSourceBean sourceBean) {

		HashMap<String, Object> output = new HashMap<String, Object>();
		// commented for UCO by INT8624
		/*
		 * if(setupBean.getFileType().equals("MAN")){
		 * 
		 * //flag="MANUPLOAD_FLAG"; Part_id=2; //man_flag="Y";
		 * 
		 * }else{
		 * 
		 * //flag="UPLOAD_FLAG"; //upload_flag="Y"; Part_id=1; }
		 */

		try {
			boolean uploaded = false;

			// modified by INT8624 FOR UCO
			System.out.println("Entered CBS File IS " + file.getOriginalFilename());
//		System.out.println(file.isEmpty());
			String charPos = file.getOriginalFilename();
			int indexOf = charPos.indexOf(".");
			String exten = charPos.substring(indexOf + 1);
			System.out.println("Extension of cbs file is " + exten);

//			System.out.println("Reached till here");
//		BufferedReader br;
//		List<String> result = new ArrayList<>();
//		try {
//
//		     String line;
//		     InputStream is = file.getInputStream();
//		     br = new BufferedReader(new InputStreamReader(is));
//		     while ((line = br.readLine()) != null) {
//		          result.add(line);
//		     }
//
//		  } catch (IOException e) {
//		    System.err.println(e.getMessage());       
//		  }
//		System.out.println("data inside file");
//		System.out.println(result);
//			System.out.println("Now error");
//		
//		System.exit(0);

			if (exten.equalsIgnoreCase("prt")) {
				return uploadGLData(setupBean, connection, file, sourceBean);

			} else {
				return uploadMGBCBSData(setupBean, connection, file, sourceBean);
			}
			// return uploadISSData(setupBean, connection, file, sourceBean,
			// file.getOriginalFilename());

		} catch (Exception e) {

			System.out.println("Error Occured");
			e.printStackTrace();
			output.put("result", false);
			output.put("msg", "Exception Occured");
			return output;
		}

	}

	public boolean uploadONUSData(CompareSetupBean setupBean, Connection con, MultipartFile file,
			FileSourceBean sourceBean) {

		int flag = 1, batch = 0;

		InputStream fis = null;
		boolean readdata = false;

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			System.out.println("Reading data " + new Date().toString());

			String insert = "INSERT INTO CBS_RAWDATA "
					+ "(ACCOUNT_NUMBER,TRANDATE,VALUEDATE,TRAN_ID,TRAN_PARTICULAR,TRAN_RMKS,PART_TRAN_TYPE,TRAN_PARTICULAR1,TRAN_AMT,BALANCE,PSTD_USER_ID,CONTRA_ACCOUNT,ENTRY_DATE,VFD_DATE,REF_NUM,TRAN_PARTICULAR_2,ORG_ACCT,Part_id,FILEDATE,CREATEDDATE)"
					+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,(to_date(?,'dd/mm/yyyy')),sysdate)";

			PreparedStatement ps = con.prepareStatement(insert);

			int insrt = 0;

			while ((thisLine = br.readLine()) != null) {

				String[] splitarray = null;

				if (thisLine.contains("ACCOUNT NUMBER|TRAN DATE|")) {

					readdata = true;

				}
				if (!(thisLine.contains("ACCOUNT NUMBER|TRAN DATE|")) && readdata) {

					int srl = 1;

					ps.setString(15, null);
					ps.setString(16, null);

					splitarray = thisLine.split(Pattern.quote("|"));// Pattern.quote(ftpBean.getDataSeparator())

					for (int i = 0; i < splitarray.length; i++) {

						String value = splitarray[i];
						if (!value.trim().equalsIgnoreCase("")) {

							// 2 valuedate
							// 4 tran_particular

							/*
							 * System.out.println(splitarray[4]); if(i==2) { value = value +" "+
							 * splitarray[4].substring(19,27); ps.setString(srl,value.trim());
							 * 
							 * } else {
							 * 
							 * ps.setString(srl,value.trim()); }
							 */

							ps.setString(srl, value.trim());

							++srl;
						} else {

							ps.setString(srl, null);
							// System.out.println(srl+"null");
							++srl;
						}

					}
					/**** comment 15 and 16 for online file ****/

					ps.setString(17, null);
					ps.setInt(18, Part_id);
					ps.setString(19, setupBean.getFileDate());

					// System.out.println(insert);

					ps.addBatch();
					flag++;

					if (flag == 20000) {
						flag = 1;

						ps.executeBatch();
						System.out.println("Executed batch is " + batch);
						batch++;
					}

				}

			}
			ps.executeBatch();
			br.close();
			ps.close();
			System.out.println("Reading data " + new Date().toString());
			return true;

		} catch (Exception ex) {

			ex.printStackTrace();

			System.out.println("Exception" + ex);
			return false;
		}
	}

	public boolean uploadACQData(CompareSetupBean setupBean, Connection con, MultipartFile file,
			FileSourceBean sourceBean) {

		int flag = 1, batch = 0;

		InputStream fis = null;
		boolean readdata = false;

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			System.out.println("Reading data " + new Date().toString());

			String insert = "INSERT INTO CBS_AMEX_RAWDATA "
					+ "(FORACID,TRAN_DATE,E,AMOUNT,BALANCE,TRAN_ID,VALUE_DATE,REMARKS,REF_NO,PARTICULARALS,CONTRA_ACCOUNT,pstd_user_id ,ENTRY_DATE,VFD_DATE,PARTICULARALS2,Part_id,FILEDATE)"
					+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,(to_date(?,'dd/mm/yyyy')))";

			PreparedStatement ps = con.prepareStatement(insert);

			int insrt = 0;

			while ((thisLine = br.readLine()) != null) {

				String[] splitarray = null;

				if (thisLine.contains("------")) {

					readdata = true;

				}
				if (!(thisLine.contains("-----")) && readdata) {

					int srl = 1;
					// System.out.println(thisLine);

					splitarray = thisLine.split(Pattern.quote("|"));// Pattern.quote(ftpBean.getDataSeparator())

					for (int i = 0; i < splitarray.length; i++) {

						String value = splitarray[i];
						if (!value.equalsIgnoreCase("")) {

							ps.setString(srl, value.trim());

							++srl;
						} else {

							ps.setString(srl, null);
							// System.out.println(srl+"null");
							++srl;
						}

					}
					/**** comment 15 and 16 for online file ****/

					ps.setInt(16, Part_id);
					ps.setString(17, setupBean.getFileDate());

					ps.addBatch();
					flag++;

					if (flag == 20000) {
						flag = 1;

						ps.executeBatch();
						System.out.println("Executed batch is " + batch);
						batch++;
					}

					// insrt = ps.executeUpdate();

				}

			}
			ps.executeBatch();
			br.close();
			ps.close();
			System.out.println("Reading data " + new Date().toString());
			return true;

		} catch (Exception ex) {

			System.out.println("error occurred");
			ex.printStackTrace();
			return false;

		}

	}

	public HashMap<String, Object> uploadGLData(CompareSetupBean setupBean, Connection con, MultipartFile file,
			FileSourceBean sourceBean) {

		HashMap<String, Object> output = new HashMap<String, Object>();
		String stLine = null;
		int lineNumber = 0, sr_no= 1 , reading_line = 5, batchNumber = 0, start_pos = 0, batchSize = 0;
		boolean batchExecuted = false;
		String action = null;
		Switch_POS reading = new Switch_POS();
		boolean posFile = false;
		List<String> elements = reading.readMGBAtmCBS();
		String Glnumber;

//	if(file.getOriginalFilename().contains("040000000000004133139"))
//	{
//		elements = reading.readMGBPosCBS();
//		posFile = true;
//	}

		String InsertQuery = "insert into cbs_mgb_rawdata1(branch, term, userid, txn_code, post_date, trace, amount, action, balance, card_numb, rrn, atm_id, file_type"
				+ " , createdby, filedate , contra_account ) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'dd/mm/yyyy'),?)";

		// String delQuery = "delete from cbs_dhana_rawdata_temp";
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			PreparedStatement ps = con.prepareStatement(InsertQuery);
			while ((stLine = br.readLine()) != null) {

				if (stLine.contains("BGL No")) {

					System.out.println("GL NUMBER : " + stLine.substring(9, 26));
					Glnumber = stLine.substring(9, 26);
					setupBean.setCONTRA_ACCOUNT(Glnumber);

				}

				testing: {
					lineNumber++;
					batchExecuted = false;
					sr_no = 1;

					if (lineNumber >= reading_line) {
						if (stLine.contains("----------------------------")) {
							break;
						} else {
							start_pos = 0;
							for (int i = 0; i < elements.size(); i++) {

								String[] data = elements.get(i).split("\\|");

								if (i == 0) {
									String branch = stLine.substring(Integer.parseInt(data[1]) - 1,
											Integer.parseInt(data[2]));

									if ("4000".equals(branch.trim())) {
										break testing;
									}

								}

								if (i == 1) {
									String term = stLine.substring(Integer.parseInt(data[1]) - 1,
											Integer.parseInt(data[2]));

									if ("2".equals(term.trim())) {
										break testing;
									}
								}

								if (data.length == 2) {
									// last parameter
									start_pos = Integer.parseInt(data[1]);
									String narration = stLine.substring(start_pos).trim();
									if (action != null && posFile) {
										if (action.equalsIgnoreCase("C")) {
											ps.setString(sr_no++, narration.substring(0, 18).trim());
											ps.setString(sr_no++, narration.substring(19, 33).trim());
											ps.setString(sr_no++, narration.substring(32).trim());
											ps.setString(sr_no++, "POS");
										} else {
											if (narration.contains("SETTLEMENT")) {
												ps.setString(sr_no++, "SETTLEMENT"); // cardnumber
												ps.setString(sr_no++, "");
												ps.setString(sr_no++, "");
												ps.setString(sr_no++, "POS");
											} else {
												ps.setString(sr_no++, ""); // cardnumber
												ps.setString(sr_no++,
														(narration.substring(44) + narration.substring(0, 6)).trim());
												ps.setString(sr_no++, narration.substring(28, 37).trim());
												ps.setString(sr_no++, "POS");
											}
										}
									} else {
										ps.setString(sr_no++, narration);
										ps.setString(sr_no++, "ATM");

									}

								} else {
									start_pos = Integer.parseInt(data[1]);
									ps.setString(sr_no++,
											stLine.substring((start_pos - 1), (Integer.parseInt(data[2]))).trim());

									if (data[0].equalsIgnoreCase("ACTION")) {
										action = stLine.substring((start_pos - 1), (Integer.parseInt(data[2]))).trim();
									}

								}
							}
							// System.out.println(sr_no);
							ps.setString(sr_no++, setupBean.getCreatedBy());
							ps.setString(sr_no++, setupBean.getFileDate());
							ps.setString(sr_no++, setupBean.getCONTRA_ACCOUNT());

							ps.addBatch();
							batchSize++;

							if (batchSize == 10000) {
								batchNumber++;
								System.out.println("Batch Executed is " + batchNumber);
								ps.executeBatch();
								batchSize = 0;
								batchExecuted = true;
							}
						}
					}
					if (!batchExecuted) {
						batchNumber++;
						System.out.println("Batch Executed is " + batchNumber);
						ps.executeBatch();
					}
//					br.close();
					ps.close();
					System.out.println("Reading data " + new Date().toString());
					output.put("result", true);
					output.put("msg", "Records Count is " + lineNumber);
					return output;
				}
			}
		} catch (Exception e) {
			System.out.println("Issue at line " + stLine);
			System.out.println("Exception in uploadISSData " + e);
			output.put("result", false);
			output.put("msg", "Exception Occured");
			return output;
		}
		return output;	
	}

//CBS FAIL and Success file reading
	@SuppressWarnings({ "deprecation", "resource", "unused" })
	public HashMap<String, Object> uploadMGBCBSData(CompareSetupBean setupBean, Connection con, MultipartFile file,
			FileSourceBean sourceBean) {

		HashMap<String, Object> output = new HashMap<String, Object>();
		int stLine = 0;
		int lineNumber = 0, reading_line = 6, batchNumber = 0, start_pos = 0, batchSize = 0;
		int lastRow;
		boolean batchExecuted = false;
		String action = null;
		Switch_POS reading = new Switch_POS();
		boolean failedFile = false, posFile = false;
		List<String> elements = reading.readMGBSuccessCBS();

		String InsertQuery = "insert into cbs_nainital_rawdata(tran_date, tran_time, accountid, narration, txn_type, rrn, txn_indctr, amount, branch_code, account_name, customer_id, unq_txn_id, from_account, to_account, filedate) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'dd/mm/yyyy'))";

//	if(file.getOriginalFilename().contains("FAIL")) {
//		elements = reading.readMGBFailedCBS();
//		InsertQuery = "insert into cbs_mgb_rawdata2(stan_no, rrn, station_id, tran_time, tran_date, action, banc_jou, cust_no, card_no, tran_amount, corr_jou, createdby, filedate, file_type, file_status) "+
//				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'dd/mm/yyyy'), ?, ?)";
//		failedFile = true;
//	}

		if (file.getOriginalFilename().contains("POS")) {
			posFile = true;
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			PreparedStatement ps = con.prepareStatement(InsertQuery);
			con.setAutoCommit(false);
			/*
			 * OUTER : while((stLine = br.readLine()) != null) { lineNumber++; batchExecuted
			 * = false; sr_no = 1 ;
			 * 
			 * if(lineNumber >= reading_line) { if(stLine.contains("----") || stLine.
			 * contains("STAN_N RR_NO        STATION_ID       TXN_TI TERMDT    TRAN_C BANCS_JOU CUSTOMER_NO       CARD_NO             TRAN_AMOUNT"
			 * )) { continue OUTER; } else if(stLine.trim().equals("")) { continue OUTER; }
			 * else { start_pos = 0; for(int i = 0; i < elements.size(); i++) { String[]
			 * data = elements.get(i).split("\\|"); // \\/ if(data.length == 2) { //last
			 * parameter start_pos = Integer.parseInt(data[1]); // ps.setString(sr_no++,
			 * stLine.substring((start_pos)).trim());
			 * 
			 * 
			 * } else { start_pos = Integer.parseInt(data[1]); // ps.setString(sr_no++,
			 * stLine.substring((start_pos),(Integer.parseInt(data[2]))).trim()); } }
			 * //System.out.println(sr_no); // ps.setString(sr_no++,
			 * setupBean.getCreatedBy()); // ps.setString(sr_no++, setupBean.getFileDate());
			 * System.out.println("reading start_pos"); System.out.println(start_pos);
			 * System.out.println("ended forcefully");
			 * 
			 * if(posFile) { // ps.setString(sr_no++, "POS"); } else { //
			 * ps.setString(sr_no++, "ATM"); }
			 * 
			 * if(failedFile) { // ps.setString(sr_no++, "FAILED"); } else { //
			 * ps.setString(sr_no++, "SUCCESS"); }
			 * 
			 * // ps.addBatch(); batchSize++;
			 * 
			 * if(batchSize == 10000) { batchNumber++;
			 * System.out.println("Batch Executed is "+batchNumber); // ps.executeBatch();
			 * batchSize = 0; batchExecuted = true; } } }
			 * 
			 * }
			 */

			Workbook wb = null;
			int cellIdx;
			int sr_no = 1;
			Sheet sheet = null;
			FormulaEvaluator formulaEvaluate = null;
			int extn = file.getOriginalFilename().indexOf(".");
			wb = new XSSFWorkbook(file.getInputStream());
			sheet = wb.getSheetAt(0);
			lastRow = sheet.getLastRowNum();
			lineNumber = lastRow;
//			System.out.println(lastRow);
			for (int rowNumber = 0; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
				if (rowNumber == 0) {
					continue;
				}
				
				stLine++;
				Row currentRow = (Row) sheet.getRow(rowNumber);
				cellIdx = 0;
				sr_no = 1;
				for (cellIdx = 0; cellIdx < currentRow.getLastCellNum(); cellIdx++) {
					try {
						Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						if (currentRow.getCell(cellIdx).getCellType() == Cell.CELL_TYPE_STRING
								|| currentRow.getCell(cellIdx).getCellType() == Cell.CELL_TYPE_NUMERIC
								|| currentRow.getCell(cellIdx).getCellType() == Cell.CELL_TYPE_BLANK) {
							switch (cellIdx) {
							case 0:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;
							case 1:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;
							case 2:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 3:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								String taxationType = currentCell.getStringCellValue();
								String type = taxationType.substring(taxationType.length()-3);
								System.out.println(type);
								ps.setString(sr_no++, type);
								break;

							case 4:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 5:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 6:
								System.out.println(currentCell.getNumericCellValue());
								ps.setString(sr_no++, String.valueOf(currentCell.getNumericCellValue()));
								break;

							case 7:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 8:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 9:
								if(currentCell == null|| currentCell.equals(null) || currentCell.equals("") || currentCell.getStringCellValue().trim() == null || currentCell.getStringCellValue() == "") {//|| currentCell.getStringCellValue().trim() == null || currentCell.getStringCellValue() == ""
									System.out.println(currentCell.getStringCellValue());
									ps.setString(sr_no++, " ");
									break;
								}else if(currentCell.getCellType() == Cell.CELL_TYPE_STRING || currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									System.out.println(currentCell.getStringCellValue());
									ps.setString(sr_no++, currentCell.getStringCellValue());
									break;
								}
								else {
									System.out.println(currentCell.getStringCellValue());
									ps.setString(sr_no++, String.valueOf(currentCell.getNumericCellValue()));
								}
								break;

							case 10:
								System.out.println(currentCell.getStringCellValue().trim());
								ps.setString(sr_no++, currentCell.getStringCellValue().trim());
								break;

							case 11:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								break;

							case 12:
								System.out.println(currentCell.getStringCellValue());
								ps.setString(sr_no++, currentCell.getStringCellValue());
								ps.setString(sr_no++, setupBean.getFileDate());
								System.out.println(setupBean.getFileDate()); 
								ps.addBatch();
								batchSize++;
								break;
							}// switch block	
						} // if block
					} // 2nd try block
					catch (Exception e) {
						System.out.println(e);
					}
				} // 2nd for loop
				if (batchSize == 10000) {
					batchNumber++;
					lastRow = lastRow - batchSize;
					System.out.println("Batch Executed is " + batchNumber);
					ps.executeBatch();
					batchSize = 0;
					batchExecuted = true;
				}
			} // for loop
			if (!batchExecuted || lastRow > 0) {
				batchNumber++;
				ps.executeBatch();
				System.out.println("Batch Executed is " + batchNumber);
			}
			con.commit();
			con.close();
			br.close();
			System.out.println("Reading data " + new Date().toString());
			output.put("result", true);
			output.put("msg", "Records Count is " + lineNumber);
			return output;
		} // 1st try block
		catch (Exception e) {
			System.out.println("Issue at line " + stLine);
			System.out.println("Exception in uploadISSData " + e);
			output.put("result", false);
			output.put("msg", "Exception Occured");
			return output;
		}
	}
}