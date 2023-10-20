package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE2TransfrenciasServElectronicos extends BaseExecution {
	
	/*
	 * 
		Validar Switch_Transaccion exitosa de pago con tarjeta de debito
		Validar Switch_Transaccion exitosa de pago con tarjeta de crdito
		Validar Switch_Transaccion exitosa de pago con puntos
		Validar Switch_Transaccion exitosa de retiro de efectivo
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_PE2_Transferencias_Exitosas_Servicios_Electronicos(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 ************************************************************************/

		utils.sql.SQLUtil dbFCTDC = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO,
				GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,
				GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,
				GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		//BD NUEVA
		//-SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);


		PE2 pe2Util = new PE2(data, testCase, dbFCTDC);
		
		testCase.setTest_Description(data.get("Description"));

		/*
		 * Variables
		 ********************************************************************************/

		
		String Query= "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE "
				+ " FROM tpeuser.tdc_transaction "
				+ " WHERE folio = '%s' "
				+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
		
		String Query2= "SELECT TRACK1, SW_AUTH, SWITCH, IS_NAME"
				+ " FROM tpeuser.tdc_transaction "
				+ " WHERE folio = '%s' "
				+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
		
		String QueryG= "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, TRACK1, SW_AUTH, SWITCH, IS_NAME "
				+ " FROM tpeuser.tdc_transaction "
				+ " WHERE folio = '%s' "
				+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
		
		String tdcLogErrorQuery = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION  "
				+ " FROM WMLOG.WM_LOG_ERROR_TPE" + 
				" WHERE TPE_TYPE='PE2' " + 
				" AND FOLIO='%s'" + 
				" AND ERROR_DATE = TRUNC(SYSDATE)" + 
				" ORDER BY ERROR_DATE DESC";

		String tdcTPE = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE2 = "SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY  "
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE3 = "SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPEG = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		

		String folio;
		String creationDate;
		String wmCodeRequest;
		String wmCodeDb = "";
		String tarjeta=data.get("tipo");
		String tipoT = data.get("tarjeta");
		String wmCodeToValidateFolio =  data.get("wmCodeToValidateFolio");
		String wmCodeToValidateAuth = data.get("wmCodeToValidateAuth");
		String wmCodeToValidateAck = data.get("codigoACK");
		String retiro = data.get("promType");
		
		
		/*
		 * Pasos
		 *******************************************************************************************************/
	
			
			if(retiro.equals("PTS")) {
				testCase.setPrerequisites(" Contar con acceso a la BD FCTDCQA _MTY.                                                                             "
						+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                        "
						+ "Contar con acceso a la BD FCSWPRD.                                                                                        "
						+ "Contar con una tarjeta de credito, que tenga puntos disponibles y que pueda realizar compras.                                                                                                                               "
						+ "Contar con la correcta configuracion del BIN de la tarjeta de credito con puntos en las tablas TDC_BIN y TDC_ROUTING de la BD FCTDCQA_MTY.                                                                                                     "
						+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.");
			}
			
if(retiro.equals("CAB")) {
	testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                         "
			+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                "
			+ "Contar con acceso a la BD FCSPRD.                                                                                                "
			+ "Contar con una tarjeta de debito y que pueda realizar retiros de dinero.                                             "
			+ "Contar con la correcta configuracion del BIN de la tarjeta de debito en las tablas TDC_BIN campo CASHBACK='Y' y TDC_ROUTING campo PROC_CODE='CAB' y ENTITY='BBVA' de la BD FCTDCQA_MTY.                                                                                                                                           "
			+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.");
			}

if(retiro.equals("REG")) {
	testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                                    "
			+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                         "
			+ "Contar con acceso a la BD FCSWPRD.                                                                                                          "
			+ "Contar con una tarjeta de "+tipoT+" que pueda realizar compras.                                                                 "
			+ "Contar con la correcta configuracion del BIN de la tarjeta de "+tipoT+" en las tablas TDC_BIN y TDC_ROUTING de la BD FCTDCQA_MTY.                                                                                                              "
			+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.");
}
		
		
		
		// Paso 1
		// ------------------------------------------------------------------------------------------------------------------------------------------------------
		
		String ackNegativo="01";
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("Crecimiento Servicios Electronicos (SF)");
		//testCase.setTest_Description(data.get("Description"));
		addStep("Solicitar folio desde un navegador, invocando el servicio runGetFolio:");
		// Ejecuta el servicio PE2.Pub:runGetFolio
		String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();// obtener el folio del
		System.out.println(responseRunGetFolio);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");

		// Se valida que el wm_code del xml de respuesta sea igual a 100
		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidateFolio);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+wmCodeToValidateFolio);

		// Paso 2--------------------------------------------------------------------------------------------------------------------------------------------------------------
		String tipo = data.get("Name");
		addStep("Solicitar autorizacion de "+tipo+" desde un navegador, invocando el servicio runGetAuth:");
		// Ejecuta el servicio PE2.Pub:runGetAuth
		String responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate, 0);// retorna el response
		System.out.println(responseRunGetAuth);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		wmCodeRequest = getSimpleDataXml(responseRunGetAuth, "wmCode");

		// Se valida que el wm_code del xml de respuesta sea igual al esperado
		validationRequest = wmCodeRequest.equals(wmCodeToValidateAuth);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+wmCodeToValidateAuth);

		// Paso 3--------------------------------------------------------------------------------------------------------------------------------------------------------------
		
		addStep("Solicitar confirmacion ACK de "+tipo+" desde un navegador, invocando el servicio runGetAuthAck:");
		// Ejecuta el servicio PE2.Pub:runGetAck
		String responseRunGetAck = pe2Util.ejecutarRunGetAck(folio,creationDate);// retorna el response
		System.out.println(responseRunGetAck);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		wmCodeRequest = getSimpleDataXml(responseRunGetAck, "wmCode");
		// Se valida que el wm_code del xml de respuesta sea igual al esperado
		validationRequest = wmCodeRequest.equals(wmCodeToValidateAck);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+wmCodeToValidateAck);
		

		//****************************************************Validar conexion FCTDCQA *************************************************************************

		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion de forma exitosa con la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

		// Paso 5 ------------------------------------------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TDC_TRANSACTION:");
		
	    //Consulta a BD, se forma el query a utilizar
	    String Tempquery = String.format(Query, folio);
	    String Tempquery2 = String.format(Query2, folio);
	    String TempqueryG = String.format(QueryG, folio);
			
	    //Se realiza la consulta a la BD y se obtiene el wm_code
	    SQLResult result1 = executeQuery(dbFCTDC, Tempquery);
	    if(!result1.isEmpty()) {
	    	wmCodeDb = result1.getData(0, "WM_CODE");
			System.out.print(result1);
	    }
	    
	    SQLResult result1P2 = executeQuery(dbFCTDC, Tempquery2);
	    
	    
		System.out.println("Code DB: " + wmCodeDb);

		// Se valida que sea igual al esperado
		boolean validationDb = wmCodeDb.equals(wmCodeToValidateAck);
		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
		
		testCase.addTextEvidenceCurrentStep(TempqueryG);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addQueryEvidenceCurrentStep(result1);
		testCase.addQueryEvidenceCurrentStep(result1P2);

//		assertTrue(validationDb, "El codigo wmCode no es el esperado");

		//****************************************************Validar conexion FCWMLTAEQA *************************************************************************
		addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
		 testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion de forma exitosa con la BD.");
		    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);
		    
		//Paso 7
		// ------------------------------------------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar la siguiente consulta para validar que la transaccion no registra errores:");
		String tempErrorQuery = String.format(tdcLogErrorQuery, folio);
		SQLResult result2 = executeQuery(dbFCWMLTAEQA, tempErrorQuery);
		System.out.println(result2);
		boolean validationError = result2.isEmpty();

		if (validationError) {
			testCase.addTextEvidenceCurrentStep(tempErrorQuery);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			if(data.get("ack").equals(ackNegativo)) {
				testCase.addBoldTextEvidenceCurrentStep("No se muestran errores relacionados a la reversa de la transaccion con ACK negativo.");
			}else {
				testCase.addBoldTextEvidenceCurrentStep("No se muestra ningun registro de error para la transaccion.");
			}
			
			testCase.addQueryEvidenceCurrentStep(result2,false); 
		} 
		// ****************************************************Validar conexion FCSWQA*************************************************************************

		addStep("Conectarse a la BD FCSWPRD");
        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD");
        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
        testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion de forma exitosa con la BD.");
        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWPRD_CRECI);
   

		// Paso 9
		/* --------------------------------------------------------------------------------------------------------------------------------*/
//		    Thread.sleep(6000);
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TPE_SW_TLOG:");
		
		String tempTPEQuery = String.format(tdcTPE, folio);
		String tempTPEQuery2 = String.format(tdcTPE2, folio);
		String tempTPEQuery3 = String.format(tdcTPE3, folio);
		String tempTPEQueryG = String.format(tdcTPEG, folio);
		
		SQLResult result3 = executeQuery(dbFCSWQA, tempTPEQuery);
		SQLResult result3P2 = executeQuery(dbFCSWQA, tempTPEQuery2);
		SQLResult result3P3 = executeQuery(dbFCSWQA, tempTPEQuery3);
		
		
		boolean validationTPE = result3.isEmpty();

		if (!validationTPE) {
			testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(result3);
			testCase.addQueryEvidenceCurrentStep(result3P2);
			testCase.addQueryEvidenceCurrentStep(result3P3);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD");
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult resultQROP2 = executeQuery(dbFCSWQA_QRO, tempTPEQuery2);
			SQLResult resultQROP3 = executeQuery(dbFCSWQA_QRO, tempTPEQuery3);

			validationTPE = resultQRO.isEmpty();
			
			
			if (!validationTPE) {
				testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion de forma exitosa.");
				testCase.addQueryEvidenceCurrentStep(resultQRO);
				testCase.addQueryEvidenceCurrentStep(resultQROP2);
				testCase.addQueryEvidenceCurrentStep(resultQROP3);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
			}
		}

		assertFalse(validationTPE, "No se registro la transaccion");

	}
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {		
		return "ATC_FT_011_PE2_Transferencias_Exitosas_Servicios_Electronicos";
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
