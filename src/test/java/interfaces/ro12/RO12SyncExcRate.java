package interfaces.ro12;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR2;
import util.GlobalVariables;
import util.RequestUtil;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class RO12SyncExcRate extends BaseExecution {
	public String tipo;

	/*
	 * comentario
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO12_001_SyncExcRate(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_Ebs,GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs);
		SQLUtil dbPuser = new SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
//		SQLUtil dbWm = new SQLUtil(GlobalVariables.DB_HOST_Data, GlobalVariables.DB_USER_Data, GlobalVariables.DB_PASSWORD_Data);


		

		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryWmFtpConnections = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME"
				+ " FROM  WMUSER.WM_FTP_CONNECTIONS" + " WHERE  FTP_CONN_ID = 'PR50POS'";

		String tdcInsertOrafin = "INSERT INTO GL.GL_DAILY_RATES"
				+ "(FROM_CURRENCY, TO_CURRENCY, CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE,"
				+ " STATUS_CODE, CREATION_DATE, CREATED_BY, LAST_UPDATE_DATE, LAST_UPDATED_BY, "
				+ "LAST_UPDATE_LOGIN, CONTEXT, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, "
				+ "ATTRIBUTE5, ATTRIBUTE6, ATTRIBUTE7, ATTRIBUTE8, ATTRIBUTE9, ATTRIBUTE10, ATTRIBUTE11,"
				+ " ATTRIBUTE12, ATTRIBUTE13, ATTRIBUTE14, ATTRIBUTE15, RATE_SOURCE_CODE) VALUES " + " ('"
				+ data.get("FROM") + "','" + data.get("TO") + "',TO_DATE('" + data.get("CONVERSION_DATE")
				+ "','DD/MM/YYYY'),'" + data.get("CONVERSION_TYPE") + "','" + data.get("CONVERSION_RATE") + "','"
				+ data.get("STATUS_CODE") + "'," + "TO_DATE('" + data.get("CREATION_DATE") + "','DD/MM/YYYY'), '"
				+ data.get("CREATED_BY") + "'," + "TO_DATE('" + data.get("LAST_UPDATE_DATE") + "','DD/MM/YYYY'),'"
				+ data.get("LAST_UPDATED_BY") + "'," + "'" + data.get("LAST_UPDATE_LOGIN")
				+ "',NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,NULL,"
				+ " NULL, NULL, NULL, NULL, NULL, NULL, NULL)";

		// La consulta a WM_LOG_RUN se divide en dos para mostrar los datos ATT
		String tdcQueryWmLog1 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER " 
		        + " FROM WMLOG.WM_LOG_RUN"
				+ " WHERE INTERFACE = 'RO12_MX'" 
		        + " AND ATT2 = '" + data.get("ATT2") + "'"
				+ " AND START_DT >= TRUNC(SYSDATE)" 
				+ " ORDER BY START_DT DESC";

		String tdcQueryWmLog2 = "SELECT ATT1,ATT2, ATT3, ATT4, ATT5 " 
		        + " FROM WMLOG.WM_LOG_RUN"
				+ " WHERE INTERFACE = 'RO12_MX'" 
		        + " AND ATT2 = '" 
				+ data.get("ATT2") + "'"
				+ " AND START_DT > TRUNC(SYSDATE)" 
				+ " ORDER BY START_DT DESC";

		// La consulta a WM_SYNC_POS_EXC_RATESse divide en dos para mostrar todos los
		// datos
		String tdcQueryWmSyncPosExcRates1 = "SELECT ID,CR_PLAZA,CR_TIENDA,EXC_RATE_TYPE,WM_STATUS_CODE,FROM_CURRENCY, TO_CURRENCY,CONVERSION_DATE"
				+ " FROM WMUSER.WM_SYNC_POS_EXC_RATES" + " WHERE WM_STATUS_CODE='E'" + " AND CONVERSION_TYPE = '"
				+ data.get("CONVERSION_TYPE") + "'" + " AND CONVERSION_RATE = '" + data.get("CONVERSION_RATE") + "'"
				+ " AND LAST_UPDATE_DATE >= TRUNC(SYSDATE)";

		String tdcQueryWmSyncPosExcRates2 = "SELECT CONVERSION_TYPE,CONVERSION_RATE,USER_CONVERSION_TYPE,,CREATION_DATE,LAST_UPDATE_DATE"
				+ " FROM WMUSER.WM_SYNC_POS_EXC_RATES" + " WHERE WM_STATUS_CODE='E'" + " AND CONVERSION_TYPE = '"
				+ data.get("CONVERSION_TYPE") + "'" + " AND CONVERSION_RATE = '" + data.get("CONVERSION_RATE") + "'"
				+ " AND LAST_UPDATE_DATE >= TRUNC(SYSDATE)";

		String tdcQueryPosOutboundDocs = "SELECT ID,POE_ID,DOC_NAME,DOC_TYPE,SENT_DATE,STATUS,DATE_CREATED"
				+ " FROM POSUSER.POS_OUTBOUND_DOCS" + " WHERE DOC_TYPE = 'EXR'" + " AND PARTITION_DATE>=TRUNC(SYSDATE)"
				+ " ORDER BY SENT_DATE DESC";

		String tdcQueryIntegrationServer = "select * from (SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO12_MX'" + " AND START_DT >= TRUNC (SYSDATE)" + " order by start_dt desc)"
				+ " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2 " + " FROM wmlog.wm_log_thread "
				+ " WHERE parent_id = %s";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" + " WMLOG.wm_log_error"
				+ " WHERE run_id =  %s";

		String tdcGlDaily = "SELECT * FROM GL.GL_DAILY_RATES" + " WHERE CONVERSION_DATE = TO_DATE('"
				+ data.get("CONVERSION_DATE") + "','DD/MM/RRRR')" + " AND CONVERSION_TYPE = '"
				+ data.get("CONVERSION_TYPE") + "'" + " AND CONVERSION_RATE = '" + data.get("CONVERSION_RATE") + "'";
		// "AND CREATION_DATE = TO_DATE('27/08/2003','DD/MM/RRRR');";

		// Paso Consulta
		String statusWmlog = "S", wm_status = "E";
		tipo = data.get("TIPO_CAMBIO");
		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("USER");
		String ps = PasswordUtil.decryptPassword(data.get("PS"));
		String server = data.get("SERVER");
		String searchedStatus = "R";
		String status = "S";
		

		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************
		addStep("Comprobar que existan datos de conexión del servidor FTP en la base de WMINT ");

		System.out.println(tdcQueryWmFtpConnections);

		SQLResult paso1 = dbPuser.executeQuery(tdcQueryWmFtpConnections);

		boolean conexionFtpEmpty = paso1.isEmpty();
		
		testCase.addQueryEvidenceCurrentStep(paso1);

		assertFalse(conexionFtpEmpty);

		/// Paso 2 ***************************************************
		addStep("Verificar que se haya insertado un Tipo de Cambio "+tipo+" en la tabla GL.GL_DAILY_RATES de ORAFIN");

		System.out.println(tdcQueryWmFtpConnections);
		
		SQLResult paso2 = dbPuser.executeQuery(tdcQueryWmFtpConnections);

		boolean insertGlDaily = paso2.isEmpty();

//		boolean insertGlDaily = SQLUtil.isEmptyQuery(testCase, dbWm, tdcQueryWmFtpConnections);
		
		testCase.addQueryEvidenceCurrentStep(paso2);

		assertFalse(insertGlDaily);

		/// Paso 3 ***************************************************

		addStep("Ejecutar la interfaz RO12 con el servicio " + data.get("interface"));

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u.get(contra);

		u.hardWait(4);

		pok.runIntefaceWmOneButton(data.get("interface"), data.get("servicio"));

		SQLResult is = executeQuery(dbLog, tdcQueryIntegrationServer);
    	
    	String status1 = is.getData(0, "STATUS");
    	String run_id = is.getData(0, "RUN_ID");


		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {

			is = executeQuery(dbLog, tdcQueryIntegrationServer);
        	
	       	 status1 = is.getData(0, "STATUS");	       	 
						
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

		

		/// Paso 4 *****************************************************
		addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");

    	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

    	String queryStatusLog = String.format(tdcQueryWmLog1, run_id);
    	
    	SQLResult Log2 = executeQuery(dbLog, tdcQueryWmLog2);
    	
    	System.out.println(queryStatusLog);
    	
     	testCase.addQueryEvidenceCurrentStep(Log2);
    	
 /*1*/  SQLResult is1 = executeQuery(dbLog, queryStatusLog);

 /*2*/ 	String status2 = is1.getData(0, "STATUS");

 /*3*/  testCase.addQueryEvidenceCurrentStep(is1);
    	
    	boolean validateStatus = status.equals(status2);

    	System.out.println(validateStatus);

		// Paso 5 ******************************************************

		addStep("Validar que los threads de la ejecución terminaron en estatus 'S' en la tabla WM_LOG_THREAD.");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);

		System.out.println(queryStatusThread);
		
		SQLResult is2 = executeQuery(dbLog, queryStatusThread);
    	
    	String status3 = is2.getData(0, "STATUS");

//		String regPlazaTienda = SQLUtil.getColumn(testCase, dbLog, queryStatusThread, "STATUS");

		boolean statusThread = status.equals(status3);

		System.out.println(statusThread);

		if (!statusThread) {

			String error = String.format(tdcQueryError, run_id);
			
			SQLResult is3 = executeQuery(dbLog, error);

			boolean emptyError = is3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(is3);

			}
		}

		assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		/// Paso 6 ***************************************************
		addStep("Validar la correcta actualización de WM_STATUS_CODE = 'E', Tipo de Cambio para las Tiendas en la tabla WM_SYNC_POS_EXC_RATES");

//		System.out.println(tdcQueryWmSyncPosExcRates1);

//		String wm_statusDb = SQLUtil.getColumn(testCase, dbEbs, tdcQueryWmSyncPosExcRates1, "WM_STATUS_CODE");// primera
																												// consulta
		SQLResult wm_statusDb = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates1);
	    	
	    String StatusE = wm_statusDb.getData(0, "WM_STATUS_CODE");
	    
	    SQLResult wm_statusDb2 = executeQuery(dbEbs, tdcQueryWmSyncPosExcRates2);
	    	
		testCase.addQueryEvidenceCurrentStep(wm_statusDb2);

		boolean statusRate = StatusE.equals(wm_status);

		 assertTrue(statusRate);

		/// Paso 7***************************************************
		addStep("Validar correcta inserción de los documentos enviados a Plaza en la tabla POS_OUTBOUND_DOCS de POSUSER");

		System.out.println(tdcQueryPosOutboundDocs);
		
		SQLResult Currency = executeQuery(dbPuser, tdcQueryPosOutboundDocs);

		boolean outboundDocs = Currency.isEmpty();

		testCase.addQueryEvidenceCurrentStep(Currency);

		 assertTrue(outboundDocs);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return " Validar que el Tpo Cambio Corp. sea distrib. y aplic. a todas las tdas y pzas. " + tipo;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_RO12_001_SyncExcRate";
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
