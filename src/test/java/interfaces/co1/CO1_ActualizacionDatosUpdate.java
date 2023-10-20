package interfaces.co1;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class CO1_ActualizacionDatosUpdate extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_CO1_002_ActualizacionDatosUpdate_test(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbCNT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		SeleniumUtil u;
		PakageManagment pok;
		
		/**
		 * ALM
		 * Validar la actualizacion de datos en XXFC_CENTROS_RESPONSABILIDAD 
		 * de la BD ORAFIN UPDATE
		 */

		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");

		/**
		 * VARIABLES
		 *********************************************************************/

		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id, doc_name = "";
		
		//Paso 1
		String regPorActualizar = "SELECT id, retek_loc_id, cr_superior, cr_tienda,sync_status, sent_date, run_id \r\n" + 
				"FROM WMUSER.WM_SYNC_T_OPERACION \r\n" + 
				"WHERE CR_SUPERIOR = '" + data.get("plaza") + "' \r\n" + 
				"AND CR_TIENDA = '" + data.get("tienda") + "' \r\n" + 
				"AND SYNC_STATUS='F'";

		// Paso 2
		String datosORAFIN_CNT = "SELECT RETEK_CR, ORACLE_CR_SUPERIOR, ORACLE_CR, ID_CENTRO_RESPONSABILIDAD, RETEK_ASESOR_NOMBRE \r\n" + 
				"FROM WMVIEW.XXFC_CENTROS_RESPONSABILIDAD \r\n" + 
				"WHERE RETEK_CR = '%s' \r\n" + 
				"AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' \r\n" + 
				"AND ORACLE_CR = '" + data.get("tienda") + "' \r\n" + 
				"AND ORACLE_CR_TYPE='T'";

		// Paso 3 adapters

		// Paso 6
		String ValidaActualizacion = "SELECT RETEK_ASESOR, RETEK_DISTRITO, \r\n" + "RETEK_ASESOR_NOMBRE, LAST_UPDATE_DATE \r\n"
				+ "FROM XXFC.XXFC_CENTROS_RESPONSABILIDAD \r\n" 
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza")
				+ "'  AND ORACLE_CR = '" + data.get("tienda") + "' AND ORACLE_CR_TYPE = 'T'  "
				+ "AND TRUNC(LAST_UPDATE_DATE) = SYSDATE ";

		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog

		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ " FROM WMLOG.WM_LOG_RUN Tbl "
				+ "WHERE INTERFACE = 'CO1' "
				+ "AND STATUS='S'"
				+ "and  start_dt >= TRUNC(SYSDATE) "
				+ " ORDER BY START_DT DESC) where rownum <=1";
		
		// Paso 7
		String actualizacionEjecucion = "SELECT id, retek_loc_id cr_superior, cr_tienda,sync_status, sent_date, run_id "
				+ "FROM WMUSER.WM_SYNC_T_OPERACION WHERE CR_SUPERIOR = '" + data.get("plaza") + "' "
						+ "AND RUN_ID = '%s' "
						+ "AND CR_TIENDA = '" + data.get("plaza") + "' "
						+ "AND TRUNC(SENT_DATE) = SYSDATE  "
						+ "AND SYNC_STATUS = 'E'";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Que exista registros para ser actualizdos con CR_SUPERIOR sea igual a 10DCU, CR_TIENDA sea igual "
				+ "a 50UJC y con status F en la tabla WM_SYNC_T_OPERACION de la BD CNT ");

		System.out.println(regPorActualizar);
		
		SQLResult regPorActualizarRes = executeQuery(dbCNT, regPorActualizar);
		

		String retek_loc_id = regPorActualizarRes.getData(0, "RETEK_LOC_ID");
		
		System.out.println("RETEK_LOC_ID "+ retek_loc_id);

		boolean ValidaregPorActualizar = regPorActualizarRes.isEmpty();

		if (!ValidaregPorActualizar) {
			testCase.addQueryEvidenceCurrentStep(regPorActualizarRes);
		}

		System.out.println(ValidaregPorActualizar);
		assertFalse(ValidaregPorActualizar, "No se encontro registro con las tiendas correspondientes");

		/* PASO 2 *********************************************************************/
		
		addStep("Comprobar que existan registros en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN para los valores que se van a actualizar en WM_SYNC_T_OPERACION en la BD CNT ");

		String datosORAFIN_CNT_F = String.format(datosORAFIN_CNT, retek_loc_id);
		
		System.out.println(datosORAFIN_CNT_F);
		
		SQLResult datosORAFIN_CNT_Res = executeQuery(dbCNT, datosORAFIN_CNT_F);

		boolean ValidadatosORAFIN_CNT = datosORAFIN_CNT_Res.isEmpty();

		if (!ValidadatosORAFIN_CNT) {
			testCase.addQueryEvidenceCurrentStep(regPorActualizarRes);
		}

		System.out.println(ValidadatosORAFIN_CNT);
		
		assertFalse(ValidadatosORAFIN_CNT, "No se encontraron datos que coincidan para ser actualizados ");
		
		/* PASO 3 *********************************************************************/


		/*
		 * Comprobar que el adapter notification CO1.DB.CNT:adpNotUpdTOperaciones_LT este habilitado.
		 */

		/*
		 * PASO 4
		 **********************************************************************/

		/*
		 * Se realiza un update  en la tabla WM_SYNC_T_OPERACION de la BD CNT, actualizando el campo SYNC_STATUS  igual a L  y el RETEK_MGR_NAME donde los campos CR_SUPERIOR sea igual 
		 * a 10GUD, CR_TIENDA sea igual a 50ZZL y SYNC_STATUS sea igual a F.
           UPDATE WM_SYNC_T_OPERACION SET SYNC_STATUS = 'L', RETEK_MGR_NAME = 'Eduardo Mu�oz Toscano' 
           WHERE CR_SUPERIOR = '10GUD' 
           AND CR_TIENDA = '50ZZL' 
           AND SYNC_STATUS = 'F';

		 */

		/*
		 * EJECUCION DEL SERVICIO
		 *********************************************************************/

		/*addStep("Se ejecuta el proceso C01.Pub:run ");
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interface"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S		

		if (!successRun) {
			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	*/
		/* PASO 5 *********************************************************************/

		addStep("Se valida el STATUS sea igual a S en la tabla WM_LOG_RUN  de la BD WM_LOG donde INTERFACE sea igual a CO1 y END_DT sea igual a la fecha de la prueba.");
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);

		testCase.addQueryEvidenceCurrentStep(is);
		assertFalse(!validateStatus, "La ejecuci�n de la interfaz no fue exitosa");

		/* PASO 6 *********************************************************************/

		addStep("Se confirma la actualizacion de los campos RETEK_ASESOR, RETEK_DISTRITO, RETEK_ASESOR_NOMBRE en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN donde ORACLE_CR_TYPE sea igual a T, "
				+ "ORACLE_CR_SUPERIOR igual a 10GUD, ORACLE_CR igual a 50ZZL y LAST_UPDATE_DATE igual a la fecha actual");

		System.out.println(ValidaActualizacion);
		SQLResult ValidaActualizacionRes = executeQuery(dbCNT, ValidaActualizacion);

		boolean ValidaActualizacionBool = ValidaActualizacionRes.isEmpty();

		if (!ValidaActualizacionBool) {
			testCase.addQueryEvidenceCurrentStep(ValidaActualizacionRes);
		}

		System.out.println(ValidaActualizacionBool);
		assertFalse(ValidaActualizacionBool, "No se actualizaron los datos correctamente despues de la ejecucion de la interfaz.");

		/* PASO 7 *********************************************************************/

		addStep("Confirmar que se actualicen las columnas SENT_DATE igual a la fecha de la ejecuci�n, "
				+ "CR_SUPERIOR igual a 10GUD, AND RUN_ID igual a RUN_ID de la ejecucion, CR_TIENDA igual a 50ZZL Y SYNC_STATUS igual a E en la tabla WM_SYNC_T_OPERACION de la BD CNT");
		
		run_id = is.getData(0, "RUN_ID");

		String actualizacionEjecucionF = String.format(actualizacionEjecucion, run_id);
		
		System.out.println(actualizacionEjecucionF);
		
		SQLResult actualizacionEjecucionR = executeQuery(dbCNT, actualizacionEjecucionF);

		boolean ValidaactualizacionEjecucion = actualizacionEjecucionR.isEmpty();

		if (!ValidaactualizacionEjecucion) {
			testCase.addQueryEvidenceCurrentStep(actualizacionEjecucionR);
		}

		System.out.println(ValidaactualizacionEjecucion);
		assertFalse(ValidaactualizacionEjecucion, "Los campos de los registros seleccionados no fueron actualizados correctamente ");

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
		return "Esta interface permite mantener sincronizados los modulos de CNT y MCR, cada vez que un cambio de asesor sea realizado en CNT.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizaci�n";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "Validar la actualizaci�n de datos en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN ";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

