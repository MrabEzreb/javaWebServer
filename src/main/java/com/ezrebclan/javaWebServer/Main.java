package com.ezrebclan.javaWebServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		String html = getHTMLDoc(path);
		resp.getWriter().print(html);
	}
	
	public static void main(String[] args) throws Exception {
		makeDirs();
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
	
	public static void makeDirs() {
		WWW_FOLDER.mkdirs();
		System.out.println(WWW_FOLDER.getAbsolutePath());
	}
	
	public static String getHTMLDoc(String uri) throws IOException {
		File htmlDoc = new File(WWW_FOLDER, uri+"/index.html");
		String html = "";
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
			return "<html><body><h1>Error 404: Page Not Found<br>:(</h1><h3>We are very sorry, but we could not find the requested page.</h3></body></html>";
		}
		return html;
	}
}
