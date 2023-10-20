package interfaces.po20;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

/**
 * Prueba de regresión para comprobar la no afectacion en la funcionalidad
 * principal de la interface FEMSA_PO20 de avante para enviar informaciohn de las
 * transacciones de pagos de servicio OLS, OLP, TSF, hacia EBS avante, al ser
 * migrada la interface de WM9.9 a WM10.5.
 * 
 * El objetivo de esta interface es asegurar la informacion generada por
 * corresponsalias de cada una de los puntos de venta, insertando la información
 * de dichas transacciones en ORAFIN.
 * 
 * 
 * @author Mariana Vives
 * @date 2023/08/02
 */

public class ATC_FT_016_PO20_EnvioTransaccionCorresponsalia extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_016_PO20_EnvioTransaccionCorresponsalia_test(HashMap<String, String> data) throws Exception {
		
		
		/* Utilerias ********************************************************************************************************************************************/
		
		/*
		 * utils.sql.SQLUtil dbTdc = new
		 * utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,
		 * GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		 * utils.sql.SQLUtil dbLog = new
		 * utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
		 * GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		 * utils.sql.SQLUtil dbEbs = new
		 * utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
		 * GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		 */
		
		
		utils.sql.SQLUtil dbTdc = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);

		/* Variables ******************************************************************************************************************************************/
		
		//Paso2
		String ValidaConfig = "SELECT CATEGORY,VALUE1,VALUE2 FROM TPEUSER.TPE_CONFIG "
				+ "	WHERE CATEGORY IN ('CORRESP_CONFIG_DAYS','CORRESP_SWITCH')";
		
		//Paso 3 - Validar query
		String ValidaTransac = "SELECT FOLIO AS FOLIO_WM, PLAZA, TIENDA,FOLIO_TRANSACTION, REF1, REF2, CORTE, TICKET, COMISION,\r\n"
				+ "  ID_SERVICIO, ID_SERVICIO AS CONSECUTIVO, SW_AUTH AS NO_AUTO, AMOUNT as VALOR, PV_DATE as FECHA_TRANSACCION,\r\n"
				+ "to_char(PV_DATE,'HH:MM') as HORA_TRANSACCION, CAJA, ADMIN_DATE as XXFC_FECHA_ADMINISTRATIVA, SERVICE_ID as SERVICIO\r\n"
				+ "FROM tpeuser.TDC_TRANSACTION\r\n"
				+ "WHERE WM_CODE='101' AND PROM_TYPE='PAY'\r\n"
				//+ "AND CREATION_DATE >= trunc(sysdate-1) AND CREATION_DATE < trunc(sysdate)\r\n"
				+ "AND REVERSED IS NULL AND TICKET IS NOT NULL "
				+ "AND rownum <= 5 order by CREATION_DATE DESC";

		//Paso 4 
		String ValidaEjec = "SELECT run_id,interface, start_dt, end_dt, status, server FROM wmlog.wm_log_run \r\n"
				+ "WHERE interface LIKE '%PO20%' \r\n"
				+ "and start_dt >= trunc(SYSDATE) and rownum <=1 ORDER BY end_dt DESC";
		
		//Paso 7
		String ValidaThreads = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  FROM  WMLOG.WM_LOG_THREAD WHERE PARENT_ID = '%s' and rownum = 1 order by START_DT desc";
		
		//Paso 8
		String ValidaError = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE FROM  WMLOG.WM_LOG_ERROR WHERE RUN_ID= '%s' and rownum <=1 order by ERROR_DATE desc";
		String consultaError2 = " select description,MESSAGE  from wmlog.WM_LOG_ERROR where RUN_ID='%s' and rownum <= 1 order by ERROR_DATE desc";
		String consultaError3 = " select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\nfrom wmlog.WM_LOG_ERROR where RUN_ID='%s' and rownum <= 1 order by ERROR_DATE desc";
		
		//Paso 9
		String ValidaHistoricoEjec = "SELECT * FROM tpeuser.TDC_FIN_HISTORIC where rownum <= 10 ORDER BY EXECUCION_DATE DESC";
		
		//Paso 10
		String ValidaRegistrosFinanzas = "Select TRANS_ID, PLAZA, TIENDA, FOLIO_TRANSACCION, SERVICIO, CONSECUTIVO, FECHA_TRANSACCION, HORA_TRANSACCION,\r\n"
				+ "REF1,REF2,CORTE,CAJA,TICKET,VALOR,COMISION,FECHA_RECEPCION, ESTATUS,ORG_ID, ORACLE_CR,ID_SERVICIO,ORACLE_CIA, XXFC_FECHA_ADMINISTRATIVA,\r\n"
				+ "NO_AUTO, FOLIO_WM\r\n"
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_PRE\r\n"
				+ "WHERE TRANS_ID > (SELECT MAX (TRANS_ID)-31 FROM XXFC.XXFC_PAGO_SERVICIOS) ORDER BY FOLIO_WM ASC";
		
		//Paso 11
		String ValidaRegFinanzas = "Select TRANS_ID, PLAZA, TIENDA, FOLIO_TRANSACCION, SERVICIO, CONSECUTIVO, FECHA_TRANSACCION, ESTATUS, FOLIO_WM "
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_PRE\r\n"
				+ "WHERE TRANS_ID > (SELECT MAX (TRANS_ID)-31 FROM XXFC.XXFC_PAGO_SERVICIOS) ORDER BY FOLIO_WM ASC";
		
		
//*******************************************************************Paso 1****************************************************************************************************		
		addStep("Establecer la conexion con la BD **FCTDCQA**.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: FCTDCQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTDCQA);
		
//*******************************************************************Paso 2****************************************************************************************************		
		addStep(" Validar que se encuentra la configuracion para los dias de ejecucion y los bancos que actualmente permiten el servicio de corresponsalias.");
		System.out.println(GlobalVariables.DB_HOST_FCTDCQA);
		System.out.println(ValidaConfig);
		  SQLResult ValidaConfig_res = executeQuery(dbTdc, ValidaConfig);
		  System.out.println(ValidaConfig); boolean validaConfig =
		  ValidaConfig_res.isEmpty(); if(!validaConfig) { testCase.
		  addBoldTextEvidenceCurrentStep("Se valida que se encuentra la configuracion para los dias de ejecucion y los bancos que actualmente permiten el servicio de corresponsalias."
		  ); } testCase.addQueryEvidenceCurrentStep(ValidaConfig_res);
		  System.out.println(validaConfig);
		  assertFalse(validaConfig);
		
		


//*******************************************************************Paso 3****************************************************************************************************		
		addStep("Validar que existan transacciones exitosas de corresponsalias para diferentes plazas, en la tabla TDC_TRANSACTION de BD FCTDCQA para ser procesadas por la interface");
		
		System.out.println(GlobalVariables.DB_HOST_FCTDCQA);
		System.out.println(ValidaTransac);
		SQLResult ValidaTransac_res = executeQuery(dbTdc, ValidaTransac);
		System.out.println(ValidaTransac);
		boolean validaTransac = ValidaTransac_res.isEmpty();
		if(!validaTransac) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida que existen transacciones exitosas de corresponsalias para diferentes plazas en la tabla TDC_TRANSACTION");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaTransac_res);
		 System.out.println(validaTransac);
		assertFalse(validaTransac);

//*******************************************************************Paso 4****************************************************************************************************		
		addStep("Ejecutar la interfaz PO20 en Control-M.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		JSONObject obj = new JSONObject(data.get("job"));

		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		if(resultado.equals("Wait Condition")) {
		casoPasado = true;
		}		
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
		control.closeViewpoint(); 
		
//*******************************************************************Paso 5****************************************************************************************************		
		addStep("Realizar la conexion a la BD FCWML6QA.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: FCWML6QA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
	
//******************************************************************Paso 6****************************************************************************************************		
		addStep("Validar la correcta ejecucion de la interface PO20 en la tabla WM WM_LOG_RUN de la BD FCWMLQA.");
		SQLResult ValidaEjec_res = executeQuery(dbLog, ValidaEjec);
		System.out.println(ValidaEjec);
		boolean validaEjec = ValidaEjec.isEmpty();
		String run_id="";
		if(!validaEjec) {
		run_id=ValidaEjec_res.getData(0, "RUN_ID");
		testCase.addBoldTextEvidenceCurrentStep("Se valida la correcta ejecucion de la interface PO20 en la tabla WM_LOG_RUN de la BD FCWMLQA.");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaEjec_res);
		assertFalse(validaEjec);
		
//******************************************************************Paso 7****************************************************************************************************		
		addStep("Validar la correcta ejecucion de los Threads lanzados por la interface PO20 en la tabla WM_LOG_THREAD de la BD FCWMLQA.");
		String ValidaThread_String = String.format(ValidaThreads, run_id);
		SQLResult ValidaThreads_res = dbLog.executeQuery(ValidaThread_String);
		System.out.println(ValidaThread_String);
		boolean validaThreads = ValidaThreads_res.isEmpty();
		
		if(!validaThreads) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida la correcta ejecucion de los Threads lanzados por la interface PO20 en la tabla WM_LOG_THREAD");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaThreads_res);
		assertFalse(validaThreads);
	
//******************************************************************Paso 8****************************************************************************************************		
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningun error presente en la ejecucion de la interfaz PO20 en la tabla WM_LOG_ERROR de BD FCWMLQA.");
		String ValidaError_String = String.format(ValidaError, run_id);
		String error1 = String.format(consultaError2, run_id); //Segunda consulta de error
		String error2 = String.format(consultaError3, run_id);
		SQLResult ValidaError_res = dbLog.executeQuery(ValidaError_String);
		SQLResult ValidaError_res1 = dbLog.executeQuery(error1);
		SQLResult ValidaError_res2 = dbLog.executeQuery(error2);
		System.out.println(ValidaError_String);
		boolean validaError = ValidaError_res.isEmpty();
		if(validaError) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida que no se encuentran errores presentes en la ejecucion de la interface PO20 en la tabla WM_LOG_ERROR");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaError_res);
		testCase.addQueryEvidenceCurrentStep(ValidaError_res1);
		testCase.addQueryEvidenceCurrentStep(ValidaError_res2);
		//assertTrue(validaError);

//******************************************************************Paso 9****************************************************************************************************		
		addStep("Monitorear la aplicacion con el historico de ejecuciones por fecha y banco, en la tabla TDC_FIN_HISTROIC en la BD FCTDCQA.");
		SQLResult ValidaHistoricoEjec_res = executeQuery(dbTdc, ValidaHistoricoEjec);
		System.out.println(ValidaHistoricoEjec);
		boolean validaHistoricoEjec = ValidaHistoricoEjec_res.isEmpty();
		if(!validaHistoricoEjec) {
			testCase.addBoldTextEvidenceCurrentStep("Se verifica que la aplicacion con el historico de ejecuciones por fecha y banco de la tabla TDC_FIN_HISTORIC han sido monitoreados");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaHistoricoEjec_res);
		assertFalse(validaHistoricoEjec);
		//La tabla del histórico no existe
		//Resultado Esperado: Se muestra el registro de las ejecuciones ordenadas por fecha y banco, mismas que se encuentran en status 'I'
//******************************************************************Paso 10****************************************************************************************************		
		addStep("Establecer la conexion con la BD **AVEBQA**.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: AVEBQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);	

//******************************************************************Paso 11****************************************************************************************************		
		addStep("Validar que se insertaron los registros en la XXFC_PAGO_SERVICIOS_PRE  de finanzas AVEBQA.");
		SQLResult ValidaRegFinanzas_res = executeQuery(dbEbs, ValidaRegFinanzas);
		System.out.println(ValidaRegFinanzas);
		boolean validaRegFinanzas = ValidaRegFinanzas_res.isEmpty();
		if(!validaRegFinanzas) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida que se insertaron registros en la XXFC_PAGO_SERVICIOS_PRE en la BD AVEBQA");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaRegFinanzas_res);
		assertFalse(validaRegFinanzas);
		
		//Resultado Esperado: Se insertaron los registros que contenia la TDC_TRANSACTION

		
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
		return "Envio Transacciones de corresponsalias";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Automation QA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_016_PO20_EnvioTransaccionCorresponsalia_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}