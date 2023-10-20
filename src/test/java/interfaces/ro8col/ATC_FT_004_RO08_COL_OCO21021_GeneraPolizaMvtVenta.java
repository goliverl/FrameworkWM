package interfaces.ro8col;

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
import utils.sql.SQLResult;

public class ATC_FT_004_RO08_COL_OCO21021_GeneraPolizaMvtVenta extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_RO08_COL_OCO21021_GeneraPolizaMvtVenta_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

	utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
	utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		//Paso 1 
		String pteProcesar_fem_fif_stg1 = " SELECT ITEM, ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE \r\n"
				+ "FROM fem_fif_stg \r\n" 
				+ "WHERE CR_PLAZA = '"+ data.get("plaza") + "'\r\n"
				+ "AND tran_code = 1\r\n"
				+ "AND tran_date >= TRUNC(SYSDATE-60)\r\n"
				+ "AND reference_3 IS NULL\r\n"
				+ "AND reference_9 IS NULL \r\n"
				+ "and vat_code in ('" + data.get("vat_code1") + "','" + data.get("vat_code2") 
				+ "','" + data.get("vat_code3") + "','" + data.get("vat_code4") + "')";// RMSCOL
		
		String pteProcesar_fem_fif_stg2 = "SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID \r\n"
				+ "FROM fem_fif_stg \r\n"
				+ "WHERE CR_PLAZA = '"+ data.get("plaza") + "'\r\n"
				+ "AND tran_code = 1 \r\n"
				+ "AND tran_date >= TRUNC(SYSDATE-60)\r\n"
				+ "AND reference_3 IS NULL\r\n"
				+ "AND reference_9 IS NULL \r\n"
				+ "and vat_code in ('" + data.get("vat_code1") + "','" + data.get("vat_code2") 
				+ "','" + data.get("vat_code3") + "','" + data.get("vat_code4") + "') ";// RMSCOL

		//Paso 3
		String tdcIntegrationServerFormat = "select run_id,interface, start_dt, end_dt, status, server \r\n"
						+ "from WMLOG.WM_LOG_RUN \r\n"
						+ "WHERE INTERFACE = 'RO08_COL'\r\n"
						+ "AND  start_dt >= TRUNC(SYSDATE) \r\n"
						+ "AND rownum =1 \r\n"
						+ "ORDER BY START_DT DESC";// Consulta para estatus de la ejecucion			

		//Paso  4
		String VerificacionHeader = "SELECT HEADER_ID,TRAN_DATE,CR_PLAZA,RUN_ID,JOURNAL_TYPE_ID,GL_JOURNAL_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_COL" 
				+ " WHERE cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code = '1'" 
				+ " AND run_id = '%s'";// RMSCOL
		
		String FEM_FIF_STG_reference9 = "SELECT id, reference_9 \r\n"
				+ "FROM fem_fif_stg \r\n"
				+ "WHERE reference_9 IS NOT NULL \r\n" //[wm_gl_header_col.header_id]
				+ "AND reference_3 IS NOT NULL \r\n" //[gl_interface.reference6]
				+ "AND CR_PLAZA = '"+ data.get("plaza") + "'\r\n"
				+ "AND tran_code = 1\r\n"
				+ "AND ID = '%s' ";

		
		//Paso 5
		String VerificacionLines = "SELECT LINE_ID,HEADER_ID,TYPE,NET_RETAIL,VAT_RETAIL,NET_COST,VAT_COST \r\n" +
				        "FROM WMUSER.WM_GL_LINES_COL \r\n" + 
						"WHERE HEADER_ID = '%s'";
				
		String VerificacionLines2 = "SELECT REASON_CODE,VAT_RATE_RETAIL,VAT_RATE_COST,VAT_CODE_RETAIL,VAT_CODE_COST \r\n" +
				        "FROM WMUSER.WM_GL_LINES_COL \r\n" + 
						"WHERE HEADER_ID ='%s'";
				
		//Paso 6 
		String procesadoFEM_FIF_STG1 = "SELECT id, ITEM, ITEM_DESC,STORE,TRAN_DATE \r\n"
				+ "FROM fem_fif_stg \r\n"
				+ "WHERE reference_9 = '%s' \r\n" //[wm_gl_header_col.header_id]
				+ "AND reference_3 IS NOT NULL \r\n" //[gl_interface.reference6]
				+ "AND CR_PLAZA = '"+ data.get("plaza") + "'\r\n"
				+ "AND tran_code = 1\r\n"
				+ "AND ID = '%s' ";
				
		
		String procesadoFEM_FIF_STG2 = "SELECT TRAN_CODE, REFERENCE_3,REFERENCE_9,CR_PLAZA \r\n"
				+ "FROM fem_fif_stg \r\n"
				+ "WHERE reference_9 = '%s' \r\n" //[wm_gl_header_col.header_id]
				+ "AND reference_3 IS NOT NULL \r\n" //[gl_interface.reference6]
				+ "AND CR_PLAZA = '"+ data.get("plaza") + "' \r\n"
				+ "AND tran_code = 1\r\n"
				+ "AND ID = '%s'";
		
		//Paso 7
				String InsercionGL_INTERFACE1 = "SELECT reference6,reference4,reference10,reference22,reference25 \r\n"
						+ "FROM GL.GL_INTERFACE \r\n"
						+ "WHERE reference6 = '%s'"; //fem_fif_stg.reference_3
				
				String InsercionGL_INTERFACE2 = "SELECT user_je_category_name,user_je_source_name,segment4 \r\n"
						+ "FROM GL.GL_INTERFACE \r\n"
						+ "WHERE reference6 = ''"; //fem_fif_stg.reference_3
					
       
		//Paso 8 
		String difInventario1 = "select reference6,reference4,reference10,reference22,reference25 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = 'Salida x venta al costo bienes excluidos'\r\n"
				+ "and reference6 = '%s'";
		
		String difInventario2 = "select user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = 'Salida x venta al costo bienes excluidos'\r\n"
				+ "and reference6 = '%s'";
		
		//Paso 9		
		String difInventarioTaza0_1 = "select reference6,reference4,reference10,reference22,reference25 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4Paso9") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";
		
		String difInventarioTaza0_2 = "select user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4Paso9") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";
		
		//Paso 10
		String difInventarioTaza5_1 = "select reference6,reference4,reference10,reference22,reference25 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";

		String difInventarioTaza5_2 = "select user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";
		
		//Paso 11
		String difInventarioTazaICO_1 = "select reference6,reference4,reference10,reference22,reference25 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";

		String difInventarioTazaICO_2 = "select user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";
		//Paso 12
		String salidaXVentaAlCosto1 = "select reference6,reference4,reference10,reference22,reference25 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";

		String salidaXVentaAlCosto2 = "select user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "from GL.gl_interface \r\n"
				+ "where  segment2 = '"+ data.get("segment2") + "' \r\n"
				+ "and segment4 = '"+ data.get("segment4") + "' \r\n"
				+ "and ENTERED_CR is not null \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 = '%s'";
		
		
		testCase.setProject_Name("OCO21021 - MEJORAS ADMINISTRATIVO");
		
		//testCase.setTest_Description("Verificar el proceso de "+data.get("transaccion"));

		/**
		 * OCO21021 - MEJORAS ADMINISTRATIVO: MCT-FT-001_Generar Poliza con movimientos de venta - Bogota
 Desc:
 Se requiere agregar las lineas del Costo a la Poliza contable de Ventas.
 @Marisol Rodriguiez
 @date   2022/07/18
		 * 
		 */
		
		
		  
//***************************************** Paso 1 ****************************************************************************	
		addStep("Validar informacion pendiente de procesar en la tabla FEM_FIF_STG.");

		String ID = "";
		
		System.out.println(pteProcesar_fem_fif_stg1);
		
		System.out.println(pteProcesar_fem_fif_stg2);

	   //Parte 1
		SQLResult pteProcesar_fem_fif_stg1_res = executeQuery(dbRms, pteProcesar_fem_fif_stg1);
		
		//Parte 2
		SQLResult pteProcesar_fem_fif_stg2_res = executeQuery(dbRms, pteProcesar_fem_fif_stg2);
		
		testCase.addQueryEvidenceCurrentStep(pteProcesar_fem_fif_stg1_res);
		testCase.addQueryEvidenceCurrentStep(pteProcesar_fem_fif_stg2_res);
		
		boolean validaPteProcesar_fem_fif_stg  = pteProcesar_fem_fif_stg1_res.isEmpty();

		if (!validaPteProcesar_fem_fif_stg) {

			 ID = pteProcesar_fem_fif_stg2_res.getData(0, "ID");
			 System.out.println("FEM_FIF_STG.ID: "+pteProcesar_fem_fif_stg2_res);
			
			

		}

		System.out.println(validaPteProcesar_fem_fif_stg);

		assertFalse(validaPteProcesar_fem_fif_stg, "No se obtuvo informaciÃ³n pendiente de procesar en la tabla FEM_FIF_STG.");


// ***************************************** Paso 2 **************************************************
		addStep("Ejecutar el JOB RO8_COL en control m");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		u.get(data.get("server"));
	
		
		String status = "S";
		
		
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
		assertFalse(!flag, "No se encontro el job");
		
		//Ejecucion
		String resultado = control.executeJob();
		testCase.addTextEvidenceCurrentStep( resultado);
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		testCase.addTextEvidenceCurrentStep("Output: " + control.getOutput());
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		
		if(resultado.equals("Failure")) {
			
		casoPasado = false;
		
		System.out.println("La ejecucion del Job no fue exitosa");
		
		}		
		
		assertTrue(casoPasado, "La ejecucion del Job no fue exitosa");

		control.closeViewpoint(); 
		
		u.close();
		
//***************************************** Paso 3**************************************************************************
		addStep("Verificar que la ejecucion termino con exito.");

		SQLResult ISF_Result = executeQuery(dbLog, tdcIntegrationServerFormat);
		
		System.out.println(  tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		System.out.println("STATUS: "+ STATUS);
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		System.out.println("RUN_ID: "+ RUN_ID);
		
		String statusEsperado = "S";
		
		/*
		String STATUS = "S";
		String RUN_ID = "12345";*/
		
		boolean validateStatus = STATUS .equals(statusEsperado);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		} else {
			
			testCase.addBoldTextEvidenceCurrentStep("Se presento un error en la ejecucion.");
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
		
//**************************************** Paso 4 *********************************************************************
		

		addStep("Verificar la actualizacion de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL ");	
		
		//Se hace la consulta de la tabla fem_fif_stg para obtener el reference 9
        String FEM_FIF_STG_reference9_format = String.format(FEM_FIF_STG_reference9, ID);
        
        System.out.println(FEM_FIF_STG_reference9_format); //reference_9
        
        SQLResult FEM_FIF_STG_reference9_Result = executeQuery(dbRms, FEM_FIF_STG_reference9_format);
        		     
		String reference_9 = "";
		
		boolean validaReference9 = FEM_FIF_STG_reference9_Result.isEmpty();
		
		System.out.println(validaReference9);
		
        if ( !validaReference9) {
			
        	reference_9 = FEM_FIF_STG_reference9_Result.getData(0,"REFERENCE_9");
        	System.out.println("REFERENCE_9: " + reference_9);
			
		}
		
//*********************************************************************************************************************	
        //Una vez obtenido el REFERENCE_9  se pasa a la consulta.
		String VerificacionHeader_format = String.format(VerificacionHeader, reference_9);
		
		System.out.println(VerificacionHeader_format);
		
		SQLResult VerificacionHeader_Result = executeQuery(dbRms, VerificacionHeader_format);
		
		//se extraer el header_id para usar en el paso 5 y 7
		String header_id= "";
		
		testCase.addQueryEvidenceCurrentStep(VerificacionHeader_Result);
		
		boolean validaVerificacionHeader = VerificacionHeader_Result.isEmpty();
		
		if ( !validaVerificacionHeader) {
			
			header_id = VerificacionHeader_Result.getData(0,"HEADER_ID");
			System.out.println("HEADER_ID: " + validaVerificacionHeader);
			
		}
		
		System.out.println(validaVerificacionHeader);

		assertFalse(validaVerificacionHeader, "No se actualizo el JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL");

//******************************************* Paso 5 *********************************************************************
		
		addStep("Insercion de lineas en la tabla WM_GL_LINES_COL");

		String VerificacionLines_format = String.format(VerificacionLines, header_id);
		
		System.out.println(VerificacionLines_format);
		
		String VerificacionLines2_format = String.format(VerificacionLines2, header_id);
		
		System.out.println(VerificacionLines2_format);
		
		SQLResult VerificacionLines_result = executeQuery(dbRms, VerificacionLines_format);
		SQLResult VerificacionLines2_result = executeQuery(dbRms, VerificacionLines2_format);

		boolean validaVerificacionLines = VerificacionLines_result.isEmpty(); // esta vacio, si no es asi, pasa el paso

		
			testCase.addQueryEvidenceCurrentStep(VerificacionLines_result);
			testCase.addQueryEvidenceCurrentStep(VerificacionLines2_result);

	
		System.out.println(validaVerificacionLines);

		assertFalse(validaVerificacionLines, "No se insertaron las lineas en la tabla WM_GL_LINES_COL");
		
//******************************************** Paso 6 *******************************************************************

		addStep("Verificar la actualizacion de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG ");

				
				String procesadoFEM_FIF_STG_format1 = String.format(procesadoFEM_FIF_STG1,header_id ,ID);
				
				System.out.println(procesadoFEM_FIF_STG_format1);
				
				String procesadoFEM_FIF_STG_format2 = String.format(procesadoFEM_FIF_STG2, header_id,ID);
				
				System.out.println(procesadoFEM_FIF_STG_format2);

				SQLResult procesadoFEM_FIF_STG1result = executeQuery(dbRms,procesadoFEM_FIF_STG_format1);
				SQLResult procesadoFEM_FIF_STG2result= executeQuery(dbRms, procesadoFEM_FIF_STG_format2);
				
				String reference3 = "";
				
				testCase.addQueryEvidenceCurrentStep(procesadoFEM_FIF_STG1result);
				testCase.addQueryEvidenceCurrentStep(procesadoFEM_FIF_STG2result);

				
				boolean validaProcesadoFEM_FIF_STG1 = procesadoFEM_FIF_STG1result .isEmpty();

				if (!validaProcesadoFEM_FIF_STG1) {

					reference3 = procesadoFEM_FIF_STG2result.getData(0, "reference_3");
					System.out.println("fem_fif_stg.reference_3: "+ reference3);
					
					
				}

				System.out.println(validaProcesadoFEM_FIF_STG1);

			assertFalse(validaProcesadoFEM_FIF_STG1, "No se actualizo actualizacion el REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG");
				
					
//******************************************** Paso 7 ***********************************************************************		

		addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE.");
			
		//Verificar insercion de lineas en la tabla GL_INTERFACE con el reference 3
		String InsercionGL_INTERFACE1_format = String.format(InsercionGL_INTERFACE1, reference3);
		System.out.println(InsercionGL_INTERFACE1_format);
		
		String InsercionGL_INTERFACE2_format = String.format(InsercionGL_INTERFACE2, reference3);
		System.out.println(InsercionGL_INTERFACE2_format);
			
		SQLResult  InsercionGL_INTERFACE1_result = executeQuery(dbEbs, InsercionGL_INTERFACE1_format);
				
		SQLResult  InsercionGL_INTERFACE2_result = executeQuery(dbEbs, InsercionGL_INTERFACE2_format);

		boolean validaInsercionGL_INTERFACE = InsercionGL_INTERFACE1_format.isEmpty();

		
			testCase.addQueryEvidenceCurrentStep(InsercionGL_INTERFACE1_result);
			testCase.addQueryEvidenceCurrentStep(InsercionGL_INTERFACE2_result);


		System.out.println(validaInsercionGL_INTERFACE);

		assertFalse(validaInsercionGL_INTERFACE, "No se obtuvoregistro de la inserciÃ³n de lÃ­neas en la tabla GL_INTERFACE.");

	
//******************************************* Paso 8 **********************************************************************
		
		addStep("Validar que se encuentra en el credito la sumatoria del  movimiento Salida x venta al costo bienes excluidos");
		
		//Parte1
		String difInventario1_format = String.format(difInventario1, reference3 );
		
		System.out.println(difInventario1_format);
		
		SQLResult difInventario1_result = executeQuery(dbEbs, difInventario1_format);
		
		//Parte 2
        String difInventario2_format = String.format(difInventario2, reference3 );
		
		System.out.println(difInventario2_format);
		
		SQLResult  difInventario2_result = executeQuery(dbEbs, difInventario2_format);
		
		//Validacion de query
		boolean validaDifInventario1 = difInventario1_result .isEmpty();


			testCase.addQueryEvidenceCurrentStep(difInventario1_result);
			testCase.addQueryEvidenceCurrentStep(difInventario2_result);



		System.out.println(validaDifInventario1);

		assertFalse(validaDifInventario1, "No se obtuvo el credito la sumatoria del  movimiento Salida x venta al costo bienes excluidos");
		
		
//******************************************* Paso 9 **********************************************************************
		
				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 0%'");
				
				//Parte1
				String difInventarioTaza0_1_format = String.format(difInventarioTaza0_1, "Salidas por venta al costo tasa 0%" , reference3 );
				
				System.out.println(difInventarioTaza0_1_format);
				
				SQLResult difInventarioTaza0_1_result = executeQuery(dbEbs, difInventarioTaza0_1_format);
				
				//Parte 2
		        String difInventarioTaza0_2_format = String.format(difInventarioTaza0_2,"Salidas por venta al costo tasa 0%", reference3 );
				
				System.out.println(difInventarioTaza0_2_format);
				
				SQLResult  difInventarioTaza0_2_result = executeQuery(dbEbs, difInventarioTaza0_2_format);
				
				//Validacion de query
				boolean validaDifInventarioTaza0 = difInventarioTaza0_1_result.isEmpty();

				

					testCase.addQueryEvidenceCurrentStep(difInventarioTaza0_1_result);
					testCase.addQueryEvidenceCurrentStep(difInventarioTaza0_2_result);

		

				System.out.println(validaDifInventarioTaza0 );

			assertFalse(validaDifInventarioTaza0 , "No se obtuvo el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 0%'");

//******************************************* Paso 10 **********************************************************************
				
				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 19%'");
				
				//Parte1
				String difInventarioTaza5_1_format = String.format(difInventarioTaza5_1, "Salidas por venta al costo tasa 19%", reference3 );
				
				System.out.println(difInventarioTaza5_1_format);
				
				SQLResult difInventarioTaza5_1_result = executeQuery(dbEbs, difInventarioTaza5_1_format);
				
				//Parte 2
		        String difInventarioTaza5_2_format = String.format(difInventarioTaza5_2,"Salidas por venta al costo tasa 19%",  reference3 );
				
				System.out.println(difInventarioTaza5_2_format);
				
				SQLResult  difInventarioTaza5_2_result = executeQuery(dbEbs, difInventarioTaza5_2_format);
				
				//Validacion de query
				boolean validaDifInventarioTaza5 = difInventarioTaza5_1_result.isEmpty();

				

					testCase.addQueryEvidenceCurrentStep(difInventarioTaza5_1_result);
					testCase.addQueryEvidenceCurrentStep(difInventarioTaza5_2_result);

				

				System.out.println(validaDifInventarioTaza5 );

				assertFalse(validaDifInventarioTaza5 , "No se obtuvo el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 19%'");
				
//******************************************* Paso 11 **********************************************************************
				
				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 5%'");
				
				//Parte1
				String difInventarioTazaICO_1_format = String.format(difInventarioTazaICO_1,"Salidas por venta al costo tasa 5%", reference3 );
				
				System.out.println(difInventarioTazaICO_1_format);
				
				SQLResult difInventarioTazaICO_1_result = executeQuery(dbEbs, difInventarioTazaICO_1_format);
				
				//Parte 2
		        String difInventarioTazaICO_2_format = String.format(difInventarioTazaICO_2, "Salidas por venta al costo tasa 5%", reference3 );
				
				System.out.println(difInventarioTazaICO_2_format);
				
				SQLResult difInventarioTazaICO_2_result = executeQuery(dbEbs, difInventarioTazaICO_2_format);
				
				//Validacion de query
				boolean ValidaDifInventarioTazaICO = difInventarioTazaICO_1_result.isEmpty();

				

					testCase.addQueryEvidenceCurrentStep(difInventarioTazaICO_1_result);
					testCase.addQueryEvidenceCurrentStep(difInventarioTazaICO_2_result);

				

				System.out.println(ValidaDifInventarioTazaICO);

			assertFalse(ValidaDifInventarioTazaICO, "No se obtuvo el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa 5%'");

//******************************************* Paso 12 **********************************************************************
			
			addStep("Validar que se encuentra en el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa ICO 8%'");
			
			//Parte1
			String salidaXVentaAlCosto1_format = String.format(salidaXVentaAlCosto1,"Salidas por venta al costo tasa ICO 8%",reference3 );
			
			System.out.println(salidaXVentaAlCosto1_format);
			
			SQLResult salidaXVentaAlCosto1_result = executeQuery(dbEbs, salidaXVentaAlCosto1_format);
			
			//Parte 2
	        String salidaXVentaAlCosto2_format = String.format(salidaXVentaAlCosto2,"Salidas por venta al costo tasa ICO 8%", reference3 );
			
			System.out.println(salidaXVentaAlCosto2_format);
			
			SQLResult salidaXVentaAlCosto2_result = executeQuery(dbEbs, salidaXVentaAlCosto2_format);
			
			//Validacion de query
			boolean ValidaSalidaXVentaAlCosto1 = difInventarioTazaICO_1_result.isEmpty();

			

				testCase.addQueryEvidenceCurrentStep(salidaXVentaAlCosto1_result);
				testCase.addQueryEvidenceCurrentStep(salidaXVentaAlCosto2_result);

			

			System.out.println(ValidaSalidaXVentaAlCosto1);

		assertFalse(ValidaSalidaXVentaAlCosto1, "No se obtuvo en el credito la sumatoria del  movimiento 'Salidas por venta al costo tasa ICO 8%'");





	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MCT-FT-001_Generar Poliza con movimientos de venta - Bogota";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
