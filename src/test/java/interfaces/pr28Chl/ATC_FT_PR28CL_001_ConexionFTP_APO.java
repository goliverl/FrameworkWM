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

public class ATC_FT_PR28CL_001_ConexionFTP_APO extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28CL_001_ConexionFTP_APO_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile,GlobalVariables.DB_PASSWORD_LogChile);
		
		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String Paso1 = " SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS \r\n"
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n"
				+ " WHERE B.PE_ID = A.ID \r\n"
				+ " AND B.DOC_TYPE = 'REC' \r\n"
				+ " AND B.STATUS IN ('I') \r\n"
				+ " AND C.PID_ID = B.ID \r\n"
				+ " AND C.PV_CVE_MVT = '10' \r\n"
				+ " AND C.EXT_REF_NO <> '0' \r\n"
				+ " AND C.ORDER_NO = 0 \r\n"
				+ " AND B.PARTITION_DATE >= TRUNC(SYSDATE-7) \r\n"
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS \r\n";
		
		String Paso2 = " SELECT B.ID, TO_CHAR (C.CREATED_DATE, 'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO, SUPPLIER \r\n"  
				+ " FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n" 
				+ " WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '" + data.get("plaza") + "'"+" || '"+ data.get("tienda") + "' \r\n"  
				+ " AND B.DOC_TYPE = 'REC' AND STATUS = 'I' \r\n"  
				+ " AND C.PID_ID = B.ID \r\n" 
				+ " AND C.PV_CVE_MVT = 10 \r\n" 
				+ " AND C.EXT_REF_NO <> '0' \r\n" 
				+ " AND C.PARTITION_DATE >= TRUNC(SYSDATE-7) \r\n"  
				+ " AND C.ORDER_NO = 0 \r\n";
		
		
		String Paos3 = "SELECT  ITEM, RECEIVED_QTY,  NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION \r\n"
				+ " FROM POSUSER.POS_REC_DETL \r\n"
				+ " WHERE PID_ID = '%s' \r\n";
		
		String Paso4 = " Select ftp_base_dir, ftp_serverhost, ftp_serverport, \r\n"
				+ " ftp_username, ftp_password as ftp_password \r\n" 
				+ " from   WMUSER.wm_ftp_connections \r\n"
				+ " where  ftp_conn_id = 'RTKRMS' \r\n";
		
		String Paso6 = " SELECT * FROM wmlog.wm_log_run \r\n"  
				+ " WHERE interface = 'PR28_CL' \r\n" 
				+ " AND start_dt>=TRUNC(SYSDATE) \r\n"  
				+ " ORDER BY start_dt DESC \r\n";
		
		String Paso7 = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n" 
				+ " WHERE parent_id = %s \r\n"; // FCWMLQA
		
		
		String Paso8 = " Select * from (SELECT id, pe_id, pv_doc_id, status, doc_type \r\n"
				+ " FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ " WHERE SUBSTR(PV_DOC_NAME,4,10) ='" + data.get("plaza") + "' \r\n"
				+ " AND DOC_TYPE = 'REC' \r\n"
				+ " AND STATUS = 'E' \r\n"
				+ " order by received_date desc) where rownum <=3 \r\n";
		
// Paso 1 ************************
		addStep("Tener tiendas disponibles para procesar ");

		System.out.println(Paso1);
		SQLResult tienda = dbPos.executeQuery(Paso1);
		boolean validacionpaso1 = tienda.isEmpty();
		if (!validacionpaso1) {
			testCase.addQueryEvidenceCurrentStep(tienda);
		}
		assertFalse("no hay insumos", validacionpaso1);

// Paso 2 ************************
		
		addStep("Tener datos disponibles para procesar en las tablas de POSUSER  ");

		System.out.println(Paso2);
		SQLResult POS = dbPos.executeQuery(Paso2);
		String ID = "";

		boolean validacionpaso2 = POS.isEmpty();
		if (!validacionpaso2) {
			ID = POS.getData(0, "ID");
			System.out.println (ID);
			testCase.addQueryEvidenceCurrentStep(POS);
		}
		assertFalse("no hay insumos", validacionpaso2);

// Paso 3 ************************
		addStep("Verificar que existan detalles de los documentos a procesar en POSUSER");

		String DETL = String.format(Paos3,ID);
		SQLResult DET = dbPos.executeQuery(DETL);
		System.out.println(DETL);

		boolean validacionpaso3 = DET.isEmpty();
		if (!validacionpaso3) {
			testCase.addQueryEvidenceCurrentStep(DET);
		}
		assertFalse("no hay insumos", validacionpaso3);

// Paso 4 ************************
		addStep("Verificar la configuraci�n FTP");

		SQLResult FT = dbPos.executeQuery(Paso4);
		System.out.println(FT);
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
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

// Paso 6 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run deber� existir un registro con el campo status en \"S\".");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(Paso6);
		SQLResult paso6 = executeQuery(dbLog, Paso6);		
		String runid = "";
		boolean av6 = paso6.isEmpty();	
		if (!av6) {
	        runid = paso6.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso6);
		}
		System.out.println(av6);
		assertFalse(av6, "No se obtiene informacion de la consulta");			

//Paso 7  *************************************************
		
		addStep("Validar que se inserte el detalle de la ejecuci�n de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTemp7 = String.format(Paso7, runid);
		SQLResult paso7 = executeQuery(dbLog, consultaTemp7);
		System.out.println(consultaTemp7);
		boolean SR = paso7.isEmpty();
		if (!SR) {
			testCase.addQueryEvidenceCurrentStep(paso7);
		}

		System.out.println(SR);
		assertFalse(!SR, "No se obtiene informacion de la consulta");

// Paso 8 ************************

		addStep("Ver los archivos procesados en las tablas de POSUSER  ");
		SQLResult POS2 = dbPos.executeQuery(Paso8);
		System.out.println(Paso8);
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
		return "Verificar conexion por FTP transferencia de archivos APO de Chile";
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
