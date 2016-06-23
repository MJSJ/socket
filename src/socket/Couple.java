package socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
 





import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
//��ע������ָ��һ��URI���ͻ��˿���ͨ�����URI�����ӵ�WebSocket������Servlet��ע��mapping��������web.xml�����á�
@ServerEndpoint("/ws")
public class Couple {
    //��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
    private static int onlineCount = 0;
     
    //concurrent�����̰߳�ȫSet���������ÿ���ͻ��˶�Ӧ��MyWebSocket������Ҫʵ�ַ�����뵥һ�ͻ���ͨ�ŵĻ�������ʹ��Map����ţ�����Key����Ϊ�û���ʶ
    private static CopyOnWriteArraySet<Couple> webSocketSet = new CopyOnWriteArraySet<Couple>();
    
    
    private static List<Room> rooms = new ArrayList<Room>();
     
    //��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
    private Session session;
     
    /**
     * ���ӽ����ɹ����õķ���
     * @param session  ��ѡ�Ĳ�����sessionΪ��ĳ���ͻ��˵����ӻỰ����Ҫͨ���������ͻ��˷�������
     */
    @OnOpen
    public void onOpen(Session session){
        this.setSession(session);
        
        webSocketSet.add(this);     //����set��
        addOnlineCount();           //��������1
        System.out.println("�������Ӽ��룡��ǰ��������Ϊ" + getOnlineCount());
    }
    
    
    public void registeRoom(String message,Session session){
    	String[] msgs = message.split(";");
    	String id = "0";
    	for(String msg: msgs){
    		if(msg.indexOf("id")>0){
    			id = msg.split("=")[1];
    		}
    	}
    	System.out.println("roomId "+id);
    	
    	for(Room room:rooms){
    		
    		if(room.getId().equalsIgnoreCase(id)){
    			if(room.getNum()<room.getMax()){
    				room.setNum(room.getNum()+1);
        			room.getSessions().add(session);
        			return;
    			}else{
    				rooms.add(new Room(Long.toString(new Date().getTime()),session));
        		}
    		}
    	}
    	
    	//new room 
    	rooms.add(new Room(id,session));
    }
     
    /**
     * ���ӹرյ��õķ���
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);  //��set��ɾ��
        subOnlineCount();           //��������1    
        System.out.println("��һ���ӹرգ���ǰ��������Ϊ" + getOnlineCount());
    }
     
    /**
     * �յ��ͻ�����Ϣ����õķ���
     * @param message �ͻ��˷��͹�������Ϣ
     * @param session ��ѡ�Ĳ���
     */
    @OnMessage
    public void onMessage(String message, Session session) {
    	if(message.indexOf("regist")>0){
    		this.registeRoom(message,session);
    	}else{
    		String[] msgs = message.split(";");
        	String id = "0";
        	for(String msg: msgs){
        		if(msg.indexOf("id")>0){
        			id = msg.split("=")[1];
        		}
        	}
        	
        	try {
				this.sendMessage(id,message);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
    	}
    }
     
    /**
     * ��������ʱ����
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("��������");
        error.printStackTrace();
    }
     
    /**
     * ������������漸��������һ����û����ע�⣬�Ǹ����Լ���Ҫ��ӵķ�����
     * @param message
     * @throws IOException
     */
    public void sendMessage(String id,String message) throws IOException{
        
        for(Room room: rooms){
	   		 if(room.getId().equalsIgnoreCase(id)){
	   			 for(Session session2: room.getSessions()){
	   				 try{
	   					 session2.getBasicRemote().sendText(message);
	   				 }catch (IOException e) {
	                        e.printStackTrace();
	                        continue;
	                    }
	   			 }
	   			 break;
	   		 }
        }
    }
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
    	Couple.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
    	Couple.onlineCount--;
    }


	public static List<Room> getRooms() {
		return rooms;
	}


	public static void setRooms(List<Room> rooms) {
		Couple.rooms = rooms;
	}


	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}
}