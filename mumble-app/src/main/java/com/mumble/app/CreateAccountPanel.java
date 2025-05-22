package com.mumble.app;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;
import org.springframework.security.crypto.bcrypt.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

/**
 * A CreateAccountPanel provides methods for user form validation and registration
 */
public class CreateAccountPanel extends JPanel{

    private final JTextField unameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmPasswordField = new JPasswordField(15);

    private final JLabel unameError = new JLabel(" ");
    private final JLabel emailError = new JLabel(" ");
    private final JLabel passwordError = new JLabel(" ");
    private final JLabel confirmPasswordError = new JLabel(" ");

    private MumbleApp app;
    
    /**
     * Instantiates the CreateAccountPanel
     * @param a the MumbleApp with the main thread
     */
    public CreateAccountPanel(MumbleApp a){

        super(new BorderLayout());
        this.app = a;

        // create the label
        JLabel header = new JLabel("create your account");
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.PLAIN, 18));
        this.add(header, BorderLayout.NORTH);

        // create the wrapper panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;

        // add the username label and field
        addField(inputPanel, gbc, row++, "username:", unameField, unameError);

        // add the email label and field
        addField(inputPanel, gbc, row++, "email:", emailField, emailError);

        // add the phone label and field
        addField(inputPanel, gbc, row++,"phone:", phoneField, null);
        
        // add the password and confirm password labels and fields
        addField(inputPanel, gbc, row++, "password:",passwordField, passwordError);
        addField(inputPanel, gbc, row++, "confirm password:", confirmPasswordField, confirmPasswordError);

        // login button 
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton loginButton = new JButton("login");
        inputPanel.add(loginButton, gbc);

        // SignUp button
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        JButton signUpButton = new JButton("sign up");
        inputPanel.add(signUpButton, gbc);

        this.add(inputPanel, BorderLayout.CENTER);

        signUpButton.addActionListener((ae) -> {

            // change the view to the chat page
            if(submitForm()) MumbleApp.showChatPage();

        });

        loginButton.addActionListener((ae) -> {

            // change the view to the login page
            MumbleApp.showLoginPage();

        });
    }

    /**
     * Adds fields to the given panel using the given gridbag constraints
     * @param panel the panel the fields will be added to as a JPanel
     * @param gbc the gridbag constraints for the layout
     * @param row the row as an int
     * @param label the field label as a String
     * @param input the input as a JComponent
     * @param errorLabel the error message as a JLabel
     */
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent input, JLabel errorLabel){

        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);

        // input field
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(input, gbc);

        if(errorLabel != null){
            gbc.gridx = 1;
            gbc.gridy = row + 1;
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, gbc);
        }
    }

    /**
     * Submit the form and save the user to the database
     * @return whether the form was saved as a boolean
     */
    private boolean submitForm(){

        String unameText = unameField.getText();
        unameText = InputSanitiser.sanitiseUsername(unameText);
        String emailText = emailField.getText();
        emailText = InputSanitiser.sanitiseString(emailText);
        char[] password = passwordField.getPassword();
        char[] confPassword = confirmPasswordField.getPassword();
        String phoneText = phoneField.getText();
        phoneText = InputSanitiser.sanitiseString(phoneText);

        boolean hasErrors = false;

        unameError.setText(" ");
        emailError.setText(" ");
        passwordError.setText(" ");
        confirmPasswordError.setText(" ");

        unameField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border")); // Reset border

        boolean usernameExists = DatabaseManager.usernameExists(unameText);

        if(unameText.isEmpty()){
            unameError.setText("username cannot be empty");
            hasErrors = true;
        }

        if(usernameExists){

            // colour the field border red
            unameField.setBorder(new LineBorder(Color.RED, 2));

            // add the text to the error label
            unameError.setText("That username already exists");

            hasErrors = true;
        }

        if (!phoneText.matches("\\d+")) {
            phoneField.setBorder(new LineBorder(Color.RED, 2));
            hasErrors = true;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if(!emailText.matches(emailRegex)){
            emailError.setText("please enter a valid email address");
            hasErrors = true;
        }
        else if(DatabaseManager.doesEmailExist(emailText)){
            emailError.setText("email already exists, please login");
            hasErrors = true;
        }
        else if(emailText.isEmpty()){
            emailError.setText("email is required");
            hasErrors = true;
        }

        if(password.length < 1){
            passwordError.setText("password is required");
            hasErrors = true;
        }

        if(confPassword.length < 1){
            passwordError.setText("password confirmation is required");
            hasErrors = true;
        }

        if(!new String(password).equals(new String(confPassword))){
            passwordError.setText("passwords do not match");
            confirmPasswordError.setText("passwords do not match");
            hasErrors = true;
        }

        if(hasErrors) return false;

        Date date = new Date();
        String dateText = date.toString();

        String pwHash = hashPassword(password);

        String phoneNumber = (phoneText.isEmpty()) ? null : phoneText;
    
        boolean isRegistered = false;

        try{
            KeyPair rsaKeyPair = CryptoUtils.generateRSAKeyPair();

            PublicKey publicKey = rsaKeyPair.getPublic();
            PrivateKey privateKey = rsaKeyPair.getPrivate();

            // save the public key to the db as a base64 string 
            String pk = CryptoUtils.encodeKey(publicKey);

            // save the user details to the database
            DatabaseHelper.saveUser(unameText, pwHash, emailText, phoneNumber, pk, dateText);  

            int userId = DatabaseManager.getUserId(unameText, pwHash);

            // save the private key to a disk file
            try(FileOutputStream fos = new FileOutputStream("keys/user" + userId + "_private.key")){
                fos.write(privateKey.getEncoded());
            }
            catch(IOException e){
                e.printStackTrace();
            }
            
            isRegistered = true;
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }


        

        Arrays.fill(password, '0');
        Arrays.fill(confPassword, '0');

        unameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");  
        
        if(isRegistered) System.out.println("Account created successfully!");

        return isRegistered;
    }

    /**
     * Returns the hash of the password as a String
     * @param password the password the be hashed as a character array
     * @return the password hash as a String
     */
    private String hashPassword(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
    }

}
