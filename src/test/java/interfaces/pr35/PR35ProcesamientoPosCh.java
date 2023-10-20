package interfaces.pr35;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR35ProcesamientoPosCh extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR35_ProcesamientoPosCh(HashMap<String, String> data) throws Exception {
	
/* Utilerías *********************************************************************/
		
	    
		utils.sql.SQLUtil dbRmsCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile,GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
	    utils.sql.SQLUtil dbLogCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPosCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbEbsCh = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ,GlobalVariables.DB_USER_OIEBSBDQ , GlobalVariables.DB_PASSWORD_OIEBSBDQ);
	
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		//Paso 1
                String tdcAvailebleDateIn = "select to_char(max(END_DATE),'DD/MM/YYYY HH:MI:SS AM') LAST_EXEC_DATE \r\n"// start_date
				+ " from wmuser.WM_DATES_EXEC \r\n" 
                + " where INTERFASE='PR35_CL' \r\n"; // pos user

         //Paso 2
		String tdcAvailableDateIn2 = " Select to_char(sysdate,'DD/MM/YYYY HH:MI:SS AM') \r\n" 
                + " CURRENT_DATE from DUAL \r\n"; //end_date

                //Paso 2
		String tdcDatosProc = " SELECT DISTINCT il.loc location, SUBSTR (store_name10, 1, 5) plaza \r\n"  
				+ " FROM wmuser.item_master im, wmuser.item_loc il, store s \r\n" 
				+ " WHERE im.item = il.item \r\n" 
				+ " AND ( (il.create_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM') \r\n"  
				+ " AND il.create_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')) \r\n"  
				+ " OR (im.last_update_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM') \r\n"  
				+ " AND im.last_update_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM'))) \r\n"  
				+ " AND im.pack_ind = 'Y' \r\n" 
				+ " AND s.store = il.loc\r\n"  
		      //+ " AND  il.loc = \r\n"  
				+ " ORDER BY plaza \r\n";
		

		String tdcTableMaestros = " SELECT ORACLE_CR_DESC,RETEK_CR,ESTADO,ORACLE_CR_TYPE,ORACLE_CR_SUPERIOR \r\n"
				+ " FROM XXFC_MAESTRO_DE_CRS_V \r\n" 
				+ " WHERE ESTADO = 'A' \r\n" 
				+ " AND ORACLE_CR_TYPE = 'T' \r\n"
				+ " AND RETEK_CR ='%s' \r\n"; // Appsview_EBS

                   //Paso 3

     String FTPContention = " SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD FROM WMUSER.WM_FTP_CONNECTIONS \r\n"
     		    + " WHERE FTP_CONN_ID = 'PR50POS' \r\n";
                
                //Paso 4-5

		String tdcQueryStatusLog = " SELECT run_id,interface,start_dt,status,server \r\n" 
		        + " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR35main' \r\n"
		        + " and status= 'S' \r\n" 
		        + " and start_dt >= trunc(sysdate) \r\n" // FCWMLQA
				+ " ORDER BY start_dt DESC \r\n";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status \r\n" 
		        + " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PR35main' \r\n" 
		        + " and  start_dt > To_Date ('%s', 'DD-MM-YYYY HH24:MI:SS' ) \r\n"
				+ " order by start_dt desc) \r\n" 
		        + " where rownum = 1 \r\n";

		String tdcQueryStatusThread = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = '%s' \r\n"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION \r\n" 
		        + " FROM WMLOG.WM_LOG_ERROR \r\n"
				+ " where RUN_ID='%s' \r\n"; // FCWMLQA

                    
               //Paso 6

		String tdcDocPosOutbound = " select DOC_NAME,DOC_TYPE,SENT_DATE,PV_CR_PLAZA,PV_CR_TIENDA \r\n"
				+ " FROM POSUSER.POS_OUTBOUND_DOCS \r\n" 
				+ " WHERE SOURCE_ID = '%s' \r\n"
				+ " AND SENT_DATE >= TRUNC(SYSDATE) \r\n"
				+ " AND DOC_TYPE='PCC'  ORDER BY SENT_DATE DESC \r\n ";

               //Paso 7

          String RegistroPos = " SELECT * FROM wmuser.WM_DATES_EXEC WHERE INTERFASE = 'PR35' \r\n "
          		+ " AND  RUN_ID = '%s' \r\n"
          		+ " ORDER BY END_DATE DESC \r\n";
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//***************************************************************************Paso 1	*****************************************************************************		

		addStep("Consulta la ultima fecha de ejecucion (start_date)");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);

		System.out.println(tdcAvailebleDateIn);

		SQLResult AvailableDate = executeQuery(dbPosCh, tdcAvailebleDateIn); // Es Pos

		String LAST_EXEC_DATE = AvailableDate.getData(0, "LAST_EXEC_DATE");

		System.out.println("LAST_EXEC_DATE " + LAST_EXEC_DATE);

		boolean day = AvailableDate.isEmpty();

		if (!day) {

			testCase.addQueryEvidenceCurrentStep(AvailableDate);

		}

		System.out.println(day);

		assertFalse(day, "La consulta no muestra informacion de la ultima ejecucion");

//*************************************************************************Paso 2**********************************************************************************	


		addStep("Validar que exista información para el rango de fechas de ejecucion (end_date) ");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		
		System.out.println(tdcAvailableDateIn2);

		SQLResult AvailableDate2 = executeQuery(dbRmsCh, tdcAvailableDateIn2); // Es la Pos

		String CURRENT_DATE = AvailableDate2.getData(0, "CURRENT_DATE");

		System.out.println("CURRENT_DATE " + CURRENT_DATE);

		boolean days = AvailableDate2.isEmpty();

		if (!days) {

			testCase.addQueryEvidenceCurrentStep(AvailableDate2);

		}

		System.out.println(days);

		assertFalse(days, "No se muestra la fecha actual");

//**************************************************************************Paso 3********************************************************************************		

		addStep("Validar que exista información para el rango de tiempo que designamos en las tablas ITEM_MASTER, ITEM_LOC y STORE ");
		
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String datos = String.format(tdcDatosProc, LAST_EXEC_DATE, CURRENT_DATE, LAST_EXEC_DATE, CURRENT_DATE);

		System.out.println(datos);

		SQLResult Date3 = executeQuery(dbRmsCh, datos);

		String LOCATION = Date3.getData(0, "LOCATION");

		System.out.println("LOCATION " + LOCATION);

		boolean date = Date3.isEmpty();

		if (!date) {

			testCase.addQueryEvidenceCurrentStep(Date3);

		}

		System.out.println(date);

		assertFalse(date, "No se muestran registros que cumplan con el rango de fechas de ejecucion");

//**********************************************************************Paso 4**************************************************************************************
		
		addStep("Comprobar que exista informacion necesaria para procesar en XXFC_MAESTRO_DE_CRS_V ");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);

		String tdcTableMaestrosForm = String.format(tdcTableMaestros, LOCATION);

		System.out.println(tdcTableMaestrosForm);

		SQLResult TableMaestros = executeQuery(dbRmsCh, tdcTableMaestrosForm); /// es de EBS

		boolean ma = TableMaestros.isEmpty();

		if (!ma) {

			testCase.addQueryEvidenceCurrentStep(TableMaestros);

		}

		System.out.println(ma);

		assertFalse(ma, "La tabla no contiene ningun registro");

// *************************************************************Paso 5 *****************************************************************************************
		addStep(" Comprobar los datos para conectarse al Buzón de POS");

		System.out.println(FTPContention);

		SQLResult FTPContentionRes = executeQuery(dbRmsCh, FTPContention); // Es la Pos

		boolean ValidaFTPContention = FTPContentionRes.isEmpty();

		if (!ValidaFTPContention) {

			testCase.addQueryEvidenceCurrentStep(FTPContentionRes);

		}

		System.out.println(ValidaFTPContention);

		assertFalse(ValidaFTPContention, "No se encontraron los datos para conectarse al Buzón de POS");

//***************************************************************Paso 6	*****************************************************************************************		

		addStep("Ejecutar el servicio PR35_CL.Pub:");

		String status = "S";
//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

		String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer, dateExecution);

		SQLResult IntegrationServer = executeQuery(dbRmsCh, tdcIntegrationServerFormat); // Es LOg

		run_id = IntegrationServer.getData(0, "RUN_ID");
		String Status = IntegrationServer.getData(0, "STATUS");

		boolean valuesStatus = Status.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			IntegrationServer = executeQuery(dbRmsCh, tdcIntegrationServerFormat); // ES Log

			run_id = IntegrationServer.getData(0, "RUN_ID");
			Status = IntegrationServer.getData(0, "STATUS");

			valuesStatus = Status.equals(searchedStatus);

			u.hardWait(2);

		}
		boolean successRun = Status.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);

			SQLResult errorQuery = executeQuery(dbRmsCh, error); // Es Log

			boolean emptyError = errorQuery.isEmpty();

			if (emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorQuery);

			}
		}

//*****************************************************************************Paso 7	********************************************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);

		SQLResult StatusLog = executeQuery(dbLogCh, queryStatusLog); // Log

		String fcwS = StatusLog.getData(0, "STATUS");

		boolean validateStatus = status.equals(fcwS);

		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(StatusLog);
		}

		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//	 		Paso 6	************************

		addStep("Se valida la generacion de threads");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);

		System.out.println(queryStatusThread);

		SQLResult StatusThread = executeQuery(dbRmsCh, queryStatusThread); // Log

		boolean statusThread = StatusThread.isEmpty();

		System.out.println(statusThread);
		if (!statusThread) {

			testCase.addQueryEvidenceCurrentStep(StatusThread);

		}
		assertFalse(statusThread, "No se generaron threads");

//*******************************************************Paso 8 ****************************************************************************************

		addStep("Validar que se haya insertado los registro en la tabla POS_OUTBOUND_DOCS de los documentos enviados al servidor FTP");

		String PosPutBoundFormat = String.format(tdcDocPosOutbound, run_id);

		System.out.println(PosPutBoundFormat);

		SQLResult PosPutBound = executeQuery(dbRmsCh, PosPutBoundFormat); // POS

		boolean doc = PosPutBound.isEmpty();

		if (!doc) {

			testCase.addQueryEvidenceCurrentStep(PosPutBound);

		}

		System.out.println(doc);

		assertFalse(doc, "No se encuentran registros en la tabla POS_OUTBOUND_DOCS");

//****************************************************Paso 9 *******************************************************************************************
		addStep("Validar que se inserto un nuevo registro de la ejecucion en la DB WMINT.");

		String RegistroPosFormat = String.format(RegistroPos, run_id);

		System.out.println(RegistroPosFormat);

		SQLResult RegistroPosRes = executeQuery(dbRmsCh, RegistroPosFormat); // Pos

		boolean validaRegistroPosRes = RegistroPosRes.isEmpty();

		if (!validaRegistroPosRes) {

			testCase.addQueryEvidenceCurrentStep(RegistroPosRes);

		}

		System.out.println(validaRegistroPosRes);

		assertFalse(validaRegistroPosRes, "No se encuentran registros en la tabla WM_DATES_EXEC ");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR35_ProcesamientoPosCh";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACIO";
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
