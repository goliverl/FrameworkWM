package om;

import java.util.HashMap;

import modelo.TestCase;
import utils.sql.SQLUtil;

public class PR36 {
	
	
	
	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	public PR36(HashMap<String, String> data, TestCase testCase,SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

}
