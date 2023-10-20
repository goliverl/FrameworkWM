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

public class RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor_Error extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor_Error(HashMap<String, String> data) throws Exception {
		
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
				+ " AND CONTACTNAME = '"+ data.get("CONTACTNAME")+"' \r\n";
		
		String tdcQueryPaso2 = " SELECT TERMS \r\n"
				+ " FROM TERMS \r\n"
				+ " WHERE TERMS_CODE = '%s' \r\n"; 
		

		String tdcQueryPaso4 = "select * from ( SELECT run_id,start_dt,status \r\n"
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'RO1_CL' \r\n"
				+ " AND  start_dt >= TRUNC(SYSDATE) \r\n"
			    + " order by start_dt desc)  \r\n"
				+ " where rownum = 1 \r\n";	
		
		String tdcQueryPaso5 =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION \r\n"
				+ " FROM WMLOG.WM_LOG_ERROR \r\n"
				+ " where RUN_ID=%s \\n"; //FCWMLQA
		
		String tdcQueryPaso6 = " SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE \r\n"
				+ " FROM WMUSER.WM_VENDORS_SYNC \r\n"
				+ " WHERE WM_STATUS = 'F' \r\n"
				+ " AND CONTACTNAME ='"+ data.get("CONTACTNAME")+"' \r\n"
				+ " AND ID='%s' \r\n";
		
		
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//		Paso 1	************************ 

		addStep(" Consultar que existan registros en las tablas WM_VENDORS_SYNC de la BD ORAFIN, en donde WM_STATUS es igual 'L'");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(tdcQueryPaso1);

		SQLResult result1 = executeQuery(dbEbsChile, tdcQueryPaso1);

		boolean contact = result1.isEmpty();

		String Termsname = "";
		String ID = "";

		if (!contact) {

			Termsname = result1.getData(0, "TERMSNAME");
			ID = result1.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(result1);

		}

		System.out.println(contact);

		assertFalse(contact, "No se obtiene informacion de la consulta");

//		Paso 2	************************

		addStep(" Comprobar que NO existan registros en la tabla TERMS de la BD RETEK para el registro en donde TERMS_CODE es igual a  WM_VENDORS_SYNC.TERMSNAME.\n");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso2 = String.format(tdcQueryPaso2, Termsname);
		System.out.println(FormatoPaso2);

		SQLResult result2 = executeQuery(dbRmsChile, FormatoPaso2);

		boolean terms = result2.isEmpty();

		if (!terms) {

			testCase.addQueryEvidenceCurrentStep(result2);

		}

		System.out.println(terms);

		assertFalse(terms, "La tabla no contiene registros");

//	Paso 3	************************

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		testCase.setProject_Name("AutomationQA");

		addStep("Se invoca la interface RO1.Pub:run. mediante la ejecución del JOB runRO1.\n");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);
		//pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//		Paso 4	************************

		addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'RO1' y STATUS es igual a 'E'.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso4);
		SQLResult result4 = executeQuery(dbLogChile, tdcQueryPaso4);

		boolean sups = result4.isEmpty();
		String runid = "";

		if (!sups) {

			runid = result4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(result4);

		}

		System.out.println(sups);

		assertFalse(sups, "La tabla no contiene registros");

//		Paso 5	************************
		
		addStep("Comprobar los detalles del error en la tabla WM_LOG_ERROR en la BD WMLOG.\n");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String FormatoPaso5 = String.format(tdcQueryPaso5, runid);
		System.out.println(FormatoPaso5);
		SQLResult result5 = executeQuery(dbLogChile, FormatoPaso5);

		boolean ValPaso5 = result4.isEmpty();

		if (!ValPaso5) {

			runid = result4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(result5);

		}

		System.out.println(sups);

		assertFalse(sups, "La tabla no contiene registros");

//   	Paso 6	************************

		addStep("Comprobar que los registros fueron actualizados correctamente en la tabla WM_VENDORS_SYNC en la BD ORAFIN donde WM_STATUS es igual a 'F', y CONTACTNAME");
		
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		
	     String FormatoPaso6 = String.format(tdcQueryPaso6, ID);	
	
		System.out.println(FormatoPaso6);
		SQLResult result6 = executeQuery(dbRmsChile, FormatoPaso6);

		boolean actualizarsups = result6.isEmpty();

		if (!actualizarsups) {

			testCase.addQueryEvidenceCurrentStep(result6);

		}

		System.out.println(actualizarsups);

		assertFalse(actualizarsups, "Los proveedores no se actualizaron correctamente");

	}

	@Override
	public void beforeTest() {
// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
// TODO Auto-generated method stub
		return "Actualizar los registros de proveedores en RETEK";
	}

	@Override
	public String setTestDesigner() {
// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
// TODO Auto-generated method stub
		return "ATC_FT_001_RO1_Chl_Actualizar_Registros_Proveedores_Retek_Para_Proveedor_Error";
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
