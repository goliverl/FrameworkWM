package interfaces.Tpe_Loy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Tpe_Loy;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_006_TPE_LOY_TransaccionExitosaDevolucionDeAcumulacion extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_TPE_LOY_TransaccionExitosaDevolucionDeAcumulacion_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utilerías
		 ********************************************************************/
		SQLUtil dbOXTPEQA = new SQLUtil(GlobalVariables.DB_HOST_OXTPEQA, GlobalVariables.DB_USER_OXTPEQA,GlobalVariables.DB_PASSWORD_OXTPEQA);
//		SQLUtil dbOXWMLOGQA = new SQLUtil(GlobalVariables.DB_HOST_OXWMLOGQA, GlobalVariables.DB_USER_OXWMLOGQA,GlobalVariables.DB_PASSWORD_OXWMLOGQA);
		
		Tpe_Loy TpeLoyUtil = new Tpe_Loy(data, testCase, null);
		/*
		 * Variables
		 ********************************************************************/
		String queryOperationRecords = "SELECT * "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION "
				+ "WHERE CREATION_DATE>= '30082022 0920'  \r\n"
				+ "AND APPLICATION='LOY' "
				+ "AND ENTITY='BL' "
				+ "AND FOLIO IN('3445100054','3445100056','3445120032') "
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryGetErrors = "SELECT * "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE "
				+ "WHERE ERROR_DATE BETWEEN '30082022 1200' AND '30082022 1205' "
				+ "ORDER BY ERROR_DATE DESC";
		
		String queryTransactionLogs = "SELECT * "
				+ "FROM WMLOG.SECURITY_SESSION_LOG "
				+ "WHERE CREATION_DATE BETWEEN '30082022 1200' AND '30082022 1205' "
				+ "ORDER BY CREATION_DATE DESC";
		
		/********************************Paso 1******************************/
		addStep("Ejecutar la invocacion QRY01, en nuestra barra de direccion para consultar informacion:");
		String ResponseGetQRY01 = TpeLoyUtil.QRY01();
		String wmCode = "";
		String folio = "";
		String creationDate = "";
		wmCode = RequestUtil.getWmCodeXml(ResponseGetQRY01);
		if(wmCode != data.get("ExpectedWmCode")) {
			folio = RequestUtil.getFolioXml(ResponseGetQRY01);
			creationDate = RequestUtil.getCreationDate(ResponseGetQRY01);
		}
		assertEquals(wmCode, data.get("ExpectedWmCode"), "Se obtiene wmCode diferente al esperado.");
		
		/********************************Paso 2******************************/
		addStep("Ejecutar la invocacion TRN01, en nuestra barra de direccion para solicitar un solo folio:");
		String ResponseGetTRN01 = TpeLoyUtil.TRN01withParams(creationDate);
		String wmCodeTRN01 = "";
		String folioTRN01 = "";
		
		wmCodeTRN01 = RequestUtil.getWmCodeXml(ResponseGetTRN01);
		folioTRN01 = RequestUtil.getFolioXml(ResponseGetTRN01);
		
		/********************************Paso 3******************************/
		addStep("Ejecutar la invocacion OPR01, en nuestra barra de direccion para transaccionar los puntos de la  compra del producto:");
		String ResponseGetOPR01 = TpeLoyUtil.OPR01withParams(folioTRN01, creationDate);
		
		//Pendiente validar wmCode
		/********************************Paso 4******************************/
		addStep("Ejecutar nuevamente la invocacion TRN01, en nuestra barra de direccion para solicitar un nuevo folio para la devolucion de acumulacion  de puntos:");
		String ResponseGetTRN01_2 = TpeLoyUtil.TRN01withParams(creationDate);
		String folioTRN01_2 = RequestUtil.getFolioXml(ResponseGetTRN01);
		
		
		/********************************Paso 5******************************/
		addStep("Ejecutar la invocacion OPR02, en nuestra barra de direccion para solicitar la devolucion de puntos de la  compra del producto:");
		String responseGetOPR02 = TpeLoyUtil.OPR02withParams(folioTRN01_2);
		
		/********************************Paso 6******************************/
		addStep("Ejecutar la consulta de la  BD \"OXTPEQA_PREM\", en la tabla TPEUSER.TPE_FR_TRANSACTION, para validar el registro de las Operaciones:");
		SQLResult operationRecords = executeQuery(dbOXTPEQA, queryOperationRecords);
		System.out.println(queryOperationRecords);
		if(!operationRecords.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(operationRecords);
		}
		assertFalse(operationRecords.isEmpty(), "No se obtuvieron registros de operacion.");
		
		/********************************Paso 7******************************/
		addStep("Ejecutar la consulta de la  BD \"OXWMLOGQA_PREM\", en la tabla WMLOG.WM_LOG_ERROR_TPE, para validar que no se hayan registrado errores:");
//		SQLResult getErrors = executeQuery(dbOXWMLOGQA, queryGetErrors);
//		System.out.println(queryGetErrors);
//		if(!getErrors.isEmpty()) {
//			testCase.addQueryEvidenceCurrentStep(getErrors);
//		}
//		assertTrue(getErrors.isEmpty(), "Se obtuvieron errores en la transaccion.");
		
		/********************************Paso 8******************************/
		addStep("Ejecutar la siguiente consulta en la BD \"OXWMLOGQA_PREM\", en la tabla WMLOG.SECURITY_SESSION_LOG  para validar el registro de las transacciones:");
//		SQLResult transactionLogs = executeQuery(dbOXWMLOGQA, queryTransactionLogs);
//		System.out.println(queryTransactionLogs);
//		if(!queryTransactionLogs.isEmpty()) {
//			testCase.addQueryEvidenceCurrentStep(queryTransactionLogs);
//		}
//		assertFalse(queryTransactionLogs.isEmpty(), "No se obtuvieron Logs de la transaccion.");
		
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
		return "Construido. ATC-FT-006 TPE_LOY Transaccion exitosa de devolución de acumulacion";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo-Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_TPE_LOY_TransaccionExitosaDevolucionDeAcumulacion_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
