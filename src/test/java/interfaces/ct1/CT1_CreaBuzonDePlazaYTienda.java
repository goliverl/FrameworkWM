package interfaces.ct1;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class CT1_CreaBuzonDePlazaYTienda extends BaseExecution{
	
	/**
	 * 
	 * Modificado por mantenimiento.
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp Verificar el procesamiento de la interfaz - Crear el Buzon de la Plaza y Tienda (runBuzon).
	 * @projectname LOGV2
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_CT1_CreaBuzonDePlazaYTienda(HashMap<String, String> data) throws Exception {
		
		
	
		
/* Utilerias *********************************************************************/		
		
		//dbCNT
		String FCWMLQA_HOST = GlobalVariables.DB_HOST_FCWMLQA;
		String FCWMLQA_USER = GlobalVariables.DB_USER_FCWMLQA;
		String FCWMLQA_PASS = GlobalVariables.DB_PASSWORD_FCWMLQA;
		SQLUtil FCWMLQA = new SQLUtil(FCWMLQA_HOST, FCWMLQA_USER, FCWMLQA_PASS);
		
		//dbLog
		String FCIASQA_HOST = GlobalVariables.DB_HOST_FCIASQA;
		String FCIASQA_USER = GlobalVariables.DB_USER_FCIASQA;
		String FCIASQA_PASS = GlobalVariables.DB_PASSWORD_FCIASQA;
		SQLUtil FCIASQA = new SQLUtil(FCIASQA_HOST, FCIASQA_USER, FCIASQA_PASS);
		
		Util o = new Util(testCase);
		
	
/**
* Variables ******************************************************************************************
* 
*/
		//Data Provider Variables
//		String service = data.get("servicio");
		String plaza = data.get("plaza");
		String tienda = data.get("tienda");
//		String interfaz = data.get("interface");
		String server = data.get("server");
		String user = data.get("user");
//		String encps = data.get("ps");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		
		//Generic Variables
		String fecha = o.generaFecha();
		
		//Query variables
		String run_id = "";
		
	    //Paso 1
		String queryVerificaInsumos = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n" + 
				"FROM WM_BUZONES_T_TIENDAS \r\n" + 
				"WHERE WM_STATUS_BUZON = 'L' \r\n" + 
				"AND CR_PLAZA = '" + plaza +"' \r\n" + 
				"AND CR_TIENDA = '" + tienda +"'\r\n"+
				"AND ROWNUM <=10 \r\n" + 
				"ORDER BY CREATION_DATE";
		String errorVerificaInsumos = "No existen registros con plaza: "+plaza+", tienda: "+tienda+" y wm_status_buzon: L";
		SQLResult resultVerificaInsumos;
			      
		//Paso 3
		String  queryVerificarEjecucion = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'CT1_BUZON' \r\n"
				+ "AND TRUNC(START_DT) = '"+fecha+"'  \r\n"
				+ "AND STATUS = 'S' \r\n"
				+ "AND ROWNUM <=10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorVerificarEjecucion = "No se encontro ningun registro de ejecucion exitosa de la interfaz";
		SQLResult resultVerificarEjecucion;
		
		String queryVerificarThread;
		String errorVerificarThread;
		SQLResult resultVerificarThread;
				
		//Paso 5
		String queryVerificaCambioStatus;
		String errorVerificaCambioStatus = "La consulta realizada no devolvio ningun registro";
		SQLResult resultVerificaCambioStatus;
		      
		
		      
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//**********************************************Paso 1	****************************************************************************
				

		step("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' ");
		o.logHost(FCIASQA_HOST);
		resultVerificaInsumos = ejecutaQuery(queryVerificaInsumos, FCIASQA);
		o.validaRespuestaQuery(resultVerificaInsumos);
		if (resultVerificaInsumos.isEmpty()) {
			o.muestraError(errorVerificaInsumos, queryVerificaInsumos);
		}

		
		
//**********************************************Paso 2	****************************************************************************** 
		
		
		step("Ejecutar el JOB runCT1Buzon desde Control M para invocar la interface por medio del servicio CT1.Pub:runBuzon ");
		// Utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String url = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(url);
		String dateExecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("servicio"));
		o.log("Respuesta dateExecution" + dateExecution);


//*******************************************************Paso 3************************************************************************
		
		step("Validar que la interface haya finalizado correctamente en el WMLOG");
		o.logHost(FCWMLQA_HOST);
		resultVerificarEjecucion = ejecutaQuery(queryVerificarEjecucion, FCWMLQA);
		o.validaRespuestaQuery(resultVerificarEjecucion);
		if (!resultVerificarEjecucion.isEmpty()) {
			run_id = resultVerificarEjecucion.getData(0, "RUN_ID"); 
			o.log("Recupera el run_id: "+run_id+" del primer registro encontrado");
		}else {
			o.muestraError(errorVerificarEjecucion, queryVerificarEjecucion);
		}
		
		queryVerificarThread = "SELECT THREAD_ID, NAME, START_DT, STATUS FROM WMLOG.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = "+run_id+"\r\n"
				+ "AND STATUS = 'S'";
		errorVerificarThread = "No se encontro ningun thread con parent_id: "+run_id;
		resultVerificarThread = ejecutaQuery(queryVerificarThread, FCWMLQA);
		o.validaRespuestaQuery(resultVerificarThread);
		if (resultVerificarThread.isEmpty()) {
			o.muestraError(errorVerificarThread, queryVerificarThread);
		}
		
//**************************************************Paso 4 **************************************************************************
		
//		step("Verificar que el directorio fue creado en el filesystem");
		//No especfica credenciales ni servidor
		
//**************************************************Paso 5 ***************************************************************************		
		
		step("Verificar que se actualice el registro de la tabla "
		+ "WM_BUZONES_T_TIENDAS, WM_STATUS_BUZON = E,WM_RUN_ID = [WM_LOG_RUN.RUN_ID], WM_FECHA_PROC = SYSDATE.");
		queryVerificaCambioStatus = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_RUN_ID, WM_FECHA_PROC\r\n"
					+ "FROM WM_BUZONES_T_TIENDAS \r\n"
					+ "WHERE WM_STATUS_BUZON = 'E' \r\n"
					+ "AND CR_PLAZA = '"+plaza+"'\r\n"
					+ "AND CR_TIENDA = '"+tienda+"'\r\n"
					+ "AND TRUNC(WM_FECHA_PROC) = '"+fecha+"'\r\n"
					+ "AND WM_RUN_ID = '"+run_id+"'\r\n"
					+ "ORDER BY CREATION_DATE";
		resultVerificaCambioStatus = ejecutaQuery(queryVerificaCambioStatus, FCIASQA);
		o.validaRespuestaQuery(resultVerificaCambioStatus);
		if (resultVerificaCambioStatus.isEmpty()) {
			o.muestraError(errorVerificaCambioStatus, queryVerificaCambioStatus);
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
		return "Automatiza el proceso de creacion de buzones para tiendas nuevas en webMethods";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_CT1_CreaBuzonDePlazaYTienda";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}