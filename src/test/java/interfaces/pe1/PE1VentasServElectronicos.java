package interfaces.pe1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;


import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PE1VentasServElectronicos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PE1_PE1VentasServElectronicos(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil dbFCT = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA, GlobalVariables.DB_PASSWORD_FCTAEQA);
		SQLUtil dbFCW = new SQLUtil(GlobalVariables. DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QAVIEW, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		SQLUtil dbFCS = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		//SQLUtil dbFCS = new SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		PE1 pe1Util = new PE1(data, testCase, dbFCT);
		
		

		 
		/*
		 * Variables
		 *************************************************************************/
		String wmCodeToValidate = data.get("wmCodeFolio");
		String wmCodeToValidateAuth = data.get("wmCodeAuth");
		String wmCodeToValidateAck = data.get("wmCodeAck");

		String folio;
		String wmCodeRequest;
		
		
		String tdcTransactionQuery = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE" +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')" + 
				" AND TIENDA='" + data.get("tienda") +"'" +
				" AND folio = %s" + 
				" ORDER BY CREATION_DATE DESC";
		
		String tdcTransactionQueryPart2 = "SELECT PHONE, SW_AUTH, SWITCH, IS_NAME " +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')" + 
				" AND TIENDA='" + data.get("tienda") +"'" +
				" AND folio = %s" + 
				" ORDER BY CREATION_DATE DESC";
		String tdcTransactionQueryGeneral = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, PHONE, SW_AUTH, SWITCH, IS_NAME " +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')" + 
				" AND TIENDA='" + data.get("tienda") +"'" +
				" AND folio = %s" + 
				" ORDER BY CREATION_DATE DESC";
		
		String tdcErrorQuery = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, TPE_TYPE "
				+ " FROM WMLOG.WM_LOG_ERROR_TPE "
				+ " WHERE ERROR_DATE >=TRUNC(SYSDATE) "
				+ " AND (TPE_TYPE LIKE '%%PE1%%')" 
				+ " AND folio = %s "
				+ " ORDER BY ERROR_DATE DESC";
		
		String tdcTLOGQuery = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES " + 
				" FROM SWUSER.TPE_SW_TLOG" + 
				" where TIENDA='50MCZ'" + 
				" and APPLICATION='TAE'" + 
				" and MTI='0200'" + 
				" and folio = %s" +
				" AND CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')";

		String tdcTLOGQueryPart2 = " SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY" + 
				" FROM SWUSER.TPE_SW_TLOG" + 
				" where TIENDA='50MCZ'" + 
				" and APPLICATION='TAE'" + 
				" and MTI='0200'" + 
				" and folio = %s" +
				" AND CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')";
		
		String tdcTLOGQueryPart3 = " SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE" + 
				" FROM SWUSER.TPE_SW_TLOG" + 
				" where TIENDA='50MCZ'" + 
				" and APPLICATION='TAE'" + 
				" and MTI='0200'" + 
				" and folio = %s" +
				" AND CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')";
		
		String tdcTLOGQueryGeneral = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE" + 
				" FROM SWUSER.TPE_SW_TLOG" + 
				" where TIENDA='50MCZ'" + 
				" and APPLICATION='TAE'" + 
				" and MTI='0200'" + 
				" and folio = %s" +
				" AND CREATION_DATE >=TO_CHAR(SYSDATE,'DD-MON-YY')";
		
		String recarga = data.get("pre");
		/*
		 * Pasos
		 **************************************************************************/
		
		testCase.setFullTestName(data.get("casoDePrueba"));
		testCase.setProject_Name("Crecimiento Servicios Electrónicos (SF)");
		testCase.setTest_Description(data.get("descripcion"));
		// testCase.setPrerequisites(data.get("pre"));
		
		testCase.setPrerequisites("Contar con número de celular válido para realizar la recarga de "+recarga+".                      " + 
				" Contar con acceso a la BD FCTAEQA_MTY.                                                                                   " + 
				" Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                   " + 
				" Contar con acceso a la BD FCSWPRD.");
		
		
		
/* Paso 1 *****************************************************************************************/
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de folio:");

		// Ejecuta el servicio PE1.Pub:runGetFolio

		String respuestaFolio = pe1Util.ejecutarRunGetFolio();
		
        System.out.println("\n" + respuestaFolio + "\n");
		
		folio = getSimpleDataXml(respuestaFolio, "folio");

		wmCodeRequest = getSimpleDataXml(respuestaFolio, "wmCode");
		
		boolean validationResponseFolio=true;

		if(respuestaFolio!= null) {
			validationResponseFolio= false;}
				
		 assertFalse(validationResponseFolio);
		 
		 // VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 100
		 
		boolean validationRequestFolio = wmCodeRequest.equals(wmCodeToValidate);

		System.out.println("\n" + validationRequestFolio + " - wmCode request: " + wmCodeRequest + "\n");
		
		assertTrue(validationRequestFolio, "El Codigo wmCode no es el esperado: " + wmCodeToValidate);
		 
 /* Paso 2 *****************************************************************************************/
		 addStep("Ejecutar en un navegador directamente el servicio de solicitud de Autorizacion:");
		 
		// Ejecuta el servicio PE1.Pub/runGetAuth
		String respuestaAuth = pe1Util.ejecutarRunGetAuth(folio);	
		 
		System.out.print("\n" + respuestaAuth + "\n");
		 
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");

		boolean validationResponseAuth=true;

		if(respuestaAuth!= null) {validationResponseAuth= false;}
		assertFalse(validationResponseAuth);
		
		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 000
		
		boolean validationRequestAuth = wmCodeRequest.equals(wmCodeToValidateAuth);

		System.out.println("\n" + validationRequestAuth + " - wmCode request: " + wmCodeRequest + "\n");

		testCase.addTextEvidenceCurrentStep("Codigo esperado: " + wmCodeToValidateAuth +  "\n"  + "Codigo XML: " + wmCodeRequest);

		assertTrue(validationRequestAuth, "El Código wmCode no es el esperado: " + wmCodeToValidateAuth);
		
		
		 /* Paso 3 *****************************************************************************************/
		String ackNegativo="01";
		String ackStep= " ";
		if(data.get("ack").equals(ackNegativo)) {
			ackStep="Negativo";
		}
		
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de Confirmación ACK "+ackStep+":");
		
		//Ejecuta el servicio PE1.Pub:runGetAck
	    String respuestaAck = pe1Util.ejecutarRunGetAck(folio);	
	        
	    System.out.print("\n" + respuestaAck + "\n");

		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");

		boolean validationResponseAck=true;

		if(respuestaAck!= null) {validationResponseAck= false;}
		assertFalse(validationResponseAck);
		
		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 101		
		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);

		System.out.println("\n" + validationRequestAck + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestAck, "El Codigo wmCode no es el esperado: " + wmCodeToValidateAck);
		
		/* Paso 4 *****************************************************************************************/
		addStep("Establecer conexion con la base de datos: FCTAEQA_MTY ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTAEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCTAEQA);
		
		
		/* Paso 5 *****************************************************************************************/
		addStep("Ejecutar la siguiente consulta de la tabla TAE_TRANSACTION:");

		// Se forma el query a utilizar

		String query = String.format(tdcTransactionQuery, folio);
		String queryPart2 = String.format(tdcTransactionQueryPart2, folio);
		String queryGeneral = String.format(tdcTransactionQueryGeneral, folio);
		System.out.println(query);

		SQLResult result1 = executeQuery(dbFCT, query);
		SQLResult result1Part2 = executeQuery(dbFCT, queryPart2);
		
		boolean transaction = result1.isEmpty();
		
		if (!transaction) {
			testCase.addTextEvidenceCurrentStep(queryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacción de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(result1,false);
			testCase.addQueryEvidenceCurrentStep(result1Part2,false);
		}
		
		assertTrue(!transaction);
		
	    
//****************************************************Validar conexion FCWMLTAEQA *************************************************************************
	    
	    addStep("Establecer conexión con la base de datos: FCWMLTAEQA_MTY");
	    testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCWMLTAEQA_MTY fue exitosa.");
	    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);
		
		/* Paso 7  *****************************************************************************************/
		addStep("Ejecutar la siguiente consulta de la tabla WM_LOG_ERROR_TPE:");
		
		String errorQuery = String.format(tdcErrorQuery, 1);
		
		System.out.println(errorQuery);
		
		SQLResult result2 = executeQuery(dbFCW, errorQuery);
		
		boolean error = result2.isEmpty();
		
		if (error) {
			testCase.addTextEvidenceCurrentStep(errorQuery);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("No muestra ningún registro de error.");
			testCase.addQueryEvidenceCurrentStep(result2,false);
			
		}
		
		assertTrue(error);
	    
//****************************************************Validar conexion FCSWQA*************************************************************************
	    
	
	    addStep("Establecer conexion con la base de datos: FCSWPRD");
	        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD");
	        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	        testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCSWPRD fue exitosa.");
	        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWPRD_CRECI);
	   

	
		/* Paso 9 *****************************************************************************************/
		addStep("Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG:");
		
		
		String tempTPEQuery = String.format(tdcTLOGQuery, folio);
		String tempTPEQueryPart2 = String.format(tdcTLOGQueryPart2, folio);
		String tempTPEQueryPart3 = String.format(tdcTLOGQueryPart3, folio);
		String tempTPEQueryGeneral = String.format(tdcTLOGQueryGeneral, folio);
		
		SQLResult result3 = executeQuery(dbFCS, tempTPEQuery);//parte 1
		SQLResult result3Part2 = executeQuery(dbFCS, tempTPEQueryPart2);//parte 2
		SQLResult result3Part3 = executeQuery(dbFCS, tempTPEQueryPart3);//parte 3
		
		System.out.print(tempTPEQuery);
		System.out.print(tempTPEQueryPart2);
		System.out.print(tempTPEQueryPart3);
		System.out.print(tempTPEQueryGeneral);
		
		boolean validationTPE = result3.isEmpty();

		
		
		if (!validationTPE) {			
			testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(result3,false);
			testCase.addQueryEvidenceCurrentStep(result3Part2,false);
			testCase.addQueryEvidenceCurrentStep(result3Part3,false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
		}else {
			
			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult result3Part2QRO = executeQuery(dbFCS, tempTPEQueryPart2);//parte 2
			SQLResult result3Part3QRO = executeQuery(dbFCS, tempTPEQueryPart3);//parte 3
			
			validationTPE = resultQRO.isEmpty();
			
			if(!validationTPE)
			{	testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(resultQRO,false);
			testCase.addQueryEvidenceCurrentStep(result3Part2QRO,false);
			testCase.addQueryEvidenceCurrentStep(result3Part3QRO,false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
			}
				}
		
		assertTrue(!validationTPE);
		
		}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. FEMSA_PE1_PE1_PE1VentasServElectronicos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_PE1_PE1VentasServElectronicos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	

}
