/**
 * Created by szhernovoy on 02.12.2016.
 */
package ru.szhernovoy.controllers;

import ru.szhernovoy.model.DBManager;
import ru.szhernovoy.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class UserUpdate extends javax.servlet.http.HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DBManager.instance().updateItem(new User(req.getParameter("email"),req.getParameter("name"),req.getParameter("login"),System.currentTimeMillis(),req.getParameter("password")));
        doGet(req,resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession httpSession = req.getSession(false);
        synchronized (httpSession){
            if(Boolean.valueOf((String) httpSession.getAttribute("root")) == true){
                req.setAttribute("email",((User) httpSession.getAttribute("user")).getEmail());
            }else{
                req.setAttribute("users",DBManager.instance().getUsers());
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/Update.jsp").forward(req,resp);
    }
}
