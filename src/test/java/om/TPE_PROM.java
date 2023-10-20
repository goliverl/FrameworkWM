package om;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class TPE_PROM {
	
	/**
	 * Clase para la construccion de XML segun la operacion requerida.
	 * 
	 * @author Jose Onofre
	 * @return Respuesta XML
	 * 
	 */
	
//public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.PROM.Pub/request?xmlIn=%s";
public String requestUrl = "http://%s/invoke/TPE.PROM.Pub/request?xmlIn=%s";


	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;

	public TPE_PROM(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	}

	/**
	 * Metodo para construir el XML a enviar al host con operacion QRY01
	 * 
	 * @author Jose Onofre
	 * @return Respuesta XML
	 * 
	 */
	public String QRY01() throws ReempRequestException, ClientProtocolException, IOException {
  
		//requestUrl = String.format(requestUrl, data.get("host"));
		OxxoRequesTPE_PROM or = new OxxoRequesTPE_PROM(requestUrl);
		  
		String request = or.getRequestStringQry01(data.get("application"),
				data.get("operation"), 
				data.get("entity"),  
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("pvTicket"),
				data.get("userId"),
				data.get("folioPromocional"),
				data.get("montoTotal"),
				data.get("sku")
				);
				
		

		String url = String.format(requestUrl, data.get("host"),request);
		System.out.println(request);
		System.out.println(url);
		
		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addBoldTextEvidenceCurrentStep("\n Response: \n");
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	/**
	 * Metodo para construir el XML a enviar al host con operacion QRY02
	 * 
	 * @author Jose Onofre
	 * @return Respuesta XML
	 * 
	 */
	public String QRY02() throws ReempRequestException, ClientProtocolException, IOException {
		  
		//requestUrl = String.format(requestUrl, data.get("host"));
		OxxoRequesTPE_PROM or = new OxxoRequesTPE_PROM(requestUrl);
		  
		String request = or.getRequestStringQry02(data.get("application"),
				"QRY02", 
				data.get("entity"),
				data.get("source"), 
				data.get("plaza"), 
				data.get("tienda"), 
				data.get("caja"),
				data.get("adDate"),
				data.get("pvDate"),
				data.get("userId")
				);
				
		
	
		String url = String.format(requestUrl, data.get("host"),request);
		System.out.println(request);
		System.out.println(url);
		
		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addBoldTextEvidenceCurrentStep("\n Response: \n");
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	
	
}
