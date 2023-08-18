package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ReadDhanaGLRecon {
	
	public static void main(String[] a) {

		String stLine = null;
		int line_Number = 0;

		try
		{
			File file = new File("D:\\BHAGYASHREE\\DHANALAXMI DATA\\IDBI DCRS FILES\\IDBI DCRS FILES\\15-05-2022\\Downloads\\GL DETAILS\\GL_Recon-15-MAY-22.csv");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((stLine = br.readLine()) != null)
			{
				line_Number++;
				
				if(line_Number == 2)
				{
					String[] datas = stLine.split(",");
					
					for(String data : datas)
					{
						System.out.println(data);
					}
				}
			}
		}
		catch(Exception e)
		{

		}
	}

}
