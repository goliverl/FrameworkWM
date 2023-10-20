package interfaces.po11;

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
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PO11EnvioDePolizasContables extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PO11_Envio_Polizas_Contables(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
	/*	SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);
		*/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_FCEBSSIT, GlobalVariables.DB_USER_FCEBSSIT,	GlobalVariables.DB_PASSWORD_FCEBSSIT);
		
		
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryInfo = "SELECT B.ID DOC_ID, D.PID_ID, D.MOV_TYPE, D.MOV_ID_POS, B.DOC_TYPE, B.STATUS, A.PV_CR_PLAZA, A.PV_CR_TIENDA"
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_INBOUND_DOCS B, posuser.POS_DCI_DETL D "
				+ " WHERE A.ID = B.PE_ID"
				+ " AND B.ID = D.PID_ID "
				+ " AND B.DOC_TYPE = 'DCI'"
				+ " AND B.STATUS = 'I' "
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") +"'"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +"'"
				+ " AND MOV_TYPE = 'DEP'"
				+ " AND A.PARTITION_DATE >= SYSDATE-7";
		
		String tdcQueryDistrito = "select  oracle_cr_superior, oracle_cr, oracle_ef, oracle_ef_desc, legacy_cr, retek_cr, retek_distrito"
				+ " from XXFC.xxfc_maestro_de_crs_v"
				+ " where estado = 'A'"
				+ " and oracle_cr_superior = '%s'"
				+ " and oracle_cr = '%s'"
				+ " and oracle_cr_type = 'T'";
		
		String tdcQueryCuenta = "SELECT T.DESCRIPCION, T.CUENTA, T.AUXILIAR, T.CR_MAPEO, T.TIPO_CARGO, T.DISTRITO, T.CARD_TYPE"
				+ " FROM RMS100.FEM_POS_CUENTAS_TPE T "
				+ " WHERE PLAZA = '%s'"
				+ " AND MVTO = '%s'"
				+ " AND DISTRITO = '%s'"
				+ " AND CARD_TYPE = '%s'";
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PO11' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 
		
		String tdcQueryInterface = "SELECT status, date_created, segment3, attribute1, REFERENCE6"
				+ " FROM GL.GL_INTERFACE "
				+ " WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE)"
				+ " AND STATUS = 'NEW' "
				+ " AND SEGMENT3 = '%s'"
				+ " AND ATTRIBUTE1 = '%s'";
		
		String tdcQueryActualizar = "SELECT ID, STATUS, TARGET_ID "
				+ " FROM posuser.POS_INBOUND_DOCS "
				+ " WHERE STATUS = 'E' "
				+ " AND ID = %s "
				+ " AND TARGET_ID = '%s'";
		
		//Consultas de error 
		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + 
				"where RUN_ID='%s') "
				+ "where rownum <=1";// WMLOG
		String consulta61 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		
		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " + " FROM WMLOG.wm_log_run"
				+ " WHERE interface= 'PO11' "
				+ "and start_dt >= trunc(sysdate) "
				+ "AND  rownum <=1 " // FCWMLQA
				+ " ORDER BY start_dt DESC";

		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************	
		addStep("Validar que existe información  de los documentos a procesar para la tienda en la tabla POS_DCI_DETL de POSUSER.");
		
		System.out.println("Paso 1: "+ GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryInfo);
		
		SQLResult infoResult = executeQuery(dbPos, tdcQueryInfo);
		String tienda = "null";
		String plaza = "null";
		String mov = "null";
		String card = "null";
		String pid_id = "null";
		
		boolean info = infoResult.isEmpty();
		
		if (!info) {
			
			testCase.addQueryEvidenceCurrentStep(infoResult);
			 tienda = infoResult.getData(0, "PV_CR_TIENDA");
			 plaza = infoResult.getData(0, "PV_CR_PLAZA");
			 mov = infoResult.getData(0, "MOV_TYPE");
			 card = infoResult.getData(0, "MOV_ID_POS");
			pid_id = infoResult.getData(0, "PID_ID");
		} else {
			
			testCase.addQueryEvidenceCurrentStep(infoResult);
			testCase.addBoldTextEvidenceCurrentStep("No se encontro información  de los documentos a procesar para la tienda en la tabla POS_DCI_DETL de POSUSER.");
			
		}
		
		System.out.println(info);
		
		//assertFalse(info, "No se obtiene información de la consulta");
		
//paso 2 ******************************
		addStep("Validar que exista información de la tienda (RETEK_DISTRITO) en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN con ORACLE_CR_TYPE = 'T'.");
		
		System.out.println("Paso 2: "+ GlobalVariables.DB_HOST_Ebs);
		
		String distritoFormat = String.format(tdcQueryDistrito, plaza, tienda);
		SQLResult distritoResult = dbEbs.executeQuery(distritoFormat);
		String retek_distrito = "" ;
		System.out.println(distritoFormat);
		
		boolean distrito = distritoResult.isEmpty();
		
		if (!distrito) {
			
			testCase.addQueryEvidenceCurrentStep(distritoResult);
			retek_distrito = distritoResult.getData(0, "retek_distrito");
		}
          else {
			
			testCase.addQueryEvidenceCurrentStep(distritoResult);
			testCase.addBoldTextEvidenceCurrentStep("No se encontro información  de la tienda (RETEK_DISTRITO) en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN");
			
		}
		
		System.out.println(distrito);
		
		//assertFalse(distrito, "No se obtiene información de la consulta");

// paso 3 **************************
		addStep("Validar que exista información de la cuenta para la tienda a validar en la tabla RMS100.FEM_POS_CUENTAS_TPE T de RETEK.");

		System.out.println("Paso 3: "+ GlobalVariables.DB_HOST_RMS_MEX);
		
		String cuentaFormat = String.format(tdcQueryCuenta, plaza, mov, retek_distrito, card);
		
		SQLResult cuentaResult = dbRms.executeQuery(cuentaFormat);
		
		System.out.println(cuentaFormat);
		
		boolean cuenta = cuentaResult.isEmpty();
		
		if (!cuenta) {
			
			testCase.addQueryEvidenceCurrentStep(cuentaResult);
		}
        else {
			
			testCase.addQueryEvidenceCurrentStep(cuentaResult);
			testCase.addBoldTextEvidenceCurrentStep("No se encontro información de la cuenta para la tienda a validar en la tabla RMS100.FEM_POS_CUENTAS_TPE T de RETEK.");
			
		}
		
		System.out.println(cuenta);
		
		//assertFalse(cuenta, "No se obtiene información de la consulta");
		
// paso 4 *************************
		addStep("Ejecutar el servicio: PO11.Pub:run.");
		
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Paso 4: Respuesta dateExecution" + dateExecution);
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

			String error = String.format(consulta6, run_id);
			String error1 = String.format(consulta61, run_id);
			String error2 = String.format(consulta62, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucutar la interfaz en la tabla WM_LOG_ERROR");

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
		
// paso 5 *************************
		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG.");
		
		System.out.println("Paso 5: "+GlobalVariables.DB_HOST_FCWMLQA);
		
		String fcwS = "";
		
		SQLResult statusQueryExe = dbLog.executeQuery(tdcQueryStatusLog);
		
		boolean validacionfcwS = statusQueryExe.isEmpty();
		
		if (!validacionfcwS) {
			
			fcwS = statusQueryExe.getData(0, "STATUS");		
		}
			
			
		System.out.println(validacionfcwS);
		
		 
		boolean validateStatus = status.equals(fcwS);
		
		System.out.println(validateStatus);

		boolean validacionQueryStatus = statusQueryExe.isEmpty();
		
		if (!validacionQueryStatus) {
			
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
		}
          else {
			
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
			testCase.addBoldTextEvidenceCurrentStep("La ejecucion de la interface no fue exitosa");
			
		}
		System.out.println(validacionQueryStatus);

		//assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
		
//paso 6 **************************
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG.");
		
		System.out.println("Paso 6: "+GlobalVariables.DB_HOST_FCWMLQA);
		
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);

		String statusThread = "";

		boolean ST = statusThread.equals(status);
		
		ST = !ST;

		if (!ST) {

			 testCase.addQueryEvidenceCurrentStep(threadResult);
			 statusThread = threadResult.getData(0, "Status");
			    
			} else {
				
				testCase.addQueryEvidenceCurrentStep(threadResult);
				testCase.addBoldTextEvidenceCurrentStep("No se registraron threads ");
				
			}

		System.out.println(ST);

	//	assertFalse(ST, "No se obtiene informacion de la consulta");
			
//paso 7 ****************************
		addStep("Validar que la información se inserta correctamente en la tabla GL_INTERFACE de ORAFIN.");
		
		System.out.println("Paso 7: "+GlobalVariables.DB_HOST_AVEBQA);
		
		String interfaceFormat = String.format(tdcQueryInterface, plaza, tienda);
		
		SQLResult interfaceResult = dbEbs.executeQuery(interfaceFormat);
		
		String referencia6 = "null";
		
		System.out.println(interfaceFormat);
		
		boolean inter = interfaceResult.isEmpty();
		
		if (!inter) {
			
			testCase.addQueryEvidenceCurrentStep(interfaceResult);
			referencia6 = interfaceResult.getData(0, "REFERENCE6");
		}
		
	 else {
		
		testCase.addQueryEvidenceCurrentStep(interfaceResult);
		testCase.addBoldTextEvidenceCurrentStep("No se inserto información en la tabla GL_INTERFACE de ORAFIN. ");
		
	}

		
		System.out.println(inter);
		
	//	assertFalse(inter, "No se obtiene información de la consulta");
			
//paso 8 **************************
		addStep("Validar que se actualizaron correctamente los campos STATUS y TARGET_ID de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER.");
			
		System.out.println("Paso 8: "+ GlobalVariables.DB_HOST_Puser);
		
		String actualizarFormat = String.format(tdcQueryActualizar, pid_id, referencia6);
		
		SQLResult actualizarResult = dbPos.executeQuery(actualizarFormat);
		
		System.out.println(actualizarFormat);
		
		boolean actualizar = actualizarResult.isEmpty();
		
		if (!actualizar) {
			
			testCase.addQueryEvidenceCurrentStep(actualizarResult);
		}
		
		else {
			
			testCase.addQueryEvidenceCurrentStep(actualizarResult);
			testCase.addBoldTextEvidenceCurrentStep("No se actualizaron correctamente los campos STATUS y TARGET_ID de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER. ");
			
		}

		
		System.out.println(actualizar);
		
	//	assertFalse(actualizar, "No se obtiene información de la consulta");
			
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar el envío de información de pólizas contables de POSUSER a ORAFIN para la tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PO11_Envio_Polizas_Contables";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
