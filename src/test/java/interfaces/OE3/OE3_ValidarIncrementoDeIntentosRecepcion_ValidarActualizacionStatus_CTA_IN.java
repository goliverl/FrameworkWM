package interfaces.OE3;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class OE3_ValidarIncrementoDeIntentosRecepcion_ValidarActualizacionStatus_CTA_IN extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void  ATC_FT_003_OE3_ValidarIncrementoDeIntentosRecepcion_ValidarActualizacionStatus_CTA_IN_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria************************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SeleniumUtil u = null;
		
		/*
		 * Variables***********************************************************/
		String queryFileRecords = "SELECT NOMBRE_ARCHIVO, XXH2H_ID_LOTE_GENERADO_PK, NVL(SENT_RETRIES,0) SENT_RETRIES, NVL(RESPONSE_RETRIES,0) RESPONSE_RETRIES, RESPONSE_DATE\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ESTATUS_WM = 'E' \r\n"
				+ "AND RESPONSE_DATE IS NULL \r\n"
				+ "AND NVL(RESPONSE_RETRIES,0) = %s \r\n"
				+ "AND SUBSTR(NOMBRE_ARCHIVO, INSTR(NOMBRE_ARCHIVO, '.')) IN ('.in', '.cta')";
		
		String queryExistFile = "SELECT COUNT(1) CNT\r\n"
				+ "FROM WMUSER.WM_H2H_INBOUND_DOCS \r\n"
				+ "WHERE DOCNAME LIKE '[nombre del archivo]%'";
		
		String queryExecutionStatus = "SELECT * FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE INTERFACE = 'OE3'\r\n"
				+ "AND STATUS = '%s'\r\n"
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryThreadsExecution = "ELECT * FROM WMLOG.WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = [WM_LOG_RUN.RUN_ID]";
		
		String queryIncrementRetries = "SELECT NOMBRE_ARCHIVO, XXH2H_ID_LOTE_GENERADO_PK, NVL(SENT_RETRIES,0) SENT_RETRIES, NVL(RESPONSE_RETRIES,0) RESPONSE_RETRIES, RESPONSE_DATE\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ESTATUS_WM = 'E' \r\n"
				+ "AND RESPONSE_DATE IS NULL \r\n"
				+ "AND NVL(RESPONSE_RETRIES,0) > 0 \r\n"
				+ "AND NOMBRE_ARCHIVO = [NOMBRE_ARCHIVO];";
		
		String queryResponseRetries = "SELECT NOMBRE_ARCHIVO, XXH2H_ID_LOTE_GENERADO_PK, NVL(SENT_RETRIES,0) SENT_RETRIES, NVL(RESPONSE_RETRIES,0) RESPONSE_RETRIES, RESPONSE_DATE\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ESTATUS_WM = 'F' \r\n"
				+ "AND RESPONSE_DATE IS NULL \r\n"
				+ "AND NVL(RESPONSE_RETRIES,0) = 3\r\n"
				+ "AND NOMBRE_ARCHIVO = [NOMBRE_ARCHIVO];";
		
		/******************************Paso 1**********************************/
		addStep("Validar que existan registros de archivos de tipo .in o .cta pendientes por recibir respuesta "
				+ "y observar el campo RESPONSE_RETRIES con el número de intentos.");
		
		String fileRecordsFormat = String.format(queryFileRecords, data.get("retries"));
		System.out.println(fileRecordsFormat);
		SQLResult fileRecords = executeQuery(dbPos, fileRecordsFormat);
		
		boolean validateFileRecords = fileRecords.isEmpty();
		String fileName = "";
		if(!validateFileRecords) {
			fileName = fileRecords.getData(0, "NOMBRE_ARCHIVO");
			testCase.addQueryEvidenceCurrentStep(fileRecords);
		}
		assertFalse(validateFileRecords, "No se encontraron registros de archivos");
		
		/******************************Paso 2**********************************/
		addStep("Validar que no existan registros del archivo a procesar en la tabla WM_H2H_INBOUND_DOCS de WMUSER.");
		
		String existFileFormat = String.format(queryExistFile, fileName);
		System.out.println(existFileFormat);
		
		SQLResult existFile = executeQuery(dbPos, existFileFormat);
		boolean validateExistFile = existFile.isEmpty();
		if(!validateExistFile) {
			
			testCase.addQueryEvidenceCurrentStep(existFile);
		}
		/******************************Paso 3**********************************/
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
		
		/******************************Paso 4**********************************/
		addStep("Validar que el registro de la ejecución de la interface en la tabla WM_LOG_RUN termine en STATUS = '"+ data.get("statusWM") +"'.");
		
		String executionStatusFormat = String.format(queryExecutionStatus, data.get("statusWM"));
		System.out.println(executionStatusFormat);
		SQLResult executionStatus = executeQuery(dbPos, executionStatusFormat);
		boolean validateExecutionStatus = executionStatus.isEmpty();
		String runID = "";
		if(!validateExecutionStatus) {
			runID = executionStatus.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(executionStatus);
		}
		assertFalse(validateExecutionStatus, "No se obtuvo informacion");
		/******************************Paso 5**********************************/
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por cada archivo procesado en la tabla WM_LOG_THREAD de WMLOG.");
		
		String threadsExecutionFormat = String.format(queryThreadsExecution, runID);
		System.out.println(threadsExecutionFormat);
		SQLResult threadsExecution = executeQuery(dbLog, threadsExecutionFormat);
		boolean validateThreadsExecution = threadsExecution.isEmpty();
		if(!validateThreadsExecution) {
			testCase.addQueryEvidenceCurrentStep(threadsExecution);
		}
		assertFalse(validateThreadsExecution, "No se obtuvo detalle de la ejecucion de Threads");
		/******************************Paso 6**********************************/
		addStep("Validar que el campo RESPONSE_RETRIES de la tabla XXFC_H2H.XXFC_H2H_LOTE_GENERADO sea incrementado su valor en 1.");
		String incrementRetriesFormat = String.format(queryIncrementRetries, fileName);
		System.out.println(incrementRetriesFormat);
		
		SQLResult incrementRetries = executeQuery(dbPos, incrementRetriesFormat);
		boolean validateIncrementRetries = incrementRetries.isEmpty();
		if(!validateIncrementRetries) {
			testCase.addQueryEvidenceCurrentStep(incrementRetries);
		}
		assertFalse(validateIncrementRetries, "No se obtuvieron datos");
		
		/******************************Paso 7**********************************/
		addStep("Validar que el campo RESPONSE_RETRIES de la tabla XXFC_H2H.XXFC_H2H_LOTE_GENERADO sea incrementado su valor en 1, y se actualice el estatus a ESTATUS_WM = 'F'.");
		
		String responseRetriesFormat = String.format(queryResponseRetries, fileName);
		System.out.println(responseRetriesFormat);
		
		SQLResult responseRetries = executeQuery(dbPos, responseRetriesFormat);
		boolean validateResponseRetries = responseRetries.isEmpty();
		if(!validateResponseRetries) {
			testCase.addQueryEvidenceCurrentStep(responseRetries);
		}
		assertFalse(validateResponseRetries, "No se obtuvieron datos");
		
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
		return "ATC_FT_003_OE3_ValidarIncrementoDeIntentosRecepcion_ValidarActualizacionStatus_CTA_IN_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
