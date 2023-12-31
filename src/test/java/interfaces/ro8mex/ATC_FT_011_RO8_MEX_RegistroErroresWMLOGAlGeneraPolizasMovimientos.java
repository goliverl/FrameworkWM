package interfaces.ro8mex;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONArray;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import java.util.Date;
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

public class ATC_FT_011_RO8_MEX_RegistroErroresWMLOGAlGeneraPolizasMovimientos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_RO8_MEX_RegistroErroresWMLOGAlGeneraPolizasMovimientos_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: BackOffice Oracle
		 * Caso de prueba: MTS-FT-011-C1 RO8_MX Registro de errores en WMLOG al intentar 
		 * genera polizas de movimientos por medio de la interface RO8_MX
		 * @author edwin.ramirez
		 * @date 2022/Dic/15
		 */

		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, 
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 3
		String consultaPlazas = "SELECT INTERFACE_NAME,AUTO_STATUS,MANUAL_STATUS,ATTRIBUTE1,ATTRIBUTE2 "
				+ "FROM WMUSER.WM_CFG_LAUNCHER "
				+ "WHERE INTERFACE_NAME LIKE 'RO8_MX%' "
				+ "AND ATTRIBUTE2 IN (1,20,11,32) "
				+ "AND ATTRIBUTE1='10AGC' AND ROWNUM <= 10";
//Paso 5
		String consultaRegistros = "SELECT * FROM("
				+ "SELECT ITEM, ITEM_DESC,DIVISION,GROUPS,DEPT,CLASS,TRAN_DATE,ID FROM FEM_FIF_STG "
				+ "WHERE REFERENCE_3 IS NULL AND REFERENCE_9 IS NULL AND CR_PLAZA='10AGC' AND TRAN_DATE >= SYSDATE-30)"
				+ "WHERE ROWNUM <= 10";
//Paso 5
		Date fechaEjecucionInicio;
//Paso 8
		String registrosItem = "SELECT * FROM("
				+ "SELECT ITEM, ITEM_DESC,DIVISION,GROUPS,DEPT,CLASS,TRAN_DATE,ID "
				+ "FROM FEM_FIF_STG "
				+ "WHERE REFERENCE_3 IS NULL "
				+ "AND REFERENCE_9 IS NULL AND TRAN_DATE >= TRUNC(SYSDATE-30) ORDER BY TRAN_DATE DESC)"
				+ "WHERE ROWNUM <= 10"; 
//Paso 10
		String detalleEjecucion = "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE 'RO8%' "
				+ "ORDER BY END_DT DESC";
//Paso 12
		String consultaThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WM_LOG_THREAD "
				+ "WHERE NAME LIKE 'RO8%' AND PARENT_ID = '%s'";
//Paso 13
		String consultaErrores = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
				+ "FROM WM_LOG_ERROR "
				+ "WHERE RUN_ID = '%s'";

/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar las plazas y trancode a procesar, ejecutando la siguiente consulta en **FCWM6QA**");
		System.out.println("Paso 3: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaPlazas);
		
		SQLResult exe_consultaPlaza = executeQuery(dbFCWM6QA_NUEVA, consultaPlazas);
		
		boolean validaConsultaPlaza = exe_consultaPlaza.isEmpty();
		
		if (!validaConsultaPlaza) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaPlaza);
		} 
		
		System.out.println(validaConsultaPlaza);
		assertFalse(validaConsultaPlaza, "No existe informaci�n pendiente de procesar en la tabla."); //AQUI DEBE SER asserFalse
		
		
//***************************************** Paso 4 y 5 ***************************************************************** 
		
		addStep("Consultar la tabla FEM_FIF_STG de BD **FCRMSQA**, para identificar los registros a procesar con Tran_Code 1 y 4.");
		System.out.println("Paso 4 y 5: "+GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(consultaRegistros);
		
		SQLResult exe_consultaRegistros = executeQuery(dbFCRMSQA,consultaRegistros);
		String idRegistro = "";
		
		
		boolean validaConsultaRegistros = exe_consultaRegistros.isEmpty();
		
		if(!validaConsultaRegistros) {
			idRegistro = exe_consultaRegistros.getData(0, "ID");
			System.out.println("ID: "+idRegistro);
			testCase.addQueryEvidenceCurrentStep(exe_consultaRegistros);
		}
		
		System.out.println(validaConsultaRegistros);
		assertFalse(validaConsultaRegistros, "No existe informaci�n pendiente de procesar en la tabla."); //Es FALSE
		
		
//***************************************** Paso 6 y 7 *****************************************************************
		
		addStep("Ejecuci�n control-M");

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
		
		
//***************************************** Paso 8 *****************************************************************
		
		addStep("Consultar los registros que no hayan sido procesados de la FEM_FIF_STG de la BD **FCRMSQA**, se hayan informado los campos REFERENCE_3 y REFERENCE_9, est�n diferente de NULL y mayor a 1");	
		System.out.println("Paso 8: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String registrosItemFormat = String.format(registrosItem, idRegistro);
		System.out.println(registrosItemFormat);
		
		SQLResult exe_registrosItem = executeQuery(dbFCRMSQA,registrosItemFormat);

		boolean validaRegistrosItem = exe_registrosItem.isEmpty();
		
		if(!validaRegistrosItem) {
			testCase.addQueryEvidenceCurrentStep(exe_registrosItem);
		}
		
		System.out.println(validaRegistrosItem);
		assertFalse(validaRegistrosItem, "No hay registros en la tabla");
		
//***************************************** Paso 9 y 10 *****************************************************************
		
		addStep("Validar el registro de la ejecuci�n de la interface RO8_MEX en la tabla WM WM_LOG_RUN de la BD **FCWML6QA**.");
		System.out.println("Paso 9 y 10: "+GlobalVariables.DB_HOST_FCWMLTAEQA);
		System.out.println(detalleEjecucion);
		
		SQLResult exe_detalleEjecucion = executeQuery(dbFCWMLQA_WMLOG,detalleEjecucion);
		String status2 = exe_detalleEjecucion.getData(0, "STATUS");
		String runID = exe_detalleEjecucion.getData(0, "RUN_ID");
		System.out.println("El status es: "+status2);
		
		boolean validaDetalleEjecucion = exe_detalleEjecucion.isEmpty();
		
		if(!validaDetalleEjecucion) {
			testCase.addQueryEvidenceCurrentStep(exe_detalleEjecucion);
		}
		
		System.out.println(validaDetalleEjecucion);
		assertEquals(status2, "E");
		
//***************************************** Paso 11 *****************************************************************
		
		addStep("Validar que se hayan registrado los Threads lanzados por la interface RO8_MEX en la tabla WM_LOG_THREAD de la BD **FCWML6QA.**");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaThreadFormat = String.format(consultaThread,runID);
		System.out.println(consultaThreadFormat);
				
		SQLResult exe_consultaThread = executeQuery(dbFCWMLQA_WMLOG, consultaThreadFormat);
		String status3 = exe_consultaThread.getData(0, "STATUS");
		System.out.println("El status es: "+status3);
		
		boolean validaConsultaThread = exe_consultaThread.isEmpty();
				
		if (!validaConsultaThread) {
					
			testCase.addQueryEvidenceCurrentStep(exe_consultaThread);
			} 
				
		System.out.println(validaConsultaThread);
		assertEquals(status3, "S");
//***************************************** Paso 13 *****************************************************************
		
		addStep("Realizar la siguiente consulta para verificar que registre los errores  en la ejecuci�n de la interfaz RO8_MEX en la tabla WM_LOG_ERROR de BD FCWMLQA. ");
		System.out.println("Paso 13 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaErroresFormat = String.format(consultaErrores,runID);
		System.out.println(consultaErroresFormat);
		
		SQLResult exe_consultaErrores = executeQuery(dbFCWMLQA_WMLOG, consultaErroresFormat);
		
		boolean validaConsultaErrores = exe_consultaErrores.isEmpty();
		
		if (!validaConsultaErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaErrores);
			} 
		
		System.out.println(validaConsultaErrores);
		assertFalse(validaConsultaErrores, "No existen errores en la tabla");
	
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "El objetivo de la interfaz es registrar contablemente en el modulo de Oracle General Ledger todas las transacciones que son generadas en el punto de  venta (OXXO)  y enviadas a Retek";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
