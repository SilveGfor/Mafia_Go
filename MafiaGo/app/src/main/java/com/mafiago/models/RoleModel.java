package com.mafiago.models;

import com.mafiago.enums.Role;

public class RoleModel
{
    public Role role;
    public boolean peaceful;
    public boolean visible;
    public int count;

    public RoleModel()
    {
    }

    //для отоюражения в create_room/create_custom_room
    public RoleModel(Role role, boolean peaceful) {
        this.role = role;
        this.peaceful = peaceful;
        visible = false;
    }

    public RoleModel(Role role, boolean peaceful, boolean visible) {
        this.role = role;
        this.peaceful = peaceful;
        this.visible = visible;
    }

    //для отображения внутри комнаты
    public RoleModel(Role role, int count) {
        this.role = role;
        this.count = count;
    }
}
