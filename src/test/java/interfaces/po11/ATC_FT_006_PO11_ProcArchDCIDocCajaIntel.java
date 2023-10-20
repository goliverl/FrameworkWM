package interfaces.po11;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.MEdgeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_006_PO11_ProcArchDCIDocCajaIntel extends BaseExecution {

	/**
	 * BO ORACLE - PO11: MTC-FT-006-C1 PO11 Procesar archivo DCI de documento de caja inteligente de la interface FEMSA_PO11
	 * Desc:
	 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de 
	 * la interface FEMSA_PO11 de avante para procesar archivos DCI 
	 * (Documento de Caja Inteligente) y ser enviados de WM INBOUND a EBS,
	 *  al ser migrada la interface de WM9.9 a WM10.5.
	 * 
	 * 
	 * @author Jose Onofre
	 * @date 02/01/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PO11_ProcArchDCIDocCajaIntel_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbFCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		String consultaWM_FW_BO = "Select COMPANY,APPLICATION,PROC_CODE,CR_PLAZA,"
				+ "CR_TIENDA,STATUS,DESCRIPTION,MODIFIED_DATE "
				+ " from wmuser.wm_fw_bo_encendido where application in ('PO11')";
		
		String consultaEnvelope = "SELECT I.ID,I.PE_ID,I.STATUS,I.DOC_TYPE,"
				+ "I.TARGET_ID,I.INSERTED_DATE,I.VERSION "
				+ "FROM POSUSER.POS_ENVELOPE E, POSUSER.POS_INBOUND_DOCS I "
				+ "WHERE E.ID = I.PE_ID AND "
				+ "E.RECEIVED_DATE >= '18-Jan-2023' " 
				+ "AND I.DOC_TYPE='DCI'";
		
		String consultaDetalleDCI = "select I.NO_RECORDS,I.PV_DOC_ID, D.PID_ID,"
				+ "D.MOV_TYPE,D.MOV_ID_POS,D.PV_DATE,D.MONTO "
				+ "from POSUSER.POS_DCI I, POSUSER.POS_DCI_DETL D "
				+ "WHERE I.PID_ID = D.PID_ID AND I.PID_ID= '%s'";
		
		String consultaCuentaPlaza = "SELECT T.DESCRIPCION, T.TIPO_ASIENTO, T.CUENTA,"
				+ " T.AUXILIAR, T.CR_MAPEO, T.TIPO_CARGO, T.ATTRIBUTO2, T.ATTRIBUTO3,"
				+ " T.plaza " 
				+ "FROM RMS100.FEM_POS_CUENTAS_TPE T WHERE PLAZA IN ('%s','%s') "
				+ "AND ROWNUM <= 5 ";
		
		String consultaTiendaRETEK = "select oracle_cr, oracle_cr_desc, oracle_cr_superior,"
				+ " oracle_ef, oracle_ef_desc, oracle_cia, oracle_cia_desc, legacy_ef,\r\n" + 
				"legacy_cr, retek_cr, retek_distrito, estado from xxfc_maestro_de_crs_v \r\n" + 
				"where estado = 'A' and oracle_cr_superior in ('%s') and oracle_cr in ('%s') \r\n" + 
				"and oracle_cr_type = 'T'";
		
		String consultaGL_Interface = "SELECT --*\r\n" + 
				"DATE_CREATED, USER_JE_CATEGORY_NAME, SEGMENT1, SEGMENT2, SEGMENT3, SEGMENT4, SEGMENT5, SEGMENT6, SEGMENT7, ENTERED_CR, ENTERED_DR, REFERENCE10, GROUP_ID \r\n" + 
				"FROM GL_INTERFACE\r\n" + 
				"WHERE DATE_CREATED>='12082020'\r\n" + 
				"AND GROUP_ID='6173183'\r\n" + 
				"order by DATE_CREATED desc"; 
		
		String consultaWM_LOG_RUN = "Select * from wmlog.wm_log_run where interface LIKE '%PO11%' ORDER BY RUN_ID DESC";
		
		String consultaWM_LOG_THREAD = "Select * from wmlog.WM_LOG_THREAD where PARENT_ID IN (%s)";
		
		String consultaWM_LOG_ERROR = "SELECT * FROM wmlog.WM_LOG_ERROR WHERE RUN_ID= '%s'";
		
		String consultaPOSUSER = "SELECT * FROM posuser.POS_INBOUND_DOCS WHERE DOC_TYPE = 'DCI' AND ID IN (%s)";
		
		Date fechaEjecucionInicio;
		
		String expec_DCI_status = "I";
		String expec_WM_LOG_RUN_stat = "S";
		String expec_WM_LOG_THREAD_stat = "S";
		String expec_POSUSER_stat = "E";
		
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		addStep("Realizar conexión a la BD FCWM6QA");
		
		
		boolean conexiondbFCWM6QA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWM6QA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		assertTrue(conexiondbFCWM6QA, "La conexion no fue exitosa");
	
		
		// Paso 2 ****************************************************
		
		addStep("Validar la configuración de las plazas para la interface"
				+ " PO11 en la tabla wm_fw_bo_encendido");
		
		
		SQLResult ConsultaWM_FW_BO_r = executeQuery(dbFCWM6QA, consultaWM_FW_BO);
		
		boolean validaWM_FW_BO = ConsultaWM_FW_BO_r.isEmpty();
		
		if(!validaWM_FW_BO) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaWM_FW_BO_r);
			
			
		}
		
		
		
		// Paso 3 ****************************************************
		
		addStep("Validar que el ENVELOPE exista un archivo DCI con status='I' en la BD FCWM6QA");
		
		
		String consultaEnvelope_r = String.format(consultaEnvelope, "fecha_actual");
		SQLResult dbFCWM6QAResult = executeQuery(dbFCWM6QA, consultaEnvelope_r);

		String DCI_status = "";
		String PID_ID = "";

		boolean validaEnvelope = dbFCWM6QAResult.isEmpty();

		if (!validaEnvelope) {

			DCI_status = dbFCWM6QAResult.getData(0, "STATUS");
			PID_ID = dbFCWM6QAResult.getData(0, "ID");

			System.out.println("STATUS: " + DCI_status + "\n");
			System.out.println("PID_ID: " + PID_ID + "\n");
			testCase.addQueryEvidenceCurrentStep(dbFCWM6QAResult);
		}

		System.out.println(validaEnvelope);

		assertFalse(validaEnvelope, "El status obtenido es diferente");
		assertEquals(expec_DCI_status, DCI_status);
		
		
		// Paso 4 ****************************************************
		
		
		addStep("Validar que exista el detalle de los archivos DCI"
				+ " con información a procesar en la BD FCWM6QA ");
		
		String consultaDetalleDCI_r = String.format(consultaDetalleDCI, PID_ID);
		SQLResult DetalleDCI_Result = executeQuery(dbFCWM6QA, consultaDetalleDCI_r);	

		boolean validaDetalleDCI = DetalleDCI_Result.isEmpty();

		if (!validaDetalleDCI) {

			
			testCase.addQueryEvidenceCurrentStep(DetalleDCI_Result);
		}

		System.out.println(validaDetalleDCI);

		assertFalse(validaDetalleDCI, "No se mostraron los registros del archivo DCI");
		
		// Paso 5 ****************************************************
		
		addStep("Realizar conexión a la BD FCRMSQA");
		
		
		boolean conexiondbFCRMSQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCRMSQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_MEX);

		assertTrue(conexiondbFCRMSQA, "La conexion no fue exitosa");
		
		// Paso 6 ****************************************************
		
		addStep("Validar que exista información de la cuenta para la plaza a validar en BD FCRMSQA");
		
		String consultaCuentaPlaza_r = String.format(consultaCuentaPlaza, data.get("plaza1"),data.get("plaza2"));
		SQLResult consultaCuentaPlaza_Result = executeQuery(dbFCRMSQA, consultaCuentaPlaza_r);	

		boolean validaCuentaPlaza = consultaCuentaPlaza_Result.isEmpty();

		if (!validaCuentaPlaza) {

					
			testCase.addQueryEvidenceCurrentStep(consultaCuentaPlaza_Result);
		}

		System.out.println(validaCuentaPlaza);

		assertFalse(validaCuentaPlaza, "No se mostraron los registros de la cuenta para la plaza");
		
		
		// Paso 7 ****************************************************
		
		addStep("Realizar conexión en la BD AVEBQA de EBS");
		
		boolean conexiondbAVEBQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQA, "La conexion no fue exitosa");
		
		// Paso 8 ****************************************************
		
		addStep("Validar que exista información de la tienda (RETEK_DISTRITO) en la"
				+ " xxfc_maestro_de_crs_v de la BD AVEBQA de EBS con oracle_cr_type = 'T'");
		
		
		String consultaTiendaRETEK_s = String.format(consultaTiendaRETEK, data.get("plaza1"), data.get("tienda"));
		SQLResult consultaTiendaRETEK_Result = executeQuery(dbAVEBQA, consultaTiendaRETEK_s);	

		boolean validaTiendaRETEK = consultaTiendaRETEK_Result.isEmpty();

		if (!validaTiendaRETEK) {

					
			testCase.addQueryEvidenceCurrentStep(consultaTiendaRETEK_Result);
		}

		System.out.println(validaTiendaRETEK);

		assertFalse(validaTiendaRETEK, "No existe informacion de la tienda");
		
		
		// Paso 9 ****************************************************
		
		addStep("Solicitar a los Usu FC Operadores SITE vía correo electrónico"
				+ " <UsuFEMCOMOperadoresSITE@oxxo.com>,la ejecución de la interfaz PO11");
		
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
		String resultadoEjecucion = j.jobRunner();
		
		
		
		// Paso 10 ****************************************************
		
		addStep("Consultar la ejecución del Job en Control M, donde debe mostrarse en color verde,"
				+ " que indica que la ejecución se realizó de forma exitosa para el Job PO11.");
	
		
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		
		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		
		
		// Paso 11 ****************************************************
		
		addStep("Realizar conexión a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net");
		
		
		boolean conexiondbFCWMLTAQ = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);

		assertTrue(conexiondbFCWMLTAQ, "La conexion no fue exitosa");
		
		
		// Paso 12 ****************************************************
		
		addStep("Validar la correcta ejecución de la interface PO11 "
				+ "en la tabla WM WM_LOG_RUN de la BD FCWMLQA");
		
		
		String WM_LOG_RUN_stat = null;
		String run_id = null;
		
		SQLResult consultaWM_LOG_RUN_Result = executeQuery(dbFCWMLQA, consultaWM_LOG_RUN);	

		boolean validaWM_LOG_RUN = consultaWM_LOG_RUN_Result.isEmpty();

		if (!validaWM_LOG_RUN) {

			WM_LOG_RUN_stat = consultaWM_LOG_RUN_Result.getData(0, "STATUS");
			//run_id = consultaWM_LOG_RUN_Result.getData(0, "RUN_ID");
			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_RUN_Result);
		}

		System.out.println(validaWM_LOG_RUN);

		assertEquals(expec_WM_LOG_RUN_stat, WM_LOG_RUN_stat);
		
		// Paso 13 ****************************************************
		
		addStep("Validar la correcta ejecución de los Threads lanzados por "
				+ "la interface PO11 en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		
		
		String WM_LOG_THREAD_stat = null;
		run_id = "2175269204";
		String consultaWM_LOG_THREAD_s = String.format(consultaWM_LOG_THREAD, run_id);
		SQLResult consultaWM_LOG_THREAD_Result = executeQuery(dbFCWMLQA, consultaWM_LOG_THREAD_s);	

		boolean validaWM_LOG_THREAD = consultaWM_LOG_THREAD_Result.isEmpty();

		if (!validaWM_LOG_THREAD) {

			WM_LOG_THREAD_stat = consultaWM_LOG_THREAD_Result.getData(0, "STATUS");
			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_THREAD_Result);
		}

		System.out.println(validaWM_LOG_THREAD);

		assertEquals(expec_WM_LOG_THREAD_stat, WM_LOG_THREAD_stat);
		
		// Paso 14 ****************************************************
		
		addStep("verificar que no se encuentre ningún error presente "
				+ "en la ejecución de la interfaz PO11 en la tabla WM_LOG_ERROR de BD FCWMLQA");
		
		String consultaWM_LOG_ERROR_s = String.format(consultaWM_LOG_ERROR, run_id);
		SQLResult consultaWM_LOG_ERROR_Result = executeQuery(dbFCWMLQA, consultaWM_LOG_ERROR_s);	

		boolean validaWM_LOG_ERROR = consultaWM_LOG_ERROR_Result.isEmpty();

		if (!validaWM_LOG_ERROR) {

			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_ERROR_Result);
		}
		else {
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_ERROR_Result);
			}

		System.out.println(validaWM_LOG_ERROR);

		assertTrue(validaWM_LOG_ERROR, "Se muestran errores en la ejecucion");
		
		
		
		// Paso 15 ****************************************************
		
		addStep("Validar que el status de los documentos DCI cambió a 'E' en "
				+ "la tabla POS_INBOUND_DOCS de la BD FCWM6QA y se haya generado el "
				+ "número de póliza en el campo TARGET_ID");
		
		
		String POSUSER_stat = null;
		
		String consultaPOSUSER_s = String.format(consultaPOSUSER, PID_ID);
		SQLResult consultaPOSUSER_Result = executeQuery(dbFCWM6QA, consultaPOSUSER_s);	

		boolean validaconsultaPOSUSER = consultaPOSUSER_Result.isEmpty();

		if (!validaconsultaPOSUSER) {

			POSUSER_stat = consultaPOSUSER_Result.getData(0, "STATUS");
			
			testCase.addQueryEvidenceCurrentStep(consultaPOSUSER_Result);
		}

		System.out.println(validaconsultaPOSUSER);

		assertEquals(POSUSER_stat, expec_POSUSER_stat);
		
		
		
		// Paso 16 ****************************************************
		
		addStep("Validar que se insertaron en la GL_INTERFACE de la BD AVEBQA de EBS,"
				+ " las poliza contables de documentos DCI procesados para la plaza");
		
		/*
		
		String consultaGL_Interface_r = String.format(consultaGL_Interface, data.get("group_id"));
		SQLResult consultaGL_Interface_Result = executeQuery(dbFCRMSQA, consultaGL_Interface_r);	

		boolean validaGL_Interface = consultaGL_Interface_Result.isEmpty();

		if (!validaGL_Interface) {

			
			testCase.addQueryEvidenceCurrentStep(consultaGL_Interface_Result);
		}

		System.out.println(validaGL_Interface);

		assertFalse(validaGL_Interface, "No se mostro la poliza");
		
		
		*/
		
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_006_PO11_ProcArchDCIDocCajaIntel_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseOnofre@Hexaware.com";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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

}

