package interfaces.Ro13_OG;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Ro13_OG_Actualiza_Estaciones_RETEK_Insert_Bitacora_Cierres_OxxoGas  extends BaseExecution {
	/**
	 * desc:Validar que al realizar una nueva inserción con estatus 'C' en la tabla 
	 * XXOG_BITACORA_DE_CIERRE de OxxoGas, se inserte o actualice la información 
	 * de las estaciones en RETEK
	 * @author Ultima modificacion Mariana Vives
	 * @date 27/02/2023
	 */
	@Test(dataProvider = "data-provider")
	public void Ro13_OG_Actualiza_Estaciones_RETEK_Insert_Bitacora_Cierres_OxxoGas_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbretek = new SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dboiebsbdq = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru,GlobalVariables.DB_USER_RMSWMUSERPeru, GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);

	/*
		 * Variables
		 *************************************************************************/

		String tdcPaso2 = " SELECT a.retek_cr, TO_CHAR ( GREATEST (NVL (c.maxfechacierre, b.maxfechainvfis), NVL (b.maxfechainvfis, c.maxfechacierre)), 'DDMMYYYY') \r\n"
				+ " minfechatransaccion, DECODE ( GREATEST (NVL (c.maxfechacierre, b.maxfechainvfis), \r\n"
				+ " NVL (b.maxfechainvfis, c.maxfechacierre)), c.maxfechacierre, 'C', 'I') tipofechatransaccion \r\n"
				+ " FROM xxfc_maestro_de_crs_v a, (SELECT retek_cr, MAX (fecha) maxfechainvfis FROM XXOG.xxog_inventarios_fisicos WHERE estado IS NULL GROUP BY retek_cr) \r\n"
				+ " b, (  SELECT oracle_plaza, LAST_DAY ( MAX ( TO_DATE (periodo, 'MON-RRRR', 'NLS_DATE_LANGUAGE = SPANISH'))) maxfechacierre \r\n"
				+ " FROM xxog_bitacora_de_cierres WHERE estatus = 'C' GROUP BY oracle_plaza) c \r\n"
				+ " WHERE a.oracle_cr_superior = '" +data.get("crplaza")+ " ' \r\n"
				+ " AND a.oracle_cr_type = 'T' \r\n"
				+ " AND a.estado = 'A' \r\n"
				+ " AND a.retek_cr = b.retek_cr(+) \r\n"
				+ " AND a.oracle_cr_superior = c.oracle_plaza(+) \r\n"
				+ " ORDER BY oracle_cr \r\n";
				
		
		String tdcPaso3 = "Select * from XXOG_BITACORA_DE_CIERRES "
				+ " where ORACLE_CIA = 202 "
				+ " AND ORACLE_PLAZA = '" +data.get("crplaza")+ "'"
				+ " AND Periodo = '" + data.get("periodo")+"'"
				+ " AND CIERRE_HC_BY = 11247 ";
				
				
		String tdcPaso4 = " SELECT * FROM XXOG_BITACORA_DE_CIERRES \r\n"
				+ " WHERE oracle_plaza = '" + data.get("crplaza") +"' \r\n"
				+ " AND periodo = %s \r\n"
				+ " AND estatus = 'C' \r\n"
				+ " AND TRUNC(creation_date) = TRUNC(sysdate) \r\n" ;
				
		
		String tdcPaso5 = " SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface = 'RO13_OGC' \r\n"
				+ " AND TRUNC (start_dt) = TRUNC (SYSDATE) \r\n"
				+ " AND status = 'S' \r\n"
				+ " ORDER BY 1 DESC \r\n";
				
		
         String tdcPaso6 = " SELECT * FROM WM_STORE_MINTRANDATE_OG \r\n"
         		+ " WHERE STORE = %s\r\n"
         		+ " AND MIN_TRANDATE LIKE TO_DATE(%s,'DD/MM/YYYY') \r\n"
         		+ " AND MIN_TRANDATE_TYPE = %s \r\n"
         		+ " AND TRUNC(LAST_UPDATE_DATE) = TRUNC(SYSDATE) \r\n";
				
			// Paso Consulta

			/*
			 * Pasos
			 *****************************************************************************/

			/// Paso 1 ***************************************************

			addStep("Comprobar que se encuentre habilitado el AdapterNotification:- RO13_2.DB.ORAFIN:notUpdBitacoraCierreOG");

			/// Paso 2 ***************************************************

			addStep(" Obtener la fecha máxima y tipo de fecha de las transacciones ya realizadas de la bitácora de cierre de la plaza 13GDB en ORAFIN\r\n");
					
			
			String retekcr = "";
			
			String minfechatransaccion = "";
			
			String tipofechatransaccion = "";

			System.out.println(tdcPaso2);

			SQLResult paso2 = dboiebsbdq.executeQuery(tdcPaso2);

			boolean ValPaso2 = paso2.isEmpty();

			if (!ValPaso2) {

				retekcr = paso2.getData(0, "RETEK_CR");
				minfechatransaccion  = paso2.getData(0, "MINFECHATRANSACCION");
				tipofechatransaccion  = paso2.getData(0, "TIPOFECHATRANSACCION");
				testCase.addQueryEvidenceCurrentStep(paso2);

			}

			assertFalse(ValPaso2);

			/// Paso 3 ***************************************************

			addStep("Insertar un nuevo registro en la bitácora de cierres de OxxoGas."
					+ " Se lanza la ejecución del servicio RO13_2.pub:run_cierre y se procesa la información.\r\n");

			String periodo = "";
			
			System.out.println(tdcPaso3);

			SQLResult paso3 = dbretek.executeQuery(tdcPaso3);

			boolean ValPaso3 = paso3.isEmpty();

			if (!ValPaso3) {
				
                periodo = paso3.getData(0,"Periodo");
				testCase.addQueryEvidenceCurrentStep(paso3);

			}

			assertFalse(ValPaso3);

			/// Paso 4 ***************************************************

			addStep(" Validar que existe el nuevo registro en la bitácora de cierres de OxxoGas \r\n" );
					

			String FormatoPaso4 = String.format(tdcPaso4, periodo);
			
			System.out.println(FormatoPaso4);

			SQLResult paso4 = dbretek.executeQuery(FormatoPaso4);

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

			addStep(" Validar que se insertó/actualizó la información de la trasacción de cierre en RETEK\r\n");
			
			
			String FormatoPaso6 = String.format(tdcPaso6, retekcr,minfechatransaccion,tipofechatransaccion);
	
			System.out.println(FormatoPaso6);

			SQLResult paso6 = dbretek.executeQuery(FormatoPaso6);

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
			return "Validar que al realizar una nueva inserción con estatus'C' en la tabla XXOG_BITACORA_DE_CIERRE de OxxoGas, "
					+ " se inserte o actualice la información de las estaciones en RETEK; plaza 13GDB, periodo MAR-2017";				
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return "Equipo de Automatizacion";
		}

		@Override
		public String setTestFullName() {
			return "Ro13_OG_Actualiza_Estaciones_RETEK_Insert_Bitacora_Cierres_OxxoGas_test";
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
