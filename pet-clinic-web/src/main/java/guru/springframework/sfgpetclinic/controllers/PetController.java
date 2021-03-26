package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Pet;
import guru.springframework.sfgpetclinic.services.OwnerService;
import guru.springframework.sfgpetclinic.services.PetService;
import guru.springframework.sfgpetclinic.services.PetTypeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.Collection;

/**
 * Created by jt on 9/22/18.
 */
@Controller
@RequestMapping("/owners/{ownerId}")
public class PetController {

    private final PetService petService;
    private final OwnerService ownerService;
    private final PetTypeService petTypeService;

    public PetController(PetService petService, OwnerService ownerService, PetTypeService petTypeService) {
        this.petService = petService;
        this.ownerService = ownerService;
        this.petTypeService = petTypeService;
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/pets/{petId}/edit")
    public ResponseEntity<Collection<Pet>> getPets() {
        Collection<Pet> pets = this.petService.findAll();
        if (pets.isEmpty()) {
            return new ResponseEntity<Collection<Pet>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<Pet>>(pets, HttpStatus.OK);
    }

    @PostMapping("/pets/new")
    public ResponseEntity<Pet> addPet(@RequestBody @Valid Pet pet, BindingResult bindingResult,
                                      UriComponentsBuilder ucBuilder) {
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors() || (pet == null)) {
            return new ResponseEntity<Pet>(HttpStatus.BAD_REQUEST);
        }
        this.petService.save(pet);
        headers.setLocation(ucBuilder.path("/owners/{ownerId}/pets/new").buildAndExpand(pet.getId()).toUri());
        return new ResponseEntity<Pet>(pet, headers, HttpStatus.CREATED);
    }

    @PutMapping("/pets/{petId}/edit")
    public ResponseEntity<Pet> updatePet(@PathVariable("petId") Long petId, @RequestBody @Valid Pet pet,
                                         BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors() || (pet == null)) {
            return new ResponseEntity<Pet>(headers, HttpStatus.BAD_REQUEST);
        }
        Pet currentPet = this.petService.findById(petId);
        if (currentPet == null) {
            return new ResponseEntity<Pet>(HttpStatus.NOT_FOUND);
        }
        currentPet.setBirthDate(pet.getBirthDate());
        currentPet.setName(pet.getName());
        currentPet.setPetType(pet.getPetType());
        currentPet.setOwner(pet.getOwner());
        this.petService.save(currentPet);
        return new ResponseEntity<Pet>(currentPet, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable("petId") Long petId) {
        Pet pet = this.petService.findById(petId);
        if (pet == null) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }
        this.petService.delete(pet);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}