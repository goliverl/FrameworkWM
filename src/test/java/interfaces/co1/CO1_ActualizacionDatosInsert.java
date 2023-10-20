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

public class CO1_ActualizacionDatosInsert extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_CO1_001_ActualizacionDatosInsert_test(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbCNT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		SeleniumUtil u;
		PakageManagment pok;

		/**
		 * ALM
		 * Validar la actualizacion de datos en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN 
		 */

		/**
		 * VARIABLES
		 *********************************************************************/

		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id, doc_name = "";

		// Paso 1
		String Validainfo = "SELECT RETEK_ASESOR, RETEK_DISTRITO, \r\n" + "RETEK_ASESOR_NOMBRE, LAST_UPDATE_DATE \r\n"
				+ "FROM XXFC.XXFC_CENTROS_RESPONSABILIDAD \r\n" + "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza")
				+ "'  AND ORACLE_CR = '" + data.get("tienda") + "' AND ORACLE_CR_TYPE = 'T'  "
				+ "AND TRUNC(LAST_UPDATE_DATE) = SYSDATE ";

		// Paso 2 adapters

		// Paso 5
		String Validainfo2 = "SELECT RETEK_ASESOR, RETEK_DISTRITO, \r\n" + "RETEK_ASESOR_NOMBRE, LAST_UPDATE_DATE \r\n"
				+ "FROM XXFC.XXFC_CENTROS_RESPONSABILIDAD \r\n" + "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza")
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

		addStep("Que exista registro en la tabla XXFC_CENTROS_RESPONSABILIDAD "
				+ "de la BD ORAFIN  para los valores que se van a insertar posteriormente en WM_SYNC_T_OPERACION en la BD CNT. ");

		System.out.println(Validainfo);
		SQLResult ValidainfoRes = executeQuery(dbCNT, Validainfo);

		boolean ValidainfoBool = ValidainfoRes.isEmpty();

		if (!ValidainfoBool) {
			testCase.addQueryEvidenceCurrentStep(ValidainfoRes);
		}

		System.out.println(ValidainfoBool);
		assertFalse(ValidainfoBool, "No se encontro registro con las tiendas correspondientes");

		/* PASO 2 *********************************************************************/

		/*
		 * Comprobar que el adapter notification CO1.DB.CNT:adpNotInsUpdTOperaciones
		 * este habilitado.
		 */

		/*
		 * PASO 3
		 **********************************************************************/

		/*
		 * Se inserta un registro en la tabla WM_SYNC_T_OPERACION de la BD CNT.
		 * 
		 * INSERT INTO WM_SYNC_T_OPERACION (ID, RETEK_LOC_ID, CR_SUPERIOR, CR_TIENDA,
		 * ID_TIENDA, RETEK_REGION, RETEK_DISTRICT, RETEK_MGR_NAME, SYNC_STATUS,
		 * CREATED_DATE, SENT_DATE, RUN_ID) VALUES ('99273', '21556', '10PBA', '50IEL',
		 * '9143', '101', '29', 'Daniel Castillo Reyes', 'L', '08/09/17', NULL, NULL);
		 */

		/*
		 * EJECUCION DEL SERVICIO
		 *********************************************************************/

		/*addStep("Se ejecuta el proceso CP01.Pub:run ");
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
		/* PASO 4 *********************************************************************/

		addStep("Se valida el STATUS sea igual a 'S' en la tabla WM_LOG_RUN  de la BD WM_LOG donde INTERFACE sea igual a CO1 y END_DT sea igual a la fecha de la prueba.");
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);

		testCase.addQueryEvidenceCurrentStep(is);
		assertFalse(!validateStatus, "La ejecuci�n de la interfaz no fue exitosa");

		/* PASO 5 *********************************************************************/

		addStep("Se confirma la actualizacion de los campos RETEK_ASESOR, RETEK_DISTRITO, RETEK_ASESOR_NOMBRE "
				+ "en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN donde ORACLE_CR_TYPE sea igual a T, ORACLE_CR_SUPERIOR igual a 10PBA, ORACLE_CR igual a 50IEL y LAST_UPDATE_DATE igual a la fecha actual.");

		System.out.println(Validainfo2);
		SQLResult ValidainfoRes2 = executeQuery(dbCNT, Validainfo);

		boolean ValidainfoBool2 = ValidainfoRes.isEmpty();

		if (!ValidainfoBool2) {
			testCase.addQueryEvidenceCurrentStep(ValidainfoRes2);
		}

		System.out.println(ValidainfoBool2);
		assertFalse(ValidainfoBool2, "No se encontro registro con las tiendas correspondientes");

		/* PASO 6 *********************************************************************/

		addStep("Confirmar que se actualicen las columnas SENT_DATE igual a la fecha de la ejecuci�n, CR_SUPERIOR igual a 10PBA, AND RUN_ID igual a RUN_ID de la ejecucion, "
				+ "CR_TIENDA igual a 50IEL Y SYNC_STATUS igual a E en la tabla WM_SYNC_T_OPERACION de la BD CNT");
		
		run_id = is.getData(0, "RUN_ID");

		String actualizacionEjecucionF = String.format(actualizacionEjecucion, run_id);
		
		System.out.println(actualizacionEjecucionF);
		
		SQLResult actualizacionEjecucionR = executeQuery(dbCNT, actualizacionEjecucionF);

		boolean ValidaactualizacionEjecucion = actualizacionEjecucionR.isEmpty();

		if (!ValidaactualizacionEjecucion) {
			testCase.addQueryEvidenceCurrentStep(actualizacionEjecucionR);
		}

		System.out.println(ValidaactualizacionEjecucion);
		assertFalse(ValidaactualizacionEjecucion, "No se encontro registro con las tiendas correspondientes");

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