package om;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.password.PasswordUtil;
import utils.webmethods.GetRequest;

public class FL3 {
	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@10.182.32.14:5555/invoke/wm.tn:receive?$xmldata=%s";
	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;

	public FL3(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

	public String EjecutaFL3() throws ReempRequestException, ClientProtocolException, IOException {

		String REQ_PART_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "-<ACCEPT_LOAD_OXXO>\n" + "-<CNTROLAREA>\n"
				+ "-<BSR>\n" + "<VERB value=\"ACCEPT\">" + data.get("VERB") + "</VERB>\n" + "<NOUN value=\"LOAD\">"
				+ data.get("NOUN") + "</NOUN>\n" + "<REVISION value=\"001\">" + data.get("REVISION") + "</REVISION>\n"
				+ "</BSR>\n" + "-<SENDER>\n" + "<LOGICALID>" + data.get("LOGICALID") + "</LOGICALID>\n" + "<COMPONENT>"
				+ data.get("COMPONENT") + "</COMPONENT>\n" + "<TASK>" + data.get("TASK") + "</TASK>\n"
				+ "<REFERENCEID/>\n" + "<CONFIRMATION>" + data.get("CONFIRMATION") + "</CONFIRMATION>\n"
				+ "<LANGUAGE>ES-MX</LANGUAGE>\n" + "<CODEPAGE>UTF-8</CODEPAGE>\n" + "<AUTHID>" + data.get("AUTHID")
				+ "</AUTHID>\n" + "</SENDER>\n" + "-<DATETIME qualifier=\"CREATION\">\n" + "<YEAR>" + data.get("YEAR")
				+ "</YEAR>\n" + "<MONTH>" + data.get("MONTH") + "</MONTH>\n" + "<DAY>" + data.get("DAY") + "</DAY>\n"
				+ "<HOUR>" + data.get("HOUR") + "</HOUR>\n" + "<MINUTE>" + data.get("MINUTE") + "</MINUTE>\n"
				+ "<SECOND>" + data.get("SECOND") + "</SECOND>\n" + "<SUBSECOND>" + data.get("SUBSECOND")
				+ "</SUBSECOND>\n" + "<TIMEZONE>" + data.get("TIMEZONE") + "</TIMEZONE>\n" + "</DATETIME>\n"
				+ "</CNTROLAREA>\n" + "-<DATAAREA>\n" + "-<LOAD_HEADER>\n" + "-<LOAD_DETAIL>\n" + "<LOAD_DETAIL_ID>"
				+ data.get("LOAD_DETAIL_ID") + "</LOAD_DETAIL_ID>\n" + "<COMM_PO_NUMBER>" + data.get("COMM_PO_NUMBER")
				+ "</COMM_PO_NUMBER>\n" + "<BOL>" + data.get("BOL") + "</BOL>\n" + "</LOAD_DETAIL>\n"
				+ "-<LOAD_DETAIL>\n" + "<LOAD_DETAIL_ID>" + data.get("LOAD_DETAIL_ID2") + "</LOAD_DETAIL_ID>\n"
				+ "<COMM_PO_NUMBER>" + data.get("COMM_PO_NUMBER2") + "</COMM_PO_NUMBER>\n" + "<BOL>" + data.get("BOL2")
				+ "</BOL>\n" + "</LOAD_DETAIL>\n" // "+data.get()+"
				+ "-<LOAD_SCHEDULE>\n" + "<LOAD_ID>" + data.get("LOAD_ID") + "</LOAD_ID>\n" + "<LANE_NAME>"
				+ data.get("LANE_NAME") + "</LANE_NAME>\n" + "<CARRIER_NAME>" + data.get("CARRIER_NAME")
				+ "</CARRIER_NAME>\n" + "<PORT_OF_LOADING>" + data.get("PORT_OF_LOADING") + "</PORT_OF_LOADING>\n"
				+ "<EQUIPMENT_TYPE>" + data.get("EQUIPMENT_TYPE") + "</EQUIPMENT_TYPE>\n" + "<STATUS_CODE>"
				+ data.get("STATUS_CODE") + "</STATUS_CODE>\n" + "<CARRIER_REMARKS>" + data.get("CARRIER_REMARKS")
				+ "</CARRIER_REMARKS>\n" + "</LOAD_SCHEDULE>\n" + "</LOAD_HEADER>\n" + "</DATAAREA>\n"
				+ "</ACCEPT_LOAD_OXXO>";

		String requestEnc = (URLEncoder.encode(REQ_PART_1, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, requestEnc);
		System.out.println(REQ_PART_1);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

}
