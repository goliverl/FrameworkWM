package interfaces.PR8_CO;

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

public class PR8_COEnvioCatalogos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR8CO_002_Envio_Catalogo_Articulos(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		
		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");		
		
/** VARIABLES *********************************************************************/	

		String tdcQueryInfoPlaza = "SELECT cr_plaza,cr_plaza_desc,plaza_type,fch_arranque,ventana_dias,dias_tolerancia,sinergia "
				+ " FROM POSUSER.plazas" + " where cr_plaza ='" + data.get("cr_plaza") + "'";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s" + " AND STATUS = 'S'"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryDocTSC = "select * from(SELECT id,PV_CR_PLAZA,PV_CR_TIENDA,DOC_NAME,DOC_TYPE,STATUS,PARTITION_DATE"
				+ " FROM POSUSER.pos_outbound_docs" + " WHERE 1=1" + " and doc_type='TSC'" + " and PV_CR_PLAZA ='"
				+ data.get("cr_plaza") + "'" + " and PV_CR_TIENDA ='" + data.get("cr_tienda")
				+ "' order by partition_date desc)where rownum<=1";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR08_CO%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String verifyFile = " SELECT ID, doc_type, status, DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED "
				+ " FROM POSUSER.POS_OUTBOUND_DOCS " 
				+ " WHERE PV_CR_PLAZA = '" + data.get("cr_plaza") + "' "
				+ " AND PV_CR_TIENDA = '" + data.get("cr_tienda") + "' "
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE) order by sent_date desc ";

		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=5";
		String consulta61 = " select * from (select description,MESSAGE from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 5";
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 5";

       /** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
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
		
		addStep("Ejecutar el servicio PR8_CO.Pub:run");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
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
			String error = String.format(consulta6, run_id);
			String error1 = String.format(consulta61, run_id);
			String error2 = String.format(consulta62, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}					
		
		/* PASO 3 *********************************************************************/

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		SQLResult queryStatusLogexe = dbLog.executeQuery(tdcIntegrationServerFormat);
		String fcwS = queryStatusLogexe.getData(0, "STATUS");

		System.out.println(fcwS);
		testCase.addQueryEvidenceCurrentStep(queryStatusLogexe);
		

		/* PASO 4 *********************************************************************/

		addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult regPlazaTiendaQuery = dbLog.executeQuery(queryStatusThread);
		String regPlazaTienda = regPlazaTiendaQuery.getData(0, "STATUS");
		System.out.println(regPlazaTienda);

		boolean statusThread = status.equals(regPlazaTienda);		
		System.out.println(statusThread);
		
		if (!statusThread) {
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult emptyErrorQuery = dbPos.executeQuery(error);

			boolean emptyError = emptyErrorQuery.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(emptyErrorQuery);
			}
		}
		
		testCase.addQueryEvidenceCurrentStep(regPlazaTiendaQuery);	
		assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		
		/* PASO 5 *********************************************************************/

		addStep("Validar la generacion del archivo TSC para la tienda seleccionada");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryDocTSC);
		SQLResult tdcQueryDocTSCQuery = dbPos.executeQuery(tdcQueryDocTSC);
		
		boolean tsc = tdcQueryDocTSCQuery.isEmpty();
		
		if (!tsc) {
			testCase.addQueryEvidenceCurrentStep(tdcQueryDocTSCQuery);
		}

		System.out.println(tsc);
		assertFalse(tsc, "No se obtiene informacion de la consulta");
		
	   /* PASO 6 *********************************************************************/
		
		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult paso6 = executeQuery(dbPuser, verifyFile);
		System.out.println(verifyFile);
		String doc_name = null;
		boolean step6 = paso6.isEmpty();
		if (!step6) {
		doc_name = paso6.getData(0, "DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(paso6);
		}
		
		StringBuilder DOC = new StringBuilder (doc_name);
		int i =1;
		while(i<=5) {
			DOC = DOC.deleteCharAt(8); //8,9,10,11,12
			i++;
			
		}
		
		System.out.println(step6);
		assertFalse(step6, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		
	     FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
         String ruta =  "/u01/posuser/FEMSA_OXXO/POS/" + data.get("cr_plaza") + "/working/" + DOC;
         
         System.out.println(ruta);
         
		if (ftp.fileExists(ruta)) {
			
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta" + ruta);
			
		} else {
			
			System.out.println("No existe");
			
		}
		assertFalse(!ftp.fileExists(ruta),"No se obtiene el archivo por FTP.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Catálogo de Tiendas. El procesamiento de esta interfase transfiere la información de catálogos de tiendas a buzón de cada Plaza.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR8CO_002_Envio_Catalogo_Articulos";
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
