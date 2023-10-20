package interfaces.ol10;

import static org.junit.Assert.assertFalse;
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

public class OL10_Reenvio  extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OL10_001_Reenvio(HashMap<String, String> data) throws Exception {
		/*
		 * Utiler?as
		 *********************************************************************/

		
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,
				GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		String consStatusError="SELECT SERVICIO, PLAZA, FECHA, LOTE, EXE_STATUS, EXE_HORA, comm.ATTRIBUTE6,comm.ATTRIBUTE7, " + 
				" comm.ATTRIBUTE8, comm.ATTRIBUTE9 " + 
				" FROM XXFC_SERVICES_VENDOR_COMM_DATA comm " + 
				" INNER JOIN xxfc.XXFC_LOTES lotes ON lotes.SERVICIO = comm.SERVICE_ID " + 
				" WHERE lotes.SERVICIO = comm.SERVICE_ID " + 
				" AND lotes.plaza = comm.CR_PLAZA " + 
				" AND Trunc(FECHA) = Trunc(SYSDATE) " + 
				" AND comm.SERVICE_TYPE = 'L' " + 
				" AND comm.ESTATUS_ENVIO_PROGRAMADO = 'A' " + 
				" AND EXE_HORA = ('" + data.get("hora") + "') " + 
				" AND HORARIO_DE_ENVIO =('" + data.get("hora") + "') " + 
				" AND EXE_STATUS = 'ERROR' " + 
				" AND ROWNUM <= 1 " + 
				" ORDER BY FECHA, servicio DESC";
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%OL10MultiExec%' "
				+ "ORDER BY START_DT DESC) where rownum <=3";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
        String loteok="SELECT * FROM xxfc.xxfc_lotes " + 
        		" WHERE servicio = '%s'" + 
        		" AND exe_status = 'OK' " + 
        		" AND fecha >= TRUNC(SYSDATE -30)";

 //Paso 1    ************************        
      		addStep("Validar que existe información con error pendiente de reprocesar");
      		SQLResult hora = dbEbs.executeQuery(consStatusError);
    		String id = hora.getData(0, "servicio");
    		//String plaza = hora.getData(0, "cr_plaza");
    		System.out.println("Respuesta " + id);
    		System.out.println("Respuesta " + hora);
    		boolean paso1 = hora.isEmpty();
    		if (!paso1) {
    			testCase.addQueryEvidenceCurrentStep(hora);
    		}
assertFalse("No hay insumos a procesar", paso1);

//Paso 2    ************************        
addStep("Ejecutar  el servicio de la interfaz OL10");
    				String status = "S";
    		//utileria
    				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
    				PakageManagment pok = new PakageManagment(u, testCase);

    				String user = data.get("user");
    				String ps = PasswordUtil.decryptPassword(data.get("ps"));
    				String server = data.get("server");
    				String searchedStatus = "R";

    				System.out.println(GlobalVariables.DB_HOST_LOG);
    				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
    				u.get(contra);
    				// String dateExecution = null;
    				// String dateExecution ="";
    				String boton1 = "1";
    				boolean boton = boton1.equals(data.get("boton"));
    				if (boton) {
    					String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
    					u.close();
    				}
    				if (!boton) {
    					String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
    				}

    				// String dateExecution = pok.runIntefaceWM(data.get("interfase"),
    				// data.get("servicio"),null);

    		//System.out.println("Respuesta dateExecution" + dateExecution);

    				SQLResult is = executeQuery(dbLOG, tdcIntegrationServerFormat);

    				String status1 = is.getData(0, "STATUS");
    				String run_id = is.getData(0, "RUN_ID");

    				boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
    				while (valuesStatus) {
    					// is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLOG,
    					// tdcIntegrationServerFormat, "STATUS", "RUN_ID");
    					is = executeQuery(dbLOG, tdcIntegrationServerFormat);

    					status1 = is.getData(0, "STATUS");
    					run_id = is.getData(0, "RUN_ID");
    					valuesStatus = status1.equals(searchedStatus);

    					u.hardWait(2);

    				}

    				boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
    				if (!successRun) {

    					String error = String.format(consultaERROR, run_id);
    					String error1 = String.format(consultaERROR2, run_id);
    					String error2 = String.format(consultaERROR3, run_id);

    					SQLResult errorr = dbLOG.executeQuery(error);
    					boolean emptyError = errorr.isEmpty();

    					if (!emptyError) {

    						testCase.addTextEvidenceCurrentStep(
    								"Se encontr? un error en la ejecuci?n de la interfaz en la tabla WM_LOG_ERROR");

    						testCase.addQueryEvidenceCurrentStep(errorr);

    					}

    					SQLResult errorIS = dbLOG.executeQuery(error1);
    					boolean emptyError1 = errorIS.isEmpty();
    					if (!emptyError1) {
    						testCase.addQueryEvidenceCurrentStep(errorIS);
    					}

    					SQLResult errorIS2 = dbLOG.executeQuery(error2);
    					boolean emptyError2 = errorIS2.isEmpty();

    					if (!emptyError2) {

    						testCase.addQueryEvidenceCurrentStep(errorIS2);

    					}

    				}

//Paso 3    ************************		
addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
    				SQLResult is1 = executeQuery(dbLOG, tdcIntegrationServerFormat);

    				String fcwS = is1.getData(0, "STATUS");
    				// String fcwS = SQLUtil.getColumn(testCase, dbLOG, tdcIntegrationServerFormat,
    				// "STATUS");
    				boolean validateStatus = fcwS.equals(status);
    				System.out.println(validateStatus);
    				assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");
    				SQLResult log = dbLOG.executeQuery(tdcIntegrationServerFormat);
    				System.out.println("Respuesta " + log);
    				// SQLResult errorIS= dbLOG.executeQuery(error1);

    				boolean log1 = log.isEmpty();
    				// boolean av2 = SQLUtil.isEmptyQuery(testCase, dbLOG,
    				// tdcIntegrationServerFormat);
    				if (!log1) {

    					testCase.addQueryEvidenceCurrentStep(log);
    				}

    				System.out.println(log1);
 assertFalse("r", log1);  
 //Paso 3 ************************
	addStep("Verificar que la tabla de lote sea actualizada correctamente con estatus OK.");

	String lot = String.format(loteok, id);
	SQLResult lotes1 = dbEbs.executeQuery(lot);
	System.out.println("Respuesta " + lot);

	boolean emptylot = lotes1.isEmpty();

	if (!emptylot) {

		testCase.addQueryEvidenceCurrentStep(lotes1);
		
	}
	assertFalse("", emptylot);
 		
}
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Ejecutar la interface OL10, Plazas Cln,Tij,Chi y Tienda 50CPR, X POST y HTTP, validar su ejec. WMLOG ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return  "ATC_FT_OL10_001_Reenvio";
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
	}}
