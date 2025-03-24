package com.kompetencyjny.EventBuddySpring;

import com.kompetencyjny.EventBuddySpring.model.Person;
import com.kompetencyjny.EventBuddySpring.repo.PersonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

    private final PersonRepo repo;

    public PersonController(PersonRepo repo) {
        this.repo = repo;
    }

   @PostMapping("/addPerson")
    public void addPerson(@RequestBody Person person) {
        repo.save(person);
   }

}
