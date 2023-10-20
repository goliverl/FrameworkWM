package om;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class TPE_TIC {
	
	public String request = "http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.TIC.Pub/request?xmlIn=%s";

	
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;


	
	public TPE_TIC(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	public String RunTPE_TIC_request() throws ReempRequestException, ClientProtocolException, IOException {
//		http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.TIC.Pub/request?xmlIn=
		
		String REQ_PART_1 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<TPEDoc version=\"1.0\">\n"
				+ "<header application=\"TIC\" entity=\"POS\" operation=\"QRY02\" creationDate=\"%s\" source=\"POS\" plaza=\"%s\" tienda=\"%s\" \n"
				+ "pvDate=\"%s\"/>\n"
				+ "<request>\n"
				+ "<ticket>\n"
				+ "<ticHeader version=\"1.0\" pvDocId=\"%s\" pvDocName=\"%s\" envelopeId=\"%s\" noRecords=\"1\" receivedDate=\"%s\">\n"
				+ "<ticDetail pvNumCaja=\"2\" pvCveMvt=\"12\" item=\"%s\" qty=\"1.000\" unitRetail=\"6.50\" pvFolio=\"%s\"\n"
				+ "pvFchMvt=\"%s\" pvHraMvt=\"17:03\" pvFolProm=\"0\" pvFactura=\"0\" fechaAdm=\"%s\" vtLlavero=\"\" pytaxRate=\"16.00;0.00\" pvIpeFolio=\"\" pvAtti=\"\"/>\n"
				+ "<ticDetail pvNumCaja=\"2\" pvCveMvt=\"98\" item=\"%s\" ty=\"1.000\" unitRetail=\"0.50\" pvFolio=\"%s\"\n"
				+ "pvFchMvt=\"%s\" pvHraMvt=\"17:03\" pvFolProm=\"O\" pvFactura=\"0\" fechaAdm=\"%s\" VtLlavero=\"\"\n"
				+ "pvTaxRate=\"\" pvTpeFolio=\"\" pvAtti=\"\"/>\n"
				+ "\n"
				+ "</ticHeader>\n"
				+ "</ticket>\n"
				+ "</request>\n"
				+ "</TPEDoc>";
		
		String requestFormat = String.format(REQ_PART_1, data.get("CreationDate"), data.get("plaza"),
				data.get("tienda"), data.get("CreationDate"), data.get("DocId"), data.get("DocName"), 
				data.get("DocId"),data.get("DateRecived"),data.get("item1"),data.get("DocId"),
				data.get("DateRecived"),data.get("DateRecived"),data.get("item2"),data.get("DocId"), 
				data.get("DateRecived"),data.get("DateRecived"));


		String requestEnc = (URLEncoder.encode(requestFormat, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(request, requestEnc);
		System.out.println(REQ_PART_1);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	
	
	public String RunTPE_TIC_Tickets(String Tienda, String Code) throws ReempRequestException, ClientProtocolException, IOException {
//		http://AutoPruebasIrving:pruebas.202@%s/invoke/TPE.TIC.Pub/request?xmlIn=
		
		String REQ_PART_1 ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<TPEDOC version=\"1.0\">\n"
				+ "<header application=\"TIC\" entity=\"POS\" operation=\"QRY01\" source=\"POS\" plaza=\"%s\" tienda=\"%s\" caja=\"1\"/>\n"
				+ "<request>\n"
				+ "<ticket>\n"
				+ "<tieHeader pvDocId=\"%s\"/>\n"
				+ "</ticket\n"
				+ "</request>\n"
				+ "</TPEDOc>";
		
		String requestFormat = String.format(REQ_PART_1, data.get("plaza"),Tienda, Code);


		String requestEnc = (URLEncoder.encode(requestFormat, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(request, requestEnc);
		System.out.println(REQ_PART_1);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}
	


}

