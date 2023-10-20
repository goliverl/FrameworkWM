package interfaces.po14;


import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PO14_ValidarlogueoDelErrorEnLaTablaPOS_DLS extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO14_001_Valida_Logueo_Error(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/


		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		utils.sql.SQLUtil dbUpd = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_UPD,
				GlobalVariables.DB_USER_FCWMQA_UPD, GlobalVariables.DB_PASSWORD_FCWMQA_UPD);
		
	
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
        String Id = "";
		String ValidInfo = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,INSERTED_DATE "
				+ "FROM posuser.POS_INBOUND_DOCS where STATUS = 'I' and  DOC_TYPE = 'DLS'";
//Paso 2

		String ValidNoInfo=	"SELECT COUNT(B.PID_ID) as Count "
				+ "FROM posuser.POS_DLS B, posuser.POS_DLS_DETL D "
				+ "WHERE D.PID_ID = B.PID_ID AND B.PID_ID = '%s'";
			
//		Paso 4

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PO14'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'E'  order by start_dt desc";

//		Paso 5
		String ValidError	="SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
				+ "FROM WMLOG.WM_LOG_ERROR "
				+ "WHERE RUN_ID = '%s' order by ERROR_DATE desc";
		
	

		// Paso 6

		String ValidTrhead = "SELECT * FROM WMLOG.WM_LOG_THREAD " + "WHERE PARENT_ID = '%s' " + "AND status = 'E'";
		
//****	
		

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PO14' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		
/*
 * GENERAR INSUMOS
 */
		String Upd1="update posuser.POS_INBOUND_DOCS "
			+ "set status='I' "
			+ "where  ID in ('6432028597')";
		

		String Upd2="update posuser.POS_INBOUND_DOCS "
				+ "set ID = '26437841372' "
				+ "where  ID = '6432028597'";

		String Upd3="update posuser.POS_INBOUND_DOCS "
				+ "	set ID = '6432028597' "
				+ "	where ID = '26437841372'";
				
				
		String Upd4="update POSUSER.POS_INBOUND_DOCS  "
				+ "set STATUS='I' "
				+ "where ID='6432028597'";
				


		
		dbUpd.executeUpdate(Upd1);
		dbUpd.executeUpdate(Upd2);
		dbUpd.executeQuery("commit");
		
		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		

		addStep("Tener información para procesar en la tabla POS_INBOUND_DOCS en POSUSER");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidInfo);

		SQLResult ExecValidInfo = dbPos.executeQuery(ValidInfo);

		boolean ValidaBool = ExecValidInfo.isEmpty();

		if (!ValidaBool) {
			Id = ExecValidInfo.getData(0, "ID");
			System.out.println("ID: "+Id);
			testCase.addQueryEvidenceCurrentStep(ExecValidInfo);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No existen registros para procesar de la plaza " );

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Validar que no se tiene información para los documentos pendientes en las tablas POS_DLS y POS_DLS_DETL en POSUSER.");
        String Valid="0";
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String ValidNoInfoFormat = String.format(ValidNoInfo,Id);
		System.out.println(ValidNoInfoFormat);
		SQLResult ExecValidNoInfoFormat = dbPos.executeQuery(ValidNoInfoFormat);
		String Cont = ExecValidNoInfoFormat.getData(0, "Count");
		System.out.println("Cont: "+Cont);
		
		boolean ExecValidNoInfoFormatRes = Cont.equals(Valid);
		if (ExecValidNoInfoFormatRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidNoInfoFormat);
		}

		System.out.println(ExecValidNoInfoFormatRes);
		assertTrue(ExecValidNoInfoFormatRes, "existe información para los documentos pendientes" );
		
//		********************Paso 3***************************************************************
		addStep("Ejecutar el servicio: PO14.Pub:run solicitando la ejecución del Job: runPO14.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);


		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
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

//**********************************************************Paso 4*************************************************************************************************************		

		addStep("Validar que la interfaz terminó con errores y el error se logueo correctamente en WMLOG.");
		String RunID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");

//		*************************************Paso 5 **************************************************

		addStep("Comprobar que se almaceno los detalles del error generado durante la ejecucion del servicio.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidErrorFormat = String.format(ValidError, RunID);
		System.out.println(ValidErrorFormat);
		SQLResult ExecValidErrorFormat = dbLog.executeQuery(ValidErrorFormat);

		boolean ExecValidErrorFormatRes = ExecValidErrorFormat.isEmpty();

		if (!ExecValidErrorFormatRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidErrorFormat);

		}

		System.out.println(ExecValidErrorFormatRes);
		assertFalse(ExecValidErrorFormatRes, "No se muestra la información del log.");

//	***************************Paso 6*************************************************	

		addStep("Validar que los documentos no se pudieron procesar.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidTrheadFormat = String.format(ValidTrhead, RunID);
		System.out.println(ValidTrheadFormat);
		SQLResult ExecValidTrhead = dbLog.executeQuery(ValidTrheadFormat);

		boolean ExecValidTrheadRes = ExecValidTrhead.isEmpty();

		if (!ExecValidTrheadRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidTrhead);

		}

		System.out.println(ExecValidTrheadRes);
		assertFalse(ExecValidTrheadRes, "No se muestra la información del log.");

		dbUpd.executeUpdate(Upd3);
		dbUpd.executeUpdate(Upd4);
		dbUpd.executeQuery("commit");
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO14_001_Valida_Logueo_Error";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que al no existir información de tiendas en POS_DLS para los documentos pendientes en POS_INBOUND_DOCS, se loguee el error correspondiente";
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
