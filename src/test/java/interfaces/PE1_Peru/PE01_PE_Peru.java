package interfaces.PE1_Peru;

import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class PE01_PE_Peru extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE01_Peru(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		
		PE1 pe1Util = new PE1(data, testCase, db);

		String wmCodeToValidate = "100";
		String tdcTransactionQuery = "SELECT FOLIO, WM_CODE, PLAZA, TIENDA, PHONE, CARRIER \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";

		String wmCodeToValidateAuth = "101";
		String tdcTransactionQueryAuth = "SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";

		String wmCodeToValidateAck = "101";
		String tdcTransactionQueryAck = "SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tae_transaction \r\n"
				+ "WHERE FOLIO = %s";
		
		String folio, wmCodeRequest;
		String wmCodeDb = "";

// Paso 1	******************************************************************************************

		addStep("Ejecucion de interface FEMSA_PE01_PE para obtener el folio :Pub:runGetFolio.");

		// Ejecuta el servicio PE1.Pub:runGetFolio
		String respuesta = pe1Util.ejecutarRunGetFolioPE();
		System.out.println(respuesta);

		// obtiene el value dentro del xml

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuesta, "folio");
		wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + wmCodeRequest);// Se agrega a la evidencia el wm_code

		addStep("Se valida que el wm_code del xml de respuesta sea igual a 100");
		// Se valida que el wm_code del xml de respuesta sea igual a 100

		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);// Se agrega la respuesta a la evidencia
		
		// Se valida que sea igual a 100
		boolean validationDb = wmCodeDb.equals(wmCodeToValidate);
		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDb, "WM_CODE es diferente a 100.");

		addStep(" Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER ");

		// Se forma el query a utilizar
		String query = String.format(tdcTransactionQuery, folio);
		System.out.println(query);

		// Se realiza la consulta a la BD y se obtiene el wm_code
		SQLResult result1 = executeQuery(db, query);
		if(!result1.isEmpty()) {
			wmCodeDb = result1.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result1);
		}
		assertTrue(!result1.isEmpty(), "No se obtuvieron registros");

// Paso 2	******************************************************************************************

		addStep(" Autorizacion de recarga, TAE Ejecuta el servicio PE01_PE.Pub:runGetAuth ");

		// Ejecuta el servicio PE1.Pub:runGetAuth
		String respuestaAuth = pe1Util.ejecutarRunGetAuthPE(folio);
		System.out.println(respuestaAuth);

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuestaAuth, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + wmCodeRequest);// Se agrega a la evidencia el wm_code

		System.out.println("WMCodeAuth: " + wmCodeRequest);

// Paso 3	******************************************************************************************
		
		addStep(" Verificar la respuesta generada por el servicio ");

		// Se valida que el wm_code del xml de respuesta sea igual a 101
		boolean validationRequestServAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
		System.out.println(validationRequestServAuth + " - wmCode request: " + wmCodeRequest);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuestaAuth);// Se agrega la respuesta a la evidencia

		assertTrue(validationRequestServAuth, "WM_CODE es diferente a 101");

		addStep(" Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER ");

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
		
		// Se valida que sea igual a 101
		boolean validationDbAuth = wmCodeDb.equals(wmCodeToValidateAuth);
		System.out.println(validationDbAuth + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDbAuth, "WM_CODE es diferente a 101");

		addStep(" Run ACK cierre de la transaccion ");

		// Ejecuta el servicio PE1.Pub:runGetAck
		String respuestaAck = pe1Util.ejecutarRunGetAckPE(folio);
//	     String subRespuestaAck =getSimpleDataXml(respuestaAck, "value");

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuestaAck, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);// Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("Wm Code: " + wmCodeRequest);// Se agrega a la evidencia el wm_code

		System.out.println("WMCodeACK: " + wmCodeRequest);


//	 assertTrue(valdateACK);

// Paso 4	******************************************************************************************
		
		addStep(" Verificar la respuesta generada por el servicio ");

		// Se valida que el wm_code del xml de respuesta sea igual a 101
		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
		System.out.println(validationRequestAck + " - wmCode request: " + wmCodeRequest);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuestaAck);// Se agrega la respuesta a la evidencia
		assertTrue(validationRequestAck, "WM_CODE es diferente a 101");

// Paso 5	******************************************************************************************
		addStep(" Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER ");

		// Se forma el query a utilizar
		String queryAck = String.format(tdcTransactionQueryAck, folio);
		System.out.println(queryAck);

		// Se realiza la consulta a la BD y se obtiene el wm_code
//	 		    wmCodeDb = pe1Util.getWmCodeQuery(queryAuth);	
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

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PE01_Peru";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar la operación para Generar Folio válido de la tienda.";
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
