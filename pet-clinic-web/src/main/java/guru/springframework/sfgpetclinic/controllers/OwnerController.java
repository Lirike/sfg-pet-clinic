package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/owners")
@Controller
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping()
    public String processFindForm(Owner owner, BindingResult result, Model model) {
        //allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); //empty string signifies broadest possible search
        }

        //find owners by last name
        List<Owner> results = ownerService.findAllByLastNameLike("%" + owner.getLastName() + "%");

        if (results.isEmpty()) {
            //no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.get(0);
            return "redirect:/owners/" + owner.getId();
        } else {
            //multiple owners found
            model.addAttribute("selections", results);
            return "owners/ownersList";
        }

    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<Owner> getOwner(@PathVariable Long ownerId) {
        Owner owner = null;
        owner = this.ownerService.findById(ownerId);
        if (owner == null) {
            return new ResponseEntity<Owner>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Owner>(owner, HttpStatus.OK);
    }

    @PostMapping("/new")
    public ResponseEntity<Owner> addOwner(@RequestBody @Valid Owner owner, BindingResult bindingResult,
                                          UriComponentsBuilder ucBuilder) {
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors() || owner.getId() != null) {
            return new ResponseEntity<Owner>(headers, HttpStatus.BAD_REQUEST);
        }
        this.ownerService.save(owner);
        headers.setLocation(ucBuilder.path("/owners/{ownerId}").buildAndExpand(owner.getId()).toUri());
        return new ResponseEntity<Owner>(owner, headers, HttpStatus.CREATED);
    }


    @PutMapping("/{ownerId}/update")
    public ResponseEntity<Owner> updateOwner(@PathVariable("ownerId") Long ownerId, @RequestBody @Valid Owner owner,
                                             BindingResult bindingResult, UriComponentsBuilder ucBuilder) {
        boolean bodyIdMatchesPathId = owner.getId() == null || ownerId == owner.getId();
        if (bindingResult.hasErrors() || !bodyIdMatchesPathId) {
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Owner>(headers, HttpStatus.BAD_REQUEST);
        }
        Owner currentOwner = this.ownerService.findById(ownerId);
        if (currentOwner == null) {
            return new ResponseEntity<Owner>(HttpStatus.NOT_FOUND);
        }
        currentOwner.setAddress(owner.getAddress());
        currentOwner.setCity(owner.getCity());
        currentOwner.setFirstName(owner.getFirstName());
        currentOwner.setLastName(owner.getLastName());
        currentOwner.setTelephone(owner.getTelephone());
        this.ownerService.save(currentOwner);
        return new ResponseEntity<Owner>(currentOwner, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{ownerId}/delete")
    public ResponseEntity<Void> deleteOwner(@PathVariable("ownerId") Long ownerId) {
            Owner owner = this.ownerService.findById(ownerId);
            if (owner == null) {
                    return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            this.ownerService.delete(owner);
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        }

}
