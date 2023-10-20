package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

public class TPE_TSF2 {

	public String requestUrl = "http://%s/invoke/TPE.TSF.Pub/request?xmlIn=";

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato de la fecha

	
	public TPE_TSF2(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

	public String ejecutarQRY01WesterUnion() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		or.addDoc().addElement("transfer").addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("proveedor", data.get("proveedor")).addAtribute("marca", data.get("marca"))
				.addAtribute("countryCode", data.get("countryCode"))
				.addAtribute("currencyCode", data.get("currencyCode"));

		or.addDocumentToRequest(doc);

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarQRY01MoneyGram() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		or.addDoc().addElement("transfer").addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("proveedor", data.get("proveedor")).addAtribute("countryCode", data.get("countryCode"));

		or.addDocumentToRequest(doc);

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarQRY01() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY01");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		or.addDoc().addElement("transfer").addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("proveedor", data.get("proveedor")).addElement("operator")
				.addAtribute("idOperador", data.get("operator"));

		or.addDocumentToRequest(doc);

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarQRY02WesterUnion(String folioQRY01) throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		or.addDoc().addElement("client").addElement("receiver")
				.addAtribute("tipoNombre", data.get("cliente_tipoNombre"))
				.addAtribute("nombre", data.get("cliente_nombre"))
				.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"));
		or.addDoc().addElement("transfer").addAtribute("originalReference", folioQRY01)
				.addAtribute("transferNo", data.get("transferNo")).addAtribute("proveedor", data.get("proveedor"))
				.addAtribute("marca", data.get("marca")).addAtribute("firstId", data.get("firstId"))
				.addAtribute("firstIdnum", data.get("firstIdnum")).addAtribute("countryCode", data.get("countryCode"))
				.addAtribute("currencyCode", data.get("currencyCode"));

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarQRY02() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		or.addDoc().addElement("client").addElement("receiver").addAtribute("nombre", data.get("cliente_nombre"))
				.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"));
		or.addDoc().addElement("transfer").addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("proveedor", data.get("proveedor")).addAtribute("firstId", data.get("firstId"))
				.addAtribute("firstIdnum", data.get("firstIdnum")).addAtribute("countryCode", data.get("countryCode"));

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarQRY03() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY03");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));

		or.addDoc().addElement("transfer")
		.addAtribute("transferNo", data.get("transferNo"))
		.addAtribute("telefono", data.get("telefono"));

		

		// -------
		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}
	
	public String ejecutarQRY03WesterUnion() throws ParserConfigurationException, ClientProtocolException, IOException {
		String fechaTexto = formatter.format(fecha);
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "QRY03");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", fechaTexto.substring(0, 8));
		or.addHeader("pvDate", fechaTexto);
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		Element transfer = doc.createElement("transfer");
		doc.appendChild(transfer);

		Attr attrTransfer = doc.createAttribute("transferNo");
		attrTransfer.setValue(data.get("transferNo"));
		transfer.setAttributeNode(attrTransfer);

		Attr attrTelefono = doc.createAttribute("telefono");
		attrTelefono.setValue(data.get("telefono"));
		transfer.setAttributeNode(attrTelefono);

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN01() throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));

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
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN02WesterUnion(String folio, String date, String folioQRY01)
			throws ParserConfigurationException, ClientProtocolException, IOException, ReempRequestException {
		String url = String.format(requestUrl, data.get("host"));
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", date);
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		Element transfer = doc.createElement("transfer");
		doc.appendChild(transfer);

		Attr originalReference = doc.createAttribute("originalReference");
		originalReference.setValue(folioQRY01);
		transfer.setAttributeNode(originalReference);

		Attr attrTransfer = doc.createAttribute("transferNo");
		attrTransfer.setValue(data.get("transferNo"));
		transfer.setAttributeNode(attrTransfer);

		Attr proveedor = doc.createAttribute("proveedor");
		proveedor.setValue(data.get("proveedor"));
		transfer.setAttributeNode(proveedor);

		Attr marca = doc.createAttribute("marca");
		marca.setValue(data.get("marca"));
		transfer.setAttributeNode(marca);

		Attr payStatus = doc.createAttribute("payStatus");
		payStatus.setValue(data.get("payStatus"));
		transfer.setAttributeNode(payStatus);

		Attr estatus = doc.createAttribute("estatus");
		estatus.setValue(data.get("estatus"));
		transfer.setAttributeNode(estatus);

		Attr countryCode = doc.createAttribute("countryCode");
		countryCode.setValue(data.get("countryCode"));
		transfer.setAttributeNode(countryCode);

		Attr currencyCode = doc.createAttribute("currencyCode");
		currencyCode.setValue(data.get("currencyCode"));
		transfer.setAttributeNode(currencyCode);

		Attr originatingCurrencyCode = doc.createAttribute("originatingCurrencyCode");
		originatingCurrencyCode.setValue(data.get("originatingCurrencyCode"));
		transfer.setAttributeNode(originatingCurrencyCode);

		Attr originatingCountryCode = doc.createAttribute("originatingCountryCode");
		originatingCountryCode.setValue(data.get("originatingCountryCode"));
		transfer.setAttributeNode(originatingCountryCode);

		Attr totalAmount = doc.createAttribute("totalAmount");
		totalAmount.setValue(data.get("totalAmount"));
		transfer.setAttributeNode(totalAmount);

		Attr amount = doc.createAttribute("amount");
		amount.setValue(data.get("amount"));
		transfer.setAttributeNode(amount);

		Attr principalAmount = doc.createAttribute("principalAmount");
		principalAmount.setValue(data.get("principalAmount"));
		transfer.setAttributeNode(principalAmount);

		Attr charges = doc.createAttribute("charges");
		charges.setValue(data.get("charges"));
		transfer.setAttributeNode(charges);

		Attr moneyTransferKey = doc.createAttribute("moneyTransferKey");
		moneyTransferKey.setValue(data.get("moneyTransferKey"));
		transfer.setAttributeNode(moneyTransferKey);

		Attr new_mtcn = doc.createAttribute("new_mtcn");
		new_mtcn.setValue(data.get("new_mtcn"));
		transfer.setAttributeNode(new_mtcn);

		Attr filingDate = doc.createAttribute("filingDate");
		filingDate.setValue(data.get("filingDate"));
		transfer.setAttributeNode(filingDate);

		Attr originatingCity = doc.createAttribute("originatingCity");
		originatingCity.setValue(data.get("originatingCity"));
		transfer.setAttributeNode(originatingCity);

		Attr originatingState = doc.createAttribute("originatingState");
		originatingState.setValue(data.get("originatingState"));
		transfer.setAttributeNode(originatingState);

		Attr paymentId = doc.createAttribute("paymentId");
		paymentId.setValue(data.get("paymentId"));
		transfer.setAttributeNode(paymentId);

		Attr ePayment = doc.createAttribute("ePayment");
		ePayment.setValue(data.get("ePayment"));
		transfer.setAttributeNode(ePayment);

		Attr ePaymentSaldazo = doc.createAttribute("ePaymentSaldazo");
		ePaymentSaldazo.setValue(data.get("ePaymentSaldazo"));
		transfer.setAttributeNode(ePaymentSaldazo);

		Attr isNewSaldazo = doc.createAttribute("isNewSaldazo");
		isNewSaldazo.setValue(data.get("isNewSaldazo"));
		transfer.setAttributeNode(isNewSaldazo);

		Attr saldazoCharge = doc.createAttribute("saldazoCharge");
		saldazoCharge.setValue(data.get("saldazoCharge"));
		transfer.setAttributeNode(saldazoCharge);

		Attr commission = doc.createAttribute("commission");
		commission.setValue(data.get("commission"));
		transfer.setAttributeNode(commission);

		Attr cashAmount = doc.createAttribute("cashAmount");
		cashAmount.setValue(data.get("cashAmount"));
		transfer.setAttributeNode(cashAmount);

		Attr eAmount = doc.createAttribute("eAmount");
		eAmount.setValue(data.get("eAmount"));
		transfer.setAttributeNode(eAmount);

		// --------

		Element operador = doc.createElement("operator");
		transfer.appendChild(operador);

		Attr idOperador = doc.createAttribute("idOperador");
		idOperador.setValue(data.get("operator"));
		operador.setAttributeNode(idOperador);

		Attr tipoNomOperador = doc.createAttribute("tipoNomOperador");
		tipoNomOperador.setValue(data.get("tipoNomOperador"));
		operador.setAttributeNode(tipoNomOperador);

		Attr nombreOperador = doc.createAttribute("nombreOperador");
		nombreOperador.setValue(data.get("nombreOperador"));
		operador.setAttributeNode(nombreOperador);

		Attr apPaternoOperador = doc.createAttribute("apPaternoOperador");
		apPaternoOperador.setValue(data.get("apPaternoOperador"));
		operador.setAttributeNode(apPaternoOperador);

		Attr apMaternoOperador = doc.createAttribute("apMaternoOperador");
		apMaternoOperador.setValue(data.get("apMaternoOperador"));
		operador.setAttributeNode(apMaternoOperador);

		// -------

		Element client = doc.createElement("client");
		transfer.appendChild(client);

		Element compliance = doc.createElement("compliance");
		client.appendChild(compliance);

		Attr firstId = doc.createAttribute("firstId");
		firstId.setValue(data.get("firstId"));
		compliance.setAttributeNode(firstId);

		Attr firstIdnum = doc.createAttribute("firstIdnum");
		firstIdnum.setValue(data.get("firstIdnum"));
		compliance.setAttributeNode(firstIdnum);

		Attr paisId = doc.createAttribute("paisId");
		paisId.setValue(data.get("paisId"));
		compliance.setAttributeNode(paisId);

		Attr expDateFirstId = doc.createAttribute("expDateFirstId");
		expDateFirstId.setValue(data.get("expDateFirstId"));
		compliance.setAttributeNode(expDateFirstId);

		Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");
		fechaNacimiento.setValue(data.get("fechaNacimiento"));
		compliance.setAttributeNode(fechaNacimiento);

		Attr calleNum = doc.createAttribute("calleNum");
		calleNum.setValue(data.get("calleNum"));
		compliance.setAttributeNode(calleNum);

		Attr colonia = doc.createAttribute("colonia");
		colonia.setValue(data.get("colonia"));
		compliance.setAttributeNode(colonia);

		Attr ciudad = doc.createAttribute("ciudad");
		ciudad.setValue(data.get("ciudad"));
		compliance.setAttributeNode(ciudad);

		Attr estado = doc.createAttribute("estado");
		estado.setValue(data.get("estado"));
		compliance.setAttributeNode(estado);

		Attr codigoPostal = doc.createAttribute("codigoPostal");
		codigoPostal.setValue(data.get("codigoPostal"));
		compliance.setAttributeNode(codigoPostal);

		Attr pais = doc.createAttribute("pais");
		pais.setValue(data.get("pais"));
		compliance.setAttributeNode(pais);

		Attr telefono = doc.createAttribute("telefono");
		telefono.setValue(data.get("telefono"));
		compliance.setAttributeNode(telefono);

		Attr paisNacimiento = doc.createAttribute("paisNacimiento");
		paisNacimiento.setValue(data.get("paisNacimiento"));
		compliance.setAttributeNode(paisNacimiento);

		Attr paisNacionalidad = doc.createAttribute("paisNacionalidad");
		paisNacionalidad.setValue(data.get("paisNacionalidad"));
		compliance.setAttributeNode(paisNacionalidad);

		Attr expiracionFirstId = doc.createAttribute("expiracionFirstId");
		expiracionFirstId.setValue(data.get("expiracionFirstId"));
		compliance.setAttributeNode(expiracionFirstId);

		Element receiver = doc.createElement("receiver");
		client.appendChild(receiver);

		Attr tipoNombre = doc.createAttribute("tipoNombre");
		tipoNombre.setValue(data.get("cliente_tipoNombre"));
		receiver.setAttributeNode(tipoNombre);

		Attr nombre = doc.createAttribute("nombre");
		nombre.setValue(data.get("cliente_nombre"));
		receiver.setAttributeNode(nombre);

		Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");
		apellidoPaterno.setValue(data.get("cliente_apellidoPaterno"));
		receiver.setAttributeNode(apellidoPaterno);

		Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");
		apellidoMaterno.setValue(data.get("cliente_apellidoMaterno"));
		receiver.setAttributeNode(apellidoMaterno);

		// -------

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN02MoneyGram(String folio, String date, String folioQRY01)
			throws ParserConfigurationException, ClientProtocolException, IOException, ReempRequestException {
		String url = String.format(requestUrl, data.get("host"));
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", date);
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		Element transfer = doc.createElement("transfer");
		doc.appendChild(transfer);

		Attr attrTransfer = doc.createAttribute("transferNo");
		attrTransfer.setValue(data.get("transferNo"));
		transfer.setAttributeNode(attrTransfer);

		Attr proveedor = doc.createAttribute("proveedor");
		proveedor.setValue(data.get("proveedor"));
		transfer.setAttributeNode(proveedor);

		Attr marca = doc.createAttribute("marca");
		marca.setValue(data.get("marca"));
		transfer.setAttributeNode(marca);

		Attr originalReference = doc.createAttribute("originalReference");
		originalReference.setValue(folioQRY01);
		transfer.setAttributeNode(originalReference);

		Attr paymentTax = doc.createAttribute("paymentTax");
		paymentTax.setValue(data.get("paymentTax"));
		transfer.setAttributeNode(paymentTax);

		Attr transferID = doc.createAttribute("transferID");
		transferID.setValue(data.get("transferID"));
		transfer.setAttributeNode(transferID);

		Attr countryCode = doc.createAttribute("countryCode");
		countryCode.setValue(data.get("countryCode"));
		transfer.setAttributeNode(countryCode);

		Attr paymentCurrency = doc.createAttribute("paymentCurrency");
		paymentCurrency.setValue(data.get("paymentCurrency"));
		transfer.setAttributeNode(paymentCurrency);

		Attr originatingCurrency = doc.createAttribute("originatingCurrency");
		originatingCurrency.setValue(data.get("originatingCurrency"));
		transfer.setAttributeNode(originatingCurrency);

		Attr originatingAmount = doc.createAttribute("originatingAmount");
		originatingAmount.setValue(data.get("originatingAmount"));
		transfer.setAttributeNode(originatingAmount);

		Attr amount = doc.createAttribute("amount");
		amount.setValue(data.get("amount"));
		transfer.setAttributeNode(amount);

		Attr agentCheckAmount = doc.createAttribute("agentCheckAmount");
		agentCheckAmount.setValue(data.get("agentCheckAmount"));
		transfer.setAttributeNode(agentCheckAmount);

		Attr originalSendFee = doc.createAttribute("originalSendFee");
		originalSendFee.setValue(data.get("originalSendFee"));
		transfer.setAttributeNode(originalSendFee);

		Attr new_mtcn = doc.createAttribute("new_mtcn");
		new_mtcn.setValue(data.get("new_mtcn"));
		transfer.setAttributeNode(new_mtcn);

		Attr ePaymentSaldazo = doc.createAttribute("ePaymentSaldazo");
		ePaymentSaldazo.setValue(data.get("ePaymentSaldazo"));
		transfer.setAttributeNode(ePaymentSaldazo);

		Attr isNewSaldazo = doc.createAttribute("isNewSaldazo");
		isNewSaldazo.setValue(data.get("isNewSaldazo"));
		transfer.setAttributeNode(isNewSaldazo);

		Attr saldazoCharge = doc.createAttribute("saldazoCharge");
		saldazoCharge.setValue(data.get("saldazoCharge"));
		transfer.setAttributeNode(saldazoCharge);

		Attr commission = doc.createAttribute("commission");
		commission.setValue(data.get("commission"));
		transfer.setAttributeNode(commission);

		Attr cashAmount = doc.createAttribute("cashAmount");
		cashAmount.setValue(data.get("cashAmount"));
		transfer.setAttributeNode(cashAmount);

		Attr eAmount = doc.createAttribute("eAmount");
		eAmount.setValue(data.get("eAmount"));
		transfer.setAttributeNode(eAmount);

		Attr paymentType = doc.createAttribute("paymentType");
		paymentType.setValue(data.get("paymentType"));
		transfer.setAttributeNode(paymentType);

		Attr ePayment = doc.createAttribute("ePayment");
		ePayment.setValue(data.get("ePayment"));
		transfer.setAttributeNode(ePayment);

		Attr originatingCountry = doc.createAttribute("originatingCountry");
		originatingCountry.setValue(data.get("originatingCountry"));
		transfer.setAttributeNode(originatingCountry);

		// --------

		Element operador = doc.createElement("operator");
		transfer.appendChild(operador);

		Attr idOperador = doc.createAttribute("idOperador");
		idOperador.setValue(data.get("operator"));
		operador.setAttributeNode(idOperador);

		Attr tipoNomOperador = doc.createAttribute("tipoNomOperador");
		tipoNomOperador.setValue(data.get("tipoNomOperador"));
		operador.setAttributeNode(tipoNomOperador);

		Attr nombreOperador = doc.createAttribute("nombreOperador");
		nombreOperador.setValue(data.get("nombreOperador"));
		operador.setAttributeNode(nombreOperador);

		Attr apPaternoOperador = doc.createAttribute("apPaternoOperador");
		apPaternoOperador.setValue(data.get("apPaternoOperador"));
		operador.setAttributeNode(apPaternoOperador);

		// -------

		Element client = doc.createElement("client");
		transfer.appendChild(client);

		Element receiver = doc.createElement("receiver");
		client.appendChild(receiver);

		Attr nombre = doc.createAttribute("nombre");
		nombre.setValue(data.get("nombre"));
		receiver.setAttributeNode(nombre);

		Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");
		apellidoPaterno.setValue(data.get("apellidoPaterno"));
		receiver.setAttributeNode(apellidoPaterno);

		Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");
		apellidoMaterno.setValue(data.get("apellidoMaterno"));
		receiver.setAttributeNode(apellidoMaterno);

		Element compliance = doc.createElement("compliance");
		client.appendChild(compliance);

		Attr firstId = doc.createAttribute("firstId");
		firstId.setValue(data.get("firstId"));
		compliance.setAttributeNode(firstId);

		Attr firstIdnum = doc.createAttribute("firstIdnum");
		firstIdnum.setValue(data.get("firstIdnum"));
		compliance.setAttributeNode(firstIdnum);

		Attr paisId = doc.createAttribute("paisId");
		paisId.setValue(data.get("paisId"));
		compliance.setAttributeNode(paisId);

		Attr expDateFirstId = doc.createAttribute("expDateFirstId");
		expDateFirstId.setValue(data.get("expDateFirstId"));
		compliance.setAttributeNode(expDateFirstId);

		Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");
		fechaNacimiento.setValue(data.get("fechaNacimiento"));
		compliance.setAttributeNode(fechaNacimiento);

		Attr calleNum = doc.createAttribute("calleNum");
		calleNum.setValue(data.get("calleNum"));
		compliance.setAttributeNode(calleNum);

		Attr colonia = doc.createAttribute("colonia");
		colonia.setValue(data.get("colonia"));
		compliance.setAttributeNode(colonia);

		Attr ciudad = doc.createAttribute("ciudad");
		ciudad.setValue(data.get("ciudad"));
		compliance.setAttributeNode(ciudad);

		Attr estado = doc.createAttribute("estado");
		estado.setValue(data.get("estado"));
		compliance.setAttributeNode(estado);

		Attr estadoId = doc.createAttribute("estadoId");
		estadoId.setValue(data.get("estadoId"));
		compliance.setAttributeNode(estadoId);

		Attr codigoPostal = doc.createAttribute("codigoPostal");
		codigoPostal.setValue(data.get("codigoPostal"));
		compliance.setAttributeNode(codigoPostal);

		Attr pais = doc.createAttribute("pais");
		pais.setValue(data.get("pais"));
		compliance.setAttributeNode(pais);

		Attr telefono = doc.createAttribute("telefono");
		telefono.setValue(data.get("telefono"));
		compliance.setAttributeNode(telefono);

		Attr paisNacimiento = doc.createAttribute("paisNacimiento");
		paisNacimiento.setValue(data.get("paisNacimiento"));
		compliance.setAttributeNode(paisNacimiento);

		Attr paisNacionalidad = doc.createAttribute("paisNacionalidad");
		paisNacionalidad.setValue(data.get("paisNacionalidad"));
		compliance.setAttributeNode(paisNacionalidad);

		Attr expiracionFirstId = doc.createAttribute("expiracionFirstId");
		expiracionFirstId.setValue(data.get("expiracionFirstId"));
		compliance.setAttributeNode(expiracionFirstId);

		// -------

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
	    //testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN02TransNetwork(String folio, String date)
			throws ParserConfigurationException, ClientProtocolException, IOException, ReempRequestException {
		String url = String.format(requestUrl, data.get("host"));
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", date);
		// or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("operator", data.get("operator"));

		Document doc = or.addNewDocument();

		Element transfer = doc.createElement("transfer");
		doc.appendChild(transfer);

		Attr attrTransfer = doc.createAttribute("transferNo");
		attrTransfer.setValue(data.get("transferNo"));
		transfer.setAttributeNode(attrTransfer);

		Attr proveedor = doc.createAttribute("proveedor");
		proveedor.setValue(data.get("proveedor"));
		transfer.setAttributeNode(proveedor);

		Attr countryCode = doc.createAttribute("countryCode");
		countryCode.setValue(data.get("countryCode"));
		transfer.setAttributeNode(countryCode);

		Attr paymentTax = doc.createAttribute("paymentTax");
		paymentTax.setValue(data.get("paymentTax"));
		transfer.setAttributeNode(paymentTax);

		Attr transferID = doc.createAttribute("transferID");
		transferID.setValue(data.get("transferID"));
		transfer.setAttributeNode(transferID);

		Attr new_mtcn = doc.createAttribute("new_mtcn");
		new_mtcn.setValue(data.get("new_mtcn"));
		transfer.setAttributeNode(new_mtcn);

		Attr amount = doc.createAttribute("amount");
		amount.setValue(data.get("amount"));
		transfer.setAttributeNode(amount);

		Attr marca = doc.createAttribute("marca");
		marca.setValue(data.get("marca"));
		transfer.setAttributeNode(marca);

		Attr ePaymentSaldazo = doc.createAttribute("ePaymentSaldazo");
		ePaymentSaldazo.setValue(data.get("ePaymentSaldazo"));
		transfer.setAttributeNode(ePaymentSaldazo);

		Attr isNewSaldazo = doc.createAttribute("isNewSaldazo");
		isNewSaldazo.setValue(data.get("isNewSaldazo"));
		transfer.setAttributeNode(isNewSaldazo);

		Attr saldazoCharge = doc.createAttribute("saldazoCharge");
		saldazoCharge.setValue(data.get("saldazoCharge"));
		transfer.setAttributeNode(saldazoCharge);

		Attr commission = doc.createAttribute("commission");
		commission.setValue(data.get("commission"));
		transfer.setAttributeNode(commission);

		Attr cashAmount = doc.createAttribute("cashAmount");
		cashAmount.setValue(data.get("cashAmount"));
		transfer.setAttributeNode(cashAmount);

		Attr eAmount = doc.createAttribute("eAmount");
		eAmount.setValue(data.get("eAmount"));
		transfer.setAttributeNode(eAmount);

		Attr originatingCurrency = doc.createAttribute("originatingCurrency");
		originatingCurrency.setValue(data.get("originatingCurrency"));
		transfer.setAttributeNode(originatingCurrency);

		Attr currencyCode = doc.createAttribute("currencyCode");
		currencyCode.setValue(data.get("currencyCode"));
		transfer.setAttributeNode(currencyCode);

		Attr originatingAmount = doc.createAttribute("originatingAmount");
		originatingAmount.setValue(data.get("originatingAmount"));
		transfer.setAttributeNode(originatingAmount);

		Attr ePayment = doc.createAttribute("ePayment");
		ePayment.setValue(data.get("ePayment"));
		transfer.setAttributeNode(ePayment);

		Attr originatingCity = doc.createAttribute("originatingCity");
		originatingCity.setValue(data.get("originatingCity"));
		transfer.setAttributeNode(originatingCity);

		Attr originatingState = doc.createAttribute("originatingState");
		originatingState.setValue(data.get("originatingState"));
		transfer.setAttributeNode(originatingState);

		Attr paymentType = doc.createAttribute("paymentType");
		paymentType.setValue(data.get("paymentType"));
		transfer.setAttributeNode(paymentType);

		Attr originalReference = doc.createAttribute("originalReference");
		originalReference.setValue(data.get("originalReference"));
		transfer.setAttributeNode(originalReference);

		// --------

		Element operador = doc.createElement("operator");
		transfer.appendChild(operador);

		Attr idOperador = doc.createAttribute("idOperador");
		idOperador.setValue(data.get("operator"));
		operador.setAttributeNode(idOperador);

		Attr tipoNomOperador = doc.createAttribute("tipoNomOperador");
		tipoNomOperador.setValue(data.get("tipoNomOperador"));
		operador.setAttributeNode(tipoNomOperador);

		Attr nombreOperador = doc.createAttribute("nombreOperador");
		nombreOperador.setValue(data.get("nombreOperador"));
		operador.setAttributeNode(nombreOperador);

		Attr nombreSegOperador = doc.createAttribute("nombreSegOperador");
		nombreSegOperador.setValue(data.get("nombreSegOperador"));
		operador.setAttributeNode(nombreSegOperador);

		Attr apPaternoOperador = doc.createAttribute("apPaternoOperador");
		apPaternoOperador.setValue(data.get("apPaternoOperador"));
		operador.setAttributeNode(apPaternoOperador);

		Attr apMaternoOperador = doc.createAttribute("apMaternoOperador");
		apMaternoOperador.setValue(data.get("apMaternoOperador"));
		operador.setAttributeNode(apMaternoOperador);

		// -------

		Element client = doc.createElement("client");
		transfer.appendChild(client);

		Element receiver = doc.createElement("receiver");
		client.appendChild(receiver);

		Attr nombre = doc.createAttribute("nombre");
		nombre.setValue(data.get("cliente_nombre"));
		receiver.setAttributeNode(nombre);

		Attr apellidoPaterno = doc.createAttribute("apellidoPaterno");
		apellidoPaterno.setValue(data.get("cliente_apellidoPaterno"));
		receiver.setAttributeNode(apellidoPaterno);

		Attr apellidoMaterno = doc.createAttribute("apellidoMaterno");
		apellidoMaterno.setValue(data.get("cliente_apellidoMaterno"));
		receiver.setAttributeNode(apellidoMaterno);

		Element compliance = doc.createElement("compliance");
		client.appendChild(compliance);

		Attr firstId = doc.createAttribute("firstId");
		firstId.setValue(data.get("firstId"));
		compliance.setAttributeNode(firstId);

		Attr firstIdnum = doc.createAttribute("firstIdnum");
		firstIdnum.setValue(data.get("firstIdnum"));
		compliance.setAttributeNode(firstIdnum);

		Attr paisId = doc.createAttribute("paisId");
		paisId.setValue(data.get("pais"));
		compliance.setAttributeNode(paisId);

		Attr secondId = doc.createAttribute("secondId");
		secondId.setValue(data.get("secondId"));
		compliance.setAttributeNode(secondId);

		Attr secondIdnum = doc.createAttribute("secondIdnum");
		secondIdnum.setValue(data.get("secondIdnum"));
		compliance.setAttributeNode(secondIdnum);

		Attr paisSecondId = doc.createAttribute("paisSecondId");
		paisSecondId.setValue(data.get("paisSecondId"));
		compliance.setAttributeNode(paisSecondId);

		Attr expDateFirstId = doc.createAttribute("expDateFirstId");
		expDateFirstId.setValue(data.get("expDateFirstId"));
		compliance.setAttributeNode(expDateFirstId);

		Attr fechaNacimiento = doc.createAttribute("fechaNacimiento");
		fechaNacimiento.setValue(data.get("fechaNacimiento"));
		compliance.setAttributeNode(fechaNacimiento);

		Attr calleNum = doc.createAttribute("calleNum");
		calleNum.setValue(data.get("calleNum"));
		compliance.setAttributeNode(calleNum);

		Attr colonia = doc.createAttribute("colonia");
		colonia.setValue(data.get("colonia"));
		compliance.setAttributeNode(colonia);

		Attr ciudad = doc.createAttribute("ciudad");
		ciudad.setValue(data.get("ciudad"));
		compliance.setAttributeNode(ciudad);

		Attr estado = doc.createAttribute("estado");
		estado.setValue(data.get("estado"));
		compliance.setAttributeNode(estado);

		Attr estadoId = doc.createAttribute("estadoId");
		estadoId.setValue(data.get("estadoId"));
		compliance.setAttributeNode(estadoId);

		Attr codigoPostal = doc.createAttribute("codigoPostal");
		codigoPostal.setValue(data.get("codigoPostal"));
		compliance.setAttributeNode(codigoPostal);

		Attr pais = doc.createAttribute("pais");
		pais.setValue(data.get("pais"));
		compliance.setAttributeNode(pais);

		Attr telefono = doc.createAttribute("telefono");
		telefono.setValue(data.get("telefono"));
		compliance.setAttributeNode(telefono);

		Attr paisNacimiento = doc.createAttribute("paisNacimiento");
		paisNacimiento.setValue(data.get("paisNacimiento"));
		compliance.setAttributeNode(paisNacimiento);

		Attr paisNacionalidad = doc.createAttribute("paisNacionalidad");
		paisNacionalidad.setValue(data.get("paisNacionalidad"));
		compliance.setAttributeNode(paisNacionalidad);

		Attr expiracionFirstId = doc.createAttribute("expiracionFirstId");
		expiracionFirstId.setValue(data.get("expiracionFirstId"));
		compliance.setAttributeNode(expiracionFirstId);

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN02(String folio, String date)
			throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));
		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN02");
		or.addHeader("source", data.get("source"));
		or.addHeader("plaza", data.get("plaza"));
		or.addHeader("tienda", data.get("tienda"));
		or.addHeader("folio", folio);
		or.addHeader("caja", data.get("caja"));
		or.addHeader("adDate", date.substring(0, 8));
		or.addHeader("pvDate", date);
		or.addHeader("creationDate", date);
		or.addHeader("serviceId", data.get("serviceId"));
		or.addHeader("pvTicket", data.get("pvTicket"));
		or.addHeader("operator", data.get("operator"));

		or.addDoc().addElement("transfer").addAtribute("transferNo", data.get("transferNo"))
				.addAtribute("amount", data.get("amount")).addAtribute("type", data.get("type"))
				.addAtribute("telefono", data.get("telefono")).addElement("client")
				.addAtribute("nombre", data.get("cliente_nombre"))
				.addAtribute("apellidoPaterno", data.get("cliente_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("cliente_apellidoMaterno"))
				.addAtribute("noId", data.get("cliente_noId")).addAtribute("calle", data.get("cliente_calle"))
				.addAtribute("num_exterior", data.get("cliente_num_exterior")).addAtribute("cp", data.get("cliente_cp"))
				.addAtribute("colonia", data.get("cliente_colonia"))
				.addAtribute("municipio", data.get("cliente_municipio"))
				.addAtribute("estado", data.get("cliente_estado")).addAtribute("pais", data.get("cliente_pais"))
				.addAtribute("update", data.get("cliente_update")).addElement("destinatario")
				.addAtribute("id", data.get("destinatario_id"))
				// .addAtribute("noId", data.get("destinatario_id"))
				.addAtribute("control", data.get("destinatario_control"))
				// .addAtribute("idType", "01")
				.addAtribute("nombre", data.get("destinatario_nombre"))
				.addAtribute("apellidoPaterno", data.get("destinatario_apellidoPaterno"))
				.addAtribute("apellidoMaterno", data.get("destinatario_apellidoMaterno"));

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

	public String ejecutarTRN03(String folio, String date)
			throws ParserConfigurationException, ClientProtocolException, IOException {
		String url = String.format(requestUrl, data.get("host"));

		OxxoRequest or = new OxxoRequest(url);

		or.addHeader("application", "TSF");
		or.addHeader("entity", data.get("entity"));
		or.addHeader("operation", "TRN03");
		or.addHeader("folio", folio);
		or.addHeader("creationDate", date);

		or.addDoc().addElement("ack").addAtribute("value", data.get("ack"));

		String request = or.getRequestString();

		System.out.println(request);
		//testCase.addCodeEvidenceCurrentStep("Solicitud: \n" + request);
		testCase.addTextEvidenceCurrentStep("Solicitud: \n" + request);
	
		String response = or.executeRequest();
		//testCase.addCodeEvidenceCurrentStep("Respuesta: \n" + response);
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + response);

		return response;

	}

}
