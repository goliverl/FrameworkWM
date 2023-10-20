package interfaces.tpe.copia;

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

public class ATC_FT_027_TPE_COPIA_EjecobRespaldoDepuracionDeInfoTablaTPE_FR_TRANSACTIONconError extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_027_TPE_COPIA_EjecobRespaldoDepuracionDeInfoTablaTPE_FR_TRANSACTIONconError_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: Actualización tecnológica X86 (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-027 TPE_COPIA Ejecución del job de respaldo y depuración de información de la tabla TPE_FR_TRANSACTION con estado de error
		 * @author edwin.ramirez
		 * @date 2023/Ene/12
		 */
/*
* Utilerías
*********************************************************************/

		SQLUtil dbOXTPEQA = new SQLUtil(GlobalVariables.DB_HOST_OXTPEQA,
				GlobalVariables.DB_USER_OXTPEQA, GlobalVariables.DB_PASSWORD_OXTPEQA); //Debe ser OXTPEQA_PREM
		SQLUtil dbFCWMREQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMREQA,
				GlobalVariables.DB_USER_FCWMREQA, GlobalVariables.DB_PASSWORD_FCWMREQA);


/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 2
		String consultaCantidadReg = "SELECT COUNT (*) "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION "
				+ "WHERE TRUNC(CREATION_DATE) <= '"+data.get("date")+"'";
//Paso 3
		String consultaDetalle = "SELECT * FROM(\r\n"
				+ "SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,PLAZA\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "WHERE TRUNC(CREATION_DATE) <= '"+data.get("date")+"' \r\n"
				+ "ORDER BY CREATION_DATE DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
//Paso 5
		String consultaConfig = "SELECT ID,TABLA,STATUS,TABLA_STATUS,TIPO,FECHA_COPIADA "
				+ "FROM TPEREP.TPE_TABLAS_COPIA "
				+ "WHERE TABLA = 'TPE_FR_TRANSACTION' "
				+ "AND ID = '"+data.get("id")+"'"; //ID DE UN CAMPO FIJO EDITADO PARA FALLAR
//Paso 6
		String consultaRegRespaldo = "SELECT COUNT (*) "
				+ "FROM TPEREP.TPE_FR_TRANSACTION_LOY "
				+ "WHERE TRUNC(CREATION_DATE) >= TRUNC(SYSDATE-1 - INTERVAL '5' YEAR)";
//Paso 7
		String  consultaRegRespaldo2 = "SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,PLAZA,TIENDA "
				+ "FROM TPEREP.TPE_FR_TRANSACTION_LOY "
				+ "WHERE TRUNC(CREATION_DATE) >= TRUNC(SYSDATE-1 - INTERVAL '5' YEAR) "
				+ "ORDER BY CREATION_DATE DESC";
//Paso 10
		Date fechaEjecucionInicio;
//Paso 12
		String consultaNoRespaldo = "SELECT COUNT(*) "
				+ "FROM TPEREP.TPE_FR_TRANSACTION_LOY "
				+ "WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE-1)";
//Paso 14
		String consultaTotalReg = "SELECT COUNT (*) "
				+ "FROM TPEREP.TPE_FR_TRANSACTION_LOY "
				+ "WHERE TRUNC(CREATION_DATE) >= TRUNC(SYSDATE-1 - INTERVAL '5' YEAR) "
				+ "ORDER BY CREATION_DATE DESC";
//Paso 15 
		String consultaBitacora = "SELECT * FROM("
				+ "SELECT RUN_ID,STATUS,FECHAINI,FECHAFIN,FECHAPROCESADA,TABLA \r\n"
				+ "FROM TPEREP.TPE_CONTROL_COPIA \r\n"
				+ "ORDER BY FECHAINI DESC"
				+ ")WHERE ROWNUM <= 10";

//Paso
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//***************************************** Paso 1 y 2***************************************************************** 
		
		addStep("Ejecutar la siguiente consulta en la BD \"OXTPEQA\" para validar la cantidad de registros que se van a respaldar:");
		System.out.println("Paso 1 y 2: "+GlobalVariables.DB_HOST_OXTPEQA); //Debe ser OXTPEQA_PREM
		System.out.println(consultaCantidadReg);
		
		SQLResult exe_consultaCantidadReg = executeQuery(dbOXTPEQA, consultaCantidadReg); //Debe ser OXTPEQA_PREM
		String totalFuente = exe_consultaCantidadReg.getData(0, "COUNT(*)");
				
		boolean validaConsultaCantidadReg = exe_consultaCantidadReg.isEmpty();
		
		if (!validaConsultaCantidadReg) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaCantidadReg);
		} 
		
		System.out.println(validaConsultaCantidadReg);
		assertFalse(validaConsultaCantidadReg, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 3 ***************************************************************** 
		addStep("Ejecutar la siguiente consulta en la BD \"OXTPEQA\" para ver el detalle de los registros que se van a respaldar");
		System.out.println("Paso 3 "+GlobalVariables.DB_HOST_OXTPEQA); //Debe ser OXTPEQA_PREM
		System.out.println(consultaDetalle);
		
		SQLResult exe_consultaDetalle = executeQuery(dbOXTPEQA, consultaDetalle); //Debe ser OXTPEQA_PREM
		
		boolean validaConsultaDetalle = exe_consultaDetalle.isEmpty();
		
		if (!validaConsultaDetalle) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaDetalle);
		} 
		
		System.out.println(validaConsultaDetalle);
		assertFalse(validaConsultaDetalle, "No existe información pendiente de procesar en la tabla.");
	
//***************************************** Paso 4 y 5 ***************************************************************** 
		addStep("Ejecutar la siguiente consulta en la BD \"FCWMREQA\" para ver la configuración para la copia de la tabla:");
		System.out.println("Paso 4 y 5 "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaConfig);
		
		SQLResult exe_consultaConfig = executeQuery(dbFCWMREQA, consultaConfig); 
		
		boolean validaConsultaConfig = exe_consultaConfig.isEmpty();
		
		if (!validaConsultaConfig) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaConfig);
		} 
		
		System.out.println(validaConsultaConfig);
		assertFalse(validaConsultaConfig, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 6 ***************************************************************** 
		addStep("Ejecutar la siguiente consulta en la BD \"FCWMREQA\" para consultar el total "
				+ "de registros existentes que se encuentren dentro del rango de respaldo de 5 años:");
		System.out.println("Paso 6 "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaRegRespaldo);
		
		SQLResult exe_consultaRegRespaldo = executeQuery(dbFCWMREQA, consultaRegRespaldo);
		String totalReg1 = "";
		
		boolean validaConsultaRegRespaldo = exe_consultaRegRespaldo.isEmpty();
		
		if (!validaConsultaRegRespaldo) {
			totalReg1 = exe_consultaRegRespaldo.getData(0, "COUNT(*)");
			testCase.addQueryEvidenceCurrentStep(exe_consultaRegRespaldo);
		} 
		
		System.out.println(validaConsultaRegRespaldo);
		assertFalse(validaConsultaRegRespaldo, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 7 ***************************************************************** 
		addStep("Ejecutar la siguiente consulta en la BD \"FCWMREQA\" para ver el detalle de los registros "
				+ "con los que cuenta el repositorio histórico dentro del rango de respaldo de 5 años:");
		System.out.println("Paso 7 "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaRegRespaldo2);
		
		SQLResult exe_consultaRegRespaldo2 = executeQuery(dbFCWMREQA, consultaRegRespaldo2); 
		
		boolean validaConsultaRegRespaldo2 = exe_consultaRegRespaldo2.isEmpty();
		
		if (!validaConsultaRegRespaldo2) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaRegRespaldo2);
		} 
		
		System.out.println(validaConsultaRegRespaldo2);
		assertFalse(validaConsultaRegRespaldo2, "No existe información pendiente de procesar en la tabla.");

//***************************************** Paso 8 ***************************************************************** 
		/**
		 * "NO APLICA
		 *  Las validaciones para el flujo de Depuración quedan fuera del alcance de las pruebas de QA 
		 *  debido a que actualmente en Producción existe un error con este proceso, y para estas 
		 *  pruebas de QA se anuló el código de la depuración."
		 */	
//***************************************** Paso 9, 10 y 11 *****************************************************************		
		/*addStep("Ejecución control-M");

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
		*/
//***************************************** Paso 12 *****************************************************************	
		addStep("Ejecutar la siguiente consulta en la BD \"FCWMREQA\" para validar que no se respaldó "
				+ "la información en el repositorio histórico debido a que se modificó la conexión de base de datos:");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaNoRespaldo);
		
		SQLResult exe_consultaNoRespaldo = executeQuery(dbFCWMREQA, consultaNoRespaldo); 
		//String totalDestino = exe_consultaNoRespaldo.getData(0, "COUNT(*)");
		
		boolean validaConsultaNoRespaldo = exe_consultaNoRespaldo.isEmpty();
		
		if (!validaConsultaNoRespaldo) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaNoRespaldo);
		} 
		
		System.out.println(validaConsultaNoRespaldo);
		assertFalse(validaConsultaNoRespaldo, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 13 ***************************************************************** 
		/**
		 * "NO APLICA Las validaciones para el flujo de Depuración quedan fuera del
		 * alcance de las pruebas de QA debido a que actualmente en Producción existe un
		 * error con este proceso, y para estas pruebas de QA se anuló el código de la
		 * depuración."
		 */
//***************************************** Paso 14 *****************************************************************
		addStep("Ejecutar la siguiente consulta para obtener el detalle de los registros existentes "
				+ "(Y que se encuentren dentro del rango de respaldo de 5 años) en la "
				+ "tabla TPEREP.TPE_FR_TRANSACTION_LOY de la BD FCWMREQA:");
		System.out.println("Paso 14 "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaTotalReg);
		
		SQLResult exe_consultaTotalReg = executeQuery(dbFCWMREQA, consultaTotalReg); 
		String totalReg2 = "";
		
		boolean validaConsultaTotalReg = exe_consultaTotalReg.isEmpty();
		
		if (!validaConsultaTotalReg) {
			totalReg2 = exe_consultaTotalReg.getData(0, "COUNT(*)");
			testCase.addQueryEvidenceCurrentStep(exe_consultaTotalReg);
		} 
		
		System.out.println(validaConsultaTotalReg);
		assertEquals(totalReg2, totalReg1);
	
//***************************************** Paso 15*****************************************************************
		addStep("Ejecutar la siguiente consulta en la BD \"FCWMREQA\" para validar la bitácora del respaldo de la información:");
		System.out.println("Paso 15 "+GlobalVariables.DB_HOST_FCWMREQA); 
		System.out.println(consultaBitacora);
		
		SQLResult exe_consultaBitacora = executeQuery(dbFCWMREQA, consultaBitacora); 
		String statusE = "";
		
		boolean validaConsultaBitacora = exe_consultaBitacora.isEmpty();
		
		if (!validaConsultaBitacora) {
			statusE = exe_consultaBitacora.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(exe_consultaBitacora);
		} 
		
		System.out.println(validaConsultaBitacora);
		assertEquals(statusE, "E");
	

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresión para comprobar la no afectación en la funcionalidad de Ejecución del "
				+ "job de respaldo y depuración de información de la tabla TPE_FR_TRANSACTION con "
				+ "estado de error  del proceso TPE_COPIA al ser migrado de webmethods v10.5 a "
				+ "webmethods 10.11 y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86). ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_027_TPE_COPIA_EjecobRespaldoDepuracionDeInfoTablaTPE_FR_TRANSACTIONconError_test";
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
