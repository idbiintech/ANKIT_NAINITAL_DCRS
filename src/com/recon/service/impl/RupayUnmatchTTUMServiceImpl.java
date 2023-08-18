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

import com.recon.model.UnMatchedTTUMBean;
import com.recon.service.RupayUnmatchTTUMService;
import com.recon.util.GeneralUtil;

public class RupayUnmatchTTUMServiceImpl extends JdbcDaoSupport implements RupayUnmatchTTUMService {
	
	@Autowired
	GeneralUtil generalUtil;
	
	private static final Logger logger = Logger.getLogger(RupayUnmatchTTUMServiceImpl.class);
	private static final String O_ERROR_MESSAGE = "o_error_message";
	
	@Override
	public HashMap<String,Object> checkTTUMProcessed(UnMatchedTTUMBean beanObj)
	{
		String passdate = generalUtil.DateFunction(beanObj.getLocalDate());
		String query = "select count(1) from ";
		HashMap<String,Object> output = new HashMap<>();
		try
		{
			if(beanObj.getTypeOfTTUM().equals("SURCHARGE"))
			{
				query = query+" ttum_rupay_sur_cbs where filedate = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') "
						+" and tran_type = 'SURCHARGE'";
			}
			else if(beanObj.getTypeOfTTUM().equals("WAIVER"))
			{
				query = query+" ttum_rupay_sur_cbs where filedate = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') "
						+" and tran_type = 'WAIVER'";
			}
			else if(beanObj.getTypeOfTTUM().equals("FAILED"))
			{
				query = query+" ttum_rupay_dom_cbs where filedate  = '"+passdate+"' ";
						
			}
			else if(beanObj.getTypeOfTTUM().equals("UNRECON"))
			{
				query = query+" ttum_rupay_dom_switch where tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			}
			
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
		String passdate = generalUtil.DateFunction(beanObj.getLocalDate());
		HashMap<String,Object> output = new HashMap<>();
		String query = "Select count(1) from ";
		try
		{
			//1. check whether recon date is greater than selected tran date
			query = query + "settlement_rupay_dom_cbs where filedate >= to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
						
			
				
			logger.info("query is "+query);
			
			int recordCount = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
			
			if(recordCount > 0)
			{
				//2. check whether data is present for processing
				if(beanObj.getTypeOfTTUM().equalsIgnoreCase("SURCHARGE"))
				{
					query = "select count(1) from settlement_rupay_dom_cbs where filedate = '"+beanObj.getLocalDate()+"'"
							+" and dcrs_remarks = 'RUPAY_SUR-MATCHED-3' and relax_param = 'Y' "
							/*+" and str_to_date(substring(tran_date,1,8),'%d%m%Y') = "
							+ "str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') "*/
							+" and  (mcc not in ('5541','5542','5983') "
							+"or (mcc in ('5541','5542','5983') and substr(remarks,1,6) = '436359')) "
							+ " and sur_amount != 0";
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("WAIVER"))
				{
					query = "select count(1) from settlement_rupay_dom_cbs where filedate = '"+beanObj.getLocalDate()+"'"
							+" and dcrs_remarks = 'RUPAY_SUR-MATCHED-3' and relax_param = 'Y' "
							/*+" and str_to_date(substring(tran_date,1,8),'%d%m%Y') = "
							+ "str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') "*/
							+" and mcc in ('5541','5542','5983')  and substr(remarks,1,6) != '436359'"
							+ " and sur_amount != 0";
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
				{
					query = "select count(1) from settlement_rupay_dom_cbs where filedate = '"+passdate+"' "
							+" AND dcrs_remarks LIKE '%RUPAY_DOM-FAILED%'  "
							+"and to_date(tran_date,'yyyy-mm-dd') = to_date('"+passdate+"','dd-MON-yyyy') and TXN_INDCTR = 'D' ";
					
					System.out.println("query for failed of ECOM-POS"+ query);
				}
				else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
				{
					query = "select count(1) from settlement_rupay_dom_switch where filedate = (select max(filedate) from settlement_rupay_dom_cbs) "
							+" AND dcrs_remarks = 'RUPAY_DOM-UNRECON-2' and respcode = '000' "
							+"and to_date(local_date,'dd/mm/yyyy') = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
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
			}
			else
			{
				output.put("result", false);
				output.put("msg", "Tran date is greater than recon date");
			}
			
			
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
			String passdate = generalUtil.DateFunction(beanObj.getLocalDate());
			//String datepass = generalUtil.DateFunction(beanObj.getFileDate()) ;
			
			UnmatchedTTUMProc rollBackexe = new UnmatchedTTUMProc(getJdbcTemplate());
			inParams.put("filedt", passdate);
			inParams.put("user_id", beanObj.getCreatedBy()); 
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
		private static final String insert_proc = "rupay_unmatch_ttum_process";
		public UnmatchedTTUMProc(JdbcTemplate jdbcTemplate)
		{
			//String passmonth = generalUtil.DateFunction()
			super(jdbcTemplate,insert_proc);
			setFunction(false);
			declareParameter(new SqlParameter("filedt",Types.VARCHAR));
			declareParameter(new SqlParameter("user_id",Types.VARCHAR));
			declareParameter(new SqlParameter("ttumtype",Types.VARCHAR));
			declareParameter(new SqlParameter("subcategory",Types.VARCHAR));
			declareParameter(new SqlParameter("localdt",Types.VARCHAR));
			declareParameter(new SqlOutParameter(O_ERROR_MESSAGE, Types.VARCHAR));
			compile();
		}

	}
	
	@Override
	public List<Object> getRupayTTUMData(UnMatchedTTUMBean beanObj)
	{
		String ttum_tableName = null;
		List<Object> data = new ArrayList<Object>();
		String dom_fetch_condition = "";
		String int_fetch_condition = "";
		String sett_table = "";
		String datepass =  generalUtil.DateFunction(beanObj.getLocalDate());
	//	String ttum_format = null; 
		try
		{
			
			if(beanObj.getTypeOfTTUM().equalsIgnoreCase("LP"))
			{
				
					ttum_tableName = "TTUM_VISA_ISS_VISA";
					sett_table = "SETTLEMENT_VISA_"+beanObj.getStSubCategory().substring(0, 3)+"_VISA";
					dom_fetch_condition = " WHERE TRAN_DATE = '"+beanObj.getLocalDate()+"'"+// AND FILEDATE = (SELECT TO_DATE(MAX(FILEDATE),'DD/MM/YY') FROM "+sett_table+") "+
							"  and tran_type = 'DOMESTIC'";
			}			
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("SURCHARGE"))
			{
					ttum_tableName = "ttum_rupay_sur_cbs";
					dom_fetch_condition = " WHERE filedate = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy') and tran_type = 'SURCHARGE'";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("WAIVER"))
			{
					ttum_tableName = "ttum_rupay_sur_cbs";
					dom_fetch_condition = " WHERE filedate = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')  and tran_type = 'WAIVER'";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
			{
					ttum_tableName = "ttum_rupay_dom_cbs";
//					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')  ";
					System.out.println("date is "+ datepass);
					dom_fetch_condition = " WHERE filedate = '"+ datepass +"'  ";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
			{
					ttum_tableName = "ttum_rupay_dom_switch";
					dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')";
			}
			else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("REVERSAL"))
			{
				ttum_tableName = "TTUM_VISA_ISS_rev_VISA";
					sett_table = "SETTLEMENT_VISA_"+beanObj.getStSubCategory().substring(0, 3)+"_visa";
					
					dom_fetch_condition = " WHERE TRAN_TYPE = 'DOMESTIC' AND FILEDATE = '"+beanObj.getFileDate()+"'";
					int_fetch_condition = " WHERE TRAN_TYPE = 'INTERNATIONAL' AND FILEDATE = '"+beanObj.getFileDate()+"'";
			}
			
			String getdomData = null;
			
			getdomData = "select distinct account_number as account_number,part_tran_type,FROM_ACCOUNT,"
					+ "transaction_amount,"
					+ "transaction_particular ,nvl(reference_number,' ') AS remarks"
					+ " from "+ttum_tableName
					+dom_fetch_condition;
				
			
				logger.info("Getdata query is "+getdomData);
				
				List<Object> DailyData = getJdbcTemplate().query(getdomData, new Object[] {}, new ResultSetExtractor<List<Object>>(){
					public List<Object> extractData(ResultSet rs)throws SQLException {
						List<Object> beanList = new ArrayList<Object>();
						
						while (rs.next()) {
							Map<String, String> table_Data = new HashMap<String, String>();
								table_Data.put("account_number", rs.getString("account_number"));
								table_Data.put("part_tran_type", rs.getString("part_tran_type"));
								table_Data.put("transaction_amount", rs.getString("transaction_amount"));
								table_Data.put("transaction_particular", rs.getString("transaction_particular"));
								table_Data.put("remarks", rs.getString("remarks"));
								//table_Data.put("filedate", rs.getString("filedate"));
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
	    	//String[] filelist = file.list();
	    	String [] filelist = file.list();
 	    			
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
public Boolean RupayTtumRollback(UnMatchedTTUMBean beanObj)
{
	String deleteQuery = null;
	String updateQuery = null;
	try
	{
		if(beanObj.getTypeOfTTUM().equals("SURCHARGE"))
		{
			deleteQuery = "delete from ttum_rupay_sur_cbs where filedate = to_date('"
					+beanObj.getLocalDate()+"','dd/mm/yyyy') "
					+" and tran_type = 'SURCHARGE'";

			getJdbcTemplate().execute(deleteQuery);

			updateQuery = "update settlement_rupay_dom_cbs set dcrs_remarks = 'RUPAY_SUR-MATCHED-3' where "
					+"dcrs_remarks = 'RUPAY_SUR-GENERATED-TTUM-3' and relax_param = 'Y' "
					+" and filedate = to_date('"+beanObj.getLocalDate()
					+"','dd/mm/yyyy') "
					+"and (mcc not in ('5541','5542','5983') "
					+" or (mcc in ('5541','5542','5983') and substr(remarks,1,6) = '436359'))";

			getJdbcTemplate().execute(updateQuery);

		}
		else if(beanObj.getTypeOfTTUM().equals("WAIVER"))
		{
			deleteQuery = "delete from ttum_rupay_sur_cbs where filedate = to_date('"
					+beanObj.getLocalDate()+"','dd/mm/yyyy') "
					+" and tran_type = 'WAIVER'";

			getJdbcTemplate().execute(deleteQuery);

			updateQuery = "update settlement_rupay_dom_cbs set dcrs_remarks = 'RUPAY_SUR-MATCHED-3' where "
					+"dcrs_remarks = 'RUPAY_SUR-GENERATED-TTUM-3' and relax_param = 'Y' "
					+" and filedate = to_date('"+beanObj.getLocalDate()
					+"','dd/mm/yyyy')  "
					+" and mcc in ('5541','5542','5983') and substr(remarks,1,6) != '436359'";

			getJdbcTemplate().execute(updateQuery);

		}
		else if(beanObj.getTypeOfTTUM().equals("FAILED"))
		{
			deleteQuery = "delete from ttum_rupay_dom_cbs where tran_date = to_date('"
					+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
			getJdbcTemplate().execute(deleteQuery);

			updateQuery = "update settlement_rupay_dom_cbs set dcrs_remarks = 'RUPAY_DOM-UNRECON-1 (' where "
					+"dcrs_remarks = 'RUPAY_DOM-GENERATED-TTUM-1' "
					+" and to_date(tran_date,'dd/mm/yyyy') = to_date('"+beanObj.getLocalDate()
					+"','dd/mm/yyyy') and filedate = (select max(filedate) from settlement_rupay_dom_cbs) ";

			getJdbcTemplate().execute(updateQuery);

		}
		else if(beanObj.getTypeOfTTUM().equals("UNRECON"))
		{
			deleteQuery = "delete from ttum_rupay_dom_switch where tran_date = to_date('"
					+beanObj.getLocalDate()+"','dd/mm/yyyy') ";
			getJdbcTemplate().execute(deleteQuery);

			updateQuery = "update settlement_rupay_dom_switch set dcrs_remarks = 'RUPAY_DOM-UNRECON-2' where "
					+"dcrs_remarks = 'RUPAY_DOM-GENERATED-TTUM-2' "
					+" and to_date(local_date,'dd/mm/yyyy')  = to_date('"+beanObj.getLocalDate()
					+"','dd/mm/yyyy') and filedate = (select max(filedate) from settlement_rupay_dom_switch) ";

			getJdbcTemplate().execute(updateQuery);

		}

	}
	catch(Exception e)
	{
		logger.info("Exception in VisaTtumRollback "+e);
		return false;

	}
	return true;
}

@Override
public List<String> getRUPAYTTUMDataForTXT(UnMatchedTTUMBean beanObj) {
	
	List<String> Data = new ArrayList<String>();
	String ttum_tableName = "";
	String getData  = "";
	String dom_fetch_condition = "";
	String int_fetch_condition = "";
	String sett_table = "";
	String datepass = generalUtil.DateFunction(beanObj.getLocalDate());
	
	try
	{
		if(beanObj.getTypeOfTTUM().equalsIgnoreCase("SURCHARGE"))
		{
				ttum_tableName = "ttum_visa_sur_cbs";
				dom_fetch_condition = " WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') and tran_type = 'SURCHARGE'";
		}
		else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("WAIVER"))
		{
				ttum_tableName = "ttum_visa_sur_cbs";
				dom_fetch_condition = " WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d')  and tran_type = 'WAIVER'";
		}
		else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("FAILED"))
		{
//				ttum_tableName = "ttum_visa_iss_cbs";
//				dom_fetch_condition = " WHERE tran_date = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d')";
			ttum_tableName = "ttum_rupay_dom_cbs";
//			dom_fetch_condition = " WHERE tran_date = to_date('"+beanObj.getLocalDate()+"','dd/mm/yyyy')  ";
			System.out.println("date is "+ datepass);
			dom_fetch_condition = " WHERE filedate = '"+ datepass +"'   ";
			
		}
		else if(beanObj.getTypeOfTTUM().equalsIgnoreCase("UNRECON"))
		{
				ttum_tableName = "ttum_visa_iss_switch";
				dom_fetch_condition = " WHERE tran_date = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d')";
		}
		
		
//		 getData = "select concat('1',date_format(sysdate(),'%Y%m%d')) as a from dual "
//				+" union all \n"
//				+" select concat(rpad(case when length(account_number) < 15 "
//				+" then concat('203', account_number)  else"
//				+" concat('201', account_number) end ,19,' '),"//'0',substr(account_number,1,5),"
//				+ "case when length(account_number) < 15 then '00999' "				
//				+" else lpad(substr(account_number,1,4),5,'0') end,"
//				+ "case when length(account_number) < 15 then '01005' "
//				+" else '01408' end,"
//				+" date_format(sysdate(),'%Y%m%d'),'C',date_format(sysdate(),'%Y%m%d'),'00101',"
//				+" lpad(round(transaction_amount*100),14,'0'),"
//				+" lpad(round(transaction_amount*100),14,'0'),'000000010000000000000',"
//				+"	'           ', rpad(transaction_particular,501,' '),'300000000000N',"
//				+ "lpad(account_number,16,'0'))  "    
//				+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'C'"
//				+" UNION ALL \n"		 
//				+" select concat(rpad(case when length(account_number) < 15 "
//				+" then concat('203', account_number)  else "
//				+" concat('201', account_number) end ,19,' '),"//'009990',"
//				//+ "'01408',"
//				+ "case when length(account_number) < 15 then '00999' "				
//				+" else lpad(substr(account_number,1,4),5,'0') end,"
//				+ "case when length(account_number) < 15 then '01005' "				
//				+" else '01008' end,"
//				+" date_format(sysdate(),'%Y%m%d'),'D',date_format(sysdate(),'%Y%m%d'),'00101',"
//				+" lpad(round(transaction_amount*100),14,'0'),"
//				+" lpad(round(transaction_amount*100),14,'0'),'000000010000000000000',"
//				+" '           ', rpad(transaction_particular,501,' '),'300000000000N',"
//				+ "lpad(account_number,16,'0'))"
//				+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'D'"
//				+" UNION ALL "		 
//				+" select  concat('3',(select lpad(count(1),9,'0') "
//				+" from ttum_visa_sur_cbs WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') "
//					+" and tran_type = '"+beanObj.getTypeOfTTUM()+"' and part_tran_type = 'D'),"
//					+ "(select lpad(round((sum(transaction_amount))*100),15,'0') "
//					+" from ttum_visa_sur_cbs WHERE filedate = str_to_date('"+beanObj.getLocalDate()+"','%Y/%m/%d') " 
//					+" and tran_type = '"+beanObj.getTypeOfTTUM()+"' and part_tran_type = 'D'),"					
//				+" lpad(count(1),9,'0'),lpad(round((sum(transaction_amount))*100),15,'0')) "
//				+" from "+ttum_tableName+dom_fetch_condition+" and part_tran_type = 'C'";
		
//		getData = "select DISTINCT account_number as account_number,CURRENCY_CODE ,part_tran_type,"
//				+ "transaction_amount,"
//				+ "transaction_particular ,nvl(reference_number,' ') AS remarks "
//				+ " from "+ttum_tableName
//				+dom_fetch_condition;
		
		getData =	"select DISTINCT (LPAD(ACCOUNT_NUMBER,'16',' ')||CURRENCY_CODE||FROM_ACCOUNT||'     '||PART_TRAN_TYPE||'    '||LPAD(TRANSACTION_AMOUNT,10,' ')|| transaction_particular)"
		           + " from " +ttum_tableName 
		           + dom_fetch_condition +"  " ;
		
		// and part_tran_type = 'D'
		
		System.out.println("query of ttum is"+getData);
		
		
		//getData = "select * from  "+ ttum_tableName ;
		
		logger.info("getData is "+getData);
		

		Data= getJdbcTemplate().query(getData, new Object[] {}, new ResultSetExtractor<List<String>>(){
			public List<String> extractData(ResultSet rs)throws SQLException {
				List<String> beanList = new ArrayList<String>();
				
				while (rs.next()) {
					beanList.add(rs.getString(1));
//					beanList.add(rs.getString(2));
//					beanList.add(rs.getString(3));
//					beanList.add(rs.getString(4));
//					beanList.add(rs.getString(5));

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
