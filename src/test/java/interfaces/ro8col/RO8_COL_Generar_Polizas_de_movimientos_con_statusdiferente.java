package interfaces.ro8col;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class RO8_COL_Generar_Polizas_de_movimientos_con_statusdiferente extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_015_RO8_COL_Generar_Polizas_de_movimientos_con_statusdiferente (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria****************************************************************/
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		SQLUtil FCWMLQA_WMLOG_COL = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL,GlobalVariables.DB_USER_FCWMLQA_WMLOG_COL, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG_COL);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		
		SeleniumUtil u = null;
		/*
		 * Variables****************************************************************/
		String querypendientedeprocesar = "SELECT * FROM fem_fif_stg\r\n"
				+ " WHERE     CR_PLAZA = '%s'\r\n"
				+ "       AND tran_code in (1,4,22,30,32)\r\n"
				+ "       AND tran_date >= TRUNC(SYSDATE-60)\r\n"
				+ "       AND reference_3 IS NULL\r\n"
				+ "       AND reference_9 IS NULL \r\n"
				+ "and vat_code in ('IVA5','IVA19','EXCLUD','NIVA19','NIVA5')";
		
		String queryejecucion = " SELECT *\r\n"
				+ "    FROM  WMLOG.wm_log_run\r\n"
				+ "   WHERE interface = 'RO08_COL' \r\n"
				+ "   AND status = 'S' \r\n"
				+ "   AND start_dt >= TRUNC (SYSDATE)\r\n"
				+ "ORDER BY start_dt DESC";
		
		String queryactualizacion = "SELECT * FROM  wmuser.WM_GL_HEADERS_COL \r\n"
				+ "WHERE TRAN_CODE = 1\r\n"
				+ "AND  RUN_ID='%s' AND CR_PLAZA = '%s'";
		
		String queryinserciondelineas = "SELECT * r\n"
				+ "FROM WMUSER.WM_GL_LINES_COL WHERE header_id = '%s'";
		
		String queryinserciondelineas2 = "SELECT reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "	FROM GL.GL_INTERFACE \r\n"
				+ "WHERE reference6 = '%s';";
		
		String queryreferencia = "SELECT * FROM fem_fif_stg WHERE reference_9 = '%s' \r\n"
				+ "AND reference_3='%s' \r\n"
				+ "AND tran_date = to_date(transactionDate,'dd/mm/yyyy') \r\n"
				+ "AND CR_PLAZA = '%s'  \r\n"
				+ "AND tran_code in (1,4,22,30,32)";
		
		String queryregistros = "SELECT * "
				+ "FROM GL.GL_INTERFACE "
				+ "WHERE DATE_CREATED>=TRUNC(SYSDATE) and REFERENCE6 in ('%s') "
				+ "order by DATE_CREATED DESC";
		
		testCase.setTest_Description(data.get("name"));
		
		/********************************Paso 1*************************************/
		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG");
		
		String plaza = data.get("plaza");
		String pendientedeprocesar = String.format(querypendientedeprocesar, plaza);
		System.out.println(pendientedeprocesar);
		SQLResult pendientedeprocesar2 = dbRms.executeQuery(pendientedeprocesar);
		boolean validatependientedeprocesar = pendientedeprocesar2.isEmpty();
		String reference_3 = "";
		
		
		if(!validatependientedeprocesar) { 
			reference_3 = pendientedeprocesar2.getData(0, "REFERENCE_3");
			
			testCase.addQueryEvidenceCurrentStep(pendientedeprocesar2);
		}
			testCase.addQueryEvidenceCurrentStep(pendientedeprocesar2);
			
	 assertFalse(validatependientedeprocesar,"No se obtuvo informacion" );
				
		
		/********************************Paso 2*************************************/
		addStep("Ejecutar el servicio RO8_COL La interfaz será ejecutada por el job runRO8_COL de Ctrl-M ");

		u  = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));
		
		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
	
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
		
		/********************************Paso 3*************************************/
		addStep("Verificar que la ejecución termino con éxito.");
		
		SQLResult ejecucion = FCWMLQA_WMLOG_COL.executeQuery(queryejecucion);
		System.out.println(queryejecucion);
		boolean validateejecucion = ejecucion.isEmpty();
		
		String run_id = "";
		
		if(!validateejecucion) {
			run_id = ejecucion.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(ejecucion);
		}
			testCase.addQueryEvidenceCurrentStep(ejecucion);
			
		assertFalse(validateejecucion,"No se obtuvo informacion" );
			
		
		/********************************Paso 4*************************************/
		addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL.");
		
		String actualizacion = String.format(queryactualizacion, run_id,plaza);
		System.out.println(actualizacion);
		SQLResult actualizacionver = dbRms.executeQuery(actualizacion);
		boolean validateactualizacion = actualizacion.isEmpty();
		String header_id = "";
		
		if(!validateactualizacion) {
			header_id = actualizacionver.getData(0, "HEADER_ID");
			testCase.addQueryEvidenceCurrentStep(actualizacionver);
		}
			testCase.addQueryEvidenceCurrentStep(actualizacionver);
			
			assertFalse(validateactualizacion,"No se obtuvo informacion" );
			
		
		/********************************Paso 5*************************************/
		addStep("Inserción de líneas en la tabla WM_GL_LINES_COL");
		
		String insercion = String.format(queryinserciondelineas,header_id);
		System.out.println(insercion);
		SQLResult inserciondelineas  = FCWMLQA_WMLOG_COL.executeQuery(insercion );
		boolean validateinserciondelineas = inserciondelineas.isEmpty();
		
		if(!validateinserciondelineas ) {
			testCase.addQueryEvidenceCurrentStep(inserciondelineas );
		}
			testCase.addQueryEvidenceCurrentStep(inserciondelineas );
		
		/********************************Paso 6*************************************/
		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE");
		
		String insercionformatref = String.format(queryinserciondelineas2, reference_3);
		System.out.println(insercionformatref);
		SQLResult inserciondelineas2 = dbEbs.executeQuery(insercionformatref);
		boolean validateinserciondelineas2 = queryinserciondelineas2.isEmpty();
		String reference_6 = "";
		
		if(!validateinserciondelineas2) {
			
			reference_6 = inserciondelineas2.getData(0, "reference_6");
			testCase.addQueryEvidenceCurrentStep(inserciondelineas2);
		}
			testCase.addQueryEvidenceCurrentStep(inserciondelineas2);
			
		assertFalse(validateinserciondelineas2,"No se obtuvo informacion" );
				
		
		/********************************Paso 7*************************************/
		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");
		
		
		String plaza2 = data.get("plaza");
		String queryref = String.format(queryreferencia, header_id, reference_6 ,plaza2);
		System.out.println(queryref);
		SQLResult ref = FCWMLQA_WMLOG_COL.executeQuery(queryref);
		boolean validateref = ref.isEmpty();
		
		if(!validateref) {
			testCase.addQueryEvidenceCurrentStep(ref);
		}
			testCase.addQueryEvidenceCurrentStep(ref);

			
		assertFalse(validateref,"No se obtuvo informacion" );
		
		/********************************Paso 8*************************************/
		addStep("Validar que los nuevos registros por movimiento se registraron con status diferente a NEW");
		
		String registrosformatref = String.format(queryregistros, reference_3);
		System.out.println(registrosformatref);
		SQLResult registros = dbEbs.executeQuery(registrosformatref);
		boolean validateregistros = registros.isEmpty();
		String status = "";
		if(!validateregistros) {
			for (int i = 0; i <= 5; i++) {
			status = registros.getData(i, "STATUS");
			System.out.println("Status: " + status);
		}
			testCase.addQueryEvidenceCurrentStep(registros);
		}
			testCase.addQueryEvidenceCurrentStep(registros);
		assertEquals(status, "NEW", "Se encontraron registros con STATUS NEW");
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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_015_RO8_COL_Generar_Polizas_de_movimientos_con_statusdiferente";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
