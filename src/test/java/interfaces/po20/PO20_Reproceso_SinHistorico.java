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

public class PO20_Reproceso_SinHistorico extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO20_Reproceso_Sin_Datos_Historicos(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbTdc = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				
/** VARIABLES *********************************************************************/	

		String selectHistoric = "SELECT * FROM TPEUSER.TDC_FIN_HISTORIC" + 
				" WHERE BANCO = 'FEMCO_BANAMEX' AND TO_CHAR(TRANSACTION_DATE, 'DD/MM/YYYY') = '" + data.get("date") + "' AND ESTATUS = 'I'";

		String tdcIntegrationServerFormat = "SELECT Tbl.*, (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PO20%' ORDER BY START_DT DESC";

		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		addStep("Validar que exista información del banco y dia proporcionado, en la tabla TDC_FIN_HISTORIC de TPEUSER con estatus I.");

		System.out.println(selectHistoric);
		SQLResult paso1_qry1_Result = dbTdc.executeQuery(selectHistoric);		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar.", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
	
		
		/* PASO 2 *********************************************************************/	

		addStep("Ejecutar el servicio: PO20.Pub:run. Solicitando la ejecución del Job: runPO20.  Proporcionando la fecha como parámetro de entrada.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmWithInput10(data.get("interface"), data.get("servicio"), data.get("date"), "Date");
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );
		u.close();
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
		
		
		
		/* PASO 3 *********************************************************************/	

		addStep("Validar que la interface se ejecuta con errores en WMLOG con estatus S.");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		
		if (av2 == false) {
			testCase.addQueryEvidenceCurrentStep(is);
		} else {
			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
		

		
		/* PASO 4 *********************************************************************/	

		addStep("Validar que en la tabla TDC_FIN_HISTORIC no tiene actualizaciones en el registro.");

		System.out.println(selectHistoric);
		SQLResult paso4_Result = dbTdc.executeQuery(selectHistoric);	

		boolean paso4_valida = paso4_Result.isEmpty(); // checa que el string contenga datos

		if (!paso4_valida) {
			testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso4_valida);
		assertFalse("No se encontraron registros ", paso4_valida); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PO20_Reproceso_Sin_Datos_Historicos";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. No existe informacion pendiente (Banco = 'BANAMEX', FLAG = N, Estatus Inicial / Final = 'I')";
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
