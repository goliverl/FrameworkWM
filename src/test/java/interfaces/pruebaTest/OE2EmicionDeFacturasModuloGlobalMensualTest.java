package interfaces.pruebaTest;

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


public class OE2EmicionDeFacturasModuloGlobalMensualTest extends BaseExecution{
		
		
		@Test(dataProvider = "data-provider")
		public void test(HashMap<String, String> data) throws Exception {
			
		addStep("Actualizar el estado las facturas global mensual de ventas registradas en POS para la tienda 10CUE50HGK en la tabla XXFC_CFD_FACTURA_DIGITAL de Oracle a WM_STATUS = 'L",
					"Se actualiza el estado de las facturas global mensual a procesar a WM_STATUS = 'L' y el campo DOCTO_XML = NULL.");
	/* Utilerías *********************************************************************/
	        
			utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
					GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
			
			utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
					GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
			
			
			
	/**
	* Variables
	* ******************************************************************************************
	* * 
	* 
	*/
			
			String TdcQueryEstadoFacturas= "select ID_FACTURA_DIGITAL,ORIGEN,ANIO,MES,CUSTOMER_TRX_ID,CR_PLAZA,CR_TIENDA,SERIE"+
					" from XXFC.XXFC_CFD_FACTURA_DIGITAL" + 
					" WHERE ID_FACTURA_DIGITAL ='" + data.get("Factura")+"'"+
					" AND ORIGEN = 'POSMES'"+
					" AND CR_PLAZA = '" +data.get("Plaza")+"'";
					
			
			
			String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
					+ " WHERE interface = '" + data.get("Run_interface") + "'" + " and  start_dt >= TRUNC(SYSDATE)"
					+ " order by start_dt desc)" + " where rownum = 1";

			String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
					+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA

			String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
					+ " where RUN_ID=%s"; // FCWMLQA

			String tdcQueryStatusLog = "select * from (SELECT run_id,interface,start_dt,status,server "
					+ " FROM WMLOG.wm_log_run" + " WHERE interface = '" + data.get("Run_interface") + "'"
					+ " and status= 'S' " + " and start_dt >= trunc(sysdate) " // FCWMLQA
					+ " ORDER BY start_dt DESC)" + " where rownum = 1";

			String TdcQueryFacturasEmitidas= "select ID_FACTURA_DIGITAL,ORIGEN,ANIO,MES,CUSTOMER_TRX_ID,CR_PLAZA,CR_TIENDA,SERIE"+
					" from XXFC.XXFC_CFD_FACTURA_DIGITAL" + 
					" WHERE WM_RUN_ID = '%s'" +
					" AND ORIGEN = 'AR'" +
					" AND WM_STATUS = 'E'";	
			
			
	//utills 
			
			String status = "S";
			SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
			PakageManagment pok = new PakageManagment(u, testCase);
			String user = data.get("user");
			String ps = PasswordUtil.decryptPassword(data.get("ps"));
			String server = data.get("server");
			String con = "http://" + user + ":" + ps + "@" + server + ":5555";
			String searchedStatus = "R";
			String run_id;
			String id;
			String thread;

			/**
			 * 
			 * **********************************Pasos del caso de Prueba * *****************************************
			 * 
			 * 
			 */

	//Paso 1 

			System.out.println(GlobalVariables.DB_HOST_AVEBQA);
			System.out.println(TdcQueryEstadoFacturas);

			SQLResult estadoFacturas = executeQuery(dbEBS, TdcQueryEstadoFacturas);
			boolean bolFacturas = estadoFacturas.isEmpty();

			if (!bolFacturas) {

				testCase.addQueryEvidenceCurrentStep(estadoFacturas);
				System.out.println(":)");

			}

			/* Paso 3 */
			addStep("Ejecutar el servicio OE2.Pub:run");

			u.get(con);

			pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

			SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
			String status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
			while (valuesStatus) {

				query = executeQuery(dbLog, tdcQueryIntegrationServer);
				status1 = query.getData(0, "STATUS");
				run_id = query.getData(0, "RUN_ID");

				u.hardWait(2);

			}

			boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
			if (!successRun) {

				String error = String.format(tdcQueryErrorId, run_id);
				SQLResult paso3 = executeQuery(dbLog, error);

				boolean emptyError = paso3.isEmpty();

				if (!emptyError) {

					testCase.addTextEvidenceCurrentStep(
							"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

					testCase.addQueryEvidenceCurrentStep(paso3);

				}
			}

	//Paso 4	************************

			addStep("Comprobar que se registra la ejecucion en WMLOG");

			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
			System.out.println(queryStatusLog);
			SQLResult paso4 = executeQuery(dbLog, tdcQueryStatusLog);
			boolean ejecucion = paso4.isEmpty();
			if (!ejecucion) {

				testCase.addQueryEvidenceCurrentStep(paso4);
			}

			String fcwS = paso4.getData(0, "STATUS");
			boolean validateStatus = status.equals(fcwS);
			System.out.println(validateStatus);

			assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

	//Paso 5	************************

			addStep("Se valida la generacion de thread");

			String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
			System.out.println(tdcQueryStatusThread);
			SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
			thread = queryStatusThreadResult.getData(0, "THREAD_ID");
			boolean threadResult = queryStatusThreadResult.isEmpty();
			if (!threadResult) {

				testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			}

			String regPlazaTienda = queryStatusThreadResult.getData(0, "STATUS");
			boolean statusThread = status.equals(regPlazaTienda); //
			System.out.println(statusThread);
			if (!statusThread) {

				String error = String.format(tdcQueryErrorId, run_id);
				SQLResult errorResult = dbLog.executeQuery(error);

				boolean emptyError = errorResult.isEmpty();
				if (!emptyError) {

					testCase.addTextEvidenceCurrentStep(
							"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

					testCase.addQueryEvidenceCurrentStep(errorResult);

				}

			}
			assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");



			
	//Paso 	6	
	addStep("Validar la actualización del estatus (WM_STATUS = 'E'), el XML de la factura en el campo DOCTO_XML y los campos referentes a la factura digital en la tabla XXFC_CFD_FACTURA_DIGITAL de Oracle.", 
			"Se actualiza el estatus de las facturas procesadas a WM_STATUS = 'E' y el XML de la factura en el campo DOCTO_XML en la tabla XXFC_CFD_FACTURA_DIGITAL de Oracle.");		
			

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);	
		System.out.println(TdcQueryFacturasEmitidas);
		
		
		
		

		String GetTdcQueryFacturasEmitidas = String.format(TdcQueryFacturasEmitidas, thread);
		SQLResult Facturas = executeQuery(dbEBS, GetTdcQueryFacturasEmitidas);
		
		boolean boolFacturas = Facturas.isEmpty();
		
			if (!boolFacturas) {
				
				testCase.addQueryEvidenceCurrentStep(Facturas);
				System.out.println(":)");
				
			}
		
		
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
			return null;
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return null;
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

	}