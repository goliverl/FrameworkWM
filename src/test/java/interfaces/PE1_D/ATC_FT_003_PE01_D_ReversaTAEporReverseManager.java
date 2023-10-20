package interfaces.PE1_D;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE01_D;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * MTC-FT-014-PE01_D Reversa exitosa por reversemanager de recarga de Tiempo aire electronico Telcel<br>
 * Desc: En este escenario, se estara validando que se genere una reversemanager de recarga de tiempo aire
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */

public class ATC_FT_003_PE01_D_ReversaTAEporReverseManager extends BaseExecution{
	private String folio = "";
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PE01_D_ReversaTAEporReverseManager_test(HashMap<String, String> data) throws Exception{
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
		
		String queryReverseRecord = "SELECT CREATION_DATE, PLAZA, TIENDA, PHONE, CARRIER, AMOUNT, STATUS, WM_CODE\r\n"
				+ "FROM TPEUSER.TAE_REVERSE\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'";
		
		String queryTransaction = "SELECT FOLIO, APPLICATION, ENTITY, CREATION_DATE, PAN, AMOUNT, MTI\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'";
		
		String queryErrorRecords = "SELECT ERROR_ID, ERROR_DATE, ERROR_CODE, DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE ERROR_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')"
				+ "AND TPE_TYPE LIKE '%s'";
		
		String queryUnsafeTransaction = "SELECT APPLICATION, OPERATION, FOLIO, CREATION_DATE\r\n"
				+ "FROM  WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND APPLICATION = '%s'";
		
		testCase.setTest_Description(data.get("Descripcion"));
//		testCase.setFullTestName(data.get("Name"));
		testCase.setPrerequisites(data.get("Pre- Requisitos"));
		
		SoftAssert softAssert = new SoftAssert();
		String baseUri = String.format(data.get("baseUri"), data.get("host"), data.get("interface"));
		
		/*@Nota: Antes de ejecutar este script Solicitar poner en Hold el siguiente Job 
		 * ‘runPE1ReverseManager_wmprd9’ y grupo: ‘TQA_PREMIA’ al operador en turno de UsuOperadores, 
		 * del siguiente correo:(usufemcomoperadoressite@oxxo.com)*/
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
		addStep("Ejecutar en el navegador el servicio runGetAuth  para realizar la solicitud de autorización de PE1_D para TAE");
		
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
			
			softAssert.assertEquals(wmCodeAuth, data.get("authWmCode"), "Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestAuth.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
		
		/******************************Paso 3****************************/
		addStep("Ejecutar el siguiente Job ��runPE1_DReverseManager_wmprd9a y grupo: TQA_PREMIA");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		u.get(data.get("server"));
		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addTextEvidenceCurrentStep("Job en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("pas"));
		
		testCase.addTextEvidenceCurrentStep("Login");
		
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		testCase.addTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertFalse(!flag, "No se encontro el Job");
		
		//Ejecucion
		String resultado = control.executeJob();
		testCase.addTextEvidenceCurrentStep( resultado);
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		testCase.addTextEvidenceCurrentStep("Output: " + control.getOutput());
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		
		if(resultado.equals("Failure")) {
			
		casoPasado = false;
		
		System.out.println("La ejecucion del Job fue exitosa");
		
		}		
		assertTrue(casoPasado, "La ejecucion del job fallo");

		control.closeViewpoint(); 
		
		u.close();
		
		/******************************Paso 4****************************/
		addStep("Conectarse a la Base de Datos: FCTAEQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTAEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 5****************************/
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registra "
				+ "correctamente en la tabla TPEUSER.TAE_TRANSACTION de la BD FCTAEQA");
		
		String validateTransactionFormat = String.format(queryValidateTransaction, folio);
		SQLResult validateTransaction = executeQuery(dbFCTAEQA_QRO, validateTransactionFormat);
		System.out.println(validateTransactionFormat);
		
		String creationDate = "";
		if(!validateTransaction.isEmpty()) {
			creationDate = validateTransaction.getData(0, "CREATION_DATE");
			testCase.addQueryEvidenceCurrentStep(validateTransaction);
		}
		softAssert.assertFalse(validateTransaction.isEmpty(), "No se obtuvieron registros de la transaccion.");
		
		/******************************Paso 6****************************/
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente "
				+ "en la tabla TPEUSER.TAE_REVERSE de la BD FCTAEQA");
		
		String reverseRecordFormat = String.format(queryReverseRecord, folio);
		SQLResult reverseRecord = dbFCTAEQA_QRO.executeQuery(reverseRecordFormat);
		System.out.println(reverseRecordFormat);
		
		testCase.addQueryEvidenceCurrentStep(reverseRecord);
		softAssert.assertFalse(reverseRecord.isEmpty(), "No se encontro registro de la reversa");
		
		/******************************Paso 7****************************/
		addStep("Conectarse a la Base de Datos: FCSWQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCSWQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 8****************************/
		addStep("Ejecutar la siguiente consulta para validar la transaccion en la base de datos FCSWQA");
		
		String transactionFormat = String.format(queryTransaction, folio);
		SQLResult transaction = dbFCSWQA_QRO.executeQuery(transactionFormat);
		System.out.println(transactionFormat);
		
		testCase.addQueryEvidenceCurrentStep(transaction);
		softAssert.assertFalse(transaction.isEmpty(), "No se obtuvieron registros de la transaccion.");
		
		/******************************Paso 9****************************/
		addStep("Conectarse a la Base de Datos OXWMLOGQA_PREM");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: OXWMLOGQA_PREM");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 10****************************/
		addStep("Ejecutar la siguiente consulta en la base de datos OXWMLOGQA_PREM para validar que no se encuentren registros de error de la PE1_D");
		
		System.out.println("FECHA: " + creationDate);
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = originalFormat.parse(creationDate);
        date.setTime(date.getTime() - 3000);
        String StringDate = targetFormat.format(date);
        System.out.println("Date" + date);
        System.out.println("String Date" + StringDate);
        
		String errorRecordsFormat = String.format(queryErrorRecords, StringDate, "%PE01_D%");
		SQLResult errorRecords = dbFCWMLTAEQA_QRO.executeQuery(errorRecordsFormat);
		System.out.println(errorRecordsFormat);
		
		testCase.addQueryEvidenceCurrentStep(errorRecords);
		softAssert.assertTrue(errorRecords.isEmpty(), "Se obtuvieron registros de error en la transaccion.");
		
		/******************************Paso 11****************************/
		addStep("Ejecutar la siguiente consulta en la BD OXWMLOGQA_PREM para verificar que no viajo por un canal seguro.");
		
		String unsafeTransactionFormat = String.format(queryUnsafeTransaction, StringDate, data.get("interface"));
		SQLResult unsafeTransaction = dbFCWMLTAEQA_QRO.executeQuery(unsafeTransactionFormat);
		System.out.println(unsafeTransactionFormat);
		
		testCase.addQueryEvidenceCurrentStep(unsafeTransaction);
		softAssert.assertTrue(unsafeTransaction.isEmpty(), "La transaccion no viajo por un canal seguro.");
		
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
		return "ATC_FT_003_PE01_D_ReversaTAEporReverseManager_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
