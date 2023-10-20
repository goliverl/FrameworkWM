package interfaces.rr01;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class RR01_ValidaProcesoDeCopia extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	
	public void ATC_FT_RR01_001_Valida_Proceso_copia(HashMap<String, String> data) throws Exception {
		//**********************************************************************************************************************************************************************
		
				utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
				utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
				
				//Variables
		//***********************************************************************************************************************************************************************

				//Paso 1
				
				String  validaTienda = "SELECT*FROM (SELECT DISTINCT substr(s.store_name10,0,5) plaza, substr(s.store_name10,6,10) tienda, t.LOCATION, t.loc_type, t.stock_cat, t.status\r\n" + 
						"FROM WMUSER.repl_item_loc_t t, RMS100.store s \r\n" +
					    "WHERE t.location = s.store \r\n" +
					    "AND t.stock_cat = 'D'\r\n" + 
						"AND t.loc_type = 'S' \r\n" +
						"AND t.status = 'A') \r\n" +
					    "WHERE ROWNUM <=1 ";
				
				//Paso 4, 5 y 6
				String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
						"FROM WMLOG.WM_LOG_RUN Tbl \r\n" +
					    "WHERE interface = 'RR01'  \r\n" +
					    "AND STATUS = 'S' \r\n" +
					    "AND start_dt >= trunc(SYSDATE) \r\n" + 
						"ORDER BY START_DT DESC) \r\n" +
					    "where rownum <=1";// Consulta para estatus de la ejecucion
				
				String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
						"from  wmlog.WM_LOG_ERROR \r\n" + 
						"where RUN_ID='%s') \r\n" +
					    "where rownum <=1";// Consulta para los errores
				String consultaERROR2 = " select * from (select description,MESSAGE \r\n" + 
					    "from wmlog.WM_LOG_ERROR \r\n" +
						"where RUN_ID='%s') \r\n" +
					    "WHERE rownum <= 1";// Consulta para los errores
				String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n" +
						"from wmlog.WM_LOG_ERROR \r\n" + 
						"where RUN_ID='%s') \r\n" +
						"WHERE rownum <= 1";// Consulta para los errores
				
				String validaRegistro = "SELECT*FROM (SELECT supplier, store, store_name, plaza_cr,status, last_update_date \r\n" +
						 "FROM WMUSER.wm_store_supp_country_plz \r\n" +
						 "WHERE store = '%s') \r\n" +
						 "WHERE ROWNUM <= 1"; //Recibe location
				
				
		//************************************************** Paso 1 ********************************************************************************
			
				
				addStep("Validar que la tienda exista en la tabla repl_item_loc_t de RETEK.");
				System.out.println( validaTienda);
				
				SQLResult validaTiendaR = dbRms.executeQuery(validaTienda);
				
				
				String location = validaTiendaR.getData(0, "LOCATION");
				
				System.out.println("LOCATION: " + location);
				
				boolean validaTiendaB = validaTiendaR.isEmpty();
				
				if (!validaTiendaB) {
					
					testCase.addQueryEvidenceCurrentStep(validaTiendaR);
				}
				assertFalse("No se presentan los datos de la Tienda a procesar  ", validaTiendaB );
				
				System.out.println( validaTiendaB);

				
		//************************************************ Paso 2**********************************************************************************		 
				
				addStep("Ejecutar el servicio RR01.Pub:run. La interfaz será invocada con el job runRR01 desde Ctrl-M. ");
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
				System.out.println(dateExecution);

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

		//************************************Paso 3 ***********************************************************************************	
				addStep("Validar el registro de ejecución de la interfaz en la tabla wm_log_thread de WMLOG.");
				
				SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

				String fcwS = is1.getData(0, "STATUS");
				boolean validateStatus = fcwS.equals(status);
				System.out.println(validateStatus);
				assertTrue("La ejecucion de la interfaz no fue exitosa", validateStatus);
				SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
				System.out.println(tdcIntegrationServerFormat );

				boolean log1 = log.isEmpty();

				if (!log1) {

					testCase.addQueryEvidenceCurrentStep(log);
				}

				System.out.println(log1);
				assertFalse( "La interfaz seesta ejecutando aun ",  log1);

				
		//************************************Paso 4*************************************************************************************
				addStep(" Validar la insercion del registro en la tabla wm_store_supp_country_plz de RETEK.");
				//Primera parte
				String validaRegistroF = String.format(validaRegistro, location);
				
				System.out.println(validaRegistroF);
				
				SQLResult validaRegistroR = dbRms.executeQuery(validaRegistroF);
						
				boolean validaRegistroB = validaRegistroR.isEmpty();
				
				if (!validaRegistroB) {
					
					testCase.addQueryEvidenceCurrentStep(validaRegistroR);
				}
				
				System.out.println(validaRegistroB);
				
				
				
				assertFalse("No se presento el registro insertado por parte de la interfaz.", validaRegistroB );
				

   
	
	}    
        

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RR01_001_Valida_Proceso_copia";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Copiar relaciones en RMS. Valida el proceso de copia de la Plaza con la Tienda correspondiente";
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



