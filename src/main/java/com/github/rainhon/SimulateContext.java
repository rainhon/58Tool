package com.github.rainhon;

import com.github.rainhon.proxy.AProxy;
import com.github.rainhon.proxy.ProxyManager;
import com.github.rainhon.util.IPManager;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.InetSocketAddress;

public class SimulateContext {
    private BrowserMobProxyServer server = null;
    private WebDriver driver = null;
    private boolean useProxy = false;
    ProxyManager proxyManager = new ProxyManager();
    IPManager IPManager;
    //界面controller
    Controller controller = Main.getLoader().getController();
    public WebDriverWait wait;

    public void init(boolean useProxy) throws Exception{
        this.useProxy = useProxy;
        server = new BrowserMobProxyServer();
        if(useProxy){
            //更换IP策略--使用自动代理
            proxyManager.init();
            AProxy proxy;
            try{
                proxy = proxyManager.getProxy(0);
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            server.setChainedProxy(new InetSocketAddress(proxy.getIp(), proxy.getPort()));
            System.out.println("已启用代理");
        }else{
            //更换IP策略--重连账号

            //重连频率
            int reconnectTimes = 0;
            try{
                reconnectTimes =Integer.parseInt(controller.reconnectTimes.getText().trim());
            }catch (NumberFormatException e){
                throw new Exception("重连频率只能填写0-100的整数");
            }
            if(reconnectTimes < 0 || reconnectTimes > 100){
                throw new Exception("重连频率只能填写0-100的整数");
            }

            IPManager = new IPManager(controller.ADSLNameField.getText().trim(),
                    controller.ADSLAccountField.getText().trim(),
                    controller.ADSLPasswordField.getText().trim(),
                    reconnectTimes);
            System.out.println("使用自动重连IP方式");
        }

        server.start(0);

        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();

        Proxy proxy = ClientUtil.createSeleniumProxy(server);

        chromeOptions.setProxy(proxy);
//        chromeOptions.addArguments("-headless");
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--auto-open-devtools-for-tabs");
        driver = new ChromeDriver(chromeOptions);

        wait = new WebDriverWait(driver, 20);
    }

    public WebDriver getDriver(){
        return driver;
    }

    public BrowserMobProxyServer getServer(){
        return server;
    }

    public IPManager getIPManager(){return IPManager;}
}
