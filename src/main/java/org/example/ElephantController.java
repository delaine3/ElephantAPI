package org.example;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
    "http://localhost:3000",
    "http://192.168.1.3:3000"
})
public class ElephantController {

    @GetMapping("/") 
    public String home() {
        return "The backend is running! 🐘";
    }
}
