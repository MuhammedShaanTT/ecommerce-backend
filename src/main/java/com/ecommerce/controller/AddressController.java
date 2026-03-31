package com.ecommerce.controller;

import com.ecommerce.dto.auth.AddressRequest;
import com.ecommerce.dto.auth.AddressResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .isDefault(address.isDefault())
                .build();
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        User user = getLoggedInUser();
        List<AddressResponse> addresses = addressRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody AddressRequest request) {
        User user = getLoggedInUser();
        
        // If it's the first address or marked as default, handle defaults
        boolean isFirst = addressRepository.findByUser(user).isEmpty();
        boolean shouldBeDefault = isFirst || request.isDefault();

        if (shouldBeDefault) {
            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(oldDefault -> {
                oldDefault.setDefault(false);
                addressRepository.save(oldDefault);
            });
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isDefault(shouldBeDefault)
                .build();

        return ResponseEntity.ok(mapToResponse(addressRepository.save(address)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long id, @RequestBody AddressRequest request) {
        User user = getLoggedInUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(oldDefault -> {
                oldDefault.setDefault(false);
                addressRepository.save(oldDefault);
            });
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        if (request.isDefault()) {
            address.setDefault(true);
        }

        return ResponseEntity.ok(mapToResponse(addressRepository.save(address)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        User user = getLoggedInUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (address.isDefault()) {
            // Find another address to make default
            addressRepository.findByUser(user).stream()
                    .filter(a -> !a.getId().equals(id))
                    .findFirst()
                    .ifPresent(newDefault -> {
                        newDefault.setDefault(true);
                        addressRepository.save(newDefault);
                    });
        }

        addressRepository.delete(address);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long id) {
        User user = getLoggedInUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(oldDefault -> {
            oldDefault.setDefault(false);
            addressRepository.save(oldDefault);
        });

        address.setDefault(true);
        return ResponseEntity.ok(mapToResponse(addressRepository.save(address)));
    }
}
