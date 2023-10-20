package interfaces.Ro8Peru;

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
import utils.sql.SQLUtil;

public class Ro8_Peru_Ajustes extends BaseExecution { 
    @Test(dataProvider = "data-provider")

	public void ATC_FT_RO8_Peru_001_Peru_Ajustes(HashMap<String, String> data) throws Exception {

 
		/*
		 * Utilerías
		 *********************************************************************/
    	
		SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbRmsPeru = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru, GlobalVariables.DB_USER_RMSWMUSERPeru,
				GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);
		SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ); 
		/*
		 * Variables
		 *********************************************************************/
		
		String tdcQueryPaso1 = " SELECT * FROM WMUSER.WM_CFG_LAUNCHER \r\n" 
		        + " WHERE INTERFACE_NAME = 'RO8_PER' \r\n"
		        + " AND ATTRIBUTE1= '" + data.get("plaza") + "' \r\n" 
				+ " AND ATTRIBUTE2 = '" + data.get("tranCode") + "' \r\n"
				+ " AND AUTO_STATUS = 'A' \r\n";

		String tdcQueryPaso2 = " SELECT * \r\n" 
		        + " FROM FEM_FIF_STG" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60 \r\n"			
		        + " AND cr_plaza = '" + data.get("plaza") + "' \r\n" 			
		        + " AND tran_code = '" + data.get("tranCode")+ "' \r\n" 
		        + " AND reference_3 IS NULL \r\n" 
		        + " AND reference_9 IS NULL \r\n";

		String tdcQueryPaso4 = " Select * from ( SELECT run_id,start_dt,status \r\n" 
		        + " FROM WMLOG.wm_log_run \r\n"
		        + " WHERE interface = 'RO8_PER_AJUST' \r\n" 
		        + " AND START_DT >= TRUNC (SYSDATE) \r\n"
				+ " order by start_dt desc) \r\n" 
		        + " where rownum = 1 \r\n";

		String tdcQueryPaso4_1 = " SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 \r\n" 
		        + " FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID = %s \r\n"
				+ " ORDER BY THREAD_ID ASC \r\n" ;

		String tdcQueryPaso5 = " SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,NET_COST,GL_JOURNAL_ID,RUN_ID \r\n"
				+ " FROM WMUSER.WM_GL_HEADERS_PER \r\n" 
				+ " WHERE TRAN_CODE = " + data.get("tranCode") 
				+ " AND  RUN_ID= %s  \r\n"
				+ " AND CR_PLAZA = '" + data.get("plaza") + "' \r\n";

		String tdcQueryPaso6 = " SELECT ITEM, ITEM_DESC, TRAN_DATE, TRAN_CODE, REFERENCE_3, REFERENCE_9, ID \r\n"
				+ " FROM FEM_FIF_STG \r\n" 
				+ " WHERE CR_PLAZA = '" + data.get("plaza") + "' \r\n"
				+ " AND TRAN_CODE = " + data.get("tranCode") 
				+ " AND TRAN_DATE >= TRUNC (SYSDATE) - 60 \r\n"
				+ " AND REFERENCE_3 IS NOT NULL \r\n" 
				+ " AND REFERENCE_9 = '%s' \r\n";

		String tdcQueryPaso7 = " SELECT STATUS, DATE_CREATED, ACTUAL_FLAG,REFERENCE1, GROUP_ID \r\n"
				+ " FROM GL.GL_INTERFACE "
				+ " WHERE GROUP_ID = %s \r\n";

		String tdcQueryPaso9 = " SELECT HEADER_ID, TRAN_CODE, CR_PLAZA, GL_JOURNAL_ID, JOURNAL_TYPE_ID \r\n"
				+ " FROM WMUSER.WM_GL_HEADERS_PER_HIST \r\n" 
				+ " WHERE CR_PLAZA = '" + data.get("plaza") + "' \r\n"
				+ " AND TRAN_CODE = " + data.get("tran_code") 
				+ " AND HEADER_ID = %s \r\n";


		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que la plaza se encuentre configurada en la tabla WM_CFG_LAUNCHER de WMINT");

		System.out.println(GlobalVariables.DB_HOST_PosuserPeru);

		System.out.println(tdcQueryPaso1);

		SQLResult resultQueryPaso1 = executeQuery(dbPuserPeru, tdcQueryPaso1);

		boolean ValPaso1 = resultQueryPaso1.isEmpty();

		if (!ValPaso1) {

			testCase.addQueryEvidenceCurrentStep(resultQueryPaso1);
		}

		assertFalse(ValPaso1, "La plaza se encuentra configurada");

		/*
		 * Paso 2
		 *****************************************************************************************/

		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		System.out.println(tdcQueryPaso2);

		SQLResult resultQueryPaso2 = executeQuery(dbRmsPeru, tdcQueryPaso2);

		boolean ValPaso2 = resultQueryPaso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(resultQueryPaso2);

		}

		assertFalse(ValPaso2, "No existe información pendiente de procesar");

		/*
		 * Paso 3
		 *****************************************************************************************/

		addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runAJUST.\n"
				+ "Solicitando la ejecución del job: runRO8_MEX_AJUST.\n");

		SeleniumUtil u1, u2;
		PakageManagment pok1, pok2;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		testCase.setProject_Name("Interfaces Web Methods");
		testCase.setFullTestName("RO8_MEX_" + data.get("tran"));
		u1 = new SeleniumUtil(new ChromeTest(), true);
		pok1 = new PakageManagment(u1, testCase);

		addStep("Ejecutar la interfaz RO8 con el servicio " + data.get("servicio"));

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		System.out.println(contra);
		u1.get(contra);

		pok1.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

		/*
		 * Paso 4
		 *****************************************************************************************/

		addStep("Verificar que la ejecución termina con éxito.\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQueryPaso4);

		SQLResult resultQueryPaso4 = executeQuery(dbLogPeru, tdcQueryPaso4);

		boolean ValPaso4 = resultQueryPaso4.isEmpty();

		String runid = "";

		if (!ValPaso4) {

			runid = resultQueryPaso4.getData(0, "run_id");
			testCase.addQueryEvidenceCurrentStep(resultQueryPaso4);

		}

		assertFalse(ValPaso4, "No existe información pendiente de procesar");
		/*
		 * Paso 4-1
		 *****************************************************************************************/

		addStep("Verificar que la ejecución termina con éxito.\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String FormatoPaso4_1 = String.format(tdcQueryPaso4_1, runid);

		System.out.println(FormatoPaso4_1);

		SQLResult resultQueryPaso4_1 = executeQuery(dbLogPeru, FormatoPaso4_1);

		boolean ValPaso4_1 = resultQueryPaso4_1.isEmpty();

		String threaid = "";

		if (!ValPaso4_1) {

			threaid = resultQueryPaso4_1.getData(0, "thread_id");
			testCase.addQueryEvidenceCurrentStep(resultQueryPaso4_1);

		}

		assertFalse(ValPaso4_1, "No existe información pendiente de procesar");

		/*
		 * Paso 5
		 *****************************************************************************************/

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		String FormatoPaso5 = String.format(tdcQueryPaso5, threaid);

		System.out.println(FormatoPaso5);

		SQLResult resultQueryPaso5 = executeQuery(dbRmsPeru, FormatoPaso5);

		boolean ValPaso5 = resultQueryPaso5.isEmpty();

		String header_id = "";

		if (!ValPaso5) {

			header_id = resultQueryPaso5.getData(0, "header_id");
			testCase.addQueryEvidenceCurrentStep(resultQueryPaso5);

		}

		assertFalse(ValPaso5, "No existe información pendiente de procesar");

		/*
		 * Paso 6
		 *****************************************************************************************/

		addStep("Verificar la actualización de los campos:  REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		String FormatoPaso6 = String.format(tdcQueryPaso6, header_id);

		System.out.println(FormatoPaso6);

		SQLResult resultQueryPaso6 = executeQuery(dbRmsPeru, FormatoPaso6);

		String reference3 = "";

		boolean ValPaso6 = resultQueryPaso6.isEmpty();

		if (!ValPaso6) {

			resultQueryPaso6.getData(0, "reference_3");
			testCase.addQueryEvidenceCurrentStep(resultQueryPaso6);
		}

		assertFalse(ValPaso6, "No se actualizaron los campos REFERENCE_3 y REFERENCE_9.");

		/*
		 * Paso 7
		 *****************************************************************************************/

		addStep("Verificar la inserción de lineas en la tabla GL_INTERFACE de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);

		String FormatoPaso7 = String.format(tdcQueryPaso7, reference3);

		System.out.println(FormatoPaso7);

		SQLResult resultQueryPaso7 = executeQuery(dbEbsPeru, FormatoPaso7);

		boolean ValPaso7 = resultQueryPaso7.isEmpty();

		if (!ValPaso7) {

			testCase.addQueryEvidenceCurrentStep(resultQueryPaso7);

		}

		assertFalse(ValPaso7, "No se insertaron lineas en la tabla.");

		/*
		 * Paso 8
		 *****************************************************************************************/
		u2 = new SeleniumUtil(new ChromeTest(), true);
		pok2 = new PakageManagment(u2, testCase);

		addStep("Ejecutar el servicio de historico " + data.get("servicio2"));

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u2.get(contra);

		u2.hardWait(60);

		pok2.runIntefaceWmOneButton(data.get("interface"), data.get("servicio2"));

		/*
		 * Paso 9
		 *****************************************************************************************/

		addStep("Verificar que los datos son insertados en la tabla de historico: WM_GL_HEADERS_MEX_HIST de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		String FormatoPaso9 = String.format(tdcQueryPaso9, header_id);

		System.out.println(FormatoPaso9);

		SQLResult resultPaso9 = executeQuery(dbRmsPeru, FormatoPaso9);

		boolean ValPaso9 = resultPaso9.isEmpty();

		if (!ValPaso9) {

			testCase.addQueryEvidenceCurrentStep(resultPaso9);

		}

		assertFalse(ValPaso9, "No se insertaron los datos en la tabla de historial.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestFullName() {

		return "ATC_FT_RO8_Peru_001_Peru_Ajustes";

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Construido. Procesar todas las transacciones contenidas en" + " la tabla fem_fif_stg de Retek.";

	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
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
