package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class PE3 {
	
	public String runGetFolioPath = "PE3\\RunGetFolio.txt";
	public String runGetAuth = 		"PE3\\RunGetAuth.txt";
	public String runGetAck = 		"PE3\\RunGetAck.txt";
	
	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;

	/*Date fecha = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); */
	
	LocalDateTime now = LocalDateTime.now();
	ZoneId zoneId = ZoneId.systemDefault();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String currentDate = now.atZone(zoneId).format(formatter);

	
	public PE3(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
		
	}
	
	//Metodo que ejecuta el servicio rungGetFolio
        public String runGetFolioParams() {
		
		Map <String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("plaza", data.get("plaza"));
		paramsMap.put("tienda", data.get("tienda"));
		paramsMap.put("caja", data.get("caja"));
		paramsMap.put("tokenCode", data.get("tokenCode"));
		
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
        
        
        //Metodo que ejecuta el servicio runGetActivation
        
            public String runGetActivacionParams(String folio) {
			
			Map <String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("folio", folio);
			paramsMap.put("pv_date", currentDate);
			paramsMap.put("ad_date", currentDate.substring(0, 8));
			paramsMap.put("upc", data.get("upc"));
			paramsMap.put("cardNo", data.get("cardNo"));
			paramsMap.put("amount", data.get("amount"));
			paramsMap.put("currency", data.get("currency"));
			
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
            
	//Metodo que ejecuta el servicio  RunGetAck
            
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
        
        
        

       //Metodos antiguos
	public String ejecutarRunGetFolio() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", GlobalVariables.HOST);
	    datosRequest.put("plaza", data.get("plaza"));
	    datosRequest.put("tienda", data.get("tienda"));
	    datosRequest.put("caja", data.get("caja"));
	    datosRequest.put("tokenCode", data.get("tokenCode"));
		
		String request = GetRequestFile.getRequestFile(runGetFolioPath, datosRequest);
		System.out.println(request);
		
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = GetRequest.executeGetRequest(request);
		
		return response;
	}
	
    public String ejecutarRunGetAuth(String folio) throws ReempRequestException, ClientProtocolException, IOException {
	//    String date = formatter.format(currentDate);
	
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", GlobalVariables.HOST);
		datosRequest.put("folio", folio);
	    datosRequest.put("pvDate",currentDate);
	    datosRequest.put("adDate", currentDate.substring(0, 8));
	    datosRequest.put("upc",    data.get("upc"));
	    datosRequest.put("cardNo", data.get("cardNo"));	   
	    datosRequest.put("amount", data.get("amount"));
	    datosRequest.put("currency",data.get("currency"));
	    
		
		String request = GetRequestFile.getRequestFile(runGetAuth, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		String response = GetRequest.executeGetRequest(request);		
		
		return response;
	}

public String ejecutarRunGetAck(String folio) throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", GlobalVariables.HOST);
	    datosRequest.put("folio", folio);
	    datosRequest.put("ack", data.get("ack"));
	    
		
		String request = GetRequestFile.getRequestFile(runGetAck, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		String response = GetRequest.executeGetRequest(request);
	
		return response;
	}

	
	

}
