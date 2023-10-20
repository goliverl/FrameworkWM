package interfaces.rs1;

import static org.junit.Assert.assertFalse;
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

public class ATC_FT_RS1_001_InsertInfoPromocionSINERGIA  extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RS1_001_InsertInfoPromocionSINERGIA_test(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		utils.sql.SQLUtil dbSinergia = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String Promotion = "";
		String Store = "";
		String ValidInf = "Select * from (select s.promotion, s.PROM_NAME, s.prom_desc,  "
				+ "to_char(s.start_date,'DD/MM/YYYY'), to_char(s.end_date,'DD/MM/YYYY'), "
				+ "s.status, s.comment_desc, s.wm_status, a.store  "
				+ "from WMUSER.wm_prom_sinergia s, WMUSER.promstore a, WMUSER.store_hierarchy b  "
				+ "where wm_status = 'L'  "
				+ "and a.promotion = s.promotion  "
				+ "and a.store = b.store "
				+ "and b.area = '"+data.get("Area")+"') where rownum <=10 ";

		// Paso 2
		
		String ValidItems =  "select a.promotion, a.item, b.item_desc  "
				+ "from RMS100.prom_threshold_sku a, RMS100.item_master b  "
				+ "where a.item = b.item "
				+ "and a.promotion = '%s' union select a.promotion, a.item, b.item_desc  "
				+ "from promsku a, item_master b where a.item  = b.item and a.promotion = '%s'";
		
		
//		******************
//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'RS1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

		String validThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS,ATT1,ATT2  " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s' ";

//	Paso 5 

		String ValidRmsProm ="select PROMOTION,PROMOTION_NAME,PROMOTION_DESC,WM_SEND_DATE,WM_RUN_ID"
				+ " from XXPEMP.xxse_rms_promotion  "
				+ "where promotion = '%s'  "
				+ "and wm_run_id = '%s'  "
				+ "and trunc(wm_send_date) = trunc(sysdate)";
		

		String ValidRmsSku = "select * "
				+ "from XXPEMP.xxse_rms_promotion_sku  "
				+ "where promotion = '%s'";
		

		String ValidRmsLoc = "select * "
				+ "from XXPEMP.xxse_rms_promotion_loc  "
				+ "where promotion = '%s'  "
				+ "and store_id = '%s'";
//	Paso 6 
		String ValidStatus =	"select promotion, prom_name, prom_desc, "
				+ "to_char(start_date,'DD/MM/YYYY'), to_char(end_date,'DD/MM/YYYY'), comment_desc, wm_status  "
				+ "from WMUSER.wm_prom_sinergia  "
				+ "where promotion = '%s' "
				+ "and wm_status = 'E'";
		
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='RS1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Validar que existe informacion de promociones en la tabla "
				+ "wm_prom_sinergia de RETEK con el campo WM_STATUS igual a L, para la plaza(Area) "+data.get("Area"));
			
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(ValidInf);

		SQLResult validInfRes = dbRms.executeQuery(ValidInf);

		boolean ValidaBool = validInfRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaBool) {

			Promotion = validInfRes.getData(0, "PROMOTION");
			System.out.println("Promotion: "+Promotion);
			Store = validInfRes.getData(0, "STORE");
			System.out.println("Store: "+Store);
			testCase.addQueryEvidenceCurrentStep(validInfRes); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(ValidaBool); // Si no, imprime la fechas
		assertFalse(ValidaBool,"No se muestran información de promociones en la tabla wm_prom_sinergia"); // Si esta vacio,
																										// imprime
		// mensaje

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Validar que existe informacion de ITEMS para las promociones pendientes "
				+ "de procesar en las tablas: PROM_THRESHOLD_SKU o PROMSKU en RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		String ValidItemsFormat = String.format(ValidItems, Promotion, Promotion);

		System.out.println(ValidItemsFormat);

		SQLResult ExecValidItems = dbRms.executeQuery(ValidItemsFormat);

		boolean validItemsRes = ExecValidItems.isEmpty();

		if (!validItemsRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidItems);

		}

		System.out.println(validItemsRes);
		assertFalse(validItemsRes,"No existe información de ITEMS para las promociones pendientes");

//**********************************************************Paso 3*************************************************************************************************************		

		addStep("Ejecutar el servicio:  RS1.Pub:run, solicitando el job: runRS1");

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
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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

//*********************************Paso 4*********************************************************

		addStep("Validar que la interfaz se ejecuto correctamente.");
		String RunID = "";
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			String ValidThreadFormat = String.format(validThread, RunID);
			System.out.println(ValidThreadFormat);
			SQLResult ExecThread = dbLog.executeQuery(ValidThreadFormat);

			ThreadID = ExecThread.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(ExecLog);
			testCase.addQueryEvidenceCurrentStep(ExecThread);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest,"No se muestra  la informacion del log.");

//********************************************Paso 5**************************************************************************************************************************

		addStep("Validar que se inserto la informacion en las tablas: "
				+ "xxse_rms_promotion, xxse_rms_promotion_sku, xxse_rms_promotion_loc de SINERGIA.");
		
		String ValidRmsPromFormat = String.format(ValidRmsProm, Promotion, RunID);
		System.out.println(ValidRmsPromFormat);
		SQLResult ExecValidRmsPromFormat = dbSinergia.executeQuery(ValidRmsPromFormat);
		boolean ExecValidRmsPromReq = ExecValidRmsPromFormat.isEmpty();
		System.out.println(ExecValidRmsPromReq);
		
		String ValidRmsSkuFormat = String.format(ValidRmsSku, Promotion);
		System.out.println(ValidRmsSkuFormat);
		SQLResult ExecfValidRmsSkuFormat = dbSinergia.executeQuery(ValidRmsSkuFormat);
		boolean ValidRmsSkuReq = ExecfValidRmsSkuFormat.isEmpty();
		System.out.println(ValidRmsSkuReq);
		
		String ValidRmsLocFormat = String.format(ValidRmsLoc, Promotion,Store);
		System.out.println(ValidRmsLocFormat);
		SQLResult ExecValidRmsLocFormat = dbSinergia.executeQuery(ValidRmsLocFormat);
		boolean ValidRmsLocReq = ExecValidRmsLocFormat.isEmpty();
		System.out.println(ValidRmsLocReq);

		if (!ExecValidRmsPromReq) {

			testCase.addQueryEvidenceCurrentStep(ExecValidRmsPromFormat);

		}
		
		if(!ValidRmsSkuReq) {
			testCase.addQueryEvidenceCurrentStep(ExecfValidRmsSkuFormat);
		}
		
		if(!ValidRmsLocReq) {
			testCase.addQueryEvidenceCurrentStep(ExecValidRmsLocFormat);
			
		}

		
		assertFalse(ExecValidRmsPromReq,"No se actualizo la tabla XXPEMP.xxse_rms_promotion");
		assertFalse(ValidRmsSkuReq,"No se actualizo la tabla XXPEMP.xxse_rms_promotion_sku");
		assertFalse(ValidRmsLocReq,"No se actualizo la tabla XXPEMP.xxse_rms_promotion_loc");

//*********************************************************Paso 6 **********************************************************************************************
		
		addStep("Validar que el campo WM_STATUS se actualizo a E correctamente en la tabla wm_prom_sinergia de RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String ValidStatusFormat = String.format(ValidStatus, Promotion);
		System.out.println(ValidStatusFormat);
		SQLResult ExecuteStatusFormat = dbRms.executeQuery(ValidStatusFormat);

		boolean ExecuteValidUpdtReq = ExecuteStatusFormat.isEmpty();

		if (!ExecuteValidUpdtReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteStatusFormat);

		}

		System.out.println(ExecuteValidUpdtReq);
		assertFalse(ExecuteValidUpdtReq,"No se muestran datos insertados");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que se inserte la informacion de promociones en SINERGIA";
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


