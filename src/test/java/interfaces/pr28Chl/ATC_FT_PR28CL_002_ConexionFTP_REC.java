package interfaces.pr28Chl;

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


public class ATC_FT_PR28CL_002_ConexionFTP_REC extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28CL_002_ConexionFTP_REC_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 *********************************************************************/
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile,GlobalVariables.DB_PASSWORD_LogChile);
		
		/**
		 * ALM
		 * Conexion por FTP - REC
		 */

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		

		
		String Paso1 = " SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, C.PID_ID \r\n" 
				+ " FROM POSUSER.POS_ENVELOPE A,POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n" 
				+ " WHERE B.PE_ID = A.ID \r\n"
				+ " AND B.DOC_TYPE = 'REC' \r\n"
				+ " AND B.STATUS IN ('L') \r\n"
				+ " AND C.PID_ID = B.ID \r\n"
				+ " AND C.PV_CVE_MVT = '10' \r\n"
				+ " AND C.EXT_REF_NO <> '0' \r\n"
				+ " AND C.ORDER_NO = 0 \r\n"
				+ " AND B.PARTITION_DATE >= TRUNC(SYSDATE-7) \r\n"
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, C.PID_ID \r\n";
		
		String Paso2 = " SELECT TARGET_ALT_ID \r\n"
				+ " FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n"
				+ " WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '%s' \r\n"
				+ " AND B.DOC_TYPE='REC' \r\n"
				+ " AND STATUS IN ('L') \r\n"
				+ " AND C.PID_ID=B.ID \r\n"
				+ " AND C.PV_CVE_MVT=10 \r\n"
				+ " AND C.ORDER_NO=0 \r\n"
				+ " AND C.EXT_REF_NO<>'0' \r\n"
				+ " GROUP BY TARGET_ALT_ID \r\n";
		
		String Paso3 = " SELECT B.ID, TO_CHAR(CREATED_DATE,'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO \r\n"
				+ " FROM POS_INBOUND_DOCS B, POS_REC C \r\n"
				+ " WHERE SUBSTR(B.PV_DOC_NAME, 4, 10) = '%s' \r\n"  
				+ " AND B.DOC_TYPE='REC' \r\n"
				+ " AND B.TARGET_ALT_ID is %s \r\n" 
				+ " AND STATUS IN ('L') \r\n"
				+ " AND C.PID_ID=B.ID \r\n"
				+ " AND C.PV_CVE_MVT=10 \r\n"
				+ " AND C.ORDER_NO=0 \r\n" 
				+ " AND C.EXT_REF_NO<>'0' \r\n";
		
		String Paso4 = " SELECT ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION \r\n"
				+ " FROM POSUSER.POS_REC_DETL \r\n"
				+ " WHERE PID_ID = %s \r\n";
		
		String Paso5 = " Select ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password as ftp_password \r\n" 
				+ " from  WMUSER.wm_ftp_connections \r\n"
				+ " where  ftp_conn_id = 'RTKRMS' \r\n";
		
		String Paso7 = " Select * from ( SELECT run_id,start_dt,status \r\n"
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR28_CL' \r\n"
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n"
			    + " order by start_dt desc) \r\n"
				+ " where rownum = 1 \r\n";
		
		String Paso8 = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = %s \r\n" ; 
		
		
		
// Paso 1 ************************

		addStep("Tener tiendas disponibles para procesar en POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		SQLResult tiendaResult = dbPos.executeQuery(Paso1);
		System.out.println(Paso1);
		String tienda = "";
		String plaza = "";
		String plazatienda = "";
		String pid_id = "";

		boolean paso1 = tiendaResult.isEmpty();
		if (!paso1) {

			tienda = tiendaResult.getData(0, "PV_CR_TIENDA");
			plaza = tiendaResult.getData(0, "PV_CR_PLAZA");
			plazatienda = plaza + tienda;
			pid_id = tiendaResult.getData(0, "PID_ID");

			testCase.addQueryEvidenceCurrentStep(tiendaResult);

		}

		assertFalse("No se obtiene informacion de la consulta", paso1);

// Paso 2 ************************

		addStep("Tener datos disponibles para procesar en las tablas de POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		String datosFormat = String.format(Paso2, plazatienda);
		System.out.println(datosFormat);
		SQLResult datosResult = dbPos.executeQuery(datosFormat);
		String target = "";

		boolean datos = datosResult.isEmpty();
		if (!datos) {

			target = datosResult.getData(0, "TARGET_ALT_ID");
			testCase.addQueryEvidenceCurrentStep(datosResult);

		}

		assertFalse("No se obtiene informacion de la consulta", datos);

// Paso 3 ************************

		addStep("Confirmar que exista informacion para procesar en POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		String infoFormat = String.format(Paso3, plazatienda, target);
		System.out.println(infoFormat);
		SQLResult infoResult = dbPos.executeQuery(infoFormat);
		boolean info = infoResult.isEmpty();
		if (!info) {
			testCase.addQueryEvidenceCurrentStep(infoResult);

		}

		assertFalse("No se obtiene informacion de la consulta", info);

// Paso 4 *************************

		addStep("Verificar que exista informacion para procesar en POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String detlFormat = String.format(Paso4, pid_id);
		System.out.println(detlFormat);
		SQLResult detlResult = dbPos.executeQuery(detlFormat);

		boolean detl = detlResult.isEmpty();
		if (!detl) {

			testCase.addQueryEvidenceCurrentStep(detlResult);

		}

		assertFalse("No se obtiene informacion de la consulta", detl);

//paso 5 *************************

		addStep("Verificar la configuracion FTP.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(Paso5);
		SQLResult FTPResult = dbPos.executeQuery(Paso5);
		boolean ftp = FTPResult.isEmpty();
		if (!ftp) {

			testCase.addQueryEvidenceCurrentStep(FTPResult);

		}

		assertFalse("No se obtiene informacion de la consulta", ftp);

// paso 6 **************************
		addStep("Invocar la interfaz PR28.Pub:run.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555" ;
		u.get(contra);
		System.out.println(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

// paso 7 **************************

		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(Paso7);
		SQLResult ResultadoPaso7 = dbPos.executeQuery(Paso7);
		boolean ValPaso7 = FTPResult.isEmpty();

		String run_id = "";
		if (!ValPaso7) {

			run_id = ResultadoPaso7.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(ResultadoPaso7);

		}

		assertFalse("No se obtiene informacion de la consulta", ValPaso7);

// paso 8 ***************************
		addStep("Se valida la generacion de thread.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String statusThreadFormat = String.format(Paso8, run_id);
		System.out.println(statusThreadFormat);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);
		boolean ST = threadResult.isEmpty();
		ST = !ST;

		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);

		}

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");

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
		return "Verificar conexion por FTP transferencia de archivos REC de chile.";
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

}
