package interfaces.ei1;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class EI1_ValidaEjecucion_IMMEX extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EI1_004_ValidaEjecucion_IMMEX(HashMap<String, String> data) throws Exception {
		
		
/* Utiler�as *********************************************************************/		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/**
		 * Impactos de nucleo en IMMEX
		 */
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryFTP = "SELECT FTP_CONN_ID, FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, DESCRIPTION \r\n "
				+ " FROM wmuser.WM_FTP_CONNECTIONS \r\n"
				+ " WHERE FTP_CONN_ID = 'IMMEX_EDI'";
		
		String validaEjecucion = "SELECT run_id,interface, start_dt,end_dt, status \r\n"
				+ "FROM WMLOG.wm_log_run \r\n"
				+ "WHERE interface = 'EI1-Send' \r\n"
				+ "and  start_dt >= TRUNC(SYSDATE) \r\n"
			//	+ "and status = 'S' \r\n"
				+ "and rownum = 1 \r\n"
				+ "order by start_dt desc";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION \r\n"
				+ " FROM WMLOG.WM_LOG_ERROR \r\n"
				+ " where RUN_ID= '%s' \r\n"
				+ " and rownum = 1"; 
		
		String tdcQueryStatusThread = "SELECT thread_id, parent_id, start_dt, end_dt, status, att1, att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = '%s'" ;
		
		String tdcQueryInbound = "SELECT EE_ID, ID, STATUS, DOC_TYPE, TARGET_ID, RUN_ID_SENT \r\n"
				+ " FROM wmuser.EDI_INBOUND_DOCS \r\n"
				+ " WHERE STATUS = 'E' \r\n"
				+ " AND DOC_TYPE = 'ORD' \r\n"
				+ " AND RUN_ID_SENT = '%s'";
		
		String status = "S";
		String run_id = "";
		
        testCase.setProject_Name("Impactos de N�cleo a IMMEX");
		
		testCase.setPrerequisites(data.get("prerequicitos"));
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Tener configurada la conexi�n por FTP en la tabla WM_FTP_CONNECTIONS.");
		
		System.out.println(tdcQueryFTP);
		
		SQLResult ftpResult = executeQuery(dbPos, tdcQueryFTP);
		
		boolean ftp = ftpResult.isEmpty();
		
		if (!ftp) {
			
			testCase.addQueryEvidenceCurrentStep(ftpResult);
		}
		
		System.out.println(ftp);
		
	//	assertFalse(ftp, "No se obtuvo informaci�n de la consulta");
		
//Paso 2 *************************				
		
		//Insertar informaci�n de orden en las tablas: 
		//EDI_ENVELOPE, EDI_INBOUND_DOCS, EDI_ORD y EDI_ORD_DETL de WMUSER
		
		
//Paso 3 *************************
		addStep("Verificar que la interfaz se ejecut� correctamente en WMLOG.");
		
        System.out.println(validaEjecucion);
		
		SQLResult validaEjecucionResult = executeQuery(dbLog, validaEjecucion);
		
		String statusEjecucion = validaEjecucionResult.getData(0, "STATUS");
		
		boolean validaEjecucionBoolean = statusEjecucion.equals(status); //true
		
		System.out.println(validaEjecucionBoolean);
		
		if (validaEjecucionBoolean) {
						
			 run_id = validaEjecucionResult.getData(0, "RUN_ID");
			
			testCase.addQueryEvidenceCurrentStep(validaEjecucionResult);
			
		} else {
			
			run_id = validaEjecucionResult.getData(0, "RUN_ID");
			String errorFormat = String.format(tdcQueryErrorId, run_id);
			System.out.println(errorFormat);
			SQLResult tdcQueryErrorIdResult = executeQuery(dbLog, errorFormat);
			testCase.addQueryEvidenceCurrentStep(tdcQueryErrorIdResult);
			
		}
		
		System.out.println(validaEjecucionBoolean);
		
	//	assertFalse(validaEjecucionBoolean, "Se detecto un error en la ejecucion ");
		
//Paso 4	************************
		addStep("Se valida la generacion de thread.");
		
		run_id = validaEjecucionResult.getData(0, "RUN_ID");
					
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);
				
		String thread_id = threadResult.getData(0, "THREAD_ID");

		boolean validaThreads = threadResult.isEmpty();
		
		if (!validaThreads) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
				    
				} 

		System.out.println(validaThreads);

	//	assertFalse(validaThreads, "No se generaron threads");	
		
//Paso 5 ***************************
		addStep("Validar que se actualiza el estado de los documentos a E en la tabla EDI_INBOUND_DOCS de WMUSER.");
	
		String inboundFormat = String.format(tdcQueryInbound, thread_id);
		
		SQLResult inboundResult = dbPos.executeQuery(inboundFormat);
		
		System.out.println(inboundFormat);
		String doc = "";
		
		boolean inbound = inboundResult.isEmpty();
		
		if (!inbound) {
			
			testCase.addQueryEvidenceCurrentStep(inboundResult);
			doc = inboundResult.getData(0, "TARGET_ID");
			
		}
		
		System.out.println(inbound);
		
	//	assertFalse(inbound, "No se actualizo el estado de los documentos a E en la tabla EDI_INBOUND_DOCS de WMUSER.");
		
//paso 6 ***************************** falta la ruta correcta
	/*	
		addStep("Validar que se envi� correctamente el archivo por FTP a IMMEX.");
		
		
		FTPUtil ftpOB = new FTPUtil("10.182.92.13", 21, "wmuser", "   ");
		
		                            //host,       puerto,usuario,contrase�a

		String ruta = "/export/home/wmuser/testRI1/" + data.get("cr_plaza") + "/working/" + doc;
		
		System.out.println("Ruta: " + ruta);

		if (ftpOB.fileExists(ruta)) {

		testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
					+ data.get("cr_plaza") + "/working/" + doc);

		} else {

			System.out.println("No Existe");

		}
		

		assertFalse(!ftpOB.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);*/
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. ATM-FT-010-Validar ejecuci�n de interface FEMSA_EI1";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EI1_004_ValidaEjecucion_IMMEX";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
