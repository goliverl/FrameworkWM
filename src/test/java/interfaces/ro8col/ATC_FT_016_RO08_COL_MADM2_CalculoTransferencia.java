package interfaces.ro8col;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

import modelo.BaseExecution;

/**
 *  OCO21021 - Mejoras Administrativo: 
 * Desc:
 * @author Roberto Flores
 * @date   2022/09/21
 */
public class ATC_FT_016_RO08_COL_MADM2_CalculoTransferencia extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_016_RO08_COL_MADM2_CalculoTransferencia_test(HashMap<String, String> data) throws Exception {
	
		/*
		 * Info proyecto
		 *********************************************************************/
		testCase.setProject_Name("OCO21021 - Mejoras Administrativo"); 
		testCase.setPrerequisites(data.get("prerequisites"));
		testCase.setTest_Description(data.get("id") + " - " + data.get("nombre") + "\n" + data.get("desc"));
		
		/*
		 * Utilerías
		 *********************************************************************/
		MADM2Util proyectUtil = new MADM2Util(data, testCase, this);
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

		Date fechaEjecucionInicio = new Date();
		
		List<String> idsFemFifStgList = proyectUtil.ejecutarRo8ControlM(fechaEjecucionInicio);
		
//		List<String> idsPrueba = new ArrayList<>();
//		idsPrueba.add("62060889722");
//		idsPrueba.add("62060889723");
//		idsPrueba.add("62060889724");
//		
//		List<String> referencias3 = proyectUtil.validarEjecucionGlLines(idsPrueba);
		
		List<String> referencias3 = proyectUtil.validarEjecucionGlLines(idsFemFifStgList, true);
		
		proyectUtil.validarGlInterface(referencias3);
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_016_RO08_COL_MADM2_CalculoTransferencia_test";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-001 - Calculo del ICO8 a precio de venta transferencia de entrada BOGOTA";
		
	}

	@Override
	public String setTestDesigner() { 
		return "AutomationQA";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}
	
	
}