package com.sean.service.impl;

import com.sean.bean.User;
import com.sean.service.UserService;
import com.sean.springmvc.annotation.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yunfei_li@qq.com
 * @date 2021年09月08日 9:52
 */
@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Override
    public List findAllUsers() {
        List<User> users = new ArrayList<>();
        User user1 = new User("1", "张三", "F", 18);
        User user2 = new User("1", "李四", "F", 19);
        User user3 = new User("1", "王五", "F", 20);
        users.add(user1);
        users.add(user2);
        users.add(user3);
        return users;
    }

}
