package interfaces.PR36_PER;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * Interfaces RB y BO internacional: MTC-FT-011 PR36 Generación de archivo ILS de catalogo de proveedores a traves de la interface FEMSA_PR36
 * Desc:
 * Prueba de regresion  para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PR36 
 * para generar archivos ILS (Catalogo de proveedores) de bajada ( de RMS a WM OUTBOUND) con la información actualizada o nueva de Proveedor Primario de RMS PERU , al ser migrada la interface de WM9.9 a WM10.5
 * @author Roberto Flores
 * @date   2022/07/07
 * 
 * Mtto:
 * @author Jose Onofre
 * @date 02/23/2023
 */
public class ATC_FT_001_PR36_PER_WMx86_GeneracionArchivoILS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR36_PER_WMx86_GeneracionArchivoILS_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con acceso a las bases de datos de FCWM6QA, FCWML6QA y de RMS_PERU.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para PR36_PERU de WM10.\r\n"
				+ "*Contar con acceso a repositorio de buzon de la tienda.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
				+ "");
		
		/*
		 * Utilerias
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil FCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_QAVIEW_Peru, GlobalVariables.DB_USER_RMS_QAVIEW_Peru,GlobalVariables.DB_PASSWORD_RMS_QAVIEW_Peru);
		SQLUtil FCWM6QA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryRegistrosProcesar = "select LOCATION, SUBSTR(store_name10,1,5) PLAZA from ( SELECT DISTINCT TO_NUMBER(LOCATION) LOCATION \r\n"
				+ "FROM  (SELECT DISTINCT LOCATION \r\n"
				+ "FROM repl_item_loc ril, item_loc_traits ilt, sups s \r\n"
				+ "WHERE source_wh IS NULL \r\n"
				+ "AND ilt.item (+) = ril.item \r\n"
				+ "AND ilt.loc (+) = ril.LOCATION \r\n"
				+ "AND ril.primary_repl_supplier = s.supplier \r\n"
				+ "AND (ril.last_update_datetime BETWEEN TO_DATE('01/01/2021','dd/mm/yyyy')  AND TO_DATE('06/06/2021','dd/mm/yyyy')) \r\n"
				+ "AND ril.loc_type='S') ) a, store b \r\n"
				+ "where a.LOCATION=b.STORE \r\n"
				+ "AND A.LOCATION IN ('26846','1610','15820','168','515')\r\n"
				+ "AND SUBSTR(store_name10,1,5) = '"+data.get("CR_PLAZA")+"' \r\n"
				+ "order by PLAZA";
		
		String qryRegistrosProcesarLocation = "SELECT DISTINCT ril.item, primary_repl_supplier, pct_tolerance, \n"
				+ "                        'S' loc_type, dept, CLASS, subclass, sup_name "
				+ "                   FROM repl_item_loc ril, \r\n"
				+ "                        item_loc_traits ilt, \r\n"
				+ "                        sups s, \r\n"
				+ "                        sup_traits st, \r\n"
				+ "                        sup_traits_matrix stm, \r\n"
				+ "                        xxfc_parametros_envio_cent pec,\r\n"
				+ "                       xxfc_venta_perdida_bi vp \r\n"
				+ "                  WHERE source_wh IS NULL \r\n"
				+ "                    AND ilt.item(+) = ril.item \r\n"
				+ "                    AND ilt.loc(+) = ril.LOCATION \r\n"
				+ "                    AND ril.LOCATION IN (%s) \r\n"
				+ "                    AND ril.primary_repl_supplier = s.supplier \r\n"
				+ "                    AND ril.primary_repl_supplier = stm.supplier(+) \r\n"
				+ "                    AND stm.sup_trait = st.sup_trait(+) \r\n"
				+ "                    AND vp.item(+) = ril.item \r\n"
				+ "                    AND vp.location(+) = ril.location \r\n"
				+ "                    AND vp.supplier(+) = ril.primary_repl_supplier \r\n"
				+ "                    AND (   ril.last_update_datetime > TO_DATE('01/06/2021','dd/mm/yyyy')\r\n"
				+ "                         OR ilt.last_update_datetime > TO_DATE('01/06/2021','dd/mm/yyyy')\r\n"
				+ "                         OR pec.last_update_datetime > TO_DATE('01/05/2020','dd/mm/yyyy')\r\n"
				+ "                        ) \r\n"
				+ "                    AND ril.primary_repl_supplier = pec.supplier(+) \r\n"
				+ "                    AND pec.LOCATION(+) = ril.LOCATION \r\n"
				+ "                    AND pec.item(+) = ril.item";
		
		String qryWmLogRun = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE LIKE '%s' AND STATUS = 'S' \r\n"
				+ "AND START_DT >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss') \r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryThreads = "SELECT THREAD_ID, PARENT_ID, NAME, START_DT, END_DT,STATUS \r\n"
				+ "FROM WMLOG.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = '%s' \r\n"
				+ "AND status = 'S'";
		
		String qryError = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY, ERROR_TYPE \r\n"
				+ "FROM WMLOG.WM_LOG_ERROR \r\n"
				+ "WHERE RUN_ID = '%s'";
		
		String qryOutboundDocs = "SELECT ID, DOC_NAME, DOC_TYPE, SENT_DATE, STATUS, SOURCE_ID \r\n"
				+ "FROM wmview.POS_OUTBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE = 'ILS' \r\n"
				+ "AND PV_CR_PLAZA = '"+data.get("CR_PLAZA")+"'\r\n"
				+ "AND SENT_DATE >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer conexión a la BD de RMS Perú **BDPRMSQ**..");
				
				testCase.addTextEvidenceCurrentStep("Conexion: BDPRMSQ");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_QAVIEW_Peru);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar que se tenga información de la plaza a procesar:");
				
				System.out.println("qryRegistrosProcesar: \r\n "+ qryRegistrosProcesar);
				SQLResult qryRegistrosProcesar_r = executeQuery(FCRMSQA, qryRegistrosProcesar);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
				
				assertFalse(qryRegistrosProcesar_r.isEmpty());
				
				String locations = "";
				for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
					locations += "'" + qryRegistrosProcesar_r.getData(i, "LOCATION") + "'";
					if (i < qryRegistrosProcesar_r.getRowCount() - 1) {
						locations += ",";
					}
				}
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Validar que haya informacion a procesar para el location configurado.");
				
				String qryRegistrosProcesarLocation_f = String.format(qryRegistrosProcesarLocation, locations);
				System.out.println("qryRegistrosProcesarLocation_f: \r\n "+ qryRegistrosProcesarLocation_f);
				
				SQLResult qryRegistrosProcesarLocation_r = executeQuery(FCRMSQA, qryRegistrosProcesarLocation_f);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesarLocation_r, true);
//				
				assertFalse(qryRegistrosProcesarLocation_r.isEmpty());
				
//Inicio control-m
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Ejecución control-M");
		
				fechaEjecucionInicio = new Date();
		
				// Se obtiene la cadena de texto del data provider en la columna "jobs"
				// Se asigna a un array para poder manejarlo
				JSONArray array = new JSONArray(data.get("cm_jobs"));

				testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
				SeleniumUtil u = new SeleniumUtil(new ChromeTest());
				Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));
		
				testCase.addTextEvidenceCurrentStep("Login");
				addStep("Login");
				u.get(data.get("cm_server"));
				u.hardWait(40);
				u.waitForLoadPage();
				cm.logOn();
				
				testCase.addTextEvidenceCurrentStep("Inicio de job");
				JobManagement j = new JobManagement(u, testCase, array);
				String resultadoEjecucion = j.jobRunner();
			
		
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
//Fin control-m
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion a la BD de WM Perú **OIWMQA** con usuario WMLOG.");
				
		testCase.addTextEvidenceCurrentStep("Conexion: OIWMQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_Oiwmqa);
				
		
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecucion de la interface en la tabla WM_LOG_RUN de la DB  OIWMQA con esquema WMLOG.");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%PR36%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecucion de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG");
				
				String qryThreads_f = String.format(qryThreads, runId);
				System.out.println("qryThreads_f: \r\n "+ qryThreads_f);
				
				SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);
				
				assertFalse(qryThreads_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep(" Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecucion de la interfaz  dentro de la tabla WM_LOG_ERROR:");
				
				String qryError_f = String.format(qryError, runId);
				System.out.println("qryError_f: \r\n "+ qryError);
				
				SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
				testCase.addQueryEvidenceCurrentStep(qryError_r, true);
				
				assertTrue(qryError_r.isEmpty());
				
		String wmDocNameAsn = qryWmLogRun_r.getData(0, "WM_DOC_NAME_ASN");
			
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Establecer conexión con la BD de WM Perú **OIWMQA** con usuario POSUSER.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: OIWMQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_Oiwmqa);
				
				
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el registro del archivo ILS generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
				String qryOutboundDocs_f = String.format(qryOutboundDocs, fechaEjecucionInicio);
				System.out.println("qryOutboundDocs_f: \r\n "+ qryOutboundDocs_f);
				
				SQLResult qryOutboundDocs_r = executeQuery(FCWM6QA, qryOutboundDocs_f);
				testCase.addQueryEvidenceCurrentStep(qryOutboundDocs_r, true);
				
				assertFalse(qryOutboundDocs_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexión con el servidor FTP de Buzones de tienda.");
		
				testCase.addTextEvidenceCurrentStep("FTP_HOST: " + data.get("FTP_HOST") +
						"\nFTP_PORT: " + data.get("FTP_PORT") +
						"\nFTP_USER: " + data.get("FTP_USER"));
				
				FTPUtil ftp = new FTPUtil(
						data.get("FTP_HOST"),
						Integer.parseInt(data.get("FTP_PORT")),
						data.get("FTP_USER"),
						data.get("FTP_PASSWORD"));
				
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Validar que se realice el envío del archivo ILS generado por la interface en el directorio configurado para la tienda procesada");
				
		
				String path = "/u01/posuser/FEMSA_OXXO/POS/"+data.get("CR_PLAZA")+"/"+data.get("CR_TIENDA")+"/working" + "/" +wmDocNameAsn;
		
				if (ftp.fileExists(path)) {
					System.out.println(path + " - Existe");
					testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
				} else {
					System.out.println(path + " - No xiste");
					testCase.addTextEvidenceCurrentStep("Error - no se encontró archivo: " + path);
				}
				
				assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
		

	}
	
	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-011 PR36 Generacion de archivo ILS de catalogo de proveedores a traves de la interface FEMSA_PR36_PER";
	}

	@Override
	public String setTestDesigner() {
		return "Equipo automatizacion";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}
	
}