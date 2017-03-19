package networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import networking.GameData.Tag;
import resources.Resources.Mode;
import ui.UIRes;

public class NetworkingClient extends Thread {
	
//	public static void main(String[] args) {
//		NetworkingClient c = new NetworkingClient("localhost", UIRes.username, new ConnectionDataModel(new ConnectionData()));
//		c.start();
//	}

	//private Client client;
	static int UDPport = 27970;
	static int TCPport = 27970;
	private String ip;
	private String name;
	static boolean messageReceived = false;
	private ConnectionDataModel cModel;
	
	public NetworkingClient(String ip, String name, ConnectionDataModel cModel) {
		this.ip = ip;
		this.name = name;
		this.cModel = cModel;
	}
	
	public void run() {
		
		Client client = new Client();
		
		registerClasses(client);
		
		new Thread(client).start();
		
		try {
			client.connect(5000, ip, UDPport, TCPport);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		client.addListener(new ClientListener(cModel, client));
		
		System.out.print("Client is now waiting for a packet.");
		
		Message m = new Message();
	    m.setCommand(Command.MESSAGE);
	    m.setMessage(name);
	    client.sendTCP(m);
		
		while(!messageReceived) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void registerClasses(Client client) {
		  Kryo kryo = client.getKryo();
		  kryo.register(Message.class);
		  kryo.register(Command.class);
		  kryo.register(Note.class);
		  kryo.register(CharacterInfo.class);
		  kryo.register(Session.class);
		  kryo.register(Empty.class);
		  kryo.register(ClientInformation.class);
		  kryo.register(LinkedBlockingQueue.class);
		  kryo.register(java.util.concurrent.ConcurrentHashMap.class);
		  kryo.register(GameData.class);
		  kryo.register(ArrayList.class);
		  kryo.register(Tag.class);
		  kryo.register(resources.Map.World.class);
		  kryo.register(Mode.class);
		  kryo.register(resources.Character.Class.class);
	}
}
