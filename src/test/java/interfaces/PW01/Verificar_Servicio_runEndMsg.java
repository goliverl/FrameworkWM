package interfaces.PW01;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PW01;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Verificar_Servicio_runEndMsg extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PW01_001_Verificar_Servicio_runEndMsg(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = "SELECT * FROM POSUSER.WM_LOG_INSTAL_POS\r\n"
				+ " WHERE PV_CR_PLAZA = '" + data.get("Plaza") + "' \r\n"
				+ " AND PV_CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " AND ID = '" +  data.get("id")+"' \r\n";
		
		PW01 PW01util= new PW01(data, testCase, dbFCWMQA); 

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCWMQA.executeQuery(tdcQRY1);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Ejecutar el servicio PW01.Pub:runEndMsg por HTTP.\r\n");
		
		String respuestaTRN01 = PW01util.runEndMsg();
		
		System.out.println("Respuesta: " + respuestaTRN01);
	
        testCase.passStep();
		

		/* Paso 3 *********************************************************/

		addStep("Validar la actualización de la información en la tabla WM_LOG_INSTAL_POS de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY1);

		SQLResult Paso3 = dbFCWMQA.executeQuery(tdcQRY1);

		boolean Paso3Empty = Paso3.isEmpty();
		System.out.println(Paso3Empty);
		if (!Paso3Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso3);
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
		return "Interface de Momitoreo de Instalador de POS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PW01_001_Verificar_Servicio_runEndMsg";
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
