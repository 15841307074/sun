import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

/** Standalone JDBC regression for the cleanup statements in the published SQL (H2 MySQL mode). */
public class LotteryObsoleteCleanupCheck {
    record Block(String auditCondition,String audit,String dropCondition,String drop) { }
    static final Map<String,Block> blocks=new LinkedHashMap<>();
    static int passed;

    static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static String condition(String source,boolean promotion,boolean enabled,boolean exists,Long bad){
        return source.replace("@lottery_do_promotion",promotion?"TRUE":"FALSE").replace("@lottery_do_member",promotion?"FALSE":"TRUE")
                .replace("@lottery_drop_obsolete",enabled?"1":"0").replace("@lottery_cleanup_exists",exists?"TRUE":"FALSE")
                .replace("@lottery_cleanup_bad",bad==null?"NULL":bad.toString()).replace("TABLE_SCHEMA=DATABASE()","TABLE_SCHEMA=CURRENT_SCHEMA");
    }
    static boolean evaluate(Connection connection,String sql)throws SQLException{
        try(var statement=connection.createStatement();var result=statement.executeQuery("SELECT "+sql)){result.next();return result.getBoolean(1);}
    }
    static boolean exists(Connection connection,String table)throws SQLException{
        try(var result=connection.getMetaData().getTables(null,null,table,new String[]{"TABLE"})){return result.next();}
    }
    static boolean cleanup(Connection connection,String table,boolean promotion,boolean enabled)throws SQLException{
        var block=blocks.get(table);boolean present=exists(connection,table);Long bad=null;
        if(evaluate(connection,condition(block.auditCondition,promotion,enabled,present,null))){
            try(var statement=connection.createStatement();var result=statement.executeQuery(block.audit.replace(" INTO @lottery_cleanup_bad",""))){result.next();bad=result.getLong(1);}
        }
        if(evaluate(connection,condition(block.dropCondition,promotion,enabled,present,bad))){
            try(var statement=connection.createStatement()){statement.execute(block.drop);}
            return true;
        }
        return false;
    }
    static void execute(Connection connection,String sql)throws SQLException{try(var statement=connection.createStatement()){statement.execute(sql);}}
    static Connection database()throws SQLException{return DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE","sa","");}
    static void counters(Connection connection)throws SQLException{
        execute(connection,"CREATE TABLE lottery_v2_counter(business_id BIGINT,activity_id BIGINT,member_id BIGINT,scope_key VARCHAR(128),source INT,gained BIGINT,revision BIGINT,cache_dirty INT)");
        execute(connection,"INSERT INTO lottery_v2_counter VALUES(10,20,0,'ACTIVITY_TOTAL',0,7,1,0),(10,20,40,'TOTAL',0,0,5,1)");
    }
    static void member(Connection connection)throws SQLException{
        execute(connection,"CREATE TABLE member_lottery_points_0(business_id BIGINT,member_id BIGINT,sharding_value INT,business_no VARCHAR(64),delta INT,state VARCHAR(16))");
        execute(connection,"CREATE TABLE points_log_0(business_id BIGINT,member_id BIGINT,sharding_value INT,log_code VARCHAR(64),points_change BIGINT,points_log_status INT,deleted INT)");
        execute(connection,"CREATE TABLE wx_member(member_id BIGINT,member_integral BIGINT)");execute(connection,"INSERT INTO wx_member VALUES(40,100)");
    }
    static void activityTest(boolean equal,boolean enabled,boolean selected)throws Exception{
        try(var connection=database()){
            counters(connection);execute(connection,"CREATE TABLE lottery_v2_activity_counter(business_id BIGINT,activity_id BIGINT,total BIGINT)");
            execute(connection,"INSERT INTO lottery_v2_activity_counter VALUES(10,20,"+(equal?7:8)+")");
            require(cleanup(connection,"lottery_v2_activity_counter",selected,enabled)==(equal&&enabled&&selected),"Activity total cleanup must require matched data, selection and enable flag");
            require(exists(connection,"lottery_v2_counter"),"Counter keeper table was deleted");passed++;
        }
    }
    static void cacheTest(boolean matching)throws Exception{
        try(var connection=database()){
            counters(connection);execute(connection,"CREATE TABLE lottery_v2_counter_cache_task(business_id BIGINT,activity_id BIGINT,member_id BIGINT,revision BIGINT,dirty INT)");
            execute(connection,"INSERT INTO lottery_v2_counter_cache_task VALUES(10,20,"+(matching?40:41)+",5,1)");
            require(cleanup(connection,"lottery_v2_counter_cache_task",true,true)==matching,"Pending cache tasks must be durably transferred before drop");passed++;
        }
    }
    static void memberTest(String state,int status,int deleted,String suffix,int delta,int savedDelta,boolean shouldDrop)throws Exception{
        try(var connection=database()){
            member(connection);execute(connection,"INSERT INTO member_lottery_points_0 VALUES(10,40,0,'L100:"+suffix+"',"+delta+",'"+state+"')");
            execute(connection,"INSERT INTO points_log_0 VALUES(10,40,0,'L100:"+suffix+"',"+savedDelta+","+status+","+deleted+")");
            require(cleanup(connection,"member_lottery_points_0",false,true)==shouldDrop,"Incorrect points cleanup decision for "+state+"/"+suffix+"/"+status);
            require(exists(connection,"points_log_0")&&exists(connection,"wx_member"),"Wallet or points log was deleted");
            try(var statement=connection.createStatement();var result=statement.executeQuery("SELECT member_integral FROM wx_member")){result.next();require(result.getLong(1)==100,"Cleanup changed wallet balance");}passed++;
        }
    }
    public static void main(String[] arguments)throws Exception{
        Class.forName("org.h2.Driver");String script=Files.readString(Path.of(arguments[0]));
        require(!script.contains("DROP DATABASE"),"Database drop must not be present");
        var names=Pattern.compile("(?m)^-- 清理白名单：([^\\r\\n]+)").matcher(script);
        var starts=new ArrayList<Integer>();var tables=new ArrayList<String>();while(names.find()){starts.add(names.start());tables.add(names.group(1));}
        var statements=Pattern.compile("(?m)^SET @lottery_release_ddl = IF\\((.+),\\r?\\n  '(.*)',\\r?\\n  'DO 0'\\);");
        for(int i=0;i<tables.size();i++){
            String body=script.substring(starts.get(i),i+1<starts.size()?starts.get(i+1):script.indexOf("-- ==================== 执行后核对",starts.get(i)));
            var matcher=statements.matcher(body);require(matcher.find(),"Cleanup audit missing");String auditCondition=matcher.group(1).strip(),audit=matcher.group(2).replace("''","'");
            require(matcher.find(),"Cleanup drop missing");String dropCondition=matcher.group(1).strip(),drop=matcher.group(2).replace("''","'");
            require(drop.equals("DROP TABLE IF EXISTS `"+tables.get(i)+"`"),"Drop outside whitelist");require(dropCondition.contains("@lottery_cleanup_bad=0"),"Drop without verification");
            require(body.contains("SET @lottery_cleanup_bad = NULL;"),"Audit state not reset before each table");blocks.put(tables.get(i),new Block(auditCondition,audit,dropCondition,drop));
        }
        require(blocks.size()==12,"Expected exactly two retired promotion tables and ten member shards");
        require(blocks.containsKey("lottery_v2_activity_counter")&&blocks.containsKey("lottery_v2_counter_cache_task"),"Promotion cleanup whitelist changed");
        for(int shard=0;shard<10;shard++)require(blocks.containsKey("member_lottery_points_"+shard),"Member shard cleanup missing");
        require(Pattern.compile("DROP TABLE IF EXISTS").matcher(script).results().count()==12,"Unexpected extra drop statement");
        activityTest(true,true,true);activityTest(false,true,true);activityTest(true,false,true);activityTest(true,true,false);
        cacheTest(true);cacheTest(false);
        memberTest("SUCCESS",1,0,"AWARD",50,50,true);memberTest("SUCCESS",2,1,"AWARD",50,50,true);
        memberTest("SUCCESS",1,0,"AWARD",50,49,false);memberTest("REJECTED",0,1,"DEBIT",-10,-10,true);
        memberTest("REJECTED",1,0,"AWARD",50,50,true);memberTest("REJECTED",1,0,"REFUND",10,10,true);
        memberTest("REJECTED",1,0,"DEBIT",-10,-10,false);memberTest("INIT",1,0,"AWARD",50,50,false);
        try(var connection=database()){
            member(connection);execute(connection,"DROP TABLE points_log_0");require(!cleanup(connection,"member_lottery_points_0",false,true),"Missing destination must prevent drop");passed++;
        }
        try(var connection=database()){
            counters(connection);require(!cleanup(connection,"lottery_v2_activity_counter",true,true),"Already absent table must be a no-op");passed++;
        }
        try(var connection=database()){
            counters(connection);execute(connection,"CREATE TABLE lottery_v2_counter_cache_task(business_id BIGINT,activity_id BIGINT,member_id BIGINT,revision BIGINT,dirty INT)");
            execute(connection,"INSERT INTO lottery_v2_counter_cache_task VALUES(10,20,40,5,2)");require(!cleanup(connection,"lottery_v2_counter_cache_task",true,true),"Unknown cache state must prevent drop");passed++;
        }
        try(var connection=database()){
            member(connection);execute(connection,"INSERT INTO member_lottery_points_0 VALUES(10,40,0,'L100:AWARD',50,'SUCCESS')");
            execute(connection,"INSERT INTO points_log_0 VALUES(11,40,0,'L100:AWARD',50,1,0)");require(!cleanup(connection,"member_lottery_points_0",false,true),"Other project log must not authorize drop");passed++;
        }
        try(var connection=database()){
            counters(connection);execute(connection,"CREATE TABLE lottery_v2_activity_counter(business_id BIGINT,activity_id BIGINT,total BIGINT)");execute(connection,"INSERT INTO lottery_v2_activity_counter VALUES(10,20,-1)");
            execute(connection,"UPDATE lottery_v2_counter SET gained=-1 WHERE scope_key='ACTIVITY_TOTAL'");require(!cleanup(connection,"lottery_v2_activity_counter",true,true),"Invalid negative total must prevent drop");passed++;
        }
        System.out.println("Cleanup regression passed: "+passed+" cases; whitelist=12; wallet and current tables preserved.");
    }
}
