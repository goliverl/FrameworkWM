package interfaces.OE3;


import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OE3_ValidarRecepcionArchivosCTA_IN extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OE3_ValidarRecepcionArchivosCTA_IN_test (HashMap <String, String> data) throws Exception{
		
		/**
		 * 
		 * Modificado por mantenimiento.
		 * @author Brandon Ruiz.
		 * @date   31/01/2023.
		 * @cp MTC-FT-014-2 OE3 Recepcion de archivos .cta e .in desde Finanzas a Santander a traves de la interface 
		 * FEMSA_OE3 (EBS 12.0.6  CHO_52072931_1112)
		 * @projectname Actualizacion Tecnologica Webmethods
		 * 
		 * Mtto:
		 * @author Jose Onofre
		 * @date 02/28/2023
		 *
		 */
		
		/*****Utileria******************************************************/
		String dbLogHost = GlobalVariables.DB_HOST_FCWMLQA;
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		String dbAVEBQAHost = GlobalVariables.DB_HOST_AVEBQA;
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		String dbPosHost = GlobalVariables.DB_HOST_FCWMQA_NUEVA;
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SeleniumUtil u = null;
		
		Util o = new Util(testCase);
		
		/*****Variables*****************************************************/
		
		//Data Provider 
		String extension = data.get("extension");
		String url = data.get("server");
		String user = data.get("user");
		String pass = data.get("pas");
		JSONObject job_json = new JSONObject(data.get("job"));
		String ftpPass = data.get("ftpPass");
		
		//Default variables
		String successfulConnection = "Consulta realizada con exito";
		String defaultQueryError = "La consulta realizada no devolvio ningun registro";
		
		//Step variables
		String queryPaso1 = "SELECT * FROM WMUSER.WM_FTP_CONNECTIONS\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultPaso1;
		
		String queryPaso2 = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME FROM WMUSER.WM_FTP_CONNECTIONS\r\n"
				+ "WHERE FTP_CONN_ID = 'SANTANDER_H2H_OUT'";
		String errorPaso2 = "No se encontraron datos de configuracion del servidor FTP "
				+ "para el envío del archivo "+extension;
		SQLResult resultPaso2;
		
		String queryPaso3 = "SELECT * FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultPaso3;
		
		String queryPaso4 = "SELECT NOMBRE_ARCHIVO,XXH2H_ID_LOTE_GENERADO_PK,ESTATUS_WM \r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO \r\n"
				+ "WHERE NOMBRE_ARCHIVO LIKE '%"+extension+"%' \r\n"
				+ "AND ESTATUS_WM = 'L'\r\n"
				+ "AND ROWNUM <=10";
		String errorPaso4 = "No se encontro ningun archivo pendiente por procesar";
		SQLResult resultPaso4;
		
		String queryPaso8 = "SELECT DOCNAME, WM_STATUS, RECEIPT_DATE, RUN_ID FROM WMUSER.WM_H2H_INBOUND_DOCS\r\n"
				+ "WHERE TRUNC(RECEIPT_DATE) >= TRUNC(SYSDATE) AND ROWNUM <=10\r\n"
				+ "ORDER BY RECEIPT_DATE DESC";
		String errorPaso8 = "No se encontro registro de ningun archivo";
		SQLResult resultPaso8;

		String queryPaso9 = "SELECT * FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultPaso9;

		String queryPaso10; 
		String errorPaso10; 
		SQLResult resultPaso10;
		
		String queryPaso11;
		String errorPaso11;
		SQLResult resultPaso11;
		
		String queryPaso12;
		String errorPaso12;
		SQLResult resultPaso12;
		
		/*
		 * 
		 * Pasos 5 comentado debido a que requiere existencia de archivos
		 * 
		 */
		
		/****************************Paso 1*******************************/
		step("Realizar conexión en la BD FCWMQA");
		o.logHost(dbPosHost);
		resultPaso1 = ejecutaQuery(queryPaso1,dbPos);
		if (!resultPaso1.isEmpty()) {
			o.log(successfulConnection);
		}else {
			o.mensajeError(defaultQueryError);
		}
		
		/****************************Paso 2*******************************/
		step("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS de FCWMQA");
		resultPaso2 = ejecutaQuery(queryPaso2, dbPos);
		o.validaRespuestaQuery(resultPaso2);
		String ftpPath = "";
		String ftpHost = "";
		int ftpPort = 21;
		String ftpUser = "";
		if (!resultPaso2.isEmpty()) {
			ftpPath = resultPaso2.getData(0, "FTP_BASE_DIR");
			ftpHost = resultPaso2.getData(0, "FTP_SERVERHOST");
			ftpUser = resultPaso2.getData(0, "FTP_USERNAME");
			o.log("Recupera ruta de archivos recibidos: '"+ftpPath+"', recupera el host:  '"+ftpHost+
					"', y el usuario: "+ftpUser);
		}else {
			o.muestraError(queryPaso2, errorPaso2);
		}
		
		/****************************Paso 3*******************************/
		step("Realizar la conexión a la BD AVEBQA");
		o.logHost(dbAVEBQAHost);
		resultPaso3 = ejecutaQuery(queryPaso3, dbAVEBQA);
		if (!resultPaso3.isEmpty()) {
			o.log(successfulConnection);
		}else {
			o.mensajeError(defaultQueryError);
		}
		
		/****************************Paso 4*******************************/
		step("Validar que existan registros de los archivos con extension: "
				+extension+" pendientes de procesar en la tabla XXFC_H2H.XXFC_H2H_LOTE_GENERADO de AVEBQA");
		resultPaso4 = ejecutaQuery(queryPaso4,dbAVEBQA);
		o.validaRespuestaQuery(resultPaso4);
		String id_lote_generado = ""; 
		if (!resultPaso4.isEmpty()) {
			id_lote_generado = resultPaso4.getData(0, "XXH2H_ID_LOTE_GENERADO_PK");
			o.log("Recupera el XXH2H_ID_LOTE_GENERADO_PK: "+id_lote_generado+" del primer registro");
		}else {
			o.muestraError(queryPaso4, errorPaso4);
		}
		
		/****************************Paso 5*******************************/
		/*
		step("Validar que existan archivos con extension "
				+extension+" pendientes por procesar en el directorio "
				+ "del servidor finanzas configurado en el servicio OE3.Mapping:config.");
				
		*/
		/****************************Paso 6*******************************/
		step("Solicitar a los Usu FC Operadores SITE vía correo electrónico <UsuFEMCOMOperadoresSITE@oxxo.com>,"
				+ "la ejecución de la interfaz OE3");
		
		/****************************Paso 7*******************************/
		String job_name = "runOE3";
		step("Ingresar a Control M y validar que la ejecución del Job: "+job_name+" se muestre exitosa");
		u = new SeleniumUtil(new ChromeTest(), true);
		ControlM control = new ControlM(u, testCase, job_json);
		Control_mInicio cm = new Control_mInicio(u, user, pass);
		o.log("Abrir navegador y acceder a plataforma Control-M");
		u.get(url);
		u.waitForLoadPage();
		cm.logOn();
		u.waitForLoadPage();
		if (control.searchJob()) {
			o.log("Busqueda exitosa del job: "+job_name);
			testCase.addScreenShotCurrentStep(u, "Busqueda exitosa del job "+job_name);
			o.log("Ejecuta job: "+job_name);
			String jobExecution = control.executeJob();
			o.log("Resultado de la ejecucion del job -> " + jobExecution);
			u.hardWait(30);
			String jobStatus = control.getNewStatus();
			o.log("Status de ejecucion:" +jobStatus);
			String jobOutput = control.getOutput();
			o.log("Output de ejecucion:" +jobOutput);
			if(jobStatus.equals("Ended OK")) {
				o.log("Job: "+job_name+" ejecutado exitosamente");
			}else {
//				mensajeError("El estatus del job fué diferente a Ended OK: "+jobStatus);
			}
		}else {
			o.mensajeError("No se encontro el job buscado: "+job_name);
		}
		control.closeViewpoint();
		u.close();
		
		/****************************Paso 8*******************************/
		step("Validar la inserción de la bitácora en la tabla WM_H2H_INBOUND_DOCS de la BD FCWMQA");
		resultPaso8 = ejecutaQuery(queryPaso8,dbPos);
		o.validaRespuestaQuery(resultPaso8);
		String run_id = "";
		if (resultPaso8.isEmpty()) {
			o.log(defaultQueryError);
		}
		
		/****************************Paso 9*******************************/
		step("Realizar conexión a la BD FCWMLQA con host FCWMLQA.FEMCOM.NET");
		o.logHost(dbLogHost);
		resultPaso9 = ejecutaQuery(queryPaso9, dbLog);
		if (!resultPaso9.isEmpty()) {
			o.log(successfulConnection);
		}else {
			o.mensajeError(defaultQueryError);
		}
		
		
		/****************************Paso 10*******************************/
		step("Validar la correcta ejecución de la interface OE3 en la tabla WM WM_LOG_RUN de la BD FCWMLQA");
		queryPaso10 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE START_DT >= TRUNC(SYSDATE) AND INTERFACE LIKE '%OE3%' AND ROWNUM <=10 \r\n"
				+ "ORDER BY START_DT DESC"; 
		errorPaso10 = "No se encontro ningun registro con run_id: "+run_id;
		resultPaso10 = ejecutaQuery(queryPaso10, dbLog);
		o.validaRespuestaQuery(resultPaso10);
		if (resultPaso10.isEmpty()) {
			o.muestraError(queryPaso10, errorPaso10);
		}else {
			run_id = resultPaso10.getData(0, "RUN_ID");
			o.log("Recupera run_id: "+run_id+" del primer registro");
		}
		
		/****************************Paso 11*******************************/
		step("Validar la correcta ejecución de los Threads "
				+ "lanzados por la interface OE3 en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		queryPaso11 = "SELECT THREAD_ID, NAME, START_DT, STATUS \r\n"
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID = '"+run_id+"'";
		errorPaso11 = "No se encontro ningun thread con parent_id: "+run_id;
		resultPaso11 = ejecutaQuery(queryPaso11, dbLog);
		if (resultPaso11.isEmpty()) {
			o.muestraError(queryPaso11, errorPaso11);
		}
		
		/****************************Paso 12*******************************/
		step("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente "
				+ "en la ejecución de la interfaz OE3 en la tabla WM_LOG_ERROR de BD FCWMLQA");
		queryPaso12 = "SELECT ERROR_ID, ERROR_TYPE,DESCRIPTION \r\n"
				+ "FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID = '"+run_id+"'";
		resultPaso12 = ejecutaQuery(queryPaso12, dbLog);
		if (!resultPaso12.isEmpty()) {
			o.mensajeError("Se encontro un registro de error para la interfaz con run_id: "+run_id);
		}else {
			o.log("No se encontro ningun error para la ejecucion con run_id: "+run_id);
		}
		
	}
	
	int contador = 0;
	private void step(String step) {
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	private void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}
	
	private SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
		SQLResult queryResult;
		printQuery(query);
		queryResult = executeQuery(obj, query);
		return queryResult;
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
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface "
				+ "FEMSA_OE3 de avante para trasportar entre FEMSA y Santander archivos "
				+ ".cta e .in (archivos de cuentas bancarias y pagos a proveedores) desde las rutas configuradas "
				+ "en WM_FTP_CONNECTIONS , al ser migrada la interface de WM9.9 a WM10.5. "
				+ "La interfaz se encarga de transportar los archivos de cuentas bancarias y pagos a Proveedores entre "
				+ "FEMSA y Santander. Origen: Archivos de texto plano con extensión .in y .cta de FEMSA. "
				+ "Destino: Santander con respuesta de archivos .out, .edocta, .preout y .not. "
				+ "Path: %GLOBAL_OE3%/pagos/operacion %GLOBAL_OE3%/pagos/respaldo";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub

		return "ATC_FT_001_OE3_ValidarRecepcionArchivosCTA_IN_test";

	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
