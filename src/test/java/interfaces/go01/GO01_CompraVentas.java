package interfaces.go01;

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

public class GO01_CompraVentas extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_GO01_CompraVentas(HashMap<String, String> data) throws Exception {

		/*
		 * UtilerÃƒÂ­as
		 ***********************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		
		String validaInsumo = "SELECT GID_ID, CR_PLAZA, FECHA_ACTUALIZACION, STATUS "
				+ " FROM POSUSER.GAS_TRANS_DESPACHO t, POSUSER.GAS_INBOUND_DOCS d" + 
				" WHERE t.GID_ID = d.ID" + 
				" AND d.DOC_TYPE = 'GTD'" + 
				" AND (d.STATUS = 'I' OR d.STATUS = 'R')" + 
				" AND CR_PLAZA ="+ "'"+ data.get("plaza") + "'" ;
		
		String tdcQueryIntegrationServer = "SELECT * FROM wmlog.wm_log_run" + 
				" WHERE interface = 'GO01'" + 
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
		
		String TransactionSTG="SELECT ID_TRANSACCION_ORACLE, CR_PLAZA, CR_ESTACION, ID_CLIENTE, FECHA_ACTUALIZACION"
				+ " FROM XXOG.XXOG_AR_TRANSACCIONES_STG" + 
				" WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE)" + 
				" AND TRUNC(FECHA_ACTUALIZACION) =  TRUNC(TO_DATE('%s','DD/MM/YYYY'))" + 
				" AND CR_PLAZA = "+ "'"+ data.get("plaza") + "'";

		String GasIDocs = "SELECT ID, DOC_TYPE, PV_DOC_NAME, TARGET_ID, PARTITION_DATE "
				+ " FROM POSUSER.GAS_INBOUND_DOCS" + 
				" WHERE DOC_TYPE = 'GTD'" + 
				" AND STATUS = 'L' "+ 
				" AND TRUNC(LAST_UPDATE_DATE) = TRUNC(SYSDATE)" + 
				" AND ID = '%s'";//GID_ID
		
		
	

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

//		Paso 1	************************		

		addStep("Validar que existan información pendiente por procesar en la tabla GAS_INBOUND_DOCS y GAS_TRANS_DESPACHO de POSUSER de tipo de documento DOC_TYPE = 'GTD' para la Plaza." );


		SQLResult registro = dbPos.executeQuery(validaInsumo);
		  
		String fechaAct = registro.getData(0, "FECHA_ACTUALIZACION");
		
		 String[] fechaCorrecta = fechaAct.split(" "); 
		 	String[] split = fechaCorrecta[0].split("-");
		 	fechaAct="";
		 	fechaAct= split[2] + "/"+  split[1] + "/"+ split[0];
		 	
		String GID_ID  = registro.getData(0, "GID_ID");
		System.out.println(registro);

		boolean existeRegistro = registro.isEmpty();
		if (!existeRegistro) {
			testCase.addQueryEvidenceCurrentStep(registro);
		}
		assertTrue(!existeRegistro, "No existen datos a procesar");
		

//		Paso 2	**************************************************************************************************************************************************

		addStep("Ejecutar el servicio GO01.Pub:run desde el Job runGO01 de Ctrl-M para procesar la información y de los movimientos de compras y ventas para dela plaza.");

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
		addStep("Validar la correcta ejecución de la interface GO01 en la tabla WM_LOG_RUN de WMLOG.");
		
		
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
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface GO01 en la tabla WM_LOG_THREAD de WMLOG.");
		
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
	
//		Paso 5  *************************************************
		addStep("Validar la inserción de las transacciones de la plaza en la tabla XXOG_AR_TRANSACCIONES_STG de ORAFIN.");
		
		String TransactionSTGFormat = String.format(TransactionSTG, fechaAct);
		System.out.println(TransactionSTGFormat);

		SQLResult QueryTransaction = executeQuery(dbEbs, TransactionSTGFormat);

		boolean TransactionEmpty = QueryTransaction.isEmpty();
		
		if(!TransactionEmpty) {
			testCase.addQueryEvidenceCurrentStep(QueryTransaction);
		}
	
		assertTrue(!TransactionEmpty, "No existen datos a procesar");

//		Paso 6  *************************************************
		addStep("Validar la actualización del estatus (STATUS = 'L'), en la tabla GAS_INBOUND_DOCS de POSUSER para los documentos procesados.");
		
		String GasIDocsFormat = String.format(GasIDocs, GID_ID);
		System.out.println(GasIDocsFormat);

		SQLResult QueryGasIDocs = executeQuery(dbPos, GasIDocsFormat);

		boolean GasIDocsEmpty = QueryGasIDocs.isEmpty();
		
		if(!GasIDocsEmpty) {
			testCase.addQueryEvidenceCurrentStep(QueryTransaction);
		}
	
		assertTrue(!GasIDocsEmpty, "No existen datos a procesar");


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
		return "ATC_FT_001_GO01_CompraVentas";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Enviar la información de los movimientos de las operaciones de compras y ventas de OXXO GAS a la base de ORAFIN para la Plaza correspondiente";
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

