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

public class PR1_CL_ProcesarDocumentoSinCedis extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR1_CL_002_Ejecucion_Sin_Documentos_CEDIS(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		
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
			
		String tdcQueryTsf = "SELECT PID_ID, NO_RECORDS, PV_CVE_MVT, PV_CR_FROM_LOC, PV_CR_TO_LOC, PARTITION_DATE "
				+ " FROM POSUSER.POS_TSF"
				+ " WHERE PID_ID = %s"
				+ " ORDER BY PARTITION_DATE DESC";
		
		String tdcQueryTsfDtl = "SELECT PID_ID, ITEM, SHIP_QTY, PV_MOTIVO, PV_RETAIL_PRICE, PARTITION_DATE "
				+ " FROM POSUSER.POS_TSF_DETL"
				+ " WHERE PID_ID = %s";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%PR1%'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";	
		
		String tdcQueryEstatusE = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE ID = 610678"
				+ " and status = 'E'"
				+ " ORDER BY RECEIVED_DATE DESC";
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 **************************
		addStep("Ingresar a la BD de WM OCHWMQA.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_PosUserChile);
		
//Paso 2***************************
		addStep("Consultar la tabla POS_INBOUND_DOCS para validar que se muestren registros. Ejecutar la siguiente consulta: ");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryRegistros);
		
		SQLResult registrosResult = executeQuery(dbPos, tdcQueryRegistros);
		String id = registrosResult.getData(0, "ID");
		
		boolean registros = registrosResult.isEmpty();
		
		if (!registros) {
			
			testCase.addQueryEvidenceCurrentStep(registrosResult);
		}
		
		System.out.println(registros);
		
		assertFalse(registros, "No se obtiene información de la consulta");

//Paso 3 **************************
		addStep("Consultar la tabla POS_TSF para validar que se muestre el registro relacionado con el archivo TSF. Ejecutar la siguiente consulta: ");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String tsfFormat = String.format(tdcQueryTsf, id);
		SQLResult tsfResult = dbPos.executeQuery(tsfFormat);
		
		System.out.println(tsfFormat);
		
		boolean tsf = tsfResult.isEmpty();
		
		if (!tsf) {
			
			testCase.addQueryEvidenceCurrentStep(tsfResult);
		}
		
		System.out.println(tsf);
		
		assertFalse(tsf, "No se obtiene información de la consulta");
		
//Paso 4 **********************
		addStep("Validar que se encuentre el detalle de la transferencia.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String tsfDtlFormat = String.format(tdcQueryTsfDtl, id);
		SQLResult tsfDtlResult = dbPos.executeQuery(tsfDtlFormat);
		
		System.out.println(tsfDtlFormat);
		
		boolean tsfDtl = tsfDtlResult.isEmpty();
		
		if (!tsfDtl) {
			
			testCase.addQueryEvidenceCurrentStep(tsfDtlResult);
		}
		
		System.out.println(tsfDtl);
		
		assertFalse(tsfDtl, "No se obtiene información de la consulta");
		
//Paso 5 *********************
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

//Paso 6 *************************
		addStep("Ingresar a la WMLOG para validar las corridas de las interfaces de manera exitosa.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_LogChile);
		
//Paso 7	************************ 	
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
		
//Paso 8 **************************
	    addStep("Validar nuevamente la tabla POS_INBOUND_DOCS, el cual después de la ejecución de las interfaces se encontrarán con estatus E");
	    System.out.println(GlobalVariables.DB_HOST_PosUserChile);
	    
		String estatusEFormat = String.format(tdcQueryEstatusE, id);		
		SQLResult estatusEResult = executeQuery(dbPos, estatusEFormat);
		System.out.println(estatusEFormat);
		
		boolean estatusE = registrosResult.isEmpty();
		
		if (!estatusE) {
			
			testCase.addQueryEvidenceCurrentStep(estatusEResult);
		}
		
		System.out.println(estatusE);
		
		assertFalse(estatusE, "No se obtiene información de la consulta");
		

		
//Paso 9 **********************
		addStep("Ingresar a la BD de RMS BDCHRMSQ.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: BDCHRMSQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_RMSWMUSERChile);
		
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
		return "Terminado. Validar que la interfaz PR1_CL no muestre error al ejecutarse sin documentos CEDIS para procesar.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR1_CL_002_Ejecucion_Sin_Documentos_CEDIS";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	

	

}
