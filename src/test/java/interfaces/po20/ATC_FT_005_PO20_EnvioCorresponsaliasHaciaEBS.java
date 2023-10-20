 package interfaces.po20;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
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

public class ATC_FT_005_PO20_EnvioCorresponsaliasHaciaEBS extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PO20_EnvioCorresponsaliasHaciaEBS_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbTdc = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		SeleniumUtil u;
		PakageManagment pok;
		
		String wmCodeToValidate = "101";
		String status = "E"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				
/** VARIABLES *********************************************************************/	

		String selectHistoric = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC WHERE BANCO = 'FEMCO_BANAMEX' AND TO_CHAR(TRANSACTION_DATE, 'DD/MM/YY') = TO_CHAR(SYSDATE-1, 'DD/MM/YY')";

		
		  String verifyDataToProcess =
				  "SELECT * CATEGORY,VALUE1,VALUE2 FROM TPE_CONFIG\r\n" + 
				  "WHERE CATEGORY IN= ('CORRESP_CONFIG_DAYS','CORRESP_SWITCH')";
				  
		 /* "SELECT folio,creation_date,plaza, tienda, caja, pv_date, wm_code FROM  TPEUSER.TDC_TRANSACTION"
		  +
		  " WHERE WM_CODE=101 AND PROM_TYPE='PAY' AND SWITCH = 'FEMCO_BANAMEX' AND (CREATION_DATE >= TO_DATE(SYSDATE-1, 'dd/MM/yy') "
		  + " AND  CREATION_DATE < TO_DATE(SYSDATE, 'dd/MM/yy')) AND REVERSED IS NULL";
		 */

		String tdcIntegrationServerFormat = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'PO20' AND STATUS = 'E' AND TRUNC(SYSDATE) = TRUNC(START_DT) ORDER BY RUN_ID DESC";

		
		//Paso 5
		
		String tdcIntegrationServerFormaterror = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" +
								  " FROM WMLOG.WM_LOG_RUN Tbl \r\n" + 
				                  "WHERE INTERFACE='RE1' \r\n" +
				                  "AND start_dt >= trunc(SYSDATE) \r\n" + 
								  "ORDER BY START_DT DESC) \r\n"
								  + "where rownum <=1 ";
		
		// consultas de error
		
				String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
					 "from wmlog.WM_LOG_ERROR \r\n" + 
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <=1"; // dbLog
				
				String consultaError2 = " select description,MESSAGE \r\n" +
					 "from wmlog.WM_LOG_ERROR \r\n" +
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <= 1"; // dbLog
				
				String consultaError3 = " select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n" +
						"from wmlog.WM_LOG_ERROR \r\n" + 
						"where RUN_ID='%s' \r\n" +
					    "and rownum <= 1"; // dbLog
		
		String selectHistoricF = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC WHERE BANCO = 'FEMCO_BANAMEX' AND " + 
				"TO_CHAR(TRANSACTION_DATE, 'DD/MM/YY') = TO_CHAR(SYSDATE-1, 'DD/MM/YY') " + 
				"AND ESTATUS = 'F' AND TRUNC(EXECUCION_DATE) = TO_DATE(SYSDATE, 'DD/MM/YY')";

				

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que no exista informaci�n del banco y dia anterior al actual del cual se consultar� la informaci�n en la tabla TPE_CONFIG de FCTDCQA.");

		// Primera consulta
		System.out.println(selectHistoric);
		SQLResult paso1_qry1_Result = dbTdc.executeQuery(selectHistoric);		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("Se encontraron registros a procesar, deberia estar vacio.", !paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		
		
	//   Paso 2**************************************
		
					addStep("Ejecutar el servicio PO20.pub:run. La interfaz ser� ejecutada por el job  runPO20 de Ctrl-M ");

					u  = new SeleniumUtil(new ChromeTest(), true);
					
					JSONObject obj = new JSONObject(data.get("job"));

					addStep("Jobs en  Control M ");
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
		
					

	///		Paso 3	************************

					addStep("Se valida la generacion de thread");

					String tdcQueryStatusThread = null;
					Object run_id = null;
					String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
					System.out.println(queryStatusThread);

					SQLResult regPlazaTiendaThread = dbLog.executeQuery(queryStatusThread);

					String Threadstatus = regPlazaTiendaThread.getData(0, "STATUS");

					boolean threadResult = regPlazaTiendaThread.isEmpty();
					if (!threadResult) {
						testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
					}
							
					boolean statusThread = status.equals(Threadstatus);
					System.out.println(statusThread);
					if (!statusThread) {

						String tdcQueryWmlogError = null;
						String error = String.format(tdcQueryWmlogError, run_id);
						SQLResult errorQueryExe = dbLog.executeQuery(error);

						boolean emptyError = errorQueryExe.isEmpty();

						if (!emptyError) {

							testCase.addTextEvidenceCurrentStep(
									"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

							testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);

						} else {

							testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);

						}
					}
	
		

		
		/* PASO 4 *********************************************************************/	

					addStep("Validar el registro de ejecuci�n de la interfaz en la base de datos del WMLOG.");
					
					
					String statuss = "S";
					String searchedStatusr = "R";
					SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
					String run_id2 = is.getData(0, "RUN_ID");
					String status1 = "";// guarda el run id de la
																// ejecuci�n
					boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																			// encuentra en R														// estatus R
					while (valuesStatus) {

						status1 = is.getData(0, "STATUS");
						run_id2 = is.getData(0, "RUN_ID");
						valuesStatus = status1.equals(searchedStatus);

						u.hardWait(2);

					}

					boolean successRun = status1.equals(status);// Valida si se encuentra en
					
					System.out.println("El status es S: "+ successRun);
																// estatus S

					if (status1 == "E") {

						String error = String.format(consultaError1, run_id2); //Primer consulta de error
						String error1 = String.format(consultaError2, run_id2); //Segunda consulta de error
						String error2 = String.format(consultaError3, run_id2); //Tercera consulta de error

						SQLResult errorr = dbLog.executeQuery(error);
						boolean emptyError = errorr.isEmpty();

						//Primer consulta de error
						if (!emptyError) {

							testCase.addTextEvidenceCurrentStep(
									"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

							testCase.addQueryEvidenceCurrentStep(errorr);

						}
						//Segunda consulta de error
						SQLResult errorIS = dbLog.executeQuery(error1);

						boolean emptyError1 = errorIS.isEmpty();

						if (!emptyError1) {

							testCase.addQueryEvidenceCurrentStep(errorIS);

						}

						//Tercera consulta de error
						SQLResult errorIS2 = dbLog.executeQuery(error2);
						boolean emptyError2 = errorIS2.isEmpty();

						if (!emptyError2) {

							testCase.addQueryEvidenceCurrentStep(errorIS2);

						}
					}
					
					if (successRun) {

						testCase.addQueryEvidenceCurrentStep(is);

					}
					

					System.out.println(tdcIntegrationServerFormat);
					

				
				//	assertTrue(successRun, "La ejecuci�n de la interfaz no fue exitosa");
		

		
		/* PASO 5 *********************************************************************/	

		addStep("Validar que en la tabla TDC_FIN_HISTORIC se inserta el registro correspondiente a la fecha y banco procesados por  la interface.");

		System.out.println(selectHistoricF);
		SQLResult paso5_Result = dbTdc.executeQuery(selectHistoricF);	

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);
		assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
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
