import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Shiftly standalone Java server. No Spring Boot and no Maven.
 * Requires MySQL Connector/J jar in lib/ to use MySQL.
 */
public class ShiftlyServer {
    static final int PORT = 8080;
    static final String DB_URL = System.getenv().getOrDefault("SHIFTLY_DB_URL", "jdbc:mysql://localhost:3306/shiftly");
    static final String DB_USER = System.getenv().getOrDefault("SHIFTLY_DB_USER", "root");
    static final String DB_PASSWORD = System.getenv().getOrDefault("SHIFTLY_DB_PASSWORD", "");
    static final Path WEB = Paths.get("web").toAbsolutePath();

    public static void main(String[] args) throws Exception {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            System.err.println("MySQL Connector/J missing. Put mysql-connector-j-*.jar in lib/ and run using run.bat.");
            System.exit(1);
        }
        try (Connection c = db()) { System.out.println("Connected to MySQL database 'shiftly'."); }
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/", ShiftlyServer::api);
        server.createContext("/", ShiftlyServer::staticFiles);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Shiftly running at http://localhost:" + PORT);
    }

    static Connection db() throws SQLException { return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); }
    static String body(HttpExchange x) throws IOException { return new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); }
    static Map<String,String> parse(String s) {
        Map<String,String> m = new HashMap<>();
        java.util.regex.Matcher a = java.util.regex.Pattern.compile("\"((?:\\\\.|[^\"])*)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(s);
        while(a.find()) m.put(unescape(a.group(1)), unescape(a.group(2)));
        return m;
    }
    static String unescape(String s) { return s.replace("\\n","\n").replace("\\\"","\"").replace("\\\\","\\"); }
    static String q(String s) { if(s==null)return ""; return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r",""); }
    static String json(Map<String,?> m) {
        StringJoiner j=new StringJoiner(",","{","}");
        for(var e:m.entrySet()) j.add("\""+q(e.getKey())+"\":"+(e.getValue()==null?"null":e.getValue() instanceof Number||e.getValue() instanceof Boolean?e.getValue():"\""+q(String.valueOf(e.getValue()))+"\""));
        return j.toString();
    }
    static void send(HttpExchange x,int status,String contentType,String s)throws IOException{
        byte[] b=s.getBytes(StandardCharsets.UTF_8); x.getResponseHeaders().set("Content-Type",contentType+"; charset=utf-8");
        x.getResponseHeaders().set("Access-Control-Allow-Origin","*"); x.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type");
        x.getResponseHeaders().set("Access-Control-Allow-Methods","GET,POST,PUT,OPTIONS");
        if(x.getRequestMethod().equalsIgnoreCase("OPTIONS")){x.sendResponseHeaders(204,-1);return;}
        x.sendResponseHeaders(status,b.length);try(OutputStream o=x.getResponseBody()){o.write(b);}
    }
    static void api(HttpExchange x)throws IOException{
        if(x.getRequestMethod().equalsIgnoreCase("OPTIONS")){send(x,204,"text/plain","");return;}
        String p=x.getRequestURI().getPath(), method=x.getRequestMethod(), raw=bodyIfPost(x);
        Map<String,String> d=parse(raw);
        try(Connection c=db()){
            if(p.equals("/api/auth/register")&&method.equals("POST")){
                String name=d.getOrDefault("fullName","").trim(), email=d.getOrDefault("email","").trim(), phone=d.getOrDefault("phone","").trim(), pass=d.getOrDefault("password",""), college=d.getOrDefault("college","");
                if(name.isBlank()||email.isBlank()||phone.isBlank()||pass.length()<6){send(x,400,"application/json","{\"success\":false,\"message\":\"Enter name, email, phone and a password of at least 6 characters.\"}");return;}
                try(PreparedStatement z=c.prepareStatement("INSERT INTO users(full_name,email,phone,password,college) VALUES(?,?,?,?,?)")){z.setString(1,name);z.setString(2,email);z.setString(3,phone);z.setString(4,pass);z.setString(5,college);z.executeUpdate();}
                send(x,200,"application/json","{\"success\":true,\"message\":\"Registration successful. You can now log in.\"}");return;
            }
            if(p.equals("/api/auth/login")&&method.equals("POST")){
                String email=d.getOrDefault("email","").trim(), pass=d.getOrDefault("password","");
                if((email.equalsIgnoreCase("admin")||email.equalsIgnoreCase("admin@shiftly.test"))&&pass.equals("admin123")){
                    send(x,200,"application/json","{\"success\":true,\"message\":\"Admin login successful.\",\"role\":\"admin\",\"user\":{\"fullName\":\"Administrator\",\"email\":\"admin\"}}");return;
                }
                try(PreparedStatement z=c.prepareStatement("SELECT full_name,email,phone,college FROM users WHERE LOWER(email)=LOWER(?) AND password=?")){z.setString(1,email);z.setString(2,pass);try(ResultSet r=z.executeQuery()){if(r.next()){send(x,200,"application/json","{\"success\":true,\"message\":\"Login successful.\",\"role\":\"student\",\"user\":{\"fullName\":\""+q(r.getString(1))+"\",\"email\":\""+q(r.getString(2))+"\",\"phone\":\""+q(r.getString(3))+"\",\"college\":\""+q(r.getString(4))+"\"}}");return;}}}
                send(x,401,"application/json","{\"success\":false,\"message\":\"Invalid email or password.\"}");return;
            }
            if(p.equals("/api/jobs")&&method.equals("GET")){send(x,200,"application/json",jobs(c,false));return;}
            if(p.equals("/api/admin/jobs")&&method.equals("GET")){send(x,200,"application/json",jobs(c,true));return;}
            if(p.equals("/api/admin/jobs")&&method.equals("POST")){
                try(PreparedStatement z=c.prepareStatement("INSERT INTO jobs(title,location,duration,hours,payment,experience,job_type,description,posted_by,poster_phone,poster_email,open_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,1)")){
                    z.setString(1,d.getOrDefault("title","New Job"));z.setString(2,d.getOrDefault("location","Kerala"));z.setString(3,d.getOrDefault("duration","One day"));z.setString(4,d.getOrDefault("hours","9 AM - 5 PM"));z.setString(5,d.getOrDefault("payment","₹500/day"));z.setString(6,d.getOrDefault("experience","No experience required"));z.setString(7,d.getOrDefault("jobType","Temporary"));z.setString(8,d.getOrDefault("description",""));z.setString(9,d.getOrDefault("postedBy","Administrator"));z.setString(10,d.getOrDefault("posterPhone",""));z.setString(11,d.getOrDefault("posterEmail",""));z.executeUpdate();}
                send(x,200,"application/json","{\"success\":true,\"message\":\"Job published.\"}");return;
            }
            if(p.equals("/api/applications")&&method.equals("GET")){send(x,200,"application/json",applications(c));return;}
            if(p.equals("/api/applications")&&method.equals("POST")){
                long jobId=Long.parseLong(d.getOrDefault("jobId","0"));
                try(PreparedStatement z=c.prepareStatement("INSERT INTO applications(job_id,applicant_name,applicant_email,applicant_phone,applicant_college,status,applied_date) VALUES(?,?,?,?,?,'Pending',CURRENT_DATE)")){
                    z.setLong(1,jobId);z.setString(2,d.getOrDefault("applicantName",""));z.setString(3,d.getOrDefault("applicantEmail",""));z.setString(4,d.getOrDefault("applicantPhone",""));z.setString(5,d.getOrDefault("applicantCollege",""));z.executeUpdate();}
                send(x,200,"application/json","{\"success\":true,\"message\":\"Application submitted.\"}");return;
            }
            if(p.matches("/api/admin/applications/\\d+/status")&&method.equals("POST")){
                long id=Long.parseLong(p.split("/")[4]);String st=d.getOrDefault("status","Pending");
                if(!List.of("Pending","Accepted","Rejected").contains(st)){send(x,400,"application/json","{\"success\":false,\"message\":\"Invalid status\"}");return;}
                try(PreparedStatement z=c.prepareStatement("UPDATE applications SET status=? WHERE id=?")){z.setString(1,st);z.setLong(2,id);z.executeUpdate();}
                send(x,200,"application/json","{\"success\":true,\"message\":\"Status updated.\"}");return;
            }
            send(x,404,"application/json","{\"success\":false,\"message\":\"API endpoint not found.\"}");
        }catch(SQLException e){
            String msg=e.getMessage()==null?"Database error":e.getMessage();
            send(x,500,"application/json","{\"success\":false,\"message\":\""+q(msg)+"\"}");
        }catch(Exception e){send(x,400,"application/json","{\"success\":false,\"message\":\""+q(e.getMessage())+"\"}");}
    }
    static String bodyIfPost(HttpExchange x)throws IOException{return x.getRequestMethod().equalsIgnoreCase("POST")||x.getRequestMethod().equalsIgnoreCase("PUT")?body(x):"";}
    static String jobs(Connection c,boolean admin)throws SQLException{
        String sql="SELECT id,title,location,duration,hours,payment,experience,job_type,description,posted_by,poster_phone,poster_email,open_status FROM jobs ORDER BY id DESC";
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){StringJoiner out=new StringJoiner(",","[","]");while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getLong(1));m.put("title",r.getString(2));m.put("location",r.getString(3));m.put("duration",r.getString(4));m.put("hours",r.getString(5));m.put("payment",r.getString(6));m.put("experience",r.getString(7));m.put("jobType",r.getString(8));m.put("description",r.getString(9));m.put("postedBy",r.getString(10));m.put("posterPhone",r.getString(11));m.put("posterEmail",r.getString(12));m.put("open",r.getBoolean(13));out.add(json(m));}return out.toString();}
    }
    static String applications(Connection c)throws SQLException{
        String sql="SELECT a.id,a.job_id,j.title,j.location,a.applied_date,j.hours,j.payment,a.status,a.applicant_name,a.applicant_email,a.applicant_phone,a.applicant_college FROM applications a JOIN jobs j ON j.id=a.job_id ORDER BY a.id DESC";
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){StringJoiner out=new StringJoiner(",","[","]");while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getLong(1));m.put("jobId",r.getLong(2));m.put("jobTitle",r.getString(3));m.put("location",r.getString(4));m.put("appliedDate",r.getString(5));m.put("hours",r.getString(6));m.put("payment",r.getString(7));m.put("status",r.getString(8));m.put("applicantName",r.getString(9));m.put("applicantEmail",r.getString(10));m.put("applicantPhone",r.getString(11));m.put("applicantCollege",r.getString(12));out.add(json(m));}return out.toString();}
    }
    static void staticFiles(HttpExchange x)throws IOException{
        String path=x.getRequestURI().getPath();if(path.equals("/"))path="/index.html";
        Path f=WEB.resolve(path.substring(1)).normalize();
        if(!f.startsWith(WEB)||!Files.exists(f)||Files.isDirectory(f)){send(x,404,"text/plain","Not found");return;}
        String name=f.getFileName().toString().toLowerCase(),type=name.endsWith(".html")?"text/html":name.endsWith(".css")?"text/css":name.endsWith(".js")?"application/javascript": "application/octet-stream";
        send(x,200,type,Files.readString(f,StandardCharsets.UTF_8));
    }
}
