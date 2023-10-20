package om;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class Tpe_Loy {
public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.LOY.Pub/request?xmlIn=%s";

	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;
	
	LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
	DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	String currentDate = dateformatter.format(date);

	public Tpe_Loy(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	}

	public String QRY01() throws ReempRequestException, ClientProtocolException, IOException {
  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringQry01(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				currentDate.substring(0,8), //adDate
				currentDate,  //pvDate
//				data.get("adDate"),
//				data.get("pvDate"),
				data.get("id"),
				data.get("mediumCode"));
				
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String TRN01() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringTrn01(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	public String TRN01withParams(String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringTrn01(data.get("application"),
				"TRN01", // operation
				data.get("entity"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				creationDate.substring(0,8), //adDate
				creationDate); //pvDate
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);
		System.out.println(response);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(url);
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String OPR01() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringOPR01(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("folio"), 
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"),
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("operator"),
				data.get("folio_ticket"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("receipt"),
				data.get("subCategoryId"),
				data.get("segmentId"),
				data.get("subSegmentId"),
				data.get("quantity"),
				data.get("price"),
				data.get("type"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	public String OPR01withParams(String folio, String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringOPR01(data.get("application"),
				"OPR01", // operation
//				data.get("operation"),
				data.get("entity"), 
				folio,
//				data.get("folio"),
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"),
				data.get("caja"),
				creationDate.substring(0,8), //adDate
				creationDate, //pvDate
//				data.get("adDate"),
//				data.get("pvDate"),
				data.get("operator"),
				data.get("folio_ticket"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("receipt"),
				data.get("subCategoryId"),
				data.get("segmentId"),
				data.get("subSegmentId"),
				data.get("quantity"),
				data.get("price"),
				data.get("type"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	

	public String OPR04(String folio, String creationdate) throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringOPR04(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				folio,
				//data.get("folio"), 
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"),
				data.get("caja"),
				creationdate.substring(0,8), //adDate      
				creationdate, //pvDate
//				data.get("adDate"),
//				data.get("pvDate"),
       			data.get("operator"),
				data.get("folio_ticket"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("receipt"),
				data.get("subCategoryId"),
				data.get("segmentId"),
				data.get("subSegmentId"),
				data.get("quantity"),
				data.get("price"),
				data.get("type"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	public String TRN02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringTrn02(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("folio"), 
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"),
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("operator"),
				data.get("folio_ticket"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("receipt"),
				data.get("redeemOptionId"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	
}
	
	public String TRN03() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringTrn03(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("folio"), 
				data.get("creationDate"), 
				data.get("ack"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	
}
	
	public String OPR02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringOPR02(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("folio"), 
				data.get("source"), 
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("operator"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("originalTransactionId"),
				data.get("productId"),
				data.get("subCategoryId"),
				data.get("segmentId"),
				data.get("subsegmentId"),
				data.get("quantity"),
				data.get("price"),
				data.get("type"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;
}
	public String OPR02withParams(String folio) throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Loy or = new OxxoRequestTpe_Loy(requestUrl);
		  
		String request = or.getRequestStringOPR02(data.get("application"),
				data.get("operation"), 
				data.get("entity"),
				folio,
//				data.get("folio"), 
				data.get("source"), 
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("operator"),
				data.get("id"),
				data.get("mediumCode"),
				data.get("transactionId"),
				data.get("originalTransactionId"),
				data.get("productId"),
				data.get("subCategoryId"),
				data.get("segmentId"),
				data.get("subsegmentId"),
				data.get("quantity"),
				data.get("price"),
				data.get("type"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;
}
}
