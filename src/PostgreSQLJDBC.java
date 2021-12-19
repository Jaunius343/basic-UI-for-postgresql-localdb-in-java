import org.postgresql.ds.PGSimpleDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class PostgreSQLJDBC {
    public static void main(String args[]) {
        Connection c = null;

        nl();
        System.out.println("Table list:");
        String[] tables = {"vartotojas", "saskaita", "kortele", "bankas", "paslauga"};
        for(int i = 0; i < tables.length; ++i){
            System.out.println((i+1) + ". " + tables[i]);
        }
        nl();

        Scanner sc = new Scanner(System.in);
        int number;
        do {
            System.out.println("Pick a table");
            while (!sc.hasNextInt()) {
                System.out.println("That's not a number!");
                sc.next(); // this is important!
            }
            number = sc.nextInt();
        } while (number < 1 || number > tables.length);

        String selectedTable = tables[number-1];

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/bankas?currentSchema=bank",
                            "postgres", "postgres");

            System.out.println("Opened database successfully");

            Statement statement = c.createStatement();

            String[] actions  = {"print whole table", "insert data", "update data", "delete data"};
            if (selectedTable.equals("saskaita")){
                actions  = new String[]{"print whole table", "insert data", "update data", "delete data", "transfer money", "Search by bank"};
            }

            String[] availableActions = actions;
            int selectedAction;
            boolean flag = false;
            while(!flag){
                nl();
                System.out.println("Action list:");
                for(int i = 0; i < actions.length; ++i){
                    System.out.println((i+1) + ". " + actions[i]);
                }
                nl();
                do {
                    System.out.println("Select action");
                    while (!sc.hasNextInt()) {
                        System.out.println("That's not a number!");
                        sc.next(); // this is important!
                    }
                    number = sc.nextInt();
                } while (number < 1 || number > actions.length);
                selectedAction = number;

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String sql;
                ResultSet forMd = statement.executeQuery("SELECT * FROM " + selectedTable + " FETCH FIRST ROW ONLY");

                switch(selectedAction){
                    //print whole table
                    case 1:
                        sql = "SELECT * FROM " + selectedTable;
                        ResultSet result = statement.executeQuery(sql);
                        printTable(result);
                        break;
                    //insert data
                    case 2:
                        statement.executeUpdate(insertData(forMd, selectedTable));
                        System.out.println("added");
                        break;
                    case 3:
                        statement.executeUpdate(updateData(forMd, selectedTable));
                        System.out.println("updated");
                        break;
                    case 4:
                        sql = "DELETE FROM " + selectedTable + " WHERE id = ";
                        System.out.println("Enter id to delete:");
                        long id = Long.parseLong(br.readLine());
                        sql += id;
                        statement.executeUpdate(sql);
                        System.out.println("deleted");
                        break;
                    case 5:
                        doTranscation(statement, c);
                        break;
                    case 6:
                        sql = "SELECT id, pavadinimas FROM bank.bankas";
                        result = statement.executeQuery(sql);
                        printTable(result);
                        System.out.println("Select a bank for account display");

                        int bankId = Integer.parseInt(br.readLine());
                        sql = "SELECT saskaita.id, vartotojas, balansas, tipas, pavadinimas " +
                                "FROM bank.saskaita JOIN bank.bankas " +
                                "ON bankas.id = saskaita.bankas " +
                                "WHERE bankas.id = " + bankId;
                        result = statement.executeQuery(sql);
                        printTable(result);
                        break;
                    default:
                        System.out.println("should never happen");
                }
            }


//            printTable(result);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            //System.out.println(e.getClass().getName()+": "+e.getMessage());
            //System.exit(0);
        }
        //System.out.println("Opened database successfully");
    }

    static public void doTranscation(Statement statement, Connection c)
    {
        String sql;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            c.setAutoCommit(false);

            System.out.println("Transfer money from: ");
            int num = Integer.parseInt(br.readLine());
            System.out.println("Transfer money to: ");
            int num2 = Integer.parseInt(br.readLine());
            System.out.println("Transfer amount: ");

            int sum = Integer.parseInt(br.readLine());
            sql = "UPDATE saskaita SET balansas = balansas + " + sum + " WHERE id = " + num2;
            statement.executeUpdate(sql);

//            System.out.println(sql);

            sql = "UPDATE saskaita SET balansas = balansas - " + sum + " WHERE id = " + num;
            statement.executeUpdate(sql);
//            System.out.println(sql);

            c.commit();
            System.out.println("transaction successful");
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            try{
                System.out.println("Transaction is being rolled back");
                c.rollback();
            } catch (Exception ex){
                ex.printStackTrace();
                System.err.println(ex.getClass().getName()+": "+ex.getMessage());
            }
        } finally {
            try{
                c.setAutoCommit(true);
            } catch (Exception ex){
                ex.printStackTrace();
                System.err.println(ex.getClass().getName()+": "+ex.getMessage());
            }
        }
    }

    static public String updateData(ResultSet rs, String table)
    {
        String sql = "UPDATE " + table + " SET ";
        try{
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Data update selected");
            List<String> fields = new ArrayList<>();

            ResultSetMetaData rsmd = rs.getMetaData();
            int collumnCount = rsmd.getColumnCount();

            for(int i = 2 ; i <= collumnCount; ++i){
                System.out.println("Do you want to update "+ rsmd.getColumnName(i) + ": y/n");
                String s=br.readLine();
                if(s.startsWith("y")){
                    System.out.println("New value for " + rsmd.getColumnName(i));
                    if (rsmd.getColumnClassName(i).equals("java.lang.String")){
                        String temp = br.readLine();
                        sql += rsmd.getColumnName(i) + " = '" + temp + "', ";
                    }
                    if(rsmd.getColumnClassName(i).equals("java.sql.Date")){
                        String date = br.readLine();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date temp = dateFormat.parse(date);
                        sql += rsmd.getColumnName(i) + " = '"  + dateFormat.format(temp) + "', ";
                    }
                }
            }
            sql = sql.substring(0, sql.length() - 2);
            System.out.println("User ID to update: ");
//            int id = Integer.parseInt(br.readLine());
            long id = Long.parseLong(br.readLine());
            sql += " WHERE id = " + id;

            return sql;
//            for(int i = 0 ; i <= fields.size(); ++i) {
//                System.out.println("New value for " + fields.get(i));
//                String s = br.readLine();
//            }


//            int temp = fields.size();
//            int[] selectedFields = new int[temp];

//            System.out.println(fields);
//            System.out.println(fields.size());

            //select fields to update
            //y/n if y, selectedfiels[i] = 1;

        } catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return "";
    }

    static public String insertData(ResultSet rs, String table)
    {
        String sql;
        try {
            System.out.println("Data insert selected");
            System.out.println("Enter Data");
            ResultSetMetaData rsmd = rs.getMetaData();
            int collumnCount = rsmd.getColumnCount();


            sql = "INSERT INTO " + table + " (";

            for(int i = 2 ; i <= collumnCount; i++){
                sql += rsmd.getColumnName(i) + ", ";
            }
            sql = sql.substring(0, sql.length() - 2);
            sql = sql + ") VALUES (";

            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

            for(int i = 2 ; i <= collumnCount; i++){
//                sql += rsmd.getColumnName(i) + ", ";
//                System.out.print(rsmd.getColumnClassName(i) + " ");
                System.out.println(rsmd.getColumnName(i) + ":");

                if (rsmd.getColumnClassName(i).equals("java.lang.String")){
                    String temp = br.readLine();
                    sql += "'" + temp + "', ";
                }
                if(rsmd.getColumnClassName(i).equals("java.sql.Date")){
                    String date = br.readLine();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date temp = dateFormat.parse(date);
                    sql += "'" + dateFormat.format(temp) + "', ";
                }
                if(rsmd.getColumnClassName(i).equals("java.lang.Integer")){
                    int num = Integer.parseInt(br.readLine());
                    sql += num + ", ";
                }
                if(rsmd.getColumnClassName(i).equals("java.lang.Long")){
                    long num = Long.parseLong(br.readLine());
                    sql += num + ", ";
                }
            }
            sql = sql.substring(0, sql.length() - 2);
            sql = sql + ")";

//            System.out.println(sql);

            return sql;
        } catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return "";
    }

    static public void nl()
    {
        System.out.println();
    }

    static public void printTable(ResultSet rs)
    {
        try{
            ResultSetMetaData rsmd = rs.getMetaData();
            int collumnCount = rsmd.getColumnCount();
            int len = 0;
            int size = 0;
            for(int i = 1 ; i <= collumnCount; i++){
                len += rsmd.getColumnDisplaySize(i);
                System.out.print(rsmd.getColumnName(i) + " ");
            }
            nl();

            while(rs.next()){
                ++size;
                for(int i = 1 ; i <= collumnCount; i++){
                    System.out.print(rs.getString(i) + " "); //Print one element of a row
                }
                nl();
            }

            for(int i = 0; i < len / 2; ++i){
                System.out.print("-");
            }
            nl();
            System.out.println("Total rows: " + size);

        } catch(Exception e){
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
}