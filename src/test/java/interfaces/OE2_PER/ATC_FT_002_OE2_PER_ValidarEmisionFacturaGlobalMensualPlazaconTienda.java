package interfaces.OE2_PER;


import java.util.HashMap;

import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_002_OE2_PER_ValidarEmisionFacturaGlobalMensualPlazaconTienda extends BaseExecution {
	
	/*
	 * 
	 * Modificado por mantenimiento.
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp Validar la emision de Factura Global Mensual para Plaza con Tienda (POS)
	 * @project name LOGV2
	 *
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_OE2_PER_ValidarEmisionFacturaGlobalMensualPlazaconTienda_test(HashMap<String, String> data) throws Exception {
		/** Utilerias *********************************************************************/
		
		//dbPuserPeru - dbLogPeru
		String OIWMQA_HOST = GlobalVariables.DB_HOST_Oiwmqa;
		String OIWMQA_USER = GlobalVariables.DB_USER_Oiwmqa;
		String OIWMQA_PASS = GlobalVariables.DB_PASSWORD_Oiwmqa;
		SQLUtil OIWMQA = new SQLUtil(OIWMQA_HOST, OIWMQA_USER, OIWMQA_PASS);
		
		//dbEbsPeru
		String OIEBSBDQ_HOST = GlobalVariables.DB_HOST_OIEBSBDQ;
		String OIEBSBDQ_USER = GlobalVariables.DB_USER_OIEBSBDQ;
		String OIEBSBDQ_PASS = GlobalVariables.DB_PASSWORD_OIEBSBDQ;
		SQLUtil OIEBSDQ = new SQLUtil(OIEBSBDQ_HOST, OIEBSBDQ_USER, OIEBSBDQ_PASS);
		
		Util o = new Util(testCase);
		
      /*  ***********************************************************************/
		
		//Data Provider
		String name = data.get("Name");
		String server = data.get("server");
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String interfaz = data.get("interfase");
		String service = data.get("servicio");
		String run_interface = data.get("Run_interface");
		String plaza = data.get("Plaza");
		String tienda = data.get("Cr_tienda");
		String anio = data.get("Anio");
		String mes = data.get("Mes");
		
		//General Variables
		String run_id = "";
		String id_factura_digital = "";
		
		//Precondicion
		String queryPre = "SELECT OPERACION, VALOR1, VALOR2 \r\n"
				+ "FROM WMUSER.WM_INTERFASE_CONFIG \r\n"
				+ "WHERE interfase = 'OE2_PER' \r\n"
				+ "AND ROWNUM <= 10";
		SQLResult resultPre;
		String errorPre = "No se encontro la informacion de configuracion";
		
		//Paso 1 
		String queryPaso1 = "SELECT ID_FACTURA_DIGITAL, ORIGEN, ANIO, MES, CR_PLAZA, CR_TIENDA, WM_STATUS, VERSION_CFDI \r\n"
				+ "FROM XXPE.XXPE_CFD_FACTURA_DIGITAL \r\n"
				+ "WHERE WM_STATUS = 'L' AND ORIGEN = 'IMXAR' \r\n"
				+ "AND DOCTO_XML IS NULL AND CR_PLAZA = '"+plaza+"' \r\n"
				+ "AND CR_TIENDA = '"+tienda+"' AND ANIO = '"+anio+"' \r\n"
				+ "AND MES = '"+mes+"' AND ROWNUM <= 10";
		
		String errorPaso1 = "No se encontro la informacion en la tabla";
		SQLResult resultPaso1;
				
		//Paso 3
		String queryPaso3 = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'OE2_PER'  \r\n"
				+ "AND  START_DT >= TRUNC(SYSDATE)\r\n"
				+ "AND ROWNUM <= 10\r\n"
				+ "ORDER BY START_DT DESC";
		String errorPaso3 = "No se encontro ningun registro para la ejecucion de la interfaz";
		SQLResult resultPaso3;
		
		//Paso 4
		String queryPaso4;
		String errorPaso4;
		SQLResult resultPaso4;
		
		//Paso 5
		String queryPaso5;
		String errorPaso5;
		SQLResult resultPaso5;
				
		
		
// Pre ******************************************************************************************************************/

		step("Validar que se tenga la informacion en la tabla de configuracion \r\n");
		o.logHost(OIWMQA_HOST);
		resultPre = ejecutaQuery(queryPre, OIWMQA);
		o.validaRespuestaQuery(resultPre);
		if (resultPre.isEmpty()) {
			o.muestraError(queryPre, errorPre);
		}
		
// Paso1 ******************************************************************************************************************/

		step("Validar registros con estatus: L en la tabla XXPE.XXPE_CFD_FACTURA_DIGITAL \r\n");
		o.logHost(OIEBSBDQ_HOST);
		resultPaso1 = ejecutaQuery(queryPaso1, OIEBSDQ);
		o.validaRespuestaQuery(resultPaso1);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1, errorPaso1);
		}else {
			id_factura_digital = resultPaso1.getData(0, "ID_FACTURA_DIGITAL");
			o.log("Recupera id_factura_digital: "+id_factura_digital+" del primer registro");
		}

//Paso 2 *******************************************************************************************************

		step("Ejecutar la interfaz OE2_PER ");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String url = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(url);
		u.waitForLoadPage();
		pok.runIntefaceWmTwoButtonsWihtoutInputs10_2(interfaz, service);

//Paso 3 *******************************************************************************************************

		step("Validar ejecucion de la interfaz \r\n ");
		resultPaso3 = ejecutaQuery(queryPaso3, OIWMQA);
		o.validaRespuestaQuery(resultPaso3);
		if (!resultPaso3.isEmpty()) {
			run_id = resultPaso3.getData(0, "RUN_ID");
			o.log("Recupera run_id de la ejecucion mas reciente: "+run_id);
		}else {
			o.muestraError(queryPaso3,errorPaso3);
		}

//Paso 4  *******************************************************************************************************

		step("Validar ejecucion de los threads\r\n ");
		queryPaso4 = "SELECT THREAD_ID, NAME, START_DT, END_DT, STATUS FROM WMLOG.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID ='"+run_id+"' \r\n";
		errorPaso4 = "No se encontro ningun registro con parent_id: "+run_id;
		resultPaso4 = ejecutaQuery(queryPaso4, OIWMQA);
		o.validaRespuestaQuery(resultPaso4);
		if (resultPaso4.isEmpty()) {
			o.muestraError(queryPaso4,errorPaso4);
		}


//Paso 5 *******************************************************************************************************

		step("Validar cambio de estatus en la tabla XXPE.XXPE_CFD_FACTURA_DIGITAL \r\n ");
			queryPaso5 = "SELECT ID_FACTURA_DIGITAL, ORIGEN, ANIO, MES, CR_PLAZA, CR_TIENDA,WM_STATUS, VERSION_CFDI \r\n"
					+ "FROM XXPE.XXPE_CFD_FACTURA_DIGITAL \r\n"
					+ "WHERE WM_STATUS = 'P'\r\n"
					+ "AND ID_FACTURA_DIGITAL = '"+id_factura_digital+"' \r\n";
			errorPaso5 = "No se encontro ningun registro con id_factura_digital: "+id_factura_digital+" y wm_status: P";
			resultPaso5 = ejecutaQuery(queryPaso5, OIEBSDQ);
			o.validaRespuestaQuery(resultPaso5);
			if (resultPaso5.isEmpty()) {
				o.muestraError(queryPaso5,errorPaso5);
			}else {
				o.log("El registro con id_factura_digital: "+id_factura_digital+" tiene el estatus esperado: P");
			}
			

	}

	int contador = 0;
	private void step(String step) {
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	private void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}
	
	private SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
		SQLResult queryResult;
		printQuery(query);
		queryResult = executeQuery(obj, query);
		return queryResult;
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
		return "Validar la emision de Factura Global Mensual para Plaza con Tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
