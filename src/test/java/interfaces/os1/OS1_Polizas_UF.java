package interfaces.os1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class OS1_Polizas_UF extends BaseExecution {	

	@Test(dataProvider = "data-provider")
	public void ATC_FT_OS1_003_Polizas_UF_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);

		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";
		
		/*
		 * Caso de prueba :
		 * Validar insercion de polizas en SAP_RPY_DETL para BURKS F110
		 */


/** VARIABLES *********************************************************************/

		// Paso 1
		String selectSN = "SELECT ID, TARGET_ID, DOCNUM, GSBER, RUNID, SEQNO, CREATE_DATE, ( SELECT DOCLIN FROM SAPUSER.SAP_PYR_DETL T3 WHERE T3.SID_ID = T1.ID AND ROWNUM < 2 ) AS DOCLIN " + 
				" FROM SAPUSER.SAP_INBOUND_DOCS T1, SAPUSER.SAP_PYR_HEADER T2 " + 
				" WHERE T1.ID = T2.SID_ID AND T2.TARGET_SYS = 'O' " + 
				" AND T1.STATUS IN ('S', 'N') AND T2.BUKRS = '" + data.get("bukrs") + "'";
		
		String consultaGLS = "SELECT reference6, status, date_created, created_by, ledger_id, accounting_date,currency_code, code_combination_id_interim FROM GL.GL_INTERFACE WHERE REFERENCE6 = '%s' AND UPPER( STATUS ) <> 'NEW'";

		String consultaGLN = "SELECT *  FROM GL.GL_JE_HEADERS" + 
				" WHERE EXTERNAL_REFERENCE = '%s'" + 
				" AND STATUS = 'U' AND ACTUAL_FLAG='A'";
		
		// Paso 4
		String tdcIntegrationServerFormat = "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE = 'OS1' AND STATUS = 'S' AND TRUNC(SYSDATE) = TRUNC(START_DT) ORDER BY RUN_ID DESC";

		// consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		// Consulta de Threads 
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads

		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC";// Consulta para los Threads

		// Paso 5
		String validarRPY = "SELECT * FROM SAPUSER.SAP_RPY_DETL" + 
				" WHERE DOCNUM = '%s'" + 
				" AND JOURNALID = '%s'";

		// Paso 6
		String validarStatus = "SELECT * FROM SAPUSER.SAP_INBOUND_DOCS" + 
				" WHERE ID IN '%s' AND STATUS IN ('U', 'F')";
		

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Tener informaci�n en la tabla SAP_INBOUND_DOCS de SAPUSER con estatus S.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(selectSN);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(selectSN);		
		String targetIDS = "", targetIDN = "";

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar en la tabla SAP_INBOUND_DOCS", paso1_qry1_valida); // Si esta vacio, imprime mensaje
		
		

		/* PASO 2 *********************************************************************/	
		
		addStep("Tener informaci�n de las p�lizas en EBS en la tabla GL_INTERFACE con estatus diferente de NEW.");

		String consultafor = "", thread_id, thread_status;
		SQLResult resultfor;
		System.out.println(GlobalVariables.DB_HOST_EBS);		
		String docnum = "";
		String id = "";

		boolean registro = false;

		int i = 0;
		do  {
			String t_id = paso1_qry1_Result.getData(i, "TARGET_ID");
			consultafor = String.format(consultaGLS, t_id); //Consulta si hay resultados en GL en status diferente a NEW del target id
			if (i==0) System.out.println(consultafor);
			resultfor = executeQuery(dbEbs, consultafor);			
			boolean foav = resultfor.isEmpty(); 	//Si no esta vacio, se pasan los datos del thread a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO EL reference 6 (target id): " + t_id + " en la tabla GL? "+ foav);

			if (!foav) {
				targetIDS = paso1_qry1_Result.getData(i, "TARGET_ID");
				docnum = paso1_qry1_Result.getData(i, "DOCNUM");
				id = paso1_qry1_Result.getData(i, "ID");
				registro = true;
			}			
			i++;
		} while ((i < paso1_qry1_Result.getRowCount() && registro == false));
		
		System.out.println("Hay dato en GL? " + registro + " es el Target id: " + targetIDS);

		testCase.addQueryEvidenceCurrentStep(resultfor);

		assertTrue(registro, "No se encontraron datos validos en EBS en la tabla GL_INTERFACE");
			
		
		
		/* PASO 3 *********************************************************************/	
		
		addStep("Tener informaci�n de las p�lizas en EBS en la tabla GL_JE_HEADERS con estatus U.");

		SQLResult resultfor2;
		System.out.println(GlobalVariables.DB_HOST_EBS);		
		String docnum2 = "";
		String id2 = "";

		boolean registro2 = false;

		i = 0;
		do  {
			String t_id = paso1_qry1_Result.getData(i, "TARGET_ID");
			String consultafor2 = String.format(consultaGLN, t_id); //Consulta si hay resultados en GL en status diferente a NEW del target id
			if (i==0) System.out.println(consultafor2);
			resultfor2 = executeQuery(dbEbs, consultafor2);			
			boolean foav = resultfor2.isEmpty(); 	//Si no esta vacio, se pasan los datos del thread a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO EL reference 6 (target id): " + t_id + " en la tabla GL? "+ foav);

			if (!foav) {
				targetIDN = paso1_qry1_Result.getData(i, "TARGET_ID");
				docnum2 = paso1_qry1_Result.getData(i, "DOCNUM");
				id2 = paso1_qry1_Result.getData(i, "ID");
				registro2 = true;
			}			
			i++;
		} while ((i < paso1_qry1_Result.getRowCount() && registro == false));
		
		System.out.println("Hay dato en GL? " + registro2 + " es el Target id: " + targetIDN);

		testCase.addQueryEvidenceCurrentStep(resultfor2);

		assertTrue(registro2, "No se encontraron datos validos en EBS en la tabla GL_JE_HEADERS");
		
		
		
		/* PASO 4 *********************************************************************/	

		addStep("Ejecutar el servicio: OS1.Pub:run.");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID: " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		if (successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
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

		
		
		/* PASO 5 *********************************************************************/	

		addStep("Validar que la interface OS1 se ejecuta sin errores con estatus S en WMLOG.");

		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
		assertTrue(validateStatus, "La ejecuci�n de la interfaz no fue exitosa ");

		boolean av2 = is.isEmpty();
		
		if (av2 == false) {
			testCase.addQueryEvidenceCurrentStep(is);
		} else {
			testCase.addQueryEvidenceCurrentStep(is);
		}

		System.out.println("El registro en WM_LOG_RUN esta vacio " + av2);
		
		
		
		/* PASO 6 *********************************************************************/	

		addStep("Validar que la informaci�n de la p�liza se inserta correctamente en SAP_RPY_DETL en SAPUSER.");

		String paso5_format = String.format(validarRPY, docnum, targetIDS);
		System.out.println(paso5_format);
		SQLResult paso5_Result = dbPos.executeQuery(paso5_format);

		boolean paso5_valida = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida);
		//assertFalse("No se encontraron registros ", paso5_valida); // Si esta vacio, imprime mensaje
		
		
		paso5_format = String.format(validarRPY, docnum2, targetIDN);
		System.out.println(paso5_format);
		paso5_Result = dbPos.executeQuery(paso5_format);

		boolean paso5_valida2 = paso5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_valida2) {
			testCase.addQueryEvidenceCurrentStep(paso5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_valida2);
		assertFalse("No se encontraron registros en SAP_RPY_DETL en SAPUSER.", paso5_valida || paso5_valida2); // Si esta vacio, imprime mensaje
		

		/* PASO 7 *********************************************************************/	

		addStep("Validar que se actualiza correctamente la informaci�n de la tabla SAP_INBOUND_DOCS en SAPUSER con estatus U y F.");

		String paso6_format = String.format(validarStatus, id);
		System.out.println(paso6_format);
		SQLResult paso6_Result = dbPos.executeQuery(paso6_format);

		boolean paso6_valida = paso6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida);
		//assertFalse("No se encontraron registros ", paso6_valida); // Si esta vacio, imprime mensaje
		
		paso6_format = String.format(validarStatus, id2);
		System.out.println(paso6_format);
		paso6_Result = dbPos.executeQuery(paso6_format);

		boolean paso6_valida2 = paso6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_valida2) {
			testCase.addQueryEvidenceCurrentStep(paso6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_valida2);
		assertFalse("No se encontraron registros de SAP_INBOUND_DOCS en SAPUSER con estatus U y F.", paso6_valida || paso6_valida2); // Si esta vacio, imprime mensaje

	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "OS1_Polizas_UF";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Interface entre SAP PY y LEGACY GL. Env�a el pago de N�minas generado en LEGACY GL a SAP PY.";
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
