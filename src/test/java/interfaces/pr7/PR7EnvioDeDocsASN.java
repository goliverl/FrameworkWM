package interfaces.pr7;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;

/**
 * @author 41335
 */

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR7EnvioDeDocsASN extends BaseExecution {
	
	/**
	 * Desc: Validar el envío de los documentos ASN al POS insertados en la tabla de control para la Plaza 10MON y Tienda 50EDI
	 * @author Ultima modificacion: Mariana Vives
	 * @date 27/02/2023
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void PR7EnvioDeDocsASN_test(HashMap<String, String> data) throws Exception {
	
		testCase.setPrerequisites("Validar el envío de los documentos ASN al POS de lo registros actualizados en la tabla de control "
				+ "para la Plaza  y Tienda");
		
	
		
/* Utilerías *********************************************************************/
	
	utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA );
	utils.sql.SQLUtil dbRMS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
	
	
	
/**
* Variables ******************************************************************************************
* 
* 
*/
			

	String tdcQueryTransControl = "Select load_batch_id,wm_status from transfer_control "
			+ " WHERE load_date >= SYSDATE-3 "
			+ " AND  wm_status = 'L' ";
	
	
	String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
			+ " FROM WMLOG.WM_LOG_ERROR "
			+ " where RUN_ID= '%s'"; //FCWMLQA 
	
	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
			+ " FROM WMLOG.wm_log_run"
			 + " WHERE interface = PR7'" 	
			 +" and  start_dt >= TRUNC(SYSDATE)"
		     +" order by start_dt desc)"
			+ " where rownum = 1";	
	
	String tdcQueryLogRun = "SELECT * from (SELECT run_id,interface,start_dt,status,server"
			+ " FROM WMLOG.wm_log_run"
			+ " WHERE interface  'PR7%'"
			+ " and start_dt >= trunc(sysdate) " 
			+ " ORDER BY start_dt DESC) where rownum=1";
	

	String tdcQueryRegTrnafer = "SELECT * "
			+ " FROM transfer_control "
			+ " WHERE load_batch_id = '%s'" 
			+ " AND wm_status = 'E' ";
	
	String tdcQueryPOSuser ="SELECT * "
			+ " FROM pos_outbound_docs "
			+ " WHERE doc_type = 'ASN' "
			+ " AND status = 'L' "
			+ " AND TRUNC(sent_date) = TRUNC(SYSDATE) "
			+ " AND pv_cr_plaza = '"+ data.get("plaza")+ "' "
			+ " AND pv_cr_tienda = '"+ data.get("tienda")+ "'";
	
	//source_id = batch
	
	
	

	
/**
* Pasos ******************************************************************************************
*  
* 
*/
		
	
	
addStep("Actualizar el estatus del load_batch_id  de la tabla transfer_control al valor 'L' en RETEK.");
System.out.println(GlobalVariables.DB_HOST_MEX);
System.out.println(tdcQueryTransControl);

 	SQLResult trnsf = executeQuery(dbRMS, tdcQueryTransControl);
 	String load_bath_id = trnsf.getData(0, "LOAD_BATCH_ID");
 	System.out.println("LOAD_BATCH_ID = "+ load_bath_id);

 	boolean trsnfEmpty = tdcQueryTransControl.isEmpty();
 
 
 		if (!trsnfEmpty) {
			
 			testCase.addQueryEvidenceCurrentStep(trnsf);
 			
 			
		}
 

assertFalse(trsnfEmpty, "No se tiene registros a procesar ");



addStep("Validar la ejecución de la interfaz PR7.", "");	

SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
PakageManagment pok = new PakageManagment(u, testCase);
String user = data.get("user");
String ps = PasswordUtil.decryptPassword( data.get("ps"));
String server = data.get("server");
String con ="http://"+user+":"+ps+"@"+server+ ":5555";
String searchedStatus = "R";
String status = "S";
String  run_id;
String statusWmLog = "S";	

u.get(con);
String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"),data.get("servicio"));
System.out.println(dateExecution);
SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer);

run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");//guarda el run id de la ejecución

boolean valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

	while (valuesStatus) {

				tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer);


				valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

				u.hardWait(2);

							}

boolean successRun = tdcIntegrationServerResult.getData(0, "STATUS").equals(status);//Valida si se encuentra en estatus S

if(!successRun){

	String error = String.format(tdcQueryErrorId, run_id);

	SQLResult er = dbLog.executeQuery(error);	
	boolean emptyError = er.isEmpty();

	if(!emptyError){  

			testCase.addTextEvidenceCurrentStep("Se inserta los detalles del error generado durante la ejecución de la interface en la tabla WM_LOG_ERROR de WMLOG.");

			testCase.addQueryEvidenceCurrentStep(er);

					}
			}




addStep("Validar la correcta ejecución de la interface PR7 en la tabla WM WM_LOG_RUN de WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);	
		System.out.println(tdcQueryLogRun);


		SQLResult exeQueryLogRun = executeQuery(dbLog, tdcQueryLogRun);		
			run_id = exeQueryLogRun.getData(0, "RUN_ID");	
			boolean validateStatus = statusWmLog.equals(run_id);

				if (validateStatus) {
	
					testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
				}



				System.out.println(validateStatus);



assertTrue(validateStatus,"Se inserta el detalle de la ejecución de la interface PR07 en la tabla WM_LOG_RUN de WMLOG con STATUS='E'");//cambiar a true
	



addStep("Validar el registro de documento ASN creado en la tabla pos_outbound_docs de POSUSER.", "");	
System.out.println(GlobalVariables.DB_HOST_Puser);
System.out.println(tdcQueryPOSuser);

	SQLResult exeQueryPOSuser = executeQuery(dbPuser, tdcQueryPOSuser);
	
		boolean validatePosuser = tdcQueryPOSuser.isEmpty();
		
			if (!validatePosuser) {

				testCase.addQueryEvidenceCurrentStep(exeQueryPOSuser);
			}

assertFalse(validatePosuser, "No se proceso ningun archivo");


addStep("Validar la actualización del registro del load_bacth_id en la tabla transfer_control de RETEK a estatus 'E'.", "");
System.out.println(GlobalVariables.DB_HOST_MEX);
System.out.println(tdcQueryRegTrnafer);

String tdcQueryRegTrnafer_format = String.format(tdcQueryRegTrnafer,load_bath_id);
	SQLResult exeQueryTransf = executeQuery(dbRMS, tdcQueryRegTrnafer_format);
	
		boolean validateRMS = tdcQueryRegTrnafer.isEmpty();
		
		if (!validateRMS) {
			
			testCase.addQueryEvidenceCurrentStep(exeQueryTransf);
			
		}
		
		System.out.println(validateRMS);
	
assertFalse(validateRMS, "No se proceso ningun registro");	
	
	
	
	
	
	}
	
	
	
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "PR7EnvioDeDocsASN_test";
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