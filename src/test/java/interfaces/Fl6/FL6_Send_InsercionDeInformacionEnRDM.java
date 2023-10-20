package interfaces.Fl6;

import modelo.BaseExecution;
import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class FL6_Send_InsercionDeInformacionEnRDM extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil RDM = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,
				GlobalVariables.DB_PASSWORD_RDM);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFcw = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
	
		
/** VARIABLES *********************************************************************/	
		
		String tdcQueryPaso1 = " SELECT * FROM wmuser.WM_ARRIVE_STORE \r\n"
				+ " WHERE WM_STATUS = 'I' \r\n"
				+ " AND FL_CDS = '" + data.get("Cedis")  +"' \r\n";
		
		String tdcQueryPaso3 = " SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'FL6_Send' AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC \r\n";
				
		String tdcQueryPaso4 = " SELECT * FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID = %s \r\n";
				
		String tdcQueryPaso5 = " SELECT * FROM wmuser.WM_ARRIVE_STORE \r\n"
				+ " WHERE WM_STATUS = 'E' AND \r\n"
				+ " WM_RUN_ID = %s \r\n"
				+ " AND FL_CDS = '" + data.get("Cedis")  +"' \r\n";
				
		String tdcQueryPaso6 =" SELECT * FROM RDM100.XXFC_ARRIVE_STORE_FL \r\n"
				+ " WHERE WM_RUN_ID = %s \r\n"
				+ " AND WM_STATUS = 'I' \r\n"
				+ " AND CEDIS = '" + data.get("Cedis") + "' \r\n";
		
		
		SeleniumUtil u;		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Validar que existe informacion con estatus I en la tabla WM_ARRIVE_STORE de WMUSER.");
		
		System.out.println(tdcQueryPaso1);
		SQLResult connectionResultPaso1= executeQuery(dbFcw, tdcQueryPaso1);
		

		boolean connectionPaso1 = connectionResultPaso1.isEmpty();

		if (!connectionPaso1) {
			
			testCase.addQueryEvidenceCurrentStep(connectionResultPaso1);
			
		}

		System.out.println(connectionPaso1);

		assertFalse(connectionPaso1, "La tabla no contiene informaci�n.");

	

		/* PASO 2 *********************************************************************/

		addStep("Ejecutar el servicio: FL6.Pub:runSend");
		
		u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		System.out.println(contra);
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));		
		System.out.println("Respuesta dateExecution " + dateExecution);
		
		/* PASO 3 *********************************************************************/

		addStep("EValidar que la interfaz se ejecuto correctamente con estatus S en WMLOG.");

		System.out.println(tdcQueryPaso3);
		SQLResult connectionResultPaso3 = executeQuery(dbLog, tdcQueryPaso3);
		
		String id_run = connectionResultPaso3.getData(0, "RUN_ID");

		boolean connectionPso3 = connectionResultPaso3.isEmpty();

		if (!connectionPso3) {
			testCase.addQueryEvidenceCurrentStep(connectionResultPaso3);
		}

		System.out.println(connectionPso3);

		assertFalse(connectionPso3, "La tabla no contiene informacion.");

		/* PASO 4 *********************************************************************/

		addStep("Validar que el Thread para el Cedis procesado se haya generado.");
		
	    String FormatoPaso4 = String.format(tdcQueryPaso4, id_run);
		System.out.println(FormatoPaso4);
		SQLResult ThreadPaso4 = executeQuery(dbLog, FormatoPaso4);
        String HiloPaso4 = ThreadPaso4.getData(0, "THREAD_ID");  
       
		boolean connectionPaso4 = ThreadPaso4.isEmpty();

		if (!connectionPaso4) {
			testCase.addQueryEvidenceCurrentStep(ThreadPaso4);
		}

		System.out.println(connectionPaso4);

		assertFalse(connectionPaso4, "La tabla no contiene informacion.");

		/* PASO 5 *********************************************************************/

		addStep("Validar que los registros procesados de la tabla WM_ARRIVE_STORE actualizaron el campo WM_STATUS a E.");
		
	    String FormatoPaso5 = String.format(tdcQueryPaso5, HiloPaso4);
		System.out.println(FormatoPaso5);
		SQLResult ConectionPaso5 = executeQuery(dbFcw, FormatoPaso5);


		boolean connectionPaso5 = ConectionPaso5.isEmpty();

		if (!connectionPaso5) {
			testCase.addQueryEvidenceCurrentStep(ConectionPaso5);
		}

		System.out.println(connectionPaso5);

		assertFalse(!connectionPaso5, "La tabla no contiene informacion.");
		

		/* PASO 6 *********************************************************************/

		addStep("Validar que la informacion se insert� correctamente en la tabla XXFC_ARRIVE_STORE_FL de RDM.");
		
		 String FormatoPaso6 = String.format(tdcQueryPaso6, HiloPaso4);
			System.out.println(FormatoPaso6);
			SQLResult ConexionPaso6 = executeQuery(RDM, FormatoPaso6);


			boolean connectionPaso6 = ConexionPaso6.isEmpty();

			if (connectionPaso6) {
				testCase.addQueryEvidenceCurrentStep(ConexionPaso6);
			}

			System.out.println(connectionPaso6);

			assertFalse(connectionPaso6, "La tabla no contiene informacion.");

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
		return "FL6_Send_InsercionDeInformacionEnRDM";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
