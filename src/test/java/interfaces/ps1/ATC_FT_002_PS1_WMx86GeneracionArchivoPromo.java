package interfaces.ps1;

import static org.junit.Assert.assertFalse;
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
import utils.sql.SQLUtil;

public class ATC_FT_002_PS1_WMx86GeneracionArchivoPromo extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PS1_WMx86GeneracionArchivoPromo_test(HashMap<String, String> data) throws Exception {
		
		/*Back Office Mexico: ATC-FT-018 PS1 Generacion de archivo PRS de promociones Sinergia a trav�s de la interface FEMSA_PS1
 * Desc:
 * Prueba de regresi�n  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PS1 para generar archivos 
 * PRS (Catalogo de Promociones Sinergia) de bajada (de portal Sinergia a WM OUTBOUND), al ser migrada la interface de WM9.9 a WM10.5
 * @author Marisol Rodriguez
 * @date   2022/07/16*/

		/*
		 * Utilerias
		 ***********************************************************************************************/
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA,GlobalVariables.DB_PASSWORD_FCIASQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		
		String infoProcesar = "SELECT cr_plaza, emission_id, emission_start_date, emission_end_date, promotion_id \r\n "
				+ "FROM WMUSER.XXSE_POS_SUMMARY_V \r\n"
				+ "WHERE CR_PLAZA='" + data.get("plaza") + "'";

		// consultas de error
		String consultaError1 = "select error_id, run_id, error_date, severity, error_type  \r\n"
				+ "from wmlog.WM_LOG_ERROR\r\n"
				+ "where RUN_ID='%s'"; // dbLog
		
		String consultaError2 = " select description  \r\n"
				+ "from wmlog.WM_LOG_ERROR\r\n"
				+ "where RUN_ID='%s'"; // dbLog
		
		String tdcIntegrationServerFormat = "SELECT run_id, interface, start_dt, end_dt, status, server \r\n"
				+ "FROM wmlog.wm_log_run \r\n" + 
				"WHERE interface = 'PS1' \r\n" + 
				"AND start_dt>=TRUNC(SYSDATE) \r\n" +
			    "AND  ROWNUM <= 1 \r\n" +
				//"AND STATUS = 'S' \r\n"  +
				"ORDER BY start_dt DESC";
		
		String registroArchivoPRS = "SELECT * FROM POSUSER.POS_OUTBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE = 'PRS' \r\n"
				+ "AND PV_CR_PLAZA = '" + data.get("plaza") + "' \r\n"
				+ "AND PV_CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ "AND STATUS = 'L'";
		
		 testCase.setProject_Name("POC WMX86");
		
		 testCase.setPrerequisites(data.get("prerequicitos"));

		
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */
//************************************Paso 1********************************************************
		 addStep("Establecer la conexi�n a la FCIASQA");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCIASQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCIASQA);

//************************************Paso 2*******************************++************************

			addStep("Validar que exista informaci�n para procesar en la vista: XXSE_POS_SUMMARY_V de SINERGIA DB FCIASQA esquema WMUSER");
			System.out.println("Paso 2");
			System.out.println(infoProcesar);

			SQLResult infoProcesarRes = executeQuery(dbCNT, infoProcesar);

			boolean validainfoProcesar = infoProcesarRes.isEmpty();

			if (!validainfoProcesar) {

				testCase.addQueryEvidenceCurrentStep(infoProcesarRes);

			}

			System.out.println(validainfoProcesar);

			assertFalse(validainfoProcesar, "No Hay informaci�n para la plaza " + data.get("plaza") + " en SINERGIA ");

		 
//**********************Paso 3 ****************************************************************************

		addStep(" Ingresar a la Ruta: <Packages-Management-FEMSA_PS1-Browse services in FEMSA_PS1> para buscar el servicio (PS1.pub:run) que ejecuta la interfaz PS1."
				+ "Ejecutar el Servicio PS1.pub:run de la Interfaz PS1");
		System.out.println("Paso 3");
		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
	

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		System.out.println(server);

		

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExcecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExcecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la ejecuci�n
		
		
		String searchedStatus = "R";
		String status = "S";
		
		
		System.out.println("Run_id: "+ run_id);
		System.out.println("Status: "+ status1);

	boolean valuesStatus = status1.equals(searchedStatus); // Valida si se
		// encuentra en
		// estatus R
		  while (valuesStatus) {

		 status1 = is.getData(0, "STATUS");
		 run_id = is.getData(0, "RUN_ID");
		 valuesStatus = status1.equals(searchedStatus);

	     u.hardWait(2);

		}
				
		boolean successRun = status1.equals(status);// Valida si se encuentra en S
				
		System.out.println(successRun);

//********************************************** Paso 4 ******************************************************
			 addStep("Establecer la conexi�n a la FCWMLQA");
			 
			 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
			 
//********************************************** Paso 5 ******************************************************


		addStep("Validar que se inserte el detalle de la ejecucion de la interface en la tabla WM_LOG_RUN de la DB  FCWMLQA. ");
		System.out.println("Paso 5");
		System.out.println(tdcIntegrationServerFormat);

		SQLResult tdcIntegrationServerFormatRes = executeQuery(dbLog, tdcIntegrationServerFormat);
		String runID = "";
		//valida si el status es S
		boolean validatdcIntegrationServerFormat = status1.equals(status);
		
		if (validatdcIntegrationServerFormat) {

			testCase.addQueryEvidenceCurrentStep(tdcIntegrationServerFormatRes);
			runID = tdcIntegrationServerFormatRes.getData(0, "RUN_ID");
		}

		System.out.println(validatdcIntegrationServerFormat);

		assertTrue(successRun, "La ejecucion de la interfaz no fue exitosa");
		
//********************************************** Paso 6 ***********************************************************
		
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningun error presente en la ejecucion de la interfaz  dentro de la tabla WM_LOG_ERROR ");
		System.out.println("Paso 6");
		String consultaError1Format = String.format(consultaError1, runID);
		
		//Primera parte
		System.out.println(consultaError1Format);

		SQLResult consultaError1Res = executeQuery(dbLog, consultaError1Format);

		boolean validaconsultaError1 = consultaError1Res.isEmpty();

		if (validaconsultaError1) {

			testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");
			testCase.addQueryEvidenceCurrentStep(consultaError1Res);

		} else {
			
			testCase.addQueryEvidenceCurrentStep(consultaError1Res);
		}

		System.out.println(validaconsultaError1);

	
		//Segunda parte
		String consultaError2Format = String.format(consultaError2, runID);
		System.out.println(consultaError2Format);

		SQLResult consultaError2Res = executeQuery(dbLog, consultaError2Format);

		boolean validaconsultaError2 = consultaError1Res.isEmpty();

		if (validaconsultaError2) {

			testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");
			testCase.addQueryEvidenceCurrentStep(consultaError2Res);

		} else {
			
			testCase.addQueryEvidenceCurrentStep(consultaError2Res);
		}

		System.out.println(validaconsultaError2);

		assertTrue(validaconsultaError1, "Se registraron errores en la ejecucion ");

//***************************** Paso 7 **********************************************************************
		
		addStep("Establecer la conexi�n a la FCWM6QA");
		System.out.println("Paso 7");
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
		 
//****************************************Paso 8 *************************************************************
		 
		 addStep("Validar que se inserte el registro del archivo PRS generado por la interface en la tabla POS_OUTBOUND_DOCS del esquema POSUSER de la BD FCWM6QA");
		 System.out.println("Paso 8");
			System.out.println(registroArchivoPRS);

			SQLResult registroArchivoPRSRes = executeQuery(dbPos, registroArchivoPRS );
			
			String DOC_NAME = "";

			boolean validaregistroArchivoPRS  = registroArchivoPRSRes.isEmpty();

			if (!validaregistroArchivoPRS) {
				

				DOC_NAME = registroArchivoPRSRes.getData(0, "DOC_NAME");
				testCase.addQueryEvidenceCurrentStep(registroArchivoPRSRes);
				

			}

			System.out.println("DOC_NAME" + DOC_NAME);
			System.out.println(validaregistroArchivoPRS);

			assertFalse(validaregistroArchivoPRS, "No se inserto el registro del archivo PRS ");

//**********************************Paso 9 **********************************************************************
		 Thread.sleep(20000);
				
	addStep("  Validar que se realice el env�o del archivo PRS generado por la interface en el directorio configurado para la tienda procesada. \r\n"
	+ "Servicio para obtener el directorio: Utilities.FTP:configFTP. ");
	System.out.println("Paso 9");	
				//Ruta: Ruta: /u01/posuser/FEMSA_OXXO/POS (en la carpeta working)
				
		       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser"); //Pendientes de cambiar
		       
		       Thread.sleep(20000);
		       
		       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/"+ DOC_NAME;
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
		        
		        assertTrue(validaFTP, "No se encontro el archivo xml en POSUSER ");
		        

		 
		 
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PS1_WMx86GeneracionArchivoPromo_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. ATC-FT-018 PS1 Generaci�n de archivo PRS de promociones Sinergia a trav�s de la interface FEMSA_PS1";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
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
