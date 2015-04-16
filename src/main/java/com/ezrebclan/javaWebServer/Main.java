package com.ezrebclan.javaWebServer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String url = req.getRequestURI();
		String path = url.substring(url.indexOf("/")+1);
		if(path.equals("favicon.ico")) {
			byte[] image = getFavicon();
			if(image != null) {
				resp.getOutputStream().write(image);
			}
		} else {
			String html = getHTMLDoc(path);
			resp.getWriter().print(html);
		}
	}
	
	static Thread console = new Thread(new Runnable() {
		
		@Override
		public void run() {
			InputStreamReader isr = new InputStreamReader(System.in);
			char[] command = new char[0];
			while(true) {
				char[] chars = new char[1];
				try {
					while(isr.ready() == false) {
						if(new String(command).equals("exit")) {
							System.exit(0);
						}
					}
					while(isr.ready()) {
						isr.read(chars);
						char[] command2 = new char[command.length + 1];
						System.arraycopy(command, 0, command2, 0,
								command.length);
						command2[command.length] = chars[0];
						command = command2;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(command.length > 2) {
					command = new String(command).substring(0,new String(command).length()-2).toCharArray();
				}
				if(new String(command).equals("exit")) {
					System.exit(0);
				} else {
					command = new char[0];
				}
			}
		}
	});
	
	public static void main(String[] args) throws Exception {
		start();
		console.start();
		Server server = null;
		try {
			server = new Server(Integer.valueOf(System.getenv("PORT")));
		} catch(NumberFormatException e) {
			server = new Server(80);
		} finally {
		    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		    context.setContextPath("/");
		    server.setHandler(context);
		    context.addServlet(new ServletHolder(new Main()),"/*");
		    server.start();
		    server.join();
		}
	}
	
	public static final File THIS_FOLDER = new File(".");
	public static final File WWW_FOLDER = new File(THIS_FOLDER, "www");
	public static final File ERROR_FOLDER = new File(THIS_FOLDER, "error_pages");
	public static final File CONFIG_FOLDER = new File(THIS_FOLDER, "config");
	public static final File ERROR_INSTRUCTIONS = new File(ERROR_FOLDER, "instructions.txt");
	
	public static void start() {
		WWW_FOLDER.mkdirs();
		ERROR_FOLDER.mkdirs();
		CONFIG_FOLDER.mkdirs();
		if(ERROR_INSTRUCTIONS.exists() == false) {
			try {
				ERROR_INSTRUCTIONS.createNewFile();
			} catch (IOException e) {
				System.err.println("Could not create error page instructions file.");
			}
			FileWriter fw = null;
			try {
				fw = new FileWriter(ERROR_INSTRUCTIONS);
			} catch(IOException e) {
				System.err.println("Could not create file writer for error instructions page");
			}
			try {
				fw.write("This folder is used for custom error pages.\nCustom error pages are pretty simple to make. You make them just like any other html document.\nTo make a custom error page, simply make an html file and name the file the number of the error.\nFor example, to create a 404 (not found) error page, simply create a file called 404.html.\n\nThese are the supported error pages as of 4/16/2015:\n\n401 (Unauthorized): this happens when somebody tries to access a protected webpage, and they don't put in the correct credentials. (This page is included, however there is no way to make a protected webpage at this time)\n403 (Forbidden): this is very similer to a 401, however this happens when a file or directory can't be accessed, even with a username and password.\n404 (Not Found): this is a pretty common one, second on the top five error pages seen. This happens when somebody tries to access a file that doesn't exist. This was the very first custom error page to be added to this api.\n500 (Internal Server Error): these get displayed when something goes wrong with my api. They are also the number one error page according to google. This gets displayed if an error occurs in the server, so, these things will pop up if anything ever goes wrong. My code is set up so that most of the time, if there is a page-breaking exception, it will get caught and this error page will be displayed.\n\nSo, that's how you make custom error pages. I hope that you enjoy this feature! :D");
			} catch (IOException e) {
				System.err.println("Could not write to error instruction file");
			} catch(NullPointerException n) {
				
			}
			try {
				fw.close();
			} catch (IOException e) {
				System.err.println("Could not close error instruction file");
			} catch(NullPointerException n) {
				
			}
		}
	}
	
	public static String getHTMLDoc(String uri) throws IOException {
		String html = "";
		File htmlDoc = null;
		if(uri.equals("favicon.ico")) {
			
		} else {
			htmlDoc = new File(WWW_FOLDER, uri+"/index.html");
			try {
				FileReader reader = new FileReader(htmlDoc);
				char[] chars = new char[1];
				while(reader.ready()) {
					reader.read(chars);
					html += chars[0];
				}
			} catch(FileNotFoundException f) {
				System.err.println("ERROR: File not found: "+uri+"/index.html");
				System.err.println("Displaying 404 Page");
				try {
					FileReader reader = new FileReader(new File(ERROR_FOLDER, "404.html"));
					char[] chars = new char[1];
					while(reader.ready()) {
						reader.read(chars);
						html += chars[0];
					}
				} catch(FileNotFoundException f2) {
					System.err.println("ERROR: Error 404 page not found.");
					System.err.println("Displaying plain text");
					return "<html><body><h1>Error 404: Page Not Found<br>:(</h1><h4>I don't know about the owner of this website, but I can tell you that I am very sorry about this.<br>*My* website hosting server could not find the requested page, and then it couldn't find a 404 page.<h6>Note that my web hosting server is a single file that you add into your programs. It is very possible that the owner of this website decided to change my code, and this may be causing this error. Contact the owner of the website to check.</h6></h4></body></html>";
				}
			}
		}
		return html;
	}
	public static byte[] getFavicon() throws IOException {
		File htmlDoc = new File(CONFIG_FOLDER, "favicon.ico");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(htmlDoc);
		} catch(FileNotFoundException f) {
			System.err.println("ERROR: config/favicon.ico not found");
			return null;
		}
		byte[] full = new byte[0];
		while(fis.available() > 0) {
			byte[] temp = new byte[1];
			fis.read(temp);
			byte[] full2 = new byte[full.length + 1];
			System.arraycopy(full, 0, full2, 0, full.length);
			full2[full.length] = temp[0];
			full = full2;
		}
		fis.close();
		return full;
	}
}
