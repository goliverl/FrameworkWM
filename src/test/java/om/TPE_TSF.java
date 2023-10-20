package om;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tulskiy.keymaster.windows.User32;

import modelo.TestCase;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.sql.SQLUtil;


public class TPE_TSF {

	public String requestUrl = "http://%s/invoke/TPE.TSF.Pub/request?xmlIn=";

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
	DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	String currentDate = dateformatter.format(date);
	
	public TPE_TSF(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
		public String ejecutarQRY01pt2() throws ParserConfigurationException, ClientProtocolException, IOException {		
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", currentDate.substring(0,8));
		or.addHeader("pvDate", currentDate);
		        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();	
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarQRY01() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", currentDate.substring(0,8));
		or.addHeader("pvDate", currentDate);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc().addElement("transfer").addAtribute("amount", data.get("amount"));

        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();	
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;			
	}
	
	public String ejecutarQRY01_MG() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
//		String url = String.format(requestUrl, GlobalVariables.HOST_QA10_VIRTUAL);
		String url = String.format(requestUrl, host);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("application", "TSF");
		or.addHeader("adDate", currentDate.substring(0, 8));
		or.addHeader("pvDate", currentDate);
		or.addHeader("source", data.get("source"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operation", "QRY01");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
			.addElement("transfer")
				.addAtribute("countryCode", data.get("countryCode"))
				.addAtribute("proveedor", data.get("proveedor"))
				.addAtribute("transferNo", data.get("transferNo"));
		
		String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest(request);
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);
		return response;	
	}
	public String ejecutarQRY01_WU1() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
//		String url = String.format(requestUrl, GlobalVariables.HOST_QA10_VIRTUAL);
		String url = String.format(requestUrl, host);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", currentDate.substring(0, 8 ));
		or.addHeader("pvDate", currentDate);
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc().addElement("transfer")
			.addAtribute("transferNo", data.get("transferNo"))
			.addAtribute("proveedor", data.get("proveedor"))
			.addAtribute("marca", data.get("marca"))
			.addAtribute("countryCode", data.get("countryCode"))
			.addAtribute("currencyCode", data.get("currencyCode"));
		
		String request = or.getRequestString();
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest(request);
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	
	public String ejecutarQRY01_WU(String folio) throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
//		String url = String.format(requestUrl, GlobalVariables.HOST_QA10_VIRTUAL);
		String url = String.format(requestUrl, host);
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc().addElement("transfer")
			.addAtribute("transferNo", folio)
		//	.addAtribute("amount", data.get("amount"))
			.addAtribute("proveedor", data.get("proveedor"))
			.addAtribute("marca", data.get("marca"))
			.addAtribute("countryCode", data.get("countryCode"))
			.addAtribute("currencyCode", data.get("currencyCode"));		
		
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
		testCase.addTextEvidenceCurrentStep(response);		
		return response;			
	}
	
	public String ejecutarQRY02() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", currentDate.substring(0,8));
		or.addHeader("pvDate", currentDate);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));		
		
		or.addDoc().
		addElement("transfer").
			addElement("client").
				addAtribute("noId", data.get("noId"));

        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();	
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;			
	}
	
	public String ejecutarQRY02_WU(String folio, String creatioDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", creatioDate.substring(0, 8));
		or.addHeader("pvDate", creatioDate);
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc().addElement("client")
			.addElement("receiver")
				.addAtribute("tipoNombre", data.get("tipoNombre"))
				.addAtribute("nombre", data.get("nombre"))
				.addAtribute("apellidoPaterno", data.get("apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("apellidoMaterno"));
		or.addDoc().addElement("transfer")
					.addAtribute("originalReference", folio)
					.addAtribute("transferNo", data.get("transferNo"))
					.addAtribute("proveedor", data.get("proveedor"))
					.addAtribute("marca", data.get("marca"))
					.addAtribute("firstId", data.get("firstId"))
					.addAtribute("firstIdnum", data.get("firstIdnum"))
					.addAtribute("countryCode", data.get("countryCode"))
					.addAtribute("currencyCode", data.get("currencyCode"));
		
		String request = or.getRequestString();
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	public String ejecutarQRY03() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY03");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));				
		
		or.addDoc().addElement("transfer")
			.addAtribute("transferNo", data.get("transferNo"))
			.addAtribute("telefono", data.get("telefono"));

        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN01() throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN01_WU() throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_QA10_VIRTUAL);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
	//	testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN02(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", creationDate.substring(0,8));
		or.addHeader("pvDate", creationDate);
		or.addHeader("creationDate", creationDate);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("transfer")
			.addAtribute("transferNo", data.get("transferNo"))
			.addAtribute("amount", data.get("amount"))
			.addAtribute("type", data.get("type"))
			.addAtribute("telefono", data.get("telefono"))
		.addElement("client")
			.addAtribute("nombre", data.get("cliente_nombre"))
			.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
			.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"))
			.addAtribute("noId", data.get("cliente_noId"))
			.addAtribute("calle", data.get("cliente_calle"))
			.addAtribute("num_interior", data.get("cliente_num_interior"))
			.addAtribute("num_exterior", data.get("cliente_num_exterior"))
			.addAtribute("cp", data.get("cliente_cp"))
			.addAtribute("colonia", data.get("cliente_colonia"))
			.addAtribute("municipio", data.get("cliente_municipio"))
			.addAtribute("estado", data.get("cliente_estado"))
			.addAtribute("pais", data.get("cliente_pais"))
			.addAtribute("update", data.get("cliente_update"))
		.addElement("destinatario")
			.addAtribute("id", data.get("destinatario_id"))
			.addAtribute("control", data.get("destinatario_control"))
			.addAtribute("nombre", data.get("destinatario_nombre"))
			.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno"))
			.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno"))
		;		
        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN02_1(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", creationDate.substring(0,8));
		or.addHeader("pvDate", creationDate);
		or.addHeader("creationDate", creationDate);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("transfer")
		.addAtribute("transferNo", data.get("transferNo"))
			.addAtribute("amount", data.get("amount"))
			.addAtribute("type", data.get("type"))
			.addAtribute("telefono", data.get("telefono"))
			.addElement("client")
				.addAtribute("nombre", data.get("cliente_nombre"))
				.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"))
				.addAtribute("noId", data.get("cliente_noId"))
				.addAtribute("calle", data.get("cliente_calle"))
				.addAtribute("num_interior", data.get("cliente_num_interior"))
				.addAtribute("num_exterior", data.get("cliente_num_exterior"))
				.addAtribute("cp", data.get("cliente_cp"))
				.addAtribute("colonia", data.get("cliente_colonia"))
				.addAtribute("municipio", data.get("cliente_municipio"))
				.addAtribute("estado", data.get("cliente_estado"))
				.addAtribute("pais", data.get("cliente_pais"))
				.addAtribute("update", data.get("cliente_update"))
				.addElement("destinatario")
					.addAtribute("id", data.get("destinatario_id"))
					.addAtribute("control", data.get("destinatario_control"))
					.addAtribute("nombre", data.get("destinatario_nombre"))
					.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno"))
					.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno"))
				.addElement("destinatario")
					.addAtribute("id", data.get("destinatario_id2"))
					.addAtribute("control", data.get("destinatario_control2"))
					.addAtribute("nombre", data.get("destinatario_nombre2"))
					.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno2"))
					.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno2"))
		;		
        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN02_2(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", creationDate.substring(0,8));
		or.addHeader("pvDate", creationDate);
		or.addHeader("creationDate", creationDate);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("transfer")
			.addAtribute("transferNo", data.get("transferNo"))
			.addAtribute("amount", data.get("amount"))
			.addAtribute("type", data.get("type"))
			.addAtribute("telefono", data.get("telefono"))
			.addElement("client")
				.addAtribute("nombre", data.get("cliente_nombre"))
				.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"))
				.addAtribute("noId", data.get("cliente_noId"))
				.addElement("destinatario")
					.addAtribute("id", data.get("destinatario_id"))
					.addAtribute("control", data.get("destinatario_control"))
					.addAtribute("nombre", data.get("destinatario_nombre"))
					.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno"))
					.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno"))
				.addElement("destinatario")
					.addAtribute("id", data.get("destinatario_id"))
					.addAtribute("control", data.get("destinatario_control"))
					.addAtribute("nombre", data.get("destinatario_nombre"))
					.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno"))
					.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno"))
		;		
        
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();		
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN02_MG(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("application", data.get("application"));
		or.addHeader("adDate", data.get("adDate"));
		or.addHeader("folio", folio);
		or.addHeader("pvDate", data.get("pvDate"));
		or.addHeader("source", data.get("source"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("creationDate", creationDate);
		or.addHeader("operation", "TRN02");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operator", data.get("operator"));
		
		or.addDoc()
		.addElement("transfer")
			.addAtribute("agentCheckAmount", data.get("agentCheckAmount"))
			.addAtribute("amount", data.get("amount"))
			.addAtribute("cashAmount", data.get("cashAmount"))
			.addAtribute("commission", data.get("commission"))
			.addAtribute("countryCode", data.get("countryCode"))
			.addAtribute("eAmount", data.get("eAmount"))
			.addAtribute("ePayment", data.get("ePayment"))
			.addAtribute("ePaymentSaldazo", data.get("ePaymentSaldazo"))
			.addAtribute("isNewSaldazo", data.get("isNewSaldazo"))
			.addAtribute("marca", data.get("marca"))
			.addAtribute("new_mtcn", data.get("new_mtcn"))
			.addAtribute("originalReference", data.get("originalReference"))
			.addAtribute("originalSendFee", data.get("originalSendFee"))
			.addAtribute("originatingAmount", data.get("originatingAmount"))
			.addAtribute("originatingCountry", data.get("originatingCountry"))
			.addAtribute("originatingCurrency", data.get("originatingCurrency"))
			.addAtribute("paymentCurrency", data.get("paymentCurrency"))
			.addAtribute("paymentTax", data.get("paymentTax"))
			.addAtribute("paymentType", data.get("paymentType"))
			.addAtribute("proveedor", data.get("proveedor"))
			.addAtribute("saldazoCharge", data.get("saldazoCharge"))
			.addAtribute("transferId", data.get("transferID"))
			.addAtribute("transferNo", data.get("transferNo"))
		.addElement("operator")
			.addAtribute("apPaternoOperador", data.get("apPaternoOperador"))
			.addAtribute("idOperador", data.get("idOperador"))
			.addAtribute("nombreOperador", data.get("nombreOperador"))
			.addAtribute("tipoNomOperador", data.get("tipoNomOperador"))
		.addElement("client")
		.addElement("receiver")
			.addAtribute("apellidoMaterno", data.get("apellidoMaterno"))
			.addAtribute("apellidoPaterno", data.get("apellidoPaterno"))
			.addAtribute("nombre", data.get("nombre"))
		.addElement("compliance")
			.addAtribute("calleNum", data.get("calleNum"))
			.addAtribute("ciudad", data.get("ciudad"))
			.addAtribute("codigoPostal", data.get("codigoPostal"))
			.addAtribute("colonia", data.get("colonia"))
			.addAtribute("estado", data.get("estado"))
			.addAtribute("estadoId", data.get("estadoId"))
			.addAtribute("expDateFirstId", data.get("expDateFirstId"))
			.addAtribute("expiracionFirstId", data.get("expiracionFirstId"))
			.addAtribute("fechaNacimiento", data.get("fechaNacimiento"))
			.addAtribute("firstId", data.get("firstId"))
			.addAtribute("firstIdnum", data.get("firstIdnum"))
			.addAtribute("pais", data.get("pais"))
			.addAtribute("paisId", data.get("paisId"))
			.addAtribute("paisNacimiento", data.get("paisNacimiento"))
			.addAtribute("paisNacionalidad", data.get("paisNacionalidad"))
			.addAtribute("telefono", data.get("telefono"));
			
		String request = or.getRequestString();
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest(request);
		testCase.addTextEvidenceCurrentStep(response);
		return response;
		
	}
	
	public String ejecutarTRN03(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String host = PasswordUtil.decryptPassword(data.get("host"));
		String url = String.format(requestUrl, host);
//		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN03");
		or.addHeader("folio", folio);
		or.addHeader("creationDate", creationDate);

		or.addDoc().addElement("ack").addAtribute("value", data.get("ack"));
		
        String request = or.getRequestString();		
		System.out.println(request);
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest();	
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
	public String ejecutarTRN04(String folio, String creationDate) throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, GlobalVariables.HOST_OLS);		
		OxxoRequest or = new OxxoRequest(url);
		
		or.addHeader("folio", folio);
		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN04");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", creationDate.substring(0,8));
		or.addHeader("pvDate", creationDate);
		or.addHeader("operator", data.get("operator"));

		or.addDoc()
		.addElement("request")
			.addElement("transfer")
				.addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("amount", data.get("amount"))
				.addAtribute("marca", data.get("marca"))
				.addAtribute("originalReference", data.get("aditional_data"))
		;		
				
		or.addDoc()
		.addElement("wmCodeList")		
			.addElement("wmCode")
				.addAtribute("auth", data.get("auth"))
				.addAtribute("creationDate", creationDate)
				.addAtribute("folio", folio);


        String request = or.makeRequestString();		
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep(request);
		
		String response = or.executeRequest(request);		
		testCase.addTextEvidenceCurrentStep(response);		
		return response;		
	}
	
}
