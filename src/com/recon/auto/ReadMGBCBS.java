package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.List;

import com.jcraft.jsch.Logger;
import com.recon.util.OracleConn;

public class ReadMGBCBS {

	public static void main1(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		int line_Number = 0, reading_line = 5;
		int start_pos = 0;
		List<String> elements = reading.readMGBAtmCBS();
		String action = "";
		try
		{
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\SWITCH FILE\\MGB_ATM_24082022\\MGB_Acquirer24082022.txt");
			File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\BGL FILE\\cfpdahrpadhoc040000000000004133138.prt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				if(line_Number == reading_line)
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
								System.out.println(data[0]+" : "+stLine.substring(start_pos).trim());
								String narration = stLine.substring(start_pos).trim();
								if(action != null)
								{
									if(action.equalsIgnoreCase("C"))
									{
										System.out.println("Its a C type transaction");
										System.out.println("card Number : "+narration.substring(0,18));
										System.out.println("RRN : "+narration.substring(19, 33));
										System.out.println("ATMID : "+narration.substring(33));
									}
									else
									{
										System.out.println("Its a D type transaction");
										System.out.println("rrn : "+narration.substring(0,6)+narration.substring(45));
										System.out.println("ATM Id : "+narration.substring(29,38));
									}
								}
								
							}
							else	
							{
									start_pos = Integer.parseInt(data[1]);
									System.out.println(data[0]+" : "+" "+stLine.substring((start_pos-1),(Integer.parseInt(data[2]))).trim());
									
									if(data[0].equalsIgnoreCase("ACTION"))
									{
										action = stLine.substring((start_pos-1),(Integer.parseInt(data[2]))).trim();
									}
									
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
	
	public static void main3(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		int line_Number = 0, reading_line = 5;
		int start_pos = 0;
		List<String> elements = reading.readMGBPosCBS();
		try
		{
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\SWITCH FILE\\MGB_ATM_24082022\\MGB_Acquirer24082022.txt");
			File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\BGL\\cfpdahrpadhoc040000000000004133139.prt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				if(line_Number == reading_line)
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
								System.out.println(data[0]+" : "+stLine.substring(start_pos).trim());
								
							}
							else	
							{
									start_pos = Integer.parseInt(data[1]);
									System.out.println(data[0]+" : "+" "+stLine.substring((start_pos-1),(Integer.parseInt(data[2]))).trim());
									
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
	
	
	public static void main2(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		int line_Number = 0, reading_line = 6;
		int start_pos = 0;
		List<String> elements = reading.readMGBFailedCBS();
		try
		{
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\SWITCH FILE\\MGB_ATM_24082022\\MGB_Acquirer24082022.txt");
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\CBS\\MGB_FAIL_POS_20220824.txt");
			File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\CBS\\MGB_FAIL_POS_20220824.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				if(line_Number == reading_line)
				{
					if(stLine.contains("----------------------------"))
					{
						break;
					}
					else
					{
						System.out.println(stLine);
						start_pos = 0;
						for(int i = 0; i < elements.size(); i++)
						{
							String[] data = elements.get(i).split("\\|");
							if(data.length == 2)
							{
								//last parameter
								start_pos = Integer.parseInt(data[1]);
								System.out.println(data[0]+" : "+stLine.substring(start_pos).trim());
								
							}
							else	
							{
									start_pos = Integer.parseInt(data[1]);
									System.out.println(data[0]+" : "+" "+stLine.substring((start_pos),(Integer.parseInt(data[2]))).trim());
									
							}
						}
					}
				}
				
			}

			
		}
		catch(Exception e)
		{
			System.out.println("Exception is "+e);
			
		}
	}
	
	public static void main(String[] a)
	{
		String stLine = null;
		Switch_POS reading = new Switch_POS();
		int line_Number = 0, reading_line = 6;
		int start_pos = 0;
		List<String> elements = reading.readMGBSuccessCBS();
		try
		{
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\ATM FILES 24AUG2022\\SWITCH FILE\\MGB_ATM_24082022\\MGB_Acquirer24082022.txt");
			//File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\CBS\\MGB_FAIL_POS_20220824.txt");
			File file = new File("D:\\BHAGYASHREE\\MGB\\Raw Files\\POS FILES\\CBS\\MGB_SUCC_POS_20220824.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				if(line_Number == reading_line)
				{
					if(stLine.contains("----------------------------"))
					{
						break;
					}
					else
					{
						System.out.println(stLine);
						start_pos = 0;
						for(int i = 0; i < elements.size(); i++)
						{
							String[] data = elements.get(i).split("\\|");
							if(data.length == 2)
							{
								//last parameter
								start_pos = Integer.parseInt(data[1]);
								System.out.println(data[0]+" : "+stLine.substring(start_pos).trim());
								
							}
							else	
							{
									start_pos = Integer.parseInt(data[1]);
									System.out.println(data[0]+" : "+" "+stLine.substring((start_pos),(Integer.parseInt(data[2]))).trim());
									
							}
						}
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
