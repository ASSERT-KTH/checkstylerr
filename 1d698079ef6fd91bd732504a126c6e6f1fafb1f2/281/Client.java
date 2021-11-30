package com.hlj.moudle.design.D01_UML.D05_时序图;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Client
 * @date 2019/5/24  12:44.
 * @Description
 */
public class Client {
    Server server ;
    void work(){
        server.open();
        server.print("HealerJean");
        server.close();
    }
}
class  Server{
    Device device ;
    void open(){}
    void print(String s) {
        device.write(s);
    }
    void close(){}
}
class Device {
    void write(String s) {}
}
