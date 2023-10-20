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

public class RO12SyncRtkRmsCorporate extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO12_004_SyncRtkRmsCorporate(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
	//	SqlUtil dbWm = new SqlUtil(GlobalVariables.DB_USER_DataWmuser, GlobalVariables.DB_PASSWORD_DataWmuser, GlobalVariables.DB_HOST_RmsP);
	//	SqlUtil dbLog = new SqlUtil(GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA, GlobalVariables.DB_HOST_FCWMLQA);
	//	SqlUtil dbRms = new SqlUtil(GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX, GlobalVariables.DB_HOST_RMS_MEX);
	//	SqlUtil dbEbs = new SqlUtil(GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs, GlobalVariables.DB_HOST_Ebs);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs);
								
		
		/* Variables *************************************************************************/
		
		/*String tdcInsertOrafin = "INSERT INTO GL.GL_DAILY_RATES"
				+ "(FROM_CURRENCY, TO_CURRENCY, CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE,"
				+ " STATUS_CODE, CREATION_DATE, CREATED_BY, LAST_UPDATE_DATE, LAST_UPDATED_BY, "
				+ "LAST_UPDATE_LOGIN, CONTEXT, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, "
				+ "ATTRIBUTE5, ATTRIBUTE6, ATTRIBUTE7, ATTRIBUTE8, ATTRIBUTE9, ATTRIBUTE10, ATTRIBUTE11,"
				+ " ATTRIBUTE12, ATTRIBUTE13, ATTRIBUTE14, ATTRIBUTE15, RATE_SOURCE_CODE) VALUES "  
				+" ('"+ data.get("FROM") +"','"+ data.get("TO") +"',TO_DATE('"+ data.get("CONVERSION_DATE") 
				+"','DD/MM/YYYY'),'"+ data.get("CONVERSION_TYPE") +"','"+ data.get("CONVERSION_RATE") +"','"+ data.get("STATUS_CODE") +"',"
				+ "TO_DATE('"+ data.get("CREATION_DATE") +"','DD/MM/YYYY'), '"+ data.get("CREATED_BY") +"',"
			    + "TO_DATE('"+ data.get("LAST_UPDATE_DATE") +"','DD/MM/YYYY'),'"+ data.get("LAST_UPDATED_BY") +"',"
			    + "'"+ data.get("LAST_UPDATE_LOGIN") +"',NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,NULL,"
				+ " NULL, NULL, NULL, NULL, NULL, NULL, NULL)";
		*/
		
		String tdcSelectOrafin = "SELECT CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE, STATUS_CODE, CREATION_DATE"+
				" FROM GL.GL_DAILY_RATES" + 
				" WHERE CONVERSION_DATE =TO_DATE('14/12/2021','DD/MM/RRRR')"+
				" AND CONVERSION_TYPE = 'Corporate'" + 
				" AND CONVERSION_RATE = '18.32'" +
				" AND STATUS_CODE = 'C'" +
				" AND CREATION_DATE =TO_DATE('"+ data.get("CREATION_DATE")+"','DD/MM/RRRR')";
		
		String tdcQueryWmLog1 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER"+
				" FROM WMLOG.WM_LOG_RUN" + 
				" WHERE INTERFACE = 'RO12_MX'" + 
				" AND STATUS = 'S'" + 
				" AND ATT2 = 'C'"+
				" AND START_DT > TRUNC(SYSDATE)" + 
				" ORDER BY START_DT DESC"; 
		
		String tdcQueryWmLog2 = "SELECT ATT1, ATT2, ATT3, ATT4, ATT5 "+
				" FROM WMLOG.WM_LOG_RUN" +
				" WHERE INTERFACE = 'RO12_MX'" + 
				" AND STATUS = 'S'" + 
				" AND ATT2 = 'C'"+
				" AND START_DT > TRUNC(SYSDATE)" + 
				" ORDER BY START_DT DESC"; 
      
		String tdcQueryRmsCurrencyRates = "SELECT * "
				+"FROM RMS100.CURRENCY_RATES"  
				+"WHERE EFFECTIVE_DATE>=TO_DATE('14/12/2065','DD/MM/RRRR')"; 
		
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
        
	    System.out.println(tdcSelectOrafin);      

	      
		/* Pasos *****************************************************************************/   

		/* **************************************************************************/   
	   	 
		//Paso 1   
	    
	    addStep("Buscar y verificar el interst en la tabla GL.GL_DAILY_RATES de ORAFIN"); 
	    
    	System.out.println(tdcSelectOrafin);    
    	
    	SQLResult result = dbEbs.executeQuery(tdcSelectOrafin);
    	boolean orafin = result.isEmpty();
    	
   // 	boolean orafin = SQLUtil.isEmptyQuery(testCase, dbEbs, tdcSelectOrafin);       
    	
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
    	    	
    //	String[] is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS",

    //	"RUN_ID");
    	
    	
    	SQLResult is = executeQuery(dbLog, tdcQueryIntegrationServer);
    	
    	String status1 = is.getData(0, "STATUS");
    	String run_id = is.getData(0, "RUN_ID");


    //	run_id = is[1];// guarda el run id de la ejecución

    	boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

    	while (valuesStatus) {

   //     is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");

    	 is = executeQuery(dbLog, tdcQueryIntegrationServer);
        	
        	 status1 = is.getData(0, "STATUS");
        	 run_id = is.getData(0, "RUN_ID");
        	 
    	valuesStatus = status1.equals(searchedStatus);

    	u.hardWait(2);

    	}

    	boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

    	if (!successRun) {

    	String error = String.format(tdcQueryError, run_id);

/*1*/  	boolean emptyError = error.isEmpty();

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
    	
     	testCase.addQueryEvidenceCurrentStep(Log2);
    	
 /*1*/  SQLResult is1 = executeQuery(dbLog, queryStatusLog);

 /*2*/ 	String status2 = is1.getData(0, "STATUS");

    	//String fcwS = SQLUtil.getColumn(testCase, dbLog, queryStatusLog, "STATUS");
 /*3*/  testCase.addQueryEvidenceCurrentStep(is1);
    	
    	boolean validateStatus = status.equals(status2);

    	System.out.println(validateStatus);
    	
    	
    	

    	assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

    	// Paso 4 ******************************************************

    	addStep("Validar que los threads de la ejecución terminaron en estatus 'S' en la tabla WM_LOG_THREAD.");

    	String queryStatusThread = String.format(tdcQueryStatusThread, run_id);

    	System.out.println(tdcQueryStatusThread);
    	
    	SQLResult is2 = executeQuery(dbLog, queryStatusThread);
    	
    	String status3 = is2.getData(0, "STATUS");

    //	String regPlazaTienda = SQLUtil.getColumn(testCase, dbLog, queryStatusThread, "STATUS");

    	boolean statusThread = status.equals(status3);
    	System.out.println(statusThread);

    	if (!statusThread) {
    	
    	String error = String.format(tdcQueryError, run_id);
    	
    	SQLResult is3 = executeQuery(dbLog, error);

    	boolean emptyError = is3.isEmpty();

    //	boolean emptyError = SQLUtil.isEmptyQuery(testCase, dbLog, error);

    	if (!emptyError) {

    	testCase.addTextEvidenceCurrentStep(

    	"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

    	testCase.addQueryEvidenceCurrentStep(is3);

    	}

    	}

    	assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso"); 	
	
        //Paso 5
   	    addStep("Validar que Tipo de Cambio Corporate sea enviado de manera correcta a Retek"); 
   	       
   	       System.out.println(tdcQueryRmsCurrencyRates); 
   	       
   	       SQLResult Currency = executeQuery(dbRms, tdcQueryRmsCurrencyRates);
   	       
   	       boolean outboundDocs = Currency.isEmpty();
   	    		   
  //       boolean outboundDocs = SQLUtil.isEmptyQuery(testCase, dbRms, tdcQueryRmsCurrencyRates); 
           
           testCase.addQueryEvidenceCurrentStep(Currency);
 
         assertFalse(outboundDocs);
        	
	}

	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		return " Validar que el Tipo de Cambio Corporate sea enviado a Retek. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_RO12_004_SyncRtkRmsCorporate";
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

