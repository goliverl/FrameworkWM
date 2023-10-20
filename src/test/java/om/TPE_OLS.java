package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class TPE_OLS {
	
	public String requestUrl = "http://%s/invoke/TPE.OLS.Pub/request?xmlIn=";
	
	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	Date fecha = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); 


		
	public TPE_OLS(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		super();
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
	public String ejecutarQRY01() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);
		String date = formatter.format(fecha);
		System.out.println(date);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("service:") && value.trim().length() > 0) {
					String keytoAdd = key.split(":")[1];
					or.addService(keytoAdd, value);
			}
		}
		
		
		String request = or.getRequestString();
		
		System.out.println("Request QRY01: \r\n" + request);
		
		testCase.addTextEvidenceCurrentStep("Request QRY01: \r\n"+request);
		
		String response = or.executeRequest();
		
		System.out.println("Response QRY01: \r\n" + response);
		
		testCase.addTextEvidenceCurrentStep("Response QRY01: \r\n" + response);
		
		return response;
	}

	public String ejecutarTRN01() throws ClientProtocolException, IOException {
		String url = String.format(requestUrl,data.get("ip") );
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		testCase.addBoldTextEvidenceCurrentStep("TRN01: \r\n");
		String request = or.getRequestString();		
		System.out.println("Request TRN01: \r\n"+request);		
		testCase.addBoldTextEvidenceCurrentStep("Request TRN01: \r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
		System.out.println("Response TRN01: \r\n" + response);		
		testCase.addBoldTextEvidenceCurrentStep("Response TRN01: \r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	
	public String ejecutarTRN02(String folio) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("ip"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("folio", folio);
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("service:") && value.trim().length() > 0) {
				String keytoAdd = key.split(":")[1];
				or.addService(keytoAdd, value);
			}
		    
		}
		
		testCase.addBoldTextEvidenceCurrentStep("TRN02: \r\n");
		String request = or.getRequestString();		
		System.out.println("Request TRN02: \r\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Request: \r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();	
		System.out.println("Response TRN02: \r\n" + response);
		testCase.addBoldTextEvidenceCurrentStep("Response TRN02: \r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		
		return response;
	}
	
	public String ejecutarTRN03(String folio) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("operation", "TRN03");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("folio", folio);		
		or.addAck(data.get("ack"));
		
		testCase.addBoldTextEvidenceCurrentStep("TRN03:\r\n");
		String request = or.getRequestString();		
		System.out.println("Request TRN03: \r\n"+request);		
		testCase.addBoldTextEvidenceCurrentStep("Request TRN03: \r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		
		String response = or.executeRequest();
		System.out.println("Response TRN03: \r\n" + response);
		testCase.addBoldTextEvidenceCurrentStep("Response TRN03: \r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		
		return response;
	}
	
	
	
 //proyecto wm86 caso 32 ******************************************************************************************************+
	
public String ejecutarQRY01wm86() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrl, data.get("host"));
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("operator", data.get("operator"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));

		
		//Se crea el tag de Service dentro del request
		Document doc = or.addNewDocument();	       
        Element tagServicio = doc.createElement("service");
        doc.appendChild(tagServicio);
        
        //Se agrega el id al tag service
        Attr attrId = doc.createAttribute("id");
        attrId.setValue(data.get("idQRY01"));
        tagServicio.setAttributeNode(attrId);
        
        //Se agrega el  idClient al tag service
        
        Attr attrIdClient = doc.createAttribute("idClient");
        attrIdClient.setValue(data.get("idClientQRY01"));
        tagServicio.setAttributeNode(attrIdClient);
        
        
    	
		String request = or.getRequestString();
		
		System.out.println("Request QRY01: \r\n" + request);
		
		testCase.addTextEvidenceCurrentStep("Request QRY01: \r\n"+request);
		
		String response = or.executeRequest();
		
		System.out.println("Response QRY01: \r\n" + response);
		
		testCase.addTextEvidenceCurrentStep("Response QRY01: \r\n" + response);
		
		return response;
	}

	public String ejecutarTRN01wm86() throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		String request = or.getRequestString();
		
		System.out.println("Request TRN01: \r\n"+request);
		
		testCase.addTextEvidenceCurrentStep("Request TRN01: \r\n"+request);
		
		String response = or.executeRequest();
		
		System.out.println("Response TRN01: \r\n" + response);
		
		testCase.addTextEvidenceCurrentStep("Response TRN01: \r\n"+response);
		
		return response;
	}
	
	public String ejecutarTRN02wm86(String folio) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("folio", folio);
		or.addHeader("pvDate", date);
		or.addHeader("operator", data.get("operator"));
		or.addHeader("adDate", date.substring(0, 8));
		
		//Se crea el tag de Service dentro del request
				Document doc = or.addNewDocument();	       
		        Element tagServicio = doc.createElement("service");
		        doc.appendChild(tagServicio);
		        
		        //Se agrega el id al tag service
		        Attr attrId = doc.createAttribute("id");
		        attrId.setValue(data.get("idTRN02"));
		        tagServicio.setAttributeNode(attrId);
		        
		        //Se agrega el  idClient al tag service		        
		        Attr attrIdClient = doc.createAttribute("idClient");
		        attrIdClient.setValue(data.get("idClientTRN02"));
		        tagServicio.setAttributeNode(attrIdClient);
		        
		        //Se agrega el  accountId  al tag service		        
		        Attr attrAccountId = doc.createAttribute("accountId");
		        attrAccountId.setValue(data.get("accountId"));
		        tagServicio.setAttributeNode(attrAccountId);
		        
		      //Se agrega el  ticket  al tag service		        
		        Attr attrTicket = doc.createAttribute("ticket");
		        attrTicket.setValue(data.get("ticket"));
		        tagServicio.setAttributeNode(attrTicket);
		        
		        //Se agrega el  total  al tag service		        
		        Attr attrTotal = doc.createAttribute("total");
		        attrTotal.setValue(data.get("total"));
		        tagServicio.setAttributeNode(attrTotal );
		        
		        //Se agrega el  entryMode  al tag service		        
		        Attr attrEntryMode = doc.createAttribute("entryMode");
		        attrEntryMode.setValue(data.get("entryMode"));
		        tagServicio.setAttributeNode(attrEntryMode);
		        
		      //Se agrega el payType  al tag service		        
		        Attr attrPayType = doc.createAttribute("payType");
		        attrPayType.setValue(data.get("payType"));
		        tagServicio.setAttributeNode(attrPayType);
		
		
		String request = or.getRequestString();
		
		System.out.println("Request TRN02: \r\n"+request);
		
		testCase.addTextEvidenceCurrentStep("Request: \r\n"+request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep("Response TRN02: \r\n"+ response);
		
		System.out.println("Response TRN02: \r\n" + response);
		
		return response;
	}
	
	public String ejecutarQRY01Generico() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrl, data.get("host"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entityQRY01"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plazaQRY01"));
		or.addHeader("tienda", data.get("tiendaQRY01"));
		or.addHeader("caja", data.get("cajaQRY01"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("serviceQRY01:") && value.trim().length() > 0) {
					String keytoAdd = key.split(":")[1];
					or.addService(keytoAdd, value);
			}
		}
		
		testCase.addBoldTextEvidenceCurrentStep("QRY01:\r\n");
		System.out.println("Request QRY01: \r\n");
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request QRY01:\r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addBoldTextEvidenceCurrentStep("Response QRY01:\r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String ejecutarTRN01Generico() throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plazaTRN01"));
		or.addHeader("tienda", data.get("tiendaTRN01"));
		or.addHeader("caja", data.get("cajaTRN01"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		testCase.addBoldTextEvidenceCurrentStep("TRN01:\r\n");
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request TRN01:\r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addBoldTextEvidenceCurrentStep("Request TRN01:\r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String ejecutarTRN02Generico(String folio) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		String date = formatter.format(fecha);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("folio", folio);
		or.addHeader("ref2", data.get("ref2"));
		or.addHeader("source", data.get("source"));
		or.addHeader("operation", "TRN02");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		for(Map.Entry<String, String> entry : data.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    if (key.contains("serviceTRN02:") && value.trim().length() > 0) {
				String keytoAdd = key.split(":")[1];
				or.addService(keytoAdd, value);
			}
		    
		}
		
		testCase.addBoldTextEvidenceCurrentStep("TRN02:\r\n");
		String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request TRN02:\r\n");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addBoldTextEvidenceCurrentStep("Response TRN02:\r\n");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
  public String ejecutarTRN03wm86(String folio) throws ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "OLS");
		or.addHeader("operation", "TRN03");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("folio", folio);		
		or.addAck(data.get("ack"));
		
		String request = or.getRequestString();
		
		System.out.println("Request TRN03: \r\n"+request);
		
		testCase.addTextEvidenceCurrentStep("Request TRN03: \r\n" +request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep("Response TRN03: \r\n" + response);
		
		System.out.println("Response TRN03: \r\n" + response);
		
		return response;
	}
	
	
}
