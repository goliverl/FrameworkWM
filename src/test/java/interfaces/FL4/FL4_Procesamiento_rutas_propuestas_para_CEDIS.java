package interfaces.FL4;

import static org.testng.Assert.assertFalse;


import java.util.HashMap;


import org.testng.annotations.Test;


import modelo.BaseExecution;
import om.FL4;
import util.GlobalVariables;

import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class FL4_Procesamiento_rutas_propuestas_para_CEDIS extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL4_001_Procesar_Rutas_CEDIS(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbRDM = new SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,
				GlobalVariables.DB_PASSWORD_RDM);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		FL4 FL4Util = new FL4(data, testCase, dbRDM);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String run_id = "";

		String ValidEjecucion = "select * from (SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'FL4' " + "AND STATUS = 'S' " + "AND TRUNC(START_DT) = TRUNC(SYSDATE) "
				+ "ORDER BY RUN_ID DESC) where rownum <=1";

		String ValidInserc = "select * from (SELECT WM_RUN_ID,CEDIS,CRPLAZA,TIPO_DISTRO,RUTA_ORIGINAL,RUTA_PROPUESTA_FL FROM XXFC_RUTAS_PROP_FL "
				+ "WHERE CEDIS = '"+data.get("CEDIS") + "' AND WM_RUN_ID = %s " + "AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE) "
				+ "ORDER BY CREATE_DATE DESC) where rownum <=1";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//Paso 1 *************************	

		addStep("Se envia una petición por HTTP al servicio: wm.tn:receive");

		String respuesta = FL4Util.EjecutaFL4();
		System.out.print("Doc: " + respuesta + "aqui");

//		Paso 2 ******************************************************************************************

		addStep("Validar que la interfaz se ejecutó correctamente con estatus S en WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
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

		addStep("Validar que la información se insertó correctamente en la tabla XXFC_RUTAS_PROP_FL de RDM.");

		System.out.println(GlobalVariables.DB_HOST_RDM);
		String ValdiFormat = String.format(ValidInserc, run_id);
		System.out.println(ValdiFormat);
		SQLResult Validacion = dbRDM.executeQuery(ValdiFormat);

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
		return "Terminado_Validar que se procese la informacion correctamente de rutas propuestas para el CEDIS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_FL4_001_Procesar_Rutas_CEDIS";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
