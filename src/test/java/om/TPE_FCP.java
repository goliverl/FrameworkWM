
package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;


public class TPE_FCP {

	public String requestRTP = "http://%s/invoke/TPE.FCP.Pub/request?xmlIn=";
	public String requestFCP = "http://%s/invoke/TPE.FCP.Pub/request?xmlIn=";

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;

	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
																			// de
																			// la
																			// fecha

	String date = formatter.format(fecha);

	public TPE_FCP(HashMap<String, String> data, TestCase testCase, SQLUtil db) {


	
	

		

		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;

	}

	public String ejecutarQRY01() throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "FCP");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pv_date", date);
		or.addHeader("ad_date", date.substring(0, 8));

		for (Map.Entry<String, String> entry : data.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (key.contains("service:") && value.trim().length() > 0) {
				String keytoAdd = key.split(":")[1];
				or.addService(keytoAdd, value);
			}
		}

		// or.addService("id", data.get("id"));
		// or.addService("idClient", data.get("idClient"));

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;
	}

	public String generacionFolioTransaccionCliente() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("ad_date", date.substring(0, 8));
		or.addHeader("pv_date", date);
		or.addHeader("serviceId", data.get("serviceId"));	
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));
		
       //Inicio etiqueta card
		Document doc = or.addNewDocument();
		
		Element card = doc.createElement("card");
		doc.appendChild(card);

		Attr cardNo = doc.createAttribute("cardNo");
		cardNo.setValue(data.get("cardNo"));
		card.setAttributeNode(cardNo);

		Attr activationType = doc.createAttribute("activationType");
		activationType.setValue(data.get("activationType"));
		card.setAttributeNode(activationType); 
		
		Attr contrato = doc.createAttribute("contrato");
		contrato.setValue(data.get("contrato"));
		card.setAttributeNode(contrato); 
		
		Attr bankFolio = doc.createAttribute("bankFolio");
		bankFolio.setValue(data.get("bankFolio"));
		card.setAttributeNode(bankFolio); 
		
		Attr entryMode = doc.createAttribute("entryMode");
		entryMode.setValue(data.get("entryMode"));
		card.setAttributeNode(entryMode); 		
	    //fin card
		
		//inicio client
		
	
	
		Element client = doc.createElement("client");
		card.appendChild(client);

		Attr nombre = doc.createAttribute("nombre");
		nombre.setValue(data.get("nombre"));
		client.setAttributeNode(nombre);
		
		Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");
		apellidoPaterno.setValue(data.get("apellidoPaterno"));
		client.setAttributeNode(apellidoPaterno);
		
		Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");
		apellidoMaterno.setValue(data.get("apellidoMaterno"));
		client.setAttributeNode(apellidoMaterno);
		
		Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");
		fechaNacimiento.setValue(data.get("fechaNacimiento"));
		client.setAttributeNode(fechaNacimiento);
		
		Attr celular = doc.createAttribute("celular");
		celular.setValue(data.get("celular"));
		client.setAttributeNode(celular);
		//fin client
	

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}

	
	public String generarAutorizacion(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
		
		String url = String.format(requestFCP, "10.184.40.110:8888");
	
		OxxoRequest or = new OxxoRequest(url);

  			or.addHeader("application", data.get("applicationAut"));
  			or.addHeader("entity", data.get("entityAut"));
  			or.addHeader("operation", data.get("operationAut"));
  			or.addHeader("source", data.get("sourceAut"));
  			or.addHeader("folio", Folio);
  			or.addHeader("creationdate", date);
  			or.addHeader("ad_date", date.substring(0, 8));
  			or.addHeader("pv_date", date);
  			or.addHeader("pvTicket", data.get("pvTicket"));
  			or.addHeader("operator", data.get("operator"));
		
  		//Inicio etiqueta card
  			Document doc = or.addNewDocument();
  			
  			Element card = doc.createElement("card");
  			doc.appendChild(card);

  			Attr cardNo = doc.createAttribute("cardNo");
  			cardNo.setValue(data.get("cardNo"));
  			card.setAttributeNode(cardNo);

  			Attr activationType = doc.createAttribute("activationType");
  			activationType.setValue(data.get("activationType"));
  			card.setAttributeNode(activationType); 
  			
  			Attr entryMode = doc.createAttribute("entryMode");
  			entryMode.setValue(data.get("entryMode"));
  			card.setAttributeNode(entryMode); 		
  		    //fin card
  		
  			//inicio client
  			
  			Element client = doc.createElement("client");
  			card.appendChild(client);

  			Attr nombre = doc.createAttribute("nombre");
  			nombre.setValue(data.get("nombre"));
  			client.setAttributeNode(nombre);
  			
  			Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");
  			apellidoPaterno.setValue(data.get("apellidoPaterno"));
  			client.setAttributeNode(apellidoPaterno);
  			
  			Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");
  			apellidoMaterno.setValue(data.get("apellidoMaterno"));
  			client.setAttributeNode(apellidoMaterno);
  			
  			Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");
  			fechaNacimiento.setValue(data.get("fechaNacimiento"));
  			client.setAttributeNode(fechaNacimiento);
  			
  			Attr calle = doc.createAttribute("calle");
  			calle.setValue(data.get("calle"));
  			client.setAttributeNode(calle);

  			Attr numext = doc.createAttribute("numext");
  			numext.setValue(data.get("numext"));
  			client.setAttributeNode(numext);

  			Attr numint = doc.createAttribute("numint");
  			numint.setValue(data.get("numint"));
  			client.setAttributeNode(numint);

  			Attr colonia = doc.createAttribute("colonia");
  			colonia.setValue(data.get("colonia"));
  			client.setAttributeNode(colonia);

  			Attr delegacionmunicipio = doc.createAttribute("delegacionmunicipio");
  			delegacionmunicipio.setValue(data.get("delegacion"));
  			client.setAttributeNode(delegacionmunicipio);

  			Attr ciudadestado = doc.createAttribute("ciudadestado");
  			ciudadestado.setValue(data.get("ciudad"));
  			client.setAttributeNode(ciudadestado);

  			Attr codigopostal = doc.createAttribute("codigopostal");
  			codigopostal.setValue(data.get("cp"));
  			client.setAttributeNode(codigopostal);

  			Attr genero = doc.createAttribute("genero");
  			genero.setValue(data.get("genero"));
  			client.setAttributeNode(genero);

  			Attr entidadfederativa = doc.createAttribute("entidadfederativa");
  			entidadfederativa.setValue(data.get("entidadfed"));
  			client.setAttributeNode(entidadfederativa);

  			Attr tipodeidentificacion = doc.createAttribute("tipodeidentificacion");
  			tipodeidentificacion.setValue(data.get("tipoidentificacion"));
  			client.setAttributeNode(tipodeidentificacion);

  			Attr numerodeidentificacion = doc.createAttribute("numerodeidentificacion");
  			numerodeidentificacion.setValue(data.get("noindentificacion"));
  			client.setAttributeNode(numerodeidentificacion);

  			Attr celular = doc.createAttribute("celular");
  			celular.setValue(data.get("celular"));
  			client.setAttributeNode(celular);

  			Attr promocel = doc.createAttribute("promocel");
  			promocel.setValue(data.get("promocel"));
  			client.setAttributeNode(promocel);

  			//fin client

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}
	
	
	public String generacionFolioTransaccion() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);
	
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation01"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));

		Document doc = or.addNewDocument();
		Element carname = doc.createElement("card");
		doc.appendChild(carname);

		Attr attrType = doc.createAttribute("id");
		attrType.setValue(data.get("id"));
		carname.setAttributeNode(attrType);

		Attr type = doc.createAttribute("type");
		type.setValue(data.get("type"));
		carname.setAttributeNode(type);

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}

	public String generacionFolioTransaccion1(String folio)
			throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation2"));
		or.addHeader("source", data.get("source"));

		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		
		
		Document doc = or.addNewDocument();
		Element carname = doc.createElement("card");
	    doc.appendChild(carname);
	    
		  Attr attrType = doc.createAttribute("id");
	        attrType.setValue(data.get("id"));
	        carname.setAttributeNode(attrType);
	        
	        Attr type = doc.createAttribute("type");
	        type.setValue(data.get("type"));
	        carname.setAttributeNode(type);
	        
	        Attr type1 = doc.createAttribute("accountId");
	        type1.setValue(data.get("accountId"));
	        carname.setAttributeNode(type1);
	        
	        Document doc2 = or.addNewDocument();
	        
	    	Element rewards = doc2.createElement("rewards");
		    doc2.appendChild(rewards);
	        
	       Element carname1 = doc2.createElement("item");
		    rewards.appendChild(carname1);
		    
		    Attr attrType1 = doc2.createAttribute("id");
	        attrType1.setValue(data.get("itemid"));
	        carname1.setAttributeNode(attrType1);
	        
	        Attr attrType2 = doc2.createAttribute("desc");
	        attrType2.setValue(data.get("desc"));
	        carname1.setAttributeNode(attrType2);
	        
	        Attr attrType3 = doc2.createAttribute("promotionId");
	        attrType3.setValue(data.get("promotionId"));
	        carname1.setAttributeNode(attrType3);
	        
	        Attr attrType4 = doc2.createAttribute("emision");
	        attrType4.setValue(data.get("emision"));
	        carname1.setAttributeNode(attrType4);
	        
	        Attr attrType5 = doc2.createAttribute("sku");
	        attrType5.setValue(data.get("sku"));
	        carname1.setAttributeNode(attrType5);
	        
	        Attr attrType6 = doc2.createAttribute("ticket");
	        attrType6.setValue(data.get("ticket"));
	        carname1.setAttributeNode(attrType6);
	        
	        Attr attrType7 = doc2.createAttribute("quantity");
	        attrType7.setValue(data.get("quantity"));
	        carname1.setAttributeNode(attrType7);
	        
	        Attr attrType8 = doc2.createAttribute("monto");
	        attrType8.setValue(data.get("monto"));
	        carname1.setAttributeNode(attrType8);
	        
	        Attr attrType9 = doc2.createAttribute("discount");
	        attrType9.setValue(data.get("discount"));
	        carname1.setAttributeNode(attrType9); 
	        
	        Attr attrType10 = doc2.createAttribute("auth");
	        attrType10.setValue(data.get("auth"));
	        carname1.setAttributeNode(attrType10); 
	        
	        
	        String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}
	
	public String generacionACK2(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operationack"));
		or.addHeader("folio", folio);

		Document doc = or.addNewDocument();
		Element carname = doc.createElement("ack");
		doc.appendChild(carname);

		Attr attrType = doc.createAttribute("value");
		attrType.setValue(data.get("ackvalue"));
		carname.setAttributeNode(attrType);


		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}
		

	public String generacionFolioTransaccion2() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);
		http://10.184.40.110:8893/invoke/TPE.FCP.Pub/request?xmlIn=

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation01"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}
	
	
	public String generacionFolioTransaccion3() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, "10.184.40.110:8888");

		OxxoRequest or = new OxxoRequest(url);
	
		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation01"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}

	public String generacionACK(String folio) throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation3"));
		or.addHeader("folio", folio);

		Document doc = or.addNewDocument();
		Element carname = doc.createElement("ack");
		doc.appendChild(carname);

		Attr attrType = doc.createAttribute("value");
		attrType.setValue(data.get("valueid"));
		carname.setAttributeNode(attrType);


		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();

		testCase.addTextEvidenceCurrentStep(response);

		return response;

	}

	public String ejecutarOPR01() throws ReempRequestException, ClientProtocolException, IOException {

		 String url = String.format(requestFCP, GlobalVariables.HOST);

		 OxxoRequest or = new OxxoRequest(url);

		 // HEADERFF
		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pvDate", date);
		or.addHeader("adDate", date.substring(0, 8));

		 // REQUEST
		Document doc = or.addNewDocument();

		 Element card = doc.createElement("card"); // Etiqueta card
		doc.appendChild(card);

		 Attr cardId = doc.createAttribute("id");// Atributo id
		cardId.setValue(data.get("id"));
		card.setAttributeNode(cardId);

		 Attr type = doc.createAttribute("type");// Atributo type
		type.setValue(data.get("type"));
		card.setAttributeNode(type);

		 Attr emissionId = doc.createAttribute("emissionId");// Atributo
		// emissionId
		emissionId.setValue(data.get("emissionId"));
		card.setAttributeNode(emissionId);

		 Attr barCode = doc.createAttribute("barCode");// Atributo barCode
		barCode.setValue(data.get("barCode"));
		card.setAttributeNode(barCode);

		 /////////

		 String request = or.getRequestString();
		System.out.println("Solicitud:\n" + request);
		testCase.addTextEvidenceCurrentStep(request);

		 String response = or.executeRequest();
		testCase.addTextEvidenceCurrentStep(response);

		 return response;
		}
	
	
	public String ejecutarOPR02() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestFCP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		// HEADER
		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pv_date", date);
		or.addHeader("ad_date", date.substring(0, 8));

		// REQUEST
		Document doc = or.addNewDocument();

		Element card = doc.createElement("card"); // Etiqueta card
		doc.appendChild(card);

		Attr cardId = doc.createAttribute("id");// Atributo id
		cardId.setValue(data.get("cardId"));
		card.setAttributeNode(cardId);

		Attr type = doc.createAttribute("type");// Atributo type
		type.setValue(data.get("type"));
		card.setAttributeNode(type);

		Attr emissionId = doc.createAttribute("emissionId"); // Atributo
																// emissionId
		emissionId.setValue(data.get("emissionId"));
		card.setAttributeNode(emissionId);

		Attr barCode = doc.createAttribute("barCode");// Atributo barCode
		barCode.setValue(data.get("barCode"));
		card.setAttributeNode(barCode);

		// Etiqueta promotion
		Element promotion = doc.createElement("promotion");
		card.appendChild(promotion);

		Attr promotionId = doc.createAttribute("id");// Atributo id
		promotionId.setValue(data.get("promotionId"));
		promotion.setAttributeNode(promotionId);

		Attr ticket = doc.createAttribute("ticket");// Atributo ticket
		ticket.setValue(data.get("ticket"));
		promotion.setAttributeNode(ticket);

		Attr payDate = doc.createAttribute("payDate");// Atributo payDate
		payDate.setValue(data.get("payDate"));
		promotion.setAttributeNode(payDate);

		Attr hour = doc.createAttribute("hour");// Atributo hour
		hour.setValue(data.get("hour"));
		promotion.setAttributeNode(hour);

		Attr qty = doc.createAttribute("qty");// Atributo qty
		qty.setValue(data.get("qty"));
		promotion.setAttributeNode(qty);

		Attr version = doc.createAttribute("version");// Atributo version
		version.setValue(data.get("version"));
		promotion.setAttributeNode(version);

		///////////////

		String request = or.getRequestString();
		System.out.println("Solicitud:\n" + request);
		testCase.addTextEvidenceCurrentStep(request);

		String response = or.executeRequest();
		testCase.addTextEvidenceCurrentStep(response);

		return response;
	}

	public String ejecutarTpeQRY01() throws ReempRequestException, ClientProtocolException, IOException {

		String url = String.format(requestRTP, GlobalVariables.HOST);

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation"));
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("pv_date", date);
		or.addHeader("ad_date", date.substring(0, 8));

		Document doc = or.addNewDocument();
		Element card = doc.createElement("card");
		doc.appendChild(card);

		Attr accountId = doc.createAttribute("accountId");
		accountId.setValue(data.get("accountId"));
		card.setAttributeNode(accountId);

		Attr id = doc.createAttribute("id");
		id.setValue(data.get("cardId"));
		card.setAttributeNode(id);

		Attr type = doc.createAttribute("type");
		type.setValue(data.get("type"));
		card.setAttributeNode(type);

		String request = or.getRequestString();

		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);

		String response = or.executeRequest();
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);

		return response;

	}

}
