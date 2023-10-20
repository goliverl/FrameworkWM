package interfaces.pr1;



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

public class PR1Col extends BaseExecution {
	

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR1_COL_Verficar_procesamiento_TSF(HashMap<String, String> data) throws Exception {
	
	/* Utilerías *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	
		utils.sql.SQLUtil dbRmsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables. DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA );
		
		/**
		 * Variables ******************************************************************************************
		 * 
		 * 
		*/
		
		
		String insumoCol= "SELECT b.doc_type, b.id, b.status, a.pv_cr_plaza, a.pv_cr_tienda, e.retek_cr, b.partition_date  \r\n" + 
				"FROM posuser.pos_envelope a,\r\n" + 
				"posuser.pos_inbound_docs b,\r\n" + 
				"posuser.pos_tsf c,\r\n" + 
				"posuser.plazas d,\r\n" + 
				"posuser.cedis e,\r\n" + 
				"posuser.plaza_cedis f\r\n" + 
				"WHERE b.doc_type = 'TSF'\r\n" + 
				"AND b.pe_id = a.ID\r\n" + 
				"AND b.status = 'I'\r\n" + 
				"AND c.pid_id = b.ID\r\n" + 
				"AND d.cr_plaza = a.pv_cr_plaza\r\n" + 
				"AND d.pais = 'COL' -- Filtro solo de informacion de Colombia\r\n" + 
				"AND f.id_plaza = d.ID\r\n" + 
				"AND f.id_cedis = e.ID\r\n" + 
				"AND c.cedis_id = e.retek_cr\r\n" + 
				"AND b.partition_date >= sysdate -13";
		
		String tdcQueryConnections = "SELECT FTP_CONN_ID,FTP_BASE_DIR,FTP_SERVERHOST,DESCRIPTION "
				+ " FROM WMUSER.WM_FTP_CONNECTIONS"
				+ " WHERE FTP_CONN_ID = 'RTKRMS_CO'"; //Primer paso
					
		
		String tdcQueryIntegrationServerCol = "SELECT * from (SELECT run_id,interface,start_dt,status,server"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR1_CO'"
				+ " and start_dt >= trunc(sysdate) " 
				+ " ORDER BY start_dt DESC) where rownum=1";

		
		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 "
				+ " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = '%s'";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; //FCWMLQA 

		
		String tdcQueryStatusE = "SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID\r\n" + 
				"FROM POSUSER.POS_INBOUND_DOCS \r\n" + 
				"WHERE DOC_TYPE IN ('TSF')\r\n" + 
				"AND STATUS ='E'\r\n" + 
				"AND ID = '%s'\r\n" + 
				"AND PARTITION_DATE > SYSDATE - 13\r\n" + 
				"ORDER BY RECEIVED_DATE  DESC ";

		
		String tdcQueryRetekSendDoc ="SELECT DOC_TYPE,CR_PLAZA,CR_TIENDA,STATUS,RUN_ID,RTK_FILENAME "
				+ " FROM WMUSER.RTK_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'TSF' "
				+ " AND STATUS = 'L' "
				+ " AND RTK_FILENAME IN ('%s')"; //Target id
		
	//	SELECT * FROM WMUSER.RTK_INBOUND_DOCS WHERE DOC_TYPE = ‘TSF’;

		
		
		
		String statusWmLog = "S";
		
	/*Pasos *********************************************************************************/
		
addStep("Validar que solo se procesen los registros de las plazas de Colombia");
		

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println( insumoCol);
		
		SQLResult insumoCol_res = executeQuery(dbPos,insumoCol);
		
		String id = insumoCol_res.getData(0, "ID");
		
		System.out.println("ID=" +  id);
		
		
		boolean validaInsumoCol = insumoCol_res.isEmpty();
		
		if (!validaInsumoCol) {
			
			testCase.addQueryEvidenceCurrentStep(insumoCol_res);
			
		}
		
		
		
	assertFalse(validaInsumoCol,"No se encontraron insumos a procesar");
		
	System.out.println(validaInsumoCol);

		
	
		
		
	///								 Paso 2***************************************************
		
	addStep("Ejecutar el servicio "+data.get("servicio"));


	SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
	PakageManagment pok = new PakageManagment(u, testCase);
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");
	String con ="http://"+user+":"+ps+"@"+server +":5555";
	String searchedStatus = "R";

	String status = "S";
	
	String  run_id;
		

		u.get(con);
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"),data.get("servicio"));
		System.out.println("Excecution date " + dateExecution );
		SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServerCol);
		
		System.out.println(tdcQueryIntegrationServerCol);


		run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");//guarda el run id de la ejecución

	boolean valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

			while (valuesStatus) {

						tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServerCol);


						valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

						u.hardWait(2);

									}

	

		
	/// 								Paso 3***************************************************** 
		
	addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");    
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);	
	    System.out.println(tdcQueryIntegrationServerCol);

	    
	    SQLResult exeQueryLogRun = executeQuery(dbLog, tdcQueryIntegrationServerCol);	
	    
	   
			
		boolean validateStatus = statusWmLog.equals(exeQueryLogRun.getData(0,"STATUS"));
		
		if (validateStatus) {
			
			testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
		}
		
		
		
		System.out.println(validateStatus);
		

		
	assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");//cambiar a true

	    
//											Paso 4******************************************************
		
	addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla WM_LOG_THREAD. ");
			
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);

		
			boolean statusThread = queryStatusThreadResult.isEmpty();
			System.out.println(statusThread);
			if (!statusThread) {

				testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
				
			}

			assertFalse(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		
		
		/// 								Paso 7****************************************************
			addStep("Validar que no haya registro de errores de la ejecución de la interface en la tabla WM_LOG_ERROR de la BD de WMLOG");
			
			String error = String.format(tdcQueryErrorId, run_id);
			System.out.println(error);
			
			SQLResult errorResult = dbLog.executeQuery(error);

			boolean emptyError = errorResult.isEmpty();

			if (emptyError) {
				
				testCase.addTextEvidenceCurrentStep("No se encontraron errores en la ejecucion de la interfaz");
	
				

			} else {
				
				testCase.addQueryEvidenceCurrentStep(errorResult);
				
			}
			
			assertTrue(emptyError, "Se encontraron errores en la ejecucion de la interfaz");
		
			System.out.println(emptyError);
	
	//***********************************************Paso 8********************************************************
			
			
			addStep("Obtener los datos del servidor FTP de RMS Colombia en la tabla WM_FTP_CONNECTIONS en la BD de WM, esquema WMUSER");
			

			System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			System.out.println(tdcQueryConnections);
			
			SQLResult ftpConn = executeQuery(dbPos,tdcQueryConnections);
			
			boolean connId = ftpConn.isEmpty();
			
			if (!connId) {
				
				testCase.addQueryEvidenceCurrentStep(ftpConn);
				
			}
			
			
			
		assertFalse(connId,"No se encontró la configuración del servidor");
			
		System.out.println(connId);
		
	//**********************************************Paso 9 ***************************************************
		

		addStep("Validar que se actualice el estatus del documento procesado en la tabla POS_INBOUND_DOCS a E");
		
		
	    String tdcQueryStatusEFormat = String.format(tdcQueryStatusE, id);	
		
	    System.out.println(tdcQueryStatusEFormat);
		
	    SQLResult tdcQueryStatusEResult = executeQuery(dbPos, tdcQueryStatusEFormat);
	    
        String target_id = tdcQueryStatusEResult.getData(0, "TARGET_ID");
		
		System.out.println("TARGET_ID=" + target_id);
		
	    boolean validaActStatusE = tdcQueryStatusEResult.isEmpty();
	    
	    if (!validaActStatusE) {
			
	    	testCase.addQueryEvidenceCurrentStep(tdcQueryStatusEResult);
	    	
	    	
		}
	    
		 
		System.out.println(validaActStatusE);
		
	assertFalse(validaActStatusE,"No se actualizo el status en POS_INBOUND_DOCS");
		
		
		
	//**********************************************Paso 10 **************************************************
	addStep("Validar el registro del documento enviado al servidor FTP de RETEK en la tabla RTK_INBOUND_DOCS.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		
		
	    String queryRetekSendDoc = String.format(tdcQueryRetekSendDoc, target_id);	
		System.out.println(queryRetekSendDoc);
		SQLResult rtk = executeQuery(dbRmsCol, queryRetekSendDoc);
		
		
		
	    boolean senDoc = rtk.isEmpty();
	    
	    if (!senDoc) {
			
	    	testCase.addQueryEvidenceCurrentStep(rtk);
	    	
	    	
		}
	    
		 
		System.out.println(senDoc);
	assertFalse(senDoc,"No se registro el envío del documento al servidor FTP");
		
		
	
	
	
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Realizar la ejecución de la interface  teniendo por procesar  archivos TSFde  México y . Validar que sea procesado el de Colombia.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PR1_COL_Verficar_procesamiento_TSF";
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