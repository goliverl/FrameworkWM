package interfaces.qb1;

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

public class QB1_EnviarInfoDeTicketsDeFCTPE_a_BI extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_QB1_001_Envia_info_ticket_FCTPE_a_BI(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/

	   
	    utils.sql.SQLUtil dbTpeqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,
				GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
	    
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		utils.sql.SQLUtil dbBi = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BiTicket,
				GlobalVariables.DB_USER_BiTicket,GlobalVariables.DB_PASSWORD_BiTicket);

	    
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
        String ACTIVITY_ID = "";
        String ValidInfo = "Select * from (SELECT ACCOUNT_ID,ACTIVITY_ID,CODE,PLAZA,TIENDA,CREATION_DATE,BI_STATUS "
		 		+ "FROM TPEUSER.TPE_FCP_ACTIVITY "
		 		+ "WHERE  PLAZA = '"+data.get("Plaza")+"' "
		 		+ "AND BI_STATUS = 'I' order by CREATION_DATE desc) where rownum <= 10 ";
//Paso 2
	
		String ValidInfItem =  "SELECT ITEM_ID,ACTIVITY_ID,CODE,DESCR,CREATION_DATE "
				+ "FROM TPEUSER.TPE_FCP_ITEM "
				+ "WHERE CODE <> 'IVA' "
				+ "AND ACTIVITY_ID='%s'";
			
//		Paso 4

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'FEMSA_QB1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

//		Paso 5
		String ValidTrhead = "SELECT * FROM WMLOG.WM_LOG_THREAD " + "WHERE PARENT_ID = '%s' " + "AND status = 'S'";
		
		
	

		// Paso 6
		
		
		 
	String ValidInsercBi=	"SELECT VT_NUM_CAJA, VT_CVE_ART, VT_CVE_MOV,VT_UDS,FECHA_CARGA "
			+ "FROM BIODSMKT.TEMP_TICKET "
			+ "WHERE VT_CR_PLAZ = '"+data.get("Plaza")+"' "
		    + "AND TRUNC(FECHA_CARGA) = TRUNC(SYSDATE)";
	
//	Paso 7
	String ValidAct = 	"Select * from (SELECT ACCOUNT_ID,ACTIVITY_ID,CODE,PLAZA,TIENDA,CREATION_DATE,BI_STATUS "
			+ "FROM TPEUSER.TPE_FCP_ACTIVITY "
			+ "WHERE PLAZA = '"+data.get("Plaza")+"' "
			+ "AND BI_STATUS = 'E' "
			+ "AND TARGET_ID = '%s' order by CREATION_DATE desc ) where rownum <=10";
//****	

	
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='FEMSA_QB1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Validar que exista información de los tickets de frecuencia en la tabla TPE_FCP_ACTIVITY "
				+ "de TPEUSER con BI_STATUS='I' pendientes por procesar.");

		System.out.println(GlobalVariables.DB_HOST_FCTPE);

		System.out.println(ValidInfo);

		SQLResult ExecValidInfo = dbTpeqa.executeQuery(ValidInfo);

		boolean ValidaBool = ExecValidInfo.isEmpty();

		if (!ValidaBool) {
			ACTIVITY_ID = ExecValidInfo.getData(0, "ACTIVITY_ID");
			System.out.println("ACTIVITY_ID: "+ACTIVITY_ID);
			testCase.addQueryEvidenceCurrentStep(ExecValidInfo);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No existen registros para procesar de la plaza " );

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Validar que exista información de los ítems para el ticket de frecuencia en la"
				+ " tabla TPE_FCP_ITEM de TPEUSER.");
    
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		String ValidInfItemFormat = String.format(ValidInfItem,ACTIVITY_ID);
		System.out.println(ValidInfItemFormat);
		SQLResult ExecValidInfItemFormat = dbTpeqa.executeQuery(ValidInfItemFormat);

		boolean ExecValidInfItemFormatRes = ExecValidInfItemFormat.isEmpty();
		
		if (!ExecValidInfItemFormatRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInfItemFormat);
		}

		System.out.println(ExecValidInfItemFormatRes);
		assertFalse(ExecValidInfItemFormatRes, "No existe información de los ítems para el ticket" );
		
//		********************Paso 3***************************************************************
		addStep("Ejecutar la interface QB1.Pub:run para enviar la información de los tickets de "
				+ "frecuencia de FCTPE a la base de datos de BI.");

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

//**********************************************************Paso 4*************************************************************************************************************			

		addStep("Validar la correcta ejecución de la interface QB1 en la tabla WM_LOG_RUN de WMLOG.");
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
	 
		addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG,"
				+ " donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidTrheadFormat = String.format(ValidTrhead, RunID);
		System.out.println(ValidTrheadFormat);
		SQLResult ExecValidTrhead = dbLog.executeQuery(ValidTrheadFormat);

		boolean ExecValidTrheadRes = ExecValidTrhead.isEmpty();

		if (!ExecValidTrheadRes) {
			ThreadID = ExecValidTrhead.getData(0, "THREAD_ID");
			System.out.println("ThreadID: "+ThreadID);
			testCase.addQueryEvidenceCurrentStep(ExecValidTrhead);

		}

		System.out.println(ExecValidTrheadRes);
		assertFalse(ExecValidTrheadRes, "No se muestra la información del WM_LOG_THREAD.");

//	***************************Paso 6*************************************************	

		addStep("Validar la inserción de los ítems de los tickets de frecuencia en la tabla TEMP_TICKET "
				+ "de la base de datos de BI.");

		System.out.println(GlobalVariables.DB_USER_BiTicket);
		System.out.println(ValidInsercBi);
		SQLResult ExecValidInsercBi = dbBi.executeQuery(ValidInsercBi);

		boolean ExecValidInsercBiRes = ExecValidInsercBi.isEmpty();

		if (!ExecValidInsercBiRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidInsercBi);

		}

		System.out.println(ExecValidInsercBiRes);
		assertFalse(ExecValidInsercBiRes, "No se muestra la inserción de los ítems de los tickets de frecuencia en la tabla TEMP_TICKET");

//		***************************Paso 7*************************************************	
			 	
			addStep("Validar la actualización de los registros procesados a "
					+ "BI_STATUS = 'E' en la tabla TPE_FCP_ACTIVITY de TPEUSER.");

			System.out.println(GlobalVariables.DB_HOST_FCTPE);
			String ValidActFormat = String.format(ValidAct, ThreadID);
			System.out.println(ValidActFormat);
			SQLResult ExecValidActFormat = dbTpeqa.executeQuery(ValidActFormat);

			boolean ExecValidActFormatRes = ExecValidActFormat.isEmpty();

			if (!ExecValidActFormatRes) {
				testCase.addQueryEvidenceCurrentStep(ExecValidActFormat);

			}

			System.out.println(ExecValidActFormatRes);
			assertFalse(ExecValidActFormatRes, "No se muestra la actualización de los registros procesados a BI_STATUS = 'E'");

		
		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_QB1_001_Envia_info_ticket_FCTPE_a_BI";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Enviar la información de los tickets de frecuencia de FCTPE a la base de datos de BI ";
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

