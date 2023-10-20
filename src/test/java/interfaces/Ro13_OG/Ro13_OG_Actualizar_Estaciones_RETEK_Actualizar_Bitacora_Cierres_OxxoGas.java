package interfaces.Ro13_OG;

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

public class Ro13_OG_Actualizar_Estaciones_RETEK_Actualizar_Bitacora_Cierres_OxxoGas extends BaseExecution {
	
	/**
	 * desc: Validar que al actualizar el estatus de 'O' a 'C' en 
	 * XXOG_BITACORA_DE_CIERRE de OxxoGas, se inserte o actualice la 
	 * información de las estaciones en RETEK;
	 * @name Ultima modificacion Mariana Vives
	 * @date 27/02/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void Ro13_OG_Actualizar_Estaciones_RETEK_Actualizar_Bitacora_Cierres_OxxoGas_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbretek = new SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dboiebsbdq = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru,GlobalVariables.DB_USER_RMSWMUSERPeru, GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);


	/*
		 * Variables
		 *************************************************************************/
		
		
		String tdcPaso2 = " SELECT * FROM XXOG_BITACORA_DE_CIERRES \r\n"
				+ " WHERE estatus = 'O' \r\n"
				+ " AND oracle_plaza = '" +data.get("crplaza")+ "' \r\n"
				+ " AND periodo = '" +data.get("periodo")+ "' \r\n";
				

		String tdcPaso3 = " SELECT distinct a.RETEK_CR,TO_CHAR(GREATEST( NVL(c.maxFechaCierre,b.maxFechaInvFis) ,NVL(b.maxFechaInvFis,c.maxFechaCierre)),'DD/MM/YYYY') \r\n "
				+ "  minFechaTransaccion, DECODE(GREATEST( NVL(c.maxFechaCierre,b.maxFechaInvFis) ,NVL(b.maxFechaInvFis,c.maxFechaCierre)),c.maxFechaCierre, 'C','I')  \r\n"
				+ " TipoFechaTransaccion FROM xxfc_maestro_de_crs_v a, (SELECT retek_cr, MAX(fecha) maxFechaInvFis FROM XXOG.xxog_inventarios_fisicos \r\n"
				+ " WHERE ESTADO IS NULL GROUP BY retek_cr ) b, (SELECT oracle_plaza, LAST_DAY(MAX(TO_DATE(periodo,'MON-RRRR','NLS_DATE_LANGUAGE = SPANISH'))) \r\n"
				+ " maxFechaCierre FROM xxog_bitacora_de_cierres WHERE ESTATUS='C' GROUP BY oracle_plaza) c \r\n"
				+ " WHERE a.ORACLE_CR_SUPERIOR= '"+ data.get("crplaza") +"' \r\n"
				+ " AND a.ORACLE_CR_TYPE='T' \r\n"
				+ " AND a.ESTADO='A' \r\n"
				+ " AND a.RETEK_CR=b.RETEK_CR(+) \r\n"
				+ " AND a.oracle_cr_superior = c.oracle_plaza (+) \r\n"
				+ " ORDER BY retek_cr \r\n";
		
		String tdcPaso4 = "Select * from  XXOG.XXOG_BITACORA_DE_CIERRES  \r\n"
				+ " WHERE  estatus = 'C' \r\n"
				+ " AND estatus = 'O'  \r\n"
				+ " AND oracle_plaza = '" +data.get("crplaza")+ "' \r\n"
				+ " AND periodo = '"+ data.get("periodo")+ "' \r\n";
				
		String tdcPaso5 = "SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface = 'RO13_OGI' \r\n"
				+ " AND TRUNC (start_dt) = TRUNC (SYSDATE) \r\n"
				+ " AND status = 'S' \r\n"
				+ " ORDER BY 1 DESC \r\n";
				 
		
         String tdcPaso6 = " SELECT * FROM WM_STORE_MINTRANDATE_OG \r\n"
         		+ " WHERE store = %s \r\n"
         		+ " AND min_trandate like to_date( %s ,'DD/MM/YYYY') \r\n"
         		+ " AND min_trandate_type = %s \r\n"
         		+ " and trunc(last_update_date) = trunc(SYSDATE) \r\n";
         		
				
			// Paso Consulta

			/*
			 * Pasos
			 *****************************************************************************/

			/// Paso 1 ***************************************************

			addStep(" Comprobar que se encuentre habilitado el AdapterNotification - RO13_2.DB.ORAFIN:notUpdBitacoraCierreOG ");
					

			/// Paso 2 ***************************************************

			addStep(" Tener información previa del registro correspondiente al periodo MAR-2017 de la plaza 13GDB en la \r\n"
					+ " bitácora de cierres de OxxoGas con estatus 'O' \r\n");

			System.out.println(tdcPaso2);

			SQLResult paso2 = dbretek.executeQuery(tdcPaso2);

			boolean ValPaso2 = paso2.isEmpty();

			if (!ValPaso2) {

				testCase.addQueryEvidenceCurrentStep(paso2);

			}

			assertFalse(ValPaso2);

			/// Paso 3 ***************************************************

			addStep(" Obtener la fecha máxima y tipo de fecha de las transacciones ya realizadas de la \r\n"
					+ " bitácora de cierre de la plaza en ORAFIN \r\n");

            String retekcr = "";
            String minfechatransaccion = "";
            String tipofechatransaccion = "";
			
			System.out.println(tdcPaso3);

			SQLResult paso3 = dboiebsbdq.executeQuery(tdcPaso3);

			boolean ValPaso3 = paso3.isEmpty();

			if (!ValPaso3) {

				retekcr = paso3.getData(0, "retek_cr");
				minfechatransaccion = paso3.getData(0,"minfechatransaccion");
				tipofechatransaccion = paso3.getData(0, "tipofechatransaccion");
				
				testCase.addQueryEvidenceCurrentStep(paso3);

			}

			assertFalse(ValPaso3);

			/// Paso 4 ***************************************************

			addStep(" Actualizar el estatus de un registro en la bitácora de cierres de OxxoGas. Se lanza la ejecución del servicio "
					+ " RO13_2.pub:run_cierre y se procesa la información.\r\n" );
					
			System.out.println(tdcPaso4);

			SQLResult paso4 = dboiebsbdq.executeQuery(tdcPaso4);

			boolean ValPaso4 = paso4.isEmpty();

			if (!ValPaso4) {

				testCase.addQueryEvidenceCurrentStep(paso4);
			}

			assertFalse(ValPaso4);

			/// Paso 5 ***************************************************

			addStep(" Validar la ejecución de la interface en la tabla wm_log_run de WMLOG ");

			System.out.println(tdcPaso5);

			SQLResult paso5 = dbLog.executeQuery(tdcPaso5);

			boolean ValPaso5 = paso5.isEmpty();

			if (!ValPaso5) {

				testCase.addQueryEvidenceCurrentStep(paso5);

			}

			assertFalse(ValPaso5);

			/// Paso 6 ***************************************************

			addStep(" Validar que se insertó/actualizó la información de la trasacción de cierre en RETEK \r\n");
			
			String formatoPaso6 = String.format(tdcPaso6, retekcr,minfechatransaccion,tipofechatransaccion);
					
			System.out.println(formatoPaso6);

			SQLResult paso6 = dbretek.executeQuery(formatoPaso6);

			boolean ValPaso6 = paso6.isEmpty();
			
			if(!ValPaso6) {

			testCase.addQueryEvidenceCurrentStep(paso6);
			}
			assertFalse(ValPaso6);
		}

		@Override
		public void beforeTest() {
			// TODO Auto-generated method stub

		}

		@Override
		public String setTestDescription() {
			return "Validar que al actualizar el estatus de 'O' a 'C' en XXOG_BITACORA_DE_CIERRE de OxxoGas, "
					+ " se inserte o actualice la información de las estaciones en RETEK; plaza 13GDB, periodo MAR-2017 \r\n";
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return "Equipo de Automatizacion";
		}

		@Override
		public String setTestFullName() {
			return "Ro13_OG_Actualizar_Estaciones_RETEK_Actualizar_Bitacora_Cierres_OxxoGas_test";
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
