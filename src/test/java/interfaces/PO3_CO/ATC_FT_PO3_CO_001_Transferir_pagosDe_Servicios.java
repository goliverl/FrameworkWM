package interfaces.PO3_CO;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_PO3_CO_001_Transferir_pagosDe_Servicios extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO3_CO_001_Transferir_pagosDe_Servicios_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				
/** VARIABLES *********************************************************************/	

		String selectI = "SELECT A.PV_CR_PLAZA, A.PV_CR_TIENDA, B.ID, B.STATUS" + 
				" FROM posuser.POS_ENVELOPE A, posuser.POS_INBOUND_DOCS B, posuser.POS_SYB_DETL C" + 
				" WHERE B.PE_ID = A.ID" + 
				" AND B.ID = C.PID_ID" + 
				" AND B.STATUS = 'I'" + 
				" AND B.DOC_TYPE = 'SYB'" + 
				" AND A.PV_CR_PLAZA = '" + data.get("plaza") + "'" + 
				" AND A.PV_CR_TIENDA = '" + data.get("tienda") + "' and b.partition_date > sysdate -7 order by B.partition_date desc";

		String tdcIntegrationServerFormat = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'PO3_CO' AND STATUS = 'S' AND TRUNC(SYSDATE) = TRUNC(START_DT) ORDER BY START_DT DESC";

		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads

		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		String selectPagosValidos = "SELECT TRANS_ID, PLAZA, TIENDA, SERVICIO, FECHA_RECEPCION, ESTATUS, HORA_TRANSACCION FROM XXFC.XXFC_PAGO_SERVICIOS " + 
				" WHERE PLAZA = '" + data.get("plaza") + "'" + 
				" AND TIENDA = '" + data.get("tienda") + "'" +  
				" AND TRUNC(FECHA_RECEPCION) = TRUNC(SYSDATE)";
		
		String selectPagosInvalidos = "SELECT TRANS_ID, PLAZA, TIENDA, SERVICIO, FECHA_RECEPCION, ESTATUS, HORA_TRANSACCION FROM XXFC.XXFC_PAGO_SERVICIOS_WORK " + 
				" WHERE PLAZA = '" + data.get("plaza") + "'" + 
				" AND TIENDA = '" + data.get("tienda") + "'" +  
				" AND FECHA_RECEPCION = TO_CHAR(SYSDATE,'DD-MON-YY')";

		String posuserE = "SELECT ID, pe_id, pv_doc_id, SUBSTR(PV_DOC_NAME,4,5) AS PLAZA, DOC_TYPE, STATUS, TARGET_ID " + 
				" FROM posuser.POS_INBOUND_DOCS WHERE DOC_TYPE = 'SYB' " + 
				" AND STATUS = 'E'  AND ID ='%s'";			

		

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que exista información de documentos SYB pendientes por procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I' para la tienda.");

		System.out.println(selectI);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(selectI);		
		String ID = paso1_qry1_Result.getData(0, "ID");
		System.out.println("POS_INBOUND_DOCS.ID= " + ID); // la imprime

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			
			
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I' para la tienda. ", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		

		/* PASO 2 *********************************************************************/	

		addStep("Ejecutar la interface PO3_CO.Pub:run para procesar los documentos y transferir los pagos de servicios de POS Colombia hacia EBS Colombia.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		if (successRun) {

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

		}

		
		
		/* PASO 3 *********************************************************************/	

		addStep("Validar la correcta ejecución de la interface PO3_CO en la tabla WM WM_LOG_RUN de WMLOG.");

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

		addStep("Validar la correcta ejecución de los Threads por plaza lanzados por la interface PO3_CO en la tabla WM_LOG_THREAD.");

		String threads = String.format(consultaThreads, run_id);
		System.out.println("CONSULTA THREAD " + threads);
		SQLResult threadsResult = dbLog.executeQuery(threads);

		boolean av31 = threadsResult.isEmpty();
		
		if (av31 == false) {
			testCase.addQueryEvidenceCurrentStep(threadsResult);
		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult);
		}
		
		System.out.println("El registro en WM_LOG_THREAD esta vacio 1- " + av31);

		String threads2 = String.format(consultaThreads2, run_id); 		//Segunda parte de la consulta
		SQLResult threadsResult2 = dbLog.executeQuery(threads2);

		boolean av3111 = threadsResult2.isEmpty();
		
		if (av3111 == false) {
			testCase.addQueryEvidenceCurrentStep(threadsResult2);
		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult2);
		}
		
		System.out.println("El registro en WM_LOG_THREAD esta vacio 2- " + av31);
		assertFalse("No se generaron threads en la tabla WM_LOG_THREAD.", av3111);

		
		
		/* PASO 5 *********************************************************************/	

		addStep("Si existen pagos válidos, validar la inserción de los pagos validos de la tienda '" + data.get("tienda") + "' en la tabla XXFC.XXFC_PAGO_SERVICIOS de EBS Colombia.");

		System.out.println(selectPagosValidos);
		SQLResult paso5_Result = dbEbs.executeQuery(selectPagosValidos);

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);
		//assertFalse("No se encontraron registros en la tabla XXFC.XXFC_PAGO_SERVICIOS de EBS Colombia.", paso5_valida); // Si esta vacio, imprime mensaje
		
	
	
		/* PASO 6 *********************************************************************/	

		addStep("Si existen pagos inválidos, validar la inserción de los pagos inválidos de la tienda '" + data.get("tienda") + "' en la tabla XXFC.XXFC_PAGO_SERVICIOS_WORK de EBS Colombia.");
		
		System.out.println(selectPagosInvalidos);
		SQLResult paso6_Result = dbEbs.executeQuery(selectPagosInvalidos);
		boolean paso6_valida = paso6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida);
		//assertFalse("No se encontraron registros en la tabla XXFC.XXFC_PAGO_SERVICIOS_WORK de EBS Colombia.", paso6_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 7 *********************************************************************/

		addStep("Validar que se actualicen los campos STATUS y TARGET_ID en la tabla POS_INBOUND_DOCS de POSUSER.");

		String paso7_format = String.format(posuserE, ID);
		System.out.println(paso7_format);
		SQLResult paso7_Result = dbPos.executeQuery(paso7_format);

		boolean paso7_valida = paso7_Result.isEmpty(); // checa que el string contenga datos

		if (!paso7_valida) {
			testCase.addQueryEvidenceCurrentStep(paso7_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso7_valida);
		assertFalse("No se encontraron registros en la tabla POS_INBOUND_DOCS de POSUSER.", paso7_valida); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Transferir la información referente al pago de servicios provenientes del POS Colombia hacia la instancia de Oracle Colombia.";
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
