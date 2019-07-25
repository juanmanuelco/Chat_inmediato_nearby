package com.facci.chatinmediato.Entities;

public class Usuario {
    private  String userMac;
    private  String userName ;
    private  boolean userEstado ;

    public Usuario() {

    }

    public String getUserMac(){
        return userMac;
    }

    public String getUserName(){
        return userName;
    }
    public boolean getUserEstado(){
        return userEstado;
    }

    public void setUserMac(String userMac){
        this.userMac = userMac;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setUserEstado(boolean userEstado){
        this.userEstado = userEstado;
    }
}
