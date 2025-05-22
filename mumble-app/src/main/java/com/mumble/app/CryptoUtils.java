package com.mumble.app;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * This class provides methods to generate and store public and private keys
 * to be used in end to end encryption of user data
 */
public class CryptoUtils {
    
    /**
     * Generates a new private and public key using the RSA algorithm
     * @return the public and private keys as a KeyPair
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException{

        // generate the keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        // initialise the keypair with 2048 bits
        kpg.initialize(2048); // strong encryption

        return kpg.generateKeyPair();

    }

    
    /**
     * Encodes a key (binary) to String to be used in applications
     * @param key the key to be encoded to String as a Key
     * @return the encoded String 
     */
    public static String encodeKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Decodes a base64 String into bytes then returns an encoded public key for that String
     * @param base64 the String to be decoded into a public key
     * @return the decoded PublicKey
     * @throws Exception
     */
    public static PublicKey decodePublicKey(String base64) throws Exception{

        // decode the String into a byte array
        byte[] decodedBase64 = Base64.getDecoder().decode(base64);

        // encode the byte array using the X.509 standard specification
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBase64);

        // genetate a public key using the X.509 encoded specification
        return KeyFactory.getInstance("RSA").generatePublic(spec);  // KeyFactory converts the KeySpec into a usable key
    }

    /**
     * Returns a new PrivateKey based on a decoded base64 String using the PKCS8 standard encoding
     * @param base64 the base64 String to be decoded into a private key
     * @return the decoded PrivateKey 
     * @throws Exception
     */
    public static PrivateKey decodePrivateKey(String base64) throws Exception{
        
        // decode the base64 Sring into a byte array
        byte[] decodedBase64 = Base64.getDecoder().decode(base64);

        // encode the byte array using the PKCS8 standard
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedBase64);

        // use the KeyFactory to generate and return a new PrivateKey from the PKCS8 encoded specification
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * Retrieves a users public key using a given username
     * @param username the username to be queried as a String
     * @return the users PublicKey
     * @throws Exception
     */
    public static PublicKey getPublicKeyByUsername(String username) throws Exception{

        String sql = "SELECT public_key FROM users WHERE username = ?";

        // connect to the database and safely query using prepared statements
        try(Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){

                // decode the base64 String to a byte array
                byte[] decoded = Base64.getDecoder().decode(rs.getString("public_key"));

                // encode the decoded byte array using the X509 standard
               X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

                // generate the public key using a KeyFactory
                return KeyFactory.getInstance("RSA").generatePublic(spec);
            }
        }
        throw new IllegalArgumentException("User not found or missing public key.");
    }

    /**
     * Returns an encrypted byte array from a given String using a given public key
     * @param plainText the given String to be encrypted
     * @param publicKey the given PublicKey to be used in the encryption
     * @return the encrypted data as a byte array
     * @throws Exception
     */
    public static byte[] encryptMessage(String plainText, PublicKey publicKey) throws Exception{

        // use a cipher to encrypt the data using the RSA algorithm
        Cipher cipher = Cipher.getInstance("RSA");

        // initialise the cipher with the public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // encrypt and return the data
        return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String encryptToBase64(String eMessage, PublicKey publicKey) throws Exception{
        byte[] encryptedBytes = encryptMessage(eMessage, publicKey);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    

}
