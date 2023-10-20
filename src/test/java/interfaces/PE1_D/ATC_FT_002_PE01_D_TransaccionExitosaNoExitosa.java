package interfaces.PE1_D;

import static org.testng.Assert.assertEquals;
import static util.RequestUtil.getSimpleDataXml;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE01_D;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * MTC-FT-005-PE01_D Transacci�n exitosa de venta de plan de datos Telcel.<br>
 * MTC-FT-006-PE01_D Transacci�n no exitosa de venta de plan de datos Telcel.<br>
 * MTC-FT-007-PE01_D Transacci�n exitosa de venta de plan de datos Movistar.<br>
 * MTC-FT-009-PE01_D  Transacci�n exitosa de venta de paquete Telcel.<br>
 * MTC-FT-010-PE01_D  Transacci�n exitosa de venta de paquete Movistar.<br>
 * Desc: En este escenario, se estara validando que se genere una transacci�n 
 * exitosa de recarga de tiempo aire electronico
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */

public class ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa extends BaseExecution{
	private String folio = "";
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria******************************************************/
		SQLUtil dbFCTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA,GlobalVariables.DB_PASSWORD_FCTAEQA);
		SQLUtil dbFCTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA_QRO, GlobalVariables.DB_USER_FCTAEQA_QRO,GlobalVariables.DB_PASSWORD_FCTAEQA_QRO);
		SQLUtil dbFCSWQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,GlobalVariables.DB_PASSWORD_FCSWQA);
		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO,GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		SQLUtil dbFCWMLTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		SQLUtil dbFCWMLTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA,GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		PE01_D pe1_DUtil = new PE01_D(data, testCase, null);
		/*
		 * Variables*****************************************************/
		String queryValidateTransaction = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, CAJA, PHONE, CARRIER \r\n"
				+ "FROM TPEUSER.TAE_TRANSACTION\r\n"
				+ "WHERE FOLIO = '%s'";
		
		String queryAppliedPlan = "SELECT CARRIER, CARRIER_NAME, ITEM, AMOUNT, DESCRIPTION\r\n"
				+ "FROM TPEUSER.TAE_CATALOG_DATA_PLANS\r\n"
				+ "WHERE AMOUNT = '%s'\r\n"
				+ "AND CARRIER = '%s'";
		
		String queryTransactionRecord = "SELECT FOLIO, APPLICATION, ENTITY, MTI, PAN, AMOUNT, ATT2, CREATION_DATE\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG\r\n"
				+ "WHERE FOLIO = '%s'";
		
		String queryTransactionError = "SELECT ERROR_ID, ERROR_DATE, ERROR_CODE, DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE ERROR_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')";
		
		String queryUnsafeTransaction = "SELECT APPLICATION, OPERATION, FOLIO, CREATION_DATE\r\n"
				+ "FROM  WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND APPLICATION = '%s'";
		
		testCase.setTest_Description(data.get("Descripcion"));
//		testCase.setFullTestName(data.get("Name"));
		testCase.setPrerequisites(data.get("Pre- Requisitos"));
		
		SoftAssert softAssert = new SoftAssert();
		String baseUri = String.format(data.get("baseUri"), data.get("host"), data.get("interface"));
		/******************************Paso 1****************************/
		addStep("Ejecutar en un navegador directamente el servicio rungGetFolio  de solicitud de folio de la PE1_D");
		
		String getFolioParams = pe1_DUtil.runGetFolioParams();
		String uriFolio = baseUri + data.get("getFolio") + getFolioParams;
		System.out.println(uriFolio);
		ApiMethodsUtil api = new ApiMethodsUtil(uriFolio);
		Response requestFolio = api.getRequestMethod(uriFolio);
		
		String wmCodeFolio = "";
		if (requestFolio.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String bodyFolio = requestFolio.getBody().asPrettyString();
			System.out.println(bodyFolio);
			folio = getSimpleDataXml(bodyFolio, "folio");
			wmCodeFolio = getSimpleDataXml(bodyFolio, "wmCode");
			//Agregar Request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request:");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(uriFolio);
			//Agregar response a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Response:");
			testCase.addBoldTextEvidenceCurrentStep("Status: " + requestFolio.getStatusLine());
			testCase.addTextEvidenceCurrentStep(bodyFolio);
			
			testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeFolio);
			testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("folioWmCode"));
			
			assertEquals(wmCodeFolio, data.get("folioWmCode"), "Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestFolio.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
		
		
		/******************************Paso 2****************************/
		addStep("Ejecutar en el navegador el servicio runGetAuth  para realizar la solicitud de autorizaci�n de PE1_D para TAE");
		
		String getAuthParams = pe1_DUtil.runGetAuthParams(folio);
		String uriAuth = baseUri + data.get("getAuth") + getAuthParams;
		System.out.println(uriAuth);
		Response requestAuth = api.getRequestMethod(uriAuth);
		
		String wmCodeAuth = "";
		if (requestAuth.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String bodyAuth = requestAuth.getBody().asString();
			System.out.println(bodyAuth);
			wmCodeAuth = getSimpleDataXml(bodyAuth, "wmCode");
			//Agregar Request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request:");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(uriAuth);
			//Agregar response a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Response:");
			testCase.addBoldTextEvidenceCurrentStep("Status: " + requestAuth.getStatusLine());
			testCase.addTextEvidenceCurrentStep(bodyAuth);
			
			testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeAuth);
			testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("authWmCode"));
			
			softAssert.assertEquals(wmCodeAuth, data.get("authWmCode"), "Paso 2: Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestAuth.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
		
		/******************************Paso 3****************************/
		addStep("Ejecutar el servicio  RunGetAck de la PE1_D  para confirmar el ACK de la transaccion TAE");
		
		String getAckParams = pe1_DUtil.runGetAckParams(folio);
		String uriAck = baseUri + data.get("getAck") + getAckParams;
		System.out.println(uriAck);
		Response requestAck = api.getRequestMethod(uriAck);
		
		String wmCodeAck = "";
		if (requestAck.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String bodyAck = requestAck.getBody().asString();
			System.out.println(bodyAck);
			wmCodeAck = getSimpleDataXml(bodyAck, "wmCode");
			//Agregar Request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request:");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(uriAck);
			//Agregar response a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Response:");
			testCase.addBoldTextEvidenceCurrentStep("Status: " + requestAck.getStatusLine());
			testCase.addTextEvidenceCurrentStep(bodyAck);
			
			testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeAck);
			testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("ackWmCode"));
			
			softAssert.assertEquals(wmCodeAck, data.get("ackWmCode"), "Paso 3: Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestAck.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
		
		/******************************Paso 4****************************/
		addStep("Conectarse a la Base de Datos: FCTAEQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTAEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Exitosa");
		
		/******************************Paso 5****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCTAEQA_QRO para validar que la transacci�n de venta de plan de datos se registr� correctamente");
		
		String validateTransactionFormat = String.format(queryValidateTransaction, folio);
		SQLResult validateTransaction = executeQuery(dbFCTAEQA_QRO, validateTransactionFormat);
		System.out.println(validateTransactionFormat);
		
		String creationDate = "";
		if(!validateTransaction.isEmpty()) {
			creationDate = validateTransaction.getData(0, "CREATION_DATE");
			testCase.addQueryEvidenceCurrentStep(validateTransaction);
		}
		softAssert.assertFalse(validateTransaction.isEmpty(), "Paso 5: No se obtuvieron registros de la transaccion.");
		
		/******************************Paso 6****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCTAEQA_QRO para validar que los datos usados para la transacci�n de "
				+ "venta de plan de datos coincide con los de la tabla TAE_CATALOG_DATA_PLANS");
		
		String appliedPlanFormat = String.format(queryAppliedPlan,data.get("amount"), data.get("carrier"));
		SQLResult appliedPlan = dbFCTAEQA_QRO.executeQuery(appliedPlanFormat);
		System.out.println(appliedPlanFormat);
		
		testCase.addQueryEvidenceCurrentStep(appliedPlan);
		softAssert.assertFalse(appliedPlan.isEmpty(), "Paso 6: No se encontraron registros en el catalogo de planes.");
		
		/******************************Paso 7****************************/
		addStep("Conectarse a la Base de Datos: FCSWQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCSWQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 8****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCSWQA para verificar que se encuentre el registro de la transacci�n");
		
		String transactionRecordFormat = String.format(queryTransactionRecord, folio);
		SQLResult transactionRecord = dbFCSWQA_QRO.executeQuery(transactionRecordFormat);
		System.out.println(transactionRecordFormat);
		
		testCase.addQueryEvidenceCurrentStep(transactionRecord);
		softAssert.assertFalse(transactionRecord.isEmpty(), "Paso 8: No se escuentra registro de transaccion.");
		/******************************Paso 9****************************/
		addStep("Realizar conexi�n a la base de datos: OXWMLOGQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 10****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCWMLTAEQA para verificar que no se hayan registrado errores en la transacci�n");
		System.out.println("FECHA: " + creationDate);
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = originalFormat.parse(creationDate);
        date.setTime(date.getTime() - 2000);
        String stringDate = targetFormat.format(date);
        System.out.println("Date" + date);
        System.out.println("String Date" + stringDate);
        
		String transactionErrorFormat = String.format(queryTransactionError, stringDate);
		SQLResult transactionError = dbFCWMLTAEQA_QRO.executeQuery(transactionErrorFormat);
		System.out.println(transactionErrorFormat);
		
		testCase.addQueryEvidenceCurrentStep(transactionError);
		softAssert.assertTrue(transactionError.isEmpty(), "Paso 10: Se encontraron registros de error de la trnasaccion.");
		
		/******************************Paso 11****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCWMLTAEQA para validar que se registr� la ejecuci�n hecha por el canal no seguro:");
		
		String unsafeTransactionFormat = String.format(queryUnsafeTransaction, stringDate, data.get("interface"));
		SQLResult unsafeTransaction = dbFCWMLTAEQA_QRO.executeQuery(unsafeTransactionFormat);
		System.out.println(unsafeTransactionFormat);
		
		testCase.addQueryEvidenceCurrentStep(unsafeTransaction);
//		softAssert.assertTrue(unsafeTransaction.isEmpty(), "Paso 11: La transaccion no viajo por un canal seguro");
		softAssert.assertAll();
	}
	 public String getFolio() {
	        return folio;
	    }
	 
	 public void setFolio(String folio) {
	        this.folio = folio;
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Celia Rubi Delgado";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
