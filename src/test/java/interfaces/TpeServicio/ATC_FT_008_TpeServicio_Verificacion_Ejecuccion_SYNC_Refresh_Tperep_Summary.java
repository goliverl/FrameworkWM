package interfaces.TpeServicio;

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

public class ATC_FT_008_TpeServicio_Verificacion_Ejecuccion_SYNC_Refresh_Tperep_Summary extends BaseExecution{
	
	/*
	 * 
	 * @cp Verificar ejecucion de SYNC - REFRESH_TPEREP_SUMMARY
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_TpeServicio_Verificacion_Ejecuccion_SYNC_Refresh_Tperep_Summary_test (HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		
/** VARIABLES *********************************************************************/	
				
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		String tdcPaso2 = " SELECT DATE1 FROM TPE_CONFIG \r\n"
				+ " WHERE CATEGORY = 'TPE_SUM_TABLE' \r\n"
				+ " AND CODE = 'FR'\r\n";
			
		String tdcPaso3 = "SELECT * FROM TPE_FR_SUMMARY \r\n"
				+ " WHERE TRUNC(CREATION_DATE)=TRUNC(SYSDATE-1) \r\n";
			

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Ejecutar la interface con los parametros indicados\r\n");
		SeleniumUtil u;
		PakageManagment pok;
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		System.out.println(contra);
	
		String dateExecution = pok.runIntefaceWmTextBox(data.get("interfase"), data.get("servicio"));
		
		System.out.println(dateExecution);

		/* PASO 2 *********************************************************************/

		addStep(" Verificar que la ejecucion haya terminado con exito. ");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcPaso3);

		SQLResult Paso2 = executeQuery(dbPos, tdcPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		System.out.println(ValPaso2);

		assertFalse(ValPaso2, " No se muestran registros a procesar ");

		/* PASO 3 *********************************************************************/

		addStep("Verificar la insercion de datos procesados en la tabla TPE_FR_SUMMARY.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcPaso3);

		SQLResult Paso3 = executeQuery(dbPos, tdcPaso3);
		boolean ValPaso3 = Paso3.isEmpty();

		if (!ValPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		System.out.println(ValPaso3);

		assertFalse(ValPaso3, " No se muestran registros a procesar ");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar ejecucion de SYNC - REFRESH_TPEREP_SUMMARY";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_008_TpeServicio_Verificacion_Ejecuccion_SYNC_Refresh_Tperep_Summary_test";
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
