package om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import config.Constants;
import modelo.TestCase;
import utils.ExcelUtil;
import utils.sql.SQLUtil;

public class PR1 {
	
	HashMap<String, String> data;
	TestCase testCase;
	
	//SqlUtil db;
	utils.sql.SQLUtil db;
	
	public PR1(HashMap<String, String> data, TestCase testCase,utils.sql.SQLUtil  db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

    public  void addDataToExcelFile(int numRow,int numColumn, String dato) {
		
		try {	
		    ExcelUtil eu = new ExcelUtil(Constants.DATA_PROVIDER_PATH+"\\interfaces\\pr1\\PR1TransferenciaCedis.xlsx");
			
		    //Escribir en el documento de excel PE2Dev
		    eu.open();
		    ArrayList<String> sheets = eu.getSheetNames();		       
		       eu.setCell(sheets.get(0),numRow,numColumn, dato);
		    eu.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
	
}
