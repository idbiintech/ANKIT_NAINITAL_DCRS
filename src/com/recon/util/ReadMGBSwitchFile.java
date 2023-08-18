/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.recon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;

import com.recon.auto.Switch_POS;
//import com.recon.auto.AutoFilterNKnockoff;
import com.recon.model.CompareSetupBean;
import com.recon.model.FileSourceBean;

/**
 *
 * @author int6261
 */
public class ReadMGBSwitchFile extends JdbcDaoSupport {

	PlatformTransactionManager transactionManager;
	Connection con;
	Statement st;

	public void setTransactionManager() {
		logger.info("***** ReadSwitchFile.setTransactionManager Start ****");
		try {

			ApplicationContext context = new ClassPathXmlApplicationContext();
			context = new ClassPathXmlApplicationContext("/resources/bean.xml");

			logger.info("in settransactionManager");
			transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
			logger.info(" settransactionManager completed");

			logger.info("***** ReadSwitchFile.setTransactionManager End ****");

			((ClassPathXmlApplicationContext) context).close();
		} catch (Exception ex) {

			logger.error(" error in ReadSwitchFile.setTransactionManager",
					new Exception("ReadSwitchFile.setTransactionManager", ex));
		}

	}

	@SuppressWarnings("deprecation")
	public HashMap<String, Object> uploadSwitchData(CompareSetupBean setupBean, Connection con, MultipartFile file,
			FileSourceBean sourceBean) {
		HashMap<String, Object> output = new HashMap<String, Object>();
//		String stLine = null;
//		int lineNumber = 0, reading_line = 5, batchNumber = 0, start_pos = 0, batchSize = 0;
//		boolean batchExecuted = false;
//		String action = null;
//		int lastRow;
//		Switch_POS reading = new Switch_POS();
//		boolean posFile = false;
//		List<String> elements = reading.readMGBAtmCBS();
//		String Glnumber;
//		String InsertQuery = "insert into switch_rawdata_temp1(tranxdate, tranxtime, terminalid, terminaltype, switch, stan_no, card_type, cardno, account_type, account_no, acqbank, retrefno, txttype, amt_requested, amt_approved, intftype, void_code, atmlocation, embossed_name, status, error, filedate, createddate, createdby) "
//				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_date(?,'dd/mm/yyyy'), ?, ?)";

		String InsertQuery = "insert into switch_rawdata_nainital(tranxdate, tranxtime, terminalid, terminaltype, switch,"
				+ "stan_no, card_type, cardno, account_type, account_no, acqbank, retrefno, txntype, amt_requested, "
				+ "amt_approved, intftype, void_code, atmlocation, embossed_name, status, error, blank, filedate) "
				+ "VALUES(to_date(?,'dd/mm/yyyy'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, to_date(?,'dd/mm/yyyy'))";

		// try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
//			PreparedStatement ps = con.prepareStatement(InsertQuery);
//			con.setAutoCommit(false);
//			Workbook wb = null;
//			int cellIdx;
//			int sr_no = 1;
//			Sheet sheet = null;
//			FormulaEvaluator formulaEvaluate = null;
//			int extn = file.getOriginalFilename().indexOf(".");
//			wb = new XSSFWorkbook(file.getInputStream());
//			sheet = wb.getSheetAt(0);
//			lastRow = sheet.getLastRowNum();
//			lineNumber = lastRow;
//			for (int rowNumber = 5; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
//				if (rowNumber == 0) {
//					continue;
//				}
//				Row currentRow = (Row) sheet.getRow(rowNumber);
//				cellIdx = 0;
//				sr_no = 1;
//				DataFormatter dataFormatter = new DataFormatter();
//				for (cellIdx = 0; cellIdx < currentRow.getLastCellNum(); cellIdx++) {	
//					try {
//						Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//						if (currentRow.getCell(cellIdx).getCellType() == Cell.CELL_TYPE_STRING
//								|| currentRow.getCell(cellIdx).getCellType() == Cell.CELL_TYPE_NUMERIC
//								|| currentRow.getCell(cellIdx).getCellType() == 1
//								|| currentRow.getCell(cellIdx).getCellType() == 3) {
//							switch (cellIdx) {
//							case 0:
//								
//								String cellStringValue = dataFormatter.formatCellValue(currentRow.getCell(0));
//								System.out.println(cellStringValue);
//								String date = cellStringValue.substring(0,8).trim();
//								String time = cellStringValue.substring(9,17).trim();
//								System.out.println(date);
//								System.out.println(time);
//								ps.setString(sr_no++, date);
//								ps.setString(sr_no++, time);
//								break;
//								
//							case 1:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//								
//							case 2:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//
//							case 3:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 4:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 5:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 6:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									long value = Long.valueOf((long) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 7:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//
//							case 8:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									long value = Long.valueOf((long) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 9:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 10:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									long value = Long.valueOf((long) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//
//							case 11:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//
//							case 12:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//								
//							case 13:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//								
//							case 14:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//								
//							case 15:
//								if(currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//									int value = Integer.valueOf((int) currentCell.getNumericCellValue());
//									System.out.println(value);
//									ps.setString(sr_no++, String.valueOf(value));
//								}
//								else {
//									System.out.println(currentCell.getStringCellValue());	
//									ps.setString(sr_no++, currentCell.getStringCellValue());
//								}
//								break;
//								
//							case 16:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//								
//							case 17:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								break;
//								
//							case 18:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								if(currentRow.getLastCellNum()==19) {
//									ps.setString(sr_no++, " ");
//									ps.setString(sr_no++, setupBean.getFileDate());
//									System.out.println(setupBean.getFileDate());
////									ps.setString(sr_no++, setupBean.getExcelType());
////									System.out.println(setupBean.getExcelType());
////									ps.setString(sr_no++, setupBean.getCreatedBy());
//									ps.addBatch();
//									batchSize++;
//								}
//								break;
//
//							case 19:
//								System.out.println(currentCell.getStringCellValue());
//								ps.setString(sr_no++, currentCell.getStringCellValue());
//								ps.setString(sr_no++, setupBean.getFileDate());
//								System.out.println(setupBean.getFileDate());
////								ps.setString(sr_no++, setupBean.getExcelType());
////								System.out.println(setupBean.getExcelType());
////								ps.setString(sr_no++, setupBean.getCreatedBy());
//								ps.addBatch();
//								batchSize++;
//								break;
//							}// switch block  cellIdx=19
//						} // if block
//					} // 2nd try block
//					catch (Exception e) {
//						System.out.println(e);
//					}
//				} // 2nd for loop
//				if (batchSize == 10000) {
//					batchNumber++;
//					System.out.println("Batch Executed is " + batchNumber);
//					ps.executeBatch();
//					batchSize = 0;
//					batchExecuted = true;
//				}
//			} // 1st for loop
//			if (!batchExecuted) {
//				batchNumber++;
//				ps.executeBatch();
//				System.out.println("Batch Executed is " + batchNumber);
//			}
//			con.commit();
//			br.close();
//			System.out.println("Reading data " + new Date().toString());
//			output.put("result", true);
//			output.put("msg", "Records Count is " + lineNumber);
//			return output;
//		} catch (Exception e) {
//			System.out.println("Issue at line " + stLine);
//			System.out.println("Exception in uploadISSData " + e);
//			// return false;
//			output.put("result", false);
//			output.put("msg", "Exception Occured");
//			return output;
//		}
		String stLine = null;
		Switch_POS reading = new Switch_POS();
//		List<String> elements = reading.readMGBSwitch();
		// List<String> elements = null;
		// String[] elements = stLine.split("\\,");

//		if (setupBean.getStSubCategory().equalsIgnoreCase("ACQUIRER")) {
//			elements = reading.readMGBSwitchAcq();
//		}

		int start_pos = 0;
		int lineNumber = 0, reading_line = 5;
		int sr_no = 1;
		int batchNumber = 0, executedBatch = 0;
		boolean batchExecuted = false;
		int batchSize = 0;

		HashMap<String, Object> retOutput = new HashMap<String, Object>();

		System.out.print("insert query is :" + InsertQuery);
		// String delete_query = "delete from switch_mgb_rawdata";

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			PreparedStatement ps = con.prepareStatement(InsertQuery);

			while ((stLine = br.readLine()) != null) {
				lineNumber++;
				batchExecuted = false;
				sr_no = 0;

				if (lineNumber > 5) {
					String[] splitData = stLine.split("\\,", -1);

					for (int i = 0; i < splitData.length; i++) {
						if (i == 0) {
							String[] s = splitData[i].split(" ");
							ps.setString(++sr_no, s[0]);
							System.out.println(sr_no + " " + s[0].trim());
							ps.setString(++sr_no, s[1]);
							System.out.println(sr_no + " " + s[1].trim());
						} else {
							ps.setString(++sr_no, splitData[i]);
							System.out.println(sr_no + " " + splitData[i].trim());
						}
					}
					ps.setString(++sr_no, setupBean.getFileDate());
					ps.addBatch();
					batchSize++;
					if (batchSize == 10000) {
						batchNumber++;
						System.out.println("Batch Executed is " + batchNumber);
						ps.executeBatch();
						batchSize = 0;
						batchExecuted = true;
						output.put("result", true);
						output.put("msg", "Records Count is " + lineNumber);
					}

				}
			}

			if (!batchExecuted) {
				batchNumber++;
				System.out.println("Batch Executed is " + batchNumber);
				ps.executeBatch();
				output.put("result", true);
				output.put("msg", "Records Count is " + lineNumber);
			}

			br.close();
			
			ps.close();
			con.close();
			System.out.println("Reading data " + new Date().toString());

			return output;

		} catch (Exception e) {
			try {
				con.rollback();
			} catch (Exception ex) {
				logger.info("Exception in ReadSwitchData " + e);
				retOutput.put("result", false);
				retOutput.put("msg", "Issue at Line Number " + lineNumber);
				return retOutput;
			}

			logger.info("Exception in ReadSwitchData " + e);
			retOutput.put("result", false);
			retOutput.put("msg", "Issue at Line Number " + lineNumber);
			return retOutput;
		}

	}

}
