package interfaces.pr28Chl;

import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;



public class ATC_FT_PR28CL_004_PublicacionExitosa extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28CL_004_PublicacionExitosa_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 *********************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile,GlobalVariables.DB_PASSWORD_LogChile);
              /**
              * ALM
              * Publicación exitosa de los documentos - APO.
              */
		
		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		String Paso1 = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, C.PID_ID \r\n"
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n"
				+ " WHERE B.PE_ID = A.ID \r\n"
				+ " AND B.DOC_TYPE = 'REC' \r\n"
				+ " AND B.STATUS IN ('I') \r\n"
				+ " AND C.PID_ID = B.ID  \r\n"
				+ " AND C.PV_CVE_MVT = '10' \r\n"
				+ " AND C.EXT_REF_NO <> '0' \r\n"
				+ " AND C.ORDER_NO = 0 \r\n"
				+ " AND B.PARTITION_DATE >= TRUNC(SYSDATE-7) \r\n"
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, C.PID_ID \r\n";
		
		String Paso2 = "SELECT B.ID, TO_CHAR (C.CREATED_DATE, 'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO, SUPPLIER \r\n"
				+ " FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C \r\n"
				+ " WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '%s' \r\n"
				+ " AND B.DOC_TYPE = 'REC' \r\n"
				+ " AND STATUS = 'I'  \r\n"
				+ " AND C.PID_ID = B.ID  \r\n"
				+ " AND C.PV_CVE_MVT = 10  \r\n"
				+ " AND C.EXT_REF_NO <> '0'  \r\n"
				+ " AND C.PARTITION_DATE >= TRUNC(SYSDATE-7) \r\n"
				+ " AND C.ORDER_NO = 0 \r\n";
		
		String Paso3 = "SELECT  ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION \r\n"
				+ " FROM POSUSER.POS_REC_DETL \r\n"
				+ " WHERE PID_ID = %s \r\n";
		
		String Paso5 = "select * from ( SELECT run_id,start_dt,status \r\n"
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR28_CL' \r\n"
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n"
			    + " order by start_dt desc) \r\n"
				+ " where rownum = 1 \r\n";
		
		String Paso6 = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = %s \r\n" ; 
	
		
		
// Paso 1 ************************

		addStep("Tener tiendas disponibles para procesar.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(Paso1);
		SQLResult tiendaResult = dbPos.executeQuery(Paso1);
		String tienda = "";
		String plaza = "";
		String plazatienda = "";
		String pid_id = "";

		boolean tiendasDisponibles = tiendaResult.isEmpty();

		if (!tiendasDisponibles) {

			tienda = tiendaResult.getData(0, "PV_CR_TIENDA");
			plaza = tiendaResult.getData(0, "PV_CR_PLAZA");
			plazatienda = plaza + tienda;
			pid_id = tiendaResult.getData(0, "PID_ID");

			testCase.addQueryEvidenceCurrentStep(tiendaResult);
		}

		assertFalse("No se obtiene informacion de la consulta", tiendasDisponibles);

// Paso 2 ***********************

		addStep("Tener datos disponibles para procesar en las tablas de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String datosFormat = String.format(Paso2, plazatienda);
		System.out.println(datosFormat);
		SQLResult datosResult = dbPos.executeQuery(datosFormat);

		boolean datos = datosResult.isEmpty();
		if (!datos) {

			testCase.addQueryEvidenceCurrentStep(datosResult);

		}
		assertFalse("No se obtiene informacion de la consulta", datos);

//Paso 3 ***********************
		
		addStep("Comprobar que existan detalles para los registros que seran procesados.");
		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String detlFormat = String.format(Paso3, pid_id);
		System.out.println(detlFormat);
		SQLResult detlResult = dbPos.executeQuery(detlFormat);

		boolean detl = detlResult.isEmpty();
		if (!detl) {

			testCase.addQueryEvidenceCurrentStep(detlResult);
		}

		assertFalse("No se obtiene informacion de la consulta", detl);

// Paso 4 ************************
		addStep("Invocar la interfaz PR28.Pub:run.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		

//paso 5 **********************
		
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(Paso5);
		SQLResult ResultadoPaso5 = dbLog.executeQuery(Paso5);
		
        String runid = ""; 
		boolean ValidaPaso5 = detlResult.isEmpty();
		if (!ValidaPaso5) {

			runid= ResultadoPaso5.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(ResultadoPaso5);
		}

		assertFalse("No se obtiene informacion de la consulta", ValidaPaso5);


// paso 6 ***************************
		
		addStep("Se valida la generacion de thread.");
		
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String FormatoPaso6 = String.format(Paso6, runid);
		System.out.println(FormatoPaso6);
		SQLResult ResultadoPaso6 = dbLog.executeQuery(FormatoPaso6);
		
		boolean ValidaPaso6 = detlResult.isEmpty();
		if (!ValidaPaso6) {

			testCase.addQueryEvidenceCurrentStep(ResultadoPaso6);
		}

		assertFalse("No se obtiene informacion de la consulta", ValidaPaso6);


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
		return "Verificar la publicacion de los documentos lanzados al procesar la informacion - APO Chile";
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
