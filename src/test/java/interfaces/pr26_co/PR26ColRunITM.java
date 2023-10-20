package interfaces.pr26_co;

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

public class PR26ColRunITM extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_pr26_co_PR26ColRunITM(HashMap<String, String> data) throws Exception {
		/*
	
		 * 
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbRmsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		//Paso 1
		
		String plaza = "SELECT ORACLE_CR,ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE FROM XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'" + "AND ORACLE_CR = '" + data.get("tienda")
				+ "'" + "AND ORACLE_CR_TYPE = 'T'";
 
		//Paso 2
		
		String ITM = "select*from(SELECT BATCH_ID, ARCHIVO, LOAD_DATE, LOAD_BATCH_ID, WM_ITM_STATUS FROM WMUSER.pos_itm_prm_head_col \r\n"
				+ "WHERE  wm_itm_status = 'L' and location = '%s' ORDER BY LOAD_DATE DESC ) WHERE rownum <= 1 "; 
		
		//Paso 3
		
		String INFITM = "SELECT STORE, START_DATE, TIME, TRAN_TYPE, ITEM FROM WMUSER.fem_pos_mods_stg_col \r\n"
				+ "WHERE load_batch_id = '%s'\r\n"  + "     AND store = '%s'";
		String INFITM2 = "SELECT  ITEM_SHORT_DESC, ITEM_LONG_DESC, DEPT,CLASS, NEW_PRICE, LOAD_BATCH_ID  FROM WMUSER.fem_pos_mods_stg_col \r\n"
				+ "WHERE load_batch_id = '%s'\r\n" +  "     AND store = '%s'";
		
		//Paso 4 y 5
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE= 'PR26_COmain' "
				+ "ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 " 
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		//Paso 6
		
		String IMTPROC = "SELECT BATCH_ID, LOCATION, ARCHIVO, LOAD_DATE,LOAD_WEEK FROM WMUSER.pos_itm_prm_head_col "
				+ " WHERE load_batch_id = '%s' " + " AND location = '%s'" + "AND wm_itm_status = 'E'   ";
		String IMTPROC2 = "SELECT LOAD_BATCH_ID, WM_ITM_STATUS, WM_TARGET_ITM   FROM WMUSER.pos_itm_prm_head_col "
				+ " WHERE load_batch_id = '%s' " + " AND location = '%s'" + " AND wm_itm_status = 'E'  ";
		
		//Paso 7
		
		String consultaL = " select * from (select id,target_type, doc_name,doc_type, sent_date "
				+ "from POSUSER.POS_OUTBOUND_DOCS" + " where DOC_TYPE='ITM' and STATUS='L' and doc_name = '%s' order by SENT_DATE desc) "
				+ "WHERE rownum <= 1";

		String consultaL2 = "select * from (select pv_cr_plaza, pv_cr_tienda, status, source_id, partition_date"
				+ " from POSUSER.POS_OUTBOUND_DOCS " + "where DOC_TYPE='ITM' and STATUS='L'  order by SENT_DATE desc) "
				+ "WHERE rownum <= 1 ";
		/*
		 * paso 1 Obtener el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN
		 * para la plaza y tienda .
		 **********************/
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)"); 
		addStep("Obtener el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN para la plaza  y tienda .");
		System.out.println(plaza);
		SQLResult plazas = dbEbs.executeQuery(plaza);
		
		String retek = plazas.getData(0, "RETEK_CR");
		System.out.println("RETEK_CR " + retek);
		boolean paso1 = plazas.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(plazas);
		}
		assertFalse("No existe un valor de RETEK_CR para la plaza mencionada ", paso1);

		/*
		 * Paso 2 Validar que hay información pendiente de procesar en la tabla:
		 * POS_ITM_PRM_HEAD_COL. .
		 **********************/
		addStep("Validar que hay información pendiente de procesar en la tabla: POS_ITM_PRM_HEAD_COL.");
		
		String consulta_ITM = String.format(ITM, retek);
		
		System.out.println( consulta_ITM);
		
		SQLResult IT = dbRmsCol.executeQuery(consulta_ITM);
		
		String BATCH = IT.getData(0, "LOAD_BATCH_ID");
	
		System.out.println("LOAD_BATCH_ID " + BATCH);
		
		boolean paso2 = plazas.isEmpty();
		
		if (!paso2) {
			testCase.addQueryEvidenceCurrentStep(IT);
		}
		
		System.out.println(paso2);

		assertFalse("No existe información pendiente de procesar en RETEK.", paso2);

		/*
		 * paso 3 Validar información de ITEMS en la tabla: FEM_POS_MODS_STG_COL .
		 **********************/
		addStep("Validar información de ITEMS en la tabla: FEM_POS_MODS_STG_COL");
		
		String consulta_ITMIN = String.format(INFITM, BATCH,retek);
		System.out.println( consulta_ITMIN);
		SQLResult ITN = dbRmsCol.executeQuery(consulta_ITMIN);

		boolean paso3 = ITN.isEmpty();
		if (!paso3) {
			testCase.addQueryEvidenceCurrentStep(ITN);
		}

		System.out.println(paso3);
		
		String consulta_ITMIN2 = String.format(INFITM2, BATCH, retek);
		System.out.println("Respuesta " + consulta_ITMIN2);
		SQLResult ITN2 = dbRmsCol.executeQuery(consulta_ITMIN2);

		boolean paso33 = ITN2.isEmpty();
		if (!paso33) {
			testCase.addQueryEvidenceCurrentStep(ITN2);
		}
		System.out.println(paso33);
		
		assertFalse("Existe información de ITEMS en RETEK. ", paso33);

		/*
		 * paso 4 ejecutar la pr26 el servicio itm, prm
		 */
		addStep("Ejecutar el servicio PR26_CO.Pub:runComplete.");
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

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

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//Paso 5    ************************		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
        boolean validateStatus = fcwS.equals(status);
        
        if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(is1);
		}
	
		System.out.println("STATUS = S: "+ validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//  Paso 6  *********************************
		
		addStep("Validar que se actualizaron correctamente los campos wm_itm_status a E y wm_target_itm con el nombre del archivo enviado.");
		
	//Primera parte	
	String consulta_ITMPROC = String.format(IMTPROC, BATCH, retek);
		
		SQLResult ITMPROC2 = dbRmsCol.executeQuery(consulta_ITMPROC);
		
		System.out.println( consulta_ITMPROC);
		boolean paso6 = ITMPROC2.isEmpty();
		if (!paso6) {
			testCase.addQueryEvidenceCurrentStep(ITMPROC2);
		}
		
		System.out.println(paso6);
	
	//Segunda parte
		String consulta_ITMPROC2 = String.format(IMTPROC2,BATCH, retek);
		
		System.out.println( consulta_ITMPROC2);
		
		SQLResult ITMPROC22 = dbRmsCol.executeQuery(consulta_ITMPROC2);
		
		String wm_target_itm = ITMPROC22.getData(0, "WM_TARGET_ITM");
		
		System.out.println("WM_TARGET_ITM" + wm_target_itm );
		
		boolean paso62 = ITMPROC22.isEmpty();
		if (!paso62) {
			testCase.addQueryEvidenceCurrentStep(ITMPROC22);
			
		}
		
		System.out.println(paso62);
		
		assertFalse("No se actualizaron los datos correctamente ", paso62);
		
//Paso 7 **************************************************************************
		
		addStep(" Validar que este en L en POSUSER.POS_OUTBOUND_DOCS");
		
		//Primera parte
		
		String consultaDoc = String.format(consultaL, wm_target_itm );
		
		System.out.println(consultaDoc);
		
		SQLResult L = dbPOSUSER.executeQuery(consultaDoc);
		
                String doc_name = L.getData(0, "DOC_NAME");
		
		System.out.println("DOC_NAME: " + doc_name);

		boolean pasoL = L.isEmpty();
		if (!pasoL) {
			testCase.addQueryEvidenceCurrentStep(L);
		}
		System.out.println(pasoL);
		
		// Segunda parte

		System.out.println(consultaL2);
		
		SQLResult L2 = dbPOSUSER.executeQuery(consultaL2);

		boolean pasoL2 = L2.isEmpty();
		if (!pasoL2) {
			testCase.addQueryEvidenceCurrentStep(L2);
		}
		
		System.out.println(pasoL2);
		assertFalse("No se encontro el archivo xml en POSUSER ", pasoL2);

	
//Paso 8*************************************************************************

		addStep(" Validar que se realice el envío del archivo ITM generado por la interface en el directorio configurado para la tienda procesada ");
		
       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
       ///u01/posuser
       
                                 //host, puerto, usuario, contraseña
      String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + doc_name;
       boolean validaFTP;
       
        if (ftp.fileExists(ruta) ) {
        	
        	validaFTP = true;
            System.out.println("Existe ");
            testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
            testCase.addBoldTextEvidenceCurrentStep("Ruta: " + ruta);
            
        }else {
            System.out.println("No Existe");
            validaFTP = false;
        }
        
        assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
        
               
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
		return "Terminada. Validar el envío de información ITM ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_pr26_co_PR26ColRunITM";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
