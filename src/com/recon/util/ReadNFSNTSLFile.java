package com.recon.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.multipart.MultipartFile;

import com.recon.model.NFSSettlementBean;

public class ReadNFSNTSLFile {

	private static final Logger logger = Logger.getLogger(ReadDFSandJCBNTSLFile_bk.class);
	private static final int STRING = 0;
	private static final int NUMERIC = 0;
	private static final int BOOLEAN = 0;

	public HashMap<String, Object> fileupload2(NFSSettlementBean beanObj, MultipartFile file, Connection con)
			throws SQLException {
		int response = 0;
		String tableName = null;
		int totalcount = 0;
		HashMap<String, Object> mapObj = new HashMap<String, Object>();
		String getTableName = "select tablename from main_filesource where filename = ? and file_category = ? and file_subcategory = ?";
		PreparedStatement pstmt = con.prepareStatement(getTableName);
		pstmt.setString(1, beanObj.getFileName());
		pstmt.setString(2, beanObj.getCategory());
		pstmt.setString(3, beanObj.getStSubCategory());
		ResultSet rs = pstmt.executeQuery();
		long start = System.currentTimeMillis();

		int bankCount = 0;
		int count = 1, cellCount = 4;
		String bankName = null;
		String Ignoredescription = null;
		boolean idbiRecords = true;
		String fname = file.getOriginalFilename();

		while (rs.next()) {
			tableName = (String) rs.getString("tablename");
		}

		/*
		 * String sql =
		 * "insert  into BACIDSTAT_temp (SRNO,ACCOUNTNO,Bacid,TRAN_DATE, Particulars,TRAN_RMKS ,Debit,Credit, Closing_balance,IFSC,entry_by)"
		 * + " values (?,?,?,?,?,?,?,?,?,?,6346)";
		 */
		String sql = "INSERT INTO " + tableName.toLowerCase()
				+ "(description,no_of_txns,debit,credit,cycle,filedate,createdby,createddate,sr_no,file_name) "
				+ "VALUES(?,?,?,?,?,to_date(?,'dd/mm/yyyy'),?,SYSDATE,?,?)";

		PreparedStatement ps = con.prepareStatement(sql);
		int srl_no = 1;
		try {
			Path tempDir = Files.createTempDirectory("");
			File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
			file.transferTo(tempFile);
			String content = Jsoup.parse(tempFile, "UTF-8").toString();
			org.jsoup.nodes.Document html = Jsoup.parse(content);
			if (content != null) {
				Elements contents = html.getElementsByTag("tbody");

				System.out.println("********************** Reading tbody tags ****************");

				OUTER: for (Element a : contents) {
					// code starts from here
					Elements thContents = a.getElementsByTag("th");
					Elements tdContents = a.getElementsByTag("td");
					for (Element b : thContents) {
						if (b.text().startsWith("Daily Settlement Statement")) {
							// System.out.println(thContents.text());
							bankName = b.text();
							bankCount++;
						}
						/****** Reading main fields ****************/
						for (Element c : tdContents) {
							if (bankCount == 1 && idbiRecords) {
								// INSERT IN RAW TABLE
								if (count == 1 && c.text().equalsIgnoreCase("")) {
									continue;
								} else {
									if (count == 1) {
										if (c.text().equalsIgnoreCase(Ignoredescription)) {
											idbiRecords = false;
										} else {

											if (totalcount == 0) {
												Ignoredescription = c.text();
												ps.setString(count, c.text());
												totalcount++;
											} else {
												ps.setString(count, c.text());
												totalcount++;
											}
											count++;
										}
									} else {
										ps.setString(count, c.text());
										count++;
									}

								}

								if (count == cellCount + 1) {
									ps.setInt(5, beanObj.getCycle());
									ps.setString(6, beanObj.getDatepicker());
									ps.setString(7, beanObj.getCreatedBy());
									ps.setInt(8, srl_no++);
									ps.setString(9, fname);
									ps.addBatch();
									count = 1;
								}

							}
						}
					}

				}
				ps.executeBatch();
				// con.commit();
				con.close();
				long end = System.currentTimeMillis();
				logger.info("start and end diff" + (start - end));
				mapObj.put("result", true);
				mapObj.put("count", totalcount);
			}
			// delete the file from temp folder
			FileUtils.forceDelete(tempFile);
			logger.info("File exists? " + tempFile.exists());
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

	public HashMap<String, Object> fileupload(NFSSettlementBean beanObj, MultipartFile file, Connection con)
			throws SQLException {
		int response = 0;
		String tableName = null;
		int totalcount = 0;
		HashMap<String, Object> mapObj = new HashMap<String, Object>();
		String getTableName = "SELECT TABLENAME FROM MAIN_FILESOURCE WHERE FILENAME = ? AND FILE_CATEGORY = ? AND FILE_SUBCATEGORY = ?";
		PreparedStatement pstmt = con.prepareStatement(getTableName);
		pstmt.setString(1, beanObj.getFileName());
		pstmt.setString(2, beanObj.getCategory());
		pstmt.setString(3, beanObj.getStSubCategory());
		ResultSet rs = pstmt.executeQuery();
		long start = System.currentTimeMillis();

		String fname = file.getOriginalFilename();
		int bankCount = 0;
		int count = 1, cellCount = 4;
		String bankName = null;
		String Ignoredescription = null;
		boolean idbiRecords = true;

		while (rs.next()) {
			tableName = (String) rs.getString("TABLENAME");
		}

		/*
		 * String sql =
		 * "insert  into BACIDSTAT_temp (SRNO,ACCOUNTNO,Bacid,TRAN_DATE, Particulars,TRAN_RMKS ,Debit,Credit, Closing_balance,IFSC,entry_by)"
		 * + " values (?,?,?,?,?,?,?,?,?,?,6346)";
		 */
		String sql = "INSERT INTO " + tableName
				//+ "(DESCRIPTION,NO_OF_TXNS,DEBIT,CREDIT,CYCLE,FILEDATE,CREATEDBY,CREATEDDATE,SR_NO,file_name) VALUES(?,?,?,?,?,to_date(?,'dd/mm/yyyy'),?,SYSDATE,?,?)";
				+ "(DESCRIPTION,NO_OF_TXNS,DEBIT,CREDIT,CYCLE,FILEDATE,CREATEDBY,CREATEDDATE,SR_NO) VALUES(?,?,?,?,?,to_date(?,'dd/mm/yyyy'),?,SYSDATE,?)";

		String CoopBank_sql = "INSERT INTO COOP_NTSL_NFS_RAWDATA (BANK_NAME,DESCRIPTION,DEBIT,CREDIT,CYCLE,FILEDATE,CREATEDBY,CREATEDDATE,SR_NO) "
				+ "VALUES(?,?,?,?,?,to_date(?,'dd/mm/yyyy'),?,SYSDATE,?)";

		PreparedStatement coop_ps = con.prepareStatement(CoopBank_sql);
		PreparedStatement ps = con.prepareStatement(sql);
		int srl_no = 1;
		try {
			Path tempDir = Files.createTempDirectory("");
			File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
			file.transferTo(tempFile);
			String content = Jsoup.parse(tempFile, "UTF-8").toString();
			org.jsoup.nodes.Document html = Jsoup.parse(content);
			System.out.println("Covert val is:" + html);
			System.out.println("query is:" + sql);
			if (content != null) {
				Elements contents = html.getElementsByTag("tbody");

				System.out.println("query is:" + contents);
				System.out.println("********************** Reading tbody tags ****************");

				OUTER: for (Element a : contents) {
					// code starts from here
					Elements thContents = a.getElementsByTag("th");
					Elements tdContents = a.getElementsByTag("td");
					for (Element b : thContents) {
						if (b.text().startsWith("Daily Settlement Statement for City Union Bank Ltd.-CIU")) {
							System.out.println("");
						}
						if (b.text().startsWith("Daily Settlement Statement")) {
							System.out.println(thContents.text());
							bankName = b.text();
							bankCount++;
						}
						/****** Reading main fields ****************/
						if (b.text().startsWith("Description")) {

							for (Element c : tdContents) {
								// if (bankCount == 1 && idbiRecords)
								if (c.text().equalsIgnoreCase("Total CREDIT Adjustment Amount")) {
									System.out.println("Total CREDIT Adjustment Amount");
									idbiRecords = true;
									count = 1;
								}

								/*
								 * if(c.text().equalsIgnoreCase("Final Settlement Amount")){
								 * System.out.println("Total CREDIT Adjustment Amount"); //idbiRecords = true;
								 * //count = 1; }
								 */
								// Final Settlement Amount

								if (idbiRecords) {
									// INSERT IN RAW TABLE
									if (count == 1 && c.text().equalsIgnoreCase("")) {
										continue;
									} else {
										if (count == 1) {
											if (c.text().equalsIgnoreCase(Ignoredescription)) {
												// idbiRecords = false;
											} else {

												if (totalcount == 0) {
													Ignoredescription = c.text();
													ps.setString(count, c.text());
													totalcount++;
												} else {
													ps.setString(count, c.text());
													totalcount++;
												}
												count++;
											}
										} else {
											ps.setString(count, c.text());
											count++;
										}

									}

									if (count == cellCount + 1) {
										ps.setInt(5, beanObj.getCycle());
										ps.setString(6, beanObj.getDatepicker());
										ps.setString(7, beanObj.getCreatedBy());
										ps.setInt(8, srl_no++);
										//ps.setString(9, fname);
										ps.addBatch();
										count = 1;
									}

								}
							}
						}
					}

				}
				coop_ps.executeBatch();
				ps.executeBatch();
				// con.commit();
				con.close();
				long end = System.currentTimeMillis();
				logger.info("start and end diff" + (start - end));
				mapObj.put("result", true);
				mapObj.put("count", totalcount);
			}
			// delete the file from temp folder
			FileUtils.forceDelete(tempFile);
			logger.info("File exists? " + tempFile.exists());
		} catch (Exception e) {
			e.printStackTrace();
			mapObj.put("result", false);
			mapObj.put("count", totalcount);
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mapObj;
	}

	public HashMap<String, Object> fileuploadClosing(NFSSettlementBean beanObj, MultipartFile file, Connection con)
			throws SQLException {
		HashMap<String, Object> mapObj = null;
		try {
			mapObj = new HashMap<String, Object>();

			InputStream inputStream = file.getInputStream();
			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = workbook.getSheetAt(0);
			System.out.println("Sheet Name: " + sheet.getSheetName());
			System.out.println("File size: " + file.getSize());
			System.out.println("File name: " + file.getOriginalFilename());

			Iterator<Row> rowIterator = sheet.iterator();
			boolean startReading = false;
			String n = "", branch_num = "", status_new = "", head_new = "", Gl_head = "", open_balance = "" , amt_brought_forward = "" ,
					total_debit = "", total_credit = "", total_total = "";
			String name = "", branchNo = "", status = "", head = "", glHead = "";
			String date = "", particulars = "", chqNo = "", debit = "", credit = "", balance = "";

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Cell firstCell = row.getCell(0);

				if (row.getCell(0) != null && row.getCell(0).getCellType() == Cell.CELL_TYPE_STRING) {
					String value = row.getCell(0).getStringCellValue();
					if (value.equalsIgnoreCase("Date")) {
						continue;
					}
				}

				if (isRowEmpty(row) || getCellValue(row.getCell(1)).equals("Statement of Account")) {
					startReading = false;
					continue;
				}

				if (getCellValue(row.getCell(1)).equals("Name :")
						|| getCellValue(row.getCell(1)).equals("Head Description:")
						|| getCellValue(row.getCell(17)).equals("Opening Balance :")
						|| getCellValue(row.getCell(17)).equals("Amt Brought Forward :")) {

					if (getCellValue(row.getCell(1)).equals("Name :")) {
						n = getCellValue(row.getCell(4));
						branch_num = getCellValue(row.getCell(12));
						status_new = getCellValue(row.getCell(24));

						System.out.println("name is " + n);
						System.out.println("branch  is " + branch_num);
						System.out.println("status  is " + status_new);
					} else if (getCellValue(row.getCell(1)).equals("Head Description:")) {
						head_new = getCellValue(row.getCell(5));
						Gl_head = getCellValue(row.getCell(19));

						System.out.println("head_new  is " + head_new);
						System.out.println("Gl_head  is " + Gl_head);
					} else if (getCellValue(row.getCell(17)).equals("Opening Balance :")) {
						open_balance = getCellValue(row.getCell(26));
						System.out.println("opening balace is " + open_balance);
					} else {
						open_balance = "";
						amt_brought_forward  = getCellValue(row.getCell(26));
						System.out.println("amt_brought_forward is "+ amt_brought_forward);
					}

					continue;

				}

				if (isRowEmpty(row)) {
					startReading = false;
					continue;
				}

				if (getCellValue(row.getCell(1)).equals("Date")) {
					continue;
				}

				if (getCellValue(row.getCell(6)).equals("Total")) {

					total_debit = getCellValue(row.getCell(15));

					total_credit = getCellValue(row.getCell(22));

					total_total = getCellValue(row.getCell(6));

				}
				if (!getCellValue(row.getCell(6)).equals("Total")) {
					date = getCellValue(row.getCell(1));
					particulars = getCellValue(row.getCell(2));
					chqNo = getCellValue(row.getCell(3));
					debit = getCellValue(row.getCell(20));
					credit = getCellValue(row.getCell(23));
					balance = getCellValue(row.getCell(29));
				} else {
					date = getCellValue(row.getCell(1));
					particulars = total_total;
					chqNo = "";
					debit = total_debit;
					credit = total_credit;
					balance = "";
				}

				String in_name = n;
				String in_branch = branch_num;
				String in_status = status_new;
				String in_head = head_new;
				String in_gl = Gl_head;

				if (in_name == null && in_branch == null && in_status == null && in_head == null && Gl_head == null) {
					in_name = n;
					in_branch = branch_num;
					in_status = status_new;
					in_head = head_new;
					in_gl = Gl_head;
				}

				String in = "insert into RUPAY_CLOSING_BALANCE (name, branch_no, status, head, gl_head,date_1, particulars,"
						+ " chq_no, debit, credit, balance , opening_balance,amount_Brought_Forward)"
						+ " VALUES (?, ?, ?, ?, ?, to_date(?,'DD-MON-YYYY'), ?, ?, ?, ?, ?,?,?) ";

				PreparedStatement ps = con.prepareStatement(in);
				ps.setString(1, in_name);
				ps.setString(2, in_branch);
				ps.setString(3, in_status);
				ps.setString(4, in_head);
				ps.setString(5, in_gl);
				ps.setString(6, date); // != null ? Date.valueOf(date) : null);
				ps.setString(7, particulars);
				ps.setString(8, chqNo);
				ps.setString(9, debit);
				ps.setString(10, credit);
				ps.setString(11, balance);
				ps.setString(12, open_balance);
				ps.setString(13, amt_brought_forward);
				//ps.executeUpdate();
				ps.addBatch();

				ps.executeBatch();

				// con.commit();

//				}
			}

			workbook.close();
			System.out.println("Data inserted successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		mapObj.put("result", true);
		mapObj.put("count", 100);
		return mapObj;

	}

	private static String getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				return String.valueOf(cell.getNumericCellValue());
			}
		case Cell.CELL_TYPE_BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "";
		}
	}

	private static boolean isRowEmpty(Row row) {
		for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
				return false;
			}
		}
		return true;
	}

}
