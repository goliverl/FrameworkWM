package interfaces.ro8col;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_023_RO08_COL_MADM2_CambioStatusEjecucion2 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_023_RO08_COL_MADM2_CambioStatusEjecucion2_test(HashMap<String, String> data) throws Exception {
		
		/**
		 *  Proyecto: Mejoras Administrativo CC3
		   Caso de prueba: ATC-FT-023-Cambio de status para ejecucion de PR5
		 * Desc:
		 * Validar scripts que actualicen los registros del estatus de ‘D’ a ‘I’ por tienda, 
		   para que las interfaces lleven la información del archivo RTV a RMS.
		 * @author Marisol Rodriguez
		 * @date   2022/09/26
		 */

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
	    
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

	    //Datos del proyecto
	    
	    testCase.setProject_Name("Mejoras Administrativo CC3");
		
		testCase.setPrerequisites(data.get("prerequicitos"));
		
		testCase.setTest_Description(data.get("id")+ data.get("name"));
		
		

	//Segundo paso    
	    String statusInicial = "SELECT ID,DOC_TYPE, STATUS,INSERTED_DATE, RECEIVED_DATE  \r\n"
	    		+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
	    		+ "WHERE DOC_TYPE  = '"+ data.get("DOC_TYPE")+"' \r\n"
	    		+ "AND SUBSTR(PV_DOC_NAME,4,5) ='" + data.get("plaza") +"' \r\n"
	    		+ "and status ='"+ data.get("statusInicial")+"' \r\n"
	    		+ "AND RECEIVED_DATE>=SYSDATE -5 \r\n"
	    		+ "order by RECEIVED_DATE desc";
	    
     //Integration server  
	    String  tdcIntegrationServerFormat = "select run_id, interface, start_dt, end_dt, status, server  \r\n" + 
	      		"FROM WMLOG.WM_LOG_RUN \r\n" +
	      		"WHERE INTERFACE = '"+ data.get("interfaceConsulta")+"' \r\n"+
	      	    "AND ROWNUM <=1 \r\n" +
	      	    "AND  start_dt >= TRUNC(SYSDATE) \r\n" +
	      		"ORDER BY START_DT DESC ";
	    
	  //error
		String error ="Select ERROR_ID, RUN_ID, ERROR_DATE,DESCRIPTION\r\n"
		  				+ "from  wmlog.WM_LOG_ERROR \r\n"
		  				+ "where RUN_ID= '%s'";
	  //threads
		String threads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n"
		  				+ "FROM WMLOG.WM_LOG_THREAD   \r\n"
		  				+ "WHERE PARENT_ID='%s'\r\n"
		  				+ "ORDER BY THREAD_ID DESC";
		  		
	    
	    
	 //Tercer paso
	    
	    String statusFinal = "SELECT ID,DOC_TYPE, STATUS,INSERTED_DATE, RECEIVED_DATE  \r\n"
	    		+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
	    		+ "WHERE DOC_TYPE  = '"+ data.get("DOC_TYPE")+"' \r\n"
	    		+ "AND SUBSTR(PV_DOC_NAME,4,5) = '"+ data.get("plaza") +"' \r\n"
	    		+ "and status ='"+ data.get("statusFinal")+"'\r\n"
	    		+ "AND ID = '%s'\r\n"
	    		+ "AND RECEIVED_DATE> = SYSDATE -5 \r\n"
	    		+ "order by RECEIVED_DATE desc";
	    
	    
	            
	    
	    
//Paso 1 *********************************************************************************************************
	    
	    addStep("Conectarse a la base de datos de WM");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
		 	    

//Paso 2 ***********************************************************************************************************	    
	    addStep("Validar que los archivos que llegaron a Webmethod  en estatus D, ejecutando la siguiente consulta en FCWMQA:");	

		System.out.println(statusInicial);
		
		SQLResult statusInicial_result = executeQuery( dbPos,statusInicial);
		
		String ID = "";
		
		testCase.addQueryEvidenceCurrentStep(statusInicial_result);
		
		boolean validastatusInicial = statusInicial_result.isEmpty();
		
		System.out.println("Status inicial: " +validastatusInicial);

			if (!validastatusInicial) {
		
				ID = statusInicial_result.getData(0,"ID");
				System.out.println(ID);
				
				
		
			}


//	assertFalse(validastatusInicial, "No se otubtuvieron registros");	
		
		
//Paso 2 ************************************************************************************************************
	
	addStep("Se deberá ejecutar el servicio Pub:run de la interface "+ data.get("nombreInterface")+" correspondiente al tipo de archivo a procesar: ");
	
	System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

			
			SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
			PakageManagment pok = new PakageManagment(u, testCase);
			String status = "S";
			String statusError= "E";
			String user = data.get("user");
			String ps = PasswordUtil.decryptPassword(data.get("ps"));
			String server = data.get("server");
		
			String searchedStatus = "R";

			
			String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
			u.get(contra);

			String dateExecution = pok.runIntefaceWmOneButton(data.get("interface"), data.get("servicio"));     
			System.out.println("Respuesta dateExecution" + dateExecution);
			
			System.out.println(tdcIntegrationServerFormat);

			SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
			String run_id = is.getData(0, "RUN_ID");
			
			System.out.println("RUN_ID = "+ run_id);
			
			String status1 = is.getData(0, "STATUS");
			System.out.println("STATUS = "+ status1);

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
			
			testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+ status1);
			testCase.addQueryEvidenceCurrentStep(is);			
			System.out.println("Status ejecucion "+ successRun);
			
			//Mostrar detalles de los threads
			String threadsFormat = String.format(threads, run_id);
			System.out.println(threadsFormat);
	        SQLResult threads_result= dbLog.executeQuery(threadsFormat);
	        testCase.addQueryEvidenceCurrentStep(threads_result);
	        Boolean validaThreads = threads_result.isEmpty();
	        System.out.println("La consulta de threads esta vacia : "+ validaThreads);
			
			//Revisar si hay errores, mostrar el detalle
	        Boolean validaError = status1.equals(statusError);
	       
			if(validaError) {
				
				String errorFormat = String.format(error, run_id);
				System.out.println(errorFormat);
		        SQLResult error_result= dbLog.executeQuery(errorFormat);
		        testCase.addQueryEvidenceCurrentStep(error_result);
				
			}
			 System.out.println("Se encontro algun error : "+ validaError);
			
			assertTrue(successRun, "Se presento un error en la ejecucion");	
														// estatus S
//Paso 3 ************************************************************************************************************
			
			addStep("Validar que los archivos  cambiaron a status "+ data.get("statusFinal")+", ejecutando la siguiente consulta en POSUSER:");	
			
			String statusFinalFormat = String.format(statusFinal,ID);

			System.out.println(statusFinalFormat);
			
			SQLResult statusFinal_result = executeQuery( dbPos,statusFinalFormat);
			
			boolean validastatusFinal = statusFinal_result.isEmpty();
			
			System.out.println("Status final: "+ validastatusFinal);

				if (!validastatusFinal) {
			
					
					testCase.addQueryEvidenceCurrentStep(statusFinal_result);
			
				}


		assertFalse(validastatusFinal, "No se actualizo el registro");	
		

		 
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_023_RO08_COL_MADM2_CambioStatusEjecucion2_test";
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
