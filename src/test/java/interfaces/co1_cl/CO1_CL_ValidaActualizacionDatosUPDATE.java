package interfaces.co1_cl;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class CO1_CL_ValidaActualizacionDatosUPDATE extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_CO1_CL_Validar_Insercion_Datos(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/

		SQLUtil dbCNTCL = new SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,GlobalVariables.DB_PASSWORD_CNTCHILE);

		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);

		SQLUtil dbEBSCL = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		
		/**
		 * ALM
		 *  Validar la actualizacion de datos en XXFC_CENTROS_RESPONSABILIDAD 
		 * de la BD ORAFIN UPDATE
		 */
		

		/*
		 * Variables
		 ******************************************************************************************************************************************/

//********************************************************************************************************************************************************************************		

		/* Pasos */

		// Paso 1
		String regPorActualizar = "SELECT id, retek_loc_id cr_superior, cr_tienda,sync_status, sent_date, run_id  "
				+ "FROM WMUSER.WM_SYNC_T_OPERACION " + "WHERE CR_SUPERIOR = '" + data.get("plaza") + "'  "
				+ "AND CR_TIENDA = '" + data.get("tienda") + "'  " + "AND SYNC_STATUS='F'";

		// Paso 2
		String datosORAFIN_CNT = "SELECT RETEK_CR, ORACLE_CR_SUPERIOR, ORACLE_CR, ID_CENTRO_RESPONSABILIDAD, RETEK_ASESOR_NOMBRE  "
				+ "FROM APPS.XXFC_CENTROS_RESPONSABILIDAD  " + "WHERE RETEK_CR = '%s' " + "AND ORACLE_CR_SUPERIOR = '"
				+ data.get("plaza") + "' " + "AND ORACLE_CR = '" + data.get("tienda") + "' "
				+ "AND ORACLE_CR_TYPE='T' ";

		// Paso 3 adapters

//		Paso 5
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'CO1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

		// Paso 6
		String ValidaActualizacion = "SELECT RETEK_ASESOR, RETEK_DISTRITO,  "
				+ "RETEK_ASESOR_NOMBRE, LAST_UPDATE_DATE  " + "FROM APPS.XXFC_CENTROS_RESPONSABILIDAD  "
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'  AND ORACLE_CR = '" + data.get("tienda")
				+ "' AND ORACLE_CR_TYPE = 'T'  " + "AND TRUNC(LAST_UPDATE_DATE) = SYSDATE ";

		// Paso 7
		String actualizacionEjecucion = "SELECT id, retek_loc_id cr_superior, cr_tienda,sync_status, sent_date, run_id "
				+ "FROM WMUSER.WM_SYNC_T_OPERACION WHERE CR_SUPERIOR = '" + data.get("plaza") + "' "
				+ "AND RUN_ID = '%s' " + "AND CR_TIENDA = '" + data.get("plaza") + "' "
				+ "AND TRUNC(SENT_DATE) = SYSDATE  " + "AND SYNC_STATUS = 'E'";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Que exista registros para ser actualizdos en la tabla WM_SYNC_T_OPERACION de la BD CNT ");
		String retek_loc_id = "";
		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		System.out.println(regPorActualizar);

		SQLResult regPorActualizarRes = executeQuery(dbCNTCL, regPorActualizar);

		boolean ValidaregPorActualizar = regPorActualizarRes.isEmpty();

		if (!ValidaregPorActualizar) {
			retek_loc_id = regPorActualizarRes.getData(0, "RETEK_LOC_ID");
			System.out.println("RETEK_LOC_ID" + retek_loc_id);
			testCase.addQueryEvidenceCurrentStep(regPorActualizarRes);
		}

		System.out.println(ValidaregPorActualizar);
		assertFalse(ValidaregPorActualizar, "No se encontro registro con las tiendas correspondientes");

		/* PASO 2 *********************************************************************/

		addStep("Comprobar que existan registros en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN "
				+ "para los valores que se van a actualizar en WM_SYNC_T_OPERACION en la BD CNT ");
		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		String datosORAFIN_CNT_F = String.format(datosORAFIN_CNT, retek_loc_id);

		System.out.println(datosORAFIN_CNT_F);

		SQLResult datosORAFIN_CNT_Res = executeQuery(dbEBSCL, datosORAFIN_CNT_F);

		boolean ValidadatosORAFIN_CNT = datosORAFIN_CNT_Res.isEmpty();

		if (!ValidadatosORAFIN_CNT) {
			testCase.addQueryEvidenceCurrentStep(regPorActualizarRes);
		}

		System.out.println(ValidadatosORAFIN_CNT);

		assertFalse(ValidadatosORAFIN_CNT, "No se encontraron datos que coincidan para ser actualizados ");

		/* PASO 3 *********************************************************************/

		/*
		 * Comprobar que el adapter notification CO1.DB.CNT:adpNotInsUpdTOperaciones
		 * este habilitado.
		 */

//		  PASO 4  **********************************************************************

		/*
		 * Se realiza un update en la tabla WM_SYNC_T_OPERACION de la BD CNT chile
		 * 
		 * UPDATE WM_SYNC_T_OPERACION SET SYNC_STATUS = 'L', RETEK_MGR_NAME = 'Cesar
		 * Quiroga Rodriguez' WHERE CR_SUPERIOR = 'Dato_Plaza' AND CR_TIENDA =
		 * 'Dato_Tienda' AND SYNC_STATUS = 'F';
		 * 
		 */

		/* PASO 5 *********************************************************************/

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
		assertFalse("No se muestra  la información.", LogRequest);

		/* PASO 6 *********************************************************************/

		addStep("Se confirma la actualizacion de los campos RETEK_ASESOR, RETEK_DISTRITO, RETEK_ASESOR_NOMBRE "
				+ "en la tabla XXFC_CENTROS_RESPONSABILIDAD de la BD ORAFIN");
		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(ValidaActualizacion);
		SQLResult ValidaActualizacionRes = executeQuery(dbEBSCL, ValidaActualizacion);

		boolean ValidaActualizacionBool = ValidaActualizacionRes.isEmpty();

		if (!ValidaActualizacionBool) {
			testCase.addQueryEvidenceCurrentStep(ValidaActualizacionRes);
		}

		System.out.println(ValidaActualizacionBool);
		assertFalse(ValidaActualizacionBool,"No se actualizaron los datos correctamente despues de la ejecucion de la interfaz.");

		/* PASO 7 *********************************************************************/

		addStep("Confirmar que se actualicen las columnas en la tabla WM_SYNC_T_OPERACION de la BD CNT chile");
		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		String actualizacionEjecucionF = String.format(actualizacionEjecucion, run_id);

		System.out.println(actualizacionEjecucionF);

	SQLResult actualizacionEjecucionR = executeQuery(dbCNTCL, actualizacionEjecucionF);

		boolean ValidaactualizacionEjecucion = actualizacionEjecucionR.isEmpty();

		if (!ValidaactualizacionEjecucion) {
			testCase.addQueryEvidenceCurrentStep(actualizacionEjecucionR);
		}

		System.out.println(ValidaactualizacionEjecucion);
		assertFalse(ValidaactualizacionEjecucion,
				"Los campos de los registros seleccionados no fueron actualizados correctamente ");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_CO1_CL_Validar_actualización";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Cuando se inserte un registro en la tabla WM_SYNC_T_OPERACION de la BD CNT.";
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
