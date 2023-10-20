package interfaces.Po16;

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

public class Po16_Insercion_Informacion_para_Plaza_en_Orafin extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO16_001_Insercion_Informacion_para_Plaza_en_Orafin(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * PO15
		 * 
		 */

		String tdcQry1 = " SELECT  DISTINCT  ID AS PID_ID, SUBSTR(a.PV_DOC_NAME, 9,5) PV_CR_TIENDA \r\n"
				+ " FROM POSUSER.POS_INBOUND_DOCS A , POSUSER.POS_MTJ B \r\n"
				+ " WHERE a.STATUS = 'I' \r\n"
				+ " AND A.DOC_TYPE = 'MTJ' \r\n"
				+ " AND SUBSTR(A.PV_DOC_NAME,  4,5) = '" +data.get("plaza")+ "' \r\n"
				+ " AND A.id = B.PID_ID";
				

		String tdcQry2 = " SELECT * FROM POSUSER.pos_mtj_detl \r\n"
				+ " WHERE PID_ID = %s \r\n" ;
				

		String tdcQry4 = " SELECT * FROM wmlog.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PO16'\r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(SYSDATE) = TRUNC(START_DT) \r\n"
				+ " ORDER BY RUN_ID DESC \r\n" ;

		String tdcQry4_1 = " SELECT * FROM wmlog.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = %s \r\n"
				+ "AND STATUS = 'S' AND ATT2 = '"+data.get("plaza") +"' \r\n ";// WMLOG
		
		String tdcQry5 = " SELECT * FROM XXFC.XXFC_MTJ_HEADER \r\n"
				+ " WHERE ID = %s \r\n"
				+ "AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE) \r\n";
				
		String tdcQry5_1 = " SELECT * FROM XXFC.XXFC_MTJ_DETAIL \r\n"
				+ " WHERE CR_PLAZA = '" +data.get("tienda")+ "' \r\n"
				+ " AND MTJ_HDR_ID = %s \r\n";
				

		String tdcQry6 = " SELECT * FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ " WHERE STATUS = 'A' AND DOC_TYPE = 'MTJ' \r\n"
				+ " AND TARGET_ID = %s \r\n";
				


		// utileria

		String thread = null;

//	Paso 1	************************		

		addStep("Tener información para procesar en las tablas: POS_INBOUND_DOCS y POS_MTJ en POSUSER para la plaza 10DCU.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQry1); // get shipment
		SQLResult servQueryExePaso_1 = dbPos.executeQuery(tdcQry1);
		String PID_ID = servQueryExePaso_1.getData(0, "PID_ID");
		boolean validacionPaso1 = servQueryExePaso_1.isEmpty();
		if (!validacionPaso1) {
			testCase.addQueryEvidenceCurrentStep(servQueryExePaso_1);
		}
		System.out.println(validacionPaso1);
		assertFalse(validacionPaso1, "No hay insumos a procesar");

// paso 2 ************************

		addStep("Tener información detalle en la tabla POS_MTJ_DETL por cada documento a procesar.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String Formato_Paso2 = String.format(tdcQry2, PID_ID);

		System.out.println(Formato_Paso2); // get shipment
		SQLResult servQueryExePaso_2 = dbPos.executeQuery(Formato_Paso2);
		boolean validacionServ_Paso2 = servQueryExePaso_2.isEmpty();
		if (!validacionServ_Paso2) {
			testCase.addQueryEvidenceCurrentStep(servQueryExePaso_2);
		}
		System.out.println(validacionServ_Paso2);
		assertFalse(validacionServ_Paso2, "No hay insumos a procesar");

//		Paso 3	************************

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		addStep("Tener información detalle en la tabla POS_MTJ_DETL por cada documento a procesar.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//		Paso 4	************************

		addStep("Validar que la interface concluyó sin errores en WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQry4);

		SQLResult servQueryExePaso_4 = dbLog.executeQuery(tdcQry4);

		String run_id = servQueryExePaso_4.getData(0, "RUN_ID");

		boolean validacionServ_Paso4 = servQueryExePaso_4.isEmpty();
		if (!validacionServ_Paso4) {
			testCase.addQueryEvidenceCurrentStep(servQueryExePaso_4);

			String FormatoPaso4 = String.format(tdcQry4_1, run_id);

			System.out.println(FormatoPaso4);

			SQLResult servQueryExePaso_4_1 = dbLog.executeQuery(FormatoPaso4);

			thread = servQueryExePaso_4_1.getData(0, "PARENT_ID");

			boolean validacionServ_Paso4_1 = servQueryExePaso_4_1.isEmpty();

			if (!validacionServ_Paso4_1) {
				testCase.addQueryEvidenceCurrentStep(servQueryExePaso_4_1);
			}

			System.out.println(validacionServ_Paso4_1);
			assertFalse(validacionServ_Paso4_1, "No hay insumos a procesar");

		}
		System.out.println(validacionServ_Paso4);
		assertFalse(validacionServ_Paso4, "No hay insumos a procesar");

//      paso 5 *********************************

		addStep("Validar que se insertó correctamente la información en las tablas XXFC_MTJ_HEADER y XXFC_MTJ_DETAIL de ORAFIN");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5 = String.format(tdcQry5, PID_ID);

		System.out.println(FormatoPaso5);

		SQLResult servQueryExePaso_5 = dbEbs.executeQuery(FormatoPaso5);

		String Mtjhdrid = servQueryExePaso_5.getData(0, "MTJ_HDR_ID");

		boolean validacionServ_Paso5 = servQueryExePaso_5.isEmpty();
		if (!validacionServ_Paso5) {
			testCase.addQueryEvidenceCurrentStep(servQueryExePaso_5);

			String FormatoPaso5_1 = String.format(tdcQry5_1, Mtjhdrid);

			System.out.println(FormatoPaso5_1);

			SQLResult servQueryExePaso_5_1 = dbEbs.executeQuery(FormatoPaso5_1);
			boolean validacionServ_Paso5_1 = servQueryExePaso_5_1.isEmpty();

			if (!validacionServ_Paso5_1) {
				testCase.addQueryEvidenceCurrentStep(servQueryExePaso_5_1);
			}

			System.out.println(validacionServ_Paso5_1);
			assertFalse(validacionServ_Paso5_1, "No hay insumos a procesar");

		}
		System.out.println(validacionServ_Paso5);
		assertFalse(validacionServ_Paso5, "No hay insumos a procesar");

//  paso 6 *******************************************

		addStep("Validar que el estatus de los campos STATUS y TARGET_ID fueron actualizados correctamente en la tabla POS_INBOUND_DOCS de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String consultaPaso6 = String.format(tdcQry6, thread);
		System.out.print("" + consultaPaso6);
		SQLResult servQueryExePaso_6 = dbPos.executeQuery(consultaPaso6);

		boolean validacionServ_Paso6 = servQueryExePaso_6.isEmpty();
		if (!validacionServ_Paso6) {
			testCase.addQueryEvidenceCurrentStep(servQueryExePaso_6);
		}
		assertFalse("No hay información.", validacionServ_Paso6);
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Envio de  Trnsacciones de WM a Oracle MTJ ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO16_001_Insercion_Informacion_para_Plaza_en_Orafin";
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
