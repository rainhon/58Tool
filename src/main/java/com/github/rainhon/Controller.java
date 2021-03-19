package com.github.rainhon;

import com.github.rainhon.util.ADSL;
import com.github.rainhon.util.UtilCookie;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class Controller {

    //注册账号表元素
    public TableView<PhoneRecord> phoneTable = null;
    public TableColumn<PhoneRecord, String> tableColumnPhone = null;
    public TableColumn<PhoneRecord, String> tableColumnStatus = null;

    //短信平台登录元素
    public TextField messageAPIAccount = null;
    public TextField messageAPIPassword = null;
    public TextField messageAPIItem = null;
    public Button loginMessageAPIBtn = null;
    public Label messageAPIStatus = null;

    public CheckBox checkBoxUseProxy = null;
    public TextField ADSLAccountField = null;
    public TextField ADSLNameField = null;
    public TextField ADSLPasswordField = null;
    public TextArea consoleTextArea = null;
    public Button saveBtn = null;
    public Button registerBtn = null;
    public Button stopRegisterBtn = null;
    public Button loadAccountBtn = null;
    public TextField proxyAPI = null;
    public TextField registerTryTimes = null;
    public TextField reconnectTimes = null;

    //登录账号表元素
    public TableView<PhoneRecord> accountTableView = null;
    public TableColumn<PhoneRecord, String> accountTableColumnPhone = null;
    public TableColumn<PhoneRecord, String> accountTableColumnAccount = null;
    public TableColumn<PhoneRecord, String> accountTableColumnPassword = null;
    public TableColumn<PhoneRecord, String> accountTableColumnCookies = null;
    private TableView.TableViewSelectionModel<PhoneRecord> accountTableSelectionModel;

    private FileChooser fileChooser;

    private LoginManager loginManager = new LoginManager();

    private Future<?> future;

    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean notify = true;

    public PipedInputStream pipedInputStream;

    public void init(){
        tableColumnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

//        accountTableColumnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
//        accountTableColumnAccount.setCellValueFactory(new PropertyValueFactory<>("account"));
//        accountTableColumnPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
//        accountTableColumnCookies.setCellValueFactory(new PropertyValueFactory<>("cookiesString"));

        PipedOutputStream pipedOutputStream = new PipedOutputStream();

//        accountTableSelectionModel = accountTableView.getSelectionModel();

        try{
            pipedInputStream = new PipedInputStream(pipedOutputStream);
        }catch (IOException e){
            e.printStackTrace();
        }

        Executors.newSingleThreadExecutor().submit(()->{
            try(Reader reader = new BufferedReader(new InputStreamReader(pipedInputStream))){
                int charInt;
                while((charInt = reader.read()) != -1){
                    synchronized (stringBuilder){
                        stringBuilder.append((char)charInt);
                        if (notify) {
                            notify = false;
                            Platform.runLater(this::readConsoleBuffer);
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        });

        PrintStream ps;
        ps = new PrintStream(pipedOutputStream, true);
        System.setOut(ps);
        System.setErr(ps);
    }

    private void readConsoleBuffer(){
        synchronized (stringBuilder) {
            consoleTextArea.appendText(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
            notify = true;
        }
    }

    @FXML
    public void doRegister() {
        int tryTimes;
        try{
            tryTimes = Integer.parseInt(registerTryTimes.getText());
            if(tryTimes <= 0 || tryTimes > 100){
                throw new Exception();
            }
        }catch (Exception e){
            System.out.println("只能填写1-100的数字");
            return;
        }

        if(RegisterManager.getMessageAPI() == null){
            System.out.println("请先登录短信平台");
            return;
        }

        registerBtn.setDisable(true);
        registerTryTimes.setDisable(true);
        stopRegisterBtn.setDisable(false);
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        future = service.submit(() -> {
            register(tryTimes);
        });
    }

    @FXML
    public void doLogin() {
        //获取选中记录
        PhoneRecord record = accountTableSelectionModel.getSelectedItem();

        if(record == null){
            System.out.println("没有选中记录");
            return;
        }

        try{

            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            service.submit(() -> {
                try{
                    LoginManager.doLogin(record);
                }catch (Exception e){
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    public void ADSL() {
        Executors.newSingleThreadExecutor().submit(()->{
            try{
                ADSL.connAdsl(ADSLNameField.getText(),ADSLAccountField.getText(),ADSLPasswordField.getText());
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
}

    @FXML
    public void disconnectADSL() {
        Executors.newSingleThreadExecutor().submit(()->{
            try{
                ADSL.cutAdsl(ADSLNameField.getText());
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    public void saveRecord() {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName(new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date()) + ".txt");
        File file = fileChooser.showSaveDialog(null);
        if(file != null){
            try(Writer writer = new FileWriter(file)){
                List<PhoneRecord> accountList = LoginManager.getAccountList();
                for(PhoneRecord record : accountList){
                    writer.write(record.getPhone() + ">>>" +
                            record.getPassword() + ">>>" + record.getCookiesString() + "\n");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void loginMessageAPI(){
        try{
            RegisterManager.setMessageAPI(messageAPIAccount.getText().trim(), messageAPIPassword.getText().trim(), messageAPIItem.getText().trim());
            messageAPIStatus.setText("已登录");
        }catch (Exception e){
            System.out.println("登录失败,请检查账号密码是否正确");
            System.out.println(e.getMessage());
        }

    }

    @FXML
    public void loadAccount() {
        fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if(file != null){
            try(
                    Reader reader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    ){
                String line;
                String phone;
                String account;
                String password;
                String cookies;
                while((line = bufferedReader.readLine()) != null){
                    String[] temp = line.split(">>>");
                    phone = temp[0];
                    account = temp[1];
                    password = temp[2];
                    cookies = temp[3];
                    PhoneRecord phoneRecord = new PhoneRecord(phone, PhoneRecord.PhoneStatus.REGISTERED, 0);
                    phoneRecord.setAccount(account);
                    phoneRecord.setPassword(password);
                    phoneRecord.setCookies(UtilCookie.parseCookieString(cookies));

                    LoginManager.addNewAccount(phoneRecord);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        flushAccountTable();
    }

    @FXML
    public void stopRegister() {
        future.cancel(true);
        if(future.isCancelled()){
            stopRegisterBtn.setDisable(true);
            registerBtn.setDisable(false);
            registerTryTimes.setDisable(false);
        }
    }

    public void flushTable(){
        phoneTable.getItems().clear();
        RegisterManager.phoneList.forEach((phoneNumber, phoneRecord) ->{
            phoneTable.getItems().add(phoneRecord);
        });
    }

    public void flushAccountTable(){
        accountTableView.getItems().clear();
        LoginManager.getAccountList().forEach(phoneRecord -> {
            accountTableView.getItems().add(phoneRecord);
        });
    }

    public LoginManager getLoginManager(){
        return loginManager;
    }

    public String getProxyAPI(){
        return proxyAPI.getText();
    }

    private void register(int tryTimes){
        if(tryTimes <= 0){
            registerBtn.setDisable(false);
            registerTryTimes.setDisable(false);
            return;
        }

        if (Thread.currentThread().isInterrupted()) {
            future.cancel(true);
            System.out.println("取消任务");
            return;
        }

        RegisterManager.doRegister(checkBoxUseProxy.isSelected());
        flushTable();
        flushAccountTable();
        RegisterManager.simulateContext.getDriver().quit();
        RegisterManager.simulateContext.getServer().abort();
        register(--tryTimes);
    }

}
