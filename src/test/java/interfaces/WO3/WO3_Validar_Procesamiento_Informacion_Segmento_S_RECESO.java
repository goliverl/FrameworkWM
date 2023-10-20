package interfaces.WO3;

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

public class WO3_Validar_Procesamiento_Informacion_Segmento_S_RECESO extends BaseExecution  {
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_WO3_009_Valida_Procesamiento_Info_S_Receso(HashMap<String, String> data) throws Exception {
				
/* Utilerías *********************************************************************/

		SQLUtil dbAVEBA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbFCIASQA = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
/* Variables *********************************************************************/
					

	String tdcQRY1 = " SELECT * FROM XXBN.XXBN_IC_CONVENIO_SEGMENTOS"
			+ " WHERE SEGMENTO='S_RECESO'"
			+ " AND WM_STATUS='L'";
	
	String tdcQRY3 = " SELECT * FROM WMLOG.WM_LOG_RUN"
			+ " WHERE INTERFACE = 'WO3'"
			+ " AND STATUS='S'"
			+ " ORDER BY START_DT DESC";
	
	String tdcQRY4 = " SELECT * FROM XXFC.XXFC_IC_CONVENIO_SEGMENTOS"
			+ " WHERE SEGMENTO='S_RECESO' "
			+ " AND WM_RUN_ID= %s";

	String tdcQRY5 = " SELECT * FROM XXBN.XXBN_IC_CONVENIO_SEGMENTOS"
			+ " WHERE SEGMENTO='S_RECESO'"
			+ " AND WM_RUN_ID= %s"
			+ " AND WM_STATUS='E'";
	
	SeleniumUtil u;
	PakageManagment pok;
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword(data.get("ps"));
	String server = data.get("Server");
			
	
			
			
/* Paso 1 *****************************************************************************************/
		
		addStep("Validar que exista información a procesar en la tabla XXBN.XXBN_IC_CONVENIO_SEGMENTOS de la "
				+ "Base de Datos PORTAL para el SEGMENTO APERTURAS");
			
    	System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCIASQA.executeQuery(tdcQRY1);

		boolean Paso1Empty = Paso1.isEmpty();
		
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

     	assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		

/*Paso 2 *********************************************************/

		addStep("Ejecutar la interface por medio del flowService WO3.Pub:run");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmOneButton(data.get("interface"), data.get("servicio"));

/*Paso 3 *********************************************************/

		addStep("Validar el registro de ejecución de la interface WO3 en la tabla "
				+ "WM_LOG_RUN de WMLOG el estatus de esta deberá ser  S");
		
		
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

		addStep("Validar que la información obtenida de la tabla XXBN.XXBN_IC_CONVENIO_SEGMENTOS del SEGMENTO APERTURAS "
				+ "haya sido registrada en la tabla XXFC_IC_CONVENIO_SEGMENTOS de la BD de ORAFIN");
		
		
    	System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
    	String FormatotdcQRY4 = String.format(tdcQRY4,RUN_ID );
    	
		System.out.println(FormatotdcQRY4);

		SQLResult Paso4 = dbAVEBA.executeQuery(FormatotdcQRY4);

		boolean Paso4Empty = Paso4.isEmpty();
		
		System.out.println(Paso4Empty);
		if (!Paso4Empty) {
			
			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

     	assertFalse(Paso4Empty, "No se tiene informacion en la base datos");

		
		
/*Paso 5 *********************************************************/

		addStep("Validar que el estatus de la información procesada haya sido actualizado a estatus E");
				
    	System.out.println(GlobalVariables.DB_HOST_FCIASQA);
    	
        String FormatotdcQRY5 = String.format(tdcQRY5,RUN_ID);
    	
		System.out.println(FormatotdcQRY5);
		
		SQLResult Paso5 = dbFCIASQA.executeQuery(FormatotdcQRY5);

		boolean Paso5Empty = Paso5.isEmpty();
		
		System.out.println(Paso5Empty);
		if (!Paso5Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

     	assertFalse(Paso5Empty, "No se tiene informacion en la base datos");

}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar el procesamiento de la informacion del segmento S_RECESO";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WO3_009_Valida_Procesamiento_Info_S_Receso";
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
