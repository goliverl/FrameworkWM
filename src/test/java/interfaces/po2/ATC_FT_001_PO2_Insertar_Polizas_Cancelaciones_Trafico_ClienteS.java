package interfaces.po2;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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


public class ATC_FT_001_PO2_Insertar_Polizas_Cancelaciones_Trafico_ClienteS extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO2_Insertar_Polizas_Cancelaciones_Trafico_ClienteS_test(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA , GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		//utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
		
 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1
		
		String paso1_qry1 = "SELECT DISTINCT a.id, pe_id, pv_doc_id, status, doc_type, backup_status, pv_doc_name,target_id  FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B, POSUSER.POS_HEF_DETL H \r\n" + 
				"WHERE A.DOC_TYPE = 'HEF' \r\n" + 
				"AND A.STATUS= 'I' \r\n" + 
				"AND H.PID_ID = A.ID \r\n" + 
				"AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA \r\n" + 
				"AND B.PAIS = 'MEX' \r\n" + 
				"AND B.CR_PLAZA = '"+data.get("plaza") +"' \r\n" + 
				"AND H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV', 'EECI', 'ERCI', 'ENCI', 'TTRV')";
		
		String paso1_qry2= "SELECT DISTINCT a. target_alt_id, received_date, inserted_date, version, pr50_version, sourceapp, h. partition_date FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.PLAZAS B, POSUSER.POS_HEF_DETL H \r\n" + 
				"WHERE A.DOC_TYPE = 'HEF' \r\n" + 
				"AND A.STATUS= 'I' \r\n" + 
				"AND H.PID_ID = A.ID \r\n" + 
				"AND SUBSTR(A.PV_DOC_NAME,4,5) = B.CR_PLAZA \r\n" + 
				"AND B.PAIS = 'MEX' \r\n" + 
				"AND B.CR_PLAZA = '"+data.get("plaza") +"' \r\n" + 
				"AND H.PREFIJO IN ('TTYP', 'COUP', 'TCLI', 'TCAN','PYIO', 'TDEV', 'EECI', 'ERCI', 'ENCI', 'TTRV')";
		
		//Paso 3
		
		String tdcIntegrationServerFormat = "	select * from \r\n"
				+ "(SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl \r\n"
				+ "WHERE INTERFACE = 'PO2' \r\n"
				+ "AND STATUS = 'S' \r\n" + 
				"ORDER BY START_DT DESC) \r\n"
				+ "where rownum <=1";
		
	//	String tdcIntegrationServerFormat = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'PO2' AND TRUNC(END_DT) = TRUNC(SYSDATE) AND STATUS = 'S' ORDER BY RUN_ID DESC";
		
		
		
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		//Paso 4
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='2175167123' "
				+ "ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID='%s'";// Consulta para los Threads
		
		//Paso 5
		
		String paso5= "SELECT ID,SUBSTR(PV_DOC_NAME,4,5) AS PLAZA, DOC_TYPE, STATUS, TARGET_ID \r\n" + 
				"FROM POSUSER.POS_INBOUND_DOCS \r\n" + 
				"WHERE DOC_TYPE = 'HEF' \r\n" + 
				"AND SUBSTR(PV_DOC_NAME,4,5) = '"+ data.get("plaza") +"'\r\n" + 
				"AND ID =%s\r\n";
		
		
		//Paso 6
		
		//status, ledger_id, accounting_date, currency_code, date_created, created_by, actual_flag, user_je_category_name
		
		String paso6_qry1 = "SELECT status, ledger_id, accounting_date, currency_code, date_created, created_by, actual_flag, user_je_category_name "
				+ "FROM GL_INTERFACE \r\n" + 
				"WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n" + 
				"AND SEGMENT3 = '"+ data.get("plaza") +"' \r\n" + 
				"AND STATUS = 'NEW' \r\n" + 
				"AND REFERENCE6 = '%s'\r\n" ;
		
		String paso6_qry2 = "SELECT user_je_source_name, segment1, segment2, segment3, segment4, segment5, segment6, segment7 "
				+ "FROM GL_INTERFACE \r\n" + 
				"WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n" + 
				"AND SEGMENT3 = '"+ data.get("plaza") +"' \r\n" + 
				"AND STATUS = 'NEW' \r\n" + 
				"AND REFERENCE6 = '%s'\r\n" ;
		
		String paso6_qry3 = "SELECT entered_cr, accounted_cr, reference1, reference2"
				+ " FROM GL_INTERFACE \r\n" + 
				"WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n" + 
				"AND SEGMENT3 = '"+ data.get("plaza") +"' \r\n" + 
				"AND STATUS = 'NEW' \r\n" + 
				"AND REFERENCE6 = '%s'\r\n" ;
		
		String paso6_qry4 = "SELECT reference4, reference6, reference10, je_batch_id"
				+ " FROM GL_INTERFACE \r\n" + 
				"WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n" + 
				"AND SEGMENT3 = '"+ data.get("plaza") +"' \r\n" + 
				"AND STATUS = 'NEW' \r\n" + 
				"AND REFERENCE6 = '%s'\r\n" ;
		
		String paso6_qry5 = "SELECT je_header_id, je_line_num, group_id, request_id, set_of_books_id, code_combination_id_interim"
				+ " FROM GL_INTERFACE \r\n" + 
				"WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n" + 
				"AND SEGMENT3 = '"+ data.get("plaza") +"' \r\n" + 
				"AND STATUS = 'NEW' \r\n" + 
				"AND REFERENCE6 = '%s'\r\n" ;
		
		
//Pasos ***********************************************************************************************************************************************************************		
		
		
		// Paso 1

		addStep("Consultar que existan registros en las tablas POS_INBOUND_DOCS de la BD POSUSER \r\n"
				+ "con DOC_TYPE igual a 'HEF', Plaza igual a '" + data.get("plaza")
				+ "' y STATUS igual a 'I' y PAIS igual a 'MEX'.");

		// Primera consulta
		System.out.println("Paso 1: \n"+paso1_qry1);
		
		 SQLResult paso1_qry1_Result = dbPos.executeQuery(paso1_qry1);
		  if(paso1_qry1_Result.getRowCount() <= 0) throw new Exception("No existen los"
		  +"insumos necesarios en la base de datos para continuar con la prueba, Query: "
		  +paso1_qry1);
		  
		String ID = paso1_qry1_Result.getData(0, "ID");

		System.out.println("POS_INBOUND_DOCS.ID= " + ID); // la imprime

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {

			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);

		// Segunda consulta
		System.out.println("Paso 1: Query 2 \n"+paso1_qry2);

		SQLResult paso1_qry2_Result = dbPos.executeQuery(paso1_qry2);

		boolean paso1_qry2_valida = paso1_qry2_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry2_valida) {

			testCase.addQueryEvidenceCurrentStep(paso1_qry2_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry2_valida);

		assertFalse("No se encontraron registros a procesar ", paso1_qry2_valida); // Si esta vacio, imprime mensaje

		// ***************************************************************************************************************************************************************
		// Paso 2

		addStep("Se ejecuta el proceso PO2.Pub:run.");

		// Utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S"; // status exitoso

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		System.out.println("RUN_ID = " + run_id);
		String status1 = is.getData(0, "STATUS");

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

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

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

//***************************************************************************************************************************************************************************************

		// Paso 3

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'PO2' y STATUS es igual a 'S'.");
		System.out.println("Paso 3: ");
		
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

//***************************************************************************************************************************************************************************************

		// Paso 4

		addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
		System.out.println("Paso 4: ");
		
		String threads = String.format(consultaThreads, run_id);

		System.out.println("CONSULTA THREAD: " + threads);

		SQLResult threadsResult = dbLog.executeQuery(threads);

		boolean av31 = threadsResult.isEmpty();
		if (av31 == false) {

			testCase.addQueryEvidenceCurrentStep(threadsResult);

		} else {
			testCase.addQueryEvidenceCurrentStep(threadsResult);
		}
		System.out.println("El registro en WM_LOG_THREAD esta vacio 1- " + av31);

		// .-----------Segunda consulta

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

//************************************************************************************************************************************************************************************			
		// Paso 5

		addStep("Comprobar que los documentos fueron actualizados correctamente en la tabla POS_INBOUND_DOCS en la BD POSUSER donde WMSTATUS igual a 'E', Plaza igual a '"
				+ data.get("plaza") + "'.");

		String paso5_format = String.format(paso5, ID);

		System.out.println("Paso 5: \n"+paso5_format);

		SQLResult paso5_Result = dbPos.executeQuery(paso5_format);

		String TARGET_ID = paso5_Result.getData(0, "TARGET_ID");
		String statusE = "";

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			statusE = paso5_Result.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);

		assertEquals(statusE, "E"); // Validar status E

//**************************************************************************************************************************************************************************************

		// Paso 6

		addStep("Comprobar que los registros se insertaron correctamente en la Tabla GL_INTERFACE en la BD ORAFIN donde SEGMENT3  es igual a '"
				+ data.get("plaza") + "'.");

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
		return "ATC_FT_001_PO2_Insertar_Polizas_Cancelaciones_Trafico_ClienteS";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Insertar Polizas Cancelaciones y Trafico de Clientes";
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



