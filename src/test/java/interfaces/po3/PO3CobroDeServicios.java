package interfaces.po3;

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
import utils.sql.SQLUtil;

public class PO3CobroDeServicios extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO3_CobroDeServicio(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
		String status = "S";
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
//		String con ="http://"+user+":"+ps+"@"+server;
		String searchedStatus = "R";
		String run_id ;
		
		testCase.setProject_Name("AutomationQA");		
		
		
/** Variables *********************************************************************/
		
		String tdcQueryStatusI = "SELECT DISTINCT b.CR_PLAZA, a.ID, a.PE_ID, a.STATUS, a.DOC_TYPE, d.SERVICIO, d.FOLIO_TRANSACCION" + 
				" FROM POSUSER.POS_INBOUND_DOCS a, " + 
				" POSUSER.PLAZAS b, " + 
				" POSUSER.POS_SYB_DETL d " + 
				" WHERE a.ID = d.PID_ID " + 
				" AND a.STATUS ='I' " + 
				" AND a.DOC_TYPE ='SYB' " + 
				" AND SUBSTR(a.PV_DOC_NAME,4,5) = b.CR_PLAZA " + 
				" AND b.CR_PLAZA = '" + data.get("plaza") +"' " + 
				" AND b.PAIS ='MEX' " + 
				" AND (d.CANCELADO NOT IN ('L','N','R') " + 
				" OR d.CANCELADO IS NULL) " + 
				" AND a.PARTITION_DATE >= TRUNC(SYSDATE-7)";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+" and start_dt >= TRUNC(SYSDATE)"
			    +" order by start_dt desc)"
				+ " where rownum = 1";
		
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; //FCWMLQA
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; //FCWMLQA 
		
		String tdcQueryPagoServicio = "SELECT TRANS_ID, PLAZA, TIENDA, SERVICIO, FECHA_RECEPCION, ESTATUS, HORA_TRANSACCION "
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS "
				+ " WHERE PLAZA = '" + data.get("plaza") +"' " 
				+ " AND SERVICIO = %s" 
//				+ " AND ESTATUS IS NULL "
				+ " AND FOLIO_TRANSACCION = %s"  
				+ " AND TRUNC(FECHA_RECEPCION) = TRUNC(SYSDATE)";
		
		String tdcQueryPagoServicioW = "SELECT TRANS_ID, PLAZA, TIENDA, SERVICIO, FECHA_RECEPCION, ESTATUS, HORA_TRANSACCION "
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_WORK " 
				+ " WHERE PLAZA = '" + data.get("plaza") +"' "  
				+ " AND SERVICIO = %s" 
//				+ " AND ESTATUS = 'C' "
				+ " AND FOLIO_TRANSACCION = %s"   
				+ " AND FECHA_RECEPCION = TO_CHAR(SYSDATE,'DD-MON-YY')";
		
		String tdcQueryStatusE = "SELECT ID, PE_ID, DOC_TYPE, STATUS"
				+ " FROM POSUSER.POS_INBOUND_DOCS " + 
				" WHERE DOC_TYPE = 'SYB' " + 
				" AND STATUS = 'E' " + 
				" AND ID = %s";
		
		
		
		
/** **************************Pasos del caso de Prueba*******************************************/

		
//		Paso 1	************************		
		
		addStep("Validar que exista información de documentos SYB pendientes por procesar en la tabla POS_INBOUND_DOCS de POSUSER.");	

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println("Paso 1 ");
		System.out.println(tdcQueryStatusI);
		
		SQLResult paso1 = executeQuery(dbPos, tdcQueryStatusI);
		String servicio = "";
		String docId = "";
		String folio = "";
		boolean statusI = paso1.isEmpty();
		
		if (!statusI) {	
			servicio = paso1.getData(0, "SERVICIO");
			docId = paso1.getData(0, "ID");
			folio = paso1.getData(0, "FOLIO_TRANSACCION");
			testCase.addQueryEvidenceCurrentStep(paso1);		
		} 

		System.out.println(statusI);

		assertFalse(statusI, "La tabla no contiene registros");


//		Paso 2	************************	

		addStep("Ejecutar el servicio PO3.Pub:run para procesar los documentos SYB y transferir los pagos de servicios de POS hacia ORAFIN.");
		System.out.println("Paso 2 ");
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");


//		Paso 3	************************
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		System.out.println("Paso 3 ");
		
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
			
//		Paso 4 ************************
		addStep("Se valida la generacion de thread.");
		System.out.println("Paso 4 ");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String consultaTemp6 = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso3 = executeQuery(dbLog, consultaTemp6);
		System.out.println(consultaTemp6);
		
		String estatusThread = paso3.getData(0, "Status");
		
		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {
		
		    testCase.addQueryEvidenceCurrentStep(paso3);
		    
		} 
		
		System.out.println(SR);

		assertFalse(SR, "No se obtiene información de la consulta");		    
		    

//		Paso 5	************************
		addStep("Si existen pagos válidos, validar la inserción de los pagos válidos de la plaza en la tabla XXFC.XXFC_PAGO_SERVICIOS de ORAFIN.");
		System.out.println("Paso 5 ");
		
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String formatPagoServicio = String.format(tdcQueryPagoServicio, servicio, folio);
		System.out.println(formatPagoServicio); 
		
		SQLResult paso5 = executeQuery(dbEbs, formatPagoServicio); 

		boolean valido = paso5.isEmpty();
		 
		if (!valido) {	
			
			testCase.addQueryEvidenceCurrentStep(paso5);		
			
					} 
		
		System.out.println(valido);

		
			
//		Paso 6	************************
		addStep("Si existen pagos inválidos, validar la inserción de los pagos inválidos de la plaza en la tabla XXFC.XXFC_PAGO_SERVICIOS_WORK de ORAFIN.");
		System.out.println("Paso 6 ");
		
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String formatPagoServicioW = String.format(tdcQueryPagoServicioW, servicio, folio);
		System.out.println(formatPagoServicioW);
					
		SQLResult paso6 = dbEbs.executeQuery(formatPagoServicioW);
		boolean invalido = paso6.isEmpty();
					
		if (!invalido) {	
						
			testCase.addQueryEvidenceCurrentStep(paso6);	
		}
			else {
				testCase.addQueryEvidenceCurrentStep(paso6);
				testCase.addBoldTextEvidenceCurrentStep("No se encontraron pagos inválidos");
			}
 
				
		System.out.println(invalido);


//		Paso 7	************************
		addStep("Validar la actualización del estatus de los documentos SYB procesados en la tabla POS_INBOUND_DOCS a STATUS = 'E'.");
		System.out.println("Paso 7 ");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String formatStatusE = String.format(tdcQueryStatusE, docId);
		System.out.println(formatStatusE);
		
		SQLResult paso7 = executeQuery(dbPos, formatStatusE);
		boolean statusE = paso7.isEmpty();
		
		if (!statusE) {	
			
			testCase.addQueryEvidenceCurrentStep(paso7);		
			
					} 
		
		System.out.println(statusE);

		assertFalse(statusE, "La tabla no contiene registros");


	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Transferir pagos de servicios de POS a ORAFIN para la plaza ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PO3_CobroDeServicio";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	
	
	
	

}
