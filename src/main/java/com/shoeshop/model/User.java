package com.shoeshop.model;

public class User {

    private int    id;
    private String login;
    private String passwordHash;
    private int    roleId;
    private String roleName;     // заполняется JOIN-ом при выборке
    private String lastName;
    private String firstName;
    private String middleName;   // отчество, может быть null

    public User() {}

    // Полное ФИО: «Иванов Иван Иванович» (без отчества, если его нет)
    public String getFullName() {
        StringBuilder sb = new StringBuilder(lastName)
                .append(" ").append(firstName);
        if (middleName != null && !middleName.isBlank()) {
            sb.append(" ").append(middleName);
        }
        return sb.toString();
    }

    public int    getId()           { return id; }
    public String getLogin()        { return login; }
    public String getPasswordHash() { return passwordHash; }
    public int    getRoleId()       { return roleId; }
    public String getRoleName()     { return roleName; }
    public String getLastName()     { return lastName; }
    public String getFirstName()    { return firstName; }
    public String getMiddleName()   { return middleName; }

    public void setId(int id)                  { this.id           = id; }
    public void setLogin(String login)         { this.login        = login; }
    public void setPasswordHash(String hash)   { this.passwordHash = hash; }
    public void setRoleId(int roleId)          { this.roleId       = roleId; }
    public void setRoleName(String roleName)   { this.roleName     = roleName; }
    public void setLastName(String lastName)   { this.lastName     = lastName; }
    public void setFirstName(String firstName) { this.firstName    = firstName; }
    public void setMiddleName(String name)     { this.middleName   = name; }
}
