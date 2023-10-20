package om;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
/**
 * Requests para interface OLS_D, QRY01, TRN01, TRN02, TRN03
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */
public class OLS_D {
	
	//Obtener fecha y hora en curso
	LocalDateTime now = LocalDateTime.now();
	ZoneId zoneId = ZoneId.systemDefault();
			//systemDefault().normalized();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String currentDate = now.atZone(zoneId).format(formatter);
	
	public HashMap<String,String> data;
	public SQLUtil db;
	TestCase testCase;
	boolean tipoDePrueba;
	
	public OLS_D (HashMap<String, String> data, TestCase testCase, SQLUtil db2) {
		this.data = data;
		this.db = db2;
		this.testCase = testCase;
		}
	
	//Ruta de archivos XML para request.
	String runQRY01 = "/OLS_D/QRY01";
	String runTRN01 = "/OLS_D/TRN01";
	String runTRN02 = "/OLS_D/TRN02";
	String runTRN03 = "/OLS_D/TRN03";
	String runTRN04 = "/OLS_D/TRN04";
	
	public String QRY01() throws ReempRequestException, ClientProtocolException, IOException, Exception{
		/*
		 * Request para consulta de servicio de la interface OLS_D
		 */
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("host", data.get("host"));
		requestData.put("interface", data.get("application"));
		requestData.put("application", data.get("application"));
		requestData.put("entity", data.get("entity"));
		requestData.put("operation", "QRY01");
		requestData.put("source", data.get("source"));
		requestData.put("plaza", data.get("plaza"));
		requestData.put("tienda", data.get("tienda"));
		requestData.put("caja", data.get("caja"));
		requestData.put("adDate", currentDate.substring(0, 8));
		requestData.put("pvDate", currentDate);
//		requestData.put("operator", data.get("operator"));
		requestData.put("service id", data.get("serviceId"));
		requestData.put("idClient", data.get("idClient"));
		
		//Sustituye los datos en el XML
		String requestXML = GetRequestFile.getRequestFile(runQRY01, requestData);
		System.out.println("Request QRY01: " + requestXML);
		//Agregar Request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request:");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
		testCase.addTextEvidenceCurrentStep(requestXML);
		//Se realiza request
		String response = GetRequest.executeGetRequest(requestXML);
		System.out.println("Response QRY01: " + response);
		testCase.addBoldTextEvidenceCurrentStep("Response:");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String TRN01() throws ReempRequestException, ClientProtocolException, IOException{
		/*
		 * Request para solicitud de folio de la interface OLS_D
		 */
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("host", data.get("host"));
		requestData.put("interface", data.get("application"));
		requestData.put("application", data.get("application"));
		requestData.put("entity", data.get("entity"));
		requestData.put("operation", "TRN01");
		requestData.put("source", data.get("source"));
		requestData.put("plaza", data.get("plaza"));
		requestData.put("tienda", data.get("tienda"));
		requestData.put("caja", data.get("caja"));
		requestData.put("adDate", currentDate.substring(0, 8));
		requestData.put("pvDate", currentDate);
		
		//Sustituye los datos en el XML
		String requestXML = GetRequestFile.getRequestFile(runTRN01, requestData);
		System.out.println("Request TRN01: " + requestXML);
		//Agregar Request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request:");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
		testCase.addTextEvidenceCurrentStep(requestXML);
		//Se realiza request
		String response = GetRequest.executeGetRequest(requestXML);
		System.out.println("Response TRN01: " + response);
		testCase.addBoldTextEvidenceCurrentStep("Response:");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	public String TRN02(String folio) throws ReempRequestException, ClientProtocolException, IOException{
		/*
		 * Request para solicitud de autorizacion de pago de la interface OLS_D
		 */
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("host", data.get("host"));
		requestData.put("interface", data.get("application"));
		requestData.put("application", data.get("application"));
		requestData.put("entity", data.get("entity"));
		requestData.put("folio", folio);
		requestData.put("operation", "TRN02");
		requestData.put("source", data.get("source"));
		requestData.put("adDate", currentDate.substring(0, 8));
		requestData.put("pvDate", currentDate);
		requestData.put("operator", data.get("operator"));
		requestData.put("service id", data.get("serviceId"));
		requestData.put("accountId", data.get("accountId"));
		requestData.put("idClient", data.get("idClient"));
		requestData.put("ticket", data.get("ticket"));
		requestData.put("total", data.get("total"));
//		requestData.put("total", total.toString());
		requestData.put("entryMode", data.get("entryMode"));
		requestData.put("payType", data.get("payType"));
		
		//Sustituye los datos en el XML
		String requestXMLParams = GetRequestFile.getRequestFile(runTRN02, requestData);
		System.out.println("Request TRN02: " + requestXMLParams);
		//Agregar Request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request:");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
		testCase.addTextEvidenceCurrentStep(requestXMLParams);
		//Se realiza request
		String response = GetRequest.executeGetRequest(requestXMLParams);
		System.out.println("Response TRN02: " + response);
		testCase.addBoldTextEvidenceCurrentStep("Response:");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	public String TRN03(String folio) throws ReempRequestException, ClientProtocolException, IOException{
		/*
		 * Request para confirmacion de pago de la interface OLS_D
		 */
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("host", data.get("host"));
		requestData.put("interface", data.get("application"));
		requestData.put("application", data.get("application"));
		requestData.put("entity", data.get("entity"));
		requestData.put("operation", "TRN03");
		requestData.put("folio", folio);
		requestData.put("ack", data.get("ack"));
		
		//Sustituye los datos en el XML
		String requestXMLParams = GetRequestFile.getRequestFile(runTRN03, requestData);
		System.out.println("Request TRN03" + requestXMLParams);
		//Agregar Request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request:");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
		testCase.addTextEvidenceCurrentStep(requestXMLParams);
		//Se realiza request
		String response = GetRequest.executeGetRequest(requestXMLParams);
		System.out.println("Response TRN03: " + response);
		testCase.addBoldTextEvidenceCurrentStep("Response:");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	public String TRN04(String folio, String auth, String creationDate) throws ReempRequestException, ClientProtocolException, IOException{
		/*
		 * Request para reversa por caso Critico.
		 */
		HashMap<String, String> requestData = new HashMap<>();
		requestData.put("host", data.get("host"));
		requestData.put("interface", data.get("application"));
		requestData.put("application", data.get("application"));
		requestData.put("entity", data.get("entity"));
		requestData.put("operation", "TRN04");
		requestData.put("source", data.get("source"));
		requestData.put("plaza", data.get("plaza"));
		requestData.put("tienda", data.get("tienda"));
		requestData.put("caja", data.get("caja"));
		requestData.put("pvDate", currentDate);
		requestData.put("folio", folio);
		requestData.put("auth", auth);
		requestData.put("creationDate", creationDate);
		
		//Sustituye los datos en el XML
		String requestXMLParams = GetRequestFile.getRequestFile(runTRN04, requestData);
		System.out.println("Request TRN04" + requestXMLParams);
		//Agregar Request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request:");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
		testCase.addTextEvidenceCurrentStep(requestXMLParams);
		//Se realiza request
		String response = GetRequest.executeGetRequest(requestXMLParams);
		System.out.println("Response TRN04: " + response);
		testCase.addBoldTextEvidenceCurrentStep("Response:");
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
}
