package com.recon.util;

import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxReading {

	public static void main(String[] args) {

		try
        {
            FileInputStream file = new FileInputStream("D:\\City Union Bank JCB cashback data- 16th July to 15th Aug'23.xlsx");
 
            System.out.println("file got ");
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet ws = wb.getSheetAt(1);
 
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = ws.iterator();
            while (rowIterator.hasNext()) 
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                 
                while (cellIterator.hasNext()) 
                {
                    Cell cell = cellIterator.next();
                    //Check the cell type and format accordingly
                   
//                    if(cell.getStringCellValue().contains("PAN Masked")) {
//                    	break ;
//                    }
                    switch (cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue());
                            break;
                    }
                }
                System.out.println("Reading File Completed.");
            }
            file.close();
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
	}
}
