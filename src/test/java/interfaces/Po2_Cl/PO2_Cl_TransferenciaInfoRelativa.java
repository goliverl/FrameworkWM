package interfaces.Po2_Cl;

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

public class PO2_Cl_TransferenciaInfoRelativa extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO2_CL_001_Transferencia_Info_Relativa(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,
				GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,
    		GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
	    utils.sql.SQLUtil dbOie = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT a.id, pe_id, pv_doc_id, status, doc_type, backup_status, pv_doc_name,target_id \n"
				+ " FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B, POSUSER.POS_HEF_DETL H \n"
				+ " WHERE A.DOC_TYPE = 'HEF' AND \n"
				+ " A.STATUS= 'I' AND \n"
				+ " H.PID_ID = A.ID AND \n" 
				+ " SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA AND \n"
				+ " B.PAIS = 'CL' AND \n"
				+ " B.CR_PLAZA = '" + data.get("plaza") + "' AND \n"
				+ " H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV', 'EECI', 'ERCI', 'ENCI', 'TTRV') \n";

				

		String tdcQueryPaso3 = "SELECT * FROM WMLOG.WM_LOG_RUN \n"
				+ " WHERE INTERFACE = 'PO2_CL' AND \n"
				+ " TRUNC(END_DT) = TRUNC(SYSDATE) AND \n"
				+ " STATUS = 'S' \n"
				+ " ORDER BY RUN_ID DESC \n";
				

		String tdcQueryPaso4 = "SELECT * FROM WMLOG.WM_LOG_THREAD \n"
				+ " WHERE PARENT_ID = %s\n"
				+ " AND ATT1 = '" + data.get("plaza") + "' \n"
				+ " ORDER BY THREAD_ID DESC \n";
				

		String tdcQueryPaso5 = " SELECT ID,SUBSTR(PV_DOC_NAME,4,5) AS PLAZA, DOC_TYPE, STATUS, TARGET_ID \n"
				+ " FROM POSUSER.POS_INBOUND_DOCS \n"
				+ " WHERE DOC_TYPE = 'HEF' \n"
				+ " AND STATUS = 'E' \n"
				+ " AND SUBSTR(PV_DOC_NAME,4,5) ='" + data.get("plaza") + "'\n"
				+ " AND ID =%s\n";
				

		String tdcQueryPaso6 = "SELECT * FROM apps.GL_INTERFACE \n"
				+ " WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \n"
				+ " AND SEGMENT3 = '" + data.get("plaza") + "' \n"
				+ " AND STATUS = 'NEW' \n"
				+ " AND REFERENCE6 = %s\n";
				
//utileria

		String user = data.get("user");
		
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1 ***************************************
		addStep("Consultar que exitan registros en las tablas POS_INBOUND_DOCS de la BD POSUSER \n"
				+ "con DOC_TYPE igual a 'HEF', Plaza igual a '10APO' y STATUS igual a 'I' y PAIS igual a 'Chile'");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPos, tdcQueryPaso1); //dbPos
		
		String id = Paso1.getData(0, "id");
		
		boolean ValidacionPaso1 = Paso1.isEmpty();
		if (!ValidacionPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);


		}

		assertFalse(ValidacionPaso1, "No se encuentran resultados");

//paso 2 *********************************************

		addStep("Se ejecuta el proceso PO2.Pub:run.\n");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		
		System.out.println(contra);
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 3	******************************************

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'PO2_cl' y STATUS es igual a 'S'");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbLog, tdcQueryPaso3);
		
		String thread = Paso3.getData(0,"RUN_ID");
		boolean validacionPaso3 = Paso3.isEmpty();
		if (!validacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(validacionPaso3, "La ejecución de la interfaz no fue exitosa");

//Paso 4	***********************************

		addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S' .\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		
		 String FormatoPaso4 = String.format(tdcQueryPaso4, thread) ;
		
		System.out.println(FormatoPaso4);
		SQLResult Paso4 = executeQuery(dbLog, FormatoPaso4);
		boolean validacionPaso4 = Paso4.isEmpty();
		if (!validacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

		assertFalse(validacionPaso4, "No se tiene unformacion en la base de datos");

//Paso 5 **********************************

		addStep("Comprobar que los documentos fueron actualizados correctamente en la tabla "
				+ " POS_INBOUND_DOCS en la BD POSUSER donde WMSTATUS igual a 'E', Plaza igual a '10APO'.\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String FormatoPaso5 = String.format(tdcQueryPaso5, id);
		
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = executeQuery(dbLog, FormatoPaso5); //dbPos
		
		String target = Paso5.getData(0, "TARGET_ID");
		boolean validacionPaso5 = Paso5.isEmpty();
		if (!validacionPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

		assertFalse(validacionPaso5, "No se tiene unformacion en la base de datos ");

//Paso 6 *********************************

		addStep("Comprobar que los registros se insertaron correctamente en la Tabla GL_INTERFACE en la BD ORAFIN donde SEGMENT3  es igual a '10APO");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String FormatoPaso6 = String.format(tdcQueryPaso6, target);
		
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = executeQuery(dbLog, FormatoPaso6); //dbOie
		boolean validacionPaso6 = Paso6.isEmpty();
		if (!validacionPaso6) {

			testCase.addQueryEvidenceCurrentStep(Paso6);
		}

		assertFalse(validacionPaso6, "No se tiene unformacion en la base de datos ");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO2_CL_001_Transferencia_Info_Relativa";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. La interface se encarga de transferir la información relativa a las formas de pago de la tiendas de Chile (POS) hacia la instancia Oracle GL de Chile";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAutomation";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "- Tener ejecutada la PR50 " + "- Tener el job para su ejecución en Control-M";
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
