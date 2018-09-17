package com.database.demo.ShellCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ShellComponent
public class MyCommand {

    @Autowired
    @Qualifier("mysqlJdbcTemplate")
    private JdbcTemplate mysqlTemplate;

    @ShellMethod("Add two integers together ")
    public int add(int a, int b){
        return a + b;
    }

    @ShellMethod("operate database")
    public String optBatch(String id, String name){
        try{
            mysqlTemplate.execute("insert into temp1(id,name) values ('"+id+"','"+name+"')");
        }catch(DuplicateKeyException e){
            mysqlTemplate.execute("insert into temp1(id,name) values (UUID(),'"+name+"')");
            e.printStackTrace();
        }
        List<Map<String,Object>> list = mysqlTemplate.queryForList("select * from temp1");
        return list.toString();
    }


    @ShellMethod("query")
    public String query(String tableName){
        return mysqlTemplate.queryForList("select * from " + tableName).toString();

    }

    @ShellMethod("delete")
    public String deleteTemp1(String pk){
         mysqlTemplate.execute("delete from temp1 where id ='"+ pk +"'");
         return "ok";
    }

    @ShellMethod("execute sql")
    public void execute(String sql){
        mysqlTemplate.execute(sql);
    }

    @ShellMethod("batch insert ")
    public String batchInsert(int isrollback){
        List<String> sqlList = new ArrayList<String>();
//        sqlList.add("insert into temp1(id,name) values('002','cc')");
//        sqlList.add("insert into temp1(id,name) values('002','cc')");
//        sqlList.add("insert into temp1(id,name) values('002','cc')");
//        sqlList.add("insert into temp1(id,name) values('002','cc')");
        Connection conn = null;
        Statement st = null;
        int[] rs = null;
        try {
            conn = mysqlTemplate.getDataSource().getConnection();
            st = conn.createStatement();
            conn.setAutoCommit(false);

            st.addBatch("update temp1 set name = 'kkk' where id = '002'");
            st.addBatch("insert into temp1(id,name) values('005','cc')");
            st.addBatch("update temp1 set name = 'hhh' where id = '001'");
//            st.addBatch("insert temp1(id,name) values('002','cc')");
//            st.addBatch("insert into temp1(id,name) values('008','cc')");

            rs = st.executeBatch();
            if(isrollback > 0){
                conn.rollback();
            }else{
                conn.commit();
            }
            conn.setAutoCommit(true);
            st.close();

        } catch (SQLException e) {
//            try {
//                conn.rollback();
//            } catch (SQLException e1) {
//                e1.printStackTrace();
//            }
            e.printStackTrace();
        }
        return rs.toString();
    }

    @ShellMethod("multi not added autocommit version ")
    public String multiSituation() throws SQLException {

       Connection conn = mysqlTemplate.getDataSource().getConnection();
//       conn.setAutoCommit(false);
        //不对事务进行显式地声明
       Statement st = conn.createStatement();
       st.addBatch("delete from temp1 where id = '005'");
       st.addBatch("insert into temp1(id,name) values('002','abcdf')");
       st.executeBatch();
       return null;
    }

    @ShellMethod("multi added autocommit version")
    public String test1( ){
        Connection conn = null;
        Statement st = null;
        try {
            conn = mysqlTemplate.getDataSource().getConnection();
            conn.setAutoCommit(false);
            st = conn.createStatement();
            st.addBatch("delete from temp1 where id = '003'");
            st.addBatch("insert into temp1(id,name) values('002','abcdf')");
            st.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                st.clearBatch();
                st.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

        }
        return null;
    }
}
