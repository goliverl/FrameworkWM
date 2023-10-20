package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import utils.password.PasswordUtil;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class FCP_WMx86 {
	public String runGetFolio = "FCP\\GenerarFolioFCP.txt";
	public String runGetAut = "FCP\\GenerarAutorizacion.txt";
	public String runGetAck = "FCP\\GenerarAck.txt";
	public String runGetAutRep = "FCP\\GeneraAutorizacionRep.txt";
	public String runConsultaCupon = "FCP\\ConsultaCupon.txt";
	public String runRedencionCupon = "FCP\\RedencionCupon.txt";
  public String ejecutarTRN02_WU = "TPE_TSF\\AuthDispersionWU.txt";
	public String ejecutarTRN02_MG = "TPE_TSF\\AuthDispersionMG.TXT";
	
	String creationDate;

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;

	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
																			// de
																			// la
																			// fecha

	String date = formatter.format(fecha);

	public FCP_WMx86(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;

	}
  public String ejecutarTRN02_MG(String folio, String creationDate)  throws ReempRequestException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", host);
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", "TRN02");
		datosRequest.put("source", data.get("source"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("adDate", creationDate.substring(0, 8));
		datosRequest.put("pvDate", creationDate);
		datosRequest.put("folio", folio);
		datosRequest.put("creationDate", creationDate);
		datosRequest.put("operator", data.get("operator"));
		
		datosRequest.put("agentCheckAmount", data.get("agentCheckAmount"));
		datosRequest.put("originalReference", data.get("originalReference"));
		datosRequest.put("transferNo", data.get("transferNo"));
		datosRequest.put("proveedor", data.get("proveedor"));
		datosRequest.put("marca", data.get("marca"));
		datosRequest.put("originalSendFee", data.get("originalSendFee"));
		datosRequest.put("countryCode", data.get("countryCode"));
		datosRequest.put("originatingAmount", data.get("originatingAmount"));
		datosRequest.put("originatingCountry", data.get("originatingCountry"));
		datosRequest.put("originatingCurrency", data.get("originatingCurrency"));
		datosRequest.put("paymentCurrency", data.get("paymentCurrency"));
		datosRequest.put("amount", data.get("amount"));
		datosRequest.put("paymentType", data.get("paymentType"));
		datosRequest.put("transferID", data.get("transferID"));
		datosRequest.put("ePayment", data.get("ePayment"));
		datosRequest.put("ePaymentSaldazo", data.get("ePaymentSaldazo"));
		datosRequest.put("isNewSaldazo", data.get("isNewSaldazo"));
		datosRequest.put("saldazoCharge", data.get("saldazoCharge"));
		datosRequest.put("commission", data.get("commission"));
		datosRequest.put("cashAmount", data.get("cashAmount"));
		datosRequest.put("eAmount", data.get("eAmount"));
		
		datosRequest.put("idOperador", data.get("idOperador"));
		datosRequest.put("tipoNomOperador", data.get("tipoNomOperador"));
		datosRequest.put("nombreOperador", data.get("nombreOperador"));
		datosRequest.put("apPaternoOperador", data.get("apPaternoOperador"));
		
		datosRequest.put("firstId", data.get("firstId"));
		datosRequest.put("firstIdnum", data.get("firstIdnum"));
		datosRequest.put("paisId", data.get("paisId"));
		datosRequest.put("fechaNacimiento", data.get("fechaNacimiento"));
		datosRequest.put("calleNum", data.get("calleNum"));
		datosRequest.put("colonia", data.get("colonia"));
		datosRequest.put("ciudad", data.get("ciudad"));
		datosRequest.put("estado", data.get("estado"));
		datosRequest.put("codigoPostal", data.get("codigoPostal"));
		datosRequest.put("pais", data.get("pais"));
		datosRequest.put("telefono", data.get("telefono"));
		datosRequest.put("paisNacimiento", data.get("paisNacimiento"));
		datosRequest.put("paisNacionalidad", data.get("paisNacionalidad"));
		datosRequest.put("expiracionFirstId", data.get("expiracionFirstId"));
		
		datosRequest.put("estadoId", data.get("estadoId"));
		datosRequest.put("nombre", data.get("nombre"));
		datosRequest.put("apellidoPaterno", data.get("apellidoPaterno"));
		datosRequest.put("apellidoMaterno", data.get("apellidoMaterno"));
		
		String request = GetRequestFile.getRequestFile(ejecutarTRN02_MG, datosRequest);
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = GetRequest.executeGetRequest(request);
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	
public String ejecutarTRN02_WU(String folio, String creationDate, String originalReference)  throws ReempRequestException, ClientProtocolException, IOException {
	String host = PasswordUtil.decryptPassword(data.get("host"));
	
	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", host);
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", "TRN02");
	datosRequest.put("source", data.get("source"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", creationDate);
	datosRequest.put("pvDate", creationDate.substring(0, 8));
	datosRequest.put("folio", folio);
	datosRequest.put("creationDate", creationDate);
	datosRequest.put("operator", data.get("operator"));
	
	datosRequest.put("originalReference", originalReference);
	datosRequest.put("transferNo", data.get("transferNo"));
	datosRequest.put("proveedor", data.get("proveedor"));
	datosRequest.put("marca", data.get("marca"));
	datosRequest.put("payStatus", data.get("payStatus"));
	datosRequest.put("estatus", data.get("estatus"));
	datosRequest.put("countryCode", data.get("countryCode"));
	datosRequest.put("currencyCode", data.get("currencyCode"));
	datosRequest.put("originatingCurrencyCode", data.get("originatingCurrencyCode"));
	datosRequest.put("originatingCountryCode", data.get("originatingCountryCode"));
	datosRequest.put("totalAmount", data.get("totalAmount"));
	datosRequest.put("amount", data.get("amount"));
	datosRequest.put("principalAmount", data.get("principalAmount"));
	datosRequest.put("charges", data.get("charges"));
	datosRequest.put("moneyTransferKey", data.get("moneyTransferKey"));
	datosRequest.put("new_mtcn", data.get("new_mtcn"));
	datosRequest.put("filingDate", data.get("filingDate"));
//	datosRequest.put("originatingCity", data.get("originatingCity"));
//	datosRequest.put("originatingState", data.get("originatingState"));
	datosRequest.put("paymentId", data.get("paymentId"));
	datosRequest.put("ePayment", data.get("ePayment"));
	datosRequest.put("ePaymentSaldazo", data.get("ePaymentSaldazo"));
	datosRequest.put("isNewSaldazo", data.get("isNewSaldazo"));
	datosRequest.put("saldazoCharge", data.get("saldazoCharge"));
	datosRequest.put("commission", data.get("commission"));
	datosRequest.put("cashAmount", data.get("cashAmount"));
	datosRequest.put("eAmount", data.get("eAmount"));
	
	datosRequest.put("idOperador", data.get("idOperador"));
	datosRequest.put("tipoNomOperador", data.get("tipoNomOperador"));
	datosRequest.put("nombreOperador", data.get("nombreOperador"));
	datosRequest.put("apPaternoOperador", data.get("apPaternoOperador"));
	datosRequest.put("apMaternoOperador", data.get("apMaternoOperador"));
	
	datosRequest.put("firstId", data.get("firstId"));
	datosRequest.put("firstIdnum", data.get("firstIdnum"));
	datosRequest.put("paisId", data.get("paisId"));
	datosRequest.put("expDateFirstId", data.get("expDateFirstId"));
	datosRequest.put("fechaNacimiento", data.get("fechaNacimiento"));
	datosRequest.put("calleNum", data.get("calleNum"));
	datosRequest.put("colonia", data.get("colonia"));
	datosRequest.put("ciudad", data.get("ciudad"));
	datosRequest.put("estado", data.get("estado"));
	datosRequest.put("codigoPostal", data.get("codigoPostal"));
	datosRequest.put("pais", data.get("pais"));
	datosRequest.put("telefono", data.get("telefono"));
	datosRequest.put("paisNacimiento", data.get("paisNacimiento"));
	datosRequest.put("paisNacionalidad", data.get("paisNacionalidad"));
	datosRequest.put("expiracionFirstId", data.get("expiracionFirstId"));
	
	datosRequest.put("tipoNombre", data.get("tipoNombre"));
	datosRequest.put("nombre", data.get("nombre"));
	datosRequest.put("apellidoPaterno", data.get("apellidoPaterno"));
	datosRequest.put("apellidoMaterno", data.get("apellidoMaterno"));
	
	String request = GetRequestFile.getRequestFile(ejecutarTRN02_WU, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	String response = GetRequest.executeGetRequest(request);
	System.out.println(response);
	testCase.addTextEvidenceCurrentStep(response);
	return response;
}
public String generacionFolioTransaccion3() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();

		
		datosRequest.put("host", data.get("host"));
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", data.get("operation01"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		
		
		String request = GetRequestFile.getRequestFile(runGetFolio, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de folio proporciona un folio de forma exitosa:");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;	
	}

public String Consultacupon() throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation01"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("ad_date", date.substring(0, 8));
	datosRequest.put("pv_date", date);
	datosRequest.put("CardID", data.get("CardID"));
	datosRequest.put("emissionId", data.get("emissionId"));
	datosRequest.put("barCode", data.get("barCode"));
	

	
	String request = GetRequestFile.getRequestFile(runConsultaCupon, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de folio proporciona un folio de forma exitosa:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
	
}

public String generarAutorizacion(String Folio, String CreatioDate) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", Folio);
	datosRequest.put("creationdate", CreatioDate);
//	datosRequest.put("plaza", data.get("plaza"));
//	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("ad_date", date.substring(0, 8));
	datosRequest.put("pv_date", date);
//	datosRequest.put("serviceId", data.get("serviceId"));	
	datosRequest.put("pvTicket", data.get("pvTicket"));
	datosRequest.put("operator", data.get("operator"));
	datosRequest.put("cardNo",data.get("cardNo"));
	datosRequest.put("activationType",data.get("activationType"));
	datosRequest.put("entryMode",data.get("entryMode"));
	datosRequest.put("nombre",data.get("nombre"));
	datosRequest.put("apellidoPaterno",data.get("apellidoPaterno"));
	datosRequest.put("apellidoMaterno",data.get("apellidoMaterno"));
	datosRequest.put("fechaNacimiento",data.get("fechaNacimiento"));
	datosRequest.put("calle",data.get("calle"));
	datosRequest.put("numext",data.get("numext"));
	datosRequest.put("numint",data.get("numint"));
	datosRequest.put("colonia",data.get("colonia"));
	datosRequest.put("delegacionmunicipio",data.get("delegacion"));
	datosRequest.put("ciudadestado",data.get("ciudad"));
	datosRequest.put("codigopostal",data.get("cp"));
	datosRequest.put("genero",data.get("genero"));
	datosRequest.put("entidadfederativa",data.get("entidadfed"));
	datosRequest.put("tipodeidentificacion",data.get("tipoidentificacion"));
	datosRequest.put("numerodeidentificacion",data.get("noindentificacion"));
	datosRequest.put("celular",data.get("celular"));
	datosRequest.put("promocel",data.get("promocel"));
	
	String request = GetRequestFile.getRequestFile(runGetAut, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("Se muestra  la respuesta de la solicitud de autorización de FCP correctamente:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
		
}


public String generarAutorizacionREP(String Folio, String CreationDate) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operationAut"));
	datosRequest.put("source", data.get("sourceAut"));
	datosRequest.put("folio", Folio);
	datosRequest.put("creationdate", CreationDate);
	datosRequest.put("ad_date", date.substring(0, 8));
	datosRequest.put("pv_date", date);
	datosRequest.put("pvTicket", data.get("pvTicket"));
	datosRequest.put("operator", data.get("operator"));
	datosRequest.put("cardNo",data.get("cardNo"));
	datosRequest.put("activationType",data.get("activationType"));
	datosRequest.put("contrato",data.get("contrato"));
	datosRequest.put("bankFolio",data.get("bankFolio"));
	datosRequest.put("entryMode",data.get("entryMode"));
	datosRequest.put("promocel",data.get("promocel"));
	
	String request = GetRequestFile.getRequestFile(runGetAutRep, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("Se muestra  la respuesta de la solicitud de autorización de FCP correctamente:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
		
}

    public String generarRedencionCup(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
    	
    	HashMap<String, String> datosRequest = new HashMap<>();

    	datosRequest.put("host", data.get("host"));
    	datosRequest.put("application", data.get("application"));
    	datosRequest.put("entity", data.get("entityAut"));
    	datosRequest.put("operation", data.get("operationAut"));
    	datosRequest.put("folio", Folio);
    	datosRequest.put("source", data.get("sourceAut"));
    	datosRequest.put("plaza", data.get("plaza"));
    	datosRequest.put("tienda", data.get("tienda"));
    	datosRequest.put("caja", data.get("caja"));
    	datosRequest.put("ad_date", date.substring(0, 8));
    	datosRequest.put("pv_date", date);
    	datosRequest.put("id", data.get("CardID"));
    	datosRequest.put("emissionId", data.get("emissionIdAut"));
    	datosRequest.put("barCode", data.get("barCodeAut"));
    	datosRequest.put("promid", data.get("PromId"));
    	datosRequest.put("ticket", data.get("Ticket"));
    	datosRequest.put("payDate", data.get("payDate"));
    	datosRequest.put("hour", data.get("hour"));
    	datosRequest.put("qty", data.get("qty"));
    	datosRequest.put("version", data.get("version"));
    
    	
    	String request = GetRequestFile.getRequestFile(runRedencionCupon, datosRequest);
    	System.out.println(request);
    	testCase.addTextEvidenceCurrentStep("\n" + request);
    	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
    	testCase.addBoldTextEvidenceCurrentStep("Se muestra  la respuesta de la solicitud de autorización de FCP correctamente:");
    	String response = GetRequest.executeGetRequest(request);
    	testCase.addTextEvidenceCurrentStep("\n" + response);

    	return response;
    		
    }
    
    
public String generacionACK(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();
	
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operationack"));
	datosRequest.put("folio", Folio);
	datosRequest.put("ackval", data.get("ackvalue"));
	
	String request = GetRequestFile.getRequestFile(runGetAck, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("Ejecutar el servicio  de la FCP  para confirmar el ACK de la transaccion activacion de tarjeta Saldazo");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
		
}
	
}
