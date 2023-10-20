package interfaces.po11;

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

public class PO11GeneracionDePoliza extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PO11_Generacion_De_Polizas(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

/**
* Variables ******************************************************************************************
* 
* 
*/		
		// el paso 4 no viene en la matriz
		
		String tdcQueryValidar = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, RECEIVED_DATE "
				+ " FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE  STATUS = 'I'"
				+ " AND DOC_TYPE IN ('DCI')"
				+ " AND RECEIVED_DATE >= TO_DATE('2021-05-31', 'YYYY-MM-DD')";
		
		String tdcQueryDetalles = "SELECT PID_ID, ID_CI_TDA,MOV_TYPE, MOV_ID_POS, PV_DATE, MONTO, CI_INFO"
				+ " FROM POSUSER.POS_DCI_DETL"
				+ " WHERE PID_ID = '%s'";
		
		String tdcQueryPoliza = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, RECEIVED_DATE, TARGET_ID  "
				+ " FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE STATUS = 'E' "
				+ " AND DOC_TYPE IN ('DCI') "
				+ " AND RECEIVED_DATE >= TO_DATE('2021-05-31', 'YYYY-MM-DD')";
		
		String tdcQueryFinanzas = "SELECT REFERENCE6 POLIZA,STATUS,USER_JE_CATEGORY_NAME,GROUP_ID, SEGMENT4 NUM_CTA_GRAL,"
				+ " SEGMENT1||'.'||SEGMENT2||'.'||SEGMENT3||'.'||SEGMENT4||'.'||SEGMENT5||'.'||SEGMENT7 AS CUENTA_DETALLE"
				+ " FROM GL.GL_INTERFACE"
				+ " WHERE REFERENCE6 = '%s'"
				+ " ORDER BY DATE_CREATED DESC";
		
		String tdcQueryFinanzas2 = "SELECT ENTERED_CR, ENTERED_DR, REFERENCE10,DATE_CREATED,CURRENCY_CODE,CURRENCY_CONVERSION_DATE"
				+ " FROM GL.GL_INTERFACE"
				+ " WHERE REFERENCE6 = '%s'"
				+ " ORDER BY DATE_CREATED DESC";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO11'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum <= 1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
	
		
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("I16072  Cajas Inteligentes");
		//testCase.setTest_Description(data.get("Description"));		
		
		
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//Paso 1 *************************			
		addStep("Validamos la recepcion del documento DCI");	
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryValidar);
		
		SQLResult validarResult = executeQuery(dbPos, tdcQueryValidar);
		boolean validar = validarResult.isEmpty();
		String pid_id = validarResult.getData(0, "ID");
		
		if (!validar) {
			
			testCase.addQueryEvidenceCurrentStep(validarResult);
		}
		
		System.out.println(validar);
		
		assertFalse(validar, "No se obtiene información de la consulta");
		
//Paso 2 *************************			
		addStep("Ejecutar las siguientes consultas para validar los detalles de los archivos DCI ");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String detallesFormat = String.format(tdcQueryDetalles, pid_id);
	
		System.out.println(detallesFormat);
		
		SQLResult detallesResult = executeQuery(dbPos, detallesFormat);
		
		
		boolean detalles = detallesResult.isEmpty();
		
		if (!detalles) {
			
			testCase.addQueryEvidenceCurrentStep(detallesResult);
					
		}
		
		System.out.println(detalles);
		
		assertFalse(detalles, "No se obtiene información de la consulta");
		
//Paso 3 *************************		
		addStep("Ejecutar la interfaz PO11");		
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
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		
// paso 4 *************************
		addStep("Ejecutar el siguiente query para consultar la última ejecución.");
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
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
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);
		
// paso 5 *********************************
		addStep("Ejecutar la siguiente consulta para validar el ID  de la polizas");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryPoliza);
		
		SQLResult polizaResult = executeQuery(dbPos, tdcQueryPoliza);
		String numPoliza = polizaResult.getData(0, "TARGET_ID");
				boolean poliza = polizaResult.isEmpty();
				
				if (!poliza) {
					
					testCase.addQueryEvidenceCurrentStep(polizaResult);
				}
				
				System.out.println(poliza);
				
				assertFalse(poliza, "No se obtiene información de la consulta");			
		
// paso 6 *********************************		
		addStep("Ejecutar el siguiente Query utilizando el numero de poliza para validar que se hayan insertados los datos en finanzas.");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String finanzasFormat = String.format(tdcQueryFinanzas, numPoliza);
		String finanzasFormat2 = String.format(tdcQueryFinanzas2, numPoliza);
		System.out.println(finanzasFormat);
		
		SQLResult finanzasResult = executeQuery(dbEbs, finanzasFormat);
		SQLResult finanzasResult2 = executeQuery(dbEbs, finanzasFormat2);
		
		boolean finanzas = finanzasResult.isEmpty();
		
		if (!finanzas) {
			
			testCase.addQueryEvidenceCurrentStep(finanzasResult);
			testCase.addQueryEvidenceCurrentStep(finanzasResult2);
		}
		
		System.out.println(finanzas);
		
		assertFalse(finanzas, "No se obtiene información de la consulta");
				
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return "Terminado. Se validara la generacion de polizas con sus respectivos movimientos de multiples tiendas al ejecutar la interfaz PO11 ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_PO11_Generacion_De_Polizas";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
