package gamemodes;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import graphics.Graphics;
import physics.Physics;
import resources.Character;
import resources.Map;
import resources.Resources;
import resources.Resources.Mode;
import ui.UIRes;

/**
 * Play until only one player remains.
 * 
 * @author Luke
 *
 */
public class LastManStanding extends Thread implements GameModeFFA {

	private int maxLives;//
	private boolean gameOver = false;
	private Character winner;
	private Resources resources;//
	private boolean isServer = false;//
	private boolean singlePlayer = false;

	/**
	 * Create a new last man standing game mode.
	 * 
	 * @param resources
	 *            The resources object being used for the game.
	 * @param maxLives
	 *            The maximum number of lives for the game.
	 */
	public LastManStanding(Resources resources, int maxLives) {
		this.maxLives = maxLives;
		this.resources = resources;

		// Set up game
		setAllLives(maxLives);
		randomRespawn();

		resources.mode = Mode.LastManStanding;
		resources.gamemode = this;
	}

	/**
	 * Create a new last man standing game mode.
	 * 
	 * @param resources
	 *            The resources object being used for the game.
	 * @param maxLives
	 *            The maximum number of lives for the game.
	 */
	public LastManStanding(Resources resources, int maxLives, boolean isServer, boolean singlePlayer) {
		this.maxLives = maxLives;
		this.resources = resources;
		this.singlePlayer = singlePlayer;

		// Set up game
		setAllLives(maxLives);
		randomRespawn();

		resources.mode = Mode.LastManStanding;
		resources.gamemode = this;
		this.isServer = isServer;
	}

	/*
	 * Run the logic of this game mode.
	 */
	public void run() {
		// start the game
		Physics p = new Physics(resources, false);
		if (!isServer) {
			Graphics g = new Graphics(resources, null, false);
			SwingUtilities.invokeLater(g);

		}

		if (singlePlayer) {

			try {
				Thread.sleep(1000);
				resources.setCountdown(2);
				if (!Resources.silent) UIRes.audioPlayer.play();
				Thread.sleep(1000);
				resources.setCountdown(1);
				if (!Resources.silent) UIRes.audioPlayer.play();
				Thread.sleep(1000);
				resources.setCountdown(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		p.start();
		
		if (!Resources.silent)
		{
			if (resources.getMap().getWorldType() == Map.World.SPACE) {
				resources.getMusicPlayer().changePlaylist("ultrastorm");
			} else {
				resources.getMusicPlayer().changePlaylist("frog");
			}
			resources.getMusicPlayer().resumeMusic();
		}

		while (!isGameOver()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Game has ended
		p.pause();
		System.out.println("WE HAVE A WINNER");
		System.out.println("Player " + winner.getPlayerNumber() + " survived the longest and reached a score of "
				+ winner.getScore() + "!");
		ArrayList<Character> scores = resources.getOrderedScores();
		for (Character c : scores) {
			System.out.print("Player " + c.getPlayerNumber() + " had score " + c.getScore() + ", ");
		}
		System.out.println();
		for (Character c : getOrderedTimesOfDeath()) {
			if (c.getLives() == 0) {
				System.out.println(
						"Player " + c.getPlayerNumber() + " survived " + c.getTimeOfDeath() / 100 + " seconds.");
			}
		}
		resources.setGameOver(true);
	}

	/**
	 * Set the number of lives for all players to the specified value.
	 * 
	 * @param n
	 *            Number of lives
	 */
	public void setAllLives(int n) {
		for (Character c : resources.getPlayerList()) {
			c.setLives(n);
		}
	}

	/**
	 * @return maxLives
	 */
	public int getMaxLives() {
		return maxLives;
	}

	/**
	 * Get the total number of lives of all players
	 * 
	 * @return The combined number of lives of all players
	 */
	public int getCombinedLives() {
		int total = 0;
		for (Character c : resources.getPlayerList()) {
			total += c.getLives();
		}
		return total;
	}

	/**
	 * @return Whether the game has ended
	 */
	public boolean isGameOver() {
		checkWinner();
		return gameOver;
	}

	/**
	 * @return The winning character
	 */
	public ArrayList<Character> getWinners() {
		checkWinner();
		ArrayList<Character> winners = new ArrayList<>();
		winners.add(winner);
		return winners;
	}

	/**
	 * Find the winner if the game is over.
	 */
	private void checkWinner() {
		if (playersRemaining() == 1) {
			gameOver = true;
			findWinner();
		}
	}

	/**
	 * Finds the last remaining player in the game
	 */
	private void findWinner() {
		for (Character c : resources.getPlayerList()) {
			if (c.getLives() > 0) {
				winner = c;
				break;
			}
		}
	}

	/**
	 * @return The number of players alive
	 */
	public int playersRemaining() {
		int remaining = 0;
		for (Character c : resources.getPlayerList()) {
			if (c.getLives() > 0) {
				remaining++;
			}
		}
		return remaining;
	}

	/**
	 * @return An ArrayList of each character's time of death, in ascending
	 *         order.
	 */
	public ArrayList<Character> getOrderedTimesOfDeath() {
		ArrayList<Character> times = new ArrayList<Character>();
		times.addAll(resources.getPlayerList());
		times.sort((a, b) -> (a.getTimeOfDeath() > b.getTimeOfDeath()) ? -1
				: (a.getTimeOfDeath() < b.getTimeOfDeath()) ? 1 : 0);
		return times;
	}

	@Override
	public void resetLives() {
		setAllLives(maxLives);
	}

	@Override
	public void randomRespawn() {
		for (Character c : resources.getPlayerList()) {
			resources.getMap().spawn(c);
		}
	}
}
