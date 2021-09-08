package com.sean.controller;

import com.sean.service.UserService;
import com.sean.springmvc.annotation.AutoWired;
import com.sean.springmvc.annotation.Controller;
import com.sean.springmvc.annotation.RequestMapping;
import com.sean.springmvc.annotation.ResponseBody;

import java.util.List;

/**
 * @author yunfei_li@qq.com
 * @date 2021年09月08日 10:04
 */
@Controller
public class UserController {

    @AutoWired(value = "userService")
    UserService userService;

    @RequestMapping("/find/users/page")
    public String findPageAllUsers() {
        System.out.println("=======UserController====findPageAllUsers()....");
        return "../../success.jsp";
    }

    @RequestMapping("/find/all")
    @ResponseBody
    public List findAllUsers() {
        return userService.findAllUsers();
    }
}
