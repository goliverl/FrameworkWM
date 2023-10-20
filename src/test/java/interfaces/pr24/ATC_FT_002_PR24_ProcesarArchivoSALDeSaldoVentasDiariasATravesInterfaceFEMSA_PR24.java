package interfaces.pr24;

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

public class ATC_FT_002_PR24_ProcesarArchivoSALDeSaldoVentasDiariasATravesInterfaceFEMSA_PR24 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR24_ProcesarArchivoSALDeSaldoVentasDiariasATravesInterfaceFEMSA_PR24_test(HashMap<String, String> data) throws Exception {
		/**
		 * Proyecto: BackOffice (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-022-C1 PR24 Procesar archivo SAL de saldo de ventas diarias 
		 * a traves de la interface FEMSA_PR24.
		 * @author edwin.ramirez
		 * @date 2022/Nov/5
		 */
		
		
/*
* Utilerías
*********************************************************************/

		SQLUtil dbFCWM6QA_NUEVA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbFCWMLTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA_MTY,GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, 
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbFCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 2
		String consultaRegistro = "SELECT * FROM(\r\n"
				+ "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PV_ENVELOPE_ID,RECEIVED_DATE,NO_DOCUMENTS \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				//+ "WHERE PV_CR_PLAZA='"+data.get("plaza")+"' "
				//+ "AND PV_CR_TIENDA ='"+data.get("tienda")+"' "
				+ "WHERE RECEIVED_DATE >= trunc(sysdate)"
			//	+ "AND RECEIVED_DATE >= TO_DATE('"+data.get("RECEIVED_DATE")+"','dd/mm/yyyy hh24:mi:ss')"
			//	+ "AND ID='"+data.get("id")+"' \r\n"
				+ "ORDER BY RECEIVED_DATE DESC) \r\n"
				+ "WHERE ROWNUM <=1";
//Paso 3 
		
		String consultaStatusI = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,TARGET_ID FROM POSUSER.POS_INBOUND_DOCS "
				+ "WHERE STATUS='I' AND DOC_TYPE='SAL' AND PE_ID ='%s' and RECEIVED_DATE >= trunc(sysdate)";
//Paso 4
		String registrosSAL = "SELECT PID_ID,NO_RECORDS,PV_FCH_MVT,NET_SAL,PARTITION_DATE "
				+ "FROM  POSUSER.POS_SAL WHERE PID_ID = 880541615";
//Paso 5
		String registrosSAL2 = "SELECT PID_ID,PV_CVE_MVT,QTY,UNIT_RETAIL,ITEM,UOM "
				+ "FROM  POSUSER.POS_SAL_DETL where PID_ID = '%s' AND ROWNUM <=10";
//Paso 6
		Date fechaEjecucionInicio;
//Paso 8
		String statusE = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,RECEIVED_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE RECEIVED_DATE>='%s' AND DOC_TYPE='SAL' AND ID='%s'";
//Paso 10
		String detalleEjecucion = "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE LIKE '%PR24' "
				+ "AND STATUS = 'S' ORDER BY START_DT DESC";
//Paso 11
		String consultaThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,END_DT,STATUS "
				+ "FROM WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";
//Paso 12
		String consultaErrores = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE FROM WM_LOG_ERROR "
				+ "WHERE RUN_ID = '%s'";
//Paso 13
		//La consulta esta mal, la tabla RTK_INBOUND_DOCS no existe en la BD "RMS FCRMSQA"
		String consultaRTK_INBOUND_DOCS = "SELECT * FROM RTK_INBOUND_DOCS"
				+ "WHERE DOC_TYPE = 'SAL' "
				+ "AND STATUS = 'L'"
				+ "AND run_id in ([WM_LOG_THREAD.THREAD_ID]);";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//***************************************** Paso 1 y 2***************************************************************** 
			
		addStep("Buscar los registros enviados por XPOS para procesar en las tablas POS_ENVELOPE en BD FCWM6QA, para la plaza y tienda.");
		System.out.println("Paso 1 y 2: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(consultaRegistro);
		
		SQLResult exe_consultaRegistro = executeQuery(dbFCWM6QA_NUEVA, consultaRegistro);
		
		boolean validaConsulta = exe_consultaRegistro.isEmpty();
		String id_POS_ENVELOPE = data.get("id");
		String plaza = "";
		String tienda = "";
		String POS_ENVELOPE_Date = data.get("received_date");
		
		
		if (!validaConsulta) {
			//id_POS_ENVELOPE = exe_consultaRegistro.getData(0, "ID");
			plaza = exe_consultaRegistro.getData(0, "PV_CR_PLAZA");
			tienda = exe_consultaRegistro.getData(0, "PV_CR_TIENDA");
			//POS_ENVELOPE_Date = exe_consultaRegistro.getData(0,"RECEIVED_DATE");
			testCase.addQueryEvidenceCurrentStep(exe_consultaRegistro);
		} 
		
		System.out.println("ID: "+ id_POS_ENVELOPE);
		System.out.println("Plaza : "+ plaza);
		System.out.println("Tienda : "+ tienda);
		
		System.out.println(validaConsulta);
		assertFalse(validaConsulta, "No existe información pendiente de procesar en la tabla."); //AQUI DEBE SER asserFalse
		
//***************************************** Paso 3 ***************************************************************** 
		
		addStep("Validar con el PE_ID muestre el archivo SAL en STATUS='I', listo para procesar en la tabla POS_INBOUND_DOCS de la BD FCWM6QA.");
		System.out.println("Paso 3: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String consultaStatusIFormat = String.format(consultaStatusI, id_POS_ENVELOPE);
		System.out.println(consultaStatusIFormat);
		
		SQLResult exe_consultaStatusI = executeQuery(dbFCWM6QA_NUEVA, consultaStatusIFormat);
		
		boolean validaConsultaStatusI = exe_consultaStatusI.isEmpty();
		String id_POS_INBOUND_DOCS = "";
		
		if (!validaConsultaStatusI) {
			id_POS_INBOUND_DOCS = exe_consultaRegistro.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(exe_consultaStatusI);
		} 
		
		System.out.println(validaConsultaStatusI);
		assertFalse(validaConsultaStatusI, "No existe información pendiente de procesar en la tabla."); //Es FALSE
		
//***************************************** Paso 4 ***************************************************************** 
		
		addStep(" Validar en la tabla POS_SAL de la BD FCWM6QA, muestre el número de registro que contiene el archivo SAL.");
		System.out.println("Paso 4: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String registroSALFormat = String.format(registrosSAL, id_POS_INBOUND_DOCS);
		System.out.println(registroSALFormat);
		
		SQLResult exe_registroSAL = executeQuery(dbFCWM6QA_NUEVA,registroSALFormat);
		
		boolean validaRegistroSal = exe_registroSAL.isEmpty();
		
		if(!validaRegistroSal) {
			testCase.addQueryEvidenceCurrentStep(exe_registroSAL);
		}
		
		System.out.println(validaRegistroSal);
		assertFalse(validaRegistroSal, "No existe información pendiente de procesar en la tabla."); //Es FALSE
		
//***************************************** Paso 5 ***************************************************************** 
		
		addStep("Validar en la tabla POS_SAL_DETL de la BD FCWM6QA, muestre el número de registro que contiene el archivo SAL");	
		System.out.println("Paso 5: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String registroSALFormat2 = String.format(registrosSAL2, id_POS_INBOUND_DOCS);
		System.out.println(registroSALFormat2);
		
		SQLResult exe_registroSAL2 = executeQuery(dbFCWM6QA_NUEVA,registroSALFormat2);
		
		boolean validaRegistroSal2 = exe_registroSAL2.isEmpty();
		
		if(!validaRegistroSal2) {
			testCase.addQueryEvidenceCurrentStep(exe_registroSAL2);
		}
		
		System.out.println(validaRegistroSal2);
		assertFalse(validaRegistroSal2, "No existe información pendiente de procesar en la tabla."); //Es FALSE
		
//***************************************** Paso 6 y 7 *****************************************************************
		
		addStep("Ejecución control-M");

		fechaEjecucionInicio = new Date();

		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));

		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u.get(data.get("cm_server"));
		u.hardWait(40);
		u.waitForLoadPage();
		cm.logOn();

		testCase.addTextEvidenceCurrentStep("Inicio de job");
		JobManagement j = new JobManagement(u, testCase, array);
		u.hardWait(5);
		System.out.println(data.get("Espera 5 sec, antes de Resultado Ejecucion"));
		String resultadoEjecucion = j.jobRunner();

		// Abrir la herramienta de control M para validar que la ejecución del job haya
		// sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		
	//	  EL JOB PR24 ESTA FALLANDO A LA FECHA 30/11/2022
		 
		
		
//***************************************** Paso 8 *****************************************************************
		
		addStep("Validar que se haya actualizado el registro del archivo SAL en la tabla POS_INBOUND_DOCS con STATUS='E' y el nombre del archivo POSU[PLAZA][TIENDA][YYYYMMDDHHMISS].dat en el campo TARGED_ID.");	
		System.out.println("Paso 8: "+GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String statusEFormat = String.format(statusE, POS_ENVELOPE_Date, id_POS_ENVELOPE);
		System.out.println(statusEFormat);
		
		SQLResult exe_StatusE = executeQuery(dbFCWM6QA_NUEVA,statusEFormat);
		String status = "";
		System.out.println("El status es: "+status);
		
		boolean validaStatusE = exe_StatusE.isEmpty();
		
		if(!validaStatusE) {
			status = exe_StatusE.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(exe_StatusE);
		}
		
		System.out.println(validaStatusE);
		assertEquals(status, "E");
		
//***************************************** Paso 9 y 10 *****************************************************************
		addStep("Validar que se haya insertado el detalle de la ejecución de la interface PR24 en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'");
		System.out.println("Paso 9 y 10: "+GlobalVariables.DB_HOST_FCWMLTAEQA);
		System.out.println(detalleEjecucion);
		
		SQLResult exe_detalleEjecucion = executeQuery(dbFCWMLTAEQA_MTY,detalleEjecucion);
		String status2 = exe_detalleEjecucion.getData(0, "STATUS");
		String runID = exe_detalleEjecucion.getData(0, "RUN_ID");
		System.out.println("El status es: "+status2);
		
		boolean validaDetalleEjecucion = exe_detalleEjecucion.isEmpty();
		
		if(!validaDetalleEjecucion) {
			testCase.addQueryEvidenceCurrentStep(exe_detalleEjecucion);
		}
		
		System.out.println(validaDetalleEjecucion);
		assertEquals(status, "S");
		
//***************************************** Paso 11 *****************************************************************
		addStep("Validar el logeo de los threads en la tabla WM_LOG_THREAD de WMLOG. El nombre del thread debe estar en el "
				+ "formato PR24_CR_PLAZA_CR_TIENDA_RUN_ID. Los thread lanzados deben terminar con STATUS = 'S'.");
		System.out.println("Paso 11 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaThreadFormat = String.format(consultaThread,runID);
		System.out.println(consultaThreadFormat);
				
		SQLResult exe_consultaThread = executeQuery(dbFCWMLQA_WMLOG, consultaThreadFormat);
		String status3 = exe_consultaThread.getData(0, "STATUS");
		System.out.println("El status es: "+status3);
		
		boolean validaConsultaThread = exe_consultaThread.isEmpty();
				
		if (!validaConsultaThread) {
					
			testCase.addQueryEvidenceCurrentStep(exe_consultaThread);
			} 
				
		System.out.println(validaConsultaThread);
		assertEquals(status, "S");
//***************************************** Paso 12 *****************************************************************
		addStep("Validar que no haya registro de errores en la tabla WM_LOG_ERROR de FCWMLTAQ. ");
		System.out.println("Paso 12 "+GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		String consultaErroresFormat = String.format(consultaErrores,runID);
		System.out.println(consultaErroresFormat);
		
		SQLResult exe_consultaErrores = executeQuery(dbFCWMLQA_WMLOG, consultaErroresFormat);
		
		boolean validaConsultaErrores = exe_consultaErrores.isEmpty();
		
		if (!validaConsultaErrores) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaErrores);
			} 
		
		System.out.println(validaConsultaErrores);
		assertTrue(validaRegistroSal2, "Existen errores en la tabla");
		
//***************************************** Paso 13 y 14 *****************************************************************
		//La consulta esta mal, la tabla RTK_INBOUND_DOCS no existe en la BD "RMS FCRMSQA"
		addStep(" Se consulta la tabla RTK_INBOUND_DOCS para validar los documentos SAL depositados en el servidor "
				+ "FTP de RMS con STATUS = 'L' y el run id de cada registro es el thread que se lanzo por cada documento. ");
		/**
		 * PENDIENTE
		 */
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Enviar la información de ventas diarias al sistema RMS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR24_ProcesarArchivoSALDeSaldoVentasDiariasATravesInterfaceFEMSA_PR24_test";
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
