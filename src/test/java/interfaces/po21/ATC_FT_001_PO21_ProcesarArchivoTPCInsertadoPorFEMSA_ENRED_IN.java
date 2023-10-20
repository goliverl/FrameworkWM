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

public class ATC_FT_001_PO21_ProcesarArchivoTPCInsertadoPorFEMSA_ENRED_IN extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO21_ProcesarArchivoTPCInsertadoPorFEMSA_ENRED_IN_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1 y 2
		String statusL = "SELECT ROWNUM,ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE FROM("
				+ "    SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "    FROM POSUSER.POS_INB_DOC_FIN "
				+ "    WHERE STATUS = 'I' AND DOC_TYPE = 'TPC' "
				+ "    ORDER BY INSERTED_DATE DESC) "
				+ "WHERE ROWNUM <= 10"; 
//Paso 3
		String validarReg = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INB_DOC_NAR "
				+ "WHERE ID = '%s'";
//Paso 4 
		String validarReg2 = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,STATUS,DOC_TYPE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID = '%s'";
//Paso 5
		String consulta1 = "SELECT * FROM POSUSER.POS_TPC WHERE PID_ID = '%s'";
//Paso 6
		String consulta2 = "SELECT PID_ID,FORMA_PAGO,CVE_MVTO,FOLIO_WM,ACOUNT_TYPE,EMISOR_TYPE,PV_DATE "
				+ "FROM POSUSER.POS_TPC_DETL "
				+ "WHERE PID_ID = '%s'"; 
//Paso 10
		Date fechaEjecucionInicio;
//Paso 11
		String consultaStatusE = "SELECT ID, PV_CR_PLAZA, PV_CR_TIENDA, PV_DOC_ID, DOC_TYPE, STATUS "
				+ "FROM POSUSER.POS_INB_DOC_FIN "
				+ "WHERE ID = '4474113454' "
				+ "AND DOC_TYPE = 'TPC'";
//Paso 12
		String consultaStatusE2 = "SELECT ID, PV_CR_PLAZA, PV_CR_TIENDA, PV_DOC_ID, DOC_TYPE, STATUS "
				+ "FROM POSUSER.POS_INB_DOC_NAR "
				+ "WHERE ID = '4474113454'";
//Paso 13
		String consultaStatusE3 = "SELECT ID, PV_CR_PLAZA, PV_CR_TIENDA, PV_DOC_ID, DOC_TYPE, STATUS "
				+ "FROM POSUSER.POS_INBOUND_DOC_ALL "
				+ "WHERE ID = '4474113454'";
//Paso 15
		String consultaAVEBQA = "SELECT FORMA_PAGO,CR_PLAZA,CR_TIENDA,ACCOUNTING_DATE,STATUS_FLAG,CREATION_DATE "
				+ "FROM XXFC.XXFC_MPC_GL_TPC "
				+ "WHERE CREATION_DATE >= TO_DATE('12-OCT-22', 'DD-MON-RR') "
				+ "AND ROWNUM <= 10";
//Paso 17
		String consulta1FCWMLQA = "SELECT ROWNUM,RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM("
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE '%PO21%' ORDER BY START_DT DESC) "
				+ "WHERE ROWNUM <= 10";
//Paso 18
		String consulta2FCWMLQA = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD \"\r\n"
				+ "WHERE PARENT_ID ='%s'" ;

//Paso 19
		String consulta3FCWMLQA = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='%s'";

/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//*****************************************Paso 1 y 2	***************************************************************** 
		
		addStep("Validar que exista informacion pendiente de procesar y que fueron insertados por la FEMSA_ENRED_IN");
		System.out.println("Paso 1 y 2 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(statusL);
		
		SQLResult statusL_Res = executeQuery(dbFCWM6QA, statusL);
		
		boolean validaStatusL = statusL_Res.isEmpty();
		String id = "";
		
			if (!validaStatusL) {
				id = statusL_Res.getData(0, "ID");
				testCase.addQueryEvidenceCurrentStep(statusL_Res);
				} 
		
		System.out.println(validaStatusL);
		System.out.println("El ID es = "+id);
		
		assertFalse(validaStatusL, "No existe información pendiente de procesar en la tabla.");
		
//**************************************Paso 3	*********************************************************************** 
		
		addStep("Validar que el registro que existe en la tabla POS_INB_DOC_FIN existe también en la tabla POS_INB_DOC_NAR");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarRegFormat = String.format(validarReg, id);
		System.out.println(validarRegFormat);
		
		SQLResult statusV_Registro = executeQuery(dbFCWM6QA, validarRegFormat);
		
		boolean validaRegistroOtraTabla = statusV_Registro.isEmpty();
		
			if (!validaRegistroOtraTabla) {
		
			testCase.addQueryEvidenceCurrentStep(statusV_Registro);
			
						} 
		
		System.out.println(validaRegistroOtraTabla);

		assertFalse(validaRegistroOtraTabla, "No existe información en la tabla.");
		
//**********************************************Paso 4 *****************************************************************
		
		addStep("Validar que el registro que existe en la tabla POS_INB_DOC_FIN existe también en la tabla POS_INBOUND_DOC_ALL");
		System.out.println("Paso 4 "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarReg2Format = String.format(validarReg2, id);
		System.out.println(validarReg2Format);

		SQLResult statusV_Registro2 = executeQuery(dbFCWM6QA, validarReg2Format);
		
		boolean validaRegistroOtraTabla2 = statusV_Registro2.isEmpty();
		
			if (!validaRegistroOtraTabla2) {
				testCase.addQueryEvidenceCurrentStep(statusV_Registro2);
			}
			
		System.out.println(validaRegistroOtraTabla2);
		assertFalse(validaRegistroOtraTabla2, "No existe información en la tabla.");
		
//*******************************************************Paso 5*******************************************************
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA' ");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarConsulta1Format = String.format(consulta1, id);
		System.out.println(validarConsulta1Format);
		
		SQLResult exe_consulta1 = executeQuery(dbFCWM6QA, validarConsulta1Format);
		
		boolean validaConsulta1 = exe_consulta1.isEmpty();
		
			if (!validaConsulta1) {
				testCase.addQueryEvidenceCurrentStep(exe_consulta1);
			}
			
		System.out.println(validaConsulta1);
		assertFalse(validaConsulta1, "No existe información en la tabla.");
		
//**************************************************Paso 6 *********************************************************
		
		addStep("Ejecutar la siguiente consulta en la BD 'FCWM6QA': ");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String validarConsulta2Format = String.format(consulta2, id);
		System.out.println(validarConsulta2Format);
		
		SQLResult exe_consulta2 = executeQuery(dbFCWM6QA, validarConsulta2Format);
		
		boolean validaConsulta2 = exe_consulta2.isEmpty();
		
			if (!validaConsulta2) {
				testCase.addQueryEvidenceCurrentStep(exe_consulta2);
			}
			
		System.out.println(validaConsulta2);
		assertFalse(validaConsulta2, "No existe información en la tabla.");
		
		
//****************************************Paso 10 ****************************************************************
		
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
		
		//assertEquals(resultadoEjecucion, "Wait Condition");
		u.close(); 
		
//********************************Paso 11 *****************************************************************

		addStep("Validar que se actualizó el estatus del archivo procesado de I a E");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaStatusE);
		

		SQLResult exe_consultaStatusE = executeQuery(dbFCWM6QA, consultaStatusE);

		
		String status = exe_consultaStatusE.getData(0, "STATUS");
		System.out.println("El status es: "+status);
		
		boolean validaConsultasStatusE = exe_consultaStatusE.isEmpty();
		
		if (!validaConsultasStatusE) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusE);
		}
		
		System.out.println(validaConsultasStatusE);
		assertEquals(status, "E");
		
//********************************Paso 12 *****************************************************************
		addStep("Validar que se encuentre el registro actualizado en la tabla POS_INB_DOC_NAR");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaStatusE2);
		

		SQLResult exe_consultaStatusE2 = executeQuery(dbFCWM6QA, consultaStatusE2);

		
		String status2 = exe_consultaStatusE2.getData(0, "STATUS");
		System.out.println("El status es: "+status2);
		
		boolean validaConsultasStatusE2 = exe_consultaStatusE2.isEmpty();
		
		if (!validaConsultasStatusE2) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusE2);
		}
		
		System.out.println(validaConsultasStatusE2);
		assertEquals(status2, "E");

//********************************Paso 13 *****************************************************************
		addStep("Validar que se encuentre el registro actualizado en la tabla POS_INBOUND_DOC_ALL");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaStatusE3);


		SQLResult exe_consultaStatusE3 = executeQuery(dbFCWM6QA, consultaStatusE3);


		String status3 = exe_consultaStatusE3.getData(0, "STATUS");
		System.out.println("El status es: " + status3);

		boolean validaConsultasStatusE3 = exe_consultaStatusE3.isEmpty();

		if (!validaConsultasStatusE3) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusE3);
		}

		System.out.println(validaConsultasStatusE3);
		assertEquals(status3, "E");

//********************************Paso 14 y 15 *****************************************************************
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
	
//********************************Paso 16 y 17 *****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMLQA':"); //NO ENCUENTRA EL STATUS S
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(consulta1FCWMLQA);
		
		SQLResult exe_consulta1FCWMLQA = executeQuery(dbFCWMLQA, consulta1FCWMLQA);

		String statusS = exe_consulta1FCWMLQA.getData(0, "STATUS");

		String runID = exe_consulta1FCWMLQA.getData(0, "RUN_ID");

		System.out.println("El status es: " + statusS);

		boolean validaConsultasStatusS = exe_consulta1FCWMLQA.isEmpty();

		if (!validaConsultasStatusS) {
			testCase.addQueryEvidenceCurrentStep(exe_consulta1FCWMLQA);
		}

		System.out.println(validaConsultasStatusS);
		assertEquals(statusS, "E"); //////
		
//********************************Paso 18 *****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMLQA':"); 
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String consulta2FCWMLQAFormat = String.format(consulta2FCWMLQA, runID);
		System.out.println(consulta2FCWMLQAFormat);

		SQLResult exe_consulta2FCWMLQA = executeQuery(dbFCWMLQA, consulta2FCWMLQAFormat);

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

		String consulta3FCWMLQAFormat = String.format(consulta3FCWMLQA, runID);
		System.out.println(consulta3FCWMLQAFormat);
		
		SQLResult exe_consulta3FCWMLQA = executeQuery(dbFCWMLQA, consulta3FCWMLQAFormat);

		boolean validarNoErrores = exe_consulta3FCWMLQA.isEmpty();
		System.out.println("ValidarNoErrores es: "+validarNoErrores);

		if (!validarNoErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consulta3FCWMLQA);
		}else {
			testCase.addTextEvidenceCurrentStep("No se encontraron errores en la tabla");
			System.out.println("No se encontraron errores en la tabla");
			testCase.addQueryEvidenceCurrentStep(exe_consulta3FCWMLQA);
		}
		
		System.out.println(validarNoErrores);
		assertTrue(validarNoErrores, "Existen errores en la tabla.");

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
		return "Validar que los archivos TPC que fueron insertados por la FEMSA_ENRED_IN sean procesados por la PO21 ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO21_ProcesarArchivoTPCInsertadoPorFEMSA_ENRED_IN_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
