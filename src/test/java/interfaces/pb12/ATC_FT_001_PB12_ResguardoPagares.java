package interfaces.pb12;

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

public class ATC_FT_001_PB12_ResguardoPagares extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PB12_ResguardoPagares_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbIas = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS, GlobalVariables.DB_PASSWORD_IAS);

		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
				
/** VARIABLES *********************************************************************/	

		String selectI = "select * from (SELECT B.ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA, B.STATUS " + 
				"FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_RPA C, POSUSER.POS_RPA_DETL D " + 
				"WHERE B.PE_ID = A.ID AND B.DOC_TYPE = 'RPA' " + 
				"AND B.STATUS  IN ( 'I' ) " + 
				"AND A.PV_CR_PLAZA =  '" + data.get("plaza") + "' AND A.PV_CR_TIENDA = '" + data.get("tienda") + "' " + 
				"AND C.PID_ID = B.ID AND D.PID_ID = C.PID_ID order by B.PARTITION_DATE DESC ) where rownum <= 5";

		String tdcIntegrationServerFormat = "select * from ( SELECT run_id,start_dt,status FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PB12%' ORDER BY START_DT DESC ) where rownum <= 5";

		String consultaError1 = "Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s'";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String selectE = "SELECT id,pe_id,pv_doc_name,doc_type,status,partition_date FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE ID = '%s' AND DOC_TYPE = 'RPA' AND STATUS = 'E' " + 
				" AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' and SUBSTR(PV_DOC_NAME,9,5) = '" + data.get("tienda") + "'";
		
		String validarXXRPA = "select * from (SELECT last_updated_date, nombre_archivo "
				+ "FROM XXRPA.XXRPA_PAGARE WHERE CR_PLAZA = '" + data.get("plaza") + "' AND CR_TIENDA = '" + data.get("tienda") + "' AND TRUNC(LAST_UPDATED_DATE) = TRUNC(SYSDATE) ) where rownum <= 5";

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		addStep("Que exista al menos un registro con STATUS = I y DOC_TYPE = RPA en las tablas POS_ENVELOPE, POS_INBOUND_DOCS y POS_RPA en la BD de POSUSER.");

		System.out.println(selectI);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(selectI);		
		String id = paso1_qry1_Result.getData(0, "ID");

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar con STATUS = I y DOC_TYPE = RPA.", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
	
		
		/* PASO 2 *********************************************************************/	

		addStep("Se invoca la interfaz PB12.Pub:run ejecutando el JOB runPB12.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));		
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

		if (!successRun) {
			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	

		
		
		/* PASO 3 *********************************************************************/	

		addStep("Validar que la interface se ejecuta con errores en WMLOG con estatus S.");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
		//assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		
		if (av2 == false) {
			testCase.addQueryEvidenceCurrentStep(is);
		} else {
			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
		

		
		/* PASO 4 *********************************************************************/	

		addStep("Se valida el STATUS = E en la tabla POS_INBOUND_DOCS con Tipo de documento RPA en la BD POSUSER.");

		String paso4 = String.format(selectE, id);
		System.out.println(paso4);
		SQLResult paso5_Result = dbPos.executeQuery(paso4);	

		boolean paso4_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso4_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso4_valida);
		assertFalse("No se encontraron registros STATUS = E en la tabla POS_INBOUND_DOCS", paso4_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 6 *********************************************************************/	

		addStep("Se valida que los datos se hayan insertado en la tabla XXRPA_PAGARE de la BD XXRPA.");

		System.out.println(validarXXRPA);
		SQLResult paso6_Result = dbIas.executeQuery(validarXXRPA);	

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
		return "ATC_FT_001_PB12_ResguardoPagares";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar que los datos se inserten para la plaza y tienda";
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
