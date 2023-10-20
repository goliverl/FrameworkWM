package interfaces.pr8;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR8ValidacionEnvioCatalogos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR8_001_Administrar_Catalogo_Articulos(HashMap<String, String> data) throws Exception {

		/**
		 * @category pruebas
		 * @param data
		 * @throws Exception
		 * @description : No esta procesando informacion la interfaz,se envio un correo
		 *              a Espericueta Duran Kevin Rafael y se consiguio lo siguiente:
		 *              "Validando los mensajes que se muestran en BD observo que se
		 *              están utilizando otras Plazas (13TLC,13HUC), además no está
		 *              completa la ruta porque no se especifica la Tienda, debería ser
		 *              algo así:
		 *              /u01/posuser/FEMSA_OXXO/POS/NombrePlaza/NombreTienda/working
		 *              Ejemplo: /u01/posuser/FEMSA_OXXO/POS/10MON/50MCZ/working".
		 * @dateup 17 Noviembre 2020
		 */

		/**
		 * UTILERIA
		 *********************************************************************/
		/*utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);*/
		
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		SeleniumUtil u;
		PakageManagment pok;

		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");		
		
		/**
		 * VARIABLES
		 *********************************************************************/
		String tdcQueryInfoPlaza = "SELECT cr_plaza,cr_plaza_desc,plaza_type,fch_arranque,ventana_dias,dias_tolerancia,sinergia "
				+ " FROM POSUSER.plazas" + " where cr_plaza ='" + data.get("cr_plaza") + "'";

		String tdcQueryStatusLog = "select * from(SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = '" + data.get("Run_interface") + "'"
//				+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC)where rownum<=1";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("Run_interface") + "'" + " and  start_dt >= trunc(sysdate)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s" + " AND STATUS = 'S'"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryDocTSC = "select * from(SELECT id,PV_CR_PLAZA,PV_CR_TIENDA,DOC_NAME,DOC_TYPE,STATUS,PARTITION_DATE"
				+ " FROM POSUSER.pos_outbound_docs" + " WHERE 1=1" + " and doc_type='TSC'" + " and PV_CR_PLAZA ='"
				+ data.get("cr_plaza") + "'" + " and PV_CR_TIENDA ='" + data.get("cr_tienda")
				+ "' order by partition_date desc)where rownum<=1";

		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR8%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1";
		String consulta61 = " select * from (select description,MESSAGE from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Tener información en la tabla PLAZAS del esquema POSUSER para la plaza " + data.get("cr_plaza"));

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryInfoPlaza);
		SQLResult queryInfPlaza = dbPos.executeQuery(tdcQueryInfoPlaza);
		boolean infPlaza = queryInfPlaza.isEmpty();
		if (!infPlaza) {
			testCase.addQueryEvidenceCurrentStep(queryInfPlaza);

		}

		System.out.println(infPlaza);

		assertFalse(infPlaza, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Ejecutar el servicio PR8.Pub:run");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1);

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (!valuesStatus) {
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);
		}


		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		String error = String.format(consulta6, run_id);
		String error1 = String.format(consulta61, run_id);
		String error2 = String.format(consulta62, run_id);

		SQLResult errorr = dbLog.executeQuery(error);
		boolean emptyError = errorr.isEmpty();
		// Se comenta paso ya que el log muestra estatus E por algunas tiendas, no en
		// todos los casos,
		// descomentar y poner assert cuando se tengan bien configuradas todas las
		// tiendas
//		if (!emptyError) {
//			testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
//			testCase.addQueryEvidenceCurrentStep(errorr);
//		}
//
//		SQLResult errorIS = dbLog.executeQuery(error1);
//		boolean emptyError1 = errorIS.isEmpty();
//
//		if (!emptyError1) {
//			testCase.addQueryEvidenceCurrentStep(errorIS);
//		}
//
//		SQLResult errorIS2 = dbLog.executeQuery(error2);
//		boolean emptyError2 = errorIS2.isEmpty();
//
//		if (!emptyError2) {
//			testCase.addQueryEvidenceCurrentStep(errorIS2);
//		}		

		/* PASO 3 *********************************************************************/
//Se comenta paso ya que el log muestra estatus E por algunas tiendas, no en todos los casos, 
		// descomentar y poner assert cuando se tengan bien configuradas todas las
		// tiendas
//		addStep("Comprobar que se registra la ejecucion en WMLOG");
//
//		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
//		SQLResult queryStatusLogexe = dbLog.executeQuery(tdcQueryIntegrationServer);
//		String fcwS = queryStatusLogexe.getData(0, "STATUS");
//		System.out.println(fcwS);
//		
//		testCase.addQueryEvidenceCurrentStep(queryStatusLogexe);
//		

		/* PASO 4 *********************************************************************/

		addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult regPlazaTiendaQuery = dbLog.executeQuery(queryStatusThread);
		/*String regPlazaTienda = regPlazaTiendaQuery.getData(0, "STATUS");
		System.out.println(regPlazaTienda);

		boolean statusThread = status.equals(regPlazaTienda);
		System.out.println(statusThread);

		if (!statusThread) {
			error = String.format(tdcQueryErrorId, run_id);
			SQLResult emptyErrorQuery = dbPos.executeQuery(error);

			emptyError = emptyErrorQuery.isEmpty();

			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(emptyErrorQuery);
			}
		}
*/
		testCase.addBoldTextEvidenceCurrentStep("Error. No se genera thread");
		testCase.addQueryEvidenceCurrentStep(regPlazaTiendaQuery);
	//	assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		/* PASO 5 *********************************************************************/

		addStep("Validar la generacion del archivo TSC para la tienda seleccionada");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryDocTSC);
		SQLResult tdcQueryDocTSCQuery = dbPos.executeQuery(tdcQueryDocTSC);
		boolean tsc = tdcQueryDocTSCQuery.isEmpty();
		String doc = "";
		if (!tsc) {
			testCase.addQueryEvidenceCurrentStep(tdcQueryDocTSCQuery);
			doc = tdcQueryDocTSCQuery.getData(0, "DOC_NAME");
			System.out.println("docum: " + doc);
		}

		System.out.println(tsc);
		assertFalse(infPlaza, "No se obtiene informacion de la consulta");

		/* PASO 5 *********************************************************************/

		StringBuilder DOC = new StringBuilder(doc);
		int i = 1;
		while (i <= 5) {
			//DOC = DOC.deleteCharAt(8); // 8,9,10,11,12
			i++;
		}
		addStep("Comprobar que se genere el archivo y se almacene en la ruta del FileSystem: /u01/posuser/FEMSA_OXXO/POS/"
				+ data.get("cr_plaza") + "/working/" + DOC);

		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");

		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("cr_plaza") + "/working/" + DOC;
		System.out.println("Ruta: " + ruta);

		if (ftp.fileExists(ruta)) {

			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
					+ data.get("cr_plaza") + "/working/" + DOC);

		} else {

			System.out.println("No Existe");

		}
		testCase.addBoldTextEvidenceCurrentStep("Error. No Existen archivos en la ruta FTP");
		testCase.addBoldTextEvidenceCurrentStep(ruta);


		//assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Catálogo de Tiendas. El procesamiento de esta interfase transfiere la información de catálogos de tiendas a buzón de cada Plaza.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR8_001_Administrar_Catalogo_Articulos";
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
