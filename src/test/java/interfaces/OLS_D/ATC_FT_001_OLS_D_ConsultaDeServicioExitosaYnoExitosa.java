package interfaces.OLS_D;

import static org.testng.Assert.assertEquals;

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
 * MTC-FT-001-TPE_OLS_D Realizar consulta de servicios en l�nea existosa. <br>
 * MTC-FT-002-TPE_OLS_D Realizar consulta de servicios en l�nea no exitosa. <br>
 * Desc: En este escenario, se estara validando que se realice una consulta exitosa y no exitosa 
 * sobre un servicio en l�nea.
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */

public class ATC_FT_001_OLS_D_ConsultaDeServicioExitosaYnoExitosa extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OLS_D_ConsultaDeServicioExitosaYnoExitosa_test (HashMap<String, String>data) throws Exception{
		/*
		 * Utileria******************************************************/
		SQLUtil dbFCTPEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbFCTPEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,GlobalVariables.DB_PASSWORD_TPE_LOT);
		SQLUtil dbFCWMLTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		SQLUtil dbFCWMLTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA,GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		OLS_D ols_dUtil = new OLS_D(data, testCase, null);
		/*
		 * Variables*****************************************************/
		String queryTransaction = "SELECT APPLICATION, ENTITY, OPERATION, FOLIO, WM_CODE, WM_DESC, CREATION_DATE\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'";
		
		String queryErrorTransaction = "SELECT ERROR_TYPE, ERROR_CODE, DESCRIPTION, MESSAGE, ACTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE TPE_TYPE LIKE '%s'\r\n"
				+ "AND CR_PLAZA = '%s'\r\n"
				+ "AND CR_TIENDA = '%s'\r\n"
				+ "AND ERROR_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND (DESCRIPTION LIKE '%s' OR MESSAGE LIKE '%s' OR TPE_TYPE='SE.CTR' OR TPE_TYPE LIKE '%s' OR TPE_TYPE LIKE '%s' OR TPE_TYPE LIKE '%s')";
		
		String queryInvokedServices = "SELECT APPLICATION, ENTITY, OPERATION, SOURCE, PLAZA, TIENDA, CAJA\r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND APPLICATION = '%s'";
		
		SoftAssert softAssert = new SoftAssert();
		testCase.setTest_Description(data.get("Descripcion"));
//		testCase.setFullTestName(data.get("Name"));
		testCase.setPrerequisites(data.get("Pre- Requisitos"));
		/******************************Paso 1****************************/
		addStep("Modificar el XML de ejemplo como se requiera  y realizar la consulta de un servicio en l�nea a un proveedor.");
		
		String responseQRY01 = ols_dUtil.QRY01();
		String wmCode = RequestUtil.getWmCodeXml(responseQRY01);
		String folio = "";
		if(wmCode.equals(data.get("checkWmCode"))) {
			folio = RequestUtil.getFolioXml(responseQRY01);
		}
		System.out.println(data.get("refDesc"));
		System.out.println("Folio: " + folio + " wmCode: " + wmCode );
		testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCode);
		testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("checkWmCode"));
		assertEquals(wmCode, data.get("checkWmCode"), "Se obtiene wmCode diferente al esperado.");
		Thread.sleep(20000);
		
		/******************************Paso 2****************************/
		addStep("Conectarse a la Base de Datos: FCTPEQA con esquema TPEUSER");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTPEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 3****************************/
		addStep("Ejecutar la siguiente consulta para validar que la transacci�n se registr� correctamente "
				+ "en la tabla TPE_FR_TRANSACTION  de la BD");
		
		String transactionFormat = String.format(queryTransaction, folio);
		Thread.sleep(10000);
		SQLResult transaction = dbFCTPEQA_MTY.executeQuery(transactionFormat);
		System.out.println(transactionFormat);
		
		String creationDate = "";
		if(!transaction.isEmpty()) {
			creationDate = transaction.getData(0, "CREATION_DATE");
			testCase.addQueryEvidenceCurrentStep(transaction);
		}
		softAssert.assertFalse(transaction.isEmpty(), "No se encontraron registros de la transaccion");
		
		/******************************Paso 4****************************/
		addStep("Conectarse a la Base de Datos: FCTPEQA");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTPEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
		/******************************Paso 5****************************/
		addStep("Ejecutar la siguiente consulta para validar que la transacci�n se registr� correctamente "
				+ "en la tabla TPE_FR_TRANSACTION  de la BD");
		
		SQLResult transaction_qro = dbFCTPEQA_QRO.executeQuery(transactionFormat);
		System.out.println(transactionFormat);

		testCase.addQueryEvidenceCurrentStep(transaction_qro);
		softAssert.assertFalse(transaction_qro.isEmpty(), "No se encontraron registros de la transaccion");
		/******************************Paso 6****************************/
		addStep("Conectarse a la Base de Datos: FCWMLTAQ");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCWMLTAQ");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		/******************************Paso 7****************************/
		addStep("Realizar la siguiente consulta en la BD FCWMLTAQ.FEMCOM.NET del Site en la que se realiz� la transacci�n:");
		
		System.out.println("FECHA: " + creationDate);
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = originalFormat.parse(creationDate);
        date.setTime(date.getTime() - 2000);
        String stringDate = targetFormat.format(date);
        System.out.println("Date" + date);
        System.out.println("String Date" + stringDate);
        
		String errorTransactionFormat = String.format(queryErrorTransaction, "%" + data.get("application") + "%", data.get("plaza"), data.get("tienda"), stringDate, "%" + data.get("application") + "%",
				"%" + data.get("application") + "%", "%SEC%", "%" + data.get("application") + "%", "%TPE%");
		SQLResult errorTransaction = dbFCWMLTAEQA_QRO.executeQuery(errorTransactionFormat);
		System.out.println(errorTransactionFormat);
		
		testCase.addQueryEvidenceCurrentStep(errorTransaction);
		softAssert.assertTrue(errorTransaction.isEmpty(), "Paso 7: Se obtuvieron registros de error en la transaccion.");
		/******************************Paso 8****************************/
		addStep("Realizar la siguiente consulta en la BD FCWMLTAQ.FEMCOM.NET del Site en la que se realiz� la transacci�n, "
				+ "para validar el registro de los servicios invocados de la OLS_D:");
		String invokedServicesFormat = String.format(queryInvokedServices, stringDate , data.get("application"));
		SQLResult invokedServices = dbFCWMLTAEQA_QRO.executeQuery(invokedServicesFormat);
		System.out.println(invokedServicesFormat);
		
		testCase.addQueryEvidenceCurrentStep(invokedServices);
//		softAssert.assertTrue(errorTransaction.isEmpty(), "Paso 8: La transaccion no viajo por un canal seguro.");
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
		return "ATC_FT_001_OLS_D_ConsultaDeServicioExitosaYnoExitosa_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
