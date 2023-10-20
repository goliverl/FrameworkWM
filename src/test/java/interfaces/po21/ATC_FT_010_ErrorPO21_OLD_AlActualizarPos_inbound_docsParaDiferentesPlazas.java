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

public class ATC_FT_010_ErrorPO21_OLD_AlActualizarPos_inbound_docsParaDiferentesPlazas extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_ErrorPO21_OLD_AlActualizarPos_inbound_docsParaDiferentesPlazas_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1 y 2
		String validarExistenciaArchivos = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE STATUS = 'I'AND DOC_TYPE = 'TPC' AND TRUNC(RECEIVED_DATE) > TRUNC(SYSDATE -60)"; 
//Paso 3
		String validarRegistros = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME FROM"
				+ " POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID IN ('%s')AND ROWNUM <= 10";

//Paso 4
		String validarDetalle = "SELECT * FROM POSUSER.POS_TPC "
				+ "WHERE PID_ID IN ('%s')";
//Paso 5
		String validarDetalle2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s')";
//Paso 6
		Date fechaEjecucionInicio;
		
//Paso 7 y 8
		String validarStatusE = "SELECT ROWNUM,RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM("
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WM_LOG_RUN "
				+ "WHERE TRUNC(START_DT) = TRUNC(SYSDATE) AND INTERFACE LIKE '%PO21%' ORDER BY START_DT DESC)"
				+ "WHERE ROWNUM <=10";
//Paso 9
		String validarThreads = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN ('%s')";
		
//Paso 10
		String validarError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
				+ "FROM WM_LOG_ERROR WHERE RUN_ID in ('%s')";
//Paso 12
		String validarInfoAVEBQA = "SELECT ID_MPC_GL_TPC,FORMA_PAGO,CR_PLAZA,CR_TIENDA,CREATION_DATE,WM_RUN_ID "
				+ "FROM XXFC.XXFC_MPC_GL_TPC WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE) AND WM_RUN_ID = '%s' "
				+ "AND ROWNUM <= 10";
//Paso 13
		String validarStatus = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE ID IN ('%s') AND DOC_TYPE = 'TPC'";
//Paso 17
		String validarDetalleDETL = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL_TMP " + "WHERE PID_ID " + "IN ('%s')";

/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//***************************************** Paso 1 y 2 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la PR50 en la BD FCWM6QA");
		System.out.println("Paso 1 y 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validarExistenciaArchivos);
		
		SQLResult exe_ExistenciaArchvios = executeQuery(dbFCWM6QA_NUEVA, validarExistenciaArchivos);
		
		boolean ExistenciaArchvios = exe_ExistenciaArchvios.isEmpty();
		String id_INB_DOCS = "";
		
		
			if (!ExistenciaArchvios) {
				id_INB_DOCS = exe_ExistenciaArchvios.getData(0, "ID");
				System.out.println(id_INB_DOCS);
				testCase.addQueryEvidenceCurrentStep(exe_ExistenciaArchvios);
			} 
			
		System.out.println(ExistenciaArchvios);
		assertFalse(ExistenciaArchvios, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar que el registro que existe en la tabla POS_INBOUND_DOCS existe también en la tabla POS_INBOUND_DOC_ALL en la BD FCWM6QA");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarRegistros, id_INB_DOCS);
		System.out.println(validarRegFormat);
				
		SQLResult exe_validarRegistros = executeQuery(dbFCWM6QA_NUEVA, validarRegFormat);
				
		boolean existenRegistros = exe_validarRegistros.isEmpty();
				
			if (!existenRegistros) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros);
				
			} 
					
		System.out.println(existenRegistros);
		assertFalse(existenRegistros, "No existe información pendiente de procesar en la tabla.");
	
//***************************************** Paso 4 ***************************************************************** 		
		/*
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOCS);
		System.out.println(validarDetalleFormat);
				
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
				
		boolean validaDetalle = exe_validarDetalle.isEmpty();
				
		if (!validaDetalle) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
					
			} 
				
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe información pendiente de procesar en la tabla.");	
		*/
		
//***************************************** Paso 5 ***************************************************************** 
		/*
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DETL");
		System.out.println("Paso 5 " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOCS);
		System.out.println(validarDetalle2Format);

		SQLResult exe_validarDetalle2 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle2Format);

		boolean validaDetalle2 = exe_validarDetalle2.isEmpty();

		if (!validaDetalle2) {

			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle2);

		}

		System.out.println(validaDetalle2);
		assertFalse(validaDetalle2, "No existe información pendiente de procesar en la tabla.");*/
		
// ***************************************** Paso 6 *****************************************************************

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

		// assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
//***************************************** Paso 7 y 8 *****************************************************************
		addStep("Validar que se registro la ejecución de la interfaz");
		System.out.println("Paso 7 y 8 " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarStatusE);

		SQLResult exe_validarStatusE = executeQuery(dbFCWMLQA_WMLOG, validarStatusE);

		String status = exe_validarStatusE.getData(0, "STATUS");
		String run_ID = exe_validarStatusE.getData(0, "RUN_ID");
		System.out.println("El status es: " + status);
		System.out.println("El RUN_ID es: " + run_ID);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();

		if (!validaConsultasStatus) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "E"); // Debe de ser E
		
//***************************************** Paso 9 *****************************************************************
		
		addStep("Validar cual fue el thread que fallo en la BD FCWMLQA");
		System.out.println("Paso 9 " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarThreadsFormat = String.format(validarThreads,run_ID);
		System.out.println(validarThreadsFormat);

		SQLResult exe_validarThread = executeQuery(dbFCWMLQA_WMLOG, validarThreadsFormat);
		
		String thread_ID = exe_validarThread.getData(0,"THREAD_ID");
		System.out.println("El THREAD_ID es: " + thread_ID);

		boolean validaThreadError = exe_validarThread.isEmpty();

		if (!validaThreadError) {

			testCase.addQueryEvidenceCurrentStep(exe_validarThread);
			testCase.addTextEvidenceCurrentStep("Existen errores en el Thread");
		}

		System.out.println(validaThreadError);
		assertFalse(validaThreadError, "No existen errores en el Thread");
		
//***************************************** Paso 10 *****************************************************************
		
		addStep("Validar cual fue el error que se presento ");
		System.out.println("Paso 10 " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarErrorFormat = String.format(validarError, run_ID);
		System.out.println(validarErrorFormat);

		SQLResult exe_validarThreadE = executeQuery(dbFCWMLQA_WMLOG, validarErrorFormat);

		boolean validaError = exe_validarThreadE.isEmpty();

		if (!validaError) {

			testCase.addQueryEvidenceCurrentStep(exe_validarThreadE);
			testCase.addTextEvidenceCurrentStep("Existe el error");
		}

		System.out.println(validaError);
		assertFalse(validaError, "No existen el error");

//***************************************** Paso 11 y 12 *****************************************************************
		addStep("Validar que se inserto la información en la BD AVEBQA");
		System.out.println("Paso 14 "+GlobalVariables.DB_HOST_AVEBQA);
		String validarInfoAVEBQAFormat = String.format(validarInfoAVEBQA,thread_ID);
		System.out.println(validarInfoAVEBQAFormat);
				
		SQLResult exe_InfoAVEBQA = executeQuery(dbAVEBQA, validarInfoAVEBQAFormat);
				
		boolean ArchviosInsertados = exe_InfoAVEBQA.isEmpty();
					
			if (!ArchviosInsertados) {
						
				testCase.addQueryEvidenceCurrentStep(exe_InfoAVEBQA);
			} 
					
		System.out.println(ArchviosInsertados);
		assertFalse(ArchviosInsertados, "No existen archivos a procesar.");
		
//***************************************** Paso 13 *****************************************************************	
		addStep("Validar que actualizo en la POS_INB_INBOUND_DOCS el estatus del archivo procesado de I a E");
		System.out.println("Paso 13: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarStatusFormat = String.format(validarStatus, id_INB_DOCS);
		System.out.println(validarStatusFormat);

		SQLResult exe_validarStatusEFormat = executeQuery(dbFCWM6QA_NUEVA, validarStatusFormat);

		String statusE = exe_validarStatusEFormat.getData(0, "STATUS");
		System.out.println("El status es: " + statusE);

		boolean validaConsultasStatusE = exe_validarStatusEFormat.isEmpty();

		if (!validaConsultasStatusE) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusEFormat);
		}

		System.out.println(validaConsultasStatusE);
		assertEquals(statusE, "E"); // Debe de ser E
		
//***************************************** Paso 14 *****************************************************************	
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DETL_TMP");
		System.out.println("Paso 14 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalleDETL, id_INB_DOCS);
		System.out.println(validarDetalle2Format);
						
		SQLResult exe_validarDetalle2 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle2Format);
						
		boolean validaDetalle2 = exe_validarDetalle2.isEmpty();
						
		if (!validaDetalle2) {
							
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle2);
							
			} 
						
		System.out.println(validaDetalle2);
		assertFalse(validaDetalle2, "No existe información pendiente de procesar en la tabla.");
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
		return "Validar que al presentarse un error al actualizar la tabla pos_inbound_docs se registre el error y la interfaz PO21_OLD continue procesando el resto de los archivos de diferente plazas";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_010_ErrorPO21_OLD_AlActualizarPos_inbound_docsParaDiferentesPlazas_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
