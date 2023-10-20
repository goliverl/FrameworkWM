package interfaces.EO4;

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

public class EO4_Bloquear_Solicitudes_Mantenimiento_Tienda_Donde_No_Aplique_Servicio_CallCenter extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_EO4_Bloq_Solicitudes_Mto_No_Aplica_Call_Center(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,
				GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,
				GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbOie = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);

		/**
		 * ALM (son los pasos de la interfaz de mexico con bases de datos de chile)
		 * Bloquear las solicitudes de mantenimiento de la tienda 10TLC501H5 donde no aplique el servicio de Call Center
		 */
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT * FROM WM_DISTRITOS_VALIDOS_CC \r\n"
				+ " WHERE CR_SUPERIOR = SUBSTR('" + data.get("tienda") + "', 0,5) " ;
				

		String tdcQueryPaso2 = " UPDATE CC_EAM_INTERFACE SET WM_STATUS = 'L'\r\n"
				+ " WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n";
				
				
		String tdcQueryPaso4 = " SELECT * FROM WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = '" + data.get("wm_log") + "' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC \r\n";
				
		
		String tdcQueryPaso5 = "SELECT * FROM CC_EAM_INTERFACE \r\n"
				+ "WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ "AND WM_STATUS = 'H' \r\n"
				+ "AND WM_RUN_ID = '%s' \r\n";
				
//utileria

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1 ***************************************
		addStep(" Validar en la tabla WM_DISTRITOS_VALIDOS_CC de WMINT que la Plaza de la tienda "
				+ " no se encuentre configurada para el servicio de Call Center.\r\n");
			
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPos, tdcQueryPaso1);
		boolean ValidacionPaso1 = Paso1.isEmpty();
		if (!ValidacionPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidacionPaso1, "No se encuentran resultados");

//paso 2 *********************************************

		addStep(" Marcar las solicitudes de servicio capturadas en el Call Center "
				+ " de la tienda de la tabla CC_EAM_INTERFACE a WM_STATUS = 'L'.\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso2);
		SQLResult Paso2 = executeQuery(dbPos, tdcQueryPaso2);
		boolean ValidacionPaso2 = Paso2.isEmpty();
		if (!ValidacionPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		assertFalse(ValidacionPaso2, "No se encuentran resultados");		

		

//Paso 3	******************************************

		addStep(" Ejecutar la interface EO4.Pub:runBloqTienda para bloquear las solicitudes "
				+ " de la tienda donde la plaza no aplica el servicio de Call Center.\r\n");
				

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 4	***********************************

		addStep("Validar la correcta ejecuci�n de la interface EO4 en la tabla WM_LOG_RUN de WMLOG.");
			
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso4);
		
		SQLResult Paso4 = executeQuery(dbPos, tdcQueryPaso4);
		String runId = "";
		
		boolean ValidacionPaso4 = Paso4.isEmpty();
		if (!ValidacionPaso4) {
			runId = Paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidacionPaso4, "No se encuentran resultados");	
		
//Paso 5	***********************************

		addStep(" Validar la actualizacion del WM_STATUS = 'H' en la tabla CC_EAM_INTERFACE de la base de "
				+ " Call Center para evitar el procesamiento hacia Oracle EAM de las solicitudes.\r\n");
				
		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso5);
		String paso5Query = String.format(tdcQueryPaso5, runId);
		SQLResult Paso5 = executeQuery(dbPos, paso5Query);
		boolean ValidacionPaso5 = Paso5.isEmpty();
		if (!ValidacionPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidacionPaso5, "No se encuentran resultados");	
		
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_EO4_Bloq_Solicitudes_Mto_No_Aplica_Call_Center";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Bloqueado. Bloquear la informaci�n de las solicitudes de mantenimiento de la tienda, "
				+ "donde la plaza a la que pertenece la tienda, no aplique el servicio de Call Center.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAutomation";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "- Tener ejecutada la PR50 " + "- Tener el job para su ejecucion en Control-M";
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
