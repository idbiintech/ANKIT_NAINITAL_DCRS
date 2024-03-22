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

		String InsertQuery = "insert into switch_rawdata_nainital(tranxdate, tranxtime, terminalid, terminaltype, switch,"
				+ "stan_no, card_type, cardno, account_type, account_no, acqbank, retrefno, txntype, amt_requested, "
				+ "amt_approved, intftype, void_code, atmlocation, embossed_name, status, error, blank, filedate) "
				+ "VALUES(to_date(?,'dd/mm/yyyy'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, to_date(?,'dd/mm/yyyy'))";

		String stLine = null;
		Switch_POS reading = new Switch_POS();

		int start_pos = 0;
		int lineNumber = 0, reading_line = 5;
		int sr_no = 1;
		int batchNumber = 0, executedBatch = 0;
		boolean batchExecuted = false;
		int batchSize = 0;

		HashMap<String, Object> retOutput = new HashMap<String, Object>();

		System.out.print("insert query is :" + InsertQuery);

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			PreparedStatement ps = con.prepareStatement(InsertQuery);

			int num = lineNumber - 1 ;
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
						
						output.put("msg", "Records Count is " + num);
					
					}

				}
			}

			if (!batchExecuted) {
				batchNumber++;
				System.out.println("Batch Executed is " + batchNumber);
				ps.executeBatch();
				output.put("result", true);
				output.put("msg", "Records Count is " + num);
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
