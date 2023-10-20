package interfaces.ro8col;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.validation.constraints.AssertTrue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 *  OCO21021 - Mejoras Administrativo: MTC-FT-010 Generar Polizas Transferencias CEDIS – Tienda - Articulos MIXTOS CON y SIN ICO / ADVALOREN
 * Desc:
 * Se requiere actualizar las lineas del Costo a la Poliza Transferencias CEDIS - Tienda. 
 * Para que consideren el ICO y el Advalorem para los items de las diviciones: 26 - Cerveza, 27 - Vinos y licores, 39 - Cigarros 
 * y a su vez se consideren articulos sin ICO y sin ADVALOREN
 * @author Roberto Flores
 * @date   2022/07/18
 */
public class ATC_FT_009_RO08_MejorasAdm_GenPolizasTransferencias  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_RO08_MejorasAdm_GenPolizasTransferencias_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("OCO21021 - Mejoras Administrativo"); 
		testCase.setPrerequisites("");
		testCase.setTest_Description(data.get("name") + " - " + data.get("desc"));
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		SQLUtil FCWMLQA_WMLOG_COL = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL,GlobalVariables.DB_USER_FCWMLQA_WMLOG_COL, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG_COL);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		
		/*
		 * Querys
		 *********************************************************************/
		String qryRegistrosProcesar = "SELECT id, division, reference_9, reference_3, tran_date, CR_PLAZA, tran_code FROM fem_fif_stg\r\n"
				+ " 	  WHERE     CR_PLAZA = '"+data.get("CR_PLAZA")+"'\r\n"
				+ "       AND division in (26,27,39)\r\n"
				+ "       AND tran_date >= TRUNC(SYSDATE-60)\r\n"
				+ "       AND reference_3 IS NULL\r\n"
				+ "       AND reference_9 IS NULL ";
	
		
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryActualizacionJournal = "SELECT * FROM wmuser.WM_GL_HEADERS_COL\r\n"
				+ "WHERE TRAN_CODE = 1\r\n"
				+ "AND  header_id=%s"
				+ "AND JOURNAL_ID is not null\r\n"
				+ "AND JOURNAL_TYPE_ID is not null\r\n"
				+ "AND CR_PLAZA = '"+data.get("CR_PLAZA")+"'";
		
		String qryGlLines = "SELECT * FROM wmuser.wm_gl_lines_col \r\n"
				+ "WHERE header_id = %s";
		
		String qryActualizacionFemFifStg = "SELECT id, reference_9, reference_3, tran_date, CR_PLAZA, tran_code\r\n"
				+ "FROM fem_fif_stg \r\n"
				+ "WHERE id in (%s)\r\n"
				+ "and reference_3 is not null\r\n"
				+ "and reference_9 is not null";
		
		String qryGlInterfaceGeneral = "\r\n"
				+ "SELECT reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "FROM GL_INTERFACE \r\n"
				+ "WHERE reference6 in (%s)";
		
		String qryGlInterface = "select * from gl_interface \r\n"
				+ "where  segment2 = '"+data.get("CR_PLAZA")+"' \r\n"
				+ "and segment4 = %s \r\n"
				+ "and ENTERED_DR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 in (%s)";
		
		Date fechaEjecucionInicio;
		testCase.setTest_Description(data.get("name") + " - " + data.get("desc"));
		testCase.setProject_Name("Mejoras administrativas");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer conexión a la BD DB_HOST_RMS_COL.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: DB_HOST_RMS_COL");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_COL);
		
		
		/****************************************************************************************************************************************
		 * Paso 1.1
		 * **************************************************************************************************************************************/
		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG.");
				
				System.out.println("qryRegistrosProcesar: \r\n "+ qryRegistrosProcesar);
				SQLResult qryRegistrosProcesar_r = executeQuery(dbRms, qryRegistrosProcesar);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
				
				assertFalse(qryRegistrosProcesar_r.isEmpty());
				
				List<String> idsFemFifStgList = new ArrayList<>();
				for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
					idsFemFifStgList.add(qryRegistrosProcesar_r.getData(i, "ID"));
				}
				
				
		
////Inicio control-m
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 2
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
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
				
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
////Fin control-m
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexión a la FCWML6QA");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL);
				
		
		/****************************************************************************************************************************************
		 * Paso 3.1
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de la DB  FCWML6QA");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%RO08_COL%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG_COL, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
		
			
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");
				
				String referencia3 = "";
				String referencia9 = "";
		
				for (String id : idsFemFifStgList) {
					String qryActualizacionFemFifStg_f = String.format(qryActualizacionFemFifStg, id);
					System.out.println("qryActualizacionFemFifStg_f: \r\n "+ qryActualizacionFemFifStg_f);
					
					SQLResult qryActualizacionFemFifStg_r = executeQuery(dbRms, qryActualizacionFemFifStg_f);
					testCase.addQueryEvidenceCurrentStep(qryActualizacionFemFifStg_r, true);
					
					assertFalse(qryActualizacionFemFifStg_r.isEmpty());
					
					referencia3 = qryActualizacionFemFifStg_r.getData(0, "REFERENCE_3");
					referencia9 = qryActualizacionFemFifStg_r.getData(0, "REFERENCE_9");
				}
				
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE.");
				
				String qryGlInterfaceGeneral_f = String.format(qryGlInterfaceGeneral, referencia3);
				System.out.println("qqryGlInterfaceGeneral_f: \r\n "+ qryGlInterfaceGeneral_f);
				
				SQLResult qryGlInterfaceGeneral_r = executeQuery(dbEbs, qryGlInterfaceGeneral_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterfaceGeneral_r, true);
				
				assertFalse(qryGlInterfaceGeneral_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL.");
				
				String qryActualizacionJournal_f = String.format(qryActualizacionJournal, referencia9);
				System.out.println("qryActualizacionJournal_f: \r\n "+ qryActualizacionJournal_f);
				
				SQLResult qryActualizacionJournal_r = executeQuery(dbRms, qryActualizacionJournal_f);
				testCase.addQueryEvidenceCurrentStep(qryActualizacionJournal_r, true);
				
				assertFalse(qryActualizacionJournal_r.isEmpty());
				
				String headerId = qryActualizacionJournal_r.getData(0, "HEADER_ID");
				
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Inserción de líneas en la tabla WM_GL_LINES_COL");
				
				String qryGlLines_f = String.format(qryGlLines, headerId);
				System.out.println("qryGlLines_f: \r\n "+ qryGlLines_f);
				
				SQLResult qryGlLines_r = executeQuery(dbRms, qryGlLines_f);
				testCase.addQueryEvidenceCurrentStep(qryGlLines_r, true);
				
				assertFalse(qryGlLines_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) IVA 19%\" en Poliza Salida de Tienda: Tasa 5% y Tasa 19%");
				
				String segmento4 = "1380950012";
				String referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS) IVA 19%";
				
				String qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				SQLResult qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
			
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) IVA 5%\" en Poliza Salida de Tienda: Tasa 5% y Tasa 19%");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS) IVA 5%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
	 
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) IVA 5%\" en Poliza Salida de Tienda: Tasa 5% y Tasa 19%");
				
				segmento4 = "1435010064";
				referencia10 = "Transf. CEDIS - TIENDA Tasa 5%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el credito la sumatoria del  movimiento \"Transf. CEDIS - Tiendas Tarifa General 19%\" en Poliza Salida de Tienda: Tasa 5% y Tasa 19%");
				
				segmento4 = "1435010065";
				referencia10 = "Transf. CEDIS - Tiendas Tarifa General 19%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) \" en Poliza Salida de Tienda: Tasa EXCLUD");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS)";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"Transf. CEDIS - Tienda tasa 0\" en Poliza Salida de Tienda: Tasa EXCLUD");
				
				segmento4 = "1435010063";
				referencia10 = "Transf. CEDIS - Tienda tasa 0";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 14
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) \" en Poliza Salida de Tienda: Tasa ICO 8%");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS)";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 15
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el credito, la sumatoria del  movimiento \"Transf. CEDIS - Tiendas Tarifa general 19%\" en Poliza Salida de Tienda: Tasa ICO 8%");
				
				segmento4 = "1435010065";
				referencia10 = "Transf. CEDIS - Tiendas Tarifa general 19%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 16
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el credito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) IVA 5%\" en Poliza Entrada a CEDIS: Tasa 5% y Tasa 19%");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS) IVA 5%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 17
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el credito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) IVA 5%\" en Poliza Entrada a CEDIS: Tasa 5% y Tasa 19%");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS) IVA 19%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 18
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"Transf. CEDIS - TIENDA Tasa 5%\" en Poliza Entrada a CEDIS: Tasa 5% y Tasa 19%");
				
				segmento4 = "1435010064";
				referencia10 = "Transf. CEDIS - TIENDA Tasa 5%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 19
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"Transf. CEDIS - Tiendas Tarifa General 19%\" en Poliza Entrada a CEDIS: Tasa 5% y Tasa 19%");
				
				segmento4 = "1435010065";
				referencia10 = "Transf. CEDIS - Tiendas Tarifa General 19%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 20
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) \" en Poliza Entrada a CEDIS: Tasa ICO 8%");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS)";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 21
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"Transf. CEDIS - Tiendas Tarifa general 19%\" en Poliza Entrada a CEDIS: Tasa ICO 8%");
				
				segmento4 = "1435010065";
				referencia10 = "Transf. CEDIS - Tiendas Tarifa general 19%";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 22
		 * **************************************************************************************************************************************/
		addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"<tienda> BOGOTA (CEDIS) \" en Poliza Entrada a CEDIS: Tasa EXCLUD");
				
				segmento4 = "1380950012";
				referencia10 = data.get("CR_TIENDA") + " BOGOTA (CEDIS)";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 23
		 * **************************************************************************************************************************************/
	addStep("Validar que se encuentra en el debito la sumatoria del  movimiento \"Transf. CEDIS - Tienda tasa 0\" en Poliza Entrada a CEDIS: Tasa EXCLUD");
				
				segmento4 = "1435010063";
				referencia10 = "Transf. CEDIS - Tienda tasa 0";
				
				qryGlInterface_f = String.format(qryGlInterface, segmento4, referencia10, referencia3);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				qryGlInterface_r = executeQuery(dbEbs, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
		

	}
	
	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		return null;
		
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