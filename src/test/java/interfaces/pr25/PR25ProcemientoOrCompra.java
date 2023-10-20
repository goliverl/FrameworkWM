package interfaces.pr25;

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
import utils.sql.SQLResult;

public class PR25ProcemientoOrCompra extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR25_001_Procemiento_Or_Compra(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

		SeleniumUtil u;
		PakageManagment pok;		
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");    

/** VARIABLES *********************************************************************/	
				
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id, tienda="", plaza="", orderNum="", retek_cr="", doc_name="";

		String selectRetek2 = "SELECT RETEK_CR, ORACLE_CR_SUPERIOR, ORACLE_CR, ORACLE_CR_DESC" 
				+ " FROM XXFC_MAESTRO_DE_CRS_V" 
				+ " WHERE ESTADO = 'A'"
				+ " AND ORACLE_CR_TYPE = 'T' " 
				+ " AND retek_cr = '%s'";
		
		String tdcQueryFTPserv = "SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username "
				+ " FROM wmuser.wm_ftp_connections" + " WHERE ftp_conn_id = 'PR50POS'"; // FCWMQA
		
		String tdcQueryOrder2 = "SELECT a.load_batch_id,a.order_number,a.wm_status,location  " + 
				" FROM WMUSER.ordenes_control a, WMUSER.ordenes_head b" + 
				" WHERE a.load_batch_id = b.load_batch_id AND a.order_number = b.order_number " + 
				" AND  b.external_order_num IS NULL AND a.wm_status = 'L' order by a.load_date desc";

		String consultaThreads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC"; // FCWMLQA 
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE = 'PR25main' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String consultaError1 = "select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = "select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = "select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1"; //FCWMLQA 	
		
		String verifyFile = "SELECT ID, doc_type, status, DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED "
				+ " FROM POSUSER.POS_OUTBOUND_DOCS WHERE PV_CR_PLAZA = '%s'" 
				+ " AND PV_CR_TIENDA = '%s' AND doc_type = 'POD' AND status = 'L' and TRUNC(SENT_DATE) = TRUNC(SYSDATE) order by sent_date desc";

		String retekE = "SELECT load_batch_id,order_number,wm_status FROM WMUSER.ordenes_control WHERE wm_status = 'E' " + 
				"    AND wm_run_id = '%s'  AND location = '%s' " + 
				"    AND order_number = '%s'";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/

		addStep("Validar la configuración del servidor FTP del POS");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryFTPserv);		
		SQLResult tdcQueryFTPservRes = dbPos.executeQuery(tdcQueryFTPserv);

		boolean serv = tdcQueryFTPservRes.isEmpty();
	
		if (!serv) {
			testCase.addQueryEvidenceCurrentStep(tdcQueryFTPservRes);			
		}

		System.out.println(serv);
		assertFalse(serv, "No se tiene conexion al servidor FTP");
		
		/* PASO 2 *********************************************************************/

		addStep("Validar que existan ordenes de compra pendientes de procesar para la Tienda.");		
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryOrder2);
		SQLResult paso1 = executeQuery(dbRms, tdcQueryOrder2);		

		boolean av = paso1.isEmpty();
		
		if (!av) {
			retek_cr = paso1.getData(0, "location");
			orderNum = paso1.getData(0, "order_number");
			testCase.addQueryEvidenceCurrentStep(paso1);
		} 

		System.out.println(av);		
		assertFalse(av, "No se encontraron ordenes validas en RMS en la tabla ORDENES_CONTROL y ORDENES_HEAD");

		String consultafor = "";
		SQLResult resultfor;
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);	
		boolean registro = false;
		
		int i = 0;
		do  {
			String tienda2 = paso1.getData(i, "location");
			consultafor = String.format(selectRetek2, tienda2);//Consulta si hay resultados en la tabla para la tienda
			if (i==0) System.out.println(consultafor);
			resultfor = executeQuery(dbRms, consultafor);			
			boolean foav = resultfor.isEmpty(); 	//Si no esta vacio, se pasan los datos del validos a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO LA TIENDA  #: " + tienda2 + " en la tabla RMS? "+ foav);

			if (!foav) {
				retek_cr = paso1.getData(i, "location");
				plaza = resultfor.getData(i, "ORACLE_CR_SUPERIOR");
				tienda = resultfor.getData(i, "ORACLE_CR");
				registro = true;
			}			
			i++;
		} while ((i < paso1.getRowCount() && registro == false));
		
		System.out.println("Hay orden valido en RMS? " + registro + " es retek: " + retek_cr);

		testCase.addQueryEvidenceCurrentStep(resultfor);

		assertTrue(registro, "No se obtiene informacion de la consulta en la tabla XXFC_MAESTRO_DE_CRS_V de RMS");		
		
		/* PASO 3 *********************************************************************/
		
		addStep("Ejecutar la interfaz PR25");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S		

		if (!successRun) {
			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	

		    
		/* PASO 4 *********************************************************************/		    
		    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		//assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
		
		/* PASO 5 *********************************************************************/

		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");		
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);		
		String consultaTemp6 = String.format(consultaThreads, run_id);	
		System.out.println(consultaTemp6);		
		SQLResult paso6 = executeQuery(dbLog, consultaTemp6);	
	    Thread.sleep(8000);

		boolean validacion6 = paso6.isEmpty();

		if (!validacion6) {
			String estatusThread = paso6.getData(0, "Status");		
			run_id = paso6.getData(0, "THREAD_ID");		
			validacion6 = estatusThread.equals(status);
			System.out.println(validacion6);
			testCase.addQueryEvidenceCurrentStep(paso6);			

			validacion6 = !validacion6;		//Como se reutiliza el mismo booleano que el isEmpty lo regreso al mismo estado que estaba antes del if
		}					
		

		//assertFalse(validacion6, "No se obtiene informacion o Thread con STATUS diferente a 'S' de la consulta en la tabla WM_LOG_THREAD");

		/* PASO 6 *********************************************************************/
		
		addStep("Validar el registro del documento enviado al servidor FTP del POS en la tabla pos_outbound_docs de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String file = String.format(verifyFile, plaza, tienda);
		System.out.println(file);
		SQLResult paso7 = executeQuery(dbPos, file);

		boolean step7 = paso7.isEmpty();
		
		if (!step7) {
			doc_name = paso7.getData(0, "DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(paso7);			
		} 

		System.out.println(step7);		
		assertFalse(step7, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		Thread.sleep(15000);
		
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/FEMSA_OXXO/POS/"+ plaza +"/"+ tienda +"/working/" + doc_name;
		System.out.println(path);

		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
		} else {
			System.out.println("No Existe");
		}
		
		assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP con la ruta " + path);
		

		/* PASO 7 *********************************************************************/
		
		addStep("Validar la actualización del estatus de la ordern a 'E' en la tabla ordenes_control de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);		
		String rtk = String.format(retekE, run_id, retek_cr, orderNum);
		System.out.println(rtk);
		SQLResult querySatusLogRes = dbRms.executeQuery(rtk);

		boolean upOrder = querySatusLogRes.isEmpty();
		if (!upOrder) {
			testCase.addQueryEvidenceCurrentStep(querySatusLogRes);
		}

		System.out.println(upOrder);
		assertFalse(upOrder, "no se encuentran oredenes actualizadas");
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada..La interface PR25 envía los datos empresariales relacionados con Órdenes de Compra generadas por Retek (RMS) a los sistemas POS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR25_001_Procemiento_Or_Compra";
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
