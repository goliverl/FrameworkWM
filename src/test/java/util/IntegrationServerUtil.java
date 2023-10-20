package util;

import java.util.ArrayList;

import org.openqa.selenium.By;

import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class IntegrationServerUtil {

	SeleniumUtil u;
	
	private String user;
	private String pass;
	private String ip;
	
	public IntegrationServerUtil(SeleniumUtil u, String user, String pass, String ip) {
		this.u = u;
		this.user = user;
		this.pass = pass;
		this.ip = ip;
		
		String url = "http://" + user + ":" + pass + "@" + ip;
		u.get(url);
	}
	
	public void changeStatusAdapter(String adapterName, boolean status) throws Exception {
		
		u.getDriver().switchTo().frame(0);
		//u.highLight(By.xpath("/html"));
		
		u.getDriver().switchTo().frame(0);
		//u.highLight(By.xpath("/html"));
		
		u.getDriver().switchTo().frame(1);
		//u.highLight(By.xpath("/html"));
		
		u.click(By.xpath("//a[contains(text(), 'Adapters')]"));
		u.click(By.xpath("//a[contains(text(), 'Adapter for JDBC')]"));
		
		//Se abre una nueva pestaña con los adapters
		ArrayList<String> tabs = new ArrayList<String> (u.getDriver().getWindowHandles());
		u.getDriver().switchTo().window(tabs.get(1));
		
		u.getDriver().switchTo().frame(2);
		//u.highLight(By.xpath("/html"));
		
		u.click(By.xpath("//a[contains(text(), 'Filter Connections')]"));
		u.sendKeys(By.xpath("//*[@id=\"searchConnectionName\"]"), adapterName);
		u.click(By.xpath("//*[@id=\"submitButton\"]"));
		
		By adapter = By.xpath("//td[contains(text(), '"+adapterName+"')]/../td[4]");
		String actualStatus = u.getText(adapter);

		if ((actualStatus.equals("Yes") != status) ) {
			u.click(adapter);
			u.getDriver().switchTo().alert().accept();
		}
		
		actualStatus = u.getText(adapter);
		
		if ((actualStatus.equals("Yes") != status) ){
			throw new Exception("No se pudo cambiar el status del adapter");
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		IntegrationServerUtil iu = new IntegrationServerUtil(u, "AutoPruebasIrving", "pruebas.202", "10.182.32.15:5555");
	
		iu.changeStatusAdapter("AdapterConnections:DBS_TEST", true);
		
	}
	
	
}