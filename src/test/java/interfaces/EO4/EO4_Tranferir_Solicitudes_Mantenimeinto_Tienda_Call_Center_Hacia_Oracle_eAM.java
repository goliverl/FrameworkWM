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

public class EO4_Tranferir_Solicitudes_Mantenimeinto_Tienda_Call_Center_Hacia_Oracle_eAM extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_EO4_Transf_Solicitudes_Call_Center_Hacia_Oracle_EAM(HashMap<String, String> data) throws Exception {

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
		 * Transferir las solicitudes de mantenimiento de la tienda 10NLA50WYM del Call Center hacia Oracle eAM
		 */
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " UPDATE CC_EAM_INTERFACE SET WM_STATUS = 'R' \r\n"
				+ " WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " AND FOLIO_CC = [FOLIO_CC] \r\n";
				
		String tdcQueryPaso3 = "SELECT * FROM WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = '" + data.get("wm_log") + "' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC";
						
		String tdcQueryPaso4 = "SELECT * FROM REQS_MTTO_INTERFACE \r\n"
				+ " WHERE ORIGEN = 'CC' \r\n"
				+ " AND CR_TIENDA = [CC_EAM_INTERFACE.CR_TIENDA] \r\n"
				+ " AND FOLIO_REPARE = [CC_EAM_INTERFACE.FOLIO_CC] \r\n"
				+ " AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE) \r\n";
				
		String tdcQueryPaso5 = " SELECT * FROM CC_EAM_INTERFACE \r\n"
				+ " WHERE CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ " AND FOLIO_CC = [FOLIO_CC] \r\n"
				+ " AND WM_STATUS = 'E' \r\n"
				+ " AND WM_RUN_ID = '%s' \r\n";
				
				
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
		addStep(" Marcar las solicitudes de servicio capturadas en el Call Center de la "
				+ " tienda de la tabla CC_EAM_INTERFACE de XXCC a WM_STATUS = 'R'.\r\n");
			
			
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPos, tdcQueryPaso1);
		boolean ValidacionPaso1 = Paso1.isEmpty();
		if (!ValidacionPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidacionPaso1, "No se encuentran resultados");

//paso 2 *********************************************

		addStep(" Ejecutar la interface EO4.Pub:run con el job execEO4 de Ctrl-M para transferir las "
				+ " solicitudes de la tienda del Call Center hacia Oracle EAM.\r\n");
	
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		

//Paso 3	******************************************

		addStep("Validar la correcta ejecuci�n de la interface EO4 en la tabla WM_LOG_RUN de WMLOG.\r\n");

		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbPos, tdcQueryPaso3);
		boolean ValidacionPaso3 = Paso3.isEmpty();
		if (!ValidacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse(ValidacionPaso3, "No se encuentran resultados");
		

//Paso 4	***********************************

		addStep("Validar la inserci�n de las solicitudes de servicio de la tienda en la tabla REQS_MTTO_INTERFACE de Oracle EAM.\r\n");

		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso4);
		SQLResult Paso4 = executeQuery(dbPos, tdcQueryPaso4);
		boolean ValidacionPaso4 = Paso4.isEmpty();
		if (!ValidacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidacionPaso4, "No se encuentran resultados");
		
	
		
//Paso 5	***********************************

		addStep(" Validar la actualizaci�n de los datos fuente procesados en la base de datos de "
				+ " Call Center en la tabla CC_EAM_INTERFACE: WM_STATUS = 'E', WM_SENT_DATE = Fecha de Proceso, RUN_ID = Id de ejecuci�n de proceso.\r\n");
			
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso5);
		SQLResult Paso5 = executeQuery(dbPos, tdcQueryPaso5);
		boolean ValidacionPaso5 = Paso5.isEmpty();
		if (!ValidacionPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidacionPaso5, "No se encuentran resultados");
		
	
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_EO4_Transf_Solicitudes_Call_Center_Hacia_Oracle_EAM";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Bloqueado. Transferir la informaci�n de las solicitudes de mantenimiento nuevas y cerradas "
				+ "de la tienda reportadas al Call Center de Oficina de servicios de OXXO "
				+ "hacia el m�dulo de mantenimiento de Oracle (eAM).";
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
