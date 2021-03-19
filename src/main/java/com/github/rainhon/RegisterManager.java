package com.github.rainhon;

import com.alibaba.fastjson.JSON;
import com.github.rainhon.util.IPManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

public class RegisterManager {
    static HashMap<String, PhoneRecord> phoneList = new HashMap<>();
    static PhoneRecord phoneRecord = null;
    static SimulateContext simulateContext = null;
    static ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    static Future<?> future;
    private static  MessageAPI messageAPI = null;
    private static final String registerUrl = "https://passport.58.com/reg";
    static boolean isNickNameValid;
    static boolean hasSendGetCodeRequest;
    static int isMobileBind;
    static WebElement phoneField;
    static WebElement getCodeBtn;
    static WebElement validCodeField;
    static WebElement nicknameField;
    static WebElement passwordField;
    static WebElement newPasswordField;
    static WebElement registerBtn;
    static int IPUseTimes;
    static int tryTimes;

    public static boolean initSimulateContext(boolean useProxy) throws Exception{
        simulateContext = new SimulateContext();
        simulateContext.init(useProxy);

        simulateContext.getServer().addResponseFilter((httpResponse, httpMessageContents, httpMessageInfo) -> {
            if(httpMessageInfo.getUrl().contains("getcode?")){
                int type=0;
                try{
                    System.out.println("=========getcodeFilter");
                    String result = httpMessageContents.getTextContents();
                    String drawResult = result.substring(result.indexOf('{'));
                    drawResult = drawResult.substring(0, drawResult.length() - 1);
                    String data = JSON.parseObject(drawResult).getString("data");
                    type = JSON.parseObject(data).getInteger("type");
                    System.out.println("type:" + type);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(type == 1){
                    //可以收到验证码
                    phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.USEFUL);
                }else{
                    phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.INVALID);
                }
                hasSendGetCodeRequest = true;
            }

            if(httpMessageInfo.getUrl().contains("checknickname?")){
                System.out.println("=========checknickname");
                String result = "";
                try{
                    result = httpMessageContents.getTextContents();
                }catch (Exception e){
                    e.printStackTrace();
                }

                System.out.println("result:"+result);
                if(result.equals("1")){
                    isNickNameValid = true;
                }else{
                    nicknameField.clear();
                    phoneRecord.setAccount(randomString(10));
                    nicknameField.sendKeys(phoneRecord.getAccount());
                    passwordField.sendKeys("");
                }

            }

            if(httpMessageInfo.getUrl().contains("domobileregcheck?")){
                int ismobilebind=0;
                try{
                    System.out.println("=========domobileregcheck");
                    String result = httpMessageContents.getTextContents();
                    String drawResult = result.substring(result.indexOf('{'));
                    drawResult = drawResult.substring(0, drawResult.length() - 1);
                    String data = JSON.parseObject(drawResult).getString("data");
                    ismobilebind = JSON.parseObject(data).getInteger("ismobilebind");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(ismobilebind == 2){
                    //号码已注册
                    isMobileBind = 2;
                    phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.INVALID);
                }else{
                    isMobileBind = 1;
                    phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.USEFUL);
                }

            }

        });

        //开始加载页面，必须放在添加监听后
        Future<?> loadFuture = Executors.newSingleThreadExecutor().submit(()->{
            simulateContext.getDriver().get(registerUrl);
            getPageElement();
        });
        try{
            loadFuture.get(30, TimeUnit.SECONDS);
        }catch (Exception e){
            System.out.println("超时或无法打开");
            return false;
        }
        return true;
    }

    public static void getPhone(){
        tryTimes = 0;
        do{
            try{
                //获取手机号
                phoneRecord = messageAPI.getPhoneNumber();
                phoneList.put(phoneRecord.getPhone(), phoneRecord);
            }catch (Exception e){
                e.printStackTrace();
            }

            //填号码
            phoneField.sendKeys(phoneRecord.getPhone());
            //提交验证码请求
            getCodeBtn.click();
            //IP使用次数+1 填0不重连
            if(simulateContext.getIPManager().getReconnectTimes() != 0 && ++IPUseTimes > simulateContext.getIPManager().getReconnectTimes()){
                Future<?> reconnectFuture = Executors.newSingleThreadExecutor().submit(()->{
                    try{
                        System.out.println("重连中");
                        simulateContext.getIPManager().reconnect();
                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                });

                try{
                    reconnectFuture.get();
                }catch (InterruptedException|ExecutionException e){
                    System.out.println("重连失败:"+e.getMessage());
                }

            }
            hasSendGetCodeRequest =false;

            future = service.scheduleAtFixedRate(()->{
                if(hasSendGetCodeRequest){
                    future.cancel(true);
                }
            }, 0, 1, TimeUnit.SECONDS);

            try{
                future.get(10, TimeUnit.SECONDS);
                future.cancel(true);
            }catch (Exception ignore){
            }

            if(phoneRecord.getPhoneStatus() != PhoneRecord.PhoneStatus.USEFUL){
                System.out.println("号码状态"+phoneRecord.getPhoneStatus().getDescription());
                try{
                    messageAPI.banPhone(phoneRecord);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("拉黑号码");
                System.out.println("号码状态"+phoneRecord.getPhoneStatus().getDescription());
            }
            freshPage();
        }while(tryTimes<30 && phoneRecord.getPhoneStatus() != PhoneRecord.PhoneStatus.USEFUL);
    }

    public static void doRegister(boolean useProxy){

        if(simulateContext == null){
            boolean ready;
            try{
                ready = initSimulateContext(useProxy);
            }catch (Exception e){
                System.out.println(e.getMessage());
                return;
            }

            if(!ready){
                System.out.println("加载超时");
                return;
            }

        }

        getPhone();

        if(phoneRecord.getPhoneStatus() != PhoneRecord.PhoneStatus.USEFUL){
            System.out.println("取号过多，请稍后尝试");
            return;
        }

        try{
            Thread.sleep(10000);//等待验证码
        }catch(InterruptedException e){
            e.printStackTrace();
            return;
        }

        //接收验证码
        try{
            String validateCode = messageAPI.readMessage(phoneRecord);
            validCodeField.sendKeys(validateCode);
            passwordField.sendKeys("");
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

        //验证是否注册
        isMobileBind = -100;
        future = service.scheduleAtFixedRate(()->{
            if(isMobileBind != -100){
                future.cancel(true);
            }
        }, 0, 1, TimeUnit.SECONDS);

        try{
            future.get(10, TimeUnit.SECONDS);
            future.cancel(true);
        }catch (Exception ignore){
        }

        //已注册退出
        if(isMobileBind == 2){
            try{
                messageAPI.banPhone(phoneRecord);
            }catch (Exception e){
                e.printStackTrace();
            }
            freshPage();
            doRegister(useProxy);
            return;
        }

        //生成昵称和密码
        inputNickname(nicknameField);
        if(!isNickNameValid){
            return;
        }
        phoneRecord.setPassword(randomString(12));

        passwordField.sendKeys(phoneRecord.getPassword());
        newPasswordField.sendKeys(phoneRecord.getPassword());
        registerBtn.click();

        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        phoneRecord.setCookies(simulateContext.getDriver().manage().getCookies());
        phoneRecord.setPhoneStatus(PhoneRecord.PhoneStatus.REGISTERED);

        LoginManager.addNewAccount(phoneRecord);

        try{
            saveRecord();
            messageAPI.banPhone(phoneRecord);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static String randomString(int length){
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder StringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            StringBuilder.append(str.charAt(number));
        }
        return StringBuilder.toString();
    }

    private static void inputNickname(WebElement nicknameField){
        //填号码
        isNickNameValid = false;
        phoneRecord.setAccount(randomString(10));
        nicknameField.sendKeys(phoneRecord.getAccount());
        passwordField.sendKeys("");

        future = service.scheduleAtFixedRate(()->{
            if(isNickNameValid){
                future.cancel(true);
            }
        }, 0, 1, TimeUnit.SECONDS);

        try{
            future.get(10, TimeUnit.SECONDS);
            future.cancel(true);
            System.out.println(isNickNameValid);
        }catch (Exception ignore){
        }

    }

    public static MessageAPI getMessageAPI(){
        return messageAPI;
    }

    public static void setMessageAPI(String account, String password, String item) throws Exception{
        messageAPI = new MessageAPI(account, password, item);
    }

    private static void saveRecord() throws Exception{
        File file = new File("../data/account.txt");
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                throw new Exception(e);
            }
        }
        try(Writer writer = new FileWriter(file, true)){
            writer.write(phoneRecord.getPhone() + ">>>" +
                        phoneRecord.getPassword() + ">>>" + phoneRecord.getCookiesString() + "\n");
        }catch (IOException e){
            throw new Exception(e);
        }
    }

    //刷新页面，重新获取页面元素
    private static void freshPage(){
        simulateContext.getDriver().navigate().refresh();
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        getPageElement();
    }

    private static void getPageElement(){
        phoneField = simulateContext.getDriver().findElement(By.id("mask_body_item_phonenum"));
        getCodeBtn = simulateContext.getDriver().findElement(By.id("mask_body_item_getcode"));
        validCodeField = simulateContext.getDriver().findElement(By.id("mask_body_item_validcode"));
        nicknameField = simulateContext.getDriver().findElement(By.id("mask_body_item_username"));
        passwordField = simulateContext.getDriver().findElement(By.id("mask_body_item_password"));
        newPasswordField = simulateContext.getDriver().findElement(By.id("mask_body_item_newpassword"));
        registerBtn = simulateContext.getDriver().findElement(By.id("mask_body_item_login"));
    }
}
