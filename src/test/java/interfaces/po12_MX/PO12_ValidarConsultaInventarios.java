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

public class PO12_ValidarConsultaInventarios extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO12_ValidarConsultaInventarios(HashMap<String, String> data)
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
				+ " FROM xxfc_af.xxfc_af_inven_fisico_temporal "
				+ "	WHERE creation_date >= TRUNC((SELECT MAX(creation_Date) FROM xxfc_af.xxfc_af_inventario_teorico "
				+ " WHERE cr = '"+data.get("tienda")+"' "
				+ "AND estado_financiero = '"+data.get("plaza")+"' )) "
				+ "	AND TO_CHAR(SUBSTR(parametros,instrb(parametros,'ß',1,2)+1,5)) = '"+data.get("tienda")+"' "
				+ "	AND TO_CHAR(SUBSTR(parametros,instrb(parametros,'ß',1,3)+1,5)) = '"+data.get("plaza")+"' ";

		String ValidReg2 =  "SELECT ID_INVEN_FISICO,CR,ESTADO_FINANCIERO "
				+ "FROM xxfc_af.xxfc_af_inven_fisico "
				+ "WHERE creation_date >= TRUNC((SELECT MAX(creation_Date) "
				+ "FROM xxfc_af.xxfc_af_inventario_teorico "
				+ "WHERE cr = '"+data.get("tienda")+"' "
				+ "AND estado_financiero = '"+data.get("plaza")+"' )) "
				+ "AND cr = '"+data.get("tienda")+"'  "
				+ "AND estado_financiero = '"+data.get("plaza")+"' ";
		

		
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
		addStep("Verificar que existan información de inventarios en las tablas XXFC_AF_INVEN_FISICO_TEMPORAL de la Plaza y Tienda en la base de datos de Finanzas.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg);

		SQLResult validarResult = executeQuery(dbEbs, ValidReg);

		boolean validar = validarResult.isEmpty();

		if (!validar) {
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}

		System.out.println(validar);

		assertFalse(validar,"No información de inventarios en las tablas XXFC_AF_INVEN_FISICO_TEMPORAL");

//Paso 2 *************************		
		
		addStep("Verificar que existan información de inventarios en las tablas  XXFC_AF_INVEN_FISICO de la Plaza y Tienda en la base de datos de Finanzas.");
	
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(ValidReg2);

		SQLResult validarResult2 = executeQuery(dbEbs, ValidReg2);

		boolean validar2 = validarResult2.isEmpty();

		if (!validar2) {
			testCase.addQueryEvidenceCurrentStep(validarResult2);
		}

		System.out.println(validar2);

		assertFalse(validar2,"No existe información de inventarios en las tablas  XXFC_AF_INVEN_FISICO");
//Paso 3 *************************		
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
		return "Validar la consulta de inventarios para la Plaza  y Tienda ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PO12_ValidarConsultaInventarios";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
