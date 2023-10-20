package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;


public class TPE_RTP {
	
	public String requestRTP = "http://%s/invoke/TPE.RTP.Pub/request?xmlIn=";

	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;
	
	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
																			// de
																			// la
																			// fecha
	String date = formatter.format(fecha);
	
	
	public TPE_RTP(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {

		
		super();
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		this.data = data;
		this.testCase = testCase;
		this.db=db;
		
	}
	
	public String ejecutarOPR01Llave() throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, GlobalVariables.HOST_RTP);
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entityk"));
		or.addHeader("operation", data.get("operation"));
		//or.addHeader("folio", data.get("folio"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));

		
	//	or.addService("proveedor", data.get("proveedor"));
	//	or.addService("reader", data.get("reader"));
		
		 Document doc = or.addNewDocument();
	       
	       Element etRequest = doc.createElement("proveedor");   
	       etRequest.appendChild(doc.createTextNode(data.get("proveedor")));
	       doc.appendChild(etRequest);
	              

	       Document doc1 = or.addNewDocument();
	       
	       Element etReader = doc1.createElement("reader");   
	       etReader.appendChild(doc1.createTextNode(data.get("reader")));
	       doc1.appendChild(etReader);
	       
	       
	
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
		
		
	}
	
	
	public String ejecutarOPR0RegistroDTL() throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, GlobalVariables.HOST_RTP);
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entityt"));
		or.addHeader("operation", data.get("operation"));

		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));
		
		
		
	       Document doc = or.addNewDocument();
	       
	     //  Element etRequest = doc.createElement("request");   
	     //  doc.appendChild(etRequest);
	              
	        Element etProveedor = doc.createElement("proveedor");   ;
	        etProveedor.appendChild(doc.createTextNode( data.get("proveedor")));
	        doc.appendChild(etProveedor);
	           
	        Document doc1 = or.addNewDocument();
	        Element etTxt = doc1.createElement("tx");   
	        Attr attr = doc1.createAttribute("tkt");
	        attr.setValue("0");
	        etTxt.setAttributeNode(attr);

	        doc1.appendChild(etTxt);

	        
	        Element etCard = doc1.createElement("card");   ;
	        etCard.appendChild(doc1.createTextNode(data.get("card")));
	        etTxt.appendChild(etCard);


	         Element etmonto = doc1.createElement("monto");   ;
	         etmonto.appendChild(doc1.createTextNode(data.get("monto")));
	         etTxt.appendChild(etmonto);

	          
	          Element etAuth = doc1.createElement("auth");   ;
	          etAuth.appendChild(doc1.createTextNode(data.get("auth")));
	          etTxt.appendChild(etAuth);
	             
	           Element etRef = doc1.createElement("ref");   ;
	           etRef.appendChild(doc1.createTextNode(data.get("ref")));
	           etTxt.appendChild(etRef);
		
		
		/*
		or.addService("proveedor", data.get("proveedor"));
		or.addService("tkt", data.get("tkt"));
		or.addService("card", data.get("card"));
		or.addService("monto", data.get("monto"));
		or.addService("auth", "auth");
		or.addService("ref", data.get("ref"));*/
		
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
		
		
	}
	
	public String ejecutarOPR01LlaveWMx86() throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, data.get("host"));
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RTP");
		or.addHeader("entity", data.get("entityLlave"));
		or.addHeader("operation", "OPR01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		
		Document doc = or.addNewDocument();
	       
        Element etRequest = doc.createElement("proveedor");   
        etRequest.appendChild(doc.createTextNode(data.get("proveedor")));
        doc.appendChild(etRequest);
              

        Document doc1 = or.addNewDocument();
       
        Element etReader = doc1.createElement("reader");   
        etReader.appendChild(doc1.createTextNode(data.get("reader")));
        doc1.appendChild(etReader);
	
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
	}
	
	public String ejecutarOPR01RecargaTarjeta() throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, data.get("host"));
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "RTP");
		or.addHeader("entity", data.get("entityRecarga"));
		or.addHeader("operation", "OPR01");

		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		
	    Document doc = or.addNewDocument();
	              
		Element etProveedor = doc.createElement("proveedor");   
		etProveedor.appendChild(doc.createTextNode( data.get("proveedor")));
		doc.appendChild(etProveedor);
		   
		Document doc1 = or.addNewDocument();
		Element etTxt = doc1.createElement("tx");   
		Attr attr = doc1.createAttribute("tkt");
		attr.setValue(data.get("tkt"));
		etTxt.setAttributeNode(attr);
		
		doc1.appendChild(etTxt);
		
		
		Element etCard = doc1.createElement("card");   ;
		etCard.appendChild(doc1.createTextNode(data.get("card")));
		etTxt.appendChild(etCard);
		
		
		Element etmonto = doc1.createElement("monto");   ;
		etmonto.appendChild(doc1.createTextNode(data.get("monto")));
		etTxt.appendChild(etmonto);
		
		  
		Element etAuth = doc1.createElement("auth");   ;
		etAuth.appendChild(doc1.createTextNode(data.get("auth")));
		etTxt.appendChild(etAuth);
		     
		Element etRef = doc1.createElement("ref");   ;
		etRef.appendChild(doc1.createTextNode(data.get("ref")));
		etTxt.appendChild(etRef);
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
	}
	
}
