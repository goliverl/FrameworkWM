package om;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class TPE_LOT {

	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.LOT.Pub/request?xmlIn=%s";
	
	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;

	public TPE_LOT(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	}

	public String TRN01() throws ReempRequestException, ClientProtocolException, IOException {
  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestString(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

	public String TRN02_Cancelacion_Parte1() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestString(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String TRN02Cancelacion(String folio,String creationDate) throws ReempRequestException, ClientProtocolException, IOException  {
		  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestStringTRn02_Cancelacion(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				folio,
				creationDate,
				data.get("adDate"), 
				creationDate,
				data.get("serviceId"),
				data.get("pvTicket"),
				data.get("operator"),
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				data.get("ticketNumber"));
		
	 //  String requestTRN01Cancelacion = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"), request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String TRN02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestStringTRN02(data.get("application"),
				data.get("entity"), 
				data.get("operation"), 
				data.get("source"),
				data.get("folio"),
				data.get("creationDate"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("serviceId"),
				data.get("pvTicket"),
				data.get("operator"),
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("gameType"),
				data.get("inputMethod"),
				data.get("drawNumber"), 
				data.get("numberOfDraws"),
				data.get("couponNumber"),
				data.get("selection"));
		
	//String requestTRN02= (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"), request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String TRN03() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestStringTRN03(data.get("application"),
				data.get("entity"), 
				data.get("operation"), 
				data.get("source"),
				data.get("folio"),
				data.get("creationDate"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("serviceId"),
				data.get("pvTicket"),
				data.get("operator"),
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("gameType"),
				data.get("inputMethod"),
				data.get("drawNumber"), 
				data.get("numberOfDraws"),
				data.get("couponNumber"),
				data.get("selection"),
				data.get("value1"));
		
	//String requestTRN03= (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"), request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String TRN04() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequesTPE_LOT or = new OxxoRequesTPE_LOT (requestUrl);
		  
		String request = or.getRequestStringTRN04(data.get("application"),
				data.get("entity"), 
				data.get("operation"), 
				data.get("source"),
				data.get("folio"),
				data.get("creationDate"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("serviceId"),
				data.get("pvTicket"),
				data.get("operator"),
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("value1"),
				data.get("desc"),
				data.get("folio1"),
				data.get("creationDate1"));
		
	//String requestTRN04= (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"), request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
}
