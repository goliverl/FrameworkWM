package om;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class Tpe_Cines {
public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.CINES.Pub/request?xmlIn=%s";

	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;

	public Tpe_Cines(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	}

	public String QRY01() throws ReempRequestException, ClientProtocolException, IOException {
  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringQry01(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("ServiceOpcion"));
				
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String QRY02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringQry02(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("ServiceCiudad"));
				
		
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
		  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringTrn01(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
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
	
	public String TRN02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringTrn02(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("ServicepayType"),
				data.get("IdEntidad"),
				data.get("Idcanal"),
				data.get("IdTerminal"),
				data.get("TipoVoucher"),
				data.get("ImporterPVP"),
				data.get("IdOperacionInt"),
				data.get("NoEntradas"));
				
		
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
		  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringTrn03(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("folio"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("ackValue"));
				
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("Server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

	public String TRN04() throws ReempRequestException, ClientProtocolException, IOException {
		  
		OxxoRequestTpe_Cines or = new OxxoRequestTpe_Cines(requestUrl);
		  
		String request = or.getRequestStringTrn04(data.get("application"),
				data.get("entity"), 
				data.get("operation"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("pvDate"),
				data.get("WmCodeauth"),
				data.get("folio"),
				data.get("creationDate"));
				
		
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
