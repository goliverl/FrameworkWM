package interfaces.oe2_mx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_005_OE2_MX_ErrorObtenDatoConfigInterfaz extends BaseExecution {
	
	/*
	 * 
	 * @cp MTC-FT-005 -Error al obtener datos de configuracion de la interfaz
	 * @author JoseOnofre@Hexaware.com
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OE2_MX_ErrorObtenDatoConfigInterfaz_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		
		String consulta_registros  = "SELECT REFID,SERIE,FOLIO,UUID,FECHA_TIMBRE,TIPO_COMPROBANTE,WM_STATUS,WM_RUN_ID,VERSION_CFDI,ORIGEN,\r\n" + 
				"ID_FACTURA_DIGITAL,REC_RFC,REC_NOMBRE,REC_CODIGO_POSTAL,REC_REGIMEN_FISCAL,\r\n" + 
				"REGIMEN_TRIBUTARIO,LAST_UPDATE_DATE,EXPORTACION\r\n" + 
				"FROM XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n" + 
				"WHERE VERSION_CFDI ='%s'\r\n" + 
				"AND WM_STATUS ='%s'\r\n" + 
				"AND ORIGEN ='%s'";

		String consultaWM_LOG_RUN= "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE "
				+ "LIKE'%RR08%' ORDER BY START_DT DESC";
		
		String consultaWM_LOG_ERROR  = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='RUN_ID'";
		
		
		String expec_WM_LOG_RUN_stat = "S";
		
	
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
	
			
		
		// Paso 1 ****************************************************
		
		addStep("Ingresar el IS del servidor de FCWMINTQA3");
		
		System.out.println("http://10.182.32.15:5555/");
		
		
		// Paso 2 ****************************************************
		
		addStep("Posicionarse en la siguiente ruta Adapters > "
				+ "webMethods Adapter for JDBC > Connections y "
				+ "deshabilitar el adapter ");
		
		System.out.println("AdapterConnections:DBS_WMINT_NT");
		
		// Paso 3 ****************************************************
		
		addStep("Realizar conexion a la BD de EBS  FCAVEBQA");
		
		
		boolean conexiondbAVEBQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQA, "La conexion no fue exitosa");
	
		
		// Paso 2 ****************************************************
		
		addStep("Validar con la siguiente consulta en la base de datos de WM FCWMFSIT "
				+ "los datos de configuracion  (Operacion, Valor1) del cedis a procesar");
		
		String consulta_reg_proces = String.format(consulta_registros,data.get("version_cfdi"), data.get("wm_status"), data.get("origen"));
		
		SQLResult consulta_reg_proces_r = executeQuery(dbAVEBQA, consulta_reg_proces);
		

		boolean validaReg_proces = consulta_reg_proces_r.isEmpty();

		if (!validaReg_proces) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_reg_proces_r);
		}

		System.out.println(validaReg_proces);

		assertFalse(validaReg_proces, "No se encontraron registros en la consulta");
		
		
		// Paso 3 ****************************************************
		
		addStep("");
		
		
	}
	
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_OE2_MX_ErrorObtenDatoConfigInterfaz_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Error al facturar por no obtener configuracion de interfaz";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseOnofre@Hexaware.com";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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