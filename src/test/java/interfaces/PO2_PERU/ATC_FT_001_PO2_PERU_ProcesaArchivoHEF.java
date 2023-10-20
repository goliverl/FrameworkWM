package interfaces.PO2_PERU;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;


/**
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal 
 * de la interface FEMSA_PO2_PER para procesar archivos HEF (Hoja de Entrega Final)
 * de subida da (de WM INBOUND a EBS), al ser migrada la interface de WM9.9 a WM10.5
 * 
 * 
 * @reviewer Gilberto Martinez
 * @date 2023/15/02
 */

public class ATC_FT_001_PO2_PERU_ProcesaArchivoHEF extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO2_PERU_ProcesaArchivoHEF_test(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbEbs= new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,GlobalVariables.DB_PASSWORD_OIEBSBDQ); 
		utils.sql.SQLUtil dbLog= new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);

 /* Variables ******************************************************************************************************************************************/
		
	//Paso 3
		
		String validaRegistros = "SELECT DISTINCT a.id, a.pe_id,a.status,a.doc_type, h.pv_date   \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B, POSUSER.POS_HEF_DETL H \r\n"
				+ "WHERE A.DOC_TYPE = 'HEF' \r\n"
				+ "AND A.STATUS = 'I' \r\n"
				+ "AND H.PID_ID = A.ID \r\n"
				+ "AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA   \r\n"
				+ "AND H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV', 'EECI', 'ERCI', 'ENCI', 'TTRV')";
		
		//Paso 4
		
		String registrosPosHEF = "SELECT PI.ID, PI.STATUS, ph.no_records,ph.pv_date\r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS PI, POSUSER.POS_HEF PH \r\n"
				+ "WHERE PI.ID = PH.PID_ID \r\n"
				+ "AND PI.ID = '%s'"
				+ "AND PI.STATUS='I' \r\n"
				+ "AND PI.DOC_TYPE='HEF'";
		
		//Paso 5
		String registrosDETL = "SELECT pid_id, prefijo, pv_date, valor \r\n"
				+ "FROM POSUSER.POS_HEF_DETL \r\n"
				+ "WHERE PID_ID = '%s'";
		
		
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server  \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PO2_PER'  \r\n" + 
				"ORDER BY START_DT DESC) where rownum <=1";
		
	
		
		//consultas de error
		String consultaError = " Select ERROR_ID,ERROR_DATE,ERROR_TYPE, MESSAGE \r\n"
				+ "from  wmlog.WM_LOG_ERROR \r\n" 
				+ "where RUN_ID='%s' ";
		
	
		
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " 
		+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'"
				+ "ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID='%s' "
						+ "ORDER BY THREAD_ID DESC ";// Consulta para los Threads
		
		//Paso 12
		String RegistrosStatusE = "SELECT DISTINCT a.id, a.pe_id,a.status,a.doc_type, h.pv_date   \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B, POSUSER.POS_HEF_DETL H \r\n"
				+ "WHERE A.DOC_TYPE = 'HEF' \r\n"
				+ "AND A.STATUS= 'E' \r\n"
				+ "AND H.PID_ID = A.ID\r\n"
				+ "AND A.ID = '%s'\r\n"
				+ "AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA   \r\n"
				+ "AND H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV', 'EECI', 'ERCI', 'ENCI', 'TTRV')";
		
		
		
			//Paso 5
		String finanzas = "SELECT GROUP_ID,DATE_CREATED, SEGMENT1, SEGMENT2, SEGMENT3,SEGMENT6  \r\n"
				+ "FROM GL.GL_INTERFACE\r\n"
				+ "WHERE GROUP_ID  = '%s'\r\n"
				+ "ORDER BY DATE_CREATED DESC";
		
		testCase.setTest_Description(data.get("id") + data.get("descripcion"));
		testCase.setPrerequisites(data.get("prerequicitos"));
		
		
		
//Paso 1 *****************************************************************************************************************************************		
		
	//	@ MTC-FT-002 PR50_IN Enviar archivo HEF a traves de la DS50-TN-PR50 a WM
		
		
//Paso 2 *****************************************************************************************************************************************
		
addStep("Realizar conexión a la BD OIWMQA.FEMCOM.NET con usuario POSUSER.");
		
		
		
		testCase.addTextEvidenceCurrentStep("Conexion: POSUSER ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_Oiwmqa);
		
		boolean conexionPosuser = true;
		
		assertTrue(conexionPosuser , "La conexion no fue exitosa");
		
		
		
//Paso 3 ****************************************************************************************************************************************		
		
		addStep("Consultar que existan registros en las tablas POS_INBOUND_DOCS de la BD OIWMQA-POSUSER con DOC_TYPE igual a 'HEF' con STATUS igual a 'I' ");
		
		 System.out.println(validaRegistros);
	        
	       SQLResult validaRegistros_Result = dbPos.executeQuery(validaRegistros);
	       
	       String id  = "";
	       	       
	     
	       boolean validaRegistros_b = validaRegistros_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!validaRegistros_b) {
	    	   
	    	    id  = validaRegistros_Result.getData(0, "ID");
		       
		        System.out.println("ID = "+ id );
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(validaRegistros_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(validaRegistros_b); 

           
       assertFalse("No se encontraron registros a procesar ",validaRegistros_b ); //Si esta vacio, imprime mensaje
       
       
 //***************************************************************************************************************************************************************
       //Paso 4
       
       addStep("Consultar que existan registros en las tablas POSUSER.POS_HEF de la BD FCWM6QA con de los archivos HEF que están en la tabla POS_INBOUND_DOCS listos para procesar.   ");
		
       String registrosPosHEF_format = String.format(registrosPosHEF, id);
     
		 System.out.println(registrosPosHEF_format);
	        
	       SQLResult registrosPosHEF_Result = dbPos.executeQuery(registrosPosHEF_format);
	       
	      
	       boolean registrosPosHEF_b = registrosPosHEF_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!registrosPosHEF_b) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(registrosPosHEF_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(registrosPosHEF_b); 

       
   assertFalse("No existen registros en las tablas POSUSER.POS_HEF de la BD FCWM6QA con de los archivos HEF", registrosPosHEF_b ); //Si esta vacio, imprime mensaje
     
       
//******************************************************************************************************************************************       
       
   //Paso 5
       
       addStep("Consultar que existan registros en las tablas POSUSER.POS_HEF_DETL de la BD FCWM6QA con el detalle de la informacion contenida en los archivos.                   ");
		
       String registrosDETL_format = String.format(registrosDETL, id);
     
		 System.out.println(registrosDETL_format);
	        
	       SQLResult registrosDETL_Result = dbPos.executeQuery(registrosDETL_format);
	       
	      
	       boolean registrosDETL_b = registrosDETL_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!registrosDETL_b) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(registrosDETL_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(registrosDETL_b); 

       
   assertFalse("No existen registros con el detalle de la informacion contenida en los archivos", registrosDETL_b ); //Si esta vacio, imprime mensaje
     
     
    
     
//******************************************************************************************************************************************       
     
   //Paso 6
           
       addStep("Solicitar a los Usu FC Operadores SITE vía correo electrónico <UsuFEMCOMOperadoresSITE@oxxo.com>,la ejecución de la interfaz  PO2_PER, con la siguiente información:\r\n"
       		+ "JobName: PO2_PERU\r\n"
       		+ "Command Line: runInterfase.sh runPO2_PERU\r\n"
       		+ "Host 10.5: FCWMQA8B\r\n"
       		+ "Folder 10.5: TQA_BO_105");
       
      
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		JSONObject obj = new JSONObject(data.get("job"));

		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		u.hardWait(10);
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		if(resultado.equals("Wait Condition")) {
		casoPasado = true;
		}		
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
		control.closeViewpoint(); 
	      
//***************************************************************************************************************************************************************************************
	
		//Paso 8
		
     addStep("Realizar conexión a la BD OIWMQA.FEMCOM.NET con usuario POSUSER.");
		
				
		testCase.addTextEvidenceCurrentStep("Conexion: Log ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_Oiwmqa);
		
		boolean conexionLog = true;
		
		assertTrue(conexionLog , "La conexion no fue exitosa");
		
		
	
//*****************************************************************************************************************************************************************
		
		//Paso 9
		addStep("Verificar que la ejecucion termino con exito.");

		SQLResult ISF_Result = executeQuery(dbLog, tdcIntegrationServerFormat);
		
		System.out.println(  tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		System.out.println("STATUS: "+ STATUS);
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		System.out.println("RUN_ID: "+ RUN_ID);
		
		String statusEsperado = "S";
			
		boolean validateStatus = STATUS .equals(statusEsperado);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		} else {
			
			testCase.addBoldTextEvidenceCurrentStep("Se presento un error en la ejecucion.");
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//***************************************************************************************************************************************************************************************
			
		//Paso 10	
			
			addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
			
			String threads = String.format(consultaThreads, RUN_ID);
			
			System.out.println("CONSULTA THREAD "+ threads);
			
			 SQLResult threadsResult=  dbLog.executeQuery(threads);
	        
			
			boolean av31 = threadsResult.isEmpty();
			if (av31 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult);

			} else {
				
				testCase.addTextEvidenceCurrentStep("No se encontraron  threads");
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 1- "+ av31);
			
			// .-----------Segunda consulta
			
			String threads2 = String.format(consultaThreads2, RUN_ID);
			
			SQLResult threadsResult2=  dbLog.executeQuery(threads2);
			
			boolean av3111 = threadsResult2.isEmpty();
			if (av3111 == false) {

				testCase.addQueryEvidenceCurrentStep( threadsResult2);

			} else {
				
				testCase.addTextEvidenceCurrentStep("No se encontraron  threads");
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 2- "+ av31);
			
			assertFalse("No se generaron threads en la tabla", av3111);
			
			
//************************************************************************************************************************************************************************************			
		//Paso 11
			
			addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz PO2_PER en la tabla WM_LOG_ERROR  de BD OIWMQA-WMLOG. ");
			
           String error_format = String.format(consultaError, RUN_ID);
			
			System.out.println("Consulta error "+ error_format);
			
			 SQLResult error_Result=  dbLog.executeQuery(error_format);
	        
			
			boolean validaError = error_Result.isEmpty();
			
			if (validaError ) {

				testCase.addTextEvidenceCurrentStep("No se encontraron errores en la ejecucion");
				testCase.addQueryEvidenceCurrentStep(error_Result);

			} else {
				
				testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion");
				testCase.addQueryEvidenceCurrentStep(error_Result);
			}
			System.out.println( validaError );
			
			assertTrue(validaError, "Se presento un error en la ejecucion"   );
			
//************************************************************************************************************************************************************************************			
			
						
			//Paso 12
			
			addStep("Ejecutar el siguiente query para validar los archivos procesados y el ID  de la poliza");
			
			
           String RegistrosStatusE_format = String.format(RegistrosStatusE, id);
			
			System.out.println("Consulta status E: "+ RegistrosStatusE_format);
			
			SQLResult RegistrosStatusE_Result=  dbLog.executeQuery(RegistrosStatusE_format);
	        
			String target_id = "";
			
			boolean validaRegistrosStatusE = RegistrosStatusE_Result.isEmpty();
			
			if (!validaRegistrosStatusE) {

				 target_id  = validaRegistros_Result.getData(0, "TARGET_ID");
				 
				 System.out.println("TARGET_ID:  "+ target_id);
				 
				testCase.addQueryEvidenceCurrentStep(RegistrosStatusE_Result);

			} else {
				
				testCase.addTextEvidenceCurrentStep("No se encontraron registros en E");
				
			}
			System.out.println( validaError );
			
			assertTrue(validaRegistrosStatusE, "Se presento un error en la ejecucion"   );
			 
	       
     assertFalse("No se encontraron registros en E",validaRegistrosStatusE); //Si esta vacio, imprime mensaje
     
//**************************************************************************************************************************************************************************************
     
     //Paso 13
     
     addStep("Realizar conexión a la BD OIWMQA.FEMCOM.NET con usuario POSUSER.");
		
		
		testCase.addTextEvidenceCurrentStep("Conexion: EBS ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_OIEBSBDQ);
		
		boolean conexionEbs = true;
		
		assertTrue(conexionEbs , "La conexion no fue exitosa");
		
//**********************************************************************************************************************************************************		
 
	//Paso 14
	
		addStep("Comprobar que los registros se insertaron correctamente en la Tabla GL_INTERFACE en la BD AVEBQA EBS AVANTE donde GROUP_ID = POSUSER.POS_INBOUND_DOCS.TARJECT_ID:");
		
	       String finanzas_format = String.format(finanzas, target_id);
	     
			 System.out.println(finanzas_format);
		        
		     SQLResult finanzas_Result = dbEbs.executeQuery(finanzas_format);
		       
		      
		       boolean validaFinanzas = finanzas_Result.isEmpty(); //checa que el string contenga datos
		    		  
		       
		       if(!validaFinanzas) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(finanzas_Result);  //Si no esta vacio, lo agrega a la evidencia
		       }
		      
		       System.out.println(validaFinanzas); 

	       
	   assertFalse("No se insertaron los registros en la Tabla GL_INTERFACE", validaFinanzas); //Si esta vacio, imprime mensaje
	     
		
	
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO2_PERU_ProcesaArchivoHEF_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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



