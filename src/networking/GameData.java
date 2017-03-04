package networking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameData implements Serializable {
	
	public enum Tag {
		START, POSITION
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2012501718012423753L;
	private Tag tag;
	private List<CharacterInfo> characters;
	private CharacterInfo info;
	
	public GameData(List<CharacterInfo> characters) {
		this.characters = characters;
	}
	
	public GameData(CharacterInfo info) {
		this.info = info;
	}
	
	public List<CharacterInfo> getCharactersList() {
		return characters;
	}
	
	public CharacterInfo getInfo() {
		return this.info;
	}
	
	public Tag getTag() {
		return tag;
	}
	
}
