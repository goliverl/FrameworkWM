package interfaces.ct1_cl;

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

public class CT1_CL_CreaPartner extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_CT1_CL_Verificar_procesamiento_Crear_Partner(HashMap<String, String> data) throws Exception {

	/**
	 * ALM (son los pasos de la de mexico pero con bases de datos de chile)
	 * Verificar el procesamiento de la interfaz - Crear un nuevo Partner 22MDB57JTN (runTN)
	 */
		
		/*
		 * 
		 * Modificado por mantenimiento.
		 * @author Brandon Ruiz.
		 * @date   13/02/2023.
		 * @cp Verificar el procesamiento de la interfaz - Crear un nuevo Partner de plaza y tienda (runTN).
		 * @projectname LOGV2
		 *
		 */

		/*Utilerias*********************************************************************/

		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		
		SQLUtil dbCNTCL = new SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,
				GlobalVariables.DB_PASSWORD_CNTCHILE);
		
		Util o = new Util(testCase);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 */
		
		//Variables Generales
		String run_id = "";
		String cr_plaza = "";
		String cr_tienda = "";
		
		// Paso 1
		//comentado puesto que no hay plazas ni tiendas predefinidas
//		String ValidaSatusL = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON \r\n"
//				+ "FROM WM_BUZONES_T_TIENDAS WHERE WM_STATUS_BUZON = 'L' \r\n" 
//				+ "AND CR_PLAZA = '"+ data.get("plaza")+"' AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
//				+ "AND ROWNUM <= 10"
//				+ "ORDER BY CREATION_DATE";
		
		String queryPaso1 = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON \r\n"
				+ "FROM WMUSER.WM_BUZONES_T_TIENDAS WHERE WM_STATUS_BUZON = 'L' \r\n"
				+ "AND ROWNUM <= 10 ORDER BY CREATION_DATE";

		// Paso 3
		String queryPaso3 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE LIKE '%CT1_CL_TN%' \r\n"
				+ "AND START_DT >= TRUNC(SYSDATE) \r\n"
				+ "AND ROWNUM <= 10 AND STATUS = 'S' \r\n"
				+ "ORDER BY START_DT DESC";

		// Paso 4
		String queryPaso4 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";

		// Paso 5
		//comentado puesto que no hay plazas ni tiendas predefinidas
//		String ValidaSatusE = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC\r\n"
//				+ "   FROM WM_BUZONES_T_TIENDAS \r\n" + "WHERE WM_STATUS_BUZON = 'E' \r\n" + "     AND CR_PLAZA = '"
//				+ data.get("plaza") + "'\r\n" + "     AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
//				+ "     AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)\r\n" + "     AND WM_RUN_ID = '%s'\r\n"
//				+ " ORDER BY CREATION_DATE ";

		String queryPaso5;
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//**********************************************Paso 1	****************************************************************************

		step("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' ");
		o.logHost(GlobalVariables.DB_HOST_CNTCHILE);
		SQLResult resultPaso1 = ejecutaQuery(dbCNTCL, queryPaso1);
		o.validaRespuestaQuery(resultPaso1);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1, "No se encontraron registros con wm_status_buzon: L");
		}else {
			cr_plaza = resultPaso1.getData(0, "CR_PLAZA");
			cr_tienda = resultPaso1.getData(0, "CR_TIENDA");
			o.log("Recupera cr_plaza: "+cr_plaza+" , cr_tienda: "+cr_tienda+" del primer registro");
		}

//********************************************Script 2 y 3 ******************************************************************************
		// Verificar que no exista el perfil en el TN, orgUnitName = [PLAZA][TIENDA].

//**********************************************Paso 2	****************************************************************************** 
		step("Ejecutar el JOB runCT1Buzon desde Control M para invocar la interface por medio del servicio CT1.Pub:runBuzon ");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String url = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(url);
		String dateExecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("servicio"));
		o.log("Respuesta dateExecution" + dateExecution);
		
//*******************************************************Paso 3************************************************************************
		step("Validar que la interface haya finalizado correctamente en el WMLOG");
		o.logHost(GlobalVariables.DB_HOST_LogChile);
		SQLResult resultPaso3 = ejecutaQuery(dbLogCL, queryPaso3);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.muestraError(queryPaso3, "Error en la ejecucion de la interfaz");
		}else {
			run_id = resultPaso3.getData(0, "RUN_ID");
			o.log("Recupera run_id: "+run_id+" del primer registro");
		}

//**************************************************Paso 4 **************************************************************************
		step("Verificar que los threads de la interfaz finalizaron correctamente");
		o.logHost(GlobalVariables.DB_HOST_LogChile);
		String queryPaso4_1 = String.format(queryPaso4, run_id);
		SQLResult resultPaso4 = ejecutaQuery(dbLogCL,queryPaso4_1);
		o.validaRespuestaQuery(resultPaso4);
		if (resultPaso4.isEmpty()) {
			o.muestraError(queryPaso4_1, "No se encontro ningun thread con parent_id: "+run_id);
		}

//*****************************************Script 2 y 3*************************************************************************************
		// Verificar que el perfil del partner fue creado en el TN, CorporationName =
		// OXXO, orgUnitName = [PLAZA][TIENDA], Status = Active, Type = Other.

//*****************************************Script 2 y 3 *************************************************************************************
		/*
		 * Verificar que el password de los servidores de integraci�n fueron cambiados,
		 * servidores local y remotos.
		 */

//**************************************************Paso 5 ***************************************************************************		

		step("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS, WM_STATUS_BUZON = E,WM_RUN_ID = [WM_LOG_RUN.RUN_ID], WM_FECHA_PROC = SYSDATE.");
		o.logHost(GlobalVariables.DB_HOST_CNTCHILE);
		//modificar la consulta si se tienen plazas y tiendas predefinidas
		queryPaso5 = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC \r\n"
				+ "FROM WM_BUZONES_T_TIENDAS WHERE WM_STATUS_BUZON = 'E' \r\n" 
				+ "AND CR_PLAZA = '"+ cr_plaza + "' AND CR_TIENDA = '" + cr_tienda + "'\r\n"
				+ "AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE) \r\n" 
				+ "AND WM_RUN_ID = '%s'\r\n"
				+ "ORDER BY CREATION_DATE ";
		String queryPaso5_1 = String.format(queryPaso5, run_id);
		SQLResult resultPaso5 = ejecutaQuery(dbCNTCL, queryPaso5_1);
		o.validaRespuestaQuery(resultPaso5);
		if (resultPaso5.isEmpty()) {
			o.muestraError(queryPaso5_1,"No se actualizo correctamente el registro");
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
	
	private SQLResult ejecutaQuery(SQLUtil obj, String query) throws Exception{
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
		return "Verificar el procesamiento de la interfaz Crear Partner";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_CT1_CL_Verificar_procesamiento_Crear_Partner";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
