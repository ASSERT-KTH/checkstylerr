package com.hlj.swagger.controller;

import com.hlj.swagger.bean.Base;
import com.hlj.swagger.bean.User;
import com.hlj.swagger.common.Response;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(value = "用户管理",description = "用户管理")
@RestController
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful — 请求已完成"),
        @ApiResponse(code = 400, message = "请求中有语法问题，或不能满足请求"),
        @ApiResponse(code = 401, message = "未授权客户机访问数据"),
        @ApiResponse(code = 404, message = "服务器找不到给定的资源；文档不存在"),
        @ApiResponse(code = 500, message = "服务器不能完成请求")}
)
@RequestMapping("/user")
public class UserController {

    Logger log = LoggerFactory.getLogger(UserController.class);

    @ApiOperation(value = "获取用户列表", notes = "根据url的id来获取用户详细信息，返回List<User>类型用户信息的JSON;")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID",defaultValue = "1", required = true, paramType = "query",dataType = "string")

    })
    @GetMapping("one")
    public List<User> getUserBagOne(String id) {
        List<User> users = new ArrayList<>();
        try {
            if (id.equals("1")) {
                users.add(new User("HealerJean", "1", 24, new Base(1)));
            } else {
                users.add(new User("huangliang", "2", 25, new Base(2)));
            }
            return users;
        } catch (Exception e) {
            return users;
        }

    }



    @ApiOperation(value = "获取用户列表",notes = "根据url的id来获取用户详细信息，返回List<User>类型用户信息的JSON;",response = User.class,responseContainer = "List",produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "query",dataType = "string")

    })
    @GetMapping("two")
    public Response<?> getUserBagTwo(String id){
        List<User> users = new ArrayList<>();
        try {
            if(id.equals("1")) {
                users.add(new User("HealerJean", "1", 24, new Base(1)));
            }else {
                users.add(new User("huangliang", "2", 25, new Base(2)));
            }
            return Response.success(users);
        }catch (Exception e){
            return Response.error();
        }

    }


    @ApiOperation(value = "根据id获取用户详细信息", notes = "根据url的id来获取用户详细信息")
    //描述容器
    @ApiImplicitParam(name = "id", value = "用户ID", required = true,  paramType = "path",dataType = "long",dataTypeClass = Long.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUser(String id) {
        return new User("HealerJean", id, 24, new Base(1));
    }


    @ApiOperation(value = "创建用户", notes = "根据User对象创建用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User",dataTypeClass = User.class),
            @ApiImplicitParam(name = "flag", value = "是否开启标志位", paramType = "query", dataType = "boolean"),
            @ApiImplicitParam(name = "version", value = "版本号", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "taobao", value = "是否淘宝", required = true, paramType = "query", dataType = "boolean"),

    })
    @RequestMapping(value = "", method = RequestMethod.POST)
    public User postUser(User user, @RequestParam(defaultValue = "false") boolean flag, String version) {
        log.info(flag+"");
        log.info(version);
        return user;
    }




    @ApiOperation(value = "testUser", notes = "testUser")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public User testUser(User user) {
        log.info(user.toString());
        return user ;
    }



}