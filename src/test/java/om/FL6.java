package om;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class FL6 {

	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/wm.tn:receive?$xmldata=%s";
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;

	public FL6(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data; 
		this.testCase = testCase;
		this.db = db;
	}

	
	public String FL6_Receive() throws ReempRequestException, ClientProtocolException, IOException {
  
		OxxoRequestFl6 or = new OxxoRequestFl6 (requestUrl);
		  
		String request = or.getRequestString(data.get("LID"),
				data.get("DOC"), 
				data.get("CDS"),  
				data.get("DAT"), 
				data.get("AID"), 
				data.get("TZ"), 
				data.get("LDID"), 
				data.get("TSf"),
				data.get("BOL"), 
				data.get("FAE"), 
				data.get("CE"), 
				data.get("CG"), 
				data.get("LDID1"), 
				data.get("TSF1"), 
				data.get("BOL1"), 
				data.get("FAE1"));
	String requestFL6 = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, data.get("server"), requestFL6);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

	
}
