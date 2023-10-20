package interfaces.TpeLot;

import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.TPE_LOT;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TPE_LOT_Verificar_Operacion_TRN02_Cancelacion_Boleto extends BaseExecution {
	
	/*
	 * 
	 * @cp Verificar la operacion TRN02 - Cancelacion del boleto
	 * 
	 */

	@Test(dataProvider = "data-provider")

	public void ATC_FT_002_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN02_Cancelacion_Boleto(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,
				GlobalVariables.DB_PASSWORD_TPE_LOT);

		TPE_LOT TPELOTUTIL = new TPE_LOT(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/

		String wmCodeTRN02 = "100";
		String wmCodeRequesTRN02_cancelacion = "000";
		
		String tdcTRN02_01= "SELECT APPLICATION,ENTITY,OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,MTI,PROC_CODE,WM_CODE,WM_DESC,LOCAL_DT,AUTH_ID_RES "
				+ " FROM TPEUSER.TPE_FR_TRANSACTION"
				+ " WHERE APPLICATION='LOT' AND "
				+ " ENTITY='MLJ' AND "
				+ " OPERATION='TRN01' AND "
				//+ " TRUNC(CREATION_DATE)=TRUNC(SYSDATE) AND "
				+ " PLAZA='10MON' AND "
				+ " FOLIO=%s";
		
		String tdcTRN02_02 = "SELECT APPLICATION,ENTITY,OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,MTI,PROC_CODE,WM_CODE,WM_DESC,LOCAL_DT,AUTH_ID_RES"
				+ " FROM TPEUSER.TPE_FR_TRANSACTION"
				+ " WHERE APPLICATION='LOT' AND "
				+ " ENTITY='MLJ' AND "
				+ " OPERATION='TRN02' AND "
				//+ " TRUNC(CREATION_DATE)=TRUNC(SYSDATE) AND "
				+ " PLAZA='10MON' AND "
				+ " FOLIO = %s ";
				
				


		String folio;
		String folio1;
		String creationDate;
		

//	String wmCodeDb;

		/*
		 * Paso 1
		 *****************************************************************************************/
//Paso 1
		addStep("Correr el servicio TPE.LOT.Pub:request para obtener un nuevo folio.");

		String respuestaTRN02Cancelacion_parte1 = TPELOTUTIL.TRN02_Cancelacion_Parte1();

		System.out.println("Respuesta: " + respuestaTRN02Cancelacion_parte1);

		folio = RequestUtil.getFolioXml(respuestaTRN02Cancelacion_parte1);
		creationDate = RequestUtil.getCreationDate(respuestaTRN02Cancelacion_parte1);
	

		testCase.passStep();

		/* Paso 2 *********************************************************/

		addStep("Validar registro de la Transaccion en la tabla TPE_FR_TRASACTION");
		String query = String.format(tdcTRN02_01, folio);
		System.out.println(query);

	      SQLResult wmCodeDb = executeQuery(db, query);
	       
	       boolean validationDb = wmCodeDb.isEmpty();
	       if(!validationDb) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(wmCodeDb);   
	       }
	       System.out.println(validationDb);
			
	      assertFalse(validationDb); 

		/* Paso 3 *********************************************************/

		addStep("Ejecutar via HTTP la interface FEMSA_TPE_LOT con el XML indicado");
		
		String respuestaTRN02Cancelacion_parte2 = TPELOTUTIL.TRN02Cancelacion(folio,creationDate);

		System.out.println("Respuesta: " + respuestaTRN02Cancelacion_parte2);

		testCase.passStep();

		
		/* Paso 4 *********************************************************/

		addStep("Verificar xmlResponse, wmCode 000");
		
		folio1 = RequestUtil.getFolioXml(respuestaTRN02Cancelacion_parte2);
		wmCodeRequesTRN02_cancelacion = RequestUtil.getWmCodeXml(respuestaTRN02Cancelacion_parte2);

		testCase.passStep();

		

		/* Paso 5 *********************************************************/

		addStep("Verificar xmlResponse, wmCode 000");
		
		addStep("Validar registro de la Transaccion en la tabla TPE_FR_TRASACTION");
		String query_1 = String.format(tdcTRN02_02, folio1);
		System.out.println(query_1);

	      SQLResult wmCodeDb_1 = executeQuery(db, query_1);
	       
	       boolean validationDb2 = wmCodeDb_1.isEmpty();
	       if(!validationDb2) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(wmCodeDb_1);   
	       }
	       System.out.println(validationDb2);
			
	      assertFalse(validationDb2);
		
		
 
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion TRN02(Cancelación del boleto)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN02_Cancelacion_Boleto";
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
