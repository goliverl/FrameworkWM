package om;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import modelo.TestCase;
import utils.sql.SQLUtil;

public class PE01_D {
	
	LocalDateTime now = LocalDateTime.now();
	ZoneId zoneId = ZoneId.systemDefault();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String currentDate = now.atZone(zoneId).format(formatter);
	
	public HashMap<String,String> data;
	public SQLUtil db;
	TestCase testCase;
	boolean tipoDePrueba;
	
	public PE01_D (HashMap<String, String> data, TestCase testCase, SQLUtil db2) {
		this.data = data;
		this.db = db2;
		this.testCase = testCase;
		}
	
	public String runGetFolioParams() {
		
		Map <String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("plaza", data.get("plaza"));
		paramsMap.put("tienda", data.get("tienda"));
		paramsMap.put("caja", data.get("caja"));
		
		//Arma los parametros para el request
		String formatedParams = "";
		for (Entry<String, String> entry : paramsMap.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	
	public String runGetAuthParams(String folio) {
			
			Map <String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("folio", folio);
			paramsMap.put("pv_date", currentDate);
			paramsMap.put("ad_date", currentDate.substring(0, 8));
			paramsMap.put("carrier", data.get("carrier"));
			paramsMap.put("phone", data.get("phone"));
			paramsMap.put("amount", data.get("amount"));
			
			//Arma los parametros para el request
			String formatedParams = "";
			for (Entry<String, String> entry : paramsMap.entrySet()) {
				String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
				formatedParams += formParams;
			}
			String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
			System.out.println(params);
			
			return params;
		}
	
		public String runGetAckParams(String folio) {
			
			Map <String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("folio", folio);
			paramsMap.put("ack", data.get("ack"));
			
			//Arma los parametros para el request
			String formatedParams = "";
			for (Entry<String, String> entry : paramsMap.entrySet()) {
				String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
				formatedParams += formParams;
			}
			String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
			System.out.println(params);
			
			return params;
		}
}
