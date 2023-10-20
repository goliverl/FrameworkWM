package interfaces.pp01;



import java.util.HashMap;
import org.testng.annotations.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_PP01_001_ValidainfoPOS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PP01_001_ValidainfoPOS_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		/**
		 * CP:Validar que se envie al POS la informacion de la plaza 10APO
		 * Se encuentra en ALM
		 * Dominio: intergacion
		 * 
		 */
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1
		String statusL = "select unique_id, ms_servid, wm_sent_date, wm_status, wm_doc_name, cr_plaza, cr_tienda \r\n"+
				"from  XXPSO.XXPSO_SERVICE_LOG \r\n" + 
				"where cr_plaza = '" + data.get("plaza") +"' \r\n"+
			    "and wm_status = 'L'"; 
		
//Paso 2
		
		String xxpso_m_service = "SELECT UNIQUE_ID, MS_SERVID, MS_MCASERV, MS_DESC, MS_TIPO, MS_REF1, MS_REF2, MS_REF3, STATUS \r\n" + 
				"from XXPSO.XXPSO_M_SERVICE \r\n" + 
				"where unique_id = '%s' \r\n" + 
				"and status >= 1";
		
		
//Paso 3	   
		// consultas de error
				String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
						+ "from wmlog.WM_LOG_ERROR \r\n" 
						+ "where RUN_ID='%s') \r\n "
						+ "where rownum <=1"; // dbLog
				String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR \r\n"
						+ "where RUN_ID='%s') \r\n"
						+ "WHERE rownum <= 1"; // dbLog
				String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
						+ "from wmlog.WM_LOG_ERROR \r\n" 
						+ "where RUN_ID='%s') \r\n"
						+ "WHERE rownum <= 1"; // dbLog
			      
//Paso 4
				
		      String  tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
		      		"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PP01' \r\n"+
		      	    "AND STATUS = 'S' \r\n" +
		      		"ORDER BY START_DT DESC) \r\n"+
		            "WHERE ROWNUM <=1";
		      	  
		      
//Paso 5
		      String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n" +
		      		"FROM WMLOG.WM_LOG_THREAD \r\n" + 
					"WHERE PARENT_ID = '%s'";
				
				String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 \r\n" +
					"FROM WMLOG.WM_LOG_THREAD \r\n" + 
					"WHERE PARENT_ID = '%s'";
		      
//Paso 6
				String statusE = "select unique_id, ms_servid, wm_sent_date, wm_status, wm_doc_name, cr_plaza, cr_tienda \r\n"
						+ "from XXPSO.XXPSO_SERVICE_LOG \r\n" + 
				         " where cr_plaza = '" + data.get("plaza") +"' \r\n"
				        + "and wm_run_id = '%s' \r\n"
				        + "and wm_status = 'E'";
				
//Paso 7
				String xxpso_m_service2 = "SELECT UNIQUE_ID, MS_SERVID, MS_MCASERV, MS_DESC, MS_TIPO, MS_REF1, MS_REF2, MS_REF3, STATUS \r\n" + 
						"from XXPSO.XXPSO_M_SERVICE \r\n" + 
						"where unique_id = '%s' \r\n" + 
						"and status = 2";
				
//Paso 8
				String documentoPos ="select id, target_type, doc_name, sent_date, pv_cr_plaza, pv_cr_tienda , status FROM POSUSER.POS_OUTBOUND_DOCS \r\n" + 
						"WHERE doc_name = '%s'\r\n" + 
						"and DOC_TYPE = 'ISE' ";
				
	 
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//*****************************************Paso 1	***************************************************************** 
		addStep("Validar que exista información pendiente de procesar en la tabla xxpso_service_log en PORTAL.");
		
		System.out.println(statusL);
		
		SQLResult statusL_Res = executeQuery(dbCNT, statusL);
		
		String unique_id = statusL_Res.getData(0, "UNIQUE_ID");
		
		System.out.println("UNIQUE_ID = "+unique_id);	
		
		boolean validaStatusL = statusL_Res.isEmpty();
		
			if (!validaStatusL) {
		
			testCase.addQueryEvidenceCurrentStep(statusL_Res);
			
						} 
		
		System.out.println(validaStatusL);

		assertFalse(validaStatusL, "No existe información pendiente de procesar en PORTAL.");
		
//**************************************Paso 2	*********************************************************************** 
		
		addStep("Validar que exista información en la tabla xxpso_m_service en PORTAL.");
	
		String xxpso_m_serviceFormat = String.format(xxpso_m_service, unique_id);
		 
		System.out.println(xxpso_m_serviceFormat);
		
		SQLResult xxpso_m_service_Res = executeQuery(dbCNT, xxpso_m_serviceFormat);
		
		boolean validaxxpso_m_service = xxpso_m_service_Res.isEmpty();
		
			if (!validaxxpso_m_service) {
		
			testCase.addQueryEvidenceCurrentStep(xxpso_m_service_Res);
			
						} 
		
		System.out.println(validaxxpso_m_service);

		assertFalse(validaxxpso_m_service, "No existe información en PORTAL.");
		
//**********************************************Paso 3 *****************************************************************
		
		addStep("Ejecutar el servicio PP01.Pub:run");
		
System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));     
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println(successRun);
													// estatus S

		if (!successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

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

		
//*******************************************************Paso 4*******************************************************
		
		addStep("Validar que la interfaz se ejecutó correctamente.");
		
	
		System.out.println(tdcIntegrationServerFormat);
		
		SQLResult ejecucion = dbLog.executeQuery(tdcIntegrationServerFormat);
		
		boolean validaStatusS = status1.equals(status);
		
			if (validaStatusS) {
		
			testCase.addQueryEvidenceCurrentStep(ejecucion);
			
						} 
		
		System.out.println(validaStatusS);

		assertTrue( "La interfaz termina sin errores y se deposita el archivo XML en el buzón destino de la tienda.",validaStatusS);

		
//**************************************************Paso 5 *********************************************************
		
		addStep("Comprobar que existan threads de los documentos procesados en WM_LOG_THREAD en WMLOG ");

		String consulta1 = String.format(qry_threads1, run_id);
		
		System.out.println("CONSULTA THREAD " + consulta1);
		
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
		
		String thread_id = consultaThreads.getData(0, "THREAD_ID");
		
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		assertFalse("No se generaron threads en la tabla", threads1);
		
//****************************************Paso 6 ****************************************************************
		
		addStep("Validar que se actualizaron los campos WM_STATUS a E, WM_SENT_DATE a la fecha actual y WM_RUN_ID de la tabla xxpso_service_log en PORTAL.");
		
		String  statusEFormat= String.format(statusE, thread_id);
		
		System.out.println(statusEFormat);
		
		SQLResult statusEt_Res = executeQuery(dbCNT, statusEFormat);
		
       String wm_doc_name = statusEt_Res.getData(0, "WM_DOC_NAME");
		
		System.out.println("WM_DOC_NAME = "+ wm_doc_name);	
		
		boolean validaStatusE = statusEt_Res.isEmpty();
		
			if (!validaStatusE) {
		
			testCase.addQueryEvidenceCurrentStep(statusEt_Res);
			
						} 
		
		System.out.println(validaStatusE);

		assertFalse(validaStatusE, "No se actualizaron los campos correctamente en PORTAL.");
		
//********************************Paso 7 *****************************************************************
		
		addStep("Validar que se actualizó el campo: STATUS con el valor 2 en la tabla xxpso_m_service en PORTAL ");
		
		String xxpso_m_service2Format = String.format(xxpso_m_service2, unique_id);
		 
		System.out.println(xxpso_m_service2Format);
		
		SQLResult xxpso_m_service2_Res = executeQuery(dbCNT, xxpso_m_service2Format);
		
		boolean validaxxpso_m_service2 = xxpso_m_service2_Res.isEmpty();
		
			if (!validaxxpso_m_service2) {
		
			testCase.addQueryEvidenceCurrentStep(xxpso_m_service2_Res);
			
						} 
		
		System.out.println(validaxxpso_m_service2);

		assertFalse(validaxxpso_m_service2, "El campo STATUS se actualiza correctamente en PORTAL.");

		
//*************************************Paso 8 *******************************************************************************		
		
addStep("Validar que se depositó correctamente el archivo XML en el buzón destino de la tienda.");
		
		String documentoPosFormat = String.format(documentoPos, wm_doc_name);
		 
		System.out.println(documentoPosFormat);
		
		SQLResult documentoPosFormat_Res = executeQuery(dbPos, documentoPosFormat);
		
		boolean validadocumentoPos = documentoPosFormat_Res.isEmpty();
		
			if (!validadocumentoPos) {
		
			testCase.addQueryEvidenceCurrentStep(documentoPosFormat_Res);
			
						} 
		
		System.out.println(validadocumentoPos);

		assertFalse(validadocumentoPos, "No se encontro el archivo XML en el buzón destino de la tienda.");

		
		
		
		
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
		return "Terminado.Extraer la informacion de pagos electronicos desde el Portal de Integracion de Servicios, generar archivo XML y entregar a DS50";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
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
	
	
	


}
