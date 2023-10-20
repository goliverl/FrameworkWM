package om;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;  
import javax.xml.bind.annotation.XmlElement;  
import javax.xml.bind.annotation.XmlRootElement;  

import modelo.TestCase;
import util.GlobalVariables;


public class TPE_BUS  extends OxxoXML{
	
	public String baseURL = "http://%s/invoke/TPE.BUS.Pub/request?xmlIn=";
	HashMap<String, String> data;
	TestCase testCase;
	//SqlUtil db;
	utils.sql.SQLUtil db;
	
	Date fecha = new Date();// obtener fecha del sistema
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); // formato
// de
// la
// fecha
	public TPE_BUS(HashMap<String, String> data, TestCase testCase,utils.sql.SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
	}
	
	public String executeTRN01() throws IOException, JAXBException {
		
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1.0");
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"TRN01",
				data.get("source"),
				null,
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				null,
				null				
				));
		
		String xml = marshal(doc);
		System.out.println(xml);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String executeQRY01(String folio) throws IOException, JAXBException {
		
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1.0");
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"QRY01",
				data.get("source"),
				folio,
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				null,
				null));
		
		doc.setRequest(new BUS_Request(new Catalog(
				data.get("catalogName"),
				data.get("catalogKey"),
				data.get("catalogLine"))));
		
		String xml = marshal(doc);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);	
		testCase.addTextEvidenceCurrentStep(response);
		
		return response;
	}
	
	public String executeQRY02(String folio) throws JAXBException, IOException {
		HashMap<String, String> datosRequest = new HashMap<>();
		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1.0");
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"QRY02",
				data.get("source"),
				folio,
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				date.substring(0, 8), //addate
				null				
				));
		
		doc.setRequest(new BUS_Request(new Itinerario(
				data.get("itinerarioLine"),
				data.get("itinerarioOrigen"),
				data.get("itinerarioDestino"),
				data.get("itinerarioDate"),
				data.get("itinerarioHorario"))));
		
		String xml = marshal(doc);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);	
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	public String executeQRY03(String folio) throws IOException, JAXBException {
		HashMap<String, String> datosRequest = new HashMap<>();
		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));
		
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1.0");
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"QRY03",
				data.get("source"),
				folio,
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				date.substring(0, 8),
				null				
				));
		
		doc.setRequest(new BUS_Request(new Corrida(
				data.get("idCorrida"),
				data.get("origen"),
				data.get("destino"),
				data.get("corridaDate"),
				null,
				data.get("corridaLine"),
				null)));
				
		String xml = marshal(doc);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);	
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	public String executeTRN02(String folio) throws IOException, JAXBException {
		HashMap<String, String> datosRequest = new HashMap<>();
		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1,0");
		
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"TRN02",
				data.get("source"),
				folio,
				null,
				null,
				null,
				date.substring(0, 8),
				date));
		
		doc.setRequest(new BUS_Request(
				new Corrida (
				data.get("idCorrida"),
				data.get("origen"),
				data.get("destino"),
				data.get("corridaDate"),
				data.get("service"),
				data.get("line"),
				new Venta (
						data.get("type"),
						new Asiento ( data.get("idAsiento"),
									  data.get("client"),
									  data.get("category")
									)
						)
				)));		
		
		
		String xml = marshal(doc);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);	
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
	public String executeTRN03(String folio) throws IOException, JAXBException {
		HashMap<String, String> datosRequest = new HashMap<>();
		String date = formatter.format(fecha);
		datosRequest.put("host", data.get("host"));
		
		String url = String.format(baseURL, GlobalVariables.HOST_OLS);
		
		OxxoRequest or = new OxxoRequest(url);
		BUS_TPEDoc doc = new BUS_TPEDoc();
		doc.setVersion("1,0");
		
		doc.setHeader(new BUS_Header(
				"BUS",
				"TICKET",
				"TRN03",
				data.get("source"),
				folio,
				data.get("plaza"),
				data.get("tienda"),
				data.get("caja"),
				date.substring(0, 8), //addate
				date)); //pvdate
		
		doc.setRequest(new BUS_Request(
				new Ack (
				"00"
				)));		
		
		
		String xml = marshal(doc);
		String request = url + xml;
		
		testCase.addTextEvidenceCurrentStep(request);
		String response = or.executeRequest(request);	
		testCase.addTextEvidenceCurrentStep(response);
		return response;
	}
	
/*public String getWmCodeQuery(String query)  {
		
		testCase.addQueryEvidenceCurrentStep(db, query);
		String wmCode = null;
		try {
			Thread.sleep(8000);
		    System.out.println(query);
		    
		    ResultSet rs = db.executeQuery(query);
		    
		    rs.next();
		    
		    wmCode = rs.getString("wm_code");
		    
		    db.copyResultSetData();
		    
		    testCase.addQueryEvidenceCurrentStep(db, query);
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wmCode;
	}
	*/
	@XmlRootElement (name= "TPEDoc")
	static class BUS_TPEDoc extends TPEDoc {
		
		public BUS_TPEDoc() {}
		
		@XmlElement
		public void setHeader(BUS_Header header) {
			this.header = header;
		}
		@XmlElement
		public void setRequest(BUS_Request request) {
			this.request = request;
		}
		
		public BUS_Header getHeader() {
			return (BUS_Header) header;
		}

		public BUS_Request getRequest() {
			return (BUS_Request) request;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	static class BUS_Header extends Header {
		@XmlAttribute String source;
		@XmlAttribute String folio;
		@XmlAttribute String plaza;
		@XmlAttribute String tienda;
		@XmlAttribute String caja;
		@XmlAttribute String adDate;
		@XmlAttribute String pvDate;
		
		public BUS_Header(String application, String entity, String operation,
					  String source, String folio, String plaza,  String tienda, String caja, String adDate, String pvDate) {
		
			setApplication(application);
			setEntity(entity);
			setOperation(operation);
			setFolio(folio);
			setSource(source);
			setPlaza(plaza);
			setTienda(tienda);
			setCaja(caja);
			setAdDate(adDate);
			setPvDate(pvDate);

		}
		
		public void setSource(String source) {
			this.source = source;
		}
		
		public void setFolio(String folio) {
			this.folio = folio;
		}
		
		public void setPlaza(String plaza) {
			this.plaza = plaza;
		}
		
		public void setTienda(String tienda) {
			this.tienda = tienda;
		}
		
		public void setAdDate(String adDate) {
			this.adDate = adDate;
		}
		
		public void setPvDate(String pvDate) {
			this.pvDate = pvDate;
		}

		public String getCaja() {
			return caja;
		}

		public void setCaja(String caja) {
			this.caja = caja;
		}

		public String getSource() {
			return source;
		}

		public String getFolio() {
			return folio;
		}

		public String getPlaza() {
			return plaza;
		}

		public String getTienda() {
			return tienda;
		}

		public String getAdDate() {
			return adDate;
		}

		public String getPvDate() {
			return pvDate;
		}
		
		
	}
	@XmlAccessorType(XmlAccessType.FIELD)
	static class BUS_Request extends Request {
		@XmlElement Corrida corrida;
		@XmlElement Catalog catalog;
		@XmlElement Itinerario itinerario;
		@XmlElement Ack ack;
		
		public BUS_Request(Corrida corrida) {
			setCorrida(corrida);
		}
		
		public BUS_Request(Catalog catalog) {
			setCatalog(catalog);
		}
		
		public BUS_Request(Itinerario itinerario) {
			setItinerario(itinerario);
		}
		
		public BUS_Request(Ack ack) {
			setAck(ack);
		}
		
		public void setCorrida(Corrida corrida) {
			this.corrida = corrida;
		}
		
		public void setCatalog(Catalog catalog) {
			this.catalog = catalog;	
		}

		public void setItinerario(Itinerario itinerario) {
			this.itinerario = itinerario;
		}

		public void setAck(Ack ack) {
			this.ack = ack;
		}
		
		

	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Corrida {
		@XmlAttribute String id;
		@XmlAttribute String origen;
		@XmlAttribute String destino;
		@XmlAttribute String date;
		@XmlAttribute String service;
		@XmlAttribute String line;
		@XmlElement Venta venta;
		
		public Corrida(String id, String origen, String destino, String date, 
						String service, String line, Venta venta) {
			
			setId(id);
			setOrigen(origen);
			setDestino(destino);
			setDate(date);
			setService(service);
			setLine(line);
			setVenta(venta);
		}	
		
		public void setId(String id) {
			this.id = id;
		}
		public void setOrigen(String origen) {
			this.origen = origen;
		}
		
		public void setDestino(String destino) {
			this.destino = destino;
		}
		
		public void setDate(String date) {
			this.date = date;
		}
		
		public void setService(String service) {
			this.service = service;
		}

		public void setLine(String line) {
			this.line = line;
		}
		
		public void setVenta(Venta venta) {
			this.venta = venta;
		}

		public String getId() {
			return id;
		}

		public String getOrigen() {
			return origen;
		}

		public String getDestino() {
			return destino;
		}

		public String getDate() {
			return date;
		}

		public String getService() {
			return service;
		}

		public String getLine() {
			return line;
		}

		public Venta getVenta() {
			return venta;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Venta {
		@XmlAttribute
		String type;
		
		@XmlElement
		Asiento asiento;

		public Venta(String type, Asiento asiento) {
			setType(type);
			setAsiento(asiento);
		}
		
		
		public void setType(String type) {
			this.type = type;
		}
		
		public void setAsiento(Asiento asiento) {
			this.asiento = asiento;
		}

		public String getType() {
			return type;
		}

		public Asiento getAsiento() {
			return asiento;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Asiento {
		
		@XmlAttribute String id;
		@XmlAttribute String client;
		@XmlAttribute String category;
		
		public Asiento(String id, String client, String category) {
			setId(id);
			setCliente(client);
			setCategory(category);
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public void setCliente(String client) {
			this.client = client;
		}
		
		public void setCategory(String category) {
			this.category = category;
		}

		public String getId() {
			return id;
		}

		public String getCliente() {
			return client;
		}

		public String getCategory() {
			return category;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Catalog{
		@XmlAttribute String name;
		@XmlAttribute String key;
		@XmlAttribute String line;
		
		public Catalog(String name, String key, String line) {
			setName(name);
			setKey(key);
			setLine(line);
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getLine() {
			return line;
		}
		public void setLine(String line) {
			this.line = line;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Itinerario{
		@XmlAttribute String line;
		@XmlAttribute String origen;
		@XmlAttribute String destino;
		@XmlAttribute String date;
		@XmlAttribute String horario;
		
		public Itinerario(String line, String origen, String destino, String date, String horario){
			setLine(line);
			setOrigen(origen);
			setDestino(destino);
			setDate(date);
			setHorario(horario);
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

		public String getOrigen() {
			return origen;
		}

		public void setOrigen(String origen) {
			this.origen = origen;
		}

		public String getDestino() {
			return destino;
		}

		public void setDestino(String destino) {
			this.destino = destino;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getHorario() {
			return horario;
		}

		public void setHorario(String horario) {
			this.horario = horario;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Ack{
		
		@XmlAttribute String value;
		
		public Ack(String value) {
			setValue(value);
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
}

