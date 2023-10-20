package interfaces.ro1Chl;

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

public class RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/
		
		SQLUtil dbRmsChile = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		SQLUtil dbLogChile = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbEbsChile = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		
/**
 * Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryPaso1 = " SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE \r\n "
				+ " FROM WMUSER.WM_VENDORS_SYNC \r\n"
				+ " WHERE WM_STATUS='L' \r\n"
				+ " AND CONTACTNAME = '"+ data.get("CONTACTNAME")+"' \r\n"
				+ " Order by CREATE_DATE desc \r\n";
		
		String tdcQueryPaso2 = " SELECT TERMS \r\n"
				+ " FROM TERMS \r\n"
				+ " WHERE TERMS_CODE = '%s' \r\n"; 
		
		String tdcQueryPaso3 = " SELECT * FROM ADDR \r\n"
				+ " WHERE ADDR_TYPE = %s \r\n"
				+ " AND ORACLE_VENDOR_SITE_ID = %s \r\n";
				
		String tdcQueryPaso4 = "SELECT SUPPLIER, SUP_NAME, CONTACT_NAME \r\n"
				+ " FROM SUPS \r\n"
				+ " WHERE CONTACT_NAME ='%s' \r\n"
				+ " and SUPPLIER ='%s' \r\n";

		String tdcQueryPaso6 = "select * from ( SELECT run_id,start_dt,status \r\n"
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'RO1_CL' \r\n"
				+ " AND  start_dt >= TRUNC(SYSDATE) \r\n"
			    + " order by start_dt desc)  \r\n"
				+ " where rownum = 1 \r\n";	
		
		String tdcQueryPaso7 =" SELECT * FROM SUPS \r\n"
				+ " WHERE CONTACT_NAME = %s \r\n"
				+ " AND SUPPLIER = %s \r\n"; 
		
		String tdcQueryPaso8 = " SELECT * FROM ADDR \r\n"
				+ " where ADDR_TYPE = %s \r\n"
				+ " AND ORACLE_VENDOR_SITE_ID = %s \r\n";
		
		String tdcQueryPaso9 = " SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE FROM WM_VENDORS_SYNC \r\n"
				+ " WHERE WM_STATUS = 'E' \r\n"
				+ " AND CONTACTNAME = %s \r\n"
				+ " AND ID = %s \r\n";
				
				
		
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//		Paso 1	************************ 

		addStep("Consultar que existan registros en las tablas WM_VENDORS_SYNC de la BD ORAFIN, en donde WM_STATUS es igual 'L'");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(tdcQueryPaso1);

		SQLResult result1 = executeQuery(dbEbsChile, tdcQueryPaso1);

		boolean contact = result1.isEmpty();

		String termsanme = "";
		String ADDR_TYPE = "";
		String VENDORID = "";
		String Contacname = "";

		if (!contact) {

			termsanme = result1.getData(0, "TERMSNAME");
			ADDR_TYPE = result1.getData(0, "ADDR_TYPE");
			VENDORID = result1.getData(0, "VENDORID");
			Contacname = result1.getData(0, "CONTACTNAME");
			testCase.addQueryEvidenceCurrentStep(result1);

		}

		System.out.println(contact);

		assertFalse(contact, "No se obtiene informacion de la consulta");

//		Paso 2	************************

		addStep("Comprobar que existan registros en la tabla TERMS de la BD RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso2 = String.format(tdcQueryPaso2, termsanme);
		System.out.println(FormatoPaso2);

		SQLResult result2 = executeQuery(dbRmsChile, FormatoPaso2);

		boolean terms = result2.isEmpty();

		if (!terms) {

			testCase.addQueryEvidenceCurrentStep(result2);

		}

		System.out.println(terms);

		assertFalse(terms, "La tabla no contiene registros");

//	Paso 3	************************

		addStep("Comprobar que existan registros de los proveedores que se procesaran, en la tabla ADDR de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso3 = String.format(tdcQueryPaso3, ADDR_TYPE, VENDORID);

		System.out.println(FormatoPaso3);

		SQLResult result3 = executeQuery(dbRmsChile, FormatoPaso3);

		boolean addr = result3.isEmpty();

		if (!addr) {

			testCase.addQueryEvidenceCurrentStep(result3);

		}

		System.out.println(addr);

		assertFalse(!addr, "La tabla no contiene registros");

//		Paso 4	************************

		addStep("Comprobar que existan registros de los proveedores que se procesaran, en la tabla SUPS de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso4 = String.format(tdcQueryPaso4, Contacname, VENDORID);
		System.out.println(FormatoPaso4);

		SQLResult result4 = executeQuery(dbRmsChile, FormatoPaso4);

		boolean sups = result4.isEmpty();

		if (!sups) {

			testCase.addQueryEvidenceCurrentStep(result4);

		}

		System.out.println(sups);

		assertFalse(!sups, "La tabla no contiene registros");

//		Paso 5	************************

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		testCase.setProject_Name("AutomationQA");

		addStep("Ejecutar el servicio RO1.Pub:run.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		//pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

//	Paso 6	************************

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'RO1' y STATUS es igual a 'S'.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso6);
		SQLResult result6 = executeQuery(dbLogChile, tdcQueryPaso6);

		boolean actualizarsups = result6.isEmpty();

		if (!actualizarsups) {

			testCase.addQueryEvidenceCurrentStep(result6);

		}

		System.out.println(actualizarsups);

		assertFalse(actualizarsups, "Los proveedores no se actualizaron correctamente");

//		Paso 7	************************

		addStep("Confirmar que se actualizaron correctamente los proveedores en la tabla SUPS de la BD RETEK para el CONTACT_NAME");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso7 = String.format(tdcQueryPaso7, Contacname, VENDORID);
		System.out.println(FormatoPaso7);

		SQLResult result7 = executeQuery(dbRmsChile, FormatoPaso7);

		boolean actualizaraddr = result7.isEmpty();

		if (!actualizaraddr) {

			testCase.addQueryEvidenceCurrentStep(result7);

		}

		System.out.println(actualizaraddr);

		assertFalse(actualizaraddr, "Los registros no fueron actualizados correctamente");

//		Paso 8	************************

		addStep("Confirmar que se actualizaron los registros en la tabla ADDR de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);

		String FormatoPaso8 = String.format(tdcQueryPaso8, ADDR_TYPE, VENDORID);
		System.out.println(FormatoPaso8);

		SQLResult result8 = executeQuery(dbEbsChile, FormatoPaso8);

		boolean vendors = result8.isEmpty();

		if (!vendors) {

			testCase.addQueryEvidenceCurrentStep(result8);

		}

		System.out.println(vendors);

		assertFalse(vendors, "Los registros no fueron actualizados correctamente");

//		Paso 9	************************

		addStep("Confirmar que se actualizaron los registros en la tabla ADDR de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		String FormatoPaso9 = String.format(tdcQueryPaso9, VENDORID);
		System.out.println(FormatoPaso9);

		SQLResult result9 = executeQuery(dbEbsChile, FormatoPaso9);

		boolean ValPaso9 = result9.isEmpty();

		if (!ValPaso9) {

			testCase.addQueryEvidenceCurrentStep(result9);

		}

		System.out.println(ValPaso9);

		assertFalse(ValPaso9, "Los registros no fueron actualizados correctamente");
	}

	@Override
	public void beforeTest() {
// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
// TODO Auto-generated method stub
		return "Actualizar los registros de proveedores en RETEK Chile";
	}

	@Override
	public String setTestDesigner() {
// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
// TODO Auto-generated method stub
		return "ATC_FT_002_RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor";
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
