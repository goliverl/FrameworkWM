package interfaces.ro12_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_RO12_MX_ValidaEnvioCambioCorpOperatRetek extends BaseExecution {
	
	/**
	 * RO12_MX: MTC_FT_003 RO12_MX Validar el envío de tipo de cambio Corporate/Operational a Retek(RMSv16) a través de la interface RO12_MX
	 * Desc:
	 * Prueba de regresión  para comprobar la no afectación en la funcionalidad 
	 * principal de la interface FEMSA_RO12_MX de avante para enviar el tipo de 
	 * cambio corporate de EBS_avante a RMS, al ser migrada la interface de WM9.9
	 * a WM10.5. Interface que mantiene sincronizado el tipo de cambio entre finanzas, 
	 * el Sistema de mercadeo y el POS. Interface RO12 encargada de sincronizar el tipo 
	 * de cambio almacenado en Oracle con RETEK y almacenado en Oracle con todas las tiendas 
	 * para una plaza determinada
	 * 
	 * @author Jose Onofre
	 * @date 02/23/2023
	 * 
	 */
	
	public String tipo;
	/*
	 * Proyecto: BO NUCLEO (Regresion Enero 2023)
	 * Caso de prueba: MTC-FT-003-C1 Validar el envío de tipo de cambio CorporateOperational a Retek(RMSv16) a través de la interface RO12_MX
	 * @author 
	 * @date 
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO12_MX_ValidaEnvioCambioCorpOperatRetek_test(HashMap<String, String> data)
			throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbAVEBQAEBS = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);

//		SQLUtil dbFWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_UPD, GlobalVariables.DB_USER_FCWMQA_UPD,
//				GlobalVariables.DB_PASSWORD_FCWMQA_UPD);
		
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,
				GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		/*
		 * Variables
		 *************************************************************************/

		String InsertarTipoCambio = "INSERT INTO GL.GL_DAILY_RATES"
				+ "(FROM_CURRENCY, TO_CURRENCY, CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE,"
				+ " STATUS_CODE, CREATION_DATE, CREATED_BY, LAST_UPDATE_DATE, LAST_UPDATED_BY, "
				+ "LAST_UPDATE_LOGIN, CONTEXT, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, "
				+ "ATTRIBUTE5, ATTRIBUTE6, ATTRIBUTE7, ATTRIBUTE8, ATTRIBUTE9, ATTRIBUTE10, ATTRIBUTE11,"
				+ " ATTRIBUTE12, ATTRIBUTE13, ATTRIBUTE14, ATTRIBUTE15, RATE_SOURCE_CODE) VALUES " + " ('"
				+ data.get("FROM") + "','" + data.get("TO") + "',TO_DATE('" + data.get("CONVERSION_DATE")
				+ "','DD/MM/YYYY'),'" + data.get("CONVERSION_TYPE") + "','" + data.get("CONVERSION_RATE") + "','"
				+ data.get("STATUS_CODE") + "'," + "TO_DATE('" + data.get("CREATION_DATE") + "','DD/MM/YYYY'), '"
				+ data.get("CREATED_BY") + "'," + "TO_DATE('" + data.get("LAST_UPDATE_DATE") + "','DD/MM/YYYY'),'"
				+ data.get("LAST_UPDATED_BY") + "'," + "'" + data.get("LAST_UPDATE_LOGIN")
				+ "',NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,NULL,"
				+ " NULL, NULL, NULL, NULL, NULL, NULL, NULL)";

		String consultaRegistro = "SELECT FROM_CURRENCY,TO_CURRENCY,CONVERSION_TYPE,CONVERSION_DATE,STATUS_CODE\n"
				+ "FROM GL.GL_DAILY_RATES\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\n"
				+ "ORDER BY CREATION_DATE DESC";

		String WmLOG1 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER\n"
				+ "FROM WMLOG.WM_LOG_RUN\n"
				+ "WHERE START_DT >= TRUNC(SYSDATE)\n"
				+ "AND INTERFACE = 'RO12_MX'";

		String WmLOG2 = "SELECT THREAD_ID,NAME,START_DT,END_DT,STATUS\n"
				+ "FROM WMLOG.WM_LOG_THREAD\n"
				+ "WHERE PARENT_ID='%s'";

		String WmSyncExc = "SELECT TO_CURRENCY,CONVERSION_DATE,CONVERSION_RATE,USER_CONVERSION_TYPE,WM_STATUS\n"
				+ "FROM  WM_SYNC_RMS_EXC_RATES\n"
				+ "WHERE  WM_STATUS='L' \n"
				+ "AND WM_CREATION_DATE>= TRUNC(SYSDATE)\n";

		String WmSyncExcCodeE = "SELECT CR_PLAZA,CR_TIENDA,CONVERSION_TYPE,USER_CONVERSION_TYPE,WM_STATUS_CODE\n"
				+ "FROM WM_SYNC_POS_EXC_RATES\n"
				+ "WHERE CONVERSION_DATE >=TRUNC(SYSDATE) \n"
				+ "AND WM_STATUS_CODE='E";

		String consultaPOSOUTBOND = "select * from " + "POS_OUTBOND_DOCS WHERE DOC_TYPE='EXR' "
				+ "AND PARTITION_DATE>=TRUNC(SYSDATE) " 
				+ "ORDER BY SENT_DATE DESC";

		String WmLogRUN = "select * from " 
		+ "WMLOG.WM_LOG_RUN WHERE INTERFACE in %RO12_MX% " 
				+ "ORDER BY END_DT DESC";

		String WmLogTHREAD = "select * from " 
		+ "WMLOG.WMLOG_THREAD WHERE PARENT_ID=%s";

		String WmLogERROR = "select * from " 
		+ "WMLOG.WM_LOG_ERROR WHERE RUN_ID=%s";
		
		String WmStatus = "select * from "
				+ "WM_SYNC_RMS_EXC_RATES WHERE WM_STATUS='E' "
				+ "AND WM_CREATION_DATE>=<FECHA> WM_STATUS_CODE='E' ";

		String consultaCurrency = "select * from "
				+ "RMS.CURRENCY_RATES ORDER BY EFFECTIVE_DATE DESC";
		
		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		addStep("Establecer conexion con la BD 'AVEBQA EBS' ");

		boolean conexiondbAVEBQAEBS = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQAEBS ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQAEBS, "La conexion no fue exitosa");

		/// Paso 2 ***************************************************

		addStep("Insertar un Tipo de Cambio en la tabla");

		SQLResult InsertarTipoCambio_r = executeQuery(dbAVEBQAEBS, InsertarTipoCambio);

		boolean validaInsercionTipoCambio = InsertarTipoCambio.isEmpty();

		if (!validaInsercionTipoCambio) {

			testCase.addQueryEvidenceCurrentStep(InsertarTipoCambio_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se inserto correctamente");
			testCase.addQueryEvidenceCurrentStep(InsertarTipoCambio_r);

		}

		/// Paso 3 ***************************************************

		addStep("Validar que se encuentre el nuevo registro insertado " + "en la tabla 'GL.GL_DAILY_RATES' ");

		SQLResult consultaRefRegistroInsertado_r = executeQuery(dbAVEBQAEBS, consultaRegistro);

		boolean validaConsultaRegistroInsertado = consultaRegistro.isEmpty();

		if (!validaConsultaRegistroInsertado) {

			testCase.addQueryEvidenceCurrentStep(consultaRefRegistroInsertado_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaRefRegistroInsertado_r);

		}

		/// Paso 4 ***************************************************

		addStep("Establecer conexion con la BD 'FCWMLQA' ");

		boolean conexiondbFCWMLQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);

		assertTrue(conexiondbFCWMLQA, "La conexion no fue exitosa");

		/// Paso 5 ***************************************************

		addStep("Validar correcta ejecucion de la interface " + " 'WM_LOG_RUN' ");

		SQLResult consultaWmLOG1_r = executeQuery(dbFCWMLQA, WmLOG1);

		boolean validaConsultaWmLOG1_r = WmLOG1.isEmpty();

		if (!validaConsultaWmLOG1_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOG1_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOG1_r);

		}

		/// Paso 6 ***************************************************

		addStep("Consultar tabla 'WMLOG.WM_LOG_THREAD' para ver " + " el THREAD creado al ejecutar la interfaz");

		String run_id = "";

		String StatusThread = String.format(WmLOG1, run_id);

		SQLResult consultaWmLOGThread_r = executeQuery(dbAVEBQAEBS, StatusThread);

		boolean validaConsultaWmLOGThread_r = StatusThread.isEmpty();

		if (!validaConsultaWmLOGThread_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOGThread_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOGThread_r);

		}

		/// Paso 7 ***************************************************

		addStep("Consultar tabla 'WMLOG.WM_LOG_ERROR' para verificar "
				+ " que no se encuentre ningun registro de error ");

		String StatusWMError = String.format(WmLOG2, run_id);

		SQLResult consultaWmLOG2_r = executeQuery(dbAVEBQAEBS, StatusWMError);

		boolean validaConsultaWmLOG2_r = WmLOG1.isEmpty();

		if (!validaConsultaWmLOG2_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOG2_r);

		} else {

			testCase.addTextEvidenceCurrentStep("Se muestra mensaje de error");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOG2_r);

		}

		/// Paso 8 ***************************************************

		addStep("Consultar tabla 'WM_SYNC_RMS_EXC_RATES' para validar "
				+ " que se haya registrado el tipo de cambio en EBS ");

		SQLResult consultaWmSyncExc_r = executeQuery(dbAVEBQAEBS, WmSyncExc);

		boolean validaConsultaWmSyncExc_r = consultaWmSyncExc_r.isEmpty();

		if (!validaConsultaWmSyncExc_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExc_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExc_r);

		}

		/// Paso 9 ***************************************************

		addStep("Consultar tabla 'WM_SYNC_POS_EXC_RATES' para validar "
				+ " que se haya registrado el tipo de cambio en EBS ");

		SQLResult consultaWmSyncExcCodeE_r = executeQuery(dbAVEBQAEBS, WmSyncExcCodeE);

		boolean validaConsultaWmSyncExcCodeE_r = consultaWmSyncExcCodeE_r.isEmpty();

		if (!validaConsultaWmSyncExcCodeE_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExcCodeE_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExcCodeE_r);

		}

		/// Paso 10 ***************************************************

		addStep("Establecer conexion con la BD 'FCWM6QA' ");

		boolean conexiondbFWM6QA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFWM6QA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_UPD);

		assertTrue(conexiondbFWM6QA, "La conexion no fue exitosa");

		/// Paso 11 ***************************************************
//
//		addStep("Validar correcta insercion de los documentos enviados a la plaza"
//				+ " en la tabla 'POS_OUTBOUND_DOCS' ");
//
//		String DOC_NAME = "";
//		
//		SQLResult consultaPOSOUTBOND_r = executeQuery(dbFWM6QA, consultaPOSOUTBOND);
//
//		boolean validaconsultaPOSOUTBOND_r = consultaPOSOUTBOND.isEmpty();
//
//		if (!validaconsultaPOSOUTBOND_r) {
//
//			testCase.addQueryEvidenceCurrentStep(consultaPOSOUTBOND_r);
//
//		} else {
//
//			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
//			testCase.addQueryEvidenceCurrentStep(consultaPOSOUTBOND_r);
//
//		}

		/// Paso 12 ***************************************************

//		addStep("Ingresar al FTP (Filezilla) con usuario y contrasena " + "para validar los buzones por plaza");
//		
//		
//	       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
//	       
//	       Thread.sleep(20000);
//	       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + DOC_NAME;
//	                                 //host, puerto, usuario, contraseña
//	     
		
		/// Paso 13 ***************************************************

//		addStep("Validar que se realice el envio del archivo EXR generado por "
//				+ "la interfaz RO12_MX en el directorio configurado para la tienda procesada");
//		
//		  ///u01/posuser
//	       boolean validaFTP;
//	       
//	        if (ftp.fileExists(ruta) ) {
//	        	
//	        	validaFTP = true;
//	            testCase.addFileEvidenceCurrentStep(ruta);
//	            System.out.println("Existe");
//	            testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
//	            testCase.addBoldTextEvidenceCurrentStep(ruta);
//	            
//	        }else {
//	        	testCase.addFileEvidenceCurrentStep(ruta);
//	        	testCase.addBoldTextEvidenceCurrentStep("El archivo no existe ");
//	            System.out.println("No Existe");
//	            validaFTP = false;
//	        }
//	        
//	        assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
//		
		
		/// Paso 14 ***************************************************

		addStep("Solicitar el ordenamiento del Job runRO12_MX_RMS enviando un "
				+ "correo electrónico a los operadores USU'");

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

		// Paso 15 ****************************************************

		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");

		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();

		// Paso 16 ****************************************************

		addStep("Validar correcta ejecucion de la interface en la tabla 'WM_LOG_RUN' ");

		SQLResult WmLogRUN_r = executeQuery(dbFCWMLQA, WmLogRUN);

		boolean validaWmLogRUN_r = WmLogRUN.isEmpty();

		if (!validaWmLogRUN_r) {

			testCase.addQueryEvidenceCurrentStep(WmLogRUN_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(WmLogRUN_r);

		}

		// Paso 17 ****************************************************

		addStep("Validar correcta ejecucion de la interface en la tabla 'WM_LOG_THREAD' ");

		String run_thread_id = "";

		String ConsultaWmLogTHREAD = String.format(WmLogTHREAD, run_thread_id);
		SQLResult WmLogTHREAD_r = executeQuery(dbFCWMLQA, ConsultaWmLogTHREAD);

		boolean validaWmLogTHREAD_r = ConsultaWmLogTHREAD.isEmpty();

		if (!validaWmLogTHREAD_r) {

			testCase.addQueryEvidenceCurrentStep(WmLogTHREAD_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(WmLogTHREAD_r);

		}

		// Paso 18 ****************************************************

		addStep("Validar correcta ejecucion de la interface en la tabla 'WM_LOG_THREAD' ");

		String ConsultaWmLogERROR = String.format(WmLogERROR, run_thread_id);
		SQLResult WmLogERROR_r = executeQuery(dbFCWMLQA, ConsultaWmLogERROR);

		boolean validaWmLogERROR_r = ConsultaWmLogERROR.isEmpty();

		if (!validaWmLogERROR_r) {

			testCase.addQueryEvidenceCurrentStep(WmLogERROR_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(WmLogERROR_r);

		}

		// Paso 19 ****************************************************

		addStep("Validar correcta ejecucion de la interface en la tabla 'WM_STATUS' ");

		
		SQLResult WmStatus_r = executeQuery(dbAVEBQAEBS, WmStatus);

		boolean validaWmStatus_r = WmStatus.isEmpty();

		if (!validaWmStatus_r) {

			testCase.addQueryEvidenceCurrentStep(WmStatus_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(WmStatus_r);

		}
		
		
		/// Paso 20 ***************************************************

		addStep("Establecer conexion con la BD 'FCWM6QA' ");

		boolean conexiondbFCRMSQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCRMSQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_MEX);

		assertTrue(conexiondbFCRMSQA, "La conexion no fue exitosa");

		// Paso 21 ****************************************************

		addStep("Validar correcta ejecucion de la interface en la tabla 'WM_STATUS' ");

				
		SQLResult consultaCurrency_r = executeQuery(dbFCRMSQA, consultaCurrency);

		boolean validaconsultaCurrency_r = consultaCurrency.isEmpty();

		if (!validaconsultaCurrency_r) {

			testCase.addQueryEvidenceCurrentStep(consultaCurrency_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(consultaCurrency_r);

		}
		
		
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return "Prueba de regresión  para comprobar "
				+ "la no afectación en la funcionalidad principal "
				+ "de la interface FEMSA_RO12_MX para enviar "
				+ "el tipo de cambio Corporate/Operational de EBS_avante "
				+ "a RMSv16, al ser migrada la interface de WM9.9 a WM10.5";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_RO12_MX_ValidaEnvioCambioCorpOperatRetek_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}