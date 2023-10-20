package interfaces.oe1;

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


public class OE1_GenerarEnviarTransaccionesPOS_Juegos_Y_sorteos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OE1_001_Transacciones_POS_Juegos_Sorteos(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
				   
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, 
				GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		
		

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String ValidConf =	"SELECT  a.vendor_id, a.legacy_id, hp.party_name  vendor_desc, a.attribute1 ip_address, a.attribute2 ftp_port, a.attribute3 ftp_user, a.attribute4 ftp_pass, "
			+  " a.attribute5 remote_path, a.attribute6 vendor_email, a.attribute7 oxxo_email, a.protocol, a.service_id ,trim(a.notificacion) "
			+"FROM    xxfc.xxfc_services_vendor_comm_data a,  AP.ap_suppliers pav,  AR.hz_parties hp "
			+"WHERE   pav.party_id = hp.party_id  "
			+"AND a.vendor_id = pav.vendor_id(+) " 
			+"AND a.service_type = 'N' "
			+"AND a.service_id = '"+data.get("NumServicio")+"'";

//		Paso 2
		String ValidInf ="SELECT  DISTINCT pid.ID,pid.PE_ID,pid.PV_DOC_ID,pid.STATUS,pid.DOC_TYPE,pid.PV_DOC_NAME,pid.RECEIVED_DATE,pid.INSERTED_DATE "
				+ "FROM POS_ENVELOPE pe, POS_INBOUND_DOCS pid, POS_MTJ_DETL mtd "
				+ "WHERE pid.DOC_TYPE = 'MTJ' "
				+ "AND pid.STATUS = 'A' "
				+ "AND pid.ID = mtd.PID_ID "
				+ "AND pe.ID = pid.PE_ID "
				+ "AND mtd.ITEM = '"+data.get("NumServicio")+"' ";
		
		
		
//Paso 4		
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'OE1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

//Paso 5 
		String Statusthread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID = '%s' ";

	//Paso 6
		String UpdStatus=	"SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE DOC_TYPE = 'MTJ' "
				+ "AND STATUS = 'L' "
				+ "AND ID = '%s' ";
		
		
//		Paso 7 
		String ValidInsert="SELECT * FROM POSUSER.WM_MTJ_CONTROL_ENVIO "
				+ "WHERE ITEM = '"+data.get("NumServicio")+"' "
				+ "AND LAST_SENT_DATE = TRUNC(SYSDATE)";
		
//		Ppaso 8 
		String InsertLeg = "SELECT * FROM "
				+ "LEGUSER.LEGACY_OUTBOUND_DOCS  "
				+ "WHERE LEGACY_USER = 'MTJ' "
				+ "AND RUN_ID = '%s' "
				+ "AND STATUS = 'E' ";
		
//		Paso 9
		String 	EnvioDocs = "SELECT VENDOR_ID,ORACLE_EF,SERVICE_ID,CREATION_DATE,LAST_UPDATE_DATE "
				+ "FROM XXFC.XXFC_SERVICES_VENDOR_COMM_DATA "
				+ "WHERE SERVICE_ID = '"+data.get("NumServicio")+"' " ;

//****	
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='OE1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1*********************************************************************************************************************************		
	 
		addStep("Validar la configuración del servidor FTP del vendor a procesar en la tabla xxfc_services_vendor_comm_data de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);

		System.out.println(ValidConf);

		SQLResult ExecValidConf = dbAvebqa.executeQuery(ValidConf);

		boolean ValidaBool = ExecValidConf.isEmpty();

		if (!ValidaBool) {
			
			testCase.addQueryEvidenceCurrentStep(ExecValidConf);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No se devuelve informacion" );

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Validar que existan información de documentos MTJ "
				+ "pendientes por procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'A' ");
		String Pid_ID="";
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ValidInf);	
		SQLResult ExecValidInf = dbPos.executeQuery(ValidInf);
		boolean ValidInfBool = ExecValidInf.isEmpty();

		if (!ValidInfBool) {
			Pid_ID = ExecValidInf.getData(0, "ID");
	testCase.addQueryEvidenceCurrentStep(ExecValidInf);
		}

		System.out.println(ValidInfBool);
		assertFalse(ValidInfBool, "No se devuelve informacion" );


//********************************Paso 3***********************************************************

	addStep("Ejecutar el servicio OE1.Pub:run desde el Job runOE1 de Ctrl-M para procesar la información");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R
		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);
			
			u.hardWait(2);

		}
		

////		********************Paso 4***************************************************************
		 
			
		addStep("Validar la correcta ejecución de la interface OE1 en la tabla WM_LOG_RUN de WMLOG.");

		String RunID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");
	
//**********************************************************Paso 5*************************************************************************************************************			

		addStep("Validar la correcta ejecución de los Threads lanzados por la interface OE1 en la tabla WM_LOG_THREAD de WMLOG.");
		String THREAD_ID="";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String StatusthreadFormat = String.format(Statusthread,RunID);
		System.out.println(StatusthreadFormat);
		SQLResult ExecStatusthreadFormat = dbLog.executeQuery(StatusthreadFormat);

		boolean ReqThread= ExecStatusthreadFormat.isEmpty();

		if (!ReqThread) {
			THREAD_ID=ExecStatusthreadFormat.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(ExecStatusthreadFormat);

		}

		System.out.println(ReqThread);
		assertFalse(ReqThread, "No se muestra la información del log.");
		
		
//		**********************************Paso 6 ****************************************************
			 
	
		addStep("Validar la actualización del STATUS = 'L' de los documentos MTJ enviados en la tabla POS_INBOUND_DOCS de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String UpdStatusFormat = String.format(UpdStatus, Pid_ID);
		System.out.println(UpdStatusFormat);
		
		SQLResult ExecUpdStatusFormat = dbPos.executeQuery(UpdStatusFormat);
		
		boolean ExecUpdStatusFormatRes = ExecUpdStatusFormat.isEmpty();

		if (!ExecUpdStatusFormatRes) {
			testCase.addQueryEvidenceCurrentStep(ExecUpdStatusFormat);

		}

		System.out.println(ExecUpdStatusFormatRes);
		assertFalse(ExecUpdStatusFormatRes, "La información no ha sido actualizada");


//		************************************Paso 7 ****************************************************************************
		
addStep("Validar la inserción de los documentos enviados al Proveedor en la tabla de control WM_MTJ_CONTROL_ENVIO de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ValidInsert);
		
		SQLResult ExecValidInsert = dbPos.executeQuery(ValidInsert);
		
		boolean ExecValidInsertRes = ExecValidInsert.isEmpty();

		if (!ExecValidInsertRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidInsert);

		}

		System.out.println(ExecValidInsertRes);
		assertFalse(ExecValidInsertRes, "No fueron insertados los documentos enviados en la tabla de control WM_MTJ_CONTROL_ENVIO");
		
		
//		*****************************************Paso 8 ***************************************************************************

		addStep("Validar la inserción de los documentos enviados al Proveedor en la tabla LEGACY_OUTBOUND_DOCS del sistema Legacy.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String InsertLegFormat = String.format(InsertLeg, THREAD_ID);
		System.out.println(InsertLegFormat);	
		SQLResult ExecInsertLegFormat = dbPos.executeQuery(InsertLegFormat);
		
		boolean ExecInsertLegFormatRes = ExecInsertLegFormat.isEmpty();

		if (!ExecInsertLegFormatRes) {
			testCase.addQueryEvidenceCurrentStep(ExecInsertLegFormat);

		}

		System.out.println(ExecInsertLegFormatRes);
		assertFalse(ExecInsertLegFormatRes, "No fueron insertados los documentos enviados en la tabla LEGACY_OUTBOUND_DOCS");
		
		
		
//		**********************************************************Paso 9 ************************************************************
		
	addStep("Validar el envío de los documentos MTJ vía FTP "
		+ "al Proveedor con la información de las transacciones POS de los servicios de juegos y sorteos.");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(EnvioDocs);	
		SQLResult ExecEnvioDocs = dbAvebqa.executeQuery(EnvioDocs);
		
		boolean ExecEnvioDocsRes = ExecEnvioDocs.isEmpty();

		if (!ExecEnvioDocsRes) {
			testCase.addQueryEvidenceCurrentStep(ExecEnvioDocs);

		}

		System.out.println(ExecEnvioDocsRes);
		assertFalse(ExecEnvioDocsRes, "No se obtuvieron resultados");
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OE1_001_Transacciones_POS_Juegos_Sorteos";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Generar y enviar las transacciones POS de juegos y sorteos al proveedor para servicios";
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



