package com.example.webservise;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class UserController {

    @GetMapping("users")
//    public List<User> getUsers() throws IOException { we return instead ofList ResponseEntity object with Json
    public ResponseEntity<List<User>> getAllUsers() throws IOException {

        //in that method - we create object wit path to the file
        List<User> users = getUsersFromFile();
//        return users; //but we shouldn't return only list here we should return ResponseEntity with json because it is api
//
//        Gson gson = new Gson();
//        String usersJson = gson.toJson(users);

        return ResponseEntity.ok(users);

        //return ResponseEntity
//                .status(HttpStatus.OK)
//                .header("Content-Type","application/json;charset=utf-8")
//                .body(users);
    }

    @PostMapping("users")
    public ResponseEntity<User> addUser(@RequestBody User user) throws IOException {

        //in case to generate new id for new user, we have to get users from file - so we can use our method "get"
        List<User> usersFromFile = getUsersFromFile();
        int lastId = usersFromFile.getLast().id();
        CSVFormat csvFormat = getCSVFormat(true);
        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter("src/main/resources/users.csv", true), csvFormat)) {

            //here we should return new user in case to get proper id - not 0
            User newUser = new User(user.id() + 1, user.name(), user.age(), user.isMale());
            csvPrinter.printRecord(newUser.id(), newUser.name(), newUser.age(), newUser.isMale());
        }//we use here constructor that let us append new info

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("users/{id}") //to identificate user - we need to add id for each person
    //id we add as @RequestParam
    public ResponseEntity<User> updateUser(@RequestParam int id, @RequestBody User user) throws IOException {
        //1. we need to take all users to know how many users we have
        List<User> listOfUsers = getUsersFromFile();
        List<User> updatedUsers = new ArrayList<>();//2.records are immutable so that we have to create new list to return

        //if we have variable defined in method we have to write it outside method in case to use it
        User thatUser = null;
        for (User userFromFile : listOfUsers) {
            if (userFromFile.id() == id) { //3.we find proper user with id from @Request and added him to list with new data
                thatUser = new User(id, user.name(), user.age(), user.isMale()); //here also we add id from request
                updatedUsers.add(thatUser); //we create new list of users
            } else {
                updatedUsers.add(userFromFile);
                //4.we write new list to file
            }
        }
        CSVFormat csvFormat2 = getCSVFormat(false);
        for (User everyUser : updatedUsers) { //we remove append parameter because we create new file not append a value
            try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter("src/main/resources/users.csv"), csvFormat2)) {
            }
        }
        return ResponseEntity.ok(thatUser);
    }

    @DeleteMapping("users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) throws IOException {
        //1.we get all users
        List<User> usersFromFile = getUsersFromFile(); //that list is unmutable because of that method toList()
        ArrayList<User> users = new ArrayList<>(usersFromFile); //that list is mutable
//        for (User userInFile : users) {
//            if (userInFile.id() == id) {
        //users.remove(userInFile); // that list is unmutable
        users.removeIf(user -> user.id() == id);
        //we go through all the users and we print it to the file
        CSVFormat csvFormat = getCSVFormat(false);
        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter("src/main/resources/users.csv"), csvFormat)) {
            for (User user : users) {
                csvPrinter.printRecord(user.id(), user.name(), user.age(), user.isMale());
                //we use here constructor that let us append new info
            }
        }

        return ResponseEntity.noContent().build(); //we have to add here method build()
    }

    private List<User> getUsersFromFile() throws IOException {
        Reader input = new FileReader("src/main/resources/users.csv");
        //we declare structure csv file
        CSVFormat csvFormat = getCSVFormat(true);
        //we parse the data
        CSVParser parser = csvFormat.parse(input);
        //and we read records
        //we return ResponseEntity instead of list
        List<User> users = parser.stream() //we create particular users
                .map(record -> {
                    int id = Integer.parseInt(record.get("id"));
                    String name = record.get("name");
                    int age = Integer.parseInt(record.get("age"));
                    boolean isMale = Boolean.parseBoolean(record.get("isMale"));

                    return new User(id, name, age, isMale); //instead stream of records, we have stream of users
                })
                //we have to sort id's in case to get proper number of new user
                .sorted(Comparator.comparing(User::id))
                .toList();
        return users;
    }

    private CSVFormat getCSVFormat(Boolean setSkipHeaderRecord) {
        return CSVFormat.DEFAULT.builder()
                //  .setDelimiter(",") we add that line only when we use delimiter different from coma,
                .setHeader("id", "name", "age", "isMale")
                .setSkipHeaderRecord(setSkipHeaderRecord)
                .build();
    }


}
