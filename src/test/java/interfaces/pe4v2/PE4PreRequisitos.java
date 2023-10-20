package interfaces.pe4v2;
import java.io.IOException;
import java.util.HashMap;
import org.testng.annotations.Test;

import com.jcraft.jsch.JSchException;

import integrationServer.om.AdaptersPoolConection;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PE4v2;
import om.PE6;
import util.GlobalVariables;
import util.RequestUtil;
import util.SSHConnector;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/* Descripcion funcionamiento PE4 
 * La interfase tiene como objetivo recibir, validar los datos necesarios para crear el comprobante digital que 
 * provienen de POS, Oracle AR u Oracle IMMEX y enviarlos al proveedor que generara el comprobante fiscal, para 
 * cumplir con los lineamientos fiscales vigentes.
*/
public class PE4PreRequisitos extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE4V2_PE4PreRequisitos(HashMap<String, String> data) throws Exception {
		
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		//consultas
		String CFD_TRANSACTION = "SELECT * from (select FOLIO, CREATION_DATE, REFID, PLAZA, TIENDA, CAJA, TICKET " + 
				" FROM TPEUSER.cfd_transaction" + 
				" where TRUNC(CREATION_DATE) = TRUNC(SYSDATE)" + 
				" ORDER BY creation_date DESC)" + 
				" where rownum = 1";
		
		String consulta_CFD_TRANSACTION_pt1="select FOLIO,CREATION_DATE,REFID,PLAZA,TIENDA,SERVER from TPEUSER.CFD_TRANSACTION where folio='%s'";
		String consulta_CFD_TRANSACTION_pt2="select CAJA,TICKET,ADMIN_DATE,PV_DATE,EMI_RFC from TPEUSER.CFD_TRANSACTION where folio='%s'";
		String consulta_CFD_SERVER_pt1="SELECT SERVER, DESCRIPTION, URL  FROM TPEUSER.CFD_SERVER where server ='%s'";
		String consulta_CFD_SERVER = "SELECT SERVER,DESCRIPTION, URL FROM TPEUSER.CFD_SERVER WHERE SERVER = 'DEFAULT'";
		String consulta_CFD_COMPANY_pt1="select RFC, NOMBRE, CALLE, NOEXTERIOR, COLONIA from TPEUSER.CFD_COMPANY where rfc='%s'";
		String consulta_CFD_COMPANY_pt3="select COMPANY, REGIMEN_FISCAL, TOKEN_WS, REGIMEN_FISCAL_V33 from TPEUSER.CFD_COMPANY where rfc='%s'";

		PE4v2 pe4Obj = new PE4v2(data, testCase, db);
		String expectedWMCodeQueryTdc = "101";
	    
		
		
		addStep ("RESUMEN");
	        
	    //paso 1******************************************************************************************************************************************
	   
		testCase.addBoldTextEvidenceCurrentStep("Conectarse a la base de datos FCTPE.");
		String EMI_RFC="";
		String server="";
		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "
				+ GlobalVariables.DB_USER_FCTPE);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());
		//paso2 ******************************************************************************************************************************************
		testCase.setProject_Name("Configuracion de ambientes.");
		testCase.addBoldTextEvidenceCurrentStep("Consultar informacion de la transaccion buscando con el numero de folio en la tabla del registro de transacciones TCFD tabla CFD_TRANSACTION en la base de datos FCTPE.");
		
		SQLResult queryFolioResult = executeQuery(db, CFD_TRANSACTION);
		System.out.println(CFD_TRANSACTION);
		String folio = queryFolioResult.getData(0, "FOLIO");
		
		String query1 = String.format(consulta_CFD_TRANSACTION_pt1, folio);
		System.out.println(query1);
		SQLResult resultQuery = db.executeQuery(query1);
		boolean consultaBoolean=resultQuery.isEmpty();
		if(consultaBoolean) {
			testCase.addTextEvidenceCurrentStep("-El folio no fue encontrado en la tabla CFD_TRANSACTION.");
		}else {
			String REFID= resultQuery.getData(0, "REFID");
			testCase.addTextEvidenceCurrentStep("-La referencia unica de Factura es: '"+REFID+"'.");
			String PLAZA= resultQuery.getData(0, "PLAZA");
			testCase.addTextEvidenceCurrentStep("-La plaza donde fue utilizada es: '"+PLAZA+"'.");
			String TIENDA= resultQuery.getData(0, "TIENDA");
			testCase.addTextEvidenceCurrentStep("-La tienda donde fue utilizada es: '"+TIENDA+"'.");
			server= resultQuery.getData(0, "SERVER");
			testCase.addTextEvidenceCurrentStep("-El server utilizado es: '"+server+"'.");
			
		}
		String query2 = String.format(consulta_CFD_TRANSACTION_pt2, data.get("folio"));
		System.out.println(query2);
		SQLResult resultQuery1 = db.executeQuery(query2);
		boolean consultaBoolean1=resultQuery1.isEmpty();
		if(consultaBoolean1) {
			testCase.addTextEvidenceCurrentStep("-El folio no fue encontrado en la tabla CFD_TRANSACTION.");
		}else {
			String TICKET= resultQuery1.getData(0, "TICKET");
			testCase.addTextEvidenceCurrentStep("-EL ticket registrado con este folio es: '"+TICKET+"'.");
			EMI_RFC= resultQuery1.getData(0, "EMI_RFC");
			testCase.addTextEvidenceCurrentStep("-Clave del Registro Federal del Contribuyente: '"+EMI_RFC+"'.");
		}
		//paso 3
		testCase.addBoldTextEvidenceCurrentStep("Consultar con el servidor obtenido para saber si el URL esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE");
		String queryServer_1= String.format(consulta_CFD_SERVER_pt1, server);
		System.out.println(queryServer_1);
		SQLResult resultQueryServer1 = db.executeQuery(queryServer_1);
		boolean consultaServerBoolean1=resultQueryServer1.isEmpty();
		if(consultaServerBoolean1) {
			testCase.addTextEvidenceCurrentStep("-El URL no esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
		}else {
			testCase.addTextEvidenceCurrentStep("-URL configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
			
		}
		
		//PASO EXTRA	
				testCase.addBoldTextEvidenceCurrentStep("Consultar el servidor DEFAULT para saber si el URL esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE");
				SQLResult resultQueryServer = db.executeQuery(consulta_CFD_SERVER);
				
				boolean serverIsEmpty = resultQueryServer.isEmpty();
				
				
					if (serverIsEmpty) {
						testCase.addTextEvidenceCurrentStep("-El URL DEFAULT no esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
					}else {
						testCase.addTextEvidenceCurrentStep("-URL DEFAULT configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");

					}
					
		//paso 4
		testCase.addBoldTextEvidenceCurrentStep("Consultar con el numero de rfc obtenido informacion del registro de las razones sociales de facturacion en la tabla CFD_COMPANY en la base de datos FCTPE.");
		String query3 = String.format(consulta_CFD_COMPANY_pt1, EMI_RFC);
		System.out.println(query3);
		SQLResult resultQuery2 = db.executeQuery(query3);
		boolean consultaBoolean3=resultQuery2.isEmpty();
		if(consultaBoolean3) {
			testCase.addTextEvidenceCurrentStep("-El RFC no fue encontrado en la tabla CFD_COMPANY.");
		}else {
			String NOMBRE= resultQuery2.getData(0, "NOMBRE");
			testCase.addTextEvidenceCurrentStep("-Razón social: '"+NOMBRE+"'.");
		}
		String query4 = String.format(consulta_CFD_COMPANY_pt3, EMI_RFC);
		System.out.println(query4);
		SQLResult resultQuery4 = db.executeQuery(query4);
		boolean consultaBoolean4=resultQuery4.isEmpty();
		if(consultaBoolean4) {
			testCase.addTextEvidenceCurrentStep("-El RFC no fue encontrado en la tabla CFD_COMPANY.");
		}else {
			String REGIMEN_FISCAL= resultQuery4.getData(0, "REGIMEN_FISCAL");
			testCase.addTextEvidenceCurrentStep("-Descripcion de regimen fiscal: '"+REGIMEN_FISCAL+"'.");
			String TOKEN= resultQuery4.getData(0, "TOKEN_WS");
			testCase.addTextEvidenceCurrentStep("-Token configurado correctamente en la tabla CFD_COMPANY en la BD de TPE: '"+TOKEN+"'.");
		}
		   //*********************************************************************************************************************************************************
				//paso 1
				//fin de resumen
		
		addStep("Conectarse a la base de datos FCTPE.");
		
		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "
				+ GlobalVariables.DB_USER_FCTPE);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());
		//paso2
		testCase.setProject_Name("Configuracion de ambientes.");
		addStep("Consultar informacion de la transaccion buscando con el numero de folio en la tabla del registro de transacciones TCFD tabla CFD_TRANSACTION en la base de datos FCTPE.");
	
		testCase.addQueryEvidenceCurrentStep(resultQuery);
		if(consultaBoolean) {
			testCase.addTextEvidenceCurrentStep("-El folio no fue encontrado en la tabla CFD_TRANSACTION.");
		}else {
			String REFID= resultQuery.getData(0, "REFID");
			testCase.addTextEvidenceCurrentStep("-La referencia unica de Factura es: '"+REFID+"'.");
			String PLAZA= resultQuery.getData(0, "PLAZA");
			testCase.addTextEvidenceCurrentStep("-La plaza donde fue utilizada es: '"+PLAZA+"'.");
			String TIENDA= resultQuery.getData(0, "TIENDA");
			testCase.addTextEvidenceCurrentStep("-La tienda donde fue utilizada es: '"+TIENDA+"'.");
			server= resultQuery.getData(0, "SERVER");
			testCase.addTextEvidenceCurrentStep("-El server utilizado es: '"+server+"'.");
			
		}
		query2 = String.format(consulta_CFD_TRANSACTION_pt2, data.get("folio"));
		System.out.println(query2);
	    resultQuery1 = db.executeQuery(query2);
	    consultaBoolean1=resultQuery1.isEmpty();
		testCase.addQueryEvidenceCurrentStep(resultQuery1);
		if(consultaBoolean1) {
			testCase.addTextEvidenceCurrentStep("-El folio no fue encontrado en la tabla CFD_TRANSACTION.");
		}else {
			String TICKET= resultQuery1.getData(0, "TICKET");
			testCase.addTextEvidenceCurrentStep("-EL ticket registrado con este folio es: '"+TICKET+"'.");
			EMI_RFC= resultQuery1.getData(0, "EMI_RFC");
			testCase.addTextEvidenceCurrentStep("-Clave del Registro Federal del Contribuyente: '"+EMI_RFC+"'.");
		}
		//paso 3
		addStep("Consultar con el servidor obtenido para saber si el URL esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE");
	    queryServer_1= String.format(consulta_CFD_SERVER_pt1, server);
		System.out.println(queryServer_1);
	   resultQueryServer1 = db.executeQuery(queryServer_1);
		 consultaServerBoolean1=resultQueryServer1.isEmpty();
		testCase.addQueryEvidenceCurrentStep(resultQueryServer1);
		if(consultaServerBoolean1) {
			testCase.addTextEvidenceCurrentStep("-El URL no esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
		}else {
			testCase.addTextEvidenceCurrentStep("-URL configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
	
		}
		
		//PASO EXTRA	
		addStep("Consultar el servidor DEFAULT para saber si el URL esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE");
		 resultQueryServer = db.executeQuery(consulta_CFD_SERVER);
		
		 serverIsEmpty = resultQueryServer.isEmpty();
		 
		 testCase.addQueryEvidenceCurrentStep(resultQueryServer);
		if (serverIsEmpty) {
			testCase.addTextEvidenceCurrentStep("-El URL DEFAULT no esta configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");
		}else {
			testCase.addTextEvidenceCurrentStep("-URL DEFAULT configurado correctamente en la tabla CFD_SERVER en la BD de TPE.");

		}
		
		//paso 4
		addStep("Consultar con el numero de rfc obtenido informacion del registro de las razones sociales de facturacion en la tabla CFD_COMPANY en la base de datos FCTPE.");
		query3 = String.format(consulta_CFD_COMPANY_pt1, EMI_RFC);
		System.out.println(query3);
		 resultQuery2 = db.executeQuery(query3);
		 consultaBoolean3=resultQuery2.isEmpty();
		testCase.addQueryEvidenceCurrentStep(resultQuery2);
		if(consultaBoolean3) {
			testCase.addTextEvidenceCurrentStep("-El RFC no fue encontrado en la tabla CFD_COMPANY.");
		}else {
			String NOMBRE= resultQuery2.getData(0, "NOMBRE");
			testCase.addTextEvidenceCurrentStep("-Razón social: '"+NOMBRE+"'.");
		}
		 query4 = String.format(consulta_CFD_COMPANY_pt3, EMI_RFC);
		System.out.println(query4);
		 resultQuery4 = db.executeQuery(query4);
		 consultaBoolean4=resultQuery4.isEmpty();
		testCase.addQueryEvidenceCurrentStep(resultQuery4);
		if(consultaBoolean4) {
			testCase.addTextEvidenceCurrentStep("-El RFC no fue encontrado en la tabla CFD_COMPANY.");
		}else {
			String REGIMEN_FISCAL= resultQuery4.getData(0, "REGIMEN_FISCAL");
			testCase.addTextEvidenceCurrentStep("-Descripcion de regimen fiscal: '"+REGIMEN_FISCAL+"'.");
			String TOKEN= resultQuery4.getData(0, "TOKEN_WS");
			testCase.addTextEvidenceCurrentStep("-Token configurado correctamente en la tabla CFD_COMPANY en la BD de TPE: '"+TOKEN+"'.");
		}
		
		
	
	}

	

		
	@Override
	public String setTestFullName() {
		return "ATC_FT_001_PE4V2_PE4PreRequisitos";
	}

	@Override
	public String setTestDescription() {
		return "Construido. PE4PreRequisitos";
	}

	@Override
	public String setTestDesigner() {
		return "tbd";
	}

	@Override
	public String setTestInstanceID() {
		return "-1";                               
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}




	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}