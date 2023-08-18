package com.recon.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.recon.control.ManualKnockoffController;
import com.recon.model.RupayUploadBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.web.multipart.MultipartFile;

public class ReadRupayDSCRFile extends JdbcDaoSupport {

	private static final Logger logger = Logger.getLogger(ReadRupayDSCRFile.class);
	SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
	SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");

	public HashMap<String, Object> fileupload(RupayUploadBean beanObj, MultipartFile file, Connection con)
			throws SQLException {
		String bank_name = null, sett_bin = null, acq_iss_bin = null, in_out = null, status = null, tran_cycle = null;
		int totalcount = 0;
		HashMap<String, Object> mapObj = new HashMap<String, Object>();
		Workbook wb = null;
		Sheet sheet = null;
		FormulaEvaluator formulaEvaluate = null;
		int extn = file.getOriginalFilename().indexOf(".");
		logger.info("extension is " + extn);
		boolean Idbi_Block = false;
		int read_line = 0;
		boolean reading_line = false;
		boolean total_encounter = false, stop_reading = false, last_line = false;
		int final_line = 0;
		int rowId = 0;
		int lineNumber = 0;
		String CR_DR = null;
		ResultSet rset = null;

		try {
			long start = System.currentTimeMillis();
			System.out.println("start");
			con.setAutoCommit(false);
			// PreparedStatement ps = con.prepareStatement(sql);

			if (file.getOriginalFilename().substring(extn).equalsIgnoreCase(".XLS")) {

				// book = new HSSFWorkbook(file.getInputStream()) ;
				// wb = new HSSFWorkbook(file.getInputStream());
				/* try { */
				wb = new XSSFWorkbook(file.getInputStream());
				/*
				 * } catch (IOException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
				sheet = wb.getSheetAt(0);
				formulaEvaluate = new XSSFFormulaEvaluator((XSSFWorkbook) wb);

			} else if (file.getOriginalFilename().substring(extn).equalsIgnoreCase(".XLSX")) {

				// book = new XSSFWorkbook(file.getInputStream());
				wb = new XSSFWorkbook(file.getInputStream());
				sheet = wb.getSheetAt(0);
				formulaEvaluate = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

// OUTER:   for (Row r : sheet) {
//     totalcount++;
//     if (r.getRowNum() > 0) {
//    	 rowId = (r.getRowNum()+1);
//     	if(read_line == 1)
//     		read_line++;
//     	reading_line = false;
//            int cellCount = 1;
//            if(!stop_reading)
//            {
//         	   for (Cell c : r) {
//         		   switch(formulaEvaluate.evaluateInCell(c).getCellType())
//         		   {
//         		   case Cell.CELL_TYPE_STRING:
//         			   if(!last_line)
//         			   {
//         				   if(c.getStringCellValue().equalsIgnoreCase("total"))
//         				   {
//         					   if(cellCount == 1)
//         					   {
//         						   total_encounter = true;
//         						   cellCount++;
//         						   //logger.info("this is total field");
//         						   //logger.info("Line number is "+rowId);
//         					   }
//         					   else if(total_encounter)
//         					   {
//         						   if(cellCount > 1 && cellCount <5)
//         						   {
//         							   if(cellCount == 4)
//         							   {
//         								   logger.info("got last total at row number "+rowId);
//         								   last_line = true;
//         								   ps.setString(1, "NTB");
//         								   ps.setString(2, "Total");
//         								   ps.setString(3, "Total");
//         								   ps.setString(4, "Total");
//         								   cellCount = 5;
//         							   }
//         							   else
//         							   {
//         								   cellCount++;
//         								   continue;
//         							   }
//         						   }
//         					   }
//         					   else
//         					   {
//
//         						   continue OUTER;
//         					   }
//         				   }
//         				   else
//         				   {
//         					     total_encounter = false;
//         						   cellCount = 1;
//         						   continue OUTER;
//         					   
//         				   }
//         			   }
//         			  else
//					   {
//						  System.out.println("Cell count is "+cellCount+" and Data is "+c.getStringCellValue());
//						   ps.setString(cellCount, c.getStringCellValue());
//						   cellCount++;
//					   }
//         			   
//         			   break;
//         		   case Cell.CELL_TYPE_NUMERIC:
//         			  if(last_line)
//       			   {
//       				   boolean negativeSign = false;
//       				   String digit = c.getNumericCellValue() +"";
//       				   if(digit.contains("-"))
//       					{
//       					   		negativeSign = true;
//       					   		digit = digit.replace("-", ""); //added on 10mar
//       					}
//       				   if(digit.contains("E"))
//       				   {
//       					   digit = c.getNumericCellValue() +"";
//       					   if(digit.contains("-"))
//          						{
//          					   		negativeSign = true;
//          					   		digit = digit.replace("-", ""); //added on 10mar
//          						}
//       					   
//       					   double d = Double.parseDouble(digit);
//       					   BigDecimal bd = new BigDecimal(d);
//       					   /* digit = bd.round(new MathContext(15)).toPlainString();
//                      	   System.out.println(digit);
//       					    */
//       					   String tryDigit = bd+"";
//       					   int indexOfDot = tryDigit.indexOf(".");
//       					   int secondDigit = Integer.parseInt(tryDigit.substring(indexOfDot+1, indexOfDot+2));
//       					   if(secondDigit > 5)
//       					   {
//       						   digit = tryDigit.substring(0,indexOfDot+3);                                        	   
//       					   }
//       					   else
//       					   {
//       						   BigDecimal db = bd.setScale(1, RoundingMode.HALF_UP);
//       						   digit = db.toPlainString();
//       					   }
//       					   
//       					   if(negativeSign)
//       					   {
//       						   digit = "-"+digit;
//       					   }
//
//       				   }
//       				   
//
//       				   
//       				   else if(last_line)
//       				   {
//       					   System.out.println("Cell count is "+cellCount+" and Data is "+digit);
//       					   ps.setString(cellCount, digit);
//       					   cellCount++;
//       					  
//
//       				   }
//       				   
//       				   // logger.info("cell count is "+cellCount+" value "+ digit);
//
//
//       			   } 
//         			   
//         			   break;
//
//
//         		   }
//
//
//
//         	   }
//         	   if(last_line)
//         	   {
//         		   System.out.println("Before inserting data");
//         		   logger.info("Cell count is "+cellCount);
//         		   ps.setString(cellCount++, beanObj.getFileDate());
//         		   ps.setString(cellCount++, beanObj.getCreatedBy());
//         		   ps.setString(cellCount++, beanObj.getCycle());
//         		   ps.execute();
//         		   stop_reading= true;
//         	   }
//         	   logger.info("cellcount is "+cellCount);
//            }
//            
//     }
//     }
//  			//	ps.executeBatch();
//                  con.commit();
//                 con.close();
//                  long end = System.currentTimeMillis();
//                  System.out.println("start and end diff" + (start - end));
//                  System.out.println(" table  insert");
//                 // response = ADDBACIDSTAT();
//
//                  System.out.println("Data Inserted");
//                  mapObj.put("result", true);
//                  mapObj.put("count", totalcount);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//                  OUTER: for (Row r : sheet) {
//      				totalcount++;
//      				if (r.getRowNum() > 0) {
//      					lineNumber++;
//      					reading_line = false;
//      					int cellCount = 1;
//      					boolean bankNameFlag = true;
//      					boolean CrDrFlag = true;
//      					if (!stop_reading) {
//      						for (Cell c : r) {
//      							String cellValue = c.getStringCellValue();
//      							System.out.println("cellValue: " + cellValue);
//      							if(!cellValue.equals("") && cellValue.length()>2) {
//      								if(cellValue.substring(0,3).equals("DCI")) {
//      									final_line=0;
//      									total_encounter=false;
//      									System.out.println("gh");
//      								}
//      							}
//      							
//      							switch (formulaEvaluate.evaluateInCell(c).getCellType()) {
//      							case Cell.CELL_TYPE_STRING:
//      								if (!c.getStringCellValue().trim().equalsIgnoreCase("")) {
//      									if (c.getStringCellValue().contains("NAINITAL BANK")) {
//      										logger.info("cell count is " + cellCount + " Bank Name is  "
//      												+ c.getStringCellValue());
//      										Idbi_Block = true;
//      										bank_name = c.getStringCellValue();
//      										cellCount++;
//      										// continue OUTER;
//      									} else if (Idbi_Block) {
//      										if (c.getStringCellValue().equalsIgnoreCase("TOTAL") || total_encounter) {
////      										if (c.getStringCellValue().equalsIgnoreCase("TOTAL")) {
//      											if (!total_encounter && final_line == 0) {
//      												total_encounter = true;
//      												final_line++;
//      												continue OUTER;
//      											} else if (final_line == 5) {
//      												
//      												logger.info("Final  line starts");
//      												stop_reading = true;
//      												System.out.println("cellCount : " + cellCount);
//      												if(bankNameFlag) {
//      													ps.setString(cellCount++, bank_name);
//      													System.out.println("bank_name: " + bank_name);
//      													bankNameFlag=false;
//      													System.out.println("cellCount: " + cellCount);
//      												}												
//      												
//      												if(CrDrFlag) {
//      													ps.setString(cellCount++, CR_DR);
//      													System.out.println("CR_DR: " + CR_DR);
//      													CrDrFlag=false;
//      													System.out.println("cellCount: " + cellCount);
//      												}
//      												
//      												ps.setString(cellCount++, c.getStringCellValue());
//      												System.out.println("c.getStringCellValue(): " + c.getStringCellValue());
//      												System.out.println("cellCount: " + cellCount);
//      												System.out.println("PS.toString: " + ps.toString());
//
//      											} else {
//      												final_line++;
//      												continue OUTER;
//      											}
//
//      										} else if (lineNumber == 1 && (c.getStringCellValue().equalsIgnoreCase("CR")
//      												|| c.getStringCellValue().equalsIgnoreCase("DR"))) {
//      											CR_DR = c.getStringCellValue();
//      											continue OUTER;
//      										} else if (lineNumber > 1) {
//      											continue OUTER;
//      										}
//      									}
//
//      									break;
//      								}
//      								break;
//      							case Cell.CELL_TYPE_NUMERIC:
//      								String digit = c.getNumericCellValue() + "";
//      								if (digit.contains("E")) {
//      									digit = c.getNumericCellValue() + "";
//      									double d = Double.parseDouble(digit);
//      									BigDecimal bd = new BigDecimal(d);
//      									/*
//      									 * digit = bd.round(new MathContext(15)).toPlainString();
//      									 * System.out.println(digit);
//      									 */
//      									String tryDigit = bd + "";
//      									int indexOfDot = tryDigit.indexOf(".");
//      									int secondDigit = Integer
//      											.parseInt(tryDigit.substring(indexOfDot + 1, indexOfDot + 2));
//      									if (secondDigit > 5) {
//      										digit = tryDigit.substring(0, indexOfDot + 3);
//      									} else {
//      										BigDecimal db = bd.setScale(1, RoundingMode.HALF_UP);
//      										digit = db.toPlainString();
//      									}
//
//      								}
//
//      								if (total_encounter) {
//      									logger.info("cell count is " + cellCount + " Value is  " + digit);
//      									ps.setString(cellCount++, digit);
//      								} else if (lineNumber > 1) {
//      									continue OUTER;
//      								}
//      								break;
//
//      							}
//
//      						}
//      						if (total_encounter) {
//      							System.out.println("Before inserting data");
//      							logger.info("Cell count is " + cellCount);
//      							System.out.println("getCreatedBy: " + beanObj.getCreatedBy());
//      							System.out.println("getFileDate: " + beanObj.getFileDate());
//      							System.out.println("getCycle: " + beanObj.getCycle());
//      							ps.setString(cellCount++, beanObj.getCreatedBy());
//      							ps.setString(cellCount++, beanObj.getFileDate());
//      							ps.setString(cellCount, beanObj.getCycle());
//      							ps.execute();
//      						}
//      						logger.info("cellcount is " + cellCount);
//      					}
//
//      				}
//      			}
//      			// ps.executeBatch();
//      			// con.commit();
//      			con.close();
//      			long end = System.currentTimeMillis();
//      			System.out.println("start and end diff" + (start - end));
//      			System.out.println(" table  insert");
//      			// response = ADDBACIDSTAT();
//
//      			System.out.println("Data Inserted");
//      			mapObj.put("result", true);
//      			mapObj.put("count", totalcount);

		//////////////////////////////////////////////////////////////////////////////////////////////////

//		String sql = "insert into rupay_dscr_rawdata(bank_name,sett_bin, iss_bin, inward_outward, txn_count, txn_ccy, txn_amt_dr, txn_amt_cr, sett_curr, set_amt_dr, set_amt_cr, int_fee_dr, int_fee_cr, mem_inc_fee_dr, mem_inc_fee_cr, cus_compen_dr, cus_compen_cr, oth_fee_amt_dr, oth_fee_amt_cr, oth_fee_gst_dr, oth_fee_gst_cr, final_sum_cr, final_sum_dr, final_net, filedate, createdby, cycle) "
//				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'dd/mm/yyyy'),?,?)";

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

			Sheet sheet1 = workbook.getSheetAt(0); // Assuming you want to read the first sheet
			boolean batchExecuted = false;
			String hardcode = "";
			String stsApv = "";
			// Iterate over rows
			for (Row row : sheet1) {
				int cellCount = 1;

				int a = row.getRowNum();
				List<String> data = new ArrayList<>();
				int cnt = 0;
				if (row.getRowNum() == 0) {
					continue;
				}
// 				ps.setString(cellCount++, "NTB");
				for (Cell cell : row) {

					if (cnt > 0) {
						CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
						if (cell.getColumnIndex() == 0 || cell.getColumnIndex() == 1 || cell.getColumnIndex() == 2
								|| cell.getColumnIndex() == 3 || cell.getColumnIndex() == 14) {
							continue;
						} else {

							System.out.print(cellRef.formatAsString() + " - ");
							System.out.println(cell.toString());
							data.add(cell.toString());
						}
					}
					cnt++;

				}

				if (data.get(3).equals("")) {
					data.set(3, hardcode);
				} else if (data.get(1).equals("Total")) {
					data.set(3, "");
					// hardcode = "";
				} else {
					hardcode = data.get(3);
				}

				if (data.get(2).equals("")) {
					data.set(2, stsApv);
				} else if (data.get(2).equals("Total")) {
					data.set(2, "");
				} else {
					stsApv = data.get(2);
				}

				System.out.println("data in list is" + data);
				// int cnt = this.jdbcTemplate.queryForInt();

				String sql = "insert into rupay_dscr_rawdata(bank_name,sett_bin, iss_bin, inward_outward,status_approved,tran_cycle,channel,set_ccy, txn_count, txn_ccy, txn_amt_dr, txn_amt_cr, set_amt_dr, set_amt_cr, int_fee_dr, int_fee_cr, mem_inc_fee_dr, mem_inc_fee_cr, cus_compen_dr, cus_compen_cr, oth_fee_amt_dr, oth_fee_amt_cr, oth_fee_gst_dr, oth_fee_gst_cr, final_sum_cr, final_sum_dr, final_net, filedate, createdby, cycle) "
						+ "VALUES( 'NTB','Total','" + data.get(0) + "','" + data.get(1) + "','" + data.get(2) + "','"
						+ data.get(3) + "','" + data.get(4) + "','" + data.get(5) + "','" + data.get(6) + "','"
						+ data.get(7) + "'," + "'" + data.get(8) + "','" + data.get(9) + "','" + data.get(10) + "','"
						+ data.get(11) + "','" + data.get(12) + "','" + data.get(13) + "','" + data.get(14) + "'" + ",'"
						+ data.get(15) + "','" + data.get(16) + "','" + data.get(17) + "','" + data.get(18) + "','"
						+ data.get(19) + "','" + data.get(20) + "','" + data.get(21) + "','" + data.get(22) + "','"
						+ data.get(23) + "','" + data.get(24) + "'," + "TO_DATE('" + beanObj.getFileDate()
						+ "','dd/mm/yyyy'),'" + beanObj.getCreatedBy() + "','" + beanObj.getCycle() + "')";

				System.out.println("query is" + sql);
//				ps.setString(cellCount++, beanObj.getFileDate());
//				ps.setString(cellCount++, beanObj.getCreatedBy());
//				ps.setString(cellCount++, beanObj.getCycle());
//				ps.setString(cellCount++, beanObj.getFileDate());
				Statement ps = con.createStatement();
				try {
					ps.execute(sql);
//					boolean execute = ps.execute(sql);
//					System.out.println("flag : "+execute);
					System.out.println("flag : ");
				} catch (Exception e) {
					e.printStackTrace();
				}

				con.commit();

				// int res = ps.executeUpdate();
				// getJdbcTemplate().execute(sql);
				// ().execute(sql);

				// System.out.println("flag is" + res);

				mapObj.put("result", true);
//				mapObj.put("count", totalcount);

			}

		} catch (Exception e) {
			logger.info("Exception at line " + rowId);
			logger.info("Exception occurred while reading dscr file " + e);

			e.printStackTrace();
			mapObj.put("result", false);
			mapObj.put("count", totalcount);
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return mapObj;
	}

	public HashMap<String, Object> PBGBfileupload(RupayUploadBean beanObj, MultipartFile file, Connection con)
			throws SQLException {
		String bank_name = null, sett_bin = null, acq_iss_bin = null, in_out = null, status = null, tran_cycle = null;
		int totalcount = 0;
		HashMap<String, Object> mapObj = new HashMap<String, Object>();
		Workbook wb = null;
		Sheet sheet = null;
		FormulaEvaluator formulaEvaluate = null;
		int extn = file.getOriginalFilename().indexOf(".");
		logger.info("extension is " + extn);
		boolean Idbi_Block = false;
		int read_line = 0;
		boolean reading_line = false;
		boolean total_encounter = false, stop_reading = false, last_line = false;
		int final_line = 0;

		String sql = "INSERT INTO RUPAY_PBGB_DSCR_RAWDATA(BANK_NAME,SETT_BIN, ISS_BIN, INWARD_OUTWARD, TXN_COUNT, TXN_CCY, TXN_AMT_DR, TXN_AMT_CR, SETT_CURR, SET_AMT_DR, SET_AMT_CR, INT_FEE_DR, INT_FEE_CR, MEM_INC_FEE_DR, MEM_INC_FEE_CR, CUS_COMPEN_DR, CUS_COMPEN_CR, OTH_FEE_AMT_DR, OTH_FEE_AMT_CR, OTH_FEE_GST_DR, OTH_FEE_GST_CR, FINAL_SUM_CR, FINAL_SUM_DR, FINAL_NET, FILEDATE, CREATEDBY, CYCLE) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'DD/MON/YYYY'),?,?)";

		try {
			long start = System.currentTimeMillis();
			System.out.println("start");
			con.setAutoCommit(false);
			PreparedStatement ps = con.prepareStatement(sql);

			if (file.getOriginalFilename().substring(extn).equalsIgnoreCase(".XLS")) {

				// book = new HSSFWorkbook(file.getInputStream()) ;
				// wb = new HSSFWorkbook(file.getInputStream());
				wb = new XSSFWorkbook(file.getInputStream());
				sheet = wb.getSheetAt(0);
				formulaEvaluate = new XSSFFormulaEvaluator((XSSFWorkbook) wb);

			} else if (file.getOriginalFilename().substring(extn).equalsIgnoreCase(".XLSX")) {

				// book = new XSSFWorkbook(file.getInputStream());
				wb = new XSSFWorkbook(file.getInputStream());
				sheet = wb.getSheetAt(0);
				formulaEvaluate = new XSSFFormulaEvaluator((XSSFWorkbook) wb);
			}

			OUTER: for (Row r : sheet) {
				totalcount++;
				if (r.getRowNum() > 0) {
					if (read_line == 1)
						read_line++;
					reading_line = false;
					int cellCount = 1;
					if (!stop_reading) {
						for (Cell c : r) {
							switch (formulaEvaluate.evaluateInCell(c).getCellType()) {
							case Cell.CELL_TYPE_STRING:
								if (c.getStringCellValue().equalsIgnoreCase("THE NAINITAL BANK LTD AS ISS")) {
									bank_name = c.getStringCellValue();
									Idbi_Block = true;
									read_line++;
									logger.info("cell count is " + cellCount + " value " + c.getStringCellValue());
									cellCount++;
								} else if (total_encounter && final_line > 0) {
									logger.info("in 1st Total Encounter loop");
									if (final_line == 4 && cellCount == 1) {
										ps.setString(cellCount++, bank_name);
										ps.setString(cellCount++, c.getStringCellValue());
										stop_reading = true;
										last_line = true;
									} else if (final_line == 4 && cellCount > 1) {
										ps.setString(cellCount++, c.getStringCellValue());
										break;
									} else {
										final_line++;
										continue OUTER;
									}
								} else if (Idbi_Block) {
									if (read_line == 1) {
										if (cellCount == 2)
											sett_bin = c.getStringCellValue();
										else {
											continue OUTER;
										}

										logger.info("cell count is " + cellCount + " value " + c.getStringCellValue());
										cellCount++;

									} else if (c.getStringCellValue().equalsIgnoreCase("Total")) {
										if (cellCount == 1) {
											reading_line = true;
											if (total_encounter) {
												final_line++;
												continue OUTER;

											} else {
												ps.setString(cellCount++, bank_name);
												ps.setString(cellCount++, sett_bin);
												ps.setString(cellCount++, acq_iss_bin);
												ps.setString(cellCount, c.getStringCellValue());
												logger.info("cell count is " + cellCount + " value "
														+ c.getStringCellValue());
												cellCount++;
												total_encounter = true;
											}
										}
										/*
										 * else if(stop_reading) { ps.setString(cellCount++, c.getStringCellValue());
										 * logger.info("cell count is "+cellCount+" value "+ c.getStringCellValue()); }
										 */
									} else if (total_encounter) {
										logger.info("in Total Encounter loop");
										logger.info("cell count is " + cellCount + " value " + c.getStringCellValue());
										ps.setString(cellCount++, c.getStringCellValue());
									} else {
										continue OUTER;
									}

								} else if (reading_line) {
									ps.setString(cellCount, c.getStringCellValue());
									logger.info("cell count is " + cellCount + " value " + c.getStringCellValue());
									cellCount++;
								} else if (cellCount >= 3) {
									continue OUTER;
								}

								break;
							case Cell.CELL_TYPE_NUMERIC:
								if (Idbi_Block) {
									String digit = c.getNumericCellValue() + "";
									if (digit.contains("E")) {
										digit = c.getNumericCellValue() + "";
										double d = Double.parseDouble(digit);
										BigDecimal bd = new BigDecimal(d);
										/*
										 * digit = bd.round(new MathContext(15)).toPlainString();
										 * System.out.println(digit);
										 */
										String tryDigit = bd + "";
										int indexOfDot = tryDigit.indexOf(".");
										int secondDigit = Integer
												.parseInt(tryDigit.substring(indexOfDot + 1, indexOfDot + 2));
										if (secondDigit > 5) {
											digit = tryDigit.substring(0, indexOfDot + 3);
										} else {
											BigDecimal db = bd.setScale(1, RoundingMode.HALF_UP);
											digit = db.toPlainString();
										}

									}

									if (read_line == 1) {
										if (cellCount == 3)
											acq_iss_bin = digit;
										else {
											continue OUTER;
										}
										cellCount++;
									} else if (reading_line || last_line) {
										System.out.println("Cell count is " + cellCount + " and Data is " + digit);
										ps.setString(cellCount, digit);
										cellCount++;

									} else if (total_encounter) {
										System.out
												.println("Cell count is " + cellCount + " and acq_iss_bin is " + digit);
										acq_iss_bin = digit;
										total_encounter = false;
									}
									// logger.info("cell count is "+cellCount+" value "+ digit);

								}
								break;

							}

						}
						if (reading_line || last_line) {
							System.out.println("Before inserting data");
							logger.info("Cell count is " + cellCount);
							ps.setString(cellCount++, beanObj.getFileDate());
							ps.setString(cellCount++, beanObj.getCreatedBy());
							ps.setString(cellCount++, beanObj.getCycle());
							ps.execute();
						}
						logger.info("cellcount is " + cellCount);
					}

				}
			}
			// ps.executeBatch();
			con.commit();
			con.close();
			long end = System.currentTimeMillis();
			System.out.println("start and end diff" + (start - end));
			System.out.println(" table  insert");
			// response = ADDBACIDSTAT();

			System.out.println("Data Inserted");
			mapObj.put("result", true);
			mapObj.put("count", totalcount);
		} catch (Exception e) {
			e.printStackTrace();
			mapObj.put("result", false);
			mapObj.put("count", totalcount);
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return mapObj;
	}

}
