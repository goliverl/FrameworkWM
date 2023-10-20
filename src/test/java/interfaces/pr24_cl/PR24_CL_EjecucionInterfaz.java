package interfaces.pr24_cl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR24_CL_EjecucionInterfaz extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR24_CL_001_EjecucionInterfaz(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/	
		
		String tdcQueryRegistros = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.ID, a.partition_date"
				+ " FROM POS_ENVELOPE A, POS_INBOUND_DOCS B, POS_SAL_DETL "
				+ " WHERE B.PE_ID=A.ID"
				+ " AND B.ID = C.PID_ID"
				+ " AND B.DOC_TYPE='SAL'"
//				+ " AND B.PARTITION_DATE>=TRUNC(SYSDATE-7)"
				+ " AND B.STATUS='I'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%PR24%'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";	
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 
		
		String tdcQuerySAL = "SELECT DOC_TYPE, DOC_ID, RUN_ID, STATUS, CR_PLAZA, CR_TIENDA "
				+ " FROM wmuser.RTK_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'SAL'"
				+ " AND STATUS = 'L'"
				+ " AND run_id in (%s)";
		
		String tdcQueryprocesado = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, PV_DOC_NAME "
				+ " FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'SAL'"
				+ " AND STATUS = 'E'"
				+ " AND ID IN (%s)";
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 **************************	
		addStep("Ingresar a la BD WM Chile.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_LogChile);
	
//Paso 2 **************************	
		addStep("Buscar registros para procesar en las tablas POS_ENVELOPE, POS_INBOUND_DOCS y POS_SAL_DETL de POSUSER.");		
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
		
//Paso 3 *********************
		addStep("Ejecutar el servicio PR24.Pub:run.");
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
		
//Paso 4	************************ 	
		addStep("Validar que se inserte el detalle de la ejecución de la interface PR24 en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.");
		
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
		
//Paso 5	************************
		addStep("Se valida la generacion de thread.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);
		String statusThread = threadResult.getData(0, "Status");
		String thread_id = threadResult.getData(0, "tread_id");

		boolean ST = statusThread.equals(status);
		ST = !ST;

		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
				    
				} 

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");		
		
//Paso 6 **************************	
		addStep("Ingresar a la BD RMS Chile.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: BDCHRMSQ.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_RMSWMUSERChile);		
		
//Paso 7 *************************
		addStep("Validar que se inserten los registros de los documentos SAL procesados en la tabla RTK_INBOUND_DOCS de RETEK con STATUS = 'L'. El run id de cada registro es el thread que se lanzo por cada documento.");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		String docSALformat= String.format(tdcQuerySAL, thread_id);
		SQLResult docSALresult = executeQuery(dbRms, docSALformat);

		System.out.println(docSALformat);
	

		boolean docSAL = docSALresult.isEmpty();

		if (!docSAL) {

			testCase.addQueryEvidenceCurrentStep(docSALresult);
				    
				} 

		System.out.println(docSAL);

		assertFalse(docSAL, "No se obtiene información de la consulta");
		
//Paso 9 *********************
		addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS='E'.");
		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String procesadoFormat= String.format(tdcQueryprocesado, id);
		SQLResult procesadoResult = executeQuery(dbPos, docSALformat);

		System.out.println(procesadoFormat);
		String doc = "";

		boolean procesado = procesadoResult.isEmpty();

		if (!procesado) {

			testCase.addQueryEvidenceCurrentStep(procesadoResult);
			doc = procesadoResult.getData(0, "PV_DOC_NAME");
			System.out.println("docum: " + doc);
				    
				} 

		System.out.println(procesado);

		assertFalse(procesado, "No se obtiene información de la consulta");
		
//Paso 8 ************************* falta la ruta correcta
		addStep("Validar que se realice el envío de los documentos SALs via FTP al servidor de RETEK de manera correcta.");
		StringBuilder DOC = new StringBuilder(doc);
		int i = 1;
		while (i <= 5) {
			DOC = DOC.deleteCharAt(8); // 8,9,10,11,12
			i++;
		}
//		addStep("Comprobar que se genere el archivo y se almacene en la ruta del FileSystem: /u01/posuser/FEMSA_OXXO/POS/"
//				+ data.get("cr_plaza") + "/working/" + DOC);
//
//		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
//
//		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("cr_plaza") + "/working/" + DOC;
//		System.out.println("Ruta: " + ruta);
//
//		if (ftp.fileExists(ruta)) {
//
//			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
//					+ data.get("cr_plaza") + "/working/" + DOC);
//
//		} else {
//
//			System.out.println("No Existe");
//
//		}
//
//		assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);
		

		
		
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
		return "Construido. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR24_CL_001_EjecucionInterfaz";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
