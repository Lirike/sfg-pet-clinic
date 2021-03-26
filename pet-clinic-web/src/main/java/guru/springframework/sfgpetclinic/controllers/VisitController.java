package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Visit;
import guru.springframework.sfgpetclinic.services.PetService;
import guru.springframework.sfgpetclinic.services.VisitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jt on 2018-09-27.
 */
@Controller
@RequestMapping("visits")
public class VisitController {

    private final VisitService visitService;
    private final PetService petService;

    public VisitController(VisitService visitService, PetService petService) {
        this.visitService = visitService;
        this.petService = petService;
    }

    @InitBinder
    public void dataBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");

        dataBinder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException{
                setValue(LocalDate.parse(text));
            }
        });
    }


    @GetMapping()
    public ResponseEntity<Collection<Visit>> getAllVisits(){
        Collection<Visit> visits = new ArrayList<Visit>();
        visits.addAll(this.visitService.findAll());
        if (visits.isEmpty()){
            return new ResponseEntity<Collection<Visit>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<Visit>>(visits, HttpStatus.OK);
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<Visit> getVisit(@PathVariable("visitId") Long visitId){
        Visit visit = this.visitService.findById(visitId);
        if(visit == null){
            return new ResponseEntity<Visit>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Visit>(visit, HttpStatus.OK);
    }

    @PostMapping("/new")
    public ResponseEntity<Visit> addVisit(@RequestBody @Valid Visit visit, BindingResult bindingResult, UriComponentsBuilder ucBuilder){
        HttpHeaders headers = new HttpHeaders();
        if(bindingResult.hasErrors() || (visit == null) || (visit.getPet() == null)){
            return new ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST);
        }
        this.visitService.save(visit);
        headers.setLocation(ucBuilder.path("/visits/{visitId}").buildAndExpand(visit.getId()).toUri());
        return new ResponseEntity<Visit>(visit, headers, HttpStatus.CREATED);
    }

    @PutMapping("/{visitId}")
    public ResponseEntity<Visit> updateVisit(@PathVariable("visitId") Long visitId, @RequestBody @Valid Visit visit, BindingResult bindingResult){
        HttpHeaders headers = new HttpHeaders();
        if(bindingResult.hasErrors() || (visit == null) || (visit.getPet() == null)){
            return new ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST);
        }
        Visit currentVisit = this.visitService.findById(visitId);
        if(currentVisit == null){
            return new ResponseEntity<Visit>(HttpStatus.NOT_FOUND);
        }
        currentVisit.setDate(visit.getDate());
        currentVisit.setDescription(visit.getDescription());
        currentVisit.setPet(visit.getPet());
        this.visitService.save(currentVisit);
        return new ResponseEntity<Visit>(currentVisit, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{visitId}")
    public ResponseEntity<Void> deleteVisit(@PathVariable("visitId") Long visitId){
        Visit visit = this.visitService.findById(visitId);
        if(visit == null){
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        this.visitService.delete(visit);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}