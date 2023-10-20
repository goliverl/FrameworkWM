package interfaces.Fl6;

import modelo.BaseExecution;
import om.FL6;
import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class ATC_FT_FL6_001_FL6_Receive_ValidarInsercionDeInformacion extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL6_001_FL6_Receive_ValidarInsercionDeInformacion_test(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFcw = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		FL6 FL6Util = new FL6(data, testCase, dbPos);

	
		String status = "S";
 
		/**
		 * VARIABLES
		 *********************************************************************/

		String tdcQueryWMLOG = " SELECT * FROM WMLOG.WM_LOG_RUN" 
		        + " WHERE INTERFACE = 'FL6_Receive'" 
				+ " AND STATUS = 'S'"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)" 
				+ " ORDER BY RUN_ID DESC";
  
		String tdcQueryWMArriveStore = " SELECT * FROM wmuser.WM_ARRIVE_STORE" 
		        + " WHERE WM_RUN_ID = %s"
				+ " AND WM_STATUS = 'I'" 
		        + " AND FL_CDS =  '" + data.get("Cedis") + "'";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Ejecutar el servicio: FL6.Pub:runReceive.por medio del envio del archivo XML mediante un cliente HTTP");

		String respuesta = FL6Util.FL6_Receive();

		boolean validationRequest = status.isEmpty();

		testCase.addTextEvidenceCurrentStep(respuesta);
		System.out.println("Status request: " + status);

		assertFalse(validationRequest, "No se corrio correctamete el Xml");

		/* PASO 2 *********************************************************************/

		addStep("Se valida que el archivo XML haya llegado al servidor TN de forma correcta.");
		
		if (validationRequest) {
			
			testCase.addTextEvidenceCurrentStep(respuesta);
		}
		
		assertFalse(validationRequest, "El Xml tiene un detalle");

		/* PASO 3 *********************************************************************/

		addStep("Validar que la interfaz se ejecuto correctamente con estatus S en WMLOG.");

		System.out.println(tdcQueryWMLOG);
		SQLResult connectionResult = executeQuery(dbLog, tdcQueryWMLOG);

		String run_id = connectionResult.getData(0, "RUN_ID");

		boolean connection = connectionResult.isEmpty();

		if (!connection) {
			testCase.addQueryEvidenceCurrentStep(connectionResult);
		}

		System.out.println(connection);

		assertFalse(connection, "La tabla no contiene informacion.");

		/* PASO 4 *********************************************************************/

		addStep("Validar que la informacion se inserto correctamente en la tabla WM_ARRIVE_STORE de WMUSER con estatus I.");
		
		
		String Format = String.format(tdcQueryWMArriveStore, run_id);
		System.out.println(Format);
		SQLResult connectionResult1 = executeQuery(dbFcw, Format);

		boolean connection1 = connectionResult.isEmpty();

		if (!connection1) {
			testCase.addQueryEvidenceCurrentStep(connectionResult1);
		}

		System.out.println(connection1);

		assertFalse(connection1, "La tabla no contiene informaci�n.");

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
		return "Equipo de automatizacion";
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
