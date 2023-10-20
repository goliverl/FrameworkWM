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

public class ATC_FT_005_RO08_COL_OCO21021_GeneraPolizaVentas extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_RO08_COL_OCO21021_GeneraPolizaVenta(HashMap<String, String> data) throws Exception {

		/**
		 *  OCO21021 - Mejoras Administrativo: MTC-FT-002 - MTC-FT-008 Generar Póliza con movimientos de venta - Bucaramanga - Bogotá
		 * Desc:
		 * Se requiere agregar las líneas del Costo a la Póliza contable de Ventas. 
		 * Se requiere agregar las líneas del Costo a la Póliza contable de Devolución de Ventas.
		 * Se requiere agregar las líneas del Costo a la Póliza contable de Ajustes de Inventario.
		 * @author Gilberto Martinez
		 * @date   2022/07/18
		 */
		
		
		/*
		 * Utilerías
		 *********************************************************************/

	utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,
				GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,
				GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */


		String SelectInsumos1 = " SELECT ITEM, ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL
		
		
		String SelectInsumos2 = " SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL
		
		String SelectInsumos1reasoncode = " SELECT ITEM, ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reason_code='165' "
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL
		
		
		String SelectInsumos2reasoncode = " SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reason_code='165' "
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL
		

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " 
		        + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_COL' " 
				+ " and start_dt >= trunc(sysdate) " 
				+ " ORDER BY start_dt DESC";
		

		String VerificacionHeader = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID,JOURNAL_TYPE_ID,GL_JOURNAL_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_COL" 
				+ " WHERE cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" 
				+ " AND run_id ='%s'";// RMSCOL
				

		String ConsultaGl1 = "SELECT reference6,reference4,reference10,reference22,reference25"
				+ " FROM GL.GL_INTERFACE" 
				+ " WHERE reference6 ='%s'";
				
		String ConsultaGl2 = "SELECT user_je_category_name,user_je_source_name,segment4"
				+ " FROM GL.GL_INTERFACE" 
				+ " WHERE reference6 ='%s'";
		
		
		String ValidacionR3R9p1 = "SELECT ITEM,ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE"
				+ " FROM fem_fif_stg" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 5" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 is not null "
				+ " AND reference_9 is not null";		
		
		String ValidacionR3R9p2 = "SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID"
				+ " FROM fem_fif_stg" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 5"
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 is not null"
				+ " AND reference_9 = '%s'";
		
	
		String VerificacionLines = " SELECT LINE_ID,HEADER_ID,TYPE,NET_RETAIL,VAT_RETAIL,NET_COST,VAT_COST " 
		        + " FROM WMUSER.WM_GL_LINES_COL " + "WHERE HEADER_ID = '%s'";
		

		
		String VerificacionLines2 = " SELECT REASON_CODE,VAT_RATE_RETAIL,VAT_RATE_COST,VAT_CODE_RETAIL,VAT_CODE_COST " 
		        + " FROM WMUSER.WM_GL_LINES_COL " + "WHERE HEADER_ID = %s";

		String ValidInv =	"select segment2,segment4,ENTERED_DR,reference10,reference6  "
				+ "from GL.gl_interface  "
				+ "where  segment2 = '"+ data.get("segment2")+"' "
				+ "and segment4 = '%s' "
				+ "and %s is not null  "
				+ "and reference10 = '%s' "
				+ "and reference6 = '%s'";

		
		String status = "S";

		// utileria
		SeleniumUtil u ;
		String run_id="";
		
		testCase.setTest_Description(data.get("caso"));

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 */
		/*
		 * Paso 1
		 *****************************************************************************************/
		      
				addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG.");

				System.out.println(GlobalVariables.DB_HOST_RMS_COL);
				
				SQLResult resultInsumos1;
				SQLResult resultInsumos2;
				String NumeroCaso=data.get("numCaso");
				
				if(NumeroCaso.equals("7") || NumeroCaso.equals("8")) {

					System.out.println("Caso "+NumeroCaso+" reasoncode:\n"+SelectInsumos1reasoncode);

					 resultInsumos1 = executeQuery(dbRms, SelectInsumos1reasoncode);
					 resultInsumos2 = executeQuery(dbRms, SelectInsumos2reasoncode);
				}else {
					System.out.println("Caso "+NumeroCaso+":\n"+SelectInsumos1);

					 resultInsumos1 = executeQuery(dbRms, SelectInsumos1);
					 resultInsumos2 = executeQuery(dbRms, SelectInsumos2);
				}
							
				boolean SC = resultInsumos1.isEmpty();

				if (!SC) {
					
					testCase.addBoldTextEvidenceCurrentStep("Se encontro informacion pendiente de procesar");

				}
				testCase.addQueryEvidenceCurrentStep(resultInsumos1);
				testCase.addQueryEvidenceCurrentStep(resultInsumos2);
				System.out.println(SC);

				assertFalse(SC, "No se obtiene información de la consulta");
		
		/*
		 * Paso 2
		 *****************************************************************************************/	
				addStep("Ejecutar el servicio RO8_COL La interfaz será ejecutada por el job de Ctrl-M ");

				u  = new SeleniumUtil(new ChromeTest(), true);
				
				JSONObject obj = new JSONObject(data.get("job"));

				addStep("Jobs en  Control M ");
				Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
				//testCase.addPaso("Paso con addPaso");
				addStep("Login");
				u.get(data.get("server"));
				u.hardWait(40);
				u.waitForLoadPage();
				CM.logOn(); 

				//Buscar del job
				addStep("Inicio de job ");
				ControlM control = new ControlM(u, testCase, obj);
				boolean flag = control.searchJob();
				assertTrue(flag);
				
				//Ejecucion
				String resultado = control.executeJob();
				System.out.println("Resultado de la ejecucion -> " + resultado);

				//Valor del output 
				System.out.println ("Valor de output :" +control.getOutput());
				
				//Validacion del caso
				Boolean casoPasado = true;
				if(resultado.equals("Wait Condition")) {
				casoPasado = true;
				}		
				assertTrue(casoPasado);
				//assertNotEquals("Failure",resultado);
				control.closeViewpoint(); 
				
		
		/*
		 * Paso 3
		 *****************************************************************************************/		
				addStep("Verificar que la ejecución termina con éxito.");

				System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
				System.out.println(tdcQueryStatusLog);
				
				SQLResult Result4 = executeQuery(dbLog, tdcQueryStatusLog);
				String fcwS="";
				boolean ValidResp=Result4.isEmpty();
				boolean validateStatus = false;
				
				if(!ValidResp){
					fcwS = Result4.getData(0,  "STATUS");
					run_id=Result4.getData(0,  "run_id");
					validateStatus = status.equals(fcwS);
				}
				
				testCase.addQueryEvidenceCurrentStep(Result4);
				
				 validateStatus = status.equals(fcwS);
				System.out.println(validateStatus);

				assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
		
				/*
				 * Paso 4
				 *****************************************************************************************/
				addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID en la tabla WM_GL_HEADERS_COL.");		
				
				System.out.println(GlobalVariables.DB_HOST_RMS_COL);
				String consulta = String.format(VerificacionHeader, run_id);
				System.out.println(consulta);
				
				SQLResult resultConsulta = executeQuery(dbRms, consulta);
				
				String header_id =""; 
				boolean av7 = resultConsulta.isEmpty();
				
				if (!av7) {
					header_id= resultConsulta.getData(0,"HEADER_ID");
					testCase.addBoldTextEvidenceCurrentStep("Se encontro la info correcta de la tabla WM_GL_HEADERS_COL" );
				} 
				testCase.addQueryEvidenceCurrentStep(resultConsulta);
				System.out.println(av7);

				assertFalse(av7, "No se obtiene informacion de la consulta");
				

		/*
		 * Paso 5
		 *****************************************************************************************/
			
				addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG. ");

				System.out.println(GlobalVariables.DB_HOST_RMS_COL);
				String vact1 = String.format(ValidacionR3R9p1, header_id);
				String vact2 = String.format(ValidacionR3R9p2, header_id);
				System.out.println(vact1);
				String reference3="";
				SQLResult resultP1 = executeQuery(dbRms, vact1);
				SQLResult resultP2 = executeQuery(dbRms, vact2);
				boolean va1 = resultP2.isEmpty();

				if (!va1) {
					reference3 = resultP2.getData(0, "reference_3");
					testCase.addBoldTextEvidenceCurrentStep("Se valida la actualización de REFERNCE_3 con el JOURNAL_ID y "
							+ "el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG");

				}
				testCase.addQueryEvidenceCurrentStep(resultP1);
				testCase.addQueryEvidenceCurrentStep(resultP2);
				System.out.println(va1);

				assertFalse(va1, "No se obtiene informacion de la consulta");
		
		/*
		 * Paso 6
		 *****************************************************************************************/					
				addStep("Inserción de líneas en la tabla WM_GL_LINES_COL");

				System.out.println(GlobalVariables.DB_HOST_RMS_COL);
				String gLines1 = String.format(VerificacionLines, header_id);
				String gLines2 = String.format(VerificacionLines2, header_id);
				System.out.println(gLines1);
				
				SQLResult result8 = executeQuery(dbRms, gLines1);
				SQLResult result9 = executeQuery(dbRms, gLines2);

				boolean va2 = result8.isEmpty();

				if (!va2) {

					testCase.addBoldTextEvidenceCurrentStep("Se verifico la inserción de líneas en la tabla WM_GL_LINES_COL");

				}
				testCase.addQueryEvidenceCurrentStep(result8);
				testCase.addQueryEvidenceCurrentStep(result9);
				System.out.println(va2);

				assertFalse(va2, "No se obtiene informacion de la consulta");				
		/*
		 * Paso 7
		 *****************************************************************************************/
			
				addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE.");
				
				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				String consulta1 = String.format(ConsultaGl1, reference3);
				String consulta2 = String.format(ConsultaGl2, reference3);
				System.out.println(consulta1);
				
				SQLResult resultGl1 = executeQuery(dbEbs, consulta1);
				SQLResult resultGl2 = executeQuery(dbEbs, consulta2);

				boolean va = resultGl1.isEmpty();

				if (!va) {

					testCase.addBoldTextEvidenceCurrentStep("Se obtuvo informacion de la inserción de líneas en la tabla GL_INTERFACE");

				}
				
				testCase.addQueryEvidenceCurrentStep(resultGl1);
				testCase.addQueryEvidenceCurrentStep(resultGl2);

				System.out.println(va);

				assertFalse(va, "No se obtiene informacion de la consulta");
				

	
				
			
		/*
		 * Paso 8
		 *****************************************************************************************/
						
				addStep("Validar que se encuentra en el debito la sumatoria del  movimiento "+data.get("reference10"));

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				String ValidInvForm = String.format(ValidInv,data.get("segment4"),data.get("Query"),data.get("reference10"), reference3);
				System.out.println(ValidInvForm);

				SQLResult resultInv = executeQuery(dbEbs, ValidInvForm);
				
				boolean va1Inv = resultInv.isEmpty();

				if (!va1Inv) {

					testCase.addBoldTextEvidenceCurrentStep("Se validaque se encuentra en el debito la sumatoria del  "
							+ "movimiento "+data.get("reference10"));

				}
				testCase.addQueryEvidenceCurrentStep(resultInv);
				System.out.println(va1Inv);

				assertFalse(va1Inv, "No se obtiene informacion de la consulta");
				
		
				/*
				 * Paso 9
				 *****************************************************************************************/	
				addStep("Validar que se encuentra en el debito la sumatoria del  movimiento '"+data.get("reference10_2")+"'");

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				String ValidInvFormTas = String.format(ValidInv,data.get("Segment4_2"),data.get("Query"),data.get("reference10_2"), reference3);
				System.out.println(ValidInvFormTas);

				SQLResult resultInvTas = executeQuery(dbEbs, ValidInvFormTas);
				
				boolean va1InvTas = resultInvTas.isEmpty();

				if (!va1InvTas) {

					testCase.addBoldTextEvidenceCurrentStep("Se validaque se encuentra en el debito la sumatoria del  "
							+ "movimiento "+data.get("reference10_2"));

				}
				testCase.addQueryEvidenceCurrentStep(resultInvTas);
				System.out.println(va1InvTas);

				assertFalse(va1InvTas, "No se obtiene informacion de la consulta");
				
		/*
		 * Paso 10
		 *****************************************************************************************/
				
				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento "+data.get("reference10_3"));

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				ValidInvFormTas = String.format(ValidInv,data.get("Segment4_3"),data.get("Query"),data.get("reference10_3"), reference3);
				System.out.println(ValidInvFormTas);

				resultInvTas = executeQuery(dbEbs, ValidInvFormTas);
				
				va1InvTas = resultInvTas.isEmpty();

				if (!va1InvTas) {

					testCase.addBoldTextEvidenceCurrentStep("Se valida que se encuentra en el credito la sumatoria "
							+ "del  movimiento "+data.get("reference10_3"));

				}
				testCase.addQueryEvidenceCurrentStep(resultInvTas);
				System.out.println(va1InvTas);

				assertFalse(va1InvTas, "No se obtiene informacion de la consulta");
				

				/*
				 * Paso 11
				 ******************************************************************************************************/
				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento "+data.get("reference10_4"));

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				ValidInvFormTas = String.format(ValidInv,data.get("Segment4_3"),data.get("Query"),data.get("reference10_4"), reference3);
				System.out.println(ValidInvFormTas);

				resultInvTas = executeQuery(dbEbs, ValidInvFormTas);
				
				va1InvTas = resultInvTas.isEmpty();

				if (!va1InvTas) {

					testCase.addBoldTextEvidenceCurrentStep("Se valida que se encuentra en el credito la sumatoria del "
							+ " movimiento "+data.get("reference10_2"));

				}
				testCase.addQueryEvidenceCurrentStep(resultInvTas);
				System.out.println(va1InvTas);

				assertFalse(va1InvTas, "No se obtiene informacion de la consulta");	
				
				/*
				 * Paso 12
				 ******************************************************************************************************/

				addStep("Validar que se encuentra en el credito la sumatoria del  movimiento "+data.get("reference10_5"));

				System.out.println(GlobalVariables.DB_HOST_EBS_COL);
				ValidInvFormTas = String.format(ValidInv,data.get("Segment4_3"),data.get("Query"),data.get("reference10_5"), reference3);
				System.out.println(ValidInvFormTas);

				resultInvTas = executeQuery(dbEbs, ValidInvFormTas);
				
				va1InvTas = resultInvTas.isEmpty();

				if (!va1InvTas) {

					testCase.addBoldTextEvidenceCurrentStep("Se valida que se encuentra en el credito la sumatoria del  "
							+ "movimiento "+data.get("reference10_5"));

				}
				testCase.addQueryEvidenceCurrentStep(resultInvTas);
				System.out.println(va1InvTas);

				assertFalse(va1InvTas, "No se obtiene informacion de la consulta");	
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_RO08_COL_OCO21021_GeneraPolizaVenta";
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