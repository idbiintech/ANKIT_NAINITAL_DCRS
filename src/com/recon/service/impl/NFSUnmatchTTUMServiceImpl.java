package com.recon.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.recon.model.NFSSettlementBean;
import com.recon.model.UnMatchedTTUMBean;
import com.recon.service.NFSUnmatchTTUMService;
import com.recon.util.GeneralUtil;

public class NFSUnmatchTTUMServiceImpl extends JdbcDaoSupport implements NFSUnmatchTTUMService {
	
	private static final Logger logger = Logger.getLogger(NFSUnmatchTTUMServiceImpl.class);
	private static final String O_ERROR_MESSAGE = "o_error_message";
	
	@Autowired
	GeneralUtil genetalUtil ;
	
	@Override
	public HashMap<String,Object> checkTTUMProcessed(UnMatchedTTUMBean beanObj)
	{
		String passdate = genetalUtil.DateFunction(beanObj.getLocalDate());
		String query = " select count(1) from	";
		HashMap<String,Object> output = new HashMap<>();
		try
		{
			if(beanObj.getTypeOfTTUM().equals("FAILED"))
			{
//				query = query+" ttum_nfs_iss_cbs_nainital where tran_date = '"+passdate+"' ";
				query = query+" ttum_nfs_iss_cbs_nainital where filedate = '"+passdate+"' ";
						
			}
			else if(beanObj.getTypeOfTTUM().equals("UNRECON"))
			{
//				query = query+" ttum_nfs_iss_switch_nainital where tranxdate = to_date('"+passdate+"','yyyy/mm/dd') ";
				query = query+" ttum_nfs_iss_switch_nainital where filedate = '"+passdate+"' ";
				
						
			}
			else if(beanObj.getTypeOfTTUM().equals("LATEREV"))
			{
				query = query+" ttum_nfs_iss_nfs where tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			}
			else if(beanObj.getTypeOfTTUM().equals("RUPAYONUS"))
			{
				query = query+" ttum_nfs_onus_cbs where tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			}
			else if(beanObj.getTypeOfTTUM().equals("NIH"))
			{
				query = query+" ttum_nfs_acq_nfs where tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			}
			logger.info("query "+query);
			
			int recordCount = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
			
			if(recordCount == 0)
			{
				output.put("result", true);
			}
			else
			{
				output.put("result", false);
				output.put("msg", "TTUM is already processed");
			}
			
		}
		catch(Exception e)
		{
			logger.info("Exception in checkTTUMProcessed "+e);
			output.put("result", false);
			output.put("msg", "Exception while validating");
		}
		
		return output;
	}
	
	public HashMap<String,Object> checkReconDateAndTTUMDataPresent(UnMatchedTTUMBean beanObj)
	{
		HashMap<String,Object> output = new HashMap<>();
		//String query = "Select count(1) from ";
		String query = "" ;
		String passdate = genetalUtil.DateFunction(beanObj.getLocalDate());
		try
		{
			//1. check whether recon date is greater than selected tran date
//			query = query + "settlement_nfs_"+beanObj.getStSubCategory().substring(0, 3).toLowerCase()
//					+"_cbs_nainital where filedate >= to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			
				
	//		logger.info("query is "+query);
			
	//		int recordCount = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
			
		//	if(recordCount > 0)
			
			int recordCount ;
			
				//2. check whether data is present for processing
				if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
				{
					query = "select count(1) from settlement_nfs_iss_cbs_NAINITAL where filedate = '"+passdate+"'"
							+" and dcrs_remarks  like '%NFS-ISS-FAILED%'  ";
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
				{
					query = "select count(1) from settlement_nfs_iss_switch_NAINITAL where filedate = "
							+ "(select max(filedate) from settlement_nfs_iss_switch_NAINITAL) "
							+" and dcrs_remarks  = 'NFS-ISS-UNRECON-2' and void_code in ( '00' , '0' , '000') "
							+" and to_date(TRANXDATE,'yyyy/mm/dd') = "
							+ "to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("LATEREV"))
				{
					query = "select count(1) from nfs_rev_acq_report t3 where acq != 'DLB' " 
				            +"and to_date(t3.trasn_date ,'dd-mm-yyyy') = "
				            + "to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')"
				            +"and not exists( "
				            +" select 1 "
				            +" from cbs_rupay_rawdata t1 , nfs_rev_acq_report t2 where " 
				            +" to_date(t3.trasn_date ,'dd-mm-yyyy') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') " 
				            +" and to_number(t1.amount) = to_number(t2.requestamt) "
				            +"  and t1.remarks = t2.cardno "  
				            +" and t1.ref_no = t2.rrn "
				            +" and e = '200' AND t2.acq !='DLB' " 
				            +" AND t1.filedate BETWEEN to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') " 
				            +" AND to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')+5 "
				            +" and t3.rrn = t2.rrn and t3.requestamt = t2.requestamt and t3.filedate = t2.filedate " 
				            +" and t3.cardno = t2.cardno )";
				            
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("NIH"))
				{
					query = "select count(1) from settlement_nfs_acq_nfs t1 where filedate =  "
							+ "(select max(filedate) from settlement_nfs_acq_nfs) "
				             +" and to_date(transaction_date,'yyyymmdd') = "
				             + "to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') "
				             +" and t1.dcrs_remarks = 'NFS-ACQ-UNRECON-2'";
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("RUPAYONUS"))
				{
					query = "select count(1) from settlement_nfs_iss_cbs where dcrs_remarks = 'NFS-RUPAY-ONUS' "
							+ "and to_date(substr(tran_date,1,8),'ddmmyyyy') = "
							+ "to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				}
				logger.info("query is "+query);
				
				recordCount = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
				
				if(recordCount > 0)
				{
					output.put("result", true);
				}
				else
				{
					output.put("result", false);
					output.put("msg", "No records present for processing");
				
		}
//			else
//			{
//				output.put("result", false);
//				output.put("msg", "Tran date is greater than recon date");
//			}
			
			
		}
		catch(Exception e)
		{
			logger.info("Exception while checking records "+e);
			output.put("result", false);
			output.put("msg", "Exception while checking records ");
			
		}
		return output;
		
	}
	
	public boolean runTTUMProcess(UnMatchedTTUMBean beanObj)
	{
		
		Map<String,Object> inParams = new HashMap<>();
		Map<String, Object> outParams = new HashMap<String, Object>();
		try
		{
			String passdate = genetalUtil.DateFunction(beanObj.getLocalDate());
			
			UnmatchedTTUMProc rollBackexe = new UnmatchedTTUMProc(getJdbcTemplate());
			//inParams.put("filedt", beanObj.getFileDate());
			//inParams.put("user_id", beanObj.getCreatedBy()); 
			inParams.put("ttumtype", beanObj.getTypeOfTTUM());
			inParams.put("subcategory", beanObj.getStSubCategory());
			inParams.put("localdt", passdate);
			outParams = rollBackexe.execute(inParams);
			if(outParams !=null && outParams.get("msg") != null)
			{
				logger.info("OUT PARAM IS "+outParams.get("msg"));
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{
			logger.info("Exception in runTTUMProcess "+e);
			return false;
		}
		
	}
	
	private class UnmatchedTTUMProc extends StoredProcedure{
		private static final String insert_proc = "NFS_UNMATCH_TTUM_PROCESS_NAINITAL";
		public UnmatchedTTUMProc(JdbcTemplate jdbcTemplate)
		{
			super(jdbcTemplate,insert_proc);
			setFunction(false);
			//declareParameter(new SqlParameter("filedt",Types.VARCHAR));
			//declareParameter(new SqlParameter("user_id",Types.VARCHAR));
			declareParameter(new SqlParameter("ttumtype",Types.VARCHAR));
			declareParameter(new SqlParameter("subcategory",Types.VARCHAR));
			declareParameter(new SqlParameter("localdt",Types.VARCHAR));
			declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
			compile();
		}

	}
	
	@Override
	public List<Object> getNFSTTUMData(UnMatchedTTUMBean beanObj)
	{
		String ttum_tableName = null;
		List<Object> data = new ArrayList<Object>();
		String dom_fetch_condition = "";
	//	String ttum_format = null; 
		String passdate = genetalUtil.DateFunction(beanObj.getLocalDate());
		try
		{
			
			if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
			{
					ttum_tableName = "ttum_nfs_iss_cbs_nainital";
//					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
					dom_fetch_condition = " WHERE filedate = '"+passdate+"'  ";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
			{
					ttum_tableName = "ttum_nfs_iss_switch_nainital";
					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("LATEREV"))
			{
				ttum_tableName = "ttum_nfs_iss_nfs";
					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getAcqtypeOfTTUM().equals("NIH"))
			{
				ttum_tableName = "ttum_nfs_acq_nfs";
					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("RUPAYONUS"))
			{
				ttum_tableName = "ttum_nfs_onus_cbs";
					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			
			
			String getdomData = null;
			
			/*
			 * getdomData = "select account_number as account_number,part_tran_type," +
			 * "transaction_amount," +
			 * "transaction_particular ,nvl(reference_number,' ') AS remarks" +
			 * ",to_date(filedate,'dd/mm/yyyy') AS filedate from "+ttum_tableName
			 * +dom_fetch_condition;
			 */
			
			getdomData = "select accountid,txn_type,"
					+ "amount,"
					+ "narration "
					+ ",to_date(filedate,'dd/mm/yyyy') AS filedate from "+ttum_tableName
					+dom_fetch_condition;
				
			
				logger.info("Getdata query is "+getdomData);
				
				List<Object> DailyData = getJdbcTemplate().query(getdomData, new Object[] {}, new ResultSetExtractor<List<Object>>(){
					public List<Object> extractData(ResultSet rs)throws SQLException {
						List<Object> beanList = new ArrayList<Object>();
						
						while (rs.next()) {
							Map<String, String> table_Data = new HashMap<String, String>();
								table_Data.put("account_number", rs.getString("accountid"));
								table_Data.put("part_tran_type", rs.getString("txn_type"));
								table_Data.put("transaction_amount", rs.getString("amount"));
								table_Data.put("transaction_particular", rs.getString("narration"));
								//table_Data.put("remarks", rs.getString("remarks"));
								table_Data.put("filedate", rs.getString("filedate"));
							beanList.add(table_Data);
						}
						return beanList;
					}
				});
			

			data.add(DailyData);
			
			return data;

		}
		catch(Exception e)
		{
			System.out.println("Exception in getTTUMData "+e);
			return null;

		}
		
	}
	
	public void generateExcelTTUM(String stPath, String FileName,List<Object> ExcelData,String zipName,HttpServletResponse response, boolean ZipFolder )
	{

		StringBuffer lineData;
		List<String> files = new ArrayList<>();
		FileInputStream fis;
		try
		{
			logger.info("Filename is "+FileName);
			List<Object> TTUMData = (List<Object>) ExcelData.get(1);
			List<String> Excel_Headers = (List<String>) ExcelData.get(0);
			
			/*File file = new File(stPath+File.separator+FileName);
			if(file.exists())
			{
				FileUtils.forceDelete(file);
			}
			file.createNewFile();*/

						
			OutputStream fileOut = new FileOutputStream(stPath+File.separator+FileName);   
			
			HSSFWorkbook workbook = new HSSFWorkbook();
	        HSSFSheet sheet = workbook.createSheet("Report");   
			
	     // create header row
	    	HSSFRow header = sheet.createRow(0);
	    	
	    	for(int i =0 ;i < Excel_Headers.size(); i++)
	    	{
	    		header.createCell(i).setCellValue(Excel_Headers.get(i));
	    	}
	    	
	    	HSSFRow rowEntry;
	    	
	    	for(int i =0; i< TTUMData.size() ; i++)
	    	{
	    		rowEntry = sheet.createRow(i+1);
	    		Map<String, String> map_data =  (Map<String, String>) TTUMData.get(i);
	    		if(map_data.size()>0)
	    		{

	    			for(int m= 0 ;m < Excel_Headers.size() ; m++)
	    			{
	    				
	    						
	    					rowEntry.createCell(m).setCellValue(map_data.get(Excel_Headers.get(m)));
	    			}
	    		}

	    	}
	    	
	    	workbook.write(fileOut);
	    	fileOut.close();
	    	
	    	File file = new File(stPath);
	    	String[] filelist = file.list();
	    	
	    	for(String Names : filelist )
	    	{	
	    		logger.info("name is "+Names);
	    		files.add(stPath+File.separator+Names);
	    	}
	    	FileOutputStream fos = new FileOutputStream(stPath+File.separator+zipName+ ".zip");
			ZipOutputStream   zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
	           try
	           {
	        	   for(String filespath : files)
	        	   {
	        		   File input = new File(filespath);
	        		   fis = new FileInputStream(input);
	        		   ZipEntry ze = new ZipEntry(input.getName());
	        		  // System.out.println("Zipping the file: "+input.getName());
	        		   zipOut.putNextEntry(ze);
	        		   byte[] tmp = new byte[4*1024];
	        		   int size = 0;
	        		   while((size = fis.read(tmp)) != -1){
	        			   zipOut.write(tmp, 0, size);
	        		   }
	        		   zipOut.flush();
	        		   fis.close();
	        	   }
	        	   zipOut.close();
	        	 //  System.out.println("Done... Zipped the files...");
	           }
	           catch(Exception fe)
	           {
	        	   System.out.println("Exception in zipping is "+fe);
	           }
		}
		catch(Exception e)
		{
			logger.info("Exception in generateTTUMFile "+e );

		}


	}
	
	public boolean checkAndMakeDirectory(UnMatchedTTUMBean beanObj)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yy/mm/dd");
			java.util.Date date = sdf.parse(beanObj.getLocalDate());

			sdf = new SimpleDateFormat("dd-MM-yyyy");

			String stnewDate = sdf.format(date);
			
			//1. Delete folder
			logger.info("Path is "+beanObj.getStPath()+File.separator+beanObj.getCategory());
			File checkFile = new File(beanObj.getStPath()+File.separator+beanObj.getCategory());
			if(checkFile.exists())
				FileUtils.forceDelete(new File(beanObj.getStPath()+File.separator+beanObj.getCategory()));
			
			//2. check whether category folder is there or not
			File directory = new File(beanObj.getStPath()+File.separator+beanObj.getCategory());
			if(!directory.exists())
			{
				directory.mkdir();
			}
			directory = new File(beanObj.getStPath()+File.separator+beanObj.getCategory()+File.separator+stnewDate);
			
			if(!directory.exists())
			{
				directory.mkdir();
			}
			
			beanObj.setStPath(beanObj.getStPath()+File.separator+beanObj.getCategory()+File.separator+stnewDate);
			
			return true;
		}
		catch(Exception e)
		{
			logger.info("Exception in checkAndMakeDirectory "+e);
			return false;
		}
	}
	
//TTUM ROLLBACK CODE
	public Boolean NFSTtumRollback(UnMatchedTTUMBean beanObj)
	{
		String deleteQuery = null;
		String updateQuery = null;
		try
		{
			if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
			{
				deleteQuery = "delete from ttum_nfs_iss_cbs  WHERE to_date(tran_date,'dd/mm/yyyy') = to_date('"
						+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				getJdbcTemplate().execute(deleteQuery);
				
				updateQuery = "update settlement_nfs_iss_cbs_nainital set dcrs_remarks = 'NFS-ISS-UNRECON-2 (' where "
						+"dcrs_remarks = 'NFS-ISS-GENERATED-TTUM-2' and "
						+ "to_date(tran_date,'dd/mm/yyyy') = to_date('"+beanObj.getLocalDate()
						+"','dd/mm/yyyy') -- and filedate = (select max(filedate) from settlement_nfs_iss_cbs)";
				
				getJdbcTemplate().execute(updateQuery);
						
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
			{
				deleteQuery = "delete from ttum_nfs_iss_switch_nainital  WHERE tran_date = to_date('"
						+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				getJdbcTemplate().execute(deleteQuery);
				
				updateQuery = "update settlement_nfs_iss_switch set dcrs_remarks = 'NFS-ISS-UNRECON-2' where "
						+"dcrs_remarks = 'NFS-ISS-GENERATED-TTUM-2' and "
						+ "to_date(local_date,'dd/mm/yyyy') = to_date('"+beanObj.getLocalDate()
						+"','dd/mm/yyyy') -- and filedate = (select max(filedate) from settlement_nfs_iss_switch)";
				getJdbcTemplate().execute(updateQuery);
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("LATEREV"))
			{
				updateQuery = "update nfs_rev_acq_report t3 set dcrs_remarks = 'UNMATCHED' "
						+ "WHERE  acq != 'DLB' "+ 
			            " AND to_date(trasn_date ,'dd-mm-yyyy') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')"+
			            " and rrn in (select acquirer_reference_data from ttum_nfs_iss_nfs"
			            +" where tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') and part_tran_type = 'C')";
				
				getJdbcTemplate().execute(updateQuery);
				
				deleteQuery = "delete from ttum_nfs_iss_nfs  WHERE tran_date = to_date('"
						+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				getJdbcTemplate().execute(deleteQuery);
				
				
				
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("NIH"))
			{
				deleteQuery = "delete from ttum_nfs_acq_nfs  WHERE tran_date = to_date('"
						+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				getJdbcTemplate().execute(deleteQuery);
				
				updateQuery = "update settlement_nfs_acq_nfs set dcrs_remarks = 'NFS-ACQ-UNRECON-2' where "
						+"dcrs_remarks = 'NFS-ACQ-GENERATED-TTUM-2' and "
						+ " to_date(transaction_date,'yyyymmdd')  = to_date('"+beanObj.getLocalDate()
						+"','dd/mm/yyyy') -- and filedate = (select max(filedate) from settlement_nfs_acq_nfs)";
				getJdbcTemplate().execute(updateQuery);
				
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("RUPAYONUS"))
			{
				deleteQuery = "delete from ttum_nfs_onus_cbs  WHERE tran_date = to_date('"
						+beanObj.getLocalDate()+"','dd/mm/yyyy')";
				getJdbcTemplate().execute(deleteQuery);
				
				updateQuery = "update settlement_nfs_iss_cbs set dcrs_remarks = 'NFS-RUPAY-ONUS' where "
						+"dcrs_remarks = 'NFS-RUPAY-POS-GENERATED-TTUM' and "
						+ " to_date(substr(tran_date,1,8),'ddmmyyyy')  = to_date('"+beanObj.getLocalDate()
						+"','dd/mm/yyyy') -- and filedate = (select max(filedate) from settlement_nfs_acq_nfs)";
				getJdbcTemplate().execute(updateQuery);
				
			}
			
		}
		catch(Exception e)
		{
			logger.info("Exception in NFSSettVoucherRollback "+e);
			return false;
			
		}
		return true;
	}

	@Override
	public List<String> getNFSTTUMDataForTXT(UnMatchedTTUMBean beanObj) {
		
		List<String> Data = new ArrayList<String>();
		String ttum_tableName = "";
		String getData  = "";
		String dom_fetch_condition = "";
		String int_fetch_condition = "";
		String sett_table = "";
		String datepass = genetalUtil.DateFunction(beanObj.getLocalDate());
		
		try
		{
			if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
			{
					ttum_tableName = "ttum_nfs_iss_cbs_nainital";
//					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
					dom_fetch_condition = " WHERE filedate = '"+datepass+"' ORDER BY TXN_TYPE DESC  ";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
			{
					ttum_tableName = "ttum_nfs_iss_switch_nainital";
					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("LATEREV"))
			{
				ttum_tableName = "ttum_nfs_iss_nfs";
					dom_fetch_condition = " WHERE to_Date(tran_date,'DD/MM/YYYY') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getAcqtypeOfTTUM().equals("NIH"))
			{
				ttum_tableName = "ttum_nfs_acq_nfs";
					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("RUPAYONUS"))
			{
				ttum_tableName = "ttum_nfs_onus_cbs";
					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			
			
//			 getData = "select concat('1',date_format(sysdate(),'%Y%m%d')) as a from dual "
//					+" union all \n"
//					+" select concat(rpad(case when length(account_number) < 15 "
//					+" then concat('203', account_number)  else"
//					+" concat('201', account_number) end ,19,' '),"//'0',substr(account_number,1,5),"
//					+ "case when length(account_number) < 15 then '00999' "				
//					+" else lpad(substr(account_number,1,4),5,'0') end,"
//					+ "case when length(account_number) < 15 then '01005' "
//					+" else '01408' end,"
//					+" date_format(sysdate(),'%Y%m%d'),'C',date_format(sysdate(),'%Y%m%d'),'00101',"
//					+" lpad(round(transaction_amount*100),14,'0'),"
//					+" lpad(round(transaction_amount*100),14,'0'),'000000010000000000000',"
//					+"	'           ', rpad(transaction_particular,501,' '),'300000000000N',"
//					+ "lpad(account_number,16,'0'))  "    
//					+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'C'"
//					+" UNION ALL \n"		 
//					+" select concat(rpad(case when length(account_number) < 15 "
//					+" then concat('203', account_number)  else "
//					+" concat('201', account_number) end ,19,' '),"//'009990',"
//					//+ "'01408',"
//					+ "case when length(account_number) < 15 then '00999' "				
//					+" else lpad(substr(account_number,1,4),5,'0') end,"
//					+ "case when length(account_number) < 15 then '01005' "				
//					+" else '01008' end,"
//					+" date_format(sysdate(),'%Y%m%d'),'D',date_format(sysdate(),'%Y%m%d'),'00101',"
//					+" lpad(round(transaction_amount*100),14,'0'),"
//					+" lpad(round(transaction_amount*100),14,'0'),'000000010000000000000',"
//					+" '           ', rpad(transaction_particular,501,' '),'300000000000N',"
//					+ "lpad(account_number,16,'0'))"
//					+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'D'"
//					+" UNION ALL "		 
//					+" select  concat('3',(select lpad(count(1),9,'0') "
//					+" from ttum_visa_sur_cbs WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') "
//						+" and tran_type = '"+beanObj.getTypeOfTTUM()+"' and part_tran_type = 'D'),"
//						+ "(select lpad(round((sum(transaction_amount))*100),15,'0') "
//						+" from ttum_visa_sur_cbs WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') " 
//						+" and tran_type = '"+beanObj.getTypeOfTTUM()+"' and part_tran_type = 'D'),"					
//					+" lpad(count(1),9,'0'),lpad(round((sum(transaction_amount))*100),15,'0')) "
//					+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'C'";
			
//			getData = "select account_number as account_number,part_tran_type,"
//					+ "transaction_amount,"
//					+ "transaction_particular ,nvl(reference_number,' ') AS remarks"
//					+ ",filedate from "+ttum_tableName
//					+dom_fetch_condition;
			
//			getData = "select accountid,from_account,txn_type,"
//					+ "amount,"
//					+ "narration "
//					+ ",to_date(filedate,'dd/mm/yyyy') AS filedate from "+ttum_tableName
//					+dom_fetch_condition;
			
		//	(ACCOUNTID||'INR'||FROM_ACCOUNT||'     '|| TXN_TYPE||'          '||AMOUNT||NARRATION)
			
			getData = "select (LPAD(ACCOUNTID,'16',' ')||'INR'||FROM_ACCOUNT||'     '|| TXN_TYPE||'      '||LPAD(AMOUNT,11,' ')||replace(NARRATION,'-',''))"
					+ " from "+ttum_tableName
			+dom_fetch_condition;
			
			//getData = "select * from  "+ ttum_tableName ;
			
			logger.info("getData  TEXT QUERY is "+getData);
			

			Data= getJdbcTemplate().query(getData, new Object[] {}, new ResultSetExtractor<List<String>>(){
				public List<String> extractData(ResultSet rs)throws SQLException {
					List<String> beanList = new ArrayList<String>();
					
					while (rs.next()) {
						
						beanList.add(rs.getString(1));
//						beanList.add(rs.getString(2));
//						beanList.add(rs.getString(3));
//						beanList.add(rs.getString(4));
//						beanList.add(rs.getString(5));

						 
					}
					return beanList;
				}
			});
			
			return Data;
		}
		catch(Exception e)
		{
			System.out.println("Exception in getInterchangeData "+e);
			return null;

		}		
	}			
}
