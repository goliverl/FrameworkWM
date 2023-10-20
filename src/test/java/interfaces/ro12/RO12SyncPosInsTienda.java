package interfaces.ro12;

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

public class RO12SyncPosInsTienda extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO12_002_SyncPosInsTienda(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
	//	SqlUtil dbLog = new SqlUtil(GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA, GlobalVariables.DB_HOST_FCWMLQA);
	//	SqlUtil dbPuser = new SqlUtil(GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser, GlobalVariables.DB_HOST_Puser);
	//	SqlUtil dbEbs = new SqlUtil(GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs, GlobalVariables.DB_HOST_Ebs);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);

		
		/* Variables *************************************************************************/
				
		/*String tdcInsertOrafin = "Insert into WM_SYNC_POS_EXC_RATES" 
				+" (ID, CR_PLAZA, CR_TIENDA, EXC_RATE_TYPE, FROM_CURRENCY,"  
				+" TO_CURRENCY, CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE,"
				+" USER_CONVERSION_TYPE, WM_STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE) values ("
		        + data.get("ID") +",'"+ data.get("PLAZA") +"','"+ data.get("TIENDA")+"','"
				+ data.get("EXC_RATE_TYPE") +"','"+ data.get("FROM_CURRENCY") +"','"+ data.get("TO_CURRENCY") 
				+"',SYSDATE,'"+ data.get("CONVERSION_TYPE") +"','"+ data.get("CONVERSION_RATE") 
				+"','"+ data.get("USER_CONVERSION_TYPE") +"','"+ data.get("WM_STATUS_CODE") +"',SYSDATE,SYSDATE)";
		*/		
		String tdcSelectOrafin = "SELECT ID, CR_PLAZA, CR_TIENDA, EXC_RATE_TYPE, CONVERSION_TYPE, CONVERSION_RATE, WM_STATUS_CODE"+
				" FROM WMUSER.WM_SYNC_POS_EXC_RATES" + 
				" WHERE CR_PLAZA ='"+ data.get("PLAZA") +"'"+
				" AND CR_TIENDA='"+ data.get("TIENDA") +"'"+
				" AND ID ='63681'" + 
				" AND EXC_RATE_TYPE = 'T'" + 
				" AND WM_STATUS_CODE = 'L'" +
				" AND CONVERSION_TYPE = '1260'" +
				" AND CONVERSION_RATE ='"+ data.get("CONVERSION_RATE") +"'";
		
		//La consulta a  WM_LOG_RUN se divide en dos para mostrar los datos ATT
		String tdcQueryWmLog1 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "+
				" FROM WMLOG.FROM WM_LOG_RUN" + 
				" WHERE INTERFACE = 'RO12_MX'" + 
				" AND STATUS = 'S'" + 
				" AND START_DT > TRUNC(SYSDATE)" + 
				" ORDER BY START_DT DESC"; 
		
		String tdcQueryWmLog2 = "SELECT ATT1, ATT2, ATT3, ATT4, ATT5 "+
				" FROM WMLOG.FROM WM_LOG_RUN" + 
				" WHERE INTERFACE = 'RO12_MX'" + 
				" AND STATUS = 'S'" + 
				" AND START_DT > TRUNC(SYSDATE)" + 
				" ORDER BY START_DT DESC"; 
		
		String tdcQueryPosOutboundDocs = "SELECT ID,POE_ID,DOC_NAME,DOC_TYPE,SENT_DATE,STATUS,DATE_CREATED"+
				" FROM POSUSER.POS_OUTBOUND_DOCS" + 
				" WHERE DOC_TYPE = 'EXR'" +
				" AND PV_CR_PLAZA=' "+ data.get("PLAZA") +"'"+
				" AND PV_CR_TIENDA='"+ data.get("TIENDA") +"'"+
				" AND PARTITION_DATE>=TRUNC(SYSDATE)" + 
				" ORDER BY SENT_DATE DESC"; 
		
		//La consulta a WM_SYNC_POS_EXC_RATESse divide en dos para mostrar todos los datos
		String tdcQueryWmSyncPosExcRates1 = "SELECT ID,CR_PLAZA,CR_TIENDA,EXC_RATE_TYPE,FROM_CURRENCY, TO_CURRENCY,CONVERSION_DATE,CONVERSION_TYPE"+
				" FROM WMUSER.WM_SYNC_POS_EXC_RATES" + 
				" WHERE WHERE CR_TIENDA = '"+ data.get("TIENDA") +"'"+
				" AND CR_PLAZA = ' "+ data.get("PLAZA") +"'"+
				" AND WM_STATUS_CODE = 'E' "+
				" AND CREATION_DATE >= TRUNC(SYSDATE)"; 
		
		String tdcQueryWmSyncPosExcRates2 = "SELECT CONVERSION_RATE,USER_CONVERSION_TYPE,WM_STATUS_CODE,CREATION_DATE,LAST_UPDATE_DATE"+
				" FROM WMUSER.WM_SYNC_POS_EXC_RATES" + 
				" WHERE WHERE CR_TIENDA = '"+ data.get("TIENDA") +"'"+
				" AND CR_PLAZA = ' "+ data.get("PLAZA") +"'"+
				" AND WM_STATUS_CODE = 'E' "+
				" AND CREATION_DATE >= TRUNC(SYSDATE)"; 

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2 " + " FROM wmlog.wm_log_thread "
				+ " WHERE parent_id = %s";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" + " WMLOG.wm_log_error"
				+ " WHERE run_id = %s";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO12_MX'" 	
				+ " and START_DT >= TRUNC (SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";	
		
		SeleniumUtil u;
        PakageManagment pok;
        String user = data.get("user");
        String ps = PasswordUtil.decryptPassword(data.get("ps"));
        String server = data.get("server");
        String searchedStatus = "R";
        String status = "S";
        String wm_status="E";

		/* Pasos *****************************************************************************/     
	   	 
	    //Paso 0
     	// addStep("Insertar un Tipo de Cambio a nivel Tienda en la tabla WM_SYNC_POS_EXC_RATES de ORAFIN"); 
	        	
	    //Paso 1
	    addStep("Buscar y verificar el interst en la tabla WM_SYNC_POS_EXC_RATES de ORAFIN"); 
	    
	    	System.out.println(tdcSelectOrafin); 
	    	
	    	SQLResult result = dbEbs.executeQuery(tdcSelectOrafin);

	    	boolean orafin = result.isEmpty();
	    	
	    	testCase.addQueryEvidenceCurrentStep(result);
	    	
	    	assertFalse(orafin,"No se encontro insert");  
	    	
	    //Paso 2    	
	    	
	    	addStep("Ejecutar la interfaz RO12 con el servicio " + data.get("interface"));

	    	u = new SeleniumUtil(new ChromeTest(), true);

	    	pok = new PakageManagment(u, testCase);

	    	String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

	    	u.get(contra);

	    	u.hardWait(4);

	    	pok.runIntefaceWM(data.get("interface"), data.get("servicio"), null);
	    		

	   // 	String[] is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
	    	
	    	
	    	SQLResult is = executeQuery(dbLog, tdcQueryIntegrationServer);
	    	
	    	String status1 = is.getData(0, "STATUS");
	    	String run_id = is.getData(0, "RUN_ID");

	 //   	run_id = is[1];// guarda el run id de la ejecución

	    	boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

	    	while (valuesStatus) {

	//    	is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
	    		
	    	is = executeQuery(dbLog, tdcQueryIntegrationServer);
	        	
	        status1 = is.getData(0, "STATUS");
	        run_id = is.getData(0, "RUN_ID");
	        	 
	    	valuesStatus = status1.equals(searchedStatus);	

	    	valuesStatus = status1.equals(searchedStatus);

	    	u.hardWait(2);

	    	}

	    	boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

	    	if (!successRun) {

	    	String error = String.format(tdcQueryError, run_id);

	    	boolean emptyError = error.isEmpty();

	    	if (!emptyError) {

	    	testCase.addTextEvidenceCurrentStep(

	    	"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

	    	testCase.addQueryEvidenceCurrentStep(is);

	    	}

	    	}

	    	u.close();

	    	/// Paso 3 *****************************************************

	    	addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");

	    	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

	    	String queryStatusLog = String.format(tdcQueryWmLog1, run_id);
	    	
	    	SQLResult Log2 = executeQuery(dbLog, tdcQueryWmLog2);
	    	
	    	System.out.println(queryStatusLog);

//	    	String fcwS = SQLUtil.getColumn(testCase, dbLog, queryStatusLog, "STATUS");
	    	
	    	testCase.addQueryEvidenceCurrentStep(Log2);
	    	
	    	SQLResult is1 = executeQuery(dbLog, queryStatusLog);
	    	
	    	String status2 = is1.getData(0, "STATUS");
	    	
	    	testCase.addQueryEvidenceCurrentStep(is1);
	    	
	    	boolean validateStatus = status.equals(status2);

	    	System.out.println(validateStatus);

	    	assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

	    	// Paso 4 ******************************************************

	    	addStep("Validar que los threads de la ejecución terminaron en estatus 'S' en la tabla WM_LOG_THREAD.");

	    	String queryStatusThread = String.format(tdcQueryStatusThread, run_id);

	    	System.out.println(tdcQueryStatusThread);
	    	
	    	SQLResult is2 = executeQuery(dbLog, queryStatusThread);
	    	
	    	String status3 = is2.getData(0, "STATUS");


	//    	String regPlazaTienda = SQLUtil.getColumn(testCase, dbLog, queryStatusThread, "STATUS");

	    	boolean statusThread = status.equals(status3);
	    	System.out.println(statusThread);

	    	if (!statusThread) {
	    	String error = String.format(tdcQueryError, run_id);
	    	
	    	SQLResult is3 = executeQuery(dbLog, error);

	    	boolean emptyError = is3.isEmpty();
	    	
	 //   	boolean emptyError = SQLUtil.isEmptyQuery(testCase, dbLog, error);

	    	if (!emptyError) {

	    	testCase.addTextEvidenceCurrentStep(

	    	"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

	    	testCase.addQueryEvidenceCurrentStep(is3);

	    	}

	    	}

	    	assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso"); 	
		
        //Paso 5
        addStep("Validar correcta inserción de los documentos enviados a Plaza en la tabla POS_OUTBOUND_DOCS de POSUSER"); 
           
           System.out.println(tdcQueryPosOutboundDocs);   
           
           SQLResult Currency = executeQuery(dbPuser, tdcQueryPosOutboundDocs);
   	       
   	       boolean outboundDocs = Currency.isEmpty();
           
	//     boolean outboundDocs = SQLUtil.isEmptyQuery(testCase, dbPuser, tdcQueryPosOutboundDocs); 
	       
   	       testCase.addQueryEvidenceCurrentStep(Currency);
        	
	    assertTrue(outboundDocs);
	
        
        //Paso 6
        //Se agregan dos consultas de la misma tabla 
   	    addStep("Validar la correcta actualización de WM_STATUS_CODE = 'E', Tipo de Cambio para las Tiendas en la tabla WM_SYNC_POS_EXC_RATES de ORAFIN");
   	    
   	      
  // 	    String wm_statusDb = SQLUtil.getColumn(testCase, dbEbs, tdcQueryWmSyncPosExcRates1, "WM_STATUS_CODE");//primera consulta
   	    
   	    	SQLResult wm_statusDb = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates1);
   	    	
   	    	String StatusE = wm_statusDb.getData(0, "WM_STATUS_CODE");
   	    	
   	    	SQLResult wm_statusDb2 = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates2);
   	    	
   	       testCase.addQueryEvidenceCurrentStep(wm_statusDb2);//segunda consulta
          
   	       boolean statusRate = StatusE.equals(wm_status);
           
        assertTrue(statusRate);

	}

	
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		return " Validar que el Tipo de Cambio Tienda sea enviado a la Tienda. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_RO12_002_SyncPosInsTienda";
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

