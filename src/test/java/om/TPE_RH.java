package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;

public class TPE_RH {

public String requestUrl = "http://%s/invoke/TPE.RH.Pub/request?xmlIn=";
	
	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	Date fecha = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); 

	
	public TPE_RH(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
	public String ejecutarQRY01() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrl, GlobalVariables.HOST_RH);
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RH");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("cajaQRY01"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("service:") && value.trim().length() > 0) {
					String keytoAdd = key.split(":")[1];
					or.addService(keytoAdd, value);
			}
		}
		
		//or.addService("id", data.get("id"));
		//or.addService("idClient", data.get("idClient"));
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}

	public String ejecutarTRN01() throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_RH);
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RH");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		//or.addHeader("pvDate", date);
		//or.addHeader("adDate", date.substring(0, 8));
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String ejecutarTRN02(String folio, String creationDateTRN01) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_RH);
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RH");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("folio", folio);
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", creationDateTRN01);
		or.addHeader("pvTicket", data.get("pvTicket")); 
		or.addHeader("operator", data.get("operator"));		
		
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("serviceAut:") && value.trim().length() > 0) {
				String keytoAdd = key.split(":")[1];
				or.addServiceAut(keytoAdd, value);
			}
		    
		}
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String ejecutarTRN03(String folio, String creationDateTRN01) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_RH);
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RH");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN03");
		or.addHeader("folio", folio);
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", creationDateTRN01);
		or.addHeader("pvTicket", data.get("pvTicket")); 
		or.addHeader("operator", data.get("operator"));
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
}
