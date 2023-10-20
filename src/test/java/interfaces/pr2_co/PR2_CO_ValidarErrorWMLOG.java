package interfaces.pr2_co;


import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR2_CO_ValidarErrorWMLOG extends BaseExecution{
	
	/*
	 *
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp MTC-FT-001 -  Validar error en WMLOG al procesar la interfaz. 
	 * @projectname LOGV2
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	
	public void ATC_FT_004_PR2_CO_ValidarErrorWMLOG(HashMap<String,String> data) throws Exception{
			
	/*** Utileria****************************************************/
	
		//Paso 1
		String BDRMSCQA_HOST = GlobalVariables.DB_HOST_RMS_COL_QAVIEW;
		String BDRMSCQA_USER = GlobalVariables.DB_USER_RMS_COL_QAVIEW;
		String BDRMSCQA_PASS = GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW;
		SQLUtil BDRMSCQA = new SQLUtil(BDRMSCQA_HOST, BDRMSCQA_USER, BDRMSCQA_PASS);
		
		//Paso 3
		String server = data.get("server");
		String user = data.get("user");
		String password = PasswordUtil.decryptPassword(data.get("password"));
		String encpassword = data.get("password");
		String interfaz = data.get("interface");
		String service = data.get("service");
		
		//Paso 4
		String FCWMLQA_HOST = GlobalVariables.DB_HOST_FCWMLQA_WMLOG;
		String FCWMLQA_USER = GlobalVariables.DB_USER_FCWMLQA_WMLOG;
		String FCWMLQA_PASS = GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG;
		SQLUtil FCWMLQA = new SQLUtil(FCWMLQA_HOST, FCWMLQA_USER, FCWMLQA_PASS);
		
		Util o = new Util(testCase);
		
	/*** Variables****************************************************/
		
		//Generic Variables
		String defaultConnectionError = "Hubo un problema al consultar la base de datos ";
		String succesfulConnection = "Consulta realizada con exito";
		String fecha_actual = o.generaFecha();
		
		//Paso 1
		String queryConexionBDRMSCQA = "SELECT * FROM xxfc_ordcecol_send_pos_tsf \r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultConexionBDRMSCQA;
		
		//Paso 2
		String queryValidarCondiciones = "SELECT tsf_no, from_loc, to_loc, \r\n"
				+ "TO_CHAR(create_date, 'yyMMddHHmmss') create_date, \r\n"
				+ "TO_CHAR(delivery_date, 'yyMMddHHmmss') delivery_date \r\n"
				+ "FROM xxfc_ordcecol_send_pos_tsf \r\n"
				+ "WHERE send_pos = 'N' \r\n"
				+ "AND from_loc_type = 'S' \r\n"
				+ "AND to_loc_type = 'S' \r\n"
				+ "AND status = 'A' \r\n"
				+ "AND ROWNUM <=10";
		String errorValidarCondiciones = "No se mostro informacion para procesar";
		SQLResult resultValidarCondiciones;
		
		//Paso 4
		String queryConexionFCWMLQA = "SELECT * FROM WM_LOG_RUN\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultConexionFCWMLQA;
		
		//Paso 5
		String queryValidaEjecucionInterfaz = "SELECT RUN_ID, INTERFACE, START_DT, STATUS FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE LIKE '%PR2_CO%'\r\n"
				+ "AND trunc(START_DT) = '"+fecha_actual+"'\r\n"
				+ "AND ROWNUM <=10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorValidaEjecucionInterfaz = "No se encontro ningun registro de ejecucion con START_DT: "+fecha_actual;
		SQLResult resultValidaEjecucionInterfaz;
		
		
		
	/*** Pasos****************************************************/
		
//Paso 1
		step("Establecer conexión con la BD BDRMSCQA");
		o.logHost(FCWMLQA_HOST);
		resultConexionBDRMSCQA = ejecutaQuery(queryConexionBDRMSCQA, BDRMSCQA);
		if (!resultConexionBDRMSCQA.isEmpty()) {
			o.log(succesfulConnection);
		}else {
			o.mensajeError(defaultConnectionError);
		}
		
//Paso 2
		step("Validar que se cumplan las condiciones del archivo a procesar");
		resultValidarCondiciones = ejecutaQuery(queryValidarCondiciones, BDRMSCQA);
		o.validaRespuestaQuery(resultValidarCondiciones);
		if (resultValidarCondiciones.isEmpty()) {
			o.muestraError(queryValidarCondiciones, errorValidarCondiciones);
		}
		
//Paso 3
		step("Ejecutar el servicio Pub:run:HSC referente a la interfaz PR2_CO mediante el Software AG Designer");		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String url = "http://" + user + ":" + password + "@" + server + ":5555";
		String encurl = "http://" + user + ":" + encpassword + "@" + server + ":5555";
		boolean bandera = false;
		
		try {
			u.get(url);
			bandera = true;
		} catch (Exception e) {
			o.mensajeError("Surgio un problema al ingresar a "+encurl);
		}
		if (bandera) {
			pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("service"));
		}
		
//Paso 4
		step("Establecer conexión con la BD WMLOG_FCWMLQA");
		o.logHost(FCWMLQA_HOST);
		resultConexionFCWMLQA = ejecutaQuery(queryConexionFCWMLQA, FCWMLQA);
		if (!resultConexionFCWMLQA.isEmpty()) {
			o.log(succesfulConnection);
		}else {
			o.mensajeError(defaultConnectionError);
		}
		
//Paso 5
		step("Validar que la interfaz haya terminado exitosamente");
		resultValidaEjecucionInterfaz = ejecutaQuery(queryValidaEjecucionInterfaz, FCWMLQA);
		o.validaRespuestaQuery(resultValidaEjecucionInterfaz);
		if (!resultValidaEjecucionInterfaz.isEmpty()) {
			String run_id = resultValidaEjecucionInterfaz.getData(0, "RUN_ID");
			String status = resultValidaEjecucionInterfaz.getData(0, "STATUS");
			if (!status.equals("E")) {
				o.mensajeError("El registro con RUN_ID: "+run_id+" tiene un estatus diferente al esperado: "+status);
			}else {
				o.log("El registro con RUN_ID:"+run_id+" indica que la interfaz termino con el estatus esperado: "+status);
			}
			
		}else {
			o.muestraError(queryValidaEjecucionInterfaz, errorValidaEjecucionInterfaz);
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
		return "*Tener acceso a la BD BDRMSCQA \r\n"
				+ "\r\n"
				+ "*Tener acceso a la BD WMLOG_FCWMLQA \r\n"
				+ "\r\n"
				+ "*Tener acceso a la herramienta Oracle SQL Developer \r\n"
				+ "\r\n"
				+ "*Tener acceso a la Software AG Designer";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar error en WMLOG al procesar la interfaz";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PR2_CO_ValidarErrorWMLOG";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
