package om;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class PW01 {

	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s:5555/invoke/PW01.Pub:runEndMsg?xmlIn=%s";
	
	public String requestUrlrunRecMsg = "http://AutoPruebasIrving:pruebas.202@%s:5555/invoke/PW01.Pub:runRecMsg?xmlIn=%s";
	
	public String requestUrlrunRegister = "http://AutoPruebasIrving:pruebas.202@%s:5555/invoke/PW01.Pub:rubRegister?xmlIn=%s";
	
	public String requestUrlrunUpdInstall  = "http://AutoPruebasIrving:pruebas.202@%s:5555/invoke/PW01.Pub:runUpdInstall";
	
	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;

	public PW01(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	} 

	public String runEndMsg() throws ReempRequestException, ClientProtocolException, IOException {
  
		OxxoRequesPw01 or = new OxxoRequesPw01 (requestUrl);
		  
		String request = or.getRequestrunEndMsg(data.get("Plaza"),
				data.get("tienda"), 
				data.get("id"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

	public String runRecMsg() throws ReempRequestException, ClientProtocolException, IOException {
		
		OxxoRequesPw01 or = new OxxoRequesPw01 (requestUrl);
		  
		String request = or.getRequestrunRecMsg(data.get("id"),
				data.get("mensaje"));
		
	//String requestTRN01 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrlrunRecMsg, data.get("server"),request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String runRegister() throws ReempRequestException, ClientProtocolException, IOException  {
		  
		OxxoRequesPw01 or =  new OxxoRequesPw01 (requestUrl);
		  
		String request = or.getRequestrunRegister(data.get("Plaza"),
				data.get("Tienda"), 
				data.get("Caja"),  
				data.get("PV_Fecha"),
				data.get("PV_HORA"),
				data.get("APLICACION"),
				data.get("VERSIONN"),
				data.get("VERSIONA"),
				data.get("FILENAME"),
				data.get("PV_NUM_CAJA"));
		
	 //  String requestTRN01Cancelacion = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrlrunRegister, data.get("server"), request);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	public String runUpdInstall() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestUrlrunUpdInstall, data.get("server"));
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	
}
