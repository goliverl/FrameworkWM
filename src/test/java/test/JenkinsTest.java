package test;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;

public class JenkinsTest extends BaseExecution {
	@Test(dataProvider = "data-providerOctane")
	public void ATC_FT_002_JenkinsTest(HashMap<String, String> data) throws Exception {
		addStep("Paso 1 prueba");
		addStep("Paso 2 prueba");
		testCase.addBoldTextEvidenceCurrentStep("EvidenciaCrrentStep");
		assertFalse(false);
	}

	@Override
	public String setTestFullName() {
// TODO Auto-generated method stub
		return "ATC_FT_002_JenkinsTest";
	}

	@Override
	public String setTestDescription() {
// TODO Auto-generated method stub
		return "Descripcion del caso de prueba";
	}

	@Override
	public String setTestDesigner() {
// TODO Auto-generated method stub
		return "Nombre del automatizador";
	}

	@Override
	public String setTestInstanceID() {
// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
// TODO Auto-generated method stub
		return "Prerequisitos";
	}

	@Override
	public void beforeTest() {
// TODO Auto-generated method stub

	}

}