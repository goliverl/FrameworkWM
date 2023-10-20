///////////////Pendiente//////////////

package interfaces.WS;

import static org.testng.Assert.assertFalse;
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

public class Validar_Retorno_Resumen_Mensual_10MON_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_009_Validar_Retorno_Resumen_Mensual_10MON_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbPouser = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = "SELECT \r\n"
				+ "  PLAZA PV_CR_PLAZA,TIENDA PV_CR_TIENDA, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'01',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D01, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'02',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D02, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'03',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D03, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'04',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D04, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'05',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D05, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'06',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D06, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'07',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D07, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'08',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D08, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'09',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D09, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'10',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D10, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'11',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D11, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'12',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D12, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'13',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D13, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'14',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D14, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'15',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D15, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'16',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D16, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'17',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D17, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'18',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D18, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'19',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D19, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'20',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D20, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'21',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D21, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'22',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D22, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'23',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D23, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'24',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D24, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'25',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D25, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'26',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D26, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'27',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D27, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'28',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D28, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'29',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D29, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'30',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D30, \r\n"
				+ "  PR50_AUDIT.getColorPondTot( sum (decode(TO_CHAR(fecha,'DD'),'31',PR50_AUDIT.ponderarTotales(NET_TIC,NET_SAL,TOT_SYB,TOT_HEF,DIF_TIC_SAL,DIF_VTA_HEF,AC_SAL,AC_TIC_SAL,AC_VTA_HEF),0) ) )   D31  \r\n"
				+ "  FROM XXFC_TOTALES_ACC_V \r\n"
				+ "  WHERE \r\n"
				+ "  PLAZA= '10MON' \r\n"
				+ "  AND trunc(fecha)>=TO_DATE('01'||LPAD([mes_inicio],2,'0')||[año_inicio],'DDMMYYYY') \r\n"
				+ "  AND trunc(fecha)<=last_day(TO_DATE('01'||LPAD([mes_inicio],2,'0')||[año_incio],'DDMMYYYY')) \r\n"
				+ "  GROUP BY PLAZA,TIENDA\r\n";

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbPouser.executeQuery(tdcQRY1);

		String Countdb = Paso1.getData(0, "Count");

		System.out.println(Countdb);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Realizar una petición HTTP de tipo GET al servidor IS con la ruta:");

		u = new SeleniumUtil(new ChromeTest(), true);
		String contra = "http://" + user + ":" + ps + "@" + server
				+ ":5555/invoke/oxxows.pub:wsGetGLJournalColombia";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(8);
		String CountApi = u.getDriver()
				.findElement(By.cssSelector("#folder0 > div.opened > div.line > span:nth-child(2)")).getText();
		System.out.println(CountApi);
		u.close();

		/* Paso 3 *********************************************************/

		addStep("Validar listado de tiendas retornado.");

		System.out.println("Paso3");

		boolean Paso3 = Countdb.equals(CountApi);

		if (Paso3) {
			testCase.addCodeEvidenceCurrentStep(Countdb);

			System.out.println("La informacion es corrrecta");
			System.out.println(Paso3);
		}

		assertFalse(!Paso3, "No concide la informacion");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el retorno del resumen mensual para la plaza 10MON Xparte del serv. web. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_009_Validar_Retorno_Resumen_Mensual_10MON_ServicioWeb";
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
