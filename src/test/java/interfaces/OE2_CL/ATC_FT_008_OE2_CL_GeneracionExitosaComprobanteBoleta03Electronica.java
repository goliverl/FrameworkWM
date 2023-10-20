package interfaces.OE2_CL;


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

public class ATC_FT_008_OE2_CL_GeneracionExitosaComprobanteBoleta03Electronica extends BaseExecution{
	
	/*
	 * 
	 * Modificado por mantenimiento.
	 * @author Brandon Ruiz.
	 * @date   30/01/2023.
	 * @cp MTC-FT-015 OE2_CL Generacion de forma electronica exitosa de comprobante Boleta(03) 
	 * por medio de la interface OE2_CL
	 * @projectname Actualizacion Tecnologica Webmethods
	 *
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_OE2_CL_GeneracionExitosaComprobanteBoleta03Electronica_test(HashMap<String, String> data) throws Exception {
		
		/****** Utilerias *********************************************************************/
		String dbEbsHost = GlobalVariables.DB_HOST_OIEBSBDQ;
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ,GlobalVariables.DB_USER_OIEBSBDQ, GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		String dbPosHost = GlobalVariables.DB_HOST_PosUserChile;
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		
		Util o = new Util(testCase);
	  
		
		/****** Variables *********************************************************************/
		//Default 
		String successfulConnection = "Consulta realizada con exito";
		String defaultError = "La consulta realizada no devolvio ningun registro";
		
		//Data Provider
		String server = data.get("server");
		String user = data.get("user");
		String encpass = data.get("ps");
		String password = PasswordUtil.decryptPassword(data.get("ps"));
		String interfaz = data.get("interfase");
		String servicio = data.get("servicio");
		String comprobante = data.get("comprobante");
		
		//Pasos
		String queryPaso1 = "SELECT ID_FACTURA_DIGITAL FROM XXFC.XXFC_FACTURA_DIGITAL WHERE ROWNUM <=10";
		SQLResult resultPaso1;
		
		String queryPaso2 = "SELECT ID_FACTURA_DIGITAL, TIPO_COMPROBANTE, WM_ESTATUS FROM APPS.XXFC_FACTURA_DIGITAL \r\n"
				+ "WHERE WM_ESTATUS = 'L' AND TIPO_COMPROBANTE = '"+comprobante+"' AND ROWNUM <=10";
		String errorPaso2 = "No se encontraron facturas con estatus: L";
		SQLResult resultPaso2;
		
		String queryPaso3;
		String errorPaso3;
		SQLResult resultPaso3;
		
		String queryPaso5 = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE ROWNUM <=10";
		SQLResult resultPaso5;
		
		String queryPaso6 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN  \r\n"
				+ "WHERE INTERFACE LIKE '%OE2_CL%' AND STATUS = 'S' \r\n"
				+ "AND START_DT >= TRUNC(SYSDATE) AND ROWNUM <=10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorPaso6 = "No se encontro registro de ejecucion con estatus: S";
		SQLResult resultPaso6;
		
		String queryPaso7;
		String errorPaso7;
		SQLResult resultPaso7;
		
		String queryPaso8;
		String errorPaso8;
		SQLResult resultPaso8;
		
		String queryPaso9;
		String errorPaso9;
		SQLResult resultPaso9;
		
		
		/*
		 * 
		 * El paso 4 engloba los pasos 4,5,6 y 7 de la matriz y evidencia manual.
		 * 
		 */
		
		
		/*****Paso 1******************************************************************************************************************/
		step("Establecer conexión de EBS Chile (OIEBSBDQ) con usuario APPSVIEW");
		o.logHost(dbEbsHost);
		resultPaso1 = ejecutaQuery(queryPaso1, dbEbs);
		if (resultPaso1.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		/*****Paso 2******************************************************************************************************************/
		step("Consultar la tabla APPS.XXFC_FACTURA_DIGITAL para validar las Notas de Crédito registradas en POS para la Plaza y la Tienda "
				+ "en la tabla  de Oracle a WM_ESTATUS = 'L'");
		resultPaso2 = ejecutaQuery(queryPaso2, dbEbs);
		o.validaRespuestaQuery(resultPaso2);
		String id_factura = "";
		if (resultPaso2.isEmpty()) {
			o.muestraError(queryPaso2, errorPaso2);
		}else {
			id_factura = resultPaso2.getData(0, "ID_FACTURA_DIGITAL");
			o.log("Recupera id_factura_digital: "+id_factura+" del primer registro");
		}
		
		/*****Paso 3******************************************************************************************************************/
		step("Consultar la tabla XXFC_FACTURA_DIGITAL_LINES para validar el detalle de las facturas global mensual "
				+ "de ventas registradas en POS para la Plaza y la Tienda en la tabla de Oracle a WM_STATUS = 'L'");
		queryPaso3 = "SELECT ID_FACTURA_DIGITAL, DESCRIPCION,CANTIDAD, PRECIO_UNITARIO, IMPORTE, CLAVE, CLAVE_IMPUESTO \r\n"
				+ "FROM XXFC.XXFC_FACTURA_DIGITAL_LINES WHERE ID_FACTURA_DIGITAL = '"+id_factura+"'";
		errorPaso3 = "No se encontro ninguna factura con el id_factura_digital: "+id_factura;
		resultPaso3 = ejecutaQuery(queryPaso3, dbEbs);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.muestraError(queryPaso3, errorPaso3);
		}
		
		/*****Paso 4******************************************************************************************************************/
		step("Ejecutar servicio OE2_CL.Pub:run para la interfaz OE2 Chile");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String url = "http://" + user + ":" + password + "@" + server + ":5555";
		String encurl = "http://" + user + ":" + encpass + "@" + server + ":5555";
		boolean bandera = false;
		try {
			u.get(url);
			bandera = true;
		} catch (Exception e) {
			o.mensajeError("Surgio un problema al ingresar a "+encurl);
		}
		if (bandera) {
			pok.runIntefaceWmTwoButtonsWihtoutInputs10_2(interfaz, servicio);
		}
		
		
		/*****Paso 5******************************************************************************************************************/
		step("Establecer conexión a la BD FCWMLQA de OI_Chile");
		o.logHost(dbPosHost);
		resultPaso5 = ejecutaQuery(queryPaso5, dbPos);
		if (resultPaso5.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		
		/*****Paso 6******************************************************************************************************************/
		step("Validar la correcta ejecución de la interface OE2 en la tabla WM_LOG_RUN de WMLOG");
		resultPaso6 = ejecutaQuery(queryPaso6, dbPos);
		o.validaRespuestaQuery(resultPaso6);
		String run_id = "";
		if (resultPaso6.isEmpty()) {
			o.muestraError(queryPaso6, errorPaso6);
		}else {
			run_id = resultPaso6.getData(0, "RUN_ID");
			o.log("Recupera run_id: "+run_id+" del primer registro");
		}
		
		/*****Paso 7******************************************************************************************************************/
		//En la documentacion comenta que la ejecucion de la interfaz no genera threads
		step("Validar la correcta ejecución de los Threads lanzados por la interface OE2 en la tabla WM_LOG_THREAD de WMLOG");
		queryPaso7 = "SELECT THREAD_ID, NAME, START_DT, STATUS FROM WMLOG.WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = '"+run_id+"' ORDER BY START_DT DESC";
		errorPaso7 = "No se encontro ningun thread con run_id"+run_id;
		resultPaso7 = ejecutaQuery(queryPaso7, dbPos);
		o.validaRespuestaQuery(resultPaso7);
		if (resultPaso7.isEmpty()) {
			o.log(errorPaso7);
		}
		
		/*****Paso 8******************************************************************************************************************/
		step("Ejecutar la siguiente consulta para comprobar que no se hayan generado errores "
				+ "de la ejecución de la interface OE2 de Chile");
		queryPaso8 = "SELECT RUN_ID, ERROR_DATE, DESCRIPTION \r\n"
				+ "FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID = '"+run_id+"'";
		errorPaso8 = "Se encontraron registros de errores para la ejecucion de la interfaz";
		resultPaso8 = ejecutaQuery(queryPaso8, dbPos);
		o.validaRespuestaQuery(resultPaso8);
		o.log("Consulta ejecutada: "+queryPaso8);
		if (resultPaso8.isEmpty()) {
			o.log("No se encontro ningun registro de error para la ejecucion de la interfaz");
		}else {
			o.mensajeError(errorPaso8);
		}
		
		/*****Paso 9******************************************************************************************************************/
		step("Validar la actualización del estatus (WM_STATUS = 'S'), los campos referentes a la factura digital "
				+ "en la tabla XXFC.XXFC_FACTURA_DIGITAL de Oracle");
		queryPaso9 = "SELECT ID_FACTURA_DIGITAL, TIPO_COMPROBANTE, WM_ESTATUS FROM APPS.XXFC_FACTURA_DIGITAL\r\n"
				+ "WHERE ID_FACTURA_DIGITAL = '"+id_factura+"' AND WM_ESTATUS = 'S'";
		errorPaso9 = "No se encontro ningun registro con id_factura_digital: "+id_factura+" y wm_estatus: S";
		resultPaso9 = ejecutaQuery(queryPaso9, dbEbs);
		o.validaRespuestaQuery(resultPaso9);
		if (resultPaso9.isEmpty()) {
			o.muestraError(queryPaso9, errorPaso9);
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
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_OE2_CL "
				+ "para generar la forma electronica de comprobante Boleta(03) con la informacion de Oracle Peru y "
				+ "la respuesta Exitosa por parte del proveedor ACEPTA, al ser migrada la interface de WM9.9 a WM10.5";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_008_OE2_CL_GeneracionExitosaComprobanteBoleta03Electronica_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

