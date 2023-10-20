package interfaces.pe5;

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

public class ATC_FT_002_pe5_EnviarXMLaProveedorBEA1 extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_pe5_EnviarXMLaProveedorBEA1_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 ********************************************************************************************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, 
				GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
				   
	    
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/**
		 * ALM
		 * Enviar XML a Proveedor BEA1
		 */
		


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String VerifCon = "SELECT PROVEEDOR_ID,DESCRIPTION,FTP_IP,FTP_PORT,FTP_USR,FTP_PSW,LOCAL_DIR "
				+ "FROM RTPUSER.RTP_PROVEEDOR "
				+ "WHERE DESCRIPTION = 'BEA1' " 
				+ "AND ISACTIVE = 'Y' ";
		
		
//		Paso 2
		String ValidDatos =	"SELECT  FOLIO,TICKET,PLAZA,TIENDA,FECHAADMIN,WM_STATUS,PROVEEDOR,CREATION_DATE,LRT_ID "
				+ "FROM RTPUSER.POS_RTP_TRANS "
				+ "WHERE WM_STATUS = 'INSERTADO'   "
				+ "AND PROVEEDOR = 'BEA1'";
		  


//	Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PE5'  " + "AND START_DT >= TRUNC(SYSDATE) "
//				+ "AND STATUS = 'S'  "
				+ "order by start_dt desc";
		
		
//		Paso 5
		String VerifDatos=	"SELECT  FOLIO,TICKET,PLAZA,TIENDA,FECHAADMIN,FECHANATURAL,LRT_ID,CREATION_DATE,LRT_ID "
				+ "FROM rtpuser.POS_RTP_TRANS "
				+ "WHERE PROVEEDOR = 'BEA1' "
				+ "AND WM_STATUS = 'ENVIADO'"
				;

//		PASO 6
		String VerifEstatus ="	SELECT * "
				+ "FROM rtpuser.pos_rtp_lotes "
				+ "WHERE PROVEEDOR = 'BEA1' "
				+ "AND LRT_ID = '%s' ";
		
		

//****	
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PE5' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Verificar que la configuraci�n del proveedor BEA exista en la tabla RTP_PROVEEDOR de la BD RTPUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(VerifCon);
		SQLResult ExecVerifCon = dbPos.executeQuery(VerifCon);

		boolean ValidaBool = ExecVerifCon.isEmpty();

		if (!ValidaBool) {
			
			testCase.addQueryEvidenceCurrentStep(ExecVerifCon);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No se devuelve informacion de la configuracion" );

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Verificar que existan datos pendientes en la tabla POS_RTP_TRANS para el proveedor BEA1 en la BD de RTPUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ValidDatos);
		SQLResult ExecValidDatos = dbPos.executeQuery(ValidDatos);

		boolean ExecValidDatosRes = ExecValidDatos.isEmpty();

		if (!ExecValidDatosRes) {
			
			testCase.addQueryEvidenceCurrentStep(ExecValidDatos);
		}

		System.out.println(ExecValidDatosRes);
		assertFalse(ExecValidDatosRes, "No existan datos para confirmar en la tabla POS_RTP_LOTES" );
		
//************************************************Paso 3***************************		
		addStep("Ejecutar la interface por medio del servicio PE5.Pub:run, solicitando el job: runPE5");

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

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
//		***********************************Paso 4*******************************************************
		
		addStep("Validar que la interface haya finalizado correctamente en el WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la informaci�n del log.");
//********************************Paso 5***********************************************************
		
		addStep("Verificar el estatus actualizado en la tabla POS_RTP_TRANS de la BD RTPUSER.");
		String LRT_ID = "";
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(VerifDatos);
		SQLResult ExecVerifDatosFormat = dbPos.executeQuery(VerifDatos);

		boolean ExecVerifDatosFormatRes = ExecVerifDatosFormat.isEmpty(); 

		if (!ExecVerifDatosFormatRes) {
			LRT_ID = ExecValidDatos.getData(0, "LRT_ID");
			testCase.addQueryEvidenceCurrentStep(ExecVerifDatosFormat);
		}

		System.out.println(ExecVerifDatosFormatRes);
		assertFalse(ExecVerifDatosFormatRes, "No existan datos para confirmar en la tabla POS_RTP_TRANS" );
		
////		********************Paso 6***************************************************************

		addStep("Verificar que los datos del archivo enviado sean insertados la tabla POS_RTP_LOTES, de la BD RTPUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String VerifEstatusFormat = String.format(VerifEstatus, LRT_ID);
		System.out.println(VerifEstatusFormat);	
		SQLResult ExecVerifEstatusFormat = dbPos.executeQuery(VerifEstatusFormat);
		boolean ExecVerifEstatusFormatRes = ExecVerifEstatusFormat.isEmpty();

		if (!ExecVerifEstatusFormatRes) {
		
	testCase.addQueryEvidenceCurrentStep(ExecVerifEstatusFormat);
		}

		System.out.println(ExecVerifEstatusFormatRes);
		assertFalse(ExecVerifEstatusFormatRes, "No se actualizaron los datos" );

////	********************Paso 7***************************************************************
//		
//	String statusTrans = "	SELECT FOLIO,TICKET,PLAZA,TIENDA,FECHAADMIN,FECHANATURAL,LRT_ID,CREATION_DATE,wm_status "
//			+ " FROM rtpuser.pos_rtp_trans  	"
//			+ " WHERE lrt_id = '%s'  "
////			+ " AND creation_date > TRUNC(sysdate-3)  "
//			+ " AND wm_status = 'PROCESADO' ";
//	
//	addStep("Verificar el estatus actualizado en la tabla POS_RTP_TRANS de la BD RTPUSER.");
//
//	System.out.println(GlobalVariables.DB_HOST_Puser);
//	String statusTransFormat = String.format(statusTrans, LRT_ID);
//	System.out.println(statusTransFormat);	
//	SQLResult ExcecstatusTransFormat = dbPos.executeQuery(statusTransFormat);
//	boolean ExcecstatusTransFormatRes = ExcecstatusTransFormat.isEmpty();
//
//	if (!ExcecstatusTransFormatRes) {
//
//testCase.addQueryEvidenceCurrentStep(ExcecstatusTransFormat);
//	}
//
//	System.out.println(ExcecstatusTransFormatRes);
//	assertFalse(ExcecstatusTransFormatRes, "No se actualizo el estatus en la tabla POS_RTP_TRANS" );
	
		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Enviar XML a Proveedor BEA1";
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



