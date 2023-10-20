package interfaces.CS01;

import static org.testng.Assert.assertFalse;


import java.util.HashMap;


import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;

import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class CS01_Actualizar_como_ERROR_Tienda_con_Bitacora_ID_69381 extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_CS01_Actualizar_como_ERROR_Tienda_con_Bitacora_ID_69381(HashMap<String, String> data) throws Exception {
		
/* Utilerias *********************************************************************/		
		
		SQLUtil dbPuser = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		/**
		 * ALM
		 * Actualizar como ERROR (X) la Tienda con Bitacora_ID igual a 69381.
		 */
	
		
/**
* Variables ******************************************************************************************
* 
*/
		
//		Paso 1
		
		String ConsConfig = "SELECT  INTERFASE, ENTIDAD, OPERACION,CATEGORIA, DESCRIPCION"
				+ " FROM WMUSER.WM_INTERFASE_CONFIG "+
				"WHERE INTERFASE = 'CS01' "+
						"AND ENTIDAD = 'WM' "+
						"AND OPERACION = 'SENDIDOC' "+
						"AND CATEGORIA = 'CONFIG'";
		
		
		
		
		//Paso 2
		
		String CompReg = "SELECT WM_RUN_ID, WM_STATUS, BITACORA_ID, WM_REC_DT "
				+ "FROM TIENDAS.XX_NOMCOM_CNT_SAP "
				+ "WHERE WM_STATUS = 'L' AND BITACORA_ID =  '" + data.get("BITACORA_ID") +"'";  	
		
	
			      
	   //Paso 4
		
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'CS01'" 	
				 +" and  start_dt >= TRUNC(SYSDATE)"
			     +" order by start_dt desc)"
				+ " where rownum = 1";	
		
		String ValidEstatus= "select * from (SELECT RUN_ID, INTERFACE, start_dt, END_DT, STATUS "
		+ "FROM WMLOG.WM_LOG_RUN "
		+ "WHERE INTERFACE = 'CS01' "
		+ "and  start_dt >= TRUNC(SYSDATE)"
		+ "AND STATUS = 'E' ORDER BY START_DT DESC) where rownum <=1"; 
	


		      
	 //Paso 5
		      
		   
		      String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s' and status = 'E'";
		      

				String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s' and status = 'E'";
			
				
	//Paso 6
			String ValidaProc =	"SELECT WM_RUN_ID, WM_STATUS, BITACORA_ID, WM_REC_DT "
				+ "FROM TIENDAS.XX_NOMCOM_CNT_SAP "
				+ "WHERE WM_STATUS = 'X' "
				+ "AND BITACORA_ID =  '" + data.get("BITACORA_ID") +"' "
				+ "AND WM_RUN_ID = '%s' "
				+ "AND WM_REC_DT >= TRUNC(SYSDATE)";
	


		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//**********************************************Paso 1	****************************************************************************

			
				addStep("Consultar la configuracion en la tabla  WM_INTERFASE_CONFIG de la BD WMUSER.");
				
				System.out.println(GlobalVariables.DB_HOST_Puser);
				System.out.println(ConsConfig);
				
				SQLResult ConsultaConfig = executeQuery(dbPuser, ConsConfig);
				
				boolean ConsultaConfigur = ConsultaConfig.isEmpty();
				
					if (!ConsultaConfigur) {
				
					testCase.addQueryEvidenceCurrentStep(ConsultaConfig);
					
				
					}
					
				System.out.println(ConsultaConfigur);

				assertFalse(ConsultaConfigur, "No se encontro la configuracion en la tabla WM_INTERFASE_CONFIG" );
				
//********************************************Paso2*****************************************************************************

				 
				

addStep("Comprobar que existan registros en la tabla XX_NOMCOM_CNT_SAP de la BD CNT en donde WM_STATUS es igual a 'L'. ");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(CompReg);
		
		SQLResult ComprobarReg = executeQuery(dbCNT, CompReg);
		
		boolean validaReg = ComprobarReg.isEmpty();
		
			if (!validaReg) {
		
			testCase.addQueryEvidenceCurrentStep(ComprobarReg);
	

			}
			
		System.out.println(validaReg);

		assertFalse(validaReg, "No se encontro registros en la tabla XX_NOMCOM_CNT_SAP");
		

		
		
//**********************************************Paso 3	****************************************************************************** 
		//Comprobar que NO se pueda conectar con SAP al servidor FEMSERV_SAP
		//queda pendiente ya que no hay mas informacion de como validar que no haga conexion

//****************************************Paso 4*************************************************************************************	
		addStep("Ejecutar el servicio CS01.Pub:run");
	

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
				
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"),data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcQueryIntegrationServer);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(4);

		}

	
		
//*******************************************************Paso 5************************************************************************
		
		 
		
		addStep("Confirmar que exista registro de ejecucion de la interfaz en la tabla  WM_LOG_RUN de la BD WMLOG para la interfaz CS01 en estatus 'E'.");
			 
        System.out.println(ValidEstatus);
		
		SQLResult ValidarStatus = executeQuery(dbLog, ValidEstatus);
		
		boolean validaStatus = ValidarStatus.isEmpty();
		
			if (!validaStatus) {
		
			testCase.addQueryEvidenceCurrentStep(ValidarStatus);
			
						} 
		
		System.out.println(validaStatus);

		assertFalse(validaStatus, "El estatus de la ejecucion no es E");
		
		
		
//**************************************************Paso 6 **************************************************************************
		 

	
		addStep("Confirmar que existan los registros de los documentos procesados en la tabla WM_LOG_THREAD de la BD WMLOG.");
		
	

		String consulta1 = String.format(qry_threads1, run_id);
		
		System.out.println("CONSULTA THREAD " + consulta1);
		
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
	
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		assertFalse(threads1,"No se generaron threads en la tabla o el estatus de la ejecucion no fue E");
	
//**************************************************Paso 7 ***************************************************************************		
		

		addStep("Comprobar que se actualicen registros en la tabla XX_NOMCOM_CNT_SAP de la BD CNT en donde WM_STATUS es igual a 'X', y BITACORA_ID es igual a 69381.");
	
		
		String ValidRes = String.format(ValidaProc, run_id);

		System.out.println(ValidRes);
		SQLResult ValidResu = dbCNT.executeQuery(ValidRes);
		
		boolean Error1 = ValidResu.isEmpty();
		
		if (!Error1) {

		testCase.addQueryEvidenceCurrentStep(ValidResu);

		
		}
		
		assertFalse(Error1,"No se actualizo registros en la tabla XX_NOMCOM_CNT_SAP");
		
		//**********************************************Paso 7 **********************************************
		
//		addStep("Comprobar que se ejecuto correctamente el servicio SAP (FEMSERV_SAP).");
		 
		

		
	
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
		return " Construido. CS01 Actualizar como ERROR (X) la Tienda con Bitacora_ID igual a 69381.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_CS01_Actualizar_como_ERROR_Tienda_con_Bitacora_ID_69381";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}



