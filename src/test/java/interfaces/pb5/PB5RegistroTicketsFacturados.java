package interfaces.pb5;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
//import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PB5RegistroTicketsFacturados extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB5_003_Registro_Tickets_Facturados(HashMap<String, String> data) throws Exception {

/* Utilerías *********************************************************************/		
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbTPE = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

/**
* Variables ******************************************************************************************
* 
* 
*/
		
		String tdcQueryPos = "SELECT A.ID,B.ESTATUS,A.PV_CR_PLAZA,A.PV_CR_TIENDA,B.PV_FACTURA "
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_TIC_DETL_F B, POSUSER.POS_INBOUND_DOCS C "
				+ " WHERE A.ID = C.PE_ID "
				+ " AND C.ID = B.PID_ID "
				+ " AND B.ESTATUS = 'INSERTADO' "
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") +"' "
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +"' "
				+ " AND B.PV_FACTURA <> 0";
		
		
		String tdcQueryIntegrationServer = "SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+ " and  TRUNC(start_dt) >= TRUNC(SYSDATE)"
			    + " and rownum = 1"
				+ " order by start_dt desc";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1"; 
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 
		
		String tdcQueryTpe = "SELECT  A.ACTIVITY_ID, A.PLAZA, A.TIENDA, I.BRMS_STATUS, A.TYPE, A.ACTUALIZATION_DATE"
				+ " FROM TPEUSER.TPE_FCP_ACTIVITY A INNER JOIN TPEUSER.TPE_FCP_ITEM I "
				+ " ON I.ACTIVITY_ID = A.ACTIVITY_ID "
				+ " where A.PLAZA = '" + data.get("plaza") +"' "
				+ " AND I.BRMS_STATUS = 'POR PROCESAR' "
				+ " AND A.TIENDA = '" + data.get("tienda") +"' "
				+ " AND A.TYPE = 'FACTURADO' "
				+ " AND TRUNC(A.ACTUALIZATION_DATE) = TRUNC(sysdate)";
		
		String tdcQueryProcesado = "SELECT A.ID, B.PID_ID, B.ESTATUS, A.PV_CR_PLAZA, A.PV_CR_TIENDA  "
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_TIC_DETL_F B, posuser.POS_INBOUND_DOCS C "
				+ " WHERE A.ID = C.PE_ID "
				+ " AND C.ID = B.PID_ID  "
				+ " AND B.ESTATUS = 'PROCESADO'  "
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") +"'  "
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +"'";
		
		
		
	
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String status = "S";
		String run_id ;
		
		testCase.setProject_Name("AutomationQA");	
				
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//		Paso 1	************************ 
		
		addStep("Consultar que existan registros en las tablas POS_ENVELOPE, POS_TIC_DETL_F, POS_INBOUND_DOCS de la BD POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryPos);
		
		SQLResult posResult = executeQuery(dbPos, tdcQueryPos);
        
		boolean pos =  posResult.isEmpty();	
        
		 System.out.println(pos);
		 
			if (!pos) {
				
			testCase.addQueryEvidenceCurrentStep(posResult);
			
						} 
		 
		 System.out.println(pos);
		 
		 assertFalse(pos, "No se obtiene informacion de la consulta");
		
//		Paso 2	************************ 
		 
		addStep("Ejecutar el servicio PB5.Pub:run invocando el Job runMainPB5.");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		System.out.println(tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
		
//		Paso 3	************************
		
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		if (valuesStatus) {
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		
		   if(successRun){
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   System.out.print(error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);	
		    
//		Paso 4	************************
		
		
		addStep("Se valida la generacion de thread.");
				
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);
        
		boolean threadResult1 =threadResult.isEmpty();
		
		System.out.println(threadResult1); 
		
		if(!threadResult1) { //lo agregue
		String statusThread = threadResult.getData(0, "Status");
		boolean ST = statusThread.equals(status);
		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
			   
			} 
		System.out.println(ST);	
		
		assertFalse(ST, "No se obtiene informacion de la consulta");
		}
		
		System.out.println(threadResult1);	
		
		assertFalse(threadResult1, "No se obtiene informacion de la consulta");
//		Paso 5	************************ 	

		addStep("Confirmar que se insertaron correctamente los registros de los tickets en la tablas TPE_FCP_ACTIVITY y TPE_FCP_ITEM.");
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		System.out.println(tdcQueryTpe);
		
		SQLResult tpeResult = executeQuery(dbTPE, tdcQueryTpe);

		boolean tpe = tpeResult.isEmpty();
		
			if (!tpe) {
		
			testCase.addQueryEvidenceCurrentStep(tpeResult);
			
						} 
		
		System.out.println(tpe);

		assertFalse(tpe, "No se obtiene informacion de la consulta");
		
//		Paso 6	************************ 	
		
		addStep("Comprobar que los tickets fueron actualizados correctamente en la tabla POS_TIC_DETL_F en la BD POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryProcesado);
		
		SQLResult procesadoResult = executeQuery(dbPos, tdcQueryProcesado);

		boolean procesado = procesadoResult.isEmpty();
		
			if (!procesado) {
		
			testCase.addQueryEvidenceCurrentStep(procesadoResult);
			
						} 
		
		System.out.println(procesado);

		assertFalse(procesado, "No se obtiene informacion de la consulta");
		
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
		return "Terminado.Que la información de los tickets sea procesada y se inserten en las tablas de facturación del llavero.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB5_003_Registro_Tickets_Facturados";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
