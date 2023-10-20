package interfaces.ro25_cl;


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


public class Ro25_CL_ValidarQueSeActualizaElRegistroParaLaPlaza extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_ro25_CL_ValidarQueSeActualizaElRegistroParaLaPlaza(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,
				GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbEbschile = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		utils.sql.SQLUtil dbCntChile = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_CNTCHILE,GlobalVariables.DB_USER_CNTCHILE, GlobalVariables.DB_PASSWORD_CNTCHILE);
		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryPaso1 = " SELECT * FROM TIENDAS.T_TIENDAS \n" 
		        + " WHERE CR_SUPERIOR = '" + data.get("Plaza") + "' AND \n"
				+ " CR_TIENDA = '" + data.get("Tienda") + "' \n";

		String tdcQueryPaso2 = " SELECT A.FLEX_VALUE_ID, ORACLE_CR, ORACLE_CR_SUPERIOR, LANGUAGE, B.ORACLE_CR_TYPE \n"
				+ " FROM APPLSYS.FND_FLEX_VALUES_TL A, APPS.XXFC_MAESTRO_DE_CRS_V B \n"
				+ " WHERE A.FLEX_VALUE_ID = B.CR_FLEX_VALUE_ID \n" 
				+ " AND ORACLE_CR = '" + data.get("Tienda") + "' \n"
				+ " AND ORACLE_CR_SUPERIOR = '" + data.get("Plaza") + "' \n" 
				+ " AND a.LANGUAGE = 'ESA' AND \n" 
				+ " B.ORACLE_CR_TYPE = 'T' \n";

		String tdcQueryPaso4 = " SELECT EMAIL, A.GROUP_ID, GROUP_NAME, INTERFACE_NAME, NAME FROM WMLOG.WM_LOG_GROUP A, \n"
				+ " WMLOG.WM_LOG_USER_GROUP B, WMLOG.WM_LOG_USER C \n"
				+ " WHERE A.GROUP_ID = B.GROUP_ID \n" 
				+ " AND B.USER_ID = C.USER_ID \n" 
				+ " AND A.GROUP_NAME = 'ORAFIN' \n"
				+ " AND A.INTERFACE_NAME = 'RO25_CL' \n" 
				+ " AND C.PLAZA IS NULL \n";

		String tdcQueryPaso5 = " Select * from APPLSYS.FND_FLEX_VALUES_TL \n"
				+ " WHERE FLEX_VALUE_MEANING = '" + data.get("Tienda") + "' AND \n"
				+ " LANGUAGE = 'ESA' AND \n"
				+ " FLEX_VALUE_ID = %s \n";

		String tdcQueryPaso6 = " SELECT * FROM WMLOG.WM_LOG_RUN \n" 
		        + " WHERE INTERFACE = 'RO25_UPDCR_CL' \n"
				+ " AND STATUS = 'S' \n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \n" 
		        + " ORDER BY RUN_ID DESC \n";

		String tdcQueryPaso7 = "SELECT * FROM TIENDAS.T_TIENDAS WHERE \n"
		        + " CR_SUPERIOR = '" + data.get("Plaza") + "' AND \n"
				+ " CR_TIENDA = '" + data.get("Tienda") + "' AND \n"
		        + " NOMBRE = '%s' \n";

		 
		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("User");
		String ps = PasswordUtil.decryptPassword(data.get("Ps"));
		String server = data.get("server");
  
		/*
		//En el paso 5 de ALM, se meciona un update, el cual lo cambie por un Select.
		*/
		
	  	/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		addStep("Que Existan las Plazas y las tiendad en la DB");
		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		
		System.out.println(tdcQueryPaso1);

		SQLResult Paso1 = dbCntChile.executeQuery(tdcQueryPaso1);

		boolean Paso1Empty = Paso1.isEmpty();
		
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

     	assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/// Paso 2 ***************************************************

     	
		addStep("Valida que existe la plaza y la tienda en las tablas FND_FLEX_VALUES_TL y XXFC_MAESTRO_DE_CRS_V de la BD ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryPaso2);

		SQLResult paso2 = dbEbschile.executeQuery(tdcQueryPaso2);
		
		String FlexValuedid = paso2.getData(0,"FLEX_VALUE_ID");

		boolean Paso2Empty = paso2.isEmpty();
		
		System.out.println(Paso2Empty);
		
		if (!Paso2Empty) {
			
			testCase.addQueryEvidenceCurrentStep(paso2);
		}
		
		assertFalse(Paso2Empty, "No se tiene informacion en la base de datos");

		/// Paso 3 ***************************************************

		addStep("Comprobar que el adapter notification RO25.DB.ORAFIN:adpNotUpdFndFlexValuesTl este habilitado.");

		/// Paso 4 *****************************************************

		addStep(" Comprobar la dirección de email para recibir las notificaciones en las tablas WM_LOG_GROUP, "
				+ " WM_LOG_USER_GROUP y WM_LOG_USER para el GROUP_NAME igual a ORAFIN y INTERFACE_NAME igual a "
				+ " RO25 en la BD WMLOG. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQueryPaso4);
		
		SQLResult Paso4 = executeQuery(dbLog, tdcQueryPaso4);
		
		boolean Paso4Empty = Paso4.isEmpty();
		
		System.out.println(Paso4Empty);
        
		if (!Paso4Empty) {
			
			testCase.addQueryEvidenceCurrentStep(Paso4);
		}
		
		assertFalse(Paso4Empty, "No se tiene informacion en la base datos");
		
		
		// Paso 5 ******************************************************
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("Interface"), data.get("Servicio"));


		// Paso 5 ******************************************************

		addStep("Actualizar la columna DESCRIPTION en la tabla  FND_FLEX_VALUES_TL de la BD ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String Formato5 = String.format(tdcQueryPaso5,FlexValuedid );
		
		System.out.println(Formato5);
		
		SQLResult Paso5 = executeQuery(dbEbschile, Formato5);
		
		String Descripcion = Paso5.getData(0, "DESCRIPTION");
		
		Boolean Paso5Empty = Paso5.isEmpty();
		System.out.print(Paso5Empty);
		
         if (!Paso5Empty) {
			
			testCase.addQueryEvidenceCurrentStep(Paso5);
		}
	
		
		assertFalse(Paso5Empty, "No se tiene informacion en la base datos");

		/// Paso 6 ***************************************************

		addStep("Validar la correcta actualización de WM_STATUS_CODE = 'E', Tipo de Cambio para las Tiendas "
				+ "en la tabla WM_SYNC_POS_EXC_RATES");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.print(tdcQueryPaso6);
		System.out.print("\n");
		SQLResult Paso6 = executeQuery(dbLog, tdcQueryPaso6);
		
		boolean Paso6Empty = Paso6.isEmpty();
		
		System.out.print(Paso6Empty);
		
		testCase.addQueryEvidenceCurrentStep(Paso6);
		
		assertFalse(Paso6Empty, "No se tiene informacion en la base datos");

		/// Paso 7 ***************************************************

		addStep("Comprobar que la columna NOMBRE para el registro con el CR_SUPERIOR igual a 10CDJ y "
				+ "CR_TIENDA igual a 50WPC se actualizó correctamente en la tabla T_TIENDAS de la BD CNT.");

		String Formato7 = String.format(tdcQueryPaso7,Descripcion);
		
		System.out.println(Formato7);
		
		SQLResult Paso7 = executeQuery(dbCntChile, Formato7);

		boolean Paso7Empty = Paso7.isEmpty();
		
		System.out.println(Paso7Empty);
		
		if (!Paso7Empty) {
				
			   testCase.addQueryEvidenceCurrentStep(Paso7);
			}
		
		
		assertFalse(Paso7Empty, "No se tiene informacion en la base datos");

		/// Paso 8 ***************************************************

		addStep("Validar que se haya recibido el correo.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
	}

	@Override
	public String setTestDescription() {
		return "Terminada. El script es para validar que se actualiza la Plaza y Tienda.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatización";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_ro25_CL_ValidarQueSeActualizaElRegistroParaLaPlaza";
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
