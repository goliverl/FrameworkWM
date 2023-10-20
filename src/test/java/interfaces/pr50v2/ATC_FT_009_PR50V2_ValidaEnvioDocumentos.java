package interfaces.pr50v2;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;


import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_009_PR50V2_ValidaEnvioDocumentos extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_PR50V2_ValidaEnvioDocumentos_test(HashMap<String, String> data) throws Exception {


		

/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Validar el envio de documentos  (Outbound)
		 */
		
/** VARIABLES *********************************************************************/	

		String ValidaDocs =	"SELECT ID,DOC_NAME,DOC_TYPE,PV_CR_PLAZA,PV_CR_TIENDA,STATUS FROM posuser.pos_outbound_docs "
				+ "WHERE pv_cr_plaza =  '" + data.get("plaza") + "' "
				+ "AND pv_cr_tienda =  '" + data.get("tienda") + "' "
				+ "AND doc_type =  '" + data.get("doc_type") + "' "
				+ "AND status = 'L'"
				+ "And DOC_NAME='" + data.get("doc_name") + "'";
		
		String ValidaStatus =	"SELECT SELECT ID,POE_ID,SENT_DATE,DOC_NAME,DOC_TYPE,PV_CR_PLAZA,PV_CR_TIENDA,STATUS FROM posuser.pos_outbound_docs "
				+ "WHERE pv_cr_plaza ='" + data.get("plaza") + "' "
				+ "AND pv_cr_tienda = '" + data.get("tienda") + "' "
				+ "AND doc_type = '" + data.get("doc_type") + "' "
				+ "AND date_sent >= TRUNC(SYSDATE) "
				+ "AND status = 'P'";
	
		String ValidaInsert = "SELECT * FROM posuser.pos_outbound_env "
				+ "WHERE cr_plaza = '" + data.get("plaza") + "' "
				+ "AND cr_tienda = '" + data.get("tienda") + "' "
				+ "AND id = %s "
				+ "AND date_created >= TRUNC(SYSDATE)";

		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR50%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1";
		String consulta61 = " select * from (select description,MESSAGE from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		

		addStep("Validar que existan documentos pendientes de procesar para la Plaza y Tienda en la tabla pos_outbound_docs de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(ValidaDocs);
		SQLResult queryInf = dbPos.executeQuery(ValidaDocs);
		boolean infDoc = queryInf.isEmpty();
		if (!infDoc) {
			testCase.addQueryEvidenceCurrentStep(queryInf);

		}

		System.out.println(infDoc);

		assertFalse(infDoc, "No se obtiene informacion de la consulta");
		

		/* PASO 2 *********************************************************************/
		
				addStep("Validar que exista el archivo a procesar.");
		
				System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
				System.out.println(ValidaDocs);
				SQLResult tdcQueryDocASNQuery = dbPos.executeQuery(ValidaDocs);
				boolean ASN = tdcQueryDocASNQuery.isEmpty();
				String doc="";
				if (!ASN) {
					 doc = tdcQueryDocASNQuery.getData(0, "DOC_NAME");
					System.out.println("docum: "+doc);
				}

				System.out.println(ASN);
				
		
		 FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser"); 
			  
			  String ruta= "/u01/posuser/FEMSA_OXXO/POS/"+data.get("plaza")+"/"+data.get("tienda")+"/working/"+doc;
		    	System.out.println("Ruta: "+ruta);
		    	
		    	
			    if (ftp.fileExists(ruta)) { 
			    	
			    	testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"+data.get("plaza")+"/"+data.get("plaza")+"/working/"+doc);
			   	 
			   	  
			    	}else { 
			    		
			    		System.out.println("No Existe"); 
			    		
			    		}
			    
			    
			    assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: "+ruta);
//PAso 3 ************************************************************************************************************************************					
			    
	    addStep("El servidor TN realizara la ejecucion del servicio PR50V2.pub:runOutbound del servidor IS, este servicio comenzara con el procesamiento del documento pendiente.");

	    SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
	    
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		String error = String.format(consulta6, run_id);
		String error1 = String.format(consulta61, run_id);
		String error2 = String.format(consulta62, run_id);

		SQLResult errorr = dbLog.executeQuery(error);
		boolean emptyError = errorr.isEmpty();
		

		
		/* PASO 4 *********************************************************************/

		addStep("Validar la actualizaci�n del campo pos_outbound_docs.poe_id con el Id del registro del documento Zip creado y con estatus 'P'.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(ValidaStatus);
		SQLResult ValidaSt = dbPos.executeQuery(ValidaStatus);
		

		boolean Resstatus = ValidaSt.isEmpty();		
		System.out.println(Resstatus);
		String POEID="";
		if (!Resstatus) {
			
			testCase.addQueryEvidenceCurrentStep(ValidaSt);
			POEID = ValidaSt.getData(0, "POE_ID");
			System.out.println("pOE_ID: "+POEID);
			
		}
		
		
		assertFalse(Resstatus, "la actualizaci�n del campo pos_outbound_docs.poe_id no fue exitoso");
		

		/* PASO 5 *********************************************************************/
		
		
		addStep("Validar la inserci�n del registro en la tabla pos_outbound_env con la informaci�n del archivo Zip generado.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String InsertReg =  String.format(ValidaInsert, POEID);
		System.out.println(InsertReg);
		SQLResult ValidResu = dbPos.executeQuery(InsertReg);
		
		boolean Res = ValidResu.isEmpty();
		
		if (!Res) {
			testCase.addQueryEvidenceCurrentStep(ValidResu);
			
		}

		System.out.println(Res);
		assertFalse(Res, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Valida envio de documentos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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

