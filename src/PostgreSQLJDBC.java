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
        String[] tables = {"vartotojas", "saskaita", "kortele"};
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
                } while (number < 1 || number > 5);
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
                        updateData(forMd, selectedTable);
                        System.out.println("not implemented");
                        break;
                    case 4:
                        sql = "DELETE FROM " + selectedTable + " WHERE id = ";
                        int id = Integer.parseInt(br.readLine());
                        sql += id;
                        statement.executeUpdate(sql);
                        break;
                    default:
                        System.out.println("should never happen");
                }
            }


//            printTable(result);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        //System.out.println("Opened database successfully");
    }

    static public String updateData(ResultSet rs, String table)
    {
        String sql;
        try{
            System.out.println("Data update selected");
            List<String> fields = new ArrayList<>();

            ResultSetMetaData rsmd = rs.getMetaData();
            int collumnCount = rsmd.getColumnCount();

            for(int i = 2 ; i <= collumnCount; i++){
                fields.add(rsmd.getColumnName(i));
            }
            int temp = fields.size();
            int[] selectedFields = new int[temp];

            System.out.println(fields);
            System.out.println(fields.size());

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
                System.out.print(rsmd.getColumnClassName(i) + " ");
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
            }
            sql = sql.substring(0, sql.length() - 2);
            sql = sql + ")";

            //System.out.println(sql);

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