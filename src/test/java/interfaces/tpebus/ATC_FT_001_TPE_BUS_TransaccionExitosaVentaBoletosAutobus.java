package interfaces.tpebus;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static util.RequestUtil.getFolioXml;
import static util.RequestUtil.getWmCodeXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_BUS;
import util.GlobalVariables;

import utils.sql.SQLResult;

public class ATC_FT_001_TPE_BUS_TransaccionExitosaVentaBoletosAutobus extends BaseExecution {

	
	@Test(dataProvider= "data-provider")
	public void ATC_FT_001_TPE_BUS_TransaccionExitosaVentaBoletosAutobus_test(HashMap<String, String> data) throws Exception {
		
		/**
		 * TPE_BUS: MTC-FT-053-C1 Transaccion exitosa de venta de boletos de autobus SENDA
		 * Desc:
		 * Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de
		 * transaccionalidad de la interface TPE_BUS y TPE_FR al ser migradas de WM9.9 a WM10.5
		 * 
		 * Mtto:
		 * @author Jose Onofre
		 * @date 02/17/2022
		 * 
		 */
		//Utilerias		
		//SqlUtil db = new SqlUtil(GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE, GlobalVariables.DB_HOST_FCTPE);
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		String wmCodeQuery1 = "SELECT application, entity, operation, source, folio, creation_date, plaza,tienda, resp_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'BUS'"
				+ " AND ENTITY = 'TICKET'"
				+" AND FOLIO = %s";
		
		//Valores WMcode esperados:
		final String expectedWMCodeTRN01 = "100";
		final String expectedWMCodeQRY01 = "101";
		final String expectedWMCodeQRY02 = "101";
		final String expectedWMCodeQRY03 = "101";
		final String expectedWMCodeTRN02 = "000";
		final String expectedWMCodeTRN03 = "101";		

		TPE_BUS bus = new TPE_BUS(data, testCase, db);
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		
				//Paso 1
				addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transaccion para la generacion del Folio" ) ;
				
				String TRN01Response = bus.executeTRN01();
				String folio = getFolioXml(TRN01Response);
				String actualWmCodeTRN01 = getWmCodeXml(TRN01Response);
				assertEquals(actualWmCodeTRN01, expectedWMCodeTRN01);
				
				String wmCodeQuery=String.format(wmCodeQuery1, folio);
				
				SQLResult hora = db.executeQuery(wmCodeQuery );
	    		boolean paso1 = hora.isEmpty();
	    		if (!paso1) {
	    			testCase.addQueryEvidenceCurrentStep(hora);
	    		}
	    		assertFalse("No hay insumos a procesar", paso1);
			
				//Paso 2
			addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transaccion de consulta de destinos para la linea." ) ;
				String QRY01Response = bus.executeQRY01(folio);
				String actualWmCodeQRY01 = getWmCodeXml(QRY01Response);
				assertEquals(actualWmCodeQRY01, expectedWMCodeQRY01);
			
				
				
				//Paso 3
			addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transacción de consulta de corridas disponibles para la linea con el Origen-Destino proporcionado." ) ;
				String QRY02Response = bus.executeQRY02(folio);
				String actualWmCodeQRY02 = getWmCodeXml(QRY02Response);
				assertEquals(actualWmCodeQRY02, expectedWMCodeQRY02);
			
				
				//Paso 4
			addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transaccion de consulta de asientos disponibles de una corrida para la linea." ) ;
				String QRY03Response = bus.executeQRY03(folio);
				String actualWmCodeQRY03 = getWmCodeXml(QRY03Response);
				assertEquals(actualWmCodeQRY03, expectedWMCodeQRY03);
			
				
				//Paso 5
			addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transaccion de servicio de venta para la linea." ) ;
				String TRN02Response = bus.executeTRN02(folio);
				String actualWmCodeTRN02 = getWmCodeXml(TRN02Response);
				assertEquals(actualWmCodeTRN02, expectedWMCodeTRN02);
				SQLResult hora2= db.executeQuery(wmCodeQuery );
	    		boolean paso2 = hora.isEmpty();
	    		if (!paso2) {
	    			testCase.addQueryEvidenceCurrentStep(hora2);
	    		}
	    		assertFalse("No hay insumos a procesar", paso2);
				//Paso 6
			addStep("Ejecutar el servicio TPE.BUS.Pub:request para realizar la transaccion de cierre de transacion de venta para la linea." ) ;
				String TRN03Response = bus.executeTRN03(folio);
				String actualWmCodeTRN03 = getWmCodeXml(TRN03Response);
				assertEquals(actualWmCodeTRN03, expectedWMCodeTRN03);
				SQLResult hora3 = db.executeQuery(wmCodeQuery );
	    		boolean paso3 = hora.isEmpty();
	    		if (!paso3) {
	    			testCase.addQueryEvidenceCurrentStep(hora3);
	    		}
	    		assertFalse("No hay insumos a procesar", paso3);
			
				
	}
	
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}


	
	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
