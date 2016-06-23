package socket;

import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.Session;

public class Room {
	private int num;
	private String id;
	private final int max = 2;
	private CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
	
	public Room(String id,Session session){
		this.id = id;
		this.sessions.add(session);
	}
	
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public  CopyOnWriteArraySet<Session> getSessions() {
		return sessions;
	}
	public  void setSessions(CopyOnWriteArraySet<Session> sessions) {
		this.sessions = sessions;
	}

	public int getMax() {
		return max;
	}
}
