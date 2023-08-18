package com.recon.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.recon.dao.DataCountDao;
import com.recon.dao.RawFileRollbackDao;
import com.recon.model.NFSSettlementBean;



public class DataCountDaoImpl extends JdbcDaoSupport implements DataCountDao {

	@Override
	public Map<String, Object> CountDateValidate(NFSSettlementBean beanObj) {
		 
		Map<String, Object> output = new HashMap<>();
		int count = 0;
		String query = "";
		String tableName = "";
		try
		{
			System.out.println("checking category :" +beanObj.getCategory());
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
	 
	@Override
	public boolean DataCount(NFSSettlementBean beanObj) {
 
		
		return false;
	}

	 
}
