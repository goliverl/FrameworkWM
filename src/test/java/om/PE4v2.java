package om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.http.client.ClientProtocolException;
import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class PE4v2 {

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	public String runGetCFID = "PE4V2/RunGetCFID.txt";
	public String runGetCFIDv3_1 = "PE4V2/RunGetCFIDv3.1.txt";
	public String runGetCFIDv3_2 = "PE4V2/RunGetCFIDv3.2.txt";

	
	public PE4v2(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
public String executeRunQueryTdc() throws ReempRequestException, ClientProtocolException, IOException {
		
		String requestUrl = "http://%s/invoke/PE4v2.Pub/runQueryTdc?folio=%s&adminDate=%s&tarjeta=%s";
		
		String url = String.format(requestUrl, 
				data.get("host"),
				data.get("folio"),
				data.get("adminDate"),
				data.get("tarjeta"));
		
		System.out.println(url);
		testCase.addTextEvidenceCurrentStep(url);
		
		String responseRunQueryTdc = GetRequest.executeGetRequest(url);
		System.out.println(responseRunQueryTdc);
		testCase.addTextEvidenceCurrentStep(responseRunQueryTdc);
		
		return responseRunQueryTdc;
	}
	

public String executeRunQueryFecha() throws ReempRequestException, ClientProtocolException, IOException {
	
	String requestUrl = "http://10.80.20.103:8889/invoke/PE4v2.Pub/runGetCFDi?xmlIn= version=\"1.0\"?>" 
	+ " <TPEFacturaInput version=\"3.0\"><OxxoData plaza=\"10MON\" tienda=\"50GIT\" caja=\"1\" adminDate=\"20210606\" "
	+ "noTicket=\"6305226\" notaCredito=\"S\" reattempt=\"Y\" movDate=\"202105041005\" source=\"POS\"/><Comprobante formaDePago=\"01\" "
	+ "subTotal=\"1.85\" total=\"2.00\" metodoDePago=\"PUE\" lugarExpedicion=\"64000\" tipoDeComprobante=\"I\" moneda=\"MXN\">"
	+ "<Receptor rfc=\"GET130704R74\" nombre=\"GETIC S.A  DE C.V\" usoCFDI=\"P01\">"
	+ "<Domicilio calle=\"PUERTA DEL.  SOL 1227 COLINAS DE SAN JERONIMO\" "
	+ "municipio=\"MONTERREY NUEVO  LEON\" pais=\"MEXICO\" codigoPostal=\"64630\"/></Receptor><Conceptos><Concepto claveProdServ=\"50161813\" "
	+ "cantidad=\"1.000\" claveUnidad=\"EA\" descripcion=\"CHOC A GRANEL KISSES     \" valorUnitario=\"1.85\" importe=\"1.85\">"
	+ "<Impuestos>"
	+ "<Traslados><Traslado base=\"2.00\" impuesto=\"002\" tipoFactor=\"Tasa\" tasaOCuota=\"0.000000\" importe=\"0.00\"/>"
	+ "<Traslado base=\"1.85\" impuesto=\"003\" tipoFactor=\"Tasa\" tasaOCuota=\"0.080000\" importe=\"0.15\"/>"
	+ "</Traslados></Impuestos></Concepto></Conceptos><Impuestos totalImpuestosTrasladados=\"0.15\">"
	+ "<Traslados>"
	+ "<Traslado impuesto=\"002\" tasaOCuota=\"0.000000\" importe=\"0.00\" tipoFactor=\"Tasa\"/>"
	+ "<Traslado impuesto=\"003\" tasaOCuota=\"0.080000\" importe=\"0.15\" tipoFactor=\"Tasa\"/>"
	+ "</Traslados>"
	+ "</Impuestos>"
	+ "<DatosAdicionales descripcionRegimen=\"Opcional para Grupos de Sociedades\" descripcionTipoComprobante=\"Ingreso\" descripcionUsoCFDI=\"Por definir\" descripcionMetodoPago=\"Pago en una sola exhibición\"/>"
	+ "</Comprobante></TPEFacturaInput>";
	

	
	String responseRunQueryTdc = GetRequest.executeGetRequest(requestUrl);
	System.out.println(responseRunQueryTdc);
	testCase.addTextEvidenceCurrentStep(requestUrl);
	
	return responseRunQueryTdc;
}

public String executeRunQueryYaFacturado() throws ReempRequestException, ClientProtocolException, IOException {
	
	String requestUrl = "http://10.80.20.103:8889/invoke/PE4v2.Pub/runGetCFDi?xmlIn= version=\"1.0\"?>" 
	+ " <TPEFacturaInput version=\"3.0\">"
	+ "<OxxoData plaza=\"10MON\" tienda=\"50GIT\" caja=\"1\" adminDate=\"20210427\" noTicket=\"6305293\" notaCredito=\"N\" reattempt=\"Y\" movDate=\"202104271504\" source=\"POS\"/>"
	+ "<Comprobante formaDePago=\"01\" subTotal=\"1.85\" total=\"2.00\" metodoDePago=\"PUE\" lugarExpedicion=\"64000\" tipoDeComprobante=\"I\" moneda=\"MXN\">"
	+ "<Receptor rfc=\"GET130704R74\" nombre=\"GETIC S.A  DE C.V\" usoCFDI=\"P01\"><Domicilio calle=\"PUERTA DEL.  SOL 1227 COLINAS DE SAN JERONIMO\" municipio=\"MONTERREY NUEVO  LEON\" pais=\"MEXICO\" codigoPostal=\"64630\"/><"
	+ "/Receptor>"
	+ "<Conceptos>"
	+ "<Concepto claveProdServ=\"50161813\" cantidad=\"1.000\" claveUnidad=\"EA\" descripcion=\"CHOC A GRANEL KISSES     \" valorUnitario=\"1.85\" importe=\"1.85\">"
	+ "<Impuestos>"
	+ "<Traslados><Traslado base=\"2.00\" impuesto=\"002\" tipoFactor=\"Tasa\" tasaOCuota=\"0.000000\" importe=\"0.00\"/><Traslado base=\"1.85\" impuesto=\"003\" tipoFactor=\"Tasa\" tasaOCuota=\"0.080000\" importe=\"0.15\"/>"
	+ "</Traslados>"
	+ "</Impuestos>"
	+ "</Concepto>"
	+ "</Conceptos>"
	+ "<Impuestos totalImpuestosTrasladados=\"0.15\"><Traslados><Traslado impuesto=\"002\" tasaOCuota=\"0.000000\" importe=\"0.00\" tipoFactor=\"Tasa\"/><Traslado impuesto=\"003\" tasaOCuota=\"0.080000\" importe=\"0.15\" tipoFactor=\"Tasa\"/>"
	+ "</Traslados>"
	+ "</Impuestos>"
	+ "<DatosAdicionales descripcionRegimen=\"Opcional para Grupos de Sociedades\" descripcionTipoComprobante=\"Ingreso\" descripcionUsoCFDI=\"Por definir\" descripcionMetodoPago=\"Pago en una sola exhibición\"/>"
	+ "</Comprobante>"
	+ "</TPEFacturaInput>";
	

	
	String responseRunQueryTdc = GetRequest.executeGetRequest(requestUrl);
	System.out.println(responseRunQueryTdc);
	testCase.addTextEvidenceCurrentStep(requestUrl);
	
	return responseRunQueryTdc;
}

	public String executeRunQueryTicket()throws ReempRequestException, ClientProtocolException, IOException {
		
		String requestUrl = "http://%s/invoke/PE4v2.Pub/runQueryTicket?plaza=%s&tienda=%s&caja=%s&adminDate=%s&noTicket=%s&source=%s";
		
		String url = String.format(requestUrl, 
				data.get("host"),
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				data.get("adminDate"),
				data.get("noTicket"),
				data.get("source"));
		
		System.out.println(url);
		testCase.addTextEvidenceCurrentStep(url);
		
		String responseRunQueryTdc = GetRequest.executeGetRequest(url);
		System.out.println(responseRunQueryTdc);
		testCase.addTextEvidenceCurrentStep(responseRunQueryTdc);
		
		return responseRunQueryTdc;
	}
	
	public String executeRunGetCFDI() throws ReempRequestException, ClientProtocolException, IOException, JAXBException {
		
		
		HashMap<String, String> datosRequest = new HashMap<>();
		
		datosRequest.put("host", data.get("host"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));//*
		//datosRequest.put("caja", data.get("caja"));//*
		datosRequest.put("noTicket", data.get("noTicket"));
		datosRequest.put("notaCredito", data.get("notaCredito"));
		datosRequest.put("reattempt", data.get("reattempt"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("formaDePago", data.get("formaDePago"));
		datosRequest.put("subTotal", data.get("subTotal"));
		datosRequest.put("total", data.get("total"));
		datosRequest.put("metodoDePago", data.get("metodoDePago"));
		datosRequest.put("lugarExpedicion", data.get("lugarExpedicion"));
		datosRequest.put("moneda", data.get("moneda"));
		datosRequest.put("rfcEmisor", data.get("rfcEmisor"));
		datosRequest.put("nombreEmisor", data.get("nombreEmisor"));
		datosRequest.put("Regimen", data.get("Regimen"));
		datosRequest.put("rfcReceptor", data.get("rfcReceptor"));
		datosRequest.put("nombreReceptor", data.get("nombreReceptor"));
		datosRequest.put("usoCFDI", data.get("usoCFDI"));
		datosRequest.put("calle", data.get("calle"));
		datosRequest.put("noExterior", data.get("noExterior"));
		datosRequest.put("colonia", data.get("colonia"));
		datosRequest.put("estado", data.get("estado"));
		datosRequest.put("pais", data.get("pais"));
		datosRequest.put("codigoPostal", data.get("codigoPostal"));
		datosRequest.put("cantidad", data.get("cantidad"));
		datosRequest.put("descripcion", data.get("descripcion"));
		datosRequest.put("valorUnitario", data.get("valorUnitario"));
		datosRequest.put("importeConcepto", data.get("importeConcepto"));
		datosRequest.put("claveProdServ", data.get("claveProdServ"));
		datosRequest.put("claveUnidad", data.get("claveUnidad"));
		datosRequest.put("impuesto", data.get("impuesto"));
		datosRequest.put("base", data.get("base"));
		datosRequest.put("tipoFactor", data.get("tipoFactor"));
		datosRequest.put("tasaOCuota", data.get("tasaOCuota"));
		datosRequest.put("importe", data.get("importe"));
		datosRequest.put("totalImpuestosTrasladados", data.get("totalImpuestosTrasladados"));
		datosRequest.put("tipoFactor", data.get("tipoFactor"));
		datosRequest.put("descripcionFormaPago", data.get("descripcionFormaPago"));
		datosRequest.put("descripcionRegimen", data.get("descripcionRegimen"));
		datosRequest.put("descripcionTipoComprobante", data.get("descripcionTipoComprobante"));
		datosRequest.put("descripcionUsoCFDI", data.get("descripcionUsoCFDI"));
		datosRequest.put("descripcionMetodoPago", data.get("descripcionMetodoPago"));
		
		String request = GetRequestFile.getRequestFile(runGetCFID, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n"+request);
		
		String response = GetRequest.executeGetRequest(request);		
		System.out.println(response);
		
		return response;
	}
	
public String executeRunGetCFDIv3_1() throws ReempRequestException, ClientProtocolException, IOException, JAXBException {
		
		
		HashMap<String, String> datosRequest = new HashMap<>();
		
		datosRequest.put("host", data.get("host"));
//		datosRequest.put("plaza", data.get("plaza"));
//		datosRequest.put("tienda", data.get("tienda"));
		
		
		String request = GetRequestFile.getRequestFile(runGetCFIDv3_1, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		
		String response = GetRequest.executeGetRequest(request);	
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);
		System.out.println(response);
		
		return response;
	}
	

	
	public String executeRunGetCFDIv3_2() throws ReempRequestException, ClientProtocolException, IOException, JAXBException {
		
		
		HashMap<String, String> datosRequest = new HashMap<>();
		
		datosRequest.put("host", data.get("host"));
		
		
		String request = GetRequestFile.getRequestFile(runGetCFIDv3_2, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("Request: \n" + request);
		
		String response = GetRequest.executeGetRequest(request);	
		testCase.addTextEvidenceCurrentStep("Response: \n" + response);
		System.out.println(response);
		
		return response;
	}
	
}
