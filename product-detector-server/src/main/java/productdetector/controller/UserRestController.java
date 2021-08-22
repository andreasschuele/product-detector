package productdetector.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import productdetector.exception.ResourceNotFoundException;
import productdetector.model.*;
import productdetector.payload.*;
import productdetector.repository.UserRepository;
import productdetector.security.CurrentUser;
import productdetector.security.JwtTokenProvider;
import productdetector.security.UserPrincipal;

import javax.validation.Valid;
import java.net.URI;
import java.util.stream.Collectors;


/**
 * The UserRestController class implements user related REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<JwtAuthenticationResponseDto> signin(@Valid @RequestBody SigninRequestDto signinRequest) {
        // Authenticate.

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                signinRequest.getUsernameOrEmail(),
                signinRequest.getPassword()
            )
        );

        // Signin on server side.

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(signinRequest.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", signinRequest.getUsernameOrEmail()));


        JwtAuthenticationResponseDto responseDto = new JwtAuthenticationResponseDto();

        responseDto.setAccessToken(jwt);
        responseDto.setRole(user.getRoles().stream().map(e -> e.getName().name()).collect(Collectors.joining(";")));

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/signout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDto> signout(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser, @Valid @RequestBody SignoutRequestDto loginRequest) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Signout on server side.

        SecurityContextHolder.getContext().setAuthentication(null);

        // Redirect

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/")
                .build()
                .toUri();

        return ResponseEntity.created(location).body(new ApiResponseDto(true, "User logged out."));
    }

}
