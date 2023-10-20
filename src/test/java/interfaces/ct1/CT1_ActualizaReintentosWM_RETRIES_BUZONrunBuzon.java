package interfaces.ct1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class CT1_ActualizaReintentosWM_RETRIES_BUZONrunBuzon extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_CT1_ActualizaReintentosWM_RETRIES_BUZONrunBuzon(HashMap<String, String> data) throws Exception {
		
/* Utiler�as *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		/**
		 * ALM
		 * Verificar el procesamiento de la interfaz, actualizar los reintentos WM_RETRIES_BUZON++ (runBuzon)
		 */
		
	
/**
* Variables ******************************************************************************************
* 
*/
		//Paso 2 y 3
		
		String ValidaRetriesyStatus = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON , WM_RETRIES_BUZON " + 
				"  FROM WM_BUZONES_T_TIENDAS " + 
				" WHERE WM_STATUS_BUZON = 'L' "+ 
				"    AND CR_PLAZA = '" + data.get("plaza") +"' \r\n" + 
				"    AND CR_TIENDA = '" + data.get("tienda") +"'\r\n" + 
				"  ORDER BY CREATION_DATE";
		
		//UPDATE
		String UpdateRetriesBuzon = "UPDATE WM_BUZONES_T_TIENDAS "+
				" SET WM_RETRIES_BUZON  = 1 WHERE ID = '%s'";
		
		String comm = "commit";
		
			      
	   //Paso 5
	      
		//Usar este para pruebas ejecutando el servicio (runBuzon)	
//	String  tdcIntegrationServerFormat ="select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
//		      "FROM WMLOG.WM_LOG_RUN " +
//		      "WHERE INTERFACE = 'CT1_BUZON' " +
//		      "AND START_DT >= TRUNC(SYSDATE) "  +
//		     " ORDER BY START_DT DESC) where rownum <=1"; 
//	
//	String ValidaError = "select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
//			"FROM WMLOG.WM_LOG_RUN "+
//			"WHERE INTERFACE = 'CT1_BUZON' "+
//			"AND START_DT >= TRUNC(SYSDATE)  "+
//			"AND STATUS = 'E' "+
//			" ORDER BY START_DT DESC) where rownum <=1";
		
		//Usar esta para pruebas sin ejecutar servicio (RunBuzon) 
		String  tdcIntegrationServerFormat ="select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
			      "FROM WMLOG.WM_LOG_RUN " +
			      "WHERE INTERFACE = 'CT1_BUZON' " +
			     " ORDER BY START_DT DESC) where rownum <=1"; 
		
		String ValidaError = "select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
				"FROM WMLOG.WM_LOG_RUN "+
				"WHERE INTERFACE = 'CT1_BUZON' "+
				"AND STATUS = 'E' "+
				" ORDER BY START_DT DESC) where rownum <=1";
		      
	 //Paso 6
		      
		   
		      String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s'";
		      
				
				String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s'";
				
	//Paso 7
				//Usar este para pruebas ejecutando el servicio (runBuzon)			
//				String validaRetries = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON , WM_RETRIES_BUZON "+ 
//						 "FROM WM_BUZONES_T_TIENDAS "+
//						 "WHERE WM_RETRIES_BUZON = 2 "+
//						     "AND CR_PLAZA = '" + data.get("plaza") +"' "+
//						     "AND CR_TIENDA = '" + data.get("tienda") +"' "+
//						     "AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)";
				
				//Usar esta para pruebas sin ejecutar servicio (RunBuzon)
				String validaRetries = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON , WM_RETRIES_BUZON "+ 
						 "FROM WM_BUZONES_T_TIENDAS "+
						 "WHERE WM_RETRIES_BUZON = 2 "+
						     "AND CR_PLAZA = '" + data.get("plaza") +"' "+
						     "AND CR_TIENDA = '" + data.get("tienda")+"'";
		      
		
		      
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//**********************************************Paso 1	****************************************************************************
//				Verificar que el directorio existe en el filesystem
//				PATH=/u01/posuser/FEMSA_OXXO/POS/[PLAZA]/[TIENDA]/ 
//			    DIRECTORIO=backup,working,recovery,outbox,duplicate.
				
	addStep("Verificar que el directorio existe en el filesystem, DIRECTORIO=backup,working,recovery,outbox,duplicate.");
						
	boolean Res = true;			
	FTPClient ftpClient=null;
	(ftpClient = new FTPClient()).connect("10.182.92.13",21);
				
	ftpClient.login("posuser", "posuser");
				 
	String Ruta = "/u01/posuser/FEMSA_OXXO/POS/"+data.get("plaza")+"/" + data.get("tienda");
	
	//********************VALIDA ruta /u01/posuser/FEMSA_OXXO/POS/plaza/tinda******************************************************
	
	   // Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
							
		if(ftpClient.cwd(Ruta)==550){ 
			
					    System.out.println("La ruta "+Ruta+" no existe"); 
		testCase.addTextEvidenceCurrentStep("La ruta "+Ruta+" no existe");
		Res = true;
					   	 
		}else if(ftpClient.cwd(Ruta)==250){ 
			
					    System.out.println("La ruta "+Ruta+" existe"); 
		testCase.addTextEvidenceCurrentStep("La ruta "+Ruta+" existe");
		Res = false;
							    
		}
		assertFalse(Res, "La ruta "+Ruta+" existe");
		
//********************VALIDA CARPETA backup******************************************************
				
   // Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
						
	if(ftpClient.cwd(Ruta +"/backup")==550){ 
		
				    System.out.println("La carpeta backup no existe"); 
	testCase.addTextEvidenceCurrentStep("La carpeta backup no existe");
	Res = true;
				   	 
	}else if(ftpClient.cwd(Ruta+"/backup")==250){ 
		
				    System.out.println("La carpeta backup existe"); 
	testCase.addTextEvidenceCurrentStep("La carpeta backup existe:\n "
			+ "Ruta: "+ Ruta +"/backup");
	Res = false;
						    
	}
	assertFalse(Res, "La carpeta backup no existe");
	
//********************VALIDA CARPETA working******************************************************
	
		// Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
							
		if(ftpClient.cwd(Ruta+"/working")==550){ 
			
					    System.out.println("La carpeta working no existe"); 
		testCase.addTextEvidenceCurrentStep("La carpeta working no existe");
		Res = true;
					   	 
		}else if(ftpClient.cwd(Ruta+"/working")==250){ 
			
					    System.out.println("La carpeta working existe"); 
		testCase.addTextEvidenceCurrentStep("La carpeta working existe:\n "
				+ "Ruta: "+Ruta+"/working");
							    
		}
		assertFalse(Res, "La carpeta working no existe");
		
//********************VALIDA CARPETA recovery******************************************************
		
			// Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
								
			if(ftpClient.cwd(Ruta+"/recovery")==550){ 
				
						    System.out.println("La carpeta recovery no existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta recovery no existe");
			Res = true;
						   	 
			}else if(ftpClient.cwd(Ruta+"/recovery")==250){ 
				
						    System.out.println("La carpeta recovery existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta recovery existe:\n "
					+ "Ruta: "+Ruta +"/recovery");
			Res = false;				    
			}
			assertFalse(Res, "La carpeta recovery no existe");
			
			
//********************VALIDA CARPETA outbox******************************************************
			
			// Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
								
			if(ftpClient.cwd(Ruta +"/outbox")==550){ 
				
						    System.out.println("La carpeta outbox no existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta outbox no existe");
			Res = true;
						   	 
			}else if(ftpClient.cwd(Ruta+"/outbox")==250){ 
				
						    System.out.println("La carpeta outbox existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta outbox existe:\n "
					+ "Ruta: "+Ruta +"/outbox");
			Res = false;					    
			}
			assertFalse(Res, "La carpeta outbox no existe");
			
//********************VALIDA CARPETA duplicate******************************************************
			
			// Si retorna 250 la carpeta existe, si retorna 550 El folder no existe
								
			if(ftpClient.cwd(Ruta +"/duplicate")==550){ 
				
						    System.out.println("La carpeta duplicate no existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta duplicate no existe");
			Res = true;
						   	 
			}else if(ftpClient.cwd(Ruta+"/duplicate")==250){ 
				
						    System.out.println("La carpeta duplicate existe"); 
			testCase.addTextEvidenceCurrentStep("La carpeta duplicate existe:\n "
					+ "Ruta: "+Ruta+"/duplicate");
			Res = false;				    
			}
				
			ftpClient.logout();
			
			assertFalse(Res, "La carpeta duplicate no existe");
			
			
			
			
//********************************************Paso2 y 3*****************************************************************************
				
				
		addStep("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS = 'L' y  WM_RETRIES_BUZON = 1.");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidaRetriesyStatus);
		
		SQLResult ValidaSatusL_Res = executeQuery(dbCNT, ValidaRetriesyStatus);
		
		boolean validaStatusL = ValidaSatusL_Res.isEmpty();
		
			if (!validaStatusL) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaSatusL_Res);
			int Retries = Integer.parseInt(ValidaSatusL_Res.getData(0, "WM_RETRIES_BUZON"));
			

			System.out.println(Retries);
//		En caso de que el WM_RETRIES_BUZON sea diferente a 1 puede actualizarse el campo para la ejecuci�n del caso.
			//Se comenta hasta poder ejecutar el update desde aqui
//			if(Retries !=1) {
//				
//				String Id= ValidaSatusL_Res.getData(0, "ID");
//			    String Updateformat = String.format(UpdateRetriesBuzon, Id);
//					System.out.println(Updateformat);
//					System.out.println(comm);
//				
//				        SQLResult UpdateB = executeQuery(dbCNT,Updateformat);
//				 SQLResult commit = executeQuery(dbCNT,comm);
//				 
//				
//				 
//				SQLResult ValidaSatusL_Res2 = executeQuery(dbCNT, ValidaRetriesyStatus);
//		testCase.addTextEvidenceCurrentStep("El numero de intentos era diferente a 1, se realiza update \n"+Updateformat
//							                +"\nActualizacion: ");
//		testCase.addQueryEvidenceCurrentStep(ValidaSatusL_Res2);
//		
//					
//				}

			}
			
		System.out.println(validaStatusL);

		assertFalse(validaStatusL, "No se encontro registro de la plaza y tienda");
		

		
		
//**********************************************Paso 4	****************************************************************************** 
		
//		
//		addStep("Ejecutar el JOB runCT1Buzon desde Control M para invocar la interface por medio del servicio CT1.Pub:runBuzon ");
//
//		// Utileria
//
//		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
//		PakageManagment pok = new PakageManagment(u, testCase);
//		String status = "S";
//
//		String user = data.get("user");
//		String ps = PasswordUtil.decryptPassword(data.get("ps"));
//		String server = data.get("server");
//	
//		String searchedStatus = "R";
//
//		System.out.println(GlobalVariables.DB_HOST_LOG);
//		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
//		u.get(contra);
//		System.out.println(contra);
//   
//		String dateExecution = pok.runIntefaceWM(data.get("interface"), data.get("servicio"), null);
//		System.out.println("Respuesta dateExecution" + dateExecution);
//
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n
//
//		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
//																// encuentra en
//																// estatus R
//
//		while (valuesStatus) {
//
//			status1 = is.getData(0, "STATUS");
//			run_id = is.getData(0, "RUN_ID");
//			valuesStatus = status1.equals(searchedStatus);
//
//			u.hardWait(4);
//
//		}
//
//	
		
//*******************************************************Paso 5************************************************************************
		//Validar que la interface haya finalizado con error en la tabla WM_LOG_RUN.
	
		addStep("Validar que la interface haya finalizado con error en la tabla WM_LOG_RUN.");
			 
//	
System.out.println(ValidaError);
		
		SQLResult ValidaStatusE = executeQuery(dbLog, ValidaError);
		
		boolean validaStatus = ValidaStatusE.isEmpty();
		
			if (!validaStatus) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaStatusE);
			
						} 
		
		System.out.println(validaStatus);

		assertFalse(validaStatus, "El estatus de la ejecucion no es E");
		
		
		
//**************************************************Paso 6 **************************************************************************
		
		addStep("Validar que el thread haya finalizado con error en la tabla WM_LOG_THREAD.");
		

		String consulta1 = String.format(qry_threads1, run_id);
		
		System.out.println("CONSULTA THREAD " + consulta1);
		
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
	
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
		

//**************************************************Paso 7 ***************************************************************************		
		addStep("Validar que el WM_RETRIES_BUZON de la tabla WM_BUZONES_T_TIENDAS fue actualizado a WM_RETRIES_BUZON = 2;");
		 
		

		System.out.println(validaRetries);
		
		SQLResult validaRetriesR = executeQuery(dbCNT,validaRetries);
		
		String RetrieBuzon = validaRetriesR.getData(0, "WM_RETRIES_BUZON");
		System.out.println(RetrieBuzon);
		boolean valuesRetri = RetrieBuzon.equals("2");
		System.out.println(valuesRetri);
		
			if (valuesRetri) {
		
			testCase.addQueryEvidenceCurrentStep(validaRetriesR);
			
						} 
		
		System.out.println(valuesRetri);

		assertTrue(valuesRetri, "No se actualizo correctamente el registro");
		
		
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
		return "Automatiza el proceso de creaci�n de buzones para tiendas nuevas en webMethods";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_CT1_ActualizaReintentosWM_RETRIES_BUZONrunBuzon";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
