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

public class EO4_Actualizar_Solicitudes_Mantenimiento_Estatus_Fallida_Procesar extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_EO4_Act_Solicitudes_Mto_Estatus_Fallida(HashMap<String, String> data) throws Exception {

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
		 * ALM (son los mismos pasos que de mexico, solo cambian las bases de datos)
		 * Actualizar las solicitudes de mantenimiento con estatus de fallida a lista para procesar de la tienda 10MEX50P8F
		 */
		

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " UPDATE CC_EAM_INTERFACE SET WM_STATUS = 'F' \r\n"
				+ " WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " AND FOLIO_CC = [FOLIO_CC]\r\n" ;
				

		String tdcQueryPaso3 = " SELECT * FROM WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = '" + data.get("wm_log") + "' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC\r\n";


		String tdcQueryPaso4 = " SELECT * FROM CC_EAM_INTERFACE \r\n"
				+ " WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " AND WM_STATUS = 'R' \r\n";
				
			
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
		addStep("Marcar las solicitudes de servicio capturadas en el Call Center de la tienda de la tabla CC_EAM_INTERFACE a estatus fallida (WM_STATUS = 'F').\r\n");
				

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPos, tdcQueryPaso1);
		boolean ValidacionPaso1 = Paso1.isEmpty();
		if (!ValidacionPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidacionPaso1, "No se encuentran resultados");

//paso 2 *********************************************

		addStep("Ejecutar la interface EO4.Pub:runReprocesar para procesar las solicitudes de la tienda, y marcarlas como listas para trasferir hacia Oracle EAM.\r\n");
				

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 3	******************************************

		addStep("Validar la correcta ejecucion de la interface EO4 en la tabla WM_LOG_RUN de WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbLog, tdcQueryPaso3);
		boolean ValidacionPaso3 = Paso1.isEmpty();
		if (!ValidacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse(ValidacionPaso3, "No se encuentran resultados");
		

//Paso 4	***********************************

		addStep(" Validar la actualizacion del WM_STATUS = 'R' de las solicitudes en la tabla "
				+ " CC_EAM_INTERFACE de la base de Call Center para el procesamiento hacia Oracle EAM.\r\n");
			
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso4);
		SQLResult Paso4 = executeQuery(dbPos, tdcQueryPaso4);
		boolean ValidacionPaso4 = Paso4.isEmpty();
		if (!ValidacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidacionPaso4, "No se encuentran resultados");
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_EO4_Act_Solicitudes_Mto_Estatus_Fallida";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Bloqueado. Actualizar la informaci�n de las solicitudes de mantenimiento con estatus de "
				+ "fallida a lista para procesar de la tienda.";
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
		return "- Tener ejecutada la PR50 " + "- Tener el job para su ejecuci�n en Control-M";
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
