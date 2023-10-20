package om;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;


public class TPE_BTC {

	public String requestxmlUrl = "http://%s/invoke/TPE.TSF.Pub/request?xmlIn=";
	public String requestUrl = "http://%s/invoke/TPE.TSF.Pub/request?";
	public String jsonPath = "TPE\\BTC\\json.txt";


	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	public TPE_BTC(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
	
	public String ejecutarQRY01() throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestxmlUrl, GlobalVariables.HOST_QA10_VIRTUAL);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "BTC");
		or.addHeader("entity", "BTC");
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("creationDate", data.get("pvDate"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("request")
			.addElement("transaction")
				.addAtribute("startDate", data.get("startDate"))
				.addAtribute("plaza", data.get("plaza"))
				.addAtribute("tienda", data.get("tienda"))
				.addAtribute("caja", data.get("caja"))
				.addAtribute("serviceType", data.get("serviceType"))
				.addAtribute("entity", data.get("entity"))
				.addAtribute("ticket", data.get("ticket"))
				.addAtribute("folioWM", data.get("folioWM"))
				.addAtribute("operation", data.get("operation"))
				.addAtribute("status", data.get("status"))
				.addAtribute("description", data.get("description"))
				.addAtribute("lastStatusDate", data.get("lastStatusDate"))
				.addAtribute("amount", data.get("amount"))
				.addAtribute("ipAddress", data.get("ipAddress"))
				.addAtribute("auth", data.get("auth"))
		;		
		
		String request = or.makeRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);			
		String response = or.executeRequest(request);		
		return response;				
	}
	
	public String ejecutarQRY02() throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestxmlUrl, GlobalVariables.HOST_QA10_VIRTUAL);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "BTC");
		or.addHeader("entity", "BTC");
		or.addHeader("operation", "QRY02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("creationDate", data.get("pvDate"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("request")
			.addElement("transaction")
				.addAtribute("posDate", data.get("posDate"))
				.addAtribute("message", data.get("message"))
				.addAtribute("plaza", data.get("plaza"))
				.addAtribute("tienda", data.get("tienda"))
				.addAtribute("caja", data.get("caja1"))
				.addAtribute("serviceType", data.get("serviceType"))			
				.addAtribute("operation", data.get("operation"))
				.addAtribute("total", data.get("total"))
			.addElement("transaction")
				.addAtribute("posDate", data.get("posDate2"))
				.addAtribute("message", data.get("message2"))
				.addAtribute("plaza", data.get("plaza"))
				.addAtribute("tienda", data.get("tienda"))
				.addAtribute("caja", data.get("caja2"))
				.addAtribute("serviceType", data.get("serviceType2"))			
				.addAtribute("operation", data.get("operation2"))
				.addAtribute("total", data.get("total2"))
		;		
		
		String request = or.makeRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);			
		String response = or.executeRequest(request);		
		return response;				
	}
		
}
