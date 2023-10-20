package interfaces.jobs;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_002_Ejecuta_JobsArray extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_Ejecuta_JobsArray_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilería   s
		 *********************************************************************/
		testCase.setPrerequisites(data.get("prerequicitos"));
		 testCase.setTest_Description(data.get("Caso"));
		/*
		 * paso 1 
		 */
		addStep("ir a Jobs de la carpeta TQA_BAJO_DEMANDA");
		addStep("Ejecutar JOB ENVIA_CORREO_PORT_SAT de Ctrl-M ");
		/*
		 * SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		 * 
		 * 
		 * JSONArray array = new JSONArray(data.get("jobs"));
		 * 
		 * addStep("Jobs en  Control M "); Control_mInicio CM = new Control_mInicio(u,
		 * data.get("user"), data.get("ps")); //testCase.addPaso("Paso con addPaso");
		 * addStep("Login"); u.get(data.get("server")); u.hardWait(40);
		 * u.waitForLoadPage(); CM.logOn(); // pruebaTest prueba = new pruebaTest(); //
		 * prueba.funcionX(this); // testCase.failStep("Fallo por fuerza mayor");
		 * addStep("Inicio de job "); JobManagement j = new JobManagement(u, testCase,
		 * array); String resultadoEjecucion = j.jobRunner(); //
		 * addStep("Resultado de la ejecucion ");
		 * logger.info("Resultado de la ejecucion -> " + resultadoEjecucion);
		 * System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		 * 
		 * 
		 * u.hardWait(30); Boolean casoPasado = true;
		 * if(resultadoEjecucion.equals("Failure")) { casoPasado = false; }
		 * 
		 * // u.close(); assertTrue(casoPasado);
		 */
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
		return "QA AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_Ejecuta_JobsArray_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;

	}
}
