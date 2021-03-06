package pt.ulisboa.tecnico.sec.tg11;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.tecnico.ulisboa.sec.tg11.SharedResources.MessageManager;
import pt.tecnico.ulisboa.sec.tg11.SharedResources.exceptions.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.UUID;

public class ServerTest extends AbstractTest{

	UUID _userID;
	BigInteger _nonce;
	@Before
	public void setUp() throws Exception {
		super.setUp();

		byte[] msg = _serverRemote.register(keypair.getPublic());
		MessageManager mm = verifyMessage(msg);


		_userID = UUID.fromString(new String(mm.getContent("UUID")));
		byte[] result =  _serverRemote.requestNonce(_userID);

		mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));

	}

	
	@Test
	public void testPut() throws IOException, UserDoesNotExistException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";

		Integer logicalTimestamp = getTimestamp();


		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] msg = _serverRemote.put(manager.generateMessage());

		MessageManager receiveManager = verifyMessage(msg);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));

	}




	@Test
	public void testUpdatePasswordPut() throws IOException, UserDoesNotExistException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password2 = "testPass2";
		String password = "testPass";


		Integer logicalTimestamp = getTimestamp();


		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] msg = _serverRemote.put(manager.generateMessage());

		MessageManager receiveManager = verifyMessage(msg);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));


		logicalTimestamp = getTimestamp();


		byte[] result =  _serverRemote.requestNonce(_userID);
		receiveManager = verifyMessage(result);
		_nonce = new BigInteger(receiveManager.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password2.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] newPut = _serverRemote.put(manager.generateMessage());

		receiveManager = verifyMessage(newPut);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));
	}


	@Test
	public void testPutWithSameTimestamp() throws IOException, UserDoesNotExistException, InvalidRequestException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";
		String password2 = "testPass2";

		Integer logicalTimestamp = getTimestamp();

		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] putResult = _serverRemote.put(manager.generateMessage());

		MessageManager receiveManager = verifyMessage(putResult);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));


		byte[] result =  _serverRemote.requestNonce(_userID);
		MessageManager mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		byte[] passResult =_serverRemote.get(manager.generateMessage());
		receiveManager = verifyMessage(passResult);


		byte[] retrieved = receiveManager.getDecypheredContent("Password");
		assertArrayEquals(password.getBytes(),retrieved);


		result =  _serverRemote.requestNonce(_userID);
		mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password2.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] putResult2 = _serverRemote.put(manager.generateMessage());

		receiveManager = verifyMessage(putResult2);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));


		result =  _serverRemote.requestNonce(_userID);
		mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		passResult =_serverRemote.get(manager.generateMessage());
		receiveManager = verifyMessage(passResult);

		retrieved = receiveManager.getDecypheredContent("Password");
		assertArrayEquals(password2.getBytes(),retrieved);
	}




	@Test
	public void testCreateUsernamePut() throws IOException, UserDoesNotExistException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String username2 = "testUser2";
		String password = "testPass";


		Integer logicalTimestamp = getTimestamp();


		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());
		byte[] msg = _serverRemote.put(manager.generateMessage());

		MessageManager receiveManager = verifyMessage(msg);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));


		logicalTimestamp = getTimestamp();


		byte[] result =  _serverRemote.requestNonce(_userID);
		MessageManager mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));

		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username2.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] newPut = _serverRemote.put(manager.generateMessage());

		receiveManager = verifyMessage(newPut);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));
	}


	@Test (expected = InvalidRequestException.class)
	public void testNonExistentGet() throws IOException, UserDoesNotExistException, InvalidRequestException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException {
		byte[] empty = new byte[0];
		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",empty,secret);
		manager.putHashedContent("username",empty,secret);
		_serverRemote.get(manager.generateMessage());

	}


	@Test
	public void testGet() throws IOException, UserDoesNotExistException, InvalidRequestException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";

		Integer logicalTimestamp = getTimestamp();

		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] putResult = _serverRemote.put(manager.generateMessage());

		MessageManager receiveManager = verifyMessage(putResult);
		Assert.assertEquals("ACK",new String(receiveManager.getContent("Status")));

		byte[] result =  _serverRemote.requestNonce(_userID);
		MessageManager mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		byte[] passResult =_serverRemote.get(manager.generateMessage());
		receiveManager = verifyMessage(passResult);


		byte[] retrieved = receiveManager.getDecypheredContent("Password");
		assertArrayEquals(password.getBytes(),retrieved);
	}

	@Test
	public void testGetUpdated() throws IOException, UserDoesNotExistException, InvalidRequestException, NoSuchPaddingException, SignatureException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";
		String password2 = "pass";


		Integer logicalTimestamp = getTimestamp();

		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		_serverRemote.put(manager.generateMessage());


		byte[] result =  _serverRemote.requestNonce(_userID);
		MessageManager mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));



		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		byte[] result2 =_serverRemote.get(manager.generateMessage());
		MessageManager received = verifyMessage(result2);
		byte[] retrieved = received.getDecypheredContent("Password");



		assertArrayEquals(password.getBytes(),retrieved);


		byte[] result3 =  _serverRemote.requestNonce(_userID);
		MessageManager manager1 = verifyMessage(result3);
		_nonce = new BigInteger(manager1.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password2.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		_serverRemote.put(manager.generateMessage());


		byte[] result4 =  _serverRemote.requestNonce(_userID);
		MessageManager manager2 = verifyMessage(result4);
		_nonce = new BigInteger(manager2.getContent("Nonce"));


		manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		result4 = _serverRemote.get(manager.generateMessage());
		mm = verifyMessage(result4);
		retrieved = mm.getDecypheredContent("Password");
		assertArrayEquals(password2.getBytes(),retrieved);
	}


	@Test(expected = InvalidNonceException.class)
	public void replayAttackTest() throws IOException, UserDoesNotExistException, InvalidRequestException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
			String domain = "www.google.pt";
			String username = "testUser";
			String password = "testPass";

			Integer logicalTimestamp = getTimestamp();

			MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
			manager.putHashedContent("domain",domain.getBytes(),secret);
			manager.putHashedContent("username",username.getBytes(),secret);
			manager.putCipheredContent("password",password.getBytes());
			manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

			_serverRemote.put(manager.generateMessage());
			_serverRemote.put(manager.generateMessage());
	}

	@Test(expected = InvalidSignatureException.class)
	public void tamperMessage() throws BadPaddingException, InvalidSignatureException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, UserDoesNotExistException, InvalidNonceException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";

		Integer logicalTimestamp = getTimestamp();

		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] msg = manager.generateMessage();
		int index = new String(msg).indexOf("username");
		msg[index]='U';

		_serverRemote.put(msg);
	}
	
	@Test(expected = InvalidSignatureException.class)
	public void invalidVerification() throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, IOException, SignatureException, ClassNotFoundException, UserDoesNotExistException, InvalidNonceException, InvalidSignatureException, WrongUserIDException, InvalidAlgorithmParameterException {
		String domain = "www.google.pt";
		String username = "testUser";
		String password = "testPass";
		String password2 = "pass";

		Integer logicalTimestamp = getTimestamp();


		MessageManager manager = new MessageManager(_nonce,_userID,keypair.getPrivate(),keypair.getPublic());
		manager.putHashedContent("domain",domain.getBytes(),secret);
		manager.putHashedContent("username",username.getBytes(),secret);
		manager.putCipheredContent("password",password.getBytes());
		manager.putPlainTextContent("LogicalTimestamp",new String(""+logicalTimestamp).getBytes());

		byte[] msg = _serverRemote.put(manager.generateMessage());
		
		MessageManager fakeServer = new MessageManager(msg);
		fakeServer.setPublicKey(_fakePublicKey);
		fakeServer.verifySignature();
		
	}

	private MessageManager verifyMessage(byte[] msg) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IllegalBlockSizeException, BadPaddingException, InvalidSignatureException, ClassNotFoundException {
		MessageManager mm = new MessageManager(msg,keypair.getPrivate());
		mm.setPublicKey(_serverPublicKey);
		mm.verifySignature();
		return mm;
	}

	private Integer getTimestamp() throws BadPaddingException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, SignatureException, ClassNotFoundException, InvalidSignatureException, InvalidNonceException, WrongUserIDException {
		byte[] result =  _serverRemote.requestNonce(_userID);
		MessageManager mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));


		MessageManager content = new MessageManager(_nonce,_userID, keypair.getPrivate(), keypair.getPublic());
		 result =  _serverRemote.getLatestTimestamp(content.generateMessage());
		mm = verifyMessage(result);

		Integer logicalTimestamp = Integer.parseInt(new String(mm.getContent("LogicalTimestamp")));
		logicalTimestamp+=1;

		result =  _serverRemote.requestNonce(_userID);

		mm = verifyMessage(result);
		_nonce = new BigInteger(mm.getContent("Nonce"));

		return logicalTimestamp;
	}
	
}
