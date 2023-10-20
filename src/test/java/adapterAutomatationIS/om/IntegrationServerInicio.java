package adapterAutomatationIS.om;

import java.util.ArrayList;

import org.openqa.selenium.By;

import junit.framework.TestCase;
import utils.selenium.SeleniumUtil;

public class IntegrationServerInicio{

	// los objetos 
	SeleniumUtil u;
	By adapterOption = By.xpath("//*[@id=\"Adapters_twistie\"]");
	By forJDBC = By.xpath("//*[text()='webMethods Adapter for JDBC...']");
	
	//generacion de constructor 
	public IntegrationServerInicio(SeleniumUtil u) {
		super();
		this.u = u;
	}

	public void clickAdapter() {
		
		u.getDriver().switchTo().frame(0);		
		u.getDriver().switchTo().frame(1);
//		u.highLight(By.xpath("//html"));
		
		u.click(adapterOption);
		u.click(forJDBC);
		
		 ArrayList <String> tabs = new ArrayList <String> (u.getDriver().getWindowHandles());
		    System.out.println(tabs.size());
		    u.getDriver().switchTo().window(tabs.get(1)); 
		
		
	}


	

	
	
	
	
}


