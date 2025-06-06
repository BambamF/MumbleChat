package com.mumble.app.Utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.Base64;

import javax.crypto.Cipher;

import com.mumble.app.DB.DatabaseManager;

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
     * Loads a private key from disk
     * @param username the username associated with the private key file
     * @return the private key reconstructed using the PKCS8 standard
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String username) throws Exception{
        Path path = Paths.get("keys", "private_" + username + ".key");
        byte[] keyBytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
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

    /**
     * Encrypts a message to base64
     * @param eMessage the message as a String
     * @param publicKey the public key to be used 
     * @return the base64 String
     * @throws Exception
     */
    public static String encryptToBase64(String eMessage, PublicKey publicKey) throws Exception{
        byte[] encryptedBytes = encryptMessage(eMessage, publicKey);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts a message
     * @param message the message to be decrypted as a String
     * @param pk the private key to be used in the decryption
     * @return the decrypted message bytes
     * @throws Exception
     */
    public static byte[] decryptMessage(String message, PrivateKey pk) throws Exception{

        // decode the message into a byte array
        byte[] decodedBytes = Base64.getDecoder().decode(message);

        // use a cipher to decrypy the message
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pk);
        return cipher.doFinal(decodedBytes);
    }

    /**
     * Returns the signed bytes of a message
     * @param message the message to be signed as a String
     * @param privateKey the private key to be used in the signing
     * @return the signed message as a byte array
     * @throws Exception
     */
    public static byte[] signMessage(String message, PrivateKey privateKey) throws Exception{

        // get the signature instance using the SHAwithRSA algorithm
        Signature signature = Signature.getInstance("SHA256withRSA");

        // initialise the signature with the private key
        signature.initSign(privateKey);

        // update the signature with the message bytes
        signature.update(message.getBytes());

        // return the signed message
        return signature.sign();
    }

    /**
     * Verifies congruence between a message and the signature sent with it
     * @param message the message to be verified as a String
     * @param signatureBytes the signature to be used in the verification as a byte array
     * @param publicKey the public key to be used in the verification
     * @return whether the message and message signature match as a boolean
     * @throws Exception
     */
    public static boolean verifySignature(String message, byte[] signatureBytes, PublicKey publicKey) throws Exception{

        // get the signature instance
        Signature signature = Signature.getInstance("SHAwithRSA");

        // initialise the signature for verification
        signature.initVerify(publicKey);

        // update the signature with the message bytes
        signature.update(message.getBytes());

        // return the comparison between the signed message and the signature bytes
        return signature.verify(signatureBytes);
    }

}
