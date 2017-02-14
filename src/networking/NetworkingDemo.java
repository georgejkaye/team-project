package networking;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import graphics.Graphics;
import physics.Physics;
import resources.Character;
import resources.Map;
import resources.MapReader;
import resources.Resources;

/**
 * I try and smash graphics with physics. It works ish
 */

public class NetworkingDemo {

	public static void startGame(ConnectionDataModel cModel) {
		
		Character newPlayer;
		Random r = new Random();
		int x;
		int y;
		List<ClientInformation> clients = cModel.getSession(cModel.getSessionId()).getAllClients();
		List<CharacterInfo> charactersList = cModel.getCharactersList();
		CharacterInfo info;
		for(int i=0; i<clients.size(); i++) {
			x = r.nextInt(1200);
			y = r.nextInt(675);
			int id = clients.get(i).getId();
			newPlayer = new Character(Character.Class.ELF);
			newPlayer.setX(x);
			newPlayer.setY(y);
			Resources.playerList.add(newPlayer);
			cModel.getCharacters().put(id, newPlayer);
			info = new CharacterInfo(id, x, y);
			charactersList.add(info);
		}
		
		cModel.setCharactersList(charactersList);
		
		// make the map the default just in case the following fails
		Map.Tile[][] tiles = Resources.default_map;	
		MapReader mr = new MapReader();	
		try
		{
			tiles = mr.readMap("./resources/maps/map1.csv");
			System.out.println("I guess it worked then");
		}
		catch (IOException e)
		{
			System.out.println("File not found");
			e.printStackTrace();
			
		}
		
		Resources.map = new Map(1200, 675, tiles, Map.World.CAVE);

		// create physics thread
		Physics p = new Physics();
		p.start();

		// create ui thread
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Graphics g = new Graphics(Resources.playerList, Resources.map);
				g.start();
			}
		});
	}
	
	public static void setGame(ConnectionDataModel cModel, GameData gameData) {
		
		Character newPlayer;
		int x;
		int y;
		int id;
		List<CharacterInfo> charactersList = gameData.getCharactersList();
		CharacterInfo info;
		for(int i=0; i<charactersList.size(); i++) {
			info = charactersList.get(i);
			x = info.getX();
			y = info.getY();
		    id = info.getId();
			newPlayer = new Character(Character.Class.ELF);
			newPlayer.setX(x);
			newPlayer.setY(y);
			Resources.playerList.add(newPlayer);
			cModel.getCharacters().put(id, newPlayer);
		}
		
		cModel.setCharactersList(charactersList);
		
		// make the map the default just in case the following fails
		Map.Tile[][] tiles = Resources.default_map;	
		MapReader mr = new MapReader();	
		try
		{
			tiles = mr.readMap("./resources/maps/map1.csv");
			System.out.println("I guess it worked then");
		}
		catch (IOException e)
		{
			System.out.println("File not found");
			e.printStackTrace();
			
		}
		
		Resources.map = new Map(1200, 675, tiles, Map.World.CAVE);

		// create physics thread
		Physics p = new Physics();
		p.start();

		// create ui thread
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Graphics g = new Graphics(Resources.playerList, Resources.map);
				g.start();
			}
		});
	}
}