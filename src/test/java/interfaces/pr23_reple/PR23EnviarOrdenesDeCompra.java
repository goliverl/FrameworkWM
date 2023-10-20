package interfaces.pr23_reple;

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
import utils.sql.SQLUtil;

public class PR23EnviarOrdenesDeCompra extends BaseExecution {
	
	/**
	 * Desc:Verificar que los documentos sean creados exitosamente por la interfaz
	 * @author 1000075178 Ultima modificacion Mariana Vives
	 * @date 27/02/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR23_REPLE_Enviar_Ordenes_Compra(HashMap<String, String> data) throws Exception {
	
		
/* Utilerías *********************************************************************/
	utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
	utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA );
	//utils.sql.SQLUtil dbRMS = new SQLUtil(GlobalVariables.DB_HOST_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/
				
	
	String tdcQueryTiendasPOSUSER =" SELECT PV_CR_PLAZA, PV_CR_TIENDA,B.PE_ID, A.ID,B.DOC_TYPE,B.STATUS,C.PID_ID,B.ID,B.PARTITION_DATE" + 
			" FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C" + 
			" WHERE B.PE_ID = A.ID" + 
			" AND B.DOC_TYPE = 'REC'" + 
			" AND B.STATUS = 'I'" + 
			" AND C.PID_ID = B.ID" + 
			" AND C.PV_CVE_MVT=10" + 
			" AND C.ORDER_NO <> 0" + 
			" AND B.PARTITION_DATE > SYSDATE -45";
	
	
	String tdcQueryORAFIN ="SELECT ORACLE_CR, ORACLE_CR_DESC, ORACLE_CR_SUPERIOR, ORACLE_EF, ORACLE_EF_DESC, ORACLE_CIA, "
			+ "ORACLE_CIA_DESC, LEGACY_EF, LEGACY_CR, RETEK_CR, RETEK_DISTRITO" + 
			" FROM XXFC_MAESTRO_DE_CRS_V" + 
			" WHERE ESTADO = 'A'" + 
			" AND ORACLE_CR_SUPERIOR = '10MON'" + 
			" AND ORACLE_CR = '50MCZ'" + 
			" AND ORACLE_CR_TYPE = 'T'";
	
	String tdcQueryiNFOposuser ="SELECT B.ID, PV_CVE_MVT, TO_CHAR(CREATED_DATE,'YYYYMMDDHH24MISS') "
			+ "CREATED_DATE, PV_CED_ID, PV_CR_FROM_LOC, TSF_NO, EXT_REF_NO, ORDER_NO" + 
			" FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C " + 
			" WHERE SUBSTR(PV_DOC_NAME,4,10)= '10MON'" + 
			" || '50MCZ' AND B.DOC_TYPE='REC' " + 
			" AND STATUS ='I'" + 
			" AND C.PID_ID=B.ID AND C.PV_CVE_MVT=10 AND C.ORDER_NO<>0 AND B.PARTITION_DATE > SYSDATE -45";
	
	
	String tdcQuerydocPOSUSER="SELECT ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO,  PV_RETAIL_PRICE, NVL(REMISION,0) REMISION" + 
			" FROM POS_REC_DETL" + 
			" WHERE PID_ID = '%s'";
			 //[POS_INBOUND_DOCS.ID];";
	
	
	String tdcQueryConnections ="SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD" + 
			" FROM WM_FTP_CONNECTIONS " +
			" WHERE FTP_CONN_ID = 'RTKRMS'";
	
	String tdcQueryStatusLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER"
			+ " FROM WMLOG.WM_LOG_RUN"
			+ " WHERE RUN_ID = %s";
	
	String tdcQueryLogRun = "SELECT * from (SELECT run_id,interface,start_dt,status,server"
			+ " FROM WMLOG.wm_log_run"
			+ " WHERE interface = 'PR1main'"
			+ " and start_dt >= trunc(sysdate) " 
			+ " ORDER BY start_dt DESC) where rownum=1";
	
	String tdcQueryIntegrationServerMex = "SELECT * FROM ( SELECT RUN_ID,START_DT,STATUS"
			+ " FROM WMLOG.WM_LOG_RUN"
			 + " WHERE INTERFACE = 'PR1main'" 	
			 +" AND  START_DT >= TRUNC(SYSDATE)"
		     +" ORDER BY START_DT DESC)"
			+ " WHERE ROWNUM = 1";	
	

	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
			+ " FROM WMLOG.wm_log_run"
			 + " WHERE interface = 'PR1_CO'" 	
			 +" and  start_dt >= TRUNC(SYSDATE)"
		     +" order by start_dt desc)"
			+ " where rownum = 1";	
	
	String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 "
			+ " FROM WMLOG.WM_LOG_THREAD "
			+ " WHERE PARENT_ID = %s";
	
	String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
			+ " FROM WMLOG.WM_LOG_ERROR "
			+ " where RUN_ID=%s"; //FCWMLQA 

	
	
	
	
	
	SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
	PakageManagment pok = new PakageManagment(u, testCase);
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");
	String con ="http://"+user+":"+ps+"@"+server +":5555";
	String searchedStatus = "R";
	String  ftp_conn_id = "RTKRMS";
	String status = "S";
	String statusPos = "E";
	String statusPosInv = "I";
	String id, tarCol, tarMex, run_id;
	String statusWmLog = "S";
	
/*Pasos *********************************************************************************/
	
///										Paso 1 ************************************************	

	
addStep("Verificar que exista informacion dispoble para procesar en ORAFIN.");	

	System.out.println(GlobalVariables.DB_HOST_Puser);
	System.out.println(tdcQueryTiendasPOSUSER);


	SQLResult tinPOS = executeQuery(dbPuser,tdcQueryTiendasPOSUSER);
	boolean TndPOS = tinPOS.isEmpty();

		if (!TndPOS) {
	
			testCase.addQueryEvidenceCurrentStep(tinPOS);
	
		}


assertFalse(TndPOS, "No se otubtuvieron registros");	
	
	
///										Paso 2 ************************************************	
	
addStep("Verificar que exista información en ORAFIN para procesar.");

	System.out.println(GlobalVariables.DB_HOST_Puser);
	System.out.println(tdcQueryORAFIN);


	SQLResult pos = executeQuery(dbPuser, tdcQueryORAFIN);
	boolean oraFIN = pos.isEmpty();
	
		if (!oraFIN) {
			
			testCase.addQueryEvidenceCurrentStep(pos);
			
			
		}
assertFalse(oraFIN, "no muestra registros a procesar");
	
///										Paso 3 ************************************************			
		
addStep("Validar que existan datos para procesar en POSUSER");

	System.out.println(GlobalVariables.DB_HOST_Puser);
	System.out.println(tdcQueryiNFOposuser);


	SQLResult posu = executeQuery(dbPuser, tdcQueryiNFOposuser);
	boolean infPOs = posu.isEmpty();
	
	if (!infPOs) {
		
		testCase.addQueryEvidenceCurrentStep(posu);
		
	}

assertFalse(infPOs, "si registros");


///										Paso 4 ************************************************	

addStep("Validar que existan datos para procesar en POSUSER");

	System.out.println(GlobalVariables.DB_HOST_Puser);
	System.out.println(tdcQuerydocPOSUSER);
	
	SQLResult posur = executeQuery(dbPuser, tdcQuerydocPOSUSER);
	boolean dataP = posur.isEmpty();

		if (!dataP) {
			
			testCase.addQueryEvidenceCurrentStep(posur);
			
		}

assertFalse(dataP, "Error");

///										Paso 5 ************************************************	
addStep("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS. \n"+tdcQueryConnections);


	System.out.println(GlobalVariables.DB_HOST_Puser);
	System.out.println(tdcQueryConnections);

	SQLResult ftpConn = executeQuery(dbPuser,tdcQueryConnections);
	String nameCn = ftpConn.getData(0, "FTP_CONN_ID");
	boolean connId = ftp_conn_id.equals(nameCn);

		if (connId) {
	
			testCase.addQueryEvidenceCurrentStep(ftpConn);
	
		}


///								 Paso 6***************************************************

addStep("Ejecutar el servicio "+data.get("servicio"));
	

	u.get(con);
	String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"),data.get("servicio"));
	String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer,dateExecution);
	SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcIntegrationServerFormat);


	run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");//guarda el run id de la ejecución

boolean valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

		while (valuesStatus) {

					tdcIntegrationServerResult = dbLog.executeQuery(tdcIntegrationServerFormat);


					valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

					u.hardWait(2);

								}

boolean successRun = tdcIntegrationServerResult.getData(0, "STATUS").equals(status);//Valida si se encuentra en estatus S

if(!successRun){

		String error = String.format(tdcQueryErrorId, run_id);

		SQLResult er = dbLog.executeQuery(error);	
		boolean emptyError = er.isEmpty();

		if(!emptyError){  

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(er);

						}
				}


	
/// 								Paso 6***************************************************** 
	
addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");    
	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);	
  System.out.println(tdcQueryLogRun);

  
  SQLResult exeQueryLogRun = executeQuery(dbLog, tdcQueryLogRun);		
	run_id = exeQueryLogRun.getData(0, "RUN_ID");	
	boolean validateStatus = statusWmLog.equals(run_id);
	
	if (validateStatus) {
		
		testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
	}
	
	
	
	System.out.println(validateStatus);
	

	
assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");//cambiar a true

  
//										Paso 7******************************************************
	
addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla WM_LOG_THREAD. ");
		
	String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
	System.out.println(tdcQueryStatusThread);
	SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);

	String regPlazaTienda = queryStatusThreadResult.getData(0, "STATUS");
		boolean statusThread = status.equals(regPlazaTienda);
		System.out.println(statusThread);
			if(!statusThread){

				String	error = String.format(tdcQueryErrorId, run_id);
				SQLResult errorResult = dbLog.executeQuery(error);

				boolean emptyError = errorResult.isEmpty();

		if(!emptyError){  

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorResult);

						}
					}
	
//			Paso 7******************************************************
addStep("Verificar los status en la tabla POS_INBOUND_DOCS.");




		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Enviar ordenes de compra";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR23_REPLE_Enviar_Ordenes_Compra";
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
