package interfaces.eo06;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class EO06ProcesarArchivosWU extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO06_002_Procesar_Archivos_WU(HashMap<String, String> data) throws Exception {
		
/* Utiler�as *********************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA , GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	
		/**
		 * ALM
		 * Procesar archivos WesternUnion con prefijo REM_WUO para la plaza 10MON
		 * Procesar archivos WesternUnion con prefijo REM_WUV para la plaza 10MON
		 * Procesar archivos WesternUnion con prefijo REM_WUW para la plaza 10CAN
		 * 
		 */
		
// FALTAN INSUMOS PARA PROBAR 
		
/* Variables *********************************************************************/

		String status = "S";
		
		SeleniumUtil u;
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		
		String tdcQueryConfig = "SELECT trim(OPERACION), trim(VALOR1), trim(VALOR2), trim(VALOR3), trim(VALOR5), trim(VALOR6) "
//				+ " decode(VALOR4, NULL, NULL, wm_encryption.DECRYPT_DATA(trim(VALOR4),'wm6.1ftpkey')) " //con esta instruccion falla query srive para mostrar contrasea de servidor FTP en la consulta
				+ " FROM WMUSER.WM_INTERFASE_CONFIG "
				+ " WHERE INTERFASE = 'EO06' "
				+ " AND ENTIDAD = 'WU' "
				+ " AND OPERACION IN ('SFTP','FS') "
				+ " AND CATEGORIA = 'CONFIG'";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
	
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; //FCWMLQA
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; //FCWMLQA 
		

		
		String tdcQueryEnvioDinero = "SELECT ID_ENVIO_DINERO, NOMBRE_ARCHIVO,COMPANIA, ESTADO_WM, ESTADO_ORACLE, FECHA_ENVIO "
				+ " FROM XXFC.XXFC_GL_ENVIO_DINERO" 
				+ " WHERE TRUNC(CREATION_DATE) = TRUNC(SYSDATE)";
	
		String tdcQueryEnvioDetalle = "SELECT ID_ENVIO_DINERO, PLAZA, TIENDA, ESTADO_ORACLE "
				+ " FROM XXFC.XXFC_GL_ENVIO_DINERO_DETALLE "
				+ " WHERE ID_ENVIO_DINERO = '%s'";
	
		String tdcQueryEnvioDetx = "SELECT ID_DETX, ID_ENVIO_DINERO, DATOS_X "
				+ " FROM XXFC.XXFC_GL_ENVIO_DINERO_DETX"
				+ " WHERE ID_ENVIO_DINERO = '%s'";
		
/** **************************Pasos del caso de Prueba*******************************************/
		
//	 	Paso 1	************************
		
		addStep("Validar la configuraci�n del servidor FTP en la tabla WM_INTERFASE_CONFIG de WMUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult configResult = executeQuery(dbPos, tdcQueryConfig);
		System.out.println(tdcQueryConfig);
		boolean config = configResult.isEmpty();
		
			if (!config) {
				
				testCase.addQueryEvidenceCurrentStep(configResult);	
			
			} 
			
		System.out.println(config);
		
		assertFalse(config, "No se obtiene informacion de la consulta");
		
//	 	Paso 2	************************
		
		addStep("Ejecutar el servicio EO06.Pub:run desde el Job EO06");
		
		
		u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		String dateExecution=pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		
		SQLResult query = dbLog.executeQuery(tdcQueryIntegrationServer);	
		System.out.println(tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println("query: "+query);
		System.out.println("RunID: "+run_id);
//		Paso 3	************************
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		    if(!successRun){
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}

//		Paso 4	************************
		
		    
		addStep("Validar que se inserte el detalle de la ejecuci�n de los Threads lanzados por la ejecuci�n de la interface en la tabla WM_LOG_THREAD de WMLOG.");
		
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult paso3 = executeQuery(dbLog, queryStatusThread);
		
		boolean thread = paso3.isEmpty();
		
		if(!thread) {
			
			testCase.addQueryEvidenceCurrentStep(paso3);
		}
				
		String regPlazaTienda =  paso3.getData(0, "STATUS");
		boolean statusThread = status.equals(regPlazaTienda);
		System.out.println(statusThread);
		
		if(!statusThread){
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result = executeQuery(dbLog, error);
			testCase.addQueryEvidenceCurrentStep(result);
						
			boolean emptyError = result.isEmpty();

			if(!emptyError){
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(result);

			}
		}
				
		assertTrue(statusThread,"El registro de ejecuci�n no fue exitoso.");
		  
//		Paso 5	************************
		
		addStep("Validar la inserci�n de los datos de encabezado en la tabla XXFC.XXFC_GL_ENVIO_DINERO de ORAFIN.");
		String idEnvio="";
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryEnvioDinero);
		
		SQLResult envioResult = executeQuery(dbEbs, tdcQueryEnvioDinero);
		

		boolean envio = envioResult.isEmpty();
		
			if (!envio) {
				
				testCase.addQueryEvidenceCurrentStep(envioResult);	
				 idEnvio = envioResult.getData(0, "ID_ENVIO_DINERO");
			} 
			
		System.out.println(envio);
		
		assertFalse(envio, "No se obtiene informacion de la consulta");
		
//		Paso 6	************************
			 
		addStep("Validar la inserci�n de los datos de detalle en la tabla XXFC_GL_ENVIO_DINERO_DETALLE de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);		
		String detalleFormat = String.format(tdcQueryEnvioDetalle, idEnvio);
		System.out.println(detalleFormat);	
		
		SQLResult detalleResult = executeQuery(dbEbs, tdcQueryEnvioDetalle);

		boolean detalle = detalleResult.isEmpty();
		
		if(!detalle) {
			testCase.addQueryEvidenceCurrentStep(detalleResult);		
		}
		else {
			
			//		Paso 7	************************
			
			addStep("Validar la inserci�n de los datos de detalle inv�lido del archivo en la tabla XXFC_GL_ENVIO_DINERO_DETX de ORAFIN.");
			
			System.out.println(GlobalVariables.DB_HOST_Ebs);		
			String detxFormat = String.format(tdcQueryEnvioDetx, idEnvio);
			System.out.println(detxFormat);	
			
			SQLResult detxResult = executeQuery(dbEbs, tdcQueryEnvioDetx);
			detalle = detxResult.isEmpty();
			
			if(!detalle) {
				testCase.addQueryEvidenceCurrentStep(detxResult);		
			}
			
			System.out.println(detalle);
			
			assertFalse(detalle, "La tabla no contiene registros");
				
				
			}
		
		System.out.println(detalle);
		
		assertFalse(detalle, "La tabla no contiene registros");
			
			
		}
		

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO06_002_Procesar_Archivos_WU";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Procesar archivos WesternUnion con distintos prefijos para distintas plazas (Tambien con detalle invalido)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
