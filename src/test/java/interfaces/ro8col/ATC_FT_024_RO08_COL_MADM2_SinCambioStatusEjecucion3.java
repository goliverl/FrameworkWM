package interfaces.ro8col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_024_RO08_COL_MADM2_SinCambioStatusEjecucion3 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_024_RO08_COL_MADM2_SinCambioStatusEjecucion3_test(HashMap<String, String> data) throws Exception {
		
		/**
		 *  Proyecto: Mejoras Administrativo CC3
		   Caso de prueba: ATC-FT-017-Cambio de estatus de mov en tabla pos_inbound_docs
		 * Desc:
		 * Ejecutar el script  que cambiara los  estatus de las transacciones de la tabla INBOUND_DOCS a status 
		   ‘D’, y asegurar que la información no fluya hacia RMS( archivos SAL, REC,TSF,ADJ,RTV,INV,PRC)
		 * @author Marisol Rodriguez
		 * @date   2022/09/26
		 */

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		// Datos del proyecto

		testCase.setProject_Name("Mejoras Administrativo CC3");

		testCase.setPrerequisites(data.get("prerequicitos"));

		testCase.setTest_Description(data.get("id") + data.get("name"));

		// Segundo paso
		String statusInicial = "SELECT ID,DOC_TYPE, STATUS,INSERTED_DATE, RECEIVED_DATE  \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n" + "WHERE DOC_TYPE IN " + data.get("DOC_TYPE") + " \r\n"
				+ "AND SUBSTR(PV_DOC_NAME,4,5) ='" + data.get("plaza") + "' \r\n" + "and status ='"
				+ data.get("statusInicial") + "' \r\n" + "AND RECEIVED_DATE>=SYSDATE -5 \r\n"
				+ "order by RECEIVED_DATE desc";

		// Integration server
		String tdcIntegrationServerFormat1 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta1") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat2 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta2") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";
		String tdcIntegrationServerFormat3 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta3") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat4 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta4") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat5 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta5") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat6 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta6") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat7 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta7") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat8 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta8") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		String tdcIntegrationServerFormat9 = "select run_id, interface, start_dt, end_dt, status, server  \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n" + "WHERE INTERFACE = '" + data.get("interfaceConsulta8") + "' \r\n"
				+ "AND ROWNUM <=1 \r\n" + "AND  start_dt >= TRUNC(SYSDATE) \r\n" + "ORDER BY START_DT DESC ";

		//error
		String error ="Select ERROR_ID, RUN_ID, ERROR_DATE,DESCRIPTION\r\n"
				+ "from  wmlog.WM_LOG_ERROR \r\n"
				+ "where RUN_ID= '%s'";
		//threads
		String threads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n"
				+ "FROM WMLOG.WM_LOG_THREAD   \r\n"
				+ "WHERE PARENT_ID='%s'\r\n"
				+ "ORDER BY THREAD_ID DESC";
		
		// Tercer paso

		String statusFinal = "SELECT ID,DOC_TYPE, STATUS,INSERTED_DATE, RECEIVED_DATE  \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n" + "WHERE DOC_TYPE  IN " + data.get("DOC_TYPE") + " \r\n"
				+ "AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' \r\n" + "and status ='"
				+ data.get("statusFinal") + "'\r\n" // status E
				+ "AND ID IN (%s)\r\n" + "AND RECEIVED_DATE> = SYSDATE -5 \r\n" + "order by RECEIVED_DATE desc";

//Paso 1 *********************************************************************************************************

		addStep("Conectarse a la base de datos de WM");

		testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);

//Paso 2 ***********************************************************************************************************	    
		addStep("Validar que los archivos que llegaron a Webmethod  en estatus D, ejecutando la siguiente consulta en FCWMQA:");

		System.out.println(statusInicial);
		SQLResult statusInicial_result = executeQuery(dbPos, statusInicial);

		testCase.addQueryEvidenceCurrentStep(statusInicial_result);

		boolean validastatusInicial = statusInicial_result.isEmpty();

		System.out.println("Status inicial: " + validastatusInicial);

		assertFalse(validastatusInicial, "No se obtuvieron registros");

		List<String> idsPOS_INBOUND_DOCS = new ArrayList<>();
		for (int i = 0; i < statusInicial_result.getRowCount(); i++) {
			idsPOS_INBOUND_DOCS.add(statusInicial_result.getData(i, "ID"));
			System.out.println ("ID: "+ i);
		}



//Paso 2 ************************************************************************************************************

		addStep("Atentificarse en el integration server de INQA3 desde un navegador, acceder a la siguiente ruta: Packages/Management/<<<INTERFAZ>>> "
				+ "/Browse services in <<<interfaz>>> Pub:run/Test_run y precionar el botón [Test (Without Inputs)]: ");

		addStep("SAL (FEMSA_PR24_CO)");

		SeleniumUtil PR24 = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(PR24, testCase);
		String statusEsperado = "S";
		String statusError= "E";
		String statusQuery = "";
		String run_id = "";
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		PR24.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interface1"), data.get("servicio1"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat1);

		SQLResult is1 = dbLog.executeQuery(tdcIntegrationServerFormat1);
		run_id = is1.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is1.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		boolean valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
																	// encuentra en
																	// estatus R

		while (valuesStatus) {

			statusQuery = is1.getData(0, "STATUS");
			run_id = is1.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			PR24.hardWait(2);

		}

		boolean validaPR24 = statusEsperado.equals(statusQuery);// Valida si se encuentra en s

		//Imprime resultado de ejecucion
		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		System.out.println("Status ejecucion PR24: " + validaPR24);
		testCase.addQueryEvidenceCurrentStep(is1);
		
		//Mostrar detalles de los threads
		String threadsFormat = String.format(threads, run_id);
		System.out.println(threadsFormat);
        SQLResult threads_result= dbLog.executeQuery(threadsFormat);
        testCase.addQueryEvidenceCurrentStep(threads_result);
        Boolean validaThreads = threads_result.isEmpty();
        System.out.println("La consulta de threads esta vacia : "+ validaThreads);
		
		//Revisar si hay errores, mostrar el detalle
        Boolean validaError = statusQuery.equals(statusError);
       
		if(validaError) {
			
			String errorFormat = String.format(error, run_id);
			System.out.println(errorFormat);
	        SQLResult error_result= dbLog.executeQuery(errorFormat);
	        testCase.addQueryEvidenceCurrentStep(error_result);
			
		}
		 System.out.println("Se encontro algun error : "+ validaError);
		

	//	assertTrue(validaPR24, "Se presento un error en la ejecucion");

//REC (FEMSA_PR28)*****************************************************************************************

	addStep("REC (FEMSA_PR28_CO)");

		SeleniumUtil pr28 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr28, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr28.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface2"), data.get("servicio2"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat2);

		SQLResult is2 = dbLog.executeQuery(tdcIntegrationServerFormat2);
		run_id = is2.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is2.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is2.getData(0, "STATUS");
			run_id = is2.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr28.hardWait(2);

		}

		boolean validaPR28 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is2);
		System.out.println("Status ejecucion PR23: " + validaPR28);
		
		//Mostrar detalles de los threads
		 String threadsFormat2 = String.format(threads, run_id);
		 System.out.println(threadsFormat2);
		 SQLResult threads_result2= dbLog.executeQuery(threadsFormat2);
		 testCase.addQueryEvidenceCurrentStep(threads_result2);
		 Boolean validaThreads2 = threads_result2.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads2);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError2 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError2);
		 if(validaError2) {
					
				String errorFormat2 = String.format(error, run_id);
				System.out.println(errorFormat2);
			    SQLResult error_result2 = dbLog.executeQuery(errorFormat2);
			    testCase.addQueryEvidenceCurrentStep(error_result2);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError2);
		 
		// assertTrue(validaPR28, "Se presento un error en la ejecucion");
		

//REC (FEMSA_PR23)*****************************************************************************************

		addStep("REC (FEMSA_PR23_CO)");

		SeleniumUtil pr23 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr23, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr23.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface3"), data.get("servicio3"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat3);

		SQLResult is3 = dbLog.executeQuery(tdcIntegrationServerFormat3);
		run_id = is3.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is3.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is3.getData(0, "STATUS");
			run_id = is3.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr23.hardWait(2);

		}

		boolean validaPR23 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is3);
		System.out.println("Status ejecucion PR23: " + validaPR23);
		
		//Mostrar detalles de los threads
		 String threadsFormat3 = String.format(threads, run_id);
		 System.out.println(threadsFormat3);
		 SQLResult threads_result3= dbLog.executeQuery(threadsFormat3);
		 testCase.addQueryEvidenceCurrentStep(threads_result3);
		 Boolean validaThreads3 = threads_result3.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads3);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError3 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError3);
		 if(validaError3) {
					
				String errorFormat3 = String.format(error, run_id);
				System.out.println(errorFormat3);
			    SQLResult error_result3 = dbLog.executeQuery(errorFormat3);
			    testCase.addQueryEvidenceCurrentStep(error_result3);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError3);

		// assertTrue(validaPR23, "Se presento un error en la ejecucion");

//REC (FEMSA_PR23 reple)*****************************************************************************************

		addStep("REC (FEMSA_PR23 reple)");

		SeleniumUtil pr23reple = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr23reple, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr23reple.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface4"), data.get("servicio4"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat4);

		SQLResult is4 = dbLog.executeQuery(tdcIntegrationServerFormat4);
		run_id = is4.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is4.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is4.getData(0, "STATUS");
			run_id = is4.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr23reple.hardWait(2);

		}

		boolean validaPR23reple = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is4);
		System.out.println("Status ejecucion PR23_reple: " + validaPR23reple);
		
		//Mostrar detalles de los threads
		 String threadsFormat4 = String.format(threads, run_id);
		 System.out.println(threadsFormat4);
		 SQLResult threads_result4= dbLog.executeQuery(threadsFormat4);
		 testCase.addQueryEvidenceCurrentStep(threads_result4);
		 Boolean validaThreads4 = threads_result4.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads4);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError4 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError4);
		 if(validaError4) {
					
				String errorFormat4 = String.format(error, run_id);
				System.out.println(errorFormat4);
			    SQLResult error_result4 = dbLog.executeQuery(errorFormat4);
			    testCase.addQueryEvidenceCurrentStep(error_result4);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError4);

		// assertTrue(validaPR23reple, "Se presento un error en la ejecucion");

//TSF (FEMSA_PR1_CO)*****************************************************************************************

		addStep("TSF (FEMSA_PR1_CO)");

		SeleniumUtil pr1 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr1, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr1.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface5"), data.get("servicio5"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat5);

		SQLResult is5 = dbLog.executeQuery(tdcIntegrationServerFormat5);
		run_id = is5.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is5.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is5.getData(0, "STATUS");
			run_id = is5.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr1.hardWait(2);

		}

		boolean validaPR1 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is5);
		System.out.println("Status ejecucion PR1: " + validaPR1);
		
		//Mostrar detalles de los threads
		 String threadsFormat5 = String.format(threads, run_id);
		 System.out.println(threadsFormat5);
		 SQLResult threads_result5= dbLog.executeQuery(threadsFormat5);
		 testCase.addQueryEvidenceCurrentStep(threads_result5);
		 Boolean validaThreads5 = threads_result5.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads5);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError5 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError5);
		 if(validaError5) {
					
				String errorFormat5 = String.format(error, run_id);
				System.out.println(errorFormat5);
			    SQLResult error_result5 = dbLog.executeQuery(errorFormat5);
			    testCase.addQueryEvidenceCurrentStep(error_result5);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError5);

		// assertTrue(validaPR1, "Se presento un error en la ejecucion");

//ADJ (FEMSA_PR4_CO)*****************************************************************************************

		addStep("ADJ (FEMSA_PR4_CO)");

		SeleniumUtil pr4 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr4, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr4.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface6"), data.get("servicio6"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat6);

		SQLResult is6 = dbLog.executeQuery(tdcIntegrationServerFormat6);
		run_id = is6.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is6.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is6.getData(0, "STATUS");
			run_id = is6.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr4.hardWait(2);

		}

		boolean validaPR4 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is6);
		System.out.println("Status ejecucion PR4: " + validaPR4);
		
		//Mostrar detalles de los threads
		 String threadsFormat6 = String.format(threads, run_id);
		 System.out.println(threadsFormat6);
		 SQLResult threads_result6= dbLog.executeQuery(threadsFormat6);
		 testCase.addQueryEvidenceCurrentStep(threads_result6);
		 Boolean validaThreads6 = threads_result6.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads6);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError6 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError6);
		 if(validaError6) {
					
				String errorFormat6 = String.format(error, run_id);
				System.out.println(errorFormat6);
			    SQLResult error_result6 = dbLog.executeQuery(errorFormat6);
			    testCase.addQueryEvidenceCurrentStep(error_result6);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError6);


		// assertTrue(validaPR4, "Se presento un error en la ejecucion");

//RTV (FEMSA_PR5_CO)*****************************************************************************************

		addStep("RTV (FEMSA_PR5_CO)");

		SeleniumUtil pr5 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr5, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr5.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface7"), data.get("servicio7"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat7);

		SQLResult is7 = dbLog.executeQuery(tdcIntegrationServerFormat7);
		run_id = is7.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is7.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is7.getData(0, "STATUS");
			run_id = is7.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr5.hardWait(2);

		}

		boolean validaPR5 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is7);
		System.out.println("Status ejecucion PR5: " + validaPR5);
		
		//Mostrar detalles de los threads
		 String threadsFormat7 = String.format(threads, run_id);
		 System.out.println(threadsFormat7);
		 SQLResult threads_result7= dbLog.executeQuery(threadsFormat7);
		 testCase.addQueryEvidenceCurrentStep(threads_result7);
		 Boolean validaThreads7 = threads_result7.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads7);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError7 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError7);
		 if(validaError7) {
					
				String errorFormat7 = String.format(error, run_id);
				System.out.println(errorFormat7);
			    SQLResult error_result7 = dbLog.executeQuery(errorFormat7);
			    testCase.addQueryEvidenceCurrentStep(error_result7);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError7);

		// assertTrue(validaPR5, "Se presento un error en la ejecucion");

//INV (FEMSA_PR11_CO)*****************************************************************************************

		addStep("INV (FEMSA_PR11_CO)");

		SeleniumUtil pr11 = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(pr11, testCase);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		pr11.get(contra);

		dateExecution = pok.runIntefaceWmOneButton(data.get("interface8"), data.get("servicio8"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		System.out.println(tdcIntegrationServerFormat8);

		SQLResult is8 = dbLog.executeQuery(tdcIntegrationServerFormat8);
		run_id = is8.getData(0, "RUN_ID");

		System.out.println("RUN_ID = " + run_id);

		statusQuery = is8.getData(0, "STATUS");
		System.out.println("STATUS = " + statusQuery);

		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
															// encuentra en
															// estatus R

		while (valuesStatus) {

			statusQuery = is8.getData(0, "STATUS");
			run_id = is8.getData(0, "RUN_ID");
			valuesStatus = statusQuery.equals(searchedStatus);

			pr11.hardWait(2);

		}

		boolean validaPR11 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
		testCase.addQueryEvidenceCurrentStep(is8);
		System.out.println("Status ejecucion PR11: " + validaPR11);
		
		//Mostrar detalles de los threads
		 String threadsFormat8 = String.format(threads, run_id);
		 System.out.println(threadsFormat8);
		 SQLResult threads_result8= dbLog.executeQuery(threadsFormat8);
		 testCase.addQueryEvidenceCurrentStep(threads_result7);
		 Boolean validaThreads8 = threads_result8.isEmpty();
		 System.out.println("La consulta de threads esta vacia: "+validaThreads8);
				
		//Revisar si hay errores, mostrar el detalle
		 Boolean validaError8 = statusQuery.equals(statusError);
		 System.out.println("Se encontro algun error: "+ validaError8);
		 if(validaError8) {
					
				String errorFormat8 = String.format(error, run_id);
				System.out.println(errorFormat8);
			    SQLResult error_result8 = dbLog.executeQuery(errorFormat8);
			    testCase.addQueryEvidenceCurrentStep(error_result8);
					
		}

		 System.out.println("Se encontro algun error: "+ validaError8);

		// assertTrue(validaPR11, "Se presento un error en la ejecucion");

//PRC (FEMSA_PR37_CO)*****************************************************************************************

//		addStep("PRC (FEMSA_PR37_CO)");
//
//		SeleniumUtil pr37 = new SeleniumUtil(new ChromeTest(), true);
//		pok = new PakageManagment(pr37, testCase);
//
//		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
//		pr37.get(contra);
//
//		dateExecution = pok.runIntefaceWmOneButton(data.get("interface9"), data.get("servicio9"));
//		System.out.println("Respuesta dateExecution" + dateExecution);
//
//		System.out.println(tdcIntegrationServerFormat9);
//
//		SQLResult is9 = dbLog.executeQuery(tdcIntegrationServerFormat9);
//		run_id = is9.getData(0, "RUN_ID");
//
//		System.out.println("RUN_ID = " + run_id);
//
//		statusQuery = is9.getData(0, "STATUS");
//		System.out.println("STATUS = " + statusQuery);
//
//		valuesStatus = statusQuery.equals(searchedStatus);// Valida si se
//															// encuentra en
//															// estatus R
//
//		while (valuesStatus) {
//
//			statusQuery = is9.getData(0, "STATUS");
//			run_id = is9.getData(0, "RUN_ID");
//			valuesStatus = statusQuery.equals(searchedStatus);
//
//			pr37.hardWait(2);
//
//		}
//
//		boolean validaPR37 = statusQuery.equals(statusEsperado);// Valida si se encuentra en S
//
//		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + statusQuery);
//		testCase.addQueryEvidenceCurrentStep(is9);
//		System.out.println("Status ejecucion PR37: " + validaPR37);
		 
		//Mostrar detalles de los threads
//		 String threadsFormat9 = String.format(threads, run_id);
//		 System.out.println(threadsFormat9);
//		 SQLResult threads_result9= dbLog.executeQuery(threadsFormat9);
//		 testCase.addQueryEvidenceCurrentStep(threads_result7);
//		 Boolean validaThreads9 = threads_result9.isEmpty();
//		 System.out.println("La consulta de threads esta vacia: "+validaThreads9);
//				
//		//Revisar si hay errores, mostrar el detalle
//		 Boolean validaError9 = statusQuery.equals(statusError);
//		 System.out.println("Se encontro algun error: "+ validaError9);
//		 if(validaError9) {
					
//				String errorFormat9 = String.format(error, run_id);
//				System.out.println(errorFormat9);
//			    SQLResult error_result9 = dbLog.executeQuery(errorFormat9);
//			    testCase.addQueryEvidenceCurrentStep(error_result9);
					
//		}

//		 System.out.println("Se encontro algun error: "+ validaError9);
//
//		// assertTrue(validaPR37, "Se presento un error en la ejecucion");

//Paso 3 ************************************************************************************************************

		addStep("Validar que los archivos no cambiaron de status y sigan en  estatus D, ejecutando la siguiente consulta en POSUSER:");

		String listaIDs = "";

		for (int i = 0; i < idsPOS_INBOUND_DOCS.size(); i++) {

			listaIDs += idsPOS_INBOUND_DOCS.get(i);

			if (i < idsPOS_INBOUND_DOCS.size() - 1) {

				listaIDs +=  ",";
			}

		}

		String statusFinalFormat = String.format(statusFinal, listaIDs);
		System.out.println("statusFinalFormat : \r\n " + statusFinalFormat);

		SQLResult statusFinal_r = executeQuery(dbPos, statusFinalFormat);
		testCase.addQueryEvidenceCurrentStep(statusFinal_r);

		boolean validastatusFinal = statusFinal_r.getRowCount() == idsPOS_INBOUND_DOCS.size();

		System.out.println("Status final: " + validastatusFinal);

		assertTrue(validastatusFinal, "No todos los registros fueron actualizados.");

		

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_024_RO08_COL_MADM2_SinCambioStatusEjecucion3_test";
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
