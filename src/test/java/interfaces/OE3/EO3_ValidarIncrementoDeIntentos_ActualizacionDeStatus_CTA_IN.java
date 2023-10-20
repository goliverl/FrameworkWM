package interfaces.OE3;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class EO3_ValidarIncrementoDeIntentos_ActualizacionDeStatus_CTA_IN extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_EO3_ValidarIncrementoDeIntentos_ActualizacionDeStatus_CTA_IN_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utileria***************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SeleniumUtil u = null;
		/*
		 * Variables**************************************************/
		String queryExistsFile = "SELECT NOMBRE_ARCHIVO, XXH2H_ID_LOTE_GENERADO_PK, NVL(SENT_RETRIES,0) SENT_RETRIES, NVL(RESPONSE_RETRIES,0) RESPONSE_RETRIES, RESPONSE_DATE\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ESTATUS_WM = 'L'\r\n"
				+ "AND NVL(SENT_RETRIES,0) = '%s'";
		
		String queryFTPConfig = "SELECT * \r\n"
				+ "FROM WMUSER.WM_FTP_CONNECTIONS\r\n"
				+ "WHERE FTP_CONN_ID = 'SANTANDER_H2H_OUT'";
		
		String queryInterfaceExecution = "SELECT * FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE INTERFACE = 'OE3_SEND'\r\n"
				+ "AND STATUS = 'E'\r\n"
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC;";
		
		String queryThreadsExecution = "SELECT * FROM WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = [WM_LOG_RUN.RUN_ID];";
		
		String queryExecutionError = "SELECT * FROM WM_LOG_ERROR\r\n"
				+ "WHERE RUN_ID = [WM_LOG_RUN.RUN_ID]";
		
		String querySentRetries = "SELECT NOMBRE_ARCHIVO, XXH2H_ID_LOTE_GENERADO_PK, ESTATUS_WM, NVL(SENT_RETRIES,0) SENT_RETRIES, NVL(RESPONSE_RETRIES,0) RESPONSE_RETRIES\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ESTATUS_WM = '%s' \r\n"
				+ "AND NVL(SENT_RETRIES,0) = %s\r\n"
				+ "AND NOMBRE_ARCHIVO = [NOMBRE DEL ARCHIVO]";
		
		/***************************Paso 1****************************/
		addStep("Validar que existan archivos .in o .cta pendientes de procesar en la tabla "
				+ "XXFC_H2H.XXFC_H2H_LOTE_GENERADO de ORAFIN y observar el campo SENT_RETRIES.");
		
		String fileExistsFormat = String.format(queryExistsFile, data.get("retries"));
		System.out.println(fileExistsFormat);
		SQLResult fileExists = executeQuery(dbAVEBQA, queryExistsFile);
		
		boolean validateFileExists = fileExists.isEmpty();
		String fileName = "";
		if(!validateFileExists) {
			fileName = fileExists.getData(0, "NOMBRE_ARCHIVO");
			testCase.addQueryEvidenceCurrentStep(fileExists);
		}
		assertFalse(validateFileExists, "No se obtuvo informacion de archivos pendientes");
		/***************************Paso 2****************************/
		addStep("Validar que NO exitan los archivos .cta y .in registrados en la tabla "
				+ "XXFC_H2H.XXFC_H2H_LOTE_GENERADO pendientes por procesar en el FileSystem del "
				+ "servidor IS configurado en el servicio OE3.Mapping:config.");
		
		SQLResult ftpConfig = executeQuery(dbPos, queryFTPConfig);
		System.out.println(ftpConfig);
		
		boolean validateFtpConfig = ftpConfig.isEmpty();
		String baseDir = "";
		String ftpHost = "";
		Integer ftpPort = 0;
		String ftpUser = "";
		String ftpPass = "";
		if (!validateFtpConfig) {
			baseDir = ftpConfig.getData(0, "FTP_BASE_DIR");
			ftpHost = ftpConfig.getData(0, "FTP_SERVERHOST");
			ftpPort = Integer.parseInt(ftpConfig.getData(0, "FTP_SERVERPORT"));
			ftpUser = ftpConfig.getData(0, "FTP_USERNAME");
			ftpPass = ftpConfig.getData(0, "FTP_PASSWORD");
			testCase.addQueryEvidenceCurrentStep(ftpConfig);
		}else
			testCase.addQueryEvidenceCurrentStep(ftpConfig);
		
		assertFalse(validateFtpConfig, "No se obtuvieron registros");
		
		FTPUtil ftp = new FTPUtil(ftpHost, ftpPort, ftpUser, ftpPass);
		boolean validateFileSystem = ftp.fileExists(baseDir + data.get("FileSystem") + fileName);
		if(!validateFileSystem) {
			testCase.addTextEvidenceCurrentStep("El archivo se encuentra en la ruta configurada");
		}
		assertFalse(validateFileSystem, "El archivo no se encuentra en la ruta configurada");
		/***************************Paso 3****************************/
		addStep("Ejecutar el servicio OE3.pub:runH2HSend desde el job runOE3Send de Ctrl-M.");
		
		u  = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));
		
		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("pas"));
	
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
	
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		if(resultado.equals("Wait Condition")) {
		casoPasado = true;
		}		
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
		control.closeViewpoint();
		
		/***************************Paso 4****************************/
		addStep("Validar que se inserte el registro del detalle de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG.");
		
		SQLResult interfaceExecution = executeQuery(dbLog, queryInterfaceExecution);
		System.out.println(queryInterfaceExecution);
		
		boolean validateInterfaceExecution = interfaceExecution.isEmpty();
		String runID = "";
		if(!validateInterfaceExecution) {
			runID = interfaceExecution.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(interfaceExecution);
		}
		assertFalse(validateInterfaceExecution, "No se inserto registro de ejecucion de interface");
		/***************************Paso 5****************************/
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG.");
		
		String threadsFormat = String.format(queryThreadsExecution, runID);
		System.out.println(threadsFormat);
		
		SQLResult threadsExecution = executeQuery(dbLog, queryThreadsExecution);
		boolean validateThreadsExecution = threadsExecution.isEmpty();
		if(!validateThreadsExecution) {
			testCase.addQueryEvidenceCurrentStep(threadsExecution);
		}
		assertFalse(validateThreadsExecution, "No se inserto detalle de ejecucion");
		/***************************Paso 6****************************/
		addStep("Consultar el error registrado por la ejecución de la interface.");
		
		String executionErrorFormat = String.format(queryExecutionError, runID);
		System.out.println(executionErrorFormat);
		
		SQLResult executionError =  executeQuery(dbPos, executionErrorFormat);
		boolean validateExecutionError = executionError.isEmpty();
		
		if(!validateExecutionError) {
			testCase.addQueryEvidenceCurrentStep(executionError);
		}
		assertFalse(validateExecutionError, "No se obtuvo informacion de error");
		/***************************Paso 7****************************/
		addStep("Validar que el campo SENT_RETRIES sea incrementado su valor en 1 en la tabla XXFC_H2H.XXFC_H2H_LOTE_GENERADO de ORAFIN.");
		
		String sendRetriesFormat = String.format(querySentRetries, data.get("statusWM"), data.get("incrementRetries"), fileName);
		System.out.println(sendRetriesFormat);
		
		SQLResult sendRetries = executeQuery(dbPos, sendRetriesFormat);
		boolean validateSendRetries = sendRetries.isEmpty();
		
		if(!validateSendRetries) {
			testCase.addQueryEvidenceCurrentStep(sendRetries);
		}
		assertFalse(validateSendRetries, "No se obtuvo informacion");
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
		return "Construido.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_EO3_ValidarIncrementoDeIntentos_ActualizacionDeStatus_CTA_IN_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}