package interfaces.pr26;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class ATC_FT_002_PR26WMx86_GeneracionArchivoPRM extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR26WMx86_GeneracionArchivoPRM_test(HashMap<String, String> data) throws Exception {
		
		/*Back Office Mexico: MTC-FT-013 PR26 Generación de archivo PRM de catalogo de promociones a través de la interface FEMSA_PR26
 * Desc:
 * Prueba de regresión  para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PR26 para generar archivos 
 * PRM (Catalogo de Promociones) de bajada ( de RMS a WM OUTBOUND) con la información actualizada o nueva de promociones, al ser migrada la interface de WM9.9 a WM10.5
 * @author Marisol Rodriguez
 * @date   2022/07/16*/
		
		/*
		 * Utilerías
		 *********************************************************************/
	    utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA , GlobalVariables.DB_USER_AVEBQA ,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		//Paso 1 (pediente, en caso de agregarse extraer el ORACLE_CR_SUPERIOR , AND ORACLE_CR  y RETEK_CR
		String plaza = "SELECT ORACLE_CR,ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE FROM XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'\r\n" 
				+ " AND ORACLE_CR = '" + data.get("tienda")+ "'\r\n" 
				+ " AND ORACLE_CR_TYPE = 'T'";

		//paso de prueba 1
	/*	String plaza ="SELECT ORACLE_CR,ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE \r\n"
				+ "FROM XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE ORACLE_CR_TYPE = 'T'\r\n"
				+ "and estado = 'A'";*/
		
		
		//Paso 2
		String infoStatusL1 = "SELECT  location, archivo,load_date, load_batch_id \r\n"
				+ "FROM wmuser.POS_PRM_HEAD b, wmuser.PROMHEAD c \r\n"
				+ "WHERE  b.LOCATION = '%s'\r\n"
				+ "AND c.PROMOTION = '" + data.get("promotion") + "'\r\n"
				+ "AND b.WM_PRM_STATUS= 'L'"; //extraer location y promotion
		
		String infoStatusL2 ="SELECT batch_id,wm_prm_status, wm_target_prm, promotion,status, create_id\r\n"
				+ "FROM wmuser.POS_PRM_HEAD b, wmuser.PROMHEAD c \r\n"
				+ "WHERE  b.LOCATION = '%s'\r\n"
				+ "AND c.PROMOTION = '" + data.get("promotion") + "'\r\n"
				+ "AND b.WM_PRM_STATUS= 'L'" ;
		
		//Paso  3 , 4 y 5
		String tdcIntegrationServerFormat = "select * from run_id,interface, start_dt, end_dt, status, server \r\n "
				+ "FROM WMLOG.WM_LOG_RUN  \r\n" + 
				  "WHERE INTERFACE = 'PR26_PRM' \r\n"
			    + "AND rownum <=1 \r\n"
				+ "ORDER BY START_DT DESC ";// Consulta para estatus de la ejecucion
		
		// consultas de error
		String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
			 "from wmlog.WM_LOG_ERROR \r\n" + 
			 "where RUN_ID='%s' \r\n" +
			 "and rownum <=1"; // dbLog
		
		String consultaError2 = " select description,MESSAGE \r\n" +
			 "from wmlog.WM_LOG_ERROR \r\n" +
			 "where RUN_ID='%s' \r\n" +
			 "and rownum <= 1"; // dbLog
		
		
		
		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  \r\n"
				+ "FROM wmlog.WM_LOG_THREAD \r\n" + 
				" where PARENT_ID= '%s' \r\n"
				+ "AND ATT2 = '%s'"; 


		String consultaTHREAD2 = "SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD \r\n"
				+ "where PARENT_ID='%s' \r\n"
				+ "AND ATT2 = '%s'";


		String consultaStatusE1 = " SELECT location, archivo, load_date  \r\n"
				+ "FROM  wmuser.POS_PRM_HEAD\r\n"
				+ "WHERE LOCATION = '%s'\r\n"
				+ "AND BATCH_ID = '%s'\r\n"
				+ "AND WM_PRM_STATUS = 'E'\r\n"
				+ "AND WM_TARGET_PRM ='%s' ";
		
		String consultaStatusE2 = "SELECT batch_id, load_week, load_batch_id, wm_prm_status, wm_target_prm  \r\n"
				+ "FROM  wmuser.POS_PRM_HEAD\r\n"
				+ "WHERE LOCATION = '%s'\r\n"
				+ "AND BATCH_ID = '%s'\r\n"
				+ "AND WM_PRM_STATUS = 'E'\r\n"
				+ "AND WM_TARGET_PRM ='%s'";


       //Consultas status E
		String archivoPRM1 = "SELECT id, doc_name ,doc_type, sent_date  \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'PRS'\r\n"
				+ "AND PV_CR_PLAZA = '" + data.get("plaza") + "'\r\n"
				+ "AND PV_CR_TIENDA = '" + data.get("tienda") + "'\r\n"
				+ "AND STATUS = 'L'\r\n"
				+ "AND PARTITION_DATE >= trunc(SYSDATE)\r\n"
				+ "AND ROWNUM = 1\r\n"
				+ "ORDER BY SENT_DATE DESC ";

		String archivoPRM2 = "SELECT pv_cr_plaza, pv_cr_tienda, status, source_id, partition_date \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'PRS'\r\n"
				+ "AND PV_CR_PLAZA = '" + data.get("plaza") + "'\r\n"
				+ "AND PV_CR_TIENDA = '" + data.get("tienda") + "'\r\n"
				+ "AND STATUS = 'L'\r\n"
				+ "AND PARTITION_DATE >= trunc(SYSDATE)\r\n"
				+ "ORDER BY SENT_DATE DESC ";
			
		
		
		
		 testCase.setProject_Name("POC WMX86");
			
		 testCase.setPrerequisites(data.get("prerequicitos"));

		
//***************************Paso 1*****************************************************************************
		 addStep("Establecer la conexión a la BD **FCRMSQA**.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_MEX);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_RMS_MEX);
		 
//************************Paso 2 *******************************************************************************		 
		 
		addStep("Obtener el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN para la plaza  y tienda .");
		
		
		System.out.println(plaza);
	
		SQLResult plazas = dbEbs.executeQuery(plaza);
		String retek_cr = plazas.getData(0, "RETEK_CR");
		System.out.println("RETEK_CR: " + retek_cr);
		boolean paso11 = plazas.isEmpty();
		if (!paso11) {
			testCase.addQueryEvidenceCurrentStep(plazas);
		}
		assertFalse("No se muestra la información de la tienda.", paso11);

//*************************Paso 2 *********************************************************************************
		
		addStep("Validar que exista información pendiente por procesar en la tabla de FCRMSQA con WM_PRM_STATUS = 'L'.");
		
	   String infoStatusL1_Format = String.format(infoStatusL1 , retek_cr);
		
		//Primera parte
		System.out.println(infoStatusL1_Format );
		
		SQLResult infoStatusLRes1 = dbRms.executeQuery(infoStatusL1_Format );
		
		String BATCH_ID = "";
		String LOCATION = "";
	
		boolean  validainfoStatusLRes1 = infoStatusLRes1.isEmpty();
		
		if (!validainfoStatusLRes1) {
	
			//Se agrega a la evidencia
			testCase.addQueryEvidenceCurrentStep(infoStatusLRes1);
			
		}
		
		System.out.println(validainfoStatusLRes1);
		
		//Segunda parte
        	
       String infoStatusL2_Format = String.format(infoStatusL2, retek_cr);
		
		System.out.println(infoStatusL2_Format );
		
		SQLResult infoStatusLRes2 = dbRms.executeQuery(infoStatusL2_Format );
		
        boolean  validainfoStatusLRes2 = infoStatusLRes2.isEmpty();
		
		if (!validainfoStatusLRes2) {

			//Extraemos el batch_id
			 BATCH_ID = infoStatusLRes2.getData(0, "BATCH_ID");
			System.out.println("BARCH_ID " + BATCH_ID);
					
			//Se agrega a la evidencia
			testCase.addQueryEvidenceCurrentStep(infoStatusLRes2);
		}
		
		System.out.println(validainfoStatusLRes2);
		
		assertFalse("No se  muestra la información pendiente por procesar para la tienda ",validainfoStatusLRes2);

//***********************Paso 3 y 4 *********************************************************************************
		
		addStep("Validar que la ejecución del job  runITM en  Control M haya sido exitosa. ");
	
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		u.get(data.get("server"));
		
		JSONObject obj = new JSONObject(data.get("job"));

		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		addStep("Login");
		
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertFalse(!flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		testCase.addTextEvidenceCurrentStep("Output: " + control.getOutput());
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		
		if(resultado.equals("Failure")) {
			
		casoPasado = false;
		
		System.out.println("La ejecucion paso correctamente");
		
		}		
		assertTrue(casoPasado, "La ejecucion fallo");

		control.closeViewpoint(); 

//*****************************Paso 5 *******************************************************************
		addStep("Establecer la conexión a la BD **FCRMSQA**.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
		 
		

//********************************Paso 6 ****************************************************************
		
		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de la DB  FCWML6QA.  ");
		
		SQLResult ISF_Result = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
		

//********************************Paso 7 **************************************************************

		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla");

		//Parte 1
		String consultaTreads1 = String.format(consultaTHREAD, RUN_ID,retek_cr );
		
		System.out.println(consultaTreads1);
		
		SQLResult consultaTreads1_R = dbLOG.executeQuery(consultaTreads1);

		boolean validaThreads1 = consultaTreads1_R.isEmpty();
		
		if (!validaThreads1) {
			
			testCase.addQueryEvidenceCurrentStep(consultaTreads1_R);
		}
	
		System.out.println(validaThreads1);
		
		// Parte 2

		String consultaTreads2 = String.format(consultaTHREAD2, RUN_ID, retek_cr);
		
		System.out.println(consultaTreads2);
		
		SQLResult consultaTreads2_R = dbLOG.executeQuery(consultaTreads2);

		boolean validaThreads2 = consultaTreads2_R.isEmpty();
		
		if (!validaThreads2) {
			
			testCase.addQueryEvidenceCurrentStep(consultaTreads2_R);
		}

		System.out.println(validaThreads2);
		
	    assertFalse("No se generaron threads ",validaThreads2);
		
//***********************Paso 8 *********************************************************************
	    
	    addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR ");

		//Parte 1
		String consultaError1_f = String.format(consultaError1, RUN_ID);
		
		System.out.println(consultaTreads1);
		
		SQLResult consultaError1_R = dbLOG.executeQuery(consultaError1_f);

		boolean validaConsultaError1_f = consultaError1_R.isEmpty();
		
		if (validaConsultaError1_f) {
			
			testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");
			testCase.addQueryEvidenceCurrentStep(consultaError1_R);
			
		} else {
			
			testCase.addQueryEvidenceCurrentStep(consultaError1_R);
			
		}
	
		System.out.println(validaConsultaError1_f);
		
		
		//Parte 2
		String consultaError2_f = String.format(consultaError2, RUN_ID);
				
		System.out.println(consultaTreads2);
				
		SQLResult consultaError2_R = dbLOG.executeQuery(consultaError2_f);

		boolean validaConsultaError2_f = consultaError2_R.isEmpty();
				
		if (validaConsultaError2_f) {
			
			testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");		
			testCase.addQueryEvidenceCurrentStep(consultaError2_R);
		} else {
			
			testCase.addQueryEvidenceCurrentStep(consultaError2_R);
		}
			
		System.out.println(validaConsultaError2_f);
		assertTrue("Se genero un error en la ejecucion. ",validaConsultaError2_f);
		
//***********************Paso 9  *******************************************************************
		
		addStep("Establecer la conexión a la BD **FCRMSQA**.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
		 
		
		
//***********************Paso 10  ******************************************************************
		
		addStep("Validar que se inserte el registro del archivo PRM generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		//Primera parte
		String DOC_NAME = ""; //extraer doc_name para wm_target_itm
		System.out.println(archivoPRM1);
		
		SQLResult archivoPRM1_R = dbRms.executeQuery(archivoPRM1);
		
		boolean ValidaArchivoPRM1 = archivoPRM1_R.isEmpty();
		
		System.out.println(ValidaArchivoPRM1);

		if(!ValidaArchivoPRM1) {
			
			DOC_NAME =  archivoPRM1_R.getData(0, "DOC_NAME ");
			
			System.out.println("DOC_NAME: " + DOC_NAME );
			
			testCase.addQueryEvidenceCurrentStep( archivoPRM1_R);
			
			//Segunda parte
		
		System.out.println(archivoPRM2);
			
		SQLResult archivoPRM2_R = dbRms.executeQuery(archivoPRM2);
			
		boolean ValidaArchivoPRM2 = archivoPRM2_R.isEmpty();
			
		System.out.println(ValidaArchivoPRM2);

		if(!ValidaArchivoPRM2) {
			
		  
				testCase.addQueryEvidenceCurrentStep(archivoPRM2_R);			
			
		}
		
		assertFalse("No se insterto el archivo PRM ",ValidaArchivoPRM2);
		
//***************************************Paso 8*************************************************************************

	       Thread.sleep(20000);
	
			addStep(" Validar que se realice el envío del archivo ITM generado por la interface en el directorio configurado para la tienda procesada.");
			
	       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
	       
	       Thread.sleep(20000);
	       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + DOC_NAME;
	                                 //host, puerto, usuario, contraseña
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
	        

		
			
//********************** Paso 7 *******************************************************************
		
		addStep("Validar que se actualice el estatus la tabla POS_ITM_PRM_HEAD de DB  FCRMSQA para el registro procesado por la PR26.");
		
		//Parte 1

        String consultaStatusE_f1 = String.format(consultaStatusE1, LOCATION, BATCH_ID, DOC_NAME);
		
		System.out.println(consultaStatusE_f1 );
		
		SQLResult consultaStatusE_Res1 = dbPOSUSER.executeQuery(consultaStatusE_f1 );
		
   
		boolean validaConsultaStatusE1 = consultaStatusE_Res1.isEmpty();
		
		System.out.println(validaConsultaStatusE1 );
		
		if (!validaConsultaStatusE1 ) {
			
			testCase.addQueryEvidenceCurrentStep(consultaStatusE_Res1);
		}
		
		//Parte 2

        String consultaStatusE_f2 = String.format(consultaStatusE2, LOCATION, BATCH_ID, DOC_NAME);
		
		System.out.println(consultaStatusE_f2 );
		
		SQLResult consultaStatusE_Res2 = dbPOSUSER.executeQuery(consultaStatusE_f2 );
		
   
		boolean validaConsultaStatusE2 = consultaStatusE_Res1.isEmpty();
		
		System.out.println(validaConsultaStatusE2);
		
		if (!validaConsultaStatusE2) {
			
			testCase.addQueryEvidenceCurrentStep(consultaStatusE_Res2);
		}
		
		assertFalse("No se inserta el detalle del documento generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER.",validaConsultaStatusE2);
		
		}
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_002_PR26WMx86_GeneracionArchivoPRM_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "ATC-FT-013 PR26 Generación de archivo PRM de catalogo de promociones a través de la interface FEMSA_PR26";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Sergio Robles Ramos";
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