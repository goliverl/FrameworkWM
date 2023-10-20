package interfaces.OE14_MX;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;
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

public class OE14_PruebaVolumen extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		/*
	
		 * 
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);
		
		testCase.setProject_Name("I19040 Centralización Integral Cuentas por Pagar");
		
		//Paso 2
		String validaStatusL  = data.get("queryStatusL");
		
		//Paso 3
		
	 String registrosStatusL = data.get("query200");
		
		
		//Paso 4 , 5 y 6
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " 
				+ " WHERE interface like 'OE14MX%'  "
				+ "and  start_dt >= TRUNC(SYSDATE)"
				+ "and status = 'S'"
				+ "ORDER BY START_DT DESC) where rownum <=3";// Consulta para estatus de la ejecucion
		
		//Paso 7
		String error1 = "select error_id, run_id, error_date, severity, error_type from wmlog.WM_LOG_ERROR \r\n" + 
				"where run_id = '%s'"
				+ "and rownum <=3";
		
		String error2 = "select description from wmlog.WM_LOG_ERROR \r\n" + 
				"where run_id = '%s'"
				+ "and rownum <=3";
	
		//Paso 8
		
		String ValidaStatusE = data.get("querystatusE");
		
		
		/*
		 * Paso 1******************************************************************************************************************/
		
		addStep("Ingresar a la BD AVEBQA");
		
		System.out.println();
		
		testCase.addTextEvidenceCurrentStep("Base de Datos: AVEBQA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_AVEBQA);
		
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);
		

		/*
		 * Paso 2
		 ***************************************************************************************************************************/
		addStep("Entrar a la tabla : " + data.get("tabla") );
			
		
		System.out.println(validaStatusL);
		
		
		SQLResult validaStatusL_r = dbEbs.executeQuery(validaStatusL);
		
        String identificador0 = validaStatusL_r.getData(0, data.get("identificador")); //lugar 0
		
		System.out.println(" 0 " + identificador0);
		
        String identificador1 = validaStatusL_r.getData(1, data.get("identificador"));//lugar 1
		
		System.out.println(" 1 " + identificador1);
		
        String identificador2 = validaStatusL_r.getData(2, data.get("identificador"));//lugar 2
		
		System.out.println(" 2 " + identificador2);
		
		String identificador3 = validaStatusL_r.getData(3, data.get("identificador"));//lugar 3
			
		System.out.println(" 3 " + identificador3);
		
		String identificador4 = validaStatusL_r.getData(4, data.get("identificador"));//lugar 4
		
		System.out.println(" 4 " + identificador4);
					
		boolean validavalidaStatusL = validaStatusL_r.isEmpty();
		
		if (!validavalidaStatusL) {
			
			testCase.addQueryEvidenceCurrentStep(validaStatusL_r);
		}
		
		System.out.println(validavalidaStatusL);

		assertFalse("No se muestra contenido de la tabla", validavalidaStatusL);

		/*
		 * paso 3 
		 **************************************************************************************************************************/
		addStep("Ejecutar el siguiente query para validar que se tengan los 2000 registros información de (información de  sociedades, (\r\n" + 
				"registros de órdenes de compra\r\n" + 
				"registros de Liberaciones Abiertas\r\n" + 
				"registros de las recepciones de órdenes de compra al almacén y las devoluciones al proveedor\r\n" + 
				"Información de Rentas de Property al Portal Xpertal) ");
		
		System.out.println( registrosStatusL );
		
		SQLResult registrosStatusL_r = dbEbs.executeQuery(registrosStatusL);

		boolean validaregistrosStatusL = registrosStatusL_r.isEmpty();
		
		if (!validaregistrosStatusL) {
			
			testCase.addQueryEvidenceCurrentStep(registrosStatusL_r);
		}

		System.out.println(validaregistrosStatusL);	
		
		assertFalse("No se muestra información en los campos seleccionados por el query", validaregistrosStatusL);

		/*
		 * Paso 4  y 5****************************************************************************************************************/
		addStep("Ejecutar  la interface " + data.get("interfase"));
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);
			

		}

//Paso 6    ************************		
		addStep("Validar en la tabla WMLOG.WM_LOG_RUN  status='S' ");
		
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
        boolean validateStatus = fcwS.equals(status);
        
        if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(is1);
		}
	
		System.out.println("STATUS = S: "+ validateStatus);
		assertTrue(validateStatus, "No se muestra el status='S'");
		
//  Paso 7  *********************************
		
		addStep("Validar en la tabla  WMLOG.WM_LOG_ERROR ");
		
	//Primera parte
		
		String error1_F = String.format(error1,  run_id);
		
		System.out.println(error1_F);
		
		SQLResult error1_r = dbLog.executeQuery(error1_F);
				
		boolean validaerror1 = error1_r.isEmpty();
		
		if (!validaerror1) {
			
			testCase.addQueryEvidenceCurrentStep(error1_r);
		} else {
			
			testCase.addBoldTextEvidenceCurrentStep("No se presentaron errores relacionados al performance de la interface en relación con la carga");
		}
		
		System.out.println(validaerror1);
	
	//Segunda parte
		
        String error2_F = String.format(error2,  run_id);
		
		System.out.println(error2_F);
		
		SQLResult error2_r = dbEbs.executeQuery(error2_F);
				
		boolean validaerror2 = error2_r.isEmpty();
		
		if (!validaerror2) {
			
			testCase.addQueryEvidenceCurrentStep(error2_r);
		} else {
			
			testCase.addBoldTextEvidenceCurrentStep("No se presentaron errores relacionados al performance de la interface en relación con la carga");
		}
		
		System.out.println(validaerror2);
		
		assertTrue("Se muestran errores relacionados al performance de la interface en relación con la carga ", validaerror2);
		
//Paso 8 **************************************************************************
		
		addStep("Validar que la tabla " + data.get("tabla") + " wm_status='E'");
		
		String ValidaStatusE_F = String.format(ValidaStatusE,identificador0, identificador0, identificador0, identificador0, identificador0);
		
		System.out.println( ValidaStatusE_F);
		
		SQLResult ValidaStatusE_r = dbEbs.executeQuery(ValidaStatusE_F);

		boolean ValidaStatusE_boolean = ValidaStatusE_r.isEmpty();
		
		if (!ValidaStatusE_boolean) {
			
			testCase.addQueryEvidenceCurrentStep(registrosStatusL_r);
		}

		System.out.println(ValidaStatusE_boolean);	
		
		assertFalse("No se muestra  el status='E'", ValidaStatusE_boolean);


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
		return "Validar prueba de volumen para la interfaz catalagos_GL con 2000 registros";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "José Luis Flores Castellanos";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "MTC-FT-063 Validar prueba de volumen ";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}


