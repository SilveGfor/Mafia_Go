package com.example.mafiago.models;

import com.example.mafiago.enums.Role;

public class RoleModel
{
    public Role role;
    public boolean peaceful;

    public RoleModel()
    {
    }

    public RoleModel(Role role, boolean peaceful) {
        this.role = role;
        this.peaceful = peaceful;
    }
}
