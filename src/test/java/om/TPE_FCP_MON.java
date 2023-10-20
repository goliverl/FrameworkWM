package om;


import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLUtil;

public class TPE_FCP_MON {
	
	public String requestRTP = "http://%s/invoke/TPE.FCP.Pub/request?xmlIn=";

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil  db;
	
	
	public TPE_FCP_MON(HashMap<String, String> data, TestCase testCase, SQLUtil  db) {
		
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
		
	}
	
	public String solicitudFolioSaldazo() throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, GlobalVariables.HOST_RTP);
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application",data.get("application"));
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
	
	public String validarSolicitudActivacion(String folio, String creationDate, String tarjeta) throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, GlobalVariables.HOST_RTP);
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application",data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation02"));
		or.addHeader("source", data.get("source"));
		or.addHeader("folio", folio);
		or.addHeader("creationDate", creationDate);
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));
		
		
		
		 Document doc = or.addNewDocument();
		 
	     Element etCard = doc.createElement("card"); // etiqueta
	     doc.appendChild(etCard);
	     
	     Attr cardNo = doc.createAttribute("cardNo");//atributo
	     cardNo.setValue( tarjeta);
	     etCard.setAttributeNode(cardNo);
	     
	     Attr activationType = doc.createAttribute("activationType");//atributo
	     activationType.setValue( data.get("activationType"));
	     etCard.setAttributeNode(activationType);
	     
	     Attr entryMode = doc.createAttribute("entryMode");//atributo
	     entryMode.setValue( data.get("entryMode"));
	     etCard.setAttributeNode(entryMode);	   
	     
	       
	
	     Element etClient = doc.createElement("client"); // etiqueta
	     etCard.appendChild(etClient);
	     
	     Attr nombre = doc.createAttribute("nombre");//atributo
	     nombre.setValue( data.get("nombre"));
	     etClient.setAttributeNode(nombre);
	     
	     Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");//atributo
	     apellidoPaterno.setValue( data.get("apellidoPaterno"));
	     etClient.setAttributeNode(apellidoPaterno);
	     
	     Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");//atributo
	     apellidoMaterno.setValue( data.get("apellidoMaterno"));
	     etClient.setAttributeNode(apellidoMaterno);	
	     
	     
	     Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");//atributo
	     fechaNacimiento.setValue( data.get("fechaNacimiento"));
	     etClient.setAttributeNode(fechaNacimiento);
	     
	     Attr calle = doc.createAttribute("calle");//atributo
	     calle.setValue( data.get("calle"));
	     etClient.setAttributeNode(calle);
	     
	     Attr numExt = doc.createAttribute("numExt");//atributo
	     numExt.setValue( data.get("numExt"));
	     etClient.setAttributeNode(numExt);	   
	     
	     Attr numInt = doc.createAttribute("numInt");//atributo
	     numInt.setValue( data.get("numInt"));
	     etClient.setAttributeNode(numInt);	
	     
	     
	     Attr colonia = doc.createAttribute("colonia");//atributo
	     colonia.setValue( data.get("colonia"));
	     etClient.setAttributeNode(colonia);
	     
	     Attr delegacionMunicipio = doc.createAttribute("delegacionMunicipio");//atributo
	     delegacionMunicipio.setValue( data.get("delegacionMunicipio"));
	     etClient.setAttributeNode(delegacionMunicipio);
	     
	     Attr ciudadEstado = doc.createAttribute("ciudadEstado");//atributo
	     ciudadEstado.setValue( data.get("ciudadEstado"));
	     etClient.setAttributeNode(ciudadEstado);	   
	     
	     Attr codigoPostal = doc.createAttribute("codigoPostal");//atributo
	     codigoPostal.setValue( data.get("codigoPostal"));
	     etClient.setAttributeNode(codigoPostal);	   
	     
	     Attr genero = doc.createAttribute("genero");//atributo
	     genero.setValue( data.get("genero"));
	     etClient.setAttributeNode(genero);	
	     
	     
	     Attr entidadFederativa = doc.createAttribute("entidadFederativa");//atributo
	     entidadFederativa.setValue( data.get("entidadFederativa"));
	     etClient.setAttributeNode(entidadFederativa);
	     
	     Attr tipoIdentificacion = doc.createAttribute("tipoIdentificacion");//atributo
	     tipoIdentificacion.setValue( data.get("tipoIdentificacion"));
	     etClient.setAttributeNode(tipoIdentificacion);
	     
	     Attr numeroIdentificacion = doc.createAttribute("numeroIdentificacion");//atributo
	     numeroIdentificacion.setValue( data.get("numeroIdentificacion"));
	     etClient.setAttributeNode(numeroIdentificacion);	   
	     
	     Attr celular = doc.createAttribute("celular");//atributo
	     celular.setValue( data.get("celular"));
	     etClient.setAttributeNode(celular);
	     
	     Attr promoCel = doc.createAttribute("promoCel");//atributo
	     promoCel.setValue( data.get("promoCel"));
	     etClient.setAttributeNode(promoCel);	   
	     
	   
	 	String request = or.getRequestString();
	   
	     
		System.out.println(request);
	
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
				
	}
	
	public String cierreAutorizacion(String folio, String creationDate ) throws ReempRequestException, ClientProtocolException, IOException{
		
		String url = String.format(requestRTP, GlobalVariables.HOST_RTP);
		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application",data.get("application"));
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", data.get("operation03"));
		or.addHeader("folio", folio);
		or.addHeader("creationDate", creationDate);
	
         Document doc = or.addNewDocument();
		 
	     Element etqAck = doc.createElement("ack"); // etiqueta
	     doc.appendChild(etqAck);
	     
	     Attr value = doc.createAttribute("value");//atributo
	     value.setValue( data.get("value"));
	     etqAck.setAttributeNode(value);
	     
		String request = or.getRequestString();
		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
		
		
		
	}
}