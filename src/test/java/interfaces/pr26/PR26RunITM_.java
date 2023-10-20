package interfaces.pr26;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import utils.sql.SQLUtil;

public class PR26RunITM_ extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		/*
		 * Utiler�as
		 *********************************************************************/
		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
//		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASSIT, GlobalVariables.DB_USER_FCIASSIT, GlobalVariables.DB_PASSWORD_FCIASSIT);
//		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCEBSSIT, GlobalVariables.DB_USER_FCEBSSIT, GlobalVariables.DB_PASSWORD_FCEBSSIT);
//		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
//		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR,GlobalVariables.DB_PASSWORD_FCRMSMGR); 
		
	
		//Paso 1
		String plaza = "SELECT ORACLE_CR,ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE FROM XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'" + " AND ORACLE_CR = '" + data.get("tienda")
				+ "'" + " AND ORACLE_CR_TYPE = 'T'";

		//Paso 2
		String conITM = "SELECT  * FROM (SELECT  WM_TARGET_ITM, load_batch_id, location, wm_itm_status "
				+ "FROM wmuser.pos_itm_prm_head " + " WHERE  wm_itm_status='L' and location = '%s' ORDER BY LOAD_DATE DESC  )  "
				+ "  WHERE rownum <= 1  ";
		
		//Paso  3 , 4 y 5
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE = 'PR26_ITM' "
				+ "ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion
		
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaTHREAD = "select * from (SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  "
				+ "FROM wmlog.WM_LOG_THREAD" + " where PARENT_ID= '%s' ) where rownum <=1"; 

		//Paso 6
		String consultaTHREAD2 = "select * from (SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD "
				+ "where PARENT_ID='%s'  ) where rownum <=1";

		//Paso 7
		String consultaITM = "	select WM_TARGET_ITM, load_batch_id, location, wm_itm_status "
				+ "from WMUSER.POS_ITM_PRM_HEAD where load_batch_id='%s' and location='%s' and wm_itm_status = 'E' order by load_date desc";

		//Paso 8
		String consultaL = " select * from (select id, target_type ,doc_type, sent_date "
				+ "from POSUSER.POS_OUTBOUND_DOCS" + " where DOC_TYPE='ITM' and STATUS='L' and  doc_name = '%s'   order by SENT_DATE desc) "
				+ "WHERE rownum <= 1";

		String consultaL2 = "select * from (select pv_cr_plaza, pv_cr_tienda, status, source_id, partition_date"
				+ " from POSUSER.POS_OUTBOUND_DOCS " + "where DOC_TYPE='ITM' and STATUS='L' and  doc_name = '%s' order by SENT_DATE desc) "
				+ "WHERE rownum <= 1 ";
		
		boolean ValidapasoE = false;
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)"); 

		
//***************************Paso 1*****************************************************************************		
		addStep("Obtener el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN para la plaza  y tienda .");
		
		
		System.out.println(plaza);
	
		SQLResult plazas = dbEbs.executeQuery(plaza);
		String retek = plazas.getData(0, "RETEK_CR");
		System.out.println("RETEK_CR: " + retek);
		boolean paso11 = plazas.isEmpty();
		if (!paso11) {
			testCase.addQueryEvidenceCurrentStep(plazas);
		}
	//	assertFalse("No se muestra la informaci�n de la tienda.", paso11);

//*************************Paso 2 *********************************************************************************
		
		addStep("Validar que exista informaci�n pendiente por procesar en la tabla WMUSER.POS_ITM_PRM_HEAD DE RETEK con WM_ITM_STATUS = 'L'");
		
		String ITM_Format = String.format(conITM, retek);
		
		System.out.println(ITM_Format);
		SQLResult ITM1 = dbRms.executeQuery(ITM_Format);
		
//		String id = ITM1.getData(0, "LOAD_BATCH_ID");
//		System.out.println("LOAD_BARCH_ID " + id);
//		
//		String loc = ITM1.getData(0, "LOCATION");
//		System.out.println("LOCATION: " + loc);
//		
//		boolean paso1 = ITM1.isEmpty();
//		if (!paso1) {
//			testCase.addQueryEvidenceCurrentStep(ITM1);
//		}
		String id = "";
		String loc= "";
		
		boolean  paso1 = ITM1.isEmpty();
		if (!paso1) {

			id = ITM1.getData(0, "LOAD_BATCH_ID");
			System.out.println("LOAD_BARCH_ID " + id);
			
			loc = ITM1.getData(0, "LOCATION");
			System.out.println("LOCATION: " + loc);
			
			testCase.addQueryEvidenceCurrentStep(ITM1);
		}
		testCase.addQueryEvidenceCurrentStep(ITM1);
	//	assertFalse("No se  muestra la informaci�n pendiente por procesar para la tienda ", paso1);

//***********************Paso 3 *********************************************************************************
		
		addStep("Ejecutar el servicio PR26.Pub:runITM con el job runITM de Ctrl-M para "
				+ "enviar la informaci�n relacionada a los art�culos, cambios de precio y promociones de RMS a POS ");
	
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		String searchedStatus = "R";
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
	
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			is = executeQuery(dbLOG, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLOG.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLOG.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLOG.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//********************************Paso 4 ****************************************************************
		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		
		SQLResult is1 = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		
		boolean validateStatus = fcwS.equals(status);
		System.out.println("Status es S = "+validateStatus );
		
		SQLResult log = dbLOG.executeQuery(tdcIntegrationServerFormat);
		System.out.println(tdcIntegrationServerFormat);
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(log);
		}
		testCase.addQueryEvidenceCurrentStep(log);
		
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
		

//********************************Paso 5 **************************************************************

		addStep("Validar que el registro de ejecuci�n  en la tabla WM_LOG_THREAD.");

		String consultaTH = String.format(consultaTHREAD, run_id);
		
		System.out.println(consultaTH);
		
		SQLResult THRE = dbLOG.executeQuery(consultaTH);

		boolean paso1TH = THRE.isEmpty();
		
		if (!paso1TH) {
			testCase.addQueryEvidenceCurrentStep(THRE);
		}
		testCase.addQueryEvidenceCurrentStep(THRE);
		System.out.println(paso1TH);
		
		// .-----------Segunda consulta

		String consultaTH2 = String.format(consultaTHREAD2, run_id);
		
		System.out.println(consultaTH2);
		
		SQLResult THRE2 = dbLOG.executeQuery(consultaTH2);

		boolean paso1TH2 = THRE.isEmpty();
		
		if (!paso1TH2) {
			testCase.addQueryEvidenceCurrentStep(THRE2);
		}
		testCase.addQueryEvidenceCurrentStep(THRE2);
		
		System.out.println(paso1TH2);
		
		assertFalse("No se generaron threads ", paso1TH2);
						
//***********************Paso 6 *******************************************************************
		
		addStep("Validar que se actualice el estatus y el nombre del archivo generado por la interface de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK ");
		
		String wm_target_itm = "";
		ITM_Format = String.format(conITM, retek);
		
		System.out.println(ITM_Format);
		ITM1 = dbRms.executeQuery(ITM_Format);
		boolean ValidaITM1 = ITM1.isEmpty();
		
		

		if(!ValidaITM1) {
			//Variables del paso 2
			id = ITM1.getData(0, "LOAD_BATCH_ID");
			loc = ITM1.getData(0, "LOCATION");
			
			String consultas4 = String.format(consultaITM, id, loc);
			
			System.out.println(consultas4);
			SQLResult ITME = dbRms.executeQuery(consultas4);
			wm_target_itm = ITME.getData(0, "WM_TARGET_ITM");
			
			System.out.println("WM_TARGET_ITM" + wm_target_itm );
			testCase.addQueryEvidenceCurrentStep(ITME);
			
			 ValidapasoE = ITME.isEmpty();
			if (! ValidapasoE) {
				testCase.addQueryEvidenceCurrentStep(ITME);
				}
			}
		
		
		assertTrue("No se actualizo el estatus de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK ",  ValidapasoE);
			
//*******************************************************************************************		
	/*	String consultas4 = String.format(consultaITM, id, loc);
	
		System.out.println(consultas4); 
		
		SQLResult ITME = dbRms.executeQuery(consultas4);
	
      String wm_target_itm = ITME.getData(0, "WM_TARGET_ITM");
		
		System.out.println("WM_TARGET_ITM" + wm_target_itm );

		boolean pasoE = ITME.isEmpty();
		
		if (!pasoE) {
			testCase.addQueryEvidenceCurrentStep(ITME);
		}
		
		System.out.println(pasoE);
		assertFalse("No se actualizo el estatus de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK ", pasoE);
		
		*/
		
//********************** Paso 7 *******************************************************************
		
		addStep(" Validar que se inserte el registro del documento ITM generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER.");

       String consultaDoc = String.format(consultaL, wm_target_itm );
		
		System.out.println(consultaDoc);
		
		SQLResult L = dbPOSUSER.executeQuery(consultaDoc);
		
   
		boolean pasoL = L.isEmpty();
		if (!pasoL) {
			testCase.addQueryEvidenceCurrentStep(L);
		}
		testCase.addQueryEvidenceCurrentStep(L);
		System.out.println(pasoL);
		// .-----------Segunda consulta
		
		String consultaDoc2 = String.format(consultaL2, wm_target_itm );

		SQLResult L2 = dbPOSUSER.executeQuery(consultaDoc2);
		
		System.out.println(consultaDoc2);

		boolean pasoL2 = L2.isEmpty();
		if (!pasoL2) {
			testCase.addQueryEvidenceCurrentStep(L2);
		}
		testCase.addQueryEvidenceCurrentStep(L2);
		System.out.println(pasoL2);
		
	assertFalse("No se inserta el detalle del documento generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER.", pasoL2);
		
//Paso 8*************************************************************************

		       Thread.sleep(20000);
		
				addStep(" Validar que se realice el env�o del archivo ITM generado por la interface en el directorio configurado para la tienda procesada ");
				
		       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		       
		       Thread.sleep(20000);
		       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + wm_target_itm;
		                                 //host, puerto, usuario, contrase�a
		       ///u01/posuser
		       boolean validaFTP;
		       
		        if (ftp.fileExists(ruta) ) {
		        	
		        	validaFTP = true;
		            testCase.addFileEvidenceCurrentStep(ruta);
		            System.out.println("Existe");
		            testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
		            testCase.addBoldTextEvidenceCurrentStep(ruta);
		            
		        }else {
		        	testCase.addFileEvidenceCurrentStep(ruta);
		        	testCase.addBoldTextEvidenceCurrentStep("El archivo no existe ");
		            System.out.println("No Existe");
		            validaFTP = false;
		        }
		        
		        assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
		        

	}

	@Override
	public String setTestFullName() {
		return "MTC-FT-005-Generaci�n de documento ITM - M�xico";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la ejecuci�n del servicio PR26.Pub:runITM para enviar la informaci�n relacionada a los art�culos, cambios de precio y promociones de Retek(RMS) a POS (Archivo Item Maset ITM).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMTIZACION";
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