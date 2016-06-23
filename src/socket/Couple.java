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
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/ws")
public class Couple {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
     
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<Couple> webSocketSet = new CopyOnWriteArraySet<Couple>();
    
    
    private static List<Room> rooms = new ArrayList<Room>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
     
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.setSession(session);
        
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
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
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1    
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
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
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }
     
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
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