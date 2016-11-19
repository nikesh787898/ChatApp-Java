package Client;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.*;
import java.util.List;
class SetFrame
{
	public SetFrame(JFrame f)
	{
		f.setSize(600,500);
		f.setResizable(true);
		f.setLayout(new BorderLayout());
	}
}
class Receive extends Thread 
{	
	private Socket s;
	private JTextArea ta;
	private InputStream in;
	private JList jlist;
	private BufferedReader br;
	private String name;
	private HashMap<String,JTextArea> nameAndTextArea;
	private HashMap<String,JFrame> nameAndJFrame;
	private HashMap<JTextField,String> jtextfieldAndName;
	private ActionTextFieldPrivate actionTextField;
	private HashMap<JTextField,JTextArea> textfieldAndTextArea;
	public Receive(JTextArea ta,Socket s,String name,JList jlist,HashMap<String,JTextArea> nameAndTextArea,HashMap<String,JFrame> nameAndJFrame,HashMap<JTextField,String> jtextfieldAndName,ActionTextFieldPrivate actionTextField,HashMap<JTextField,JTextArea> textfieldAndTextArea) throws IOException
	{
		System.out.println("in A thread of Receive");
		this.s=s;
		this.textfieldAndTextArea=textfieldAndTextArea;
		this.actionTextField=actionTextField;
		this.jtextfieldAndName=jtextfieldAndName;
		this.nameAndTextArea=nameAndTextArea;
		this.nameAndJFrame=nameAndJFrame;
		this.name=name;
		this.jlist=jlist;
		this.ta=ta;
		in=s.getInputStream();
		br=new BufferedReader(new InputStreamReader(in));
		this.start();
		System.out.println("in A thread of Receive+At End");
	}
	public void run() 
	{
		while(true)
		{
			String str="";
			String data="";
			try
			{
				str=br.readLine();				
				try
				{
					data=str.substring(0,6);
				}
				catch(StringIndexOutOfBoundsException e)
				{
					System.out.println(e);
				}
				if(data.equals("delete"))
				{
					System.out.println("Delete delete delete=="+str);
					String nn=str.substring(6,str.length());
					JFrame l=nameAndJFrame.get(nn);
					l.setEnabled(false);
				}
				if(data.equals("online"))
				{
					str=str.substring(6,str.length());
					DefaultListModel model=new DefaultListModel();
					while(true)
					{
						try
						{
						int index=str.indexOf(",");
						String name=str.substring(0,index);
						if(!name.equals(this.name))
							model.addElement(name);
							str=str.substring(index+1,str.length());
						}
						catch(StringIndexOutOfBoundsException e)
						{
							break;
						}
					}
					jlist.setModel(model);
					continue;
				}
				else if(data.equals("privat"))
				{
					String nam;
					int index=str.indexOf(":");
					nam=str.substring(6,index);
					System.out.println("Send This Message to "+nam);
					str=str.substring(6,str.length());
					index=str.indexOf(":");
					int index2=str.indexOf(",");
					String str1=str.substring(0,index);
					String str2=str.substring(index2+1,str.length());
					str=str1+":"+str2;
					JFrame f=nameAndJFrame.get(nam);
					if(f==null)
					{
						JFrame frame=new JFrame("online "+":"+nam);
						frame.setSize(400,400);
						JTextArea ta=new JTextArea();
						JTextField tf=new JTextField();
						tf.addActionListener(actionTextField);
						frame.add(new JScrollPane(ta));
						ta.setText(ta.getText()+str);
						frame.add(tf,"South");
						frame.setVisible(true);
						nameAndTextArea.put(nam,ta);
						nameAndJFrame.put(nam,frame);
						jtextfieldAndName.put(tf,nam);
						textfieldAndTextArea.put(tf,ta);
					}
					else
					{
						JTextArea tta=nameAndTextArea.get(nam);
						tta.setText(tta.getText()+"\n"+str);
						f.setEnabled(true);
						f.setVisible(true);
					}
				}
				else
				{
					System.out.println(str);
					ta.setText(ta.getText()+"\n"+str);
				}
			}
			catch(IOException io)
			{
				System.out.println(io);
			}
		}
	}
}
class Send
{
	private Socket s;
	private OutputStream out;
	private PrintWriter pr;
	public Send(Socket ss,String str) throws IOException 
	{
		this.s=ss;
		out=s.getOutputStream();
		pr=new PrintWriter(out,true);
		pr.println(str);
		System.out.println("Send Message"+str);
	}
}
class ActionTextFieldPrivate implements ActionListener
	{
		private String name;
		private Socket s;
		private HashMap<JTextField,String> textfieldAndName;
		private HashMap<JTextField,JTextArea> textfieldAndTextArea;
		ActionTextFieldPrivate(String name,Socket s,HashMap<JTextField,String> textfieldAndName,HashMap<JTextField,JTextArea> textfieldAndTextArea)
		{
			this.textfieldAndTextArea=textfieldAndTextArea;
			this.name=name;
			this.textfieldAndName=textfieldAndName;
			this.s=s;
		}
		public void actionPerformed(ActionEvent ae)
		{
			JTextField tf=(JTextField)ae.getSource();
			JTextArea ta=(JTextArea)textfieldAndTextArea.get(tf);
			ta.setText(ta.getText()+"\n"+name+":"+tf.getText());
			String msg="privat"+name+":"+(String)textfieldAndName.get(tf)+","+tf.getText();
			tf.setText("");
			try
			{
				new Send(s,msg);
			}
			catch(IOException e)
			{
				System.out.println("Error in sending private message.");
			}
			
		}
	}
class User
{
	private JFrame f;
	private JTextField tf;
	private JTextArea ta;
	private Socket s;
	private	String name;
	private JList jlist;
	private DefaultListModel listModel=new DefaultListModel();
	private HashMap<String,JFrame> nameAndFrame=new HashMap<String,JFrame>();
	private HashMap<String,JTextArea> nameAndTextArea=new HashMap<String,JTextArea>();
	private HashMap<JTextField,String> textfieldAndName=new HashMap<JTextField,String>();
	private HashMap<JTextField,JTextArea> textfieldAndTextArea=new HashMap<JTextField,JTextArea>();
	private ActionTextFieldPrivate actionTextField;
	class WindowAction extends WindowAdapter
	{
		public void windowclosed(WindowEvent we)
		{
			System.out.println("closed");
			try
			{
				new Send(s,"delete"+name);
				System.exit(0);
			}
			catch(IOException e)
			{
				
			}
		}
		public void windowClosing(WindowEvent we)
		{
			System.out.println("closing");
			try
			{
				new Send(s,"delete"+name);
				System.exit(0);
			}
			catch(IOException e)
			{
			
			}
		}
	}
	private void init()
	{
		f=new JFrame();
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAction());
		tf=new JTextField();
		ta=new JTextArea();
		jlist=new JList(listModel);
	}
	private void design()
	{
		JPanel p=new JPanel();
		JMenuBar mb=new JMenuBar();
		JMenu File=new JMenu("File");
		mb.add(File);
		f.add(mb,"North");
		tf.setText("Press Enter to send Message");
		tf.addActionListener(new ActionTextField());
		tf.setBackground(Color.green);
		f.add(tf,"South");
		ta.setBackground(Color.gray);
		ta.setForeground(Color.white);
		ta.setEnabled(false);
		f.add(new JScrollPane(ta));
		p.setLayout(new BorderLayout());
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				if(e.getValueIsAdjusting())
					return;
				System.out.println("My Event : " + e.getFirstIndex());
				
				String chatGuy=(String)jlist.getModel().getElementAt(e.getFirstIndex());
				//System.out.println(jlist.getSelectedIndex() + "; size - " +jlist.getModel().getSize() );
				System.out.println(chatGuy);
				if(!nameAndFrame.containsKey(chatGuy))
				{
					JFrame frame=new JFrame("online "+":"+chatGuy);
					frame.setSize(400,400);
					JTextArea ta=new JTextArea();
					JTextField tf=new JTextField();
					tf.addActionListener(actionTextField);
					frame.add(new JScrollPane(ta));
					frame.add(tf,"South");
					frame.setVisible(true);
					nameAndTextArea.put(chatGuy,ta);
					nameAndFrame.put(chatGuy,frame);
					textfieldAndName.put(tf,chatGuy);
					textfieldAndTextArea.put(tf,ta);
				}
				else
				{
					JFrame frame=nameAndFrame.get(chatGuy);
					frame.setVisible(true);
				}
			}
		});
		p.add(jlist);
		JButton bbb=new JButton("Start Chat");
		bbb.addActionListener(new ButtonAction());
		p.add(bbb,"South");
		f.add(p,"East");
	}
	class ButtonAction implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(true)
			{
				String chatGuy=(String)jlist.getSelectedValue();
				System.out.println(jlist.getSelectedIndex() + "; size - " +jlist.getModel().getSize() );
				System.out.println(chatGuy);
				if(!nameAndFrame.containsKey(chatGuy))
				{
					JFrame frame=new JFrame("online "+":"+chatGuy);
					frame.setSize(400,400);
					JTextArea ta=new JTextArea();
					JTextField tf=new JTextField();
					tf.addActionListener(actionTextField);
					frame.add(new JScrollPane(ta));
					frame.add(tf,"South");
					frame.setVisible(true);
					nameAndTextArea.put(chatGuy,ta);
					nameAndFrame.put(chatGuy,frame);
					textfieldAndName.put(tf,chatGuy);
					textfieldAndTextArea.put(tf,ta);
				}
				else
				{
					JFrame frame=nameAndFrame.get(chatGuy);
					frame.setVisible(true);
				}
			}
		}
	}
	/*
	class ActionTextFieldPrivate implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			JTextField tf=(JTextField)ae.getSource();
			String msg="privat"+name+":"+(String)textfieldAndName.get(tf)+","+tf.getText();
			tf.setText("");
			try
			{
				new Send(s,msg);
			}
			catch(IOException e)
			{
				System.out.println("Error in sending private message.");
			}
			
		}
	}
	*/
	class ActionTextField implements ActionListener
	{
		public void actionPerformed(ActionEvent Ae) 
		{
			String str=tf.getText();
			System.out.println(name);
			tf.setText("");
			try
			{
				new Send(s,name+":"+str);
			}
			catch(IOException io)
			{
				System.out.println(io);
			}
		}
	}
	public User() throws IOException
	{
		name=JOptionPane.showInputDialog(f,"Enter Name:");
		System.out.println(name);
		s=new Socket(InetAddress.getByName("localhost"),2020);
		actionTextField=new ActionTextFieldPrivate(name,s,textfieldAndName,textfieldAndTextArea);
		OutputStream out=s.getOutputStream();
		PrintWriter pr=new PrintWriter(out,true);
		pr.println("@@@---"+name+"@@@---");
		init();
		new Receive(ta,s,name,jlist,nameAndTextArea,nameAndFrame,textfieldAndName,actionTextField,textfieldAndTextArea);
		new SetFrame(f);
		design();
		f.setVisible(true);
	}
	public static void main(String arg[]) throws IOException
	{
		new User();
	}
}
