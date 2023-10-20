package om;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import exceptions.FrameworkException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class TPE_FAC {

	Date fecha = new Date();// obtener fecha del sistema
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
	
	public String requestFAC ="http://%s/invoke/TPE.FAC.Pub/request?xmlIn=";
	public String runGetQRY01="FAC\\QRY01.txt";
	public String runGetQRY01SinSadel="FAC\\QRY01SinSadel.txt";
	public String runConsQRY01="FAC\\ConsCajeroQRY01.txt";
	public String runLoginQRY09="FAC\\LoginQRY09.txt";
	public String runUpdPass="FAC\\ActulizaPass.txt";
	public String runLogOut="FAC\\LogOutQRY29.txt";
	public String runResQRY12 = "FAC\\QRY12.txt";
	public String LoginQRY29Bandera="FAC\\LoginQRY29Bandera.txt";
	public String ResetQRY12Bandera="FAC\\ResetQRY12.txt";
	public String ResetQRY13Bandera="FAC\\ResetQRY13.txt"; //Provisional String Actualizacion de Matriz
	
	String creationDate;
	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;
	
	String date = formatter.format(fecha);
	
	public  TPE_FAC (HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {

		this.data = data;
		this.testCase = testCase;
		this.db = db;
		
	}
	
	public String ConsultaCajeros() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();

		datosRequest.put("host", data.get("host"));
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", data.get("operation"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("folio", data.get("folio"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("adDate", date.substring(0, 8));
		datosRequest.put("pvDate", date);
		datosRequest.put("app", data.get("app"));
		
		String request = GetRequestFile.getRequestFile(runConsQRY01, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud para validar la nueva consulta de cajeros (QRY01) fue exitoso: ");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
		
	}
	
public String ValidLogin(String IdUser, String Pass) throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();

		datosRequest.put("host", data.get("host"));
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", data.get("operation02"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("folio", data.get("folio"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("adDate", date.substring(0, 8));
		datosRequest.put("pvDate", date);
		datosRequest.put("retry", data.get("retry"));
		datosRequest.put("idusuario", IdUser);
		datosRequest.put("passwd", Pass);
		datosRequest.put("type", data.get("type"));
		datosRequest.put("app", data.get("app"));
		
		
		String request = GetRequestFile.getRequestFile(runLoginQRY09, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud para alidar el registro  de  el login (QRY09) fue exitoso: ");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
		
	}
	
public String ActualizacionPass(String IdUser) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation03"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", date.substring(0, 8));
	datosRequest.put("pvDate", date);
	datosRequest.put("idusuario", IdUser);
	datosRequest.put("passwd", data.get("NewPass"));
	datosRequest.put("question", data.get("question"));
	datosRequest.put("answer", data.get("answer"));
	datosRequest.put("expireddate", data.get("expireddate"));
	datosRequest.put("createdby", data.get("createdby"));
	
	String request = GetRequestFile.getRequestFile(runUpdPass, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de Actualizaci�n de contrase�a (QRY13) funciona correctamente ");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
}


public String ValidLogout(String IdUser) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation04"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", date.substring(0, 8));
	datosRequest.put("pvDate", date);
	datosRequest.put("idusuario", IdUser);
	datosRequest.put("rol", data.get("rol"));
	datosRequest.put("app", data.get("app"));
	datosRequest.put("type", data.get("type02"));

	
	String request = GetRequestFile.getRequestFile(runLogOut, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de LogOut (QRY29) funciona correctamente ");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
}
		

public String QRY0() throws ReempRequestException, ClientProtocolException, IOException{
	
String url = String.format(requestFAC, GlobalVariables.HOST_FAC);
	
	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", "GAME");
	or.addHeader("operation",data.get("operation"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("source", data.get("source"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("pvDate", data.get("pvDate"));
	or.addHeader("adDate", data.get("adDate"));
	
   String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
}

public String QRYBank() throws ReempRequestException, ClientProtocolException, IOException{
	/**
	 * se empiez la construcion se analizara la similutud de campos con las consultas
	 * QRY03
	 * QRY04
	 * QRY05
	 * QRY06
	 * para asi abarcar todo solamente con este escript
	 */
String url = String.format(requestFAC, GlobalVariables.HOST_QA9);
	
	OxxoRequest or = new OxxoRequest(url);
	String date = formatter.format(fecha);

	or.addHeader("application", "FAC");
	or.addHeader("entity", "BANK");
	or.addHeader("operation",data.get("operation"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("source", data.get("source"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("pvDate", date);
	or.addHeader("adDate", date.substring(0,8));
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("usuario");
	doc.appendChild(valid);
	//atributos 
	Attr idusuario = doc.createAttribute("idusuario");
	idusuario.setValue(data.get("idusuario"));
    valid.setAttributeNode(idusuario);
    
	
   String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
}

public String QRY01BANK() throws ReempRequestException, ClientProtocolException, IOException {
	
String url = String.format(requestFAC, data.get("host"));
String fechaTexto = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", "BANK");
	or.addHeader("operation", "QRY01");
	or.addHeader("source", "POS");
	or.addHeader("folio",data.get("folio"));	
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("pvDate", fechaTexto.substring(0, 8));
	or.addHeader("adDate", fechaTexto.substring(0, 8));
	
	
	
    
    
	Document doc = or.addNewDocument();
    Attr app = doc.createAttribute("app");
    app.setValue(data.get("app"));

	
	
    String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
}

public String QRY01BIODAT() throws ReempRequestException, ClientProtocolException, IOException {
	
String url = String.format(requestFAC, GlobalVariables.HOST_FAC);
	
	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", "BIO");
	or.addHeader("operation", data.get("operation"));
	or.addHeader("source", "POS");	
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", data.get("adDate"));
	or.addHeader("pvDate", data.get("pvDate"));
	
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("validpass");
	doc.appendChild(valid);
	//atributos 
	Attr idusuario = doc.createAttribute("idusuario");
	idusuario.setValue(data.get("idusuario"));
    valid.setAttributeNode(idusuario);
    
    Attr passwd = doc.createAttribute("passwd");
    passwd.setValue(data.get("passwd"));
    valid.setAttributeNode(passwd);
    
    Attr fechaHora = doc.createAttribute("fechaHora");
    fechaHora.setValue(data.get("fechaHora"));
    valid.setAttributeNode(fechaHora);
    
    Attr evento = doc.createAttribute("evento");
    evento.setValue(data.get("evento"));
    valid.setAttributeNode(evento);
    
    Attr comentario = doc.createAttribute("comentario");
    comentario.setValue(data.get("comentario"));
    valid.setAttributeNode(comentario);
    
    String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
}

public String QRY01GAME() throws ReempRequestException, ClientProtocolException, IOException {
	
String url = String.format(requestFAC, GlobalVariables.HOST_FAC);
	
	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", data.get("operation"));
	or.addHeader("source", "POS");	
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", data.get("adDate"));
	or.addHeader("pvDate", data.get("pvDate"));
	
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("usuario");
	doc.appendChild(valid);
	//atributos 
	Attr idusuario = doc.createAttribute("idusuario");
	idusuario.setValue(data.get("idusuario"));
    valid.setAttributeNode(idusuario);
    
    Attr nombre = doc.createAttribute("nombre");
    nombre.setValue(data.get("nombre"));
    valid.setAttributeNode(nombre);
    
    Attr paterno = doc.createAttribute("paterno");
    paterno.setValue(data.get("paterno"));
    valid.setAttributeNode(paterno);
    
    Attr materno = doc.createAttribute("materno");
    materno.setValue(data.get("materno"));
    valid.setAttributeNode(materno);
    
    Attr status = doc.createAttribute("status");
    status.setValue(data.get("status"));
    valid.setAttributeNode(status);
    
    Attr rol = doc.createAttribute("rol");
    status.setValue(data.get("rol"));
    valid.setAttributeNode(rol);
    
  //etiqueta
  	Element tien = doc.createElement("tienda");
  	doc.appendChild(tien);
    
  	 Attr plaza = doc.createAttribute("plaza");
  	plaza.setValue(data.get("plaza"));
     tien.setAttributeNode(plaza);
     
     Attr tienda = doc.createAttribute("tienda");
     status.setValue(data.get("tienda"));
     tien.setAttributeNode(tienda);
     
     Attr lider = doc.createAttribute("lider");
     status.setValue(data.get("lider"));
     tien.setAttributeNode(lider);
  	
    String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
}

public String QRY01MDM() throws ReempRequestException, ClientProtocolException, IOException {
	
String url = String.format(requestFAC, GlobalVariables.HOST_QA9);
	
	String date = formatter.format(fecha);
	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", data.get("operation"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("source", "POS");	
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate",date);
	
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("usuario");
	doc.appendChild(valid);
	//atributos 
	Attr idusuario = doc.createAttribute("idusuario");
	idusuario.setValue(data.get("idusuario"));
    valid.setAttributeNode(idusuario);
    
   
    
    String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
}


public String QRY05() throws ReempRequestException, ClientProtocolException, IOException {
	
String url = String.format(requestFAC, GlobalVariables.HOST_FAC);
	
	OxxoRequest or = new OxxoRequest(url);
	
	HashMap<String, String> datosRequest = new HashMap<>();
	
	or.addHeader("application","FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY05");
	or.addHeader("source", data.get("source"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", data.get("adDate"));
	or.addHeader("pvDate", data.get("pvDate")); 
	
	Document doc = or.addNewDocument();
    //etiqueta
    Element usuario = doc.createElement("usuario");
    doc.appendChild(usuario);
    //atributos
    Attr idusuario = doc.createAttribute("idusuario");
    idusuario.setValue(data.get("idusuario"));
    usuario.setAttributeNode(idusuario);
   
    Attr type = doc.createAttribute("type");
    type.setValue(data.get("tipousuario"));
    usuario.setAttributeNode(type);
    
	
    String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
}

	



public String QRY06() throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, GlobalVariables.HOST_RTP);
	
	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "application");
	or.addHeader("entity", data.get("entityt"));
	or.addHeader("operation", data.get("operation"));	
	or.addHeader("source", data.get("source"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", data.get("adDate"));
	or.addHeader("pvDate", data.get("pvDate"));
	
	or.addHeader("type", data.get("type"));
	
	
	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}

public String QRY09() throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, GlobalVariables.HOST_QA9);
	String date = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY09");	
	or.addHeader("source", data.get("source"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate", date);
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("validpass");
	doc.appendChild(valid);
	
	//atributos
	
	Attr type = doc.createAttribute("type");
	type.setValue(data.get("type"));
    valid.setAttributeNode(type);
	
    Attr idusuario = doc.createAttribute("idusuario");
    idusuario.setValue(data.get("idusuario"));
    valid.setAttributeNode(idusuario);
    
    Attr passwd = doc.createAttribute("passwd");
    passwd.setValue(data.get("password"));
    valid.setAttributeNode(passwd);
    
    Attr retry = doc.createAttribute("retry");
    retry.setValue(data.get("retry"));
    valid.setAttributeNode(retry);
    
    Attr app = doc.createAttribute("app");
    app.setValue(data.get("app"));
    valid.setAttributeNode(app);
    
    Attr pospkg = doc.createAttribute("pospkg");
    pospkg.setValue(data.get("pospkg"));
    valid.setAttributeNode(pospkg);
    
	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}


public String QRY09_2(String user_id, String user_pass) throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, data.get("ipServer"));
	String date = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY09");	
	or.addHeader("source", data.get("source"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate", date);
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("validpass");
	doc.appendChild(valid);
	
	//atributos
	
	Attr type = doc.createAttribute("type");
	type.setValue(data.get("type"));
    valid.setAttributeNode(type);
	
    Attr idusuario = doc.createAttribute("idusuario");
    idusuario.setValue(user_id);
    valid.setAttributeNode(idusuario);
    
    Attr passwd = doc.createAttribute("passwd");
    passwd.setValue(user_pass);
    valid.setAttributeNode(passwd);
    
    Attr retry = doc.createAttribute("retry");
    retry.setValue(data.get("retry"));
    valid.setAttributeNode(retry);
    
    Attr app = doc.createAttribute("app");
    app.setValue(data.get("app"));
    valid.setAttributeNode(app);
    
    Attr pospkg = doc.createAttribute("pospkg");
    pospkg.setValue(data.get("pospkg"));
    valid.setAttributeNode(pospkg);
    
    Attr flagEncr = doc.createAttribute("flagEncriptado");
    flagEncr.setValue(data.get("flagEncriptado"));
    valid.setAttributeNode(flagEncr);
    
	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}



public String QRY12(String user_id, String user_pass) throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, data.get("ipServer"));
	String date = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY12");	
	or.addHeader("source", data.get("source"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate", date);
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element reset = doc.createElement("reset");
	doc.appendChild(reset);
	
	//atributos
	
    Attr idusuario = doc.createAttribute("idusuario");
    idusuario.setValue(user_id);
    reset.setAttributeNode(idusuario);
    
    Attr createdby = doc.createAttribute("createdby");
    createdby.setValue(data.get("createdby"));
    reset.setAttributeNode(createdby);
    
    Attr passwd = doc.createAttribute("passwd");
    passwd.setValue(user_pass);
    reset.setAttributeNode(passwd);
  
    Attr flagEncr = doc.createAttribute("flagEncriptado");
    flagEncr.setValue(data.get("flagEncriptado"));
    reset.setAttributeNode(flagEncr);
    
	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}



public String QRY29() throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, GlobalVariables.HOST_QA9);
	String date = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY29");	
	or.addHeader("source", data.get("source"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate", date);
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element login = doc.createElement("login");
	doc.appendChild(login);
	
	//atributos
	
	Attr type = doc.createAttribute("type");
	type.setValue(data.get("type"));
	login.setAttributeNode(type);
	
    Attr idusuario = doc.createAttribute("idusuario");
    idusuario.setValue(data.get("idusuario"));
    login.setAttributeNode(idusuario);
   
    Attr rol = doc.createAttribute("rol");
    rol.setValue(data.get("rol"));
    login.setAttributeNode(rol);
    
    Attr app = doc.createAttribute("app");
    app.setValue(data.get("app"));
    login.setAttributeNode(app);
    

	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}

public String QRYSinSadel() throws ReempRequestException, ClientProtocolException, IOException {

	String fechaTexto = formatter.format(fecha);
	String url = String.format(requestFAC, data.get("host"));
	OxxoRequest or = new OxxoRequest(url);

	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", data.get("operation"));
	or.addHeader("folio", data.get("folio"));
	or.addHeader("source", data.get("source"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("pvDate", fechaTexto.substring(0, 8));
	or.addHeader("adDate", fechaTexto.substring(0, 8));

	String request = or.getRequestString();

	System.out.println(request);
	//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
	testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);

	String response = or.executeRequest();
	//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + respuestaTRN01);
	testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

	return response;
}

public String QRYConSadel() throws ReempRequestException, ClientProtocolException, IOException, InterruptedException {

	String fechaTexto = formatter.format(fecha);
	//String url = String.format(requestFAC, data.get("host"));

	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", data.get("host"));

	datosRequest.put("application", "FAC");
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("pvDate", fechaTexto.substring(0, 8));
	datosRequest.put("adDate", fechaTexto.substring(0, 8));

	String request = GetRequestFile.getRequestFile(runGetQRY01, datosRequest);
	System.out.println(request);
	//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
	testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
		
	String response = GetRequest.executeGetRequest(request);
	//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + respuestaTRN01);
	testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

	return response;
}



//Proyecto encripcion correspopnsalias

public String QRY09(String id_usuario) throws ReempRequestException, ClientProtocolException, IOException{
	
	String url = String.format(requestFAC, data.get("host"));
	String date = formatter.format(fecha);

	OxxoRequest or = new OxxoRequest(url);
	
	or.addHeader("application", "FAC");
	or.addHeader("entity", data.get("entity"));
	or.addHeader("operation", "QRY09");	
	or.addHeader("source", data.get("source"));
	//or.addHeader("folio", data.get("folio"));
	or.addHeader("plaza", data.get("plaza"));
	or.addHeader("tienda", data.get("tienda"));
	or.addHeader("caja", data.get("caja"));
	or.addHeader("adDate", date.substring(0,8));
	or.addHeader("pvDate", date);
	
	Document doc = or.addNewDocument();
	//etiqueta
	Element valid = doc.createElement("validpass");
	doc.appendChild(valid);
	
	//atributos
	
	Attr type = doc.createAttribute("type");
	type.setValue(data.get("type"));
  valid.setAttributeNode(type);
	
  Attr idusuario = doc.createAttribute("idusuario");
  idusuario.setValue(id_usuario);
  valid.setAttributeNode(idusuario);
  
  Attr passwd = doc.createAttribute("passwd");
  passwd.setValue(data.get("password"));
  valid.setAttributeNode(passwd);
  
  Attr retry = doc.createAttribute("retry");
  retry.setValue(data.get("retry"));
  valid.setAttributeNode(retry);
  
  Attr app = doc.createAttribute("app");
  app.setValue(data.get("app"));
  valid.setAttributeNode(app);
  
  Attr pospkg = doc.createAttribute("pospkg");
  pospkg.setValue(data.get("pospkg"));
  valid.setAttributeNode(pospkg);
  
	String request = or.getRequestString();
	
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep(request);
	
	String response = or.executeRequest();
	
	testCase.addTextEvidenceCurrentStep(response);
	
	return response;
	
	
	
}

public String LoginQRY29(String Pass) throws ReempRequestException, ClientProtocolException, IOException, InterruptedException {

	String fechaTexto = formatter.format(fecha);

	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", fechaTexto.substring(0, 8));
	datosRequest.put("pvDate", fechaTexto);
	datosRequest.put("type", data.get("type"));
	datosRequest.put("idusuario", data.get("idusuario"));
	datosRequest.put("passwd", Pass);
	datosRequest.put("retry", data.get("retry"));
	datosRequest.put("app", data.get("app"));
	datosRequest.put("pospkg", data.get("pospkg"));
	datosRequest.put("flagEncriptado", data.get("flagEncriptado"));
	
	String request = GetRequestFile.getRequestFile(LoginQRY29Bandera, datosRequest);
	System.out.println(request);
	//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
	testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
		
	String response = GetRequest.executeGetRequest(request);
	//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + respuestaTRN01);
	testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

	return response;
}

public String LoginQRY12(String Pass) throws ReempRequestException, ClientProtocolException, IOException, InterruptedException {

	String fechaTexto = formatter.format(fecha);

	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", fechaTexto.substring(0, 8));
	datosRequest.put("pvDate", fechaTexto);
	datosRequest.put("idusuario", data.get("idusuario"));
	datosRequest.put("createdby", data.get("createdby"));
	datosRequest.put("passwd", Pass);
	datosRequest.put("flagEncriptado", data.get("flagEncriptado"));

	String request = GetRequestFile.getRequestFile(ResetQRY12Bandera, datosRequest);
	System.out.println(request);
	//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
	testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
		
	String response = GetRequest.executeGetRequest(request);
	//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + respuestaTRN01);
	testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

	return response;
}


public String LoginQRY13(String Pass) throws ReempRequestException, ClientProtocolException, IOException, InterruptedException {

	String fechaTexto = formatter.format(fecha);

	HashMap<String, String> datosRequest = new HashMap<>();
	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", data.get("folio"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", fechaTexto.substring(0, 8));
	datosRequest.put("pvDate", fechaTexto);
	datosRequest.put("idusuario", data.get("idusuario"));
	datosRequest.put("createdby", data.get("createdby"));
	datosRequest.put("passwd", Pass);
	datosRequest.put("flagEncriptado", data.get("flagEncriptado"));
	
	String request = GetRequestFile.getRequestFile(ResetQRY13Bandera, datosRequest);
	System.out.println(request);
	//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
	testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
		
	String response = GetRequest.executeGetRequest(request);
	//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + respuestaTRN01);
	testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

	return response;

}



public long obtenerSegundosInicio() {
	long segundos = System.currentTimeMillis();
	return segundos;
}

public long obtenerSegundosFin() {
	long segundos = System.currentTimeMillis();
	return segundos;
}

public double obtenerDiferencia(long fin, long inicio) {
	double tiempo = (double) ((fin - inicio) / 1000);
	return tiempo;
}






	
}
