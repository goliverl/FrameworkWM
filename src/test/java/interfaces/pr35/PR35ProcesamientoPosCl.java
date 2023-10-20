package interfaces.pr35;

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

public class PR35ProcesamientoPosCl extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR35_ProcesamientoPosCl(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		//utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		
		
/** VARIABLES *********************************************************************/			
		

		//Paso 1
                String tdcAvailebleDateIn = "select to_char(max(END_DATE),'DD/MM/YYYY HH:MI:SS AM') LAST_EXEC_DATE"// start_date
				+ " from wmuser.WM_DATES_EXEC" + " where INTERFASE='PR35_CO'"; // pos user

         //Paso 2
		String tdcAvailableDateIn2 = "select to_char(sysdate,'DD/MM/YYYY HH:MI:SS AM')" + "CURRENT_DATE from DUAL"; //end_date

                //Paso 3
		String tdcDatosProc = "SELECT DISTINCT il.loc location, SUBSTR (store_name10, 1, 5) plaza \r\n" + 
				"FROM wmuser.item_master im, wmuser.item_loc il, store s\r\n" + 
				"WHERE im.item = il.item\r\n" + 
				"AND ( (il.create_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')\r\n" + 
				"AND il.create_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM'))\r\n" + 
				"OR (im.last_update_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')\r\n" + 
				"AND im.last_update_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')))\r\n" + 
				"AND im.pack_ind = 'Y'\r\n" + 
				"AND s.store = il.loc\r\n" + 
			//	"AND  il.loc = '38567'\r\n" + 
				"ORDER BY plaza ";
  
		String tdcTableMaestros = "SELECT ORACLE_CR_DESC,RETEK_CR,ESTADO,ORACLE_CR_TYPE,ORACLE_CR_SUPERIOR"
				+ " FROM XXFC_MAESTRO_DE_CRS_V " + " WHERE ESTADO = 'A'" + " AND ORACLE_CR_TYPE = 'T'"
				+ " AND RETEK_CR = '%s' "; // Appsview_EBS



                   //Paso 3

     String FTPContention = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD FROM WMUSER.WM_FTP_CONNECTIONS WHERE FTP_CONN_ID = 'PR50POS'";
                
                //Paso 4-5

		String tdcQueryStatusLog = "select*from(SELECT run_id,interface,start_dt,status,server " + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR35_CO' " + " and status= 'S' " + " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC) where rownum = 1";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR35_CO'" + " and  start_dt > To_Date ('%s', 'DD-MM-YYYY HH24:MI:SS' )"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; // FCWMLQA

                    
               //Paso 6

		String tdcDocPosOutbound = "select DOC_NAME,DOC_TYPE,SENT_DATE,PV_CR_PLAZA,PV_CR_TIENDA"
				+ " FROM POSUSER.POS_OUTBOUND_DOCS" 
				+ " WHERE SOURCE_ID = '%s' "
				+ "AND SENT_DATE >= TRUNC(SYSDATE)"
				+ " AND DOC_TYPE='PCC'  ORDER BY SENT_DATE DESC ";

               //Paso 7

          String RegistroPos = "SELECT * FROM wmuser.WM_DATES_EXEC WHERE INTERFASE = 'PR35_CO' "
          		+ "AND  RUN_ID = '%s' "
          		+ "ORDER BY END_DATE DESC";
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */


//***************************************************************************Paso 1	*****************************************************************************		

		addStep("Consulta la ultima fecha de ejecucion (start_date)");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcAvailebleDateIn);

		SQLResult AvailableDate = executeQuery(dbPos, tdcAvailebleDateIn);
		
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

		System.out.println(tdcAvailableDateIn2);

		SQLResult AvailableDate2 = executeQuery(dbPos, tdcAvailableDateIn2);
		
       String CURRENT_DATE = AvailableDate2.getData(0, "CURRENT_DATE");
		
		
		System.out.println("CURRENT_DATE " + CURRENT_DATE);

		boolean days = AvailableDate2.isEmpty();

		if (!days) {

			testCase.addQueryEvidenceCurrentStep(AvailableDate2);

		}

		System.out.println(days);

		assertFalse(days, "No se muestra la fecha actual");
		
//**************************************************************************Paso 3********************************************************************************		
/* COMENTADO PARA SUBIR A OCTANE
		addStep("Validar que exista información para el rango de tiempo que designamos en las tablas ITEM_MASTER, ITEM_LOC y STORE ");

		String datos = String.format(tdcDatosProc, LAST_EXEC_DATE, CURRENT_DATE, LAST_EXEC_DATE, CURRENT_DATE);
		
		System.out.println(datos);
		
		SQLResult Date3 = executeQuery(dbRms, datos);
		
		String LOCATION = Date3.getData(0, "LOCATION");
		
		System.out.println("LOCATION " + LOCATION );

		boolean date = Date3.isEmpty();

		if (!date) {

			testCase.addQueryEvidenceCurrentStep(Date3);

		}

		System.out.println(date);

		assertFalse(date, "No se muestran registros que cumplan con el rango de fechas de ejecucion");

	
	
//**********************************************************************Paso 4**************************************************************************************
		addStep("Comprobar que exista informacion necesaria para procesar en XXFC_MAESTRO_DE_CRS_V ");
		
		System.out.println(GlobalVariables.DB_HOST_EBS);
		
		String tdcTableMaestrosF = String.format(tdcTableMaestros , LOCATION);
		
	   
		System.out.println(tdcTableMaestrosF);
	
		SQLResult TableMaestros = executeQuery(dbEbs, tdcTableMaestrosF);


		boolean ma = TableMaestros.isEmpty();

		if (!ma) {

			testCase.addQueryEvidenceCurrentStep(TableMaestros);

		}

		System.out.println(ma);

		assertFalse(ma, "La tabla no contiene ningun registro");
		
		
		*/
// *************************************************************Paso 5 *****************************************************************************************
		addStep(" Comprobar los datos para conectarse al Buzón de POS");

	
		System.out.println(FTPContention);

		SQLResult FTPContentionRes = executeQuery(dbPos, FTPContention);
	
		boolean ValidaFTPContention = FTPContentionRes .isEmpty();

		if (!ValidaFTPContention) {

			testCase.addQueryEvidenceCurrentStep(FTPContentionRes);

		}

		System.out.println(ValidaFTPContention);

		assertFalse(ValidaFTPContention, "No se encontraron los datos para conectarse al Buzón de POS");

//***************************************************************Paso 6	*****************************************************************************************		

		addStep("Ejecutar el servicio PR35.Pub:");

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

		SQLResult IntegrationServer = executeQuery(dbLog, tdcIntegrationServerFormat);

		run_id = IntegrationServer.getData(0, "RUN_ID");
		String Status = IntegrationServer.getData(0, "STATUS");

		boolean valuesStatus = Status.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			IntegrationServer = executeQuery(dbLog, tdcIntegrationServerFormat);

			run_id = IntegrationServer.getData(0, "RUN_ID");
			Status = IntegrationServer.getData(0, "STATUS");

			valuesStatus = Status.equals(searchedStatus);

			u.hardWait(2);

		}
		boolean successRun = Status.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);

			SQLResult errorQuery = executeQuery(dbLog, error);

			
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

		SQLResult StatusLog = executeQuery(dbLog, queryStatusLog);

		String fcwS = StatusLog.getData(0, "STATUS");

		boolean validateStatus = status.equals(fcwS);
		
		if(validateStatus) {
			
			testCase.addQueryEvidenceCurrentStep(StatusLog);
		}
		
		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//	 		Paso 6	************************

		addStep("Se valida la generacion de threads");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		
		System.out.println(queryStatusThread);

		SQLResult StatusThread = executeQuery(dbLog, queryStatusThread);

		
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

		SQLResult PosPutBound = executeQuery(dbPos, PosPutBoundFormat);


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

	SQLResult RegistroPosRes = executeQuery(dbPos, RegistroPosFormat);


		boolean validaRegistroPosRes = RegistroPosRes .isEmpty();

		if (!validaRegistroPosRes) {

			testCase.addQueryEvidenceCurrentStep(RegistroPosRes);

		}

		System.out.println(validaRegistroPosRes);

		assertFalse(validaRegistroPosRes, "No se encuentran registros en la tabla WM_DATES_EXEC ");
	
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PR35_ProcesamientoPosCl";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Realizar la ejecucion de la interface de manera correcta contemplando sus datos de tienda diferente ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomatQA";
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
