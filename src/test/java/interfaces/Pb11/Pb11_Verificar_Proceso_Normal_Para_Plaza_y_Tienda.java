package interfaces.Pb11;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class Pb11_Verificar_Proceso_Normal_Para_Plaza_y_Tienda extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_Pb11_Verificar_Proceso_Normal_Para_Plaza_y_Tienda_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
				
/** VARIABLES *********************************************************************/	

		String tdcQry1 = " SELECT DISTINCT SUBSTR(A.PV_DOC_NAME,4,5) PV_CR_PLAZA, SUBSTR(A.PV_DOC_NAME,9,5) PV_CR_TIENDA, A.ID, A.STATUS, A.DOC_TYPE FROM \r\n"
				+ " POSUSER.POS_INBOUND_DOCS A, \r\n"
				+ " POSUSER.POS_RHE P, \r\n"
				+ " POSUSER.POS_RHE_DETL D WHERE A.ID = P.PID_ID \r\n"
				+ " AND D.PID_ID = P.PID_ID \r\n"
				+ " AND A.STATUS = 'I' \r\n"
				+ " AND A.DOC_TYPE = 'RHE' \r\n"
				+ " AND SUBSTR(A.PV_DOC_NAME,4,5) = '"+data.get("plaza")+"'\r\n"
				+ " AND SUBSTR(A.PV_DOC_NAME,9,5) = '"+data.get("tienda")+"'\r\n";
				
		String tdcQry3 =  " SELECT RUN_ID, interface, start_dt, end_dt, status FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'PB11' \r\n"
				+ " AND status = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)\r\n"
				+ " Order by run_id desc \r\n";
				
		String tdcQry4 = " SELECT * FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE ATT1 = '" +data.get("plaza") +"' \r\n"
				+ " AND ATT2 = '" + data.get("tienda") + "' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " AND PARENT_ID = %s \r\n"
				+ " AND STATUS = 'S' \r\n";
			
	    String tdcQry5 =  " SELECT distinct pos_rhe.pid_id, substr(pv_doc_name,4,5), substr(pv_doc_name,9,5) \r\n "
	    		+ " FROM POSUSER.pos_rhe_detl, POSUSER.pos_rhe \r\n"
	    		+ " WHERE pos_rhe.pid_id = pos_rhe_detl.pid_id \r\n "
	    		+ " AND substr(pv_doc_name,4,5) = '"+data.get("plaza")+"' \r\n"
	    		+ " AND substr(pv_doc_name,9,5) = '"+data.get("tienda")+"' \r\n";
	    		
	    String tdcQry6 =  " SELECT SUBSTR(PV_DOC_NAME,4,5) PLAZA, pos_inbound_docs.id, status FROM POSUSER.pos_inbound_docs \r\n"
	    		+ " WHERE status = 'E' \r\n"
	    		+ " AND substr(pv_doc_name,4,5)= '"+data.get("plaza")+"' \r\n"
	    		+ " AND DOC_TYPE = 'RHE' \r\n"
	    		+ " AND target_id =  %s "
	    		+ " AND SUBSTR(PV_DOC_NAME,9,5) = '"+data.get("tienda")+"' \r\n";
	    		
	
		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep(" Validar que exista información en la tabla POS_INBOUND_DOCS de POSUSER para la plaza 10BGA y tienda 50UCF con tipos de documento RHE y STATUS I. ");

		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQry1);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(tdcQry1);

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); 

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); 
		}

		System.out.println(paso1_qry1_valida);

		assertFalse(" No se encontro informacion en la base de datos. ", paso1_qry1_valida); 
		/* PASO 2 *********************************************************************/

		addStep(" Invocar el servicio PB11.Pub:run ejecutando el JOB runPB11 ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
	    

		/* PASO 3 *********************************************************************/

		addStep(" Verificar que el estatus sea igual a 'S' para la interface PB11 en la tabla WM_LOG_RUN de la BD del WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQry3);
		
		SQLResult paso3_qry3_Result = dbLog.executeQuery(tdcQry3);

		String runid = paso3_qry3_Result.getData(0, "RUN_ID");

		System.out.println(runid);
		boolean paso3_qry3_valida = paso3_qry3_Result.isEmpty(); 

		if (!paso3_qry3_valida) {
			testCase.addQueryEvidenceCurrentStep(paso3_qry3_Result);
		}

		System.out.println(paso3_qry3_valida);

		assertFalse(" No se encontro informacion en la base de datos. ", paso3_qry3_valida); 
																							

		/* PASO 4 *********************************************************************/

	
		addStep(" Verificar que existan registro en la tabla WM_LOG_THREAD  para la plaza 10BGA y la tienda 50UCF con status S.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String formatotdcQry4 = String.format(tdcQry4, runid);
		
		System.out.println(formatotdcQry4);
	
		SQLResult paso4_qry4_Result = dbLog.executeQuery(formatotdcQry4);
		
		String thread = paso4_qry4_Result.getData(0, "THREAD_ID");

		boolean paso4_qry4_valida = paso4_qry4_Result.isEmpty(); 

		if (!paso4_qry4_valida) {
			testCase.addQueryEvidenceCurrentStep(paso4_qry4_Result); 
		}

		System.out.println(paso4_qry4_valida);

		assertFalse(" No se encontro informacion en la base de datos. ", paso4_qry4_valida); 

		/* PASO 5 *********************************************************************/

		addStep(" Verificar que la informacion de la plaza 10BGA y la tienda 50UCF sea insertada en las tablas POS_RHE y POS_RHE_DETL en POSREP\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQry5);
		SQLResult paso5_qry5_Result = dbPos.executeQuery(tdcQry5);

		boolean paso5_qry5_valida = paso5_qry5_Result.isEmpty(); 
		if (!paso5_qry5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_qry5_Result); 
		}

		System.out.println(paso5_qry5_valida);

		assertFalse("No se encontro informacion en la base de datos.", paso5_qry5_valida); 
		/* PASO 6 *********************************************************************/

		addStep(" Verificar que la informacion de la plaza 10BGA y la tienda 50UCF sea insertada en las tablas POS_RHE y POS_RHE_DETL en POSREP\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String FormtatotdcQry6 = String.format(tdcQry6, thread);
		System.out.println(FormtatotdcQry6);
		SQLResult paso6_qry6_Result = dbPos.executeQuery(FormtatotdcQry6);

		boolean paso6_qry6_valida = paso6_qry6_Result.isEmpty(); 

		if (!paso6_qry6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_qry6_Result); 
		}

		System.out.println(paso6_qry6_valida);

		assertFalse("No se encontro informacion en la base de datos.", paso6_qry6_valida); 
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_Pb11_Verificar_Proceso_Normal_Para_Plaza_y_Tienda_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Esta interfaz se encagra de enviar la informacion de las hojas de entrega final hacia el repsitorio de WebMethods, para su almacenamiento ";
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

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
