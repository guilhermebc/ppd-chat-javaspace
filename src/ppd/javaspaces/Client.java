/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppd.javaspaces;

import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import sun.misc.GC;
import net.jini.space.JavaSpace;

/**
 *
 * @author guilhermecosta
 */
public class Client extends javax.swing.JFrame {

    /**
     * Creates new form Client
     */
    User user;
    public int latitude, longitude, countInt;
    public String username, receiver, latStr1, longStr1;
    public int numberOfClients;
    public Message messageSpace;
    public ArrayList<String> localList = new ArrayList<String>();
    public boolean isExists = false;
    public Lookup finder = new Lookup(JavaSpace.class);
    public JavaSpace space = (JavaSpace) finder.getService();

    public Client() {
        super("Client Space");

        if (checkSpace(finder, space)) {
            //init UI
            initComponents();

            //get user info
            numberOfClients();
            
            setLat();
            
            setLong();
            
            getClients();
            
            setUserName();
            
            registerInList();
            
            timeOutRefresh();
            
            readMessage();
            
        } else {
            System.exit(-1);
        }
    }

    public void setUserName() {
        do{
            username = JOptionPane.showInputDialog("Forneça um nome: ");
        }while(checkUnicUser(username) == 0);
    }
    
    public int checkUnicUser(String username){
        for(String index : localList){
            System.out.println("inside");
            if(index.equals(username)){
                System.out.println("Nome inválido, tente outro!");
                return 0;
            }
        }
        System.out.println("Nome válido!");
        userLabel.setText(username);
        return 1;
    }

    public void setLat() {
        String latString = JOptionPane.showInputDialog("Forneça a sua latitude: ");
        latStr1 = latString;
        latitude = Integer.parseInt(latString);
        latLabel.setText(latString);
    }

    public void setLong() {
        String longString = JOptionPane.showInputDialog("Forneça a sua longitude: ");
        longStr1 = longString;
        longitude = Integer.parseInt(longString);
        longLabel.setText(longString);
    }

    public boolean checkSpace(Lookup finder, JavaSpace space) {
        System.out.println("Procurando pelo servico JavaSpace...");

        finder = new Lookup(JavaSpace.class);
        space = (JavaSpace) finder.getService();

        if (space == null) {
            System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
            return false;
        }
        System.out.println("O servico JavaSpace foi encontrado.");
        return true;
    }

    public void writeMessage(String destiny, String input) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    checkSpace(finder, space);
                    
                    messageSpace = new Message();

                    messageSpace.from = username;
                    messageSpace.content = input;
                    messageSpace.receiver = destiny;

                    space.write(messageSpace, null, 60 * 1000);

                } catch (RemoteException | TransactionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void readMessage() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    checkSpace(finder, space);

                    if (space == null) {
                        System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
                        System.exit(-1);
                    }
                    System.out.println("Lendo...");
                    
                    while (true) {
//                        User templateUser = new User();
//                        User user = (User) space.take(templateUser, null, 60 * 1000);
                        
                        Message template = new Message();
                        Message msg = (Message) space.take(template, null, 120*1000);
//                         && checkPosition(user.latitude, user.longitude) == 0
                        
                        if(username.equals(msg.receiver)){
                            textArea.append("De " + msg.from + " : " + msg.content + "\n");    
                        }else {
                            Message templateWrite = new Message();
                            templateWrite.from = msg.from;
                            templateWrite.content = msg.content;
                            templateWrite.receiver = msg.receiver;
                            space.write(templateWrite, null, 60 * 1000);
                            
//                            User templateUser2 = new User();
//                            templateUser2.username = user.username;
//                            templateUser2.latitude = user.latitude;
//                            templateUser2.longitude = user.longitude;
//                            space.write(templateWrite, null, 60 * 1000);
                        }
                        
                        
                        
                        if (msg == null) {
                            textArea.append("Tempo de espera esgotado. Encerrando...\n");
                            System.exit(0);
                        }
                    }
                } catch (ServerException | InterruptedException | UnusableEntryException | TransactionException e) {
                    e.printStackTrace();
                } catch (RemoteException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

    }

    public void registerInList() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (space == null) {
                        System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
                        System.exit(-1);
                    }

                    System.out.println("Registrando...");

                    User userInfo = new User();
                    userInfo.username = username;
                    userInfo.latitude = latStr1;
                    userInfo.longitude = longStr1;
                    
                    space.write(userInfo, null, 60 * 1000);
                    
                    System.out.println("Registrado com sucesso!");

                } catch (RemoteException | TransactionException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void refreshList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (space == null) {
                        System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
                        System.exit(-1);
                    }

                    System.out.println("Atualizando lista...");
                    logArea.setText("");
                    readNumberClients();
                    for (int i = 0; i < numberOfClients; i++) {
                        User template = new User();
                        User user = (User) space.take(template, null, 60 * 1000);
                        if (user != null) {
                            System.out.println(user.username + " " + user.latitude + " " + user.longitude);
                            if (checkPosition(user.latitude, user.longitude) == 0 && !(user.username.equals(username))) {
                                logArea.append(user.username + "\n");
                            }
                        }
                        User templateWrite = new User();
                        
                        templateWrite.username = user.username;
                        templateWrite.latitude = user.latitude;
                        templateWrite.longitude = user.longitude;
                        
                        space.write(templateWrite, null, 60 * 1000);
                    }

                    System.out.println("Lista atualizada!");

                } catch (InterruptedException | RemoteException | UnusableEntryException | TransactionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public void editLatLong(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                setLat();
                setLong();
                readNumberClients();
                for (int i = 0; i < numberOfClients; i++) {
                    try {
                        User template = new User();
                        User user = (User)space.takeIfExists(template, null, 60 * 1000);
                        System.out.println("Atualizando lat e long...");
                        if(user != null){
                            if(user.username.equals(username)){
                                User templateWrite = new User();
                                templateWrite.username = username;
                                templateWrite.latitude = latStr1;
                                templateWrite.longitude = longStr1;
                                space.write(templateWrite, null, 60 * 1000);
                                System.out.println(templateWrite.username + " " + 
                                        templateWrite.latitude + " " + 
                                        templateWrite.longitude + "atualizados!");
                            }else {
                                User templateWrite2 = new User();
                                templateWrite2.username = user.username;
                                templateWrite2.latitude = user.latitude;
                                templateWrite2.longitude = user.longitude;
                                space.write(templateWrite2, null, 60 * 1000);
                                System.out.println(templateWrite2.username + " " + 
                                        templateWrite2.latitude + " " + 
                                        templateWrite2.longitude + "retornados!!");
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    public void numberOfClients() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Counter template = new Counter();
                    Counter user = (Counter)space.takeIfExists(template, null, 60 * 1000);
                    if(user == null){
                        Counter templateWrite = new Counter();
                        System.out.println(templateWrite.count);
                        templateWrite.count = "1";
                        space.write(templateWrite, null, 60 * 1000);
                        System.out.println("Numero de clientes: " + templateWrite.count);
                    }else {
                        Counter templateWrite2 = new Counter();
                        System.out.println("Numero de clientes: " + templateWrite2.count);
                        countInt = Integer.parseInt(user.count);
                        countInt++;
                        templateWrite2.count = Integer.toString(countInt);
                        space.write(templateWrite2, null, 60 * 1000);
                        System.out.println("Numero de clientes: " + templateWrite2.count);
                    }
                    
                    
                } catch (InterruptedException | RemoteException | UnusableEntryException | TransactionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    //metodo readnumberclients
    public void readNumberClients(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Counter template = new Counter();
                    Counter user = (Counter)space.takeIfExists(template, null, 60 * 1000);
                    if(user != null){
                        
                        numberOfClients = Integer.parseInt(user.count);
                        
                        Counter templateWrite = new Counter();
                        templateWrite.count = user.count;
                        space.write(templateWrite, null, 60 * 1000);
                        System.out.println("Numero de clientes lido: " + numberOfClients 
                                + "user count: " + user.count);
                    }
                    
                } catch (InterruptedException | RemoteException | UnusableEntryException | TransactionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public void getClients(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < countInt - 1; i++) {
                    try {
                        User template = new User();
                        User user = (User)space.takeIfExists(template, null, 60 * 1000);
                        if(user != null){
                            localList.add(user.username);
                            System.out.println(user.username);
                            User templateWrite = new User();
                            templateWrite.username = user.username;
                            templateWrite.latitude = user.latitude;
                            templateWrite.longitude = user.longitude;
                            
                            space.write(templateWrite, null, 60 * 1000);
                        }else {
                            System.out.println("Não há clientes!");
                        }

                    } catch (TransactionException | RemoteException | UnusableEntryException | InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    public int checkPosition(String latitudeTaked, String longitudeTaked){
        System.out.println(latitudeTaked);
        System.out.println(longitudeTaked);
        
        int latIntTaked = Integer.parseInt(latitudeTaked);
        int longIntTaked = Integer.parseInt(longitudeTaked);
        int resLat, resLong;

        resLat = (latIntTaked - latitude);
        resLong = (longIntTaked - longitude);
        
        if (resLat > 200 | resLat < -200 | resLong > 200 | resLong < -200) {
            System.out.println("usuario distante");
            return 1;
        }
        
        return 0;
    }
    
    public void timeOutRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        refreshList();
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                    }
                    System.out.println("Hello world!");
                }
            }
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sendField = new javax.swing.JTextField();
        sendBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        userLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();
        refreshBtn = new javax.swing.JButton();
        latLabel = new javax.swing.JLabel();
        longLabel = new javax.swing.JLabel();
        closeUsers = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        exitBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        sendField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sendFieldKeyPressed(evt);
            }
        });

        sendBtn.setText("Enviar");
        sendBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sendBtnMouseClicked(evt);
            }
        });

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(20);
        textArea.setFocusable(false);
        jScrollPane1.setViewportView(textArea);

        userLabel.setText("Usuário");

        logArea.setEditable(false);
        logArea.setColumns(14);
        logArea.setRows(20);
        logArea.setFocusable(false);
        jScrollPane2.setViewportView(logArea);

        refreshBtn.setText("Atualizar");
        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refreshBtnMouseClicked(evt);
            }
        });

        latLabel.setText("Latitude");

        longLabel.setText("Longitude");

        closeUsers.setText("Usuários próximos");

        jLabel1.setText("Usuário:");

        jLabel2.setText("Latitude:");

        jLabel3.setText("Longitude:");

        exitBtn.setText("Sair");
        exitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitBtnMouseClicked(evt);
            }
        });

        editBtn.setText("Editar");
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editBtnMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(userLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(latLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3)
                                .addComponent(longLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendField, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sendBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(refreshBtn, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(closeUsers)
                        .addGap(43, 43, 43))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(editBtn)
                        .addGap(52, 52, 52)
                        .addComponent(exitBtn)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(exitBtn)
                        .addComponent(editBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(latLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(longLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeUsers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendBtn)
                    .addComponent(refreshBtn))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sendBtnMouseClicked
        // TODO add your handling code here:
        String message = sendField.getText().trim();
        String splittedMsg[] = message.split(":", 2);
        
        if(!message.equals("")){
            writeMessage(splittedMsg[0], splittedMsg[1]);
            textArea.append("Voce para " + splittedMsg[0] + " : " + splittedMsg[1] + "\n");
            sendField.setText("");   
        }
    }//GEN-LAST:event_sendBtnMouseClicked

    private void refreshBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshBtnMouseClicked
        // TODO add your handling code here:
        refreshList();
    }//GEN-LAST:event_refreshBtnMouseClicked

    private void sendFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sendFieldKeyPressed
        // TODO add your handling code here:
        String message = sendField.getText().trim();
        String splittedMsg[] = message.split(":", 2);
        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if(!message.equals("")){
                writeMessage(splittedMsg[0], splittedMsg[1]);
                textArea.append("Voce para " + splittedMsg[0] + " : " + splittedMsg[1] + "\n");
                sendField.setText("");   
            }
        }
    }//GEN-LAST:event_sendFieldKeyPressed

    private void exitBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitBtnMouseClicked
        // TODO add your handling code here:
        System.exit(1);
    }//GEN-LAST:event_exitBtnMouseClicked

    private void editBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBtnMouseClicked
        // TODO add your handling code here:
        editLatLong();
    }//GEN-LAST:event_editBtnMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();
                client.setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel closeUsers;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton exitBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JLabel latLabel;
    public javax.swing.JTextArea logArea;
    public javax.swing.JLabel longLabel;
    public javax.swing.JButton refreshBtn;
    public javax.swing.JButton sendBtn;
    public javax.swing.JTextField sendField;
    public javax.swing.JTextArea textArea;
    public javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
}
