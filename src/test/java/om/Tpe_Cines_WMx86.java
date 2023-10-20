package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

import exceptions.ReempRequestException;
import modelo.TestCase;
import util.GetRequestFile;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;

public class Tpe_Cines_WMx86 {
	public String RunConsultaCiudades = "Cines\\ConsultaCiudades.txt";
	public String RunConsultaComp = "Cines\\ConsultaComplejos.txt";
	public String RunSolicitaFolio = "Cines\\SolicitaFolio.txt";
	public String RunSolicitaAutor = "Cines\\SolicitudAutorizacion.txt";
	public String RunConfirmACK= "Cines\\ConfirmaACK.txt";
	
	String creationDate;

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;

	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
																			// de
																			// la
																			// fecha

	String date = formatter.format(fecha);

	public Tpe_Cines_WMx86(HashMap<String, String> data, TestCase testCase, SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;

	}

	public String ConsultaCiudad() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();

		
		datosRequest.put("host", data.get("host"));
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", data.get("operation"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("adDate", date.substring(0, 8));
		datosRequest.put("pvDate", date);
		datosRequest.put("opcion", data.get("opcion"));

		
		String request = GetRequestFile.getRequestFile(RunConsultaCiudades, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de Consulta de ciudad es exitosa:");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
		
		
	}
	
	
public String ConsultaComplejos() throws ReempRequestException, ClientProtocolException, IOException {
		
		HashMap<String, String> datosRequest = new HashMap<>();

		datosRequest.put("host", data.get("host"));
		datosRequest.put("application", data.get("application"));
		datosRequest.put("entity", data.get("entity"));
		datosRequest.put("operation", data.get("operation02"));
		datosRequest.put("source", data.get("source"));
		datosRequest.put("plaza", data.get("plaza"));
		datosRequest.put("tienda", data.get("tienda"));
		datosRequest.put("caja", data.get("caja"));
		datosRequest.put("adDate", date.substring(0, 8));
		datosRequest.put("pvDate", date);
		datosRequest.put("ciudad", data.get("ciudad"));

		String request = GetRequestFile.getRequestFile(RunConsultaComp, datosRequest);
		System.out.println(request);
		testCase.addTextEvidenceCurrentStep("\n" + request);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de consulta de complejos es exitosa:");
		String response = GetRequest.executeGetRequest(request);
		testCase.addTextEvidenceCurrentStep("\n" + response);

		return response;
		
		
	}

public String SolicitaFolio() throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation03"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", date.substring(0, 8));
	datosRequest.put("pvDate", date);

	String request = GetRequestFile.getRequestFile(RunSolicitaFolio, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de folio proporciona un folio de forma exitosa:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
}

public String SolicitaAutorizacion(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("operation", data.get("operation04"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", Folio);
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", date.substring(0, 8));
	datosRequest.put("pvDate", date);
	datosRequest.put("payType", data.get("payType"));
	datosRequest.put("IdTerminal", data.get("IdTerminal"));
	datosRequest.put("TipoVoucher", data.get("TipoVoucher"));
	datosRequest.put("ImportePVP", data.get("ImportePVP"));
	datosRequest.put("IdOperacionInt", data.get("IdOperacionInt"));
	datosRequest.put("NoEntradas", data.get("NoEntradas"));	
	
	String request = GetRequestFile.getRequestFile(RunSolicitaAutor, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de solicitud de autorizacion es exitosa:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
}


public String ConfirmacionACK(String Folio) throws ReempRequestException, ClientProtocolException, IOException {
	
	HashMap<String, String> datosRequest = new HashMap<>();

	datosRequest.put("host", data.get("host"));
	datosRequest.put("application", data.get("application"));
	datosRequest.put("operation", data.get("operation05"));
	datosRequest.put("entity", data.get("entity"));
	datosRequest.put("source", data.get("source"));
	datosRequest.put("folio", Folio);
	datosRequest.put("plaza", data.get("plaza"));
	datosRequest.put("tienda", data.get("tienda"));
	datosRequest.put("caja", data.get("caja"));
	datosRequest.put("adDate", date.substring(0, 8));
	datosRequest.put("ackValue", data.get("ackValue"));
	
	String request = GetRequestFile.getRequestFile(RunConfirmACK, datosRequest);
	System.out.println(request);
	testCase.addTextEvidenceCurrentStep("\n" + request);
	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	testCase.addBoldTextEvidenceCurrentStep("El servicio de confirmacion ACK es exitosa:");
	String response = GetRequest.executeGetRequest(request);
	testCase.addTextEvidenceCurrentStep("\n" + response);

	return response;
	
}


}