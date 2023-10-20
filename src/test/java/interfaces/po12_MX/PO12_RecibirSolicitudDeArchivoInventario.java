package interfaces.po12_MX;

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
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PO12_RecibirSolicitudDeArchivoInventario extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO12_RecibirSolicitudDeArchivoInventario(HashMap<String, String> data)
			throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String ValidReg = "SELECT ESTADO, ORACLE_CR_SUPERIOR,ORACLE_CR,ORACLE_CR_TYPE "
				+ "FROM  XXFC_MAESTRO_DE_CRS_V "
				+ "WHERE  ESTADO = 'A'  "
				+ "AND ORACLE_CR_SUPERIOR = '"+data.get("plaza")+"' "
				+ "AND ORACLE_CR = '"+data.get("tienda")+"' "
				+ "AND ORACLE_CR_TYPE = 'T'";
		
		String ValidReg2 = "SELECT CR,ESTADO_FINANCIERO,LAST_UPDATE_DATE "
				+ "FROM XXFC_AF.XXFC_AF_BITACORA "
				+ "WHERE CR = '"+data.get("plaza")+"' "
				+ "AND ESTADO_FINANCIERO = '"+data.get("tienda")+"' "
				+ "AND LAST_UPDATE_DATE = Trunc(sysdate)";

		String VerifFTP = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT "
				+ "FROM WMUSER.WM_FTP_CONNECTIONS WHERE  FTP_CONN_ID = 'AF_INVENTARIO'";
	
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status,INTERFACE" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO12_runPeticion'" + "AND status = 'S'" + " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String ValidInsert = "SELECT INS_DATE,SP_CODIGO,SP_DESCRIP  FROM  WMUSER.XXFC_AF_DATA_WM WHERE TRUNC(INS_DATE) = TRUNC(SYSDATE)";

		/**
		 * ************************************** Pasos del caso de Prueba
		 *******************************************/

//Paso 1 *************************		
		addStep("Consultar que existan registros en la tabla XXFC_MAESTRO_DE_CRS_V, para el ORACLE_CR_SUPERIOR igual a PLAZA,  ORACLE_CR es igual a TIENDA, ORACLE_CR_TYPE es igual a T, y ESTADO es igual a A en la BD ORAFIN.");
		
		 
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg);

		SQLResult validarResult = executeQuery(dbEbs, ValidReg);

		boolean validar = validarResult.isEmpty();

		if (!validar) {
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}

		System.out.println(validar);

		assertFalse(validar,"No existen registros en la tabla XXFC_MAESTRO_DE_CRS_V con los parametros buscados");

//Paso 2 *************************		
		addStep("Verificar que existan datos en la tabla XXFC_AF_BITACORA, para el CR igual a PLAZA, ESTADO_FINANCIERO es igual a TIENDA.");
		
		
	
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg2);

		SQLResult validarResult2 = executeQuery(dbEbs, ValidReg2);

		boolean validar2 = validarResult2.isEmpty();

		if (!validar2) {
			testCase.addQueryEvidenceCurrentStep(validarResult2);
		}

		System.out.println(validar2);

		assertFalse(validar2,"No existen registros en la tabla XXFC_AF_BITACORA con los parametros buscados");

//Paso 3 *************************		
		addStep("Verificar la configuracion del servicio FTP en la tabla WM_FTP_CONNECTIONS en donde FTP_ID es igual a AF_INVENTARIO.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(VerifFTP);

		SQLResult profundidadResult = executeQuery(dbPos, VerifFTP);
		boolean prof = profundidadResult.isEmpty();

		if (!prof) {

			testCase.addQueryEvidenceCurrentStep(profundidadResult);

		}

		System.out.println(prof);

		assertFalse(prof, "No se obtiene información de la consulta");

// paso 4 *************************
		addStep("Se inicia el servicio PO12.Pub:runRequest.");
		String status = "S";

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id="";
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		// paso 5 *********************************

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'PO12_runPeticion' y STATUS es igual a 'S'.");
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			query = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		testCase.addQueryEvidenceCurrentStep(query);
		assertFalse(successRun, "No se obtiene información de la consulta");

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
		return "Recibir la solicitud  de archivo del inventario para la Plaza 10OBR y Tienda 50V1C";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PO12_RecibirSolicitudDeArchivoInventario";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
