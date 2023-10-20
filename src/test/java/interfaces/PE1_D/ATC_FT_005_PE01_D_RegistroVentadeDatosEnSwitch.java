package interfaces.PE1_D;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.SSHConnector;

/**
 * MTC-FT-016-PE01_D SWITCH_PE01_D Registro de transacci�n de venta de datos en el switch<br>
 * Desc: En este escenario, se validara que se realice a tiempo el registro de una una venta de datos en el switch
 * 
 * @author Oliver Martinez
 * @date 04/21/2023
 */

public class ATC_FT_005_PE01_D_RegistroVentadeDatosEnSwitch extends BaseExecution{
	@Test(dataProvider = "data-provider" )
	public void ATC_FT_005_PE01_D_RegistroVentadeDatosEnSwitch_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria******************************************************/
//		SQLUtil dbFCTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA_MTY, GlobalVariables.DB_USER_FCTAEQA_MTY,GlobalVariables.DB_PASSWORD_FCTAEQA_MTY);
//		SQLUtil dbFCTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA_QRO, GlobalVariables.DB_USER_FCTAEQA_QRO,GlobalVariables.DB_PASSWORD_FCTAEQA_QRO);
//		SQLUtil dbFCSWQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_MTY, GlobalVariables.DB_USER_FCSWQA_MTY,GlobalVariables.DB_PASSWORD_FCSWQA_MTY);
//		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO,GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
//		SQLUtil dbOXWMLOGQA = new SQLUtil(GlobalVariables.DB_HOST_OXWMLOGQA, GlobalVariables.DB_USER_OXWMLOGQA,GlobalVariables.DB_PASSWORD_OXWMLOGQA);
//		
		/*
		 * Variables*****************************************************/
		testCase.setTest_Description(data.get("Descripcion"));
//		testCase.setFullTestName(data.get("Name"));
		testCase.setPrerequisites(data.get("Pre- Requisitos"));
		/******************************Paso 1****************************/
		addStep("Transaccion exitosa de venta de plan de datos");
		ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa transaccionExitosa =  new ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa();
		transaccionExitosa.setTestCase(this.testCase);
		transaccionExitosa.ATC_FT_002_PE01_D_TransaccionExitosaNoExitosa_test(data);
		String folio = transaccionExitosa.getFolio();
		System.out.println(folio);
		
		/******************************Paso 2****************************/
		addStep("Establecer la conexi�n con el servidor FTP para acceder al repositorio del LOG DEL SWITCH con los siguientes datos: ");
		
//		String folio = "5597514862";
//		Thread.sleep(50000);
		Integer ftpPort = Integer.parseInt(data.get("ftpPort"));
		String ftpHost = data.get("ftpHost");
		String ftpUser = data.get("ftpUser");
		String ftpPass = data.get("ftpPass");
		String ftpPath = data.get("ftpPath") + data.get("fileName");
		SSHConnector ftpConnector = new SSHConnector();
		ftpConnector.connect(ftpUser, ftpPass, ftpHost, ftpPort);
		boolean invoiceExists = false;
		if(ftpConnector.isConected()) {
			testCase.addBoldTextEvidenceCurrentStep("Se realiza conexion exitosa con el servidor FTP");
			//Busca el folio el en archivo switch_oxxo.log
			String ss = ftpConnector.executeCommand("grep " + folio + " " + ftpPath);
			System.out.println("Folio encontrado: " + ss);
			invoiceExists = ss.isEmpty();
			//Valida si existe el folio
			if(!invoiceExists) {
				System.out.println("Se encontro el folio: "+ folio + " en el Log Switch");
				testCase.addTextEvidenceCurrentStep("Se encontro el folio: "+ folio + " en el Log Switch");
			}else {
				System.out.println("No se encontro el folio: " + folio + " el Log Switch");
				testCase.addTextEvidenceCurrentStep("Se encontro el folio: "+ folio + " en el Log Switch");
			}
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Conexion Fallida con el servidor FTP");
		}
		ftpConnector.disconnect();
		
		assertFalse(invoiceExists, "No se encontro folio de transaccion en el Log del Switch.");
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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Celia Rubi Delgado";
	}
	
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PE01_D_RegistroVentadeDatosEnSwitch_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
