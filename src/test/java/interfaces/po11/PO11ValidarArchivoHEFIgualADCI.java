package interfaces.po11;

import java.util.HashMap;

import org.testng.annotations.Test;

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

public class PO11ValidarArchivoHEFIgualADCI extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_PO11_Validar_Archivos_HEF_Igual_DCI(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		
		String tdcQueryRecepcion = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE"
			+ "  FROM POSUSER.POS_INBOUND_DOCS "
			+ " WHERE RECEIVED_DATE >= sysdate-7 "
			+ " AND DOC_TYPE IN ('DCI','HEF') "
			+ " and status = 'I'";
		
		String tdcQueryRecepcionDCI = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE"
				+ "  FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE RECEIVED_DATE >= sysdate-7 "
				+ " AND DOC_TYPE IN ('DCI') "
				+ " and status = 'I'";
		
		String tdcQueryRecepcionHEF = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE"
				+ "  FROM POSUSER.POS_INBOUND_DOCS "
				+ " WHERE RECEIVED_DATE >= sysdate-7 "
				+ " AND DOC_TYPE IN ('HEF') "
				+ " and status = 'I'";
		
		String tdcQueryArchivoHEF = " SELECT PID_ID, PREFIJO, FORMA_PAGO_NUM, PV_DATE, NUM_CORTE, VALOR, PARTITION_DATE"
				+ " FROM POSUSER.POS_HEF_DETL "
				+ " WHERE PID_ID= '%s' "
				+ " AND PREFIJO = 'TCAM'";
		
		String tdcQueryArchivoDCI = "SELECT PID_ID, ID_CI_TDA, MOV_TYPE, MOV_ID_POS, PV_DATE, MONTO, CI_INFO "
				+ " FROM POSUSER.POS_DCI_DETL "
				+ " WHERE PID_ID= '%s' "
				+ " AND MOV_TYPE ='TCCI'";
		
		String tdcQueryValor = " SELECT  VALOR"
				+ " FROM POSUSER.POS_HEF_DETL "
				+ " WHERE PID_ID= '%s' "
				+ " AND PREFIJO = 'TCAM'"
				+ " AND VALOR = '%s'";
		
		String tdcQueryMonto = "SELECT MONTO "
				+ " FROM POSUSER.POS_DCI_DETL "
				+ " WHERE PID_ID= '%s' "
				+ " AND MOV_TYPE ='TCCI'"
				+ " AND MONTO = '%s'";

		
		
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("I16072  Cajas Inteligentes");
		//testCase.setTest_Description(data.get("Description"));	
		
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//Paso 1 *************************			
		addStep("Ejecutar la siguiente consulta para validar la recepcion de los archivos.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryRecepcion);
		
		SQLResult recepcionResult = executeQuery(dbPos, tdcQueryRecepcion);
		SQLResult DCIresult = executeQuery(dbPos, tdcQueryRecepcionDCI);
		SQLResult HEFresult = executeQuery(dbPos, tdcQueryRecepcionHEF);
		
		boolean recepcion = recepcionResult.isEmpty();
		
		String dci_id = "";
		String hef_id = "";
		
		if (!recepcion) {
			dci_id = DCIresult.getData(0, "ID");
			hef_id = HEFresult.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(recepcionResult);
		}	
		System.out.println(recepcion);
		
		assertFalse(recepcion, "No se obtiene información de la consulta");
		
//Paso 2 *************************			
		addStep("Ejecutamos el query para hacer la validacion del archivo HEF.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String archivoHEFformat = String.format(tdcQueryArchivoHEF, hef_id);
	
		System.out.println(archivoHEFformat);
		
		SQLResult archivoHEFresult = executeQuery(dbPos, archivoHEFformat);
		String valor = archivoHEFresult.getData(0, "VALOR");
		
		boolean validar = archivoHEFresult.isEmpty();
		
		if (!validar) {
			
			testCase.addQueryEvidenceCurrentStep(archivoHEFresult);
		
		}
		
		System.out.println(validar);
		
		assertFalse(validar, "No se obtiene información de la consulta");
		
//Paso 3 *************************			
		addStep("Ejecutamos el query para hacer la validacion del archivo DCI.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String archivoDCIformat = String.format(tdcQueryArchivoDCI, dci_id);
	
		System.out.println(archivoDCIformat);
		
		SQLResult archivoDCIresult = executeQuery(dbPos, archivoDCIformat);
		String monto = archivoDCIresult.getData(0, "MONTO");
		
		boolean validarDCI = archivoDCIresult.isEmpty();
		
		if (!validarDCI) {
			
			testCase.addQueryEvidenceCurrentStep(archivoDCIresult);
		
		}
		
		System.out.println(validarDCI);
		
		assertFalse(validarDCI, "No se obtiene información de la consulta");
		
//Paso 4 ************************
		addStep("Comparar ambos valores del paso 2 y paso 3.");
		
		System.out.println(valor);
		System.out.println(monto);
		
		String valorFormat = String.format(tdcQueryValor, hef_id,valor);
		String montoFormat = String.format(tdcQueryMonto, dci_id,monto);
		
		SQLResult valorResult = executeQuery(dbPos, valorFormat);
		SQLResult montoResult = executeQuery(dbPos, montoFormat);
		
		testCase.addQueryEvidenceCurrentStep(valorResult, false);
		testCase.addQueryEvidenceCurrentStep(montoResult, false);
		
		
		
		if (valor.equals(monto)) {
			
			testCase.addBoldTextEvidenceCurrentStep("El campo VALOR Y MONTO, tienen la misma cantidad.");
			
		}
		else {
			
			testCase.addBoldTextEvidenceCurrentStep("El campo VALOR Y MONTO, no tienen la misma cantidad.");
			
		}
				
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
		return "Terminado. Validar que el Tipo de cambio del archivo HEF (TCAM) sea igual al tipo de cambio del archivo DCI (TCCI1) cuando la recoleccion incluya dolares";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_010_PO11_Validar_Archivos_HEF_Igual_DCI";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
