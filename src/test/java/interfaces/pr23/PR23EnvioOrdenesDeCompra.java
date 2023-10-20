package interfaces.pr23;

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

public class PR23EnvioOrdenesDeCompra extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/
		
						//ORIGINAL
		SQLUtil dbFcr = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
	//	SQLUtil dbFcr = new SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR); //NUCLEO
				
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA); // SIN CAMBIOS
		
						//ORIGINAL
		SQLUtil dbFcw =new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		//SQLUtil dbFcw = new SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit); //NUCLEO
		
		String status = "S";	
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		
/** Variables *********************************************************************/

		String tdcQueryInfo = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, D.PID_ID, B.PE_ID, B.PARTITION_DATE, C.TSF_NO"
				+ " FROM POSUSER.POS_ENVELOPE A,"  
				+ " POSUSER.POS_INBOUND_DOCS B," 
				+ " POSUSER.POS_REC C," 
				+ " POSUSER.POS_REC_DETL D" 
				+ " WHERE B.PE_ID = A.ID" 
				+ " AND B.DOC_TYPE = 'REC'"
				+ " AND B.STATUS  IN ('I')" 
				+ " AND C.PID_ID = B.ID"
				+ " AND C.PV_CVE_MVT = 06"
				+ " AND B.PARTITION_DATE > SYSDATE -45"
				+ " AND C.TSF_NO != 0" 
				+ " AND D.PID_ID = C.PID_ID" 
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS, D.PID_ID, B.PE_ID, B.PARTITION_DATE, C.TSF_NO";
		
		String tdcQueryTransfer = "SELECT DISTINCT STATUS, TRANSFER_NUMBER"
				+ " FROM WMUSER.TRANSFER_HEAD"
				+ " WHERE TRANSFER_NUMBER = %s"
				+ " AND STATUS = 'S'";
		
		String tdcQueryConfig = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD"
				+ " FROM WMUSER.WM_FTP_CONNECTIONS"
				+ " WHERE FTP_CONN_ID = 'RTKRMS'";
		
		String tdcQueryIntegrationServer = "SELECT * FROM (SELECT run_id, start_dt, status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR23main'"
			//	+" and  start_dt > To_Date ('%s', 'DD-MM-YYYY hh24:mi' )"
			    + " ORDER BY start_dt desc)"
				+ " WHERE ROWNUM = 1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = %s"; //FCWMLQA
		
		String tdcQueryStatusThread = "SELECT parent_id, thread_id, name, wm_log_thread.status, att1, att2"
				+ " FROM WMLOG.wm_log_thread"
				+ " WHERE parent_id = %s" ; //FCWMLQA 
		
		String tdcQueryInboundPos = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME"
				+ " FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE ID = %s"
				+ " AND STATUS = 'E'";
		
		String tdcQueryRtk = "SELECT DOC_TYPE, DOC_ID, RUN_ID, STATUS, CR_PLAZA, CR_TIENDA"
				+ " FROM WMUSER.rtk_inbound_docs"
				+ " WHERE run_id IN (%s)";
	
		
/** **************************Pasos del caso de Prueba*******************************************/

		
//													PASO 1		
		
		addStep("Validar que exista información de la Plaza y Tienda pendiente de procesar con antigüedad mayor a 45 días en POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		System.out.println(tdcQueryInfo);
		
		SQLResult infoResult = executeQuery(dbFcw, tdcQueryInfo);
		String tsfNo = null;
		String pidID = null;
		
		boolean info = infoResult.isEmpty();		
		if (!info) {
			tsfNo = infoResult.getData(0, "TSF_NO");
			pidID = infoResult.getData(0, "PID_ID");
			//testCase.addQueryEvidenceCurrentStep(infoResult);
		} else {
			testCase.addTextEvidenceCurrentStep("La consulta no arroja resultados.");
		}
		
		testCase.addQueryEvidenceCurrentStep(infoResult);
		
		System.out.println(info);

		//assertFalse(info, "La tabla no contiene registros");
		
		
//													PASO 2	
		
		addStep("Verificar la informacion disponible para procesar en RETEK.");
		System.out.println(GlobalVariables.DB_HOST_FCRMSMGR);
		String transferFormat = String.format(tdcQueryTransfer, tsfNo);
		System.out.println(transferFormat);
		
		SQLResult transferResult = executeQuery(dbFcr, transferFormat);
		boolean transfer = transferResult.isEmpty();
		
		if (transfer) { // !
			//testCase.addQueryEvidenceCurrentStep(transferResult);
			testCase.addTextEvidenceCurrentStep("No existe registro en 'TRANSFER_HEAD' con TRANSFER_NUMBER = " + tsfNo
					+ " y STATUS = 'S'");
		} 

		System.out.println(transfer);
		testCase.addQueryEvidenceCurrentStep(transferResult);
		//assertFalse(transfer, "La tabla no contiene registros");
		

		
//													PASO 3	
		
		addStep("Verificar que la configuración FTP existe.");
		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		System.out.println(tdcQueryConfig);
		
		SQLResult configResult = executeQuery(dbFcw, tdcQueryConfig);
		
		boolean config = configResult.isEmpty();		
		if (config) {
			testCase.addTextEvidenceCurrentStep("La tabla 'WM_FTP_CONNECTIONS' no contiene registros");
			//testCase.addQueryEvidenceCurrentStep(configResult);					
		} 
		
		testCase.addQueryEvidenceCurrentStep(configResult);
		System.out.println(config);

		//assertFalse(config, "La tabla no contiene registros");
		
		
		
//													PASO 4	
		
		addStep("Ejecutar el servicio PR23.Pub:run");
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		u.hardWait(4);
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));		
		//String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer,dateExecution);
		System.out.println("Respuesta dateExecution: " + dateExecution);
		
		SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer);
		
		
//													PASO 5
		
		addStep("Verificar que la interfaz terminó con éxito en WMLOG.");

		run_id = tdcIntegrationServerResult.getData(0, "RUN_ID"); //guarda el run id de la ejecución

		boolean valuesStatus = tdcIntegrationServerResult.getData(0, "STATUS").equals(searchedStatus); //Valida si se encuentra en estatus R
		while (valuesStatus) {			
			tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer);
			valuesStatus = tdcIntegrationServerResult.getData(0, "STATUS").equals(searchedStatus); //Valida si se encuentra en estatus R
			
			u.hardWait(2);
		}

		boolean successRun = tdcIntegrationServerResult.getData(0, "STATUS").equals(status); //Valida si se encuentra en estatus S
		if(!successRun) {			
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult er = dbLog.executeQuery(error);
			boolean emptyError = er.isEmpty();

			if(!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(er);
			}
			
		}
		
		testCase.addQueryEvidenceCurrentStep(tdcIntegrationServerResult);
		

//													PASO 6	
		
		addStep("Verificar los threads generados durante la ejecución.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String threadFormat = String.format(tdcQueryStatusThread, run_id);
		
		SQLResult threadResult = executeQuery(dbLog, threadFormat);
		System.out.println(threadFormat);
		
		String threadID = null;
		
		boolean ST = threadResult.isEmpty();
		if (!ST) {
			threadID = threadResult.getData(0, "thread_id");				
		} else {
			testCase.addTextEvidenceCurrentStep("No existe registro en 'WMLOG.wm_log_thread' con parent_id = " + run_id);
		}
		
		testCase.addQueryEvidenceCurrentStep(threadResult);

		System.out.println(ST);
		//assertFalse(ST, "No se obtiene información de la consulta");
		//u.close();
	
		
//													PASO 7	
		
		addStep("Verificar los status en la tabla POS_INBOUND_DOCS.");
		
		System.out.println(GlobalVariables.DB_HOST_fcwmesit);		
		String posFormat = String.format(tdcQueryInboundPos, pidID);
		System.out.println(posFormat);
		
		SQLResult posResult = executeQuery(dbFcw, posFormat);
		
		boolean posInbound = posResult.isEmpty();		
		if (posInbound) {				
			testCase.addTextEvidenceCurrentStep("No existen registros en 'WMUSER.rtk_inbound_docs' con ID = " + pidID + " y STATUS = 'E'");	
		} 				
		
		testCase.addQueryEvidenceCurrentStep(posResult);
		
		System.out.println(posInbound);
		//assertFalse(posInbound, "La tabla no contiene registros");
		
		
//												PASO 8	
		
		addStep("Verificar el registro insertado en la tabla RTK_INBOUND_DOCS.");
		
		System.out.println(GlobalVariables.DB_HOST_FCRMSMGR);		
		String rtkFormat = String.format(tdcQueryRtk, threadID);
		System.out.println(rtkFormat);
		
		SQLResult rtkResult = executeQuery(dbFcr, rtkFormat);
//		Thread.sleep(140);
		boolean rtkInbound = rtkResult.isEmpty();
		if (!rtkInbound) {				
			testCase.addQueryEvidenceCurrentStep(rtkResult);
		} else {
			testCase.addTextEvidenceCurrentStep("No se encontro ningun registro en: 'RTK_INBOUND_DOCS' con run_id = " + threadID);					
		}
				
		testCase.addQueryEvidenceCurrentStep(rtkResult);
		
		System.out.println(rtkInbound);
		// assertFalse(rtkInbound, "La tabla no contiene registros");
		
	}

	@Override
	public void beforeTest() {
		
	}

	@Override
	public String setPrerequisites() {		
		return null;
	}

	@Override
	public String setTestDescription() {		
		return "Confirmar a RMS los recibos de órdenes de compra del proveedor directo, así como recibos de transferencias.";
	}

	@Override
	public String setTestDesigner() {		
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {		
		return "PR23";
	}

	@Override
	public String setTestInstanceID() {		
		return null;
	}

}
