package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.util.SystemOutLogger;

import com.recon.util.OracleConn;

public class ReadUCOSwitchATM {

	//public static void main(String[] a)
	public static void main(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		List<String> elements = reading.readATMSwitch();
		int start_pos = 0;
		int lineNumber = 0;
		int sr_no = 1;
		int batchNumber = 0, executedBatch = 0;
		boolean batchExecuted = false;
		
		
		String InsertQuery = "INSERT INTO SWITCH_UCO_ATM_RAWDATA(RECORD_LENGTH, DATA_LENGTH, DATE_TIME, RECORD_TYPE, AUTH_PPD, TERM_LN, TERM_FIID, TERM_ID, CARD_LN, CARD_FIID, CARD_NUMBER, BRANCH_ID, CODE_INDICATOR_ENVE, MESSAGE_TYPE, AUTH_ORIGINATOR, AUTH_RESPONDER, TRAN_BEGIN_DATE, TIME, AUTH_SEQ_NUMB, TERMINAL_TYPE, ACQU_INST_ID, RECEIVING_INST_ID, TRANSACTION_TYPE, FROM_ACCOUNT, TO_ACCOUNT, AMOUNT1, AMOUNT2, AVAILABLE_BAL, RESPONSE_CODE, TERM_NAME_LOC, TERM_OWNER_NAME, TERM_CITY, STATE, TERM_COUNTRY, ORIG_SEQ_NUMBER, TRAN_ORIGIN_DATE, TRAN_ORIGIN_TIME, CURRENCY_CODE, RRN, CREATEDBY, FILEDATE) "+
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?,'DD/MM/YYYY'))";
		
		
		
		try
		{
			System.out.println("Enter file date ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			String filedate = reader.readLine();
			
			if(!filedate.contains("/"))
			{
				System.out.println("Enter proper date ");
				System.exit(1);
			}
			System.out.println("Enter File Path ");
			
			String filePath = reader.readLine();
			
			OracleConn oracObj = new OracleConn();
			Connection conn = oracObj.getconn();
			PreparedStatement ps = conn.prepareStatement(InsertQuery);
			
			//File file = new File("\\\\172.28.96.150\\h\\March 2022\\13-03-2022\\SWITCH FILES 07062021\\UCOTLF_Z0901000_m_130322.txt");
			File file = new File(filePath);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);			
			conn.setAutoCommit(false);
			
			while((stLine = br.readLine()) != null)
			{
				sr_no = 1;
				batchExecuted = false;
				start_pos = 0;
				
				if(!stLine.contains("\\FSS2.$"))
				{
					lineNumber++;
					for(int i = 0; i < elements.size(); i++)
					{
						String[] data = elements.get(i).split("\\|");
						if(data.length > 2)
						{
							if(data[0].equalsIgnoreCase("Transaction begin date") )
							{
								start_pos = Integer.parseInt(data[1]);
								DateFormat fmt1 = new SimpleDateFormat("yymmDD");
								Date date = fmt1.parse(stLine.substring(start_pos,(start_pos+Integer.parseInt(data[2]))).trim());
								DateFormat fmt2 = new SimpleDateFormat("dd/mm/yyyy");
								String output = fmt2.format(date);
								//System.out.println("Date time "+output);
								ps.setString(sr_no++ , output);
								start_pos = start_pos+Integer.parseInt(data[2]);
							}
							else if(data[0].equalsIgnoreCase("Date Time") )
							{
								start_pos = Integer.parseInt(data[1]);
								DateFormat fmt1 = new SimpleDateFormat("yyDDD");
								Date date = fmt1.parse(stLine.substring(start_pos,(start_pos+Integer.parseInt(data[2]))).trim());
								DateFormat fmt2 = new SimpleDateFormat("dd/MM/yyyy");
								String output = fmt2.format(date);
								//System.out.println(output);
								ps.setString(sr_no++ , output);
								start_pos = start_pos+Integer.parseInt(data[2]);
							
							}
							else
							{
								start_pos = Integer.parseInt(data[1]);
//								System.out.println("1 sr no is "+sr_no+data[0]+" "+stLine.substring(start_pos,(start_pos+Integer.parseInt(data[2]))));


								//System.out.println("1 sr no is "+sr_no);
								ps.setString(sr_no++, stLine.substring(start_pos,(start_pos+Integer.parseInt(data[2]))).trim());

								start_pos = start_pos+Integer.parseInt(data[2]);
							}
							
						}
						else
						{
								//System.out.println("2 sr no is  "+sr_no+data[0]+" "+stLine.substring(start_pos, (start_pos+Integer.parseInt(data[1]))));

								//System.out.println("2 sr no is "+sr_no);
								ps.setString(sr_no++, stLine.substring(start_pos,(start_pos+Integer.parseInt(data[1]))).trim());

								start_pos  = start_pos + Integer.parseInt(data[1]);
						}
					}
					
					ps.setString(sr_no++, "AUTOJOB");
					ps.setString(sr_no, filedate);
					
					ps.addBatch();
					batchNumber++;
					
					if(batchNumber == 20000)
					{
						executedBatch++;
						System.out.println("Batch Executed is "+executedBatch);
						ps.executeBatch();
						batchNumber = 0;
						batchExecuted = true;
					}
				}
			}
			
			if(!batchExecuted)
			{
				executedBatch++;
				System.out.println("Batch Executed of switch ATM file is "+executedBatch);
				ps.executeBatch();
			}
			
			
			System.out.println("File Reading Completed !!!");
			try
			{
				System.out.println("Entering in switch rawdata ");
				//entry in main raw tables
				String insert = "insert into switch_rawdata(MSGTYPE, PAN, TERMID, LOCAL_DATE, LOCAL_TIME, AMOUNT, ACCEPTORNAME, RESPCODE, TERMLOC, NEW_AMOUNT, AMOUNT_EQUIV, CH_AMOUNT, ISS_CURRENCY_CODE, ACQ_CURRENCY_CODE, TRACE, ACCTNUM, PAN2, ISSUER, CREATEDDATE, CREATEDBY, FILEDATE, DCRS_REMARKS, FPAN, NETWORK, TRANSACTION_TYPE)"
					    +" select MESSAGE_TYPE, SUBSTR(trim(CARD_NUMBER),1,6) ||'XXXXXX'|| SUBSTR(trim(CARD_NUMBER),-4), trim(TERM_ID), TRAN_BEGIN_DATE, TIME, TO_NUMBER(AMOUNT1/100), TERM_OWNER_NAME, RESPONSE_CODE,  TERM_NAME_LOC, TO_NUMBER(AMOUNT2/100), TO_NUMBER(AMOUNT1/100), TO_NUMBER(AMOUNT2/100), CURRENCY_CODE, CURRENCY_CODE, "
					    + "trim(ORIG_SEQ_NUMBER), FROM_ACCOUNT, RECEIVING_INST_ID, trim(RRN), CREATEDDATE, CREATEDBY,"
				         +"FILEDATE, 'ATM', ibkl_encrypt_decrypt.ibkl_set_encrypt_val(trim(CARD_NUMBER)) enc , TERM_FIID, TRANSACTION_TYPE" 
				    +" FROM SWITCH_UCO_ATM_RAWDATA where filedate = to_date('"+filedate+"','DD/MM/YYYY')";
				PreparedStatement pstm = conn.prepareStatement(insert);
				pstm.execute();
				
				pstm.close();
				System.out.println("Entered in switch rawdata ");
				
				System.out.println("updating in switch rawdata ");
				
				String updateTable = "UPDATE SWITCH_UCO_ATM_RAWDATA SET CARD_NUMBER = ibkl_encrypt_decrypt.ibkl_set_encrypt_val(trim(CARD_NUMBER)) "
						+ "WHERE filedate = to_date('"+filedate+"','DD/MM/YYYY')";
				pstm = conn.prepareStatement(updateTable);

				pstm.execute();
				pstm.close();
				
				System.out.println("encrypted pan in switch ATM rawdata ");	
				/*insert = "insert into SWITCH_UCO_POS_RAWDATA select * from SWITCH_UCO_POS_RAWDATA_temp";
				pstm = conn.prepareStatement(insert);
				pstm.execute();

				String delete = "DELETE switch_uco_POS_rawdata_TEMP";
				pstm = conn.prepareStatement(delete);
				pstm.execute();*/


				// entry in main file upload dtls table
				System.out.println("Entry in main file upload");
				//1. check whether already entry is present if yes then update file count
			/*	String checkEntry = "select count(*) from main_file_upload_dtls where filedate = to_date('"+filedate+"','dd/mm/yyyy') and "+
						"fileid in (select fileid from main_filesource where filename = 'SWITCH')";
				pstm = conn.prepareStatement(checkEntry);
				ResultSet rs = pstm.executeQuery();
				int entrycount = 0;
				while(rs.next())
				{
					entrycount = rs.getInt(1);
				}

				if(entrycount > 0)
				{
					//update file count
					String updateQuery = "UPDATE MAIN_FILE_UPLOAD_DTLS SET file_count = 2 where filedate = '"+filedate+"' and fileid in "
							+"(select fileid from main_filesource where filename = 'SWITCH')";
					pstm = conn.prepareStatement(updateQuery);
					pstm.execute();

				}
				else
				{
					//new entry
					String insertQuery = "insert into main_file_upload_Dtls(FILEID,FILEDATE,UPDLODBY,UPLOADDATE,CATEGORY,FILE_SUBCATEGORY,Upload_FLAG,Filter_FLAG,Knockoff_FLAG,Comapre_FLAG,ManualCompare_Flag,"
							+"MANUPLOAD_FLAG) "
							+"select fileid, '"+filedate+"','AUTOJOB',SYSDATE, FILE_CATEGORY, FILE_SUBCATEGORY, 'Y','N','N','N','N','Y' from main_filesource where filename = 'SWITCH'";
					pstm = conn.prepareStatement(insertQuery);
					pstm.execute();
				}*/
				System.out.println("Entry done in main file upload");
				
				conn.commit();
				br.close();
				ps.close();
			}
			catch(Exception e)
			{
				System.out.println("Exception in transfering data "+e);
			}
			
			br.close();
			ps.close();
			
			//entry in tracking table
		}
		catch(Exception e)
		{
			System.out.println("Exception at line "+lineNumber);
			System.out.println("Exception at line "+stLine);
			System.out.println("Exception in ReadUCOATMSwitchData "+e);
		}
	
	}
}
