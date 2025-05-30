package com.recon.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.lowagie.text.pdf.codec.postscript.ParseException;
import com.recon.model.SettlementBean;
import com.recon.model.TTUMBean;
import com.recon.model.UnMatchedTTUMBean;
import com.recon.service.impl.RupayTTUMServiceImpl;

public class GenerateDLBVoucher {
	
	private static final Logger logger = Logger.getLogger(GenerateDLBVoucher.class);
	
	public String checkAndMakeDirectory(String fileDate, String category)
	{
		SimpleDateFormat sdf;
		java.util.Date date;
		String stnewDate = "";
		try
		{
			String stPath =  System.getProperty("java.io.tmpdir");//+beanObj.getCategory()+File.separator+stnewDate;
			
			try
			{
				sdf = new SimpleDateFormat("yyyy-mm-dd");
				date = sdf.parse(fileDate);
				sdf = new SimpleDateFormat("dd-MM-yyyy");

				stnewDate = sdf.format(date);
				
			}
			catch(java.text.ParseException parseExep)
			{
				logger.info("Exception while parsing date "+fileDate);
				sdf = new SimpleDateFormat("yyyy/mm/dd");
				date = sdf.parse(fileDate);
				sdf = new SimpleDateFormat("dd-MM-yyyy");

				stnewDate = sdf.format(date);
				
			}
			catch(Exception excp)
			{
				logger.info("Exception while parsing Date"+excp);
			}
			
			//1. Delete folder
			try
			{
				logger.info("Path is "+stPath+File.separator+category);
				File checkFile = new File(stPath+File.separator+category);
				if(checkFile.exists())
					FileUtils.forceDelete(new File(stPath+File.separator+category));

				//2. check whether category folder is there or not
				File directory = new File(stPath+File.separator+category);
				if(!directory.exists())
				{
					directory.mkdir();
				}
				directory = new File(stPath+File.separator+category+File.separator+stnewDate);

				if(!directory.exists())
				{
					directory.mkdir();
				}

				return (stPath+File.separator+category+File.separator+stnewDate);
			}
			catch(IOException ioExcep)
			{
				int fileCount = 0;
				logger.info("Path is "+stPath+File.separator+category);
				File checkFile = new File(stPath+File.separator+category);
				if(checkFile.exists())
				{
					String[] files = checkFile.list();
					logger.info("Number if files are "+files.length);
					fileCount = files.length;
				}
				logger.info("New Path is "+stPath+File.separator+category+"_"+(fileCount+1));
				//2. check whether category folder is there or not
				File directory = new File(stPath+File.separator+category+"_"+(fileCount+1));
				if(!directory.exists())
				{
					directory.mkdir();
				}
				directory = new File(stPath+File.separator+category+"_"+(fileCount+1)+File.separator+stnewDate);

				if(!directory.exists())
				{
					directory.mkdir();
				}

				return (stPath+File.separator+category+"_"+(fileCount+1)+File.separator+stnewDate);
			
			}
			
		}
		catch(Exception e)
		{
			logger.info("Exception in checkAndMakeDirectory "+e);
			return "Exception Occured";
		}
	}

	/*public void generateTTUMFile(String stPath, String FileName,List<Object> TTUMData )
	{
		StringBuffer lineData;
		try
		{
			
			File file = new File(stPath+File.separator+FileName);
			if(file.exists())
			{
				FileUtils.forceDelete(file);
			}
				file.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+FileName, true));
			int startLine = 0;
			//LOGIC TO CREATE TTUM DATA
			for(int i =0 ;i<TTUMData.size(); i++)
			{
				Map<String, String> table_Data = (Map<String, String>) TTUMData.get(i);
				
				if(startLine > 0)
				{
					out.write("\n");
				}
				startLine++;
				lineData = new StringBuffer();
				lineData.append(table_Data.get("ACCOUNT_NUMBER")+"  "+"INR1735"+"    "+table_Data.get("PART_TRAN_TYPE"));
				lineData.append(table_Data.get("TRANSACTION_AMOUNT")+table_Data.get("TRANSACTION_PARTICULAR")+"         "+table_Data.get("REMARKS"));
				lineData.append("                                                                          ");
				lineData.append(table_Data.get("FILEDATE"));
				//logger.info(lineData.toString());
				out.write(lineData.toString());	
					
			}
			
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			logger.info("Exception in generateTTUMFile "+e );
			
		}
		
	}*/
	
	public void generateTTUMFile(String stPath, String FileName,List<String> TTUMData )
	{
		StringBuffer lineData;
		try
		{
			checkNcreateFolder(stPath, new ArrayList<String>());
			
			File file = new File(stPath+File.separator+FileName);
			if(file.exists())
			{
				FileUtils.forceDelete(file);
			}
				file.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+FileName, true));
			int startLine = 0;
			//LOGIC TO CREATE TTUM DATA
			for(String data :TTUMData)
			{
				if(startLine > 0)
				{
					out.write("\n");
				}
				startLine++;
				lineData = new StringBuffer();
				lineData.append(data);
				
				out.write(lineData.toString());	
			}
			
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			logger.info("Exception in generateTTUMFile "+e );
			
		}
		
	}
	
	
	public boolean checkNcreateFolder(String settlementBeanObj, List<String> stFileNames) {
		try {
			File directory = new File(settlementBeanObj);

			if (!directory.exists()) {
				directory.mkdirs();
			}
			/*
			 * Date date = new Date(settlementBeanObj.getDatepicker()); SimpleDateFormat sdf
			 * = new SimpleDateFormat("dd-MM-yy"); String formatteddate = sdf.format(date);
			 * System.out.println("formatteddate is "+formatteddate);
			 */

			// SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM-yyyy");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");

			//java.util.Date date = sdf.parse(settlementBeanObj.getDatepicker());

			sdf = new SimpleDateFormat("dd-mm-yyyy");

			//String stnewDate = sdf.format(date);

			// String stnewPath = settlementBeanObj.getStPath() + File.separator +
			// stnewDate;

			// settlementBeanObj.setStPath(stnewPath);

			directory = new File(settlementBeanObj);
			String stPath = settlementBeanObj;
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// CHANGES DONE BY INT8624 FOR DUPLICATE RECORDS ISSUE
			logger.info(directory.listFiles() + " size is " + directory.listFiles().length);

			/*
			 * stnewPath = settlementBeanObj.getStPath() + File.separator + stnewDate +
			 * File.separator + settlementBeanObj.getStMergerCategory();
			 */

			/*
			 * if (directory.listFiles() != null && directory.listFiles().length > 0) {
			 * stnewPath = stnewPath + "_" + (directory.listFiles().length + 1);
			 * logger.info("stnewPath is " + stnewPath); } else { stnewPath = stnewPath +
			 * "_" + directory.listFiles().length; logger.info("stnewPath is " + stnewPath);
			 * 
			 * }
			 */

			//settlementBeanObj.setStPath(stPath);

			directory = new File(stPath);
			if (!directory.exists()) {
				directory.mkdirs();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}
	
	/*public void generateMultipleTTUMFiles(String stPath, String FileName,int fileCount,List<Object> TTUMData )
	{
		StringBuffer lineData;
		List<String> files = new ArrayList<>();
		FileInputStream fis;
		
		try
		{
			for(int j = 1; j <= fileCount ; j++)
			{
				List<Object> lstData = (java.util.ArrayList<Object>) TTUMData.get(j-1);
				
				String newFileName = FileName.replaceAll(".txt", "_"+j+".txt");
				
				File file = new File(stPath+File.separator+newFileName);
				if(file.exists())
				{
					FileUtils.forceDelete(file);
				}
				file.createNewFile();

				BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+newFileName, true));
				int startLine = 0;
				//LOGIC TO CREATE TTUM DATA
				for(int i =0 ;i< lstData.size(); i++)
				{
					
						Map<String, String> table_Data = (Map<String, String>) lstData.get(i);

						if(startLine > 0)
						{
							out.write("\n");
						}
						startLine++;
						lineData = new StringBuffer();
						lineData.append(table_Data.get("ACCOUNT_NUMBER")+"  "+"INR1735"+"    "+table_Data.get("PART_TRAN_TYPE"));
						lineData.append(table_Data.get("TRANSACTION_AMOUNT")+table_Data.get("TRANSACTION_PARTICULAR"));//+"         "+table_Data.get("REMARKS"));
						lineData.append("                                                                                                       ");
						lineData.append(table_Data.get("FILEDATE"));
						//logger.info(lineData.toString());
						out.write(lineData.toString());	

				}
				out.flush();
				out.close();
				
				 files.add(stPath+File.separator+newFileName);

			}
			FileOutputStream fos = new FileOutputStream(stPath+File.separator+ "TTUMS.zip");
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
		
	}*/
	public void generateMultipleTTUMFiles(String stPath, String FileName,int fileCount,List<Object> TTUMData )
	{
		StringBuffer lineData;
		//List<String> files = new ArrayList<>();
		FileInputStream fis;
		
		try
		{
			for(int j = 1; j <= fileCount ; j++)
			{
				List<Object> lstData = (java.util.ArrayList<Object>) TTUMData.get(j-1);
				
				String newFileName = FileName.replaceAll(".txt", "_"+j+".txt");
				
				File file = new File(stPath+File.separator+newFileName);
				if(file.exists())
				{
					FileUtils.forceDelete(file);
				}
				file.createNewFile();

				BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+newFileName, true));
				int startLine = 0;
				//LOGIC TO CREATE TTUM DATA
				for(int i =0 ;i< lstData.size(); i++)
				{
					
						Map<String, String> table_Data = (Map<String, String>) lstData.get(i);

						if(startLine > 0)
						{
							out.write("\n");
						}
						startLine++;
						lineData = new StringBuffer();
						lineData.append(table_Data.get("ACCOUNT_NUMBER")+"  "+"INR1735"+"    "+table_Data.get("PART_TRAN_TYPE"));
						lineData.append(table_Data.get("TRANSACTION_AMOUNT")+table_Data.get("TRANSACTION_PARTICULAR"));//+"         "+table_Data.get("REMARKS"));
						lineData.append("                                                                                                       ");
						lineData.append(table_Data.get("FILEDATE"));
						//logger.info(lineData.toString());
						out.write(lineData.toString());	

				}
				out.flush();
				out.close();
				
			//	 files.add(stPath+File.separator+newFileName);

			}
			/*FileOutputStream fos = new FileOutputStream(stPath+File.separator+ "TTUMS.zip");
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
	           }*/
		}
		catch(Exception e)
		{
			logger.info("Exception in generateMultipleTTUMFiles "+e );
			
		}
		
	}
	
	public void generateMultipleDRMTTUMFiles(String stPath, String FileName,int fileCount,List<Object> TTUMData,String TTUMName )
	{
		StringBuffer lineData;
		List<String> files = new ArrayList<>();
		FileInputStream fis;
		
		try
		{
			for(int j = 1; j <= fileCount ; j++)
			{
				List<Object> lstData = (java.util.ArrayList<Object>) TTUMData.get(j-1);
				String newFileName = FileName.replaceAll(".txt", "_"+j+".txt");
								
				File file = new File(stPath+File.separator+newFileName);
				if(file.exists())
				{
					FileUtils.forceDelete(file);
				}
				file.createNewFile();

				BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+newFileName, true));
				int startLine = 0;
				//LOGIC TO CREATE TTUM DATA
				for(int i =0 ;i< lstData.size(); i++)
				{
					
						Map<String, String> table_Data = (Map<String, String>) lstData.get(i);

						if(startLine > 0)
						{
							out.write("\n");
						}
						startLine++;
						lineData = new StringBuffer();
						lineData.append(table_Data.get("ACCOUNT_NUMBER")+"|"+table_Data.get("PART_TRAN_TYPE"));
						lineData.append("|"+table_Data.get("TRANSACTION_AMOUNT")+"|"+table_Data.get("TRANSACTION_PARTICULAR")+"|"+TTUMName);
						
						/*lineData.append(table_Data.get("ACCOUNT_NUMBER")+"  "+"INR0391"+"    "+table_Data.get("PART_TRAN_TYPE"));
						lineData.append(table_Data.get("TRANSACTION_AMOUNT")+table_Data.get("TRANSACTION_PARTICULAR")+"         "+table_Data.get("REMARKS"));
						lineData.append("                                                                          ");
						lineData.append(table_Data.get("FILEDATE"));*/
						//logger.info(lineData.toString());
						out.write(lineData.toString());	

				}
				out.flush();
				out.close();
				
				 files.add(stPath+File.separator+newFileName);

			}
		/*	FileOutputStream fos = new FileOutputStream(stPath+File.separator+ "TTUMS.zip");
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
	           }
	           catch(Exception fe)
	           {
	        	   System.out.println("Exception in zipping is "+fe);
	           }*/
		}
		catch(Exception e)
		{
			logger.info("Exception in generateMultipleDRMTTUMFiles "+e );
			
		}
		
	}
	
	public void generateDRMTTUM(String stPath, String FileName,List<Object> TTUMData,String TTUMName )
	{

		StringBuffer lineData;
		try
		{

			File file = new File(stPath+File.separator+FileName);
			if(file.exists())
			{
				FileUtils.forceDelete(file);
			}
			file.createNewFile();

			BufferedWriter out = new BufferedWriter(new FileWriter(stPath+File.separator+FileName, true));
			int startLine = 0;
			//LOGIC TO CREATE TTUM DATA
			for(int i =0 ;i<TTUMData.size(); i++)
			{
				Map<String, String> table_Data = (Map<String, String>) TTUMData.get(i);

				if(startLine > 0)
				{
					out.write("\n");
				}
				startLine++;
				lineData = new StringBuffer();
				lineData.append(table_Data.get("ACCOUNT_NUMBER")+"|"+table_Data.get("PART_TRAN_TYPE"));
				lineData.append("|"+table_Data.get("TRANSACTION_AMOUNT")+"|"+table_Data.get("TRANSACTION_PARTICULAR")+"|"+TTUMName);
				//logger.info(lineData.toString());
				out.write(lineData.toString());	

			}

			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			logger.info("Exception in generateTTUMFile "+e );

		}


	}
	
	public void generateExcelTTUM(String stPath, String FileName,List<Object> ExcelData,String TTUMName,String zipName )
	{

		StringBuffer lineData;
		List<String> files = new ArrayList<>();
		FileInputStream fis;
		try
		{
			logger.info("Filename is "+FileName);
			List<Object> TTUMData = (List<Object>) ExcelData.get(1);
			List<String> Excel_Headers = (List<String>) ExcelData.get(0);
			
			List<Object> Data;
			/*File file = new File(stPath+File.separator+FileName);
			if(file.exists())
			{
				FileUtils.forceDelete(file);
			}
			file.createNewFile();*/

						
			OutputStream fileOut = new FileOutputStream(stPath+File.separator+FileName);   
			
			HSSFWorkbook workbook = new HSSFWorkbook();
			
			for(int record_count = 0 ; record_count < TTUMData.size() ; record_count++)
			{
				Data = (List<Object>) TTUMData.get(record_count);
				HSSFSheet sheet = workbook.createSheet("Report"+record_count);   

				// create header row
				HSSFRow header = sheet.createRow(0);

				for(int i =0 ;i < Excel_Headers.size(); i++)
				{
					header.createCell(i).setCellValue(Excel_Headers.get(i));
				}

				HSSFRow rowEntry;

				for(int i =0; i< Data.size() ; i++)
				{
					rowEntry = sheet.createRow(i+1);
					Map<String, String> map_data =  (Map<String, String>) Data.get(i);
					if(map_data.size()>0)
					{

						for(int m= 0 ;m < Excel_Headers.size() ; m++)
						{


							rowEntry.createCell(m).setCellValue(map_data.get(Excel_Headers.get(m)));
						}
					}

				}

			}
			
			workbook.write(fileOut);
			fileOut.close();
			
	    	File file = new File(stPath);
	    	String[] filelist = file.list();
	    	
	    	for(String Names : filelist )
	    	{	
	    		logger.info("Files name is "+Names);
	    		files.add(stPath+File.separator+Names);
	    	}
	    	logger.info("Before zipping all files zipname is "+zipName);
	    	//FileOutputStream fos = new FileOutputStream(stPath+File.separator+ "EXCEL_TTUMS.zip");
	    	FileOutputStream fos = new FileOutputStream(stPath+File.separator+ zipName);
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
	        	   fos.close();
	        	   System.out.println("1............Done... Zipped the files...");
	           }
	           catch(Exception fe)
	           {
	        	   System.out.println("Exception in zipping is "+fe);
	           }
	           System.out.println("Zipping completed..............");
		}
		catch(Exception e)
		{
			logger.info("Exception in generateTTUMFile "+e );

		}


	}
}
