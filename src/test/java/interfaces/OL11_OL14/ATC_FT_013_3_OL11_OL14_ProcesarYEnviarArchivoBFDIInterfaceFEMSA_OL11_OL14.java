package interfaces.OL11_OL14;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONArray;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import java.util.Date;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_013_3_OL11_OL14_ProcesarYEnviarArchivoBFDIInterfaceFEMSA_OL11_OL14 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_013_3_OL11_OL14_ProcesarYEnviarArchivoBFDIInterfaceFEMSA_OL11_OL14test(HashMap<String, String> data) throws Exception {

/*
* Utilerías
*********************************************************************/

		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, 
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 4
		String consultaInterfaz = "SELECT * FROM("
								+ "    SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS "
								+ "    FROM WMLOG.WM_LOG_RUN "
								+ "    WHERE INTERFACE  LIKE '%OL11_14%' "
								+ "    ORDER BY START_DT DESC"
								+ ")WHERE ROWNUM <= 10";
//Paso 5
		String consultaErrores = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE "
								+ "FROM  WMLOG.WM_LOG_ERROR WHERE RUN_ID = '%s' "
								+ "AND ROWNUM <= 10";
//Paso 7
		String consultaArchivo = "SELECT ID,DOC_NAME,STATUS,RECEIVED_DATE,RUN_ID,INTERFASE,PARTITION_DATE "
								+ "FROM WMUSER.BANK_ENVELOPE "
								+ "WHERE RECEIVED_DATE = SYSDATE AND ROWNUM <=10";
//Paso 8
		String consultaDetalle = "SELECT * FROM("
								+ "    SELECT ID,BE_ID,BANK_TYPE,CR_PLAZA,ACCOUNT,STATUS,PARTITION_DATE "
								+ "    FROM WMUSER.BANK_INBOUND_DOCS "
								+ "    WHERE SENT_DATE>=SYSDATE ORDER BY SENT_DATE DESC)"
								+ "WHERE ROWNUM <=10";
//Paso 9
		String consultaLog = "SELECT * FROM("
							+ "    SELECT LOG_ID,THREAD_ID,PARENT_ID,APPLICATION,PAIS,CREATION_DATE,CR_PLAZA "
							+ "    FROM WMUSER.WM_FW_BO_LOG WHERE APPLICATION = 'OL11_14' AND CREATION_DATE = SYSDATE ORDER BY CREATION_DATE DESC "
							+ ")WHERE ROWNUM <= 10";
//Paso 11
		String consultaArchivo2 = "SELECT STATEMENT_NUMBER,BANK_ACCOUNT_NUM,STATEMENT_DATE,BANK_NAME,BANK_BRANCH_NAME "
				+ "FROM WMUSER.CE_STATEMENT_HEADERS_INT "
				+ "WHERE STATEMENT_NUMBER like  '%SERI10MON20210715083403MXN%' AND ROWNUM <=10;";
//Paso 12 
		String consultaDoc = "SELECT * FROM ( SELECT BANK_ACCOUNT_NUM,STATEMENT_NUMBER,LINE_NUMBER,TRX_DATE,TRX_CODE,EFFECTIVE_DATE \r\n"
				+ "FROM XXFC.XXFC_STATEMENT_LINES_FILTER \r\n"
				+ "WHERE BANK_ACCOUNT_NUM IN ('9100135436710') ORDER BY STATEMENT_NUMBER DESC ) \r\n"
				+ "WHERE STATEMENT_NUMBER IN ('SERI10MON20210715083403MXN') AND ROWNUM <=10";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		/**
		 * Paso 1
		 * - Depositar el archivo en la carpeta configurada en el trasmite, ejecutar el transmite.ALL.bat, realizarlo desde CITRIX.
		 * 
		 * Paso 2
		 * Ejecutar el transmite_ALL y validar el archivo salidaftp, donde registra el LOG de la corrida 
		 */
				
//***************************************** Paso 3 y 4***************************************************************** 
			
		addStep("Validar correcta ejecución de la interface en la tabla WM_LOG_RUN de BD FCWMLQA.");
		System.out.println("Paso 3 y 4: "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		System.out.println(consultaInterfaz);
		
		SQLResult exe_consultaInterfaz = executeQuery(dbFCWMLQA_WMLOG, consultaInterfaz);
		String statusInterfaz = "";
		String runID = "";
		
		boolean validaConsultaInterfaz = exe_consultaInterfaz.isEmpty();
		
		if (!validaConsultaInterfaz) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaInterfaz);
			statusInterfaz = exe_consultaInterfaz.getData(0, "STATUS");
			runID = exe_consultaInterfaz.getData(0, "RUN_ID");
			System.out.println("Status: "+statusInterfaz);
			System.out.println("Status: "+runID);
		} 
		
		System.out.println(validaConsultaInterfaz);
		assertFalse(validaConsultaInterfaz, "No existe información pendiente de procesar en la tabla."); //AQUI DEBE SER asserFalse
		
		
//***************************************** Paso 5 ***************************************************************** 
		
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz OL11_14 en la tabla WM_LOG_ERROR de BD FCWML6QA.");
		System.out.println("Paso 5: "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaErroresFormat = String.format(consultaErrores, runID);
		System.out.println(consultaErroresFormat);
		
		SQLResult exe_consultaErrores = executeQuery(dbFCWMLQA_WMLOG,consultaErroresFormat);
		String idRegistro = exe_consultaErrores.getData(0, "ID");
		System.out.println("ID: "+idRegistro);
		
		boolean validaConsultaErrores = exe_consultaErrores.isEmpty();
		
		if(!validaConsultaErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaErrores);
		}
		
		System.out.println(validaConsultaErrores);
		assertTrue(validaConsultaErrores, "Existen errores en la tabla.");
		
//***************************************** Paso 6 y 7 *****************************************************************
		addStep("Verificar que se ha insertado el registro del archivo en la tabla bank_envelope de WMUSER.");
		System.out.println("Paso 6 y 7: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaArchivo);
		
		SQLResult exe_consultaArchivo = executeQuery(dbFCWM6QA_NUEVA,consultaArchivo);
		
		boolean validaConsultaArchivo = exe_consultaArchivo.isEmpty();
		
		if(!validaConsultaArchivo) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaArchivo);
		}
		
		System.out.println(validaConsultaArchivo);
		assertFalse(validaConsultaArchivo, "No existe información pendiente de procesar en la tabla.");
		
//***************************************** Paso 8 *****************************************************************
		
		addStep("Verificar que se haya insertó el detalle del documento en la tabla bank_inbound_docs de WMUSER.");	
		System.out.println("Paso 8: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaDetalle);
		
		SQLResult exe_consultaDetalle = executeQuery(dbFCRMSQA,consultaDetalle);

		boolean validaConsultaDetalle = exe_consultaDetalle.isEmpty();
		
		if(!validaConsultaDetalle) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaDetalle);
		}
		
		System.out.println(validaConsultaDetalle);
		assertFalse(validaConsultaDetalle, "No hay registros en la tabla");
		
		
//***************************************** Paso 9 *****************************************************************
		
		addStep("Validar en la tabla WM_FW_BO_LOG que no haya registrado el LOG de la ejecución para la OL11_14 ya que para esta prueba el Ruteo esta apagado.");
		System.out.println("Paso 9: "+GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(consultaLog);
		
		SQLResult exe_consultaLog = executeQuery(dbAVEBQA,consultaLog);
		
		boolean validaLog = exe_consultaLog.isEmpty();
		
		if(!validaLog) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaLog);
		}
		
		System.out.println(validaLog);
		assertTrue(validaLog, "Si hay registros de la ejecucion");
		
//***************************************** Paso 11 *****************************************************************
		
		addStep("Validar en la tabla  CE_STATEMENT_HEADERS_INT que se tenga registro del header del documento mediante el nombre del archivo.");
		System.out.println("Paso 11: "+GlobalVariables.DB_HOST_AVEBQA); //FCFINQA 12.2.4
		System.out.println(consultaArchivo2);
		
		SQLResult exe_consultaArchivo2 = executeQuery(dbAVEBQA,consultaArchivo2); //FCFINQA 12.2.4

	
		boolean validaConsultaArchivo2 = exe_consultaArchivo2.isEmpty();
		
		if(!validaConsultaArchivo2) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaArchivo2);
		}
		
		System.out.println(validaConsultaArchivo2);
		assertFalse(validaConsultaArchivo2, "No hay archivos");
		
//***************************************** Paso 12 *****************************************************************
		
		addStep("Validar que las líneas del documento hayan sido insertadas en la tabla XXFC_STATEMENT_LINES_FILTER de ORAFIN.");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_AVEBQA); //FCFINQA 12.2.4
		System.out.println(consultaDoc);
				
		SQLResult exe_consultaThread = executeQuery(dbAVEBQA, consultaDoc); //FCFINQA 12.2.4
		String status3 = exe_consultaThread.getData(0, "STATUS");
		System.out.println("El status es: "+status3);
		
		boolean validaConsultaThread = exe_consultaThread.isEmpty();
				
		if (!validaConsultaThread) {
					
			testCase.addQueryEvidenceCurrentStep(exe_consultaThread);
			} 
				
		System.out.println(validaConsultaThread);
		assertEquals(status3, "S");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "El objetivo de esta interfaz es recibir los banks statements (Flat Files) del usuario mediante ftp, "
				+ "aplicar algunas transformaciones por tipo de banco (Serfin, Bancomer y HSBC, Femsa) "
				+ "e insertar los datos en las tablas de oracle cash management interface usando el servidor de "
				+ "integración de Webmethods.. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_013_3_OL11_OL14_ProcesarYEnviarArchivoBFDIInterfaceFEMSA_OL11_OL14test";
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
