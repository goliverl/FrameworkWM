package interfaces.pr1_cl;

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
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR1_CL_EjecucionSinDocumentos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR1_CL_001_Ejecucion_Sin_Documentos(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryRegistros = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME"
				+ " FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE STATUS = 'I' "
				+ " AND DOC_TYPE IN ('TSF')"
				+ " ORDER BY RECEIVED_DATE DESC";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%PR1%'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";				
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Ingresar a la BD de WM OCHWMQA.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_PosUserChile);
		
//Paso 2 *************************
		addStep("Consultar la tabla POS_INBOUND_DOCS para validar que NO se muestren registros. Ejecutar la siguiente consulta: ");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryRegistros);
		
		SQLResult registrosResult = executeQuery(dbPos, tdcQueryRegistros);
		
		boolean registros = registrosResult.isEmpty();
		
		if (registros) {			
			testCase.addQueryEvidenceCurrentStep(registrosResult);
		}
		
		System.out.println(registros);
		
		assertTrue(registros, "No se obtiene información de la consulta");
		
//Paso 3	************************ 	
		addStep("Ejecutar la interface PR1_CL.");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 4 *************************
		addStep("Ingresar a la WMLOG para validar las corridas de las interfaces de manera exitosa.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_LogChile);	
				
//Paso 5	************************ 	
		addStep("Validar la ejecución de la interface PR1 en la wmlog.");
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
	    if(!successRun){
	   
	 
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	  
	    }
	    testCase.addQueryEvidenceCurrentStep(query);		

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
		return "Terminado. Validar que la interfaz PR1_CL no muestre error al ejecutarse sin documentos para procesar.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR1_CL_001_Ejecucion_Sin_Documentos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
