package interfaces.PW01;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PW01;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Verificar_Servicio_runRecMSg extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PW01_002_Verificar_Servicio_runRecMSg(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);


		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT * FROM POSUSER.WM_LOG_INSTAL_POS_DETL \r\n"
				+ " WHERE ID = " + data.get("id") + "  \r\n"
				+ " AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE) \r\n";
		
		PW01 PW01util = new PW01(data, testCase, dbFCWMQA);
		
		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Ejecutar el servicio PW01.Pub:runRecMsg por HTTP.");

		String respuestaTRN01 = PW01util.runRecMsg();
		
		System.out.println("Respuesta: " + respuestaTRN01);
		
		 

		/* Paso 2 *********************************************************/

		addStep("Validar que el registro de la operación se inserta en la tabla WM_LOG_INSTAL_POS_DETL de la BD de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String formatotdcQry1 = String.format(tdcQRY1);

		System.out.println(formatotdcQry1);

		SQLResult Paso2 = dbFCWMQA.executeQuery(formatotdcQry1);

		boolean Paso2Empty = Paso2.isEmpty();
		System.out.println(Paso2Empty);
		if (!Paso2Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		System.out.println("Paso3");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Interface de Monitoreo de Instalador e POS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PW01_002_Verificar_Servicio_runRecMSg";
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
