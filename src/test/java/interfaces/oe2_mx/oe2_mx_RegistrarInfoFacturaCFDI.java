package interfaces.oe2_mx;



import static org.testng.Assert.assertTrue;

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

public class oe2_mx_RegistrarInfoFacturaCFDI extends BaseExecution{
	
	/*
	 * 
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp MTC-FT-002- Registrar informacion de la factura al generar un CFDI.
	 * @projectname LOGV2
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	
	public void ATC_FT_006_OE2_MX_RegistrarInfoFacturaCFDI_test(HashMap<String,String> data) throws Exception{
		
		/*** Utileria****************************************************/
		//AVEBQA, paso 1, 2 y 7
		String hostAVEBQA = GlobalVariables.DB_HOST_AVEBQA_UPD;
		String userAVEBQA = GlobalVariables.DB_USER_AVEBQA_UPD;
		String passwordAVEBQA = GlobalVariables.DB_PASSWORD_AVEBQA_UPD;
		SQLUtil dbEBS = new SQLUtil(hostAVEBQA, userAVEBQA, passwordAVEBQA);
		
		//FCWMLQA.FEMCOM.NET paso 4, 5 6 y 7
		String hostFCWMLQA = GlobalVariables.DB_HOST_FCWMLQA;
		String userFCWMLQA = GlobalVariables.DB_USER_FCWMLQA;
		String passwordFCWMLQA = GlobalVariables.DB_PASSWORD_FCWMLQA;
		SQLUtil FCWMLQA = new SQLUtil(hostFCWMLQA, userFCWMLQA, passwordFCWMLQA);
		
		Util o = new Util(testCase);
		
		/*** Variables****************************************************/
		String defaultError = "El query ejecutado no devolvio ningun registro";
		String successfulConnection = "Conexion establecida con exito";
		String anio = data.get("anio");
		String mes = data.get("mes");
		String start_date = data.get("start_date");
		
		//paso 1
		String queryPruebaConexion = "SELECT * FROM XXFC.XXFC_CFD_FACTURA_DIGITAL WHERE ROWNUM<=10";
		SQLResult resultPruebaConexionAVEBQA;
		String errorPruebaConexionAVEBQA = "No se pudo establecer una conexion con "+hostAVEBQA;
		
		//paso 2
		String queryRegistrosPendientes = "SELECT id_factura_digital, version_cfdi, rec_rfc, rec_regimen_fiscal, rec_rfc, wm_status \r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE VERSION_CFDI ='4.0' \r\n"
				+ "AND ANIO='"+anio+"' \r\n"
				+ "AND MES='"+mes+"' \r\n"
				+ "AND ORIGEN ='AR' \r\n"
				+ "AND WM_STATUS = 'L' \r\n"
				+ "AND rownum <= 10";
		SQLResult resultRegistrosPendientes;
		String errorRegistrosPendientes = "No existen registros pendientes a ser procesados en AVEBQA";
		
		//paso 4
		String queryPruebaConexionFCWMLQA = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE ROWNUM <=10";
		SQLResult resultPruebaConexionFCWMLQA;
		String errorPruebaConexionFCWMLQA = "No se pudo establecer una conexion con "+hostFCWMLQA;
		
		//paso 5
		String queryEstatusEjecucionFCWMLQA = "Select RUN_ID, STATUS, SERVER FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'OE2_MX'\r\n"
				+ "AND SERVER = 'fcwmintqa3'\r\n"
				+ "AND START_DT>='"+start_date+"' \r\n"
				+ "AND ROWNUM <= 10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorEstatusEjecucionFCWMLQA = "No existe ningun registro con estatus en ejecucion para FCWMLQA";
		SQLResult resultEstatusEjecucionFCWMLQA;
		
		//paso 6
		String errorEstatusThreads = "No existe ningun registro de los threads para FCWMLQA";
		SQLResult resultEstatusThreads;
		
		//paso 7
		SQLResult resultRegistrosProcesadosAVEBQA;
		String errorRegistrosProcesadosAVEBQA = "No existen registros timbrados actualizados para AVEBQA";
		
/******************** Pasos ****************************************************/
		
//Paso 1
		step("Ingresar a la BD AVEBQA");
		o.logHost(hostAVEBQA);
		resultPruebaConexionAVEBQA =  ejecutaQuery(queryPruebaConexion, dbEBS);
		if (resultPruebaConexionAVEBQA.isEmpty()) {
			o.muestraError(queryPruebaConexion,errorPruebaConexionAVEBQA);
		}else {
			o.log(successfulConnection);
		}
		
//Paso 2
		step("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz");
		resultRegistrosPendientes = ejecutaQuery(queryRegistrosPendientes, dbEBS);
		o.validaRespuestaQuery(resultRegistrosPendientes);
		//recupera el primer registro por procesar
		String id_factura_digital = "";
		if (!resultRegistrosPendientes.isEmpty()) {
			id_factura_digital = resultRegistrosPendientes.getData(0, "ID_FACTURA_DIGITAL");
			o.log("Recupera id_factura_digital de un registro por procesar: "+id_factura_digital);
		}else {
			o.muestraError(queryRegistrosPendientes, errorRegistrosPendientes);
		}
		
		
//Paso 3
		step("Ejecutar el servicio OE2_MX.Pub:run");			
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String password = PasswordUtil.decryptPassword(data.get("password"));
		String encps = data.get("password");
		String server = data.get("server");
		String url = "http://" + user + ":" + password + "@" + server + ":5555";
		String encurl = "http://" + user + ":" + encps + "@" + server + ":5555";
		boolean bandera = false;
		
		try {
			u.get(url);
			bandera = true;
		} catch (Exception e) {
			System.out.println("Surgio un problema al ingresar a "+encurl);
			assertTrue(false,"Surgio un problema al ingresar a "+encurl);
		}
		if (bandera) {
			pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("service"));
		}
		
				

//Paso 4
		step("Realizar conexion a la BD FCWMLQA.FEMCOM.NET");
		o.logHost(hostFCWMLQA);
		resultPruebaConexionFCWMLQA = ejecutaQuery(queryPruebaConexionFCWMLQA, FCWMLQA);
		if (resultPruebaConexionFCWMLQA.isEmpty()) {
			o.muestraError(queryPruebaConexionFCWMLQA,errorPruebaConexionFCWMLQA);
		}else {
			o.log(successfulConnection);
		}
//Paso 5
		step("Ejecutar el siguiente query en la BD FCWMLQA.FEMCOM.NET para validar el estatus de la ejecucion de la interfaz");
		resultEstatusEjecucionFCWMLQA = ejecutaQuery(queryEstatusEjecucionFCWMLQA, FCWMLQA);
		//almaceno el valor del id del primer registro encontrado para utilizarlo en el siguiente paso
		o.validaRespuestaQuery(resultEstatusEjecucionFCWMLQA);
		String run_id = "";
		if (!resultEstatusEjecucionFCWMLQA.isEmpty()) {
			run_id = resultEstatusEjecucionFCWMLQA.getData(0, "RUN_ID");
			o.log("Recupera run_id del primer registro, de la interfaz ejecutada: "+run_id);
		}else {
			o.muestraError(queryEstatusEjecucionFCWMLQA, errorEstatusEjecucionFCWMLQA);
		}
		

//Paso 6
		step("Ejecutar el siguiente query para validar el estatus del threads, en la tabla WM_LOG_THREAD de la BD FCWMLQA");
			String queryEstatusThreads = "SELECT THREAD_ID, NAME, STATUS FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID = "+run_id;
			resultEstatusThreads = ejecutaQuery(queryEstatusThreads, FCWMLQA);
			o.validaRespuestaQuery(resultEstatusThreads);
			if (resultEstatusThreads.isEmpty()) {
				o.muestraError(queryEstatusThreads, errorEstatusThreads);
			}
		
//Paso 7
		step("Ejecutar el siguiente query en la BD AVEBQA para validar los registros procesados se muestre actualizado");
			String queryRegistrosProcesadosAVEBQA = "SELECT id_factura_digital, version_cfdi, rec_rfc, rec_regimen_fiscal, rec_rfc, wm_status  \r\n"
					+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL  \r\n"
					+ "WHERE VERSION_CFDI ='4.0' \r\n"
					+ "AND ANIO='"+anio+"' \r\n"
					+ "AND MES='"+mes+"' \r\n"
					+ "AND WM_STATUS !='L' \r\n"
					+ "AND ID_FACTURA_DIGITAL='"+id_factura_digital+"' \r\n"
					+ "AND ORIGEN ='AR'";
			resultRegistrosProcesadosAVEBQA = ejecutaQuery(queryRegistrosProcesadosAVEBQA, dbEBS);
			o.validaRespuestaQuery(resultRegistrosProcesadosAVEBQA);
			if (resultRegistrosProcesadosAVEBQA.isEmpty()) {
				o.muestraError(queryRegistrosProcesadosAVEBQA, errorRegistrosProcesadosAVEBQA);
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
		return "Contar con acceso a la BD AVEBQA. \r\n"
				+ "\r\n"
				+ "Contar con facturas desde EBS, en la tabla XXFC_CFD_FACTURA_DIGITAL listas para procesar. \r\n"
				+ "\r\n"
				+ "Contar con acceso a la BD FCWMLQA.FEMCOM.NET con host oxfwm6q00.femcom.net";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que la interfaz actualiza el estatus de la factura en la base de datos de AR en la tabla XXFC-FACTURA-DIGITAL.WM_STATUS = E. Se registra la informacion del CFD1 en la Base de datos de AR";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_OE2_MX_RegistrarInfoFacturaCFDI_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
