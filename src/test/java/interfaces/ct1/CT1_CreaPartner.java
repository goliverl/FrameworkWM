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

public class CT1_CreaPartner extends BaseExecution{
	
	/*
	 * 
	 * Modificado por mantenimiento.
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp Verificar el procesamiento de la interfaz - Crear un nuevo Partner de plaza y tienda (runTN).
	 * @projectname LOGV2
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_CT1_CreaPartner(HashMap<String, String> data) throws Exception {
		
		
		
		/**
		 * ALM
		 *   Este script cubre los siguientes casos de prueba: (A excepcion de los pasos que estan comentados)
		  
          -Verificar el procesamiento de la interfaz - Crear un nuevo Partner 22MDB57JTN (runTN).
          -Verificar el procesamiento de la interfaz - Crear un nuevo Partner de plaza y tienda (runTN).
	    */
		
/* Utilerias *********************************************************************/		
		
		String dbLog_HOST = GlobalVariables.DB_HOST_FCWMLQA;
		SQLUtil dbLog = new SQLUtil(dbLog_HOST, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		String dbCNT_HOST = GlobalVariables.DB_HOST_FCIASQA;
		SQLUtil dbCNT = new SQLUtil(dbCNT_HOST, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		Util o = new Util(testCase);
	
/**
* Variables ******************************************************************************************
* 
*/
			//Data Provider
			String plaza = data.get("plaza");
			String tienda = data.get("tienda");
			
			//Generales
			String run_id = "";
			String defaultError = "La consulta realizada no devolvio ningun registro";
			
		    //Paso 1
			String queryPaso1 = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n" + 
					"FROM WM_BUZONES_T_TIENDAS \r\n" + 
					"WHERE WM_STATUS_BUZON = 'L' \r\n" + 
					"AND CR_PLAZA = '" + plaza +"' \r\n" + 
					"AND CR_TIENDA = '" + tienda +"'\r\n"+
					"AND ROWNUM <=10" + 
					"ORDER BY CREATION_DATE";
			SQLResult resultPaso1;
			      
			//Paso 3
		  	String  queryPaso3 ="SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER \r\n"+
		  			  "FROM WMLOG.WM_LOG_RUN \r\n" +
				      "WHERE INTERFACE = 'CT1_TN' \r\n" +
				      "AND START_DT >= TRUNC(SYSDATE) \r\n"  +
				      "AND STATUS = 'S' \r\n"+
				      "AND ROWNUM < = 10"+
				      "ORDER BY START_DT DESC"; 
		  	SQLResult resultPaso3;
		      
		  	//Paso 4
		  	String queryPaso4 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s'";
		  	SQLResult resultPaso4;
				
			//Paso 5
			String queryPaso7= "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC\r\n" + 
						"FROM WM_BUZONES_T_TIENDAS \r\n" + 
						"WHERE WM_STATUS_BUZON = 'E' \r\n" + 
						"AND CR_PLAZA = '" + plaza +"'\r\n" + 
						"AND CR_TIENDA = '" + tienda +"'\r\n" + 
						"AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)\r\n" + 
						"AND WM_RUN_ID = '%s'\r\n" + 
						"ORDER BY CREATION_DATE ";
			SQLResult resultPaso7;
		      
		
		      
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//**********************************************Paso 1	****************************************************************************
				
		step("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' ");
		o.logHost(GlobalVariables.DB_HOST_FCIASQA);
		resultPaso1 = ejecutaQuery(queryPaso1, dbCNT);
		o.validaRespuestaQuery(resultPaso1);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1, defaultError);
		}
		
//********************************************Paso 2 ******************************************************************************
	//	Verificar que no exista el perfil en el TN, orgUnitName = [PLAZA][TIENDA].

		
		
//**********************************************Paso 3	****************************************************************************** 
		step("Ejecutar el JOB runCT1Buzon desde Control M para invocar la interface por medio del servicio CT1.Pub:runBuzon ");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String url = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(url);
		String dateExecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("servicio"));
		o.log("Respuesta dateExecution: " + dateExecution);

//*******************************************************Paso 4************************************************************************
		
		step("Validar que la interface haya finalizado correctamente en el WMLOG");
		resultPaso3 = ejecutaQuery(queryPaso3, dbLog);
		o.validaRespuestaQuery(resultPaso3);
		if (!resultPaso3.isEmpty()) {
			run_id = resultPaso3.getData(0, "RUN_ID");
			o.log("recupera run_id: "+run_id+" del primer registro");
		}else {
			o.muestraError(queryPaso3, "Error en la ejecucion de la interfaz");
		}
		
		o.logBold("Verificar que los threads de la interfaz finalizaron correctamente.");
		String queryPaso4_1 = String.format(queryPaso4, run_id);
		resultPaso4 = ejecutaQuery(queryPaso4_1, dbLog);
		o.validaRespuestaQuery(resultPaso4);
		if (resultPaso4.isEmpty()) {
			o.muestraError(queryPaso4, "No se encontro ningun thread con parent_id: "+run_id);
		}
		
//*****************************************Paso 5*************************************************************************************
	//	Verificar que el perfil del partner fue creado en el TN, CorporationName = OXXO, orgUnitName = [PLAZA][TIENDA], Status = Active, Type = Other.
		
		
//*****************************************Paso 6*************************************************************************************
		
		/*Verificar que el password de los servidores de 
		integracion fueron cambiados, servidores local y remotos.*/

		

//**************************************************Paso 7***************************************************************************		
		
		step("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS, WM_STATUS_BUZON = E,WM_RUN_ID = [WM_LOG_RUN.RUN_ID], WM_FECHA_PROC = SYSDATE.");
        String queryPaso7_1 = String.format(queryPaso7, run_id);
        resultPaso7 = ejecutaQuery(queryPaso7_1, dbCNT);
        o.validaRespuestaQuery(resultPaso7);
		if (resultPaso7.isEmpty()) {
			o.muestraError(queryPaso7_1,"No se actualizo correctamente el registro");
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
		return "ATC_FT_006_CT1_CreaPartner";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	


}
