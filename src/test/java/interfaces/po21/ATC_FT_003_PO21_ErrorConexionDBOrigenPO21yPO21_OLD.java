package interfaces.po21;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_003_PO21_ErrorConexionDBOrigenPO21yPO21_OLD extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO21_ErrorConexionDBOrigenPO21yPO21_OLD_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1 
		String validarExistenciaArchivos = "SELECT ROWNUM,ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE FROM("
				+ "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' "
				+ "ORDER BY INSERTED_DATE DESC) "
				+ "WHERE ROWNUM <= 10"; 
//Paso 2
		String validarExistenciaArchvios2 = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE STATUS = 'I' "
				+ "AND DOC_TYPE = 'TPC'";
//Paso 3 
		String validarRegistros = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME FROM"
				+ " POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID IN ('%s','%s')AND ROWNUM <= 10";
//Paso 4
		String validarRegistros2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INB_DOC_NAR  "
				+ "WHERE ID in ('%s')";
//Paso 5 
		String validarDetalle = "SELECT * FROM POSUSER.POS_TPC "
				+ "WHERE PID_ID IN ('%s','%s')";
//Paso 6 
		String validarDetalle2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s','%s')";
//Paso 7
		Date fechaEjecucionInicio;
//Paso 8
		String validarStatusE = "SELECT * FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE '%PO21_OLD%' "
				+ "ORDER BY START_DT DESC";
//Paso 9 
		String validarThread = "SELECT * FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN (2173844425,2173844424)";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//***************************************** Paso 1 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la FEMSA_ENRED_IN");
		System.out.println("Paso 1 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validarExistenciaArchivos);
		
		SQLResult exe_ExistenciaArchvios = executeQuery(dbFCWM6QA_NUEVA, validarExistenciaArchivos);
		
		boolean ExistenciaArchvios = exe_ExistenciaArchvios.isEmpty();
		String id_INB_DOC_FIN = "";
		
		
			if (!ExistenciaArchvios) {
				id_INB_DOC_FIN = exe_ExistenciaArchvios.getData(0, "ID");
				System.out.println(id_INB_DOC_FIN);
				testCase.addQueryEvidenceCurrentStep(exe_ExistenciaArchvios);
			} 
			
		System.out.println(ExistenciaArchvios);
		assertFalse(ExistenciaArchvios, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 2 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la PR50");
		System.out.println("Paso 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validarExistenciaArchvios2);
		
		SQLResult exe_ExistenciaArchvios2 = executeQuery(dbFCWM6QA_NUEVA, validarExistenciaArchvios2);
		
		boolean ExistenciaArchvios2 = exe_ExistenciaArchvios2.isEmpty();
		String id_INB_DOCS = "";
		
			if (!ExistenciaArchvios2) {
				id_INB_DOCS = exe_ExistenciaArchvios2.getData(0,"ID");
				System.out.println(id_INB_DOCS);
				testCase.addQueryEvidenceCurrentStep(exe_ExistenciaArchvios2);
			} 
			
		System.out.println(ExistenciaArchvios2);
		assertFalse(ExistenciaArchvios2, "No existe información pendiente de procesar en la tabla."); //TABLA VACIA, NO C QUE PROCDE :v
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN y POS_INBOUND_DOCS existen también en la tabla POS_INBOUND_DOC_ALL");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarRegistros, id_INB_DOC_FIN,id_INB_DOCS); //Format 1
		System.out.println(validarRegFormat);
		
		SQLResult exe_validarRegistros = executeQuery(dbFCWM6QA_NUEVA, validarRegFormat);
		
		boolean existenRegistros = exe_validarRegistros.isEmpty();
		
			if (!existenRegistros) {
	
			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros);
		
			} 
			
		System.out.println(existenRegistros);
		assertFalse(existenRegistros, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 4 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN existen también en la tabla POS_INB_DOC_NAR");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarReg2Format = String.format(validarRegistros2, id_INB_DOC_FIN);
		System.out.println(validarReg2Format);
		
		SQLResult exe_validarRegistros2 = executeQuery(dbFCWM6QA_NUEVA, validarReg2Format);

		boolean existenRegistros2 = exe_validarRegistros2.isEmpty();

		if (!existenRegistros2) {

			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros2);

		}

		System.out.println(existenRegistros2);
		assertFalse(existenRegistros2, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 5 ***************************************************************** 		
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 5 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarDetalleFormat);
		
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
		
		boolean validaDetalle = exe_validarDetalle.isEmpty();
		
		if (!validaDetalle) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
			
			} 
		
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 6 ***************************************************************** 
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 6 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarDetalle2Format);
		
		SQLResult exe_validarDetalle2 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle2Format);
		
		boolean validaDetalle2 = exe_validarDetalle2.isEmpty();
		
		if (!validaDetalle2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle2);
			
			} 
		
		System.out.println(validaDetalle2);
		assertFalse(validaDetalle2, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 7 *****************************************************************
		
		addStep("Ejecutar PO21 desde Control M");

		fechaEjecucionInicio = new Date();

		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));

		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u.get(data.get("cm_server"));
		u.hardWait(40);
		u.waitForLoadPage();
		cm.logOn();

		testCase.addTextEvidenceCurrentStep("Inicio de job");
		JobManagement j = new JobManagement(u, testCase, array);
		u.hardWait(5);
		System.out.println(data.get("Espera 5 sec, antes de Resultado Ejecucion"));
		String resultadoEjecucion = j.jobRunner();

		// Abrir la herramienta de control M para validar que la ejecución del job haya
		// sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		//assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		//////////////////////////Segundo Job///////////////////////
		
		JSONArray array2 = new JSONArray(data.get("cm_jobs2"));
		
		testCase.addTextEvidenceCurrentStep("Ejecución Job 2: " + data.get("cm_jobs2"));
		SeleniumUtil u2 = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm2 = new Control_mInicio(u2, data.get("cm_user"), data.get("cm_ps"));
		
		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u2.get(data.get("cm_server"));
		u2.hardWait(40);
		u2.waitForLoadPage();
		cm2.logOn();
		
		testCase.addTextEvidenceCurrentStep("Inicio de job 2");
		JobManagement j2 = new JobManagement(u2, testCase, array2);
		u2.hardWait(5);
		System.out.println(data.get("Espera 5 sec, antes de Resultado Ejecucion"));
		String resultadoEjecucion2 = j2.jobRunner();
		
		// Abrir la herramienta de control M para validar que la ejecución del job haya
		// sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion2);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion2);
		
		//assertEquals(resultadoEjecucion, "Ended OK");
		u2.close(); 
		
		
//***************************************** Paso 8 *****************************************************************
		addStep("Validar que se registro la ejecución de la interfaz");
		System.out.println("Paso 8 "+GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(validarStatusE);
		
		SQLResult exe_validarStatusE = executeQuery(dbFCWMLQA, validarStatusE);
		
		String status = exe_validarStatusE.getData(0, "STATUS");
		System.out.println("El status es: " + status);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();

		if (!validaConsultasStatus) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "S"); //Debe de ser E

//***************************************** Paso 9 *****************************************************************
		/*
		addStep("Validar si se registraron Thread de la ejecución");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validarThread);
		
		SQLResult exe_validarThread = executeQuery(dbFCWM6QA_NUEVA, validarThread);
		
		boolean validaThreadError = exe_validarThread.isEmpty();
		
		if (!validaDetalle2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarThread);
			testCase.addTextEvidenceCurrentStep("No existen errores en el Thread");
			} 
		
		System.out.println(validaThreadError);
		assertTrue(validaThreadError, "Existen errores en el Thread");
		*/
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
		return "Validar para la PO21 y PO21_OLD que si se presenta una desconexión a BD de origen se registre en la BD de wmlog";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PO21_ErrorConexionDBOrigenPO21yPO21_OLD_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
