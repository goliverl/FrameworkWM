///////////////Funciona////////////// 

package interfaces.WS;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicaccion_distinta_5S50_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_006_Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicacion_Distinta_5S50_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT APPLI, CAMPO, CLAVE, MAX(VALUE) FROM\r\n"
				+ " (SELECT 'POS' AS APPLI, 'PV_FOLIO'AS CAMPO, '35'AS CLAVE, NVL(MAX(TO_NUMBER(C.EXT_REF_NO)),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_INBOUND_DOCS B, posuser.POS_REC C\r\n"
				+ " WHERE A.ID = B.PE_ID AND A.PV_CR_PLAZA  = '" + data.get("plaza") +  "' AND A.PV_CR_TIENDA = '" + data.get("tienda") +  "'  AND B.ID = C.PID_ID AND \r\n"
				+ " B.DOC_TYPE = 'REC'\r\n"
				+ " AND C.EXT_REF_NO NOT LIKE '999%'\r\n"
				+ " AND C.PV_CVE_MVT = '10'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI,\r\n"
				+ " 'FC_FOLIO' AS CAMPO,\r\n"
				+ " 'N/A' AS CLAVE,\r\n"
				+ " NVL(MAX(C.VALOR),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A,\r\n"
				+ " posuser.POS_INBOUND_DOCS B,\r\n"
				+ " posuser.POS_HEF_DETL C\r\n"
				+ " WHERE A.ID = B.PE_ID\r\n"
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") +  "'\r\n"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +  "'\r\n"
				+ " AND B.ID = C.PID_ID\r\n"
				+ " AND B.DOC_TYPE = 'HEF'\r\n"
				+ " AND C.PREFIJO = 'FFAC'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI,\r\n"
				+ " 'PV_FOLIO' AS CAMPO,\r\n"
				+ " '26' AS CLAVE,\r\n"
				+ " NVL(MAX(TO_NUMBER(C.EXT_REF_NO)),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A,\r\n"
				+ " posuser.POS_INBOUND_DOCS B,\r\n"
				+ " posuser.POS_TSF C\r\n"
				+ " WHERE A.ID = B.PE_ID\r\n"
				+ " AND A.PV_CR_PLAZA  = '" + data.get("plaza") + "'\r\n"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") + "'\r\n"
				+ " AND B.ID = C.PID_ID\r\n"
				+ " AND B.DOC_TYPE = 'TSF'\r\n"
				+ " AND C.PV_CVE_MVT = '26'\r\n"
				+ " AND C.EXT_REF_NO NOT LIKE '999%'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI,\r\n"
				+ " 'PV_CON_TSN' AS CAMPO,\r\n"
				+ " 'N/A' AS CLAVE,\r\n"
				+ " NVL(MAX(TO_NUMBER(C.EXT_REF_NO)),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A,\r\n"
				+ " posuser.POS_INBOUND_DOCS B,\r\n"
				+ " posuser.POS_TSF C\r\n"
				+ " WHERE A.ID = B.PE_ID\r\n"
				+ " AND A.PV_CR_PLAZA  = '" + data.get("plaza") + "'\r\n"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +  "'\r\n"
				+ " AND B.ID = C.PID_ID\r\n"
				+ " AND B.DOC_TYPE = 'TSF'\r\n"
				+ " AND C.PV_CVE_MVT  IN ('04','34')\r\n"
				+ " AND C.EXT_REF_NO NOT LIKE '999%'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI,\r\n"
				+ " 'PV_FOLIO' AS CAMPO,\r\n"
				+ " '13' AS CLAVE,\r\n"
				+ " NVL(MAX(TO_NUMBER(F.EXT_REF_NO)),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE D,\r\n"
				+ " posuser.POS_INBOUND_DOCS E,\r\n"
				+ " posuser.POS_RTV F\r\n"
				+ " WHERE D.ID = E.PE_ID\r\n"
				+ " AND D.PV_CR_PLAZA  = '" + data.get("plaza") + "'\r\n"
				+ " AND D.PV_CR_TIENDA = '" + data.get("tienda") +  "'\r\n"
				+ " AND E.ID = F.PID_ID\r\n"
				+ " AND E.DOC_TYPE = 'RTV'\r\n"
				+ " AND F.EXT_REF_NO NOT LIKE '999%'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI,\r\n"
				+ " 'PV_FOLIO' AS CAMPO,\r\n"
				+ " C.PV_CVE_MVT AS CLAVE,\r\n"
				+ " NVL(MAX(C.FOLIO),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A,\r\n"
				+ " posuser.POS_INBOUND_DOCS B,\r\n"
				+ " posuser.POS_SDI_DETL C\r\n"
				+ " WHERE A.ID = B.PE_ID\r\n"
				+ " AND A.PV_CR_PLAZA  = '" + data.get("plaza") +  "'\r\n"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +  "'\r\n"
				+ " AND B.ID  = C.PID_ID\r\n"
				+ " AND B.DOC_TYPE  = 'SDI'\r\n"
				+ " GROUP BY C.PV_CVE_MVT\r\n"
				+ " UNION\r\n"
				+ " SELECT 'DS50' AS APPLI,\r\n"
				+ " 'PV_DOC_ID' AS CAMPO,\r\n"
				+ " 'N/A'  AS CLAVE,\r\n"
				+ " NVL(MAX(PV_DOC_ID),1) AS VALUE\r\n"
				+ " FROM posuser.POS_ENVELOPE A,\r\n"
				+ " posuser.POS_INBOUND_DOCS B\r\n"
				+ " WHERE A.ID  = B.PE_ID\r\n"
				+ " AND A.PV_CR_PLAZA  = '" + data.get("plaza") + "'\r\n"
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") +  "'\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '26', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '0', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '4', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '6', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '10', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '11', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '12', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '13', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '14', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '18', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '21', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '22', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '26', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '27', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '30', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '31', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '35', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '51', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '52', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '61', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '66', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '70', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '71', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '72', 1 FROM DUAL\r\n"
				+ " UNION\r\n"
				+ " SELECT 'POS' AS APPLI, 'PV_FOLIO', '73', 1 FROM DUAL\r\n"
				+ " ) GROUP BY APPLI, CAMPO, CLAVE ORDER BY 1,2, DECODE(clave,'N/A',0,to_number(clave))";

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCWMLQA.executeQuery(tdcQRY1);

		String APPLI = Paso1.getData(0, "APPLI");
		String Valuedb = Paso1.getData(0, "MAX(VALUE)");
		
		System.out.println(APPLI);
		System.out.println(Valuedb);
		
		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Realizar una petición HTTP de tipo GET al servidor IS con la ruta:");

		u = new SeleniumUtil(new ChromeTest(), true);
		String api = "http://" + user + ":" + ps + "@" + server + ":5555/invoke/oxxows.pub/wsGetMaxPvDocID?"
				+ "pvCrPlaza=" + data.get("plaza") + "&pvCrTienda=" + data.get("tienda") + "&appli=" + APPLI + "";
		System.out.println(api);
		u.get(api);
		u.hardWait(8);
		String ValueWeb = u.getDriver().findElement(By.cssSelector("body > folios > field_value")).getText();
		System.out.println(ValueWeb);
		u.close();

		/* Paso 3 *********************************************************/

		addStep("Validar listado de tiendas retornado.");

		System.out.println("Paso3");

		boolean Paso3 = Valuedb.equals(ValueWeb);

		if (Paso3) {
			
			testCase.addCodeEvidenceCurrentStep(ValueWeb);
			testCase.addCodeEvidenceCurrentStep(ValueWeb);

			System.out.println("La informacion es corrrecta");
			System.out.println(Paso3);
		}

		assertTrue(Paso3, "No concide la informacion");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el retorno del últ. ID de docto. enviado X la tda 50QTE  para  pza 10MON con aplic. dist. de DS50 X parte del serv. web ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_006_Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicacion_Distinta_5S50_ServicioWeb";
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
