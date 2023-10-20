package interfaces.cp01;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import modelo.BaseExecution;
import om.Util;

public class CP01_generaArchivoDGT extends BaseExecution{
	
	/**
	 * Construido.
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp MTC-FT-011 CP01 Generacion de archivo DGT a traves de la interface FEMSA_CP01
	 * @projectname Actualizacion Tecnologica Webmethods
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_CP01_generaArchivoDGT(HashMap<String, String> data) throws Exception{
		
		/*** Utileria****************************************************/
		
		//XXCNT Paso 1
		String XXCNT_HOST = GlobalVariables.DB_HOST_FCIASQA;
		String XXCNT_USER = GlobalVariables.DB_USER_FCIASQA;
		String XXCNT_PASS = GlobalVariables.DB_PASSWORD_FCIASQA;
		SQLUtil XXCNT = new SQLUtil(XXCNT_HOST, XXCNT_USER, XXCNT_PASS);
		
		//FCWMLQA Paso 3
		String FCWMLQA_HOST = GlobalVariables.DB_HOST_FCWMLQA_WMLOG;
		String FCWMLQA_USER = GlobalVariables.DB_USER_FCWMLQA_WMLOG;
		String FCWMLQA_PASS = GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG;
		SQLUtil FCWMLQA = new SQLUtil(FCWMLQA_HOST, FCWMLQA_USER, FCWMLQA_PASS);
		
		//FCWM6QA Paso 10
		String FCWM6QA_HOST = GlobalVariables.DB_HOST_FCWMQA_NUEVA;
		String FCWM6QA_USER = GlobalVariables.DB_USER_FCWMQA_NUEVA;
		String FCWM6QA_PASS = GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA;
		SQLUtil FCWM6QA = new SQLUtil(FCWM6QA_HOST, FCWM6QA_USER, FCWM6QA_PASS);
		
		Util o = new Util(testCase);
		
		
		/*** Variables****************************************************/
		
		//Generic Variables
		String defaultConnectionError = "Hubo un problema al consultar la base de datos";
		String succesfulConnection = "Consulta realizada con exito";
		String plaza_cr = data.get("plaza");
		String current_date = o.generaFecha();
		String job_name = data.get("job_name");
		String job_group = data.get("job_group");
		String ftp_host = data.get("ftp_host");
		String ftp_user = data.get("ftp_user");
		String ftp_pass = data.get("ftp_pass");
		int ftp_port = Integer.parseInt(data.get("ftp_port"));
		
		
		//Paso 1
		String queryConexionXXCNT = "SELECT * FROM POS_CAMBIOS WHERE ROWNUM <=10";
		SQLResult resultConexionXXCNT;
		
		//Paso 2
		String queryStatusLPOSCambios = "SELECT PLAZA_CR, TIENDA_CR, WM_STATUS FROM POS_CAMBIOS \r\n"
				+ "WHERE WM_STATUS = 'L' AND ROWNUM <=10";
		String errorStatusLPOSCambios = "No existe ningun registro con status L";
		SQLResult resultStatusLPOSCambios;
		
		//Paso 3
		String queryConexionFCWMLQA = "SELECT * FROM WM_LOG_GROUP A, WM_LOG_USER_GROUP B, WM_LOG_USER C \r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultConexionFCWMLQA;
		
		//Paso 6
		String cm_server = data.get("server");
		String cm_user = data.get("user");
		String cm_pass = data.get("ps");
		JSONObject job_json = new JSONObject(data.get("job"));
		By input_user = By.xpath("//input[@id=\"login-user-name\"]");
		By input_pass = By.xpath("//input[@id=\"login-user-password\"]");
		By button_login = By.xpath("(//button[contains(text(),\"LOG IN\")])[1]");
		
		//Paso 7
		String run_id = "";
		String queryEjecucionInterfaz = "SELECT RUN_ID, INTERFACE, END_DT, STATUS, SERVER FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'CP01' AND STATUS='S' \r\n"
				+ "AND TRUNC(end_dt) = '"+current_date+"' AND ROWNUM <=10\r\n"
				+ "ORDER BY START_DT DESC";
		SQLResult resultEjecucionInterfaz;
		String errorEjecucionInterfaz = "No existe ningun registro de interfaces ejecutadas con STATUS = 'S'";
		
		//Paso 8
		String queryErrorEjecucion;
		String errorErrorEjecucion;
		SQLResult resultErrorEjecucion;
		
		//Paso 9
		String queryValidaCambioEstatus;
		String errorValidaCambioEstatus= "No se encontro ningun registro con estatus diferente a L";
		SQLResult resultValidaCambioEstatus;
		
		//Paso 10
		String queryConexionFCWM6QA="SELECT * FROM POSUSER.POS_OUTBOUND_DOCS WHERE ROWNUM <=10";
		SQLResult resultConexionFCWM6QA;
		
		//Paso 11
		String queryRegistroArchivoDGT;
		String errorRegistroArchivoDGT = "No se encontro ningun archivo DGT con estatus: L";
		SQLResult resultRegistroArchivoDGT;
		
		
		/******************** Pasos ****************************************************/
		
//Paso 1
		step("Establecer la conexion con la BD *XXCNT_WMUSER*");
		o.logHost(XXCNT_HOST);
		resultConexionXXCNT = ejecutaQuery(queryConexionXXCNT, XXCNT);
		if (!resultConexionXXCNT.isEmpty()) {
			o.log(succesfulConnection);
		}else {
			System.out.println(defaultConnectionError);
			assertTrue(false,defaultConnectionError);
		}
		
//Paso 2
		step("Comprobar que exista registro de la Plaza 10MON Y TIENDA <TIENDA> "
				+ "con STATUS L en la tabla POS_CAMBIOS de la BD CNT con la siguiente busqueda:");
		resultStatusLPOSCambios = ejecutaQuery(queryStatusLPOSCambios, XXCNT);
		o.validaRespuestaQuery(resultStatusLPOSCambios);
		String tienda_cr = "";
		if (!resultStatusLPOSCambios.isEmpty()) {
			tienda_cr = resultStatusLPOSCambios.getData(0, "TIENDA_CR");
			o.log("Recupera el primer valor para TIENDA_CR: "+tienda_cr);
		}else {
			o.muestraError(queryStatusLPOSCambios,errorStatusLPOSCambios);
		}
		
//Paso 3
		step("Establecer la conexión con la BD *FCWMLQA_WMLOG*");
		o.logHost(FCWMLQA_HOST);
		resultConexionFCWMLQA = ejecutaQuery(queryConexionFCWMLQA, FCWMLQA);
		if (!resultConexionFCWMLQA.isEmpty()) {
			o.log(succesfulConnection);
		}else {
			System.out.println(defaultConnectionError);
			assertTrue(false,defaultConnectionError);
		}
	
//Paso 5
		o.logBold("Solicita la ejecución del siguiente Job:"+job_name+" y grupo: "+job_group
				+ " al operador en turno de UsuOperadores, del siguiente correo: usufemcomoperadoressite@oxxo.com");
		
//Paso 6
		step("Ingresar a Control M y validar que la ejecución del Job:"+job_name+" y grupo: "+job_group+" se muestre exitosa");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		o.log("Abrir navegador y acceder a plataforma Control-M");
		u.get(cm_server);
		u.waitForLoadPage();
		o.log("Ingresar user: "+cm_user);
		o.insertaTexto(u, input_user, cm_user);
		o.log("Ingresar password: ******");
		o.insertaTexto(u, input_pass, cm_pass);
		u.highLight(button_login);
		testCase.addScreenShotCurrentStep(u, "Login a Control-M");
		o.log("Presionar boton login");
		u.unhighLight(button_login);
		o.seleccionaElemento(u, button_login);
		u.waitForLoadPage();
		testCase.addScreenShotCurrentStep(u, "Login exitoso");
		ControlM control = new ControlM(u, testCase, job_json);
		o.log("Buscar job: "+job_name);
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
					System.out.println("El estatus del job fué diferente a Ended OK: "+jobStatus);
					assertTrue(false,"El estatus del job fué diferente a Ended OK: "+jobStatus);
				}
		}else {
			System.out.println("No se encontro el job buscado: "+job_name);
			assertTrue(false,"No se encontro el job buscado: "+job_name);
		}
		control.closeViewpoint();
		u.close();
		
//Paso 7
		step("Validar que el STATUS sea igual a S en la tabla WM_LOG_RUN de la BD *FCWMLQA_WMLOG*. Donde INTERFACE = CP01.");
		resultEjecucionInterfaz = ejecutaQuery(queryEjecucionInterfaz, FCWMLQA);
		o.validaRespuestaQuery(resultEjecucionInterfaz);
		if (!resultEjecucionInterfaz.isEmpty()) {
			run_id = resultEjecucionInterfaz.getData(0, "RUN_ID");
			o.log("Recupera el primer valor para RUN_ID: "+run_id);
		}else {
			o.muestraError(queryEjecucionInterfaz, errorEjecucionInterfaz);
		}

//Paso 8
		step("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente "
				+ "en la ejecución de la interfaz dentro de la tabla WM_LOG_ERROR de la BD *FCWMLQA_WMLOG*");
			queryErrorEjecucion = "SELECT ERROR_ID, RUN_ID, ERROR_DATE FROM WM_LOG_ERROR \r\n"
					+ "WHERE ROWNUM <=10\r\n"
					+ "AND RUN_ID = '"+run_id+"'";
			errorErrorEjecucion = "No existe ningun registro de error en la interfaz ejecutada con run_id: "+run_id;
			resultErrorEjecucion = ejecutaQuery(queryErrorEjecucion, FCWMLQA);
			if (!resultErrorEjecucion.isEmpty()) {
				System.out.println("Se encontro un registro de error en la interfaz ejecutada con run_id: "+run_id);
				assertTrue(false,"Se encontro un registro de error en la interfaz ejecutada con run_id: "+run_id);
			}else {
				o.log("Consulta realizada con exito, no devolvio ningun registro de errores en la ejecucion de la interfaz");
			}
		
//Paso 9
		step(" Validar que WM_STATUS = E en la tabla POS_CAMBIOS "
				+ "de la BD *XXCNT_WMUSER* donde PLAZA_CR ="+plaza_cr+" Y TIENDA_CR = "+tienda_cr);
			queryValidaCambioEstatus= "SELECT WM_RUN_ID, PLAZA_CR, TIENDA_CR, WM_STATUS, WM_SEND_DT \r\n"
					+ "FROM POS_CAMBIOS WHERE WM_STATUS = 'E' \r\n"
					+ "AND TRUNC(WM_SEND_DT) = '"+current_date+"' \r\n"
					+ "AND PLAZA_CR = '"+plaza_cr+"' AND TIENDA_CR = '"+tienda_cr+"' \r\n"
					+ "AND WM_RUN_ID = '"+run_id+"'";
			resultValidaCambioEstatus = ejecutaQuery(queryValidaCambioEstatus, XXCNT);
			o.validaRespuestaQuery(resultValidaCambioEstatus);
			if (resultValidaCambioEstatus.isEmpty()) {
				o.muestraError(queryValidaCambioEstatus, errorValidaCambioEstatus);
			}
		
//Paso 10
		step("Establecer la conexión a la BD *FCWM6QA*");
		o.logHost(FCWM6QA_HOST);
		resultConexionFCWM6QA = ejecutaQuery(queryConexionFCWM6QA, FCWM6QA);
		if (!resultConexionFCWM6QA.isEmpty()) {
			o.log(succesfulConnection);
		}else {
			System.out.println(defaultConnectionError);
			assertTrue(false,defaultConnectionError);
		}
		
//Paso 11
		step("Ejecutar la siguiente consulta en la DB FCWM6QA "
				+ "para validar que se creo el registro del archivo DGT con Status=L:");
			queryRegistroArchivoDGT = "SELECT ID, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA, STATUS \r\n"
					+ "FROM POSUSER.POS_OUTBOUND_DOCS \r\n"
					+ "WHERE SENT_DATE>='"+current_date+"' AND PV_CR_PLAZA='"+plaza_cr+"' \r\n"
					+ "AND PV_CR_TIENDA IN '"+tienda_cr+"' \r\n"
					+ "AND DOC_TYPE='DGT' AND ROWNUM <=10\r\n"
					+ "ORDER BY SENT_DATE DESC";
			resultRegistroArchivoDGT = ejecutaQuery(queryRegistroArchivoDGT, FCWM6QA);
			o.validaRespuestaQuery(resultRegistroArchivoDGT);
			if (!resultRegistroArchivoDGT.isEmpty()) {
				String status = resultRegistroArchivoDGT.getData(0, "STATUS");
				if (status != "L") {
					System.out.println("El estatus para el archivo de tipo DGT es diferente a L: "+status);
					assertTrue(false,"El estatus para el archivo de tipo DGT es diferente a L: "+status);
				}
			}else {
				o.muestraError(queryRegistroArchivoDGT,errorRegistroArchivoDGT);
			}
		
		
//Paso 12
		step("Validar que el archivo DGT  se encuentre en la siguiente Ruta, se pueda descargar y abrir. \r\n"
				+ "RUTA=/u01/posuser/FEMSA_OXXO/POS/(PLAZA)/(TIENDA)");
		o.logBold("Faltan credenciales de acceso de user y pass para el host: "+ftp_host);
//		if (plaza_cr.isBlank() || tienda_cr.isBlank()) {
//			muestraError("No se recuperaron valores de plaza_cr: "+plaza_cr+" o tienda_cr: "+tienda_cr);
//		}else {
//			String ftp_path = "/u01/posuser/FEMSA_OXXO/POS/"+plaza_cr+"/"+tienda_cr;
//			FTPUtil ftp = new FTPUtil(ftp_host,ftp_port,ftp_user,ftp_pass);
//			String path = ftp_path+"/DGT";
//					
//			if (ftp.fileExists(path)) {
//				testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
//			} else {
//				muestraError("El archivo buscado con la ruta: "+path+" no existe");
//			}
//		}
		
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
		return "*Contar con acceso a las bases de datos de FCWM6QA,  XXCNT_WMUSER y FCWMLQA_WMLOG.\r\n"
				+ "*Contar con acceso a repositorio de buzon de la tienda.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para CP01 de WM10.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
				+ "*exista informacion en la tabla POS_CAMBIOS  con STATUS=L  en la BD  XXCNT_WMUSER";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_CP01"
				+ " para generar archivos DGT (Datos Generales de Tienda) de bajada (de CNT a WM OUTBOUND) "
				+ "con la informacion actualizada de la tienda, al ser migrada la interface de WM9.9 a WM10.5\r\n";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_CP01_generaArchivoDGT";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
