package interfaces.pr23_cl;


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

public class PR23_CL_EnviarOrdenesCompra extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR23_CL_EnviarOrdenesCompra_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		SQLUtil dbRmsCL = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPosCL = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String VlidInfo = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, D.PID_ID, B.PE_ID, B.PARTITION_DATE, C.TSF_NO"
				+ " FROM POSUSER.POS_ENVELOPE A,"  
				+ " POSUSER.POS_INBOUND_DOCS B," 
				+ " POSUSER.POS_REC C," 
				+ " POSUSER.POS_REC_DETL D" 
				+ " WHERE B.PE_ID = A.ID" 
				+ " AND B.DOC_TYPE = 'REC'"
				+ " AND B.STATUS  IN ('I')" 
				+ " AND C.PID_ID = B.ID"
				+ " AND C.PV_CVE_MVT = '06'"
				+ " AND B.PARTITION_DATE > SYSDATE -45"
				+ " AND C.TSF_NO != '0'" 
				+ " AND D.PID_ID = C.PID_ID" 
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, D.PID_ID, B.PE_ID, B.PARTITION_DATE, C.TSF_NO";  
		//	Paso 2
		String QueryTransfer = "SELECT DISTINCT STATUS, TRANSFER_NUMBER "
				+ " FROM WMUSER.TRANSFER_HEAD "
				+ "WHERE TRANSFER_NUMBER = '%s' "
				+ "AND STATUS = 'S'";
		// Paso 3
		String ConfigFTP = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD "
				+ " FROM WMUSER.WM_FTP_CONNECTIONS "
				+ " WHERE FTP_CONN_ID = 'RTKRMS'";
		


//		Paso 5
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PR23main'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";
//		Paso 6
		String LogThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; 

//		Paso 7
		
		String ValidStatus = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME "
				+ " FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE ID = %s "
				+ " AND STATUS = 'E'";
		
//		Paso 8
		String VerifInsert = "SELECT DOC_TYPE, DOC_ID, RUN_ID, STATUS, CR_PLAZA, CR_TIENDA "
				+ " FROM WMUSER.rtk_inbound_docs "
				+ "WHERE run_id IN ('%s')";
		
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PR23main' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */
	
	

//		Paso 1	************************	
		addStep("Validar que exista información de la Plaza y Tienda pendiente de procesar con antigüedad mayor a 45 días en POSUSER.");
	
		String tsfNo = "";
		String pidID ="";
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(VlidInfo);
		
		SQLResult infoResult = executeQuery(dbPosCL, VlidInfo);
		
		
		boolean info = infoResult.isEmpty();
		
		if (!info) {	
			 tsfNo = infoResult.getData(0, "TSF_NO");
			 pidID = infoResult.getData(0, "PID_ID");
			testCase.addQueryEvidenceCurrentStep(infoResult);		
			
		} 

		System.out.println(info);

		assertFalse(info, "La tabla no contiene registros");
		
//		Paso 2	************************
		addStep("Verificar la informacion disponible para procesar en RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		String transferFormat = String.format(QueryTransfer, tsfNo);
		System.out.println(transferFormat);
		
		SQLResult transferResult = executeQuery(dbRmsCL, transferFormat);
		boolean transfer = transferResult.isEmpty();
		
		if (!transfer) {	
			
			testCase.addQueryEvidenceCurrentStep(transferResult);		
			
		} 

		System.out.println(transfer);

		assertFalse(transfer, "La tabla no contiene registros");

//		Paso 3	************************
			 
		addStep("Verificar que la configuracion FTP existe en WMINT.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(ConfigFTP);
		
		SQLResult configResult = executeQuery(dbPosCL, ConfigFTP);
		boolean config = configResult.isEmpty();
		
		if (!config) {	
			
			testCase.addQueryEvidenceCurrentStep(configResult);		
			
		} 

		System.out.println(config);

		assertFalse(config, "La tabla no contiene registros");
		
//		Paso 4	************************
		
		addStep("Ejecutar el servicio PR23.Pub:run");

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

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLogCL.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}
		
		
//		Paso 5	************************
			
		addStep("Verificar que la interfaz termino con exito en WMLOG. ");

		String RUN_ID ="";
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLogCL.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: "+ RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest,"No se muestra  la información.");
		
		

//		Paso 6	************************
		
		addStep("Verificar los threads generados durante la ejecucion");
		String THREAD_ID="";
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String threadFormat = String.format(LogThread, RUN_ID);
		System.out.println(threadFormat);
		SQLResult ExecLogThread = dbLogCL.executeQuery(threadFormat);

		boolean LogRequestTh = ExecLogThread.isEmpty();

		if (!LogRequestTh) {
		
			THREAD_ID = ExecLogThread.getData(0, "thread_id");
			System.out.println("THREAD_ID: "+ THREAD_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLogThread);
		}

		System.out.println(LogRequestTh);
		assertFalse(LogRequestTh,"No se muestra  la información.");

//		Paso 7	************************

		addStep("Verificar los status en la tabla POS_INBOUND_DOCS.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);		
		String posFormat = String.format(ValidStatus, pidID);
		System.out.println(posFormat);
		SQLResult posResult = executeQuery(dbPosCL, posFormat);
		boolean posInbound = posResult.isEmpty();
		
		if (!posInbound) {		
			testCase.addQueryEvidenceCurrentStep(posResult);			
		} 

		System.out.println(posInbound);

		assertFalse(posInbound, "La tabla no contiene registros");
		
//		Paso 8	************************

		addStep("Verificar el registro insertado en la tabla RTK_INBOUND_DOCS");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);		
		String VerifInsertFormat = String.format(VerifInsert, THREAD_ID);
		System.out.println(VerifInsertFormat);
		
		SQLResult rtkResult = executeQuery(dbRmsCL, VerifInsertFormat);
		boolean rtkInbound = rtkResult.isEmpty();
		
		if (!rtkInbound) {	
			
			testCase.addQueryEvidenceCurrentStep(rtkResult);		
			
		} 

		System.out.println(rtkInbound);

		assertFalse(rtkInbound, "La tabla no contiene registros");

		


	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR23_CL_EnviarOrdenesCompra";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "PR23_CL_EnviarOrdenesCompra";
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
