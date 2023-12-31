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

public class ATC_FT_004_PO21_ErrorconexiOnDBDestino_PO21yPO21_OLD extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO21_ErrorconexiOnDBDestino_PO21yPO21_OLD_test(HashMap<String, String> data) throws Exception {
		
/* Utiler�as *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1 y 2
		String validarExistenciaArchivos = "SELECT ROWNUM,ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE FROM("
				+ "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' "
				+ "ORDER BY INSERTED_DATE DESC) "
				+ "WHERE ROWNUM <= 10"; 
//Paso 3
		String validarExistenciaArchvios2 = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE STATUS = 'I' "
				+ "AND DOC_TYPE = 'TPC'";
//Paso 4
		String validarRegistros = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME FROM"
				+ " POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID IN ('%s','%s')AND ROWNUM <= 10";
//Paso 5
		String validarRegistros2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INB_DOC_NAR  "
				+ "WHERE ID in ('%s')";
//Paso 6
		String validarDetalle = "SELECT * FROM POSUSER.POS_TPC "
				+ "WHERE PID_ID IN ('%s','%s')";
//Paso 7 
		String validarDetalle2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,NUM_CAJA "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID "
				+ "IN ('%s','%s')";
//Paso 8
		Date fechaEjecucionInicio;
//Paso 9 Y 10
		String validarStatusE = "SELECT ROWNUM,RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM("
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WM_LOG_RUN "
				+ "WHERE TRUNC(START_DT) = TRUNC(SYSDATE) AND INTERFACE LIKE '%PO21%' ORDER BY START_DT DESC)"
				+ "WHERE ROWNUM <=10";
//Paso 11
		String validarThreads = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN ('%s','%s')";
//Paso 12
		String validarError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
				+ "FROM WM_LOG_ERROR WHERE RUN_ID in ('%s','%s')";
//Paso 13
		String validarArchivosPermanecen = "SELECT ROWNUM,ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE FROM("
				+ "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' "
				+ "ORDER BY INSERTED_DATE DESC) "
				+ "WHERE ROWNUM <= 10"; 
//Paso 14
		String validarArchivosPermanecen2 = "SELECT ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE STATUS='I' AND DOC_TYPE='TPC' AND ROWNUM <=10";
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
		assertFalse(ExistenciaArchvios, "No existe informaci�n pendiente de procesar en la tabla.");
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar que existen archivos pendientes de procesar y que fueron insertados por la PR50");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
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
		assertFalse(ExistenciaArchvios2, "No existe informaci�n pendiente de procesar en la tabla."); //TABLA VACIA, NO C QUE PROCDE :v
		
//***************************************** Paso 4 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN y POS_INBOUND_DOCS existen tambi�n en la tabla POS_INBOUND_DOC_ALL");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarRegistros, id_INB_DOC_FIN,id_INB_DOCS); //Format 1
		System.out.println(validarRegFormat);
		
		SQLResult exe_validarRegistros = executeQuery(dbFCWM6QA_NUEVA, validarRegFormat);
		
		boolean existenRegistros = exe_validarRegistros.isEmpty();
		
			if (!existenRegistros) {
	
			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros);
		
			} 
			
		System.out.println(existenRegistros);
		assertFalse(existenRegistros, "No existe informaci�n pendiente de procesar en la tabla.");
		
//***************************************** Paso 5 ***************************************************************** 
		
		addStep("Validar que los registros que existe en la tabla POS_INB_DOC_FIN existen tambi�n en la tabla POS_INB_DOC_NAR");
		System.out.println("Paso 5 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarReg2Format = String.format(validarRegistros2, id_INB_DOC_FIN);
		System.out.println(validarReg2Format);
		
		SQLResult exe_validarRegistros2 = executeQuery(dbFCWM6QA_NUEVA, validarReg2Format);

		boolean existenRegistros2 = exe_validarRegistros2.isEmpty();

		if (!existenRegistros2) {

			testCase.addQueryEvidenceCurrentStep(exe_validarRegistros2);

		}

		System.out.println(existenRegistros2);
		assertFalse(existenRegistros2, "No existe informaci�n pendiente de procesar en la tabla.");
		
//***************************************** Paso 6 ***************************************************************** 		
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC");
		System.out.println("Paso 6 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalleFormat = String.format(validarDetalle, id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarDetalleFormat);
		
		SQLResult exe_validarDetalle = executeQuery(dbFCWM6QA_NUEVA, validarDetalleFormat);
		
		boolean validaDetalle = exe_validarDetalle.isEmpty();
		
		if (!validaDetalle) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle);
			
			} 
		
		System.out.println(validaDetalle);
		assertFalse(validaDetalle, "No existe informaci�n pendiente de procesar en la tabla.");
		
//***************************************** Paso 7 ***************************************************************** 
		
		addStep("Validar que los archivos contengan detalle en la tabla POS_TPC_DETL");
		System.out.println("Paso 7 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDetalle2Format = String.format(validarDetalle2, id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarDetalle2Format);
		
		SQLResult exe_validarDetalle2 = executeQuery(dbFCWM6QA_NUEVA, validarDetalle2Format);
		
		boolean validaDetalle2 = exe_validarDetalle2.isEmpty();
		
		if (!validaDetalle2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDetalle2);
			
			} 
		
		System.out.println(validaDetalle2);
		assertFalse(validaDetalle2, "No existe informaci�n pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 8 *****************************************************************
		
		addStep("Ejecutar PO21 desde Control M");

		fechaEjecucionInicio = new Date();

		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecuci�n Job: " + data.get("cm_jobs"));
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

		// Abrir la herramienta de control M para validar que la ejecuci�n del job haya
		// sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		//assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		
		
//***************************************** Paso 9 Y 10 *****************************************************************
		addStep("Validar que se registro la ejecuci�n de la interfaz");
		System.out.println("Paso 9 y 10 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarStatusE);
		
		SQLResult exe_validarStatusE = executeQuery(dbFCWMLQA_WMLOG, validarStatusE);
		
		String status = exe_validarStatusE.getData(0, "STATUS");
		System.out.println("El status es: " + status);

		boolean validaConsultasStatus = exe_validarStatusE.isEmpty();

		if (!validaConsultasStatus) {
			testCase.addQueryEvidenceCurrentStep(exe_validarStatusE);
		}

		System.out.println(validaConsultasStatus);
		assertEquals(status, "E"); //Debe de ser E

//***************************************** Paso 11 *****************************************************************
		
		addStep("Ejecutar la consulta de la BD 'FCWMLQA_ WMLOG' en las tabla WMLOG.WM_LOG_THREAD para validar si se registraron Thread de la ejecuci�n");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarThreadsFormat = String.format(validarThreads,id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarThreadsFormat);
		
		SQLResult exe_validarThread = executeQuery(dbFCWMLQA_WMLOG, validarThreadsFormat);
		
		boolean validaThreadError = exe_validarThread.isEmpty();
		
		if (!validaThreadError) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarThread);
			testCase.addTextEvidenceCurrentStep("Existen errores en el Thread");
			} 
		
		System.out.println(validaThreadError);
		assertFalse(validaThreadError, "No existen errores en el Thread");
		
//***************************************** Paso 12 *****************************************************************
		
		addStep("Ejecutar la consulta de la BD 'FCWMLQA_ WMLOG' en las tabla  WM_LOG_ERROR para Validar el registro del detalle del error ");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String validarErrorFormat = String.format(validarError,id_INB_DOC_FIN,id_INB_DOCS);
		System.out.println(validarErrorFormat);
		
		SQLResult exe_validarThreadE = executeQuery(dbFCWMLQA_WMLOG, validarErrorFormat);
		
		boolean validaError = exe_validarThreadE.isEmpty();
		
		if (!validaError) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarThreadE);
			testCase.addTextEvidenceCurrentStep("Existe el error");
			} 
		
		System.out.println(validaError);
		assertFalse(validaError, "No existen el error");
		
//***************************************** Paso 13 *****************************************************************
		addStep("Validar que siguen los archivos  con el mismo estatus pendientes de procesar y que fueron insertados por la FEMSA_ERRED_IN");
		System.out.println("Paso 13 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarArchivosPermanecen);
		
		SQLResult exe_ArchviosPermanecen = executeQuery(dbFCWM6QA_NUEVA, validarArchivosPermanecen);
		
		boolean ArchviosPermanecen = exe_ArchviosPermanecen.isEmpty();
			
			if (!ArchviosPermanecen) {
				
				testCase.addQueryEvidenceCurrentStep(exe_ArchviosPermanecen);
			} 
			
		System.out.println(ArchviosPermanecen);
		assertFalse(ArchviosPermanecen, "No existen archivos a procesar.");
		
//***************************************** Paso 14 *****************************************************************
		addStep("Validar que siguen archivos pendientes de procesar y que fueron insertados por la PR50	");
		System.out.println("Paso 14 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(validarArchivosPermanecen2);
				
		SQLResult exe_ArchviosPermanecen2 = executeQuery(dbFCWM6QA_NUEVA, validarArchivosPermanecen2);
				
		boolean ArchviosPermanecen2 = exe_ArchviosPermanecen2.isEmpty();
					
			if (!ArchviosPermanecen2) {
						
				testCase.addQueryEvidenceCurrentStep(exe_ArchviosPermanecen2);
			} 
					
		System.out.println(ArchviosPermanecen2);
		assertFalse(ArchviosPermanecen2, "No existen archivos a procesar.");
		
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
		return "Validar para la PO21 y PO21_OLD que si se presenta una desconexi�n a BD de destino se registre en la BD de wmlog";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PO21_ErrorconexiOnDBDestino_PO21yPO21_OLD_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
