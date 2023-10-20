package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import utils.webmethods.GetRequest;


public class PE2V2 {

	public String runGetFolioPath = "PE2V2\\RunGetFolio.txt";
	public String runGetAuthPath = "PE2V2\\RunGetAuth.txt";
	public String runGetAckPath = "PE2V2\\RunGetAuthAck.txt";
	public String RequestKeys = "PE2V2\\RequestKeys.txt";
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;
	String swAuth;
	
	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
	
	public PE2V2(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

	
	
	public String ejecutarRunGetFolio() throws ReempRequestException, ClientProtocolException, IOException {
	
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("type", data.get("type"));
			
		String request = GetRequestFile.getRequestFile(runGetFolioPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio getFolio funciona correctamente ");

		

		String response = GetRequest.executeGetRequest(request);		
		testCase.addTextEvidenceCurrentStep("\n"+response);
		
		return response;
		
		
	}
	
	
	public String ejecutarRunGetAuth(String folio,String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
		   
		HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		datosRequestRunGetAuth.put("host", data.get("host"));
		datosRequestRunGetAuth.put("creationDate", creationDate);
		datosRequestRunGetAuth.put("pvDate", creationDate);
		datosRequestRunGetAuth.put("adDate", creationDate.substring(0, 8));
		datosRequestRunGetAuth.put("cardNo", data.get("cardNo"));
		datosRequestRunGetAuth.put("folio", folio);
		datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		datosRequestRunGetAuth.put("seqNo", data.get("seqNo"));
		datosRequestRunGetAuth.put("cvv", data.get("cvv"));
		datosRequestRunGetAuth.put("track2", data.get("track2"));
		datosRequestRunGetAuth.put("promType", data.get("promType"));
		datosRequestRunGetAuth.put("amount", data.get("amount"));
		datosRequestRunGetAuth.put("f55", data.get("f55"));
		datosRequestRunGetAuth.put("operator", data.get("operator"));
		datosRequestRunGetAuth.put("accountType", data.get("accountType"));
		datosRequestRunGetAuth.put("serviceId", data.get("serviceId"));
		datosRequestRunGetAuth.put("tokenC4",data.get("tokenC4"));
		datosRequestRunGetAuth.put("tokenCZ", data.get("tokenCZ"));
	
		
		String request = GetRequestFile.getRequestFile(runGetAuthPath, datosRequestRunGetAuth);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio getAuth funciona correctamente ");

		

		String response = GetRequest.executeGetRequest(request);		
		testCase.addTextEvidenceCurrentStep("\n"+response);
		return response;
		}


	public String ejecutarRunGetInitialization(String folio,String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
		   
		HashMap<String, String> datosRequestRunGetInitialization = new HashMap<>();
		datosRequestRunGetInitialization.put("host", data.get("host"));
		datosRequestRunGetInitialization.put("folio", folio);
		datosRequestRunGetInitialization.put("plaza", data.get("plaza"));
		datosRequestRunGetInitialization.put("tienda", data.get("tienda"));
		datosRequestRunGetInitialization.put("caja", data.get("caja"));
		datosRequestRunGetInitialization.put("tokenES", data.get("tokenES"));
		datosRequestRunGetInitialization.put("tokenEW", data.get("tokenEW"));
		datosRequestRunGetInitialization.put("pvDate", creationDate);
		datosRequestRunGetInitialization.put("adDate", creationDate.substring(0, 8));
		datosRequestRunGetInitialization.put("tokenC4",data.get("tokenC4"));
		
		String request = GetRequestFile.getRequestFile(RequestKeys, datosRequestRunGetInitialization);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio RunGetInitialization funciona correctamente ");

		String response = GetRequest.executeGetRequest(request);		
		testCase.addTextEvidenceCurrentStep("\n"+response);
		return response;
		}
		
		
	public String ejecutarRunGetAck(String folio, String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
	    HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
	    datosRequestRunGetAuthAck.put("host", data.get("host"));
	    datosRequestRunGetAuthAck.put("folio", folio);
	    datosRequestRunGetAuthAck.put("creationDate", creationDate);
	    datosRequestRunGetAuthAck.put("ack", data.get("ack"));

	
	    String request = GetRequestFile.getRequestFile(runGetAckPath, datosRequestRunGetAuthAck);
	    System.out.println(request);
	    testCase.addTextEvidenceCurrentStep("\n"+request);
	    testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	    testCase.addBoldTextEvidenceCurrentStep("El servicio getAuthAck funciona correctamente ");

		

		String response = GetRequest.executeGetRequest(request);		
		testCase.addTextEvidenceCurrentStep("\n"+response);
	
	return response;
}
	
	
	}	
	
	
	

