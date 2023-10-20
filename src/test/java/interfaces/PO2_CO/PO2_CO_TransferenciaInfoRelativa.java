package interfaces.PO2_CO;

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
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class PO2_CO_TransferenciaInfoRelativa extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO2_CO_TransferenciaInfoRelativa(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; //status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				
/** VARIABLES *********************************************************************/	

		// Paso 1
		String paso1_qry1 = "SELECT A.PV_CR_PLAZA, A.PV_CR_TIENDA, B.ID, b.PE_ID, H.PREFIJO || H.FORMA_PAGO_NUM FORMA_PAGO,  H.PREFIJO, H.FORMA_PAGO_NUM, H.NUM_CORTE, H.VALOR, TO_CHAR(H.PV_DATE, 'YYYYMMDD') PV_DATE  "
				+ "FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_HEF_DETL  H "
				+ "WHERE A.ID = B.PE_ID AND B.ID = H.PID_ID "
				+ "AND H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV') "
				+ "AND B.DOC_TYPE = 'HEF' AND B.STATUS = 'I' " + "AND A.PV_CR_PLAZA = '" + data.get("plaza")
				+ "' AND A.PV_CR_TIENDA = '" + data.get("tienda") + "' and b.partition_date > sysdate -7 order by B.partition_date desc";

		// Paso 3
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server " + 
				" FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PO2_CO' AND STATUS = 'S' " + 
			    " ORDER BY START_DT DESC) where rownum <=1";

		// consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		// Paso 4 
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads

		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads

		// Paso 5 PASO POSUSER
		String posuserE = "SELECT ID,SUBSTR(PV_DOC_NAME,4,5) AS PLAZA, DOC_TYPE, STATUS, TARGET_ID  "
				+ "FROM POSUSER.POS_INBOUND_DOCS  " + "WHERE DOC_TYPE = 'HEF'  " + "AND STATUS = 'E'  "
				+ "AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' " + " AND ID ='%s'";

		// Paso 6
		String paso6_qry1 = "SELECT status, ledger_id, accounting_date, currency_code, date_created, created_by, actual_flag, user_je_category_name "
				+ "FROM GL_INTERFACE  " + "WHERE STATUS = 'NEW'  " + "AND REFERENCE6 = '%s' ";

		String paso6_qry2 = "SELECT user_je_source_name, segment1, segment2, segment3, segment4, segment5, segment6, segment7 "
				+ "FROM GL_INTERFACE  " + "WHERE STATUS = 'NEW'  " + "AND REFERENCE6 = '%s' ";

		String paso6_qry3 = "SELECT entered_cr, accounted_cr, reference1, reference2 FROM GL_INTERFACE  "
				+ "WHERE STATUS = 'NEW'  " + "AND REFERENCE6 = '%s' ";

		String paso6_qry4 = "SELECT reference4, reference6, reference10, je_batch_id FROM GL_INTERFACE  "
				+ "WHERE STATUS = 'NEW'  " + "AND REFERENCE6 = '%s' ";

		String paso6_qry5 = "SELECT je_header_id, je_line_num, group_id, request_id, set_of_books_id, code_combination_id_interim"
				+ " FROM GL_INTERFACE  " + "WHERE STATUS = 'NEW'  " + "AND REFERENCE6 = '%s' ";

		

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que existen documentos pendientes de procesar de tipo HEC " + "para la plaza '" + data.get("plaza") + "' con estatus I en POSUSER.");

		// Primera consulta
		System.out.println(paso1_qry1);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(paso1_qry1);		
		String ID = null;
		System.out.println("POS_INBOUND_DOCS.ID= " + ID); // la imprime

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			ID = paso1_qry1_Result.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
	//	assertFalse("No se encontraron registros a procesar ", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		

		/* PASO 2 *********************************************************************/	

		addStep("Se ejecuta el proceso PO2_CO.Pub:run.");

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

		addStep("Validar que se inserte el detalle de la ejecución de la interface PO2_CO en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'");

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

		addStep("Validar que se inserte el detalle de ejecución de los threads lanzados por la interface PO2_CO en la tabla WM_LOG_THREAD de WMLOG.");

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

		//Segunda consulta

		String threads2 = String.format(consultaThreads2, run_id);
		SQLResult threadsResult2 = dbLog.executeQuery(threads2);

		boolean av3111 = threadsResult2.isEmpty();
		
		if (av3111 == false) {
			testCase.addQueryEvidenceCurrentStep(threadsResult2);
		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult2);
		}
		
		System.out.println("El registro en WM_LOG_THREAD esta vacio 2- " + av31);
		assertFalse("No se generaron threads en la tabla", av3111);

		
		
		/* PASO 5 *********************************************************************/	

		addStep("Validar que se actualicen los campos STATUS y TARGET_ID en la tabla POS_INBOUND_DOCS de POSUSER.");

		String paso5_format = String.format(posuserE, ID);
		System.out.println(paso5_format);
		SQLResult paso5_Result = dbPos.executeQuery(paso5_format);
		String TARGET_ID = paso5_Result.getData(0, "TARGET_ID");

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);
		assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje

	
	
		//* PASO 6 *********************************************************************	

		addStep("Validar que la información se inserta correctamente en la tabla GL_INTERFACE de EBS_COL.");

		// Parte 1

		String paso6_format_1 = String.format(paso6_qry1, TARGET_ID);

		System.out.println(paso6_format_1);

		SQLResult paso6_Result_1 = dbEbs.executeQuery(paso6_format_1);

		boolean paso6_valida_1 = paso6_Result_1.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida_1) {

			testCase.addQueryEvidenceCurrentStep(paso6_Result_1); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida_1);

		// Parte 2

		String paso6_format_2 = String.format(paso6_qry2, TARGET_ID);

		System.out.println(paso6_format_2);

		SQLResult paso6_Result_2 = dbEbs.executeQuery(paso6_format_2);

		boolean paso6_valida_2 = paso6_Result_2.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida_2) {

			testCase.addQueryEvidenceCurrentStep(paso6_Result_2); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida_2);

		// Parte 3

		String paso6_format_3 = String.format(paso6_qry3, TARGET_ID);

		System.out.println(paso6_format_3);

		SQLResult paso6_Result_3 = dbEbs.executeQuery(paso6_format_3);

		boolean paso6_valida_3 = paso6_Result_3.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida_3) {

			testCase.addQueryEvidenceCurrentStep(paso6_Result_3); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida_3);

		// Parte 4

		String paso6_format_4 = String.format(paso6_qry4, TARGET_ID);

		System.out.println(paso6_format_4);

		SQLResult paso6_Result_4 = dbEbs.executeQuery(paso6_format_4);

		boolean paso6_valida_4 = paso6_Result_4.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida_4) {

			testCase.addQueryEvidenceCurrentStep(paso6_Result_4); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida_4);

		//Parte  5

		String paso6_format_5 = String.format(paso6_qry5, TARGET_ID);

		System.out.println(paso6_format_5);

		SQLResult paso6_Result_5 = dbEbs.executeQuery(paso6_format_5);

		boolean paso6_valida_5 = paso6_Result_5.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida_5) {

			testCase.addQueryEvidenceCurrentStep(paso6_Result_5); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida_5);

		assertFalse("No se encontraron registros ", paso6_valida_5); // Si esta vacio, imprime mensaje

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO2_CO_TransferenciaInfoRelativa";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que se inserta correctamente la información desde POSUSER a ORAFIN para la plaza 10BCA";
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
