package com.example.webservise;

//controller is a class that handle requests from users

import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {


    @GetMapping("health")
//    how ro check if app is healthy
//    1 solution:
//     String checkHealth() {
//        return "Ok";
//    }

    public ResponseEntity<String> checkHealth() {

        Gson gson = new Gson();
        ApplicationStatus applicationStatus = new ApplicationStatus(1);
        String json = gson.toJson(applicationStatus);//here we put Object with status
//   2 solution:
//   return new ResponseEntity<>("OK", HttpStatus.OK); //ctrl+B we can go to the definition of class HttpStatus
//   3 solution -> pattern builder:

        return ResponseEntity
                .status(200)
                .header("Content-Type","application/json;charset=utf-8")
                .body(json);
    }
}
