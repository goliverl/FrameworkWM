package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import config.Constants;

public class DocumentUtil { 

	public String createRequestFile(String request, String docName) throws IOException {
		
		validateDocsDirectory();
		
		String filepath = Constants.DOCUMENTS_PATH+"/"+docName+".txt";
		File file = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		try {
		   file = new File(filepath);
		   fw = new FileWriter(file);
           bw = new BufferedWriter(fw);
          
           bw.write(request);
         
		} catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                  if (null != fw)
                      bw.close();
                      fw.close();
                } catch (Exception e2) {
                      e2.printStackTrace();
                }
	
        }

		
		return "/"+docName+".txt";
	}
	
	public void validateDocsDirectory() throws IOException{
		
		System.out.print(Constants.DOCUMENTS_PATH+"\n");
		File folder = new File(Constants.DOCUMENTS_PATH);	
		
		if(folder.exists()&&folder.isDirectory()) {
			System.out.print("Ya existe la carpeta docs"+"\n");	
		    
		}else {
			folder.mkdirs();
			//System.out.print("Se creó la carpeta docs"+"\n");
		}
		
	}

}
