package om;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import utils.webmethods.GetRequest;

public class FL4 {
	public String requestUrl = "http://AutoPruebasIrving:pruebas.202@10.182.32.14:5555/invoke/wm.tn:receive?$xmldata=%s";

	HashMap<String, String> data;
	TestCase testCase;
	utils.sql.SQLUtil db;

	public FL4(HashMap<String, String> data, TestCase testCase, utils.sql.SQLUtil db) {
		super();
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}

	public String EjecutaFL4() throws ReempRequestException, ClientProtocolException, IOException {

		String REQ_PART_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "-<ROADNET_PLAN_001>\n-<CNTROLAREA>\n-<BSR>\n<VERB>ROADNET</VERB>\n"
				+ "<NOUN>PLAN</NOUN>\n<NOUN>PLAN</NOUN>\n<REVISION>1.1</REVISION>\n</BSR>\n"
				+ "-<SENDER>\n<LOGICALID>%s</LOGICALID>\n<REFERENCEID>%s</REFERENCEID>\n<AUTHID>%s</AUTHID>\n"
				+ "</SENDER>\n</CNTROLAREA>\n-<DATAAREA>\n-<HEADER>\n<CEDIS>%s</CEDIS>\n<NO_RECORDS>%s</NO_RECORDS>\n"
				+ "</HEADER>\n-<DETAIL>\n<PLAZA>%s</PLAZA>\n<TIPO_RUTA>%s</TIPO_RUTA>\n<RUTA_ORIGINAL>%s</RUTA_ORIGINAL>\n"
				+ "<RUTA_PROPUESTA>%s</RUTA_PROPUESTA>\n<DEST_ID>%s</DEST_ID>\n<CR_TIENDA>%s</CR_TIENDA>\n<TSF_NO>%s</TSF_NO>\n"
				+ "<CAJAS_TOTAL>%s</CAJAS_TOTAL>\n<CANASTILLAS_TOTAL>%s</CANASTILLAS_TOTAL>\n<VOLUMEN_CAJAS>%s</VOLUMEN_CAJAS>\n"
				+ "<VOLUMEN_CANASTILLAS>%s</VOLUMEN_CANASTILLAS>\n<PESO>%s</PESO>\n<FH_PMO_INICIO_CARGA>%s</FH_PMO_INICIO_CARGA>\n"
				+ "<FH_PMO_FIN_CARGA>%s</FH_PMO_FIN_CARGA>\n<FH_PMO_DESP>%s</FH_PMO_DESP>\n"
				+ "<FH_PMO_INI_VENT_ENTREGA>%s</FH_PMO_INI_VENT_ENTREGA>\n<FH_PMO_FIN_VENT_ENTREGA>%s</FH_PMO_FIN_VENT_ENTREGA>\n"
				+ "<SECUENCIA_CARGA>%s</SECUENCIA_CARGA>\n<SECUENCIA_ENTREGA>%s</SECUENCIA_ENTREGA>\n"
				+ "<FH_EST_INICIO_CARGA_PROP_FL>%s</FH_EST_INICIO_CARGA_PROP_FL>\n<FH_EST_FIN_CARGA_PROP_FL>%s</FH_EST_FIN_CARGA_PROP_FL>\n"
				+ "<FH_EST_DESP_PROP_FL>%s</FH_EST_DESP_PROP_FL>\n<FH_EST_ARRIBO_TIENDA_PROP_FL>%s</FH_EST_ARRIBO_TIENDA_PROP_FL>\n"
				+ "<EST_TMPO_SERV_TIENDA_PROP_FL>%s</EST_TMPO_SERV_TIENDA_PROP_FL>\n</DETAIL>\n</DATAAREA>\n</ROADNET_PLAN_001>";
		
		String requestFormat = String.format(REQ_PART_1, data.get("LOGICALID"), data.get("REFERENCEID"),
				data.get("AUTHID"), data.get("CEDIS"), data.get("NORECORDS"), data.get("PLAZA"), data.get("TIPORUTA"),
				data.get("RUTAORIGINAL"), data.get("RUTAPROPUESTA"), data.get("DESTID"), data.get("CRTIENDA"),
				data.get("TSFNO"), data.get("CAJASTOTAL"), data.get("CANASTILLASTOTAL"), data.get("VOLUMENCAJAS"),
				data.get("VOLUMENCANASTILLAS"), data.get("PESO"), data.get("FHPMOINICIOCARGA"),
				data.get("FHPMOFINCARGA"), data.get("FHPMODESP"), data.get("FHPMOINIVENTENTREGA"),
				data.get("FHPMOFINVENTENTREGA"), data.get("SECUENCIACARGA"), data.get("SECUENCIAENTREGA"),
				data.get("FHESTINICIOCARGAPROPFL"), data.get("FHESTFINCARGAPROPFL"), data.get("FHESTDESPPROPFL"),
				data.get("FHESTARRIBOTIENDAPROPFL"), data.get("ESTTMPOSERVTIENDAPROPFL"));

		
		
		String requestEnc = (URLEncoder.encode(requestFormat, java.nio.charset.StandardCharsets.UTF_8.toString()));

		String url = String.format(requestUrl, requestEnc);
		System.out.println(requestFormat);
		System.out.println(url);

		String response = GetRequest.executeGetRequest(url);

		testCase.addTextEvidenceCurrentStep(url);
		testCase.addTextEvidenceCurrentStep(response);
		return response;

	}

}
