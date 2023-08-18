package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.List;

import com.jcraft.jsch.Logger;
import com.recon.util.OracleConn;

public class ReadMGBSwitch {

	public static void main(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		int line_Number = 0, reading_line = 10;int start_pos = 0;
		List<String> elements = reading.readMGBSwitch();
		try
		{
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\SWITCH FILE\\MGB_ATM_24082022\\MGB_Acquirer24082022.txt");
			File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\SWITCH FILE\\MGB_POS_24082022\\MGB_POS24082022.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				if(line_Number >= reading_line)
				{
					if(stLine.contains("----------------------------"))
					{
						break;
					}
					else
					{
						start_pos = 0;
						for(int i = 0; i < elements.size(); i++)
						{
							String[] data = elements.get(i).split("\\|");
							if(data.length == 2)
							{
								//last parameter
								start_pos = Integer.parseInt(data[1]);
								System.out.println("Data is "+stLine.substring(start_pos));
								
							}
							else	
							{
									start_pos = Integer.parseInt(data[1]);
									System.out.println("data is"+" "+stLine.substring((start_pos-1),(Integer.parseInt(data[2]))));
							}
						}
						//System.out.println("\n");
					}
				}
				
			}

			
		}
		catch(Exception e)
		{
			System.out.println("Exception is "+e);
			
		}
	}
}
