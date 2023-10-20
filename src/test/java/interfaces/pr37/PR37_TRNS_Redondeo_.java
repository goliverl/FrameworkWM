package interfaces.pr37;

import static org.junit.Assert.assertFalse;
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


public class PR37_TRNS_Redondeo_ extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR37_TRNS_Redondeo(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/
		/*
		 utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRms =  new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		*/
		
		
		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);

		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRms =  new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR,GlobalVariables.DB_PASSWORD_FCRMSMGR);
		
		
		
		
		
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR37%' AND start_dt>=TRUNC(SYSDATE) "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String consulta44 = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR37'" + " and  start_dt > To_Date ('%s', 'DD-MM-YYYY hh24:mi' )"
				+ " order by start_dt desc)" + " where rownum = 1";
		String CON_PRC = "select * from(SELECT DISTINCT A.pv_cr_plaza, A.pv_cr_tienda,  b.id, b.received_date FROM POSUSER.pos_envelope A, POSUSER.pos_inbound_docs b WHERE b.status = 'I' AND b.doc_type = 'PRC' AND A.ID = b.pe_id AND A.pv_cr_plaza = '"
				+ data.get("plaza") + "' AND a.pv_cr_tienda = '" + data.get("tienda")
				+ "' ORDER BY b.received_date DESC)" + "WHERE rownum <= 3";
		String consDTL = "SELECT * FROM POSUSER.POS_PRC_DETL " + "WHERE PID_ID='%s'";
		String consultaTHREAD = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD " + "WHERE PARENT_ID='%s'";
		String consultaTHREAD2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 " + "FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID='%s'";
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";
		String consSTATUSE = " SELECT id, PE_ID, PV_DOC_ID,STATUS, DOC_TYPE" + " FROM posuser.POS_INBOUND_DOCS "
				+ "where DOC_TYPE='PRC' and STATUS='E'  and TARGET_ID='%s'";
		String consSTATUSE1 = " SELECT pv_doc_name, TARGET_ID,RECEIVED_DATE, INSERTED_DATE "
				+ "FROM posuser.POS_INBOUND_DOCS" + " where DOC_TYPE='PRC' and STATUS='E'  and TARGET_ID='%s'";
		String consSTATUSE2 = " SELECT VERSION,PR50_VERSION,SOURCEAPP, PARTITION_DATE "
				+ "FROM posuser.POS_INBOUND_DOCS " + "where DOC_TYPE='PRC' and STATUS='E'  and TARGET_ID='%s'";
		String consultaPRC = "SELECT * FROM (select item, store, tran_date,tran_code,units,total_retail,old_unit_retail, new_unit_retail, processed_ind "
				+ "from xxfc.xxfc_price_change_stg " + "where ITEM='%s')WHERE rownum <= 3";
		
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	//	String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		
		
		/*
		 * paso 1 - Consultar la tabla POS_INBOUND_DOCS para validar que se muestren
		 * generado el registro perteneciente al archivo PRC
		 **********************/
		addStep("Consultar la tabla POS_INBOUND_DOCS para validar que se muestren  archivo PRC ");
		System.out.println("Respuesta"+ CON_PRC);
		SQLResult PRC1 = dbPOSUSER.executeQuery(CON_PRC);
		
		
		String id = null;

		// String plaza = hora.getData(0, "cr_plaza");
		System.out.println("Respuesta " + id);

		boolean paso1 = PRC1.isEmpty();
		if (!paso1) {
			id = PRC1.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(PRC1);
		}
		
		testCase.addBoldTextEvidenceCurrentStep("Error. No se encuentra el  documentos ADJ");
		testCase.addQueryEvidenceCurrentStep(PRC1);
		
		//assertFalse("No hay insumos a procesar", paso1);

		/*
		 * paso 2 Consultar la tabla POS_PRC_DETL para validar que se muestre el
		 * registro con el detalle relacionado con el archivo PRC.
		 *****************************************************/
		addStep(" Consultar la tabla POS_PRC_DETL para validar que se muestre el registro con el detalle relacionado con el archivo PRC ");

		String item ="";
		if (!paso1) {
			String consultaDETL1 = String.format(consDTL, id);
			SQLResult DETL1 = dbPOSUSER.executeQuery(consultaDETL1);
		    DETL1.getData(0, "ITEM");
			boolean paso2 = DETL1.isEmpty();
			testCase.addQueryEvidenceCurrentStep(DETL1);
		}
		
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		

		
		//assertFalse("No hay insumos a procesar",paso1);

		/*
		 * paso 2 correr la pr26 el servicio eun itm, prm
		 */
		addStep("Ejecutar  el servicio de la interfaz PR37");
	
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		// String dateExecution = null;
		// String dateExecution ="";

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult is = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			// is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLOG,
			// tdcIntegrationServerFormat, "STATUS", "RUN_ID");
			is = executeQuery(dbLOG, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLOG.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecuci?n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLOG.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLOG.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//Paso 3    ************************		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		// String fcwS = SQLUtil.getColumn(testCase, dbLOG, tdcIntegrationServerFormat,
		// "STATUS");
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");
		SQLResult log = dbLOG.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);
		// SQLResult errorIS= dbLOG.executeQuery(error1);

		boolean log1 = log.isEmpty();
		// boolean av2 = SQLUtil.isEmptyQuery(testCase, dbLOG,
		// tdcIntegrationServerFormat);
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

		addStep("Validar que el registro de ejecución  en la tabla WM_LOG_THREAD.");

		String consultaTH = String.format(consultaTHREAD, run_id);
		SQLResult THRE = dbLOG.executeQuery(consultaTH);

		boolean paso1TH = THRE.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(THRE);
		}
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(THRE);

		//assertFalse("No hay insumos a procesar", paso1TH);

		// .-----------Segunda consulta

		String consultaTH2 = String.format(consultaTHREAD2, run_id);
		SQLResult THRE2 = dbLOG.executeQuery(consultaTH2);

		boolean paso1TH2 = THRE.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(THRE2);
		}
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(THRE2);

		
		//assertFalse("No hay insumos a procesar", paso1TH2);
		/*
		 * paso 7 consultar la tabla POS_INBOUND_DOCS para validar que el registro
		 * perteneciente al archivo PRC se muestre en el campo STATUS = E
		 */
		addStep("  Validar archivo PRC se muestre en el campo STATUS = E");
		String STATUSE1 = String.format(consSTATUSE, run_id);
		SQLResult E = dbPOSUSER.executeQuery(STATUSE1);

		boolean paso7 = E.isEmpty();
		if (!paso7) {
			testCase.addQueryEvidenceCurrentStep(E);
		}
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(E);


		//assertFalse("No hay insumos a procesar", paso7);

		String STATUSE12 = String.format(consSTATUSE1, run_id);
		SQLResult E1 = dbPOSUSER.executeQuery(STATUSE12);

		boolean paso71 = E1.isEmpty();
		if (!paso71) {
			testCase.addQueryEvidenceCurrentStep(E1);
		}
		
		
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(E1);


		//assertFalse("No hay insumos a procesar", paso71);
		String STATUSE3 = String.format(consSTATUSE2, run_id);
		SQLResult E3 = dbPOSUSER.executeQuery(STATUSE3);

		boolean paso73 = E3.isEmpty();
		if (!paso73) {
			testCase.addQueryEvidenceCurrentStep(E3);
		}

		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(E3);


		//	assertFalse("No hay insumos a procesar", paso73);

		/*
		 * paso 8 Consultar la tabla xxfc_price_change_stg para validar que se encuntre
		 * generados registros perteneciente al PRC
		 */
		addStep("  validar que se encuntre generados registros perteneciente al PRC");

		String consultaPRC1 = String.format(consultaPRC, item);
	System.out.print(""+consultaPRC1);
		SQLResult PRC= dbRms.executeQuery(consultaPRC1);

		boolean paso8 = PRC.isEmpty();
		if (!paso8) {
			testCase.addQueryEvidenceCurrentStep(PRC);
		}
		
		testCase.addBoldTextEvidenceCurrentStep("Error. No hay insumos a procesar");
		testCase.addQueryEvidenceCurrentStep(PRC);


		//assertFalse("No hay insumos a procesar", paso8);

	}
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar el envio del detalle de transacciones de redondeo";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR37_TRNS_Redondeo";
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
