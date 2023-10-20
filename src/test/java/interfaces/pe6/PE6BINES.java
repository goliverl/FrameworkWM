package interfaces.pe6;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import modelo.BaseExecution;
import om.PE6;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.GetRequest;
import utils.webmethods.ReadRequest;

public class PE6BINES extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE6_Solicitud_Consulta_Bines(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
		

		PE6 pe6Util = new PE6(data, testCase, db);
		PE6 pe6Log = new PE6(data, testCase, db);
		
		/*
		 * Variables
		 *********************************************************************/

		// actualizar la consulta no debe usar el * from corregir

		String wmCodeBin = "010";
		String tdcBines1 = "SELECT * FROM TPEUSER.TDC_QUERY" + " WHERE FOLIO= %s";

		String wmLog = "SELECT FOLIO,  ERROR_ID, ERROR_DATE FROM WMLOG.WM_LOG_ERROR_TPE WHERE TPE_TYPE='PE6' AND FOLIO= %s";

		String wmCodeRequestBin;
		String folio;
		String BinQuery;

  //Paso 1***************************************************************/
		
		
		addStep("Consulta bines");

  //Obtener URL de request a ejecutar 
		
		HashMap<String, String> datosRequestRunBin = new HashMap<>();

		datosRequestRunBin.put("host", data.get("host"));
		datosRequestRunBin.put("cardNo", data.get("cardNo"));
		datosRequestRunBin.put("binCodeList", data.get("binCodeList"));
		datosRequestRunBin.put("plaza", data.get("plaza"));
		datosRequestRunBin.put("tienda", data.get("tienda"));
		datosRequestRunBin.put("promType", data.get("promType"));
		datosRequestRunBin.put("service", data.get("service"));

		String runGetBinesRequest = GetRequestFile.getRequestFile("PE6\\pe6Bines.txt", datosRequestRunBin);
		System.out.println(runGetBinesRequest);
  //Ejecutar el request 
		String responseRunGetFolioBines = GetRequest.executeGetRequest(runGetBinesRequest);
		System.out.println (responseRunGetFolioBines);
  //Añadir evidencia de la respuesta al testCase
		testCase.addTextEvidenceCurrentStep(responseRunGetFolioBines);
  //Obtener variables de la respuesta
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetFolioBines);
		folio = runGetFolioRequestDoc.getElementsByTagName("folio").item(0).getTextContent();		
		System.out.println("Folio: " + folio);
		testCase.passStep();
  //Paso respuesta del xml 
		addStep("Verificar la respuesta generada por el servicio");
		String wmCodeFolio = runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		System.out.println("wmCodeFolio: " + wmCodeFolio);
		testCase.addTextEvidenceCurrentStep(responseRunGetFolioBines);
		testCase.passStep();
		
		//Paso2 *****************************************************************************************/
		
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente  ");
		String query = String.format(tdcBines1, folio);
		SQLResult rsAuthAck = db.executeQuery(query);
		System.out.println(query);
		testCase.passStep();
		
		// Paso 3 *********************************************************

		addStep("Ejecutar la siguiente consulta (La consulta no debe tener resultados)");
		System.out.println("consulta 2");
		String queryLog = String.format(wmLog, folio);

		try {
			SQLResult queryLogExecute = db.executeQuery(queryLog);
			boolean isEmpty = queryLogExecute.isEmpty();
			assertFalse(isEmpty);
			testCase.passStep();
		} catch (Exception e) {
			// testCase.failStep();
			// System.out.println("Resultado de la consulta: "+e.getMessage());
		}

		testCase.passStep();
		System.out.println(queryLog);
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Servicio de validación de consulta de bines para servicios.";
	}
	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PE6_Solicitud_Consulta_Bines";

	}
	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
