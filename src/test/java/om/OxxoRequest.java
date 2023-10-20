package om;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import utils.webmethods.GetRequest;

public class OxxoRequest {

	private String requestURL;
	
	private final String REQ_PART_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"	<TPEDoc version=\"1.0\">\r\n" + 
			"		<header ";
	
	private HashMap<String, String> headers;
	
	private final String REQ_PART_2 = "/> \r\n" + 
			"		<request> \r\n\t\t\t" ;

	private HashMap<String, String> services;
	
	private HashMap<String, String> servicesAut;
	
	private final String REQ_PART_3 = "\r\n" + 
			"		</request> \r\n" + 
			"	</TPEDoc>";
		
	private String ackValue;
	
	private ArrayList<Document> docs;
	
	private HashMap<String, String> values;
	
	public OxxoRequest(String requestURL) {
		this.requestURL = requestURL;
		headers = new HashMap<>();
		values = new HashMap<>();
		services = new HashMap<>();
		servicesAut = new HashMap<>();
		docs = new ArrayList<>();
	}

	public OxxoRequest(String requestURL, HashMap<String, String> headers, HashMap<String, String> services) {
		super();
		this.requestURL = requestURL;
		this.headers = headers;
		this.services = services;
		docs = new ArrayList<>();
	}
	
	public OxxoRequest(String requestURL, HashMap<String, String> values) {
		super();
		this.requestURL = requestURL;
		this.values = values;
	}
	
	public DocUtil addDoc() {
		return new DocUtil(addNewDocument());
		
	}
	
	
	public Document addNewDocument() {
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
    
		try {
			  documentBuilder = documentFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			  e.printStackTrace();
		}
        Document doc = documentBuilder.newDocument();
        addDocumentToRequest(doc);
        return doc;
	}
	
	public void addDocumentToRequest(Document doc) {
		this.docs.add(doc);
	}

	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}
	
	public void addValue(String key, String value) {
		this.values.put(key, value);
	}
	
	public void addService(String key, String value) {
		this.services.put(key, value);
	}
	
	public void addServiceAut(String key, String value) {
		this.servicesAut.put(key, value);
	}
	
	public void addAck(String ackValue) {
		this.ackValue = ackValue;
	}
	
	
    public String getRequestValuesString() {		
    	
		String request;		
		request = requestURL ;

		int i = values.size();
		for (Entry<String, String> entry : values.entrySet()) {
			
			String key = entry.getKey();
			String value = entry.getValue();
			
			if(i!=values.size()){
				request += "&" ;			
			}
		
			request += key + "=" + value ;
			
			i--;
		}
		
		return request;
	}
	

	public String getRequestString() {
		
		String request;		
		request = requestURL + "\n";		
		request += REQ_PART_1;
		
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();			
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}
		
		request += REQ_PART_2;
		
		if (!docs.isEmpty()) {
			for (Document document : docs) {				
				String xmlString = xmlDocToString(document);
				request += xmlString;				
			}
		}
		
		if (ackValue!=null) {
			String ackString = "\r\n" + 
					"  			<ack value=\""+ackValue+"\"/>\r\n" + 
					" 		";
			request += ackString;
		}		
		
		if (!services.isEmpty()) {
			request += "<service ";
			for (Entry<String, String> entry : services.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();				
				request += "\n\t\t\t" + key + " = \"" + value + "\" ";
			}
			request += " />";			
		} else if (!servicesAut.isEmpty())	{
			request += "<serviceAut ";
			for (Entry<String, String> entry : servicesAut.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();				
				request += "\n\t\t\t" + key + " = \"" + value + "\" ";
			}
			request += " />";
			
		}
		
		request += REQ_PART_3;		
		return request;
	}
	
	public String makeRequestString() {
		
		String request;		
		request = requestURL + "\n";		
		request += REQ_PART_1;
		
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();			
			request += "\n\t\t          " + key + " = \"" + value + "\" ";
		}
		
		request +=  "/> \r\n";
		
		if (!docs.isEmpty()) {
			for (Document document : docs) {				
				String xmlString = xmlDocToString(document);
				request += "\n\t\t" + xmlString;				
			}
		}
		
		if (ackValue!=null) {
			String ackString = "\r\n" + 
					"  			<ack value=\""+ackValue+"\"/>\r\n" + 
					" 		";
			request += ackString;
		}		
		
		if (!services.isEmpty()) {
			request += "<service ";
			for (Entry<String, String> entry : services.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();				
				request += "\n\t\t\t" + key + " = \"" + value + "\" ";
			}
			request += " />";			
		}		
		
		request += "\n	</TPEDoc>";		
		return request;
	}
	
	public String xmlDocToString(Document doc) {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		String xmlString = null;
		
		doc.getFirstChild();
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			StringWriter writer = new StringWriter();
	        //transform document to string 
	        transformer.transform(new DOMSource(doc), new StreamResult(writer));
	        xmlString = writer.getBuffer().toString();   	        
	        
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return xmlString;
	}
	
	public String executeRequest() throws ClientProtocolException, IOException{
		return GetRequest.executeGetRequest(getRequestString());
	}
	
	public String executeRequestValues() throws ClientProtocolException, IOException{
		return GetRequest.executeGetRequest(getRequestValuesString());
	}
	
	public String executeRequest(String request) throws ClientProtocolException, IOException{
		return GetRequest.executeGetRequest(request);
	}
	
	public static String executeRequestDisableSSL(String request) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		request = request.replace("\n", "").replace("\r", "");
		String[] splitUrl = request.split("jsonIn=");
		request = splitUrl[0] + "jsonIn=" + URLEncoder.encode(splitUrl[1], "UTF-8"); 

		HttpClient httpClient = HttpClients
	            .custom()
	            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
	            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	            .build();
		
		
		HttpGet requestHttp = new HttpGet(request);
		HttpResponse response = httpClient.execute(requestHttp);
		
		HttpEntity entity = response.getEntity();
		
		String result = null;
		
		if (entity != null) {
			  result = EntityUtils.toString(response.getEntity());
        }
		
		return result;
	}
	
	//Getters and setters
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public HashMap<String, String> getServices() {
		return services;
	}

	public void setServices(HashMap<String, String> services) {
		this.services = services;
	}	
	
	public static void main(String[] args) throws ParserConfigurationException, ClientProtocolException, IOException {
		String baseUrlRequest = "http://%s/invoke/TPE.MUN.Pub/request?xmlIn=";
		String urlRequest = String.format(baseUrlRequest, "10.184.40.110:8900");
		
		OxxoRequest olsRequest = new OxxoRequest(urlRequest);
		olsRequest.addHeader("application", "MUN");
		olsRequest.addHeader("entity", "PRD");
		olsRequest.addHeader("operation", "QRY01");
		olsRequest.addHeader("source", "POS");
		olsRequest.addHeader("plaza", "10MON");
		olsRequest.addHeader("tienda", "50QTE");
		olsRequest.addHeader("caja", "1");
		olsRequest.addHeader("adDate", "20200414");
		olsRequest.addHeader("pvDate", "20200414000000");
		
		olsRequest.addDoc()
			.addElement("transfer")
				.addAtribute("amount", "1000")
				.addAtribute("type", "ENV")
				.addAtribute("telefono", "8100000001")
			.addElement("client")
				.addAtribute("nombre", "jose luis")
				.addAtribute("apellidoPaterno", "valenzuela")
		;
		
		
		
//		olsRequest.addService("id", "2101655006");
//		olsRequest.addService("idClient", "0600034433");
		
		//olsRequest.addAck("00");
		

//        Document doc = olsRequest.addNewDocument();
//        
//        Element transfer = doc.createElement("transfer");
//        doc.appendChild(transfer);
//        
//        Attr amount = doc.createAttribute("amount");
//        amount.setValue("1000");
//        transfer.setAttributeNode(amount);
//        
//        Attr type = doc.createAttribute("type");
//        type.setValue("ENV");
//        transfer.setAttributeNode(type);
//        
//        Attr telefono = doc.createAttribute("telefono");
//        type.setValue("8100000001");
//        transfer.setAttributeNode(telefono);
//        
//        Element client = doc.createElement("client");
//        transfer.appendChild(client);
//        
//        Attr nombre = doc.createAttribute("nombre");
//        nombre.setValue("8100000001");
//        client.setAttributeNode(nombre);
        
        
        
        String rep =GetRequest.executeGetRequest("http://10.184.40.110:8890/invoke/PE4v2.Pub/runQueryTdc?folio=226621&adminDate=20161107&tarjeta=4766840000004296");
        System.out.println(rep);

		String request = olsRequest.makeRequestString();
		
		System.out.println(request);
	}
	
}
