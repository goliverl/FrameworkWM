package interfaces.pe4v2;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE4v2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE4v2FacturaExitosa extends BaseExecution {
	
	/*
	 * 
	 * @cp1 Generar CFDi. Source AR
	 * @cp2 Generar CFDi. Source POS
	 * @cp3 Generar CFDi. Source POSMES 
	 * @cp4 Generar CFDi. Source PORTAL
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE4V2_PE4v2FacturaExitosa(HashMap<String, String> data) throws Exception {
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,
				GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);		
		
		String sqlQuery = "SELECT FOLIO, CREATION_DATE, REFID, PLAZA, TICKET, TOTAL, SOURCE, WM_CODE "
				+ "FROM TPEUSER.cfd_transaction "
				+ " WHERE ticket = '%s'"
				+ " AND TRUNC(creation_date) = TRUNC(SYSDATE) "
				+ " AND source = '%s'"
				+ " AND WM_CODE = '101'"
				+ " ORDER BY creation_date DESC";
				
		
		//Valores WMcode esperados:
		String expectedWMCodeQueryCFDI = "101";
		
		PE4v2 pe4Obj = new PE4v2(data, testCase, db);
	/**************************************************************************************************
	 * Pasos
	 *************************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Llamar al servico PE4v2.Pub:runGetCFDi" ) ;
		
    	String queryTDCResponse = pe4Obj.executeRunGetCFDI();
    	
    	String actualWMCode = RequestUtil.getWmCodeXml(queryTDCResponse);
    	testCase.addTextEvidenceCurrentStep("-El servicio PE4v2.Pub:runGetCFDi se ejecuto");
    	
    	/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Verificar la respuesta generada por el servicio");

		boolean validationRequest = actualWMCode.equals(expectedWMCodeQueryCFDI);

		System.out.println(validationRequest + " - wmCode request: " + actualWMCode);

		testCase.addTextEvidenceCurrentStep(queryTDCResponse);
		
    	testCase.addTextEvidenceCurrentStep("-La Respuesta WM_CODE es:"+actualWMCode);
    	
		assertTrue(validationRequest, "El campo wmCode no es el esperado");
		
		
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Verificar la insercion del ticket consultado en la tabla TPEUSER.CFD_TRANSACTION");
		
		
		String CFDI = String.format(sqlQuery, data.get("noTicket"), data.get("source"));
		System.out.println(CFDI);

		SQLResult resultQuery = db.executeQuery(CFDI);

		boolean resultQueryEmpty = resultQuery.isEmpty();
		
		
		if(!resultQueryEmpty) {
		testCase.addQueryEvidenceCurrentStep(resultQuery);
    	testCase.addTextEvidenceCurrentStep("Se genero el registro");

		}
		
		assertTrue(!resultQueryEmpty, "El campo wmCode no es el esperado");

  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_002_PE4V2_PE4v2FacturaExitosa" ;
  }

  @Override
  public String setTestDescription() {
    return "Verificar la generacion del CFDi source AR\r\n"
    		+ "Verificar la generacion del CFDi source POS\r\n"
    		+ "Verificar la generacion del CFDi source PORTAL\r\n"
    		+ "Verificar la generacion del CFDi source POSMES";
  }

  @Override
  public String setTestDesigner() {
    return "Equipo de Automatizacion" ;
  }

  @Override
  public String setTestInstanceID() {
    return null ;
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

