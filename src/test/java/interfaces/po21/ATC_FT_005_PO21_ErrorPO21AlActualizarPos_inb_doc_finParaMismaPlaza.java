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

public class ATC_FT_005_PO21_ErrorPO21AlActualizarPos_inb_doc_finParaMismaPlaza extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PO21_ErrorPO21AlActualizarPos_inb_doc_finParaMismaPlaza_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
	
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
//Paso 3
		String validarRegistros = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME FROM"
				+ " POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID IN ('%s')AND ROWNUM <= 10";
//Paso 2
		String validarRegistros2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INB_DOC_NAR  "
				+ "WHERE ID in ('%s')";
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
//Paso 7
		String validarStatusE = "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WM_LOG_RUN "
				+ "WHERE TRUNC(start_dt) = TRUNC(sysdate) "
				+ "AND INTERFACE LIKE 'PO21'";
//Paso 9
		String validarThreads = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN ('%s')";
//Paso 8
		String validarError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
				+ "FROM WM_LOG_ERROR WHERE RUN_ID in ('%s')";
//Paso 10
		String validarInfoAVEBQA = "SELECT ID_MPC_GL_TPC,FORMA_PAGO,CR_PLAZA,CR_TIENDA,CREATION_DATE,WM_RUN_ID \r\n"
				+ "FROM XXFC.XXFC_MPC_GL_TPC WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE)";
//Paso 11
		String validarExistenciaArchivos2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE ID = '%s' AND DOC_TYPE = 'TPC'";
//Paso 12 
		String validarDetalle3 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s')";
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
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN existen también en la tabla POS_INB_DOC_NAR");
		System.out.println("Paso 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarReg2Format = String.format(validarRegistros2, id_INB_DOC_FIN);
		System.out.println(validarReg2Format);
				
		SQLResult exe_validarRegistros2 = executeQuery(dbFCWM6QA_NUEVA, validarReg2Format);

		boolean existenRegistros2 = exe_validarRegistros2.isEmpty();

		if (!existenRegistros2) {

			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros2);

		}

		System.out.println(existenRegistros2);
		assertFalse(existenRegistros2, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN y POS_INBOUND_DOCS existen también en la tabla POS_INBOUND_DOC_ALL");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarRegistros, id_INB_DOC_FIN); //Format 1
		System.out.println(validarRegFormat);
				
		SQLResult exe_validarRegistros = executeQuery(dbFCWM6QA_NUEVA, validarRegFormat);
				
		boolean existenRegistros = exe_validarRegistros.isEmpty();
				
			if (!existenRegistros) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros);
				
			} 
					
		System.out.println(existenRegistros);
		assertFalse(existenRegistros, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 4 ***************************************************************** 		
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOC_FIN);
		System.out.println(validarDetalleFormat);
				
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
				
		boolean validaDetalle = exe_validarDetalle.isEmpty();
				
		if (!validaDetalle) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
					
			} 
				
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 5 ***************************************************************** 
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DET");
		System.out.println("Paso 5 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOC_FIN);
		System.out.println(validarDetalle2Format);
				
		SQLResult exe_validarDetalle2 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle2Format);
				
		boolean validaDetalle2 = exe_validarDetalle2.isEmpty();
				
		if (!validaDetalle2) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle2);
					
			} 
				
		System.out.println(validaDetalle2);
		assertFalse(validaDetalle2, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 6 *****************************************************************
		
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
		
		
		
//***************************************** Paso 7 *****************************************************************
		addStep("Validar que se registro la ejecución de la interfaz");
		System.out.println("Paso 7 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarStatusE);
		
		SQLResult exe_validarStatusE = executeQuery(dbFCWMLQA_WMLOG, validarStatusE);
		
		String status = exe_validarStatusE.getData(0, "STATUS");
		System.out.println("El status es: " + status);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();

		if (!validaConsultasStatus) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "E"); //Debe de ser E*/
		
//***************************************** Paso 8 *****************************************************************
		
		addStep("Validar cual fue el error que se presento");
		System.out.println("Paso 8 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarErrorFormat = String.format(validarError,id_INB_DOC_FIN);
		System.out.println(validarErrorFormat);
				
		SQLResult exe_validarError = executeQuery(dbFCWMLQA_WMLOG, validarErrorFormat);
				
		boolean validaError = exe_validarError.isEmpty();
				
		if (!validaError) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarError);
			testCase.addTextEvidenceCurrentStep("No existe el error");
			} 
				
		System.out.println(validaError);
		assertTrue(validaError, "Error existe");

//***************************************** Paso 9 *****************************************************************
		
		addStep("Validar cual fue el thread que fallo");
		System.out.println("Paso 9 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarThreadsFormat = String.format(validarThreads,id_INB_DOC_FIN);
		System.out.println(validarThreadsFormat);
		
		SQLResult exe_validarThread = executeQuery(dbFCWMLQA_WMLOG, validarThreadsFormat);
		
		boolean validaThreadError = exe_validarThread.isEmpty();
		
		if (!validaDetalle2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarThread);
			testCase.addTextEvidenceCurrentStep("Existen errores en el Thread");
			} 
		
		System.out.println(validaThreadError);
		assertFalse(validaThreadError, "No existen errores en el Thread");
		
//***************************************** Paso 10 *****************************************************************
		
		addStep("Validar que se inserto la información en la BD AVEBQA");
		System.out.println("Paso 10 "+GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(validarInfoAVEBQA);
		
		SQLResult exe_ArchviosPermanecen = executeQuery(dbAVEBQA, validarInfoAVEBQA);
		
		boolean ArchviosPermanecen = exe_ArchviosPermanecen.isEmpty();
			
			if (!ArchviosPermanecen) {
				
				testCase.addQueryEvidenceCurrentStep(exe_ArchviosPermanecen);
			} 
			
		System.out.println(ArchviosPermanecen);
		assertFalse(ArchviosPermanecen, "No existen archivos a procesar.");
		
//***************************************** Paso 11 *****************************************************************
		addStep("Validar que actualizo en la POS_INB_DOC_FIN el estatus del archivo");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarExistenciaArchivos2Format = String.format(validarExistenciaArchivos2,id_INB_DOC_FIN);
		System.out.println(validarExistenciaArchivos2Format);
		
		SQLResult exe_ExistenciaArchvios2 = executeQuery(dbFCWM6QA_NUEVA, validarExistenciaArchivos2Format);
		
		String statusE = exe_ExistenciaArchvios2.getData(0, "STATUS");
		System.out.println("El status es: " + statusE);
		
		boolean ExistenciaArchvios2 = exe_ExistenciaArchvios.isEmpty();
		
			if (!ExistenciaArchvios2) {
				testCase.addQueryEvidenceCurrentStep(exe_ExistenciaArchvios2);
			} 
			
		System.out.println(ExistenciaArchvios2);
		assertEquals(statusE, "E"); //Debe de ser E*/
		
//***************************************** Paso 12 *****************************************************************	
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle3Format = String.format(validarDetalle3, id_INB_DOC_FIN);
		System.out.println(validarDetalle3Format);
		
		SQLResult exe_validarDetalle3 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle3Format);
		
		boolean validaDetalle3 = exe_validarDetalle3.isEmpty();
		
		if (!validaDetalle3) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle3);
			
			} 
		
		System.out.println(validaDetalle3);
		assertFalse(validaDetalle3, "No existe información pendiente de procesar en la tabla.");
		
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
		return "Validar que al presentarse un error al actualizar la tabla pos_inb_doc_fin NO se registre el error y la interfaz PO21 continue procesando el resto de los archivos de una misma plaza";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PO21_ErrorPO21AlActualizarPos_inb_doc_finParaMismaPlaza_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
