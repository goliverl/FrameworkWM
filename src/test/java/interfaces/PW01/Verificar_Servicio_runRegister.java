package interfaces.PW01;


import java.util.HashMap;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PW01;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Verificar_Servicio_runRegister extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PW01_003_Verificar_Servicio_runRegister(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT * FROM WMLOG.WM_LOG_RUN\r\n" 
		        + " WHERE INTERFACE = 'PW01'\r\n"
				+ " AND STATUS = 'S'\r\n" 
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)\r\n" 
				+ " ORDER BY RUN_ID DESC";
		
		String tdcQRY2 = " SELECT * FROM POSUSER.WM_LOG_INSTAL_POS \r\n"
				+ " WHERE PV_CR_PLAZA = '" + data.get("Plaza") + "' \r\n"
				+ " AND PV_CR_TIENDA = '" +data.get("Tienda") + "' \r\n"
				+ " AND ID = '" + data.get("id") +	 "' \r\n";

		PW01 PW01util = new PW01(data, testCase, dbFCWMQA);

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Ejecutar el servicio PW01.Pub:runRegister por HTTP.");

		String respuestaRunRegister = PW01util.runRegister();

		System.out.println("Respuesta: " + respuestaRunRegister);

		/* Paso 2 *********************************************************/

		addStep("Validar que el registro de la operación se inserta en la tabla WM_LOG_INSTAL_POS_DETL de la BD de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQRY1);

		SQLResult Paso2 = dbLog.executeQuery(tdcQRY1);

		boolean Paso2Empty = Paso2.isEmpty();
		System.out.println(Paso2Empty);
		if (Paso2Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		/* Paso 3 *********************************************************/

		addStep("Verificar en el XML de respuesta, se proporcione el folio_id de la operación.");

		/* Paso 4 *********************************************************/

		addStep("Validar que se inserte el registro de la operación en la tabla WM_LOG_INSTAL_POS de la BD de POSUSER.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY2);

		SQLResult Paso4 = dbFCWMQA.executeQuery(tdcQRY2);

		boolean Paso4Empty = Paso4.isEmpty();
		System.out.println(Paso4Empty);
		if (!Paso4Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "INterface de Monitoreo de INstalador de POS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PW01_003_Verificar_Servicio_runRegister";
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
