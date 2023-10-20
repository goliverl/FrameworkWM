package interfaces.pr11;

/*
 * I20016 Actualizacion tecnologica Webmethods 10
 * MTC-FT-019 PR36 Generación de archivo ILS de catalogo de 
 * proveedores a través de la interface FEMSA_PR36
 * 
 * Prueba de regresión  para comprobar la no afectacion en la funcionalidad principal de la interface 
 * eFEMSA_PR36 para generar archivos ILS (Catalogo de proveedores) de bajada ( de RMS a WM OUTBOUND) 
 * con la información actualizada o nueva de Proveedor Primario de RETEK , al ser migrada la interface de WM9.9 a WM10.5
* El objetivo de esta interfaz es mantener sincronizado los datos del Proveedor 
* Primario de RETEK en el sistema de Punto de Venta, ya que el dato de proveedor 
* es indispensable para poder generar Ordenes de compra a proveedores y requisiciones para 
* transferencias en la tienda. Además también será utilizada para obtener el atributo 
* STORE_REORDERABLE que será utilizado para identificar si un artículo se puede recibir o no. 
* También se utilizará para enviar al POS las tolerancias que se aplicarán a 
* las órdenes de compra generadas en Retek.
* Origen: 
* Retek (REPL_ITEM_LOC, ITEM_LOC_TRAITS, SUPS, WH) 
* Destino: 
* POS
*
*@author: Ultimo Mantenimiento Mariana Vives
*@date: 23/02/2023
*/

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_005_PR11_WMx86_ProcesaArchivosINVError extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR11_WMx86_ProcesaArchivosINVError_test(HashMap<String, String> data) throws Exception {
		
		/*Back Office Mexico: MTC-FT-020 Procesar al menos 10 archivos INV de inventario de tienda a través de la interface FEMSA_PR11
 * Desc:
 * Prueba de regresión  para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PR11 para procesar al menos 
 * 10 archivos INV (Inventario de Tienda) de subida (de WM INBOUND a RMS), al ser migrada la interface de WM9.9 a WM10.5
 * @author Marisol Rodriguez
 * @date   2022/07/16*/

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
         //Paso 2
		String tdcQueryDocProcesar = "SELECT DISTINCT PE.PV_CR_PLAZA, PE.PV_CR_TIENDA,PID.ID, PID.PE_ID, PID.STATUS, TO_CHAR(PD.FECHA_ADM,'YYYYMMDD') FECHA_MVT\r\n"
				+ "FROM POSUSER.POS_ENVELOPE PE\r\n" + "JOIN POSUSER.POS_INBOUND_DOCS PID \r\n"
				+ "ON PID.PE_ID = PE.ID\r\n" + "JOIN POSUSER.POS_INV_DETL PD \r\n" + "ON  PID.ID = PD.PID_ID\r\n"
				+ "WHERE PID.DOC_TYPE = 'INV' \r\n" + "AND PID.STATUS = 'I'\r\n"
				+ "AND PID.PARTITION_DATE>=TRUNC(SYSDATE-2)\r\n" + "AND PE.PARTITION_DATE>=TRUNC(SYSDATE-2)\r\n"
				+ "AND ROWNUM = 1 \r\n" + "order by FECHA_MVT desc";

		//Paso 6
	String tdcQueryIntegrationServer = "select run_id,interface, start_dt, end_dt, status, server \r\n"
				+ "from WMLOG.WM_LOG_RUN  \r\n"
				+ "WHERE INTERFACE = 'PR11'\r\n"
				+ "AND  start_dt >= TRUNC(SYSDATE) \r\n"
				+ "AND rownum =1 \r\n"
				+ "ORDER BY START_DT DESC";

		//Paso 7
	   //Consultas thread
		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  \r\n"
				+ "FROM wmlog.WM_LOG_THREAD \r\n" + 
				" where PARENT_ID= '%s' " ;


		String consultaTHREAD2 = "SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD \r\n"
				+ "where PARENT_ID='%s' ";
		
		//Paso 8
		//Consultas de error
				String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
					 "from wmlog.WM_LOG_ERROR \r\n" + 
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <=1"; // dbLog
				
				String consultaError2 = " select description,MESSAGE \r\n" +
					 "from wmlog.WM_LOG_ERROR \r\n" +
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <= 1"; // dbLog
				


				//Paso 10
		String queryDocsProcesados = "select*from XXFC.XXFC_ITEM_LOC_POS;\r\n"
				+ "SELECT * FROM XXFC.XXFC_ITEM_LOC_POS\r\n"
				+ "WHERE CR_PLAZA = '" + data.get("plaza") + "'\r\n"
				+ "AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
				+ "AND FECHA_MVT >= '%s'\r\n"
				+ "AND TRUNC(PROCESSED_DATE) = TRUNC(SYSDATE)";

		//Paso 11
		String tdcQueryUpdateStatusPos = "SELECT id, pe_id, pv_doc_id,pv_doc_name , status, doc_type\r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'INV'\r\n"
				+ "AND STATUS = 'I';\r\n"
				+ "AND ID = '%s'";// [POS_INBOUND_DOCS.ID]
		
		testCase.setProject_Name("POC WMX86");
		
		testCase.setPrerequisites(data.get("prerequicitos"));


		
		
		// utileria

		String id = "";
		String fecha_mvt = "";
		

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 * 
		 */

//***********************************************Paso 1 **********************************************
/*
		addStep("Establecer la conexión a la BD **FCWM6QA**.");

		testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);

//************************************************Paso 2 **********************************************

		addStep("Validar que exista documentos de tipo DOC_TYPE='INV' pendiente de procesar en la tabla POS_INBOUND_DOCS "
				+ "de POSUSER con STATUS = 'I' para la plaza.");

		System.out.println(tdcQueryDocProcesar);

		SQLResult DocProcesarI = executeQuery(dbPos, tdcQueryDocProcesar);

		boolean validaDocI = DocProcesarI.isEmpty();

		if (!validaDocI) {

			id = DocProcesarI.getData(0, "ID");
			System.out.println("ID: " + id);
			fecha_mvt = DocProcesarI.getData(0, "FECHA_MVT");

			testCase.addQueryEvidenceCurrentStep(DocProcesarI);

		}

		System.out.println(validaDocI);

		assertFalse(validaDocI, "No existen documentos de tipo DOC_TYPE='INV' pendientes de procesar");
*/
//***********************************************Paso 3	y 4 ***********************************************
		addStep("Ingresar a Control-M con sus respectivas credenciales. Validar que el Job se haya ejecutado exitosamente siguiendo la ruta correspondiente para ubicar al Job.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		u.get(data.get("server"));

		String status = "E";

		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addTextEvidenceCurrentStep("Job en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));

		testCase.addTextEvidenceCurrentStep("Login");

		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn();

//Buscar del job
		testCase.addTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertFalse(!flag, "No se encontro el job ");

//Ejecucion
		String resultado = control.executeJob();
		testCase.addTextEvidenceCurrentStep(resultado);
		System.out.println("Resultado de la ejecucion -> " + resultado);

//Valor del output 
		testCase.addTextEvidenceCurrentStep("Output: " + control.getOutput());
		System.out.println("Valor de output :" + control.getOutput());

//Validacion del caso
		Boolean casoPasado = true;

		if (resultado.equals("Failure")) {

			casoPasado = false;

			System.out.println("El caso paso correctamente");

		}
		assertFalse(casoPasado, "Se presento una  falla en la ejecucion");

		control.closeViewpoint();

		u.close();

//********************************************** Paso 5 	************************************
		
		addStep("Establecer conexión a la BD FCWMLQA (oxfwm6q00.femcom.net).");
		
		testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
		
//**********************************************Paso 6 ******************************************		

		addStep("Validar que se inserte el detalle de la ejecución de la interface PR11 en la tabla WM_LOG_RUN de WMLOG con STATUS = 'E' (Error).");

		SQLResult ISF_Result = executeQuery(dbLog, tdcQueryIntegrationServer);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es E = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		} else {
			
			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "No se presentaron errores de ejecucion.");
		

//******************************************* Paso 7 ********************************************
/*
		addStep("Validar que no se haya hecho el insertado de los threads ya que la corrida de la interfaz PR11 no se realizó de manera correcta, "
				+ "consultar la tabla WM_LOG_THREAD de WMLOG.");

		//Parte 1
				String consultaTreads1 = String.format(consultaTHREAD, RUN_ID);
				
				System.out.println(consultaTreads1);
				
				SQLResult consultaTreads1_R = dbLog.executeQuery(consultaTreads1);

				boolean validaThreads1 = consultaTreads1_R.isEmpty();
				
				if (validaThreads1) {
					
					testCase.addTextEvidenceCurrentStep("No se insertaron los threads ya que la corrida de la interfaz PR11 no se realizó de manera correcta");
					testCase.addQueryEvidenceCurrentStep(consultaTreads1_R);
				}
			
				System.out.println(validaThreads1);
				
				// Parte 2

				String consultaTreads2 = String.format(consultaTHREAD2, RUN_ID);
				
				System.out.println(consultaTreads2);
				
				SQLResult consultaTreads2_R = dbLog.executeQuery(consultaTreads2);

				boolean validaThreads2 = consultaTreads2_R.isEmpty();
				
				if (validaThreads2) {
					
					testCase.addTextEvidenceCurrentStep("No se insertaron los threads ya que la corrida de la interfaz PR11 no se realizó de manera correcta");
					testCase.addQueryEvidenceCurrentStep(consultaTreads2_R);
				}

				System.out.println(validaThreads2);
				
			    assertFalse("Se generaron threads ",validaThreads2);
			    
//**************************************************Paso 8 *************************************************
			    
	addStep(" Validar que se haya logeado el error de la ejecución de la interfaz PR11 en la tabla WM_LOG_ERROR mediante la siguiente consulta:");

				//Parte 1
				String consultaError1_f = String.format(consultaError1, RUN_ID);
				
				System.out.println(consultaTreads1);
				
				SQLResult consultaError1_R = dbLog.executeQuery(consultaError1_f);

				boolean validaConsultaError1_f = consultaError1_R.isEmpty();
				
				if (!validaConsultaError1_f) {
					
					testCase.addBoldTextEvidenceCurrentStep("Se registraron errores en la ejecucion correctamente");
					testCase.addQueryEvidenceCurrentStep(consultaError1_R);
					
				} else {
					
					
					testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion correctamente");
				}
			
				System.out.println(validaConsultaError1_f);
				
				
				//Parte 2
				String consultaError2_f = String.format(consultaError2, RUN_ID);
						
				System.out.println(consultaTreads2);
						
				SQLResult consultaError2_R = dbLog.executeQuery(consultaError2_f);

				boolean validaConsultaError2_f = consultaError2_R.isEmpty();
						
				if (!validaConsultaError2_f) {
					
					testCase.addBoldTextEvidenceCurrentStep("Se registraron errores en la ejecucion correctamente");		
					testCase.addQueryEvidenceCurrentStep(consultaError2_R);
				} else {
					
					testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion correctamente");
				}
					
				System.out.println(validaConsultaError2_f);
				
				assertFalse("No se logeo el error en la ejecucion. ",validaConsultaError2_f);
				
//***************************************************Paso 9  **********************************************
				
				 addStep("Establecer la conexión a la BD **FCRMSQA**.");
				 
				 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_MEX);
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_RMS_MEX);
				 
//****************************************************Paso 10 **********************************************	

		addStep("Validar que no se haya insertado la información de los documentos que no fueron procesados en la tabla XXFC.XXFC_ITEM_LOC_POS de RETEK.");

		
		String queryDocsProcesados_Format = String.format(queryDocsProcesados, fecha_mvt);
		System.out.println(queryDocsProcesados_Format);
		SQLResult queryDocsProcesados_Res = executeQuery(dbRms, queryDocsProcesados_Format);
		boolean validaDocsProcesados = queryDocsProcesados_Res.isEmpty();

		if (validaDocsProcesados) {
			
			testCase.addTextEvidenceCurrentStep("No inserto la información de los documentos que no fueron procesados");

			testCase.addQueryEvidenceCurrentStep(queryDocsProcesados_Res);

		}

		System.out.println(validaDocsProcesados);

		assertTrue(validaDocsProcesados, "Se muestran registros procesados ");

//******************************************************Paso 11	************************************************

		addStep(" Validar que NO se haya actualizado el estatus de los documentos procesados en la tabla de WM POS_INBOUND_DOCS ya que la interfaz no se ejecutó de manera exitosa, el estatus deberá seguir en STATUS = 'I'.");

		String tdcQueryUpdateStatusPos_fortmat = String.format(tdcQueryUpdateStatusPos, id);
		
		SQLResult tdcQueryUpdateStatusPos_Res = executeQuery(dbPos, tdcQueryUpdateStatusPos_fortmat);

		boolean validatdcQueryUpdateStatusPos  = tdcQueryUpdateStatusPos_Res .isEmpty();
		
		System.out.println(validatdcQueryUpdateStatusPos);

		if (!validatdcQueryUpdateStatusPos ){
			
			testCase.addTextEvidenceCurrentStep("No se actualizo el status de los documentos ");

			testCase.addQueryEvidenceCurrentStep(tdcQueryUpdateStatusPos_Res );

		}

		System.out.println(validatdcQueryUpdateStatusPos );

		assertFalse(validatdcQueryUpdateStatusPos , " No se muestran registros procesados en  POS_INBOUND_DOCS");
*/
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "ATC-FT-021 Registro de errores en WMLOG al intentar procesar archivos INV de inventario de tienda a través de la interface FEMSA_PR11";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Sergio Robles Ramos";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PR11_WMx86_ProcesaArchivosINVError_test";
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

