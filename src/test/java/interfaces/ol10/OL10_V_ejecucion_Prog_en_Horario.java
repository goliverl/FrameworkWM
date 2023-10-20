package interfaces.ol10;

import static org.junit.Assert.assertFalse;
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


public class OL10_V_ejecucion_Prog_en_Horario extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OL10_002_V_ejecucion_Prog_en_Horario(HashMap<String, String> data) throws Exception {
		/*
		 * Utiler?as
		 *********************************************************************/
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,
				GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		String Cons_hora = "SELECT comm.service_id, comm.attribute6, comm.attribute7, comm.attribute9, "
				+ "  comm.cr_plaza, comm.horario_de_envio " + " FROM xxfc_services_vendor_comm_data comm "
				+ " WHERE comm.service_type = 'L' " + " AND comm.estatus_envio_programado = 'A' "
				+ " AND UPPER(comm.horario_de_envio) = UPPER('" + data.get("hora") + "') "
				+ " and comm.attribute6 is not null " + " AND comm.protocol = 'MAIL' "
				+ " AND comm.enviar_servicio = 'S' " + " AND EXISTS ( " + "  SELECT 1 FROM xxfc_pago_servicios pagos "
				+ "  WHERE pagos.plaza=comm.cr_plaza " + "  AND pagos.lote IS NULL "
				+ "  AND pagos.fecha_transaccion >= TRUNC(SYSDATE-30) "
				+ "  AND pagos.xxfc_fecha_administrativa >= TRUNC(SYSDATE-30) "
				+ "  AND (pagos.estatus IS NULL OR pagos.estatus = '') " + "  AND ROWNUM = 1 "
				+ "  AND pagos.servicio = comm.service_id )";// Validar que existe información en la tabla
																// xxfc_pago_servicios y xxfc_services_vendor_comm_data
																// de ORAFIN con respecto a los últimos 30 días de la
																// fecha actual pendientes de procesar.
		
		String consultainsumos="  SELECT pagos.trans_id, pagos.plaza, pagos.tienda ,pagos.servicio, pagos.estatus, pagos.fecha_transaccion,   pagos.hora_transaccion, pagos.ref1, pagos.ref2, pagos.valor   " + 
				"  FROM xxfc_pago_servicios pagos" + 
				"  where plaza ='%s'AND fecha_transaccion >= TRUNC(SYSDATE-30)  " + 
				"  and pagos.lote IS NULL" + 
				"  AND pagos.fecha_transaccion >= TRUNC(SYSDATE-30)" + 
				"  AND pagos.xxfc_fecha_administrativa >= TRUNC(SYSDATE-30)  " + 
				"  AND (pagos.estatus IS NULL OR pagos.estatus = '') ";

		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%OL10MultiExec%' "
				+ "ORDER BY START_DT DESC) where rownum <=3";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		String lotes = "SELECT * FROM XXFC.xxfc_lotes " + " WHERE servicio = '%s' " + " AND fecha >= TRUNC(SYSDATE) "; // Verificar
																														// que
																														// en
																														// la
																														// tabla
																														// xxfc_lotes
																														// de
																														// ORAFIN
																														// se
																														// inserto
																														// un
																														// registro
																														// con
																														// el
																														// servicio
																														// y
																														// el
																														// lote
																														// procesados.
		String ConsultaDatosProc = "SELECT a.trans_id, a.plaza, a.tienda , b.oracle_cr_desc, b.oracle_ef_desc, a.servicio, a.estatus, a.fecha_transaccion, "
				+ "  a.hora_transaccion, a.ref1,  a.valor "
				+ " FROM xxfc_pago_servicios a, xxfc_maestro_de_crs_v b " + " WHERE servicio = '%s'"
				+ " AND plaza = '%s' " + " AND estatus = 'E' " + " AND fecha_transaccion >= TRUNC(SYSDATE-30) "
				+ " AND a.xxfc_fecha_administrativa >= TRUNC(SYSDATE-30) " + " AND b.estado = 'A' "
				+ " AND b.oracle_cr_type = 'T' " + " AND A.plaza = b.oracle_cr_superior(+) "
				+ " AND a.tienda = b.oracle_cr(+) ";
		
		
		//utileria
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				PakageManagment pok = new PakageManagment(u, testCase);
				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
				String searchedStatus = "R";
				String status = "S";
				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";


//Paso 1    ************************        
		addStep("Validar que existe información en la tabla xxfc_services_vendor_comm_data de ORAFIN");
		SQLResult hora = dbEbs.executeQuery(Cons_hora);
		String id = hora.getData(0, "service_id");
		String plaza = hora.getData(0, "cr_plaza");
		System.out.println("Respuesta " + id);
		System.out.println("Respuesta " + hora);
		boolean paso1 = hora.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(hora);
		}
		assertFalse("No hay insumos a procesar", paso1);
addStep("Validar que existe información en la tabla xxfc_pago_servicios de ORAFIN");
			
		String ins = String.format(consultainsumos, plaza);
		SQLResult insumos = dbEbs.executeQuery(ins);
		System.out.println("Respuesta " + ins);
		boolean empty22 = insumos.isEmpty();

		if (!empty22) {

			testCase.addQueryEvidenceCurrentStep(insumos);

		}
		assertFalse("prueba insumos", empty22);

//Paso 2    ************************        
		addStep("Ejecutar  el servicio de la interfaz OL10");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		u.get(contra);
		// String dateExecution = null;
		// String dateExecution ="";
		String boton1 = "1";
		boolean boton = boton1.equals(data.get("boton"));
		if (boton) {
			String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
			u.close();
		}
		if (!boton) {
			String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		}

		// String dateExecution = pok.runIntefaceWM(data.get("interfase"),
		// data.get("servicio"),null);

//System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			// is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLOG,
			// tdcIntegrationServerFormat, "STATUS", "RUN_ID");
			is = executeQuery(dbLOG, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLOG.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecuci?n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLOG.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLOG.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//Paso 3    ************************		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		// String fcwS = SQLUtil.getColumn(testCase, dbLOG, tdcIntegrationServerFormat,
		// "STATUS");
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");
		SQLResult log = dbLOG.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);
		// SQLResult errorIS= dbLOG.executeQuery(error1);

		boolean log1 = log.isEmpty();
		// boolean av2 = SQLUtil.isEmptyQuery(testCase, dbLOG,
		// tdcIntegrationServerFormat);
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

		// Paso 3 ************************
		addStep("Verificar que en la tabla xxfc_lotes de ORAFIN se inserto un registro con el servicio y el lote procesados.");

		String lot = String.format(lotes, id);
		SQLResult lotes1 = dbEbs.executeQuery(lot);
		System.out.println("Respuesta " + lot);

		boolean emptylot = lotes1.isEmpty();

		if (!emptylot) {

			testCase.addQueryEvidenceCurrentStep(lotes1);
			assertFalse("", emptylot);

		}
		// Paso 4 ************************
		addStep("Validar la actualización de los datos procesados en la tabla xxfc_pago_servicios de ORAFIN.");
		String proc = String.format(ConsultaDatosProc, id, plaza);
		SQLResult proce = dbEbs.executeQuery(proc);
		System.out.println("Respuesta " + proc);
		boolean empty4 = proce.isEmpty();

		if (!empty4) {

			testCase.addQueryEvidenceCurrentStep(proce);

		}
		assertFalse("prueba final", empty4);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar el proceso de ejecución de la interface OL10 10AM, 12 y 2PM ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OL10_002_V_ejecucion_Prog_en_Horario";
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
