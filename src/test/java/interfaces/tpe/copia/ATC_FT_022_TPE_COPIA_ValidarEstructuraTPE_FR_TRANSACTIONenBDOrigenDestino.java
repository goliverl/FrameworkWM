package interfaces.tpe.copia;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONArray;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import java.util.Date;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_022_TPE_COPIA_ValidarEstructuraTPE_FR_TRANSACTIONenBDOrigenDestino extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_022_TPE_COPIA_ValidarEstructuraTPE_FR_TRANSACTIONenBDOrigenDestino_test(HashMap<String, String> data) throws Exception {

		
		/**
		 * Proyecto: Regresion Enero 2023
		 * Caso de prueba: MTC-FT-022 TPE_COPIA Validar la estructura de la tabla TPE_FR_TRANSACTION en la BD Origen y BD Destino
		 * @author edwin.ramirez
		 * @date 2023/Ene/9
		 */
/*
* Utilerías
*********************************************************************/

		SQLUtil dbOXTPEQA = new SQLUtil(GlobalVariables.DB_HOST_OXTPEQA,
				GlobalVariables.DB_USER_OXTPEQA, GlobalVariables.DB_PASSWORD_OXTPEQA);
		SQLUtil dbFCWMREQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMREQA,
				GlobalVariables.DB_USER_FCWMREQA, GlobalVariables.DB_PASSWORD_FCWMREQA);
/**
* Variables
* ******************************************************************************************
* 
*/
//Paso 2
		String consultaCampos = "DESC TPEUSER.TPE_FR_TRANSACTION";
//Paso 4
		String consultaCampos2 = "DESC TPEREP.TPE_FR_TRANSACTION_LOY";

/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//***************************************** Paso 1 y 2***************************************************************** 
		
		addStep("Ejecutar la siguiente consulta en la BD 'OXTPEQA' para ver los campos de la tabla TPE_FR_TRANSACTION:");
		System.out.println("Paso 1 y 2: "+GlobalVariables.DB_HOST_OXTPEQA);
		System.out.println(consultaCampos);
		
		SQLResult exe_consultaCampos = executeQuery(dbOXTPEQA, consultaCampos);
		
		boolean validaConsultaCampos = exe_consultaCampos.isEmpty();
		
		if (!validaConsultaCampos) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaCampos);
		} 
		
		System.out.println(validaConsultaCampos);
		assertFalse(validaConsultaCampos, "No existe información pendiente de procesar en la tabla.");
		
		
//***************************************** Paso 3 y 4 ***************************************************************** 
		addStep("Ejecutar la siguiente consulta en la BD 'FCWMREQA' para ver los campos de la tabla TPE_FR_TRANSACTION_LOY:");
		System.out.println("Paso 3 y 4: "+GlobalVariables.DB_HOST_FCWMREQA);
		System.out.println(consultaCampos2);
		
		SQLResult exe_consultaCampos2 = executeQuery(dbFCWMREQA, consultaCampos2);
		
		boolean validaConsultaCampos2 = exe_consultaCampos2.isEmpty();
		
		if (!validaConsultaCampos2) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaCampos2);
		} 
		
		System.out.println(validaConsultaCampos2);
		assertFalse(validaConsultaCampos2, "No existe información pendiente de procesar en la tabla.");
	
//***************************************** Paso 5 ***************************************************************** 
		addStep("Comparar las estructuras de las tablas obtenidas en el paso 2 y 4 para validar que sean iguales");
		System.out.println("Paso 5");
		//assertEquals(); FALTA ESTE PASO
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresión para comprobar la no afectación en la estructura de las tablas "
				+ "TPE_FR_TRANSACTION y TPE_FR_TRANSACTION_LOY en la BD Origen y BD Destino "
				+ "respectivamente del proceso de TPE_COPIA al ser migrado de webmethods v10.5 "
				+ "a webmethods 10.11 y del sistema operativo Solaris a Linux redhat 8.5 (X86). ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_022_TPE_COPIA_ValidarEstructuraTPE_FR_TRANSACTIONenBDOrigenDestino_test";
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
