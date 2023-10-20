package interfaces.pb1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class ATC_FT_001_PB1_Validar_Info_Tickets extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PB1_Validar_Info_Tickets_test(HashMap<String, String> data) throws Exception {
		
		
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbIas = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS, GlobalVariables.DB_PASSWORD_IAS);
		//utils.sql.SQLUtil dbBi = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI, GlobalVariables.DB_USER_BI,GlobalVariables.DB_PASSWORD_BI);

		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		
				
/** VARIABLES *********************************************************************/	
		
		String selectPlazas = "SELECT * FROM POSUSER.PLAZAS WHERE FCH_ARRANQUE <= SYSDATE AND FCH_ARRANQUE IS NOT NULL"
				+ " AND CR_PLAZA= '" + data.get("plaza") + "'"
				; //POSUSER
		
		String selectI = "SELECT  B.ID, SUBSTR(PV_DOC_NAME,4,5) PLAZA, RECEIVED_DATE, B.STATUS, B.DOC_TYPE " + 
				" FROM POSUSER.POS_TIC_DETL A, POSUSER.POS_INBOUND_DOCS B " + 
				" WHERE A.PID_ID = B.ID AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' and SUBSTR(PV_DOC_NAME,9,5) = '" + data.get("tienda") + "'" + 
				" AND RECEIVED_DATE BETWEEN TRUNC(SYSDATE-30) AND TRUNC(SYSDATE)" + 
				" AND B.STATUS = 'I' AND B.DOC_TYPE = 'TIC'" +
				" AND rownum <= 5";

		String tdcIntegrationServerFormat = "select * from ( SELECT run_id,start_dt,status FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PB1main%' ORDER BY START_DT DESC ) where rownum <= 5";

		String consultaError1 = "Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s'";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaThreads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  AND ATT1 = '" + data.get("plaza") + "' ORDER BY THREAD_ID DESC"; // FCWMLQA 
 
		String consultaThreads2 = "SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  AND ATT1 = '" + data.get("plaza") + "' ORDER BY THREAD_ID DESC"; // FCWMLQA 
		
		String selectE = "SELECT id,pe_id,pv_doc_name,doc_type,status,partition_date FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE ID = '%s' AND DOC_TYPE = 'TIC' AND STATUS = 'E' " + 
				" AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' and SUBSTR(PV_DOC_NAME,9,5) = '" + data.get("tienda") + "'";
		
		String selectE2 = "SELECT id,pe_id,pv_doc_name,doc_type,status,partition_date FROM POSUSER.POS_INBOUND_DOCS " + 
				" WHERE DOC_TYPE = 'TIC' 	AND STATUS = 'E' AND RECEIVED_DATE BETWEEN TRUNC(SYSDATE-30) AND SYSDATE " +
				" AND SUBSTR(PV_DOC_NAME,4,5) =  '" + data.get("plaza") + "' AND TARGET_ID = '%s'	AND ID = '%s'";
		
		String selectTempTicket= "SELECT * FROM TEMP_TICKET WHERE TRUNC(FECHA_CARGA) = TRUNC(SYSDATE) AND VT_CR_PLAZ = '" + data.get("plaza") + "'";


/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
			
		/* PASO 1 *********************************************************************/	

		addStep("Que exista al menos un registro en la tabla PLAZAS en POSUSER, con FCH_ARRANQUE menor a la fecha actual, para la plaza indicada.");

		System.out.println(selectPlazas);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(selectPlazas);		
		//String id = paso1_qry1_Result.getData(0, "ID");

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar con STATUS = I y DOC_TYPE = RPA.", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		
		/* PASO 2 *********************************************************************/	

		addStep("Que existan registros en la tabla POS_INBOUND_DOCS y en POS_TIC_DETL con STATUS = I y DOC_TYPE = TIC en la BD POSUSER.");

		System.out.println(selectI);
		SQLResult paso_qry_result = dbPos.executeQuery(selectI);		
		String id = paso_qry_result.getData(0, "ID");

		boolean paso_qry_valida = paso_qry_result.isEmpty(); // checa que el string contenga datos

		if (!paso_qry_valida) {
			testCase.addQueryEvidenceCurrentStep(paso_qry_result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso_qry_valida);
		assertFalse("No se encontraron registros a procesar con STATUS = I y DOC_TYPE = RPA.", paso_qry_valida); // Si esta vacio, imprime mensaje
			
		
		/* PASO 3 *********************************************************************/	

		addStep("Invocar el servicio FEMSA_PB1.Pub:run mediante la ejecución del JOB PB1.");

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
		
		
		/* PASO 4 *********************************************************************/		    
	    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		//assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
		

		/* PASO 5 *********************************************************************/
		
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");		
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);		
		String consultaTemp6 = String.format(consultaThreads, run_id);	
		System.out.println(consultaTemp6);		
		SQLResult paso6 = executeQuery(dbLog, consultaTemp6);	
		
		boolean validacion6 = paso6.isEmpty();

		if (!validacion6) {
			String estatusThread = paso6.getData(0, "Status");			
			validacion6 = estatusThread.equals(status);
			System.out.println(validacion6);
			testCase.addQueryEvidenceCurrentStep(paso6);
			
			String consultaTemp6pt2 = String.format(consultaThreads2, run_id);	
			paso6 = executeQuery(dbLog, consultaTemp6pt2);	
			testCase.addQueryEvidenceCurrentStep(paso6);

			validacion6 = !validacion6;		//Como se reutiliza el mismo booleano que el isEmpty lo regreso al mismo estado que estaba antes del if
		}					
		
		//assertFalse(validacion6, "No se obtiene informacion o Thread con STATUS diferente a 'S' de la consulta en la tabla WM_LOG_THREAD");
		

		
		/* PASO 6 *********************************************************************/	

		addStep("Se valida el STATUS = 'E' en la tabla POS_INBOUND_DOCS y que el TARGET_ID sea igual a el THREAD_ID de la tabla WM_LOG_THREAD de la BD WMLOG.");

		String paso4 = String.format(selectE, id);
		System.out.println(paso4);
		SQLResult paso5_Result = dbPos.executeQuery(paso4);	

		boolean paso4_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso4_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso4_valida);
		assertFalse("No se encontraron registros STATUS = E en la tabla POS_INBOUND_DOCS", paso4_valida); // Si esta vacio, imprime mensaje
		
		
		
		/* PASO 7 *********************************************************************/	

//		addStep("Se valida que los datos se hayan insertado en la tabla TEMP_TICKET de la BD BI");
//
//		System.out.println(selectTempTicket);
//		SQLResult paso6_Result = dbIas.executeQuery(selectTempTicket);	
//
//		boolean paso6_valida = paso6_Result.isEmpty(); // checa que el string contenga datos
//
//		if (!paso6_valida) {
//			testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
//		}
//
//		System.out.println(paso6_valida);
//		assertFalse("No se encontraron registros ", paso6_valida); // Si esta vacio, imprime mensaje	

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Esta interface permite consultar y procesar la información de los tickets (TIC) de las tiendas almacenados en el sistema del POS, y enviarla al sistema de BI.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}