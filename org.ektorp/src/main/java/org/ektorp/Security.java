package org.ektorp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author n3integration
 */
public class Security implements Serializable {

    private static final long serialVersionUID = -255108257321474837L;

    private final SecurityGroup admins;
    private final SecurityGroup members;

    public Security() {
        admins = new SecurityGroup();
        members = new SecurityGroup();
    }

    @JsonCreator
    public Security(@JsonProperty("admins") SecurityGroup admins, @JsonProperty("members") SecurityGroup members) {
        this.admins = admins;
        this.members = members;
    }

    public SecurityGroup getAdmins() {
        return admins;
    }

    public SecurityGroup getMembers() {
        return members;
    }
}
