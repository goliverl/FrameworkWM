package interfaces.pr28;

import static org.junit.Assert.assertFalse;
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

public class ATC_FT_PR28_001_Conexion_por_FTP_APO extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28_001_Conexion_por_FTP_APO_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
			/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String tiendas = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS "
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C "
				+ " WHERE B.PE_ID = A.ID "
				+ "AND B.DOC_TYPE = 'REC'"
				+ " AND B.STATUS IN ('I') "
				+ "AND C.PID_ID = B.ID "
				+ "AND C.PV_CVE_MVT = '10' "
				+ " AND C.EXT_REF_NO <> '0' AND C.ORDER_NO = 0 AND B.PARTITION_DATE >= TRUNC(SYSDATE-7) "
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS";
		
		String datosPos = "SELECT B.ID, TO_CHAR (C.CREATED_DATE, 'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO, SUPPLIER" + 
				" FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C" + 
				" WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '" + data.get("plaza") + "'"+" || '"+ data.get("tienda") + "'" + 
				" AND B.DOC_TYPE = 'REC' AND STATUS = 'I'" + 
				" AND C.PID_ID = B.ID" + 
				" AND C.PV_CVE_MVT = 10" + 
				" AND C.EXT_REF_NO <> '0'" + 
				" AND C.PARTITION_DATE >= TRUNC(SYSDATE-7)" + 
				" AND C.ORDER_NO = 0";
		
		
		String datosPosDETL = "SELECT  ITEM, RECEIVED_QTY,  NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION "
				+ " FROM POSUSER.POS_REC_DETL"
				+ " WHERE PID_ID = '%s'";
		
		String FTP = "select ftp_base_dir, ftp_serverhost, ftp_serverport, "
				+ " ftp_username, ftp_password as ftp_password " + " from   WMUSER.wm_ftp_connections "
				+ " where  ftp_conn_id = 'RTKRMS'";
		

		String tdcQueryIntegrationServer = "SELECT * FROM wmlog.wm_log_run" + 
				" WHERE interface = 'PR28main'" + 
				" AND start_dt>=TRUNC(SYSDATE)" + 
				" ORDER BY start_dt DESC";
		
		
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA


		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";
		
		String datosPos2 = "select * from (SELECT id, pe_id, pv_doc_id, status, doc_type"
				+ "FROM POSUSER.POS_INBOUND_DOCS WHERE SUBSTR(PV_DOC_NAME,4,10) ='" + data.get("plaza") + "'"
				+ "AND DOC_TYPE = 'REC' AND STATUS = 'E' order by received_date desc) where rownum <=3";
		
	
		
		
		
// Paso 1 ************************
		addStep("Tener tiendas disponibles para procesar ");
		
		SQLResult tienda = dbPos.executeQuery(tiendas);
		System.out.println("Respuesta tienda " + tiendas);

		boolean validacionpaso1 = tienda.isEmpty();
		if (!validacionpaso1) {
			testCase.addQueryEvidenceCurrentStep(tienda);
		}
		assertFalse("No hay insumos", validacionpaso1);
		
// Paso 2 ************************
		addStep("Tener datos disponibles para procesar en las tablas de POSUSER  ");
		
		SQLResult POS = dbPos.executeQuery(datosPos);
		System.out.println("Respuesta " + datosPos);
		String ID = POS.getData(0, "ID");
		
		boolean validacionpaso2 = POS.isEmpty();
		if (!validacionpaso2) {
			testCase.addQueryEvidenceCurrentStep(POS);
		}
		assertFalse("no hay insumos", validacionpaso2);
		
// Paso 3 ************************
		addStep("Verificar que existan detalles de los documentos a procesar en POSUSER");
		
		String DETL = String.format(datosPosDETL, ID);
		SQLResult DET = dbPos.executeQuery(DETL);
		System.out.println("Respuesta " + DETL);

		boolean validacionpaso3 = DET.isEmpty();
		if (!validacionpaso3) {
			testCase.addQueryEvidenceCurrentStep(DET);
		}
		assertFalse("no hay insumos", validacionpaso3);
		
// Paso 4 ************************
		addStep("Verificar la configuracion FTP");
		
		SQLResult FT = dbPos.executeQuery(FTP);
		System.out.println("Respuesta " + FT);

		boolean validacionpaso4 = FT.isEmpty();
		if (!validacionpaso4) {
			testCase.addQueryEvidenceCurrentStep(FT);
		}
		assertFalse("Esta mal configurado el FTP", validacionpaso4);
		
// Paso 5 ************************

		addStep("Invocar la interfaz PR28.Pub:run ");
		
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String run_id;

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		
		
		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			

			u.hardWait(2);

		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();
			

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(result3);

			}
		}

// Paso 6 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run debe existir un registro con el campo status en \"S\".");
		
		
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String verificacionInterface = String.format(consulta5, run_id);
			SQLResult paso4 = executeQuery(dbLog, verificacionInterface);
			System.out.println(verificacionInterface);



			boolean av5 = paso4.isEmpty();
			
			if (!av5) {

				testCase.addQueryEvidenceCurrentStep(paso4);
				
			} 

			System.out.println(av5);

			
			assertFalse(av5, "No se obtiene informacion de la consulta");
//			

//Paso 7  *************************************************
		addStep("Validar que se inserte el detalle de la ejecucion de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTemp6 = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso5 = executeQuery(dbLog, consultaTemp6);
		
		System.out.println(consultaTemp6);
		String estatusThread = paso5.getData(0, "Status");

		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {

			testCase.addQueryEvidenceCurrentStep(paso5);
			
		} 

		System.out.println(SR);

		
		assertFalse(SR, "No se obtiene informacion de la consulta");

// Paso 8 ************************
		
		addStep("Ver los archivos procesados en las tablas de POSUSER  ");
		SQLResult POS2 = dbPos.executeQuery(datosPos2);
		System.out.println("Respuesta " + datosPos2);
		// String ID = POS.getData(0, "ID");
		boolean paso22 = POS2.isEmpty();
		if (!paso22) {
			testCase.addQueryEvidenceCurrentStep(POS2);
		}

			}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar conexion por FTP transferencia de archivos APO";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
