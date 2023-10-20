package interfaces.jobs;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;


public class ATC_001_Jobs_Protocolo_Seguridad extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_001_Jobs_Protocolo_Seguridad_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/

        testCase.setPrerequisites(data.get("prerequicitos"));
        testCase.setTest_Description(data.get("Caso"));

		/*
		 * paso 1 
		 */
		
		addStep("Ir a Jobs de la carpeta TQA_BAJO_DEMANDA - Ejecutar JOB ProcAclaraciones de Ctrl-M ");
		
		testCase.addTextEvidenceCurrentStep("Ejecución Job: TQA_BAJO_DEMANDA" );
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		
		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(50);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(40);
		
		//Valor del output 
		

		String Res2 = control.getNewStatus();
		

		System.out.println ("Valor de output getNewStatus:" +Res2);
		
		
		String output = control.getOutput();
		System.out.println ("Valor de output control:" +output);
		
		testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);
		//Validacion del caso
		Boolean casoPasado = false;
		
		if(Res2.equals("Ended OK")) {
		casoPasado = true;
		}		
		
		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
	
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
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;

	}
}
