import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.*;
import java.util.List;
class RegisterThread extends Thread
{
	private InputStream in;
	private OutputStream out;
	private BufferedReader br;
	private List<Socket> arrList;
	private Socket s;
	private List<String> arrName;
	private HashMap<String,Socket> nameAndSocket;
	public RegisterThread(Socket s,Vector<Socket> arrList,Vector<String> arrName,HashMap<String,Socket>nameAndSocket) throws IOException
	{
		this.arrName=arrName;
		this.nameAndSocket=nameAndSocket;
		this.s=s;
		this.arrList=arrList;
		in=s.getInputStream();
		br=new BufferedReader(new InputStreamReader(in));
		this.start();
	}
	public void run()
	{
		
		while(true)
		{
			String str="";	
				try
				{
				str=br.readLine();
				}
				catch(Exception e)
				{
					return;
				}
				if(str.equals("") || str.equals("\n"))
					continue;
				String temp="";
				System.out.println(str);
				try
				{
					temp=str.substring(0,6);
				}
				catch(StringIndexOutOfBoundsException se)
				{
					System.out.println(se);
				}
				if(temp.equals("delete"))
				{
					synchronized (arrName)
					{
					System.out.println("Delete");
					String del=str.substring(6,str.length());
					Socket ll=null;
					try
					{
					ll=nameAndSocket.get(del);
					OutputStream out=ll.getOutputStream();
					PrintWriter pr=new PrintWriter(out,true);
					pr.println("Delete"+str);
					}
					catch(Exception e)
					{
						System.out.println("new Error");
					}
					arrName.remove(del);
					nameAndSocket.remove(del);
					arrList.remove(ll);
					return;
					}
				}
				if(temp.equals("@@@---"))
				{
					str=str.substring(6,str.length());
					int index=str.lastIndexOf("@@@---");
					temp=str.substring(0,index);
					synchronized(arrName)
					{
						for(int m=0;m<arrName.size();m++)
						{
							if(arrName.get(m).equals(temp))
							{
								return;
							}
						}
					}
					nameAndSocket.put(temp,s);
					arrName.add(temp);
					System.out.println("new user adding to the arrName"+temp);
				}
				else if(temp.equals("privat"))
				{
					//str=str.substring(6,str.length());
					int index1=str.indexOf(":");
					int index2=str.indexOf(",");
					Iterator k=nameAndSocket.keySet().iterator();
					System.out.println("private Message so traversing ");
					while(k.hasNext())
					{
						System.out.println((String)k.next());
					}
					temp=str.substring(index1+1,index2);
					System.out.println("private Message recieved for"+temp);
					System.out.println(nameAndSocket.containsKey(temp));
					try
					{
						Socket ss1=nameAndSocket.get(temp);
						if(ss1==null)
						{
							System.out.println("null in socket");
						}
						OutputStream out=ss1.getOutputStream();
						PrintWriter pr=new PrintWriter(out,true);
						pr.println(str);
					}
					catch(IOException io)
					{

					}
				}
				else
				{
					Iterator ii=arrList.iterator();
					while(ii.hasNext())
					{
						Socket s1=(Socket)ii.next();
						{
							try
							{
							out=s1.getOutputStream();
							PrintWriter pr=new PrintWriter(out,true);
							pr.println(str);
							}
							catch(IOException io)
							{
								System.out.println("Socketdel");
							}
						}
					}
				}
		}
	}
}
class OnlineThread extends Thread
{
	Vector<String> arrName;
	Vector<Socket> arrSocket;
	OnlineThread(Vector<String> arrName,Vector<Socket> arrSocket)
	{	
		this.arrName=arrName;	
		this.arrSocket=arrSocket;
		this.start();
	}
	public void run()
	{
		while(true)
		{
			
			Iterator i=arrName.iterator();
			String data="";
			try
			{
			while(i.hasNext())
			{
				data=data+(String)i.next()+",";
			}
			}
			catch(ConcurrentModificationException e)
			{
				System.out.println("Concuent but runing");
			}
			data="online"+data;
			for(int ii=0;ii<arrSocket.size();ii++)
			{
				Socket s=arrSocket.get(ii);
				try
				{
					OutputStream out=s.getOutputStream();
					PrintWriter pr=new PrintWriter(out,true);
					pr.println(data);
				}
				catch(IOException E)
				{
					System.out.println(E);					
				}
			}
		}
	}
}
class Server
{
	private ServerSocket ss;
	private Socket s;
	private Vector<Socket> arrList=new Vector<Socket>();
	private Vector<String> arrName=new Vector<String>();
	private HashMap<String,Socket> nameAndSocket=new HashMap<String,Socket>();
	public Server() throws IOException
	{
		ss=new ServerSocket(2020);	
		new OnlineThread(arrName,arrList);
		while(true)
		{
			s=ss.accept();
			arrList.add(s);
			String str="";
			new RegisterThread(s,arrList,arrName,nameAndSocket);
		}
	}
	public static void main(String arg[]) throws IOException
	{
		new Server();
	}
}
