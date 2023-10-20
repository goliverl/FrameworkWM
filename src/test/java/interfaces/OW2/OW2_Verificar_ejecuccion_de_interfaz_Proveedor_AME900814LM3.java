package interfaces.OW2;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OW2_Verificar_ejecuccion_de_interfaz_Proveedor_AME900814LM3 extends BaseExecution  {
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_OW2_002_Verificar_ejecuccion_de_interfaz_Proveedor_AME900814LM3(HashMap<String, String> data) throws Exception {
				
/* Utilerías *********************************************************************/

		SQLUtil dbAVEBA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCIASQA = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
/* Variables *********************************************************************/
					

	String tdcQRY1 = " SELECT *"
			+ " FROM XXFC.XXINV_WM_ITEM_SUPPLIERS_TMP "
			+ " WHERE SUPPLIER_NUMBER = '"+data.get("number")+"' AND "
			+ " WM_STATUS = 'L'";
			
	
	String tdcQRY3 = " SELECT * FROM WMLOG.WM_LOG_RUN "
			+ " WHERE INTERFACE='OW2' AND "
			+ " STATUS='S' "
			+ " ORDER BY RUN_ID DESC";
			
	
	String tdcQRY4 = " SELECT * FROM XXEX.XXEX_SYNC_ART_PROV "
			+ " WHERE TRUNC(RECEIVED_DATE)=TRUNC(SYSDATE) AND "
			+ " WM_STATUS = 'L'  AND "
			+ " NUM_PROVEEDOR = '"+data.get("number")+"'";

	String tdcQRY5 = " SELECT * FROM XXFC.XXINV_WM_ITEM_SUPPLIERS_TMP "
			+ " WHERE  SUPPLIER_NUMBER = '"+data.get("number")+"' AND "
			+ " WM_STATUS='E'  AND "
			+ " TRUNC(WM_SENT_DATE)=TRUNC(SYSDATE) AND  "
			+ " WM_RUN_ID=%s ";
	
	String tdcQRY6 = " SELECT * FROM XXEX.XXEX_LOG_EJECUCION "
			+ "WHERE ERRORES = %s";

	
	SeleniumUtil u;
	PakageManagment pok;
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword(data.get("ps"));
	String server = data.get("Server");
			
	
			
			
/* Paso 1 *****************************************************************************************/
		
		addStep("Validar que exista información en la tabla XXINV_WM_ITEM_SUPPLIERS_TMP de ORAFIN.");
			
    	System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbAVEBA.executeQuery(tdcQRY1);

		boolean Paso1Empty = Paso1.isEmpty();
		
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

     	assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		

/*Paso 2 *********************************************************/

		addStep("Invocar el servicio OW2.Pub:run mediante la ejecucion del job runOW2.sh");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmOneButton(data.get("interface"), data.get("servicio"));

/*Paso 3 *********************************************************/

		addStep("Validar registro de la ejecución de la interfaz en las tablas de WMLOG.");
		
		
    	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		System.out.println(tdcQRY3);

		SQLResult Paso3 = dbLog.executeQuery(tdcQRY3);
		
		String RUN_ID = Paso3.getData(0,"RUN_ID");

		boolean Paso3Empty = Paso3.isEmpty();
		
		System.out.println(Paso3Empty);
		if (!Paso3Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

     	assertFalse(Paso3Empty, "No se tiene informacion en la base datos");

 
/*Paso 4 *********************************************************/

		addStep("Verificar registro de información en la tabla XXEX_SYNC_ART_PROV de PORTAL.");
		
		
    	System.out.println(GlobalVariables.DB_HOST_FCIASQA);
    	
		System.out.println(tdcQRY4);

		SQLResult Paso4 = dbFCIASQA.executeQuery(tdcQRY4);

		boolean Paso4Empty = Paso4.isEmpty();
		
		System.out.println(Paso4Empty);
		if (!Paso4Empty) {
			
			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

     	assertFalse(Paso4Empty, "No se tiene informacion en la base datos");

		
		
/*Paso 5 *********************************************************/

		addStep("Validar actualización del estatus a E de los registros procesados en ORAFIN.");
				
    	System.out.println(GlobalVariables.DB_HOST_AVEBQA);
    	
        String FormatotdcQRY5 = String.format(tdcQRY5,RUN_ID);
    	
		System.out.println(FormatotdcQRY5);
		
		SQLResult Paso5 = dbAVEBA.executeQuery(FormatotdcQRY5);

		boolean Paso5Empty = Paso5.isEmpty();
		
		System.out.println(Paso5Empty);
		if (!Paso5Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

     	assertFalse(Paso5Empty, "No se tiene informacion en la base datos");
     	
     	/*Paso 6  *********************************************************/

     	
     	addStep("Validar registro log de Ejecución");

     	System.out.println(GlobalVariables.DB_HOST_FCIASQA);
    	
        String FormatotdcQRY6 = String.format(tdcQRY6,RUN_ID);
    	
		System.out.println(FormatotdcQRY6);
		
		SQLResult Paso6 = dbFCIASQA.executeQuery(FormatotdcQRY6);

		boolean Paso6Empty = Paso6.isEmpty();
		
		System.out.println(Paso6Empty);
		if (!Paso6Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso6);
		}

     	assertFalse(Paso6Empty, "No se tiene informacion en la base datos");
     	
     	

}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Esta interface servive de funete entre los sistemas de Oracle Compras-Inventarios";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OW2_002_Verificar_ejecuccion_de_interfaz_Proveedor_AME900814LM3";
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
