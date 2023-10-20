package interfaces.ct1_cl;

import static org.junit.Assert.assertFalse;
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
import utils.sql.SQLUtil;

public class CT1_CL_ActualizaReintentosExcedidosRunTN extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_CT1_CL_Verificar_procesamiento_actualizar_runTN(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbCNTCL = new SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,
				GlobalVariables.DB_PASSWORD_CNTCHILE);
		
		/**
		 * ALM (es el mismo caso que el de CT1 Mexico , solo cambian algunos status
		 * Verificar el procesamiento de la interfaz, actualizar los reintentos WM_RETRIES++ (runTN)
		 */
		

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 */
		
		
		// Paso 2 y 3

		String ValidaRetriesyStatus = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS , WM_RETRIES "
				+ "  FROM WM_BUZONES_T_TIENDAS " + " WHERE WM_STATUS = 'L' " + "    AND CR_PLAZA = '"
				+ data.get("plaza") + "' \r\n" + "    AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
				+ "  ORDER BY CREATION_DATE";

		// UPDATE
		String UpdateRetriesBuzon = "UPDATE WM_BUZONES_T_TIENDAS " + "SET WM_RETRIES  = 2 WHERE ID = '%s'";

		// Paso 5

		// Usar este para pruebas ejecutando el servicio (runTN)
//	String  tdcIntegrationServerFormat ="select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
//		      "FROM WMLOG.WM_LOG_RUN " +
//		      "WHERE INTERFACE = 'CT1_TN' " +
//		      "AND START_DT >= TRUNC(SYSDATE) "  +
//		     " ORDER BY START_DT DESC) where rownum <=1"; 
//	
//	String ValidaError = "select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
//			"FROM WMLOG.WM_LOG_RUN "+
//			"WHERE INTERFACE = 'CT1_TN' "+
//			"AND START_DT >= TRUNC(SYSDATE)  "+
//			"AND STATUS = 'E' "+
//			" ORDER BY START_DT DESC) where rownum <=1";

		// Usar esta para pruebas sin ejecutar servicio (RunTN)
		String tdcIntegrationServerFormat = "select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN " + "WHERE INTERFACE = 'CT1_TN' "
				+ "And START_DT between '07-JUN-21' and '08-JUN-21' " + // Ajustar a ultimo registro en estatus E
				" ORDER BY START_DT DESC) where rownum <=1";

		String ValidaError = "select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN " + "WHERE INTERFACE = 'CT1_TN' "
				+ "And START_DT between '07-JUN-21' and '08-JUN-21' " + // Ajustar a ultimo registro en estatus E
				"AND STATUS = 'E' " + " ORDER BY START_DT DESC) where rownum <=1";

		// Paso 6

		String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";

		String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";

		// Paso 7
		String LogError1 = "select * from (SELECT ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID = '%s') WHERE rownum <= 1";

		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s') WHERE rownum <= 1"; // dbLog

		// Paso 8

		// Usar este para pruebas ejecutando el servicio (runTN)
//				String validaRetries = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS , WM_RETRIES "+ 
//						 "FROM WM_BUZONES_T_TIENDAS "+
//						 "WHERE WM_STATUS = 'M' "+
//						     "AND CR_PLAZA = '" + data.get("plaza") +"' "+
//						     "AND CR_TIENDA = '" + data.get("tienda") +"' "+
//						     "AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)";

		// Usar esta para pruebas sin ejecutar servicio (RunTN)
		String validaRetries = "SELECT ID,CR_PLAZA, CR_TIENDA, WM_STATUS , WM_RETRIES " + "FROM WM_BUZONES_T_TIENDAS "
				+ "WHERE WM_STATUS = 'M' " + "AND CR_PLAZA = '" + data.get("plaza") + "' " + "AND CR_TIENDA = '"
				+ data.get("tienda") + "' ";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//**********************************************Paso 1	****************************************************************************

		// Verificar que exista el perfil en el TN, orgUnitName = [PLAZA][TIENDA].
		// Se omite este paso ya que es desconocido donde verificar estos datos

//********************************************Paso2 y 3*****************************************************************************

		addStep("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS = 'L' y  WM_RETRIES = 2.");

		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		System.out.println(ValidaRetriesyStatus);

		SQLResult ValidaSatusL_Res = executeQuery(dbCNTCL, ValidaRetriesyStatus);

		boolean validaStatusL = ValidaSatusL_Res.isEmpty();

		if (!validaStatusL) {

			testCase.addQueryEvidenceCurrentStep(ValidaSatusL_Res);
			int Retries = Integer.parseInt(ValidaSatusL_Res.getData(0, "WM_RETRIES"));
			
			

//			En caso de que el WM_RETRIES sea diferente a 2 puede actualizarse el campo para la ejecuci�n del caso.

//			if(Retries !=2) {
//				
//				String Id= ValidaSatusL_Res.getData(0, "ID");
//			    String Updateformat = String.format(UpdateRetriesBuzon, Id);
//					System.out.println(Updateformat);
//				SQLResult UpdateB = executeQuery(dbCNTCL,Updateformat);
//				SQLResult ValidaSatusL_Res2 = executeQuery(dbCNTCL, ValidaRetriesyStatus);
//		testCase.addTextEvidenceCurrentStep("El numero de intentos era diferente a 1, se realiza update \n"+Updateformat
//							                +"\nActualizacion: ");
//		testCase.addQueryEvidenceCurrentStep(ValidaSatusL_Res2);
//		
//					
//				}

		}

		System.out.println(validaStatusL);

		assertFalse(validaStatusL, "No se encontro registro de la plaza y tienda");

//**********************************************Paso 4	****************************************************************************** 

		addStep("Ejecutar el JOB runCT1TN desde Control M para invocar la interface por medio del servicio CT1.Pub:runTN");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interface"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLogCL.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(4);

		}

//*******************************************************Paso 5************************************************************************
		// Validar que la interface haya finalizado con error en la tabla WM_LOG_RUN.

		addStep("Validar que la interface haya finalizado con error en la tabla WM_LOG_RUN.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(ValidaError);

		SQLResult ValidaStatusE = executeQuery(dbLogCL, ValidaError);

		boolean validaStatus = ValidaStatusE.isEmpty();

		if (!validaStatus) {

			testCase.addQueryEvidenceCurrentStep(ValidaStatusE);

		}

		System.out.println(validaStatus);

		assertFalse(validaStatus, "El estatus de la ejecucion no es E");

//**************************************************Paso 6 **************************************************************************

		addStep("Validar que el thread haya finalizado con error en la tabla WM_LOG_THREAD.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String consulta1 = String.format(qry_threads1, run_id);

		System.out.println("CONSULTA THREAD " + consulta1);

		SQLResult consultaThreads = dbLogCL.executeQuery(consulta1);

		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLogCL.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		System.out.println(consulta2);
		assertFalse("No se generaron threads en la tabla", threads1);

//**************************************************Paso 7 ***************************************************************************		

		addStep("Validar que el error se inserto en la tabla WM_LOG_ERROR.");
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String LogError1F = String.format(LogError1, run_id);
		String ConError2 = String.format(consultaError2, run_id);
		SQLResult consultaLogError1 = dbLogCL.executeQuery(LogError1F);

		boolean Error1 = consultaLogError1.isEmpty();

		if (!Error1) {
			SQLResult errorr = dbLogCL.executeQuery(ConError2);

			testCase.addTextEvidenceCurrentStep(
					"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

			testCase.addQueryEvidenceCurrentStep(consultaLogError1);
			testCase.addQueryEvidenceCurrentStep(errorr);

		}

		assertFalse(Error1, "No se inserto error en la tabla WM_LOG_ERROR.");

		// **********************************************Paso 8
		// **********************************************

		addStep("Validar que el WM_STATUS de la tabla WM_BUZONES_T_TIENDAS fue actualizado a WM_STATUS = 'M';");

		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		System.out.println(validaRetries);

		SQLResult validaRetriesR = executeQuery(dbCNTCL, validaRetries);

		boolean valuesRetri = validaRetriesR.isEmpty();

		if (!valuesRetri) {
			testCase.addTextEvidenceCurrentStep("WM_STATUS fue actualizado a 'M'");

			testCase.addQueryEvidenceCurrentStep(validaRetriesR);

		} else if (valuesRetri) {
			testCase.addTextEvidenceCurrentStep("WM_STATUS NO fue actualizado a 'M'");

		}

		System.out.println(valuesRetri);
		System.out.println(valuesRetri);

		assertFalse(valuesRetri, "No se actualizo correctamente el registro a M");

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
		return "Construida. Verificar el procesamiento de la interfaz, actualizar a M los registros con reintentos excedidos.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_CT1_CL_Verificar_procesamiento_actualizar_runTN";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
