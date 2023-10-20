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

public class PO20_Procesamiento_Normal extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO20_Procesamiento_Normal(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbTdc = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

		SeleniumUtil u;
		PakageManagment pok;
		
		String wmCodeDb = null, plaza = null, tienda = null, folio = null;
		String wmCodeToValidate = "101";
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				 
/** VARIABLES *********************************************************************/	

		String selectHistoric = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC WHERE BANCO = '" + data.get("banco") + "' AND TO_CHAR(TRANSACTION_DATE, 'DD/MM/YY') = TO_CHAR(SYSDATE-1, 'DD/MM/YY')";

		String verifyDataToProcess = "SELECT folio,creation_date,plaza, tienda, caja, pv_date, wm_code, TICKET FROM  TPEUSER.TDC_TRANSACTION" + 
				" WHERE WM_CODE=101 AND PROM_TYPE='PAY' AND SWITCH = '" + data.get("banco") + "' AND (CREATION_DATE >= TO_DATE(SYSDATE-1, 'dd/MM/yy') " + 
				" AND  CREATION_DATE < TO_DATE(SYSDATE, 'dd/MM/yy')) AND REVERSED IS NULL AND TICKET IS NOT NULL";

		String tdcIntegrationServerFormat = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'PO20' AND STATUS = 'S' AND TRUNC(SYSDATE) = TRUNC(START_DT) ORDER BY RUN_ID DESC";

		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String selectEbs = "SELECT * FROM XXFC.XXFC_PAGO_SERVICIOS" + 
				" WHERE FOLIO_TRANSACCION = '%s'" + 
				" AND PLAZA = '%s'" + 
				" AND TIENDA = '%s'" + 
				" AND TRUNC(FECHA_RECEPCION) = TO_DATE(SYSDATE, 'dd/MM/yy')";
		
		String selectHistoricI = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC WHERE BANCO = '" + data.get("banco") + "' AND " + 
				"TO_CHAR(TRANSACTION_DATE, 'DD/MM/YY') = TO_CHAR(SYSDATE-1, 'DD/MM/YY') " + 
				"AND ESTATUS = 'I' AND TRUNC(EXECUCION_DATE) = TO_DATE(SYSDATE, 'DD/MM/YY')";

				

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		addStep("Validar que NO exista información del banco y dia anterior al actual del cual se consultará la información en la tabla TDC_FIN_HISTORIC de TPEUSER.");

		System.out.println(selectHistoric);
		SQLResult paso1_qry1_Result = dbTdc.executeQuery(selectHistoric);		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		} 

		System.out.println(paso1_qry1_valida);
		assertFalse("Se encontraron registros a procesar, deberia estar vacio.", !paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 2 *********************************************************************/	

		addStep("Validar que se tenga información de transacciones exitosas (WM_CODE = 101) para la fecha correspondiente.");

		System.out.println(verifyDataToProcess);
		SQLResult paso2 = executeQuery(dbTdc, verifyDataToProcess);
		
		boolean validationDb = paso2.isEmpty();
		
		if (!validationDb) {				
			testCase.addQueryEvidenceCurrentStep(paso2);
			wmCodeDb = paso2.getData(0, "WM_CODE");
			folio = paso2.getData(0, "folio");
			plaza = paso2.getData(0, "plaza");
			tienda = paso2.getData(0, "tienda");

			validationDb = wmCodeDb.equals(wmCodeToValidate);
			System.out.println("Codigo valido?: "+validationDb + " - wmCode db: " + wmCodeDb);
			validationDb = !validationDb;
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado o WM_CODE incorrecto.");
		

		
		/* PASO 3 *********************************************************************/	

		addStep("Ejecutar el servicio: PO20.Pub:run. Solicitando la ejecución del Job: runPO20.");

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

		String error = String.format(consultaError1, run_id);
		String error1 = String.format(consultaError2, run_id);
		String error2 = String.format(consultaError3, run_id);

		SQLResult errorr = dbLog.executeQuery(error);
		boolean emptyError = errorr.isEmpty();
		
		if (!emptyError) {
			testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
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
		
		/* PASO 4 *********************************************************************/	

		addStep("Validar que la interface se ejecuta con errores en WMLOG con estatus S.");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = E - " + validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		 
		if (av2 == false) {
			testCase.addQueryEvidenceCurrentStep(is);
		} else {
			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
		

		
		/* PASO 5 *********************************************************************/	

		addStep("Validar que la infomación se insertó correctamente en ORAFIN en la tabla: XXFC_PAGO_SERVICIOS.");

		String paso5 = String.format(selectEbs, folio, plaza, tienda);
		System.out.println(paso5);
		SQLResult paso5_Result = dbEbs.executeQuery(paso5);	

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);
		assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 6 *********************************************************************/	

		addStep("Validar que en la tabla TDC_FIN_HISTORIC se inserta el registro con estatus I correspondiente a la fecha y banco procesados por  la interface.");

		System.out.println(selectHistoricI);
		SQLResult paso6_Result = dbTdc.executeQuery(selectHistoricI);	

		boolean paso6_valida = paso6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida);
		assertFalse("No se encontraron registros ", paso6_valida); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PO20_Procesamiento_Normal";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. No existen datos historicos. La interface termina correctamente.";
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
