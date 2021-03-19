package com.github.rainhon;

import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.List;

public class LoginManager {
    public static List<PhoneRecord> accountList = new ArrayList<>();
    public static SimulateContext simulateContext = new SimulateContext();

    public LoginManager(){

    }

    public static void addNewAccount(PhoneRecord phoneRecord){
        accountList.add(phoneRecord);
    }

    public static void doLogin(PhoneRecord phoneRecord) throws Exception{
        simulateContext.init(false);
        simulateContext.getDriver().get("https://58.com");
        for(Cookie c : phoneRecord.getCookies()){
            simulateContext.getDriver().manage().addCookie(c);
        }
        simulateContext.getDriver().navigate().refresh();
    }

    public static List<PhoneRecord> getAccountList(){
        return accountList;
    }
}
