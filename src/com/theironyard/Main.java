package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    static void insertBeer (Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES(NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }
    static void deleteBeer (Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id =?");
        stmt.setInt(1, id);
        stmt.execute();
    }
    static ArrayList<Beer> selectBeers (Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList();
        while (results.next()) {
            Beer beer = new Beer();
            beer.id = results.getInt("id");
            beer.name = results.getString("name");
            beer.type  = results.getString("type");
            beers.add(beer);
        }
        return beers;
    }

    static void updateBeer(Connection conn, int id, String beerName, String beerType) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ?, type = ? WHERE id = ?");
        stmt.setString(1, beerName);
        stmt.setString(2, beerType);
        stmt.setInt(3, id);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (id IDENTITY, name VARCHAR, type VARCHAR)");

        Spark.get (
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    ArrayList<Beer> beers = selectBeers(conn);
                    String username = session.attribute("username");//read username
                    if (username == null) { //if user is not logged in
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeers(conn));
                    //m.put("beers", beers);
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    //beer.id = beers.size() + 1;
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    insertBeer(conn, beer.name, beer.type); // CALLING FROM INSERT BEER
                    //beers.add(beer);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);

                    }catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String editBeer = request.queryParams("beerid");

                    //PreparedStatement stmt2 = conn.prepareStatement("UPDATE beers SET name = ? AND type = ? WHERE ROWNUM = ? ");
                    //int beerNum = Integer.valueOf(editBeer);
                    String id = request.queryParams("beerid");
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");
                    try {
                        int idNum = Integer.valueOf(id);
                        updateBeer(conn, idNum, name, type);
                    }catch (Exception e) {

                    }
                    //stmt2.execute();

                    response.redirect("/");
                    return "";
                })

        );

    }
}
