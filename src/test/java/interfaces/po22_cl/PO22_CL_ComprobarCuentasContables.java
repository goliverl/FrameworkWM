package interfaces.po22_cl;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PO22_CL_ComprobarCuentasContables extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO22_CL_Comprobar_Cuentas_Contables(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryFac = "SELECT NAME, MODULE, IF_ID, VALUE "
				+ " FROM wmuser.WM_ORACLE_PARAMS"
				+ " WHERE IF_ID = '" + data.get("interface") +"'"
				+ " AND NAME = 'CUENTA_ITEM' "
				+ " AND MODULE = 'FC'"
				+ " AND VALUE = 21020208";
		
		String tdcQueryDev = "SELECT NAME, MODULE, IF_ID, VALUE "
				+ " FROM wmuser.WM_ORACLE_PARAMS"
				+ " WHERE IF_ID = '" + data.get("interface") +"'"
				+ " AND NAME = 'CUENTA_ITEM'"
				+ " AND MODULE = 'DV'"
				+ " AND vALUE = 11040308";
		
		String tdcQueryFreight = "SELECT NAME, MODULE, IF_ID, VALUE "
				+ " FROM wmuser.WM_ORACLE_PARAMS"
				+ " WHERE IF_ID = '" + data.get("interface") +"' "
				+ " AND NAME = 'CUENTA_FREIGHT'"
				+ " AND MODULE IN ( 'FC', 'DV' ) "
				+ " AND VALUE = 51020102";
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Acceder a la BD WM Chile con esquema WMUSER.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_PosUserChile);
		
//Paso 2 *************************	
		addStep("Comprobar que la cuenta item del archivo FAC tenga 21020208.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryFac);
		
		SQLResult facResult = executeQuery(dbPos, tdcQueryFac);
		
		boolean item = facResult.isEmpty();
		
		if (!item) {
			
			testCase.addQueryEvidenceCurrentStep(facResult);
		}
		
		System.out.println(item);
		
		assertFalse(item, "No se obtiene información de la consulta");
		
//Paso 3 *************************		
		addStep("Comprobar que la cuenta item del archivo DEV tenga 11040308.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryDev);
		
		SQLResult devResult = executeQuery(dbPos, tdcQueryDev);
		
		boolean itemDev = devResult.isEmpty();
		
		if (!itemDev) {
			
			testCase.addQueryEvidenceCurrentStep(devResult);
		}
		
		System.out.println(itemDev);
		
		assertFalse(itemDev, "No se obtiene información de la consulta");
		
//Paso 4 *************************		
		addStep("Comprobar que la cuenta FREIGHT de ambos archivos tenga 51020102.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryFreight);
		
		SQLResult freightResult = executeQuery(dbPos, tdcQueryFreight);
		
		boolean freight = freightResult.isEmpty();
		
		if (!freight) {
			
			testCase.addQueryEvidenceCurrentStep(freightResult);
		}
		
		System.out.println(freight);
		
		assertFalse(freight, "No se obtiene información de la consulta");
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Comprobar que las cuentas contables que se encuentran en la configuración de la interface se encuentren con sus valores correctos.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO22_CL_Comprobar_Cuentas_Contables";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
