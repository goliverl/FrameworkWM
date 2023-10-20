package interfaces.po21;

import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_002_PO21_ProcesarArchivoTPCInsertadoPorPR50 extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO21_ProcesarArchivoTPCInsertadoPorPR50_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1 y 2
		String validarDatosPR50 = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE STATUS = 'I' "
				+ "AND DOC_TYPE = 'TPC'"; 
//Paso 3
		String validarDatos2 = "SELECT ID, PV_CR_PLAZA,PV_CR_TIENDA,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID = '%s'";
//Paso 4
		String validarDatos3 = "SELECT * FROM POSUSER.POS_TPC WHERE PID_ID = '%s'";
		
//Paso 5 
		String validarDatos4 = "SELECT PID_ID, FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,PV_DATE "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID = '%s'";
//Paso 6
		Date fechaEjecucionInicio;
//Paso 10
		String validarStatusE = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE ID = '%s' "
				+ "AND DOC_TYPE = 'TPC'";
//Paso 11 
		String validarStatusE2 = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID = '%s'";
//Paso 12 y 13
		String consultaAVEBQA = "SELECT FORMA_PAGO,CR_PLAZA,CR_TIENDA,ACCOUNTING_DATE,STATUS_FLAG,CREATION_DATE "
				+ "FROM XXFC.XXFC_MPC_GL_TPC "
				+ "WHERE CREATION_DATE >= TO_DATE('27-JUL-22', 'DD-MON-RR') "
				+ "AND ROWNUM <= 10";
//Paso 14 y 15
		String consulta1FCWMLQA = "SELECT ROWNUM,RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM("
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE '%PO21_OLD%' ORDER BY START_DT DESC) "
				+ "WHERE ROWNUM <= 10";
//Paso 16 
		String consulta2FCWMLQA = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='2173932183'";
//Paso 17 
		String consulta3FCWMLQA = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='2173932183'";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//***************************************** Paso 1 y 2 ***************************************************************** 
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA' para validar que existen archivos pendientes de procesar y que fueron insertados por la PR50:");
		System.out.println("Paso 1 y 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(validarDatosPR50);
		
		SQLResult exe_validarDatosPR50 = executeQuery(dbFCWM6QA_NUEVA, validarDatosPR50);
				
		boolean validaDatos = exe_validarDatosPR50.isEmpty();
		String id = "";
		
			if (!validaDatos) {
				id = exe_validarDatosPR50.getData(0, "ID");
				testCase.addQueryEvidenceCurrentStep(exe_validarDatosPR50);
			
			} 
		
		System.out.println(validaDatos);

		assertFalse(validaDatos, "No existe información pendiente de procesar en la tabla."); //AQUI DEBE SER asserFalse
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA' para validar que el registro que existe en la tabla POS_INBOUND_DOCS existe también en la tabla POS_INBOUND_DOC_ALL:");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDatos2Format = String.format(validarDatos2, id);
		System.out.println(validarDatos2Format);
		
		SQLResult exe_validarDatos2 = executeQuery(dbFCWM6QA_NUEVA, validarDatos2Format);
		
		boolean validaDatos2 = exe_validarDatos2.isEmpty();
		
		if (!validaDatos2) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDatos2);
			
		} 
			
		System.out.println(validaDatos2);
		assertFalse(validaDatos2, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 4 *****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA': ");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDatos3Format = String.format(validarDatos3, id);
		System.out.println(validarDatos3Format);
		
		SQLResult exe_validarDatos3 = executeQuery(dbFCWM6QA_NUEVA, validarDatos3Format);
		
		boolean validaDatos3 = exe_validarDatos3.isEmpty();
		
			if (!validaDatos3) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDatos3);
			
			} 
		
		System.out.println(validaDatos3);
		assertFalse(validaDatos3, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 5 *****************************************************************
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA': ");
		System.out.println("Paso 5 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarDatos4Format = String.format(validarDatos4, id);
		System.out.println(validarDatos4Format);
		
		SQLResult exe_validarDatos4 = executeQuery(dbFCWM6QA_NUEVA, validarDatos4Format);
		
		boolean validaDatos4 = exe_validarDatos4.isEmpty();
		
		if (!validaDatos4) {
			
			testCase.addQueryEvidenceCurrentStep(exe_validarDatos4);
			
		} 
		
		System.out.println(validaDatos4);
		assertFalse(validaDatos4, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 6,7,8,9 *****************************************************************
		addStep("Ejecución control-M");
		
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
		
		
		//Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		
		assertEquals(resultadoEjecucion, "Ended OK");
		u.close(); 
		
//***************************************** Paso 10 *****************************************************************	
		
		addStep("Validar que se actualizó el estatus del archivo procesado de I a E");
		System.out.println("Paso 10 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarStatusEFormat = String.format(validarStatusE, id);
		System.out.println(validarStatusEFormat);
		
		SQLResult exe_consultaStatusE = executeQuery(dbFCWM6QA_NUEVA, validarStatusEFormat);
		
		String status = exe_consultaStatusE.getData(0, "STATUS");
		System.out.println("El status es: "+status);
		
		boolean validaConsultasStatusE = exe_consultaStatusE.isEmpty();
		
		if (!validaConsultasStatusE) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusE);
		}
		
		System.out.println(validaConsultasStatusE);
		assertEquals(status, "E");
		
//***************************************** Paso 11 *****************************************************************
		/*
		addStep("Validar que se encuentre el registro actualizado en la tabla POS_INBOUND_DOC_ALL");
		System.out.println("Paso 11 "+ GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarStatusE2Format = String.format(validarStatusE2, id);
		System.out.println(validarStatusE2Format);
		
		SQLResult exe_consultaStatusE2 = executeQuery(dbFCWM6QA_NUEVA, validarStatusE2Format);
		
		String statusE2 = exe_consultaStatusE2.getData(0, "STATUS");
		System.out.println("El status es: "+statusE2);
		
		boolean validaConsultasStatusE2 = exe_consultaStatusE2.isEmpty();
		
		if (!validaConsultasStatusE2) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusE2);
		}
		
		System.out.println(validaConsultasStatusE2);
		assertEquals(statusE2, "E");
		
//***************************************** Paso 12 y 13 *****************************************************************
		
		addStep("Ejecutar la siguiente consulta en la BD 'AVEBQA' para validar que se insertó la información procesada");
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(consultaAVEBQA);

		SQLResult exe_consultaAVEBQA = executeQuery(dbAVEBQA, consultaAVEBQA);

		boolean validaConsultaAVEBQA = exe_consultaAVEBQA.isEmpty();

		if (!validaConsultaAVEBQA) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaAVEBQA);
		}

		System.out.println(validaConsultaAVEBQA);
		assertFalse(validaConsultaAVEBQA, "No existe información en la tabla.");
		
//********************************Paso 14 y 15 *****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMLQA':"); 
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(consulta1FCWMLQA);

		SQLResult exe_consulta1FCWMLQA = executeQuery(dbFCWMLQA, consulta1FCWMLQA);

		String statusS = exe_consulta1FCWMLQA.getData(0, "STATUS");
		System.out.println("El status es: " + statusS);

		boolean validaConsultasStatusS = exe_consulta1FCWMLQA.isEmpty();
		String run_id = "";

		if (!validaConsultasStatusS) {
			run_id = exe_validarDatosPR50.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(exe_consulta1FCWMLQA);
		}

		System.out.println(validaConsultasStatusS);
		assertEquals(statusS, "S");
			
//********************************Paso 16 y 17 *****************************************************************	
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMLQA':"); 
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String validaThreadsFormat = String.format(consulta2FCWMLQA, run_id);
		System.out.println(validaThreadsFormat);
		
		SQLResult exe_consulta2FCWMLQA = executeQuery(dbFCWMLQA, validaThreadsFormat);
		
		String statusS2 = exe_consulta2FCWMLQA.getData(0, "STATUS");
		System.out.println("El status es: " + statusS2);
		
		boolean validaConsultasStatusS2 = exe_consulta2FCWMLQA.isEmpty();
		
		if (!validaConsultasStatusS2) {
			testCase.addQueryEvidenceCurrentStep(exe_consulta2FCWMLQA);
		}
		
		System.out.println(validaConsultasStatusS2);
		assertEquals(statusS2, "S");
		
//********************************Paso 19 *****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMLQA':");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(consulta3FCWMLQA);

		SQLResult exe_consulta3FCWMLQA = executeQuery(dbFCWMLQA, consulta3FCWMLQA);

		boolean validarNoErrores = exe_consulta3FCWMLQA.isEmpty();
		System.out.println("ValidarNoErrores es: " + validarNoErrores);

		if (!validarNoErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consulta3FCWMLQA);
		} else {
			testCase.addTextEvidenceCurrentStep("No se encontraron errores en la tabla");
			System.out.println("No se encontraron errores en la tabla");
			testCase.addQueryEvidenceCurrentStep(exe_consulta3FCWMLQA);
		}

		System.out.println(validarNoErrores);
		assertTrue(validarNoErrores, "Existen errores en la tabla.");
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
		return "Validar que los archivos TPC que fueron insertados por la PR50 sean procesados por la PO21_OLD";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PO21_ProcesarArchivoTPCInsertadoPorPR50_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
