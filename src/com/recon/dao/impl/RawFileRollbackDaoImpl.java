package com.recon.dao.impl;

import static com.recon.util.GeneralUtil.GET_FILE_ID;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.web.multipart.MultipartFile;

import com.recon.control.ReconProcess;
import com.recon.dao.CompareDao;
import com.recon.dao.ManualKnockoffDao;
import com.recon.dao.RawFileRollbackDao;
import com.recon.dao.SettlementRollbackDao;
import com.recon.model.FileSourceBean;
import com.recon.model.KnockOffBean;
import com.recon.model.ManualKnockoffBean;
import com.recon.model.NFSSettlementBean;

public class RawFileRollbackDaoImpl extends JdbcDaoSupport implements RawFileRollbackDao{

	private static final Logger logger = Logger.getLogger(RawFileRollbackDaoImpl.class);
	private static final String O_ERROR_CODE = "o_error_code";
	private static final String O_ERROR_MESSAGE = "o_error_message";

	public Map<String, Object> RawFileDateValidate(NFSSettlementBean beanObj)
	{
		Map<String, Object> output = new HashMap<>();
		int count = 0;
		String query = "";
		String tableName = "";
		try
		{
			if(beanObj.getStSubCategory().equalsIgnoreCase("ISSUER"))
			{
				tableName = beanObj.getFileName().toLowerCase()+"_"+beanObj.getFileName().toLowerCase()+"_iss_rawdata";
			}
			else if(beanObj.getStSubCategory().equals("ACQUIRER"))
			{
				tableName = beanObj.getFileName().toLowerCase()+"_"+beanObj.getFileName().toLowerCase()+"_acq_rawdata";
			}
			logger.info("table name is "+tableName);
			String checkFileDate = "select count(1) from "+tableName+" where filedate > '"+beanObj.getDatepicker()+"'";
			
			int records_count = getJdbcTemplate().queryForObject(checkFileDate, new Object[]{}, Integer.class);

			if(records_count == 0)
			{
				//check whether selected date and cycle raw file is uplaoded or not
				String checkRecords = "select count(1) from "+tableName+" where filedate = to_date(?,'dd/mm/yyyy') "
						+" and cycle = ?";
				records_count = getJdbcTemplate().queryForObject(checkRecords, new Object[]{beanObj.getDatepicker(),
						beanObj.getCycle()}, Integer.class);
				
				if(records_count >0)
					output.put("result", true);
				else
				{
					output.put("result"	,false);
					output.put("msg", "File is not uploaded");
				}
				
			}
			else
			{
				output.put("result", false);
				output.put("msg", "Future date raw file is uploaded");
			}
		}
		catch(Exception e)
		{
			logger.info("Exception in RawFileDateValidate "+e);
			output.put("result", false);
			output.put("msg", "Exception Occurred while validating");
		}
		return output;

	}

	public Map<String, Object> ReconValidate(NFSSettlementBean beanObj)
	{
		Map<String, Object> output = new HashMap<>();
		int count = 0;
		String query = "";
		String tableName = "";
		try
		{
			query = "select count(1) from main_file_upload_dtls where filedate = to_date(?,'dd/mm/yyyy') and "
					+"comapre_flag = 'Y' AND fileid = (select fileid from main_filesource where filename = '"+beanObj.getFileName()+"' "
					+"and file_subcategory = '"+beanObj.getStSubCategory()+"')";
			int recordCount = getJdbcTemplate().queryForObject(query, new Object[]{beanObj.getDatepicker()},Integer.class);
			
			if(recordCount == 0)
			{
				output.put("result", true);
			}
			else
			{
				output.put("result", false);
				output.put("msg", "Recon is already processed for selected date");
			}
		}
		catch(Exception e)
		{
			logger.info("Exception in RawFileDateValidate "+e);
			output.put("result", false);
			output.put("msg", "Exception Occurred while validating");
		}
		
		return output;

	}	
	
public boolean CashnetRawFileRollback(NFSSettlementBean beanObj)
{
	String tableName = "";
	try
	{
		
		if(beanObj.getStSubCategory().equalsIgnoreCase("ISSUER"))
		{
			tableName = beanObj.getFileName().toLowerCase()+"_"+beanObj.getFileName().toLowerCase()+"_iss_rawdata";
		}
		else if(beanObj.getStSubCategory().equals("ACQUIRER"))
		{
			tableName = beanObj.getFileName().toLowerCase()+"_"+beanObj.getFileName().toLowerCase()+"_acq_rawdata";
		}
		
		String query = "delete from "+tableName+" where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and "
					+"cycle = '"+beanObj.getCycle()+"'";
		getJdbcTemplate().execute(query);
		
		query = "select file_count from main_file_upload_dtls where filedate = to_date('"+beanObj.getDatepicker()+"',"
				+ "'dd/mm/yyyy') and fileid = (select fileid from main_filesource where filename = '"+beanObj.getFileName()
				+"' AND "
				+ "file_subcategory = '"+beanObj.getStSubCategory()+"')";
		
		int file_count = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
		
		if(file_count == 1)
		{
			query = "delete from main_file_upload_dtls where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') "
					+" and fileid = (select fileid from main_filesource where filename = '"+beanObj.getFileName()+"' and "
					+"file_subcategory = '"+beanObj.getStSubCategory()+"')";
			getJdbcTemplate().execute(query);
		}
		else
		{
			query = "update main_file_upload_dtls set file_count = '"+(file_count-1)
					+"' where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') "
					+" and fileid = (select fileid from main_filesource where filename = '"+beanObj.getFileName()+"' and "
					+"file_subcategory = '"+beanObj.getStSubCategory()+"')";
			getJdbcTemplate().execute(query);
		}
		
	}
	catch(Exception e)
	{
		logger.info("Exception while deleting raw file "+e);
		return false;
	}
	return true;
}

 
 

public Map<String, Object> VisaRawFileDateValidate(NFSSettlementBean beanObj)
{
	Map<String, Object> output = new HashMap<>();
	int count = 0;
	String query = "";
	String tableName = "VISA_VISA_RAWDATA".toLowerCase();
	
	String condition = "and MERCHANT_COUNTRY_CODE not in ('IN')" ;
	String condition1 = "and MERCHANT_COUNTRY_CODE  in ('IN')"  ;
	try
	{
		if(beanObj.getStSubCategory().equalsIgnoreCase("INTERNATIONAL"))
//			{
//				
 		    query = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+ " " +condition;
//			
//			}
//			else if(beanObj.getStSubCategory().equals("DOMESTIC"))
//			{
//				
 				query = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+" " +condition1; 
//			} 
		logger.info("table name is "+tableName);
		String checkFileDate = "select count(1) from "+tableName+" where filedate > '"+beanObj.getDatepicker()+"'";

		int records_count = getJdbcTemplate().queryForObject(checkFileDate, new Object[]{}, Integer.class);

		if(records_count == 0)
		{
			//check whether selected date and cycle raw file is uplaoded or not
			String checkRecords = "select count(1) from "+tableName+" where filedate = to_date(?,'dd/mm/yyyy') ";
			records_count = getJdbcTemplate().queryForObject(checkRecords, new Object[]{beanObj.getDatepicker()
					 }, Integer.class);
			
			if(records_count >0)
				output.put("result", true);
			else
			{
				output.put("result"	,false);
				output.put("msg", "File is not uploaded");
			}
			
		}
		else
		{
			output.put("result", false);
			output.put("msg", "Future date raw file is uploaded");
		}
	}
	catch(Exception e)
	{
		logger.info("Exception in RawFileDateValidate "+e);
		output.put("result", false);
		output.put("msg", "Exception Occurred while validating");
	}
	return output;

}


 
public boolean VisaRawFileRollback(NFSSettlementBean beanObj)
{
	int count = 0;
	String query = "";
	String que = " ";
	String tableName = "VISA_VISA_RAWDATA".toLowerCase();
	
	String checkRecords = "select count(1) from "+tableName+" where filedate = to_date(?,'dd/mm/yyyy')" ;
	String checkFileDate = "select count(1) from "+tableName+" where filedate > '"+beanObj.getDatepicker()+"'";

	String condition = " and (MERCHANT_COUNTRY_CODE  is null or  MERCHANT_COUNTRY_CODE   not in ('IN') ) " ;
	String condition1 = "and MERCHANT_COUNTRY_CODE  in ('IN')"  ;
	
	

	try {
	
	if(beanObj.getStSubCategory().equalsIgnoreCase("INTERNATIONAL")) {
		
		query = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+ " " +condition;
	}
	
	else {
		
		query = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+" " +condition1; 
	}
	 getJdbcTemplate().execute(query);

	 
	 
	 que = "select distinct file_count from MAIN_FILE_UPLOAD_DTLS where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') "
			    + "and CATEGORY = 'VISA' ";
					
	 int file_count = getJdbcTemplate().queryForObject(que, new Object[]{},Integer.class);
					
     if(file_count == 1)
		{
			query = "delete from main_file_upload_dtls where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' ";
								 
			
		}
	else
		{
			query = "update main_file_upload_dtls set file_count = '"+(file_count-1)
								+"' where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' " ;
		}	
	 
     getJdbcTemplate().execute(query);
	
	
	}
	catch(Exception e)
	{
		logger.info("Exception in VisaRawFileRollback "+e);
		 
	}
	return true;
}



/*try
{
	System.out.println("here");

	logger.info("table name is "+tableName);

	int records_count = getJdbcTemplate().queryForObject(checkFileDate, new Object[]{}, Integer.class);

	if(records_count == 0)
	{
		 
		System.out.println("here 2");

		records_count = getJdbcTemplate().queryForObject(checkRecords, new Object[]{beanObj.getDatepicker()}, Integer.class);
				
		
		if(records_count > 1 &&  beanObj.getStSubCategory().equalsIgnoreCase("INTERNATIONAL"))
		{
		   que = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+ " " +condition;
		    
           query = "select  distinct file_count from MAIN_FILE_UPLOAD_DTLS where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' ";
			
		int file_count = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
			
			if(file_count == 1)
			{
				query = "delete from main_file_upload_dtls where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' ";
						 
				getJdbcTemplate().execute(query);
			}
			else
			{
				query = "update main_file_upload_dtls set file_count = '"+(file_count-1)
						+"' where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' " ;
						 
				getJdbcTemplate().execute(query);
			}	    
		    
 			System.out.println("here 5" );

			
		}
		else if(records_count > 1 &&  beanObj.getStSubCategory().equalsIgnoreCase("DOMESTIC")) {
		
			

		que = "delete from VISA_VISA_RAWDATA where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy')"+" " +condition1; 
			
        query = "select distinct file_count from MAIN_FILE_UPLOAD_DTLS where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') "
	    + "and CATEGORY = 'VISA' ";
			
			int file_count = getJdbcTemplate().queryForObject(query, new Object[]{},Integer.class);
			
			if(file_count == 1)
			{
				query = "delete from main_file_upload_dtls where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' ";
						 
				getJdbcTemplate().execute(query);
			}
			else
			{
				query = "update main_file_upload_dtls set file_count = '"+(file_count-1)
						+"' where filedate = to_date('"+beanObj.getDatepicker()+"','dd/mm/yyyy') and CATEGORY = 'VISA' " ;
						 
				getJdbcTemplate().execute(query);
			}	

			System.out.println("here 6" );
		}
		   getJdbcTemplate().execute(query);
		   
		   getJdbcTemplate().execute(que);
	}
	 
}*/ 











}
