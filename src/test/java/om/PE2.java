package om;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.poi.EncryptedDocumentException;
import org.w3c.dom.Document;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.ExcelUtil;
import config.Constants;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;


public class PE2 {

	public String runGetFolioPath = "PE2\\RunGetFolio.txt";
	public String runGetQueryPath = "PE2\\RunGetQuery.txt";
	public String runGetAuthPath = "PE2\\RunGetAuth.txt";
	public String runGetAuthRetiroPath = "PE2\\RunGetAuthRetiro.txt";
	public String runGetAckPath = "PE2\\RunGetAuthAck.txt";
	public String runGetDevPath = "PE2\\RunGetDev.txt";
	public String runGetDevAckPath = "PE2\\RunGetDevAck.txt";
	
	//utilerias
		public final String consultaBin="SELECT * FROM (SELECT DISTINCT PROM_TYPE, BIN,BANK,ACQUIRER,entry_mode, track1, card_type FROM TPEUSER.TDC_TRANSACTION WHERE track1 = '%s')";
		
		public final String consultaAPPLY_DEP="SELECT APPLY_DEP from  TPEUSER.TDC_BIN where bin='%s'";
		public final String consultaProc_code="SELECT * FROM TPEUSER.TDC_ROUTING WHERE PROC_CODE='PAY' and BIN = '%s'";
		public final String consultaTarjetaBloqueada="select * from TPEUSER.TDC_BLACK_LIST where track1='%s'";
		public final String consultaCuentaBloqueada="select CREATION_DATE,CARD_NO,BLOQUED_TIME,PLAZA from TPEUSER.TDC_BLOQUED_ACCOUNT where card_no='%s'";
		public final String consultaTiendaBloqueada="SELECT creation_date,store, card_no, bloqued_time FROM TPEUSER.TDC_BLOQUED_STORE WHERE card_no = '%s'"; 
		private final String fe6Query = "SELECT folio, wm_code FROM tpeuser.tdc_transaction where folio = %s";
		private final String fe6CCQuery = "SELECT folio, wm_code, critic_case FROM tpeuser.tdc_transaction where folio = %s";
		
		boolean tipoDePrueba;
		
		public boolean tipoDePrueba(SQLUtil db) throws SQLException {
			
			String tdcBlackListQuery = String.format(consultaTarjetaBloqueada, data.get("cardNo"));
			SQLResult result = db.executeQuery(tdcBlackListQuery);
			
			tipoDePrueba = result.isEmpty();
			
			return tipoDePrueba;
			
		}

		
		int validacionesExitosas=0;
		
		public int validacionesExitosas() {
			return validacionesExitosas;
			
		}

		private String folio;
		private String creationDate;
		private String wmCode;
		private String dbWmCode;
		private String dbCriticCase;
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;
	String swAuth;
	
	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
	
	public PE2(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
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
		String wm131="131";
		String DPwm131 = data.get("codigoACK");
		String wmCodeToValidateReverse = data.get("codigoACK");
		String type = data.get("type");
		String ackNegativo = data.get("ack");
		boolean validateAck = wm131.equals(DPwm131);
		
		if(type.equals("D")) {
				
			
			if(ackNegativo.equals("01")) {
				testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un número de folio de la siguiente manera:");

			}else {
			testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera obteniendo un número de folio:");
			}
		}else {
		
		if(validateAck) {
			testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera obteniendo un número de folio:");
		}else {
			if(wmCodeToValidateReverse.equals("111")){
				testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un número de folio de la siguiente manera:");
			}else {
				
				if(ackNegativo.equals("01")) {
					testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un número de folio de la siguiente manera:");

				}else {
					testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera:");

				}
			}
		}
		}
		String response = GetRequest.executeGetRequest(request);		
		testCase.addTextEvidenceCurrentStep("\n"+response);
		
		return response;
		
		
	}
	
	public String ejecutarRunGetAuth(String folio,String creationDate, int numRow) throws ReempRequestException, ClientProtocolException, IOException {
		   
		HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		datosRequestRunGetAuth.put("host", data.get("host"));
		datosRequestRunGetAuth.put("folio", folio);
		datosRequestRunGetAuth.put("pvDate", creationDate);
		datosRequestRunGetAuth.put("adDate", creationDate.substring(0, 8));
		datosRequestRunGetAuth.put("cardNo", data.get("cardNo"));
		datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		datosRequestRunGetAuth.put("track2", data.get("track2"));
		datosRequestRunGetAuth.put("promType", data.get("promType"));
		datosRequestRunGetAuth.put("amount", data.get("amount"));
		datosRequestRunGetAuth.put("operator", data.get("operator"));
		datosRequestRunGetAuth.put("serviceId", data.get("serviceId"));
		datosRequestRunGetAuth.put("creationDate",creationDate);
		datosRequestRunGetAuth.put("Bank_Id", data.get("Bank_Id"));
		System.out.println(data.get("cabAmount"));
		String tipo =data.get("Name");
		String wmCodeToValidateReverse = data.get("codigoACK");
		
		if(!data.get("cabAmount").isEmpty())
		{	
			datosRequestRunGetAuth.put("cabAmount", data.get("cabAmount"));
			 String requestRetiro = GetRequestFile.getRequestFile(runGetAuthRetiroPath, datosRequestRunGetAuth);
				System.out.println(requestRetiro);
				testCase.addTextEvidenceCurrentStep("\n"+requestRetiro);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				String wm111="111";
				String DPwm111 = data.get("codigoACK");
				String retiro = data.get("promType");
				boolean validateAck = wm111.equals(DPwm111);
				if(validateAck) {
					if(retiro.equals("CAB")) {
						testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:");
						}else {
							testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización de "+tipo+" al proveedor:");

						}
					
				}else {
					if(wmCodeToValidateReverse.equals("111")){
						if(retiro.equals("CAB")) {
							testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:");
							}else {
						testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización de "+tipo+" al proveedor:");
						}
					}else {
						if(data.get("ack").equals("01")) {
							testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización de "+tipo+" al proveedor:");

						}else {
							testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:");

						}
				}
				}
				String responseRetiro = GetRequest.executeGetRequest(requestRetiro);	
				testCase.addTextEvidenceCurrentStep("\n"+responseRetiro);
				
				Document xml = ReadRequest.convertStringToXMLDocument(responseRetiro);
				swAuth = xml.getElementsByTagName ("swAuth").item(0).getTextContent();
				addDataToExcelFile(creationDate,creationDate.substring(0, 8),creationDate.substring(0, 8),data.get("cardNo"),data.get("track2"),swAuth,data.get("amount"),numRow);	
			
				return responseRetiro;
		} else {
		
	    String request = GetRequestFile.getRequestFile(runGetAuthPath, datosRequestRunGetAuth);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		String wm111="111";
		String DPwm111 = data.get("codigoACK");
		boolean validateAck = wm111.equals(DPwm111);
		if(validateAck) {
			testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización de "+tipo+" al proveedor:");
		}else {
			if(data.get("ack").equals("01")) {
				testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización de "+tipo+" al proveedor:");
			}else {
			testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:");
			}
			}
			String response = GetRequest.executeGetRequest(request);	
		testCase.addTextEvidenceCurrentStep("\n"+response);
		
		Document xml = ReadRequest.convertStringToXMLDocument(response);
		swAuth = xml.getElementsByTagName ("swAuth").item(0).getTextContent();
		addDataToExcelFile(creationDate,creationDate.substring(0, 8),creationDate.substring(0, 8),data.get("cardNo"),data.get("track2"),swAuth,data.get("amount"),numRow);	
	
		return response;
		}
	}	
	
	
	public static void addDataToExcelFile(String pvDate, String adDate,String adAuthDate,String cardNo,String track2,String swAuth, String amount,int numRow) {
		
		try {	
		    ExcelUtil eu = new ExcelUtil(Constants.DATA_PROVIDER_PATH+"interfaces\\pe2\\PE2Dev.xlsx");
			
		    //Escribir en el documento de excel PE2Dev
		    eu.open();
		    ArrayList<String> sheets = eu.getSheetNames();
		    eu.setCell(sheets.get(0),numRow,5, pvDate);//pvDate
		    eu.setCell(sheets.get(0),numRow,6, adDate);//adDate
		    eu.setCell(sheets.get(0),numRow,7, adAuthDate);//adAuthDate
		    eu.setCell(sheets.get(0),numRow,8, cardNo);//cardNo
		    eu.setCell(sheets.get(0),numRow,11, track2);//track2
		    eu.setCell(sheets.get(0),numRow,12,swAuth);//swAuth
		    eu.setCell(sheets.get(0),numRow,13, amount);//amount
		    eu.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public String ejecutarRunGetDev(String folio) throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
		String date = formatter.format(fecha);
		
		datosRequestRunGetAuth.put("host",data.get("host"));
		datosRequestRunGetAuth.put("folio", folio);
		datosRequestRunGetAuth.put("pvDate", date);
		datosRequestRunGetAuth.put("adAuthDate", date.substring(0, 8) );
		datosRequestRunGetAuth.put("adDate", date.substring(0, 8));
		datosRequestRunGetAuth.put("cardNo",  data.get("cardNo"));
		datosRequestRunGetAuth.put("entryMode", data.get("entryMode"));
		//datosRequestRunGetAuth.put("seqNo", data.get("seqNo"));	  
		datosRequestRunGetAuth.put("track2", data.get("track2"));
		datosRequestRunGetAuth.put("swAuth", data.get("swAuth"));
		datosRequestRunGetAuth.put("amount", data.get("amount"));
		
	    String request = GetRequestFile.getRequestFile(runGetDevPath, datosRequestRunGetAuth);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
    	testCase.addBoldTextEvidenceCurrentStep("El web service responde exitosamente de la siguiente manera al enviar la solicitud de autorización al proveedor:");
		String response = GetRequest.executeGetRequest(request);	
		testCase.addTextEvidenceCurrentStep("\n"+response);
		
		return response;	
	}	
	
	
	public String ejecutarRunGetAck(String folio, String creationDate) throws ReempRequestException, ClientProtocolException, IOException {
			String tipo = data.get("Name");
			String ackNegativo = "01";
			String retiro = data.get("promType");
		    HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
		    datosRequestRunGetAuthAck.put("host", data.get("host"));
		    datosRequestRunGetAuthAck.put("folio", folio);
		    datosRequestRunGetAuthAck.put("ack", data.get("ack"));
		    datosRequestRunGetAuthAck.put("track2", data.get("track2"));
		    datosRequestRunGetAuthAck.put("creationDate", creationDate);
		   
		
		    String request = GetRequestFile.getRequestFile(runGetAckPath, datosRequestRunGetAuthAck);
		    System.out.println(request);
		    testCase.addTextEvidenceCurrentStep("\n"+request);
		    testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		    if(data.get("ack").equals(ackNegativo)) {
		    	testCase.addBoldTextEvidenceCurrentStep("El web service confirma de manera negativa la transacción de "+tipo+" generando la reversa de la transacción y responde de la siguiente manera:");
		    }else {
		    	String tipoconde = data.get("resultadoE");
		    	if(retiro.equals("CAB")) {
					testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un ACK exitoso para una transacción de retiro de efectivo exitosa con wmCode '101':");

		    	}else {
					testCase.addBoldTextEvidenceCurrentStep("El web service proporciona un ACK exitoso para una transacción de "+tipoconde+" con wmCode \"101\":");}

		    	}
		    String response = GetRequest.executeGetRequest(request);
		    testCase.addTextEvidenceCurrentStep("\n"+response);
		
		
		return response;
	}
	
	public String ejecutarRunGetDevAck(String folio) throws ReempRequestException, ClientProtocolException, IOException {
		String tipo = data.get("Name");
	    HashMap<String, String> datosRequestRunGetDevAck = new HashMap<>();
	    datosRequestRunGetDevAck.put("host", data.get("host"));
	    datosRequestRunGetDevAck.put("folio", folio);
	    datosRequestRunGetDevAck.put("ack", data.get("ack"));
	    datosRequestRunGetDevAck.put("cardNo", data.get("track2"));
	    
	    String request = GetRequestFile.getRequestFile(runGetDevAckPath, datosRequestRunGetDevAck);
	    String ackNegativo = data.get("ack");
	    System.out.println(request);
	    testCase.addTextEvidenceCurrentStep("\n"+request);
	    testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	    if(ackNegativo.equals("01")) {
		    testCase.addBoldTextEvidenceCurrentStep("El web service confirma de manera negativa la devolución de "+tipo+" generando la reversa y responde de la siguiente manera:");
	    }else {
	    testCase.addBoldTextEvidenceCurrentStep("El web service confirma la transacción de devolución de "+tipo+" exitosamente y responde de la siguiente manera:");
	    }
	    String response = GetRequest.executeGetRequest(request);
	    testCase.addTextEvidenceCurrentStep("\n"+response);
	
	
	return response;
}	
	
    public String ejecutarRunGetQuery() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();
		datosRequest.put("host", data.get("host"));
		datosRequest.put("cardNo", data.get("cardNo"));
		datosRequest.put("binCodeList", data.get("binCodeList"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("promType", data.get("promType"));
		
		String request = GetRequestFile.getRequestFile(runGetQueryPath, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n"+request);
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("Response: \n"+response);	
		
		return response;
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
		//testCase.addTextEvidenceCurrentStep("Valida que App_dep este en Y en la tabla TPEUSER.TDC_BIN");
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
	//	testCase.addTextEvidenceCurrentStep("Valida que Proc_code este en PAY en la tabla TPEUSER.TDC_ROUTING"); 
		
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
		//testCase.addTextEvidenceCurrentStep("Valida que el registro este en la tabla TPEUSER.TDC_BLOQUED_STORE");
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
		//testCase.addTextEvidenceCurrentStep("Valida que el registro este en la tabla TPEUSER.TDC_BLOQUED_ACCOUNT");
		
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
	
	public String runGetFolioParams() {
		Map <String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("plaza",data.get("plaza"));
		paramsMap.put("tienda", data.get("tienda"));
		paramsMap.put("caja", data.get("caja"));
		paramsMap.put("type", data.get("type"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsMap.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	
	public String runGetAuthParams (String folio, String creationDate) {
		
		Map <String, String> paramsAuth = new HashMap <String, String>();
		paramsAuth.put("folio", folio);
		paramsAuth.put("creationDate", creationDate);
		paramsAuth.put("pvDate", creationDate);
		paramsAuth.put("adDate", creationDate.substring(0, 8));
		paramsAuth.put("cardNo", data.get("cardNo"));
		paramsAuth.put("entryMode", data.get("entryMode"));
		paramsAuth.put("seqNo", data.get("seqNo"));
		paramsAuth.put("cvv", data.get("cvv"));
		paramsAuth.put("track2", data.get("track2"));
		paramsAuth.put("promType", data.get("promType"));
		paramsAuth.put("amount", data.get("amount"));
		paramsAuth.put("operator", data.get("operator"));
		paramsAuth.put("f55", data.get("f55"));
		paramsAuth.put("accountType", data.get("accountType"));
		paramsAuth.put("tokenC4", data.get("tokenC4"));
		paramsAuth.put("tokenCZ", data.get("tokenCZ"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsAuth.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	
public String runGetAuthParamsIntegridad (String folio, String creationDate, String integridadEncriptedString) {
		
		Map <String, String> paramsAuth = new HashMap <String, String>();
		paramsAuth.put("folio", folio);
		paramsAuth.put("creationDate", creationDate);
		paramsAuth.put("pvDate", creationDate);
		paramsAuth.put("adDate", creationDate.substring(0, 8));
		paramsAuth.put("cardNo", data.get("cardNo"));
		paramsAuth.put("entryMode", data.get("entryMode"));
		paramsAuth.put("track2", data.get("track2"));
		paramsAuth.put("promType", data.get("promType"));
		paramsAuth.put("amount", data.get("amount"));
		paramsAuth.put("operator", data.get("operator"));
		paramsAuth.put("serviceId", data.get("serviceId"));
		paramsAuth.put("accountNo", data.get("accountNo"));
		paramsAuth.put("accountType", data.get("accountType"));
		paramsAuth.put("bankId", data.get("bankId"));
		paramsAuth.put("purchaseType", data.get("purchaseType"));
		paramsAuth.put("folioTransaccion", data.get("folioTransaccion"));
		paramsAuth.put("idServicio", data.get("idServicio"));
		paramsAuth.put("ref1", data.get("ref1"));
		paramsAuth.put("ref2", data.get("ref2"));
		paramsAuth.put("ref3", data.get("ref3"));
		paramsAuth.put("ref4", data.get("ref4"));
		paramsAuth.put("corte", data.get("corte"));
		paramsAuth.put("ticket", data.get("ticket"));
		paramsAuth.put("comisión", data.get("comisión"));
		paramsAuth.put("folioTicket", data.get("folioTicket"));
		paramsAuth.put("integridad", integridadEncriptedString);
		paramsAuth.put("idAut", data.get("idAut"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsAuth.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	
	public String runGetAckParams(String folio, String creationDate) {
		Map <String, String> paramsAck = new HashMap <String, String>();
		paramsAck.put("folio", folio);
		paramsAck.put("creationDate", creationDate);
		paramsAck.put("ack", data.get("ack"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsAck.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	public String runGetDevAckParams(String folio) {
		Map <String, String> paramsAck = new HashMap <String, String>();
		paramsAck.put("folio", folio);
		paramsAck.put("ack", data.get("ack"));
		paramsAck.put("track2", data.get("track2"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsAck.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
	}
	
	public String runGetDevParams(String folio, String creationDate) {
		Map <String, String> paramsAck = new HashMap <String, String>();
		paramsAck.put("folio", folio);
		paramsAck.put("pvDate", creationDate);
		paramsAck.put("adDate", creationDate.substring(0, 8));
		paramsAck.put("adAuthDate", creationDate.substring(0, 8));
		paramsAck.put("cardNo", data.get("cardNo"));
		paramsAck.put("entryMode", data.get("entryMode"));
		paramsAck.put("seqNo", data.get("seqNo"));
		paramsAck.put("track2", data.get("track2"));
		paramsAck.put("swAuth", data.get("swAuth"));
		paramsAck.put("amount", data.get("amount"));
		
		String formatedParams = "";
		for (Entry<String, String> entry : paramsAck.entrySet()) {
			String formParams = String.format("%s=%s&",entry.getKey(), entry.getValue());
			formatedParams += formParams;
		}
		String params = formatedParams.substring(0, formatedParams.length()-1).replace(",", "&");
		System.out.println(params);
		
		return params;
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
