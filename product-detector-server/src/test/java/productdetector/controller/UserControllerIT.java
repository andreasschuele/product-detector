package productdetector.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import productdetector.payload.*;
import productdetector.repository.UserRepository;
import productdetector.filter.JwtAuthenticationFilter;
import unittest.ClearDatabaseAfterTestClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.JsonUtils.jsonToObj;
import static utils.JsonUtils.objToJson;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
@ClearDatabaseAfterTestClass
public class UserControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilter(jwtAuthenticationFilter)
                .build();
    }

    private JwtAuthenticationResponseDto obtainSessionToken(String username, String password) throws Exception {
        SigninRequestDto signinRequest = new SigninRequestDto() {{
            setUsernameOrEmail(username);
            setPassword(password);
        }};

        MvcResult result = mockMvc.perform(post("/api/user/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objToJson(signinRequest).get()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        return jsonToObj(result.getResponse().getContentAsString(), JwtAuthenticationResponseDto.class).get();
    }

    @Test
    public void testUserSignin() throws Exception {
        JwtAuthenticationResponseDto tokenDto = obtainSessionToken("test1", "secret");

        assertNotNull(tokenDto.getAccessToken());
    }

}
