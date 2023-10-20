package interfaces.OE2_PER;


import static org.junit.Assert.assertFalse;
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

public class ATC_FT_001_OE2_PER_ValidaEmisionFacturaGlobalMensualImpuestoEstatal extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OE2_PER_ValidaEmisionFacturaGlobalMensualImpuestoEstatal_test(HashMap<String, String> data) throws Exception {
		/*
	
		 * 
		 * Utilerias
		 *********************************************************************/
		
		SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ); 
		SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		
      /*  ***********************************************************************/
		
		
	   //Precondicion
		
		String Pre = "SELECT operacion, valor1, valor2 "
				+ "FROM WMUSER.wm_interfase_config "
				+ "WHERE interfase = 'OE2_PER'";
		
		//Paso 1 
		String tdcPaso1 = "SELECT ID_FACTURA_DIGITAL, ORIGEN, ANIO, MES, CR_PLAZA, CR_TIENDA,WM_STATUS, VERSION_CFDI, "
				+ " WM_RUN_ID, DOCTO_XML "
				+ " FROM XXPE.XXPE_CFD_FACTURA_DIGITAL "
				+ " WHERE ORIGEN = 'IMXAR' "
				+ " AND CR_PLAZA = '"+ data.get("Cr_Plaza") +"' "
				+ " AND CR_TIENDA = '"+ data.get("Cr_tienda") +"' "
				+ " AND ANIO = '"+data.get("Anio")+"' "
				+ " AND MES = '"+data.get("Mes")+ "' "
				+ " AND WM_STATUS = '"+data.get("WM_STATUS")+ "' ";
				
		
		//Paso 3
		String tdcPaso3 = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server" 
				+ " FROM WMLOG.WM_LOG_RUN Tbl "
				+ " WHERE interface = 'OE2_PER'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion
		
		//Paso 3
		String tdcPaso3_1 = " Select * from WMLOG.WM_LOG_THREAD"
				+ " WHERE THREAD_ID = '%s' ";
		
		//Paso 5
		String tdcPaso5 = " SELECT * FROM XXFC_CFD_FACTURA_DIGITAL "
				+ " WHERE ORIGEN = 'AR' "
				+ " AND CR_PLAZA =  '" +data.get("Plaza")+ "'"
				+ " AND CR_TIENDA = '"+ data.get("Cr_tienda") +"'"
				+ " AND ANIO = '"+data.get("Anio")+"'"
				+ " AND MES = '"+data.get("Mes")+ "'"
				+ " AND WM_STATUS = 'P'"
				+ " AND WM_RUN_ID = %s ";
				
		
		
// Pre ******************************************************************************************************************/

				addStep(" Validar que se tenga la información en la tabla de configuración \r\n");

				System.out.println(Pre);

				SQLResult Pasopre = dbPuserPeru.executeQuery(Pre);

				boolean ValidaPre = Pasopre.isEmpty();

				if (!ValidaPre) {

					testCase.addQueryEvidenceCurrentStep(Pasopre);
				}

				assertFalse("No se encontro la información de configuración ", ValidaPre);

		
// Paso1 ******************************************************************************************************************/

		addStep(" Validar la tabla XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n");

		System.out.println(tdcPaso1);

		SQLResult Paso1 = dbEbsPeru.executeQuery(tdcPaso1);

		boolean ValidaPaso1 = Paso1.isEmpty();
		System.out.println(ValidaPaso1);
		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse("No se encontro la informacion en la tabla ", ValidaPaso1);

//Paso 2 *******************************************************************************************************

		addStep("Ejecutar la interface OE2_PER ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println(contra);

//Paso 3 *******************************************************************************************************

		addStep(" Validar el Log\r\n ");

		System.out.println(tdcPaso3);

		SQLResult Paso3 = dbLogPeru.executeQuery(tdcPaso3);

		String runid = "";

		boolean ValidaPaso3 = Paso3.isEmpty();
		System.out.println(ValidaPaso3);
		
		if (!ValidaPaso3) {

			runid = Paso3.getData(0, "Run_id");
			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse("No se encontro la información de configuración ", ValidaPaso3);

//Paso 3_1  *******************************************************************************************************

		addStep(" Validar el Log\r\n ");

		String FormatoPaso3_1 = String.format(tdcPaso3_1, runid);

		System.out.println(FormatoPaso3_1);

		SQLResult Paso3_1 = dbLogPeru.executeQuery(FormatoPaso3_1);
		System.out.println(Paso3_1);
		
		String threadid = "927170";
		
		boolean ValidaPaso3_1 = Paso3_1.isEmpty();

		if (!ValidaPaso3_1) {

			//threadid = Paso3_1.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(Paso3_1);
		}

		assertFalse("No se encontro la información de configuración ", ValidaPaso3_1);

//Paso 4 *******************************************************************************************************   

		addStep(" Validar el tag ImpLocRetenido contenga el valor Impuesto Estatal en el XML almacenado en el campo "
				+ " DOCTO_XML de la tabla XXFC.XXFC_CFD_FACTURA_DIGITAL\r\n");
		
		
		

//Paso 5 *******************************************************************************************************

		addStep(" Validar la tabla XXFC.XXFC_CFD_FACTURA_DIGITAL \r\n ");

		String FormatoPaso5 = String.format(tdcPaso5, threadid);

		System.out.println(FormatoPaso5);

		SQLResult Paso5 = dbPuserPeru.executeQuery(FormatoPaso5);

		boolean ValidaPaso5 = Paso5.isEmpty();

		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

		assertFalse("No se encontro la información de configuración ", ValidaPaso5);

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
		return "Construido. ";
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
