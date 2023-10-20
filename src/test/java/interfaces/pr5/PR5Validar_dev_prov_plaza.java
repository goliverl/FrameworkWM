package interfaces.pr5;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR5Validar_dev_prov_plaza extends BaseExecution {
	@Test(dataProvider = "data-provider")

	public void ATC_FT_PR5_001_Validar_Dev_Prov_Plaza(HashMap<String, String> data) throws Exception {

		/**
		 * @category pruebas
		 * @param data
		 * @throws Exception
		 * @description :Se hizo una prueba y se completo correctamente.
		 * @dateup 28 Enero 2021
		 */
	
		/*
		 * Utilerías pr5
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR5%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String consultaRTV = "select * from (select ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME, PARTITION_DATE "
				+ "from POSUSER.POS_INBOUND_DOCS  "
				+ " where DOC_TYPE='RTV'"
			//	+ " and  SUBSTR(PV_DOC_NAME,4,5) = '"+ data.get("plaza") + "' "
			    + "and STATUS='I' ORDER BY received_date DESC) where rownum <= 3 ";
		
		String consultaRTVCabecera = "SELECT * FROM POSUSER.POS_RTV WHERE PID_ID ='%s'";
		String detallePos_RTV_DETL = "SELECT * FROM POSUSER.POS_RTV_DETL WHERE PID_ID='%s'";
		String consultathread = "select * from (SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD where PARENT_ID= '%s' ) where rownum <=1";
		String consultaerror = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1";
		String consultaerror1 = " select * from (select description,MESSAGE from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String consultaerror2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; 	//FCWMLQA 
		
		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR5'"
				+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " 				// FCWMLQA 
				+ " ORDER BY start_dt DESC";

		String consultaRTV2 = "select * from (select ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME, PARTITION_DATE "
				+ "from POSUSER.POS_INBOUND_DOCS  "
				+ " where DOC_TYPE='RTV'"
			//	+ " and  SUBSTR(PV_DOC_NAME,4,5) = '"+ data.get("plaza") + "' "
			    + "and STATUS='E' ORDER BY received_date DESC) where rownum <=3 ";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String status = "S";
		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		/*
		 * paso 1 Buscar en la BD FCWMQA archivos RTV en estatus I (listos para ser
		 * procesados por la interfase)
		 **********************/
		
		addStep("Buscar en la BD FCWMQA archivos RTV en estatus I.  ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaRTV);

		SQLResult consultaRTVQuery = dbPos.executeQuery(consultaRTV);
		String iD = "";
		System.out.println(iD);
		boolean validateRTV=consultaRTVQuery.isEmpty();
		if (validateRTV == false) {
			iD = consultaRTVQuery.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(consultaRTVQuery);

		} 
		System.out.println(validateRTV);
		assertFalse(validateRTV, "Los datos no se encuentran configurados correctamente");

		/*
		 * paso 2.- Validar que exista información en la cabecera del documento RTV
		 * SELECT * FROM POS_RTV WHERE PID_ID = [POS_INBOUND_DOCS.ID];
		 *****************************************************/
		addStep("Validar que exista información en la cabecera del documento RTV.");
		String consultaRTVcabecera = String.format(consultaRTVCabecera, iD);
		SQLResult executeConsultaRTVCabecera = dbPos.executeQuery(consultaRTVcabecera);
		String pid_id = executeConsultaRTVCabecera.getData(0, "PID_ID");

		boolean validateCabecera=executeConsultaRTVCabecera.isEmpty();
		if (validateCabecera == false) {

			testCase.addQueryEvidenceCurrentStep(executeConsultaRTVCabecera);

		} 
		System.out.println(validateCabecera);
		assertFalse(validateCabecera, "No se ecnuentran datos en la cabecera del documento rtv");

		/*
		 * paso 3. Validar su detalle SELECT * FROM POS_RTV_DETL WHERE PID_ID =
		 * [POS_INBOUND_DOCS.ID];
		 */
		addStep("Validar su detalle SELECT * FROM POS_RTV_DETL ");

		String consultaDetalle = String.format(detallePos_RTV_DETL, pid_id);
		SQLResult executeConsultaDetalle = dbPos.executeQuery(consultaDetalle);
		
		boolean validateConsultaDetalle = executeConsultaDetalle.isEmpty();
		if (validateConsultaDetalle == false) {

			testCase.addQueryEvidenceCurrentStep(executeConsultaDetalle);

		} 
		System.out.println(validateConsultaDetalle);
		assertFalse(validateConsultaDetalle, "Consulta no exitosa");

		/*
		 * paso 4. Ejecutar la interface PR5 los servidores que te dije, que se llaman
		 * intqa
		 */

		addStep("Ejecucion de la interfaz con Integration server ");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);
		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			is = executeQuery(dbLog, tdcIntegrationServerFormat);
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);
		
		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {
		
			String error = String.format(consultaerror, run_id);
			String error1 = String.format(consultaerror1, run_id);
			String error2 = String.format(consultaerror2, run_id);
		
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
		
			if (!emptyError) {
		
				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucutar la interfaz en la tabla WM_LOG_ERROR");
		
				testCase.addQueryEvidenceCurrentStep(errorr);
		
			}
		
			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}
		
			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();
		
			if (!emptyError2) {
		
				testCase.addQueryEvidenceCurrentStep(errorIS2);
		
			}
		
		}
		u.close();
		
		addStep("Comprobar que se registra la ejecucion en WMLOG");



					System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
					String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
					System.out.println(queryStatusLog);
					SQLResult executeQueryStatusLog=dbLog.executeQuery(queryStatusLog);
					testCase.addQueryEvidenceCurrentStep(executeQueryStatusLog);
					String fcwS=executeQueryStatusLog.getData(0, "STATUS");
					boolean validateStatus = status.equals(fcwS);
					System.out.println(validateStatus);
					
					
					
					
					
					
		assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");
		
		addStep("Validar que el registro de ejecución de la plaza y tienda generaron hilos.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult executeQueryStatusThread=dbLog.executeQuery(queryStatusThread);
		String regPlazaTienda=executeQueryStatusThread.getData(0, "STATUS");
		boolean statusThread = status.equals(regPlazaTienda);
		System.out.println(statusThread);
				if(!statusThread){

						String error = String.format(tdcQueryErrorId, run_id);
						SQLResult queryEmptyError=dbLog.executeQuery(error);

						boolean emptyError = queryEmptyError.isEmpty();
						if(!emptyError){  

							testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR."
									+ " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
									+ "from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1");

							testCase.addQueryEvidenceCurrentStep(queryEmptyError);

										}
									}
assertTrue(statusThread,"El registro de ejecución de la plaza y tienda no fue exitoso");

		// .-----------Segunda consulta
		String consultado = String.format(consultathread, run_id);
		System.out.println("Respuesta 2 consulta");
		SQLResult consultaTabla = dbLog.executeQuery(consultado);

		boolean av311 = consultaTabla.isEmpty();
		if (av311 == false) {
			testCase.addQueryEvidenceCurrentStep(consultaTabla);

		}
		System.out.println(av311);
		assertFalse("No se generaron datos en la tabla", av311);


		/* 7.- Validar que el archivo RTV haya cambiado a estatus E en la bd FCWMQA */
		addStep(" Validar que el archivo RTV haya cambiado a estatus E en la bd FCWMQA .");
		System.out.println("Respuesta paso ultimo ");
		//String consulta3 = String.format(consulta33, w);
		
		SQLResult verifStatusQuery = dbPos.executeQuery(consultaRTV2);
		boolean verificarStatus = verifStatusQuery.isEmpty();

		if (verificarStatus == false) {

			testCase.addQueryEvidenceCurrentStep(verifStatusQuery);

		} 
		System.out.println(verificarStatus);
		assertFalse("No se generaron datos en la tabla", verificarStatus);

	}

	@Override
	public String setTestFullName() {

		return "ATC_FT_PR5_001_Validar_Dev_Prov_Plaza";

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Construido. Validar la devolución a proveedores directos para la plaza.";

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