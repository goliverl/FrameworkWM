package om;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;



public class TPE_MUN {
	public String requestUrlQRY01 = "http://%s/invoke/TPE.MUN.Pub/request?xmlIn=";

	HashMap<String, String> data; 
	TestCase testCase;
	utils.sql.SQLUtil db;
	
//	public TPE_MUN(HashMap<String, String> data, TestCase testCase, SqlUtil db) {
	public TPE_MUN(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {

		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
	public String ejecutarConsulta() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrlQRY01, "10.184.40.110:5555");
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "MUN");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01"); 
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		
	
        
		Document doc = or.addNewDocument();
		Element carname = doc.createElement("record");
	    doc.appendChild(carname);
		
        Attr expediente = doc.createAttribute("expediente");
        expediente.setValue(data.get("expediente"));
        carname.setAttributeNode(expediente);
        
        
        Attr attrType2 = doc.createAttribute("estado");
        attrType2.setValue(data.get("estado"));
        carname.setAttributeNode(attrType2);
        
      
        Attr municipio = doc.createAttribute("municipio");
        municipio.setValue(data.get("municipio"));
        carname.setAttributeNode(municipio);
        
        
        
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		
		return response;
	}
	
	
	
	public String ejecutarQRY01() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestUrlQRY01, "10.184.40.110:5555");
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "MUN");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		
	
        
		Document doc = or.addNewDocument();
		Element carname = doc.createElement("record");
	    doc.appendChild(carname);
		
        Attr attrType = doc.createAttribute("municipio");
        attrType.setValue(data.get("municipio"));
        carname.setAttributeNode(attrType);
        
        
        Attr attrType2 = doc.createAttribute("estado");
        attrType2.setValue(data.get("estado"));
        carname.setAttributeNode(attrType2);
        
      
        Attr attrType3 = doc.createAttribute("expediente");
        attrType3.setValue(data.get("expediente"));
        carname.setAttributeNode(attrType3);
        
        
        
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		
		return response;
	}
	
	
}
