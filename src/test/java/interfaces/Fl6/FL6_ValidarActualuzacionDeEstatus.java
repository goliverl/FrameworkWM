package interfaces.Fl6;

import modelo.BaseExecution;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class FL6_ValidarActualuzacionDeEstatus extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRdm = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM, GlobalVariables.DB_PASSWORD_RDM);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		SeleniumUtil u;		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
	
/** VARIABLES *********************************************************************/	
		
		String tdcQueryPaso1 = " SELECT * FROM wmuser.WM_RDM_CONNECTIONS"
				+ " WHERE RETEK_CR = '" + data.get("Retek_CR") + "'"
				+ " AND TRANSACTION_TYPE IN ('NT', 'XA')"
				+ " AND STATUS = 'A' "; //dbPos
				
		
		String tdcQueryPaso2 = " SELECT * FROM wmuser.WM_ARRIVE_STORE"
				+ " WHERE WM_STATUS = 'I' "
				+ " AND WM_RETRIES = 2"
				+ " AND FL_CDS = '"+ data.get("Retek_CR") +"' "; //dbPos
				
		
		String tdcQueryPaso4 = " SELECT * FROM WMLOG.WM_LOG_RUN "
				+ " WHERE INTERFACE = 'FL6_Send' AND STATUS = 'E'"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ " ORDER BY RUN_ID DESC"; //dbLog
		
		String tdcQueryPaso5 = " SELECT * FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID = %s"; //LOog Run //dbLog
		
		String tdcQueryPaso6 =" SELECT * FROM RDM100.WM_ARRIVE_STORE"
				+ " WHERE WM_STATUS = 'M' "
				+ " AND WM_RETRIES = 3"
				+ " AND WM_RUN_ID =%s" //[WM_LOG_THREAD.THREAD_ID]
				+ " AND FL_CDS = '" + data.get("Retek_CR") + "'"; //dbRdm
				
		
		
		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Validar que existen conexiones para un CEDIS de Prueba en la tabla WM_RDM_CONNECTIONS con estatus A.");

		System.out.println(tdcQueryPaso1);
		SQLResult connectionResultPaso1 = executeQuery(dbPos, tdcQueryPaso1);

		boolean connectionPaso1 = connectionResultPaso1.isEmpty();

		if (!connectionPaso1) {

			testCase.addQueryEvidenceCurrentStep(connectionResultPaso1);

		}

		System.out.println(connectionPaso1);

		assertFalse(connectionPaso1, "La tabla no contiene información.");

		/* PASO 2 *********************************************************************/

		addStep("Validar que existe información con el campo WM_STATUS igual a I y WM_RETRIES igual a 2 en la tabla "
				+ "WM_ARRIVE_STORE de WMUSER para el CEDIS de Prueba.");

		System.out.println(tdcQueryPaso2);
		SQLResult connectionResultPaso2 = executeQuery(dbPos, tdcQueryPaso2);

		boolean connectionPaso2 = connectionResultPaso2.isEmpty();

		if (!connectionPaso2) {

			testCase.addQueryEvidenceCurrentStep(connectionResultPaso2);

		}

		System.out.println(connectionPaso2);

		assertFalse(connectionPaso2, "La tabla no contiene información.");

		/* PASO 3 *********************************************************************/

		addStep("Ejecutar el servicio: FL6.Pub:runSend.");

		u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("Servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);

		/* PASO 4 *********************************************************************/

		addStep("Validar que la interfaz se ejecutó con estatus E en WMLOG.");

		System.out.println(tdcQueryPaso4);
		SQLResult connectionResultPaso4 = executeQuery(dbLog, tdcQueryPaso4);

		String run_id = connectionResultPaso4.getData(0, "RUN_ID");

		boolean connectionPaso4 = connectionResultPaso4.isEmpty();

		if (!connectionPaso4) {

			testCase.addQueryEvidenceCurrentStep(connectionResultPaso4);

		}

		System.out.println(connectionPaso4);

		assertFalse(connectionPaso4, "La tabla no contiene información.");

		/* PASO 5 *********************************************************************/

		addStep("Validar que el Thread para el Cedis procesado se haya generado.");

		String Formato5 = String.format(tdcQueryPaso5, run_id);

		System.out.println(Formato5);
		SQLResult connectionResultPaso5 = executeQuery(dbLog, Formato5);

		String thread = connectionResultPaso5.getData(0, "THREAD_ID");

		boolean connectionPaso5 = connectionResultPaso5.isEmpty();

		if (!connectionPaso5) {

			testCase.addQueryEvidenceCurrentStep(connectionResultPaso5);

		}

		System.out.println(connectionPaso5);

		assertFalse(connectionPaso5, "La tabla no contiene información.");

		/* PASO 6 *********************************************************************/

		addStep("Validar que el campo WM_RETRIES en la tabla WM_ARRIVE_STORE de "
				+ "WMUSER haya aumentado a 1 y el campo WM_STATUS se actualizó a M.");

		String Formtato6 = String.format(tdcQueryPaso6, thread);

		System.out.println(Formtato6);
		SQLResult connectionResultPaso6 = executeQuery(dbRdm, Formtato6);

		boolean connectionPaso6 = connectionResultPaso6.isEmpty();

		if (!connectionPaso6) {

			testCase.addQueryEvidenceCurrentStep(connectionResultPaso6);

		}

		System.out.println(connectionPaso6);

		assertFalse(connectionPaso6, "La tabla no contiene información.");
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
		return "Interfase que envia la informacion recibida de FL con los estimados de arriba a  tiendas de los caminos.";
	}

	@Override
	public String setTestDesigner() {
// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
// TODO Auto-generated method stub
		return "FL6_Send Validar  Actualizaion de estatus al alcanxar maximo de intentos en CEdis";
	}

	@Override
	public String setTestInstanceID() {
// TODO Auto-generated method stub
		return null;
	}

}
