package com.aaa.aaa;

public class MemberInfo {

    public String name;
    public int phone_number;


    //초기화
    public MemberInfo(String name, int phone_number){
        this.name=name;
        this.phone_number=phone_number;
    }

    //getter, setter
    public String getName(){
        return this.name;
    }

    public void setName(){
        this.name=name;
    }
    public int getPhone_number(){
        return this.phone_number;
    }

    public void setPhone_number(){
        this.phone_number=phone_number;
    }
}
