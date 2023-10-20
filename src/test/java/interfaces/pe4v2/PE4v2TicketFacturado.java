package interfaces.pe4v2;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getWmCodeXml;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE4v2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE4v2TicketFacturado extends BaseExecution {
	
	/*
	 * 
	 * @cp1 Consulta de ticket; ya fue facturado
	 * @cp2 Generar CFDi. Source PORTAL - ticket ya facturado
	 * @cp3 Generar CFDi. Source POS - ticket ya facturado
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PE4V2_PE4v2TicketFacturado(HashMap<String, String> data) throws Exception {
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,	GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		//Valores WMcode esperados:
		final String expectedWMCodeQueryTdc = "117";
		
		PE4v2 pe4Obj = new PE4v2(data, testCase, db);
	/**************************************************************************************************
	 * Solicitud de folio
	 *************************************************************************************************/
			
		//Paso 1
		addStep("Ejecutar el servicio PE4v2.Pub:runGetCFDi" ) ;
		
    	String queryTDCResponse = pe4Obj.executeRunQueryYaFacturado();
    	
		String queryTDCWMCode = getWmCodeXml(queryTDCResponse);
		testCase.addTextEvidenceCurrentStep("Se ejecuto el servicio PE4v2.Pub:runGetCFDi");

    	//assertEquals(queryTDCWMCode,expectedWMCodeQueryTdc);
    	
    	 //* Paso 2
		 //*****************************************************************************************/
		addStep("Validar que la respuesta de la ejecucion sea 'Ticket ya facturado', wmCode=117");

		
		boolean validationRequest = queryTDCWMCode.equals(expectedWMCodeQueryTdc);

		System.out.println(validationRequest + " - wmCode request: " + queryTDCWMCode);

		testCase.addTextEvidenceCurrentStep(queryTDCResponse);
		
		if(validationRequest) {
			testCase.addTextEvidenceCurrentStep("El ticket ya ha sido facturado");
			testCase.addTextEvidenceCurrentStep("wmCode:"+queryTDCWMCode);

		}

		assertTrue(validationRequest, "El campo wmCode no es el esperado");
		
	

  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_006_PE4V2_PE4v2TicketFacturado" ;
  }

  @Override
  public String setTestDescription() {
    return "Realizar la consulta de ticket y validar que ya fue facturado\r\n"
    		+ "Verificar la generación del CFDi source PORTAL ticket ya facturado\r\n"
    		+ "Verificar la generación del CFDi source POS ticket ya facturado" ;
  }

  @Override
  public String setTestDesigner() {
    return "Equipo de Automatizacion" ;
  }

  @Override
  public String setTestInstanceID() {
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

