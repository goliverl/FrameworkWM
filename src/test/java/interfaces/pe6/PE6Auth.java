package interfaces.pe6;

import static org.junit.Assert.assertTrue;
import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class PE6Auth extends BaseExecution {

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE6_Flujo_Auth_Interfaz_PE6(HashMap<String, String> data) throws Exception {
		
		/*
		 * 
		 * 
		 * Utilerías
		 *********************************************************************/
		
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
        
		String host = data.get("host");
		
		
		/*
		 * Variables
		 * *****************************************************************************
		 * *************
		 * 
		 * 
		 */
		
		//Valores WMcode esperados:
		final String expectedWMCodeGetFolio = "100";
		final String expectedWMCodeGetAuth = "115"; //000
		final String expectedWMCodeGetAuthAck = "115";//
		
		//Query para validacion de BD
		final String dbValidationQuery = "SELECT folio, wm_code FROM tpeuser.tdc_transaction where folio = %s";
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

		//Paso 1*************************************************************
		
		addStep("Llamar al servico PE6.Pub:runGetFolio" ) ;
	
	    HashMap<String, String> datosRequestRunGetFolio = new HashMap<>();
	    datosRequestRunGetFolio.put("host", host);
	    datosRequestRunGetFolio.put("plaza", data.get("plaza"));
	    datosRequestRunGetFolio.put("tienda", data.get("tienda"));
	    datosRequestRunGetFolio.put("caja", data.get("caja"));
	    datosRequestRunGetFolio.put("type", data.get("type"));
	    
		//Obtener URL del request a ejecutar
	    String runGetFolioRequest = GetRequestFile.getRequestFile("PE6\\RunGetFolio.txt", datosRequestRunGetFolio);
		
		String folio;
		String creationDate;
		System.out.println(runGetFolioRequest);
    	
		//Ejecutar el request 
    	String responseRunGetFolio = GetRequest.executeGetRequest(runGetFolioRequest);
    	System.out.println(responseRunGetFolio);
    	
    	//Añadir evidencia de la respuesta al testCase
    	testCase.addTextEvidenceCurrentStep(responseRunGetFolio);
    	
    	//Obtener variables de la respuesta
    	Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetFolio);
    	folio = runGetFolioRequestDoc.getElementsByTagName("folio").item(0).getTextContent();
    	creationDate = runGetFolioRequestDoc.getElementsByTagName ("creationDate").item(0).getTextContent();
    	
    	System.out.println("Folio: " + folio);
    	System.out.println("creationDate: " + creationDate);
    	
    	testCase.passStep();
   
    	//Paso 2********************************************************************
     	
    	testCase.nextStep("Verificar la respuesta generada por el servicio") ;
    	String wmCodeFolio = runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
    	boolean validateRequest = wmCodeFolio.equals(expectedWMCodeGetFolio);
    	System.out.println("wmCodeFolio: " + wmCodeFolio);
    	testCase.addTextEvidenceCurrentStep(responseRunGetFolio);
    	
    	testCase.validateStep(validateRequest, responseRunGetFolio);


    	//Paso 3****************************************************************
    	
    	testCase.nextStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER, "
    			+ "pasando como folio el que recibimos en la respuesta de la ejecución del servicio. SELECT * FROM "
    			+ "tpeuser.tdc_transaction WHERE folio = %s") ;
    
    	//Consulta a BD
	    
	    String query = String.format(dbValidationQuery,folio);
	    
	    SQLResult rs = db.executeQuery(query);
	    //Thread.sleep(10000);
	    //rs.next();
        String wmCodedb = rs.getData(0, "wm_code");

	    boolean validationQuery1 = wmCodedb.equals(expectedWMCodeGetFolio);
	    
	    
	    System.out.println("db wmCode: " + wmCodedb);
	    System.out.println (validationQuery1);
	    testCase.addQueryEvidenceCurrentStep(rs);
	    
	    System.out.println(query);
		assertTrue(expectedWMCodeGetFolio,validationQuery1 ); //Lo cambie por True
	    	
	/**************************************************************************************************
	 * Solicitud de autoriazación
	 *************************************************************************************************/
	
    //Paso 4********************************************************************************
	
    addStep("Llamar al servico PE2.Pub:runGetAuth y pasar como parámetros algunos de los valores retornados al crear el folio de solicitud.");

		//Obtener URL del request a ejecutar
	    HashMap<String, String> datosRequestRunGetAuth = new HashMap<>();
	    datosRequestRunGetAuth.put("host", host);
	    datosRequestRunGetAuth.put("folio", folio);
	    datosRequestRunGetAuth.put("creationDate", creationDate);
	    datosRequestRunGetAuth.put("pvDate", creationDate);
	    datosRequestRunGetAuth.put("adDate", data.get("adDate"));
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
	
		String runGetAuthRequest = GetRequestFile.getRequestFile("PE6\\RunGetAuth.txt", datosRequestRunGetAuth);
		System.out.println(runGetAuthRequest);
		//Ejecutar el request 
		String responseRunGetAuth = GetRequest.executeGetRequest(runGetAuthRequest);
		//Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep(responseRunGetAuth);
		//Obtener variables de la respuesta
		Document runGetAuthRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);
		testCase.passStep();
		
		//Paso 5*********************************************************************************
		
		addStep("Verificar la respuesta generada por el servicio");
		testCase.addTextEvidenceCurrentStep(responseRunGetAuth);
		String wmCodeAuth = runGetAuthRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		boolean validationAuth = wmCodeAuth.equals(expectedWMCodeGetAuth);
		System.out.println("wmCodeAuth: " + wmCodeAuth);
		testCase.validateStep(validationAuth,expectedWMCodeGetAuth);
		
		//Paso 6 ************************************************************************************
		
		addStep("Validar que se insertaron los datos proporcionados como parámetros para el folio indicado.");
		//Consulta a BD
	    String queryAuth = String.format(dbValidationQuery,folio);
	    SQLResult rsAuth = db.executeQuery(queryAuth);
	    //Thread.sleep(10000);
	    // rsAuth.next();
	    String wmCodeAuthDB = rsAuth.getData(0, "wm_code");
	    boolean validationQueryAuth = wmCodeAuthDB.equals(expectedWMCodeGetAuth);
	    System.out.println("db wmCodeAthDB: " + wmCodeAuthDB);
	    System.out.println (queryAuth);
	    testCase.addQueryEvidenceCurrentStep(rsAuth);
	    assertTrue(expectedWMCodeGetAuth,validationQueryAuth); //Lo cambie por True
	    	
	/**************************************************************************************************
	 * ACK de Autorización
	 *************************************************************************************************/
	
		//Paso 7**********************************************************
	    
		addStep("llamar al servico PE6.Pub:runGetAuthAck");
		//Obtener URL del request a ejecutar
	    HashMap<String, String> datosRequestRunGetAuthAck = new HashMap<>();
	    datosRequestRunGetAuthAck.put("host", host);
	    datosRequestRunGetAuthAck.put("folio", folio);
	    datosRequestRunGetAuthAck.put("creationDate", creationDate);
	    datosRequestRunGetAuthAck.put("ack", data.get("ack"));
	    datosRequestRunGetAuthAck.put("track2", data.get("track2"));
	
		String runGetAuthAckRequest = GetRequestFile.getRequestFile("PE6\\RunGetAuthAck.txt", datosRequestRunGetAuthAck);
		System.out.println(runGetAuthAckRequest);
		
		//Ejecutar el request 
		String responseRunGetAuthAck = GetRequest.executeGetRequest(runGetAuthAckRequest);
		
		//Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep(responseRunGetAuthAck);
		
		//Obtener variables de la respuesta
		Document runGetAuthAckRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);
	
		testCase.passStep();
		
		//Paso 8*********************************************************
		
		addStep("Verificar la respuesta generada por el servicio");
	
		String wmCodeAuthAck = runGetAuthAckRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		
		boolean responseAck = wmCodeAuthAck.equals(expectedWMCodeGetAuthAck);
		
		testCase.addTextEvidenceCurrentStep("Response \n" + runGetAuthAckRequest);
		System.out.println("wmCodeAuthAck: " + wmCodeAuthAck);
	
		testCase.validateStep(responseAck, wmCodeAuthAck);
		
		// Paso 9**************************************************** 
		
		addStep("Validar que el estatus del folio ha sido actualizado en la tabla tdc_transaction de TPEUSER.");
		//Consulta a BD
	    String queryAuthAck = String.format(dbValidationQuery,folio);
	    SQLResult rsAuthAck = db.executeQuery(queryAuthAck);
	    String wmCodeAuthAckDB = rsAuthAck.getData(0, "wm_code");
	    boolean validationACK = wmCodeAuthAckDB.equals(expectedWMCodeGetAuthAck);
	    System.out.println("db wmCodeAthDB: " + wmCodeAuthAckDB);
	    System.out.println(queryAuthAck);
	   // testCase.addQueryEvidenceCurrentStep(db, dbValidationQuery);
	    assertTrue(expectedWMCodeGetAuthAck,validationACK);	
		
  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_001_PE6_Flujo_Auth_Interfaz_PE6" ;
  }
  @Override
  public String setTestDescription() {
    return "Terminada. Flujo de autorización de la interfaz PE6";
  }
  @Override
  public String setTestDesigner() {
    return "Equipo de Automatizacion" ;
  }
  @Override
  public String setTestInstanceID() {
    return "-1" ;
  }
@Override
  public void beforeTest() {
	// TODO Auto-generated method stub
	
}
@Override
  public String setPrerequisites() {
	// TODO Auto-generated method stub
	return null;
}
}