package interfaces.co1_cl;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class CO1_CL_ValidaActualizacionDeTabla_Plaza extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_CO1_CL_Validar_actualizacion_datos_Tabla_Plaza(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 ********************************************************************************************************************************************/

		SQLUtil dbCNTCL = new SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,
				GlobalVariables.DB_PASSWORD_CNTCHILE);

		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);

		SQLUtil dbEBSCL = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);

		/**
		 * ALM
		 * Validar la actualizacion de datos en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN  
		 */
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String ValidReg = "SELECT  ORACLE_CR_TYPE,ORACLE_CR,ORACLE_CR_SUPERIOR,RETEK_CR "
				+ "FROM APPS.XXFC_CENTROS_RESPONSABILIDAD " + "WHERE RETEK_CR = '" + data.get("RETEK_CR") + "' "
				+ "AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' " + "AND ORACLE_CR = '" + data.get("tienda")
				+ "' " + "AND ORACLE_CR_TYPE='T'";
//	Paso 3
		String Insert = "INSERT INTO WM_SYNC_T_OPERACION "
				+ "(ID, RETEK_LOC_ID, CR_SUPERIOR, CR_TIENDA, ID_TIENDA, RETEK_REGION, RETEK_DISTRICT, "
				+ "RETEK_MGR_NAME, SYNC_STATUS, CREATED_DATE, SENT_DATE, RUN_ID) "
				+ "VALUES ('99272', '499', '10MON', '50EDI', '945', '23', '534', 'Rafael Garza Acunia', 'L', '09/11/16', NULL, NULL)";

//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'CO1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

//		Paso 5

		String Validainfo2 = "SELECT RETEK_ASESOR, RETEK_DISTRITO, RETEK_ASESOR_NOMBRE, LAST_UPDATE_DATE  "
				+ "FROM APPS.XXFC_CENTROS_RESPONSABILIDAD  " + "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' "
				+ "AND ORACLE_CR = '" + data.get("tienda") + "' " + " AND ORACLE_CR_TYPE = 'T'  "
				+ "AND RUNC(LAST_UPDATE_DATE) = SYSDATE";

//		Paso 6
		String actualizacionEjecucion = "SELECT id, retek_loc_id cr_superior, cr_tienda,sync_status, sent_date, run_id "
				+ "FROM WMUSER.WM_SYNC_T_OPERACION " + "WHERE CR_SUPERIOR = '" + data.get("plaza") + "' "
				+ "AND RUN_ID = '%s' " + "AND CR_TIENDA = '" + data.get("tienda") + "' "
				+ "AND TRUNC(SENT_DATE) = SYSDATE  " + "AND SYNC_STATUS = 'E'";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************
		addStep("Que exista registro en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN ");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);

		System.out.println(ValidReg);

		SQLResult ConsRegRes = dbEBSCL.executeQuery(ValidReg);

		boolean ValidaDatBool = ConsRegRes.isEmpty();

		if (!ValidaDatBool) {
			testCase.addQueryEvidenceCurrentStep(ConsRegRes);
		}

		System.out.println(ValidaDatBool);
		assertFalse("No existe registro en la tabla XXFC_CENTROS_RESPONSABILIDAD", ValidaDatBool);

//		**********************************Paso 2***********************************************

		/*
		 * Comprobar que el adapter notification CO1.DB.CNT:adpNotInsUpdTOperaciones
		 * este habilitado.
		 */

//*************************************************Paso 3***********************************************************************************************************************

		/*
		 * Se inserta un registro en la tabla WM_SYNC_T_OPERACION de la BD CNT chile .
		 * 
		 * INSERT INTO WM_SYNC_T_OPERACION (ID, RETEK_LOC_ID, CR_SUPERIOR, CR_TIENDA,
		 * ID_TIENDA, RETEK_REGION, RETEK_DISTRICT, RETEK_MGR_NAME, SYNC_STATUS,
		 * CREATED_DATE, SENT_DATE, RUN_ID) VALUES ('99273', '21556', '10PBA', '50IEL',
		 * '9143', '101', '29', 'Daniel Castillo Reyes', 'L', '08/09/17', NULL, NULL);
		 */

		addStep("Se inserta un registro en la tabla WM_SYNC_T_OPERACION de la BD CNT chile.");

		testCase.addTextEvidenceCurrentStep(
				"No se puede realizar insert\n" + Insert + "\n realizar insert manualmente a forma de pre-requisito");

//**********************************************************Paso 4*************************************************************************************************************		

		addStep("Se valida el STATUS sea igual a 'S' en la tabla WM_LOG_RUN  de la BD WM_LOG donde INTERFACE sea igual a CO1");
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLogCL.executeQuery(ValidLog);
		String run_id = "";
		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			run_id = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la informaci�n.", LogRequest);

//*********************************************************Paso 5**************************************************************************************************

		addStep("Se confirma la actualizacion de los campos "
				+ "en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN ");
		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(Validainfo2);
		SQLResult ValidainfoRes2 = executeQuery(dbEBSCL, Validainfo2);

		boolean ValidainfoBool2 = ValidainfoRes2.isEmpty();

		if (!ValidainfoBool2) {
			testCase.addQueryEvidenceCurrentStep(ValidainfoRes2);
		}

		System.out.println(ValidainfoBool2);
		assertFalse(ValidainfoBool2, "No se encontro registro con las tiendas correspondientes");

		/* PASO 6 *********************************************************************/

		addStep("Confirmar que se actualicen las columnas en la tabla WM_SYNC_T_OPERACION de la BD CNT chile");

		String actualizacionEjecucionF = String.format(actualizacionEjecucion, run_id);
		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		System.out.println(actualizacionEjecucionF);

		SQLResult actualizacionEjecucionR = executeQuery(dbCNTCL, actualizacionEjecucionF);

		boolean ValidaactualizacionEjecucion = actualizacionEjecucionR.isEmpty();

		if (!ValidaactualizacionEjecucion) {
			testCase.addQueryEvidenceCurrentStep(actualizacionEjecucionR);
		}

		System.out.println(ValidaactualizacionEjecucion);
		assertFalse(ValidaactualizacionEjecucion, "No se encontro registro con las tiendas correspondientes");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_CO1_CL_Validar_actualizacion_datos_Tabla_Plaza";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. ATC_FT_002_CO1_CL_Validar_actualizaci�n_datos";
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
