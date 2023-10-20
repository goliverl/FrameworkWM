package interfaces.pr26_per;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_PR26_PER_GeneracionArchivoITMItemMaster extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR26_PER_GeneracionArchivoITMItemMaster_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * MTC-FT-010-2-C1 PR26_PER Generacion de archivo ITM de Item Master a traves de la interface FEMSA_PR26_PER 
		 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PR26_CL 
		 * para generar archivos ITM (Item Master) de bajada (de RMS a WM OUTBOUND) con la información actualizada 
		 * o nueva de articulos, al ser migrada la interface de WM9.9 a WM10.5
		 * @author luis jasso
		 * @date 2023/02/21
		 * @proyecto Actualizacion tecnologica webmethods
			 */
		
/* Utilerías *********************************************************************/		
		SQLUtil dbrms = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru, GlobalVariables.DB_USER_RMSWMUSERPeru, GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);
		SQLUtil dbOIW = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa,GlobalVariables.DB_USER_Oiwmqa, GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);


/**
* Variables ******************************************************************************************
* 
* 
*/
		
		String tdcQueryInfo = "	SELECT *\r\n"
				+ "		FROM wmuser.FEM_POS_MODS_STG a, wmuser.POS_ITM_PRM_HEAD b\r\n"
				+ "		WHERE a.LOAD_BATCH_ID = b.LOAD_BATCH_ID\r\n"
				+ "		AND a.LOAD_WEEK = b.LOAD_WEEK\r\n"
				+ "		AND b.LOCATION = 3\r\n"
				+ "		AND a.STORE = b.LOCATION\r\n"
				+ "		AND b.WM_ITM_STATUS = 'L'\r\n"
				+ "		AND a.TRAN_TYPE NOT IN (31, 32);";
	
		
		String tdcQueryIntegrationServer = "SELECT * FROM ( SELECT RUN_ID, START_DT, STATUS\n"
				+ "FROM WMLOG.wm_log_run\n"
				+ "WHERE interface = 'PR26_PER_ITM'\n"
				+ "AND START_DT >= TRUNC(SYSDATE)\n"
				+ "ORDER BY start_dt desc)\n"
				+ "WHERE ROWNUM = 1";
		
		String ValidThread = "SELECT THREAD_ID,PARENT_ID,NAME,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID = '%s'";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		String tdcQueryEnvio = "SELECT ID, DOC_NAME, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA"
				+ " FROM POS_OUTBOUND_DOCS"
				+ " WHERE doc_type = 'ITM' "
				+ " AND Trunc( sent_date ) = Trunc( SYSDATE )"
				+ " ORDER BY sent_date";
		
		String ValidUPD ="	SELECT LOCATION,BATCH_ID,WM_ITM_STATUS,WM_TARGET_ITM FROM wmuser.POS_ITM_PRM_HEAD "
			+ "WHERE LOCATION = '%s'"
			+ "AND BATCH_ID ='%s' "
			+ "AND WM_ITM_STATUS = 'E' ";	
		
		
		testCase.setProject_Name("AutomationQA");	
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************
		addStep("Acceder a la BD de RMS.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: BDPRMSQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_RMSWMUSERPeru);
								
//Paso 2	************************ 	
		addStep("Validar que exista información pendiente por procesar en la tabla WMUSER.POS_ITM_PRM_HEAD DE RETEK con WM_ITM_STATUS = 'L'.");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);
		System.out.println(tdcQueryInfo);
		String LOAD_BATCH_ID = "";
		String LOCATION = "";
		SQLResult infoResult = executeQuery(dbrms, tdcQueryInfo);
		
		boolean info = infoResult.isEmpty();
		
		if (!info) {
			LOAD_BATCH_ID=infoResult.getData(0, LOAD_BATCH_ID);
			LOCATION=infoResult.getData(0, LOCATION);
			System.out.println("se obtiene información de la consulta");
		}
		testCase.addQueryEvidenceCurrentStep(infoResult);
		System.out.println(info);
		
		assertFalse(info, "No se obtiene información de la consulta");
		
//Paso 3	************************ 	
		/*
		 * Solicitar el ordenamiento del Job runITM_PER de la interfaz PR26  enviando un correo electrónico a los operadores USU UsuFEMCOMOperadoresSITE@oxxo.com , en Control M para su ejecución.
			Job name: runITM_PER
			Comando: runInterfase.sh runITM_PER
			Host : FCWMQA8B
			Usuario: isuser
			Grupo: TQA_BO_105
		 */

		addStep("Solicitar el ordenamiento del Job runRO1_MEX, en Control M para su ejecución.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);

		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		// testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn();

		// Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);

		// Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(30);

		// Valor del output

		String Res2 = control.getNewStatus();

		System.out.println("Valor de output getNewStatus:" + Res2);

		String output = control.getOutput();
		System.out.println("Valor de output control:" + output);

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: " + output);
		// Validacion del caso
		Boolean casoPasado = false;
		if (Res2.equals("Ended OK")) {
			casoPasado = true;
		}

		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		// assertNotEquals("Failure",resultado);


				
//Paso 5	************************ 	
		addStep("Verificar que la interfaz se haya ejecutado sin problemas.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(tdcQueryIntegrationServer);
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1  = query.getData(0, "STATUS");;
		String run_id  = query.getData(0, "RUN_ID");;
		String searchedStatus = "S";
		
		boolean valuesStatus = status1.equals(searchedStatus);

	    if(!valuesStatus){
	   
	    	
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	  
	    }
	    testCase.addQueryEvidenceCurrentStep(query);
		assertTrue(info, "No se obtiene status S");
	    
//Paso 6 *******************************
		
			addStep(" Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG.");
			
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
			String ThreadFormat = String.format(ValidThread, run_id);
			System.out.println(ThreadFormat);
			SQLResult ResThreadFormat = executeQuery(dbLog, ThreadFormat);
			
	        boolean validateThreadFormat = ResThreadFormat.isEmpty();
	        
	        if (!validateThreadFormat) {
	        	testCase.addTextEvidenceCurrentStep("Se valida la correcta ejecución de los Threads lanzados por la interface");
			}
	        testCase.addQueryEvidenceCurrentStep(ResThreadFormat);
			System.out.println(validateThreadFormat);
			assertFalse(validateThreadFormat, "No se encontraron threads");

//paso 7 ****************************
	   
	    addStep("Verificar que no contenga errores de ejecución.");
	    System.out.println(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
	   String error = String.format(tdcQueryErrorId, run_id);
	   SQLResult paso2 = executeQuery(dbLog, error);
	   
	   boolean emptyError = paso2.isEmpty();
	   
	   if(!emptyError){  
	   	   
		   testCase.addTextEvidenceCurrentStep(
					"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");
	   
	   }
	   
	   testCase.addQueryEvidenceCurrentStep(paso2);
	   testCase.addTextEvidenceCurrentStep(" No se encontró un error en la ejecución de la interfaz, en la tabla WM_LOG_ERROR");
	   assertFalse(emptyError, "Se encontraron Errores de ejecucion");
	   
	 //paso 8 ****************************   
	   addStep(" Validar que se inserte el registro del documento ITM generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER de la BD FCWMQA_PERU:");
	   System.out.println(GlobalVariables.DB_HOST_Oiwmqa);
	  
	   System.out.println(tdcQueryEnvio);
		
		SQLResult envioResult = executeQuery(dbOIW, tdcQueryEnvio);
		
		boolean envio = envioResult.isEmpty();
		
		if (!envio) {
			
			testCase.addQueryEvidenceCurrentStep(envioResult);
		}
		
		System.out.println(envio);
		
		assertFalse(envio, "No se obtiene información de la consulta");
		
		/*
		 * NO SE PUEDE REALIZAR PASO DE BUSQUEDA DE ARCHIVO EN SERVER FTP QUEDA FUERA DE ALCANCE
		 */
		

		
		 //paso 9 ****************************   
		   addStep("Validar que se actualice el estatus y el nombre del archivo generado por la interface de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK.");
		   System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);
		   String ValidUPDFormat = String.format(ValidUPD,LOCATION, LOAD_BATCH_ID);
			System.out.println(ValidUPDFormat);
			
			SQLResult ValidUPDFormatResult = executeQuery(dbrms, ValidUPDFormat);
			
			boolean infoValidUPDFormat = ValidUPDFormatResult.isEmpty();
			
			if (!infoValidUPDFormat) {
				
				System.out.println("se obtiene información de la consulta");
			}
			testCase.addQueryEvidenceCurrentStep(ValidUPDFormatResult);
			System.out.println(infoValidUPDFormat);
			
			assertFalse(infoValidUPDFormat, "No se obtiene información de la consulta");
		
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
		return "MTC-FT-010-2 PR26_PER Generación de archivo ITM de Item Master a través de la interface FEMSA_PR26_PER";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR26_PER_GeneracionArchivoITMItemMaster_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
