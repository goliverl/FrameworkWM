package om;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;

public class PE6 {
	
	public String requestPE6 = "http://%s/invoke/PE6.Pub/%s?";
	private final String runGetFolioPath = "PE6\\RunGetFolio.txt";
	private final String runGetFolioCorresponsaliaPath = "PE6\\RunGetFolioCorresponsalia.txt";
	private final String runGetAuthPath = "PE6\\RunGetAuth.txt";
	private final String runGetAuthAckPath = "PE6\\RunGetAuthAck.txt";
	private final String runGetCCPath = "PE6\\RunGetCC.txt";
	private final String runBines = "PE6\\pe6Bines.txt";
	private final String SolicitaAutorizacion = "PE6\\SolicitaAut.txt";
	private final String ConfirmACK = "PE6\\ConfirmacionACK.txt";
	private final String SolicAutTar = "PE6\\SolicitaAutNumTarj.txt";
	
	
	//utilerias
	public final String consultaBin="SELECT * FROM (SELECT DISTINCT PROM_TYPE, BIN,BANK,ACQUIRER,entry_mode, track1, card_type FROM TPEUSER.TDC_TRANSACTION WHERE track1 = '%s')";
	
	public final String consultaAPPLY_DEP="SELECT BIN,STATUS, BANK_ID, BANK_DESC, BANK_CODE, APPLY_DEP from  TPEUSER.TDC_BIN where bin='%s'";
	public final String consultaProc_code="SELECT * FROM TPEUSER.TDC_ROUTING WHERE PROC_CODE='PAY' and BIN = '%s'";
	public final String consultaTarjetaBloqueada="select * from TPEUSER.TDC_BLACK_LIST where track1='%s'";
	public final String consultaCuentaBloqueada="select CREATION_DATE,CARD_NO,BLOQUED_TIME,PLAZA from TPEUSER.TDC_BLOQUED_ACCOUNT where card_no='%s'";
	public final String consultaTiendaBloqueada="SELECT creation_date,store, card_no, bloqued_time FROM TPEUSER.TDC_BLOQUED_STORE WHERE card_no = '%s'"; 
	private final String fe6Query = "SELECT folio, wm_code FROM tpeuser.tdc_transaction where folio = %s";
	private final String fe6CCQuery = "SELECT folio, wm_code, critic_case FROM tpeuser.tdc_transaction where folio = %s";
	public HashMap<String,String> data;
	public SQLUtil db;
	TestCase testCase;
	boolean tipoDePrueba;
	
	public boolean tipoDePrueba(SQLUtil db) throws SQLException {
		
		String tdcBlackListQuery = String.format(consultaTarjetaBloqueada, data.get("cardNo"));
		SQLResult result = db.executeQuery(tdcBlackListQuery);
		
		tipoDePrueba = result.isEmpty();
		
		return tipoDePrueba;
		
	}

	

	private String folio;
	private String creationDate;
	private String wmCode;
	private String dbWmCode;
	private String dbCriticCase;
	
	public String requestUrlBines = "http://%s/invoke/PE6.Pub/runGetQuery?";
	
	
	//cambios nuevos 
	public PE6 (HashMap<String, String> data, TestCase testCase, SQLUtil db2) {
		this.data = data;
		this.db = db2;
		this.testCase = testCase;
		
		
		
	}
	
	/**
	 * Ejecuta request RunGetFolio de la PE6 y regresa el xml de response como Document (w3c)
	 * @return response como Document
	 * @throws ReempRequestException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String runGetFolio_request() throws ReempRequestException, ClientProtocolException, IOException {

		HashMap<String, String> datosRequestRunGetFolio = new HashMap<>();
		datosRequestRunGetFolio.put("user", data.get("user"));
		datosRequestRunGetFolio.put("ps", data.get("ps"));
	    datosRequestRunGetFolio.put("host", data.get("host"));
	    datosRequestRunGetFolio.put("plaza", data.get("plaza"));
	    datosRequestRunGetFolio.put("tienda", data.get("tienda"));
	    datosRequestRunGetFolio.put("caja", data.get("caja"));
	    datosRequestRunGetFolio.put("type", data.get("type"));
	    
		//Obtener URL del request a ejecutar
	    String runGetFolioRequest = GetRequestFile.getRequestFile(runGetFolioPath, datosRequestRunGetFolio);

		System.out.println(runGetFolioRequest);
		//Ejecutar el request 
		testCase.addTextEvidenceCurrentStep("\n"+runGetFolioRequest);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera obteniendo un número de folio:");
		String responseRunGetFolio = GetRequest.executeGetRequest(runGetFolioRequest);
		Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetFolio);
		System.out.println("XML: "+responseRunGetFolio);
		folio = xml.getElementsByTagName("folio").item(0).getTextContent();
		creationDate = xml.getElementsByTagName ("creationDate").item(0).getTextContent();
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent();
	 	testCase.addTextEvidenceCurrentStep("\n"+responseRunGetFolio);
	 	System.out.println("Response=  "+responseRunGetFolio);
		return responseRunGetFolio;
	}
	

	
	public String runGetAuth_request() throws ReempRequestException, ClientProtocolException, IOException {
		 HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		 char[] cArray = creationDate.toCharArray();
		   int x=0;
		   String adDate ="";
		   while(x<=7) {
			   adDate=adDate+cArray[x];
			   x++;
		   }
		   	String tarjeta = data.get("Name");
		    datosRequestRunGetAuth.put("host", data.get("hostAuth"));
		    datosRequestRunGetAuth.put("folio", folio);
		    datosRequestRunGetAuth.put("creationDate", creationDate);
		    datosRequestRunGetAuth.put("pvDate", creationDate);
		    datosRequestRunGetAuth.put("adDate", adDate);
		    datosRequestRunGetAuth.put("cardNo", data.get("cardNo"));
		    datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		    datosRequestRunGetAuth.put("promType", data.get("promType"));
		    datosRequestRunGetAuth.put("amount", data.get("amount"));
		    datosRequestRunGetAuth.put("operator", data.get("operator"));
		    datosRequestRunGetAuth.put("serviceId", data.get("serviceId"));
		    datosRequestRunGetAuth.put("accountType", data.get("accountType"));
		    datosRequestRunGetAuth.put("accountNo", data.get("accountNo"));
		    datosRequestRunGetAuth.put("bankId", data.get("bankId"));
		    datosRequestRunGetAuth.put("track2", data.get("track2"));
		
			String runGetAuthRequest = GetRequestFile.getRequestFile(runGetAuthPath, datosRequestRunGetAuth);
			System.out.println(runGetAuthRequest);
			testCase.addTextEvidenceCurrentStep("\n"+runGetAuthRequest);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			String wmcode="119";
			boolean boton =data.get("WMCodeGetAuthAck").equals(wmcode);
			if(boton) {
			testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un ACK rechazado por bloqueo para una transacción de "+tarjeta+" con wmCode '119':");
			}else {
				testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:  ");
			}
			//Ejecutar el request 
			String responseRunGetAuth = GetRequest.executeGetRequest(runGetAuthRequest);
			Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);
			wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent();
			//Añadir evidencia de la respuesta al testCase
			testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuth);
		 	System.out.println("Response=  "+responseRunGetAuth);

			return responseRunGetAuth;
	}
	
	
	public String runGetAuth_requestWM() throws ReempRequestException, ClientProtocolException, IOException {
		 HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		 char[] cArray = creationDate.toCharArray();
		   int x=0;
		   String adDate ="";
		   while(x<=7) {
			   adDate=adDate+cArray[x];
			   x++;
		   }
		    datosRequestRunGetAuth.put("user", data.get("user"));
		    datosRequestRunGetAuth.put("ps", data.get("ps"));
		   	datosRequestRunGetAuth.put("host", data.get("hostAuth"));
		    datosRequestRunGetAuth.put("folio", folio);
		    datosRequestRunGetAuth.put("creationDate", creationDate);
		    datosRequestRunGetAuth.put("pvDate", creationDate);
		    datosRequestRunGetAuth.put("adDate", adDate);
		    datosRequestRunGetAuth.put("cardNo", data.get("cardNo"));
		    datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		    datosRequestRunGetAuth.put("track2", data.get("track2"));
		    datosRequestRunGetAuth.put("promType", data.get("promType"));
		    datosRequestRunGetAuth.put("amount", data.get("amount"));
		    datosRequestRunGetAuth.put("operator", data.get("operator"));
		    datosRequestRunGetAuth.put("serviceId", data.get("serviceId"));
		    datosRequestRunGetAuth.put("accountType", data.get("accountType"));
		    datosRequestRunGetAuth.put("accountNo", data.get("accountNo"));
		    datosRequestRunGetAuth.put("bankId", data.get("bankId"));
		    
		
			String runGetAuthRequest = GetRequestFile.getRequestFile(SolicAutTar, datosRequestRunGetAuth);
			System.out.println(runGetAuthRequest);
			testCase.addTextEvidenceCurrentStep("\n"+runGetAuthRequest);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			
			//Ejecutar el request 
			String responseRunGetAuth = GetRequest.executeGetRequest(runGetAuthRequest);
			Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);
			wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent();
			//Añadir evidencia de la respuesta al testCase
			testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuth);
		 	System.out.println("Response=  "+responseRunGetAuth);

			return responseRunGetAuth;
	}
	
	public String runGetAuth_requestWM_NumCel(String FOLIO,String CreationDat) throws ReempRequestException, ClientProtocolException, IOException {
		 HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		 char[] cArray = creationDate.toCharArray();
		   int x=0;
		   String adDate ="";
		   while(x<=7) {
			   adDate=adDate+cArray[x];
			   x++;
		   }
		    datosRequestRunGetAuth.put("user", data.get("user"));
		    datosRequestRunGetAuth.put("ps", data.get("ps"));
		   	datosRequestRunGetAuth.put("host", data.get("hostAuth"));
		    datosRequestRunGetAuth.put("folio", FOLIO);
		    datosRequestRunGetAuth.put("creationDate", CreationDat);
		    datosRequestRunGetAuth.put("pvDate", CreationDat);
		    datosRequestRunGetAuth.put("adDate", adDate);
		    datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		    datosRequestRunGetAuth.put("promType", data.get("promType"));
		    datosRequestRunGetAuth.put("amount", data.get("amount"));
		    datosRequestRunGetAuth.put("operator", data.get("operator"));
		    datosRequestRunGetAuth.put("serviceId", data.get("serviceId"));
		    datosRequestRunGetAuth.put("accountType", data.get("accountType"));
		    datosRequestRunGetAuth.put("accountNo", data.get("accountNo"));
		    datosRequestRunGetAuth.put("bankId", data.get("bankId"));
		
			String runGetAuthRequest = GetRequestFile.getRequestFile(SolicitaAutorizacion, datosRequestRunGetAuth);
			System.out.println(runGetAuthRequest);
			testCase.addTextEvidenceCurrentStep("\n"+runGetAuthRequest);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			
			//Ejecutar el request 
			String responseRunGetAuth = GetRequest.executeGetRequest(runGetAuthRequest);
			Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);
			wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent();
			//Añadir evidencia de la respuesta al testCase
			testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuth);
		 	System.out.println("Response=  "+responseRunGetAuth);

			return responseRunGetAuth;
	}
	
	
	public String runGetAuthAck_request() throws ReempRequestException, ClientProtocolException, IOException {
	
		HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
	    datosRequestRunGetAuthAck.put("host", data.get("hostAck"));
	    datosRequestRunGetAuthAck.put("folio", folio);
	    datosRequestRunGetAuthAck.put("creationDate", creationDate);
	    datosRequestRunGetAuthAck.put("ack", data.get("ack"));
	    datosRequestRunGetAuthAck.put("track2", data.get("track2"));
	    String tarjeta = data.get("Name");
	    String corresponsalia = data.get("WMCodeGetFolioC");
		String runGetAuthAckRequest = GetRequestFile.getRequestFile(runGetAuthAckPath, datosRequestRunGetAuthAck);
		System.out.println(runGetAuthAckRequest);
		testCase.addTextEvidenceCurrentStep("\n"+runGetAuthAckRequest);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		String wmcode="102";
		boolean boton =data.get("WMCodeGetAuthAck").equals(wmcode);
		if(boton) {
			testCase.addBoldTextEvidenceCurrentStep("El web service confirma de manera negativa el "+tarjeta+" generando la reversa de la siguiente manera:");
		}else {
			if(corresponsalia.equals("101")) {
				
				testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un ACK exitoso para una transacción de "+tarjeta+" de la siguiente manera:");
			
			} else {
				testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un ACK exitoso para una transacción de "+tarjeta+" con wmCode '101':");
			}
		
		}
		String responseRunGetAuthAck = GetRequest.executeGetRequest(runGetAuthAckRequest);
		//Ejecutar el request 
		Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent(); 
		//Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuthAck);
	 	System.out.println("Response=  "+responseRunGetAuthAck);

		return responseRunGetAuthAck;
	}

	
	
	public String runGetAuthAck_requestWM() throws ReempRequestException, ClientProtocolException, IOException {
		HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
		datosRequestRunGetAuthAck.put("user", data.get("user"));
		datosRequestRunGetAuthAck.put("ps", data.get("ps"));
	    datosRequestRunGetAuthAck.put("host", data.get("hostAck"));
	    datosRequestRunGetAuthAck.put("folio", folio);
	    datosRequestRunGetAuthAck.put("creationDate", creationDate);
	    datosRequestRunGetAuthAck.put("ack", data.get("ack"));
	    datosRequestRunGetAuthAck.put("track2", data.get("track2"));

		String runGetAuthAckRequest = GetRequestFile.getRequestFile(runGetAuthAckPath, datosRequestRunGetAuthAck);
		System.out.println(runGetAuthAckRequest);
		
		String responseRunGetAuthAck = GetRequest.executeGetRequest(runGetAuthAckRequest);
		//Ejecutar el request 
		Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent(); 
		//Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuthAck);
	 	System.out.println("Response=  "+responseRunGetAuthAck);

		return responseRunGetAuthAck;
	}
	
	public String runGetAuthAck_requestWM_NumCel() throws ReempRequestException, ClientProtocolException, IOException {
	
		HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
		datosRequestRunGetAuthAck.put("user", data.get("user"));
		datosRequestRunGetAuthAck.put("ps", data.get("ps"));
	    datosRequestRunGetAuthAck.put("host", data.get("hostAck"));
	    datosRequestRunGetAuthAck.put("folio", folio);
	    datosRequestRunGetAuthAck.put("creationDate", creationDate);
	    datosRequestRunGetAuthAck.put("ack", data.get("ack"));

		String runGetAuthAckRequest = GetRequestFile.getRequestFile(ConfirmACK, datosRequestRunGetAuthAck);
		System.out.println(runGetAuthAckRequest);
		
		String responseRunGetAuthAck = GetRequest.executeGetRequest(runGetAuthAckRequest);
		//Ejecutar el request 
		Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent(); 
		//Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep("\n"+responseRunGetAuthAck);
	 	System.out.println("Response=  "+responseRunGetAuthAck);

		return responseRunGetAuthAck;
	}
	
	
	
	public String runGetFolioCorresponsalia_request()
		throws ReempRequestException, ClientProtocolException, IOException {
		String url = String.format(requestPE6, data.get("hostFolioCorresponsalia"),"runGetCC");
		OxxoRequest or = new OxxoRequest(url);
	
		//or.addValue("host", GlobalVariables.HOST_OLS);
		or.addValue("application", "TDC");
		or.addValue("operation", "PAY");
		or.addValue("folio",folio);
		or.addValue("track2", data.get("track2"));
		or.addValue("cvv","PAY" );
		or.addValue("creationDate", creationDate);
	

		String request = or.getRequestValuesString();
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		String responseRunGetFolioCorresponsalia = GetRequest.executeGetRequest(request);
		System.out.println(responseRunGetFolioCorresponsalia);

		return responseRunGetFolioCorresponsalia;

	}	
	public String runGetCC_request() throws ReempRequestException, ClientProtocolException, IOException {
		HashMap<String, String> datosRequestRunGetCC = new HashMap<>();
		datosRequestRunGetCC.put("host", data.get("host"));
		datosRequestRunGetCC.put("folio", data.get("folio"));
		datosRequestRunGetCC.put("creationDate",data.get("creationDate"));
		
		String runGetCCRequest = GetRequestFile.getRequestFile(runGetCCPath, datosRequestRunGetCC);
		System.out.println(runGetCCRequest);
		
		String responseRunGetCC = GetRequest.executeGetRequest(runGetCCRequest);
		
		Document xml = ReadRequest.convertStringToXMLDocument(responseRunGetCC);
		//folio = xml.getElementsByTagName("folio").item(0).getTextContent();
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent(); 
		return responseRunGetCC;
		
	}
	
	public String consultaBines() throws ClientProtocolException, IOException, ReempRequestException {
		HashMap<String, String> datosRequestBines = new HashMap<>();
		String url = String.format(requestUrlBines, GlobalVariables.HOST);
		
		datosRequestBines.put("cardNo",data.get("cardNo"));
		datosRequestBines.put("binCodeList", data.get("binCodeList"));	
		datosRequestBines.put("plaza", data.get("plaza"));
		datosRequestBines.put("tienda", data.get("tienda"));
		datosRequestBines.put("promType", data.get("promType"));
		datosRequestBines.put("service", data.get("service"));
		
				
		
		
		String binesRequest = GetRequestFile.getRequestFile(runBines, datosRequestBines);
		
		System.out.println(binesRequest);
		
		String responseBines = GetRequest.executeGetRequest(binesRequest);
		
		Document xml = ReadRequest.convertStringToXMLDocument(responseBines);
		folio = xml.getElementsByTagName("folio").item(0).getTextContent();
		wmCode = xml.getElementsByTagName ("wmCode").item(0).getTextContent(); 
	
		
		return responseBines;
	}
	
	
	
	public void validaConfiguracionAmbientePositivo(SQLUtil db) throws SQLException {
		//paso 1
		testCase.nextStep("Conseguir Bin de la tarjeta a utilizar en TPEUSER.TDC_TRANSACTION");
		String cardNo=data.get("cardNo");
		String executeConsultaBin = String.format(consultaBin, cardNo);
		System.out.println(executeConsultaBin);

		SQLResult resultadoBin = db.executeQuery(executeConsultaBin);
        String bin = resultadoBin.getData(0, "BIN");
		boolean validaBin = resultadoBin.isEmpty();

		System.out.println(validaBin);

		if (!validaBin) {
			
			testCase.addTextEvidenceCurrentStep("Bin encontrado: "+bin);
			testCase.addQueryEvidenceCurrentStep(resultadoBin);
		}else {
			testCase.addTextEvidenceCurrentStep("Bin no encontrado");                                

		}
		//paso 2
		testCase.nextStep("Valida que App_dep este en Y en la tabla TPEUSER.TDC_BIN");
		String executeconsultaAPPLY_DEP = String.format(consultaAPPLY_DEP, bin);
		System.out.println(executeconsultaAPPLY_DEP);

		SQLResult resultadoExecuteConsultaAPPLY_DEP = db.executeQuery(executeconsultaAPPLY_DEP);
        String APPLY_DEP = resultadoExecuteConsultaAPPLY_DEP.getData(0, "APPLY_DEP");
		boolean validaApply_dep = APPLY_DEP.equals("Y");

		System.out.println(validaApply_dep);

		if (validaApply_dep) {
			testCase.addTextEvidenceCurrentStep("APPLY_DEP si es Y");                             
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

		}else {
			testCase.addTextEvidenceCurrentStep("APPLY_DEP no es Y");                                
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaAPPLY_DEP);

		}
		//paso 3
		testCase.nextStep("Valida que Proc_code este en PAY en la tabla TPEUSER.TDC_ROUTING");
		String executeconsultaProc_code = String.format(consultaProc_code, bin);
		System.out.println(executeconsultaProc_code);
		SQLResult resultadoExecuteConsultaProc_code = db.executeQuery(executeconsultaProc_code);
        String Proc_code = resultadoExecuteConsultaAPPLY_DEP.getData(0, "APPLY_DEP");
		boolean validaProc_code = Proc_code.equals("PAY");
		System.out.println(validaProc_code);

		if (!validaProc_code) {
			testCase.addTextEvidenceCurrentStep("Proc_code si es PAY");                                
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);
 
		}else {
			testCase.addTextEvidenceCurrentStep("Proc_code no es PAY");                                
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaProc_code);

		}
		
	
	
		
	}
	
	public void validaConfiguracionAmbienteNegativo(SQLUtil db) throws SQLException {
		//paso1
		validaConfiguracionAmbientePositivo(db);
		// paso 2
		String cardNo = data.get("cardNo");
		testCase.nextStep("Valida que el registro este en la tabla TPEUSER.TDC_BLACK_LIST");
		String executeConsultaTarjetaBloqueada = String.format(consultaTarjetaBloqueada, cardNo);
		System.out.println(executeConsultaTarjetaBloqueada);
		SQLResult resultadoExecuteConsultaTarjetaBloqueada = db.executeQuery(executeConsultaTarjetaBloqueada);
		boolean validaTarjeta = resultadoExecuteConsultaTarjetaBloqueada.isEmpty();
		System.out.println(validaTarjeta);

		if (!validaTarjeta) {
			testCase.addTextEvidenceCurrentStep("La tarjeta esta registrada en TPEUSER.TDC_BLACK_LIST");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

		} else {
			testCase.addTextEvidenceCurrentStep("La tarjeta no esta registrada en TPEUSER.TDC_BLACK_LIST");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTarjetaBloqueada);

		}
		// paso 3
		testCase.nextStep("Valida que el registro este en la tabla TPEUSER.TDC_BLOQUED_STORE");
		String executeCuentaTiendaBloqueada = String.format(consultaTiendaBloqueada, cardNo);
		System.out.println(executeCuentaTiendaBloqueada);
		SQLResult resultadoExecuteConsultaTiendaBloqueada = db.executeQuery(executeCuentaTiendaBloqueada);
		boolean validaTienda = resultadoExecuteConsultaTiendaBloqueada.isEmpty();
		System.out.println(validaTienda);

		if (!validaTienda) {
			testCase.addTextEvidenceCurrentStep("La tarjeta esta registrada en TPEUSER.TDC_BLOQUED_STORE");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

		} else {
			testCase.addTextEvidenceCurrentStep("La tarjeta no esta registrada en TPEUSER.TDC_BLOQUED_STORE");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaTiendaBloqueada);

		}
		// paso 4
		testCase.nextStep("Valida que el registro este en la tabla TPEUSER.TDC_BLOQUED_ACCOUNT");
		String executeCuentaTarjetaBloqueada = String.format(consultaCuentaBloqueada, cardNo);
		System.out.println(executeCuentaTarjetaBloqueada);
		SQLResult resultadoExecuteConsultaCuentaBloqueada = db.executeQuery(executeCuentaTarjetaBloqueada);
		boolean validaCuenta = resultadoExecuteConsultaCuentaBloqueada.isEmpty();
		System.out.println(validaCuenta);

		if (!validaCuenta) {
			testCase.addTextEvidenceCurrentStep("La tarjeta esta registrada en TPEUSER.TDC_BLOQUED_ACCOUNT");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

		} else {
			testCase.addTextEvidenceCurrentStep("La tarjeta no esta registrada en TPEUSER.TDC_BLOQUED_ACCOUNT");
			testCase.addQueryEvidenceCurrentStep(resultadoExecuteConsultaCuentaBloqueada);

		}

	}
	
	
	
	
	
	
	
	public String getFolio(){
		return folio;
	}
	
	public String getCreationDate(){
		return creationDate;
	}
	
 	public String getWmCode() {
		return wmCode;
	}
	
	public SQLResult runBDQuery(String folio) throws SQLException {
	    return db.executeQuery( String.format(fe6Query, folio));
	}
	
	public String getWmCodeFromQuery() throws SQLException {
		SQLResult result = db.executeQuery( String.format(fe6Query, folio));
		//result.next();
		dbWmCode =  result.getData(0, "wm_code");
		return dbWmCode;
		
	}
	
	public String getCriticCaseFromQuery() throws SQLException {
		
		
		String queryFormat =  String.format(fe6CCQuery,data.get("folio"));
		System.out.println(queryFormat);
		
		SQLResult result = db.executeQuery( String.format(fe6CCQuery,data.get("folio")));
		
		
		
		//result.next();
		dbWmCode =  result.getData(0, "wm_code");
		dbCriticCase =  result.getData(0, "critic_case");
		return dbCriticCase;
	}
	
	public String getDbWmCode() {
		return dbWmCode;	
	}
	
	public String getDbCriticCase() {
		return dbCriticCase;
	}
}
