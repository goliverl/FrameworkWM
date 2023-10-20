package test;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;

public class TestScript extends BaseExecution{
	
	@Test(dataProvider = "data-provider", enabled = true)
	public void TestScript_test(HashMap<String, String> data) throws Exception {
		
		addStep("paso1");
		testCase.addTextEvidenceCurrentStep("evidencia1 " + data.get("usuario"));
		
	}

	@Override
	public String setTestFullName() {
		return "TestScript";
	}

	@Override
	public String setTestDescription() {
		return "Ejecucion test";
	}

	@Override
	public String setTestDesigner() {
		return "Roberto Flores";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public String setPrerequisites() {
		return "Prerequisitos";
	}

	@Override
	public void beforeTest() {
// TODO Auto-generated method stub
	}

	

}
