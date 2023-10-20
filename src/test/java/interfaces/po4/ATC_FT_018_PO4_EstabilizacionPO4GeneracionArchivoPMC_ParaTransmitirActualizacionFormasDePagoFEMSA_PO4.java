package interfaces.po4;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONArray;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import java.util.Date;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_018_PO4_EstabilizacionPO4GeneracionArchivoPMC_ParaTransmitirActualizacionFormasDePagoFEMSA_PO4 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_018_PO4_EstabilizacionPO4GeneracionArchivoPMC_ParaTransmitirActualizacionFormasDePagoFEMSA_PO4_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: BackOffice Oracle (Regresion Enero 2023)
		 * Caso de prueba: MTS-FT-018-CE PO4 Generación de archivo PMC para transmitir actualización 
		 * 							de formas de pago y cupones de punto de venta por medio de la FEMSA_PO4
		 * @author edwin.ramirez
		 * @date 2022/Dic/17
		 */
		
/**
 * Bases de Datos
 */
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, 
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, 
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 2
		String consultaTiendas = "SELECT S.STORE, S.* "
				+ "FROM RMS100.FEM_POS_CFG_STG_STORE S, RMS100.FEM_POS_CFG_STG C "
				+ "WHERE S.SOURCE_ID = C.SOURCE_ID "
				+ "AND S.POS_CONFIG_ID = C.POS_CONFIG_ID "
				+ "AND S.RECORD_TYPE = C.RECORD_TYPE "
				+ "AND S.WM_ID IS NULL "
				+ "AND C.COMPLETE_IND='Y' "
				+ "AND ROWNUM <= 2"
				+ "ORDER BY S.STORE DESC";
//Paso 3
		String detalleTiendas = "SELECT S.STORE, C.SOURCE_ID, C.POS_CONFIG_ID, C.RECORD_TYPE, C.COUPON_DESC,S.SEQ_NO, S.STATUS, I.ITEM\r\n"
				+ "FROM WMUSER.FEM_POS_CFG_STG C, WMUSER.FEM_POS_CFG_STG_STORE S,WMUSER.FEM_POS_CFG_STG_ITEM I\r\n"
				+ "WHERE C.SOURCE_ID = S.SOURCE_ID\r\n"
				+ "AND C.POS_CONFIG_ID = S.POS_CONFIG_ID\r\n"
				+ "AND C.RECORD_TYPE = S.RECORD_TYPE\r\n"
				+ "AND S.SOURCE_ID = I.SOURCE_ID(+)\r\n"
				+ "AND S.POS_CONFIG_ID = I.POS_CONFIG_ID(+)\r\n"
				+ "AND S.RECORD_TYPE = I.RECORD_TYPE(+)\r\n"
				+ "AND S.STORE = I.STORE(+)\r\n"
				+ "AND C.COMPLETE_IND='Y'\r\n"
				+ "AND S.WM_ID IS NULL\r\n"
				+ "AND S.STORE IN ('%s')\r\n"
				+ "AND ROWNUM <= 2\r\n"
				+ "ORDER BY S.STORE";
//Paso 4
		String consultaOracleCR = "SELECT  ORACLE_CR, \r\n"
				+ " ORACLE_CR_DESC, \r\n"
				+ " ORACLE_CR_SUPERIOR, \r\n"
				+ " ORACLE_EF, \r\n"
				+ " ORACLE_EF_DESC, \r\n"
				+ " ORACLE_CIA, \r\n"
				+ " ORACLE_CIA_DESC, \r\n"
				+ " LEGACY_EF, \r\n"
				+ " LEGACY_CR,\r\n"
				+ " RETEK_DISTRITO \r\n"
				+ "FROM  XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE  ESTADO = 'A'\r\n"
				+ "AND RETEK_CR IN ('%s')\r\n"
				+ "AND ORACLE_CR_TYPE = 'T'";
//Paso 5
		String  tdcIntegrationServerFormat = "SELECT * FROM (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
	      		"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PO4' \r\n"+
	      	    "AND STATUS = 'S' \r\n" +
	      		"ORDER BY START_DT DESC) \r\n"+
	            "WHERE ROWNUM <=2";
		// consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
				+ "from wmlog.WM_LOG_ERROR \r\n" + "where RUN_ID='%s') \r\n " + "where rownum <=1"; // dbLog
		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR \r\n"
				+ "where RUN_ID='%s') \r\n" + "WHERE rownum <= 1"; // dbLog
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
				+ "from wmlog.WM_LOG_ERROR \r\n" + "where RUN_ID='%s') \r\n" + "WHERE rownum <= 1"; // dbLog
//Paso 8
		String consultaInterfaz = "SELECT * FROM("
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER "
				+ "FROM WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE '%PO4%' "
				+ "ORDER BY START_DT DESC)"
				+ "WHERE ROWNUM <=1";
//Paso 9
		String consultaThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";
//Paso 10
		String consultaErrores = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE\r\n"
				+ "FROM WM_LOG_ERROR WHERE RUN_ID = '%s'";
//Paso 11
		String consultaArchivo = "SELECT S.STORE, S.*\r\n"
				+ "FROM RMS100.FEM_POS_CFG_STG_STORE S, RMS100.FEM_POS_CFG_STG C\r\n"
				+ "WHERE S.SOURCE_ID = C.SOURCE_ID\r\n"
				+ "AND S.POS_CONFIG_ID = C.POS_CONFIG_ID\r\n"
				+ "AND S.RECORD_TYPE = C.RECORD_TYPE\r\n"
				+ "AND S.WM_ID IS NULL\r\n"
				+ "AND C.COMPLETE_IND='Y'\r\n"
				+ "ORDER BY S.STORE";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//***************************************** Paso 1 y 2 ***************************************************************** 
		
		addStep("Consultar que haya tiendas que tengan cambios en el catalogo de formas de pago con la siguiente consulta");
		System.out.println("Paso 2: "+GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(consultaTiendas);
		
		SQLResult exe_consultaTiendas = executeQuery(dbFCRMSQA, consultaTiendas);
		String store = "";
		
		boolean validaConsultaTiendas = exe_consultaTiendas.isEmpty();
		
		if (!validaConsultaTiendas) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaTiendas);
			store = exe_consultaTiendas.getData(0, "STORE");
			System.out.println("Store: " + store);
		} 
		
		System.out.println(validaConsultaTiendas);
		assertFalse(validaConsultaTiendas, "No existe información pendiente de procesar en la tabla."); 
		
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Consultar el detalle de las tiendas disponibles con la siguiente consulta ");
		System.out.println("Paso 3: "+GlobalVariables.DB_HOST_RMS_MEX);
		String detalleTiendasFormat = String.format(detalleTiendas, store);
		System.out.println(detalleTiendasFormat);
		
		SQLResult exe_detalleTiendas = executeQuery(dbFCRMSQA,detalleTiendasFormat);

		boolean validaDetalleTiendas = exe_detalleTiendas.isEmpty();
		
		if(!validaDetalleTiendas) {
			testCase.addQueryEvidenceCurrentStep(exe_detalleTiendas);
		}
		
		System.out.println(validaDetalleTiendas);
		assertFalse(validaDetalleTiendas, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 4 *****************************************************************
		
		addStep("Comprobar que cada número de tienda obtenido en le paso anterior tenga un ORACLE_CR y un ORACLE_CR_SUPERIOR en la base de datos de RMS");
		System.out.println("Paso 4: "+GlobalVariables.DB_HOST_RMS_MEX);
		String consultaOracleCRFormat = String.format(consultaOracleCR, store);
		System.out.println(consultaOracleCRFormat);
		
		SQLResult exe_consultaOracleCR = executeQuery(dbFCRMSQA,consultaOracleCRFormat);
		
		boolean validaConsultaOracleCR = exe_consultaOracleCR.isEmpty();
		
		if(!validaConsultaOracleCR) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaOracleCR);
		}
		
		System.out.println(validaConsultaOracleCR);
		assertFalse(validaConsultaOracleCR, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 5 y 6 *****************************************************************
		
		addStep("Ejecutar el servicio PO4.Pub:run");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

				
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				PakageManagment pok = new PakageManagment(u, testCase);
				String status = "S";

				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
			
				String searchedStatus = "R";

				System.out.println(GlobalVariables.DB_HOST_LOG);
				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
				u.get(contra);

				String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));     
				System.out.println("Respuesta dateExecution" + dateExecution);

				SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
				String run_id = is.getData(0, "RUN_ID");
				String status1 = is.getData(0, "STATUS");// guarda el run id de la
															// ejecución

				boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																		// encuentra en
																		// estatus R
				while (valuesStatus) {

					status1 = is.getData(0, "STATUS");
					run_id = is.getData(0, "RUN_ID");
					valuesStatus = status1.equals(searchedStatus);

					u.hardWait(2);

				}

				boolean successRun = status1.equals(status);// Valida si se encuentra en
				
				System.out.println(successRun);
															// estatus S
				if (!successRun) {

					String error = String.format(consultaError1, run_id);
					String error1 = String.format(consultaError2, run_id);
					String error2 = String.format(consultaError3, run_id);

					SQLResult errorr = dbLog.executeQuery(error);
					boolean emptyError = errorr.isEmpty();

					if (!emptyError) {

						testCase.addTextEvidenceCurrentStep(
								"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

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
		
//***************************************** Paso 7 y 8 *****************************************************************
		
		addStep("Comprobar que la ejecución de la interface haya sido exitosa y quede con estatus = S");
		System.out.println("Paso 7 y 8: "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(consultaInterfaz);
		
		SQLResult exe_consultaInterfaz = executeQuery(dbFCWMLQA_WMLOG,consultaInterfaz);
		String statusInterfaz = "";
		String runID = "";
		
		
		boolean validaDetalleEjecucion = exe_consultaInterfaz.isEmpty();
		
		if(!validaDetalleEjecucion) {
			statusInterfaz = exe_consultaInterfaz.getData(0, "STATUS");
			runID = exe_consultaInterfaz.getData(0, "RUN_ID");
			System.out.println("El status es: "+statusInterfaz);
			System.out.println("Run ID: "+runID);
			testCase.addQueryEvidenceCurrentStep(exe_consultaInterfaz);
		}
		
		System.out.println(validaDetalleEjecucion);
		assertEquals(statusInterfaz, "S");
		
//***************************************** Paso 9 *****************************************************************
		
		addStep("Comprobar la ejecución correcta de los threads. ");
		System.out.println("Paso 9 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaThreadFormat = String.format(consultaThread,runID);
		System.out.println(consultaThreadFormat);
				
		SQLResult exe_consultaThread = executeQuery(dbFCWMLQA_WMLOG, consultaThreadFormat);
		
		boolean validaConsultaThread = exe_consultaThread.isEmpty();
				
		if (!validaConsultaThread) {
					
			testCase.addQueryEvidenceCurrentStep(exe_consultaThread);
		} 
				
		System.out.println(validaConsultaThread);
		assertFalse(validaConsultaThread, "No hay threads a procesar");
//***************************************** Paso 10 *****************************************************************
		
		addStep("Comprobar que No se hayan registrado errores en la ejecución de la interface en WMLOG ");
		System.out.println("Paso 10 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaErroresFormat = String.format(consultaErrores,runID);
		System.out.println(consultaErroresFormat);
		
		SQLResult exe_consultaErrores = executeQuery(dbFCWMLQA_WMLOG, consultaErroresFormat);
		
		boolean validaConsultaErrores = exe_consultaErrores.isEmpty();
		
		if (!validaConsultaErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaErrores);
		} 
		
		System.out.println(validaConsultaErrores);
		assertTrue(validaConsultaErrores, "Se encontraron logs de errores en la tabla");
	
//***************************************** Paso 11 *****************************************************************
		
		addStep("Consultar el nombre del archivo generado con la siguiente consulta:");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(consultaArchivo);
		
		SQLResult exe_consultaArchivo = executeQuery(dbFCRMSQA, consultaArchivo);
		
		boolean validaConsultaArchivo = exe_consultaArchivo.isEmpty();
		
		if (!validaConsultaArchivo) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaArchivo);
		} 
		
		System.out.println(validaConsultaArchivo);
		assertFalse(validaConsultaArchivo, "No existe información pendiente de procesar en la tabla.");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interface PO4 "
				+ "al generar el archivo PMC en los buzones de tiendas para actualizar formas de pago y cupones de "
				+ "punto de venta, al ser migrada la interface FEMSA_PO4 de  WM9.9 a WM10.5 ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_018_PO4_EstabilizacionPO4GeneracionArchivoPMC_ParaTransmitirActualizacionFormasDePagoFEMSA_PO4_test";
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