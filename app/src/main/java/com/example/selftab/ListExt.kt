package com.example.selftab

fun MutableList<Int>.getObj(index:Int):Int{
    if (index > this.size-1){
        return this[this.size-1]
    }else{
        return this[index]
    }
}