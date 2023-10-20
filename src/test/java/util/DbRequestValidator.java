package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DbRequestValidator {

	public static String generateQueryTransaction(String tableName, String request, String folio, String wmCodeToValidate, Date startDate, Date endDate) throws ParseException {
		String application = null;
		String entity = null;
		String operation = null;
		String source = null;
		String plaza = null;
		String tienda = null;
		String caja = null;
		
		if (request.contains("jsonIn")) {
			String[] splitUrl = request.split("jsonIn=");
			String jsonStr = splitUrl[1];
			
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(jsonStr);
			JSONObject doc = (JSONObject) json.get("TPEDoc");
			JSONObject header = (JSONObject) doc.get("header");

			application = header.get("application").toString();
			entity = header.get("entity").toString();
			operation = header.get("operation").toString();
			
			source = header.get("source").toString();
			plaza = header.get("plaza").toString();
			tienda = header.get("tienda").toString();
			caja = header.get("caja").toString();
			
		}
		
		String query = "SELECT application, entity, operation, source, folio, plaza, tienda, caja, wm_code, creation_date \r\n" + 
				"FROM "+tableName+"\r\n" + 
				"where APPLICATION = '"+application+"'\r\n" + 
				"AND ENTITY = '"+entity+"'\r\n" + 
				"AND OPERATION = '"+operation+"'\r\n" + 
				"AND FOLIO = '"+folio+"'\r\n" + 
				"AND WM_CODE = '"+wmCodeToValidate+"'\r\n";
		
		if (source != null) {
			query += "AND SOURCE = '"+source+"'\r\n";
		}
		
		if (plaza != null) {
			query += "AND PLAZA = '"+plaza+"'\r\n";
		}
		
		if (tienda != null) {
			query += "AND TIENDA = '"+tienda+"'\r\n";
		}
		
		if (caja != null) {
			query += "AND CAJA = "+caja+"\r\n";
		}
				
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		String startDateStr = formatter.format(startDate);
		String endDateStr = formatter.format(endDate);
		
		query += "AND CREATION_DATE BETWEEN TO_DATE('"+startDateStr+"','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"                  AND TO_DATE('"+endDateStr+"','dd/mm/yyyy hh24:mi:ss')\r\n";

		return query;
		
	}
	
}
