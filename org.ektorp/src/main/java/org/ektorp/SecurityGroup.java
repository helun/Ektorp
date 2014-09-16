package org.ektorp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author n3integration
 */
public class SecurityGroup implements Serializable {

    private static final long serialVersionUID = -346108257321474838L;

    private final List<String> names;
    private final List<String> roles;

    public SecurityGroup() {
        this.names = new ArrayList<String>();
        this.roles = new ArrayList<String>();
    }

    @JsonCreator
    public SecurityGroup(@JsonProperty("names") List<String> names, @JsonProperty("roles") List<String> roles) {
        this.names = names;
        this.roles = roles;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getRoles() {
        return roles;
    }
}
