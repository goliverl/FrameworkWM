package interfaces.pr11_CL;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR11_CL_SincronizacionDeInventariosFisicos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR11_CL_003_SincronizacionDeInventariosFisicos(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbLogCh = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPosCh = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile,
				GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRmsCh = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile,
				GlobalVariables.DB_PASSWORD_RMSWMUSERChile);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT DISTINCT PE.PV_CR_PLAZA, PE.PV_CR_TIENDA, PID.ID, PID.PE_ID, PID.STATUS, TO_CHAR(PD.FECHA_ADM,'YYYYMMDD') FECHA_MVT \r\n"
				+ " FROM POSUSER.POS_ENVELOPE PE \r\n" + " JOIN POSUSER.POS_INBOUND_DOCS PID \r\n"
				+ " ON PID.PE_ID = PE.ID \r\n" + " JOIN POSUSER.POS_INV_DETL PD \r\n" + " ON  PID.ID = PD.PID_ID \r\n"
				+ " WHERE PID.DOC_TYPE = 'INV' \r\n" + " AND PID.STATUS = 'I' \r\n"
				+ " AND PID.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n" + " AND PE.PARTITION_DATE>=TRUNC(SYSDATE-2) \r\n"
				+ " AND PE.PV_CR_PLAZA = '" + data.get("plaza") + "' \r\n";

		String tdcQueryPaso2 = " SELECT PID_ID, PV_INV_FIS, TO_CHAR(FECHA_ADM,'YYYYMMDD') FECHA_ADM \r\n"
				+ " FROM POSUSER.POS_INV \r\n" + " WHERE PID_ID = %s \r\n" + " AND PV_INV_FIS = 'F' \r\n";

		String tdcQueryPaso4 = " SELECT run_id,interface,start_dt,status,server \r\n" + " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR11_CL' \r\n" + " and status= 'S' \r\n" + " and start_dt >= trunc(sysdate) \r\n" // FCWMLQA
				+ " ORDER BY start_dt DESC \r\n";

		String tdcQueryPaso5 = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n" + " WHERE parent_id = %s \r\n"; // FCWMLQA

		String tdcQueryPaso6 = " SELECT CR_PLAZA,CR_TIENDA,FECHA_MVT,PROCESSED_DATE \r\n"
				+ " FROM XXFC.XXFC_ITEM_LOC_POS \r\n" + " WHERE 1=1 \r\n" + " AND CR_PLAZA = '" + data.get("plaza")
				+ "' \r\n" + " AND CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " Order by processed_date desc \r\n";

		String tdcQueryPaso7 = "SELECT DOC_TYPE,STATUS,ID \r\n" + " FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ " WHERE DOC_TYPE = 'INV' \r\n" + " AND STATUS = 'E' \r\n" + " AND ID = '%s' \r\n";

		String tdcQueryPaso8 = "SELECT CR_PLAZA,CR_TIENDA \r\n" + " FROM XXFC.XXFC_REPLEN_CONTROL_LOCS_F \r\n"
				+ " WHERE CR_PLAZA ='" + data.get("plaza") + "' \r\n" + " AND CR_TIENDA = '" + data.get("tienda")
				+ "' \r\n";

		// utileria

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 * 
		 */

//						Paso 1	************************

		addStep("Validar que exista documentos de tipo DOC_TYPE='INV' pendiente de procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I'");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		System.out.println(tdcQueryPaso1);

		SQLResult paso1 = executeQuery(dbPosCh, tdcQueryPaso1);

		String id = paso1.getData(0, "ID");

		boolean ValidaPaso1 = paso1.isEmpty();

		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(paso1);

		}

		System.out.println(ValidaPaso1);

		assertFalse(ValidaPaso1, "No se obtiene informacion de la consulta");

//						Paso 2	***********************

		addStep("Validar que el documento a procesar sea de tipo inventario físico en la tabla POS_INV de POSUSER, registro con PV_INV_FIS = 'F'.\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		String FormatoPaso2 = String.format(tdcQueryPaso2, id);

		System.out.println(FormatoPaso2);

		SQLResult paso2 = executeQuery(dbPosCh, FormatoPaso2);

		boolean ValidaPaso2 = paso2.isEmpty();

		if (!ValidaPaso2) {

			testCase.addQueryEvidenceCurrentStep(paso1);

		}

		System.out.println(ValidaPaso2);

		assertFalse(ValidaPaso2, "No se obtiene informacion de la consulta");

//				Paso 3	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//				Paso 4	************************

		addStep("Validar que se inserte el detalle de la ejecución de la interface PR11 en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.\n"
				+ "Se valida la generacion de thread");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		System.out.println(tdcQueryPaso4);

		SQLResult paso4 = executeQuery(dbLogCh, tdcQueryPaso4);

		String parent = paso4.getData(0, "RUN_ID");

		boolean ValidaPaso4 = paso4.isEmpty();

		if (!ValidaPaso4) {

			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		System.out.println(ValidaPaso4);

		assertFalse(ValidaPaso4, "No se obtiene informacion de la consulta");

//				Paso 5	************************

		addStep("Validar que se inserte el detalle de la ejecución de los threads lanzados por la interface PR11 en la tabla WM_LOG_THREAD de WMLOG con STATUS = 'S'.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		String FormatoPaso5 = String.format(tdcQueryPaso5, parent);

		System.out.println(FormatoPaso5);

		SQLResult paso5 = executeQuery(dbLogCh, FormatoPaso5);

		boolean ValidaPaso5 = paso5.isEmpty();

		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		System.out.println(ValidaPaso5);

		assertFalse(ValidaPaso5, "No se obtiene informacion de la consulta");

//				Paso 6	************************

		addStep("Validar que se inserte la información de los documentos procesados en la tabla XXFC.XXFC_ITEM_LOC_POS de RETEK.'");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		System.out.println(tdcQueryPaso6);

		SQLResult paso6 = executeQuery(dbRmsCh, tdcQueryPaso6);

		boolean ValidaPaso6 = paso6.isEmpty();

		if (!ValidaPaso6) {

			testCase.addQueryEvidenceCurrentStep(paso6);

		}

		System.out.println(ValidaPaso6);

		assertFalse(ValidaPaso6, "No se obtiene informacion de la consulta");

//				Paso 7	************************

		addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS = 'E'.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		String FormatoPaso7 = String.format(tdcQueryPaso7, id);

		System.out.println(FormatoPaso7);

		SQLResult paso7 = executeQuery(dbRmsCh, FormatoPaso7);

		boolean ValidaPaso7 = paso7.isEmpty();

		if (!ValidaPaso7) {

			testCase.addQueryEvidenceCurrentStep(paso7);

		}

		System.out.println(ValidaPaso7);

		assertFalse(ValidaPaso7, "No se obtiene informacion de la consulta");

//              Paso 8 	************************

		addStep("Validar que se actualice la fecha de movimiento para la plaza-tienda con inventario físico procesada en la taba XXFC_REPLEN_CONTROL_LOCS_F de RETEK.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		System.out.println(tdcQueryPaso8);

		SQLResult paso8 = executeQuery(dbRmsCh, tdcQueryPaso8);

		boolean ValidaPaso8 = paso8.isEmpty();

		if (!ValidaPaso8) {

			testCase.addQueryEvidenceCurrentStep(paso8);

		}

		System.out.println(ValidaPaso8);

		assertFalse(ValidaPaso8, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
// TODO Auto-generated method stub
		return "Construido. Sincronizacion de Inventarios";
	}

	@Override
	public String setTestDesigner() {
// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
// TODO Auto-generated method stub
		return "ATC_FT_PR11_CL_003_SincronizacionDeInventariosFisicos";
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
