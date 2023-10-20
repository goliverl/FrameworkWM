package interfaces.pe3;
	import java.io.IOException;
	import java.util.HashMap;
	import org.testng.annotations.Test;

	import com.jcraft.jsch.JSchException;

import integrationServer.om.AdaptersPoolConection;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
	import om.PE6;
	import util.GlobalVariables;
	import util.SSHConnector;
	import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


	public class PE3PreRequisitos extends BaseExecution {
		 private static final String USERNAME = PasswordUtil.decryptPassword("B154EF642C53C68266902033F242CC52");
		    private static final String HOST = "10.184.80.19";
		    private static final int PORT = 22;
		    private static final String PASSWORD =  PasswordUtil.decryptPassword("E1D7C6C1F1080EC9B01E3EC18DF1BC77");

		    @Test(dataProvider = "data-provider")
		public void ATC_FT_002_PE3_Pre_Requisitos_Config_Ambientes_Pruebas(HashMap<String, String> data) throws Exception {
			SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
			PE6 pe6Util = new PE6(data, testCase, null);
			boolean resumen=false;
			
			
			//CONSULTAS
			String consultaValidacionUPC="select * from TPEUSER.gif_routing where upc='%s'";
			String consultaValidacionUPCEnGifPromotion="select * from TPEUSER.gif_promotion where upc='%s'";
			
			
			
			
			testCase.setProject_Name("Configuracion de ambientes.");
			//********************* PASO 1 **********************************************************************************************************
			addStep("RESUMEN");
			testCase.addBoldTextEvidenceCurrentStep("Conectarse a la base de datos FCTPE.");
			testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "+ GlobalVariables.DB_USER_FCTDCQA);
			testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());
			
			//********************* PASO 2 **********************************************************************************************************
			testCase.addBoldTextEvidenceCurrentStep("Mostrar si la tarjeta insertada esta registrada en la tabla gif_routing en la base de datos FCTPEQA.FEMCOM.NET con la siguiente consulta.");
			
			String query = String.format(consultaValidacionUPC, data.get("tarjeta"));
			System.out.println(query);
			SQLResult resultQuery = db.executeQuery(query);
			boolean consultaBoolean=resultQuery.isEmpty();
			
			if(consultaBoolean) {
    			testCase.addTextEvidenceCurrentStep("-La tarjeta no esta en la tabla gif_routing.");

			}else {
				String descripcionTarjeta= resultQuery.getData(0, "description");
    			testCase.addTextEvidenceCurrentStep("-Tarjeta encontrada: '"+descripcionTarjeta+"'");

			}
			
			//********************* PASO 3 **********************************************************************************************************
			testCase.addBoldTextEvidenceCurrentStep("Mostrar si la tarjeta insertada esta registrada en la tabla gif_promotion en la base de datos FCTPEQA.FEMCOM.NET con la siguiente consulta.");
			
			String queryProm = String.format(consultaValidacionUPCEnGifPromotion, data.get("tarjeta"));
			System.out.println(queryProm );
			SQLResult resultQueryProm = db.executeQuery(query);
			boolean consultaBooleanProm=resultQueryProm.isEmpty();
			if(consultaBooleanProm) {
    			testCase.addTextEvidenceCurrentStep("-La tarjeta no esta en la tabla gif_promotion.");

			}else {
				String descripcionTarjeta= resultQueryProm.getData(0, "description");
    			testCase.addTextEvidenceCurrentStep("-Tarjeta encontrada en la tabla gif_promotion: '"+descripcionTarjeta+"'");

			}
			
			testCase.addBoldTextEvidenceCurrentStep("EVIDENCIAS                                                                                                                                                                                                                                                                                                                                                             ");
			//********************* PASO 1 **********************************************************************************************************
			addStep("Conectarse a la base de datos FCTDCQA.");

			testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "
					+ GlobalVariables.DB_USER_FCTDCQA);
			testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());
			testCase.setProject_Name("Configuracion de ambientes.");
			

		
			//********************* PASO 2 **********************************************************************************************************
			addStep("Mostrar si la tarjeta insertada esta registrada en la tabla gif_routing en la base de datos FCTPEQA.FEMCOM.NET con la siguiente consulta.");
			
			testCase.addQueryEvidenceCurrentStep(resultQuery);
			if(consultaBoolean) {
    			testCase.addTextEvidenceCurrentStep("-La tarjeta no esta en la tabla gif_routing.");

			}else {
				String descripcionTarjeta= resultQuery.getData(0, "description");
    			testCase.addTextEvidenceCurrentStep("-Tarjeta encontrada: '"+descripcionTarjeta+"'");

			}
			//********************* PASO 3 **********************************************************************************************************
			addStep("Mostrar si la tarjeta insertada esta registrada en la tabla gif_promotion en la base de datos FCTPEQA.FEMCOM.NET con la siguiente consulta.");
			
			
			testCase.addQueryEvidenceCurrentStep(resultQueryProm);
			if(consultaBooleanProm) {
    			testCase.addTextEvidenceCurrentStep("-La tarjeta no esta en la tabla gif_promotion.");

			}else {
				String descripcionTarjeta= resultQueryProm.getData(0, "description");
    			testCase.addTextEvidenceCurrentStep("-Tarjeta encontrada en la tabla gif_promotion: '"+descripcionTarjeta+"'");

			}
		}
			
		@Override
		public String setTestFullName() {
			return "ATC_FT_002_PE3_Pre_Requisitos_Config_Ambientes_Pruebas";
		}

		@Override
		public String setTestDescription() {
			return "Terminado. Valida la configuracion de ambientes para pruebas con tarjetas de regalo";
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