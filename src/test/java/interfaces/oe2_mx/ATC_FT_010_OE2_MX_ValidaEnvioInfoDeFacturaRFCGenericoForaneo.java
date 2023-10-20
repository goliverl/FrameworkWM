package interfaces.oe2_mx;

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



		/************************************************************************************
		 * Automation 
		 * Interfaz OE2_MX
		 * MTC-FT-010- Validar el envio de informacion de factura con RFC Generico Foraneo
		 * 05/01/2023
		 * Gilberto Martinez
		 *************************************************************************************/
public class ATC_FT_010_OE2_MX_ValidaEnvioInfoDeFacturaRFCGenericoForaneo extends BaseExecution{

	/**
	 * la creacion de este script fue en base a las pruebas unitarias
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_OE2_MX_ValidaEnvioInfoDeFacturaRFCGenericoForaneo_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * *
		 * 
		 */

		String TdcQueryEstadoFacturas = "SELECT REFID,SERIE,FOLIO,UUID,FECHA_TIMBRE,TIPO_COMPROBANTE,WM_STATUS\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='L'\r\n"
				+ "AND ORIGEN ='AR'";
		
		String TdcQueryEstadoFacturas2 = "SELECT WM_RUN_ID,VERSION_CFDI,ORIGEN,ID_FACTURA_DIGITAL,REC_RFC,REC_NOMBRE\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='L'\r\n"
				+ "AND ORIGEN ='AR'";
		
		String TdcQueryEstadoFacturas3 = "SELECT REC_CODIGO_POSTAL,REC_REGIMEN_FISCAL,\r\n"
				+ "REGIMEN_TRIBUTARIO,LAST_UPDATE_DATE,EXPORTACION\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='L'\r\n"
				+ "AND ORIGEN ='AR'";

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
		


		String TdcQueryFacturasProcesadas = "SELECT WM_RUN_ID,WM_STATUS,REFID,FOLIO,FECHA_TIMBRE,TIPO_COMPROBANTE\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='E'\r\n"
				+ "AND ORIGEN ='AR'\r\n"
				+ "AND WM_RUN_ID = '%s'";
		
		String TdcQueryFacturasProcesadas2 = "SELECT VERSION_CFDI,ORIGEN,\r\n"
				+ "ID_FACTURA_DIGITAL,REC_RFC,REC_NOMBRE\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='E'\r\n"
				+ "AND ORIGEN ='AR'\r\n"
				+ "AND WM_RUN_ID = '%s'";
		
		String TdcQueryFacturasProcesadas3 = "SELECT SERIE,REC_REGIMEN_FISCAL,REGIMEN_TRIBUTARIO,LAST_UPDATE_DATE,EXPORTACION\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='E'\r\n"
				+ "AND ORIGEN ='AR'\r\n"
				+ "AND WM_RUN_ID = '%s'";

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
		String thread=null;

		/**
		 * 
		 * **********************************Pasos del caso de Prueba *
		 * *****************************************
		 * 
		 * 
		 */

//Paso 1 ***************************************************

		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		testCase.addBoldTextEvidenceCurrentStep("BD consultada: "+GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryEstadoFacturas);

		SQLResult estadoFacturas = executeQuery(dbAVEBQA, TdcQueryEstadoFacturas);
		SQLResult estadoFacturas2 = executeQuery(dbAVEBQA, TdcQueryEstadoFacturas2);
		SQLResult estadoFacturas3 = executeQuery(dbAVEBQA, TdcQueryEstadoFacturas3);
		
		boolean bolFacturas = estadoFacturas.isEmpty();
		
		if (!bolFacturas) {
			
			testCase.addTextEvidenceCurrentStep("Se encontro registros a procesar por la interfaz");
			
		}
		
		testCase.addQueryEvidenceCurrentStep(estadoFacturas);
		testCase.addQueryEvidenceCurrentStep(estadoFacturas2);
		testCase.addQueryEvidenceCurrentStep(estadoFacturas3);
		
		System.out.println(bolFacturas);
		assertFalse(bolFacturas, "No se encontraron registros a procesar por la interfaz");	
		
			
//Paso2 ***************************************************************

		addStep("Ejecucion de la interfaz", "La interfaz se ejecuto correctamente y el estatus es NORMAL");

		u.get(con);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			query = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso3 = executeQuery(dbLog, error);

			boolean emptyError = paso3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso3);

			}
		}

//Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG para validar el estatus de la ejecucion");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		testCase.addBoldTextEvidenceCurrentStep("BD consultada: "+GlobalVariables.DB_HOST_FCWMLQA);
		
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);
	
		SQLResult paso4 = executeQuery(dbLog, queryStatusLog);
		boolean ejecucion = paso4.isEmpty();
		if (!ejecucion) {

			testCase.addTextEvidenceCurrentStep("Se Encontraron registros en el log");
			
		}
		testCase.addQueryEvidenceCurrentStep(paso4);
		
		String fcwS = paso4.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");

//Paso 5	************************

		addStep("Se valida la generacion de thread y validar estatus");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		testCase.addBoldTextEvidenceCurrentStep("BD consultada: "+GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		String regPlazaTienda = null;
		
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
		
		boolean threadResult = queryStatusThreadResult.isEmpty();
		
		if (!threadResult) {
			thread = queryStatusThreadResult.getData(0, "THREAD_ID");
			regPlazaTienda = queryStatusThreadResult.getData(0, "STATUS");
			testCase.addTextEvidenceCurrentStep("Se encontraron registros de thread");
		}
		
		testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
		
		
		boolean statusThread = status.equals(regPlazaTienda); //
		System.out.println(statusThread);
		
		if (!statusThread) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult errorResult = dbLog.executeQuery(error);

			boolean emptyError = errorResult.isEmpty();
			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorResult);

			}

		}
		assertTrue(statusThread, "El status del thread no fue correcto");

//Paso 6 
		addStep("Ejecutamos el siguiente query en la BD AVEBQA para validar los registros procesados");
		
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		testCase.addBoldTextEvidenceCurrentStep("BD consultada: "+GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryEstadoFacturas);

		/*Dar formato a query*/
		
		String queryFacProc = String.format(TdcQueryFacturasProcesadas, thread);
		String queryFacProc2 = String.format(TdcQueryFacturasProcesadas2, thread);
		String queryFacProc3 = String.format(TdcQueryFacturasProcesadas3, thread);
		
		SQLResult FacturasProcesadas = executeQuery(dbAVEBQA, queryFacProc);
		SQLResult FacturasProcesadas2 = executeQuery(dbAVEBQA, queryFacProc2);
		SQLResult FacturasProcesadas3 = executeQuery(dbAVEBQA, queryFacProc3);
		
		boolean bolFacturasProc = FacturasProcesadas.isEmpty();
		
		if (!bolFacturasProc) {
			
			testCase.addTextEvidenceCurrentStep("Se encontraron registros procesados");
			
		}
		
		testCase.addQueryEvidenceCurrentStep(FacturasProcesadas);
		testCase.addQueryEvidenceCurrentStep(FacturasProcesadas2);
		testCase.addQueryEvidenceCurrentStep(FacturasProcesadas3);
		
		System.out.println(bolFacturasProc);
		assertFalse(bolFacturasProc, "No se encontraron registros procesados");	
		
		

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
		return "Realizar la ejecucion de la interfaz OE2_MX, provocando un error en la interfaz para que la factura se guarde y validemos que los datos enviados son los correctos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Gil";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_OE2_MX_ValidaEnvioInfoDeFacturaRFCGenericoForaneo_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
