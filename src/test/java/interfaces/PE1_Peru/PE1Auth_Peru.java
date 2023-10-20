package interfaces.PE1_Peru;

import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class PE1Auth_Peru extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE01_Peru_Auth(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/
		
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		
		PE1 pe1Util = new PE1(data, testCase, db);

		/**
		 * 
		 * Variables de ejecucion
		 * 
		 */

		String wmCodeToValidate = "100";
		String tdcTransactionQuery = " SELECT FOLIO, WM_CODE,PLAZA,TIENDA,PHONE,CARRIER \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";

		String wmCodeToValidateAuth = "000";
		String tdcTransactionQueryAuth = " SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";

		String wmCodeToValidateAck = "101";
		String tdcTransactionQueryAck = " SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";
		
		String folio, wmCodeRequest;
		String wmCodeDb = "";

		/**
		 * 
		 * Pasos de Ejecucion
		 * 
		 */

// 	Paso 1 ******************************************************************************************	

		addStep(" Ejecucion de interface FEMSA_PE01_PE para obtener el folio :Pub:runGetFolio. ");

		// Ejecuta el servicio PE1.Pub:runGetFolio
		String respuesta = pe1Util.ejecutarRunGetFolioPeru();
		
		System.out.println(respuesta);

		// obtiene el value dentro del xml


		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia

		String subfolio = getSimpleDataXml(respuesta, "folio");
		String subWmcode  = getSimpleDataXml(respuesta, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + subfolio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + subWmcode);// Se agrega a la evidencia el wm_code

		boolean validateRequest = subfolio.equals(subfolio);
		System.out.println(validateRequest + " es igual ? :" + subfolio);

		addStep("Se valida que el wm_code del xml de respuesta sea igual a 100");
		// Se valida que el wm_code del xml de respuesta sea igual a 100

		boolean validationRequest = subWmcode.equals(wmCodeToValidate);
		System.out.println(validationRequest + " - wmCode request: " + subWmcode);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);// Se agrega la respuesta a la evidencia

		addStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER");

		// Se forma el query a utilizar
		String query = String.format(tdcTransactionQuery, subfolio);
		System.out.println(query);

		// Se realiza la consulta a la BD y se obtiene el wm_code

		SQLResult result1 = executeQuery(db, query);
		if(!result1.isEmpty()) {
			wmCodeDb = result1.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result1);
		}
		assertTrue(!result1.isEmpty(), "No se obtuvieron registros");
		
		// Se valida que sea igual a 100
		boolean validationDb = wmCodeDb.equals(wmCodeToValidate);
		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDb, "WM_CODE es diferente a 100");

//	Paso 2 ******************************************************************************************
		
		addStep(" Autorizacion de recarga, TAE Ejecuta el servicio PE1.Pub:runGetAuth ");

		// Ejecuta el servicio PE1.Pub:runGetAuth
		String respuestaAuth = pe1Util.ejecutarRunGetAuthPeru(subfolio);
		

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
	   	folio = getSimpleDataXml(respuestaAuth, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + wmCodeRequest);// Se agrega a la evidencia el wm_code

		boolean validateRequestAuth = wmCodeRequest.equals(wmCodeToValidateAck);
		System.out.println(validateRequestAuth + " es igual ? :" + wmCodeRequest);

		assertTrue(validateRequestAuth, "WM_CODE es diferente a 000");

//		Paso 3 ******************************************************************************************
		
		addStep("Verificar la respuesta generada por el servicio");

		// Se valida que el wm_code del xml de respuesta sea igual a 000
		boolean validationRequestServAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
		System.out.println(validationRequestServAuth + " - wmCode request: " + wmCodeRequest);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuestaAuth);// Se agrega la respuesta a la evidencia

		assertTrue(validationRequestServAuth, "WM_CODE es diferente a 000");

		testCase.nextStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER");

		// Se forma el query a utilizar
		String queryAuth = String.format(tdcTransactionQueryAuth, folio);
		System.out.println(queryAuth);

		// Se realiza la consulta a la BD y se obtiene el wm_code

		SQLResult result2 = executeQuery(db, queryAuth);
		if(!result2.isEmpty()) {
			wmCodeDb = result2.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result2);
		}
		assertTrue(!result2.isEmpty(), "No se obtuvieron registros");
		
		// Se valida que sea igual a 000
		boolean validationDbAuth = wmCodeDb.equals(wmCodeToValidateAuth);
		System.out.println(validationDbAuth + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDbAuth, "WM_CODE es diferente a 000");
		

// Paso4  *******************************************************************************
		
		addStep("Run ACK cierre de la transaccion");

		// Ejecuta el servicio PE1.Pub:runGetAck
		String respuestaAck = pe1Util.ejecutarRunGetAckPeru(folio);


		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuestaAck, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + wmCodeRequest);// Se agrega a la evidencia el wm_code

//		boolean valdateACK = folio.equals(folio);
//
//		assertTrue(valdateACK);

//Paso 5  ******************************************************************************************
		
		addStep(" Verificar la respuesta generada por el servicio ");

		// Se valida que el wm_code del xml de respuesta sea igual a 101
		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
		System.out.println(validationRequestAck + " - wmCode request: " + wmCodeRequest);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuestaAck);// Se agrega la respuesta a la evidencia
		
		assertTrue(validationRequestAck, "WM_CODE es diferente a 101");

//Paso 6 ******************************************************************************************
		
		addStep(" Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER ");

		// Se forma el query a utilizar
		String queryAck = String.format(tdcTransactionQueryAck, folio);
		System.out.println(queryAck);

		// Se realiza la consulta a la BD y se obtiene el wm_code

		SQLResult result3 = executeQuery(db, queryAuth);
		if(!result3.isEmpty()) {
			wmCodeDb = result3.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result3);
		}
		assertTrue(!result3.isEmpty(), "No se obtuvieron registros");
		
		// Se valida que sea igual a 101
		boolean validationDbAck = wmCodeDb.equals(wmCodeToValidateAck);
		System.out.println(validationDbAuth + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDbAck, "WM_CODE es diferente a 101");

//Paso 6 ******************************************************************************************	
		addStep("Recarga No exitosa por falta de datos de autenticacion ");
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PE01_Peru_Auth";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. FEMSA_PE1_Peru_PE01_Peru_Auth";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Automation QA";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
