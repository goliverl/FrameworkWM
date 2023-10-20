package om;

import java.io.IOException;
import java.io.StringWriter;
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

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;

import utils.webmethods.GetRequest;

public class OxxoRequestTpe_Cines {

	private String request;
	private String requestURL;

	
	private final String REQ_PART_1_Qry01 = "<?xml version= \"1.0\" encoding = \"UTF-8\"?> \n"
			+ "<TPEDoc version= \"1.0\"> \n"
			+ "	<header application=\"%s\"\r\n"
			+ "	        entity=\"%s\"\r\n"
			+ "	        operation=\"%s\"\r\n"
			+ "	        source=\"%s\"\r\n"
			+ "	        plaza=\"%s\"\r\n"
			+ "	        tienda=\"%s\"\r\n"
			+ "	        caja=\"%s\"\r\n"
			+ "	        adDate=\"%s\"\r\n"
			+ "	        pvDate=\"%s\" \r\r/>";
	
	private final String REQ_PART_2_Qry01 = "<request>\r\n"
			+ "		<service opcion=\"%s\">\r\n"
			+ "	</request>\r\n"
			+ "	</TPEDoc>";
	
	private final String REQ_PART_2_Qry02 = "<request>\r\n"
			+ "		<service ciudad=\"%s\">\r\n"
			+ "	</request>\r\n"
			+ "	</TPEDoc>";
	
	private final String REQ_PART_1_TRN01 = "<?xml version= \"1.0\" encoding = \"UTF-8\"?> \n"
			+ "<TPEDoc version= \"1.0\"> \n"
			+ "	<header application=\"%s\" \r\n"
			+ "	        entity=\"%s\" \r\n"
			+ "	        operation=\"%s\" \r\n"
			+ "	        source=\"%s\" \r\n"
			+ "	        plaza=\"%s\" \r\n"
			+ "	        tienda=\"%s\" \r\n"
			+ "	        caja=\"%s\" \r\n"
			+ "	        adDate=\"%s\" \r\n"
			+ "	        pvDate=\"%s\" \r\n/>";
	
	private final String REQ_PART_2_TRN01 = "<request> \r\n"
			+ "	</request>\r\n"
			+ "	</TPEDoc> \r\n";

	private final String REQ_PART_1_TRN02 = "<?xml version= \"1.0\" encoding = \"UTF-8\"?> \n"
			+ "<TPEDoc version= \"1.0\"> \n"
			+ "	<header application=\"%s\" \r\n"
			+ "	        entity=\"%s\" \r\n"
			+ "	        operation=\"%s\" \r\n"
			+ "	        source=\"%s\" \r\n"
			+ "	        plaza=\"%s\" \r\n"
			+ "	        tienda=\"%s\" \r\n"
			+ "	        caja=\"%s\" \r\n"
			+ "	        adDate=\"%s\" \r\n"
			+ "	        pvDate=\"%s\" \r\n/>";
	
	private final String REQ_PART_2_TRN02 = "	<request> \r\n"
			+ "	<service payType = \"%s\"  \r\n"
			+ "	    IdEntidad = \"%s\"  \r\n"
			+ "	    IdCanal = \"%s\"  \r\n"
			+ "	    IdTermianl = \"%s\" \r\n"
			+ "		TipoVoucher = \"%s\" \r\n"
			+ "		ImportePVP = \"%s\" \r\n"
			+ "		IdOperacionInt = \"%s\" \r\n"
			+ "		NoEntrada = \"%s\"/> \r\n"
			+ "	</request> \r\n"
			+ "	</TPEDoc> \r\n";
	
	private final String REQ_PART_1_TRN03 = "<?xml version= \"1.0\" encoding = \"UTF-8\"?> \n"
			+ "<TPEDoc version= \"1.0\"> \n"
			+ "	<header application=\"%s\" \r\n"
			+ "	        operation=\"%s\" \r\n"
			+ "	        entity=\"%s\" \r\n"
			+ "	        folio=\"%s\" \r\n"
			+ "	        plaza=\"%s\" \r\n"
			+ "	        tienda=\"%s\" \r\n"
			+ "	        caja=\"%s\" \r\n"
			+ "	        adDate=\"%s\" \r\n/>";
	
	private final String REQ_PART_2_TRN03 = "<request> \r\n"
			+ "	<service \r\n"
			+ "	     <ack value = \"%s\"/> \r\n"
			+ "	</request> \r\n"
			+ "	</TPEDoc> \r\n";

	private final String REQ_PART_1_TRN04 = "<?xml version= \"1.0\" encoding = \"UTF-8\"?> \n"
			+ "<TPEDoc version= \"1.0\"> \n"
			+ "	<header application=\"%s\" \r\n"
			+ "	        operation=\"%s\" \r\n"
			+ "	        entity=\"%s\" \r\n"
			+ "	        folio=\"%s\" \r\n"
			+ "	        plaza=\"%s\" \r\n"
			+ "	        tienda=\"%s\" \r\n"
			+ "	        caja=\"%s\" \r\n"
			+ "	        pvDate=\"%s\" \r\n/>";
	
	private final String REQ_PART_2_TRN04 = "<request> \r\n"
			+ "	<service \r\n"
			+ "	     <ack value = \"%s\"/> \r\n"
			+ "	</request> \r\n"
			+ "	</TPEDoc> \r\n";

	
	
	private HashMap<String, String> headers;

	
	private HashMap<String, String> services;

	private final String REQ_PART_3 = " ";
	
	private String ackValue;

	private ArrayList<Document> docs;

	private HashMap<String, String> values;

	public OxxoRequestTpe_Cines(String requestURL) {
		this.requestURL = requestURL;
		headers = new HashMap<>();
		values = new HashMap<>();
		services = new HashMap<>();
		docs = new ArrayList<>();
	}

	public OxxoRequestTpe_Cines(String requestURL, HashMap<String, String> headers, HashMap<String, String> services) {
		super();
		this.requestURL = requestURL;
		this.headers = headers;
		this.services = services;
		docs = new ArrayList<>();
	}

	public OxxoRequestTpe_Cines(String requestURL, HashMap<String, String> values) {
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

	public void addAck(String ackValue) {
		this.ackValue = ackValue;
	}

	public String getRequestValuesString() {

		String request;
		request = requestURL;

		int i = values.size();
		for (Entry<String, String> entry : values.entrySet()) {

			String key = entry.getKey();
			String value = entry.getValue();

			if (i != values.size()) {
				request += "&";
			}

			request += key + "=" + value;

			i--;
		}

		return request;
	}

	public String getRequestStringQry01( String application, String entity, String operation, String source, String plaza, 
			String tienda, String caja,String adDate,String pvDate, String ServiceOpcion) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_Qry01, application, entity, operation,source,plaza,tienda,caja,adDate,pvDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_Qry01,ServiceOpcion);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String getRequestStringQry02( String application, String entity, String operation, String source, String plaza, 
			String tienda, String caja,String adDate,String pvDate, String ServiceCiudad) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_Qry01, application, entity, operation,source,plaza,tienda,caja,adDate,pvDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_Qry02,ServiceCiudad);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String getRequestStringTrn01( String application, String entity, String operation, String source, String plaza, 
			String tienda, String caja,String adDate,String pvDate) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_TRN01, application, entity, operation,source,plaza,tienda,caja,adDate,pvDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_TRN01);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String getRequestStringTrn02( String application, String entity, String operation, String source, String plaza, 
			String tienda, String caja,String adDate,String pvDate,String ServicepayType, String IdEntidad, String Idcanal, String idTerminal
			, String TipoVoucher, String ImporterPVP, String IdOperacionInt,String NoEntradas ) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_TRN02, application, entity, operation,source,plaza,tienda,caja,adDate,pvDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_TRN02,ServicepayType,IdEntidad,Idcanal,idTerminal,TipoVoucher,ImporterPVP,IdOperacionInt,NoEntradas);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String getRequestStringTrn03( String application, String operation, String entity, String folio, String plaza, 
			String tienda, String caja,String adDate,String ackValue) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_TRN03, application, entity,folio, operation,plaza,tienda,caja,adDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_TRN03,ackValue);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String getRequestStringTrn04( String application, String entity, String operation, String source, String plaza, 
			String tienda, String caja,String pvDate,String WmCodeauth,String folio, String creationDate) {
		
		String request = "";
//		request = requestURL + "\n";	
		String Part1 = String.format(REQ_PART_1_TRN04, application, entity, operation,source,plaza,tienda,caja,pvDate);

		request += Part1;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t      " + key + " = \"" + value + "\" ";
		}

		String Part2 = String.format(REQ_PART_2_TRN04,WmCodeauth,folio,creationDate);

		request += Part2;

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += xmlString;
			}

		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
        
		String Part3 = String.format(REQ_PART_3);
		request += Part3;
		this.request = request;
		return request;

	}
	
	public String makeRequestString() {

		String request;
		request = requestURL + "\n";
		request += REQ_PART_1_Qry01;

		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			request += "\n\t\t          " + key + " = \"" + value + "\" ";
		}

		request += "/> \r\n";

		if (!docs.isEmpty()) {
			for (Document document : docs) {
				String xmlString = xmlDocToString(document);
				request += "\n\t\t" + xmlString;
			}
		}

		if (ackValue != null) {
			String ackString = "\r\n" + "  			<ack value=\"" + ackValue + "\"/>\r\n" + " 		";
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
			// transform document to string
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			xmlString = writer.getBuffer().toString();

		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return xmlString;
	}

	public String executeRequest() throws ClientProtocolException, IOException {
		return GetRequest.executeGetRequest(this.request);
	}

	public String executeRequestValues() throws ClientProtocolException, IOException {
		return GetRequest.executeGetRequest(getRequestValuesString());
	}

	public String executeRequest(String request) throws ClientProtocolException, IOException {
		return GetRequest.executeGetRequest(request);
	}

	// Getters and setters

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

		OxxoRequesTPE_LOT olsRequest = new OxxoRequesTPE_LOT(urlRequest);
		olsRequest.addHeader("application", "MUN");
		olsRequest.addHeader("entity", "PRD");
		olsRequest.addHeader("operation", "QRY01");
		olsRequest.addHeader("source", "POS");
		olsRequest.addHeader("plaza", "10MON");
		olsRequest.addHeader("tienda", "50QTE");
		olsRequest.addHeader("caja", "1");
		olsRequest.addHeader("adDate", "20200414");
		olsRequest.addHeader("pvDate", "20200414000000");

		olsRequest.addDoc().addElement("transfer").addAtribute("amount", "1000").addAtribute("type", "ENV")
				.addAtribute("telefono", "8100000001").addElement("client").addAtribute("nombre", "jose luis")
				.addAtribute("apellidoPaterno", "valenzuela");

//		olsRequest.addService("id", "2101655006");
//		olsRequest.addService("idClient", "0600034433");

		// olsRequest.addAck("00");

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

		String rep = GetRequest.executeGetRequest(
				"http://10.184.40.110:8890/invoke/PE4v2.Pub/runQueryTdc?folio=226621&adminDate=20161107&tarjeta=4766840000004296");
		System.out.println(rep);

		String request = olsRequest.makeRequestString();

		System.out.println(request);
	}

}

