import org.junit.*;

import pt.tecnico.ulisboa.sec.tg11.SharedResources.MessageManager;
import pt.tecnico.ulisboa.sec.tg11.SharedResources.PWMInterface;
import pt.tecnico.ulisboa.sec.tg11.SharedResources.exceptions.*;
import pt.ulisboa.tecnico.sec.tg11.PwmLib;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by trosado on 01/03/17.
 */
public class PwmLibTest {

    private static final String PATH_TO_SERVER_CERT = "./src/main/resources/server1.cer";
    private static final String PATH_TO_RSAKEYSTORE = "./src/main/resources/keystore.jks";
    private static final String PATH_TO_RSAKEYSTORE2 = "./src/main/resources/user2.jks";
    private static final String CLIENT_PUBLIC_KEY = "privatekey";
    private static KeyStore _keystore;
    private static PwmLib _pwmlib;
    private static String _keystorepw;
    private static UUID _userID;
    private static Key _privateKey;
    private static Key _publicKey;
    private static PWMInterface _server;
    private static UUID _userID2;
    private static PwmLib _pwmlib2;
    private static KeyStore _keystore2;
    private static Key _privateKey2;
    private static Key _publicKey2;
    private static PublicKey _serverPublicKey;
    
    @BeforeClass
    public static void setUp() throws Throwable {

        /* http://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html */

        _pwmlib = new PwmLib();
        _keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        _keystorepw = "1234567";
        // get user password and file input stream
        char[] password = _keystorepw.toCharArray();
        _keystore.load(new FileInputStream(PATH_TO_RSAKEYSTORE), password);

        _pwmlib.init(_keystore,password);
        _userID = _pwmlib.register_user();


        _keystore2 = KeyStore.getInstance(KeyStore.getDefaultType());
        _keystore2.load(new FileInputStream(PATH_TO_RSAKEYSTORE2), password);
        _pwmlib2 = new PwmLib();
        _pwmlib2.init(_keystore2,password);

        _userID2 = _pwmlib2.register_user();
        _privateKey2 = (PrivateKey) _keystore2.getKey(CLIENT_PUBLIC_KEY, password);
        _publicKey2 = _keystore2.getCertificate(CLIENT_PUBLIC_KEY).getPublicKey();

       
    }
    

    @AfterClass
    public static void tearDown() throws Exception {
        _pwmlib.close();
    }


    @Test
    public void save_password() throws Throwable {
        String domain = "www.google.pt";
        String username = "testUser";
        String password = "testPass";

        _pwmlib.save_password(_userID,domain.getBytes(),username.getBytes(),password.getBytes());
    }

    @Test
    public void retrieve_password() throws Throwable {
        String domain = "www.google.pt";
        String username = "testUser";
        String password = "testPass";
        byte [] pw = _pwmlib.retrieve_password(_userID,domain.getBytes(), username.getBytes());

        Assert.assertArrayEquals(password.getBytes(),pw);
    }

    @Test
    public void retrive_altered_password() throws Throwable {
        String domain = "www.google.pt";
        String username = "testUser";
        String password = "testPass";
        _pwmlib.save_password(_userID,domain.getBytes(),username.getBytes(),password.getBytes());
        
        String password2 = "testPass2";
        _pwmlib.save_password(_userID,domain.getBytes(),username.getBytes(),password2.getBytes());


        byte [] pw = _pwmlib.retrieve_password(_userID,domain.getBytes(), username.getBytes());


        Assert.assertArrayEquals(password2.getBytes(),pw);
    }

    
    @Test(expected = InvalidRequestException.class)
    public void unexisting_pass() throws Throwable {
    	
    	String domain = "www.google.pt";
    	String username = "juanito";
    	
    	_pwmlib.retrieve_password(_userID, domain.getBytes(), username.getBytes());
    	
    }
    
    @Test(expected = UserAlreadyExistsException.class)
    public void wrong_register() throws Throwable {
    	_pwmlib.register_user();
    }
    
    @Test(expected = WrongUserIDException.class)
    public void retrieve_invalid_user() throws Throwable {
    	UUID u = UUID.randomUUID();
    	_pwmlib.retrieve_password(u, "domain".getBytes(), "username".getBytes());
    }
    
    @Test(expected = InvalidSignatureException.class)
    public void impersonate_request() throws Throwable {
    	byte[] domain = "www.google.pt".getBytes();
    	byte[] username = "juanito".getBytes();
    	byte[] password = "mypass".getBytes();
    	
    	_pwmlib.save_password(_userID, domain, username, password);
    	_pwmlib2.retrieve_password(_userID, domain, username);
    	
    }
    
    @Test(expected = InvalidSignatureException.class)
    public void impersonate_put() throws Throwable {
    	byte[] domain = "www.google.pt".getBytes();
    	byte[] username = "juanito".getBytes();
    	byte[] password = "mypass".getBytes();
    	
    	_pwmlib.save_password(_userID, domain, username, password);
    	_pwmlib2.save_password(_userID, domain, username, password);
    	
    }
    
    @Test(expected = WrongUserIDException.class)
    public void put_invalid_user() throws Throwable {
    	UUID u = UUID.randomUUID();
    	_pwmlib.save_password(u, "domain".getBytes(), "username".getBytes(), "pass".getBytes());
    }





}