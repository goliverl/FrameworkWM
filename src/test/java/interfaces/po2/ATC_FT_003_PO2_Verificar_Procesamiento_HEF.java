package interfaces.po2;

import static org.junit.Assert.assertFalse;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;


public class ATC_FT_003_PO2_Verificar_Procesamiento_HEF extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO2_Verificar_Procesamiento_HEF_test(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA , GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit,GlobalVariables.DB_PASSWORD_fcwmesit);

 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1
		
		String validaRegistros = "SELECT * FROM(SELECT SUBSTR(PV_DOC_NAME,4,5) AS PLAZA,\r\n" + 
				"SUBSTR(PV_DOC_NAME,9,5) AS TIENDA,\r\n" + 
				"TO_CHAR(C.PV_DATE,'YYYYMMDD') PV_DATE, \r\n" + 
				"COUNT(*) AS RECORDS,\r\n" + 
				"A.ID\r\n" + 
				"FROM POSUSER.POS_INBOUND_DOCS A, \r\n" + 
				"POSUSER.PLAZAS B, \r\n" + 
				"POSUSER.POS_HEF C  \r\n" + 
				"WHERE A.DOC_TYPE='HEF'\r\n" + 
				"AND A.STATUS='I'\r\n" + 
				"AND SUBSTR(A.PV_DOC_NAME,4,5)=B.CR_PLAZA AND B.PAIS='MEX'\r\n" + 
				"AND A.ID = C.PID_ID \r\n" + 
				"GROUP BY SUBSTR(A.PV_DOC_NAME,4,5), SUBSTR(PV_DOC_NAME,9,5), TO_CHAR(C.PV_DATE,'YYYYMMDD'), A.ID\r\n" + 
				"ORDER BY RECORDS DESC)WHERE rownum <= 1";
		
		
		//Paso 2 
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server  \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PO2'  \r\n" + 
				"ORDER BY START_DT DESC) where rownum <=1";
		
	
		
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		//Paso 3
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " 
		+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'"
				+ "ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID='%s' "
						+ "ORDER BY THREAD_ID DESC ";// Consulta para los Threads
		
		//Paso 4
		
		String RegistrosProcesados = "SELECT A.id, A.pe_id, pv_doc_id, A.status, A.pv_doc_name, A.received_date, A.target_id \r\n" + 
				"FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B\r\n" + 
				"WHERE A.DOC_TYPE='HEF'\r\n" + 
				"AND A.STATUS = 'E'\r\n"
				+ "AND A.ID = '%s'" + 
				"AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA\r\n" + 
				"AND B.PAIS= 'MEX'\r\n" + 
				"AND TRUNC(A.PARTITION_DATE) >= TRUNC(SYSDATE-7)\r\n" + 
				"ORDER BY PV_DOC_NAME"; //revisar el parametro del id de poliza que es el target_id
		
		//Paso 5
		String finanzas = "SELECT REFERENCE6 POLIZA,STATUS,USER_JE_CATEGORY_NAME,GROUP_ID, SEGMENT4 NUM_CTA_GRAL,\r\n" + 
				"SEGMENT1||'.'||SEGMENT2||'.'||SEGMENT3||'.'||SEGMENT4||'.'||SEGMENT5||'.'||SEGMENT7 AS CUENTA_DETALLE,\r\n" + 
				"ENTERED_CR, ENTERED_DR, REFERENCE10,DATE_CREATED,CURRENCY_CODE,CURRENCY_CONVERSION_DATE\r\n" + 
				"FROM GL_INTERFACE\r\n" + 
				"WHERE REFERENCE6  = '%s'\r\n" + 
				"ORDER BY DATE_CREATED DESC ";
		
		
		
//Pasos ***********************************************************************************************************************************************************************		
		
		// Paso 1

		addStep("Ejecutar el siguiente query para validar si hay registros a procesar.",
				"Se visualizara los registros pendientes a procesar por la interfaz ");

		// Primera consulta

		System.out.println("Paso 1: \n" + validaRegistros);

		SQLResult validaRegistros_Result = dbPos.executeQuery(validaRegistros);

		String id = validaRegistros_Result.getData(0, "ID");

		System.out.println("ID = " + id);

		boolean validaRegistros_b = validaRegistros_Result.isEmpty(); // checa que el string contenga datos

		if (!validaRegistros_b) {

			testCase.addQueryEvidenceCurrentStep(validaRegistros_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validaRegistros_b);

		assertFalse("No se encontraron registros a procesar ", validaRegistros_b); // Si esta vacio, imprime mensaje

		// ***************************************************************************************************************************************************************
		// Paso 2

		addStep("Se ejecuta el proceso PO2.Pub:run.");
		System.out.println("Paso 2: \n");

		// Utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S"; // status exitoso

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM2(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		System.out.println("RUN_ID = " + run_id);
		String status1 = is.getData(0, "STATUS");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		if (successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//***************************************************************************************************************************************************************************************

		// Paso 3

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'PO2' y STATUS es igual a 'S'.");
		System.out.println("Paso 3: \n");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		if (av2 == false) {

			testCase.addQueryEvidenceCurrentStep(is);

		} else {

			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);

//***************************************************************************************************************************************************************************************

		addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");

		String threads = String.format(consultaThreads, run_id);

		System.out.println("CONSULTA THREAD " + threads);

		SQLResult threadsResult = dbLog.executeQuery(threads);

		boolean av31 = threadsResult.isEmpty();
		if (av31 == false) {

			testCase.addQueryEvidenceCurrentStep(threadsResult);

		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult);
		}
		System.out.println("El registro en WM_LOG_THREAD esta vacio 1- " + av31);

		// .-----------Segunda consulta

		String threads2 = String.format(consultaThreads2, run_id);

		SQLResult threadsResult2 = dbLog.executeQuery(threads2);

		boolean av3111 = threadsResult2.isEmpty();
		if (av3111 == false) {

			testCase.addQueryEvidenceCurrentStep(threadsResult2);

		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult2);
		}
		System.out.println("El registro en WM_LOG_THREAD esta vacio 2- " + av31);
		assertFalse("No se generaron threads en la tabla", av3111);

//************************************************************************************************************************************************************************************			

		// Paso 4

		addStep("Ejecutar el siguiente query para validar los archivos procesados y el ID  de la poliza");
		System.out.println("Paso 4: \n");

		String RegistrosProcesadosForm = String.format(RegistrosProcesados, id);

		System.out.println(RegistrosProcesadosForm);

		SQLResult RegistrosProcesados_Result = dbPos.executeQuery(RegistrosProcesadosForm);

		String TARGET_ID = RegistrosProcesados_Result.getData(0, "TARGET_ID");

		boolean RegistrosProcesados_valida = RegistrosProcesados_Result.isEmpty(); // checa que el string contenga datos

		if (!RegistrosProcesados_valida) {

			testCase.addQueryEvidenceCurrentStep(RegistrosProcesados_Result); // Si no esta vacio, lo agrega a la
																				// evidencia
		}

		System.out.println(RegistrosProcesados_valida);

		assertFalse("No se procesaron los archivos ", RegistrosProcesados_valida); // Si esta vacio, imprime mensaje

//**************************************************************************************************************************************************************************************

		// Paso 5

		addStep("Ejecutar el siguiente Query  utilizando el numero de poliza para validar que se hayan insertados los datos en finanzas.");
		System.out.println("Paso 5: \n");

		String finanzasFormat = String.format(finanzas, TARGET_ID);

		System.out.println(finanzasFormat);

		SQLResult finanzasResult = dbEbs.executeQuery(finanzasFormat);

		boolean finanzasValida = finanzasResult.isEmpty(); // checa que el string contenga datos

		if (!finanzasValida) {

			testCase.addQueryEvidenceCurrentStep(finanzasResult); // Si no esta vacio, lo agrega a la evidencia
		}

		assertFalse("No se encontraron registros en fianzas ", finanzasValida); // Si esta vacio, imprime mensaje

	}  
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar procesamiento de archivo HEF por interfaz PO2 (validar cuentas)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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



