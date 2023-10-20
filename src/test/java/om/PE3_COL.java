package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.webmethods.GetRequest;

public class PE3_COL {
	public String requestUrlFolio = "http://%s:%s@%s/invoke/PE3_COL.Pub/runGetFolio?plaza=%s&tienda=%s&caja=%s";
	public String RequestUrlActiv="http://%s:%s@%s/invoke/PE3_COL.Pub/runGetAuth?folio=%s&pvDate=%s&adDate=%s&cardNo=%s&amount=%s&upc=%s&currency=%s";
	public String RequestAck="http://%s:%s@%s/invoke/PE3_COL.Pub/runGetAuthAck?folio=%s&ack=%s";
//	public String RequestReverse="http://%s:%s@%s/invoke/PE3_COL.Pub/runReverseManager";

	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;

	Date fecha = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); 
	
	public PE3_COL(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	public String SolicitudFolio() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestUrlFolio, data.get("user"), data.get("pass"), data.get("host"),
													data.get("plaza"),data.get("tienda"),data.get("caja"));
		System.out.println(url);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	

		public String SolicitudActiv(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
			String date = formatter.format(fecha);
			
	
		String url = String.format(RequestUrlActiv, data.get("user"), data.get("pass"), data.get("host"),
													Folio,date,date.substring(0, 8),data.get("cardNo"),
													data.get("amount"),data.get("upc"),data.get("currency"));
		System.out.println(url);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
		
		

		public String RunGetAck(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
	
		String url = String.format(RequestUrlActiv, data.get("user"), data.get("pass"), data.get("host"),
													Folio,data.get("ack"));
		System.out.println(url);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
		
		
		public String RunReverse() throws ReempRequestException, ClientProtocolException, IOException {
			
			String url = String.format(RequestUrlActiv, data.get("user"), data.get("pass"), data.get("host"));
			System.out.println(url);
			System.out.println(url);

			String response = GetRequest.executeGetRequest(url);

			testCase.addTextEvidenceCurrentStep(url);
			testCase.addTextEvidenceCurrentStep(response);
			return response;

		}
		
}

