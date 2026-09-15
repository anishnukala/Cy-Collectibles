package cycollectibles.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;

public class Helper {
    public static HashMap<String,String> errorMap(String key, String value){
        HashMap<String,String> response= new HashMap<>();
        response.put(key,value);
        return response;
    }

    public static ResponseEntity<HashMap<String,String>> validationErrors(BindingResult result){
        HashMap<String,String> response = new HashMap<>();
        for(FieldError fr : result.getFieldErrors()){
            response.put(fr.getField(), fr.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(response);
    }
}
