package interfaces.ol5;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class OL5_ActualizarRegistrosDevRechazados extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL5_Actualizar_Registros_Dev_Rechazados(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/		
		//falta la bd de legacy
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	
		
/**
* Variables ******************************************************************************************
* 
* 
*/	
		String tdcQueryCount = "SELECT COUNT(REQ_NUMBER) "
				+ " FROM WMUSER.XXPO_REQ_SI_WEB "
				+ " WHERE REQ_NUMBER = " + data.get("REQ_NUMBER") +"";
		
		String tdcQueryReqNumber = "SELECT REQ_NUMBER, SI_NUMBER, REQ_TYPE, WM_STATUS "
				+ " FROM WMUSER.XXPO_REQ_SI_WEB "
				+ " WHERE REQ_NUMBER = " + data.get("REQ_NUMBER") +"";
		
		
		String tdcQueryInvSol1 = "SELECT (INV_SOL1_AUTORIZADO - INV_SOL1_EROGADO - INV_SOL1_COMPROMETIDO) AS AV_AMOUNT, INV_SOL1_AUTORIZADO AS AUTORIZADO"
				+ " FROM REG1_INV_SOL"
				+ " WHERE INV_SOL1_NUMERO = %s"
				+ " AND  INV_SOL1_CONSECUTIVO = 0";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'OL5'"
				+ "AND status = 'E'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		String tdcQueryReqAct = "SELECT REQ_NUMBER, SI_NUMBER, WM_STATUS, WM_SENT_DATE, WM_RUN_ID, WM_RESULT"
				+ " FROM WM_USER.XXPO_REQ_SI_WEB"
				+ " WHERE WM_STATUS = 'E' "
				+ " AND TRUNC(WM_SENT_DATE) = sysdate"
				+ " AND WM_RUN_ID = %s"
				+ " AND WM_RESULT = 'R' "
				+ " AND REQ_NUMBER = " + data.get("REQ_NUMBER") +" "
				+ " AND SI_NUMBER = %s";
		
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Consultar que NO existan registros en la tabla XXPO_REQ_SI_WEB, para el registro con REQ_NUMBER igual a "+data.get("REQ_NUMBER")+" en ORAFIN.");
		
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryCount);		
		SQLResult countResult = executeQuery(dbEbs, tdcQueryCount);
		SQLResult numberResult = executeQuery(dbEbs, tdcQueryReqNumber);
		String si_number = numberResult.getData(0, "SI_NUMBER");
		String total_amount =  numberResult.getData(0, "SI_NUMBER");
					
		boolean number = countResult.isEmpty();
		
		if (!number) {
			
			testCase.addQueryEvidenceCurrentStep(countResult);
		}
		
		System.out.println(number);
		
		assertFalse(number, "No se obtiene información de la consulta");
		
//Paso 2 *************************
		addStep("Consultar el valor de AV_AMOUNT en la tabla REG1_INV_SOL en donde INV_SOL1_NUMERO es igual a XXPO_REQ_SI_WEB.SI_NUMBER y INV_SOL1_CONSECUTIVO es igual a 0.");
		//falta la bd de legacy 
		
		String invFormat = String.format(tdcQueryInvSol1, si_number);	
		System.out.println(invFormat);
		SQLResult invResult = executeQuery(dbEbs, invFormat); //cambiar por la bd de legacy
		String av_amount = invResult.getData(0, "AV_AMOUNT");

		boolean inv = invResult.isEmpty();
		
		if (!inv) {
			testCase.addQueryEvidenceCurrentStep(invResult);
		}
		
		System.out.println(inv);		
		assertFalse(inv, "No se obtiene información de la consulta");
		
//Paso 3 ************************
		addStep("Verificar que el valor de AV_AMOUNT sea menor que XXPO_REQ_SI_WEB.TOTAL_AMOUNT.");
		
		double doubleAv_amount = Double.parseDouble(av_amount);
		double doubleTotal_amount = Double.parseDouble(total_amount);
		
		if (doubleAv_amount < doubleTotal_amount)
		{
			testCase.addTextEvidenceCurrentStep("El valor de AV_AMOUNT es:" +doubleAv_amount);
			testCase.addTextEvidenceCurrentStep("El valor de TOTAL_AMOUNT es:" +doubleTotal_amount);
			testCase.addTextEvidenceCurrentStep("El valor de AV_AMOUNT sí es menor al de TOTAL_AMOUNT.");
		}
		else
		{
			testCase.addTextEvidenceCurrentStep("El valor de AV_AMOUNT es:" +doubleAv_amount);
			testCase.addTextEvidenceCurrentStep("El valor de TOTAL_AMOUNT es:" +doubleTotal_amount);
			testCase.addTextEvidenceCurrentStep("El valor de AV_AMOUNT no es menor al de TOTAL_AMOUNT.");
		}
		
		System.out.println("av_amount:" +doubleAv_amount);
		System.out.println("total_amount:" +doubleTotal_amount);
		
//Paso 4 *********************
		addStep("Comprobar que este habilitado el AdapterNotification OL5.DB.ORAFIN:adpNotInsREQ en el IS.");
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
		
//paso 5 **********************
		addStep("Comprobar que existan registros de la ejecucion de la interfaz generando error en la tabla WM_LOG_RUN en donde INTERFACE es igual a 'OL5', STATUS es igual a 'E'.");
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

//paso 6 ********************
		addStep("Comprobar los detalles del error en la tabla WM_LOG_ERROR.");
		
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

//paso 7 ***********************
		addStep("Comprobar que se actualicen los registros en la tabla XXPO_REQ_SI_WEB de ORAFIN, en donde WM_STATUS es igual a 'E', WM_SENT_DATE es igual a la Fecha_Actual, WM_RESULT es igual a 'R'");
		String ReqActFormat = String.format(tdcQueryReqAct, run_id, si_number);
		System.out.println(ReqActFormat);
		SQLResult ReqActResult = executeQuery(dbEbs, ReqActFormat);
		
		boolean reqAct = ReqActResult.isEmpty();
		
		if (!reqAct) {
			testCase.addQueryEvidenceCurrentStep(ReqActResult);
		}
		
		System.out.println(reqAct);		
		assertFalse(reqAct, "No se obtiene información de la consulta");
		
		
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
		return "Construido. Actualizar registros de inversion por medio de un nuevo registro (DEV Aceptado).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL5_Actualizar_Registros_Dev_Rechazados";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
