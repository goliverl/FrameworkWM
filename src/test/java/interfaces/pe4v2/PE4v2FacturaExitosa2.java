package interfaces.pe4v2;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE4v2;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE4v2FacturaExitosa2 extends BaseExecution {
	

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PE4V2_PE4v2FacturaExitosa2(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,	GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);		
		
/**
* Variables ******************************************************************************************
* 
* 
*/			
		String tdcQueryCFD = "select * from (Select FOLIO, CREATION_DATE,PLAZA, TIENDA, TICKET, TOTAL, SOURCE, WM_CODE, WM_DESC, is_name "
				+ " from  TPEUSER.CFD_TRANSACTION"
				+ " where folio = 167531039"
				+ " order by CREATION_DATE DESC)"
				+ " where rownum= 1";
		
		String tdcQueryCFD2 = "Select FOLIO, CREATION_DATE,PLAZA, TIENDA, TICKET, TOTAL, SOURCE, WM_CODE, WM_DESC, IS_NAME "
				+ " from  TPEUSER.CFD_TRANSACTION"
//				+ " where folio = %"
				+ " order by CREATION_DATE DESC";
				
		
		//Valores WMcode esperados:
		String expectedWMCodeQueryCFDI = "101";
		
		PE4v2 pe4Obj = new PE4v2(data, testCase, db);
		testCase.setProject_Name("Carta Porte");
	/**************************************************************************************************
	 * Pasos
	 *************************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Enviar desde navegador:" ) ;

    	String queryTDCResponse = pe4Obj.executeRunGetCFDIv3_1();
//    	String queryTDCResponse = pe4Obj.executeRunGetCFDIv3_2();
    	
    	System.out.println("\n" + queryTDCResponse + "\n");
    	
//    	String folio = getSimpleDataXml(queryTDCResponse, "plaza");
    	
//    	System.out.println(folio);
    	
    	String actualWMCode = RequestUtil.getWmCodeXml(queryTDCResponse); 	

		boolean validationRequest = actualWMCode.equals(expectedWMCodeQueryCFDI);

		System.out.println(validationRequest + " - wmCode request: " + actualWMCode);

//		testCase.addTextEvidenceCurrentStep(queryTDCResponse);
		
    	testCase.addTextEvidenceCurrentStep("-La Respuesta WM_CODE es:"+actualWMCode);
    	
		assertTrue(validationRequest, "El campo wmCode no es el esperado");
		
		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Ingresar a la BD FCTPEQA.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTPE);
		
		
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Realizar la consulta:");
		
		
//		String CFDI = String.format(tdcQueryCFD, folio);
//		System.out.println(CFDI);
		testCase.addTextEvidenceCurrentStep(tdcQueryCFD2);
		SQLResult resultQuery = db.executeQuery(tdcQueryCFD);
		
//		testCase.addQueryEvidenceCurrentStep(resultQuery,false);
		

		boolean resultQueryEmpty = resultQuery.isEmpty();
		
		
		if(!resultQueryEmpty) {
		testCase.addQueryEvidenceCurrentStep(resultQuery, false);
    	testCase.addTextEvidenceCurrentStep("Se registra transacción en tabla.");

		}
		
		assertFalse(resultQueryEmpty, "No se registra transacción en tabla.");

  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_003_PE4V2_PE4v2FacturaExitosa2" ;
  }

  @Override
  public String setTestDescription() {
    return "Construida. FEMSA_PE4V2_PE4v2FacturaExitosa2" ;
  }

  @Override
  public String setTestDesigner() {
    return "Carmen Maria Rivas Salgado" ;
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


