package com.recon.auto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GLReadAtmPos {

	public static void main(String[] args) {
		String stLine = null;
		try {
			
			File file = new File("C:\\Users\\user\\Desktop\\FILES\\12122022\\CBS FILE\\cfpdahrpadhoc040000000000004306217.prt");
			
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			while((stLine = br.readLine()) != null) {
				System.out.println("FIRST LINE :" +stLine);
				System.out.println("GL NUMBER : " + stLine.substring(9, 26) );
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println(".......");
		}

	}

}
