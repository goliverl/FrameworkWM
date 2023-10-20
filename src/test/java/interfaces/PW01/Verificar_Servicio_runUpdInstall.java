package interfaces.PW01;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import om.PW01;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Verificar_Servicio_runUpdInstall extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PW01_004_Verificar_Servicio_runUpdInstall(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		PW01 PW01util = new PW01(data, testCase, dbFCWMQA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = "SELECT * FROM POSUSER.WM_LOG_INSTAL_POS\r\n"
				+ "WHERE WM_STATUS = 'R' \r\n"
				+ "AND CREATION_DATE < SYSDATE - ( 15/1440 ) \r\n";
		
		String tdcQRY3= " SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PW01' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY RUN_ID DESC \r\n";
				
		String tdcQRY4= " SELECT * FROM POSUSER.WM_LOG_INSTAL_POS\r\n"
				+ " WHERE WM_STATUS = 'E' \r\n"
				+ " AND STATUS = '0' \r\n"
			//	+ "AND ID = %s \r\n";
                + " order by Creation_date desc \r\n";  
		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existan registros pendientes por procesar para la interface PW01 en la tabla WM_LOG_INSTAL_POS de POSUSER.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCWMQA.executeQuery(tdcQRY1);
		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(!Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Ejecutar el servicio PW01.Pub:runUpdInstall por HTTP.\r\n");
		
		String respuestaRunRegister = PW01util.runUpdInstall();

		System.out.println("Respuesta: " + respuestaRunRegister);
				

		/* Paso 3 *********************************************************/

		addStep("Validar que se inserte el registro de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQRY3);

		SQLResult Paso3 = dbLog.executeQuery(tdcQRY3);
		boolean Paso3Empty = Paso3.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso3Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(Paso3Empty, "No se tiene informacion en la base datos");
		
		
		/* Paso 4 *********************************************************/

		addStep("Validar que se actualice el status de la información procesada a WM_STATUS = 'E' y STATUS = '0' en la tabla WM_LOG_INSTAL_POS de POSUSER.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY4);

		SQLResult Paso4 = dbFCWMQA.executeQuery(tdcQRY4);
		boolean Paso4Empty = Paso4.isEmpty();
		System.out.println(Paso4Empty);
		if (!Paso4Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

		assertFalse(Paso4Empty, "No se tiene informacion en la base datos");



	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "INterface de Monitoreo de Instalador de Pos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PW01_004_Verificar_Servicio_runUpdInstall";
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
