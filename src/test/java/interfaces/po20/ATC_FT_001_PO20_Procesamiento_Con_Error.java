 package interfaces.po20;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

/**
 * ATC_FT_001_PO20_Procesamiento_Con_Error
 * 
 * 
 * @reviewer Gilberto Martinez
 * @date 2023/15/02
 */

public class ATC_FT_001_PO20_Procesamiento_Con_Error extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO20_Procesamiento_Con_Error_tets(HashMap<String, String> data) throws Exception {

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
				  "SELECT FOLIO AS FOLIO_WM, PLAZA, TIENDA,FOLIO_TRANSACTION, REF1, REF2, CORTE, TICKET, COMISION FROM TPEUSER.TDC_TRANSACTION\n" + 
				  "WHERE WM_CODE=101 AND PROM_TYPE='PAY'\n" + 
				  "AND SWITCH = 'FEMCO_BBVA'\n" + 
				  "AND CREATION_DATE >= sysdate-30\n" +
				  "AND REVERSED IS NULL AND TICKET IS NOT NULL ORDER BY CREATION_DATE DESC";
				  
		 /* "SELECT folio,creation_date,plaza, tienda, caja, pv_date, wm_code FROM  TPEUSER.TDC_TRANSACTION"
		  +
		  " WHERE WM_CODE=101 AND PROM_TYPE='PAY' AND SWITCH = 'FEMCO_BANAMEX' AND (CREATION_DATE >= TO_DATE(SYSDATE-1, 'dd/MM/yy') "
		  + " AND  CREATION_DATE < TO_DATE(SYSDATE, 'dd/MM/yy')) AND REVERSED IS NULL";
		 */

		String tdcIntegrationServerFormat = "SELECT run_id,interface, start_dt, end_dt, status, server FROM WMLOG.WM_LOG_RUN WHERE\r\n"
				+ "INTERFACE = 'PO20' AND STATUS = 'E' \r\n"
				+ "AND TRUNC(SYSDATE) = TRUNC(START_DT) \r\n"
				+ "and ROWNUM=1 ORDER BY END_DT DESC";

		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		
		String selectHistoricF = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC WHERE BANCO = 'FEMCO_BANAMEX' AND " + 
				"TO_CHAR(TRANSACTION_DATE, 'DD/MM/YY') = TO_CHAR(SYSDATE-1, 'DD/MM/YY') " + 
				"AND ESTATUS = 'F' AND TRUNC(EXECUCION_DATE) = TO_DATE(SYSDATE, 'DD/MM/YY')";

				

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que no exista informaci�n del banco y dia anterior al actual del cual se consultar� la informaci�n en la tabla TDC_FIN_HISTORIC de TPEUSER.");

		// Primera consulta
		System.out.println(selectHistoric);
		SQLResult paso1_qry1_Result = dbTdc.executeQuery(selectHistoric);		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("Se encontraron registros a procesar, deberia estar vacio.", !paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 2 *********************************************************************/	

		addStep("Validar que se tenga informaci�n de transacciones exitosas (WM_CODE = 101) para la fecha correspondiente.");

		System.out.println(verifyDataToProcess);
		SQLResult paso2 = executeQuery(dbTdc, verifyDataToProcess);
		
		boolean validationDb = paso2.isEmpty();
		
		if (!validationDb) {				
			testCase.addQueryEvidenceCurrentStep(paso2);
			/*
			 * String wmCodeDb; wmCodeDb = paso2.getData(0, "WM_CODE"); validationDb =
			 * wmCodeDb.equals(wmCodeToValidate);
			 * System.out.println("Codigo valido?: "+validationDb + " - wmCode db: " +
			 * wmCodeDb); validationDb = !validationDb;
			 */
		}	
		System.out.println(validationDb);

		assertFalse(validationDb, "No se encontr� un registro con el folio solicitado o WM_CODE incorrecto.");
		

		/* PASO 3 *********************************************************************/	

		addStep("Ejecutar el servicio: PO20.Pub:run. Solicitando la ejecuci�n del Job: runPO20.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM2(data.get("interface"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}
		
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus E		

		if (!successRun) {
			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	

		
		
		
		/* PASO 4 *********************************************************************/	

		addStep("Validar que la interface se ejecuta con errores en WMLOG con estatus E.");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = E - " + validateStatus);
		assertTrue(validateStatus, "La ejecuci�n de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		
		if (av2 == false) {
			testCase.addQueryEvidenceCurrentStep(is);
		} else {
			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
		

		
		/* PASO 5 *********************************************************************/	

		addStep("Validar que en la tabla TDC_FIN_HISTORIC se inserta el registro correspondiente a la fecha y banco procesados por  la interface con estatus F.");

		System.out.println(selectHistoricF);
		SQLResult paso5_Result = dbTdc.executeQuery(selectHistoricF);	

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}
		testCase.addQueryEvidenceCurrentStep(paso5_Result);
		System.out.println(paso5_valida);
		assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO20_Procesamiento_Con_Error_tets";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Procesamiento cuando no existen datos historicos (La interface concluye con Error (Banco = 'BANAMEX', FLAG =I, Estatus Final = 'F'))";
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
