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

public class PR50V2 {

	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@%s/invoke/wm.tn:receive?$xmldata=%s";
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;

	public PR50V2(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db){
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

	
	public String EjecutarRunInboundREC() throws ReempRequestException, ClientProtocolException, IOException {

		OxxoRequest2 or = new OxxoRequest2(requestUrl);

		String request = or.getRequestString(data.get("PV_DOC_ID"), data.get("SENDER_ID"), data.get("DOC_TYPE"));
		String requestEnc = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, GlobalVariables.HOST_PR50v2, requestEnc);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

	public String EjecutarRunInbounTIC(String SENDER_ID, String DOC_TYPE, String PV_DOC_ID)
			throws ReempRequestException, ClientProtocolException, IOException {

		OxxoRequest2 or = new OxxoRequest2(requestUrl);

		String request = or.getRequestString(PV_DOC_ID, SENDER_ID, DOC_TYPE);
		String requestEnc = (URLEncoder.encode(request, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, GlobalVariables.HOST_PR50v2, requestEnc);
		System.out.println(request);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

//		HashMap<String, String> datosRequest = new HashMap<>();
//		datosRequest.put("host", GlobalVariables.HOST_PR50v2);
//		datosRequest.put("SENDER_ID", SENDER_ID);
//		datosRequest.put("DOC_TYPE", DOC_TYPE);
//		datosRequest.put("PV_DOC_ID", PV_DOC_ID);
//
//		String request = GetRequestFile.getRequestFile(runInboundTIC, datosRequest);
//		System.out.println(request);
//
//		String response = GetRequest.executeGetRequest(request);
//
//		testCase.addTextEvidenceCurrentStep(request);
//
//		return response;

	}

}
