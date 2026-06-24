package model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Lobby implements Serializable {
	
	public final int MAX_LOBBY_SIZE = 6;
	public final int MIN_LOBBY_SIZE = 3;
	
	private static final SecureRandom secRandom = new SecureRandom();
	private static final int LOBBY_CODE_LEN = 6;
	private static final String LOBBY_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	private long id;
	private String code;
	private Slot[] slots;
	private List<Player> spectators;
	
	// when closed the lobby appear inexistent
	private boolean closed;
	
	// if locked when joining it goes into spectator mode
	private boolean locked;
	
	public Lobby(long id, String code) {
		this.id = id;
		this.code = code;
		this.slots = generateSlots();
		this.spectators = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return "[LOBBY OBJECT] ID: " + this.id;
//		return "[LOBBY]  |  id: " + id + "  |  code: " + code + "  |  slots:  " + slots.toString();
	}
	
	private Slot[] generateSlots() {
		Slot[] slots = new Slot[MAX_LOBBY_SIZE];
		for (int i=0; i<MAX_LOBBY_SIZE; i++) {
			slots[i] = new Slot();
		}
		return slots;
	}
	
	public static String generateCode() {
		StringBuilder strBuilder = new StringBuilder();
		for (int i=0; i<LOBBY_CODE_LEN; i++) {
			strBuilder.append(LOBBY_CODE_CHARSET.charAt(secRandom.nextInt(LOBBY_CODE_CHARSET.length())));
		}
		return strBuilder.toString();
	}
	
	public static boolean isCodeValid(String code) {
		return code.matches("^[A-Z0-9]{6}$");
	}
	
	public boolean isFull() {
		return !Arrays.stream(this.slots).anyMatch(slot -> (slot.getStatus() == SlotStatus.EMPTY));
	}
	
	public boolean isEmpty() {
		return Arrays.stream(this.slots)
			    .allMatch(slot ->
			        slot.getStatus() == SlotStatus.EMPTY
			            || (slot.getPlayer() != null && slot.getPlayer().isBot())
			    );
	}
	
	public Slot getFirstEmpty() {
		return Arrays.stream(this.slots)
				.filter(slot -> (slot.getStatus() == SlotStatus.EMPTY))
				.findFirst()
				.orElse(null);
	}
	
	public Slot getPlayerSlot(Player player) {
	    return Arrays.stream(this.slots)
		        .filter(slot -> !slot.isEmpty() && slot.getPlayer().getId() == player.getId())
		        .findFirst()
		        .orElse(null); 
	}
	
	public Slot getPlayerSlot(long playerId) {
	    return Arrays.stream(this.slots)
	        .filter(slot -> !slot.isEmpty() && slot.getPlayer().getId() == playerId)
	        .findFirst()
	        .orElse(null);
	}
	
	public Player getPlayer(long playerId) {
		return Arrays.stream(this.slots)
		        .filter(slot -> !slot.isEmpty() && slot.getPlayer().getId() == playerId)
		        .map(Slot::getPlayer)
		        .findFirst()
		        .orElse(null);
	}
	
	public Player getPlayer(PlayerColor color) {
		return Arrays.stream(this.slots)
		        .filter(slot -> !slot.isEmpty() && slot.getPlayer().getColor() == color)
		        .map(Slot::getPlayer)
		        .findFirst()
		        .orElse(null);
	}
	
	public ArrayList<Player> getPlayers(){
		return new ArrayList<Player>(
			Arrays.stream(this.getSlots())
				.filter(slot -> !slot.isEmpty())
				.map(slot -> slot.getPlayer())
				.toList());
	}
	
	// collectors!!!
	public List<Long> getPlayersIds() {
	    return Arrays.stream(this.getSlots())
	        .filter(slot -> !slot.isEmpty())
	        .map(slot -> slot.getPlayer().getId())
	        .collect(Collectors.toList());
	}
	
	public void add(Player player) {
		Slot slot = getFirstEmpty();
		slot.setPlayer(player);
		slot.setStatus(SlotStatus.CONNECTED);
	}
	
	public void remove(Player player) {
		Slot slot = getPlayerSlot(player.getId());
		
		if (slot==null) {
			throw new NullPointerException("no player with id: " + player.getId() + " in the lobby: " + this.code);
		}
		
		slot.setPlayer(null);
		slot.setStatus(SlotStatus.EMPTY);
	}
	
	public void remove(long playerId) {
		Slot slot = getPlayerSlot(playerId);
		
		if (slot==null) {
			throw new NullPointerException("no player with id: " + id + " in the lobby: " + this.code);
		}
		
		slot.setPlayer(null);
		slot.setStatus(SlotStatus.EMPTY);
	}
	
	public void removeAll() {
		Arrays.stream(this.slots)
			.forEach(slot -> {
				slot.setPlayer(null);
				slot.setStatus(SlotStatus.EMPTY);
			});
	}
	
	// remove first avaiable bot
	public void removeBot() {
		long id = -1;
		for (Slot slot : this.slots) {
			Player player = slot.getPlayer();
			if(player != null && player.isBot()) {
				id = player.getId();
				break;
			}
		}
		if (id > 0) {
			remove(id);
		}
		
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public Slot[] getSlots() {
		return this.slots;
	}
	
	public Slot getSlot(int index) {
		if (index < 0 || index > MAX_LOBBY_SIZE) {
			throw new ArrayIndexOutOfBoundsException("slot index out of bounds");
		} else {
			return this.slots[index];
		}
	}
	
	public int getPlayerCount() {
	    return (int)Arrays.stream(this.slots)
	        .filter(slot -> !slot.isEmpty())
	        .count();
	}
	
	public boolean canStart() {
	    long readyCount = Arrays.stream(this.slots)
	        .filter(slot -> slot.getStatus() == SlotStatus.READY)
	        .count();
	    boolean can = (readyCount >= MIN_LOBBY_SIZE && allPlayersReady());
	    System.out.println("CAN START: " + can);
	    return can;
	}
	
	private boolean allPlayersReady() {
		return Arrays.stream(this.slots)
			    .allMatch(slot ->
			        slot.getPlayer() == null
			            || slot.getStatus() == SlotStatus.READY
			    );
	}
	
	public boolean shouldAutoStart() {
	    return isFull() && Arrays.stream(this.slots)
	        .allMatch(slot -> slot.getStatus() == SlotStatus.READY);
	}
	
	public void close() {
		this.closed = true;
	}
	
	public void open() {
		this.closed = false;
	}
	
	public void lock() {
		this.locked = true;
	}
	
	public void unlock() {
		this.locked = false;
	}
	
	/**
	 * for private lobby
	 * @return
	 */
	public boolean isClosed() {
		return this.closed;
	}
	
	public boolean isLocked() {
		return this.locked;
	}
	
	public PlayerColor getRandomFreeColor() {
	    List<PlayerColor> taken = Arrays.stream(this.slots)
	        .filter(slot -> !slot.isEmpty() && slot.getPlayer().getColor() != null)
	        .map(slot -> slot.getPlayer().getColor())
	        .collect(Collectors.toList());
 
	    List<PlayerColor> free = Arrays.stream(PlayerColor.values())
	        .filter(c -> c != PlayerColor.NOT_SELECTED && !taken.contains(c))
	        .collect(Collectors.toList());
 
	    if (free.isEmpty()) return PlayerColor.NOT_SELECTED;
 
	    return free.get(new SecureRandom().nextInt(free.size()));
	}

	
	// #### SPECTATORS ####################################
	
	public void addSpectator(Player player) {
		this.spectators.add(player);
	}
	
	public void removeSpectator(Player player) {	
		this.spectators.remove(player);
	}
	
	// overload
	public void removeSpectator(long id) {
		Player spectator = this.spectators.stream()
								.filter(player -> player.getId() == id)
								.findFirst()
								.orElse(null);
		
		if (spectator==null) {
			throw new NullPointerException("no spectator with id: " + id + " in the lobby: " + this.code);
		}
		
		this.spectators.remove(spectator);
		
	}
	
	public void removeAllSpectators() {
		this.spectators.clear();
	}
	
	
	public Player getSpectator(long id) {
		return this.spectators.stream()
			.filter(player -> player.getId() == id)
			.findFirst()
			.orElse(null);
	}
	
	public List<Long> getSpectatorsIds() {
	    return this.spectators.stream()
	        .map(Player::getId)
	        .collect(Collectors.toList());
	}
	
	public boolean isSpectator(long id) {
		return getSpectatorsIds().contains(id);
	}
	
	
	
	// #### devs ##########################################
	
	public void setCode(String code) {
		System.out.println("SETCODE: " + code);
		if (isCodeValid(code)) {
			this.code = code;
		}
	}
	


	public void setLocked(boolean value) {
		this.locked = value;
	}
	
}
