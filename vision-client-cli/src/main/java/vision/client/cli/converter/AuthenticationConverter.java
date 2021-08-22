package vision.client.cli.converter;

import vision.client.cli.model.Authentication;
import picocli.CommandLine;

public class AuthenticationConverter implements CommandLine.ITypeConverter<Authentication> {

    public Authentication convert(String value) throws Exception {
        if (value == null || value.length() == 0) {
            return null;
        }

        String[] usernamePassword = value.split(":");

        Authentication authentication = new Authentication();

        authentication.setUsername(usernamePassword[0]);
        authentication.setPassword(usernamePassword[1]);

        return authentication;
    }

}