package interfaces.Po9_Cl;

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

public class ATC_FT_001_PO9_CL_Valida_Envio_Pagos_Electronicos_CL extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO9_CL_Valida_Envio_Pagos_Electronicos_CL_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbOie = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,GlobalVariables.DB_PASSWORD_OIEBSBDQ);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT B.ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA,TO_CHAR(T.PV_DATE, 'YYYYMMDD') PV_DATE \r\n"
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_TPE_DETL T \r\n"
				+ " WHERE A.ID  = B.PE_ID \r\n" 
				+ " AND B.ID    = T.PID_ID \r\n" 
				+ " AND B.DOC_TYPE = 'TPE' \r\n"
				+ " AND B.STATUS   = 'I' \r\n" 
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") + "' \r\n" 
				+ " GROUP BY B.ID, \r\n"
				+ " A.PV_CR_PLAZA, \r\n"
				+ " A.PV_CR_TIENDA,\r\n"
				+ " T.PV_DATE \r\n ";

		String tdcQueryPaso3 = " Select * from ( SELECT run_id,start_dt,status \r\n" 
		        + " FROM WMLOG.wm_log_run \r\n"
		        + " WHERE interface = '" + data.get("Run_interface") + "' \r\n" 
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n"
				+ " order by start_dt desc) \r\n" 
				+ " where rownum = 1 \r\n ";

		String tdcQueryPaso4 = " SELECT id,status,doc_type,pv_doc_name,target_id \r\n" 
		        + " FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ " WHERE STATUS = 'E' \r\n" 
				+ " AND TARGET_ID IS NOT NULL \r\n" 
				+ " AND PE_ID = '%s' \r\n";

		String tdcQueryPaso5 = " SELECT status, currency_code, date_created, created_by, actual_flag, user_je_category_name \r\n"
				+ " FROM GL.GL_INTERFACE \r\n"
				+ " WHERE 1=1 \r\n" 
				+ " AND TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n"
				+ " AND REFERENCE6 = '%s'\r\n"
				+ " AND  SUBSTR(REFERENCE1,0,5)='" + data.get("plaza") + "'"; 


//utileria

		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
	
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1 
		addStep("Validar documentos TPE disponibles para procesar en POSUSER.\n");
				

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPos, tdcQueryPaso1);
		String id = Paso1.getData(0,"ID");
		boolean ValidaPaso1 = Paso1.isEmpty();
		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidaPaso1, "No se encuentran resultados");

//paso 2
		addStep("Ejecutar  el servicio PO9.Pub:run.", "Ejecucion de la interfaz sin error ");

		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		
//paso 3    ************************
		
		addStep("Verificar estatus de ejecucion de la interfaz");
		
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbLog, tdcQueryPaso3);
		boolean ValidacionPaso3 = Paso3.isEmpty();
		if (!ValidacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse(ValidacionPaso3, "No se encuentran resultados");
		
		
//Paso 4	************************

		addStep("Verificar el estatus de los documentos enviados.");
		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String FormatoPaso4  = String.format(tdcQueryPaso4,id);
		System.out.println(FormatoPaso4);
		SQLResult Paso4 = executeQuery(dbPos, FormatoPaso4);
		String target = "";
		boolean ValidacionPaso4 = Paso4.isEmpty();
		if (!ValidacionPaso4) {
			target = Paso4.getData(0, "TARGET_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidacionPaso4, "No se encuentran resultados");

//Paso 5	************************

		addStep("Verificar la informacion en FINANZAS.");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		
		String FormatoPaso5 = String.format(tdcQueryPaso5, target);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = executeQuery(dbOie, FormatoPaso5);
		boolean ValidacionPaso5 = Paso5.isEmpty();
		if (!ValidacionPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidacionPaso5, "No se encuentran resultados");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Construido.Validar la operacion para el envio de formas de pago Electronicas de la tienda POS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "- Tener ejecutada la PR50 " + "- Tener el job para su ejecucion en Control-M";
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
