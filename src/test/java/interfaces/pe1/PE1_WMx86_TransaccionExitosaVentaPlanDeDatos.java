package interfaces.pe1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PE1;
import om.Util;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE1_WMx86_TransaccionExitosaVentaPlanDeDatos extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_PE1_WMx86_TransaccionExitosaPlanDeDatos(HashMap<String, String> data) throws Exception {
		
		/*
		 * 
		 * Modificado por mantenimiento
		 * @author Brandon Ruiz
		 * @date 15/02/2023
		 * @cp MTC-FT-003 PE1 Transaccion exitosa de venta de plan de datos 
		 * @projectname Actualizacion Tecnologica Webmethods
		 * 
		 */
		
		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil dbFCT = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA, GlobalVariables.DB_PASSWORD_FCTAEQA);
		SQLUtil dbFCW = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		SQLUtil dbFCS = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
//		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		PE1 pe1Util = new PE1(data, testCase, dbFCT);
		Util o = new Util(testCase);
	 
		/*
		 * Variables
		 *************************************************************************/
		String wmCodeToValidate = data.get("wmCodeFolio");
		String wmCodeToValidateAuth = data.get("wmCodeAuth");
		String wmCodeToValidateAck = data.get("wmCodeAck");

		String folio;
		String wmCodeRequest;

	
		String tdcTransactionQuery = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, AMOUNT" +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TRUNC(SYSDATE)" + 
				" AND folio = %s"+
				" AND WM_CODE='101'"+
				" AND SW_AUTH_CODE='00'" + 
				" ORDER BY CREATION_DATE DESC";
		
		String tdcTransactionQueryPart2 = "SELECT PHONE, SW_AUTH, SWITCH, IS_NAME, ITEM, SW_AUTH_CODE " +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TRUNC(SYSDATE)" + 
				" AND folio = %s" + 
				" AND WM_CODE='101'"+
				" AND SW_AUTH_CODE='00'" + 
				" ORDER BY CREATION_DATE DESC";
		
		String tdcTransactionQueryGeneral = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, PHONE, SW_AUTH, SWITCH, IS_NAME " +
				" FROM TPEUSER.TAE_TRANSACTION" + 
				" WHERE CREATION_DATE >=TRUNC(SYSDATE)" +
				" AND folio = %s" + 
				" AND WM_CODE='101'"+
				" AND SW_AUTH_CODE='00'" + 
				" ORDER BY CREATION_DATE DESC";
		
		String queryStep7 = "SELECT FOLIO, MTI, RESP_CODE, AUTH_ID_RES, CREATION_DATE FROM SWUSER.TPE_SW_TLOG \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND FOLIO = '%s'";
		
		String LogSecSess="SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG "
				+ "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION = 'PE1' AND FOLIO = '%s'"
				+ "ORDER BY CREATION_DATE DESC";
		
		
		String ValidTAE="SELECT CARRIER,CARRIER_NAME,ITEM,AMOUNT,DESCRIPTION,CREATION_DATE  "
				+ "FROM TPEUSER.TAE_CATALOG_DATA_PLANS "
				+ "WHERE ITEM = '%s' "
				+ "ORDER BY CREATION_DATE DESC";
		
		testCase.setProject_Name("Actualizacion tecnologica");
	
		/*
		 * Pasos
		 ************************************************************************
		
		
		
/* Paso 1 *****************************************************************************************/
		
			
		step("Solicitar un folio desde el navegador , invocando el servicio runGetFolio de la Interface PE1 para una venta de plan de datos");

		// Ejecuta el servicio PE1.Pub:runGetFolio

		String respuestaFolio = pe1Util.ejecutarRunGetFolio();
		
        System.out.println("\n" + respuestaFolio + "\n");
		
		folio = getSimpleDataXml(respuestaFolio, "folio");
		System.out.println("Folio: " + folio);
		wmCodeRequest = getSimpleDataXml(respuestaFolio, "wmCode");
		System.out.println("WMcode: " + wmCodeRequest);
		boolean validationResponseFolio=true;

		if(respuestaFolio!= null) {
			validationResponseFolio= false;}
				
		 assertFalse(validationResponseFolio);
		 
		 
		boolean validationRequestFolio = wmCodeRequest.equals(wmCodeToValidate);

		System.out.println("\n" + validationRequestFolio + " - wmCode request: " + wmCodeRequest + "\n");
		
		assertTrue(validationRequestFolio, "El Codigo wmCode no es el esperado: " + wmCodeToValidate);
		 
 /* Paso 2 *****************************************************************************************/
		
		 step("Ejecutar la solicitud de autorizacion invocando el servicio runGetAuth");
		 
		// Ejecuta el servicio PE1.Pub/runGetAuth
		String respuestaAuth = pe1Util.ejecutarRunGetAuth(folio);	
		 
		System.out.print("\n" + respuestaAuth + "\n");
		 
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");

		boolean validationResponseAuth=true;

		if(respuestaAuth!= null) {validationResponseAuth= false;}
		assertFalse(validationResponseAuth);
		
		
		boolean validationRequestAuth = wmCodeRequest.equals(wmCodeToValidateAuth);

		System.out.println("\n" + validationRequestAuth + " - wmCode request: " + wmCodeRequest + "\n");

		testCase.addTextEvidenceCurrentStep("Codigo esperado: " + wmCodeToValidateAuth +  "\n"  + "Codigo XML: " + wmCodeRequest);

		assertTrue(validationRequestAuth, "El Codigo wmCode no es el esperado: " + wmCodeToValidateAuth);
		
		
		 /* Paso 3 *****************************************************************************************/
		String ackNegativo="01";
		String ackStep= " ";
		if(data.get("ack").equals(ackNegativo)) {
			ackStep="Negativo";
		}
		
		step("Ejecutar en un navegador directamente el servicio de solicitud de Confirmacion ACK "+ackStep+":");
		

		//Ejecuta el servicio PE1.Pub:runGetAck
	    String respuestaAck = pe1Util.ejecutarRunGetAck(folio);	
	        
	    System.out.print("\n" + respuestaAck + "\n");

		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");

		boolean validationResponseAck=true;

		if(respuestaAck!= null) {validationResponseAck= false;}
		assertFalse(validationResponseAck);
			
		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);

		System.out.println("\n" + validationRequestAck + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestAck, "El Codigo wmCode no es el esperado: " + wmCodeToValidateAck);
		
		/* Paso 4 *****************************************************************************************/
		step("Establecer conexion con la base de datos: FCTAEQA_MTY ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTAEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCTAEQA);
		
		
		/* Paso 5 *****************************************************************************************/
		step("Validar que la transaccion de venta de plan de datos se registro correctamente en la tabla TPEUSER.TAE_TRANSACTION de la BD **FCTAEQA** ");
	
		// Se forma el query a utilizar
		System.out.println(GlobalVariables.DB_HOST_FCTAEQA);
		String query = String.format(tdcTransactionQuery, folio);
		String queryPart2 = String.format(tdcTransactionQueryPart2, folio);
		String queryGeneral = String.format(tdcTransactionQueryGeneral, folio);
		System.out.println(query);

		SQLResult result1 = executeQuery(dbFCT, query);
		SQLResult result1Part2 = executeQuery(dbFCT, queryPart2);
		String ITEM = "";
		String AMOUNT="";
		boolean transaction = result1.isEmpty();
		
		if (!transaction) {
			AMOUNT=result1.getData(0,"AMOUNT");
			ITEM = result1Part2.getData(0, "ITEM");
			testCase.addTextEvidenceCurrentStep(queryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion de TAE de forma exitosa.");
			
		}
		testCase.addQueryEvidenceCurrentStep(result1);
		testCase.addQueryEvidenceCurrentStep(result1Part2);
		
		assertTrue(!transaction);
		
		/* Paso 6 *****************************************************************************************/
		step("Validar que el monto de la transaccion de venta de plan de datos coincide con el monto de la tabla TPEUSER.TAE_CATALOG_DATA_PLANS de la BD **FCTAEQA**");
	
		System.out.println(GlobalVariables.DB_HOST_FCTAEQA);
		
		System.out.println(ValidTAE);
		
		String queryStep6 = String.format(ValidTAE, ITEM); 

		SQLResult ValidTAEExec = executeQuery(dbFCT, queryStep6);
		String AMOUNT2="";
		
		boolean ValidTAEVal = ValidTAEExec.isEmpty();
		boolean ValidAmount=false;
		
		if (!ValidTAEVal) {
			AMOUNT2=ValidTAEExec.getData(0,"AMOUNT");
			ValidAmount=AMOUNT.equals(AMOUNT2);
			if(ValidAmount) {
				testCase.addTextEvidenceCurrentStep(queryStep6);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("el monto de la transaccion de venta de plan de datos coincide con el monto de la tabla TPEUSER.TAE_CATALOG_DATA_PLANS");
			}
		}
		testCase.addQueryEvidenceCurrentStep(ValidTAEExec);
	
			
		assertTrue(!ValidTAEVal);
//****************************************************Paso 7*************************************************************************
	    
			step("Establecer conexion con la base de datos: FCSWQA");
	        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA");
	        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	        testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCSWQA fue exitosa.");
	        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWQA);		
		

		
		/* Paso 8 *****************************************************************************************/
	        step("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion");
	        String format = String.format(queryStep7, folio);
	        queryStep7 = format;
	        SQLResult resultPaso7 = ejecutaQuery(queryStep7,dbFCS);
	        o.validaRespuestaQuery(resultPaso7);
			if (resultPaso7.isEmpty()) {
				o.muestraError(queryStep7, "No se encontro ningun registro de la transaccion con folio: "+folio);
			}else {
				o.log("Se comprueba el registro exitoso del folio: "+folio);
			}
	        
	        
	        
 //****************************************************Paso 9 *************************************************************************
		    
		    step("Establecer conexion con la base de datos: FCWMLTAEQA_MTY");
		    testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCWMLTAEQA_MTY fue exitosa.");
		    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);   

	
		/* Paso 10 *****************************************************************************************/
		    

		    step("Validar en la base de datos que se este registrando el securityLog ");
			
			System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
			format = String.format(LogSecSess, folio);
			LogSecSess = format;
			System.out.println(LogSecSess);
			SQLResult resultSec = executeQuery(dbFCW, LogSecSess);
			
			boolean resultSecVal = resultSec.isEmpty();
			
			if (!resultSecVal) {
				testCase.addTextEvidenceCurrentStep(LogSecSess);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Se visualiza los registros de las invocaciones realizadas a la PE1  de la  transaccion  de TAE en la BD *FCWMLTAEQA* , por lo tanto no viajo por un canal seguro");
				
				
			}
			testCase.addQueryEvidenceCurrentStep(resultSec);
			
			assertTrue(!resultSecVal,"No se encontraron registros en la consulta");
			
			/* Paso 11 *****************************************************************************************/
			step("Realizar la siguiente consulta para validar si hay errores en la transaccion de recarga de tiempo aire");
			String queryStep11 = "SELECT ERROR_ID, FOLIO, ERROR_DATE, MESSAGE FROM WMLOG.WM_LOG_ERROR_TPE \r\n"
					+ "WHERE ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO = '%s'\r\n"
					+ "ORDER BY ERROR_DATE DESC";
			format = String.format(queryStep11, folio);
			queryStep11 = format;
			SQLResult resultPaso11 = ejecutaQuery(queryStep11, dbFCW);
			o.validaRespuestaQuery(resultPaso11);
			if (resultPaso11.isEmpty()) {
				o.log("No se muestra ningun error referente al folio: "+folio);
			}else {
				o.muestraError(queryStep11, "Se encontro registro de error para la transaccion con folio: "+folio);
			}
		
	}
	
	public void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}
	
	int contador = 0;
	public void step(String step){
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	public SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
		SQLResult queryResult;
		printQuery(query);
		queryResult = executeQuery(obj, query);
		return queryResult;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de "
				+ "venta de datos telcel de la interface FEMSA_PE1 al ser migrada de webmethods v10.5 a webmethods 10.11 y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_009_PE1_WMx86_TransaccionExitosaPlanDeDatos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "*Contar con un simulador de proveedor de DATOS que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTAEQA_MTY, FCTAEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA. tener acceso a la tabla de TAE_CATALOG_DATA_PLANS  FCTAEQA";
	}
	
	
}

