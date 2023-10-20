package interfaces.pe3;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE3;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_001_PE3_Flujo_Completo_De_Autorizacion extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE3_Flujo_Completo_De_Autorizacion_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		PE3 pe3Util = new PE3(data, testCase, db);

		/**
		 * ALM
		 * CP: Solicitud de Folio
		 * Falta el XML para probar con su server y puerto
		 */
		
		/*
		 * Variables
		 *********************************************************************/

		String wmCodeToValidate = "100";
		String wmCodeToValidateAuth = "000";
		String wmCodeToValidateAck = "101";
		
		String tdcTransactionQuery = "SELECT FOLIO, CREATION_DATE, UPC,"
				+ " CARD_NO, AMOUNT, WM_CODE "
				+ "FROM TPEUSER.GIF_TRANSACTION WHERE FOLIO = %s";

		String folio;
		String wmCodeRequest;
		String wmCodeDb;

		/**************************************************************************************************
		 * Pasos
		 *************************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Llamar al servico PE3.Pub:runGetFolio");

		String respuesta = pe3Util.ejecutarRunGetFolio();

		folio = getSimpleDataXml(respuesta, "folio");

		wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");

		boolean validationResponse = true;

		if (respuesta != null) {
			validationResponse = false;
		}

		assertFalse(validationResponse);

		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Verificar la respuesta generada por el servicio");

		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);

		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);

		assertTrue(validationRequest, "El campo wmCode no es el esperado");
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Validar que se ha creado un registro del folio en la tabla tpeuser.gif_transaction de TPEUSER");

		String query = String.format(tdcTransactionQuery, folio);
		System.out.println(query);

		SQLResult resultQuery = db.executeQuery(query);

		wmCodeDb = resultQuery.getData(0, "WM_CODE");
		
		testCase.addQueryEvidenceCurrentStep(resultQuery);

		boolean validationDb = wmCodeDb.equals(wmCodeToValidate);

		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDb, "El campo wmCode no es el esperado");

		/**************************************************************************************************
		 * Solicitud de autoriazaci�n
		 *************************************************************************************************/

		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Llamar al servico PE3.Pub:runGetAuth");

		String respuestaAuth = pe3Util.ejecutarRunGetAuth(folio);

		folio = getSimpleDataXml(respuestaAuth, "folio");

		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");

		if (respuesta != null) {
			validationResponse = false;
		}

		assertFalse(validationResponse);
		
		/*
		 * Paso 5
		 *****************************************************************************************/

		addStep("Verificar la respuesta generada por el servicio");

		boolean validationRequestAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
		
		System.out.println(validationRequestAuth + " - wmCode request: " + wmCodeRequest);

		testCase.addTextEvidenceCurrentStep(respuestaAuth);

		assertTrue(validationRequestAuth, "El campo wmCode no es el esperado");

		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep("Validar que se ha creado un registro del folio en la tabla tpeuser.gif_transaction de TPEUSER");

		String queryAuth = String.format(tdcTransactionQuery, folio);
		
		System.out.println(queryAuth);

		SQLResult resultQueryAuth = db.executeQuery(queryAuth);

		wmCodeDb = resultQueryAuth.getData(0, "WM_CODE");
		
		testCase.addQueryEvidenceCurrentStep(resultQueryAuth);
		
		boolean validationDbAuth = wmCodeDb.equals(wmCodeToValidateAuth);

		System.out.println(validationDbAuth + " - wmCode db: " + wmCodeToValidateAuth);

		assertTrue(validationDbAuth, "El campo wmCode no es el esperado");

		/**************************************************************************************************
		 * Solicitud de autoriazaci�n ACK
		 *************************************************************************************************/
		/*
		 * Paso 7
		 *****************************************************************************************/

		addStep("Llamar al servico PE3.Pub:runGetFolio");

		String respuestaAck = pe3Util.ejecutarRunGetAck(folio);

		folio = getSimpleDataXml(respuestaAck, "folio");

		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");

		if (respuesta != null) {
			validationResponse = false;
		}

		assertFalse(validationResponse);

		/*
		 * Paso 8
		 *****************************************************************************************/

		addStep("Verificar la respuesta generada por el servicio");

		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
		
		System.out.println(validationRequestAck + " - wmCode request: " + wmCodeRequest);

		testCase.addTextEvidenceCurrentStep("Response \n" + respuestaAck);

		assertTrue(validationRequestAck, "El campo wmCode no es el esperado");

		/*
		 * Paso 9
		 *****************************************************************************************/
		addStep("Validar que se ha creado un registro del folio en la tabla tpeuser.gif_transaction de TPEUSER");

		String queryAck = String.format(tdcTransactionQuery, folio);
		
		System.out.println(queryAck);
		
		SQLResult resultQueryAck = db.executeQuery(queryAck);

		wmCodeDb = resultQueryAck.getData(0, "WM_CODE");
		
		testCase.addQueryEvidenceCurrentStep(resultQueryAck);

		boolean validationDbAck = wmCodeDb.equals(wmCodeToValidateAck);

		System.out.println(validationDbAuth + " - wmCode db: " + wmCodeToValidateAck);

		assertTrue(validationDbAck, "El campo wmCode no es el esperado");

	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Flujo completo de autorizacion (generacion de folio, autorizacion y ACK)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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
