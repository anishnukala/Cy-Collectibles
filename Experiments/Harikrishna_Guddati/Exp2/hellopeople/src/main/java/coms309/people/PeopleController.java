package coms309.people;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  ArrayList<Person> getAllPersons(@RequestParam(required=false) String order,
                                            @RequestParam(required=false) String firstName,
                                            @RequestParam(required=false) String lastName,
                                            @RequestParam(required=false) String address,
                                            @RequestParam(required=false) String telephone) {
        ArrayList<Person> resultList = fullFilter(firstName,lastName,address,telephone);
        if(order==null){
            return resultList;
        }
        else if(order.equals("ds")){
            resultList.sort(Collections.reverseOrder());
        }
        else if(order.equals("as")){
            resultList.sort(null);
        }
        else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid mode value"
            );
        }

        return resultList;

    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public  String createPerson(@RequestBody ArrayList<Person> persons) {

        for(Person p:persons){
            peopleList.put(p.getFirstName(), p);
        }
        return "Saved people";

        //public  ResponseEntity<Map<String, String>>  //unused
        // createPerson(@RequestBody Person person) { // unused
        //Map <String, String> body = new HashMap<>();// unused
        //body.put("message", s); // unused
        //ResponseEntity<>(body, HttpStatus.OK); // unused
    }
    @PatchMapping("/people") public ArrayList<Person> patchPeople(@RequestBody Person patchInfo,
                                                                  @RequestParam(required=false) String firstName,
                                                                  @RequestParam(required=false) String lastName,
                                                                  @RequestParam(required=false) String address,
                                                                  @RequestParam(required=false) String telephone){
        ArrayList<Person> resultList = fullFilter(firstName,lastName,address,telephone);
        for(Person g:resultList){
            patchPerson(g,patchInfo);
        }
        return resultList;
    }
    @DeleteMapping("/people")
    public  ArrayList<Person> deleteByParamsFull(@RequestParam(required=false) String firstName,
                                             @RequestParam(required=false) String lastName,
                                             @RequestParam(required=false) String address,
                                             @RequestParam(required=false) String telephone){
        ArrayList<Person> resultList = fullFilter(firstName,lastName,address,telephone);
        for(Person g:resultList){
            peopleList.remove(g.getFirstName());
        }
        resultList=fullFilter(null,null,null,null);
        return resultList;
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public Person getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/people/contains")
    public ArrayList<Person> getPersonByParam(@RequestParam(required=false) String firstName,
                                              @RequestParam(required=false) String lastName,
                                              @RequestParam(required=false) String address,
                                              @RequestParam(required=false) String telephone) {
        return partialFilter(firstName,lastName,address,telephone);
    }

    @PatchMapping("/people/contains")
    public ArrayList<Person> patchByParam(@RequestBody Person patchInfo,
            @RequestParam(required=false) String firstName,
            @RequestParam(required=false) String lastName,
            @RequestParam(required=false) String address,
            @RequestParam(required=false) String telephone
    ){
        ArrayList<Person> resultList = partialFilter(firstName,lastName,address,telephone);
        for(Person g:resultList){
            patchPerson(g,patchInfo);
        }
        return resultList;
    }

    @DeleteMapping("/people/contains")
    public  ArrayList<Person> deleteByParamsPartial(@RequestParam(required=false) String firstName,
                                                 @RequestParam(required=false) String lastName,
                                                 @RequestParam(required=false) String address,
                                                 @RequestParam(required=false) String telephone){
        ArrayList<Person> resultList = partialFilter(firstName,lastName,address,telephone);
        for(Person g:resultList){
            peopleList.remove(g.getFirstName());
        }
        resultList=fullFilter(null,null,null,null);
        return resultList;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public Person updatePerson(@PathVariable String firstName, @RequestBody Person p) {
        peopleList.replace(firstName, p);
        return peopleList.get(firstName);
    }

    @PatchMapping("/people/{firstName}")
    public Person updateByFieldPerson(@PathVariable String firstName, @RequestBody Person patchInfo) {
        Person personToBeEdited=peopleList.get(firstName);
        patchPerson(personToBeEdited,patchInfo);
        return personToBeEdited;
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public ArrayList<Person> deletePerson(@PathVariable String firstName) {
        peopleList.remove(firstName);
        return fullFilter(null,null,null,null);
    }

    private ArrayList<Person> partialFilter(String firstName,
                                            String lastName,
                                            String address,
                                            String telephone){
        ArrayList<Person> resultList = new ArrayList<>();
        if(firstName==null && lastName == null && address==null && telephone==null){
            resultList = new ArrayList<>(peopleList.values());
        }
        else{
            resultList = new ArrayList<>();
            for (Person h:peopleList.values()){
                if(h.partialMatch(firstName,lastName,address,telephone)){
                    resultList.add(h);
                }
            }
        }
        return resultList;
    }

    private ArrayList<Person> fullFilter(String firstName,
                                         String lastName,
                                         String address,
                                         String telephone){
        ArrayList<Person> resultList ;
        if(firstName==null && lastName == null && address==null && telephone==null){
           resultList = new ArrayList<>(peopleList.values());
        }
        else{
            resultList = new ArrayList<>();
            for (Person h:peopleList.values()){
                if(h.fullMatch(firstName,lastName,address,telephone)){
                    resultList.add(h);
                }
            }
        }
        return resultList;
    }

    private void patchPerson(Person p,Person patchInfo){
        if(patchInfo.getFirstName()!=null){
            p.setFirstName(patchInfo.getFirstName());
        }
        if(patchInfo.getLastName()!=null){
            p.setLastName(patchInfo.getLastName());
        }
        if(patchInfo.getAddress()!=null){
            p.setAddress(patchInfo.getAddress());
        }
        if(patchInfo.getTelephone()!=null){
            p.setTelephone(patchInfo.getTelephone());
        }
    }
} // end of people controller

