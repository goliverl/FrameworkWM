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

public class PO12_EjecutarConDocumentosDeInventariosEnSolicitud extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO12_EjecutarConDocumentosDeInventariosEnSolicitud(HashMap<String, String> data)
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
		String ValidReg = "SELECT ID, ID_SEQ,CREATION_DATE,LAST_UPDATE_DATE,TO_CHAR(SUBSTR(PARAMETROS,10,5)) AS TIENDA, TO_CHAR(SUBSTR(PARAMETROS,16,5)) AS PLAZA "
				+ "FROM XXFC_AF.XXFC_AF_INVEN_FISICO_TEMPORAL " + "	WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE) "
				+ "	AND TRUNC(LAST_UPDATE_DATE) = TRUNC(SYSDATE) " + "	AND TO_CHAR(SUBSTR(PARAMETROS,10,5)) = '"
				+ data.get("tienda") + "' " + " AND TO_CHAR(SUBSTR(PARAMETROS,16,5)) = '" + data.get("plaza") + "'";

		String VerifFTP = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT "
				+ "FROM WMUSER.WM_FTP_CONNECTIONS WHERE  FTP_CONN_ID = 'XML_IAF'";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO12'" + "AND status = 'S'" + " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String ValidInsert = "SELECT INS_DATE,SP_CODIGO,SP_DESCRIP  FROM  WMUSER.XXFC_AF_DATA_WM WHERE TRUNC(INS_DATE) = TRUNC(SYSDATE)";

		/**
		 * ************************************** Pasos del caso de Prueba
		 *******************************************/

//Paso 1 *************************		
		addStep("Consultar que NO existan registros insertados en la tabla XXFC_AF_INVEN_FISICO_TEMPORAL para la Plaza y Tienda, con CREATION_DATE con fecha actual en la BD ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg);

		SQLResult validarResult = executeQuery(dbEbs, ValidReg);

		boolean validar = validarResult.isEmpty();

		if (validar) {
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}

		System.out.println(validar);

		assertTrue(validar,
				"existan registros insertados en la tabla XXFC_AF_INVEN_FISICO_TEMPORAL para la Plaza y Tienda, con CREATION_DATE con fecha actual");

//Paso 2 *************************		
		addStep("Verificar en la tabla WM_FTP_CONNECTIONS los datos del Servidor FTP en donde se enviara el archivo de inventario.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(VerifFTP);

		SQLResult profundidadResult = executeQuery(dbPos, VerifFTP);
		boolean prof = profundidadResult.isEmpty();

		if (!prof) {

			testCase.addQueryEvidenceCurrentStep(profundidadResult);

		}

		System.out.println(prof);

		assertFalse(prof, "No se obtiene información de la consulta");

//Paso 3 *************************		
		addStep("Se envia un archivo XML a TN");
		/*
		 * EL FRAMEWORK NO ESTA PREPARADO PARA ENVIAR ARCHIVOS A SERVIDOR FTP, TAMPOCO
		 * SE CONOCE USER Y PASSWORD DE SERVER FTP
		 */

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

		addStep("Ejecutar el siguiente query para consultar la última ejecución.");

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
// paso 6 *********************************
		addStep("Comprobar que se inserten los datos del inventario en la tabla XXFC_AF_INVEN_FISICO_TEMPORAL en donde CREATION_DATE corresponde a la fecha actual en la BD ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg);

		SQLResult validarResult1 = executeQuery(dbEbs, ValidReg);

		boolean validar1 = validarResult1.isEmpty();

		if (validar1) {
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}

		System.out.println(validar1);
		assertFalse(validar1, "No se obtiene información de la consulta");

// paso 7 *********************************		
		addStep("Comprobar que se inserte el registro en la tabla XXFC_AF_DATA_WM de la BD WMINT en donde INS_DATE corresponde a la FECHA_ACTUAL.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ValidInsert);

		SQLResult finanzasResult = executeQuery(dbPos, ValidInsert);

		boolean Res = finanzasResult.isEmpty();

		if (!Res) {

			testCase.addQueryEvidenceCurrentStep(finanzasResult);
		}

		System.out.println(Res);

		assertFalse(Res, "No se obtiene información de la consulta");

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
		return "Ejecutar cuando existen documentos de inventarios en la solicitud para la Plaza 10MON y Tienda 50TTV.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO12_EjecutarConDocumentosDeInventariosEnSolicitud";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
