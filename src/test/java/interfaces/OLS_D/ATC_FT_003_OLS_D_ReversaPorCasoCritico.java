package interfaces.OLS_D;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import om.OLS_D;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * MTC-FT-008-TPE_OLS_D Reversa exitosa por Caso Critico de transacci�n de pago de servicio a un proveedor<br>
 * Desc: En este escenario, se estara validando que se realice una reversa exitosa por caso cr�tico 
 * de un pago de servicio a un proveedor.
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */

public class ATC_FT_003_OLS_D_ReversaPorCasoCritico extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_OLS_D_ReversaPorCasoCritico_test(HashMap<String, String> data) throws Exception{
		/*
		 * Utileria******************************************************/
		SQLUtil dbFCTPEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbFCTPEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,GlobalVariables.DB_PASSWORD_TPE_LOT);
		SQLUtil dbFCWMLTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		SQLUtil dbFCWMLTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA,GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
//		SQLUtil dbOXWMLOGQA = new SQLUtil(GlobalVariables.DB_HOST_OXWMLOGQA, GlobalVariables.DB_USER_OXWMLOGQA,GlobalVariables.DB_PASSWORD_OXWMLOGQA);
		
		OLS_D ols_dUtil = new OLS_D(data, testCase, null);
		/*
		 * Variables*****************************************************/
		String queryTransaction = "SELECT APPLICATION, ENTITY, OPERATION, FOLIO, MTI,WM_CODE, WM_DESC, CREATION_DATE\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION\r\n"
				+ "WHERE APPLICATION = '%s' \r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO IN ('%s', '%s')";
		
		String queryUnsafeTransaction = "SELECT APPLICATION, OPERATION, FOLIO, CREATION_DATE\r\n"
				+ "FROM  WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND APPLICATION = '%s'";
		
		String queryErrorRecords = "SELECT ERROR_TYPE, ERROR_CODE, DESCRIPTION, MESSAGE, ACTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE TPE_TYPE LIKE '%s'\r\n"
				+ "AND CR_PLAZA = '%s'\r\n"
				+ "AND CR_TIENDA = '%s'\r\n"
				+ "AND ERROR_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND (DESCRIPTION LIKE '%s' OR MESSAGE LIKE '%s' OR TPE_TYPE='SE.CTR' OR TPE_TYPE LIKE '%s' OR TPE_TYPE LIKE '%s' OR TPE_TYPE LIKE '%s')";
		
		SoftAssert softAssert = new SoftAssert();
		testCase.setTest_Description(data.get("Descripcion"));
//		testCase.setFullTestName(data.get("Name"));
		testCase.setPrerequisites(data.get("Pre- Requisitos"));
		/******************************Paso 1****************************/
		addStep("Modificar el XML de ejemplo como se requiera  y realizar la consulta un servicio en l�nea a un proveedor:");
		
		String responseQRY01 = ols_dUtil.QRY01();
		String wmCode = RequestUtil.getWmCodeXml(responseQRY01);
		String folioService = RequestUtil.getFolioXml(responseQRY01);
//		Integer total = null;
//		if(wmCode.equals(data.get("checkWmCode"))) {
//			total = Integer.parseInt(RequestUtil.getTotal(responseQRY01));
//		}
//		System.out.println("wmCode: " + wmCode + " Total: " + total);
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("checkWmCode"));
		softAssert.assertEquals(wmCode, data.get("checkWmCode"), "Paso 1: Se obtiene wmCode diferente al esperado.");
		
		/******************************Paso 2****************************/
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de folio de la TPE_OLS_D "
				+ "para una transacci�n de pago de servicio mediante el siguiente request:");
		
		String responseTRN01 = ols_dUtil.TRN01();
		String TRN01wmCode = RequestUtil.getWmCodeXml(responseTRN01);
		String folio = "";
		String resCreationDate = "";
		if(TRN01wmCode.equals(data.get("FolioWmCode"))) {
			folio = RequestUtil.getFolioXml(responseTRN01);
			resCreationDate = RequestUtil.getCreationDate(responseTRN01);
		}
		System.out.println("Folio: " + folio + " wmCode: " + TRN01wmCode );
		System.out.println(" Creationdate: " + resCreationDate );
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + TRN01wmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("FolioWmCode"));
		softAssert.assertEquals(TRN01wmCode, data.get("FolioWmCode"), "Paso 2: Se obtiene wmCode diferente al esperado.");
		
		/******************************Paso 3****************************/
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de Autorizaci�n de la TPE_OLS_D mediante el siguiente request:");
		
		String responseAuth = ols_dUtil.TRN02(folio);
		String authWmCode = RequestUtil.getWmCodeXml(responseAuth);
		
		String [] arr = responseAuth.split("auth=\"");
		String [] arr2 = arr[1].split("\"");
		String auth = arr2[0];
		System.out.print("\r\n");
		System.out.print("\r\nauth: "+ auth +"\r\n");
		
		System.out.println("Auth: " + auth );
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + authWmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("authWmCode"));
		softAssert.assertEquals(authWmCode, data.get("authWmCode"), "Paso 3: Se obtiene wmCode diferente al esperado.");
		
		/******************************Paso 4****************************/
		addStep("Ingresar al navegador para solicitar confirmaci�n ACK de una transacci�n de pago de servicio DISH mediante el sigueinte request:");
		
		String responseAck = ols_dUtil.TRN03(folio);
		String ackWmCode = RequestUtil.getWmCodeXml(responseAck);
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + ackWmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("ackWmCode"));
		softAssert.assertEquals(ackWmCode, data.get("ackWmCode"), "Paso 4: Se obtiene wmCode diferente al esperado.");
		
		/******************************Paso 5****************************/
		addStep("Ejecutar en un navegador directamente el servicio de Reversa por caso critico de la TPE_OLS_D:");
		
		String responseReverse = ols_dUtil.TRN04(folio, auth, resCreationDate);
		String reverseWmCode = RequestUtil.getWmCodeXml(responseReverse);
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + reverseWmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("reversewmCode"));
		softAssert.assertEquals(reverseWmCode, data.get("reversewmCode"), "Paso 5: Se obtiene wmCode diferente al esperado.");
		
		/******************************Paso 6****************************/
		addStep("Realizar conexi�n a la BD FCTPEQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTPEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 7****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCTPEQA para validar que se registr� la transacci�n "
				+ "de pago de servicio DISH en la tabla TPE_FR_TRANSACTION");
		
		String transactionFormat = String.format(queryTransaction, data.get("application"), folioService, folio);
		SQLResult transaction = dbFCTPEQA_QRO.executeQuery(transactionFormat);
		System.out.println(transactionFormat);
		String creationDate = "";
		if(!transaction.isEmpty()) {
			creationDate = transaction.getData(0, "CREATION_DATE");
			testCase.addQueryEvidenceCurrentStep(transaction);
		}
		softAssert.assertFalse(transaction.isEmpty(), "Paso 7: No se obtuvo registro de la transaccion.");
		/******************************Paso 8****************************/
		addStep("Realizar conexi�n a la BD FCWMLTAEQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCWMLTAEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 9****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCWMLTAEQA para validar que se registr� la ejecuci�n hecha por el canal no seguro:");
		
		System.out.println("FECHA: " + creationDate);
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = originalFormat.parse(creationDate);
        date.setTime(date.getTime() - 2000);
        System.out.println("Date: " + date);
        String stringDate = targetFormat.format(date);
        System.out.println("String Date" + stringDate);
		
		String unsafeTrasactionFormat = String.format(queryUnsafeTransaction, stringDate, data.get("application"));
		SQLResult unsafeTransaction = dbFCWMLTAEQA_QRO.executeQuery(unsafeTrasactionFormat);
		System.out.println(unsafeTrasactionFormat);
		
		if(!unsafeTransaction.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(unsafeTransaction);
		}
//		softAssert.assertTrue(unsafeTransaction.isEmpty(), "Paso 9: La transaccion no viajo por un canal seguro");
		
		/******************************Paso 10****************************/
		addStep("Ejecutar la siguiente consulta en la BD FCWMLTAEQA para validar que no se hayan registrado errores en la ejecuci�n de la OLS_D:");
		
		String errorRecordsFormat = String.format(queryErrorRecords, "%" + data.get("application") + "%", data.get("plaza"), data.get("tienda"), stringDate, "%" + data.get("application") + "%",
				"%" + data.get("application") + "%", "%SEC%", "%" + data.get("application") + "%", "%TPE%");
		SQLResult errorRecords = dbFCWMLTAEQA_QRO.executeQuery(errorRecordsFormat);
		System.out.println(errorRecordsFormat);
		
		testCase.addQueryEvidenceCurrentStep(errorRecords);
		softAssert.assertTrue(errorRecords.isEmpty(), "Paso 10: Se obtuvieron registros de error en la transaccion.");
		softAssert.assertAll();
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
		return "ATC_FT_003_OLS_D_ReversaPorCasoCritico_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
