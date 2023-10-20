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

public class PR1_Mexico extends BaseExecution{

//ALM caso de enmedio
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR1_Mexico_test(HashMap<String, String> data) throws Exception {
	
		/* Utilerías *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	
		utils.sql.SQLUtil dbRmsMex = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA );
		
		/**
		 * Variables ******************************************************************************************
		 * 
		 * 
		*/
		
		String tdcQueryConnections = "SELECT FTP_CONN_ID,FTP_BASE_DIR,FTP_SERVERHOST,DESCRIPTION "
				+ " FROM WMUSER.WM_FTP_CONNECTIONS"
				+ " WHERE FTP_CONN_ID = 'RTKRMS'"; //Primer paso
		
		String tdcQueryConnectionsTSF ="SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID "
				+" FROM POSUSER.POS_INBOUND_DOCS " + 
				" WHERE DOC_TYPE IN ('TSF')" + 
				" AND STATUS ='I'" + 
				" AND PARTITION_DATE > SYSDATE - 13"+
				" ORDER BY RECEIVED_DATE  DESC";  //Segundo paso
	    
	    String tdcQueryPosTSF2 = "SELECT PID_ID,NO_RECORDS,PV_CVE_MVT,SHIP_DATE,PV_CR_FROM_LOC,CEDIS_ID  FROM POSUSER.POS_TSF" + 
	    		" WHERE PID_ID IN ("+data.get("col")+","+data.get("mex")+")"+ 
				" ORDER BY PARTITION_DATE DESC";
		
	    String tdcQueryPosDETLTSF2 = "SELECT * FROM POSUSER.POS_TSF_DETL" +  
	   		    " WHERE PID_ID in ("+data.get("col")+","+data.get("mex")+")" ;  //Paso 4
					
		
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
		

	/*	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR1_CO'" 	
				 +" and  start_dt >= TRUNC(SYSDATE)"
			     +" order by start_dt desc)"
				+ " where rownum = 1";	*/
		
		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 "
				+ " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = '%s'";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; //FCWMLQA 

		
		String tdcQueryConnectionsTSF2 = "SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID "+
				" FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE DOC_TYPE IN ('TSF')" + 
				" AND ID IN ("+data.get("col")+","+data.get("mex")+")"+
				" ORDER BY RECEIVED_DATE DESC"; //Consulta los archivos TSF en posuser tercer paso
		
		String tdcQueryConnectionsTSFCol = "SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID "+
				" FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE DOC_TYPE IN ('TSF')" + 
				" AND ID = "+data.get("col");	
		
		String tdcQueryConnectionsTSFMex = "SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID "+
				" FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE DOC_TYPE IN ('TSF')" + 
				" AND ID = "+data.get("mex");	

		
		String tdcQueryRetekSendDoc ="SELECT DOC_TYPE,CR_PLAZA,CR_TIENDA,STATUS,RUN_ID,RTK_FILENAME "
				+ " FROM WMUSER.RTK_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'TSF' "
				+ " AND STATUS = 'L' "
				+ " AND RTK_FILENAME IN ('%s')";

		
		
		
		String statusWmLog = "S";
		
	/*Pasos *********************************************************************************/
		
	///										Paso 1 ************************************************
	addStep("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS. \n"+tdcQueryConnections);
		

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryConnections);
		
		SQLResult ftpConn = executeQuery(dbPos,tdcQueryConnections);
		
		boolean connId = ftpConn.isEmpty();
		
		if (!connId) {
			
			testCase.addQueryEvidenceCurrentStep(ftpConn);
			
		}
		
		
		
	assertFalse(connId,"No se encontró la configuración del servidor");
		
	System.out.println(connId);


	/// 								Paso 2****************************************************
		
		
	addStep("Deben existir documentos TSF pendientes de procesar en la tabla POS_INBOUND_DOCS de POSUSER. \n"+tdcQueryConnectionsTSF);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
		System.out.println(tdcQueryConnectionsTSF);
		
		SQLResult tsfPen = executeQuery(dbPos, tdcQueryConnectionsTSF);
		boolean docTsf = tsfPen.isEmpty();
		
		if (!docTsf) {
			
			testCase.addQueryEvidenceCurrentStep(tsfPen);
			
		}

			
		
		
	assertFalse(docTsf,"No existen documentos pendientes de procesar");
	System.out.println(docTsf);
		
	/// 								Paso 3****************************************************
		
	addStep("Validar que se muestre el registro relacionado con el archivo TSF en la tabla POS_TSF de POSUSER. \n"+tdcQueryPosTSF2);
			
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
		System.out.println(tdcQueryPosTSF2);
		
		SQLResult reg = executeQuery(dbPos, tdcQueryConnectionsTSF2);
			
			boolean posTsf = reg.isEmpty();
		
					if (!posTsf) {
			
								testCase.addQueryEvidenceCurrentStep(reg);
			
							}
		
		
			
	assertFalse(posTsf,"No hay registro relacionado con los documentos TSF");

	System.out.println(posTsf);

		
	/// 								Paso 4****************************************************
					
	addStep("Validar que se encuentre el detalle de la transferencia en la tabla POS_TSF_DETL de POSUSER \n"+tdcQueryPosDETLTSF2);
			
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
		System.out.println(tdcQueryPosDETLTSF2);
		
		SQLResult posTSF = executeQuery(dbPos, tdcQueryPosDETLTSF2);
		boolean posDetl = posTSF.isEmpty();
			
		if (!posDetl) {
			
			testCase.addQueryEvidenceCurrentStep(posTSF);
			
		}
		

		
	assertFalse(posDetl,"No hay detalle de las transferencias");
		System.out.print(posDetl);
		
		
	///								 Paso 5***************************************************
		
	addStep("Ejecutar el servicio "+data.get("servicio"));


	SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
	PakageManagment pok = new PakageManagment(u, testCase);
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");
	String con ="http://"+user+":"+ps+"@"+server +":5555";
	String searchedStatus = "R";

	String status = "S";
	String statusPos = "E";
	String statusPosInv = "I";
	String  run_id;
		

		u.get(con);
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"),data.get("servicio"));
		System.out.println("Excecution date " + dateExecution );
		SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServerMex);
		
		System.out.println(tdcQueryIntegrationServerMex);


		run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");//guarda el run id de la ejecución

	boolean valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

			while (valuesStatus) {

						tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServerMex);


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
	    
	   
			
		boolean validateStatus = statusWmLog.equals(exeQueryLogRun.getData(0,"STATUS"));
		
		if (validateStatus) {
			
			testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
		}
		
		
		
		System.out.println(validateStatus);
		

		
	assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");//cambiar a true

	    
//											Paso 7******************************************************
		
	addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla WM_LOG_THREAD. ");
			
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(tdcQueryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);

		
			boolean statusThread = queryStatusThreadResult.isEmpty();
			System.out.println(statusThread);
			if (!statusThread) {

				testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
				String error = String.format(tdcQueryErrorId, run_id);
				SQLResult errorResult = dbLog.executeQuery(error);

				boolean emptyError = errorResult.isEmpty();

				if (!emptyError) {

					testCase.addTextEvidenceCurrentStep(
							"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

					testCase.addQueryEvidenceCurrentStep(errorResult);

				}
			}

			assertFalse(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		
		
		/// 								Paso 8****************************************************
		
		
	addStep("Validar nuevamente la tabla POS_INBOUND_DOCS de POSUSER, donde solamente el registro de México cambie a estatus E.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
		System.out.println(tdcQueryConnectionsTSF2);
		
		
		
		
		//colombia
		//String queryConnectionsTSFCol = String.format(tdcQueryConnectionsTSFCol,id);
		
		
		
		SQLResult getCOL = executeQuery(dbPos, tdcQueryConnectionsTSFCol);
		
		System.out.println(tdcQueryConnectionsTSFCol);
		
		String statusCol2 = getCOL.getData(0, "STATUS");
		
		
		SQLResult getMex = executeQuery(dbPos, tdcQueryConnectionsTSFMex);
		
		System.out.println(tdcQueryConnectionsTSFMex);
		
		String statusMex2 = getMex.getData(0, "STATUS");
		
		
		String targetMex = getMex.getData(0, "TARGET_ID");
		
		System.out.println("TARGET_ID "+ targetMex);
		
		
		

		 //MEXICO
	/*    if(statusMex2.equals(statusPos)&&statusCol2.equals(statusPos)) {
	    	
	    assertTrue(statusMex2.equals(statusPos)&&statusCol2.equals(statusPosInv),"Se procesaron los dos archivos");		
	    
	    }
	    
	    if(statusMex2.equals(statusPos)&&statusCol2.equals(statusPosInv)) {
	        assertTrue(statusMex2.equals(statusPos)&&statusCol2.equals(statusPosInv));		
	    }
	    
	    if(statusMex2.equals(statusPosInv)&&statusCol2.equals(statusPosInv)) {
	        assertFalse(statusMex2.equals(statusPos)&&statusCol2.equals(statusPosInv),"No se proceso ningun archivo");		
	    }
	  */  
	    boolean resultado= false;
		
		if(statusMex2.equals(statusPos)&&statusCol2.equals(statusPosInv)) {
			
			resultado = true;
	        
	    }
	    
		assertTrue(resultado,"Solo se proceso el archivo de Mexico");
		
	///								Paso 9********************************************************
	addStep("Validar el registro del documento enviado al servidor FTP de RETEK en la tabla RTK_INBOUND_DOCS.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		
		
		
	    String queryRetekSendDoc = String.format(tdcQueryRetekSendDoc, targetMex);	
		System.out.println(queryRetekSendDoc);
		SQLResult rtk = executeQuery(dbRmsMex, queryRetekSendDoc);
		
		
		
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
		return "Realizar la ejecución de la interface  teniendo por procesar dos archivos TSF, uno correspondiente a México y otro a Colombia , validar que sea procesado el de Mexico.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Qaautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR1_Mexico_test";
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

