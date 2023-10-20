package interfaces.OE3;


import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class OE3_ValidarEnvioArchivosCTA_IN extends BaseExecution {
	@Test(dataProvider = "data-provider")

	public void ATC_FT_002_OE3_ValidarEnvioArchivosCTA_IN_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * 
		 * Modificado por mantenimiento.
		 * @author Brandon Ruiz.
		 * @date   31/01/2023.
		 * @cp MTC-FT-014-1 OE3_SEND Envio de archivos .cta e .in desde Finanzas a Santander a traves de la interface 
		 * FEMSA_OE3 (EBS 12.0.6  CHO_52072931_1112)
		 * @projectname Actualizacion Tecnologica Webmethods
		 *
		 *Mtto:
		 *@author Jose Onofre
		 *@date 02/28/2023
		 */

		/*
		 ***************************************Utileria******************************/
//		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		String dbLog_host = GlobalVariables.DB_HOST_FCWMLQA;
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		String dbAVEBQA_host = GlobalVariables.DB_HOST_AVEBQA;
		utils.sql.SQLUtil dbAVEBQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		String dbPos_host = GlobalVariables.DB_HOST_FCWMQA_NUEVA;
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		Util o = new Util(testCase);
		
		SeleniumUtil u = null;
		/*
		 ***************************************Variables******************************/
		//Variables generales
		String defaultQueryError = "La consulta realizada no devolvio ningun registro";
		String defaultConnectionSuccess = "Consulta realizada con exito";
		
		//Data Provider
		String extension = data.get("extension");
		String ftpPass = data.get("ftpPass");
		
		String queryPaso1 = "SELECT * FROM WMUSER.WM_FTP_CONNECTIONS \r\n"
				+ "WHERE ROWNUM <= 10";
		SQLResult resultPaso1;
		
		String queryPaso2 = "SELECT * FROM WMUSER.WM_FTP_CONNECTIONS\r\n"
				+ "WHERE FTP_CONN_ID = 'SANTANDER_H2H_IN'";
		String errorPaso2 = "No se obtuvo informacion";
		SQLResult resultPaso2;
		
		String queryPaso3 = "SELECT * FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO \r\n"
				+ "WHERE ROWNUM <= 10";
		SQLResult resultPaso3;
		
		String queryPaso4 = "SELECT NOMBRE_ARCHIVO,XXH2H_ID_LOTE_GENERADO_PK, ESTATUS_WM \r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO WHERE NOMBRE_ARCHIVO LIKE '%"+data.get("extension")
				+ "%' AND ESTATUS_WM = 'L' AND ROWNUM <=10";
		String errorPaso4 = "No se encontro ningun registro con estatus: L";
		SQLResult resultPaso4;
		
		String queryPaso9= "SELECT ID, DOCNAME, WM_STATUS FROM WMUSER.WM_H2H_INBOUND_DOCS\r\n"
				+ "WHERE TRUNC(RECEIPT_DATE) >= TRUNC(SYSDATE)\r\n"
				+ "AND ROWNUM <=10 ORDER BY RECEIPT_DATE DESC";
		String errorPaso9= "Se mostro el archivo registrado";
		SQLResult resultPaso9;
		
		String queryPaso11;
		String errorPaso11;
		SQLResult resultPaso11;
		
		String queryPaso12 = "SELECT * FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultPaso12;
		
		String queryPaso13 = "SELECT RUN_ID, INTERFACE, STATUS, SERVER \r\n"
				+ "FROM WMLOG.WM_LOG_RUN WHERE INTERFACE LIKE '%OE3%' \r\n"
				+ "AND START_DT >= TRUNC(SYSDATE) AND ROWNUM <= 10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorPaso13 = "No se encontro ningun registro de ejecucion de la interfaz reciente";
		SQLResult resultPaso13;
		
		String queryPaso14;
		String errorPaso14;
		SQLResult resultPaso14;
		
		String queryPaso15;
		String errorPaso15;
		SQLResult resultPaso15;
		
		/*
		 * 
		 * Pasos 5, 8 y 10 comentados debido a que requieren existencia de archivos
		 * 
		 */
		
		/****************************************Paso 1*********************************/
		step("Realizar conexión en la BD FCWMQA");
		o.logHost(dbPos_host);
		resultPaso1 = ejecutaQuery(queryPaso1, dbPos);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1, defaultQueryError);
		}else{
			o.log(defaultConnectionSuccess);
		}
		
		
		
		/****************************************Paso 2*********************************/
		step("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS de FCWMQA");
		resultPaso2 = ejecutaQuery(queryPaso2, dbPos);
		o.validaRespuestaQuery(resultPaso2);
		String ftpPathStep8 = "";
		String ftpHost = "";
		Integer ftpPort = 21;
		String ftpUser = "";
		if (!resultPaso2.isEmpty()) {
			ftpPathStep8 = resultPaso2.getData(0, "FTP_BASE_DIR");
			ftpHost = resultPaso2.getData(0, "FTP_SERVERHOST");
			ftpUser = resultPaso2.getData(0, "FTP_USERNAME");
			o.log("Recupera ruta de archivos enviados: '"+ftpPathStep8+"', recupera el host:  '"+ftpHost+
					"', y el usuario: "+ftpUser);
		}else {
			o.muestraError(queryPaso2, errorPaso2);
		}
		
		/****************************************Paso 3*********************************/
		step("Realizar la conexión a la BD AVEBQA");
		o.logHost(dbAVEBQA_host);
		resultPaso3 = ejecutaQuery(queryPaso3, dbAVEBQA);
		if (resultPaso3.isEmpty()) {
			o.mensajeError(defaultQueryError);
		}else {
			o.log(defaultConnectionSuccess);
		}
		
		/****************************************Paso 4*********************************/
		step("Validar que existan registros de los archivos "+extension+" pendientes de procesar en la tabla XXFC_H2H.XXFC_H2H_LOTE_GENERADO de AVEBQA");
		resultPaso4 = ejecutaQuery(queryPaso4, dbAVEBQA);
		o.validaRespuestaQuery(resultPaso4);
		String id_lote_generado = "";
		if (!resultPaso4.isEmpty()) {
			id_lote_generado = resultPaso4.getData(0, "XXH2H_ID_LOTE_GENERADO_PK");
			o.log("Recupera el XXH2H_ID_LOTE_GENERADO_PK: "+id_lote_generado+" del primer registro");
		}else {
			o.muestraError(queryPaso4, errorPaso4);
		}
		
		/****************************************Paso 5*********************************/
		/*
		step("Validar que existan archivos con extension: " + data.get("extension") +" pendientes por procesar en el directorio del servidor finanzas "
				+ "configurado en el servicio OE3.Mapping:config.");
		String ftpPathStep5 = "/export/home/wmuser/EO3_TMP/pagos/operacion";
		log("Ruta utilizada: "+ftpPathStep5);
		FTPUtil ftp = new FTPUtil(ftpHost, ftpPort, ftpUser, ftpPass);
		String fileName = "";
		FTPClient ftpClient = ftp.getClient();
		ftpClient.changeWorkingDirectory(ftpPathStep5);
		FTPFile[] files = ftpClient.listFiles();
		int filecount = 0;
		System.out.println("Archivos encontrados: ");
		for (FTPFile file : files) {
			System.out.println(file.getName());
			if (file.getName().endsWith(data.get("extension"))) {
					filecount++;
					fileName = file.getName();
					testCase.addTextEvidenceCurrentStep(fileName);
				}
		}
		if (filecount == 0) {
			mensajeError("No se encontraron archivos con la extension: "+data.get("extension")+" por procesar");
		}else {
			log("Se encontraron "+files.length+" archivo(s) por procesar");
		}
		*/
		/****************************************Paso 6*********************************/
		step("Solicitar a los Usu FC Operadores SITE vía correo electrónico <UsuFEMCOMOperadoresSITE@oxxo.com>,la ejecución del job OE3,");

		/****************************************Paso 7*********************************/
		String job_name = "runOE3Send";
		step("Ingresar a Control M y validar que la ejecución del Job: "+job_name+" se muestre exitosa");
		u = new SeleniumUtil(new ChromeTest(), true);
		JSONObject job_json = new JSONObject(data.get("job"));
		ControlM control = new ControlM(u, testCase, job_json);
		Control_mInicio cm = new Control_mInicio(u, data.get("user"), data.get("pas"));
		o.log("Abrir navegador y acceder a plataforma Control-M");
		String url = data.get("server");
		u.get(url);
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
				o.log("El estatus del job fué diferente a Ended OK: "+jobStatus);
			}
		}else {
			o.mensajeError("No se encontro el job buscado: "+job_name);
		}
		control.closeViewpoint();
		u.close();
		
		/****************************************Paso 8*********************************/
		/*
		step("Validar que el archivo con extension "+data.get("extension")+
				" haya sido enviado al servidor FTP configurado del simulador bancario");
		log("Ruta utilizada: "+ftpPathStep8);
		if (ftpPathStep8.isBlank()) {
			mensajeError("La ruta de archivos enviados en el servidor no fue recuperada correctamente en el paso 2");
		}
		ftpClient.changeWorkingDirectory(ftpPathStep8);
		files = ftpClient.listFiles();
		filecount = 0;
		System.out.println("Archivos encontrados: ");
		for (FTPFile file : files) {
			System.out.println(file.getName());
			if (file.getName().endsWith(data.get("extension"))) {
					filecount++;
					fileName = file.getName();
					testCase.addTextEvidenceCurrentStep(fileName);
				}
		}
		if (filecount == 0) {
			mensajeError("No se encontraron archivos con la extension: "+data.get("extension")+" enviados");
		}else {
			log("Se encontraron "+files.length+" archivo(s) enviados");
		}
		*/
		/****************************************Paso 9*********************************/
		step("Validar la no inserción  bitácora de la tabla WM_H2H_INBOUND_DOCS de la BD FCWMQA");
		resultPaso9 = ejecutaQuery(queryPaso9, dbPos);
		o.validaRespuestaQuery(resultPaso9);
		if (!resultPaso9.isEmpty()) {
			o.mensajeError(errorPaso9);
		}else {
			o.log("Validacion realizada exitosamente");
		}
		
		/****************************************Paso 10*********************************/
		/*
		step("Validar que el archivo con extension "+data.get("extension")+
				" fue enviado al directorio de respaldo configurado y eliminado del origen");
		//ruta respaldo de archivos pendientes de procesar
		String ftpPathStep10= "/export/home/wmuser/EO3_TMP/pagos/respaldo";
		//ruta origen de archivos pendientes de procesar definida en el paso 5
		logBold("Validar que el archivo con extension "+data.get("extension")+
				" fue enviado al directorio de respaldo configurado");
		log("Ruta utilizada: "+ftpPathStep10);
		ftpClient.changeWorkingDirectory(ftpPathStep10);
		files = ftpClient.listFiles();
		filecount = 0;
		System.out.println("Archivos encontrados: ");
		for (FTPFile file : files) {
			System.out.println(file.getName());
			if (file.getName().endsWith(data.get("extension"))) {
					filecount++;
					fileName = file.getName();
					testCase.addTextEvidenceCurrentStep(fileName);
				}
		}
		if (filecount == 0) {
			mensajeError("No se encontraron archivos con la extension: "+data.get("extension")+" respaldados");
		}else {
			log("Se encontraron "+files.length+" archivo(s) respaldados");
		}
		
		logBold("Validar que el archivo con extension "+data.get("extension")+" fue eliminado del origen");
		log("Ruta utilizada: "+ftpPathStep5);
		ftpClient.changeWorkingDirectory(ftpPathStep5);
		files = ftpClient.listFiles();
		filecount = 0;
		System.out.println("Archivos encontrados: ");
		for (FTPFile file : files) {
			System.out.println(file.getName());
			if (file.getName().endsWith(data.get("extension"))) {
					filecount++;
					fileName = file.getName();
					testCase.addTextEvidenceCurrentStep(fileName);
				}
		}
		if (filecount == 0) {
			log("Los archivos con la extension: "+data.get("extension")+" fueron eliminados del origen correctamente");
		}else {
			mensajeError("El archivo con la extension "+files.length+" no fue eliminado del origen");
		}
		*/
		/****************************************Paso 11*********************************/
		step("Validar que se actualice el registro del archivo en status: P de que ya fue enviado, en la BD AVEBQA");
		queryPaso11 = "SELECT NOMBRE_ARCHIVO,XXH2H_ID_LOTE_GENERADO_PK,ESTATUS_WM,SENT_DATE\r\n"
				+ "FROM XXFC_H2H.XXFC_H2H_LOTE_GENERADO WHERE XXH2H_ID_LOTE_GENERADO_PK = "+id_lote_generado+"\r\n"
				+ "AND ESTATUS_WM = 'P'";
		errorPaso11 = "No se encontro registro con xxh2h_id_lote_generado: "+id_lote_generado+" y estatus: P";
		resultPaso11 = ejecutaQuery(queryPaso11, dbAVEBQA);
		o.validaRespuestaQuery(resultPaso11);
		if (resultPaso11.isEmpty()) {
			o.muestraError(queryPaso11, errorPaso11);
		}
		
		/****************************************Paso 12*********************************/
		step("Realizar conexión a la BD con host FCWMLQA.FEMCOM.NET");
		o.logHost(dbLog_host);
		resultPaso12 = ejecutaQuery(queryPaso12, dbLog);
		if (resultPaso12.isEmpty()) {
			o.mensajeError(defaultQueryError);
		}else {
			o.log(defaultConnectionSuccess);
		}
		
		/****************************************Paso 13*********************************/
		step("Validar la correcta ejecución de la interface OE3 en la tabla WMLOG.WM_LOG_RUN de la BD FCWMLQA");
		resultPaso13 = ejecutaQuery(queryPaso13, dbLog);
		o.validaRespuestaQuery(resultPaso13);
		String run_id = "";
		if (!resultPaso13.isEmpty()) {
			run_id = resultPaso13.getData(0, "RUN_ID");
			o.log("Recupera run_id: "+run_id+" del primer registro");
		}else {
			o.muestraError(queryPaso13, errorPaso13);
		}
		
		/****************************************Paso 14*********************************/
		step("Validar la correcta ejecución de los Threads "
				+ "lanzados por la interface OE3 en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		queryPaso14 = "SELECT THREAD_ID, NAME, START_DT, STATUS FROM WMLOG.WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = "+run_id;
		errorPaso14 = "No se encontro ningun thread con parent_id: "+run_id;
		resultPaso14 = ejecutaQuery(queryPaso14, dbLog);
		o.validaRespuestaQuery(resultPaso14);
		if (resultPaso14.isEmpty()) {
			o.muestraError(queryPaso14, errorPaso14);
		}
		
		/****************************************Paso 15*********************************/
		step("Realizar la siguiente consulta para verificar que no se encuentre ningún error "
				+ "presente en la ejecución de la interfaz OE3 en la tabla WM_LOG_ERROR de BD FCWMLQA");
		queryPaso15 = "SELECT ERROR_ID, ERROR_DATE, DESCRIPTION FROM  WMLOG.WM_LOG_ERROR\r\n"
				+ "WHERE RUN_ID ="+run_id;
		errorPaso15 = "Se encontro un error en la ejecucion de la interfaz con run_id: "+run_id;
		resultPaso15 = ejecutaQuery(queryPaso15, dbLog);
		o.validaRespuestaQuery(resultPaso15);
		if (!resultPaso15.isEmpty()) {
			o.muestraError(queryPaso15, errorPaso15);
		}else {
			o.log("No se encontro ningun error para la ejecucion de la interfaz");
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
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_OE3"
				+ " de avante para trasportar entre FEMSA y Santander archivos .cta e .in "
				+ "(archivos de cuentas bancarias y pagos a proveedores) desde las rutas configuradas en WM_FTP_CONNECTIONS"
				+ " , al ser migrada la interface de WM9.9 a WM10.5. La interfaz se encarga de transportar los archivos "
				+ "de cuentas bancarias y pagos a Proveedores entre FEMSA y Santander. "
				+ "Origen: Archivos de texto plano con extensión .in y .cta de FEMSA. "
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

		return "ATC_FT_002_OE3_ValidarEnvioArchivosCTA_IN_test";

	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
