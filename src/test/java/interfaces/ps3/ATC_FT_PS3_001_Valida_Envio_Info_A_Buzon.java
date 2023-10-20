package interfaces.ps3;

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

public class ATC_FT_PS3_001_Valida_Envio_Info_A_Buzon extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PS3_001_Valida_Envio_Info_A_Buzon_test(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		utils.sql.SQLUtil dbSinergia = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String Id_Plaza="";
		String ValidInfPlz ="SELECT ID,CR_PLAZA,PLAZA_TYPE,CR_PLAZA_DESC,FCH_ARRANQUE,VENTANA_DIAS,DIAS_TOLERANCIA,SINERGIA "
				+ "FROM posuser.plazas"
				+ " WHERE sinergia = 'Y' "
				+ "AND cr_plaza = '"+ data.get("Plaza")+"'";
		// Paso 2
		
		String ValidInfEjec="SELECT * FROM posuser.wm_last_exec_datetime "
				+ "WHERE if_id = 'PS3' "
				+ "AND plaza_id = '%s'";
		
		
//		Paso 3
		 
	String ValidInfPend="SELECT cr_plaza, bar_code, 1 catalog, TO_CHAR(last_update_date,'YYYYMMDD HH24MISS') last_update_date "
			+ " FROM XXPEMP.xxse_card_full_v "
			+ "WHERE cr_plaza = '"+data.get("Plaza")+"'";
	
//	*********
//		Paso 4

	
	String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
			+ "WHERE INTERFACE = 'PS3'  " + "AND START_DT >= TRUNC(SYSDATE) "
			+ "AND STATUS = 'S'  order by start_dt desc";

		String validThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS,ATT1,ATT2  " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s' ";

//	Paso 6 

		String ValidUpdtTab =	"SELECT * "
				+ "FROM posuser.wm_last_exec_datetime "
				+ "WHERE if_id = 'PS3' "
				+ "AND  plaza_id = '%s' "
				+ "AND trunc(last_exec_date) = trunc(sysdate)";

		
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PS3' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		

		addStep("Tener informacion de la plaza "+ data.get("Plaza")+" en la tabla PLAZAS de POSUSER con el campo SINERGIA = Y.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidInfPlz);

		SQLResult ValidInfPlzRes = dbPos.executeQuery(ValidInfPlz);

		boolean ValidaBool = ValidInfPlzRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaBool) {
			
			Id_Plaza = ValidInfPlzRes.getData(0, "ID");
			System.out.println("Id_Plaza: "+Id_Plaza);

			testCase.addQueryEvidenceCurrentStep(ValidInfPlzRes); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(ValidaBool); 
		assertFalse(ValidaBool,"No se muestra info en la tabla PLAZAS de POSUSER con el campo SINERGIA = Y"); 

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Validar que haya informacion de la ultima ejecución para la interfaz PS3 en POSUSER:");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String ValidInfEjecFormat = String.format(ValidInfEjec, Id_Plaza);

		System.out.println(ValidInfEjecFormat);

		SQLResult ExecValidInfEjec = dbPos.executeQuery(ValidInfEjecFormat);

		boolean ValidInfEjecRes = ExecValidInfEjec.isEmpty();

		if (!ValidInfEjecRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInfEjec);

		}

		System.out.println(ValidInfEjecRes);
		assertFalse(ValidInfEjecRes,"No existe  informacion de la última ejecución para la interfaz PS3 en POSUSER");

//**********************************************************Paso 3*************************************************************************************************************		
		
		addStep("Tener informacion pendiente de procesar en la tabla XXSE_CARD_FULL_V de SINERGIA para la plaza.");

		System.out.println(ValidInfPend);

		SQLResult ExecValidInfPend = dbSinergia.executeQuery(ValidInfPend);

		boolean ValidInfPendRes = ExecValidInfPend.isEmpty();

		if (!ValidInfPendRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInfPend);

		}

		System.out.println(ValidInfPendRes);
		assertFalse(ValidInfPendRes,"No existe informacion pendiente de procesar en la tabla XXSE_CARD_FULL_V de SINERGIA para la plaza");
		
//		*************************************Paso 4 **************************************************
		addStep("Ejecutar el servicio: PS3.Pub:run.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		
		String dateExecution = pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"),data.get("execType"),"execType");
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
u.close();
//*********************************Paso 5*********************************************************
		
		addStep("Validar que la interfaz concluyo correctamente en WMLOG.");
		String RunID = "";
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");


			testCase.addQueryEvidenceCurrentStep(ExecLog);


		}

		System.out.println(LogRequest);
		assertFalse(LogRequest,"No se muestra  la informacion del log.");

//********************************************Paso 6**************************************************************************************************************************

		addStep("Validar que en la tabla WM_LAST_EXEC_DATETIME se actualizo correctamente la ultima fecha de ejecucion.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String ValidUpdtTabFormat = String.format(ValidUpdtTab, Id_Plaza);
		System.out.println(ValidUpdtTabFormat);
		SQLResult ExecValidUpdtTabFormat = dbPos.executeQuery(ValidUpdtTabFormat);
		boolean ValidUpdtTabReq = ExecValidUpdtTabFormat.isEmpty();
		System.out.println(ValidUpdtTabReq);
		


		if (!ValidUpdtTabReq) {

			testCase.addQueryEvidenceCurrentStep(ExecValidUpdtTabFormat);

		}
		
		assertFalse(ValidUpdtTabReq,"No se actualizo la ultima fecha de ejecucion de la tabla WM_LAST_EXEC_DATETIME ");



	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null ;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que se la informacion obtenida de la tabla XXSE_CARD_FULL_V de SINERGIA se envia al buzon";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
}


