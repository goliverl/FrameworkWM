package interfaces.fl3;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.FL3;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_FL3_002_Info_Embarques_Salientes extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL3_002_Info_Embarques_Salientes_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utiler�as
		 *********************************************************************/

		SQLUtil dbRDM = new SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,
				GlobalVariables.DB_PASSWORD_RDM);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
	    SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		FL3 FL3Util = new FL3(data, testCase, dbRDM);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String run_id = "";

		String ValidEjecucion = "select * from (SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'FL3_Receive' " + "AND STATUS = 'S' " + "AND TRUNC(START_DT) = TRUNC(SYSDATE) "
				+ "ORDER BY RUN_ID DESC) where rownum <=1";
		

		String ValidInserc = "SELECT WM_RUN_ID, FL_CEDIS, FL_TSF,FL_GRUPO, FL_NUM_CHOFER, FL_NOMBRE_CHOFER, WM_STATUS,WM_RECEIVE_DATE "
				+ " FROM wmuser.WM_CHOFER_TRANSFER "
				+ "WHERE WM_STATUS = 'L' " + "AND FL_CEDIS = '"+data.get("PORT_OF_LOADING")+"' "
				+ " AND WM_RUN_ID = '%s'";

		

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//Paso 1 *************************	

		addStep("Se envia una peticion por HTTP al servicio: wm.tn:receive");

		String respuesta = FL3Util.EjecutaFL3();
		System.out.print("Doc: " + respuesta);

//		Paso 2 ******************************************************************************************
	
		addStep("Validar que la interfaz se ejecut� correctamente con estatus S en WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ValidEjecucion);
		SQLResult queryInfPlaza = dbLog.executeQuery(ValidEjecucion);

		boolean inf = queryInfPlaza.isEmpty();
		if (!inf) {
			run_id = queryInfPlaza.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(queryInfPlaza);
		}

		System.out.println(inf);
		assertFalse(inf, "No se obtiene informacion de la consulta");

//		Paso 3 ************************************************************************************************************
		
		addStep("Validar que se inserto correctamente la informaci�n en la tabla WM_CHOFER_TRANSFER de WMUSER con estatus L.");

		System.out.println(GlobalVariables.DB_HOST_RDM);
		String ValdiFormat = String.format(ValidInserc, run_id);
		System.out.println(ValdiFormat);
		SQLResult Validacion = dbPuser.executeQuery(ValdiFormat);

		boolean validinf = Validacion.isEmpty();
		if (!validinf) {
			testCase.addQueryEvidenceCurrentStep(Validacion);
		}

		System.out.println(validinf);
		assertFalse(validinf, "No se obtiene informacion de la consulta");

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
		return "Validar que se inserte la informaci�n de los embarques salientes del CEDIS";
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
