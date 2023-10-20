package interfaces.pb7;

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

public class PB7_ProcesoNormal extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB7_001_Verifica_Proceso_Normal(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias 
		 ***********************************************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		
		
		String ValidarInsumo = "SELECT DISTINCT(SUBSTR(PV_DOC_NAME,4,5)) PV_PLAZA, SUBSTR (PV_DOC_NAME,9,5) PV_TIENDA, STATUS, DOC_TYPE, ID" + 
				" FROM POSUSER.POS_INBOUND_DOCS I, POSUSER.POS_FFC F, POSUSER.POS_FFC_DETL D" + 
				" WHERE I.STATUS = 'I' " + 
				" AND F.PID_ID = D.PID_ID " + 
				" AND I.ID = D.PID_ID" +
				" AND SUBSTR(PV_DOC_NAME,4,5) =" + "'"+ data.get("plaza") + "'" + 
				" AND I.DOC_TYPE = 'FFC' "
				+ " AND SUBSTR(PV_DOC_NAME,9,5)= " + "'"+ data.get("tienda") + "'";
		
		String tdcQueryIntegrationServer = "SELECT * FROM wmlog.wm_log_run" + 
				" WHERE interface = 'PB7'" + 
				" AND start_dt>=TRUNC(SYSDATE)" + 
				" ORDER BY start_dt DESC";
		
		
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA


		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";
		
		String validarPosRep = "SELECT DISTINCT F.PID_ID, SUBSTR(I.PV_DOC_NAME,4,5) PV_CR_PLAZA, SUBSTR(I.PV_DOC_NAME,9,5) PV_CR_TIENDA, F.PARTITION_DATE" + 
				" FROM POSUSER.POS_FFC_DETL D, POSUSER.POS_FFC F, POSUSER.POS_INBOUND_DOCS I" + 
				" WHERE F.PID_ID = D.PID_ID " + 
				" AND F.PID_ID = '%s' " + 
				" AND SUBSTR(PV_DOC_NAME,4,5)= " + "'"+ data.get("plaza") + "'"+ 
				" AND SUBSTR(PV_DOC_NAME,9,5)= " + "'"+ data.get("tienda") + "'";
		
		String validarStatusE = "SELECT DISTINCT(SUBSTR(PV_DOC_NAME,4,5)) PV_PLAZA, " + 
				" SUBSTR(PV_DOC_NAME,9,5) PV_TIENDA, ID, STATUS, TARGET_ID" + 
				" FROM POSUSER.POS_INBOUND_DOCS " + 
				" WHERE STATUS = 'E' " + 
				" AND ID = '%s'" + //ID O PID_ID 
				" AND SUBSTR(PV_DOC_NAME,4,5)= " + "'"+ data.get("plaza") + "'"+ 
				" AND SUBSTR(PV_DOC_NAME,9,5)= " + "'"+ data.get("tienda") + "'"+ 
				" AND DOC_TYPE = 'FFC' " + 
				" AND TARGET_ID =  '%s'";//RUN_ID
		
		
		
		// utileria
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				PakageManagment pok = new PakageManagment(u, testCase);
				String status = "S";

				
				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
				String con = "http://" + user + ":" + ps + "@" + server;
				String searchedStatus = "R";
				String run_id;
				/**
				 * 
				 * **********************************Pasos del caso de Prueba
				 * *****************************************
				 * 
				 * 
				 */

//				Paso 1	************************		
				
		addStep("Validar que exista información en la tabla POS_INBOUND_DOCS de POSUSER para la plaza y tienda con tipos de documento FFC y STATUS I.");
				
		SQLResult queryRegristro = executeQuery(dbPos, ValidarInsumo);
		String ID = queryRegristro.getData(0, "ID");
		boolean validarRegistro = queryRegristro.isEmpty();
		System.out.println(ValidarInsumo);
		
		if(!validarRegistro) {
			testCase.addQueryEvidenceCurrentStep(queryRegristro);
		}
		
		assertTrue(!validarRegistro, "No se encuentran registros");
		
				
//		Paso 2	************************	
		
		addStep("Invocar el servicio PB7.Pub:run mediante la ejecución del JOB PB7");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		
		
		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			

			u.hardWait(2);

		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();
			

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(result3);

			}
		}

		// Paso 3 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run deberá existir un registro con el campo status en \"S\".");
		
		
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String verificacionInterface = String.format(consulta5, run_id);
			SQLResult paso4 = executeQuery(dbLog, verificacionInterface);
			System.out.println(verificacionInterface);



			boolean av5 = paso4.isEmpty();
			
			if (!av5) {

				testCase.addQueryEvidenceCurrentStep(paso4);
				
			} 

			System.out.println(av5);

			
			assertFalse(av5, "No se obtiene informacion de la consulta");
//			

//		Paso 4  *************************************************
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTemp6 = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso5 = executeQuery(dbLog, consultaTemp6);
		
		System.out.println(consultaTemp6);
		String estatusThread = paso5.getData(0, "Status");

		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {

			testCase.addQueryEvidenceCurrentStep(paso5);
			
		} 

		System.out.println(SR);

		
		assertFalse(SR, "No se obtiene informacion de la consulta");
		
// 		Paso 5 **********************************************************
		
		addStep("Verificar que la informacion sea insertada en las tablas  POS_FFC y POS_FFC_DETL en POSREP");
		
		String consultaRep = String.format(validarPosRep, ID);
		SQLResult queryRegristroRep = executeQuery(dbPos, consultaRep);
		
		boolean validarRegistroRep = queryRegristroRep.isEmpty();
		
		if(!validarRegistroRep) {
			testCase.addQueryEvidenceCurrentStep(queryRegristroRep);
		}
		
		assertTrue(!validarRegistroRep, "No se encuentran registros");
		
// 		Paso 5 **********************************************************
		
		addStep("Validar que el estatus en la tabla POS_INBOUND_DOCS sea igual a 'E' para los registros procesados en la Base de Datos del POSUSER");
		
		String queryFormat = String.format(validarStatusE, ID, run_id);
		SQLResult queryEstatus= executeQuery(dbPos, queryFormat);
		
		boolean validarStatus = queryEstatus.isEmpty();
		
		if(!validarStatus) {
			testCase.addQueryEvidenceCurrentStep(queryEstatus);
		}
		
		assertTrue(!validarStatus, "No se encuentran registros");
		
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
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB7_001_Verifica_Proceso_Normal";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Ejecución de la interfaz para verificar el proceso normal de la plaza y tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}


	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
