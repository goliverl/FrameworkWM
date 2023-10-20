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

public class ATC_FT_009_PO21_ErrorPO21_OLDAlActualizarPos_inbound_docsUnaMismaPlaza extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_PO21_ErrorPO21_OLDAlActualizarPos_inbound_docsUnaMismaPlaza_test(HashMap<String, String> data) throws Exception {
		
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
		String validarExistenciaArchivos = "SELECT ID,PE_ID,STATUS,DOC_TYPE,BACKUP_STATUS,RECEIVED_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' AND ROWNUM <= 10 "
				+ "ORDER BY RECEIVED_DATE"; 
//Paso 3
		String validarRegistros = "SELECT ID,PE_ID,STATUS,DOC_TYPE,BACKUP_STATUS,RECEIVED_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL WHERE ID IN ('%s')";
		
//Paso 4
		String validarRegistros2 = "SELECT * FROM POSUSER.POS_TPC WHERE PID_ID IN ('%s')";
		
//Paso 5
		String validarDetalle = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,PV_DATE "
				+ "FROM POSUSER.POS_TPC_DETL WHERE PID_ID IN ('%s')";
//Paso 7
		Date fechaEjecucionInicio;
		
// Paso 10
		String validarStatusS = "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS FROM("
				+ "  SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS "
				+ "  FROM WMLOG.WM_LOG_RUN WHERE INTERFACE LIKE '%PO21_OLD%' " + "  ORDER BY START_DT DESC) "
				+ "WHERE ROWNUM <=10";
// Paso 11
		String validarThreads = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID IN ('%s');";
//Paso 12
		String validarErrores = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE,DESCRIPTION "
				+ "FROM WMLOG.WM_LOG_ERROR "
				+ "WHERE RUN_ID = '%s'";
//Paso 14
		String validarInfoAVEBQA = "SELECT ID_MPC_GL_TPC,FORMA_PAGO,CR_PLAZA,CR_TIENDA,CREATION_DATE,WM_RUN_ID "
				+ "FROM XXFC.XXFC_MPC_GL_TPC WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE) AND WM_RUN_ID = '%s' "
				+ "AND ROWNUM <= 10";
//Paso 15
		String validarStatusE = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE ID IN ('%s') AND DOC_TYPE = 'TPC'";
//Paso 16
		String validarStatusE2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL WHERE ID IN ('%s')";
//Paso 17
		String validarDetalle2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s')";

/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//***************************************** Paso 1 y 2 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la la PR50");
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
		
		addStep("Validar que el registro que existe en la tabla POS_INBOUND_DOCS existe también en la tabla POS_INBOUND_DOC_ALL");
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
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA'");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarReg2Format = String.format(validarRegistros2, id_INB_DOCS);
		System.out.println(validarReg2Format);
				
		SQLResult exe_validarRegistros2 = executeQuery(dbFCWM6QA_NUEVA, validarReg2Format);
				
		boolean existenRegistros2 = exe_validarRegistros2.isEmpty();
				
			if (!existenRegistros2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros2);
				
			} 
					
		System.out.println(existenRegistros2);
		assertFalse(existenRegistros2, "No existe información pendiente de procesar en la tabla.");
		//NO HAY INFOR EN LA TABLA CON NINGUNO DE LOS IDS
		
//***************************************** Paso 5 ***************************************************************** 		
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA': ");
		System.out.println("Paso 5 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOCS);
		System.out.println(validarDetalleFormat);
				
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
				
		boolean validaDetalle = exe_validarDetalle.isEmpty();
				
		if (!validaDetalle) {
					
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
					
			} 
				
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe información pendiente de procesar en la tabla.");
		//NO HAY INFOR EN LA TABLA CON NINGUNO DE LOS IDS
		

//***************************************** Paso 6, 7 y 8 *****************************************************************
		
		addStep("Ejecutar PO21 desde Control M");
		System.out.println("Paso 6,7,8 ");

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
		
//***************************************** Paso 10  *****************************************************************
		
		addStep("Validar que se registro la ejecución de la interfaz");
		System.out.println("Paso 10: "+GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(validarStatusS);
		
		SQLResult exe_validarStatusS = executeQuery(dbFCWMLQA, validarStatusS);
		
		String statusS = exe_validarStatusS.getData(0, "STATUS");
		String runID = exe_validarStatusS.getData(0, "RUN_ID");
		System.out.println("El status es: " + statusS);
		System.out.println("El RUN_ID es: " + runID);

		boolean validaConsultasStatusS = exe_validarStatusS.isEmpty();

		if (!validaConsultasStatusS) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusS);
		}

		System.out.println(validaConsultasStatusS);
		assertEquals(statusS, "S");
		
//***************************************** Paso 11 *****************************************************************
		
		addStep("Consultar el thread generado del procesamiento de los archivos de la misma plaza");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_FCWMLQA);
		String validarThreadsFormat = String.format(validarThreads, runID);
		System.out.println(validarThreadsFormat);
				
		SQLResult validarThreadsExe = executeQuery(dbFCWMLQA, validarThreadsFormat);
		
		String statusS2 = validarThreadsExe.getData(0, "STATUS");
		System.out.println("El status es: " + statusS2);
				
		boolean validaThreads = validarThreadsExe.isEmpty();
				
		if (!validaThreads) {
					
			testCase.addQueryEvidenceCurrentStep(validarThreadsExe);
					
			} 
				
		System.out.println(validaThreads);
		assertEquals(statusS, "S");
		
//***************************************** Paso 12 *****************************************************************

		addStep("Validar que no se hayan registardo erroes en la interface PO21_OLD ");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMLQA);
		String validarErroresFormat = String.format(validarErrores, runID);
		System.out.println(validarErroresFormat);
		
		SQLResult validarErroresExe = executeQuery(dbFCWMLQA, validarErroresFormat);
		
		boolean validaErrores = validarErroresExe.isEmpty();
		
		if (!validaErrores) {
			
			testCase.addQueryEvidenceCurrentStep(validarErroresExe);
				
		} 
		System.out.println(validaErrores);
		assertTrue(validaErrores,"Se encontraron errores");
		
//***************************************** Paso 14 *****************************************************************
		addStep("Validar que se inserto la información en la BD AVEBQA");
		System.out.println("Paso 14 "+GlobalVariables.DB_HOST_AVEBQA);
		String validarInfoAVEBQAFormat = String.format(validarInfoAVEBQA,id_INB_DOCS);
		System.out.println(validarInfoAVEBQAFormat);
				
		SQLResult exe_InfoAVEBQA = executeQuery(dbAVEBQA, validarInfoAVEBQAFormat);
				
		boolean ArchviosInsertados = exe_InfoAVEBQA.isEmpty();
					
			if (!ArchviosInsertados) {
						
				testCase.addQueryEvidenceCurrentStep(exe_InfoAVEBQA);
			} 
					
		System.out.println(ArchviosInsertados);
		assertFalse(ArchviosInsertados, "No existen archivos a procesar.");
		
		
//***************************************** Paso 15 *****************************************************************	
		addStep("Validar que actualizo en la POS_INB_INBOUND_DOCS el estatus del archivo procesado de I a E");
		System.out.println("Paso 15: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarStatusEFormat = String.format(validarStatusE,id_INB_DOCS);
		System.out.println(validarStatusEFormat);
				
		SQLResult exe_validarStatusE = executeQuery(dbFCWM6QA_NUEVA, validarStatusEFormat);
				
		String status = exe_validarStatusE.getData(0, "STATUS");
		System.out.println("El status es: " + status);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();

		if (!validaConsultasStatus) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "E"); //Debe de ser E
		
		
//***************************************** Paso 16 *****************************************************************	
		addStep("Validar que se actualizaron los registros en la tabla POS_INBOUND_DOC_ALL:");
		System.out.println("Paso 16 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarStatusE2Format = String.format(validarStatusE2,id_INB_DOCS);
		System.out.println(validarStatusE2Format);
				
		SQLResult exe_validarStatusE2 = executeQuery(dbFCWM6QA_NUEVA, validarStatusE2Format);
		
		String statusE2 = exe_validarStatusE2.getData(0, "STATUS");
		System.out.println("El status es: " + statusE2);
				
		boolean validaStatusE2 = exe_validarStatusE2.isEmpty();
				
		if (!validaStatusE2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE2);
			} 
				
		System.out.println(validaStatusE2);
		assertEquals(statusE2, "E"); //Debe de ser E
		
//***************************************** Paso 17 ***************************************************************** 
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DETL");
		System.out.println("Paso 17 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOCS);
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
		return "Validar que al presentarse un error al actualizar la tabla PO21_OLD pos_inbound_docs NO se registre y la interfaz continue procesando el resto de los archivos de una misma plaza";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_009_PO21_ErrorPO21_OLDAlActualizarPos_inbound_docsUnaMismaPlaza_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
