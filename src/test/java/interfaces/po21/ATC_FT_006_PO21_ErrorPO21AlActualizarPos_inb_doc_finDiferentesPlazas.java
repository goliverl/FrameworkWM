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

public class ATC_FT_006_PO21_ErrorPO21AlActualizarPos_inb_doc_finDiferentesPlazas extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PO21_ErrorPO21AlActualizarPos_inb_doc_finDiferentesPlazas_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
	
/**
* Variables ******************************************************************************************
* 
*/
//Paso 2
		String validarExistenciaArchivos = "SELECT ROWNUM,ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE FROM("
				+ "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' "
				+ "ORDER BY INSERTED_DATE DESC) "
				+ "WHERE ROWNUM <= 10"; 
//Paso 3
		String validarRegistros = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INB_DOC_NAR  "
				+ "WHERE ID in ('%s')";
		
//Paso 4
		String validarRegistros2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME FROM"
				+ " POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID IN ('%s')AND ROWNUM <= 10";
		
//Paso 5
		String validarDetalle = "SELECT * FROM POSUSER.POS_TPC "
				+ "WHERE PID_ID IN ('%s')";
//Paso 6
		String validarDetalle2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s')"; 
//Paso 7
		Date fechaEjecucionInicio;
		
//Paso 11
		String validarStatusE = "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WM_LOG_RUN "
				+ "WHERE TRUNC(start_dt) = TRUNC(sysdate) "
				+ "AND INTERFACE LIKE 'PO21'";
//Paso 12
		String validarThreads = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN ('%s') AND STATUS = 'S'";
//Paso 13
		String validarError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE,DESCRIPTION "
				+ "FROM WM_LOG_ERROR WHERE RUN_ID in ('%s')";
//Paso 15
		String validarInfoAVEBQA = "SELECT ID_MPC_GL_TPC,FORMA_PAGO,CR_PLAZA,CR_TIENDA,CREATION_DATE,WM_RUN_ID "
				+ "FROM XXFC.XXFC_MPC_GL_TPC WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE) AND WM_RUN_ID = '%s' "
				+ "AND ROWNUM <= 10";
//Paso 16
		String validarExistenciaArchivos2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE ID = '%s' AND DOC_TYPE = 'TPC'";
//Paso 17 
		String validarDetalle3 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s')";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//***************************************** Paso 1 y 2 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la FEMSA_ENRED_IN");
		System.out.println("Paso 1 y 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
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
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN existen también en la tabla POS_INB_DOC_NAR");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarRegistros, id_INB_DOC_FIN);
		System.out.println(validarRegFormat);
				
		SQLResult exe_validarRegistros = executeQuery(dbFCWM6QA_NUEVA, validarRegFormat);

		boolean existenRegistros = exe_validarRegistros.isEmpty();

		if (!existenRegistros) {

			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros);

		}

		System.out.println(existenRegistros);
		assertFalse(existenRegistros, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 4 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN y POS_INBOUND_DOCS existen también en la tabla POS_INBOUND_DOC_ALL");
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
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOC_FIN);
		System.out.println(validarDetalleFormat);
				
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
				
		boolean validaDetalle = exe_validarDetalle.isEmpty();
				
		if (!validaDetalle) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
					
			} 
				
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 6 ***************************************************************** 
	
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DET");
		System.out.println("Paso 6 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOC_FIN);
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
		System.out.println("Paso 6,7,8 y 9 ");

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
		
//***************************************** Paso 10 y 11 *****************************************************************
		addStep("Validar que se registro la ejecución de la interfaz");
		System.out.println("Paso 10 y 11 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarStatusE);
				
		SQLResult exe_validarStatusE = executeQuery(dbFCWMLQA_WMLOG, validarStatusE);
				
		String status = exe_validarStatusE.getData(0, "STATUS");
		System.out.println("El status es: " + status);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();
		String run_ID = "";
		System.out.println("El run ID es: " + run_ID);

		if (!validaConsultasStatus) {
			run_ID = exe_validarStatusE.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "E"); //Debe de ser E
		
//***************************************** Paso 12 *****************************************************************
		
		addStep("Validar cual fue el thread que fallo");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarThreadsFormat = String.format(validarThreads,run_ID);
		System.out.println(validarThreadsFormat);
				
		SQLResult exe_validarThread = executeQuery(dbFCWMLQA_WMLOG, validarThreadsFormat);
				
		boolean validaThreadError = exe_validarThread.isEmpty();
		String threadID = "";
				
		if (!validaThreadError) {
			threadID = exe_validarThread.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(exe_validarThread);
			testCase.addTextEvidenceCurrentStep("Existen errores en el Thread");
			} 
				
		System.out.println(validaThreadError);
		assertFalse(validaThreadError, "No existen errores en el Thread");
		
//***************************************** Paso 13 *****************************************************************
		
		addStep("Validar el registro del detalle del error");
		System.out.println("Paso 13 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarErrorFormat = String.format(validarError,run_ID);
		System.out.println(validarErrorFormat);
				
		SQLResult exe_validarError = executeQuery(dbFCWMLQA_WMLOG, validarErrorFormat);
				
		boolean validaError = exe_validarError.isEmpty();
		String errorDesc = "";
				
		if (!validaDetalle2) {
			errorDesc = exe_validarError.getData(0, "DESCRIPTION");
			System.out.println("Error Description: "+errorDesc);
			
			testCase.addQueryEvidenceCurrentStep(exe_validarError);
			testCase.addTextEvidenceCurrentStep("Existe el error");
			} 
				
		System.out.println(validaError);
		assertFalse(validaError, "No existen el error");
		
//***************************************** Paso 15 *****************************************************************
		
		addStep("Validar que se inserto la información en la BD AVEBQA");
		System.out.println("Paso 15 "+GlobalVariables.DB_HOST_AVEBQA);
		String validarInfoAVEBQAFormat = String.format(validarInfoAVEBQA,threadID);
		System.out.println(validarInfoAVEBQAFormat);
				
		SQLResult exe_InfoAVEBQA = executeQuery(dbAVEBQA, validarInfoAVEBQAFormat);
				
		boolean ArchviosInsertados = exe_InfoAVEBQA.isEmpty();
					
			if (!ArchviosInsertados) {
						
				testCase.addQueryEvidenceCurrentStep(exe_InfoAVEBQA);
			} 
					
		System.out.println(ArchviosInsertados);
		assertFalse(ArchviosInsertados, "No existen archivos a procesar.");
		
//***************************************** Paso 16 *****************************************************************
		
		addStep("Validar que actualizo en la POS_INB_DOC_FIN el estatus del archivo procesado de I a E");
		System.out.println("Paso 16 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
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
		assertEquals(statusE, "E"); //Debe de ser E
		
//***************************************** Paso 17 *****************************************************************	
		
		addStep("Validar que se insertó el detalle del archivo en la POS_TPC_DETL_TMP");
		System.out.println("Paso 17 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
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
		return "Validar que al presentarse un error al actualizar la tabla pos_inb_doc_fin se registre "
				+ "el error para la plaza y la interfaz PO21 continue procesando el resto de los archivos "
				+ "de diferente plazas";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_PO21_ErrorPO21AlActualizarPos_inb_doc_finDiferentesPlazas_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
