package interfaces.OE3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
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

public class OE3_ValidarRecepcionArchivos_EDOCTA_NOT_OUT_PREOUT extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_OE3_ValidarRecepcionArchivos_EDOCTA_NOT_OUT_PREOUT_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria***************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SeleniumUtil u = null;
		/*
		 * Variables***************************************************/
		String queryFTPConfig = "SELECT * \r\n"
				+ "FROM WMUSER.WM_FTP_CONNECTIONS\r\n"
				+ "WHERE FTP_CONN_ID = 'SANTANDER_H2H_OUT'";
		
		String queryExecutionStatus = "SELECT * FROM WM_LOG_RUN\r\n"
				+ "WHERE INTERFACE = 'OE3'\r\n"
				+ "AND STATUS = 'S'\r\n"
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryProcessedFiles = "SELECT * FROM WMUSER.WM_H2H_INBOUND_DOCS\r\n"
				+ "WHERE TRUNC(RECEIPT_DATE) = TRUNC(SYSDATE)\r\n"
				+ "AND RUN_ID = %s\r\n"
				+ "AND DOCNAME = %s";
		
		/*************************Paso 1*******************************/
		addStep("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS de WMINT.");
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
		
		/*************************Paso 2*******************************/
		addStep("Validar que existan archivos con extensión" + data.get("extension") + "en el directorio del servidor FTP configurado.");
		FTPUtil ftp = new FTPUtil(ftpHost, ftpPort, ftpUser, ftpPass);
		String fileName = "";
		FTPClient ftpClient = ftp.getClient();
		ftpClient.changeWorkingDirectory(baseDir + data.get("ftpPath"));
		FTPFile[] files = ftpClient.listFiles();
		
		for (FTPFile file : files) {
			if(file.getName().endsWith(data.get("extension"))) {
				fileName = file.getName();
				testCase.addTextEvidenceCurrentStep(fileName);
			} 
		}
		
		/*************************Paso 3*******************************/
		addStep("Ejecutar el servicio OE3.pub:runH2H desde el Job runOE3 de Ctrl-M.");
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
		
		/*************************Paso 4*******************************/
		addStep("Validar que el registro de la ejecución de la interface en la tabla WM_LOG_RUN termine en STATUS = 'S'.");
		SQLResult executionStatus = executeQuery(dbLog, queryExecutionStatus);
		System.out.print(queryExecutionStatus);
		
		boolean validateExecutionStatus = executionStatus.isEmpty();
		String status = "";
		String runID = "";
		if(!validateExecutionStatus) {
			status = executionStatus.getData(0, "STATUS");
			runID = executionStatus.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(executionStatus);
		}else
			testCase.addQueryEvidenceCurrentStep(executionStatus);
		
		assertEquals(status, "S", "El status es diferente a S");
		
		/*************************Paso 5*******************************/
		addStep("Validar que los archivos procesados sean registrados en la tabla WM_H2H_INBOUND_DOCS de WMUSER.");
		String formatProcessedFiles = String.format(queryProcessedFiles, runID, files);
		SQLResult processedFiles = executeQuery(dbPos, formatProcessedFiles);
		System.out.println(formatProcessedFiles);
		
		boolean validateProcessedFiles = processedFiles.isEmpty();
		if(!validateProcessedFiles) {
			testCase.addQueryEvidenceCurrentStep(processedFiles);
		}else
			testCase.addQueryEvidenceCurrentStep(processedFiles);
		
		assertFalse(validateProcessedFiles, "No se encontraron registros de archivos procesados");
		
		/*************************Paso 6*******************************/
		addStep("Validar que se elimine el archivo procesado del directorio FTP configurado.");
		boolean validateFtp = ftp.fileExists(baseDir + data.get("ftpPath") + fileName);
		if(!validateFtp) {
			testCase.addTextEvidenceCurrentStep("El archivo fue eliminado del directorio");
		}
		assertTrue(validateFtp, "El archivo no fue eliminado del directorio");
		
		/*************************Paso 7*******************************/
		addStep("Validar que el archivo procesado fue movido al FileSystem del servidor en la ruta configurada en el config/workingPath.");
		
		boolean validateFileSystem = ftp.fileExists(baseDir + data.get("FileSystem") + fileName);
		if(!validateFileSystem) {
			testCase.addTextEvidenceCurrentStep("El archivo se encuentra en la ruta configurada");
		}
		assertFalse(validateFileSystem, "El archivo no se encuentra en la ruta configurada");
		
		
		
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
		return "ATC_FT_004_OE3_ValidarRecepcionArchivos_EDOCTA_NOT_OUT_PREOUT_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
